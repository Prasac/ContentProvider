package tomislav.piskur.com.contentprovider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import tomislav.piskur.com.contentprovider.Data.AppProvider;
import tomislav.piskur.com.contentprovider.Data.TasksContract;

public class MainActivity extends AppCompatActivity {

    private SwipeRefreshLayout srlSwipe;
    private EditText etTaskName, etTaskDescription;
    private Button bAdd, bUpdate, bDelete;
    private ListView lvResults;

    private SimpleCursorAdapter adapter;                        // posebna vrsta adaptera koji zna iz kursora izvući podatke

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidgets();
        initList();
        setupListeners();
    }

    private void initWidgets() {
        srlSwipe = findViewById(R.id.srlSwipe);
        etTaskName = findViewById(R.id.etTaskName);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        bAdd = findViewById(R.id.bAdd);
        bUpdate = findViewById(R.id.bUpdate);
        bDelete = findViewById(R.id.bDelete);
        lvResults = findViewById(R.id.lvResults);
    }

    private void setupListeners() {
        bAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();         // slično kao intent
                values.put(TasksContract.Columns.TASKS_NAME, etTaskName.getText().toString().trim());
                values.put(TasksContract.Columns.TASKS_DESCRIPTION, etTaskDescription.getText().toString().trim());
                // ID kolonu će baza popuniti sama
                // TASK_ORDER kolona nas ne zanima sada
                getContentResolver().insert(TasksContract.CONTENT_URI, values);   // to pozove insert podataka u bazu
                if (adapter != null) {
                    refreshList();
                }
            }
        });

        bUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                values.put(TasksContract.Columns.TASKS_NAME,etTaskName.getText().toString());
                values.put(TasksContract.Columns.TASKS_DESCRIPTION, etTaskDescription.getText().toString());

                String selection = TasksContract.Columns.TASKS_NAME + " = ?";
                String[] selectionArgs = {etTaskName.getText().toString()};

                getContentResolver().update(TasksContract.CONTENT_URI,values,selection,selectionArgs);
                if (adapter != null) {
                    refreshList();
                }
            }
        });

        bDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String selection = TasksContract.Columns.TASKS_NAME + " = ?";
                String[] selectionArgs = {etTaskName.getText().toString()};

                getContentResolver().delete(TasksContract.CONTENT_URI, selection, selectionArgs);
                refreshList();
            }
        });

        srlSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Toast.makeText(MainActivity.this, "ola!", Toast.LENGTH_SHORT).show();
                if (adapter == null) {
                    initList();
                }
                srlSwipe.setRefreshing(false);  //ovaj dio koda treba ostati kao zadnja linija u ovoj metodi
            }
        });

        lvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                // long l - ovo je zapravo ID zapisa koji je kliknut, isti onaj id iz baze
                // int i - ovo je pozicija na listi

                String taskId = "/" + id;
                getContentResolver().delete(Uri.parse(TasksContract.CONTENT_URI + taskId), null, null);
                Toast.makeText(MainActivity.this, "Please click on button Delete", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initList() {
        //prikazati rezultate

        //prvo izvući podatke
        Cursor results = getContentResolver().query(TasksContract.CONTENT_URI, null, null, null, null);
        String[] fromColumns = {TasksContract.Columns.TASKS_NAME, TasksContract.Columns.TASKS_DESCRIPTION};
        int[] toViews = {R.id.tvTaskName, R.id.tvTaskDescription};

        // ako nema podataka, Cursor će biti null - provjeriti je li Cursor null
        if (results != null) {
            adapter = new SimpleCursorAdapter(MainActivity.this, R.layout.list_view, results, fromColumns, toViews, 0);  // ograničenje je layout - koristimo predefinirani layout
            lvResults.setAdapter(adapter);
            refreshList();
        }
    }

    private void refreshList() {
        CursorLoader cursorLoader = new CursorLoader(this, TasksContract.CONTENT_URI, null, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        adapter.swapCursor(cursor);
    }

}