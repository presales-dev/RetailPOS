package com.dfit.dfpos;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class LaporanRankingActivity extends AppCompatActivity {

    EditText edtanggal_dari, edtanggal_hingga;
    ImageView bimg_tanggal_dari, bimg_tanggal_hingga;
    ListView lvdata;
    TextView ltotal, ljudul, lkode, lnama, ljumlah;
    Button bexport;
    Dblocalhelper dbo;
    NumberFormat nf = NumberFormat.getInstance();
    reportadapter adapter;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Calendar cal = Calendar.getInstance();
    String tipe = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan_ranking);
        edtanggal_dari = findViewById(R.id.edtanggaldari);
        edtanggal_hingga = findViewById(R.id.edtanggalhingga);
        bimg_tanggal_dari = findViewById(R.id.bimg_tanggal_dari);
        bimg_tanggal_hingga = findViewById(R.id.bimg_tanggal_hingga);
        ltotal = findViewById(R.id.ltotal);
        lvdata = findViewById(R.id.lvdata);
        ljudul = findViewById(R.id.ljudul);
        ljumlah = findViewById(R.id.ljumlah);
        lkode = findViewById(R.id.lkode);
        lnama = findViewById(R.id.lnama);
        bexport = findViewById(R.id.bexport);
        dbo = new Dblocalhelper(this);
        edtanggal_dari.setFocusable(false);
        edtanggal_hingga.setFocusable(false);
        bexport.setFocusable(true);
        bexport.requestFocus();
        edtanggal_dari.setText(sdf.format(new Date()));
        edtanggal_hingga.setText(sdf.format(new Date()));
        loadreport();
        gettanggal();
        exportreport();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }


    private void loadreport() {
        SQLiteDatabase db = dbo.getReadableDatabase();
        String query = "";
        query = "SELECT pjd.kode_barang,ps.nama_barang,ps.satuan_barang,SUM(pjd.jumlah) FROM penjualan_detail pjd " +
                "INNER JOIN persediaan ps ON pjd.kode_barang=ps.kode_barang " +
                "INNER JOIN penjualan_master pjm ON pjd.kode_penjualan_master=pjm.kode_penjualan_master " +
                "WHERE tanggal_penjualan BETWEEN '" + edtanggal_dari.getText().toString() + "' AND '" + edtanggal_hingga.getText().toString() + "' " +
                " GROUP BY pjd.kode_barang ORDER BY SUM(pjd.jumlah) DESC";
        Cursor c = db.rawQuery(query, null);
        ArrayList<reportmodel> ls = new ArrayList<>();
        while (c.moveToNext()) {
            ls.add(new reportmodel(c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getDouble(3)));
        }

        adapter = new reportadapter(this, ls);
        lvdata.setAdapter(adapter);

    }


    private void gettanggal() {
        bimg_tanggal_dari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(LaporanRankingActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, month);
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String tanggalkini = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
                        edtanggal_dari.setText(tanggalkini);
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

                dpd.show();
            }
        });

        bimg_tanggal_hingga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(LaporanRankingActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, month);
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String tanggalkini = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
                        edtanggal_hingga.setText(tanggalkini);
                        loadreport();
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

                dpd.show();
            }
        });
    }

    private void exportreport() {
        bexport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File kasiroffbackup = new File(Environment.getExternalStorageDirectory(), "FnBApps");
                File laporandirectori = new File(Environment.getExternalStorageDirectory(), "FnBApps/report");
                if (ActivityCompat.checkSelfPermission(LaporanRankingActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(LaporanRankingActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(LaporanRankingActivity.this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    android.Manifest.permission.CAMERA}, 1);
                    if (!kasiroffbackup.exists()) {
                        kasiroffbackup.mkdirs();
                    }
                    if (!laporandirectori.exists()) {
                        laporandirectori.mkdirs();
                    }
                    return;
                }

                if (!kasiroffbackup.exists()) {
                    kasiroffbackup.mkdirs();
                }


                if (!laporandirectori.exists()) {
                    laporandirectori.mkdirs();
                }


                File laporanfile = null;
                String ddari = edtanggal_dari.getText().toString();
                String dhingga = edtanggal_hingga.getText().toString();
                laporanfile = new File(Environment.getExternalStorageDirectory(),
                        "FnBApps/report/ranking-" + ddari + "-" + dhingga + ".csv");

                if (!laporanfile.exists()) {
                    try {
                        laporanfile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(laporanfile));
                    SQLiteDatabase db = dbo.getReadableDatabase();
                    String query = "";
                    StringBuilder sb = new StringBuilder();
                    query = "SELECT pjd.kode_barang,ps.nama_barang,ps.satuan_barang,SUM(pjd.jumlah) FROM penjualan_detail pjd " +
                            "INNER JOIN persediaan ps ON pjd.kode_barang=ps.kode_barang " +
                            "INNER JOIN penjualan_master pjm ON pjd.kode_penjualan_master=pjm.kode_penjualan_master " +
                            "WHERE tanggal_penjualan BETWEEN '" + edtanggal_dari.getText().toString() + "' AND '" + edtanggal_hingga.getText().toString() + "' " +
                            " GROUP BY pjd.kode_barang ORDER BY SUM(pjd.jumlah) DESC";
                    Cursor c = db.rawQuery(query, null);
                    sb.append("Item Code,Item Name,Amount,Unit");
                    sb.append("\n");
                    while (c.moveToNext()) {
                        sb.append("C:" + c.getString(0) + "," + c.getString(1) + "," + c.getString(2) + "," +
                                "" + c.getDouble(3));
                        sb.append("\n");
                    }
                    c.close();
                    db.close();

                    bw.write(sb.toString());
                    bw.flush();
                    bw.close();
                    final Uri muri = Uri.fromFile(laporanfile);
                    AlertDialog.Builder adb = new AlertDialog.Builder(LaporanRankingActivity.this);
                    adb.setTitle("Information");
                    adb.setMessage("Data successfully exported, exported data is csv file stored " +
                            "automatically in the Report folder of FnBApps (FnBApps/Report)");
                    adb.setPositiveButton("Open File", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Intent in = new Intent(Intent.ACTION_VIEW);
                                in.setDataAndType(muri, "text/csv");
                                in.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(in);
                            } catch (Exception ex) {
                                Toast.makeText(LaporanRankingActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }


                        }
                    });
                    adb.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    adb.show();

                } catch (IOException e) {
                    Toast.makeText(LaporanRankingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                }


            }
        });
    }

    public class reportadapter extends BaseAdapter {

        Context ct;
        ArrayList<reportmodel> ls = new ArrayList<>();
        LayoutInflater inf;

        public reportadapter(Context ct, ArrayList<reportmodel> ls) {
            this.ct = ct;
            this.ls = ls;
            inf = (LayoutInflater) ct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return ls.size();
        }

        @Override
        public Object getItem(int position) {
            return ls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(ct).inflate(R.layout.adapter_laporan_ranking, parent, false);
                TextView lkode = v.findViewById(R.id.lkode);
                TextView lnama = v.findViewById(R.id.lnama);
                TextView ljumlah = v.findViewById(R.id.ljumlah);
                lkode.setText(ls.get(position).getKode_barang());
                lnama.setText(ls.get(position).getNama_barang());
                ljumlah.setText(nf.format(ls.get(position).getJumlah()));//+" "+ls.get(position).getSatuan_barang()
            }
            return v;
        }


    }

    public class reportmodel {
        String kode_barang, nama_barang, satuan_barang;
        double jumlah;


        public reportmodel(String kode_barang, String nama_barang, String satuan_barang, double jumlah) {
            this.kode_barang = kode_barang;
            this.nama_barang = nama_barang;
            this.satuan_barang = satuan_barang;
            this.jumlah = jumlah;
        }

        public String getKode_barang() {
            return kode_barang;
        }

        public void setKode_barang(String kode_barang) {
            this.kode_barang = kode_barang;
        }

        public String getNama_barang() {
            return nama_barang;
        }

        public void setNama_barang(String nama_barang) {
            this.nama_barang = nama_barang;
        }

        public String getSatuan_barang() {
            return satuan_barang;
        }

        public void setSatuan_barang(String satuan_barang) {
            this.satuan_barang = satuan_barang;
        }

        public double getJumlah() {
            return jumlah;
        }

        public void setJumlah(double jumlah) {
            this.jumlah = jumlah;
        }
    }
}
