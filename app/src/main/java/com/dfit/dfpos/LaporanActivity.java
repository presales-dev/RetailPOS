package com.dfit.dfpos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class LaporanActivity extends AppCompatActivity {

    ListView lvdata;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan);
        lvdata = findViewById(R.id.lvdata);
        loaddatalist();
    }

    private void loaddatalist() {
        List<Listviewglobaladapter.listglobalmodel> ls = new ArrayList<>();
        ls.add(new Listviewglobaladapter.listglobalmodel("0", "Laporan Penjualan", "Cek laporan penjualan"));
        ls.add(new Listviewglobaladapter.listglobalmodel("2", "Laporan Stok", "Cek status stok ketersediaan item"));
        ls.add(new Listviewglobaladapter.listglobalmodel("3", "Rangking Stok", "Cek item paling laris"));
        ls.add(new Listviewglobaladapter.listglobalmodel("4", "Laporan Margin Penjualan", "Cek margin penjualan"));
        //ls.add(new Listviewglobaladapter.listglobalmodel("5", "Purchasing Report", "View your purchasing record, set the period you want to view"));
        ArrayAdapter<String> adapter = new Listviewglobaladapter(this, ls);
        lvdata.setAdapter(adapter);

        lvdata.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Intent in = new Intent(LaporanActivity.this, LaporanPenjualanActivity.class);
                    startActivity(in);
                } else if (position == 4) {//1
                    Intent in = new Intent(LaporanActivity.this, LaporanPembelianActivity.class);
                    startActivity(in);
                } else if (position == 1) {
                    Intent in = new Intent(LaporanActivity.this, LaporanStokActivity.class);
                    startActivity(in);
                } else if (position == 2) {
                    Intent in = new Intent(LaporanActivity.this, LaporanRankingActivity.class);
                    startActivity(in);
                } else if (position == 3) {
                    Intent in = new Intent(LaporanActivity.this, LaporanMarginActivity.class);
                    in.putExtra("tipe", "margin");
                    startActivity(in);
                }
            }
        });
    }


}
