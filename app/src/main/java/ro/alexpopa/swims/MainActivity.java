package ro.alexpopa.swims;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    final int cameraCode = 21;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //cerem permisiuni de camera si memorie daca nu le avem la inceperea aplicatiei; daca le primim bine, daca nu, nu
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, cameraCode);
        }
        Objects.requireNonNull(getSupportActionBar()).setTitle("Swims PRE-1"); //Setăm ca titlu in partea de sus a aplicatiei numele aplicatiei ("Swims"), la care adaugam versiunea
        setContentView(R.layout.activity_main);
    }
    //logoul nu se afiseaza decat pe modul portret; daca detectam schimbari ale confoguratiei verificam daca s-a schimbat orientarea ecranului astfel incat sa setam layoutul corespunzator
    @Override
    public void onConfigurationChanged (@NonNull Configuration config) {
        super.onConfigurationChanged(config);
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_landscape);
        }
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main);
        }
    }
    //daca e apasat butonul de scanare, se executa aceasta functie, ce trimite utilizatorul la activitatea de scanare
    public void toScanActivity (View view) {
        Intent scanIntent = new Intent(this, ScanActivity.class);
        startActivity(scanIntent);
    }
    //daca e apasat butonul baza de date, ducem utilizatorul la activitatea relevanta
    public void toDBActivity (View view) {
        Intent dbIntent = new Intent(this, DBActivity.class);
        startActivity(dbIntent);
    }
 }
