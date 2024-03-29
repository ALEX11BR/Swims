package ro.alexpopa.swims;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class StudentsGenActivity extends AppCompatActivity {

    /**************************************************************
     * dupa com.google.zxing.client.android.encode.QRCodeEncoder
     * pentru generarea codului de bare in imaginea legitimatiei
     *
     * http://code.google.com/p/zxing/
     * http://code.google.com/p/zxing/source/browse/trunk/android/src/com/google/zxing/client/android/encode/EncodeActivity.java
     * http://code.google.com/p/zxing/source/browse/trunk/android/src/com/google/zxing/client/android/encode/QRCodeEncoder.java
     */
    Bitmap genCodDeBare(String cod, int img_width, int img_height) throws WriterException {
        if (cod == null) {
            return null;
        }
        Map<EncodeHintType, Object> hint = null;
        String encoding = verUtf(cod);
        if (encoding != null) {
            hint = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hint.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(cod, BarcodeFormat.EAN_8, img_width, img_height, hint);
        } catch (IllegalArgumentException iae) {
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? 0xFF000000 : 0xFFFFFFFF; //negru : alb
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String verUtf(CharSequence c) {
        for (int i = 0; i < c.length(); i++) {
            if (c.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

    public static int checkDigitFromSum (int sum) {
        if (sum%10==0) {
            return 0;
        } else {
            return 10 - (sum % 10);
        }
    }
    //aici generam un nou cod de bare EAN8
    public static int newBarcode () {
        int result=0, check=0;
        for (int i = 1; i <= 7; i++) {
            int d;
            do {
                d = new Random().nextInt(10);
            } while (i==1 && d==0); //nu vreau ca prima cifra sa fie zero
            result = result*10 + d;
            if (i%2==1) {
                check += 3 * d;
            } else {
                check += d;
            }
        }
        return result*10 + checkDigitFromSum(check);
    }

    int toGen = 1;
    boolean generating = false;
    SQLiteDatabase db;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_gen);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Generați coduri de bare noi"); //"requireNonNull" avem ca altfel imi baga AndroidStudio avertismente
        final Button genButton = (Button) findViewById(R.id.genStudentsBtn);
        TextView studentsToGenView = (TextView) findViewById(R.id.studentsToGenField);
        studentsToGenView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) { //daca sirul nu e gol (lungime > 0), putem sa-i luam numarul
                    toGen = Integer.parseInt(s.toString());
                }
                else {
                    toGen = 1;
                }
                if (toGen < 1) {
                    toGen = 1;
                }
                genButton.setText("Generează " + InfoStrings.barcodeGenText(toGen));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        db = SQLiteDatabase.openOrCreateDatabase(this.getDatabasePath("evidenta"), null);
    }

    @Override
    protected void onResume () {
        super.onResume();
        TextView studentsGenerationInfo = (TextView) findViewById(R.id.studentsList);
        int generatedStudents, actualStudents;
        cursor = db.rawQuery("SELECT * FROM genlog", null);
        generatedStudents = cursor.getCount();
        cursor = db.rawQuery("SELECT * FROM evidenta", null);
        actualStudents = cursor.getCount();
        cursor.close();
        studentsGenerationInfo.setText(InfoStrings.generatedStudentsInfo(generatedStudents, actualStudents));
    }

    @Override
    public void onBackPressed() {
        if (generating) {
            Toast.makeText(this, "La sfârșitul generării veți reveni automat la meniul principal.", Toast.LENGTH_SHORT).show();
        }
        else {
            finish();
        }
    }
    //pentru a genera mai multe coduri de bare afisand simultan progresul generarii ne folosim de aceasta clasa
    class GenStudents extends AsyncTask<Void, Integer, Void> {
        TextView progressText = (TextView) findViewById(R.id.progressText);
        @Override
        protected void onPreExecute() {
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            Button button = (Button) findViewById(R.id.genStudentsBtn);
            button.setEnabled(false); //odata ce incepe generarea, nu mai permite inceperea uneia noi
            progressText.setVisibility(View.VISIBLE);
            progressText.setText("0/" + toGen); //incepem cu 0 din totalul de generat, evoluam pe parcurs
        }
        protected Void doInBackground(Void... voids) {
            File genDir = new File(getExternalFilesDir(null), InfoStrings.timeForDirName(new Date()));
            genDir.mkdirs(); //imaginile vor fi generate intr-un director cu data si ora generarii din folderul aplicatiei, pe care avem grija sa il generam
            Bitmap bmp0 = BitmapFactory.decodeResource(getResources(), R.drawable.base);
            Paint paint = new Paint(); //necesar pentru adaugarea textului cu codul de bare
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFF000000); //negru opac
            //paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); //facem textul aldin (bold)
            paint.setTextSize(25); //punem sub codul de bare valoarea acestuia, cu un font mai mic, si centrat, neaparat
            for (int i = 1; i <= toGen; i++) {
                String barcode;
                do {
                    barcode = String.valueOf(newBarcode());
                    cursor = db.rawQuery("SELECT * FROM genlog WHERE CodDeBare=" + barcode, null);
                }
                while (cursor.getCount() > 0);
                cursor.close();
                Bitmap bmp = bmp0.copy(Bitmap.Config.ARGB_8888, true); // bitmap-ul initial nu poate fi editat, ii facem o copie ce se poate edita
                Canvas canvas = new Canvas(bmp); // cu ajutorul canvasului imbinam bitmapul cu baza legitimatiei (cel de mai sus) cu cel ce va contine codul de bare generat + scriem nr. codului de bare
                float ltext = paint.measureText(barcode);
                canvas.drawText(barcode, (1305 - ltext) / 2, 590, paint); //punem textul cu numarul codului de bare la mijlocul acestuia
                try {
                    Bitmap bmp1 = genCodDeBare(barcode, 585, 270);
                    canvas.drawBitmap(bmp1, 360, 290, null);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                File picfile = new File(genDir, barcode + ".png");
                try {
                    FileOutputStream fos = new FileOutputStream(picfile);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                db.execSQL("INSERT INTO genlog VALUES (" + barcode + ")");
                publishProgress(i);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressText.setText(values[0] + "/" + toGen);
        }

        protected void onPostExecute(Void v) {
            finish();
        }
    }
    public void genStudents(View view) {
        generating = true;
        new GenStudents().execute();
    }
}
