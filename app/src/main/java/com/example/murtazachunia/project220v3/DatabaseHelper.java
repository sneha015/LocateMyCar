package com.example.murtazachunia.project220v3;

/**
 This class is responsible from the creating the database. It provides various apis
 with the help of which other classes can perform queries on the apps database.
 It provides insertData() to insert new entrys, updateData() to update a specific
 entry and getdata to retrieve specific information from the database.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Parking220v2.db";
    public static final String TABLE_NAME = "information";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // String query = "CREATE TABLE " + TABLE_NAME +"(ID INTEGER PRIMARY KEY AUTOINCREMENT,NAME TEXT,ZONE TEXT,ADDRESS TEXT,FLOOR TEXT,UPPER_X INT,UPPER_Y INT,LOWER_X INT,LOWER_Y INT,LAT DOUBLE,LNG DOUBLE);";
        String query = "CREATE TABLE " + TABLE_NAME +"(ID INTEGER PRIMARY KEY AUTOINCREMENT,NAME TEXT,ZONE TEXT,ADDRESS TEXT,FLOOR TEXT);";

        // db.execSQL("CREATE TABLE " + TABLE_NAME +"ID INTEGER PRIMARY KEY AUTOINCREMENT,BOOKNAME TEXT,AUTHORNAME TEXT");
        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS"+TABLE_NAME);
        onCreate(db);

    }


//    public boolean insertdata(String name,String zone,String address,String floor,String ux,String uy,String lx,String ly,String lat,String lng){

    public boolean insertdata(String name,String zone,String address,String floor){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues content = new ContentValues();
        content.put("NAME",name);
        content.put("ZONE",zone);
        content.put("ADDRESS",address);
        content.put("FLOOR",floor);
       /* content.put("UPPER_X",Integer.parseInt(ux));
        content.put("UPPER_Y",Integer.parseInt(uy));
        content.put("LOWER_X",Integer.parseInt(lx));
        content.put("LOWER_Y",Integer.parseInt(ly));
        content.put("LAT",Double.parseDouble(lat));
        content.put("LNG",Double.parseDouble(lng));*/

        long result = db.insert(TABLE_NAME,null,content);
        if(result == -1){
            return false;
        }
        else return true;
    }

    //    public boolean updatedata(String name,String zone,String address,String floor,String ux,String uy,String lx,String ly,String lat,String lng){
    public boolean updatedata(String name,String zone,String address,String floor){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues content = new ContentValues();
        String id = "1";
        content.put("NAME",name);
        content.put("ZONE",zone);
        content.put("ADDRESS",address);
        content.put("FLOOR",floor);
       /* content.put("UPPER_X",Integer.parseInt(ux));
        content.put("UPPER_Y",Integer.parseInt(uy));
        content.put("LOWER_X",Integer.parseInt(lx));
        content.put("LOWER_Y",Integer.parseInt(ly));
        content.put("LAT",Double.parseDouble(lat));
        content.put("LNG",Double.parseDouble(lng));*/

        int result = db.update(TABLE_NAME,content,"ID = ?",new String[] { id });
        if(result == 0){
            return false;
        }
        else return true;
    }

    public Cursor getdata(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("select * from "+TABLE_NAME,null);
        return result;

    }
}