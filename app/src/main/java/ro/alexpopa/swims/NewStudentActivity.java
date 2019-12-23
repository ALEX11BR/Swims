package ro.alexpopa.swims;

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

public class NewStudentActivity extends AppCompatActivity {

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

    int barcode=0;
    SQLiteDatabase db;
    EditText studentText;
    Button addStudentBtn;
    String name;

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
        for (int i=1;i<=7;i++) {
            int d = new Random().nextInt(10);
            while (i==1 && d==0) { //nu vreau ca prima cifra sa fie zero
                d = new  Random().nextInt();
            }
            result = result*10 + d;
            if (i%2==1) {
                check += 3 * d;
            } else {
                check += d;
            }
        }
        return result*10 + checkDigitFromSum(check);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_student);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Adăugați un elev nou"); //"requireNonNull" avem ca altfel imi baga AndroidStudio avertismente
        studentText = (EditText) findViewById(R.id.studentName);
        addStudentBtn = (Button) findViewById(R.id.createStudentBtn);
        db = SQLiteDatabase.openOrCreateDatabase(this.getDatabasePath("evidenta"), null);
        Cursor cursor;
        do {
            barcode = newBarcode();
            cursor = db.rawQuery("SELECT * FROM evidenta WHERE CodDeBare=" + barcode , null);
        } while (cursor.getCount()>0); //daca avem un cod de bare generat ce se regaseste deja, incercam iar pana avem unul bun
        cursor.close();
        studentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                name = s.toString().replaceAll("[^A-Za-zăâîșțĂÂÎȘȚ]", " ").toUpperCase(); //orice caracter din nume care nu e litera (am pus la socoteală și diacriticele) se face spatiu ca sa nu se poata face nebunii si e facut litera mare pentru consistenta
                if (name.replaceAll(" ","").isEmpty()) { //daca sirul fara spatii e gol, dezactivam butonul
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
    public void createStudent (View view) {
        Bitmap bmp0 = BitmapFactory.decodeResource(getResources(), R.drawable.base);
        Bitmap bmp = bmp0.copy(Bitmap.Config.ARGB_8888, true); // bitmap-ul initial nu poate fi editat, ii facem o copie ce se poate edita
        Canvas canvas = new Canvas(bmp); // cu ajutorul canvasului imbinam bitmapul cu baza legitimatiei (cel de mai sus) cu cel ce va contine codul de bare generat
        Paint paint = new Paint(); //necesar pentru adaugarea textului cu numele elevului
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF000000); //negru
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
        ltext = paint.measureText(String.valueOf(barcode));
        canvas.drawText(String.valueOf(barcode),(1305-ltext)/2, 590, paint);
        try {
            Bitmap bmp1 = genCodDeBare(String.valueOf(barcode), 585, 270);
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
        }
        db.execSQL("INSERT INTO evidenta VALUES ('" + name + "', " + barcode + ", 0, 0)");
        finish();
    }
}
