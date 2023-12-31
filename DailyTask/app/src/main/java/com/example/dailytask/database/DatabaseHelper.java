package com.example.dailytask.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    String TableName = "daftar_tg";

    public DatabaseHelper(Context context) {
        super(context, "daftar_tg", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sQuery = "create table " + TableName
                + "(id INTEGER primary key autoincrement, text TEXT, date TEXT, deadline TEXT)";

        sqLiteDatabase.execSQL(sQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String sQuery = "drop table if exists " + TableName;
        sqLiteDatabase.execSQL(sQuery);
        //buat table baru
        onCreate(sqLiteDatabase);
    }

    public void insert(String text, String date, String deadline) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("text", text);
        values.put("date", date);
        values.put("deadline", deadline);
        database.insertWithOnConflict(TableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        database.close();
    }

    public void update(String id, String text) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("text", text);
        database.update(TableName, values, "id=?", new String[]{id});
        database.close();
    }

    public void delete(String id) {
        SQLiteDatabase database = getWritableDatabase();
        //delete
        String sQuery = "delete from " + TableName + " where id='" + id + "'";
        //exec
        database.execSQL(sQuery);
        //close
        database.close();
    }

    public void deleteExpiredTasks() {
        SQLiteDatabase database = getWritableDatabase();
        try {
            // Hapus tugas yang deadline-nya sudah tercapai
            String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
            String sQuery = "delete from " + TableName + " where deadline < '" + currentDate + "'";
            // Eksekusi
            database.execSQL(sQuery);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DatabaseHelper", "Error deleting expired tasks: " + e.getMessage());
        } finally {
            // Tutup koneksi
            database.close();
        }
    }

    public void truncate() {
        SQLiteDatabase database = getWritableDatabase();
        // truncate
        String sQuery1 = "delete from " + TableName;
        // reset
        String sQuery2 = "delete from sqlite_sequence where name='" + TableName + "'";
        // exec
        database.execSQL(sQuery1);
        database.execSQL(sQuery2);
        // close
        database.close();
    }

    public JSONArray getArray() {
        SQLiteDatabase database = getReadableDatabase();
        JSONArray jsonArray = new JSONArray();
        String sQuery = "select * from " + TableName + " order by deadline";
        Cursor cursor = database.rawQuery(sQuery, null);
        if (cursor.moveToFirst()) {
            do {
                JSONObject object = new JSONObject();
                try {
                    object.put("id", cursor.getString(0));
                    object.put("text", cursor.getString(1));
                    object.put("date", cursor.getString(2));
                    object.put("deadline", cursor.getString(3));
                    jsonArray.put(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return jsonArray;
    }

    public JSONArray getExpiredTasks() {
        SQLiteDatabase database = getReadableDatabase();
        JSONArray jsonArray = new JSONArray();
        String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        String sQuery = "SELECT * FROM " + TableName + " WHERE deadline < ?";

        Cursor cursor = database.rawQuery(sQuery, new String[]{currentDate});

        if (cursor.moveToFirst()) {
            do {
                JSONObject object = new JSONObject();
                try {
                    object.put("id", cursor.getString(0));
                    object.put("text", cursor.getString(1));
                    object.put("date", cursor.getString(2));
                    object.put("deadline", cursor.getString(3));
                    jsonArray.put(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return jsonArray;
    }
}
