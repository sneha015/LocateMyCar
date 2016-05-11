package com.example.murtazachunia.project220v3;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

/**
 This class is responsible for storing and retrieving Ble device information
 in the apps database on the phone. Object of this class is called twice. Once when the walking
 activity is detected this object is called to store the information of the Ble device. Next
 the object of this class is called when myCar button is pressed. It fetches the information from
 the phones database and gives this information to BackgroundTask object which gets the floormap
 from the central server. This class also creates an object of DatabaseHelper class which is responsible
 for creating the database and perform various operation on the database as requested by this class.
 This class provides an interface which provides a mechanism to communicate back with the class which created
 this class's object once its task is completed. Class which creates this class's object have to implement
 the interface's method.
 */
public class UpdataDatabaseTask extends AsyncTask<String, Void, String[]> {
    Context ctx;
    DatabaseHelper mydb;

    public interface OnUpdateListener{
        public void onUpdate(String[] obj);
    }

    OnUpdateListener listener;

    public void setUpdateListener(OnUpdateListener listener){
        this.listener = listener;
    }

    UpdataDatabaseTask(Context ctx){
        this.ctx = ctx;
    }

    protected void onPreExecute(){

        super.onPreExecute();
        mydb = new DatabaseHelper(ctx);
    }

    protected void onPostExecute(String[] obj){
        if(listener != null)
            listener.onUpdate(obj);
    }
    @Override
    protected String[] doInBackground(String... params) {
        String method = params[0];

        if(method.equals("update")) {
            String name = params[1];
            String zone = params[2];
            String address = params[3];
            String floor = params[4];
           /* String ux = params[5];
            String uy = params[6];
            String lx = params[7];
            String ly = params[8];
            String lat = params[9];
            String lng = params[10];*/
            // boolean isUpdated = mydb.updatedata(name, zone, address, floor, ux, uy, lx, ly, lat, lng);
            boolean isUpdated = mydb.updatedata(name, zone, address, floor);

            if (isUpdated) {
                return new String[]{"Updated Successfully....."};
            } else {
                // boolean isInserted = mydb.insertdata(name, zone, address, floor, ux, uy, lx, ly, lat, lng);
                boolean isInserted = mydb.insertdata(name, zone, address, floor);

                if (isInserted)
                    return new String[]{"Inserted Successfully"};
                else
                    return new String[]{"Could not Insert or Update"};
            }
        }else if(method.equals("getdata")){
            Cursor cursor = mydb.getdata();
            // return new String[]{"No data found on getdata"};
            if(cursor.getCount() == 0){
                return new String[]{"No data found on getdata"};
            }else{
                String [] mystring = new String[10];
                cursor.moveToFirst();
                mystring[0] = cursor.getString(0);
                mystring[1] = cursor.getString(1);
                mystring[2] = cursor.getString(2);
                mystring[3] = cursor.getString(3);
                mystring[4] = cursor.getString(4);
               /* mystring[5] = cursor.getString(5);
                mystring[6] = cursor.getString(6);
                mystring[7] = cursor.getString(7);
                mystring[8] = cursor.getString(8);
                mystring[9] = cursor.getString(9);
                mystring[10] = cursor.getString(10);*/
                System.out.println("mystring = " + mystring);
                return mystring;
            }
        }
        return null;
    }
}