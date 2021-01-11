package com.example.barcodes;
import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper { //klasa obsługująca bazę danych tj tworzenie itp
    private Context context;
    private static final String DATABASE_NAME = "list.db";
    private static final String TABLE_NAME = "my_story";
    private static final String COLUMN_CODE = "KOD";



    DBHelper(@Nullable Context context) { // konstruktor
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) { // nadpisanie metody onCreate ( tworzenie tabeli)
        String query= "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "KOD)" ;

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) { //nadpisanie metody przy aktualizacji
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public boolean addData(String item1) // funkcja dodająca rekord do tabeli w bazie danych
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(COLUMN_CODE,item1);
        long result =db.insert(TABLE_NAME,null,cv);

        if(result==-1) return false;
        else
            return true;
    }

    public Cursor getListContent() //funkcja zwracająca dane z bazy
    {
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM "+ TABLE_NAME,null);
        return data;
    }


}
