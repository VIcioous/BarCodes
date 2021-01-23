package com.example.barcodes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;


public class menu extends AppCompatActivity {
    Button add, story,search; //przyciski
    EditText editText; //pole tekstowe
    DBHelper dbHelper; //instancja bazy danych
    FusedLocationProviderClient fusedLocationProviderClient; //instancja lokalizacji
    ProgressDialog pd;

    String dane, wx, wy, miasto; // po kolei: kod wpisany/otrzymany z czytnika,współrzedna x, współrzedna y, miasto z lokalizacji

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
                if(!text.isEmpty()) {
                    String str = "https://api.barcodable.com/api/v1/upc/" + text;

                    dataToInsert = editText.getText().toString() + " " + getResources().getString(R.string.szer) +
                            " " + wx + " " + getResources().getString(R.string.dlug) + " " + wy + " " + miasto;

                    JsonTask task = new JsonTask();
                    task.execute(str);

                    editText.setText(""); //wyczyszczenie pola
                }
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

    private String dataToInsert;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //po zakończeniu skanowania
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult =IntentIntegrator.parseActivityResult(requestCode,resultCode,data);

        if(intentResult.getContents() != null)
        {
            //przetworzenie danych do stringów
            dane = intentResult.getContents().toString();
            //Bartek, Tutaj będzie trzeba zrobić połączenie z API

            String str = "https://api.barcodable.com/api/v1/upc/" + dane;
            dataToInsert = dane + " " + getResources().getString(R.string.szer) +
                    " " + wx + " " + getResources().getString(R.string.dlug) + " " + wy + " " + miasto;

            JsonTask task = new JsonTask();
            task.execute(str);
        }
        else
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

    private class JsonTask extends AsyncTask<String, String, JSONObject> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(menu.this);
            pd.setMessage(getResources().getString(R.string.wait));
            pd.setCancelable(false);
            pd.show();
        }

        protected JSONObject doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
                }

                JSONObject jsonResult = new JSONObject(buffer.toString());

                return jsonResult;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            if (pd.isShowing()){
                pd.dismiss();
            }

            if(result == null){
                dataToInsert += " " + getResources().getString(R.string.notfound);
            }
            else {

                try {
                    if (result.getString("message").contentEquals("OK")) {
                        dataToInsert += " " + result.getJSONObject("item").getJSONArray("matched_items").getJSONObject(0).getString("title"); //bindowanie
                    } else
                        dataToInsert += " " + result.getString("message");

                } catch (JSONException e) {
                    e.printStackTrace();

                    Log.e("App", "yourDataTask", e);
                    dataToInsert = "";
                }
            }
            addData(dataToInsert); //dodanie do bazy danych tekstu
            AlertDialog.Builder builder = new AlertDialog.Builder(menu.this);
            builder.setTitle(getResources().getString(R.string.result));
            builder.setMessage(dataToInsert);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();

            dataToInsert = "";
        }
    }



}