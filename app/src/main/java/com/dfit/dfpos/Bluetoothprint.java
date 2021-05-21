package com.dfit.dfpos;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Minami on 28/09/2019.
 */

public class Bluetoothprint {

    BluetoothAdapter mbadapter;
    BluetoothDevice mbdevice;
    BluetoothSocket mbsocket;
    OutputStream os;
    InputStream is;
    Thread workthread;
    byte[] readbuffer;
    int readbufferposition;
    volatile boolean stopworker;
    String value = "";
    Activity ct;
    SharedPreferences sp;

    public Bluetoothprint(Activity ct) {
        this.ct = ct;
        sp= ct.getSharedPreferences("config",0);
    }


    public void initprinter() {
        mbadapter = BluetoothAdapter.getDefaultAdapter();
        try {
            if (!mbadapter.isEnabled()) {
                Intent enableblutooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ct.startActivityForResult(enableblutooth, 0);
            }

            Set<BluetoothDevice> pairdevice = mbadapter.getBondedDevices();
            if (pairdevice.size() > 0) {
                for (BluetoothDevice mbdevicechild : pairdevice) {
                    if (mbdevicechild.getName().equals(sp.getString("default_printer","none"))) {
                        mbdevice = mbdevicechild;
                        break;
                    }
                }
            }

            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            //Method m = mbdevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            mbsocket = mbdevice.createRfcommSocketToServiceRecord(uuid);
            mbadapter.cancelDiscovery();
            mbsocket.connect();
            os = mbsocket.getOutputStream();
            is = mbsocket.getInputStream();
            beginlistendata();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void beginlistendata() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopworker = false;
            readbufferposition = 0;
            readbuffer = new byte[1024];

            workthread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopworker) {

                        try {

                            int bytesAvailable = is.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                is.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readbufferposition];
                                        System.arraycopy(
                                                readbuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readbufferposition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                Log.d("e", data);
                                            }
                                        });

                                    } else {
                                        readbuffer[readbufferposition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopworker = true;
                        }

                    }
                }
            });

            workthread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void print(String textprint) {
        byte[] buffer = textprint.getBytes();
        byte[] PrintHeader = {(byte) 0xAA, 0x55, 2, 0};
        PrintHeader[3] = (byte) buffer.length;
        initprinter();
        if (PrintHeader.length > 128) {
            value += "\nValue is more than 128 size\n";
            Toast.makeText(ct, value, Toast.LENGTH_LONG).show();
        } else {
            try {
                os.write(textprint.getBytes());
                os.close();
                mbsocket.close();
                Toast.makeText(ct, "Success", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                value += ex.toString() + "\n" + "Excep IntentPrint \n";
                Toast.makeText(ct, value, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void closesocket(){
        stopworker=true;
        try {
            os.close();
            is.close();
            mbsocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
