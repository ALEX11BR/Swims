package ro.alexpopa.swims;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class DBActivity extends AppCompatActivity {

    final int dbPickerCode = 40;
    RecyclerView recyclerView;
    ArrayList<Student> students;
    SQLiteDatabase db;

    //aceast adapter a fost pus in cadrul clasei activitate caci la apasarea unui card trebuie sa trimitem utilizatorul la activitatea specifica elevului din card, lucru care la "startActivity" nu mergea altfel decat pus aici in clasa activitatii
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {
        private ArrayList<Student> students;
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
            students = Students;
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

    void copyFile (File Source, File Destination) {
        FileInputStream sourceStream = null; //ca sa putem inchide stream-ul cum trebuie daca el a fost deschis in "try", el trebuie declarat si atribuit cu null
        FileOutputStream destinationStream = null;
        try {
            sourceStream = new FileInputStream(Source);
            destinationStream = new FileOutputStream(Destination);
            FileChannel sourceChannel = sourceStream.getChannel(), destinationChannel = destinationStream.getChannel();
            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (sourceStream != null) sourceStream.close();
                if (destinationStream != null) destinationStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Listă de elevi");
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); //cica asta imbunatateste performantele
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }
    //de fiecare data cand se incepe activitatea sau revenim la ea dinr-o alta, trebuie sa reimprostam lista de elevi si s-o aplicam
    @Override
    protected void onResume () {
        super.onResume();
        students = new ArrayList<Student>(); //ne asiguram ca avem lista de elevi goala cand o construim
        db = SQLiteDatabase.openOrCreateDatabase(getDatabasePath("evidenta"), null); //aici deschidem si cream (daca e nevoie) o baza de date intr-un loc specific ce nu-l poate atinge nimeni
        db.execSQL("CREATE TABLE IF NOT EXISTS evidenta (Nume TEXT, CodDeBare INT, Scanari TEXT, Credit INT)"); //initializam baza de date cu toate campurile necesare: un TEXT cu numele elevului, un INT cu codul de bare al elevului, un TEXT cu datile unix (in milisecunde, cum ii place lui Java) ale scanarilor, separate printr-un spatiu, si un INT cu numarul de sedinte ramase ale elevului
        copyFile(new File(getDatabasePath("evidenta").getAbsolutePath()), new File(getExternalFilesDir(null), "evidenta.db"));
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
    //daca e apasat butonul de adaugare elev, trimitem utilizatorul la activitatea de creare elev
    public void toCreateStudentActivity(View view) {
        Intent newStudentIntent = new Intent(this, NewStudentActivity.class);
        startActivity(newStudentIntent);
    }
    //daca e apasat butonul de importare baza de date anuntam utilizatorul ce e de anuntat, si dupa ce confirma, putem sa il lasam sa-si aleaga fisierul de importat
    //FUNCTIONALITATEA DE IMPORTARE NU FUNCTIONEAZA IN ACEST MOMENT
    public void toDBSelector(View view) {
        new AlertDialog.Builder(this)
        .setMessage("Prin acest meniu puteți importa o bază de date salvată pe dispozitivul dvs, înlocuind-o pe cea existentă.\nAceasta are extensia .db sau .sqlite.\n\nUn backup al bazei de date curente se află în memoria externă (dacă există, altfel cea internă), în Android/data/ro.alexpopa.swims/files/evidenta.db.\nImportarea noii baze de date va suprascrie backup-ul existent, așadar dacă doriți să-l păstrați, copiați-l altundeva.\n\nNu uitați să acordați acestei aplicații permisiunile necesare pentru fișiere din setări.")
        .setPositiveButton("Importă", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent DBSelectorIntent = new Intent(Intent.ACTION_GET_CONTENT);
                DBSelectorIntent.setType("*/*"); //as putea pune aici tipul MIME al bazelor de date sqlite ("application/x-sqlite3"), dar dupa aia din varii motive nu-mi pune la socoteala niciun fisier bun
                DBSelectorIntent = Intent.createChooser(DBSelectorIntent, "Alege fișierul bază de date");
                startActivityForResult(DBSelectorIntent, dbPickerCode);
            }
        })
        .setNegativeButton("Înapoi", null)
        .show();
    }
    //aici facem ce e de facut cand primim un rezultat de mai sus
    @Override
    public void onActivityResult(int RequestCode, int ResultCode, Intent Data) {
        super.onActivityResult(RequestCode, ResultCode, Data);
        if (RequestCode == dbPickerCode) { //formalitate
            if (ResultCode == RESULT_OK) { //daca am primit un rezultat favorabil (nu o parasire din varii motive a alegatorului de fisier), facem ce e de facut
                String filePath = Objects.requireNonNull(Data.getData()).getPath(), fileExtension = Objects.requireNonNull(filePath).substring(filePath.lastIndexOf(".") + 1); //extensia fisierului e subsirul care incepe de la pozitia ultimului punct din calea fisierului, la care adaugam 1 ca altfel si punctul e luat in considerare, dar si ca daca calea data n-are puncte (se mai intampla) lastIndexOf da -1, iar substring-ul vrea macar 0, altfel da eroare; "requireNonNull" avem ca altfel imi baga AndroidStudio avertismente
                if (fileExtension.equals("db") || fileExtension.equals("sqlite")) { //daca extensia e buna, copiem in liniste (daca o fi un fisier corupt sau ceva, aia e)
                    copyFile(new File(filePath), new File(getDatabasePath("evidenta").getAbsolutePath()));
                }
                else {
                    Toast.makeText(this, "Nu ați ales o bază de date cu extensia corespunzătoare. Ea trebuie să aibă extensia .db sau .sqlite.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
