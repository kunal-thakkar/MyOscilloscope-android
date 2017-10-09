package com.example.kunal.myscope;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends Activity {

    private byte[] bytes;
    private static int TIMEOUT = 0;
    private boolean forceClaim = true;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    int Vendor = 0x16c0;
    int productId = 0x05df;

    private static final String TAG = "MyScope";
    private UsbDeviceConnection usbDeviceConnection;
    private UsbInterface usbInterface;
    private boolean deviceFound = false;

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        Log.i(TAG, "releaseUsb()");
        if (usbDeviceConnection != null) {
            if (usbInterface != null) {
                usbDeviceConnection.releaseInterface(usbInterface); usbInterface = null;
            }
            usbDeviceConnection.close();
            usbDeviceConnection = null;
        }
    }

    byte[] toByteArray(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value };
    }

    int fromByteArray(byte[] bytes) {
        //return bytes[0] << 24 | (bytes[1] & 0xFF) << 16;// | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
        return bytes[0] & 0xFF;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        UsbDevice device = (UsbDevice) this.getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if(device == null) {
            while (deviceIterator.hasNext()) {
                device = deviceIterator.next();
                if (device.getProductId() == productId && device.getVendorId() == Vendor) {
                    deviceFound = true;
                    break;
                }
            }
        }
        else {
            deviceFound = true;
        }
        if(!deviceFound){
            Log.i(TAG, "device not found");
        }
        else {
            Log.i(TAG, "device found");
            Log.i(TAG, "Name: " + device.getDeviceName());
            Log.i(TAG, "ID: " + device.getDeviceId());
            Log.i(TAG, "Protocol: " + device.getDeviceProtocol());
            Log.i(TAG, "Class: " + device.getDeviceClass());
            Log.i(TAG, "Subclass: " + device.getDeviceSubclass());
            Log.i(TAG, "Product ID: " + device.getProductId());
            Log.i(TAG, "Vendor ID: " + device.getVendorId());
            Log.i(TAG, "Interface count: " + device.getInterfaceCount());

            usbInterface = device.getInterface(0);
            UsbEndpoint tOut = null;
            UsbEndpoint tIn = null;
            int tEndpointCnt = usbInterface.getEndpointCount();
            Log.i(TAG, "End Point Count : " + tEndpointCnt);
            for (int j = 0; j < tEndpointCnt; j++) {
                Log.i(TAG, j + " : " + usbInterface.getEndpoint(j).getDirection());
                if (usbInterface.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (usbInterface.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_OUT) {
                        tOut = usbInterface.getEndpoint(j);
                    } else if (usbInterface.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_IN) {
                        tIn = usbInterface.getEndpoint(j);
                    }
                }
            }
//            if(tOut != null && tIn != null){
//                Log.i(TAG, "Found OK");
                bytes = new byte[1];
                UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                usbDeviceConnection = mUsbManager.openDevice(device);

                int i = 0, t;
                while(i < 100) {
                    byte buffer[] = new byte[1];
                    //connection.bulkTransfer(tIn, buffer, 0x01, 0);
                    //requestType, request, value, index, buffer, length, timeout
                    t = usbDeviceConnection.controlTransfer(UsbConstants.USB_DIR_IN, 0x03 << 8, 0x01, 0x00, buffer, 1, 1000); //0x27 0x00
                    Log.i(TAG, "Value : " + fromByteArray(buffer) + ", " + t);
                    //connection.bulkTransfer(tOut, null, 0x01, 0);
                    t = usbDeviceConnection.controlTransfer(UsbConstants.USB_DIR_OUT, 0x09, 0x03 << 8, 0x01, null, 0, 1000);
                    Log.i(TAG, "Value : " + fromByteArray(buffer) + ", " + t);
                    i++;
                }
                //connection.bulkTransfer(endpoint, bytes, bytes.length, TIMEOUT); //do in another thread
//            }
        }

    }
}
