package de.gruppe8.culturehelper;

import android.database.SQLException;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

import java.io.IOException;
import java.util.Date;

public class Phone extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "CulturePhone";
    private boolean t=false;
    private DatabaseCall DB = new DatabaseCall(this);

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

        try {
            DB.createDataBase();

        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        try {

            DB.openDataBase();

        }catch(SQLException sqle){
            throw sqle;
        }

      /*  String a[] =  DB.Anfrage2("SELECT * FROM Ort WHERE Region =?", new String[]{"Deutschland"});
        TextView Information = (TextView) findViewById(R.id.Information);
            Information.setText(a[0]);
        Log.i("DB",a[0]);*/




    }

    //Algorithmus zur Warnungsermittlung hier
    private void CreateWarning(double cLat, double cLong){
        //Alle Ort Locations werden angefragt
        double locs[][]= DB.Anfrage("SELECT Latitude,Longitude  FROM Ort ",null);
        //Per schleife werden sie alle auf folgende Bedingungen geprüft
        for(int i=0; i<locs.length; i++) {

            //Befindet sich die derzeitige Location im Umkreis der Ortslocation?
            if ((cLat <= locs[i][0] + 0.008 && cLat >= locs[i][0] - 0.008) && (cLong <= locs[i][1] + 0.008 && cLong >= locs[i][1] - 0.008)) {
                //Erfragt den Typ des Entsprechenden Ortes(kann man sich sparen wenn in loc eingebaut)
                String OTyp[][] =DB.Anfrage2("SELECT Typus,Region,Name FROM Ort WHERE Latitude=?", new String []{Double.toString(locs[i][0])});
               Log.i(TAG,""+OTyp[0][0]);
                //Finde alle Warnungen die dazu passen
                //TODO:ergänze WHERE Region=Ort.Region
                String Warnung[][] =DB.Anfrage2("SELECT Text,Delaymin,Sperre FROM Warnung WHERE Typus=?", new String[]{OTyp[0][0]});

                //Für alle diese Warnungen...
                for(int j=0; j<Warnung.length;j++){
                    //wird geprüft ob sie ein Event sind oder nicht
                    if( OTyp[0][0].equals("event")){
                        //und die nötige Information an Wear gepushed
                       Inhalt.IMAGE="Lichtpunkt.jpg";
                        Inhalt.NOTIFICATION_PATH="/Event";
                        Inhalt.LAT=locs[i][0];
                        Inhalt.LONG=locs[i][1];
                      //  pushStringsToWear();
                      //  Log.i(TAG, "Event gefunden und gepushed"+Warnung[j][0]);
                    }
                    else {
                        //Verlegt die Ausführung der warnung um die in "Delaymin" angegebene Delaytime
                       // Log.i(TAG,"Gefunde Warnung:"+Warnung[j][0]);
                        //Überprüft ob Funktion in den letzen 60 minuten schoneinmal aufgerufen wurde.
                        // wenn noch nie aufgerufen oder die Zeit schon abgelaufen ist:
                        int s=Integer.parseInt(Warnung[j][2]);
                        if (s == 0 ||s + 60 <= System.currentTimeMillis() / 60000) {
                            Handler myHandler = new Handler();
                            int zeit=Integer.parseInt(Warnung[j][1])*60000+1;
                            myHandler.postDelayed(mMyRunnable, zeit);
                            Log.i(TAG, "Derzeit keine Sperrung bei dieser warnung");
                            DB.setDB("UPDATE Warnung SET Sperre =" + System.currentTimeMillis() / 60000 + " WHERE Text='" + Warnung[j][0] + "'");

                            if(t==true) {
                                Log.i(TAG, "Erfolgreich Delay abgewartet");
                                //Überprüft ob Delaytime abgelaufen ist und ob die Funktion in den letzen 60 minuten schoneinmal aufgerufen wurde.
                                // wenn noch nie aufgerufen oder die Zeit schon abgelaufen ist:

                                //Sende nötige Informationen an Wear
                                Inhalt.IMAGE = "" + OTyp[0][0] + ".jpg";
                                Inhalt.TEXT = "" + Warnung[j][0];
                                Inhalt.NOTIFICATION_PATH = "/Warnung";
                               pushStringsToWear();
                               // DB.setDB("UPDATE Warnung SET Sperre =" + System.currentTimeMillis() / 60000 + " WHERE Text='" + Warnung[j][0]+"'");


                                t = false;
                            }
                        }
                    }
                }
            }
        }
    }



    //Sendet Daten an Wear, wenn aufgerufen
    private void pushStringsToWear() {
        //Requester
        PutDataMapRequest Sender = PutDataMapRequest.create(Inhalt.NOTIFICATION_PATH);
        Sender.setUrgent();

        //Diese Daten werden versendet, unter dem angegeben Pfad (siehe hier drüber)
        Sender.getDataMap().putLong("time", System.currentTimeMillis());
        Sender.getDataMap().putString("Bilddateiname", Inhalt.IMAGE);
        Sender.getDataMap().putString("Text", Inhalt.TEXT);
        Sender.getDataMap().putDouble("LONG", Inhalt.LONG);
        Sender.getDataMap().putDouble("LAT", Inhalt.LAT);

        Wearable.DataApi.putDataItem(mGoogleApiClient, Sender.asPutDataRequest());
    }



    //zu versendende Daten--->müssen anhand der Datenbank vor dem Absenden verändert werden
    public static class Inhalt {

        public static  String NOTIFICATION_PATH = "/Warnung";
        public static  Long Zeit = System.currentTimeMillis();
        public static  String IMAGE = "restaurant";
        public static String TEXT = "content content content content content";
        public static double LONG =  0.0000;
        public static double LAT =  0.0000;

    }
    private Runnable mMyRunnable = new Runnable()
    {@Override
        public void run() {t=true;}
    };



    //---------------------- Request Location Updates ----------------------
    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) //is more likely to use GPS than other priority values
                .setInterval(10*1000)
                .setFastestInterval(5*1000);

        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, locationRequest, this)
                .setResultCallback(new ResultCallback() {

                    @Override
                    public void onResult(Result result) {
                        Status status = result.getStatus();
                        if (status.getStatus().isSuccess()) {
                            if (Log.isLoggable(TAG, Log.INFO)) {
                                Log.i(TAG, "Successfully requested location updates");
                            }
                        } else {
                            Log.i(TAG,
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
        //Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        double cLat = location.getLatitude();
        double cLong = location.getLongitude();
        CreateWarning(cLat,cLong);
        Log.i(TAG, "LocationChanged");

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
        Log.i(TAG, "connected to APICLIENT" + new Date().getTime());
    }

    @Override
     public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection Failed");
    }


    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        Log.i(TAG, "disconected from APICLIENT" + new Date().getTime());
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, "connection to location client suspended");
        }
    }



}

/*

 */


