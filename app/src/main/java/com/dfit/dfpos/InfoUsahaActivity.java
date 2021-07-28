package com.dfit.dfpos;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class InfoUsahaActivity extends AppCompatActivity {
    EditText ednama_usaha,edalamat,ednohp,edemail,edwebsite;
    Button bsimpan;
    Dblocalhelper dbo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_usaha);
        ednama_usaha=findViewById(R.id.ednama_usaha);
        edalamat=findViewById(R.id.edalamat);
        ednohp=findViewById(R.id.ednohp);
        edemail=findViewById(R.id.edemail);
        edwebsite=findViewById(R.id.edwebsite);
        bsimpan=findViewById(R.id.bsimpan);
        dbo=new Dblocalhelper(this);
        loaddata();
        simpan();
    }

    private void loaddata(){
        SQLiteDatabase db=dbo.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT nama_usaha,alamat_usaha,nohp_usaha,email_usaha,website FROM perusahaan WHERE id=1",null);
        if(c.moveToFirst()){
            ednama_usaha.setText(c.getString(0));
            edalamat.setText(c.getString(1));
            ednohp.setText(c.getString(2));
            edemail.setText(c.getString(3));
            edwebsite.setText(c.getString(4));
        }
        c.close();
        db.close();
    }

    private void simpan(){
        bsimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db=dbo.getWritableDatabase();
                db.beginTransaction();
                try {
                    String nama_usaha=ednama_usaha.getText().toString();
                    String alamat=edalamat.getText().toString();
                    String nohp=ednohp.getText().toString();
                    String email=edemail.getText().toString();
                    String website=edwebsite.getText().toString();
                    db.execSQL("UPDATE perusahaan SET nama_usaha='"+nama_usaha+"', alamat_usaha='"+alamat+"'," +
                            "nohp_usaha='"+nohp+"',email_usaha='"+email+"',website='"+website+"' WHERE id=1");
                    db.setTransactionSuccessful();
                    Toast.makeText(InfoUsahaActivity.this, "Information updated successfully", Toast.LENGTH_SHORT).show();
                }catch (Exception ex){
                    ex.printStackTrace();
                }finally {
                    db.endTransaction();
                    db.close();
                }
            }
        });

    }
}
