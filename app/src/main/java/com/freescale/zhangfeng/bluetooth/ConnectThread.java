package com.freescale.zhangfeng.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final BluetoothAdapter myAdapter;
    private Handler mHandler;
    private ConnectedSocket manageConnectedSocket;
    private final static String COM_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    /**
     * @param myAdapter 蓝牙适配器
     * @param device 设备
     */
    public ConnectThread(Handler mHandler,BluetoothAdapter myAdapter,BluetoothDevice device,String MY_UUID) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmSocket = tmp;
        this.myAdapter = myAdapter;
        this.mHandler = mHandler;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        myAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }
        manageConnectedSocket = new ConnectedSocket(mHandler,mmSocket);
        // Do work to manage the connection (in a separate thread)
        manageConnectedSocket.start();
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        if(manageConnectedSocket!=null){
            manageConnectedSocket.write(bytes);
        }
    }
    /* Call this from the main activity to send data to the remote device */
    public void write(char[] chars) {
        if(manageConnectedSocket!=null){
            manageConnectedSocket.write(chars);
        }
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
            if(manageConnectedSocket!=null){
                manageConnectedSocket.cancel();
            }
        } catch (IOException e) { }
    }
}
