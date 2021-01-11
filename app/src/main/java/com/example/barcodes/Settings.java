package com.example.barcodes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

public class Settings extends AppCompatActivity {
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //ustawienie przycisków oraz obsługa
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_settings);
        button = findViewById(R.id.changeLanguage);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage();
            }
        });

    }
    private void changeLanguage() //funkcja odpowiedzialna za wyświetlenie okna wyboru języka
    {
        final String[]  languages ={getResources().getString(R.string.pol),getResources().getString(R.string.ang)};
        AlertDialog.Builder budowniczy = new AlertDialog.Builder(Settings.this); //builder do obsługi okna wyboru
        budowniczy.setTitle(getResources().getString(R.string.jezyk));
        budowniczy.setSingleChoiceItems(languages, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { //sprawdzanie która opcja została wybrana i ustawienie odpowiedniego języka
                if (which == 0) //język polski
                {
                    setLanguage("pl");
                    recreate();
                } else if (which == 1) //język angielski
                {
                    setLanguage("en");
                    recreate();
                }
                dialog.dismiss();
            }
        });
        AlertDialog dialog= budowniczy.create();
        dialog.show();


    }
    private void setLanguage(String en){ //Ustawienie języka, sam nie jestem pewien jak to działa ale działa
        Locale locale = new Locale(en);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale=locale;
        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
        editor.putString("Mój język",en);
        editor.apply();
    }
    public void loadLocale(){ //załadowanie konkretnego języka
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String lang =prefs.getString("Mój język","");
        setLanguage(lang);
    }

}