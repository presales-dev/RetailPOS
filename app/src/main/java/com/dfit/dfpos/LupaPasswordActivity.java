package com.dfit.dfpos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class LupaPasswordActivity extends AppCompatActivity {

    Button blupapass,brestore;
    EditText printersn;
    TextView hasilLP;
    Dblocalhelper dbo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lupa_password);

        blupapass=findViewById(R.id.blupapass);
        brestore=findViewById(R.id.brestoreback);
        printersn=findViewById(R.id.edprintsn);
        hasilLP=findViewById(R.id.hasilLP);
        dbo=new Dblocalhelper(this);

        brestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restore();
            }
        });

        blupapass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = dbo.getReadableDatabase();
                Cursor c = db.rawQuery("SELECT nama_usaha,alamat_usaha,nohp_usaha,email_usaha,website FROM perusahaan WHERE id=1", null);
                c.moveToFirst();
                //Toast.makeText(LupaPasswordActivity.this, c.getString(3)+" "+c.getString(4), Toast.LENGTH_SHORT).show();
                if(printersn.getText().toString().equals(c.getString(4))){
                    c.close();
                    db.close();
                    showusername();
                }else{
                    Toast.makeText(LupaPasswordActivity.this, "Serial tidak sesuai dengan data backup!", Toast.LENGTH_LONG).show();
                    //printersn.setText(c.getString(4));
                    c.close();
                    db.close();

                }


            }
        });

    }


    public void showusername(){
        SQLiteDatabase db = dbo.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT username,password FROM pengguna", null);
        c.moveToFirst();
        //Toast.makeText(LupaPasswordActivity.this, c.getString(0)+" "+c.getString(1), Toast.LENGTH_SHORT).show();
        hasilLP.setText("Username : "+c.getString(0)+", Password : "+c.getString(1));
        c.close();
        db.close();
    }
    public void restore(){
        AlertDialog.Builder adb = new AlertDialog.Builder(LupaPasswordActivity.this);
        adb.setCancelable(false);
        adb.setTitle("Information");
        adb.setMessage("Pastikan EpsonRetailPOS folder berada pada internal storage." +
                "lalu pastikan file backup telah tersimpan didalam folder EpsonRetailPOS");
        adb.setPositiveButton("Restore", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (ActivityCompat.checkSelfPermission(LupaPasswordActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(LupaPasswordActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(LupaPasswordActivity.this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    android.Manifest.permission.CAMERA}, 1);
                    return;
                }

                File lokasidb = getDatabasePath("kasirku.db");
                File lokasibackupdb = new File(Environment.getExternalStorageDirectory(), "EpsonRetailPOS/database.db");
                File lokasiimage = new File(getFilesDir(), "kasirkuimage");
                File lokasibackupimage = new File(Environment.getExternalStorageDirectory(), "EpsonRetailPOS/Image");
                try {
                    Oneforallfunc.copyfile(lokasibackupdb, lokasidb);
                    Oneforallfunc.copyfile(lokasibackupimage, lokasiimage);
                    Toast.makeText(LupaPasswordActivity.this, "Data Restore Berhasil!", Toast.LENGTH_LONG).show();
                    blupapass.setEnabled(true);
                    blupapass.setTextColor(Color.parseColor("#FFFFFF"));
                    brestore.setEnabled(false);
                    brestore.setTextColor(Color.parseColor("#000000"));
                    printersn.setEnabled(true);
                    printersn.requestFocus();

                } catch (Exception e) {
                    Toast.makeText(LupaPasswordActivity.this, "Restore Gagal, file tidak ditemukan"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        adb.show();
    }
}