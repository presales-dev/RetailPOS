package com.dfit.dfpos;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.epson.epos2.Log;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class LoginActivity extends AppCompatActivity {

    EditText edusername,edpassword;
    Button bmasuk;
    TextView lcapregister,lregister;
    Dblocalhelper dbo;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edusername=findViewById(R.id.edusername);
        edpassword=findViewById(R.id.edpassword);
        bmasuk=findViewById(R.id.bmasuk);
        lregister=findViewById(R.id.lregister);
        lcapregister=findViewById(R.id.lcapregister);
        dbo=new Dblocalhelper(this);
        sp=getApplicationContext().getSharedPreferences("config",0);
        loadpermission();
        cekuser();
        login();
        register();

        try {
            Log.setLogSettings(this, Log.PERIOD_TEMPORARY, Log.OUTPUT_STORAGE, null, 0, 1, Log.LOGLEVEL_LOW);
        }catch (Exception e) {

        }

    }

    private void login(){
        bmasuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor edit=sp.edit();
                SQLiteDatabase db=dbo.getReadableDatabase();
                Cursor c=db.rawQuery("SELECT COUNT(kode_user) FROM pengguna WHERE (username='"+edusername.getText().toString()+"' " +
                        "OR email='"+edusername.getText().toString()+"') AND password='"+edpassword.getText().toString()+"' ",null);
                if(c.moveToFirst()){
                    if(c.getInt(0)==0){
                        Toast.makeText(LoginActivity.this, "Wrong Username or Password", Toast.LENGTH_SHORT).show();
                    }else{
                        Cursor cdatapengguna=db.rawQuery("SELECT kode_user,email,username,password,read_persediaan,write_persediaan, " +
                                "read_pembelian,write_pembelian,read_penjualan,write_penjualan,read_laporan,read_user FROM pengguna WHERE username='"+edusername.getText().toString()+"' " +
                                "OR email='"+edusername.getText().toString()+"' AND password='"+edpassword.getText().toString()+"' ",null);
                        if(cdatapengguna.moveToFirst()){

                            /*Oneforallfunc.kode_user=cdatapengguna.getString(0);
                            Oneforallfunc.email=cdatapengguna.getString(1);
                            Oneforallfunc.username=cdatapengguna.getString(2);
                            Oneforallfunc.password=cdatapengguna.getString(3);
                            Oneforallfunc.read_persediaaan=cdatapengguna.getInt(4);
                            Oneforallfunc.write_persediaaan=cdatapengguna.getInt(5);
                            Oneforallfunc.read_pembelian=cdatapengguna.getInt(6);
                            Oneforallfunc.write_pembelian=cdatapengguna.getInt(7);
                            Oneforallfunc.read_penjualan=cdatapengguna.getInt(8);
                            Oneforallfunc.write_penjualan=cdatapengguna.getInt(9);
                            Oneforallfunc.read_laporan=cdatapengguna.getInt(10);
                            Oneforallfunc.read_user=cdatapengguna.getInt(11);*/

                            edit.putString("kode_user",cdatapengguna.getString(0));
                            edit.putString("email",cdatapengguna.getString(1));
                            edit.putString("username",cdatapengguna.getString(2));
                            edit.putString("password",cdatapengguna.getString(3));
                            edit.putInt("read_persediaan",cdatapengguna.getInt(4));
                            edit.putInt("write_persediaan",cdatapengguna.getInt(5));
                            edit.putInt("read_pembelian",cdatapengguna.getInt(6));
                            edit.putInt("write_pembelian",cdatapengguna.getInt(7));
                            edit.putInt("read_penjualan",cdatapengguna.getInt(8));
                            edit.putInt("write_penjualan",cdatapengguna.getInt(9));
                            edit.putInt("read_laporan",cdatapengguna.getInt(10));
                            edit.putInt("read_user",cdatapengguna.getInt(11));

                        }
                        cdatapengguna.close();


                        Cursor cdatausaha=db.rawQuery("SELECT nama_usaha,alamat_usaha,nohp_usaha,email_usaha,website FROM perusahaan WHERE id=1",null);
                        if(cdatausaha.moveToFirst()){
                            edit.putString("nama_usaha",cdatausaha.getString(0));
                            edit.putString("alamat_usaha",cdatausaha.getString(1));
                            edit.putString("nohp_usaha",cdatausaha.getString(2));
                            edit.putString("email_usaha",cdatausaha.getString(3));
                            edit.putString("website",cdatausaha.getString(4));
                        }

                        Cursor cdatapengaturan=db.rawQuery("SELECT id,default_printer,host_sync,sync_time,issync,view_tipe FROM pengaturan WHERE id=1",null);
                        if(cdatapengaturan.moveToFirst()){
                            /*Oneforallfunc.def_printer=cdatapengaturan.getString(1);
                            Oneforallfunc.host_sync=cdatapengaturan.getString(2);
                            Oneforallfunc.sync_time=cdatapengaturan.getDouble(3);
                            Oneforallfunc.issync=cdatapengaturan.getInt(4);*/
                            edit.putString("default_printer",cdatapengaturan.getString(1));
                            edit.putString("host_sync",cdatapengaturan.getString(2));
                            edit.putLong("sync_time",cdatapengaturan.getLong(3));
                            edit.putInt("issync",cdatapengaturan.getInt(4));
                            edit.putInt("view_tipe",cdatapengaturan.getInt(5));
                        }
                        edit.commit();
                        cdatapengaturan.close();
                        Intent in=new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(in);
                        finish();

                    }
                    c.close();
                }

                db.close();
            }
        });
    }

    private void cekuser(){
        SQLiteDatabase db=dbo.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT COUNT(kode_user) FROM pengguna ",null);
        if(c.moveToFirst()){
            if(c.getInt(0)>0){
                lcapregister.setVisibility(View.VISIBLE);
                lregister.setVisibility(View.VISIBLE);
            }else{
                lcapregister.setVisibility(View.VISIBLE);
                lregister.setVisibility(View.VISIBLE);
            }
        }
        c.close();
        db.close();
    }

    private void loadpermission(){
        if (
                        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED  ||
                        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            File kasiroffbackup=new File(Environment.getExternalStorageDirectory(),"FnBApps");
            if(!kasiroffbackup.exists()){
                kasiroffbackup.mkdirs();
            }
            return;
        }

    }

    private void register(){
        lregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(in);
            }
        });
    }
}
