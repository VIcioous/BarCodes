package com.example.barcodes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button menuButton;
    private Button settingsButton; //przyciski

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        menuButton = (Button) findViewById(R.id.toMenu);
        menuButton.setOnClickListener(new View.OnClickListener() { //połączenie przycisków oraz obsługa
            @Override
            public void onClick(View v) {
                toMenu();
            }
        });

        settingsButton = (Button) findViewById(R.id.toSettings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toSettings();
            }
        });
    }
    @Override
    protected void onRestart(){
        super.onRestart();
        recreate();
    }

    public void toMenu() //przejście do widoku menu głównego
    {
        Intent intent = new Intent(this, menu.class);
        startActivity(intent);
    }

    public void toSettings() //przejście do ustawień
    {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }
}