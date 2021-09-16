package com.dfit.dfpos;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    CardView cpersediaan, cpembelian, cpenjualan, claporan, cpengaturan,ckeluar;
    TextView ltotal_penjualan, ltanggal, lnama_pengguna;
    ImageView breload;
    Dblocalhelper dbo;
    NumberFormat nf=NumberFormat.getInstance();
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cpersediaan = findViewById(R.id.cpersediaan);
        //cpembelian = findViewById(R.id.cpembelian);
        cpenjualan = findViewById(R.id.cpenjualan);
        claporan = findViewById(R.id.claporan);
        cpengaturan = findViewById(R.id.cpengaturan);
        ckeluar = findViewById(R.id.ckeluar);
        ltotal_penjualan = findViewById(R.id.ltotal_penjualan);
        ltanggal = findViewById(R.id.ltanggal);
        lnama_pengguna = findViewById(R.id.lnama_pengguna);
        breload = findViewById(R.id.breload);
        sp=getApplicationContext().getSharedPreferences("config",0);
        dbo = new Dblocalhelper(this);
        loadpermission();
        File internalstorage = new File(getFilesDir(), "kasirkuimage");
        if (!internalstorage.exists()) {
            internalstorage.mkdirs();
        }
        File kasiroffbackup=new File(Environment.getExternalStorageDirectory(),"EpsonRetailPOS");
        if(!kasiroffbackup.exists()){
            kasiroffbackup.mkdirs();
        }

        File laporandirectori=new File(Environment.getExternalStorageDirectory(),"EpsonRetailPOS/laporan");
        if(!laporandirectori.exists()){
            laporandirectori.mkdirs();
        }
        showpersediaan();
        //showpembelian();
        showpenjualan();
        showlaporan();
        keluar();
        reload();
        loadtotalpenjualan();
        showpengaturan();
        File fldb=getDatabasePath("kasirku.db");
        File flimage=new File(getFilesDir(),"kasirkuimage");
        lnama_pengguna.setText(sp.getString("username","none"));


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
            File kasiroffbackup=new File(Environment.getExternalStorageDirectory(),"EpsonRetailPOS");
            if(!kasiroffbackup.exists()){
                kasiroffbackup.mkdirs();
            }
            File laporandirectori=new File(Environment.getExternalStorageDirectory(),"EpsonRetailPOS/laporan");
            if(!laporandirectori.exists()){
                laporandirectori.mkdirs();
            }
            return;
        }

    }

    private void loadtotalpenjualan() {
        String tanggal = new SimpleDateFormat("dd").format(new Date());
        String bulanangka = new SimpleDateFormat("MM").format(new Date());
        String tahun = new SimpleDateFormat("yyyy").format(new Date());
        String bulan = "";

        switch (bulanangka) {
            case "01":
                bulan = getResources().getString(R.string.January);
                break;
            case "02":
                bulan = getResources().getString(R.string.February);
                break;
            case "03":
                bulan = getResources().getString(R.string.March);
                break;
            case "04":
                bulan = getResources().getString(R.string.April);
                break;
            case "05":
                bulan = getResources().getString(R.string.May);
                break;
            case "06":
                bulan = getResources().getString(R.string.June);
                break;
            case "07":
                bulan = getResources().getString(R.string.July);
                break;
            case "08":
                bulan = getResources().getString(R.string.August);
                break;
            case "09":
                bulan = getResources().getString(R.string.September);
                break;
            case "10":
                bulan = getResources().getString(R.string.October);
                break;
            case "11":
                bulan = getResources().getString(R.string.November);
                break;
            case "12":
                bulan = getResources().getString(R.string.December);
                break;

        }

        ltanggal.setText(tanggal+" "+bulan+" "+tahun);
        String tanggalterkini=tahun+"-"+bulanangka+"-"+tanggal;
        SQLiteDatabase db=dbo.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT pd.jumlah*(pd.harga_jual-pd.diskon) FROM penjualan_master pm " +
                "INNER JOIN penjualan_detail pd ON pm.kode_penjualan_master=pd.kode_penjualan_master WHERE pm.tanggal_penjualan='"+tanggalterkini+"' AND pm.status=1",null);
        double total_jual=0;
        while (c.moveToNext()){
            total_jual=total_jual+c.getDouble(0);
        }
        ltotal_penjualan.setText(nf.format(total_jual));
        c.close();
        db.close();

    }

    private void reload() {
        breload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadtotalpenjualan();
            }
        });
    }

    private void showpersediaan() {
        cpersediaan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sp.getInt("read_persediaan",0)==1) {
                    Intent in = new Intent(MainActivity.this, PersediaanActivity.class);
                    startActivity(in);
                }else{
                    Toast.makeText(MainActivity.this, getResources().getText(R.string.process_rejected), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void showpembelian() {
        cpembelian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sp.getInt("read_pembelian",0)==1) {
                    Intent in = new Intent(MainActivity.this, PembelianActivity.class);
                    startActivity(in);
                }else{
                    Toast.makeText(MainActivity.this, getResources().getText(R.string.process_rejected), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void showpenjualan() {
        cpenjualan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sp.getInt("read_penjualan",0)==1) {
                    Intent in = new Intent(MainActivity.this, PenjualanActivity.class);
                    startActivity(in);
                }else{
                    Toast.makeText(MainActivity.this, getResources().getText(R.string.process_rejected), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void showlaporan() {
        claporan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sp.getInt("read_laporan",0)==1) {
                    Intent in = new Intent(MainActivity.this, LaporanActivity.class);
                    startActivity(in);
                }else{
                    Toast.makeText(MainActivity.this, getResources().getText(R.string.process_rejected), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void showpengaturan() {
        cpengaturan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(MainActivity.this, PengaturanActivity.class);
                startActivity(in);
            }
        });

    }

    private void keluar() {
        ckeluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
                adb.setTitle(getResources().getString(R.string.alert_dialog_Confirmation));
                adb.setMessage(getResources().getString(R.string.alert_dialog_main_exit));
                adb.setPositiveButton(getResources().getText(R.string.alert_dialog_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });

                adb.setNegativeButton(getResources().getString(R.string.alert_dialog_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                adb.show();

            }
        });

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
        adb.setTitle(getResources().getString(R.string.alert_dialog_Confirmation));
        adb.setMessage(getResources().getString(R.string.alert_dialog_main_exit));
        adb.setPositiveButton(getResources().getString(R.string.alert_dialog_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });

        adb.setNegativeButton(getResources().getString(R.string.alert_dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        adb.show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadtotalpenjualan();
    }

}
