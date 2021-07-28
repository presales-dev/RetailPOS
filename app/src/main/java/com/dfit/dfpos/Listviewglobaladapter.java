package com.dfit.dfpos;

import android.app.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Minami on 27/09/2019.
 */

public class Listviewglobaladapter extends ArrayAdapter<String> {

    Activity ct;
    List<listglobalmodel> ls;

    public Listviewglobaladapter(Activity ct, List ls) {
        super(ct, R.layout.listviewglobaladapter,ls);
        this.ct = ct;
        this.ls=ls;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater=ct.getLayoutInflater();
        View v=inflater.inflate(R.layout.listviewglobaladapter,null,true);

        TextView ltiitle=v.findViewById(R.id.ljudul);
        TextView lsubtitt=v.findViewById(R.id.lsubjudul);

        ltiitle.setText(ls.get(position).getJudul());
        lsubtitt.setText(ls.get(position).getSubjudul());

        return v;
    }

    public static class listglobalmodel{
        String id,judul,subjudul;

        public listglobalmodel(String id, String judul, String subjudul) {
            this.id = id;
            this.judul = judul;
            this.subjudul = subjudul;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getJudul() {
            return judul;
        }

        public void setJudul(String judul) {
            this.judul = judul;
        }

        public String getSubjudul() {
            return subjudul;
        }

        public void setSubjudul(String subjudul) {
            this.subjudul = subjudul;
        }
    }


}
