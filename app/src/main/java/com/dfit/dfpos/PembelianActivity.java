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

        import com.google.android.material.floatingactionbutton.FloatingActionButton;

        import java.text.NumberFormat;
        import java.util.ArrayList;

        import androidx.annotation.NonNull;
        import androidx.appcompat.app.AlertDialog;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.recyclerview.widget.DefaultItemAnimator;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

public class PembelianActivity extends AppCompatActivity {

    FloatingActionButton fbadd;
    ImageView breload;
    RecyclerView rvdata;
    RecyclerView.LayoutManager layman;
    PembelianAdapter adapter;
    android.widget.SearchView svdata;
    Dblocalhelper dbo;
    ArrayList<PembelianModel> lsdata=new ArrayList<>();
    private int currentoffset=0;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pembelian);
        fbadd=findViewById(R.id.fbadd);
        breload=findViewById(R.id.breload);
        rvdata=findViewById(R.id.rvdata);
        layman=new LinearLayoutManager(this);
        rvdata.setLayoutManager(layman);
        rvdata.setHasFixedSize(true);
        rvdata.setItemAnimator(new DefaultItemAnimator());
        adapter=new PembelianAdapter(lsdata,this);
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
                    Toast.makeText(PembelianActivity.this, "memuat data", Toast.LENGTH_SHORT).show();
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
                if(sp.getInt("write_pembelian",0)==1) {
                    Intent in=new Intent(PembelianActivity.this,TambahPembelianActivity.class);
                    startActivity(in);
                }else{
                    Toast.makeText(PembelianActivity.this, "Process rejected, you do not have access right", Toast.LENGTH_SHORT).show();
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
                    String query="SELECT kode_pembelian_master,no_faktur,tanggal_pembelian,deskripsi," +
                            "(SELECT SUM(pbd.jumlah*pbd.harga_beli) " +
                            "FROM pembelian_detail pbd INNER JOIN persediaan ps ON pbd.kode_barang=ps.kode_barang " +
                            "WHERE kode_pembelian_master=pbm.kode_pembelian_master) " +
                            "FROM pembelian_master pbm ORDER BY date_created DESC LIMIT 100 OFFSET "+currentoffset+" ";
                    Cursor c=db.rawQuery(query,null);
                    while (c.moveToNext()){

                        lsdata.add(new PembelianModel(c.getString(0),c.getString(1),
                                c.getString(2), c.getString(3),c.getDouble(4)));
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
                            String query="SELECT kode_pembelian_master,no_faktur,tanggal_pembelian,deskripsi," +
                                    "(SELECT SUM(pbd.jumlah*pbd.harga_beli) " +
                                    "FROM pembelian_detail pbd INNER JOIN persediaan ps ON pbd.kode_barang=ps.kode_barang " +
                                    "WHERE kode_pembelian_master=pbm.kode_pembelian_master) " +
                                    "FROM pembelian_master pbm WHERE " +
                                    "kode_pembelian_master LIKE '%"+setquery+"%' OR " +
                                    "no_faktur LIKE '%"+setquery+"%' OR " +
                                    "tanggal_pembelian LIKE '%"+setquery+"%' OR " +
                                    "deskripsi LIKE '%"+setquery+"%'  ORDER BY date_created DESC LIMIT 100";
                            Cursor c=db.rawQuery(query,null);
                            while (c.moveToNext()){

                                lsdata.add(new PembelianModel(c.getString(0),c.getString(1),
                                        c.getString(2), c.getString(3),c.getDouble(4)));
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

    public class PembelianAdapter extends RecyclerView.Adapter {
        ArrayList<PembelianModel> model=new ArrayList<>();
        Context ct;
        NumberFormat nf=NumberFormat.getInstance();
        int content=0;

        public PembelianAdapter(ArrayList<PembelianModel> model, Context ct) {
            this.model = model;
            this.ct = ct;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater lin=LayoutInflater.from(parent.getContext());
            View v=lin.inflate(R.layout.adapter_pembelian,parent,false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if(holder instanceof Holder){
                Holder h=(Holder) holder;
                h.lno_transaksi.setText(model.get(position).getKode_pembelian_master()+" | "+model.get(position).getTanggal_pembelian());
                h.lfaktur.setText("Invoice No : "+model.get(position).getNo_faktur());
                h.ldesk.setText(model.get(position).getDeskripsi());
                h.ljumlah.setText("Amount : "+nf.format(model.get(position).getTotal_pembelian()));
                h.img_optionmenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popmenu=new PopupMenu(ct,v);
                        popmenu.inflate(R.menu.pembelian_option_menu);
                        popmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()){
                                    case  R.id.medit:
                                        Intent in=new Intent(PembelianActivity.this,TambahPembelianActivity.class);
                                        in.putExtra("kode_pembelian_master",model.get(position).getKode_pembelian_master());
                                        startActivity(in);
                                        break;
                                    case R.id.mhapus:
                                        AlertDialog.Builder adb=new AlertDialog.Builder(ct);
                                        adb.setTitle("Confirmation");
                                        adb.setMessage("Confirm delete item "+model.get(position).getKode_pembelian_master()+" ? ");
                                        adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                SQLiteDatabase db=dbo.getWritableDatabase();
                                                db.beginTransaction();
                                                try {

                                                    Cursor c=db.rawQuery("SELECT kode_barang,jumlah FROM pembelian_detail WHERE kode_pembelian_master='"+model.get(position).getKode_pembelian_master()+"'",null);
                                                    while (c.moveToNext()){
                                                        db.execSQL("UPDATE persediaan SET jumlah_barang=jumlah_barang-"+c.getDouble(1)+" WHERE kode_barang='"+c.getString(0)+"'");
                                                    }
                                                    db.execSQL("DELETE FROM pembelian_detail  WHERE kode_pembelian_master='"+model.get(position).getKode_pembelian_master()+"'");
                                                    db.execSQL("DELETE FROM pembelian_master WHERE kode_pembelian_master='"+model.get(position).getKode_pembelian_master()+"'");
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
        TextView lno_transaksi,ldesk,ljumlah,lfaktur;
        ImageView img_optionmenu;
        public Holder(View itemView) {
            super(itemView);
            lno_transaksi=itemView.findViewById(R.id.lnama_barang);
            lfaktur=itemView.findViewById(R.id.lfaktur);
            ldesk=itemView.findViewById(R.id.ldesk);
            ljumlah=itemView.findViewById(R.id.ljudul);
            img_optionmenu=itemView.findViewById(R.id.img_optionmenu);
        }
    }

    public class PembelianModel{
        String kode_pembelian_master ,no_faktur,tanggal_pembelian,deskripsi;
        double total_pembelian;

        public PembelianModel(String kode_pembelian_master, String no_faktur, String tanggal_pembelian, String deskripsi, double total_pembelian) {
            this.kode_pembelian_master = kode_pembelian_master;
            this.no_faktur = no_faktur;
            this.tanggal_pembelian = tanggal_pembelian;
            this.deskripsi = deskripsi;
            this.total_pembelian = total_pembelian;
        }

        public String getKode_pembelian_master() {
            return kode_pembelian_master;
        }

        public void setKode_pembelian_master(String kode_pembelian_master) {
            this.kode_pembelian_master = kode_pembelian_master;
        }

        public String getNo_faktur() {
            return no_faktur;
        }

        public void setNo_faktur(String no_faktur) {
            this.no_faktur = no_faktur;
        }

        public String getTanggal_pembelian() {
            return tanggal_pembelian;
        }

        public void setTanggal_pembelian(String tanggal_pembelian) {
            this.tanggal_pembelian = tanggal_pembelian;
        }

        public String getDeskripsi() {
            return deskripsi;
        }

        public void setDeskripsi(String deskripsi) {
            this.deskripsi = deskripsi;
        }

        public double getTotal_pembelian() {
            return total_pembelian;
        }

        public void setTotal_pembelian(double total_pembelian) {
            this.total_pembelian = total_pembelian;
        }
    }
}
