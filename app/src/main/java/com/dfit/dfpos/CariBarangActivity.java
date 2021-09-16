package com.dfit.dfpos;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
//import android.support.annotation.NonNull;
//import android.support.design.widget.FloatingActionButton;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
/*import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;*/
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;

public class CariBarangActivity extends AppCompatActivity {

    FloatingActionButton fbadd;
    ImageView breload;
    RecyclerView rvdata;
    RecyclerView.LayoutManager layman;
    CariBarangAdapter adapter;
    android.widget.SearchView svdata;
    Dblocalhelper dbo;
    ArrayList<CariBarangModel> lsdata = new ArrayList<>();
    String tipe_transaksi = "";
    int tipe = 0;
    private int currentoffset = 0;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cari_barang);
        fbadd = findViewById(R.id.fbadd);
        breload = findViewById(R.id.breload);
        rvdata = findViewById(R.id.rvdata);
        sp=getApplicationContext().getSharedPreferences("config",0);
        tipe=sp.getInt("view_tipe",0);
        if (tipe == 0) {
            layman = new LinearLayoutManager(this);
        } else {
            layman = new GridLayoutManager(this, Oneforallfunc.calculateNoOfColumns(this));
        }

        rvdata.setLayoutManager(layman);
        rvdata.setHasFixedSize(true);
        rvdata.setItemAnimator(new DefaultItemAnimator());
        adapter = new CariBarangAdapter(lsdata, this);
        rvdata.setAdapter(adapter);
        svdata = findViewById(R.id.svdata);
        dbo = new Dblocalhelper(this);
        lsdata.clear();
        loaddata();
        rvdata.addOnScrollListener(new EndlessScroll() {
            @Override
            public void onLoadMore() {
                if (svdata.getQuery() == null || (svdata.getQuery().toString().equals(""))) {
                    Toast.makeText(CariBarangActivity.this, "Load data", Toast.LENGTH_SHORT).show();
                    loaddata();
                    currentoffset = currentoffset + 100;
                }
            }
        });
        caridata();
        reloaddata();
        Bundle ex = getIntent().getExtras();
        tipe_transaksi = ex.getString("tipe_transaksi");
    }


    private void loaddata() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbo.getReadableDatabase();
                try {
                    String query = "";
                    if (tipe_transaksi.equals("racik")) {
                        query = "SELECT kode_barang,nama_barang,satuan_barang,harga_beli,harga_jual," +
                                "jumlah_barang,gambar_barang,tipe_barang,diskon FROM persediaan WHERE tipe_barang=0 LIMIT 100 OFFSET " + currentoffset + " ";
                    } else {
                        query = "SELECT kode_barang,nama_barang,satuan_barang,harga_beli,harga_jual," +
                                "jumlah_barang,gambar_barang,tipe_barang,diskon FROM persediaan LIMIT 100 OFFSET " + currentoffset + " ";
                    }

                    Cursor c = db.rawQuery(query, null);
                    while (c.moveToNext()) {
                        if(c.getDouble(5)==0){
                            continue;
                        }else{
                            lsdata.add(new CariBarangModel(c.getString(0), c.getString(1),
                                    c.getString(2), c.getInt(3), c.getDouble(4),
                                    c.getDouble(5), c.getString(6), c.getInt(7), c.getDouble(8)));
                        }
                    }
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.close();
                }
            }
        }, 100);


    }

    private void caridata() {
        svdata.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final String setquery = query;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        lsdata.clear();
                        SQLiteDatabase db = dbo.getReadableDatabase();
                        try {
                            String query = "";
                            if (tipe_transaksi.equals("racik")) {
                                query = "SELECT kode_barang,nama_barang,satuan_barang,harga_beli,harga_jual," +
                                        "jumlah_barang,gambar_barang,tipe_barang,diskon FROM persediaan WHERE tipe_barang=0 AND " +
                                        "(kode_barang LIKE '%" + setquery + "%' OR " +
                                        "nama_barang LIKE '%" + setquery + "%') LIMIT 100 ";
                            } else {
                                query = "SELECT kode_barang,nama_barang,satuan_barang,harga_beli,harga_jual," +
                                        "jumlah_barang,gambar_barang,tipe_barang,diskon FROM persediaan WHERE " +
                                        "kode_barang LIKE '%" + setquery + "%' OR " +
                                        "nama_barang LIKE '%" + setquery + "%' LIMIT 100 ";
                            }
                            Cursor c = db.rawQuery(query, null);
                            while (c.moveToNext()) {
                                lsdata.add(new CariBarangModel(c.getString(0), c.getString(1),
                                        c.getString(2), c.getInt(3), c.getDouble(4),
                                        c.getDouble(5), c.getString(6), c.getInt(7), c.getDouble(8)));
                            }
                            ;
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            db.close();
                        }
                    }
                }, 100);


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void reloaddata() {
        breload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EndlessScroll.mPreviousTotal = 0;
                lsdata.clear();
                currentoffset = 0;
                loaddata();
                svdata.setQuery("", false);
            }
        });
    }

    public class CariBarangAdapter extends RecyclerView.Adapter {
        ArrayList<CariBarangModel> model = new ArrayList<>();
        Context ct;
        NumberFormat nf = NumberFormat.getInstance();
        int content = 0;

        public CariBarangAdapter(ArrayList<CariBarangModel> model, Context ct) {
            this.model = model;
            this.ct = ct;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater lin = LayoutInflater.from(parent.getContext());
            View v;
            if (tipe == 0) {
                v = lin.inflate(R.layout.adapter_cari_barang, parent, false);
            } else {
                v = lin.inflate(R.layout.adapter_cari_barang_grid, parent, false);
            }

            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof Holder) {
                Holder h = (Holder) holder;
                h.lnama_barang.setText(model.get(position).getNama_barang());
                h.lkodebarang.setText(model.get(position).getKode_barang());
                h.lharga_beli.setText("Buy : " + nf.format(model.get(position).getHarga_beli()));
                h.lharga_jual.setText("Sell : " + nf.format(model.get(position).getHarga_jual()) + "/" + nf.format(model.get(position).getDiskon()));
                h.ljumlah.setText("Stock : " + nf.format(model.get(position).getJumlah_barang()) ); //+ " " + model.get(position).getSatuan_barang()
                Glide.with(ct).
                        load(new File(model.get(position).getGambar_barang())).
                        placeholder(R.drawable.ic_assessment_70dp).
                        centerCrop().
                        diskCacheStrategy(DiskCacheStrategy.ALL).
                        into(h.gambar_barang);

                h.img_add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (tipe_transaksi.equals("beli")) {

                            int posisiindex = -1;

                            for (int i = 0; i < TambahPembelianActivity.lsdata.size(); i++) {
                                TambahPembelianActivity.TambahPembelianModel inmodel = TambahPembelianActivity.lsdata.get(i);
                                if (inmodel.getKode_barang().equals(model.get(position).getKode_barang())) {
                                    posisiindex = i;
                                }
                            }

                            if (posisiindex < 0) {
                                TambahPembelianActivity.lsdata.add(new TambahPembelianActivity.TambahPembelianModel(
                                        model.get(position).getKode_barang(),
                                        model.get(position).getNama_barang(),
                                        model.get(position).getSatuan_barang(),
                                        model.get(position).getHarga_beli(),
                                        1,
                                        model.get(position).getHarga_beli() * 1,
                                        model.get(position).getGambar_barang()

                                ));
                                Toast.makeText(ct, "1 Item Added", Toast.LENGTH_SHORT).show();
                            } else {
                                double jumlahawal = TambahPembelianActivity.lsdata.get(posisiindex).getJumlah();
                                double harga_beli = TambahPembelianActivity.lsdata.get(posisiindex).getHarga_beli();
                                TambahPembelianActivity.lsdata.get(posisiindex).setJumlah(jumlahawal + 1);
                                TambahPembelianActivity.lsdata.get(posisiindex).setTotal(harga_beli * (jumlahawal + 1));

                                Toast.makeText(ct, model.get(position).getNama_barang() + " Added 1", Toast.LENGTH_SHORT).show();
                            }

                        } else if (tipe_transaksi.equals("jual")) {

                            int posisiindex = -1;

                            for (int i = 0; i < TambahPenjualanActivity.lsdata.size(); i++) {
                                TambahPenjualanActivity.TambahpenjualanModel inmodel = TambahPenjualanActivity.lsdata.get(i);
                                if (inmodel.getKode_barang().equals(model.get(position).getKode_barang())) {
                                    posisiindex = i;
                                }
                            }

                            if (posisiindex < 0) {
                                double input_add = Double.parseDouble(h.addcount.getText().toString());
                                double jumlahawal = model.get(position).getJumlah_barang();
                                if(jumlahawal-input_add<0){
                                    Toast.makeText(ct, "Stok "+model.get(position).getNama_barang() + " Tidak Cukup hanya tersedia "+Math.round(jumlahawal), Toast.LENGTH_SHORT).show();
                                }else{
                                    TambahPenjualanActivity.lsdata.add(new TambahPenjualanActivity.TambahpenjualanModel(
                                            model.get(position).getKode_barang(),
                                            model.get(position).getNama_barang(),
                                            model.get(position).getSatuan_barang(),
                                            model.get(position).getHarga_jual(),
                                            input_add,
                                            (model.get(position).getHarga_jual() - (model.get(position).getHarga_jual() * (model.get(position).getDiskon() / 100))) * 1,
                                            model.get(position).getDiskon(),
                                            model.get(position).getGambar_barang(),
                                            model.get(position).getTipe_barang(),
                                            model.get(position).getHarga_beli()
                                    ));
                                    Toast.makeText(ct, model.get(position).getNama_barang() + " Berhasil ditambahkan sebanyak "+Math.round(input_add), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                double diskonpersen = TambahPenjualanActivity.lsdata.get(posisiindex).getDiskon();
                                double diskonnominal = TambahPenjualanActivity.lsdata.get(posisiindex).getHarga_jual() * (diskonpersen / 100);
                                double diskon = TambahPenjualanActivity.lsdata.get(posisiindex).getDiskon();
                                double jumlahawal = TambahPenjualanActivity.lsdata.get(posisiindex).getJumlah();
                                double harga_jual = TambahPenjualanActivity.lsdata.get(posisiindex).getHarga_jual();
                                double input_add = Double.parseDouble(h.addcount.getText().toString());
                                if((model.get(position).getJumlah_barang()-jumlahawal)-input_add<0){
                                    Toast.makeText(ct, "Stok "+model.get(position).getNama_barang() + " Tidak Cukup, Sisa "+Math.round(model.get(position).getJumlah_barang()-jumlahawal), Toast.LENGTH_SHORT).show();
                                }else{
                                    TambahPenjualanActivity.lsdata.get(posisiindex).setJumlah(jumlahawal + input_add);
                                    TambahPenjualanActivity.lsdata.get(posisiindex).setTotal((harga_jual - diskon) * (jumlahawal + 1));
                                    int stoksekarang = (int)TambahPenjualanActivity.lsdata.get(posisiindex).getJumlah();
                                    Toast.makeText(ct, model.get(position).getNama_barang() + " Ditambahkan sebanyak "+ Math.round(input_add)+"\nTotal dikeranjang "+Math.round(input_add+jumlahawal), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else if (tipe_transaksi.equals("racik")) {

                            int posisiindex = -1;

                            for (int i = 0; i < RacikActivity.lsdata.size(); i++) {
                                RacikActivity.RacikModel inmodel = RacikActivity.lsdata.get(i);
                                if (inmodel.getKode_barang().equals(model.get(position).getKode_barang())) {
                                    posisiindex = i;
                                }
                            }

                            if (posisiindex < 0) {
                                RacikActivity.lsdata.add(new RacikActivity.RacikModel(
                                        model.get(position).getKode_barang(),
                                        model.get(position).getNama_barang(),
                                        1,
                                        model.get(position).getHarga_beli(),
                                        model.get(position).getHarga_beli(),
                                        model.get(position).getGambar_barang()

                                ));
                                Toast.makeText(ct, "1 item added", Toast.LENGTH_SHORT).show();
                            } else {
                                double jumlahawal = RacikActivity.lsdata.get(posisiindex).getJumlah();
                                double harga_beli = RacikActivity.lsdata.get(posisiindex).getHarga_beli();
                                RacikActivity.lsdata.get(posisiindex).setJumlah(jumlahawal + 1);
                                RacikActivity.lsdata.get(posisiindex).setTotal_harga(harga_beli * (jumlahawal + 1));
                                Toast.makeText(ct, model.get(position).getNama_barang() + " Added 1", Toast.LENGTH_SHORT).show();

                            }
                        }


                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return model.size();
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }
    }


    public class Holder extends RecyclerView.ViewHolder {
        TextView lnama_barang, lkodebarang, lharga_beli, lharga_jual, ljumlah;
        ImageView gambar_barang, img_add;
        EditText addcount;

        public Holder(View itemView) {
            super(itemView);
            lnama_barang = itemView.findViewById(R.id.lnama_barang);
            lkodebarang = itemView.findViewById(R.id.lkodebarang);
            lharga_beli = itemView.findViewById(R.id.ltotal_harga_final);
            lharga_jual = itemView.findViewById(R.id.lharga_jual);
            ljumlah = itemView.findViewById(R.id.ljudul);
            gambar_barang = itemView.findViewById(R.id.gambar_barang);
            img_add = itemView.findViewById(R.id.img_add);
            addcount = itemView.findViewById(R.id.addcount);
        }
    }

    public class CariBarangModel {
        String kode_barang, nama_barang, satuan_barang;
        double harga_beli, harga_jual, jumlah_barang;
        String gambar_barang;
        int tipe_barang;
        double diskon;

        public CariBarangModel(String kode_barang, String nama_barang, String satuan_barang, double harga_beli, double harga_jual, double jumlah_barang, String gambar_barang, int tipe_barang, double diskon) {
            this.kode_barang = kode_barang;
            this.nama_barang = nama_barang;
            this.satuan_barang = satuan_barang;
            this.harga_beli = harga_beli;
            this.harga_jual = harga_jual;
            this.jumlah_barang = jumlah_barang;
            this.gambar_barang = gambar_barang;
            this.tipe_barang = tipe_barang;
            this.diskon = diskon;
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

        public double getHarga_beli() {
            return harga_beli;
        }

        public void setHarga_beli(double harga_beli) {
            this.harga_beli = harga_beli;
        }

        public double getHarga_jual() {
            return harga_jual;
        }

        public void setHarga_jual(double harga_jual) {
            this.harga_jual = harga_jual;
        }

        public double getJumlah_barang() {
            return jumlah_barang;
        }

        public void setJumlah_barang(double jumlah_barang) {
            this.jumlah_barang = jumlah_barang;
        }

        public String getGambar_barang() {
            return gambar_barang;
        }

        public void setGambar_barang(String gambar_barang) {
            this.gambar_barang = gambar_barang;
        }

        public int getTipe_barang() {
            return tipe_barang;
        }

        public void setTipe_barang(int tipe_barang) {
            this.tipe_barang = tipe_barang;
        }

        public double getDiskon() {
            return diskon;
        }

        public void setDiskon(double diskon) {
            this.diskon = diskon;
        }
    }
}
