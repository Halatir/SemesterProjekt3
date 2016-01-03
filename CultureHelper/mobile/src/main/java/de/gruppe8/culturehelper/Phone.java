package de.gruppe8.culturehelper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

public class Phone extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "PHONE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        //Erstelle Google API Client zur Kommunikation mit Wear
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //Testbutton zum Daten versenden
        Button button = (Button) findViewById(R.id.Testbutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pushStringsToWear();
            }
        });

    }

    //Sendet Daten an Wear, wenn aufgerufen  !!!!
    private void pushStringsToWear() {

        Inhalt.TEXT ="ätzend";
        //Requester
        PutDataMapRequest Sender = PutDataMapRequest.create(Inhalt.NOTIFICATION_PATH);
        Sender.setUrgent();

        //Diese Daten werden versendet, unter dem angegeben Pfad (siehe hier drüber)
        Sender.getDataMap().putLong("time", System.currentTimeMillis());
        Sender.getDataMap().putString("Bilddateiname", Inhalt.IMAGE);
        Sender.getDataMap().putString("Text", Inhalt.TEXT);


        Wearable.DataApi.putDataItem(mGoogleApiClient, Sender.asPutDataRequest());

        //bestätigt das aufrufen der Funktion
        TextView Information = (TextView) findViewById(R.id.Information);
        String a= "dgdfjkjhg";
        Information.setText(a);
    }

    //zu versendende Daten--->müssen anhand der Datenbank vor dem Absenden verändert werden
    public static class Inhalt {

        public static  String NOTIFICATION_PATH = "/Warning";
        public static  Long Zeit = System.currentTimeMillis();
        public static  String IMAGE = "Image";
        public static String TEXT = "content";


    }

    //Pflichtfunktionen onStart,onStop,onConnected,onConnectionFailed/Suspended, onStop
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Log.d(TAG, "connected to APICLIENT" + new Date().getTime());
    }
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected");
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection Suspended");
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection Failed");
    }


    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        Log.d(TAG, "disconected from APICLIENT" + new Date().getTime());
    }



}

/*
addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .addApi(AppIndex.API).build();

 */


