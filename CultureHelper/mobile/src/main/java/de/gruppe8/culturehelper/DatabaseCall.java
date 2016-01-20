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

  /*  private final static String tabelle1 = "Nutzer";

    private final static String nutzer_id = "nutzer_id";
    private final static String nutzer_name = "nutzer_Name";
    private final static String nutzer_email = "nutzer_email";*/

    public DatabaseCall(Context context) {
        super(context, DB_Name, null, DB_Version);
        this.myContext = context;
    }

    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if (dbExist) {
            //do nothing - database already exist
        } else {
            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getWritableDatabase();
            copyDataBase();
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {
            String myPath = DB_PATH + DB_Name;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {
            //database does't exist yet.
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    private void copyDataBase() throws IOException {

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_Name);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_Name;

        //myDataBase.close();

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = DB_PATH + DB_Name;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);

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
    public void setDB(String befehl){
        myDataBase.execSQL(befehl);

    }
}

/*
      Cursor cur = db.query("tbl_countries",
       		null, null, null, null, null, null);
        cur.moveToFirst();
        while (cur.isAfterLast() == false) {
            view.append("n" + cur.getString(1));
       	    cur.moveToNext();
        }
        cur.close();
    }
}


int id[] = new int[c.getCount()];
        int i = 0;
        if (c.getCount() > 0)
        {
            c.moveToFirst();
            do {
                id[i] = c.getInt(c.getColumnIndex("field_name"));
                i++;
            } while (c.moveToNext());
            c.close();
        }
 */