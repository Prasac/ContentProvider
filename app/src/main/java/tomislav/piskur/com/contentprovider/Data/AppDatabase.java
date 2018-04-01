package tomislav.piskur.com.contentprovider.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
  * Klasa za kreiranje baze i rad sa bazom i tablicama
 * ovdje je prikazan princip singletona
 * <p>
 * kad prvi put iz aplikacije zatražimo neku operaciju nad bazom, pozvati će se metoda onCreate() - prvi puta i samo tada
 * za sve ostale promjene strukture iz aplikacije, poziva se onUpgrade()
 * <p>
 * konstanta verzija_baze - služi za praćenje koje verzije je baza,
 * i pozivanje onUpdate metode jer se mora podići verzija sa 1 na 2 tj uvećati za 1
 * <p>
 * onUpdate - koristi se npr kad se tijekom života aplikacije pokaže potreba za dodavanjem kolona i slično
 */

public class AppDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Tasks.db";
    public static final int DATABASE_VERSION = 1;               // verzija se pri promjeni strukture baze mora podići na veći integer

    private static AppDatabase instance = null;


    // pretvaranjem public u private - ovaj konstruktor postaje privatni
    private AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  // factory parametar je null
    }

    // static metoda koja vraća jednu instancu ove klase
    // public modifikator pristupa se izostavlja
    static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new AppDatabase(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String sql = "CREATE TABLE " + TasksContract.TABLE_NAME + " ("
                + TasksContract.Columns._ID + "  INTEGER PRIMARY KEY NOT NULL, "
                + TasksContract.Columns.TASKS_NAME + " TEXT NOT NULL, "
                + TasksContract.Columns.TASKS_DESCRIPTION + " TEXT);"
        + TasksContract.Columns.TASKS_ORDER + " INTEGER"
        + " ); ";

        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                // upgrade logic 1
                break;
            case 2:
                // upgrade logic 2
                break;
            default:
                throw new IllegalStateException("Problems with db on upgrade");
        }
    }

}