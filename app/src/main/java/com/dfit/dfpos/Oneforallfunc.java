package com.dfit.dfpos;

import android.content.Context;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

/**
 * Created by Minami on 20/09/2019.
 */

public class Oneforallfunc {

   /* public static String kode_user="";
    public static String email="";
    public static String username="";
    public static String password="";
    public static String nohp="";
    public static String nama_usaha="";
    public static String alamat_usaha="";
    public static String nohp_usaha="";
    public static String email_usaha="";
    public static String website="";
    public static String device_id="";
    public static int id=0;
    public static String def_printer="";
    public static String host_sync="";
    public static double sync_time=0.0;
    public static int issync=0;
    public static String db_path="";
    public static String image_path="";
    public static int read_persediaaan=0;
    public static int write_persediaaan=0;
    public static int read_pembelian=0;
    public static int write_pembelian=0;
    public static int read_penjualan=0;
    public static int write_penjualan=0;
    public static int read_laporan=0;
    public static int read_user=0;*/


    public static double Stringtodouble(String nilai){
        double hasil=0.0;
        try{
            hasil=Double.parseDouble(nilai);
        }catch (Exception ex){
            hasil=0.0;
        }
        return hasil;
    }

    public static int Stringtoint(String nilai){
        int hasil=0;
        try{
            hasil=Integer.parseInt(nilai);
        }catch (Exception ex){
            hasil=0;
        }
        return hasil;
    }

    public static int validint(int nilai){
        int hasil=0;
        try{
            hasil=Integer.parseInt(String.valueOf(nilai));
        }catch (Exception ex){
            hasil=0;
        }
        return hasil;
    }

    public static double validdouble(double nilai){
        double hasil=0;
        try{
            hasil=Double.parseDouble(String.valueOf(nilai));
        }catch (Exception ex){
            hasil=0;
        }
        return hasil;
    }

    public static void copyfile(File sumber, File tujuan) {
        InputStream is=null;
        OutputStream os=null;
        try {
            if(sumber.isDirectory()){

                if(!tujuan.exists()){
                    tujuan.mkdirs();
                }

                String[] child=sumber.list();
                for (int i = 0; i < child.length ; i++) {
                    is=new FileInputStream(new File(sumber,child[i]));
                    os=new FileOutputStream(new File(tujuan,child[i]));
                    byte[] buffer=new byte[1024];
                    int length;
                    while ((length=is.read(buffer))>0){
                        os.write(buffer,0,length);
                    }
                }
            }else{
                is=new FileInputStream(sumber);
                os=new FileOutputStream(tujuan);
                byte[] buffer=new byte[1024];
                int length;
                while ((length=is.read(buffer))>0){
                    os.write(buffer,0,length);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static String doubleround(double nilai){
        DecimalFormat df=new DecimalFormat("####0.00");
        String hasil=df.format(nilai).replace(",",".");
        return hasil;
    }

    public static int calculateNoOfColumns(Context ct) {
        DisplayMetrics displayMetrics =  ct.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (screenWidthDp / 180 + 0.5);
        return noOfColumns;
    }



}
