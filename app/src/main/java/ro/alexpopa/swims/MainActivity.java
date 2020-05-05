package ro.alexpopa.swims;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    final int cameraCode = 21;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //cerem permisiuni de camera si memorie daca nu le avem la inceperea aplicatiei; daca le primim bine, daca nu, nu
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, cameraCode);
        }
        Objects.requireNonNull(getSupportActionBar()).setTitle("Swims " + InfoStrings.appVersion); //Setăm ca titlu in partea de sus a aplicatiei numele aplicatiei ("Swims"), la care adaugam versiunea; cu necesitate de nonNull ca altfel imi apar warninguri
        setContentView(R.layout.activity_main);
        //initializam baza de date cu tabelele necesare de la bun inceput
        db = SQLiteDatabase.openOrCreateDatabase(this.getDatabasePath("evidenta"), null);
        db.execSQL("CREATE TABLE IF NOT EXISTS evidenta (Nume TEXT, CodDeBare INT, Scanari TEXT, Credit INT)"); //tabelul cu evidenta elevilor: un TEXT cu numele elevului, un INT cu codul de bare al elevului, un TEXT cu datile unix (in milisecunde, cum ii place lui Java) ale scanarilor, separate printr-un spatiu, si un INT cu numarul de sedinte ramase ale elevului
        db.execSQL("CREATE TABLE IF NOT EXISTS genlog (CodDeBare INT)"); //tabelul cu codurile de bare generate: un INT cu ele
    }
    //daca e apasat butonul lista de elevi, ducem utilizatorul la activitatea relevanta
    public void toStudentListActivity (View view) {
        Intent dbIntent = new Intent(this, StudentListActivity.class);
        startActivity(dbIntent);
    }
    public void toGenerateStudentsActivity (View view) {
        Intent generateIntent = new Intent(this, StudentsGenActivity.class);
        startActivity(generateIntent);
    }
    //daca e apasat butonul de scanare, se executa aceasta functie, ce trimite utilizatorul la activitatea de scanare
    public void toScanActivity (View view) {
        Intent scanIntent = new Intent(this, ScanActivity.class);
        startActivity(scanIntent);
    }
 }
