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
import android.os.Environment;
import android.os.StrictMode;

import android.os.Bundle;
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

public class LihatLaporanActivity extends AppCompatActivity {

    EditText edtanggal_dari, edtanggal_hingga;
    ImageView bimg_tanggal_dari, bimg_tanggal_hingga;
    ListView lvdata;
    TextView ltotal, ljudul,ltanggal,ljumlah,lkode_trans,lnama,lharga,lhtotal;
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
        setContentView(R.layout.activity_lihat_laporan);
        edtanggal_dari = findViewById(R.id.edtanggaldari);
        edtanggal_hingga = findViewById(R.id.edtanggalhingga);
        bimg_tanggal_dari = findViewById(R.id.bimg_tanggal_dari);
        bimg_tanggal_hingga = findViewById(R.id.bimg_tanggal_hingga);
        ltotal = findViewById(R.id.ltotal);
        lhtotal = findViewById(R.id.lhtotal);
        lvdata = findViewById(R.id.lvdata);
        ljudul = findViewById(R.id.ljudul);
        ltanggal = findViewById(R.id.ltanggal);
        ljumlah = findViewById(R.id.ljumlah);
        lkode_trans = findViewById(R.id.lkode_trans);
        lnama = findViewById(R.id.lnama);
        lharga = findViewById(R.id.lharga);
        bexport = findViewById(R.id.bexport);
        dbo = new Dblocalhelper(this);
        edtanggal_dari.setFocusable(false);
        edtanggal_hingga.setFocusable(false);
        bexport.setFocusable(true);
        bexport.requestFocus();
        edtanggal_dari.setText(sdf.format(new Date()));
        edtanggal_hingga.setText(sdf.format(new Date()));
        Bundle ex = getIntent().getExtras();
        tipe = ex.getString("tipe");
        if (tipe.equals("penjualan")) {
            ljudul.setText("Sales Report");
        } else if (tipe.equals("pembelian")) {
            ljudul.setText("Purchase Report");
        }else if(tipe.equals("stok")){
            ltanggal.setVisibility(View.GONE);
            float density=getApplicationContext().getResources().getDisplayMetrics().density;
            lkode_trans.getLayoutParams().width= Math.round(90 * density);
            lkode_trans.setText("Item Code");
            ljumlah.setVisibility(View.GONE);
            lharga.setText("Amount");
            ljudul.setText("Inventory Stock Report");
            ltotal.setVisibility(View.INVISIBLE);
            lhtotal.setVisibility(View.INVISIBLE);
        }else if(tipe.equals("ranking")){
            ltanggal.setVisibility(View.GONE);
            float density=getApplicationContext().getResources().getDisplayMetrics().density;
            lkode_trans.getLayoutParams().width= Math.round(90 * density);
            lkode_trans.setText("Item Code");
            ljumlah.setVisibility(View.GONE);
            lharga.setText("Amount");
            ljudul.setText("Item Sales Ranking");
            ltotal.setVisibility(View.INVISIBLE);
            lhtotal.setVisibility(View.INVISIBLE);
        }
        loadreport();
        gettanggal();
        exportreport();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

    }

    private void loadreport() {
        SQLiteDatabase db = dbo.getReadableDatabase();
        String query = "";
        if (tipe.equals("penjualan")) {
            query = "SELECT pjd.kode_penjualan_master,pjm.tanggal_penjualan,ps.nama_barang,pjd.jumlah,pjd.harga_jual,pjd.diskon," +
                    "pjd.jumlah*(pjd.harga_jual-(pjd.harga_jual*(pjd.diskon/100))) from penjualan_detail pjd " +
                    "INNER JOIN persediaan ps ON ps.kode_barang=pjd.kode_barang " +
                    "INNER JOIN penjualan_master pjm ON pjm.kode_penjualan_master=pjd.kode_penjualan_master " +
                    "WHERE tanggal_penjualan BETWEEN '" + edtanggal_dari.getText().toString() + "' AND '" + edtanggal_hingga.getText().toString() + "' " +
                    "ORDER BY pjm.date_created DESC";
        } else if (tipe.equals("pembelian")) {
            query = "SELECT pbd.kode_pembelian_master,pbm.tanggal_pembelian,ps.nama_barang,pbd.jumlah,pbd.harga_beli,0," +
                    "pbd.jumlah*pbd.harga_beli from pembelian_detail pbd " +
                    "INNER JOIN persediaan ps ON ps.kode_barang=pbd.kode_barang " +
                    "INNER JOIN pembelian_master pbm ON pbm.kode_pembelian_master=pbd.kode_pembelian_master " +
                    "WHERE tanggal_pembelian BETWEEN '" + edtanggal_dari.getText().toString() + "' AND " +
                    "'" + edtanggal_hingga.getText().toString() + "' ORDER BY pbm.date_created DESC";
        }else if (tipe.equals("stok")) {
            query = "SELECT kode_barang,nama_barang,satuan_barang,jumlah_barang,0,0,0 FROM persediaan ORDER BY jumlah_barang DESC";
        }else if (tipe.equals("ranking")) {
            query = "SELECT pjd.kode_barang,ps.nama_barang,ps.satuan_barang,SUM(pjd.jumlah),0,0,0 FROM penjualan_detail pjd " +
                    "INNER JOIN persediaan ps ON pjd.kode_barang=ps.kode_barang " +
                    "INNER JOIN penjualan_master pjm ON pjd.kode_penjualan_master=pjm.kode_penjualan_master " +
                    "WHERE tanggal_penjualan BETWEEN '" + edtanggal_dari.getText().toString() + "' AND '" + edtanggal_hingga.getText().toString() + "' " +
                    " GROUP BY pjd.kode_barang ORDER BY SUM(pjd.jumlah) DESC";
        }
        Cursor c = db.rawQuery(query, null);
        ArrayList<reportmodel> ls = new ArrayList<>();
        while (c.moveToNext()) {
            ls.add(new reportmodel(c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getDouble(3),
                    c.getDouble(4),
                    c.getDouble(5),
                    c.getDouble(6)));
        }

        adapter = new reportadapter(this, ls);
        lvdata.setAdapter(adapter);
        double total = 0;
        for (int i = 0; i < ls.size(); i++) {
            total = total + ls.get(i).getTotal();
        }
        ltotal.setText(nf.format(total));

    }

    private void gettanggal() {
        bimg_tanggal_dari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(LihatLaporanActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                DatePickerDialog dpd = new DatePickerDialog(LihatLaporanActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                if (ActivityCompat.checkSelfPermission(LihatLaporanActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(LihatLaporanActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(LihatLaporanActivity.this,
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
                if (tipe.equals("penjualan")) {
                    laporanfile = new File(Environment.getExternalStorageDirectory(), "FnBApps/report/sales" + ddari + "-" + dhingga + ".csv");
                } else if (tipe.equals("pembelian")) {
                    laporanfile = new File(Environment.getExternalStorageDirectory(), "FnBApps/report/purchase" + ddari + "-" + dhingga + ".csv");
                }else if (tipe.equals("stok")) {
                    laporanfile = new File(Environment.getExternalStorageDirectory(), "FnBApps/report/inventory" + ddari + "-" + dhingga + ".csv");
                }else if (tipe.equals("ranking")) {
                    laporanfile = new File(Environment.getExternalStorageDirectory(), "FnBApps/report/ranking" + ddari + "-" + dhingga + ".csv");
                }
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
                    if (tipe.equals("penjualan")) {
                        query = "SELECT pjd.kode_penjualan_master,pjm.tanggal_penjualan,pjd.kode_barang,ps.nama_barang,pjd.jumlah,pjd.harga_jual,pjd.diskon," +
                                "pjd.harga_jual-(pjd.harga_jual*(pjd.diskon/100)),pjd.jumlah*(pjd.harga_jual-(pjd.harga_jual*(pjd.diskon/100))) " +
                                "from penjualan_detail pjd " +
                                "INNER JOIN persediaan ps ON ps.kode_barang=pjd.kode_barang " +
                                "INNER JOIN penjualan_master pjm ON pjm.kode_penjualan_master=pjd.kode_penjualan_master " +
                                "WHERE tanggal_penjualan BETWEEN '" + edtanggal_dari.getText().toString() + "' AND '" + edtanggal_hingga.getText().toString() + "' " +
                                "ORDER BY pjm.date_created DESC";
                        Cursor c = db.rawQuery(query, null);
                        sb.append("Transaction Date,Transaction Code,Item Code,Item Name,Amount Sold,Original Selling Price,Discount,Discounted Price,Total Sales");
                        sb.append("\n");
                        while (c.moveToNext()) {
                            sb.append("" + c.getString(1) + "," + c.getString(0) + ",C:" + c.getString(2) + "," +
                                    "" + c.getString(3) + "," + c.getDouble(4) + "," + c.getDouble(5) + "," +
                                    "" + c.getDouble(6) + ","+c.getDouble(7)+"," + c.getDouble(8) + "");
                            sb.append("\n");
                        }
                        c.close();
                        db.close();

                    } else if (tipe.equals("pembelian")) {
                        query = "SELECT pbd.kode_pembelian_master,pbm.tanggal_pembelian,pbd.kode_barang,ps.nama_barang,pbd.jumlah,pbd.harga_beli,pbd.jumlah*pbd.harga_beli from pembelian_detail pbd " +
                                "INNER JOIN persediaan ps ON ps.kode_barang=pbd.kode_barang " +
                                "INNER JOIN pembelian_master pbm ON pbm.kode_pembelian_master=pbd.kode_pembelian_master " +
                                "WHERE tanggal_pembelian BETWEEN '" + edtanggal_dari.getText().toString() + "' AND '" + edtanggal_hingga.getText().toString() + "' " +
                                "ORDER BY pbm.date_created DESC";
                        Cursor c = db.rawQuery(query, null);
                        sb.append("Transaction Date,Transaction Code,Item Code,Item Name,Purchased Amount,Purchased Price,Total Purchased");
                        sb.append("\n");
                        while (c.moveToNext()) {
                            sb.append("" + c.getString(1) + "," + c.getString(0) + ",C:" + c.getString(2) + "," +
                                    "" + c.getString(3) + "," + c.getDouble(4) + "," + c.getDouble(5) + "," +
                                    "" + c.getDouble(6) + "");
                            sb.append("\n");
                        }
                        c.close();
                        db.close();

                    }else if (tipe.equals("stok")) {
                        query = "SELECT kode_barang,nama_barang,jumlah_barang,satuan_barang FROM persediaan ORDER BY jumlah_barang DESC";
                        Cursor c = db.rawQuery(query, null);
                        sb.append("Item Code,Itam Name,Amount,Unit");
                        sb.append("\n");
                        while (c.moveToNext()) {
                            sb.append("C:" + c.getString(0) + "," + c.getString(1) + "," + c.getDouble(2) + "," +
                                    "" + c.getString(3));
                            sb.append("\n");
                        }
                        c.close();
                        db.close();

                    }else if (tipe.equals("ranking")) {
                        query = "SELECT pjd.kode_barang,ps.nama_barang,SUM(pjd.jumlah),ps.satuan_barang FROM penjualan_detail pjd " +
                                "INNER JOIN persediaan ps ON pjd.kode_barang=ps.kode_barang " +
                                "INNER JOIN penjualan_master pjm ON pjd.kode_penjualan_master=pjm.kode_penjualan_master " +
                                "WHERE tanggal_penjualan BETWEEN '" + edtanggal_dari.getText().toString() + "' AND '" + edtanggal_hingga.getText().toString() + "' " +
                                " GROUP BY pjd.kode_barang ORDER BY SUM(pjd.jumlah) DESC";
                        Cursor c = db.rawQuery(query, null);
                        sb.append("Item Code,Item Name,Amount,Unit");
                        sb.append("\n");
                        while (c.moveToNext()) {
                            sb.append("C:" + c.getString(0) + "," + c.getString(1) + "," + c.getDouble(2) + "," +
                                    "" + c.getString(3));
                            sb.append("\n");
                        }
                        c.close();
                        db.close();
                    }

                    bw.write(sb.toString());
                    bw.flush();
                    bw.close();
                    final Uri muri=Uri.fromFile(laporanfile);
                    AlertDialog.Builder adb=new AlertDialog.Builder(LihatLaporanActivity.this);
                    adb.setTitle("Information");
                    adb.setMessage("Data successfully exported, exported data is csv file stored " +
                            "automatically in the Report folder of FnBApps (FnBApps/Report)");
                    adb.setPositiveButton("Open File", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Intent in=new Intent(Intent.ACTION_VIEW);
                                in.setDataAndType(muri,"text/csv");
                                in.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(in);
                            }catch (Exception ex){
                                Toast.makeText(LihatLaporanActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(LihatLaporanActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                v = LayoutInflater.from(ct).inflate(R.layout.lihat_laporan_adapter, parent, false);
                TextView ltanggal = v.findViewById(R.id.ltanggal);
                TextView lkode_trans = v.findViewById(R.id.lkode_trans);
                TextView lnama = v.findViewById(R.id.lnama);
                TextView ljumlah = v.findViewById(R.id.ljumlah);
                TextView lharga = v.findViewById(R.id.lharga);

                if(tipe.equals("stok")||tipe.equals("ranking")){
                    ltanggal.setVisibility(View.GONE);
                    float density=getApplicationContext().getResources().getDisplayMetrics().density;
                    lkode_trans.getLayoutParams().width= Math.round(90 * density);
                    lkode_trans.setText(ls.get(position).getKode_master());
                    lnama.setText(ls.get(position).getTanggal());
                    ljumlah.setVisibility(View.GONE);
                    lharga.setText(nf.format(ls.get(position).getJumlah())+" "+ls.get(position).getNama());
                }else {
                    ltanggal.setText(ls.get(position).getTanggal().substring(2));
                    lkode_trans.setText(ls.get(position).getKode_master());
                    lnama.setText(ls.get(position).getNama());
                    if (ls.get(position).getDiskon() == 0) {
                        ljumlah.setText(nf.format(ls.get(position).getJumlah()));
                    } else {
                        ljumlah.setText(nf.format(ls.get(position).getJumlah()) + "/@" + nf.format(ls.get(position).getDiskon()) + "%");
                    }

                    lharga.setText(nf.format(ls.get(position).getTotal()));

                }

            }
            return v;
        }


    }

    public class reportmodel {
        String kode_master, tanggal, nama;
        double jumlah;
        double harga_jual, diskon, total;

        public reportmodel(String kode_master, String tanggal, String nama, double jumlah, double harga_jual, double diskon, double total) {
            this.kode_master = kode_master;
            this.tanggal = tanggal;
            this.nama = nama;
            this.jumlah = jumlah;
            this.harga_jual = harga_jual;
            this.diskon = diskon;
            this.total = total;
        }

        public String getKode_master() {
            return kode_master;
        }

        public void setKode_master(String kode_master) {
            this.kode_master = kode_master;
        }

        public String getTanggal() {
            return tanggal;
        }

        public void setTanggal(String tanggal) {
            this.tanggal = tanggal;
        }

        public String getNama() {
            return nama;
        }

        public void setNama(String nama) {
            this.nama = nama;
        }

        public double getJumlah() {
            return jumlah;
        }

        public void setJumlah(double jumlah) {
            this.jumlah = jumlah;
        }

        public double getHarga_jual() {
            return harga_jual;
        }

        public void setHarga_jual(double harga_jual) {
            this.harga_jual = harga_jual;
        }

        public double getDiskon() {
            return diskon;
        }

        public void setDiskon(double diskon) {
            this.diskon = diskon;
        }

        public double getTotal() {
            return total;
        }

        public void setTotal(double total) {
            this.total = total;
        }
    }
}
