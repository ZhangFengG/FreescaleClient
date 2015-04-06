package com.freescale.zhangfeng.bluetooth;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.util.UUID;

/**
 * @author zhangfeng
 *	服务端socket
 */
public class AcceptThread extends Thread{
    private final BluetoothServerSocket mmServerSocket;
    //private final static String NAME = "MYBLUE";
    //private final static String STR_UUID = "F1768F16-94A9-5AEE-9FA0-3C5A47FD82B0";
    private Handler mHandler;
    private ConnectedSocket manageConnectedSocket;
    public AcceptThread(Handler mHandler,BluetoothAdapter myAdapter,String NAME,String MY_UUID){
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = myAdapter.listenUsingRfcommWithServiceRecord(NAME, UUID.fromString(MY_UUID));
        } catch (IOException e) { }
        mmServerSocket = tmp;
        this.mHandler = mHandler;
    }
    public void run(){
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                manageConnectedSocket = new ConnectedSocket(mHandler,socket);
                // Do work to manage the connection (in a separate thread)
                manageConnectedSocket.start();
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        if(manageConnectedSocket!=null){
            manageConnectedSocket.write(bytes);
        }
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }
}
