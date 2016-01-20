package de.gruppe8.culturehelper;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Thamar on 06.01.2016.
 */
public class DatabaseCall extends SQLiteOpenHelper {


    private final static int DB_Version = 1;

    private static String DB_PATH = "/data/data/de.gruppe8.culturehelper/databases/";
    private final static String DB_Name = "DB.db";
    private SQLiteDatabase myDataBase;
    private final Context myContext;


    public DatabaseCall(Context context) {
        super(context, DB_Name, null, DB_Version);
        this.myContext = context;
    }

    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if (dbExist) {
            //Datenbank existiert bereits
        } else {
            //Leere Datenbank wird im Standartverzeichnis der App angelegt
            this.getWritableDatabase();
            copyDataBase();
        }

    }

    //Überprüft ob die Datenbank bereits vorhanden ist, damit die Datenbank nicht bei jedem start kopiert wird
    private boolean checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {
            String myPath = DB_PATH + DB_Name;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {
            //Datenbank existiert nicht
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }

    //Kopiert die eigene Datenbank aus dem Lokalen assets-Ordner in die eben erstellte, leere Datenbank
    //im Systemordner der App, wo man auf sie zugreifen kann.
    private void copyDataBase() throws IOException {

        //Öffne lokale Datenbank als Inputstream
        InputStream myInput = myContext.getAssets().open(DB_Name);

        // Pfad zur eben erstellten Datenbank
        String outFileName = DB_PATH + DB_Name;
        //Öffne den leeren Datenbank-outputstream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //Transferiere bytes vom Inputstream zum Outputstream
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        //Schließe die Streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }
    //Öffnet die Datenbank
    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_Name;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    //Anfrage die einen Double[][] zurückgeben kann
    public double[][] Anfrage(String Anfrage, String[] bedingung) {

        Cursor c = myDataBase.rawQuery(Anfrage, bedingung);
        double [][] s= new double[c.getCount()][2];
        c.moveToFirst();

        for(int i=0; i<c.getCount();i++) {
            s[i][0] = c.getDouble(0);
            s[i][1] = c.getDouble(1);
            c.moveToNext();
                //c.moveToPosition(Position);

        }
        c.close();
        return s;

    }

    //Anfrage, die einen String[][] zurückgeben kann
    public String [][] Anfrage2(String Anfrage, String[] bedingung) {

        Cursor c = myDataBase.rawQuery(Anfrage, bedingung);
        String s[][]= new String[c.getCount()][3];
        c.moveToFirst();
        for(int i=0; i<c.getCount();i++) {
                s[i][0] = (c.getString(0));
                s[i][1] = (c.getString(1));
                s[i][2] = (c.getString(2));
                c.moveToNext();

        }
        c.close();
        return s;
    }

    //Zum beschreiben der Datenbank
    public void setDB(String befehl){
        myDataBase.execSQL(befehl);
    }


    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();

        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
