package ro.alexpopa.swims;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TooManyListenersException;

public class NewStudentActivity extends AppCompatActivity {

    SQLiteDatabase db;
    EditText studentText;
    Button addStudentBtn;
    String name, barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_student);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Înregistrați un elev nou"); //"requireNonNull" avem ca altfel imi baga AndroidStudio avertismente
        studentText = (EditText) findViewById(R.id.studentName);
        addStudentBtn = (Button) findViewById(R.id.createStudentBtn);
        TextView noticeText = (TextView) findViewById(R.id.noticeView);
        db = SQLiteDatabase.openOrCreateDatabase(this.getDatabasePath("evidenta"), null);
        barcode = Objects.requireNonNull(getIntent().getExtras()).getString("barcode");
        Cursor cursor = db.rawQuery("SELECT * FROM genlog WHERE CodDeBare=" + barcode , null);
        if (cursor.getCount() == 0) { //daca nu am gasit coduri de bare in lista cu coduri de bare generate anuntam utilizatorul de acest lucru
            Toast.makeText(this,"Nu am identificat codul de bare " + barcode + " în lista de coduri de bare generate.", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        noticeText.setText("Am detectat codul de bare " + barcode + ", care nu corespunde niciunui elev din baza de date. Puteți înregistra aici un nou elev cu acest cod de bare.");
        studentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                name = s.toString().replaceAll("[^A-Za-zăâîșțĂÂÎȘȚ]", " ").toUpperCase(); //orice caracter din nume care nu e litera (am pus la socoteală și diacriticele) se face spatiu ca sa nu se poata face nebunii si e facut litera mare pentru consistenta
                if (name.replaceAll(" ","").isEmpty() || name.charAt(0) == ' ') { //daca sirul fara spatii e gol, sau daca el incepe in spatiu (luam cazurile separat, in aceasta ordine pentru a preveni erori la sirul gol pentru conditia a 2-a) dezactivam butonul
                    addStudentBtn.setEnabled(false);
                }
                else {
                    addStudentBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    public void createStudent (View view) {/*
        Bitmap bmp0 = BitmapFactory.decodeResource(getResources(), R.drawable.base);
        Bitmap bmp = bmp0.copy(Bitmap.Config.ARGB_8888, true); // bitmap-ul initial nu poate fi editat, ii facem o copie ce se poate edita
        Canvas canvas = new Canvas(bmp); // cu ajutorul canvasului imbinam bitmapul cu baza legitimatiei (cel de mai sus) cu cel ce va contine codul de bare generat
        Paint paint = new Paint(); //necesar pentru adaugarea textului cu numele elevului
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF000000); //negru opac
        int textSize = 56;
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); //facem textul aldin (bold)
        float ltext = paint.measureText(name);
        while (ltext>580) { //daca textul depaseste o anumita latime de siguranta ii reducem marimea fontului pana cand nu o mai depaseste
            textSize--;
            paint.setTextSize(textSize);
            ltext = paint.measureText(name);
        }
        canvas.drawText(name,(1305-ltext)/2, 250, paint);
        paint.setTextSize(25); //punem sub codul de bare valoarea acestuia, cu un font mai mic, si centrat, neaparat
        ltext = paint.measureText(barcode);
        canvas.drawText(barcode,(1305-ltext)/2, 590, paint);
        try {
            Bitmap bmp1 = genCodDeBare(barcode, 585, 270);
            canvas.drawBitmap(bmp1, 360, 290, null);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        File picfile = new File(getExternalFilesDir(null), barcode + ".png"); // salvam fara probleme in folderul dedicat aplicatiei
        try {
            FileOutputStream fos = new FileOutputStream(picfile);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush(); fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        db.execSQL("INSERT INTO evidenta VALUES ('" + name + "', " + barcode + ", 0, 0)"); //introducem elevul cu codul de bare si numele date, fara sedinte si cu valoare 0 de istoric gol
        finish();
    }
}
