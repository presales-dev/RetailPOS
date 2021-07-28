package com.dfit.dfpos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PenjualanActivity extends AppCompatActivity {

    FloatingActionButton fbadd;
    ImageView breload;
    RecyclerView rvdata;
    RecyclerView.LayoutManager layman;
    PenjualanAdapter adapter;
    android.widget.SearchView svdata;
    Dblocalhelper dbo;
    ArrayList<PenjualanModel> lsdata=new ArrayList<>();
    private int currentoffset=0;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penjualan);
        fbadd=findViewById(R.id.fbadd);
        breload=findViewById(R.id.breload);
        rvdata=findViewById(R.id.rvdata);
        layman=new LinearLayoutManager(this);
        rvdata.setLayoutManager(layman);
        rvdata.setHasFixedSize(true);
        rvdata.setItemAnimator(new DefaultItemAnimator());
        adapter=new PenjualanAdapter(lsdata,this);
        rvdata.setAdapter(adapter);
        svdata=findViewById(R.id.svdata);
        sp=getApplicationContext().getSharedPreferences("config",0);
        dbo=new Dblocalhelper(this);
        adddata();
        lsdata.clear();
        loaddata();
        rvdata.addOnScrollListener(new EndlessScroll() {
            @Override
            public void onLoadMore() {
                if(svdata.getQuery()==null || (svdata.getQuery().toString().equals(""))){
                    Toast.makeText(PenjualanActivity.this, "loading data", Toast.LENGTH_SHORT).show();
                    loaddata();
                    currentoffset=currentoffset+100;
                }

            }
        });
        caridata();
        reloaddata();
        //svdata.setIconified(false);
        //svdata.setQueryHint("Cari : Ketik Kode,Tanggal atau Deskripsi");
    }

    private void adddata(){
        fbadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sp.getInt("write_penjualan",0)==1) {
                    Intent in=new Intent(PenjualanActivity.this,TambahPenjualanActivity.class);
                    startActivity(in);
                }else{
                    Toast.makeText(PenjualanActivity.this, "Process rejected, access denied", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loaddata(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db=dbo.getReadableDatabase();
                try {
                    String query="SELECT kode_penjualan_master,tanggal_penjualan,status,deskripsi," +
                            "(SELECT SUM(pjd.jumlah*(pjd.harga_jual-pjd.diskon)) " +
                            "FROM penjualan_detail pjd INNER JOIN persediaan ps ON pjd.kode_barang=ps.kode_barang " +
                            "WHERE kode_penjualan_master=pjm.kode_penjualan_master) " +
                            "FROM penjualan_master pjm ORDER BY date_created DESC LIMIT 100 OFFSET "+currentoffset+" ";
                    Cursor c=db.rawQuery(query,null);
                    while (c.moveToNext()){

                        lsdata.add(new PenjualanModel(c.getString(0),c.getString(1),
                                c.getInt(2), c.getString(3),c.getDouble(4)));
                    };


                    adapter.notifyDataSetChanged();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    db.close();
                }
            }
        },100);


    }

    private void caridata(){
        svdata.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final String setquery=query.toLowerCase();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        lsdata.clear();
                        SQLiteDatabase db=dbo.getReadableDatabase();
                        try {

                            String query="SELECT kode_penjualan_master,tanggal_penjualan,status,deskripsi," +
                                    "(SELECT SUM(pjd.jumlah*(pjd.harga_jual-pjd.diskon)) " +
                                    "FROM penjualan_detail pjd INNER JOIN persediaan ps ON pjd.kode_barang=ps.kode_barang " +
                                    "WHERE kode_penjualan_master=pjm.kode_penjualan_master) " +
                                    "FROM penjualan_master pjm WHERE " +
                                    "kode_penjualan_master LIKE '%"+setquery+"%' OR " +
                                    "tanggal_penjualan LIKE '%"+setquery+"%' OR " +
                                    "deskripsi LIKE '%"+setquery+"%' "+
                                    "ORDER BY date_created DESC LIMIT 100";
                            Cursor c=db.rawQuery(query,null);
                            while (c.moveToNext()){

                                lsdata.add(new PenjualanModel(c.getString(0),c.getString(1),
                                        c.getInt(2), c.getString(3),c.getDouble(4)));
                            };

                            adapter.notifyDataSetChanged();
                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            db.close();
                        }
                    }
                },100);



                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void reloaddata(){
        breload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EndlessScroll.mPreviousTotal=0;
                lsdata.clear();
                currentoffset=0;
                loaddata();
                svdata.setQuery("",false);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        EndlessScroll.mPreviousTotal=0;
        lsdata.clear();
        currentoffset=0;
        loaddata();
        svdata.setQuery("",false);
    }

    public class PenjualanAdapter extends RecyclerView.Adapter {
        ArrayList<PenjualanModel> model=new ArrayList<>();
        Context ct;
        NumberFormat nf=NumberFormat.getInstance();
        int content=0;

        public PenjualanAdapter(ArrayList<PenjualanModel> model, Context ct) {
            this.model = model;
            this.ct = ct;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater lin=LayoutInflater.from(parent.getContext());
            View v=lin.inflate(R.layout.adapter_penjualan,parent,false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if(holder instanceof Holder){
                Holder h=(Holder) holder;
                h.lno_transaksi.setText(model.get(position).getKode_penjualan_master()+" | "+model.get(position).getTanggal_penjualan());
                if(model.get(position).getStatus()==0){
                    h.lstatus.setText("On Hold");
                    h.lstatus.setTextColor(Color.parseColor("#FFDF1227"));
                }else{
                    h.lstatus.setText("Completed");
                    h.lstatus.setTextColor(Color.parseColor("#6e18c9"));
                }

                h.ldesk.setText(model.get(position).getDeskripsi());
                h.ljumlah.setText("Total : "+nf.format(model.get(position).getTotal_penjualan()));
                h.img_optionmenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popmenu=new PopupMenu(ct,v);
                        popmenu.inflate(R.menu.penjualan_option_menu);
                        popmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()){
                                    case  R.id.medit:
                                        if(model.get(position).getStatus()==1){
                                            Toast.makeText(ct, "You are not allowed to edit Completed Transaction", Toast.LENGTH_SHORT).show();
                                        }else{
                                            Intent in=new Intent(PenjualanActivity.this,TambahPenjualanActivity.class);
                                            in.putExtra("kode_penjualan_master",model.get(position).getKode_penjualan_master());
                                            startActivity(in);
                                        }
                                        break;
                                    case R.id.mhapus:
                                        AlertDialog.Builder adb=new AlertDialog.Builder(ct);
                                        adb.setTitle("Confirmation");
                                        adb.setMessage("Confirm to delete "+model.get(position).getKode_penjualan_master()+" ? ");
                                        adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                SQLiteDatabase db=dbo.getWritableDatabase();
                                                db.beginTransaction();
                                                try {

                                                    Cursor c=db.rawQuery("SELECT kode_barang,jumlah FROM penjualan_detail WHERE kode_penjualan_master='"+model.get(position).getKode_penjualan_master()+"'",null);
                                                    while (c.moveToNext()){
                                                        Cursor ccek=db.rawQuery("SELECT tipe_barang FROM persediaan WHERE kode_barang='"+c.getString(0)+"'",null);
                                                        if(ccek.moveToFirst()){
                                                            if(ccek.getInt(0)==1){
                                                                String kode_barang_racik =  c.getString(0);
                                                                Cursor ccekup = db.rawQuery("SELECT kode_barang_isi,jumlah_isi FROM racikan WHERE kode_barang_racik='" + kode_barang_racik + "'", null);
                                                                while (ccekup.moveToNext()) {
                                                                    db.execSQL("UPDATE persediaan SET jumlah_barang=jumlah_barang+" + (ccekup.getDouble(1)*c.getDouble(1)) + " " +
                                                                            "WHERE kode_barang='" + ccekup.getString(0) + "' ");
                                                                }
                                                            }else{
                                                                db.execSQL("UPDATE persediaan SET jumlah_barang=jumlah_barang+" + c.getDouble(1) + " " +
                                                                        "WHERE kode_barang='" + c.getString(0) + "' ");
                                                            }
                                                        }
                                                    }
                                                    db.execSQL("DELETE FROM penjualan_detail  WHERE kode_penjualan_master='"+model.get(position).getKode_penjualan_master()+"'");
                                                    db.execSQL("DELETE FROM penjualan_master WHERE kode_penjualan_master='"+model.get(position).getKode_penjualan_master()+"'");
                                                    db.setTransactionSuccessful();
                                                    model.remove(position);
                                                    notifyDataSetChanged();
                                                }catch (Exception ex){
                                                    ex.printStackTrace();
                                                }finally {
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

    public class Holder extends RecyclerView.ViewHolder{
        TextView lno_transaksi,ldesk,ljumlah,lstatus;
        ImageView img_optionmenu;
        public Holder(View itemView) {
            super(itemView);
            lno_transaksi=itemView.findViewById(R.id.lnama_barang);
            lstatus=itemView.findViewById(R.id.lstatus);
            ldesk=itemView.findViewById(R.id.ldesk);
            ljumlah=itemView.findViewById(R.id.ljudul);
            img_optionmenu=itemView.findViewById(R.id.img_optionmenu);
        }
    }

    public class PenjualanModel{
        String kode_penjualan_master ,tanggal_penjualan;
        int status;
        String deskripsi;
        double total_penjualan;

        public PenjualanModel(String kode_penjualan_master, String tanggal_penjualan, int status, String deskripsi, double total_penjualan) {
            this.kode_penjualan_master = kode_penjualan_master;
            this.tanggal_penjualan = tanggal_penjualan;
            this.status = status;
            this.deskripsi = deskripsi;
            this.total_penjualan = total_penjualan;
        }

        public String getKode_penjualan_master() {
            return kode_penjualan_master;
        }

        public void setKode_penjualan_master(String kode_penjualan_master) {
            this.kode_penjualan_master = kode_penjualan_master;
        }

        public String getTanggal_penjualan() {
            return tanggal_penjualan;
        }

        public void setTanggal_penjualan(String tanggal_penjualan) {
            this.tanggal_penjualan = tanggal_penjualan;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getDeskripsi() {
            return deskripsi;
        }

        public void setDeskripsi(String deskripsi) {
            this.deskripsi = deskripsi;
        }

        public double getTotal_penjualan() {
            return total_penjualan;
        }

        public void setTotal_penjualan(double total_penjualan) {
            this.total_penjualan = total_penjualan;
        }
    }

}
