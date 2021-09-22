package com.dfit.dfpos;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    EditText ednama_usaha,edalamat,ednohp,edemail,edserial,edusername,edpassword,edrepassword;
    Button bsimpan,bserial;
    Dblocalhelper dbo;

    APIInterface apiInterface;
    String userId;
    String phoneID;
    String serialin;
    String countryID = "ID"; //TODO: set this in configuration for next version

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiInterface = APIClient.getClient().create(APIInterface.class);
        ednama_usaha=findViewById(R.id.ednama_usaha);
        edalamat=findViewById(R.id.edalamat);
        ednohp=findViewById(R.id.ednohp);
        edemail=findViewById(R.id.edemail);
        edserial=findViewById(R.id.edserial);
        edusername=findViewById(R.id.edusername);
        edpassword=findViewById(R.id.edpassword);
        edrepassword=findViewById(R.id.edrepassword);
        bsimpan=findViewById(R.id.bsimpan);
        bserial=findViewById(R.id.btCekSerial);
        dbo=new Dblocalhelper(this);

        bsimpan.setEnabled(false);
        bserial.setEnabled(true);
        aggreementpopup();
        simpan();


        bserial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cekisianserial()){
                    if (CommonMethod.isNetworkAvailable(RegisterActivity.this)) {
                        verfySerial(edserial.getText().toString(), ednohp.getText().toString(), countryID);
                        /*Toast.makeText(RegisterActivity.this, "Success to Verify ", Toast.LENGTH_SHORT).show();
                        edserial.setEnabled(false);
                        ednohp.setEnabled(false);
                        bserial.setText("Verified!");
                        bserial.setEnabled(false);
                        edemail.setEnabled(false);
                        bsimpan.setEnabled(true);*/
                    }else {
                        CommonMethod.showAlert("Internet Connectivity Failure", RegisterActivity.this);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Periksa kembali kolom isian", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean checkValidation(){
        serialin = edserial.getText().toString();

        Log.e("RetailPOS", "serial is -> " + serialin);

        if (edserial.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "Serial Cannot Left Blank", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;

    }
    public void verfySerial(String userId, String password,String countryID){
        final LoginResponse login = new LoginResponse(userId, password,countryID);
        //Call<LoginResponse> call1 = apiInterface.createUser(login);

        //use string value
        Call<LoginResponse> call1 = apiInterface.createUser(login.data);


        call1.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse loginResponse = response.body();
                Log.e("retailpos", "loginResponse 1 --> " + loginResponse);
                if (loginResponse != null) {
                    Log.e("retailpos", "getSerial          -->  " + loginResponse.getSerial());
                    Log.e("retailpos", "getPhone       -->  " + loginResponse.getPhone());
                    Log.e("retailpos", "getCountry        -->  " + loginResponse.getCountry());

                    String responseCode = loginResponse.getResponseCode();
                    Log.e("retailpos", "getResponseCode  -->  " + loginResponse.getResponseCode());
                    Log.e("retailpos", "getResponseMessage  -->  " + loginResponse.getMessage());
                    Log.e("retailpos", "getStatus  -->  " + loginResponse.getStatus());
                    if (responseCode != null && responseCode.equals("404")) {
                        Toast.makeText(RegisterActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    } else if (loginResponse.getStatus().equals("true")) {
                        Toast.makeText(RegisterActivity.this, "Success to Verify ", Toast.LENGTH_SHORT).show();
                        edserial.setEnabled(false);
                        ednohp.setEnabled(false);
                        bserial.setText("Verified!");
                        bserial.setEnabled(false);
                        edemail.setEnabled(false);
                        bsimpan.setEnabled(true);
                    }else{
                        Toast.makeText(RegisterActivity.this, "Try Again, "+loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "onFailure called ", Toast.LENGTH_SHORT).show();
                call.cancel();
            }
        });
    }

    public void aggreementpopup(){

        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        //ad.setIcon(R.drawable.icon);
        ad.setTitle("Terms of Use");
        ad.setView(LayoutInflater.from(this).inflate(R.layout.aggrement_popup,null));
        ad.setCancelable(false);
        ad.setPositiveButton("Agree",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        // OK, go back to Main menu
                    }
                }
        );

        ad.setNegativeButton("Disagree", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });

        ad.show();
    }

    public boolean isEditTextEmpty(EditText mInput){
        if(TextUtils.isEmpty(mInput.getText())){
            mInput.setError("Tidak boleh kosong");
            return false;
        }else{
            return true;
        }
    }
    private boolean cekisianserial(){
        if(isEditTextEmpty(ednama_usaha) && isEditTextEmpty(edalamat) && isEditTextEmpty(edemail) && isEditTextEmpty(ednohp) && isEditTextEmpty(edserial)){
            return true;
        }else{
            return false;
        }

    }

    private boolean cekisianregis(){
        if(isEditTextEmpty(edusername) && isEditTextEmpty(edpassword) && isEditTextEmpty(edrepassword)){
            return true;
        }else{
            return false;
        }

    }
    private void simpan(){
        /*bserial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable(){
                    public void run(){
                        final ArrayList<String> urls=new ArrayList<String>(); //to read each line
                        //TextView t; //to show the result, please declare and find it inside onCreate()
                        try {
                            // Create a URL for the desired page
                            URL url = new URL("https://einpresales.com/serial.html"); //My text file location
                            //First open the connection
                            HttpURLConnection conn=(HttpURLConnection) url.openConnection();
                            conn.setConnectTimeout(60000); // timing out in a minute

                            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                            //t=(TextView)findViewById(R.id.TextView1); // ideally do this in onCreate()
                            String str;
                            while ((str = in.readLine()) != null) {
                                urls.add(str);
                            }
                            in.close();
                        } catch (Exception e) {
                            //Log.d("MyTag",e.toString());
                        }

                        //since we are in background thread, to post results we have to go back to ui thread. do the following for that

                        RegisterActivity.this.runOnUiThread(new Runnable(){
                            public void run(){
                                //t.setText(urls.get(0)); // My TextFile has 3 lines
                                //if (edwebsite.getText().toString().equals(urls.get(0))){
                                if (edwebsite.getText().toString().equals("test")){
                                    edwebsite.setText("Done");
                                    edwebsite.setEnabled(false);
                                    bserial.setEnabled(false);
                                }else{
                                    edwebsite.setText("Wrong Serial!!");
                                }
                            }
                        });
                    }
                }).start();
            }
        });*/
        bsimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cekisianregis() && !cekisianserial()) {

                } else {
                    if(!edserial.isEnabled()){// release jadi edserial
                        if(edpassword.getText().toString().equals(edrepassword.getText().toString())) {
                            SQLiteDatabase db = dbo.getWritableDatabase();
                            db.beginTransaction();
                            try {
                                String currenttime = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS").format(new Date());
                                String nama_usaha = ednama_usaha.getText().toString();
                                String alamat = edalamat.getText().toString();
                                String nohp = ednohp.getText().toString();
                                String email = edemail.getText().toString();
                                String website = edserial.getText().toString();
                                String username = edusername.getText().toString();
                                String password = edpassword.getText().toString();
                                db.execSQL("UPDATE perusahaan SET nama_usaha='" + nama_usaha + "', alamat_usaha='" + alamat + "'," +
                                        "nohp_usaha='" + nohp + "',email_usaha='" + email + "',website='" + website + "',date_created='"+currenttime+"' WHERE id=1");
                                db.execSQL("INSERT INTO pengguna(kode_user,email,username,password,read_persediaan,write_persediaan, " +
                                        "read_pembelian,write_pembelian,read_penjualan,write_penjualan,read_laporan,read_user)" +
                                        "VALUES('1001','"+email+"','"+username+"','"+password+"',1,1,1,1,1,1,1,1)");
                                db.setTransactionSuccessful();
                                AlertDialog.Builder adb = new AlertDialog.Builder(RegisterActivity.this);
                                adb.setTitle(getResources().getString(R.string.information));
                                adb.setMessage(getResources().getString(R.string.registrationAlert));
                                adb.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                                adb.show();
                            } catch (Exception ex) {
                                deleteDatabase("kasirku.db");
                                AlertDialog.Builder adb=new AlertDialog.Builder(RegisterActivity.this);
                                adb.setTitle(getResources().getString(R.string.information));
                                adb.setMessage(ex.getMessage());
                                adb.show();
                            } finally {
                                db.endTransaction();
                                db.close();
                            }
                        }else{
                            Toast.makeText(RegisterActivity.this, getResources().getString(R.string.password_incorrect), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(RegisterActivity.this, "Silakan Verifikasi Serial", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }


}
