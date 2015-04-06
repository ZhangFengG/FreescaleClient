package com.freescale.zhangfeng.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;


/**
 * Created by zhangfeng on 2015/3/27.
 */
public class BluetoothUtil{
    public static BluetoothDevice device;
    public static BluetoothAdapter bluetoothAdapter;
    public static ConnectThread connectThread;
    public static final int BLUETOOTH_RESPONSE = 1;
    public static final int BLUETOOTH_ERROR = 0;
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    public BluetoothUtil(Context context,Handler mHandler,String MAC,String uuid){
        device = bluetoothAdapter.getRemoteDevice(MAC);
        if(bluetoothAdapter!=null){
            if(bluetoothAdapter.isEnabled()){
                connectThread = new ConnectThread(mHandler,bluetoothAdapter,device,uuid);
                connectThread.start();
            }else{
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivity(enableBluetooth);
            }
        }
    }
    public void write(byte[] bytes){
        connectThread.write(bytes);
    }
    public void write(char[] chars){
        connectThread.write(chars);
    }

    /**
     * 取消时关闭
     */
    public void cancel(){
        connectThread.cancel();
    }
    /**
     * 获取一位数据发送到单片机
     * @param progress
     * @return
     */
    public static byte getIntegerToByte(int progress){
        byte b;
        //b = (byte) ((byte) ((progress / 16) << 4) | (byte) (progress % 16));
        b = (byte)(0x00|((progress/16)<<4));
        b = (byte)(b|(progress%16));
        return b;
    }

}