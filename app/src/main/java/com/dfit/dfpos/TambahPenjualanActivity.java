package com.dfit.dfpos;

import android.app.DatePickerDialog;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.CountDownTimer;
import android.os.Handler;

import android.os.Bundle;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TambahPenjualanActivity extends AppCompatActivity {

    private Context mContext = null;
    public static Printer mPrinter = null;
    public static ArrayList<TambahpenjualanModel> lsdata = new ArrayList<>();
    FloatingActionButton fbadd, fbbarcode;
    Button bsimpan, bbayar;
    RecyclerView rvdata;
    RecyclerView.LayoutManager layman;
    TambahpenjualanAdapter adapter;
    Dblocalhelper dbo;
    EditText ednotrans, edtanggaltrans, eddesk, edkodebarang, ednofaktur;
    ImageView bimg_barcode, bimg_tanggal, bcalc;
    TextView ltotal;
    NumberFormat nf = NumberFormat.getInstance();
    String kode_transaksi = "";
    Calendar cal = Calendar.getInstance();
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_penjualan);
        fbadd = findViewById(R.id.fbadd);
        rvdata = findViewById(R.id.rvdata);
        ednotrans = findViewById(R.id.ednotrans);
        ednofaktur = findViewById(R.id.ednofaktur);
        edtanggaltrans = findViewById(R.id.edtanggaltrans);
        eddesk = findViewById(R.id.eddesk);
        edkodebarang = findViewById(R.id.edkodebarang);
        bimg_barcode = findViewById(R.id.bimg_barcode);
        bimg_tanggal = findViewById(R.id.bimg_tanggal);
        bcalc = findViewById(R.id.bcalc);
        ltotal = findViewById(R.id.ltotal);
        bsimpan = findViewById(R.id.bsimpan);
        bbayar = findViewById(R.id.bbayar);
        layman = new LinearLayoutManager(this);
        rvdata.setLayoutManager(layman);
        rvdata.setHasFixedSize(true);
        rvdata.setItemAnimator(new DefaultItemAnimator());
        sp = getApplicationContext().getSharedPreferences("config", 0);
        adapter = new TambahpenjualanAdapter(lsdata, this);
        rvdata.setAdapter(adapter);
        ednotrans.setEnabled(false);
        edtanggaltrans.setEnabled(false);
        dbo = new Dblocalhelper(this);
        adddata();
        lsdata.clear();
        Bundle ex = getIntent().getExtras();
        if (ex != null) {
            ednotrans.setText(ex.getString("kode_penjualan_master"));
            kode_transaksi = ex.getString("kode_penjualan_master");
            loaddata(ex.getString("kode_penjualan_master"));
            SQLiteDatabase db = dbo.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT tanggal_penjualan,deskripsi " +
                    "FROM penjualan_master WHERE kode_penjualan_master='" + ex.getString("kode_penjualan_master") + "' LIMIT 1", null);
            if (c.moveToFirst()) {
                edtanggaltrans.setText(c.getString(0));
                eddesk.setText(c.getString(1));
            }
            c.close();
            db.close();
        } else {
            ednotrans.setText(dbo.getkodetransaksi("PJ"));
            edtanggaltrans.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            kode_transaksi = "";
        }
        savedata();
        caribarang();
        caribarcode();
        bayar();
        gettanggal();
        edtanggaltrans.requestFocus();
        calcpersen();

    }


    private void calcpersen() {
        bcalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder adb = new AlertDialog.Builder(TambahPenjualanActivity.this);
                adb.setTitle("Calculate Discount");
                LinearLayout ll = new LinearLayout(TambahPenjualanActivity.this);
                ll.setOrientation(LinearLayout.VERTICAL);
                TextView tvnominal = new TextView(TambahPenjualanActivity.this);
                tvnominal.setText("Normal Price");
                final EditText edjumlahnominal = new EditText(TambahPenjualanActivity.this);
                edjumlahnominal.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                final TextView tvpotongan = new TextView(TambahPenjualanActivity.this);
                tvpotongan.setText("Total Deduction (Price)");
                EditText edjumlahpotongan = new EditText(TambahPenjualanActivity.this);
                edjumlahpotongan.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                final TextView tvhasil = new TextView(TambahPenjualanActivity.this);
                tvhasil.setText("Total Discount (%)");
                final EditText edhasil = new EditText(TambahPenjualanActivity.this);
                final CheckBox cbtipe = new CheckBox(TambahPenjualanActivity.this);
                cbtipe.setText("Convert % to deduction amount ");
                cbtipe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked==true){
                            tvpotongan.setText("Discount in Percentage (%)");
                            tvhasil.setText("Amount to be deducted");
                        }
                        else{
                            tvpotongan.setText("Total Deduction (Price)");
                            tvhasil.setText("Total Discount (%)");
                        }
                    }
                });
                ll.setPadding(30, 30, 30, 30);
                ll.addView(tvnominal);
                ll.addView(edjumlahnominal);
                ll.addView(tvpotongan);
                ll.addView(edjumlahpotongan);
                ll.addView(tvhasil);
                ll.addView(edhasil);
                ll.addView(cbtipe);
                edjumlahpotongan.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(cbtipe.isChecked()==false) {
                            double nominal = Oneforallfunc.Stringtodouble(edjumlahnominal.getText().toString());
                            double potongan = Oneforallfunc.Stringtodouble(s.toString());
                            double hasil = (potongan / nominal) * 100;
                            edhasil.setText(String.valueOf(hasil));
                        }else{
                            double nominal = Oneforallfunc.Stringtodouble(edjumlahnominal.getText().toString());
                            double persen = Oneforallfunc.Stringtodouble(s.toString());
                            double hasil = nominal*(persen/100);
                            edhasil.setText(String.valueOf(hasil));
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                adb.setView(ll);
                adb.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                adb.show();
            }
        });
    }

    private void caribarcode() {
        bimg_barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(TambahPenjualanActivity.this, BarcodeActivity.class);
                startActivityForResult(in, 1);
            }
        });

    }

    private void caribarang() {
        fbadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(TambahPenjualanActivity.this, CariBarangActivity.class);
                in.putExtra("tipe_transaksi", "jual");
                startActivity(in);
            }
        });
    }

    private void rawadddata(String kode_barang) {
        SQLiteDatabase db = dbo.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT kode_barang,nama_barang,satuan_barang,harga_jual,diskon,gambar_barang,tipe_barang,harga_beli " +
                "FROM persediaan WHERE kode_barang='" + kode_barang + "' LIMIT 1", null);
        if (c.moveToFirst()) {
            if (c.getString(0).equals("") || c.getString(0) == null) {
                Toast.makeText(TambahPenjualanActivity.this, "Item not found", Toast.LENGTH_SHORT).show();
            } else {

                int posisiindex = -1;
                for (int i = 0; i < lsdata.size(); i++) {
                    TambahpenjualanModel inmodel = lsdata.get(i);
                    if (inmodel.getKode_barang().equals(kode_barang)) {
                        posisiindex = i;
                    }
                }

                if (posisiindex < 0) {
                    //double diskonpersen = c.getDouble(4);
                    //double diskonnominal = c.getDouble(3) * (diskonpersen / 100);
                    double diskon=c.getDouble(4);
                    lsdata.add(new TambahpenjualanModel(c.getString(0), c.getString(1),
                            c.getString(2), c.getDouble(3), 1,
                            (c.getDouble(3) - diskon) * 1,
                            c.getDouble(4), c.getString(5), c.getInt(6),c.getDouble(7)));
                } else {
                    double jumlahawal = lsdata.get(posisiindex).getJumlah();
                    //double diskonpersen = lsdata.get(posisiindex).getDiskon();
                    //double diskonnominal = lsdata.get(posisiindex).getHarga_jual() * (diskonpersen / 100);
                    double diskon = lsdata.get(posisiindex).getDiskon();
                    double harga_jual = lsdata.get(posisiindex).getHarga_jual();
                    lsdata.get(posisiindex).setJumlah(jumlahawal + 1);
                    lsdata.get(posisiindex).setTotal((harga_jual - diskon) * (jumlahawal + 1));
                    Toast.makeText(TambahPenjualanActivity.this, (harga_jual - diskon) * (jumlahawal + 1)+" ", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
                double total = 0;
                for (int i = 0; i < lsdata.size(); i++) {
                    total = total + lsdata.get(i).getTotal();
                }
                ltotal.setText(nf.format(total));
            }
        } else {
            Toast.makeText(TambahPenjualanActivity.this, "Item not found", Toast.LENGTH_SHORT).show();
        }
        c.close();
        db.close();
    }

    private void adddata() {
        edkodebarang.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    String kode = v.getText().toString();

                    rawadddata(kode);
                    edkodebarang.setText("");
                    edkodebarang.setFocusable(true);
                    edkodebarang.requestFocus();
                }
                return false;
            }
        });
    }

    private void loaddata(final String kode_penjualan_master) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbo.getReadableDatabase();
                try {

                    /*String query = "SELECT pd.kode_barang,nama_barang,satuan_barang,pd.harga_jual," +
                            "jumlah,(pd.harga_jual-(pd.harga_jual*(pd.diskon/100)))*jumlah AS total,pd.diskon,gambar_barang,tipe_barang " +
                            "FROM penjualan_detail pd INNER JOIN persediaan ps ON " +
                            "pd.kode_barang=ps.kode_barang WHERE kode_penjualan_master='" + kode_penjualan_master + "'";*/

                    String query = "SELECT pd.kode_barang,nama_barang,satuan_barang,pd.harga_jual," +
                            "jumlah,(pd.harga_jual-diskon)*jumlah AS total,pd.diskon,gambar_barang,tipe_barang,harga_beli " +
                            "FROM penjualan_detail pd INNER JOIN persediaan ps ON " +
                            "pd.kode_barang=ps.kode_barang WHERE kode_penjualan_master='" + kode_penjualan_master + "'";
                    Cursor c = db.rawQuery(query, null);
                    while (c.moveToNext()) {
                        lsdata.add(new TambahpenjualanModel(c.getString(0), c.getString(1),
                                c.getString(2), c.getDouble(3), c.getDouble(4),
                                c.getDouble(5), c.getDouble(6), c.getString(7),
                                c.getInt(8),c.getDouble(9)));
                    }
                    ;

                    adapter.notifyDataSetChanged();
                    double total = 0;
                    for (int i = 0; i < lsdata.size(); i++) {
                        total = total + lsdata.get(i).getTotal();
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

    private void rawsavedata(int status) {
        if (lsdata.size() <= 0) {
            Toast.makeText(this, "Process rejected, access denied", Toast.LENGTH_SHORT).show();
        } else {
            SQLiteDatabase db = dbo.getWritableDatabase();
            String currenttime = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS").format(new Date());
            try {
                db.beginTransaction();
                if (kode_transaksi.equals("") || kode_transaksi == null) {
                    db.execSQL("INSERT INTO penjualan_master(kode_penjualan_master,status,tanggal_penjualan,deskripsi,last_update,date_created) " +
                            "VALUES('" + ednotrans.getText().toString() + "'," + status + "," +
                            "'" + edtanggaltrans.getText().toString() + "','" + eddesk.getText().toString() + "'," +
                            "'" + currenttime + "'," +
                            "'" + currenttime + "')");
                    for (int i = 0; i < lsdata.size(); i++) {
                        db.execSQL("INSERT INTO penjualan_detail(kode_penjualan_detail,kode_penjualan_master," +
                                "kode_barang,jumlah,harga_beli,harga_jual,diskon) " +
                                "VALUES('" + ednotrans.getText().toString() + i + "'," +
                                "'" + ednotrans.getText().toString() + "'," +
                                "'" + lsdata.get(i).getKode_barang() + "'," +
                                "" + Oneforallfunc.validdouble(lsdata.get(i).getJumlah()) + "," +
                                "" + Oneforallfunc.validdouble(lsdata.get(i).getHarga_beli()) + "," +
                                "" + Oneforallfunc.validdouble(lsdata.get(i).getHarga_jual()) + "," +
                                "" + Oneforallfunc.validdouble(lsdata.get(i).getDiskon()) + ")");
                        if (lsdata.get(i).getTipe_barang() == 1) {
                            String kode_barang_racik = lsdata.get(i).getKode_barang();
                            Cursor cin = db.rawQuery("SELECT kode_barang_isi,jumlah_isi FROM racikan WHERE kode_barang_racik='" + kode_barang_racik + "'", null);
                            while (cin.moveToNext()) {
                                db.execSQL("UPDATE persediaan SET jumlah_barang=jumlah_barang-" + (cin.getDouble(1) * lsdata.get(i).getJumlah()) + " " +
                                        "WHERE kode_barang='" + cin.getString(0) + "' ");
                            }
                        } else {
                            db.execSQL("UPDATE persediaan SET jumlah_barang=jumlah_barang-" + lsdata.get(i).getJumlah() + " " +
                                    "WHERE kode_barang='" + lsdata.get(i).getKode_barang() + "' ");
                        }

                    }
                } else {

                    db.execSQL("UPDATE penjualan_master  SET kode_penjualan_master='" + ednotrans.getText().toString() + "'," +
                            "status=" + status + ",tanggal_penjualan='" + edtanggaltrans.getText().toString() + "'," +
                            "deskripsi='" + eddesk.getText().toString() + "',last_update='" + currenttime + "' " +
                            "WHERE kode_penjualan_master='" + kode_transaksi + "'");
                    Cursor c = db.rawQuery("SELECT kode_penjualan_detail,kode_barang,jumlah " +
                            "FROM penjualan_detail WHERE kode_penjualan_master='" + kode_transaksi + "'", null);
                    while (c.moveToNext()) {
                        Cursor ccek = db.rawQuery("SELECT tipe_barang FROM persediaan WHERE kode_barang='" + c.getString(1) + "'", null);
                        if (ccek.moveToFirst()) {
                            if (ccek.getInt(0) == 1) {
                                String kode_barang_racik = c.getString(1);
                                Cursor ccekup = db.rawQuery("SELECT kode_barang_isi,jumlah_isi FROM racikan WHERE kode_barang_racik='" + kode_barang_racik + "'", null);
                                while (ccekup.moveToNext()) {
                                    db.execSQL("UPDATE persediaan SET jumlah_barang=jumlah_barang+" + (ccekup.getDouble(1) * c.getDouble(2)) + " " +
                                            "WHERE kode_barang='" + ccekup.getString(0) + "' ");
                                }
                            } else {
                                db.execSQL("UPDATE persediaan SET jumlah_barang=jumlah_barang+" + c.getDouble(2) + " " +
                                        "WHERE kode_barang='" + c.getString(1) + "' ");
                            }
                        }
                        //db.execSQL("DELETE FROM penjualan_detail WHERE kode_penjualan_detail='"+c.getString(0)+"'");
                    }

                    db.execSQL("DELETE FROM penjualan_detail WHERE kode_penjualan_master='" + kode_transaksi + "'");

                    for (int i = 0; i < lsdata.size(); i++) {
                        String nodetail = ednotrans.getText().toString() + "/" + i;
                        db.execSQL("INSERT INTO penjualan_detail(kode_penjualan_detail,kode_penjualan_master," +
                                "kode_barang,jumlah,harga_beli,harga_jual,diskon) " +
                                "VALUES('" + nodetail + "','" + ednotrans.getText().toString() + "'," +
                                "'" + lsdata.get(i).getKode_barang() + "'," +
                                "" + Oneforallfunc.validdouble(lsdata.get(i).getJumlah()) + "," +
                                "" + Oneforallfunc.validdouble(lsdata.get(i).getHarga_beli()) + "," +
                                "" + Oneforallfunc.validdouble(lsdata.get(i).getHarga_jual()) + "," +
                                "" + Oneforallfunc.validdouble(lsdata.get(i).getDiskon()) + "" +
                                ")");
                        if (lsdata.get(i).getTipe_barang() == 1) {
                            String kode_barang_racik = lsdata.get(i).getKode_barang();
                            Cursor cup = db.rawQuery("SELECT kode_barang_isi,jumlah_isi FROM racikan WHERE kode_barang_racik='" + kode_barang_racik + "'", null);
                            while (cup.moveToNext()) {
                                db.execSQL("UPDATE persediaan SET jumlah_barang=jumlah_barang-" + (cup.getDouble(1) * lsdata.get(i).getJumlah()) + " " +
                                        "WHERE kode_barang='" + cup.getString(0) + "' ");
                            }
                        } else {
                            db.execSQL("UPDATE persediaan SET jumlah_barang=jumlah_barang-" + lsdata.get(i).getJumlah() + " " +
                                    "WHERE kode_barang='" + lsdata.get(i).getKode_barang() + "' ");
                        }
                    }

                }
                db.setTransactionSuccessful();
                Toast.makeText(TambahPenjualanActivity.this, "Sales Data Successfully Saved", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder adb = new AlertDialog.Builder(TambahPenjualanActivity.this);
                adb.setTitle("Confirmation");
                adb.setMessage("Print Receipt?");
                adb.setCancelable(false);
                adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder sb = new StringBuilder();
                        double total = 0;
                        for (int i = 0; i < lsdata.size(); i++) {
                            /*if (lsdata.get(i).getDiskon() > 0) {
                                //sb.append(i + 1 + "." + lsdata.get(i).getNama_barang() + " x " + nf.format(lsdata.get(i).getJumlah()) + "/@" + nf.format(lsdata.get(i).getDiskon()) + "% = " + nf.format(lsdata.get(i).getTotal()) + "\n");
                            } else {*/
                                sb.append(lsdata.get(i).getNama_barang()+" x "+Math.round((int)lsdata.get(i).getJumlah())+"\n");
                                sb.append("\t\t\t\t"+(int)lsdata.get(i).getTotal()+"\n");
                                total = total + lsdata.get(i).getTotal();
                                //sb.append(nf.format(lsdata.get(i).getJumlah())+"\t"+lsdata.get(i).getNama_barang()+"\t\t\t\t"+nf.format(lsdata.get(i).getTotal())+"\n");
                                //sb.append(i + 1 + "." + lsdata.get(i).getNama_barang() + " x " + nf.format(lsdata.get(i).getJumlah()) + " = " + nf.format(lsdata.get(i).getTotal()) + "\n");
                            //}
                        }
                        //sb.append(lsdata.get(0).getNama_barang());
                        Intent in = new Intent(TambahPenjualanActivity.this, CetakStruk.class);
                        SQLiteDatabase db=dbo.getReadableDatabase();
                        Cursor c=db.rawQuery("SELECT total,uang,kembalian FROM cekout WHERE id=1113",null);
                        if(c.moveToFirst()){
                            in.putExtra("totalbelanja",c.getString(0));
                            in.putExtra("tunai",c.getString(1));
                            in.putExtra("kembalian",c.getString(2));
                        }
                        c.close();
                        db.close();
                        in.putExtra("struk",sb.toString());
                        in.putExtra("receiptNo",ednotrans.getText().toString());
                        //Toast.makeText(getApplicationContext(),sb.toString(),Toast.LENGTH_LONG).show();
                        lsdata.clear();
                        startActivity(in);
                    }
                });
                adb.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        lsdata.clear();
                        dialog.dismiss();
                    }
                });
                adb.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                db.endTransaction();
                db.close();
            }


            if (status == 0) {
                AlertDialog.Builder adb = new AlertDialog.Builder(TambahPenjualanActivity.this);
                adb.setTitle("Confirmation");
                adb.setMessage("Proceed to print receipt?");
                adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        /*StringBuilder sb = new StringBuilder();
                        sb.append("\n");
                        sb.append(sp.getString("nama_usaha", "none") + "\n");
                        sb.append(sp.getString("alamat_usaha", "none") + "\n");
                        sb.append(sp.getString("nohp_usaha", "none") + "\n");
                        sb.append("\n");
                        for (int i = 0; i < lsdata.size(); i++) {
                            if (lsdata.get(i).getDiskon() > 0) {
                                sb.append(i + 1 + "." + lsdata.get(i).getNama_barang() + " x " + nf.format(lsdata.get(i).getJumlah()) + "/@" + nf.format(lsdata.get(i).getDiskon()) + "% = " + nf.format(lsdata.get(i).getTotal()) + "\n");
                            } else {
                                sb.append(i + 1 + "." + lsdata.get(i).getNama_barang() + " x " + nf.format(lsdata.get(i).getJumlah()) + " = " + nf.format(lsdata.get(i).getTotal()) + "\n");
                            }

                        }
                        sb.append("\n");
                        sb.append("Total : " + ltotal.getText().toString());
                        sb.append("\n\n\n");

                        if (!sp.getString("default_printer", "none").equals("none")) {
                            try {
                                BluetoothAdapter btadapter = BluetoothAdapter.getDefaultAdapter();
                                if (btadapter.isEnabled()) {
                                    Bluetoothprint bt = new Bluetoothprint(TambahPenjualanActivity.this);
                                    bt.print(sb.toString());
                                } else {
                                    AlertDialog.Builder adb = new AlertDialog.Builder(TambahPenjualanActivity.this);
                                    adb.setTitle("Informasi");
                                    adb.setMessage("Bluetooth Tidak Aktif, Aktifkan bluetooth atau masuk ke pengaturan untuk setting bluetooth printer");
                                    adb.setPositiveButton("Tutup", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                }
                            } catch (Exception ex) {
                                Toast.makeText(TambahPenjualanActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            }*/

                    }
                });
                adb.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                adb.show();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("\n");
                sb.append(sp.getString("nama_usaha", "none") + "\n");
                sb.append(sp.getString("alamat_usaha", "none") + "\n");
                sb.append(sp.getString("nohp_usaha", "none") + "\n");
                sb.append("\n");
                for (int i = 0; i < lsdata.size(); i++) {
                    if (lsdata.get(i).getDiskon() > 0) {
                        sb.append(i + 1 + "." + lsdata.get(i).getNama_barang() + " x " + nf.format(lsdata.get(i).getJumlah()) + "/@" + nf.format(lsdata.get(i).getDiskon()) + " = " + nf.format(lsdata.get(i).getTotal()) + "\n");
                    } else {
                        sb.append(i + 1 + "." + lsdata.get(i).getNama_barang() + " x " + nf.format(lsdata.get(i).getJumlah()) + " = " + nf.format(lsdata.get(i).getTotal()) + "\n");
                    }

                }
                sb.append("\n");
                sb.append("Total : " + ltotal.getText().toString());
                sb.append("\n\n\n");
                if (!sp.getString("default_printer", "none").equals("none")) {
                    try {
                        BluetoothAdapter btadapter = BluetoothAdapter.getDefaultAdapter();
                        if (btadapter.isEnabled()) {
                            Bluetoothprint bt = new Bluetoothprint(TambahPenjualanActivity.this);
                            bt.print(sb.toString());
                        } else {
                            AlertDialog.Builder adb = new AlertDialog.Builder(this);
                            adb.setTitle("Information");
                            adb.setMessage("Bluetooth is switch off, switch on Bluetooth or go to setting to setup bluetooth printer");
                            adb.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                        }
                    } catch (Exception ex) {
                        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }
    }

    private void savedata() {
        /*bsimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rawsavedata(0);
            }
        });*/
    }

    private void bayar() {
        bbayar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder adb = new AlertDialog.Builder(TambahPenjualanActivity.this);
                View vi = getLayoutInflater().inflate(R.layout.pilih_bayar, null);
                Button pilemoney = (Button) vi.findViewById(R.id.pilemoney);
                Button piltunai = (Button) vi.findViewById(R.id.piltunai);
                adb.setTitle("Pilih metode pembayaran");
                adb.setView(vi);
                adb.setCancelable(false);
                adb.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                final AlertDialog ad = adb.create();
                pilemoney.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        emoney();
                        ad.dismiss();
                    }
                });
                piltunai.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tunai();
                        ad.dismiss();
                    }
                });
                ad.show();
            }
        });
    }

    private void emoney(){
        final AlertDialog.Builder adb = new AlertDialog.Builder(TambahPenjualanActivity.this);
        View vi = getLayoutInflater().inflate(R.layout.bayar_nontunai, null);
        Button bproses = (Button) vi.findViewById(R.id.beproses);
        Button bcomplete = (Button) vi.findViewById(R.id.bcomplete);
        TextView timerpay = (TextView)vi.findViewById(R.id.timeremoney);
        TextView tvtimerpay = (TextView)vi.findViewById(R.id.tvtimer);
        bcomplete.setVisibility(View.GONE);
        timerpay.setVisibility(View.GONE);
        tvtimerpay.setVisibility(View.GONE);
        adb.setTitle("E-Money");
        adb.setView(vi);
        adb.setCancelable(false);
        adb.setNeutralButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog ad = adb.create();
        final TextView ltotalbelanja = (TextView) vi.findViewById(R.id.ltotalbelanja);
        ltotalbelanja.setText(ltotal.getText().toString());
        adb.setNeutralButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        bproses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerpay.setVisibility(View.VISIBLE);
                tvtimerpay.setVisibility(View.VISIBLE);
                bproses.setVisibility(View.GONE);
                bcomplete.setVisibility(View.VISIBLE);
                new CountDownTimer(120000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        timerpay.setText(""+millisUntilFinished / 1000);
                        //here you can have your logic to set text to edittext
                    }
                    public void onFinish() {
                        ad.dismiss();
                    }
                }.start();
            }
        });
        bcomplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = dbo.getReadableDatabase();
                db.beginTransaction();
                try {
                    db.execSQL("UPDATE cekout SET total='"+ltotalbelanja.getText().toString()+"',uang='"+
                            ltotalbelanja.getText().toString()+"',kembalian='"+"0"+"'" +
                            "WHERE id = 1113");
                                /*db.execSQL("INSERT INTO cekout (id,total,uang,kembalian)VALUES(1113,'"+
                                        ltotalbelanja.getText().toString()+"','"+edjumlahuang.getText().toString()+"','"+
                                        lkembalian.getText().toString()+"')");*/
                    db.setTransactionSuccessful();
                } catch (Exception ex) {
                    AlertDialog.Builder adb=new AlertDialog.Builder(TambahPenjualanActivity.this);
                    adb.setTitle("Information");
                    adb.setMessage(ex.getMessage());
                    adb.show();
                } finally {
                    db.endTransaction();
                    db.close();
                }
                rawsavedata(1);
                ad.dismiss();
                //lsdata.clear();
                adapter.notifyDataSetChanged();
                ednotrans.setText(dbo.getkodetransaksi("PJ"));
                edtanggaltrans.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                eddesk.setText("");
                edkodebarang.setText("");
                ltotal.setText("0");
                edkodebarang.setFocusable(true);
                edkodebarang.requestFocus();
            }
        });
        ad.show();
    }
    private void tunai(){
        if (lsdata.size() <= 0) {
            Toast.makeText(TambahPenjualanActivity.this, "Gagal, anda belum memasukkan item satupun", Toast.LENGTH_SHORT).show();
        } else {
            final AlertDialog.Builder adb = new AlertDialog.Builder(TambahPenjualanActivity.this);
            View vi = getLayoutInflater().inflate(R.layout.bayar_layout, null);
            final TextView ltotalbelanja = (TextView) vi.findViewById(R.id.ltotalbelanja);
            final TextView lkembalian = (TextView) vi.findViewById(R.id.lkembalian);
            final EditText edjumlahuang = (EditText) vi.findViewById(R.id.edjumlahuang);
            Button boke = (Button) vi.findViewById(R.id.boke);
            Button bpas = (Button) vi.findViewById(R.id.bpass);
            Button b1 = (Button) vi.findViewById(R.id.b1);
            Button b2 = (Button) vi.findViewById(R.id.b2);
            Button b5 = (Button) vi.findViewById(R.id.b5);
            Button b10 = (Button) vi.findViewById(R.id.b10);
            Button b20 = (Button) vi.findViewById(R.id.b20);
            Button b50 = (Button) vi.findViewById(R.id.b50);
            Button b100 = (Button) vi.findViewById(R.id.b100);
            b1.setText(getResources().getString(R.string.currency) + " " + getResources().getString(R.string.denomination1));
            b2.setText(getResources().getString(R.string.currency) + " " + getResources().getString(R.string.denomination2));
            b5.setText(getResources().getString(R.string.currency) + " " + getResources().getString(R.string.denomination3));
            b10.setText(getResources().getString(R.string.currency) + " " + getResources().getString(R.string.denomination4));
            b20.setText(getResources().getString(R.string.currency) + " " + getResources().getString(R.string.denomination5));
            b50.setText(getResources().getString(R.string.currency) + " " + getResources().getString(R.string.denomination6));
            b100.setText(getResources().getString(R.string.currency) + " " + getResources().getString(R.string.denomination7));
            ltotalbelanja.setText(ltotal.getText().toString());
            final double kembali = 0 - Oneforallfunc.Stringtodouble(ltotal.getText().toString().replace(".", "").replace(",",""));
            lkembalian.setText(nf.format(kembali));
            adb.setTitle("Payment");
            adb.setView(vi);
            adb.setCancelable(false);
            adb.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            final AlertDialog ad = adb.create();
            boke.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Double.parseDouble(lkembalian.getText().toString().replace(".", "").replace(",",""))<0){
                        Toast.makeText(TambahPenjualanActivity.this,"Jumlah uang masih kurang", Toast.LENGTH_SHORT).show();
                    }else {
                        SQLiteDatabase db = dbo.getReadableDatabase();
                        db.beginTransaction();
                        try {
                            db.execSQL("UPDATE cekout SET total='"+ltotalbelanja.getText().toString()+"',uang='"+
                                    edjumlahuang.getText().toString()+"',kembalian='"+lkembalian.getText().toString()+"'" +
                                    "WHERE id = 1113");
                                /*db.execSQL("INSERT INTO cekout (id,total,uang,kembalian)VALUES(1113,'"+
                                        ltotalbelanja.getText().toString()+"','"+edjumlahuang.getText().toString()+"','"+
                                        lkembalian.getText().toString()+"')");*/
                            db.setTransactionSuccessful();
                        } catch (Exception ex) {
                            AlertDialog.Builder adb=new AlertDialog.Builder(TambahPenjualanActivity.this);
                            adb.setTitle("Information");
                            adb.setMessage(ex.getMessage());
                            adb.show();
                        } finally {
                            db.endTransaction();
                            db.close();
                        }
                        rawsavedata(1);
                        ad.dismiss();
                        //lsdata.clear();
                        adapter.notifyDataSetChanged();
                        ednotrans.setText(dbo.getkodetransaksi("PJ"));
                        edtanggaltrans.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                        eddesk.setText("");
                        edkodebarang.setText("");
                        ltotal.setText("0");
                        edkodebarang.setFocusable(true);
                        edkodebarang.requestFocus();
                    }

                }
            });

            View.OnClickListener blistener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double hasil = 0.0;
                    switch (v.getId()) {
                        case R.id.bpass:
                                    /*try {
                                        rawsavedata(1);
                                        ad.dismiss();
                                        lsdata.clear();
                                        adapter.notifyDataSetChanged();
                                        ednotrans.setText(dbo.getkodetransaksi("PJ"));
                                        edtanggaltrans.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                                        eddesk.setText("");
                                        edkodebarang.setText("");
                                        ltotal.setText("0");
                                        edkodebarang.setFocusable(true);
                                        edkodebarang.requestFocus();
                                        break;
                                    } catch (Exception ex) {
                                        Toast.makeText(TambahPenjualanActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                                        ex.printStackTrace();
                                    }*/
                            edjumlahuang.setText(ltotalbelanja.getText().toString().replace(".", "").replace(",",""));
                            hasil = Oneforallfunc.Stringtodouble(edjumlahuang.getText().toString().replace(".", "").replace(",","")) -
                                    Oneforallfunc.Stringtodouble(ltotalbelanja.getText().toString().replace(".", "").replace(",",""));
                            lkembalian.setText(nf.format(hasil));
                            break;
                        case R.id.b1:
                            edjumlahuang.setText(R.string.denomination1);
                            hasil = Oneforallfunc.Stringtodouble(edjumlahuang.getText().toString().replace(".", "").replace(",","")) -
                                    Oneforallfunc.Stringtodouble(ltotalbelanja.getText().toString().replace(".", "").replace(",",""));
                            lkembalian.setText(nf.format(hasil));
                            break;
                        case R.id.b2:
                            edjumlahuang.setText(R.string.denomination2);
                            hasil = Oneforallfunc.Stringtodouble(edjumlahuang.getText().toString().replace(".", "").replace(",","")) -
                                    Oneforallfunc.Stringtodouble(ltotalbelanja.getText().toString().replace(".", "").replace(",",""));
                            lkembalian.setText(nf.format(hasil));
                            break;
                        case R.id.b5:
                            edjumlahuang.setText(R.string.denomination3);
                            hasil = Oneforallfunc.Stringtodouble(edjumlahuang.getText().toString().replace(".", "").replace(",","")) -
                                    Oneforallfunc.Stringtodouble(ltotalbelanja.getText().toString().replace(".", "").replace(",",""));
                            lkembalian.setText(nf.format(hasil));
                            break;
                        case R.id.b10:
                            edjumlahuang.setText(R.string.denomination4);
                            hasil = Oneforallfunc.Stringtodouble(edjumlahuang.getText().toString().replace(".", "").replace(",","")) -
                                    Oneforallfunc.Stringtodouble(ltotalbelanja.getText().toString().replace(".", "").replace(",",""));
                            lkembalian.setText(nf.format(hasil));
                            break;
                        case R.id.b20:
                            edjumlahuang.setText(R.string.denomination5);
                            hasil = Oneforallfunc.Stringtodouble(edjumlahuang.getText().toString().replace(".", "").replace(",","")) -
                                    Oneforallfunc.Stringtodouble(ltotalbelanja.getText().toString().replace(".", "").replace(",",""));
                            lkembalian.setText(nf.format(hasil));
                            break;
                        case R.id.b50:
                            edjumlahuang.setText(R.string.denomination6);
                            hasil = Oneforallfunc.Stringtodouble(edjumlahuang.getText().toString().replace(".", "").replace(",","")) -
                                    Oneforallfunc.Stringtodouble(ltotalbelanja.getText().toString().replace(".", "").replace(",",""));
                            lkembalian.setText(nf.format(hasil));
                            break;
                        case R.id.b100:
                            edjumlahuang.setText(R.string.denomination7);
                            hasil = Oneforallfunc.Stringtodouble(edjumlahuang.getText().toString().replace(".", "").replace(",","")) -
                                    Oneforallfunc.Stringtodouble(ltotalbelanja.getText().toString().replace(".", "").replace(",",""));
                            lkembalian.setText(nf.format(hasil));
                            //Toast.makeText(getApplicationContext(),ltotalbelanja.getText().toString(),Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            };
            bpas.setOnClickListener(blistener);
            b1.setOnClickListener(blistener);
            b2.setOnClickListener(blistener);
            b5.setOnClickListener(blistener);
            b10.setOnClickListener(blistener);
            b20.setOnClickListener(blistener);
            b50.setOnClickListener(blistener);
            b100.setOnClickListener(blistener);

            edjumlahuang.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    double jumlahuang = Oneforallfunc.Stringtodouble(String.valueOf(s));
                    double jumlahbelanja = Oneforallfunc.Stringtodouble(ltotalbelanja.getText().toString().replace(".", "").replace(",",""));
                    double kembali = jumlahuang - jumlahbelanja;
                    lkembalian.setText(nf.format(kembali));
                    //Toast.makeText(getApplicationContext(),jumlahuang+" "+jumlahbelanja+" "+kembali,Toast.LENGTH_LONG).show();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            ad.show();
        }
    }
    private void gettanggal() {
        bimg_tanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(TambahPenjualanActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, month);
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String tanggalkini = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
                        edtanggaltrans.setText(tanggalkini);
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

                dpd.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String kode = data.getData().toString();
                rawadddata(kode);
            }
        } else if (requestCode == 5) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth activated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        adapter.notifyDataSetChanged();
        double total = 0;

        for (int i = 0; i < lsdata.size(); i++) {
            total = total + lsdata.get(i).getTotal();
        }
        ltotal.setText(nf.format(total));
    }

    public static class TambahpenjualanModel {
        String kode_barang, nama_barang, satuan;
        double harga_jual, jumlah, total, diskon;
        String gambar_barang;
        int tipe_barang;
        double harga_beli;

        public TambahpenjualanModel(String kode_barang, String nama_barang, String satuan, double harga_jual, double jumlah,
                                    double total, double diskon, String gambar_barang, int tipe_barang,double harga_beli) {
            this.kode_barang = kode_barang;
            this.nama_barang = nama_barang;
            this.satuan = satuan;
            this.harga_jual = harga_jual;
            this.jumlah = jumlah;
            this.total = total;
            this.diskon = diskon;
            this.gambar_barang = gambar_barang;
            this.tipe_barang = tipe_barang;
            this.harga_beli=harga_beli;
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

        public String getSatuan() {
            return satuan;
        }

        public void setSatuan(String satuan) {
            this.satuan = satuan;
        }

        public double getHarga_jual() {
            return harga_jual;
        }

        public void setHarga_jual(double harga_jual) {
            this.harga_jual = harga_jual;
        }

        public double getJumlah() {
            return jumlah;
        }

        public void setJumlah(double jumlah) {
            this.jumlah = jumlah;
        }

        public double getTotal() {
            return total;
        }

        public void setTotal(double total) {
            this.total = total;
        }

        public double getDiskon() {
            return diskon;
        }

        public void setDiskon(double diskon) {
            this.diskon = diskon;
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

        public double getHarga_beli() {
            return harga_beli;
        }

        public void setHarga_beli(double harga_beli) {
            this.harga_beli = harga_beli;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TambahpenjualanModel) {
                return (((TambahpenjualanModel) obj).getKode_barang().equals(this.getKode_barang()));
            }
            return false;
        }
    }

    public class TambahpenjualanAdapter extends RecyclerView.Adapter {
        ArrayList<TambahpenjualanModel> model = new ArrayList<>();
        Context ct;
        NumberFormat nf = NumberFormat.getInstance();
        int content = 0;

        public TambahpenjualanAdapter(ArrayList<TambahpenjualanModel> model, Context ct) {
            this.model = model;
            this.ct = ct;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater lin = LayoutInflater.from(parent.getContext());
            View v = lin.inflate(R.layout.adapter_tambah_penjualan, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof Holder) {
                final Holder h = (Holder) holder;
                h.lnama_barang.setText(model.get(position).getNama_barang());
                h.lkode_barang.setText(model.get(position).getKode_barang());
                h.lharga_jual.setText("Price : " + nf.format(model.get(position).getHarga_jual()));
                h.ljumlah.setText("Jumlah : "+nf.format(model.get(position).getJumlah())+" / Diskon @ Item : Rp. "+nf.format(model.get(position).getDiskon()));
                double jumlahdiskon = model.get(position).getHarga_jual() * (model.get(position).getDiskon() / 100);
                double total_harga = (model.get(position).getHarga_jual() - model.get(position).getDiskon()) * model.get(position).getJumlah();
                h.ltotal_harga.setText(nf.format(total_harga));
                model.get(position).setTotal(total_harga);
                double total = 0;
                for (int i = 0; i < lsdata.size(); i++) {
                    total = total + lsdata.get(i).getTotal();
                }
                ltotal.setText(nf.format(total));
                Glide.with(ct).
                        load(new File(model.get(position).getGambar_barang())).
                        placeholder(R.drawable.ic_assessment_70dp).
                        centerCrop().
                        diskCacheStrategy(DiskCacheStrategy.ALL).
                        into(h.gambar_barang);
                /*h.edjumlah.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            double jumlah=Oneforallfunc.Stringtodouble(v.getText().toString());
                            double harga=model.get(position).getHarga_jual();
                            double diskonpersen=Oneforallfunc.Stringtodouble(h.eddiskon.getText().toString());
                            double diskonnominal=harga*(diskonpersen/100);
                            double total_harga = (harga-diskonnominal)*jumlah;
                            model.get(position).setTotal(total_harga);
                            notifyItemChanged(position);
                            double total = 0;
                            for (int i = 0; i < lsdata.size(); i++) {
                                total = total + lsdata.get(i).getTotal();
                            }
                            ltotal.setText(nf.format(total));
                        }
                        return false;
                    }
                });

                h.eddiskon.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            double jumlah=Oneforallfunc.Stringtodouble(h.edjumlah.getText().toString());
                            double harga=model.get(position).getHarga_jual();
                            double diskonpersen=Oneforallfunc.Stringtodouble(v.getText().toString());
                            double diskonnominal=harga*(diskonpersen/100);
                            double total_harga = (harga-diskonnominal)*jumlah;
                            model.get(position).setTotal(total_harga);
                            notifyItemChanged(position);
                            double total = 0;
                            for (int i = 0; i < lsdata.size(); i++) {
                                total = total + lsdata.get(i).getTotal();
                            }
                            ltotal.setText(nf.format(total));
                        }
                        return false;
                    }
                });*/

                h.bset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder adb=new AlertDialog.Builder(ct);
                        adb.setTitle("Edit Diskon");
                        LinearLayout ll = new LinearLayout(TambahPenjualanActivity.this);
                        ll.setOrientation(LinearLayout.VERTICAL);
                        TextView tvjumlah = new TextView(TambahPenjualanActivity.this);
                        tvjumlah.setText("Jumlah Barang");
                        final EditText edjumlah = new EditText(TambahPenjualanActivity.this);
                        edjumlah.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        edjumlah.setText(nf.format(model.get(position).getJumlah()));
                        final TextView tvdiskon = new TextView(TambahPenjualanActivity.this);
                        tvdiskon.setText("Diskon (@ Item)");
                        final EditText eddiskon = new EditText(TambahPenjualanActivity.this);
                        eddiskon.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_CLASS_NUMBER);
                        eddiskon.setHint(Math.round(model.get(position).getDiskon())+"");
                        ll.setPadding(30, 30, 30, 30);
                        //ll.addView(tvjumlah);
                        //ll.addView(edjumlah);
                        ll.addView(tvdiskon);
                        ll.addView(eddiskon);
                        adb.setView(ll);
                        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                model.get(position).setJumlah(Oneforallfunc.Stringtodouble(edjumlah.getText().toString().replace(".","").replace(",","")));
                                model.get(position).setDiskon(Double.parseDouble(eddiskon.getText().toString()));
                                double total_harga = model.get(position).getHarga_jual() - model.get(position).getDiskon();
                                model.get(position).setTotal(total_harga);
                                notifyItemChanged(position);
                                double total = 0;
                                for (int i = 0; i < lsdata.size(); i++) {
                                    total = total + lsdata.get(i).getTotal();
                                }
                                ltotal.setText(nf.format(total));
                            }
                        });
                        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        adb.show();

                        /*model.get(position).setJumlah(Oneforallfunc.Stringtodouble(h.edjumlah.getText().toString()));
                        model.get(position).setDiskon(Oneforallfunc.Stringtodouble(h.eddiskon.getText().toString()));
                        double diskonnominal = model.get(position).getHarga_jual() * (model.get(position).getDiskon() / 100);
                        double total_harga = model.get(position).getJumlah() * (model.get(position).getHarga_jual() - diskonnominal);
                        model.get(position).setTotal(total_harga);
                        notifyItemChanged(position);
                        double total = 0;
                        for (int i = 0; i < lsdata.size(); i++) {
                            total = total + lsdata.get(i).getTotal();
                        }
                        ltotal.setText(nf.format(total));*/
                    }
                });

                h.img_hapus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(ct);
                        adb.setTitle("Confirmation");
                        adb.setMessage("Confirm to delete " + model.get(position).getNama_barang() + " ? ");
                        adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    model.remove(position);
                                    notifyDataSetChanged();
                                    double total = 0;
                                    for (int i = 0; i < lsdata.size(); i++) {
                                        total = total + lsdata.get(i).getTotal();
                                    }
                                    ltotal.setText(nf.format(total));
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
        TextView lnama_barang, lkode_barang, lharga_jual,ljumlah, ltotal_harga;
        Button bset;
        ImageView gambar_barang, img_hapus;

        public Holder(View itemView) {
            super(itemView);
            lnama_barang = itemView.findViewById(R.id.lnama_barang);
            lkode_barang = itemView.findViewById(R.id.lkode_barang);
            lharga_jual = itemView.findViewById(R.id.lharga_jual);
            ltotal_harga = itemView.findViewById(R.id.ltotal_harga_final);
            ljumlah = itemView.findViewById(R.id.ljumlah);
            bset = itemView.findViewById(R.id.bset);
            gambar_barang = itemView.findViewById(R.id.gambar_barang);
            img_hapus = itemView.findViewById(R.id.img_hapus);
        }
    }
}
