package ro.alexpopa.swims;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.Objects;

public class StudentActivity extends AppCompatActivity {

    int credit, creditToAdd = 1; String barcode, name, scans; boolean status = false;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        TextView creditAddView = (TextView) findViewById(R.id.addCreditField);
        final Button addCreditBtn = (Button) findViewById(R.id.creditAddBtn);
        creditAddView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) { //daca sirul nu e gol (lungime > 0), putem sa-i luam numarul
                    creditToAdd = Integer.parseInt(s.toString());
                }
                else {
                    creditToAdd = 1;
                }
                if (creditToAdd < 1) {
                    creditToAdd = 1;
                }
                addCreditBtn.setText("Adăugare " + InfoStrings.addCreditText(creditToAdd));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Objects.requireNonNull(getSupportActionBar()).setTitle("Elev");
        if (getIntent().getExtras() == null) { //dacă n-avem extra-uri, ne cărăm de aici, căci nu putem face nimic, altfel ne putem alege cu erori
            finish();
        }
        db = SQLiteDatabase.openOrCreateDatabase(this.getDatabasePath("evidenta"), null);
        barcode = getIntent().getExtras().getString("barcode"); //codul de bare trebuie tinut ca string, ca altfel nu se poate
    }
    @Override
    protected void onResume() {
        super.onResume();
        Button btn = (Button) findViewById(R.id.recordBtn);
        TextView nameView = (TextView) findViewById(R.id.nameView), idView = (TextView) findViewById(R.id.idView), infoView = (TextView) findViewById(R.id.studentsInfo), logView = (TextView) findViewById(R.id.logView);
        Cursor cursor = db.rawQuery("SELECT * FROM evidenta WHERE CodDeBare=" + barcode, null); //reimprospatam cursorul la fiecare Resume
        if (!cursor.moveToFirst()) { //daca cursorul e gol, nu e nimeni inregistrat cu codul scanat, deci trimitem la activitatea de creare elev nou
            if (status) { //daca am mai fost in aceasta activitate cu un cursor gol, e cazul sa ne intoarcem la cea precedenta (de scanare)
                finish();
            }
            else { //daca nu am mai fost aici, iar cursorul e gol, e timpul sa trimitem utilizatorul la activitatea de creare elev, cu codul de bare corespunzător
                status = true;
                Intent newStudentIntent = new Intent(this, NewStudentActivity.class);
                newStudentIntent.putExtra("barcode", barcode);
                startActivity(newStudentIntent);
            }
            return; //altfel codul de mai jos va fi executat si va genera erori urate
        }
        name = cursor.getString(0);
        nameView.setText(name); //numele elevului
        idView.setText(barcode);
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
        .setMessage("Sunteți pe cale să adăugați " + InfoStrings.addCreditText(creditToAdd) + " elevului " + name + ". Sunteți de acord?")
        .setPositiveButton("Da", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                credit = credit + creditToAdd;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }
}
