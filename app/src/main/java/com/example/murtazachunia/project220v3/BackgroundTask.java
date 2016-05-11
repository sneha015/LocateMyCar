package com.example.murtazachunia.project220v3;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 This class is responsible to carry out activity in a separate thread.
 It gets parameters from the mainactivity. Object of this class is created in the
 mainactivity. When myCar button is pressed object of this is called in the onUpdate
 method of the UpdataDatabaseTask. UpdataDatabase class gets the information of the
 stored ble device from the database of the app in the phone. This information is than passed
 to object of this class. This class connects to the central server which has the complete
 floormap information of the place to which the ble device belongs too. It gets the complete
 floormap information in the json format from the central server.
 This class provides an interface which provides a mechanism to communicate back with the class which created
 this class's object once its task is completed. Class which creates this class's object have to implement
 the interface's method.
 */



public class BackgroundTask extends AsyncTask<String,Void,String> {
    Context ctx;
    public interface OnUpdateListener{
        public void onUpdateBackground(String obj);
    }

    OnUpdateListener listener;

    public void setUpdateListener(OnUpdateListener listener){
        this.listener = listener;
    }

    BackgroundTask(Context ctx){
        this.ctx = ctx;
    }

    protected void onPreExecute(){
        super.onPreExecute();
    }

    protected void onPostExecute(String obj){
        if(listener != null)
            listener.onUpdateBackground(obj);
    }

    protected void onProgressUpdate(){
        super.onProgressUpdate();
    }

    @Override
    protected String doInBackground(String... params) {

        String method = params[0];
        if(method.equals("ble_info")){
            String device_addr = params[1];
            try{
                String check_url = "http://192.168.0.3/android/check.php";
                URL url = new URL(check_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String data = URLEncoder.encode("address","UTF-8")+"="+URLEncoder.encode(device_addr,"UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
                String response = "";
                String line = "";
                while ((line = bufferedReader.readLine())!=null){
                    response+=line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return response.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(method.equals("Floor Plan")){
            String dbName = params[1];
            String tableName = params[2];
            try{
                String check_url = "http://192.168.0.3/android/floor.php";
                URL url = new URL(check_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String data = URLEncoder.encode("dbName","UTF-8")+"="+URLEncoder.encode(dbName,"UTF-8") + "&" + URLEncoder.encode("tableName","UTF-8")+"="+URLEncoder.encode(tableName,"UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
                String response = "";
                String line = "";
                while ((line = bufferedReader.readLine())!=null){
                    response+=line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return response.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "exception";
    }
}