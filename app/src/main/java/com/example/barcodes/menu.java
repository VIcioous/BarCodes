package com.example.barcodes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class menu extends AppCompatActivity {
    Button add, story,search; //przyciski
    EditText editText; //pole tekstowe
    DBHelper dbHelper; //instancja bazy danych
    FusedLocationProviderClient fusedLocationProviderClient; //instancja lokalizacji

    String dane,wx,wy,miasto; // po kolei: kod wpisany/otrzymany z czytnika,współrzedna x, współrzedna y, miasto z lokalizacji

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        //Inicjalizacja elementów
        editText = (EditText) findViewById(R.id.textbox);
        add=(Button) findViewById(R.id.scan);
        search=(Button) findViewById(R.id.search);
        story=(Button) findViewById(R.id.toStory);
        dbHelper= new DBHelper(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Coordinates(); //pobranie lokalizacji na samym początku

        search.setOnClickListener(new View.OnClickListener() { //do API wyszukiwanie z tekstu
            @Override
            public void onClick(View v) {
                Coordinates(); //aktualizacja koordynatów
                String text = editText.getText().toString();
                //Bartek, Tutaj będzie trzeba zrobić połączenie z API
                text=text+" "+getResources().getString(R.string.szer) +wx+" "+getResources().getString(R.string.dlug)  +wy+ " "+miasto; //zbindowanie tekstu
                addData(text); //dodanie do bazy danych tekstu
                editText.setText(""); //wyczyszczenie pola
            }
        });

        story.setOnClickListener(new View.OnClickListener() { //obsługa przycisku historii wyszukiwań
            @Override
            public void onClick(View v) {
                toStory();
            }
        });

        add.setOnClickListener(new View.OnClickListener() { //obsługa przycisku do skanera
            @Override
            public void onClick(View v) {

                Coordinates();
                IntentIntegrator intentIntegrator= new IntentIntegrator(
                        menu.this
                );
                intentIntegrator.setBeepEnabled(true);
                intentIntegrator.setOrientationLocked(true);
                intentIntegrator.setCaptureActivity(Capture.class );
                intentIntegrator.initiateScan();

            }
        });
    }

    private void Coordinates() { //Pobieranie koordynatów oraz nazwy miasta, wszystko jest zrzutowane na stringi
        if(ActivityCompat.checkSelfPermission(menu.this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location =task.getResult();
                    if(location!= null)
                    {
                        try {
                            Geocoder geocoder= new Geocoder(menu.this, Locale.getDefault());
                            List<Address> addressList =geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);

                            wx=String.valueOf(addressList.get(0).getLatitude());
                            wy=String.valueOf(addressList.get(0).getLongitude());
                            miasto=String.valueOf(addressList.get(0).getLocality());


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        else {
            ActivityCompat.requestPermissions(menu.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44); //prośba o włączenie lokalizacji
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //po zakończeniu skanowania
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult =IntentIntegrator.parseActivityResult(requestCode,resultCode,data);

        if(intentResult.getContents()!=null)
        {
            //builder odpowiedzialny za wyświetlenie okienka z informacją dotyczącą kodu kreskowego lub QR, będzie można tą częśc usunąć finalnie
            AlertDialog.Builder builder = new AlertDialog.Builder(menu.this);
            builder.setTitle(getResources().getString(R.string.result));
            builder.setMessage(intentResult.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            //przetworzenie danych do stringów
            dane=intentResult.getContents().toString();
            builder.show();
            //Bartek, Tutaj będzie trzeba zrobić połączenie z API
            dane=dane+getResources().getString(R.string.szer) +wx+getResources().getString(R.string.dlug)  +wy+ " "+miasto;//bindowanie
            addData(dane); //dodawanie do bazy rekordu
            editText.setText("");
        }else
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.error4),Toast.LENGTH_SHORT).show(); //wyświetlenie info że nic sie nie zeskanowało
    }

    public void toStory() //przejście do Activity Historii wyszukiwań
    {
        Intent intent = new Intent(this,Story.class);
        startActivity(intent);
    }

    public void addData(String newRecord){ //dodanie rekordu
        boolean insert = dbHelper.addData(newRecord);
    }





}