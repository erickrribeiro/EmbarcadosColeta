package com.eribeiro.embarcadoscoleta.servidor;

import android.os.Environment;
import android.util.Log;
import com.eribeiro.embarcadoscoleta.MainActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

/**
 * Created by eribeiro on 09/09/15.
 * email: erick.ribeiro.16@gmail.com
 */

public class FileLogManager {
    private FileOutputStream arqCompleto;
    private String experimento;
    private String label;
    private File diretorioCompleto;

    public FileLogManager(String Label){
        this.label = Label;

        Calendar calendario = Calendar.getInstance();
        int ano = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH) + 1;
        int dia = calendario.get(Calendar.DAY_OF_MONTH);
        int hora = calendario.get(Calendar.HOUR_OF_DAY);
        int minuto = calendario.get(Calendar.MINUTE);
        int segundos = calendario.get(Calendar.SECOND);

        experimento = String.valueOf(ano) + "_" + String.valueOf(mes) + "_" + String.valueOf(dia) + "_" +
                String.valueOf(hora) + "-" + String.valueOf(minuto) + "-" + String.valueOf(segundos);

    }
//CRIA ARQUIVO .CSV COMO TODOS OS SENSORES
    public void criaLogsComTodosSensores(){
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Sensors_log");
        Log.d("PATH", f.getAbsolutePath());
        if (!f.exists()) {
            Log.d("MAKE DIR", f.mkdirs() + "");
        }


        try {
            String nomeArquivo = label+"_" + experimento + ".csv";

            MainActivity.nomeArquivo = nomeArquivo;
            MainActivity.rotulo = label;

            diretorioCompleto = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Sensors_log"), nomeArquivo);
            arqCompleto = new FileOutputStream(diretorioCompleto, true);

            //Log.d(TAG, "Todos os sensores criado com sucesso\n");

            String colunas = "x1, y1, z1, orientacao1, x2, y2, z2, orientacao2, Label \n";
            arqCompleto.write(colunas.getBytes());
            arqCompleto.flush();

        } catch (Exception e) {
            Log.d("ERRO", "erro em criar o arquivo completo: " + e.getMessage());
        }
    }

    public void insereValoresTodosSensores(String linha){
        try {
            this.arqCompleto.write(linha.getBytes());
            this.arqCompleto.flush();
        }catch (Exception e){
            Log.d("ERRO", "erro em criar o arquivo completo: " + e.getMessage());
        }
    }


    public void fechaLogSensor(){
        try {
            this.arqCompleto.close();
        }catch (Exception e){
            Log.d("ERRO", "erro ao fechar o arquivo completo: " + e.getMessage());
        }
    }

}

