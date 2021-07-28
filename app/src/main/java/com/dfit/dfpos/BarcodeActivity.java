package com.dfit.dfpos;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Vibrator;
//import android.support.v4.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class BarcodeActivity extends AppCompatActivity {

    SurfaceView surfv;
    TextView lcode;
    CameraSource camsource;
    BarcodeDetector barcodedetect;
    Vibrator vib;
    ToneGenerator toneg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);
        surfv = findViewById(R.id.surfv);
        vib=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        toneg=new ToneGenerator(AudioManager.STREAM_RING,200);

        barcodedetect = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build();
        camsource = new CameraSource.Builder(this, barcodedetect).
                setRequestedPreviewSize(1600, 1024).setAutoFocusEnabled(true).build();

        surfv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(BarcodeActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BarcodeActivity.this,
                                new String[]{android.Manifest.permission.CAMERA}, 1);
                        return;
                    }
                    camsource.start(surfv.getHolder());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                camsource.stop();
            }
        });

        barcodedetect.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    toneg.startTone(ToneGenerator.TONE_PROP_BEEP,100);
                    vib.vibrate(100);
                    Intent data=new Intent();
                    data.setData(Uri.parse(barcodes.valueAt(0).displayValue));
                    setResult(RESULT_OK,data);
                    finish();
                }

            }


        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        vib.cancel();
        toneg.stopTone();
        camsource.release();
        barcodedetect.release();

    }
}
