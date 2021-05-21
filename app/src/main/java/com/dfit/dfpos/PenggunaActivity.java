package com.dfit.dfpos;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class PenggunaActivity extends AppCompatActivity {

    FloatingActionButton fbtambahpengguna;
    ListView lvdata;
    Dblocalhelper dbo;
    List<Listviewglobaladapter.listglobalmodel> ls = new ArrayList<>();
    String passlama = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengguna);
        fbtambahpengguna = findViewById(R.id.fbtambahpengguna);
        lvdata = findViewById(R.id.lvdata);
        dbo = new Dblocalhelper(this);
        loaddata();
        adduser();
    }

    private void loaddata() {
        ls.clear();
        SQLiteDatabase db = dbo.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT kode_user,email,username,password,read_persediaan,write_persediaan, " +
                "read_pembelian,write_pembelian,read_penjualan,write_penjualan,read_laporan,read_user FROM pengguna", null);

        while (c.moveToNext()) {
            ls.add(new Listviewglobaladapter.listglobalmodel(c.getString(0), c.getString(2), c.getString(1)));
        }
        ArrayAdapter<String> adapter = new Listviewglobaladapter(this, ls);
        lvdata.setAdapter(adapter);
        c.close();
        db.close();

        lvdata.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String kode = ls.get(position).getId();
                userop(kode);
            }
        });

        lvdata.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder adb = new AlertDialog.Builder(PenggunaActivity.this);
                adb.setTitle("Konfirmasi");
                adb.setMessage("Yakin Ingin Menghapus Data Ini");
                adb.setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = dbo.getWritableDatabase();
                        db.beginTransaction();
                        try {
                            db.execSQL("DELETE FROM pengguna WHERE kode_user='" + ls.get(position).getId() + "'");
                            db.setTransactionSuccessful();
                            Toast.makeText(PenggunaActivity.this, "Data successfully deleted", Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            db.endTransaction();
                            db.close();
                        }
                        loaddata();
                    }
                });

                adb.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                adb.show();
                return false;
            }
        });

    }

    private void userop(final String kode_user) {
        AlertDialog.Builder adb = new AlertDialog.Builder(PenggunaActivity.this);
        adb.setTitle("Tambah Pengguna");
        adb.setCancelable(false);
        final EditText edemail = new EditText(PenggunaActivity.this);
        edemail.setInputType(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        final EditText edusername = new EditText(PenggunaActivity.this);
        edemail.setInputType(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        final EditText edoldpasswords = new EditText(PenggunaActivity.this);
        edoldpasswords.setTransformationMethod(PasswordTransformationMethod.getInstance());
        final EditText edpasswords = new EditText(PenggunaActivity.this);
        edpasswords.setTransformationMethod(PasswordTransformationMethod.getInstance());
        final EditText edrepasswords = new EditText(PenggunaActivity.this);
        edrepasswords.setTransformationMethod(PasswordTransformationMethod.getInstance());


        TextInputLayout tilemail = new TextInputLayout(PenggunaActivity.this);
        tilemail.addView(edemail);
        tilemail.setHint("Email");

        TextInputLayout tilusername = new TextInputLayout(PenggunaActivity.this);
        tilusername.addView(edusername);
        tilusername.setHint("Username");

        TextInputLayout tiloldpassword = new TextInputLayout(PenggunaActivity.this);
        tiloldpassword.addView(edoldpasswords);
        tiloldpassword.setHint("Password Lama");

        TextInputLayout tilpassword = new TextInputLayout(PenggunaActivity.this);
        tilpassword.addView(edpasswords);
        if (kode_user.equals("")) {
            tilpassword.setHint("Password");
        } else {
            tilpassword.setHint("Password baru");
        }


        TextInputLayout tilrepassword = new TextInputLayout(PenggunaActivity.this);
        tilrepassword.addView(edrepasswords);
        tilrepassword.setHint("Konfirmasi Password");


        final CheckBox ckread_persediaan = new CheckBox(PenggunaActivity.this);
        ckread_persediaan.setText("Lihat Persediaan");

        final CheckBox ckwrite_persediaan = new CheckBox(PenggunaActivity.this);
        ckwrite_persediaan.setText("Tambah Persediaan");

        final CheckBox ckread_pembelian = new CheckBox(PenggunaActivity.this);
        ckread_pembelian.setText("Lihat Pembelian");

        final CheckBox ckwrite_pembelian = new CheckBox(PenggunaActivity.this);
        ckwrite_pembelian.setText("Tambah Pembelian");

        final CheckBox ckread_penjualan = new CheckBox(PenggunaActivity.this);
        ckread_penjualan.setText("Lihat Penjualan");

        final CheckBox ckwrite_penjualan = new CheckBox(PenggunaActivity.this);
        ckwrite_penjualan.setText("Tambah Penjualan");

        final CheckBox ckread_laporan = new CheckBox(PenggunaActivity.this);
        ckread_laporan.setText("Lihat Laporan");

        final CheckBox ckread_pengguna = new CheckBox(PenggunaActivity.this);
        ckread_pengguna.setText("Lihat Pengguna");


        LinearLayout ll = new LinearLayout(PenggunaActivity.this);
        ll.setPadding(10, 10, 10, 10);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(tilemail);
        ll.addView(tilusername);
        if (!kode_user.equals("")) {
            ll.addView(tiloldpassword);
        }
        ll.addView(tilpassword);
        ll.addView(tilrepassword);
        ll.addView(ckread_persediaan);
        ll.addView(ckwrite_persediaan);
        ll.addView(ckread_pembelian);
        ll.addView(ckwrite_pembelian);
        ll.addView(ckread_penjualan);
        ll.addView(ckwrite_penjualan);
        ll.addView(ckread_laporan);
        ll.addView(ckread_pengguna);
        adb.setView(ll);

        if (!kode_user.equals("")) {
            SQLiteDatabase db = dbo.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT kode_user,email,username,password,read_persediaan,write_persediaan, " +
                    "read_pembelian,write_pembelian,read_penjualan,write_penjualan,read_laporan,read_user FROM pengguna " +
                    "WHERE kode_user='" + kode_user + "'", null);
            if (c.moveToFirst()) {
                edemail.setText(c.getString(1));
                edusername.setText(c.getString(2));
                passlama = c.getString(3);
                c.getString(3);
                if (c.getInt(4) == 1) {
                    ckread_persediaan.setChecked(true);
                }

                if (c.getInt(5) == 1) {
                    ckwrite_persediaan.setChecked(true);
                    ckread_persediaan.setChecked(true);
                    ckread_persediaan.setEnabled(false);
                }

                if (c.getInt(6) == 1) {
                    ckread_pembelian.setChecked(true);
                }

                if (c.getInt(7) == 1) {
                    ckwrite_pembelian.setChecked(true);
                    ckread_pembelian.setChecked(true);
                    ckread_pembelian.setEnabled(false);
                }

                if (c.getInt(8) == 1) {
                    ckread_penjualan.setChecked(true);
                }

                if (c.getInt(9) == 1) {
                    ckwrite_penjualan.setChecked(true);
                    ckread_penjualan.setChecked(true);
                    ckread_penjualan.setEnabled(false);
                }

                if (c.getInt(10) == 1) {
                    ckread_laporan.setChecked(true);
                }

                if (c.getInt(11) == 1) {
                    ckread_pengguna.setChecked(true);
                }


            }
            c.close();
            db.close();
        }

        ckwrite_persediaan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    ckread_persediaan.setChecked(true);
                    ckread_persediaan.setEnabled(false);
                } else {
                    ckread_persediaan.setChecked(false);
                    ckread_persediaan.setEnabled(true);
                }
            }
        });

        ckwrite_pembelian.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    ckread_pembelian.setChecked(true);
                    ckread_pembelian.setEnabled(false);
                } else {
                    ckread_pembelian.setChecked(false);
                    ckread_pembelian.setEnabled(true);
                }
            }
        });

        ckwrite_penjualan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    ckread_penjualan.setChecked(true);
                    ckread_penjualan.setEnabled(false);
                } else {
                    ckread_penjualan.setChecked(false);
                    ckread_penjualan.setEnabled(true);
                }
            }
        });


        adb.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (edemail.getText().toString().equals("") ||
                        edusername.getText().toString().equals("") ||
                        edpasswords.getText().toString().equals("") ||
                        edrepasswords.getText().toString().equals("")) {

                    Toast.makeText(PenggunaActivity.this, "Operasi Gagal, Inputan tidak boleh kosong", Toast.LENGTH_SHORT).show();

                } else {
                    if (edpasswords.getText().toString().equals(edrepasswords.getText().toString())) {
                        SQLiteDatabase db = dbo.getWritableDatabase();
                        try {
                            db.beginTransaction();

                            int iread_persediaan = 0, iwrite_persediaan = 0, iread_pembelian = 0, iwrite_pembelian = 0,
                                    iread_penjualan = 0, iwrite_penjualan = 0, iread_laporan = 0, iread_pengguna = 0;


                            if (ckread_persediaan.isChecked()) {
                                iread_persediaan = 1;
                            } else {
                                iread_persediaan = 0;
                            }

                            if (ckwrite_persediaan.isChecked()) {
                                iwrite_persediaan = 1;
                            } else {
                                iwrite_persediaan = 0;
                            }

                            if (ckread_pembelian.isChecked()) {
                                iread_pembelian = 1;
                            } else {
                                iread_pembelian = 0;
                            }

                            if (ckwrite_pembelian.isChecked()) {
                                iwrite_pembelian = 1;
                            } else {
                                iwrite_pembelian = 0;
                            }

                            if (ckread_penjualan.isChecked()) {
                                iread_penjualan = 1;
                            } else {
                                iread_penjualan = 0;
                            }

                            if (ckwrite_penjualan.isChecked()) {
                                iwrite_penjualan = 1;
                            } else {
                                iwrite_penjualan = 0;
                            }

                            if (ckread_laporan.isChecked()) {
                                iread_laporan = 1;
                            } else {
                                iread_laporan = 0;
                            }

                            if (ckread_pengguna.isChecked()) {
                                iread_pengguna = 1;
                            } else {
                                iread_pengguna = 0;
                            }


                            if (kode_user.equals("")) {
                                String query = "INSERT INTO pengguna(email,username,password,read_persediaan,write_persediaan," +
                                        "read_pembelian,write_pembelian,read_penjualan,write_penjualan," +
                                        "read_laporan,read_user)" +
                                        "VALUES('" + edemail.getText().toString() + "'," +
                                        "'" + edusername.getText().toString() + "'," +
                                        "'" + edpasswords.getText().toString() + "'," + iread_persediaan + "," +
                                        "" + iwrite_persediaan + "," + iread_pembelian + "," + iwrite_pembelian + "," +
                                        "" + iread_penjualan + "," + iwrite_penjualan + "," + iread_laporan + "," + iread_pengguna + ")";
                                db.execSQL(query);
                                Toast.makeText(PenggunaActivity.this, "Data Berhasil Disimpan", Toast.LENGTH_SHORT).show();
                            } else {
                                if (passlama.equals(edoldpasswords.getText().toString())) {
                                    String query = "UPDATE pengguna  SET email='" + edemail.getText().toString() + "'," +
                                            "username='" + edusername.getText().toString() + "'," +
                                            "password='" + edpasswords.getText().toString() + "',read_persediaan=" + iread_persediaan + "," +
                                            "write_persediaan=" + iwrite_persediaan + ",read_pembelian=" + iread_pembelian + ",write_pembelian=" + iwrite_pembelian + "," +
                                            "read_penjualan=" + iread_penjualan + ",write_penjualan=" + iwrite_penjualan + ",read_laporan=" + iread_laporan + ",read_user=" + iread_pengguna + " " +
                                            "WHERE kode_user='" + kode_user + "'";
                                    db.execSQL(query);
                                    Toast.makeText(PenggunaActivity.this, "Data Berhasil Disimpan", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(PenggunaActivity.this, "Data Gagal Disimpan,Password Lama Tidak Cocok", Toast.LENGTH_SHORT).show();
                                }

                            }
                            db.setTransactionSuccessful();

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            db.endTransaction();
                            db.close();
                        }
                        loaddata();
                    } else {
                        Toast.makeText(PenggunaActivity.this, "Data Gagal Disimpan, Konfirmasi Password Tidak Cocok", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        adb.setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        adb.show();
    }

    private void adduser() {
        fbtambahpengguna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userop("");
            }
        });

    }
}
