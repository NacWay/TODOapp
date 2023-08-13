package com.admin.myapp5;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class DataBase extends SQLiteOpenHelper {

    private static final String db_name = "myDb";
    private static final int db_ver = 1;
    private static final String db_table = "tasks";
    private static final String db_column = "taskName";

    public DataBase(Context context) {
        super(context, db_name, null, db_ver);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = String.format("CREATE TABLE %s (ID INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL);", db_table, db_column);
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = String.format("DELETE TABLE IF EXISTS %s", db_table);
        db.execSQL(query);
        onCreate(db);
    }

    public void insertData(String task){
        SQLiteDatabase db = this.getWritableDatabase(); //создаем таблицу в которую мы можем записывать
        ContentValues values = new ContentValues();  // Можем получть в БД определенные значения с помощью объекта
        values.put(db_column, task);
        db.insertWithOnConflict(db_table, null, values, SQLiteDatabase.CONFLICT_REPLACE); //Добавляем запись в таблицу
        db.close();
    }

    public void deleteData(String task){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(db_table, db_column + " = ?", new String[]{task});
        db.close();
    }

    public ArrayList<String> getAllTasks() {
        ArrayList<String> all_tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // на основе этого класса помещаются ряды из таблицы, которые можно перебарть
        Cursor cursor = db.query(db_table, new String[]{db_column}, null, null, null, null, null);
        //переводи полученные данные в список
        while (cursor.moveToNext()){
            int index = cursor.getColumnIndex(db_column); // индекс колонки
            all_tasks.add(cursor.getString(index));       // добавляем строку в лист
        }
        cursor.close();
        db.close();
        return all_tasks;
    }

}
