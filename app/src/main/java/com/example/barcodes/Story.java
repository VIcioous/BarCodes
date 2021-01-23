package com.example.barcodes;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;


public class Story extends AppCompatActivity { //Klasa odpowiedzialna za Activity historii

    DBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        ListView listView =(ListView) findViewById(R.id.listView); //połączenie z ListView z XML
        dbHelper=new DBHelper(this); //połączenie z bazą danych

        ArrayList<String> theList = new ArrayList<>(); //lista danych
        Cursor data = dbHelper.getListContent();

        if(data.getCount()==0)
        {
            Toast.makeText(Story.this,getResources().getString(R.string.error3),Toast.LENGTH_SHORT).show(); //informacja pokazująca że baza danych jest pusta

        }
        else {
            while(data.moveToNext()) //ładowanie danych aż do końca
            {
                theList.add(data.getString(1));
                ListAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, theList);
                listView.setAdapter(listAdapter);
            }
        }
    }




}

