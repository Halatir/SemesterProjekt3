package de.gruppe8.culturehelper;

import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

public class Watch extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener, SensorEventListener {

    ViewPager viewpager;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "WEAR";

    private String Text;
    private String Bild;
    private Long Zeit;
    private boolean FakeDrag=true;
    public PagerAdapter padapter;

    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    private boolean ComOn=false;
    private float degrees=0;




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
        padapter = new PagerAdapter(getSupportFragmentManager());
        viewpager.setAdapter(padapter);
        viewpager.beginFakeDrag();
        final Handler myHandler = new Handler();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        image = (ImageView)findViewById(R.id.imageViewCompass);



        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                if (position == 0) {
                    if(FakeDrag==false) {
                        myHandler.postDelayed(mMyRunnable, 10);
                        FakeDrag=true;
                    }

                    ComOn=false;
                }
                else if(position==1){
                    if (FakeDrag == true) {
                        viewpager.endFakeDrag();
                        FakeDrag=false;
                    }
                }
                else if(position==2){
                    TableRow layout2 = (TableRow)findViewById(R.id.fragment_three_layout);
                    TextView txt = (TextView) findViewById(R.id.Nachricht);
                    Typeface type = Typeface.createFromAsset(getAssets(),"fonts/WorkSans-Regular.otf");
                    txt.setTypeface(type);

                    //Hintergrund für Bildschirm 3 festlegen
                    if(Bild.equals("restaurant")){
                        layout2.setBackgroundResource(R.drawable.restaurant2);
                    }
                    else if(Bild.equals("gesetz")){
                        layout2.setBackgroundResource(R.drawable.gesetz2);
                    }
                    else if(Bild.equals("other")) {
                        layout2.setBackgroundResource(R.drawable.other2);
                    }

                    TextView Nachricht = (TextView) findViewById(R.id.Nachricht);
                    Nachricht.setText(Text);
                    ComOn=false;
                }
            }
        });
    }
    //Zu beliebigen Zeitpunkt ausführbares Codefragment
    private Runnable mMyRunnable = new Runnable()
    {@Override
     public void run() {Log.i("CultureWatch", "" + viewpager.beginFakeDrag());}
    };



    //Wenn Daten vom Handy abgesendet werden, werden sie hier empfangen
    public void onDataChanged(DataEventBuffer dataEvent) {
        for (DataEvent event : dataEvent) {
            //Wenn eine Warnung gesendet wurde
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/Warnung")) {
                Log.i("CultureWatch","recieved");

                final DataMapItem dMI = DataMapItem.fromDataItem(event.getDataItem());
                //Empfangene Daten werden abgerufen:
                Text = dMI.getDataMap().getString("Text");
                Bild = dMI.getDataMap().getString("Bilddateiname");
                Zeit = dMI.getDataMap().getLong("time");

                viewpager.setCurrentItem(1);
                Background();
            }
            //Wenn ein Event gesendet wurde
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/Event")) {

                final DataMapItem dMI = DataMapItem.fromDataItem(event.getDataItem());
                //Empfangene Daten werden abgerufen:
                Text = dMI.getDataMap().getString("Text");
                Zeit = dMI.getDataMap().getLong("time");
                degrees= dMI.getDataMap().getFloat("degrees");
                Bild = "event";
                viewpager.setCurrentItem(1);
                Compass();

            }
        }
    }

    //Startet die Berechnung des Richtungsweisers und dessen Darstellung
    private void Compass(){
        viewpager.setCurrentItem(1);
        ComOn=true;
        image = (ImageView)findViewById(R.id.imageViewCompass);
            image.setBackgroundResource(R.drawable.kompasstest);
        RelativeLayout layout1 = (RelativeLayout)findViewById(R.id.fragment_two_layout);
        layout1.setBackgroundResource(R.drawable.background);

    }


    //Legt den Hintergrund des zweiten Bildschirms fest im Falle einer Warnung
    private void Background(){
        viewpager.setCurrentItem(1);
          ImageView layout = (ImageView)findViewById(R.id.imageViewCompass);
         layout.setBackgroundColor(0x000000);
        RelativeLayout layout1 = (RelativeLayout)findViewById(R.id.fragment_two_layout);

        if(Bild.equals("restaurant")) {
            layout1.setBackgroundColor(0xFFFFDA00);
        }
        else if(Bild.equals("gesetz")){
            layout1.setBackgroundColor(0xFFFF0000);
        }
        else if(Bild.equals("other")){
            layout1.setBackgroundColor(0xFF009ED4);
        }
    }

    @Override
    //Hier wird der Richtungsweiser berechnet
    public void onSensorChanged(SensorEvent event) {
        if (ComOn == true) {

         // get the angle around the z-axis rotated
            float degree = Math.round(event.values[0]);


            // create a rotation animation (reverse turn degree degrees)
            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    -degree-degrees,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            // how long the animation will take place
            ra.setDuration(210);
            // set the animation after the end of the reservation status
            ra.setFillAfter(true);
            // Start the animation
            image.startAnimation(ra);
            currentDegree = -degree-degrees;
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
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);

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
        mSensorManager.unregisterListener(this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "connection to location client suspended");
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}



//nicht verwendete Code Fragmente
// aus dem xml: android.support.wearable.view.WatchViewStub

/*

one=true;
                    Handler myHandler = new Handler();
                    myHandler.postDelayed(mMyRunnable2, 2000);

 private Runnable mMyRunnable2 = new Runnable()
    {@Override
     public void run() {
            RelativeLayout l=(RelativeLayout) findViewById(R.id.fragment_one_layout);
            if(one==true) {
                l.setBackgroundColor(0xFF000000);
            }
        }
    };

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

   <ImageView
        android:id="@+id/imageViewCompass"
        android:layout_width="320px"
        android:layout_height="320px"
        android:layout_centerVertical="true"
        android:layout_centerHorizontal="true"
        android:src="@drawable/kompasstest"
       />



*/