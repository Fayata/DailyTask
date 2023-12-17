package com.example.dailytask;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DatabaseHelper extends SQLiteOpenHelper{

    String TableName = "daftar_tg";
    public DatabaseHelper(Context context){
        super(context, "daftar_tg", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sQuery = "create table " + TableName
                +"(id INTEGER primary key autoincrement, text TEXT,date TEXT)";

        sqLiteDatabase.execSQL(sQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String sQuery = "drop table if exists " + TableName;
        sqLiteDatabase.execSQL(sQuery);
        //buat table baru
        onCreate(sqLiteDatabase);
    }

    public void insert(String text, String date){
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        //tambah value
        values.put("text", text);
        values.put("date", date);
        //masukkan value ke database
        database.insertWithOnConflict(TableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        //close
        database.close();
    }

    public void update(String id, String text){
        SQLiteDatabase database = getWritableDatabase();
        //update
        String sQuery = "update " + TableName + "set text='" + text
                + "' where id='" + id + "'";
        //exec
        database.execSQL(sQuery);
        //close
        database.close();
    }

    public void delete(String id){
        SQLiteDatabase database = getWritableDatabase();
        //delete
        String sQuery = "delete from " + TableName + "where id='" + id + "'";
        //exec
        database.execSQL(sQuery);
        //close
        database.close();
    }
    public void truncate(){
        SQLiteDatabase database = getWritableDatabase();
        //truncate
        String sQuery1 = "delete from " +TableName;
        //reset
        String sQuery2 = "delete from sql_sequence where name='" + TableName + "'";
        //exec
        database.execSQL(sQuery1);
        database.execSQL(sQuery2);
        //close
        database.close();
    }
    public JSONArray getArray(){
        SQLiteDatabase database = getReadableDatabase();
        JSONArray jsonArray = new JSONArray();
        //select
        String sQuery = "select * from " + TableName;
        //cursor
        Cursor cursor = database.rawQuery(sQuery, null);
        //cek kondisi
        if (cursor.moveToFirst()){
            //saat cursor ke item pertama
            do {
                JSONObject object = new JSONObject();
                try {
                    //masukan value ke object
                    object.put("id", cursor.getString(0));
                    object.put("text", cursor.getString(1));
                    object.put("date", cursor.getString(2));
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }while (cursor.moveToNext());
        }
        //close
        cursor.close();
        //pass json array
        return jsonArray;
    }
}

