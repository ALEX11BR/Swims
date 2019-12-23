package ro.alexpopa.swims;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.Objects;

public class StudentActivity extends AppCompatActivity {

    int credit; String barcode, name, scans;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Elev");
        if (getIntent().getExtras() == null) { //dacă n-avem extra-uri, ne cărăm de aici, căci nu putem face nimic
            finish();
        }
        db = SQLiteDatabase.openOrCreateDatabase(this.getDatabasePath("evidenta"), null);
        barcode = getIntent().getExtras().getString("barcode"); //codul de bare trebuie tinut ca string, ca altfel nu se poate
    }
    @Override
    protected void onResume() {
        super.onResume();
        Button btn = (Button) findViewById(R.id.recordBtn);
        TextView nameView = (TextView) findViewById(R.id.nameView), idView = (TextView) findViewById(R.id.idView), infoView = (TextView) findViewById(R.id.infoView), logView = (TextView) findViewById(R.id.logView);
        Cursor cursor = db.rawQuery("SELECT * FROM evidenta WHERE CodDeBare=" + barcode, null);
        if (!cursor.moveToFirst()) { //daca cursorul e gol, nu e nimeni cu codul scanat, deci lasam ecranul initial, dar ascundem butoanele + textul cu datile scanarilor
            btn.setVisibility(View.GONE);
            btn = (Button) findViewById(R.id.deleteStudentBtn);
            btn.setVisibility(View.GONE);
            btn = (Button) findViewById(R.id.creditAddBtn);
            btn.setVisibility(View.GONE);
            logView.setVisibility(View.GONE);
            return; //altfel se continua cu instructiunile de mai jos, aparand imediat crash-uri de la apelare de elemente din cursor inexistente
        }
        name = cursor.getString(0);
        nameView.setText(name); //numele elevului
        idView.setText(String.valueOf(barcode));
        credit = cursor.getInt(3);
        if (credit <= 0) {
            btn.setVisibility(View.GONE);
        }
        else {
            btn.setVisibility(View.VISIBLE);
        }
        scans = cursor.getString(2);
        if (!scans.equals("0")) {
            String recordText = "Elevul a fost scanat pe următoarele dăți:";
            String[] indScans = scans.split(" ");
            for (String indScan : indScans) recordText = recordText + "\n" + InfoStrings.timeInfo(Long.parseLong(indScan));
            logView.setText(recordText);
        }
        infoView.setText(InfoStrings.creditInfo(credit));
        cursor.close(); //inchidem cursorul ca altfel pot aparea turbulente inexplicabile
    }
    public void addCredit (View view) {
        new AlertDialog.Builder(this)
        .setMessage("Sunteți pe cale să adăugați 10 ședințe elevului " + name + ". Sunteți de acord?")
        .setPositiveButton("Da", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                credit = credit + 10;
                db.execSQL("UPDATE evidenta SET Credit = " + credit + " WHERE CodDeBare = " + barcode);
                onResume();
            }
        })
        .setNegativeButton("Nu", null)
        .show();
    }
    public void record (View view) {
        new AlertDialog.Builder(this)
                .setMessage("Sunteți pe cale să înregistrați ședința elevului " + name + ". Sunteți de acord?")
                .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        credit = credit - 1;
                        String scanData = String.valueOf(new Date().getTime());
                        if (!scans.equals("0")) {
                            scanData = scanData + " " + scans;
                        }
                        db.execSQL("UPDATE evidenta SET Credit = " + credit + ", Scanari = '" +  scanData + "' WHERE CodDeBare = " + barcode);
                        onResume();
                    }
                })
                .setNegativeButton("Nu", null)
                .show();
    }
    public void removeStudent (View view) {
        new AlertDialog.Builder(this)
                .setMessage("Sunteți pe cale să-l ștergeți din baza de date pe " + name + ". Sunteți de acord?")
                .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.execSQL("DELETE FROM evidenta WHERE CodDeBare = " + barcode);
                        finish();
                    }
                })
                .setNegativeButton("Nu", null)
                .show();
    }
}
