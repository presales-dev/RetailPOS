package com.dfit.dfpos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;

import java.util.Calendar;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;

public class CetakStruk extends AppCompatActivity implements ReceiveListener {
    Dblocalhelper dbo;
    private Context mContext = null;
    public static Printer mPrinter = null;
    SharedPreferences sp;


    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                //Toast.makeText(getApplicationContext(),"9"+e.getMessage(),Toast.LENGTH_LONG).show();
                dispPrinterWarnings(status);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        disconnectPrinter();
                    }
                }).start();
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cetak_struk);
        Button btPrintStruk = findViewById(R.id.btPrintStruk);
        Button btBack = findViewById(R.id.btBack);

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btPrintStruk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printReceipt();
            }
        });
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

        //EditText tes = (EditText)findViewById(R.id.cobaStruk);
        //tes.setText(hasilStruk);
        try {
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addTextSize(3,3);
            mPrinter.addTextSmooth(Printer.TRUE);
            mPrinter.addText(sp.getString("nama_usaha", "none")+"\n");
            mPrinter.addFeedLine(1);
            mPrinter.addTextSize(1,1);
            mPrinter.addText(sp.getString("alamat_usaha", "none")+"\n\n");
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            mPrinter.addText("Transaction No. " + transactionNo+"\n");
            mPrinter.addText("------------------------------------------------\n");
            mPrinter.addText("QTY\tItem\t\t\t\tAmount\n");
            mPrinter.addText("------------------------------------------------\n");
            mPrinter.addText(hasilStruk);
            mPrinter.addText("------------------------------------------------\n");
            mPrinter.addText("TOTAL\t\t\t\t"+ getResources().getString(R.string.currency)+""+totalBelanja.replace(",", "")+"\n");
            mPrinter.addText("CASH\t\t\t\t\t"+tunai+"\n");
            mPrinter.addText("CHANGE\t\t\t\t\t"+kembalian.replace(",", "")+"\n");
            mPrinter.addText("------------------------------------------------\n");
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addText("Thank You!\n");
            mPrinter.addText(currentTime.toString()+"\n");
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, method, mContext);
            //Toast.makeText(getApplicationContext(),"1"+e.getMessage(),Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
    private boolean printReceipt() {
        if (!initializeObject()) {
            return false;
        }
        if (!createReceiptData()) {
            finalizeObject();
            return false;
        }
        if (!printData()) {
            finalizeObject();
            return false;
        }
        return true;
    }

    private void dispPrinterWarnings(PrinterStatusInfo status) {
        String warningsMsg = "";
        if (status == null) {
            return;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += getString(R.string.handlingmsg_warn_battery_near_end);
        }
        //Toast.makeText(getApplicationContext(),"5"+warningsMsg,Toast.LENGTH_LONG).show();
    }
    private void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }
        try {
            mPrinter.endTransaction();
        }
        catch (final Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }

        try {
            mPrinter.disconnect();
        }
        catch (final Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }
        finalizeObject();
    }
    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }
        mPrinter.clearCommandBuffer();
        mPrinter.setReceiveEventListener(null);
        mPrinter = null;
    }
    private boolean initializeObject() {
        try {
            mPrinter = new Printer(Printer.TM_M30,Printer.MODEL_ANK,mContext);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "Printer", mContext);
            //Toast.makeText(getApplicationContext(),"2"+e.getMessage(),Toast.LENGTH_LONG).show();
            return false;
        }
        mPrinter.setReceiveEventListener(this);
        return true;
    }
    private boolean connectPrinter() {
        boolean isBeginTransaction = false;
        if (mPrinter == null) {
            return false;
        }
        try {
            mPrinter.connect(sp.getString("default_printer", "none"), Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            //Toast.makeText(getApplicationContext(),"6"+e.getMessage(),Toast.LENGTH_LONG).show();
            //ShowMsg.showException(e, "connect", mContext);
            return false;
        }
        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        }
        catch (Exception e) {
            //Toast.makeText(getApplicationContext(),"7"+e.getMessage(),Toast.LENGTH_LONG).show();
        }
        if (isBeginTransaction == false) {
            try {
                mPrinter.disconnect();
            }
            catch (Epos2Exception e) {
                // Do nothing
                return false;
            }
        }
        return true;
    }
    private boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) {
            return false;
        }
        if (status.getConnection() == Printer.FALSE) {
            return false;
        }
        else if (status.getOnline() == Printer.FALSE) {
            return false;
        }
        else {
            ;//print available
        }
        return true;
    }
    private boolean printData() {
        if (mPrinter == null) {
            return false;
        }
        if (!connectPrinter()) {
            return false;
        }
        PrinterStatusInfo status = mPrinter.getStatus();
        dispPrinterWarnings(status);
        if (!isPrintable(status)) {
            //Toast.makeText(getApplicationContext(),"3"+makeErrorMessage(status),Toast.LENGTH_LONG).show();
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }
        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            //Toast.makeText(getApplicationContext(),"4"+e.getMessage(),Toast.LENGTH_LONG).show();
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

}