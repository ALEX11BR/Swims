package ro.alexpopa.swims;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.Objects;

public class ScanActivity extends AppCompatActivity {

    //declaram in cadrul acestei clase cateva variabile ce trebuie sa fie luate demersuri suplimentare pentru a le incadra in onCreate
    TextView textInfo;
    SurfaceView cameraSurface;
    BarcodeDetector detector;
    CameraSource cameraSource;

    //aici aflam marimile ecranului, pe care le folosim la a dimensiona cum trebuie camera
    public int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    public int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide(); //ascundem bara de titlu a aplicatiei, lucru ce necesita sa nu rezulte null ca altfel se pot intampla lucruri ciudate
        setContentView(R.layout.activity_scan);
        textInfo = (TextView) findViewById(R.id.textView);
        cameraSurface = (SurfaceView) findViewById(R.id.cameraSurface);
        detector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.EAN_8).build(); //facem detectorul de cod de bare, setat pe formatul EAN8
        cameraSource = new CameraSource.Builder(this, detector).setRequestedPreviewSize(screenHeight,screenWidth).build(); //facem sursa de camera, automat pe spate, dar cu inaltimea si latimea inversate ca altfel iese distorsionat
        cameraSurface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(holder); //pornim camera, lucru ce necesita manipularea unei exceptii
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
        detector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> detectedItems = detections.getDetectedItems();
                if (detectedItems.size()!=0) {
                    textInfo.post(new Runnable() { //un Runnable e necesar pentru a face ce e de facut cand primim un rezultat
                        @Override
                        public void run() {
                            Intent scanResultIntent = new Intent(getApplicationContext(), StudentActivity.class); //la primul parametru din diverse motive nu merge "this"
                            scanResultIntent.putExtra("barcode",detectedItems.valueAt(0).rawValue);
                            startActivity(scanResultIntent);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
