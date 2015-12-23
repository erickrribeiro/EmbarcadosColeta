package com.eribeiro.embarcadoscoleta;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.eribeiro.embarcadoscoleta.servidor.AudioManagerCostumizado;
import com.eribeiro.embarcadoscoleta.servidor.Dado;
import com.eribeiro.embarcadoscoleta.servidor.FileLogManager;
import com.eribeiro.embarcadoscoleta.servidor.HttpConnection;
import com.eribeiro.embarcadoscoleta.sqlite.DadoDAO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity:";
    public static String nomeArquivo;
    public static String rotulo;

    private ImageView buttonIniciaServico;
    private ImageView buttonParaServico;
    private Spinner spinner2;

    /**
     * Representa dispositivo bluetooth do aparelho.
     */
    private BluetoothAdapter mBluetoothAdapter;

    /**
     * Porta de comunicação bluetooth com outros dispositivos.
     */
    private BluetoothSocket mBluetoothSocket;

    /**
     * Representa um dispositivo bluetooth remoto, permite a conexão ou a aquisição de dados do aparelho.
     */
    private BluetoothDevice mBluetoothDevice;

    /**
     * Opositalmente ao fluxo de entrada, o fluxo de saída é o meio pelo qual o soquete envia dados para um dispositivo remoto.
     */
    private OutputStream escritor;

    /**
     * Opositalmente ao fluxo de saída, o fluxo de entrada é o meio pelo qual o soquete recebe dados para um dispositivo remoto.
     */
    private InputStream leitor;

    /**
     * Responsável por informar se o dispositivo está conectado a uma Bluetooth ou não.
     */

    private boolean conectado = false;

    /**
     * Armazena o nome do Bluetooth
     */
    private static final String NOMEBLUETOOTH = "EPA07";

    private static final String MENSAGEM_ATIVACAO = "1";
    private static final String MENSAGEM_DESATIVACAO = "0";

    private TextView textViewStatus;
    private TextView textViewDado;

    public Thread workerThread;
    public byte[] readBuffer;
    public int readBufferPosition;
    volatile boolean stopWorker;

    /**
     * Variável utilizada na alteração do UI dentro do Runable.
     */

    public static Activity activity;
    public static FileLogManager fileLogManager;
    private String data;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        setContentView(R.layout.activity_main);

        /**
         * Artimanha utilizada para fazer como que seja possivível alterar a UI dentro de uma Runable.
         * (Existem outras formas melhores)
         */
        this.activity = this;

        this.textViewStatus = (TextView) findViewById(R.id.txt_conectado);
        this.textViewStatus.setText(R.string.txt_status_desconectado);

        this.textViewDado = (TextView) findViewById(R.id.txt_dado);
        this.textViewDado.setText(R.string.txt_campo_dado_padrao);

        //*****************************************************************************************
        this.buttonIniciaServico = (ImageView) findViewById(R.id.play);

        this.buttonParaServico = (ImageView) findViewById(R.id.stopService);
        this.spinner2 = (Spinner) findViewById(R.id.spinner1);

        this.buttonIniciaServico.setOnClickListener(this);
        this.buttonParaServico.setOnClickListener(this);

        this.buttonParaServico.setEnabled(false);

        Log.d(TAG, "onCreate completo");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(preferences.getBoolean("prefAtivo", false)) {
            updateUI(true);
        }else{
            updateUI(false);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            Toast.makeText(this, R.string.txt_nao_tem_bluetooth, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, R.string.txt_tem_bluetooth, Toast.LENGTH_SHORT).show();

            /**
             * Caso o Bluetooth não esteja habilitado aparece a caixinha pedindo que o habilite.
             */

            if (!mBluetoothAdapter.isEnabled()){
                Intent habilitarBluet = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(habilitarBluet, 1);
            } else {
                //sgetPairedDevices();
                mBluetoothAdapter.startDiscovery();
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Cria o menu, e adiciona os itens no action bar, se ele estiver visivél.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id== R.id.configuracoes){
            startActivity(new Intent(this, SettingsActivity.class));
        }
        if(id==R.id.historico){
            startActivity(new Intent(this, HistoricoActivity.class));
        }

        if (id == R.id.action_connect) {
            try {
                this.conectarComBluetoohArduino();
            }catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.txt_nao_conectou, Toast.LENGTH_LONG).show();
            }

            if (conectado) {
                Log.d("EU", getString(R.string.txt_status_conectado));
                sendMessage(MENSAGEM_ATIVACAO);
                beginListenForData();
            }
        }

        if (id == R.id.action_disconnect) {
            desconectarBluetoothDoArduino();
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean desconectarBluetoothDoArduino() {
        try {
            sendMessage(MENSAGEM_DESATIVACAO);
            this.conectado = false;
            mBluetoothSocket.close();
            this.stopWorker = true;
            Thread.currentThread().interrupt();
            this.leitor.close();
            this.textViewStatus.setText(R.string.txt_status_desconectado);
            recreate();
            return true;
        }catch (Exception e) {
            return false;
        }

    }

    public boolean conectarComBluetoohArduino() {
        buscarBluetoothArduino(NOMEBLUETOOTH);

        try {
            Log.d("EU", "Conectando com o dispositivo...");
            mBluetoothAdapter.cancelDiscovery();
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            mBluetoothSocket.connect();

            this.escritor = mBluetoothSocket.getOutputStream();
            this.leitor = mBluetoothSocket.getInputStream();

            this.conectado = true;
            this.textViewStatus.setText("Conectado a " + NOMEBLUETOOTH);

            Toast.makeText(getApplicationContext(), "Conectado a " + NOMEBLUETOOTH, Toast.LENGTH_LONG).show();
            //Connect();
            return true;
        } catch (Exception e) {
            Log.d("ERROR", "" + e.getMessage());
            Toast.makeText(getApplicationContext(), "Não foi possivel conectar", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    private void buscarBluetoothArduino(String nomeBluetooth){
        boolean bluetoothEncontrado = false;

        try {
            Set<BluetoothDevice> paired = mBluetoothAdapter.getBondedDevices();
            if (paired.size() > 0) {
                for (BluetoothDevice d : paired) {
                    if (d.getName().equals(nomeBluetooth)) {
                        this.mBluetoothDevice = d;
                        bluetoothEncontrado = true;
                        break;
                    }
                }
            }

            if (!bluetoothEncontrado) {
                Toast.makeText(getApplicationContext(), R.string.txt_toast_erro_bluetooth_pareamento, Toast.LENGTH_LONG).show();
                Log.d("Erro Dispositivo", getString(R.string.txt_toast_erro_bluetooth_pareamento));
            }

        } catch (Exception e) {
            Log.d("Erro no Bluetooth", getString(R.string.txt_erro_criar_conexao) + e.getMessage());
        }

    }

    public void sendMessage(String msg){
        try {
            if (conectado) {
                escritor.write(msg.getBytes());
            }

        } catch (Exception e){
            Toast.makeText(getApplicationContext(), "Não foi possivel conectar", Toast.LENGTH_LONG).show();
            Log.d("Error while sendMessa: ", e.getMessage());
        }
    }

    void beginListenForData(){
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = leitor.available();

                        if(bytesAvailable > 0)
                        {

                            byte[] packetBytes = new byte[bytesAvailable];

                            leitor.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {

                                            //TextView textView = (TextView) MainActivity.activity.findViewById(R.id.txt_dado);
                                            //textView.setText(data);
                                            //Log.d("Teste", data);
                                            String linha = data.replace("\n", "")+ ", " +MainActivity.rotulo+ "\n";
                                            try {

                                                fileLogManager.insereValoresTodosSensores(linha);

                                            }catch(Exception e){
                                                Log.d("ERRO", "erro em escrever acelerometro: " + e.getMessage());
                                            }

                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                        //   workerThread.sleep(400);
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }
    //****************************************************************************************
    public void updateUI(boolean opcao){
        if(opcao){
            this.buttonIniciaServico.setEnabled(false);
            this.buttonIniciaServico.setVisibility(View.GONE);

            this.buttonParaServico.setEnabled(true);
            this.buttonParaServico.setVisibility(View.VISIBLE);
            this.spinner2.setEnabled(false);
        }else{
            this.buttonParaServico.setEnabled(false);
            this.buttonParaServico.setVisibility(View.GONE);

            this.buttonIniciaServico.setEnabled(true);
            this.buttonIniciaServico.setVisibility(View.VISIBLE);

            this.spinner2.setEnabled(true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.play:
                if(conectado) {
                    editor = sharedPreferences.edit();
                    editor.putBoolean("prefAtivo", true);
                    editor.commit();

                    /**
                     * inicia conexao com os dados do Sensor
                     */

                    try {
                        this.conectarComBluetoohArduino();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), R.string.txt_nao_conectou, Toast.LENGTH_LONG).show();
                    }

                    if (conectado) {
                        Log.d("EU", getString(R.string.txt_status_conectado));
                        sendMessage(MENSAGEM_ATIVACAO);
                        beginListenForData();
                    }

                    //----------------------------------------------------------------------------------

                    Toast.makeText(getApplicationContext(), "SERVIÇO INICIADO", Toast.LENGTH_LONG).show();
                    updateUI(true);

                    PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
                    wakeLock.acquire();

                    /**
                     * Inicializa as funções para savar o log em um arquivo.
                     */
                    File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Sensors_log");
                    Log.d("PATH", f.getAbsolutePath());

                    if (!f.exists()) {
                        Log.d("MAKE DIR", f.mkdirs() + "");
                    }

                    fileLogManager = new FileLogManager(spinner2.getSelectedItem().toString());
                    fileLogManager.criaLogsComTodosSensores();
                    rotulo = spinner2.getSelectedItem().toString();

                    //Log.d(TAG, "Coleta Iniciada");
                }else{
                    Toast.makeText(this, "Verifique a conexão com o Bluetooth", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.stopService:
                fileLogManager.fechaLogSensor();
                desconectarBluetoothDoArduino();

                /**
                 * Finaliza conexao os dados do Sensor
                 */

                editor = sharedPreferences.edit();
                editor.putBoolean("prefAtivo", false);
                editor.commit();

                //Log.d(TAG, "Coleta finalizada.");

                updateUI(false);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

                if(preferences.getBoolean("checkBoxEnviarDadosPref", false)) {
                    Log.d(TAG, "Envio ao servidor ativado.");
                    AudioManagerCostumizado costumizado = new AudioManagerCostumizado(this);
                    if(costumizado.sendAudioToServer(MainActivity.nomeArquivo)) {
                        Dado dado = new Dado(rotulo, nomeArquivo, this);
                        Log.d("Dados enviados", dado.toString());
                        HttpConnection.sendJson(dado);
                    }
                    else{
                        /**
                         * Todas as informações que seriam enviadas para o servidor, agora serão
                         * salvas de forma local.
                         *
                         * - Log.cvs (path)
                         * informações do autor
                         */
                        //Log.d(TAG, "Envio ao servidor desativado.");

                        Dado dado = new Dado(rotulo, nomeArquivo, this);
                        DadoDAO dadoDAO = new DadoDAO(this);

                        if(dadoDAO.insert(dado)){
                            Toast.makeText(this, "Os Dados foram salvos localmente", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(this, "ERRO!, Os Dados não foram salvos localmente", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else{
                    /**
                     * Todas as informações que seriam enviadas para o servidor, agora serão
                     * salvas de forma local.
                     *
                     * - Log.cvs (path)
                     * informações do autor
                     */
                    Log.d(TAG, "Envio ao servidor desativado.");

                    Dado dado = new Dado(rotulo, nomeArquivo, this);
                    DadoDAO dadoDAO = new DadoDAO(this);

                    if(dadoDAO.insert(dado)){
                        Toast.makeText(this, "Os Dados foram salvos localmente", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(this, "ERRO!, Os Dados não foram salvos localmente", Toast.LENGTH_LONG).show();
                    }
                }

                break;

        }
    }
}
