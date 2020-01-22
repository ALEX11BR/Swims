package ro.alexpopa.swims;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.*;

public class StudentListActivity extends AppCompatActivity {

    final int dbPickerCode = 40;
    RecyclerView recyclerView;
    ArrayList<Student> students;
    SQLiteDatabase db;

    //aceast adapter a fost pus in cadrul clasei activitate caci la apasarea unui card trebuie sa trimitem utilizatorul la activitatea specifica elevului din card, lucru care la "startActivity" nu mergea altfel decat pus aici in clasa activitatii
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {
        private ArrayList<Student> students, allStudents;
        public class RecyclerViewHolder extends RecyclerView.ViewHolder {
            TextView nameView, lastScanView, creditView, idView;
            public RecyclerViewHolder (View view) {
                super (view);
                nameView = (TextView) view.findViewById(R.id.nameView);
                lastScanView = (TextView) view.findViewById(R.id.lastScanView);
                creditView = (TextView) view.findViewById(R.id.creditView);
                idView = (TextView) view.findViewById(R.id.idView);
            }
        }
        public RecyclerViewAdapter (ArrayList<Student> Students) {
            allStudents = Students;
            students = allStudents;
        }
        @NonNull
        @Override
        public RecyclerViewHolder onCreateViewHolder (ViewGroup parent, int type) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_db, parent, false);
            return new RecyclerViewHolder(view);
        }
        @Override
        public void onBindViewHolder (RecyclerViewHolder holder, final int i) { //i-ul e final, ca altfel nu-l putem folosi in OnClickListener
            TextView nameView = holder.nameView, lastScanView = holder.lastScanView, creditView = holder.creditView, idView = holder.idView;
            nameView.setText(students.get(i).name);
            lastScanView.setText(InfoStrings.lastTimeInfo(students.get(i).lastScanDate));
            creditView.setText(InfoStrings.creditInfo(students.get(i).credit));
            idView.setText(String.valueOf(students.get(i).barcode));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(),StudentActivity.class);
                    intent.putExtra("barcode", String.valueOf(students.get(i).barcode));
                    startActivity(intent);
                }
            });
        }
        @Override
        public int getItemCount () {
            return students.size();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.studentListToolbar);
        toolbar.setTitle("Listă de elevi");
        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); //cica asta imbunatateste performantele
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    //aici facem o bara de sus personalizata, cu un buton de cautare prin numele si codurile de bare ale elevilor
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.student_list_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.studentSearch);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Căutați după nume sau cod de bare"); //din varii motive nu merge setat in xml
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                setNewAdapter(query.toUpperCase());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                setNewAdapter(newText.toUpperCase());
                return false;
            }
        });
        return true;
    }
    public void setNewAdapter(String Query) {
        ArrayList<Student> studentList = new ArrayList<Student>();
        for (int i=0; i < students.size(); i++) {
            if (students.get(i).name.contains(Query) || String.valueOf(students.get(i).barcode).contains(Query)) {
                studentList.add(students.get(i));
            }
        }
        recyclerView.setAdapter(new RecyclerViewAdapter(studentList));
    }

    //de fiecare data cand se incepe activitatea sau revenim la ea dinr-o alta, trebuie sa reimprostam lista de elevi si s-o aplicam
    @Override
    protected void onResume () {
        super.onResume();
        students = new ArrayList<Student>(); //ne asiguram ca avem lista de elevi goala cand o construim
        db = SQLiteDatabase.openOrCreateDatabase(getDatabasePath("evidenta"), null); //aici deschidem si cream (daca e nevoie) o baza de date intr-un loc specific ce nu-l poate atinge nimeni
        //copyFile(new File(getDatabasePath("evidenta").getAbsolutePath()), new File(getExternalFilesDir(null), "evidenta" /* + new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss", Locale.getDefault()).format(new Date()) */ + ".db")); //facem backupuri ale bazei de date care contin in numele fisierului data la care au fost luate
        Cursor cursor = db.rawQuery("SELECT * FROM evidenta", null);
        while (cursor.moveToNext()) {
            students.add(new Student(cursor.getString(0), cursor.getInt(1), cursor.getLong(2), cursor.getInt(3)));
        }
        if (students.size()>100000) {
            Toast.makeText(this,"Aveți peste 100 000 elevi aici. Ar fi cazul să mai faceți puțină curățenie.", Toast.LENGTH_LONG).show();
        }
        Collections.sort(students);
        recyclerView.setAdapter(new RecyclerViewAdapter(students));
        cursor.close();
    }
}
