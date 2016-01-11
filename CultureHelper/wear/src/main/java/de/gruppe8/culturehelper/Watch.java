package de.gruppe8.culturehelper;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.Objects;

public class Watch extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener {

    ViewPager viewpager;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "WEAR";

    private TextView mTextView;
    GoogleApiAvailability a;

    private String Text;
    private String Bild;
    private Long Zeit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        //Starten des Google API Clients zur kommunikation mit Smartphone

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
              //  .addApi(AppIndex.API)
                .build();
        //viewpager initialisieren
        viewpager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter padapter = new PagerAdapter(getSupportFragmentManager());
            viewpager.setAdapter(padapter);
    }

    //Wenn Daten vom Handy abgesendet werden, werden sie hier empfangen
    public void onDataChanged(DataEventBuffer dataEvent) {
        for (DataEvent event : dataEvent) {
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/Warning")) {


                final DataMapItem dMI = DataMapItem.fromDataItem(event.getDataItem());
                //Empfangbare Daten, zur weiterverarbeitung If states einbauen und Folge festlegen(externe Funktionen?)
                Text = dMI.getDataMap().getString("Text");
                Bild = dMI.getDataMap().getString("Bilddateiname");
                Zeit = dMI.getDataMap().getLong("time");

                setContentView(R.layout.warning);
            }

            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/Event")) {


                final DataMapItem dMI = DataMapItem.fromDataItem(event.getDataItem());
                //Empfangbare Daten, zur weiterverarbeitung If states einbauen und Folge festlegen(externe Funktionen?)
                Text = dMI.getDataMap().getString("Text");
                Bild = dMI.getDataMap().getString("Bilddateiname");
                Zeit = dMI.getDataMap().getLong("time");

                //setContentView(R.layout.Eventbilschirm);
            }

            //Teststring
            TextView Testergebnis = (TextView) findViewById(R.id.text);
            String s= Objects.toString(Zeit, null);
            // Testergebnis.setText(s);

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    //Startet die Listener
    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection Failed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "connection to location client suspended");
        }
    }
}



//nicht verwendete Code Fragmente
// aus dem xml: android.support.wearable.view.WatchViewStub

/*  adb -d forward tcp:emulator-5554 tcp:X9LDU15826002349
   7D 84 42 92 A9 D0 46 9A   AA 80 32 73 93 92 FB 4B 3E 12 ED 39

  public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public void onProviderEnabled(String provider) {

    }


    public void onProviderDisabled(String provider) {

    }


aus dem manifest entfernt:   <meta-data
 <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
          </intent-filter>




   .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
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
               .build();



 private GoogleApiClient mGoogleApiClient;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_qrcode_generation);

    mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addApi(Wearable.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build();

    final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
        @Override
        public void onLayoutInflated(WatchViewStub stub) {
            ivQrImage = (ImageView) stub.findViewById(R.id.ivQRImage);
        }
    });
}
*/