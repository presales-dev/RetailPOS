package com.dfit.dfpos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Vibrator;

import android.os.Bundle;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PosBarcodeActivity extends AppCompatActivity {

    FloatingActionButton fbadd;
    ImageView breload;
    RecyclerView rvdata;
    RecyclerView.LayoutManager layman;
    PosBarcodeAdapter adapter;
    Dblocalhelper dbo;
    ArrayList<PosBarcodeModel> lsdata = new ArrayList<>();
    String tipe_transaksi = "";
    SurfaceView surfv;
    TextView ltotal_harga_final;
    CameraSource camsource;
    BarcodeDetector barcodedetect;
    Vibrator vib;
    ToneGenerator toneg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos_barcode);
        fbadd = findViewById(R.id.fbadd);
        breload = findViewById(R.id.breload);
        rvdata = findViewById(R.id.rvdata);
        layman = new LinearLayoutManager(this);
        rvdata.setLayoutManager(layman);
        rvdata.setHasFixedSize(true);
        rvdata.setItemAnimator(new DefaultItemAnimator());
        adapter = new PosBarcodeAdapter(lsdata, this);
        rvdata.setAdapter(adapter);
        surfv=findViewById(R.id.surfv);
        dbo = new Dblocalhelper(this);
        lsdata.clear();
        Bundle ex = getIntent().getExtras();
        tipe_transaksi = ex.getString("tipe_transaksi");
        vib=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        toneg=new ToneGenerator(AudioManager.STREAM_RING,200);

        barcodedetect = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build();
        camsource = new CameraSource.Builder(this, barcodedetect).
                setRequestedPreviewSize(1600, 1024).setAutoFocusEnabled(true).build();

        surfv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(PosBarcodeActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PosBarcodeActivity.this,
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
                    adddata(barcodes.valueAt(0).displayValue);
                }

            }


        });
    }


    private void adddata(String kode_barang){
        SQLiteDatabase db = dbo.getReadableDatabase();
        String query = "SELECT kode_barang,nama_barang,satuan_barang,harga_beli,harga_jual," +
                "jumlah_barang,gambar_barang,tipe_barang,harga_beli FROM persediaan WHERE " +
                "kode_barang ='" + kode_barang + "' LIMIT 1";
        Cursor c=db.rawQuery(query,null);

        if(c.moveToFirst()){
            int posisiindexout = -1;
            for (int i = 0; i < lsdata.size(); i++) {
                PosBarcodeModel model = lsdata.get(i);
                if (model.getKode_barang().equals(c.getString(0))) {
                    posisiindexout = i;
                }
            }

            if (posisiindexout < 0) {
                lsdata.add(new PosBarcodeModel(
                        c.getString(0),
                        c.getString(1),
                        c.getString(2),
                        c.getDouble(3),
                        c.getDouble(4),
                        1,
                        c.getString(6),
                        c.getInt(7)

                ));
               // Toast.makeText(PosBarcodeActivity.this, "1 Barang Baru Ditambahkan", Toast.LENGTH_SHORT).show();
            } else {
                double jumlahawal = lsdata.get(posisiindexout).getJumlah_barang();
                double harga_beli = lsdata.get(posisiindexout).getHarga_beli();
                lsdata.get(posisiindexout).setJumlah_barang(jumlahawal + 1);
                //Toast.makeText(PosBarcodeActivity.this, c.getString(1) + " Ditambahkan 1", Toast.LENGTH_SHORT).show();

            }

            adapter.notifyDataSetChanged();
        }




        if (tipe_transaksi.equals("beli")) {

            int posisiindex = -1;

            for (int i = 0; i < TambahPembelianActivity.lsdata.size(); i++) {
                TambahPembelianActivity.TambahPembelianModel inmodel = TambahPembelianActivity.lsdata.get(i);
                if (inmodel.getKode_barang().equals(c.getString(0))) {
                    posisiindex = i;
                }
            }

            if (posisiindex < 0) {
                TambahPembelianActivity.lsdata.add(new TambahPembelianActivity.TambahPembelianModel(
                        c.getString(0),
                        c.getString(1),
                        c.getString(2),
                        c.getDouble(3),
                        1,
                        c.getDouble(3) * 1,
                        c.getString(6)

                ));
                //Toast.makeText(PosBarcodeActivity.this, "1 Barang Baru Ditambahkan", Toast.LENGTH_SHORT).show();
            } else {
                double jumlahawal = TambahPembelianActivity.lsdata.get(posisiindex).getJumlah();
                double harga_beli = TambahPembelianActivity.lsdata.get(posisiindex).getHarga_beli();
                TambahPembelianActivity.lsdata.get(posisiindex).setJumlah(jumlahawal + 1);
                TambahPembelianActivity.lsdata.get(posisiindex).setTotal(harga_beli * (jumlahawal + 1));
               // Toast.makeText(PosBarcodeActivity.this, c.getString(1) + " Ditambahkan 1", Toast.LENGTH_SHORT).show();

            }

        } else if (tipe_transaksi.equals("jual")) {

            int posisiindex = -1;

            for (int i = 0; i < TambahPenjualanActivity.lsdata.size(); i++) {
                TambahPenjualanActivity.TambahpenjualanModel inmodel = TambahPenjualanActivity.lsdata.get(i);
                if (inmodel.getKode_barang().equals(c.getString(0))) {
                    posisiindex = i;
                }
            }

            if (posisiindex < 0) {
                TambahPenjualanActivity.lsdata.add(new TambahPenjualanActivity.TambahpenjualanModel(
                        c.getString(0),
                        c.getString(1),
                        c.getString(2),
                        c.getDouble(4),
                        1,
                        c.getDouble(4) * 1,
                        c.getDouble(8),
                        c.getString(6),
                        c.getInt(7),
                        c.getDouble(8)

                ));
                //Toast.makeText(PosBarcodeActivity.this, "1 Barang Baru Ditambahkan", Toast.LENGTH_SHORT).show();
            } else {
                double jumlahawal = TambahPenjualanActivity.lsdata.get(posisiindex).getJumlah();
                double harga_beli = TambahPenjualanActivity.lsdata.get(posisiindex).getHarga_jual();
                TambahPenjualanActivity.lsdata.get(posisiindex).setJumlah(jumlahawal + 1);
                TambahPenjualanActivity.lsdata.get(posisiindex).setTotal(harga_beli * (jumlahawal + 1));
//                Toast.makeText(PosBarcodeActivity.this, c.getString(1) + " Ditambahkan 1", Toast.LENGTH_SHORT).show();

            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vib.cancel();
        toneg.stopTone();
        camsource.release();
        barcodedetect.release();

    }

    public class PosBarcodeAdapter extends RecyclerView.Adapter {
        ArrayList<PosBarcodeModel> model = new ArrayList<>();
        Context ct;
        NumberFormat nf = NumberFormat.getInstance();
        int content = 0;

        public PosBarcodeAdapter(ArrayList<PosBarcodeModel> model, Context ct) {
            this.model = model;
            this.ct = ct;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater lin = LayoutInflater.from(parent.getContext());
            View v = lin.inflate(R.layout.adapter_pos_barcode, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof Holder) {
                Holder h = (Holder) holder;
                h.lnama_barang.setText(model.get(position).getNama_barang());
                h.lkodebarang.setText(model.get(position).getKode_barang());
                if(tipe_transaksi.equals("beli")){
                    h.ljumlah.setText(model.get(position).getHarga_beli() +" x "+ nf.format(model.get(position).getJumlah_barang()));
                    h.ltotal_harga.setText("Total : " + nf.format(model.get(position).getHarga_beli()*model.get(position).getJumlah_barang()));
                }else{
                    h.ljumlah.setText(model.get(position).getHarga_jual() +" x "+ nf.format(model.get(position).getJumlah_barang()));
                    h.ltotal_harga.setText("Total : " + nf.format(model.get(position).getHarga_jual()*model.get(position).getJumlah_barang()));
                }

                h.img_del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(ct);
                        adb.setTitle("Confirmation");
                        adb.setMessage("Do you want to delete " + model.get(position).getNama_barang() + " ? ");
                        adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    model.remove(position);
                                    if(tipe_transaksi.equals("beli")){
                                        TambahPembelianActivity.lsdata.remove(position);
                                    }else{
                                        TambahPenjualanActivity.lsdata.remove(position);
                                    }

                                    notifyDataSetChanged();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
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
                    }
                });

                Glide.with(ct).
                        load(new File(model.get(position).getGambar_barang())).
                        placeholder(R.drawable.ic_assessment_70dp).
                        centerCrop().
                        diskCacheStrategy(DiskCacheStrategy.ALL).
                        into(h.gambar_barang);
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
        TextView lnama_barang, lkodebarang, ljumlah, ltotal_harga;
        ImageView gambar_barang, img_del;

        public Holder(View itemView) {
            super(itemView);
            lnama_barang = itemView.findViewById(R.id.lnama_barang);
            lkodebarang = itemView.findViewById(R.id.lkodebarang);
            ltotal_harga = itemView.findViewById(R.id.ltotal_harga_final);
            ljumlah = itemView.findViewById(R.id.ljudul);
            gambar_barang = itemView.findViewById(R.id.gambar_barang);
            img_del = itemView.findViewById(R.id.img_del);
        }
    }

    public class PosBarcodeModel {
        String kode_barang, nama_barang, satuan_barang;
        double harga_beli, harga_jual, jumlah_barang,total_harga;
        String gambar_barang;
        int tipe_barang;

        public PosBarcodeModel(String kode_barang, String nama_barang, String satuan_barang, double harga_beli, double harga_jual, double jumlah_barang, String gambar_barang, int tipe_barang) {
            this.kode_barang = kode_barang;
            this.nama_barang = nama_barang;
            this.satuan_barang = satuan_barang;
            this.harga_beli = harga_beli;
            this.harga_jual = harga_jual;
            this.jumlah_barang = jumlah_barang;
            this.gambar_barang = gambar_barang;
            this.tipe_barang = tipe_barang;
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
    }
}
