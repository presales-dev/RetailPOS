package com.dfit.dfpos;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Minami on 05/09/2019.
 */

public class Dblocalhelper extends SQLiteOpenHelper {

    public static String dbname = "kasirku.db";

    public Dblocalhelper(Context context) {
        super(context, dbname, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {

            db.execSQL("CREATE TABLE persediaan (kode_barang TEXT PRIMARY KEY,nama_barang TEXT," +
                    "satuan_barang TEXT,harga_beli REAL,harga_jual REAL,jumlah_barang REAL,diskon REAL,gambar_barang TEXT,tipe_barang integer,date_created TEXT)");
            db.execSQL("CREATE TABLE racikan (kode_racik TEXT PRIMARY KEY,kode_barang_racik TEXT,kode_barang_isi TEXT,jumlah_isi REAL)");
            db.execSQL("CREATE TABLE pembelian_master (id integer PRIMARY KEY AUTOINCREMENT,kode_pembelian_master TEXT UNIQUE,tanggal_pembelian TEXT,no_faktur TEXT,deskripsi TEXT,last_update TEXT,date_created TEXT)");
            db.execSQL("CREATE TABLE pembelian_detail (kode_pembelian_detail TEXT PRIMARY KEY,kode_pembelian_master TEXT," +
                    "kode_barang TEXT,jumlah REAL,harga_beli REAL)");
            db.execSQL("CREATE TABLE penjualan_master (id integer PRIMARY KEY AUTOINCREMENT, kode_penjualan_master TEXT UNIQUE,tanggal_penjualan TEXT,status integer,deskripsi TEXT,last_update TEXT,date_created TEXT)");
            db.execSQL("CREATE TABLE penjualan_detail (kode_penjualan_detail TEXT PRIMARY KEY,kode_penjualan_master TEXT," +
                    "kode_barang TEXT,jumlah REAL,harga_beli REAL,harga_jual REAL,diskon REAL)");
            db.execSQL("CREATE TABLE catatan_master (kode_catatan_master TEXT PRIMARY KEY,tanggal_catatan TEXT,deskripsi TEXT,last_update TEXT,date_created TEXT)");
            db.execSQL("CREATE TABLE catatan_detail (kode_catatan_detail TEXT PRIMARY KEY,kode_catatan_master TEXT," +
                    "tipe integer,deskripsi TEXT,jumlah REAL)");

            db.execSQL("CREATE TABLE perusahaan (id INTEGER PRIMARY KEY,nama_usaha TEXT,alamat_usaha TEXT," +
                    "nohp_usaha TEXT,email_usaha TEXT,website TEXT,device_id TEXT,date_created TEXT)");
            db.execSQL("CREATE TABLE pengguna (kode_user INTEGER PRIMARY KEY AUTOINCREMENT,email TEXT," +
                    "username TEXT,password TEXT,read_persediaan INTEGER, write_persediaan INTEGER," +
                    "read_pembelian INTEGER,write_pembelian INTEGER, " +
                    "read_penjualan INTEGER,write_penjualan INTEGER," +
                    "read_laporan INTEGER,read_user INTEGER)");

            db.execSQL("CREATE TABLE pengaturan(id INTEGER PRIMARY KEY,default_printer TEXT,host_sync TEXT," +
                    "sync_time REAL,issync INTEGER,view_tipe INTEGER)");
            db.execSQL("CREATE TABLE cekout(id INTEGER PRIMARY KEY, total TEXT, uang TEXT, kembalian TEXT)");

            db.execSQL("INSERT INTO perusahaan(id,nama_usaha,alamat_usaha,nohp_usaha,email_usaha,website,device_id)" +
                    "VALUES(1,'Hello World','World','123456789','helloworld@example.com','www.example.com','001')");
            db.execSQL("INSERT INTO cekout (id,total,uang,kembalian)VALUES(1113,'1','1','1')");
            db.execSQL("INSERT INTO pengaturan(id,default_printer,host_sync,sync_time,issync,view_tipe)" +
                    "VALUES(1,'pos','www.epson.co.id',50,0,0)");
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }



    public String getkodetransaksi(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "";
        String kode = "";
        String newkode = "";
        String curentkode = new SimpleDateFormat("yyMMdd").format(new Date());
        if (type.equals("PB")) {
            query = "SELECT kode_pembelian_master FROM pembelian_master ORDER BY id DESC LIMIT 1";
        } else {
            query = "SELECT kode_penjualan_master FROM penjualan_master ORDER BY id DESC LIMIT 1";
        }
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            kode = c.getString(0);
        }

        if (kode.equals("") || kode == null) {
            newkode = type + "/" + curentkode + "/1";
        } else {
            String head = kode.split("/")[0];
            String mid = kode.split("/")[1];
            String end = kode.split("/")[2];
            if (mid.equals(curentkode)) {
                int plusend = Integer.parseInt(end) + 1;
                newkode = head + "/" + mid + "/" + plusend;
            } else {
                newkode = type + "/" + curentkode + "/1";
            }
        }
        c.close();
        //db.close();
        return newkode;
    }

}
