package de.gruppe8.culturehelper;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

public class Phone extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "PHONE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        //Erstelle Google API Client zur Kommunikation mit Wear
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
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


    //Datenbankaufruf hier
    private void callDatabase(){


    }

    //Algorithmus zur Warnungsermittlung hier
    private void CreateWarning(){
        /*if DatenbankLocation is ähnlich wie currentLocation{
            Warnung.Type aus Datenbank holen
            if(Warnung.type= event){
            Inhalt.IMAGE = "Lichtpunkt.jpg"
            Inhalt.NOTIFICATOIN_PATH ="/Event"
            pushStringtoWear();
            }
            else{
            Inhalt.IMAGE = (Datenbank) Warning.Image;
            Inhalt.NOTIFICATION_PATH = "/Warning";
            Inhalt.TEXT = (Datenbank) Warning.Text;
            }
        */
    }


    //Sendet Daten an Wear, wenn aufgerufen
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

    //---------------------- Request Location Updates ----------------------
    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) //is more likely to use GPS than other priority values
                .setInterval(60*1000)
                .setFastestInterval(10*1000);

        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, locationRequest, (LocationListener) this)
                .setResultCallback(new ResultCallback() {

                    @Override
                    public void onResult(Result result) {
                        Status status = result.getStatus();
                        if (status.getStatus().isSuccess()) {
                            if (Log.isLoggable(TAG, Log.DEBUG)) {
                                Log.d(TAG, "Successfully requested location updates");
                            }
                        } else {
                            Log.e(TAG,
                                    "Failed in requesting location updates, "
                                            + "status code: "
                                            + status.getStatusCode()
                                            + ", message: "
                                            + status.getStatusMessage());
                        }
                    }
                });
    }

    @Override
    public void onLocationChanged(Location location) {

    }


    //Pflichtfunktionen
    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, (LocationListener) this);
        }
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Log.d(TAG, "connected to APICLIENT" + new Date().getTime());
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

    @Override
    public void onConnectionSuspended(int i) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "connection to location client suspended");
        }
    }



}

/*
 @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

       @Override
    public void onLocationChanged(Location location) {

    }

 */


