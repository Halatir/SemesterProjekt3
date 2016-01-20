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

    private static  String NOTIFICATION_PATH = "/Event";
    private static  String IMAGE = "restaurant";
    private static String TEXT = "content content content content content";
    private static double LONG =  8.869020;
    private static double LAT =  50.033562;
    private static double LongNow =  0;
    private static double LatNow =  0;


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

        //erzeuge/öffne di Datenbank
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

        //Testbuttons zum Daten versenden
        Button button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMAGE ="restaurant";
                TEXT = "Du wirst zum Tisch geführt";
                NOTIFICATION_PATH = "/Warnung";
                pushStringsToWear();
            }
        });

        Button button1 = (Button) findViewById(R.id.button3);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler myHandler = new Handler();
                myHandler.postDelayed(mMyRunnable2, 10000);
                Log.i(TAG,"delayed");
            }
        });

        Button button2 = (Button) findViewById(R.id.button4);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    IMAGE ="gesetz";
                    TEXT = "120 Kmh Höchstgeschwindigkeit";
                    NOTIFICATION_PATH = "/Warnung";
                    pushStringsToWear();

            }
        });

        Button button3 = (Button) findViewById(R.id.button5);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMAGE ="event";
                TEXT = "Strandbadfestival 2016";
                NOTIFICATION_PATH = "/Event";
                pushStringsToWear();

            }
        });


    }

    //Algorithmus zur Warnungsermittlung hier
    private void CreateWarning(double cLat, double cLong){
        //Alle Ort Locations werden angefragt
        double locs[][]= DB.Anfrage("SELECT Latitude,Longitude  FROM Ort ", null);
        //Per schleife werden sie alle auf folgende Bedingungen geprüft
        for(int i=0; i<locs.length; i++) {

            //Befindet sich die derzeitige Location im Umkreis der Ortslocation?
            if ((cLat <= locs[i][0] + 0.008 && cLat >= locs[i][0] - 0.008) && (cLong <= locs[i][1] + 0.008 && cLong >= locs[i][1] - 0.008)) {
                //Erfragt den Typ des Entsprechenden Ortes(kann man sich sparen wenn in loc eingebaut)
                String OTyp[][] =DB.Anfrage2("SELECT Typus,Region,Name FROM Ort WHERE Latitude=?", new String []{Double.toString(locs[i][0])});
                //Finde alle Warnungen die dazu passen
                //TODO:ergänze WHERE Region=Ort.Region
                String Warnung[][] =DB.Anfrage2("SELECT Text,Delaymin,Sperre FROM Warnung WHERE Typus=?", new String[]{OTyp[0][0]});


                //Für alle diese Warnungen...
                for(int j=0; j<Warnung.length;j++){
                    //wird geprüft ob sie ein Event sind oder nicht
                    if( OTyp[0][0].equals("event")){
                        //und die nötige Information an Wear gepushed
                       IMAGE="Lichtpunkt.jpg";
                        NOTIFICATION_PATH="/Event";
                        LAT=locs[i][0];
                        LONG=locs[i][1];
                        pushStringsToWear();
                      //  Log.i(TAG, "Event gefunden und gepushed"+Warnung[j][0]);
                    }
                    else {
                        int s=Integer.parseInt(Warnung[j][2]);
                        //Überprüft ob Funktion in den letzen 60 minuten schoneinmal aufgerufen wurde.
                        if (s == 0 ||s + 1 <= System.currentTimeMillis() / 60000) {

                            //Verlegt die Ausführung der warnung um die in "Delaymin" angegebene Delaytime
                            Handler myHandler = new Handler();
                            int zeit=Integer.parseInt(Warnung[j][1])*60000+1;
                            myHandler.postDelayed(mMyRunnable, zeit);
                            Log.i(TAG, "Derzeit keine Sperrung bei dieser warnung:"+Warnung[j][0]);
                            DB.setDB("UPDATE Warnung SET Sperre =" + System.currentTimeMillis() / 60000 + " WHERE Text='" + Warnung[j][0] + "'");

                            }
                        if(t==true) {
                            Log.i(TAG, "Erfolgreich Delay abgewartet");
                            //Sende nötige Informationen an Wear
                            IMAGE = "" + OTyp[0][0];
                            TEXT = "" + Warnung[j][0];
                            NOTIFICATION_PATH = "/Warnung";
                            pushStringsToWear();

                            t = false;
                        }
                    }
                }
            }
        }
    }



    //Sendet Daten an Wear, wenn aufgerufen
    private void pushStringsToWear() {
        //Requester
        Log.i(TAG, ""+LAT);
        PutDataMapRequest Sender = PutDataMapRequest.create(NOTIFICATION_PATH);
        Sender.setUrgent();

        //Diese Daten werden versendet, unter dem angegeben Pfad (siehe hier drüber)
        Sender.getDataMap().putLong("time", System.currentTimeMillis());
        Sender.getDataMap().putString("Bilddateiname", IMAGE);
        Sender.getDataMap().putString("Text", TEXT);
        Sender.getDataMap().putDouble("LONG",LONG);
        Sender.getDataMap().putDouble("LAT", LAT);
        Sender.getDataMap().putDouble("LongNow",LongNow);
        Sender.getDataMap().putDouble("LatNow", LatNow);

        Wearable.DataApi.putDataItem(mGoogleApiClient, Sender.asPutDataRequest());
    }

    private Runnable mMyRunnable = new Runnable()
    {@Override
     public void run() {t=true; Log.i(TAG,"t true");}
    };
    private Runnable mMyRunnable2 = new Runnable()
    {@Override
     public void run() {
            Log.i(TAG, "Erfolgreich Delay abgewartet");
            IMAGE ="restaurant";
            TEXT = "Hier werden 10% trinkgeld gegeben";
            NOTIFICATION_PATH = "/Warnung";
            pushStringsToWear();}
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
        LongNow=location.getLongitude();
        LatNow=location.getLatitude();
        double cLat = location.getLatitude();
        double cLong = location.getLongitude();
       // Log.i("CultureWatch",""+location.bearingTo());
     //   CreateWarning(cLat, cLong);
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
android:id="@+id/Testbutton"
 */


