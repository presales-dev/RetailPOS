package com.dfit.dfpos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import android.os.Bundle;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RacikActivity extends AppCompatActivity {

    public static ArrayList<RacikModel> lsdata = new ArrayList<>();
    FloatingActionButton fbadd;
    EditText ednama_barang;
    Button bsimpan;
    RecyclerView rvdata;
    RecyclerView.LayoutManager layman;
    RacikAdapter adapter;
    Dblocalhelper dbo;
    ImageView bimg_refresh;
    TextView ltotal;
    NumberFormat nf = NumberFormat.getInstance();
    String kode_barang="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_racik);
        fbadd = findViewById(R.id.fbadd);
        rvdata = findViewById(R.id.rvdata);
        bsimpan = findViewById(R.id.bsimpan);
        ltotal = findViewById(R.id.ltotal);
        ednama_barang=findViewById(R.id.ednama_barang);
        bimg_refresh=findViewById(R.id.bimg_refresh);
        layman = new LinearLayoutManager(this);
        rvdata.setLayoutManager(layman);
        rvdata.setHasFixedSize(true);
        rvdata.setItemAnimator(new DefaultItemAnimator());
        adapter = new RacikAdapter(lsdata, this);
        rvdata.setAdapter(adapter);
        dbo = new Dblocalhelper(this);
        Bundle ex = getIntent().getExtras();
        kode_barang=ex.getString("kode_barang");
        ednama_barang.setText(ex.getString("nama_barang"));
        ednama_barang.setEnabled(false);
        lsdata.clear();
        loaddata();
        savedata();
        caribarang();


    }


    private void loaddata() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbo.getReadableDatabase();
                try {

                    Cursor c = db.rawQuery("SELECT rc.kode_barang_isi,pd.nama_barang,rc.jumlah_isi," +
                            "pd.harga_beli,pd.gambar_barang " +
                            "FROM racikan rc INNER JOIN persediaan pd ON rc.kode_barang_isi=pd.kode_barang  " +
                            "WHERE rc.kode_barang_racik='" + kode_barang + "' ", null);
                    while (c.moveToNext()) {
                        lsdata.add(new RacikModel(c.getString(0), c.getString(1),
                                c.getDouble(2), c.getDouble(3),
                                c.getDouble(2)*c.getDouble(3),
                                c.getString(4)));
                    }
                    ;

                    adapter.notifyDataSetChanged();
                    double total = 0;
                    for (int i = 0; i < lsdata.size(); i++) {
                        total = total + lsdata.get(i).getTotal_harga();
                    }
                    ltotal.setText(nf.format(total));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.close();
                }
            }
        }, 100);


    }

    private void rawsavedata() {
        SQLiteDatabase db = dbo.getWritableDatabase();
        try {
            db.beginTransaction();
            db.execSQL("DELETE FROM racikan WHERE kode_barang_racik='" + kode_barang + "'");
                for (int i = 0; i < lsdata.size(); i++) {
                    db.execSQL("INSERT INTO racikan(kode_racik,kode_barang_racik,kode_barang_isi,jumlah_isi) " +
                            "VALUES('" + kode_barang + i + "','" + kode_barang + "'," + "'" + lsdata.get(i).getKode_barang() +
                            "'," + lsdata.get(i).getJumlah()+")");
                }
            db.setTransactionSuccessful();
            Toast.makeText(RacikActivity.this, "Data Racik "+ednama_barang.getText().toString()+" Berhasil Disimpan", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private void savedata() {
        bsimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rawsavedata();
                AlertDialog.Builder adb=new AlertDialog.Builder(RacikActivity.this);
                adb.setTitle("Informasi");
                adb.setMessage("Jadikan total harga racikan sebagai harga modal?");
                adb.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = dbo.getWritableDatabase();
                        try {
                            db.beginTransaction();
                            String replacer= ltotal.getText().toString().replace(".","");
                            db.execSQL("UPDATE persediaan SET harga_beli="+
                                   replacer.replace(",",".")+" " +
                                    "WHERE kode_barang='" + kode_barang + "'");

                            db.setTransactionSuccessful();
                            Toast.makeText(RacikActivity.this, "Data "+ednama_barang.getText().toString()+" Berhasil Diupdate", Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            db.endTransaction();
                            db.close();
                        }
                    }
                });
                adb.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                adb.setCancelable(false);
                adb.show();
            }
        });
    }

    private void caribarang() {
        fbadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(RacikActivity.this, CariBarangActivity.class);
                in.putExtra("tipe_transaksi", "racik");
                startActivity(in);
            }
        });
    }

    private void reload(){
        bimg_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lsdata.clear();
                loaddata();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        adapter.notifyDataSetChanged();
        double total = 0;
        for (int i = 0; i < lsdata.size(); i++) {
            total = total + lsdata.get(i).getTotal_harga();
        }
        ltotal.setText(nf.format(total));
    }

    public static class RacikModel {
        String kode_barang,nama_barang;
        double jumlah,harga_beli,total_harga;
        String gambar_barang;

        public RacikModel(String kode_barang, String nama_barang, double jumlah, double harga_beli, double total_harga, String gambar_barang) {
            this.kode_barang = kode_barang;
            this.nama_barang = nama_barang;
            this.jumlah = jumlah;
            this.harga_beli = harga_beli;
            this.total_harga = total_harga;
            this.gambar_barang = gambar_barang;
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

        public double getJumlah() {
            return jumlah;
        }

        public void setJumlah(double jumlah) {
            this.jumlah = jumlah;
        }

        public double getHarga_beli() {
            return harga_beli;
        }

        public void setHarga_beli(double harga_beli) {
            this.harga_beli = harga_beli;
        }

        public double getTotal_harga() {
            return total_harga;
        }

        public void setTotal_harga(double total_harga) {
            this.total_harga = total_harga;
        }

        public String getGambar_barang() {
            return gambar_barang;
        }

        public void setGambar_barang(String gambar_barang) {
            this.gambar_barang = gambar_barang;
        }
    }


    public class RacikAdapter extends RecyclerView.Adapter {
        ArrayList<RacikModel> model = new ArrayList<>();
        Context ct;
        NumberFormat nf = NumberFormat.getInstance();
        int content = 0;

        public RacikAdapter(ArrayList<RacikModel> model, Context ct) {
            this.model = model;
            this.ct = ct;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater lin = LayoutInflater.from(parent.getContext());
            View v = lin.inflate(R.layout.adapter_racik, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof Holder) {
                final Holder h = (Holder) holder;
                h.lnama_barang.setText(model.get(position).getNama_barang());
                h.lkode_barang.setText(model.get(position).getKode_barang());
                h.edjumlah.setText(nf.format(model.get(position).getJumlah()));
                double total_harga = model.get(position).getHarga_beli() * model.get(position).getJumlah();
                h.ltotal_harga.setText(nf.format(total_harga));
                Glide.with(ct).
                        load(new File(model.get(position).getGambar_barang())).
                        placeholder(R.drawable.ic_assessment_70dp).
                        centerCrop().
                        diskCacheStrategy(DiskCacheStrategy.ALL).
                        into(h.gambar_barang);
                h.edjumlah.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            model.get(position).setJumlah(Double.parseDouble(v.getText().toString()));
                            double total_harga = model.get(position).getJumlah() * model.get(position).getHarga_beli();
                            model.get(position).setTotal_harga(total_harga);
                            notifyItemChanged(position);
                            double total = 0;
                            for (int i = 0; i < lsdata.size(); i++) {
                                total = total + lsdata.get(i).getTotal_harga();
                            }
                            ltotal.setText(nf.format(total));
                        }
                        return false;
                    }
                });

                h.bset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        model.get(position).setJumlah(Double.parseDouble(h.edjumlah.getText().toString()));
                        double total_harga = model.get(position).getJumlah() * model.get(position).getHarga_beli();
                        model.get(position).setTotal_harga(total_harga);
                        notifyItemChanged(position);
                        double total = 0;
                        for (int i = 0; i < lsdata.size(); i++) {
                            total = total + lsdata.get(i).getTotal_harga();
                        }
                        ltotal.setText(nf.format(total));
                    }
                });

                h.img_hapus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(ct);
                        adb.setTitle("Konfirmasi");
                        adb.setMessage("Yakin ingin menghapus " + model.get(position).getNama_barang() + " ? ");
                        adb.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    model.remove(position);
                                    notifyDataSetChanged();
                                    double total = 0;
                                    for (int i = 0; i < lsdata.size(); i++) {
                                        total = total + lsdata.get(i).getTotal_harga();
                                    }
                                    ltotal.setText(nf.format(total));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                        adb.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        adb.show();
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
        TextView lnama_barang, lkode_barang, ltotal_harga;
        EditText edjumlah;
        Button bset;
        ImageView gambar_barang, img_hapus;

        public Holder(View itemView) {
            super(itemView);
            lnama_barang = itemView.findViewById(R.id.lnama_barang);
            lkode_barang = itemView.findViewById(R.id.lkode_barang);
            ltotal_harga = itemView.findViewById(R.id.ltotal_harga_final);
            edjumlah = itemView.findViewById(R.id.edjumlah);
            bset = itemView.findViewById(R.id.bset);
            gambar_barang = itemView.findViewById(R.id.gambar_barang);
            img_hapus = itemView.findViewById(R.id.img_hapus);
        }
    }
}
