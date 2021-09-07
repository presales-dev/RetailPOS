package com.dfit.dfpos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PersediaanActivity extends AppCompatActivity {

    FloatingActionButton fbadd;
    ImageView breload;
    RecyclerView rvdata;
    RecyclerView.LayoutManager layman;
    PersediaanAdapter adapter;
    android.widget.SearchView svdata;
    Dblocalhelper dbo;
    ArrayList<PersediaanModel> lsdata = new ArrayList<>();
    private int currentoffset = 0;
    SharedPreferences sp;
    int tipe=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persediaan);
        fbadd = findViewById(R.id.fbadd);
        breload = findViewById(R.id.breload);
        rvdata = findViewById(R.id.rvdata);
        sp=getApplicationContext().getSharedPreferences("config",0);
        tipe=sp.getInt("view_tipe",0);
        if(tipe==0){
            layman = new LinearLayoutManager(this);
        }else{
            layman = new GridLayoutManager(this,Oneforallfunc.calculateNoOfColumns(this));
        }
        rvdata.setLayoutManager(layman);
        rvdata.setHasFixedSize(true);
        rvdata.setItemAnimator(new DefaultItemAnimator());
        adapter = new PersediaanAdapter(lsdata, this);
        rvdata.setAdapter(adapter);
        svdata = findViewById(R.id.svdata);
        dbo = new Dblocalhelper(this);
        adddata();
        lsdata.clear();
        loaddata();
        rvdata.addOnScrollListener(new EndlessScroll() {
            @Override
            public void onLoadMore() {
                if (svdata.getQuery() == null || (svdata.getQuery().toString().equals(""))) {
                    //Toast.makeText(PersediaanActivity.this, "Load data", Toast.LENGTH_SHORT).show();
                    loaddata();
                    currentoffset = currentoffset + 100;
                }

            }
        });
        caridata();
        reloaddata();
        //svdata.setQueryHint("Cari : Ketik nama atau kode");
    }

    private void adddata() {
        fbadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sp.getInt("write_persediaan",0)==1) {
                    Intent in = new Intent(PersediaanActivity.this, TambahPersediaanActivity.class);
                    startActivity(in);
                }else{
                    Toast.makeText(PersediaanActivity.this, "Process Denied, You do not have the access right", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loaddata() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbo.getReadableDatabase();
                try {
                    String query = "SELECT kode_barang,nama_barang,satuan_barang,harga_beli,harga_jual," +
                            "jumlah_barang,gambar_barang,tipe_barang,diskon FROM persediaan ORDER BY date_created DESC LIMIT 100 OFFSET " + currentoffset + " ";
                    Cursor c = db.rawQuery(query, null);
                    while (c.moveToNext()) {

                        lsdata.add(new PersediaanModel(c.getString(0), c.getString(1),
                                c.getString(2), c.getInt(3), c.getDouble(4),
                                c.getDouble(5), c.getString(6),c.getInt(7),c.getDouble(8)));
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
                            String query = "SELECT kode_barang,nama_barang,satuan_barang,harga_beli,harga_jual," +
                                    "jumlah_barang,gambar_barang,tipe_barang,diskon FROM persediaan WHERE " +
                                    "kode_barang LIKE '%" + setquery + "%' OR " +
                                    "nama_barang LIKE '%" + setquery + "%' ORDER BY date_created DESC LIMIT 100 ";
                            Cursor c = db.rawQuery(query, null);
                            while (c.moveToNext()) {
                                lsdata.add(new PersediaanModel(c.getString(0), c.getString(1),
                                        c.getString(2), c.getInt(3), c.getDouble(4),
                                        c.getDouble(5), c.getString(6),c.getInt(7),c.getDouble(8)));
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

    @Override
    protected void onRestart() {
        super.onRestart();
        EndlessScroll.mPreviousTotal = 0;
        lsdata.clear();
        currentoffset = 0;
        loaddata();
        svdata.setQuery("", false);
    }

    public class PersediaanAdapter extends RecyclerView.Adapter {
        ArrayList<PersediaanModel> model = new ArrayList<>();
        Context ct;
        NumberFormat nf = NumberFormat.getInstance();
        int content = 0;

        public PersediaanAdapter(ArrayList<PersediaanModel> model, Context ct) {
            this.model = model;
            this.ct = ct;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater lin = LayoutInflater.from(parent.getContext());
            View v;
            if(tipe==0){
                v = lin.inflate(R.layout.adapter_persediaan, parent, false);
            }else{
                v = lin.inflate(R.layout.adapter_persediaan_grid, parent, false);
            }

            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof Holder) {
                Holder h = (Holder) holder;
                h.lnama_barang.setText(model.get(position).getNama_barang());
                h.lkode_barang.setText(model.get(position).getKode_barang());
                h.lharga_beli.setText("Buy : " + nf.format(model.get(position).getHarga_beli()));
                h.lharga_jual.setText("Sell : " + nf.format(model.get(position).getHarga_jual()));//nf.format(model.get(position).getDiskon())+"%" diskonan
                h.ljumlah.setText("Stock : " + nf.format(model.get(position).getJumlah_barang())); //model.get(position).getSatuan_barang() menampilkan satuan
                Glide.with(ct).
                        load(new File(model.get(position).getGambar_barang())).
                        placeholder(R.drawable.ic_image_black_24dp).
                        centerCrop().
                        diskCacheStrategy(DiskCacheStrategy.ALL).
                        into(h.gambar_barang);
                h.img_optionmenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popmenu = new PopupMenu(ct, v);
                        popmenu.inflate(R.menu.persedian_option_menu);
                        popmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.medit:
                                        Intent in = new Intent(ct, TambahPersediaanActivity.class);
                                        in.putExtra("kode_barang", model.get(position).getKode_barang());
                                        startActivity(in);
                                        break;
                                    case R.id.mhapus:
                                        AlertDialog.Builder adb = new AlertDialog.Builder(ct);
                                        adb.setTitle("Confirmation");
                                        adb.setMessage("You would like to delete " + model.get(position).getNama_barang() + " ? ");
                                        adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                SQLiteDatabase db = dbo.getWritableDatabase();
                                                db.beginTransaction();
                                                try {
                                                    db.execSQL("DELETE FROM persediaan WHERE kode_barang='" + model.get(position).getKode_barang() + "'");
                                                    db.execSQL("DELETE FROM racikan WHERE kode_barang_racik='" + model.get(position).getKode_barang() + "'");
                                                    db.setTransactionSuccessful();
                                                    model.remove(position);
                                                    notifyDataSetChanged();
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                } finally {
                                                    db.endTransaction();
                                                    db.close();
                                                }
                                            }
                                        });
                                        adb.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        adb.show();
                                        break;
                                    /*case R.id.mracik:
                                        if(model.get(position).getTipe_persediaan()==0){
                                            Toast.makeText(ct, "Not a concoction good", Toast.LENGTH_SHORT).show();
                                        }else{
                                           Intent ins=new Intent(ct,RacikActivity.class);
                                           ins.putExtra("kode_barang",model.get(position).getKode_barang());
                                           ins.putExtra("nama_barang",model.get(position).getNama_barang());
                                           startActivity(ins);
                                        }*/
                                }
                                return false;
                            }
                        });
                        popmenu.show();
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
        TextView lnama_barang, lkode_barang, lharga_beli, lharga_jual, ljumlah;
        ImageView gambar_barang, img_optionmenu;

        public Holder(View itemView) {
            super(itemView);
            lnama_barang = itemView.findViewById(R.id.lnama_barang);
            lkode_barang = itemView.findViewById(R.id.lkode_barang);
            lharga_beli = itemView.findViewById(R.id.ltotal_harga_final);
            lharga_jual = itemView.findViewById(R.id.lharga_jual);
            ljumlah = itemView.findViewById(R.id.ljudul);
            gambar_barang = itemView.findViewById(R.id.gambar_barang);
            img_optionmenu = itemView.findViewById(R.id.img_optionmenu);
        }
    }

    public class PersediaanModel {
        String kode_barang, nama_barang, satuan_barang;
        double harga_beli, harga_jual, jumlah_barang;
        String gambar_barang;
        int tipe_persediaan;
        double diskon;

        public PersediaanModel(String kode_barang, String nama_barang, String satuan_barang, double harga_beli, double harga_jual, double jumlah_barang, String gambar_barang, int tipe_persediaan, double diskon) {
            this.kode_barang = kode_barang;
            this.nama_barang = nama_barang;
            this.satuan_barang = satuan_barang;
            this.harga_beli = harga_beli;
            this.harga_jual = harga_jual;
            this.jumlah_barang = jumlah_barang;
            this.gambar_barang = gambar_barang;
            this.tipe_persediaan = tipe_persediaan;
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

        public int getTipe_persediaan() {
            return tipe_persediaan;
        }

        public void setTipe_persediaan(int tipe_persediaan) {
            this.tipe_persediaan = tipe_persediaan;
        }

        public double getDiskon() {
            return diskon;
        }

        public void setDiskon(double diskon) {
            this.diskon = diskon;
        }
    }
}
