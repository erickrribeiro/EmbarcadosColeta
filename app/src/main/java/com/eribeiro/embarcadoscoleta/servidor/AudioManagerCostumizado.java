package com.eribeiro.embarcadoscoleta.servidor;

import android.app.Activity;
import android.app.NotificationManager;

import android.content.Context;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.eribeiro.embarcadoscoleta.R;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by eribeiro on 28/06/15.
 * email: erick.ribeiro.16@gmail.com
 */

public class AudioManagerCostumizado {

    private final String urlServer;
    int serverResponseCode = 0;
    private Activity context;

    public static String IP = "http://gise.icomp.ufam.edu.br";
    //public static String IP = "http://192.168.1.136";
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int id=0;

    public AudioManagerCostumizado(Activity context) {
        this.context = context;
        this.urlServer = IP+"/embarcados/logs/upload.php";
        //this.urlServer = IP+"/embarcados/upload.php";
    }

    public static int code;
    public boolean sendAudioToServer(final String nome){
        mNotifyManager =  (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context.getApplicationContext());
        mBuilder.setContentTitle("Envio dos dados")
                .setContentText("Upload em progresso.")
                .setSmallIcon(R.drawable.app_sensores);


        new Thread(new Runnable() {
            public void run() {
                    AudioManagerCostumizado.code = uploadFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+ "/Sensors_log/"+nome);
            }
        }).start();

        return (code== 200)? true: false;
    }

    private int uploadFile(String urlLocal){
        int incr=0;
        mBuilder.setProgress(100, incr+=1, false);
        mNotifyManager.notify(id, mBuilder.build());

        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 2 * 1024 * 1024;
        File sourceFile = new File(urlLocal);

        mBuilder.setProgress(100, incr+=5, false);
        mNotifyManager.notify(id, mBuilder.build());

        if (!sourceFile.isFile()) {

            //dialog.dismiss();

            Log.e("uploadFile", "Source File not exist :"
                    +urlLocal);
            return 0;

        }
        else
        {
            try {
                mBuilder.setProgress(100, incr+=10, false);
                mNotifyManager.notify(id, mBuilder.build());

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(this.urlServer);


                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", urlLocal);

                mBuilder.setProgress(100, incr+=20, false);
                mNotifyManager.notify(id, mBuilder.build());

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name='uploaded_file';filename= '"
                        + urlLocal + "' " + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                mBuilder.setProgress(100, incr+=30, false);
                mNotifyManager.notify(id, mBuilder.build());

                Log.e("PROGRESSBAR", "BUFFERSIZE: "+bufferSize);
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                Log.e("PROGRESSBAR", "BUFFERSIZE: "+bufferSize);



                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    Log.e("PROGRESSBAR", "BYREAD: "+bytesRead);
                    Log.e("PROGRESSBAR", "BUFFERSIZE: "+bufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);


                    mBuilder.setProgress(bufferSize, incr+=5, false);
                    // Displays the progress bar for the first time.
                    mNotifyManager.notify(id, mBuilder.build());
                    Thread.sleep(5 * 1000);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                Log.d("Responde Message: ",conn.getContent().toString());
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);


                if(serverResponseCode == 200){
                    Log.d("Servidor","File Upload Completed.\n\n See uploaded file here : \n\n"
                            + IP+"/progweb/uploads/" );
                }
                mBuilder.setContentText("Upload completo")
                        .setProgress(100, 100, false);
                mNotifyManager.notify(id, mBuilder.build());

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                ex.printStackTrace();

                mBuilder.setContentText("Não foi possivel Enviar")
                        .setProgress(0, 0, false);
                mNotifyManager.notify(id, mBuilder.build());

                Log.e("MalformedURLException", "check script url");
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                e.printStackTrace();

                Log.e("Upload server Exception", "Exception : "+ e.getMessage());

                mBuilder.setContentText("Não foi possivel Enviar")
                        .setProgress(0, 0, false);
                mNotifyManager.notify(id, mBuilder.build());
            }


            return serverResponseCode;

        } // End else block
    }
}
