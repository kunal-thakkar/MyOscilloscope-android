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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends Activity {

    private static final String TAG = "MyScope";

    public static final int REQ_OUT = UsbConstants.USB_TYPE_CLASS + UsbConstants.USB_DIR_OUT;
    public static final int REQ_IN = UsbConstants.USB_TYPE_CLASS + UsbConstants.USB_DIR_IN;

    private CalibrationTask mCalibTask;
    private static final int DIGISPARK_VID = 0x16C0;
    private static final int DIGISPARK_PID = 0x05DF;
    private static final int REQ_USB_PERMISSION = 1;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getData() != null && "usb".equals(intent.getData().getScheme())) {
            UsbDevice device = intent.getParcelableExtra("device");
            if (device != null) {
                onDeviceSelected(device);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        Log.i(TAG, "releaseUsb()");
        if(mCalibTask != null && !mCalibTask.isCancelled()) mCalibTask.cancel(true);
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
                if (device.getProductId() == DIGISPARK_PID && device.getVendorId() == DIGISPARK_VID) {
                    Intent in = new Intent(MainActivity.this, MainActivity.class);
                    Uri u = new Uri.Builder().scheme("usb").path(device.getDeviceName()).build();
                    in.setData(u);
                    in.putExtra("device", device);  // Parcel
                    PendingIntent pi = PendingIntent.getActivity(MainActivity.this, REQ_USB_PERMISSION, in, PendingIntent.FLAG_UPDATE_CURRENT);
                    manager.requestPermission(device, pi);
                    break;
                }
            }
        }
        else {
            onDeviceSelected(device);
        }
    }

    //https://digistump.com/board/index.php?topic=1675.0
    class CalibrationTask extends AsyncTask<UsbDevice, Integer, Void>{
        @Override
        protected Void doInBackground(UsbDevice... params) {
            UsbDevice device = params[0];
            UsbManager manager = (UsbManager) MainActivity.this.getSystemService(Context.USB_SERVICE);
            UsbDeviceConnection cnx = manager.openDevice(device);
            // handle openDevice failure as needed for your application
            byte buffer[] = new byte[2];
            while (!isCancelled()) {
                //int requesttype, int request, int value, int index, char *bytes, int size, int timeout
                cnx.controlTransfer(REQ_IN, 0x01, 0x00, 0x00, buffer, 2, 1000);
                publishProgress((buffer[1] & 0xFF) << 8 | (buffer[0] & 0xFF));
                cnx.controlTransfer(REQ_OUT, 0x09, 0x00, 0x01, null, 0, 1000);
            }
            cnx.close();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.i(TAG, "Value : "+ values[0]);
        }
    }

    private void onDeviceSelected(UsbDevice device) {
        Log.i(TAG, "device found");
        Log.i(TAG, "Name: " + device.getDeviceName());
        Log.i(TAG, "ID: " + device.getDeviceId());
        Log.i(TAG, "Protocol: " + device.getDeviceProtocol());
        Log.i(TAG, "Class: " + device.getDeviceClass());
        Log.i(TAG, "Subclass: " + device.getDeviceSubclass());
        Log.i(TAG, "Product ID: " + device.getProductId());
        Log.i(TAG, "Vendor ID: " + device.getVendorId());
        Log.i(TAG, "Interface count: " + device.getInterfaceCount());
        mCalibTask = new CalibrationTask();
        mCalibTask.execute(device);
    }
}
