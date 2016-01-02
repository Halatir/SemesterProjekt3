package de.gruppe8.culturehelper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.Objects;

public class Watch extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener, MessageApi.MessageListener {

    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "WEAR";

    private TextView mTextView;
    GoogleApiAvailability a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        //Starten des Google API Clients zur kommunikation mit Smartphone

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    //Wenn Daten vom Handy abgesendet werden, werden sie hier empfangen ---> Derzeit nur bei Deinstallation der App
    public void onDataChanged(DataEventBuffer dataEvent) {
        for (DataEvent event : dataEvent) {
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/Warning")) {


                final DataMapItem dMI = DataMapItem.fromDataItem(event.getDataItem());
                //Empfangbare Daten, zur weiterverarbeitung If states einbauen und Folge festlegen(externe Funktionen?)
                String Text = dMI.getDataMap().getString("Text");
                String Bild = dMI.getDataMap().getString("Bilddateiname");
                Long Zeit = dMI.getDataMap().getLong("time");

                //Teststring
                    TextView Testergebnis = (TextView) findViewById(R.id.text);
                String s= Objects.toString(Zeit, null);
                    Testergebnis.setText(s);
            }
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
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
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
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection Failed");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

    }
}

// aus dem xml: android.support.wearable.view.WatchViewStub

/*aus dem manifest entfernt:   <meta-data
 <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
/*


/*
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
 */


 /*private GoogleApiClient mGoogleApiClient;

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

@Override
public void onDataChanged(DataEventBuffer dataEvents) {
    for (DataEvent event : dataEvents) {
        if (event.getType() == DataEvent.TYPE_CHANGED &&
                event.getDataItem().getUri().getPath().equals("/image")) {
            final DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
            final Asset profileAsset = dataMapItem.getDataMap().getAsset("profileImage");
            final Bitmap bitmap = loadBitmapFromAsset(profileAsset);
            Log.d(TAG, ""+bitmap);
            if (null != bitmap) {
                ivQrImage.setImageBitmap(bitmap);
                bitmap.recycle();
            }

        }
    }
}

@Override
protected void onStart() {
    super.onStart();

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
    Log.d(TAG,"Connection Failed");
}

@Override
public void onConnected(Bundle bundle) {
    Wearable.DataApi.addListener(mGoogleApiClient, this);
    Wearable.MessageApi.addListener(mGoogleApiClient, this);
 */



/*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
    }
 */
