package tomislav.piskur.com.contentprovider.Data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;



public class AppProvider extends ContentProvider {

    private AppDatabase openHelper;

    // ove dvije konstante su potrebne kako bi se razdvojilo rad nad jednim retkom
    // i rada nad više od jednog retka

    // ako se vrati ova konstanta, radi se nad više redaka kao rezultat dohvaćanja podataka
    static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "."
            + TasksContract.CONTENT_AUTHORITY + "."
            + TasksContract.TABLE_NAME;

    // ako se vrati ova konstanta, radi se nad jednim retkom podatka
    static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "."
            + TasksContract.CONTENT_AUTHORITY + "."
            + TasksContract.TABLE_NAME;

    private static final int TASKS = 100;           // ovo se vraća ako ima više zapisa, počinje se po PSu od 100
    private static final int TASK_ID = 101;         // defaultno se stavlja TASKS broj + 1, vraća se ako je jedan zapis i naziva se [jednina]_ID

    /*  da postoji dodatni podatak imali bismo FEATURES = 200 i FEATURE_ID = 201 it
     */

    private static UriMatcher uriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);  // ovo je default ako se ne uspije ništa matchirati/povezati
        // npr: content://tomislav.piskur.com.contentprovider/Tasks   - ovo znači da imamo više redaka kao rezultat
        uriMatcher.addURI(TasksContract.CONTENT_AUTHORITY, TasksContract.TABLE_NAME, TASKS);

        // npr: content://tomislav.piskur.com.contentprovider/Tasks/8 - ovo znači da smo dobili jedan redak kao rezultat
        uriMatcher.addURI(TasksContract.CONTENT_AUTHORITY, TasksContract.TABLE_NAME + "/#", TASK_ID);    // # označavaju - bilo koji broj može doći
        // iza znaka / - npr 1 ili 3265 ili 25
        return uriMatcher;
    }


    @Override
    public boolean onCreate() {
        openHelper = AppDatabase.getInstance(getContext());     // getContext() metoda je ovdje super korisna
        return true;                                            // i uvijek ovako treba izgledati
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {       // ovdje se vraća tip podatka (npr plain text ili jpeg ili json i sl
        // mi vraćamo radi li se o jednom zapisu/retku ili o više redaka

        // objekt za matchiranje podataka - UriMatcher
        final int match = uriMatcher.match(uri);

        switch (match) {
            case TASKS:
                return CONTENT_TYPE;
            case TASK_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        final int match = uriMatcher.match(uri);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (match) {
            case TASKS:
                queryBuilder.setTables(TasksContract.TABLE_NAME);
                break;
            case TASK_ID:
                queryBuilder.setTables(TasksContract.TABLE_NAME);
                long taskId = TasksContract.getTaskId(uri);
                queryBuilder.appendWhere(TasksContract.Columns._ID + " = " + taskId);
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }

        SQLiteDatabase database = openHelper.getReadableDatabase();
        Cursor result = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        return result;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = uriMatcher.match(uri);
        final SQLiteDatabase database;
        Uri returningUri;
        long recordId;

        switch (match) {
            case TASKS:
                database = openHelper.getWritableDatabase();
                recordId = database.insert(TasksContract.TABLE_NAME, null, contentValues);         // vraća broj uspješno upisanih rekorda
                if (recordId > 0) {
                    returningUri = TasksContract.buildTaskUri(recordId);
                } else {
                    throw new SQLException();
                }
                break;
            //case TASK_ID:
            // TODO - definirati i ovaj slučaj u insert() metodi - zapravo nije niti potrebno jer ID ne upisujemo ručno i
            // iz pozivajućeg koda to je uvijek TASKS slučaj
            default:
                throw new IllegalArgumentException("Wrong URI" + uri);
        }
        return returningUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        final SQLiteDatabase database;
        int count;
        String selectionCriteria;

        switch (match) {
            case TASKS:
                database = openHelper.getWritableDatabase();
                count = database.delete(TasksContract.TABLE_NAME, selection, selectionArgs);
                break;
            case TASK_ID:
                database = openHelper.getWritableDatabase();
                long id = TasksContract.getTaskId(uri);
                selectionCriteria = TasksContract.Columns._ID + " = " + id;
                if ((selection != null) && (selection.length() > 0)) {
                    selectionCriteria += " AND (" + selection + ")";
                }
                count = database.delete(TasksContract.TABLE_NAME, selectionCriteria, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI" + uri);
        }

        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        final SQLiteDatabase database;
        int count;
        String selectionCriteria;

        switch (match) {
            case TASKS:
                database = openHelper.getWritableDatabase();
                count = database.update(TasksContract.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case TASK_ID:
                database = openHelper.getWritableDatabase();
                long id = TasksContract.getTaskId(uri);
                selectionCriteria = TasksContract.Columns._ID + " = " + id;
                if ((selection != null) && (selection.length() > 0)) {
                    selectionCriteria += " AND (" + selection + ")";
                }
                count = database.update(TasksContract.TABLE_NAME, contentValues, selectionCriteria, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Wrong Uri" + uri);
        }
        return count;
    }
}