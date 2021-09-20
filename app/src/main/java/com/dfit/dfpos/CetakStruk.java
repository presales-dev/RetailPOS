package com.dfit.dfpos;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.Log;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CetakStruk extends AppCompatActivity implements ReceiveListener {
    Dblocalhelper dbo;
    private Context mContext = null;
    public static Printer mPrinter = null;
    SharedPreferences sp;
    private static final int REQUEST_PERMISSION = 100;
    private static final int DISCONNECT_INTERVAL = 500;//millseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cetak_struk);
        Button btPrintStruk = findViewById(R.id.btPrintStruk);
        Button btBack = findViewById(R.id.btBack);
        dbo = new Dblocalhelper(this);
        Intent in = getIntent();
        Bundle b = in.getExtras();
        String hasilStruk = (String) b.get("struk");
        requestRuntimePermission();

        enableLocationSetting();

        mContext = this;
        initializeObject();
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                //Toast.makeText(getApplicationContext(),hasilStruk,Toast.LENGTH_LONG).show();

            }
        });
        btPrintStruk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateButtonState(false);
                if (!runPrintReceiptSequence()) {
                    updateButtonState(true);
                }
            }
        });

        try {
            Log.setLogSettings(mContext, Log.PERIOD_TEMPORARY, Log.OUTPUT_STORAGE, null, 0, 1, Log.LOGLEVEL_LOW);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "setLogSettings", mContext);
        }
    }
    @Override
    protected void onDestroy() {

        finalizeObject();

        super.onDestroy();
    }

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                ShowMsg.showResult(code, makeErrorMessage(status), mContext);

                dispPrinterWarnings(status);

                updateButtonState(true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        disconnectPrinter();
                    }
                }).start();
            }
        });
    }

    private void updateButtonState(boolean state) {
        Button btPrintStruk = findViewById(R.id.btPrintStruk);
        Button btBack = findViewById(R.id.btBack);

        btPrintStruk.setEnabled(state);
        btBack.setEnabled(state);
    }
    private boolean createReceiptData() {
        String method = "";
        if (mPrinter == null) {
            return false;
        }
        sp = getApplicationContext().getSharedPreferences("config", 0);
        Intent in = getIntent();
        Bundle b = in.getExtras();
        String hasilStruk = (String) b.get("struk");
        String totalBelanja = (String) b.get("totalbelanja");
        String kembalian = (String) b.get("kembalian");
        String tunai = (String) b.get("tunai");
        String transactionNo = (String)b.get("receiptNo");
        Date currentTime = Calendar.getInstance().getTime();

        SQLiteDatabase db = dbo.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT nama_usaha,alamat_usaha,nohp_usaha,email_usaha,website FROM perusahaan WHERE id=1", null);
        c.moveToFirst();
        //EditText tes = (EditText)findViewById(R.id.cobaStruk);
        //tes.setText(hasilStruk);
        try {
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addTextSize(3,3);
            mPrinter.addTextSmooth(Printer.TRUE);
            mPrinter.addText(c.getString(0)+"\n");
            mPrinter.addFeedLine(1);
            mPrinter.addTextSize(1,1);
            mPrinter.addText(c.getString(1)+"\n");
            mPrinter.addText(c.getString(2)+"\n\n");
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addText("Transaction No. " + transactionNo+"\n");
            //mPrinter.addText("-------------------------------\n");
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            mPrinter.addText("Item\t\t\t\tAmount\n");
            mPrinter.addText("------------------------------------------\n");
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            //mPrinter.addText("-------------------------------\n");
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            mPrinter.addText(hasilStruk);
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            //mPrinter.addText("-------------------------------\n");
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            mPrinter.addText("------------------------------------------\n");
            mPrinter.addTextStyle(Printer.FALSE,Printer.FALSE,Printer.TRUE,Printer.PARAM_DEFAULT);
            mPrinter.addText("TOTAL\t\t\t\t"+totalBelanja.replace(",", "")+"\n");
            mPrinter.addText("CASH\t\t\t\t"+tunai+"\n");
            mPrinter.addText("CHANGE\t\t\t\t"+kembalian.replace(",", "")+"\n");
            mPrinter.addTextStyle(Printer.FALSE,Printer.FALSE,Printer.FALSE,Printer.PARAM_DEFAULT);
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            //mPrinter.addText("-------------------------------\n");
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addText("Thank You!\n");
            mPrinter.addText(currentTime.toString()+"\n");
            mPrinter.addCut(Printer.CUT_FEED);
            c.close();
            db.close();
        }
        catch (Exception e) {
            //ShowMsg.showException(e, method, mContext);
            //Toast.makeText(getApplicationContext(),"1"+e.getMessage(),Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
    private boolean runPrintReceiptSequence() {

        if (!createReceiptData()) {
            return false;
        }

        if (!printData()) {
            return false;
        }

        return true;
    }

    private void dispPrinterWarnings(PrinterStatusInfo status) {
        //EditText edtWarnings = (EditText)findViewById(R.id.edtWarnings);
        String warningsMsg = "";

        if (status == null) {
            return;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += getString(R.string.handlingmsg_warn_receipt_near_end);
            Toast.makeText(CetakStruk.this, warningsMsg, Toast.LENGTH_LONG).show();
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += getString(R.string.handlingmsg_warn_battery_near_end);
            Toast.makeText(CetakStruk.this, warningsMsg, Toast.LENGTH_LONG).show();
        }
    }
    private void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        while (true) {
            try {
                mPrinter.disconnect();
                break;
            } catch (final Exception e) {
                if (e instanceof Epos2Exception) {
                    //Note: If printer is processing such as printing and so on, the disconnect API returns ERR_PROCESSING.
                    if (((Epos2Exception) e).getErrorStatus() == Epos2Exception.ERR_PROCESSING) {
                        try {
                            Thread.sleep(DISCONNECT_INTERVAL);
                        } catch (Exception ex) {
                        }
                    }else{
                        runOnUiThread(new Runnable() {
                            public synchronized void run() {
                                //Toast.makeText(getApplicationContext(),e.toString() +" 6",Toast.LENGTH_LONG).show();
                                ShowMsg.showException(e, "disconnect", mContext);
                            }
                        });
                        break;
                    }
                }else{
                    runOnUiThread(new Runnable() {
                        public synchronized void run() {
                            ShowMsg.showException(e, "disconnect", mContext);
                            //Toast.makeText(getApplicationContext(),e.toString() +" 7",Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
                }
            }
        }

        mPrinter.clearCommandBuffer();
    }
    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.setReceiveEventListener(null);

        mPrinter = null;
    }

    private boolean initializeObject() {
        try {
            SQLiteDatabase db = dbo.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT host_sync FROM pengaturan WHERE id=1", null);
            c.moveToFirst();
            mPrinter = new Printer(Integer.parseInt(c.getString(0)),Printer.MODEL_ANK,mContext);
            //Toast.makeText(getApplicationContext(),Integer.parseInt(c.getString(0)) +" 4",Toast.LENGTH_LONG).show();
            c.close();
            db.close();
        }
        catch (Exception e) {
            //Toast.makeText(getApplicationContext(),e.toString() +" 4",Toast.LENGTH_LONG).show();
            ShowMsg.showException(e, "Printer", mContext);
            return false;
        }
        mPrinter.setReceiveEventListener(this);
        return true;
    }

    private boolean connectPrinter() {
        if (mPrinter == null) {
            return false;
        }
        try {
            SQLiteDatabase db = dbo.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT default_printer FROM pengaturan WHERE id=1", null);
            c.moveToFirst();
            mPrinter.connect(c.getString(0), Printer.PARAM_DEFAULT);
            c.close();
            db.close();
        }catch (Exception e) {
            String msg = "";
            msg = String.format(
                    "%s\n\t%s\n%s\n\t%s",
                    "error code",
                    getEposExceptionText(((Epos2Exception) e).getErrorStatus()),
                    "method",
                    "connect");
            //Toast.makeText(getApplicationContext()," 5 \n"+msg,Toast.LENGTH_LONG).show();
            ShowMsg.showException(e, "connect", mContext);
            return false;
        }

        return true;
    }

    private boolean printData() {
        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter()) {
            mPrinter.clearCommandBuffer();
            return false;
        }

        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            mPrinter.clearCommandBuffer();
            //Toast.makeText(getApplicationContext(),e.toString() +" 3",Toast.LENGTH_LONG).show();
            ShowMsg.showException(e, "sendData", mContext);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        return true;
    }

    private String makeErrorMessage(PrinterStatusInfo status) {
        String msg = "";

        if (status.getOnline() == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_offline);
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_no_response);
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += getString(R.string.handlingmsg_err_cover_open);
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += getString(R.string.handlingmsg_err_receipt_end);
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += getString(R.string.handlingmsg_err_paper_feed);
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += getString(R.string.handlingmsg_err_autocutter);
            msg += getString(R.string.handlingmsg_err_need_recover);
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += getString(R.string.handlingmsg_err_unrecover);
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_head);
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_motor);
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_battery);
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += getString(R.string.handlingmsg_err_wrong_paper);
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += getString(R.string.handlingmsg_err_battery_real_end);
        }

        return msg;
    }
    private void requestRuntimePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //int permissionLocationCoarse= ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionLocationFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> requestPermissions = new ArrayList<>();

        if (permissionStorage == PackageManager.PERMISSION_DENIED) {
            requestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionLocationFine == PackageManager.PERMISSION_DENIED) {
            requestPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
//        if (permissionLocationCoarse == PackageManager.PERMISSION_DENIED) {
//            requestPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
//        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION || grantResults.length == 0) {
            return;
        }

        List<String> requestPermissions = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }
            if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }

            // If your app targets Android 9 or lower, you can declare ACCESS_COARSE_LOCATION instead.
//            if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)
//                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
//                requestPermissions.add(permissions[i]);
//            }
        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSION);
        }
    }

    //When searching for a device running on Android 10 or later as a Bluetooth-capable device, enable access to location information of the device.
    private void enableLocationSetting() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);;

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(CetakStruk.this,
                                CommonStatusCodes.RESOLUTION_REQUIRED);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }
    private static String getEposExceptionText(int state) {
        String return_text = "";
        switch (state) {
            case    Epos2Exception.ERR_PARAM:
                return_text = "ERR_PARAM";
                break;
            case    Epos2Exception.ERR_CONNECT:
                return_text = "ERR_CONNECT";
                break;
            case    Epos2Exception.ERR_TIMEOUT:
                return_text = "ERR_TIMEOUT";
                break;
            case    Epos2Exception.ERR_MEMORY:
                return_text = "ERR_MEMORY";
                break;
            case    Epos2Exception.ERR_ILLEGAL:
                return_text = "ERR_ILLEGAL";
                break;
            case    Epos2Exception.ERR_PROCESSING:
                return_text = "ERR_PROCESSING";
                break;
            case    Epos2Exception.ERR_NOT_FOUND:
                return_text = "ERR_NOT_FOUND";
                break;
            case    Epos2Exception.ERR_IN_USE:
                return_text = "ERR_IN_USE";
                break;
            case    Epos2Exception.ERR_TYPE_INVALID:
                return_text = "ERR_TYPE_INVALID";
                break;
            case    Epos2Exception.ERR_DISCONNECT:
                return_text = "ERR_DISCONNECT";
                break;
            case    Epos2Exception.ERR_ALREADY_OPENED:
                return_text = "ERR_ALREADY_OPENED";
                break;
            case    Epos2Exception.ERR_ALREADY_USED:
                return_text = "ERR_ALREADY_USED";
                break;
            case    Epos2Exception.ERR_BOX_COUNT_OVER:
                return_text = "ERR_BOX_COUNT_OVER";
                break;
            case    Epos2Exception.ERR_BOX_CLIENT_OVER:
                return_text = "ERR_BOX_CLIENT_OVER";
                break;
            case    Epos2Exception.ERR_UNSUPPORTED:
                return_text = "ERR_UNSUPPORTED, Silakan ubah Printer Series sesuai dengan printer yang dihubungkan";
                break;
            case    Epos2Exception.ERR_FAILURE:
                return_text = "ERR_FAILURE";
                break;
            default:
                return_text = String.format("%d", state);
                break;
        }
        return return_text;
    }
}