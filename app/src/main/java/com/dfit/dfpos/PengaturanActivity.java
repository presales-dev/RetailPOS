package com.dfit.dfpos;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public class PengaturanActivity extends AppCompatActivity {

    ListView lvdata;
    Dblocalhelper dbo;
    int printcount = 1;
    SharedPreferences sp;
    SharedPreferences.Editor ed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengaturan);
        lvdata = findViewById(R.id.lvdata);
        sp = getApplicationContext().getSharedPreferences("config", 0);
        ed = sp.edit();
        dbo = new Dblocalhelper(this);
        loaddatalist();
    }

    private void loaddatalist() {

        List<Listviewglobaladapter.listglobalmodel> ls = new ArrayList<>();
        ls.add(new Listviewglobaladapter.listglobalmodel("0", "Company Profile", "Describe the company information"));
        //ls.add(new Listviewglobaladapter.listglobalmodel("1", "Profile Pengguna", "Atur siapa saja yang boleh menggunakan aplikasi beserta hak aksesnya"));
        ls.add(new Listviewglobaladapter.listglobalmodel("2", "Setup POS Printer", "Setup your POS Printer for receipt printing")); //Printer Saat ini " + sp.getString("default_printer", "none")
        //ls.add(new Listviewglobaladapter.listglobalmodel("3", "Test Koneksi Printer", "Cek apakah printer anda sudah terkoneksi dan berfungsi dengan baik"));
        //ls.add(new Listviewglobaladapter.listglobalmodel("6", "Tipe Tampilan Menu Barang", (sp.getInt("view_tipe",0)==0)?"Tampilan Menu List":"Tampilan Menu Grid" ));

        ls.add(new Listviewglobaladapter.listglobalmodel("7", "About Application", "Application Retail POS V1.1"));
        ls.add(new Listviewglobaladapter.listglobalmodel("3", "Backup", "Cadangkan data anda untuk mengantisipasi kemungkinan data terhapus, default backup file ada pada folder kasirkubackup di internal storage anda"));
        ls.add(new Listviewglobaladapter.listglobalmodel("4", "Restore", "Pulihkan data yang sudah anda cadangkan, default restore file harus ada di dalam folder kasirkubackup di internal storage anda, pastikan data yang ingin anda pulihkan berada didalam folder tersebut "));
        //ls.add(new Listviewglobaladapter.listglobalmodel("4", "Backup", "It is recommended to backup your data as it may get destroyed or lost, default backup file is stored in internal storage"));
        //ls.add(new Listviewglobaladapter.listglobalmodel("5", "Restore", "Restore the data you have backup, default restore file have to be in the folder \"kasirkubackup\" in internal storage. Make sure the data you want to restore is in the folder mentioned "));
        final ArrayAdapter<String> adapter = new Listviewglobaladapter(this, ls);
        lvdata.setAdapter(adapter);

        lvdata.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                if (position == 0) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(PengaturanActivity.this);
                    adb.setTitle("Company Information");
                    adb.setCancelable(false);
                    final EditText ednama_usaha = new EditText(PengaturanActivity.this);
                    final EditText edalamat_usaha = new EditText(PengaturanActivity.this);
                    final EditText ednohp_usaha = new EditText(PengaturanActivity.this);
                    final EditText edemail_usaha = new EditText(PengaturanActivity.this);
                    final EditText edwebsite = new EditText(PengaturanActivity.this);

                    TextInputLayout tilnama_usaha = new TextInputLayout(PengaturanActivity.this);
                    tilnama_usaha.addView(ednama_usaha);
                    tilnama_usaha.setHint("Company Name");

                    TextInputLayout tilalamat_usaha = new TextInputLayout(PengaturanActivity.this);
                    tilalamat_usaha.addView(edalamat_usaha);
                    tilalamat_usaha.setHint("Address");

                    TextInputLayout tilnohp_usaha = new TextInputLayout(PengaturanActivity.this);
                    tilnohp_usaha.addView(ednohp_usaha);
                    tilnohp_usaha.setHint("Handphone No");

                    TextInputLayout tilemail = new TextInputLayout(PengaturanActivity.this);
                    tilemail.addView(edemail_usaha);
                    tilemail.setHint("Email");

                    TextInputLayout tilweb = new TextInputLayout(PengaturanActivity.this);
                    tilweb.addView(edwebsite);
                    tilweb.setEnabled(false);
                    tilweb.setHint("Serial Status");

                    LinearLayout ll = new LinearLayout(PengaturanActivity.this);
                    ll.setPadding(10, 10, 10, 10);
                    ll.setOrientation(LinearLayout.VERTICAL);
                    ll.addView(tilnama_usaha);
                    ll.addView(tilalamat_usaha);
                    ll.addView(tilnohp_usaha);
                    ll.addView(tilemail);
                    ll.addView(tilweb);
                    adb.setView(ll);


                    SQLiteDatabase db = dbo.getReadableDatabase();
                    Cursor c = db.rawQuery("SELECT nama_usaha,alamat_usaha,nohp_usaha,email_usaha,website FROM perusahaan WHERE id=1", null);
                    if (c.moveToFirst()) {
                        ednama_usaha.setText(c.getString(0));
                        edalamat_usaha.setText(c.getString(1));
                        ednohp_usaha.setText(c.getString(2));
                        edemail_usaha.setText(c.getString(3));
                        edwebsite.setText(c.getString(4));
                    }
                    c.close();
                    db.close();


                    adb.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SQLiteDatabase db = dbo.getWritableDatabase();
                            db.beginTransaction();
                            try {
                                String nama_usaha = ednama_usaha.getText().toString();
                                String alamat = edalamat_usaha.getText().toString();
                                String nohp = ednohp_usaha.getText().toString();
                                String email = edemail_usaha.getText().toString();
                                String website = edwebsite.getText().toString();
                                db.execSQL("UPDATE perusahaan SET nama_usaha='" + nama_usaha + "', alamat_usaha='" + alamat + "'," +
                                        "nohp_usaha='" + nohp + "',email_usaha='" + email + "',website='" + website + "' WHERE id=1");
                                db.setTransactionSuccessful();
                                Toast.makeText(PengaturanActivity.this, "Information successfully saved", Toast.LENGTH_SHORT).show();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            } finally {
                                db.endTransaction();
                                db.close();
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

                } else if (position == 10) {//tadinya 1
                    if (sp.getInt("read_user", 0) == 1) {
                        Intent in = new Intent(PengaturanActivity.this, PenggunaActivity.class);
                        startActivity(in);

                    } else {
                        Toast.makeText(PengaturanActivity.this, "Process rejected, you do not have access right", Toast.LENGTH_SHORT).show();
                    }

                } else if (position == 1) {
                    Intent in = new Intent(PengaturanActivity.this, AddPrinterActivity.class);
                    startActivity(in);
                    /*final BluetoothAdapter btadapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice bdev;
                    if (btadapter.isEnabled()) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(PengaturanActivity.this);
                        adb.setTitle("Pilih Perangkat");
                        adb.setCancelable(false);

                        Set<BluetoothDevice> paireddevice = btadapter.getBondedDevices();
                        final List<String> ls = new ArrayList<>();
                        ls.add("none");
                        for (BluetoothDevice btdev : paireddevice) {
                            ls.add(btdev.getName());

                        }
                        int currentprinter = ls.indexOf(sp.getString("default_printer", "none"));
                        final String[] isi = ls.toArray(new String[ls.size()]);
                        adb.setSingleChoiceItems(isi, currentprinter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SQLiteDatabase db = dbo.getWritableDatabase();
                                db.beginTransaction();
                                try {
                                    db.execSQL("UPDATE pengaturan SET default_printer='" + isi[which] + "' WHERE id=1");
                                    db.setTransactionSuccessful();
                                    Toast.makeText(PengaturanActivity.this, isi[which], Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    db.endTransaction();
                                    db.close();
                                }
                                ed.putString("default_printer", isi[which].split("---")[0]);
                                ed.apply();


                            }
                        });
                        adb.setPositiveButton("Tutup", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        adb.show();
                    } else {
                        Intent in = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(in, 5);
                    }*/

                } else if (position == 8) {
                    BluetoothAdapter btadapter = BluetoothAdapter.getDefaultAdapter();
                    if (btadapter.isEnabled()) {
                        if (sp.getString("default_printer", "none").equals("none")) {
                            Toast.makeText(PengaturanActivity.this, "No Printer", Toast.LENGTH_SHORT).show();
                        } else {
                            if (printcount > 1) {
                                Toast.makeText(PengaturanActivity.this, "Printer can be used", Toast.LENGTH_SHORT).show();
                            } else {
                                printcount = 2;
                                String textprint = "Lorem ipsum dolor sit amet,consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. \n\n\n";
                                byte[] bt = textprint.getBytes();
                                Toast.makeText(PengaturanActivity.this, String.valueOf(bt.length), Toast.LENGTH_SHORT).show();
                                Bluetoothprint bprint = new Bluetoothprint(PengaturanActivity.this);
                                bprint.print(textprint);
                            }
                        }
                    } else {
                        Intent in = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(in, 5);
                    }


                } else if (position == 3) { //tadinya 4
                    AlertDialog.Builder adb = new AlertDialog.Builder(PengaturanActivity.this);
                    adb.setCancelable(false);
                    adb.setTitle("Information");
                    adb.setMessage("Backup data will be moved automatically to App folder in internal storage, " +
                            "do not delete the backup in this folder" +
                            "you can copy the backup file to SDCard and any storage " +
                            "and to restore it, you need to move the backup file to FnBApps folder ");
                    adb.setPositiveButton("Backup", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            File kasiroffbackup = new File(Environment.getExternalStorageDirectory(), "FnBApps");
                            if (ActivityCompat.checkSelfPermission(PengaturanActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(PengaturanActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(PengaturanActivity.this,
                                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                android.Manifest.permission.CAMERA}, 1);
                                if (!kasiroffbackup.exists()) {
                                    kasiroffbackup.mkdirs();
                                }
                                return;
                            }

                            if (!kasiroffbackup.exists()) {
                                kasiroffbackup.mkdirs();
                            }

                            File lokasidb = getDatabasePath("kasirku.db");
                            File lokasibackupdb = new File(Environment.getExternalStorageDirectory(), "FnBApps/database.db");
                            File lokasiimage = new File(getFilesDir(), "kasirkuimage");
                            File lokasibackupimage = new File(Environment.getExternalStorageDirectory(), "FnBApps/Image");
                            try {
                                Oneforallfunc.copyfile(lokasidb, lokasibackupdb);
                                Oneforallfunc.copyfile(lokasiimage, lokasibackupimage);
                                Toast.makeText(PengaturanActivity.this, "Backup Completed", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    adb.show();


                } else if (position == 4) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(PengaturanActivity.this);
                    adb.setCancelable(false);
                    adb.setTitle("Information");
                    adb.setMessage("Make sure the kasirkubackup folder is not deleted in internal storage." +
                            "make sure the backup file is inside the folder, otherwise please" +
                            "copy the file to kasirkubackup folder before restoring it");
                    adb.setPositiveButton("Restore", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (ActivityCompat.checkSelfPermission(PengaturanActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(PengaturanActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(PengaturanActivity.this,
                                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                android.Manifest.permission.CAMERA}, 1);
                                return;
                            }

                            File lokasidb = getDatabasePath("kasirku.db");
                            File lokasibackupdb = new File(Environment.getExternalStorageDirectory(), "FnBApps/kasirku.db");
                            File lokasiimage = new File(getFilesDir(), "kasirkuimage");
                            File lokasibackupimage = new File(Environment.getExternalStorageDirectory(), "kasirkubackup/kasirkuimage");
                            try {
                                Oneforallfunc.copyfile(lokasibackupdb, lokasidb);
                                Oneforallfunc.copyfile(lokasibackupimage, lokasiimage);
                                Toast.makeText(PengaturanActivity.this, "Data Restore Completed", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(PengaturanActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    });
                    adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    adb.show();

                } else if (position == 6) {
                    String pilihan[] = {"List", "Grid"};
                    AlertDialog.Builder adb = new AlertDialog.Builder(PengaturanActivity.this);
                    adb.setTitle("Select a Display Type");
                    adb.setSingleChoiceItems(pilihan, sp.getInt("view_tipe",0), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                SQLiteDatabase db = dbo.getWritableDatabase();
                                db.beginTransaction();
                                try {
                                    db.execSQL("UPDATE pengaturan SET view_tipe=0 WHERE id=1");
                                    db.setTransactionSuccessful();
                                    Toast.makeText(PengaturanActivity.this, "Display List", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    db.endTransaction();
                                    db.close();
                                }
                                ed.putInt("view_tipe", 0);
                                ed.apply();
                            } else {
                                SQLiteDatabase db = dbo.getWritableDatabase();
                                db.beginTransaction();
                                try {
                                    db.execSQL("UPDATE pengaturan SET view_tipe=0 WHERE id=1");
                                    db.setTransactionSuccessful();
                                    Toast.makeText(PengaturanActivity.this, "Display Grid", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    db.endTransaction();
                                    db.close();
                                }
                                ed.putInt("view_tipe", 1);
                                ed.apply();
                            }
                        }
                    });
                    adb.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    adb.show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "Bluetooth activated", Toast.LENGTH_SHORT).show();
        }
    }
}
