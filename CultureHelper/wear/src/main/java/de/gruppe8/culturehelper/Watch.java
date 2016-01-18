package de.gruppe8.culturehelper;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

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
    private boolean FakeDrag=true;
    public PagerAdapter padapter;

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

       // final Handler myHandler = new Handler();
        //viewpager initialisieren
        viewpager = (ViewPager) findViewById(R.id.pager);
        padapter = new PagerAdapter(getSupportFragmentManager());
        viewpager.setAdapter(padapter);
        viewpager.beginFakeDrag();
        final Handler myHandler = new Handler();

        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
               // Log.i("CultureWatch", "" + position);
                if (position == 0) {
                    if(FakeDrag==false) {
                  //      Log.i("CultureWatch", "fake drag began" );
                        myHandler.postDelayed(mMyRunnable, 10);
                        FakeDrag=true;
                    }
                }
                else if(position==1){
                    if (FakeDrag == true) {
                        viewpager.endFakeDrag();
                   //     Log.i("CultureWatch", "fake drag ended");

                        FakeDrag=false;
                    }
                }
                else if(position==2){
                    TableRow layout2 = (TableRow)findViewById(R.id.fragment_three_layout);
                 //   TableRow layout22 = (TableRow)findViewById(R.id.fragment_three_layout2);
                    Color c= new Color();
                    //c.;
                    //layout22.setBackgroundColor();

                    if(Bild.equals("restaurant")){
                        layout2.setBackgroundResource(R.drawable.restaurant2);
                    }
                    else if(Bild.equals("gesetz")){
                        layout2.setBackgroundResource(R.drawable.gesetz2);
                    }
                    else if(Bild.equals("other")){
                        layout2.setBackgroundResource(R.drawable.other2);
                    }
                    TextView Nachricht = (TextView) findViewById(R.id.Nachricht);
                    Nachricht.setText(Text);
                }
            }
        });
    }

    private Runnable mMyRunnable = new Runnable()
    {@Override
     public void run() {Log.i("CultureWatch", "" + viewpager.beginFakeDrag());}
    };


    //Wenn Daten vom Handy abgesendet werden, werden sie hier empfangen
    public void onDataChanged(DataEventBuffer dataEvent) {
        for (DataEvent event : dataEvent) {
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/Warnung")) {
                Log.i("CultureWatch","recieved");

                final DataMapItem dMI = DataMapItem.fromDataItem(event.getDataItem());
                //Empfangbare Daten, zur weiterverarbeitung If states einbauen und Folge festlegen(externe Funktionen?)
                Text = dMI.getDataMap().getString("Text");
                Bild = dMI.getDataMap().getString("Bilddateiname");
                Zeit = dMI.getDataMap().getLong("time");

                Hintergrund();
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
        }
    }

    private void Hintergrund(){
        viewpager.setCurrentItem(1);
        RelativeLayout layout1 = (RelativeLayout)findViewById(R.id.fragment_two_layout);
        if(Bild.equals("restaurant")) {
            layout1.setBackgroundResource(R.drawable.restaurant);
            Log.i("CultureWatch",Bild);
        }
        else if(Bild.equals("gesetz")){
            layout1.setBackgroundResource(R.drawable.gesetz);
        }
        else if(Bild.equals("other")){
            layout1.setBackgroundResource(R.drawable.other);
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

/*

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#000000"
    android:id="@+id/fragment_three_layout"
    >

    <TableRow
        android:layout_width="320px"
        android:layout_height="100px"
        android:background="#CC000000"
        android:layout_centerVertical="true"
        android:id="@+id/fragment_three_layout2"
        android:layout_alignParentStart="true">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:textAppearance="?android:attr/textAppearanceLarge"
            android:text=""
            android:id="@+id/Nachricht"
            android:textAlignment="center"
            android:textColor="#ffffff" />
    </TableRow>
</RelativeLayout>

*/