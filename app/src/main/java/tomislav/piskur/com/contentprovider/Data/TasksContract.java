package tomislav.piskur.com.contentprovider.Data;


import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Špranca za pristup bilo kojoj bazi podataka
 */

public class TasksContract {
    // ovdje se definiraju stvari za rad sa bazom

    static final String TABLE_NAME = "Tasks";       // dobro i potrebno ovako definirati

    public static class Columns {
        public static final String _ID = BaseColumns._ID;       // ovaj BaseColumns._ID je super i za autoincrement i za prim key itd
        public static final String TASKS_NAME = "Name";
        public static final String TASKS_DESCRIPTION = "Description";
        public static final String TASKS_ORDER = "Order";

        private Columns () {}

    }

    // CONTENT_AUTHORITY - naziv našeg content providera i mora biti unique
    // u manifest moramo staviti baš ovu identičnu vrijednost
    static final String CONTENT_AUTHORITY = "tomislav.piskur.com.contentprovider";

    // putanja do providera
    static final Uri CONTENT_AUTHORITY_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // putanja za pristup tablici Tasks
    public static final Uri CONTENT_URI = Uri.withAppendedPath(CONTENT_AUTHORITY_URI,TABLE_NAME);

    // pomoćna metoda za konverziju ID u URI
    static Uri buildTaskUri(long taskId) {                          // id je long a ne int
        return ContentUris.withAppendedId(CONTENT_URI,taskId);
    }

    // pomoćna metoda za konverziju URI u ID
    static long getTaskId(Uri taskUri) {
        return ContentUris.parseId(taskUri);
    }

}