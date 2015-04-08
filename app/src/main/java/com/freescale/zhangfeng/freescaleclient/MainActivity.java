package com.freescale.zhangfeng.freescaleclient;

//
//                       _oo0oo_
//                      o8888888o
//                      88" . "88
//                     (|-  _  -|)
//                      0\  =  /0
//                    ___/`---'\___
//                  .' \\|     |// '.
//                 / \\|||  :  |||// \
//                / _||||| -:- |||||- \
//               |   | \\\  -  /// |   |
//               | \_|  ''\---/''  |_/ |
//               \  .-\__  '-'  ___/-. /
//             ___'. .'  /--.--\  `. .'___
//          ."" '<  `.___\_<|>_/___.' >' "".
//         | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//         \  \ `_.   \_ __\ /__ _/   .-` /  /
//     =====`-.____`.___ \_____/___.-`___.-'=====
//                       `=---='
//
//
//     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
//               佛祖保佑         永无BUG
//

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.freescale.zhangfeng.bluetooth.BluetoothUtil;

import java.text.DateFormat;
import java.text.DecimalFormat;


public class MainActivity extends Activity {
    private static final String CAR_ADR = "00:14:03:05:08:A4";
    private static final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    static boolean gravity_flag = true;
    private BluetoothUtil bluetoothUtil;
    private View main;
    private TextView tvSpeed;
    private Window window;
    private SeekBar bar1;
    private SeekBar bar2;
    private final static Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BluetoothUtil.BLUETOOTH_RESPONSE:

                    //tempNum.setText(null);
                    String str = (String) msg.obj;
                    if(str.equals("$")){
                        return ;
                    }
                    //tempNum.setText(str);
//                    Log.i("BLUE", str);
                    break;
                case BluetoothUtil.BLUETOOTH_ERROR:
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        /*设置全屏+无标题导航+常量*/

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        main = getLayoutInflater().inflate(R.layout.activity_main,null);//获取view对象
        main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        main.setKeepScreenOn(true);
        setContentView(main);
        tvSpeed =(TextView)findViewById(R.id.textView2);
        bar1 = (SeekBar)findViewById(R.id.seekBar1);
        bar2 = (SeekBar)findViewById(R.id.seekBar2);
        /*拖动条拖动触发事件*/
        bar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {     //速度控制
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int p = progress;
                tvSpeed.setText(Integer.toString(p));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                //TODO 发送数据到蓝牙
            }
        });
        bar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {     //舵机控制
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i("Gy", "Gy:"+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                //TODO 发送数据到蓝牙
            }
        });
        bluetoothUtil = new BluetoothUtil(this,mHandler,CAR_ADR,MY_UUID);
        initSensor();
        //setContentView(main);
    }
    private void initSensor(){
        //获取传感器管理
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //得到传感器
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(sensor!=null){
            //注册监听器
            MyListener listener = new MyListener();
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);
        }else{
            Log.i("Gy", "Gy:NULL");
        }
//    	sensorManager.un

    }
    //设置监听器内部类
    private class MyListener implements SensorEventListener {
        int speed_num=0;
        @Override
        public void onSensorChanged(SensorEvent event) {

            // 传感器数据变化时
            float[] data = event.values;
           // Log.i("Gx", "Gx:"+data[0]);
           //Log.i("Gy", "Gy:"+data[1]);
           // Log.i("Gz", "Gz:"+data[2]);
//            etx = (EditText) MainActivity.this.findViewById(R.id.editTextx);
//            ety = (EditText) MainActivity.this.findViewById(R.id.editTexty);
//            etz = (EditText) MainActivity.this.findViewById(R.id.editTextz);
            data[0]-=4.5;
            data[0] = -data[0];
            DecimalFormat df = new DecimalFormat("0.00");
            if(gravity_flag){
                /*舵机算法处理*/
                if((data[1]>4.5)){//fff
                    data[1] = (float)4.5;
                }else if(data[1]<-4.5){
                    data[1] = (float)(-4.5);
                }
                int y_data = (int)(data[1]*100/10);
                y_data = (y_data+46)*128/92;
                //Log.i("Gy", "Gy:"+y_data);
                bluetoothUtil.write(new byte[]{(byte)~BluetoothUtil.getIntegerToByte(y_data)});
                gravity_flag = false;
            }else{
                /*电机算法处理*/
                if(data[0]>=0){ //后退处理
                    if(data[0]>4.5){
                        data[0]=(float)4.5;
                    }
                    int x_data = (int)(data[0]*100/10);
                    speed_num = x_data;
                    x_data = x_data*80/45;
                   // Log.i("Gy", "Gy:"+x_data);
                    bluetoothUtil.write(new byte[]{BluetoothUtil.getIntegerToByte(x_data)});
                }else{  //前进处理
                    if(data[0]<-4.5){
                        data[0]=(float)-4.5;
                    }
                    int x_data = (int)(data[0]*100/10);
                    speed_num = x_data;
                    x_data = 80-x_data;
                   // Log.i("Gy", "----Gy:"+x_data);
                    bluetoothUtil.write(new byte[]{BluetoothUtil.getIntegerToByte(x_data)});
                }
                DecimalFormat decimalFormat = new DecimalFormat("00.0");
                tvSpeed.setText(Integer.toString(speed_num));
                gravity_flag = true;
            }


//            etx.setText(df.format(data[0]));
//            ety.setText(df.format(data[1]));
//            etz.setText(df.format(data[2]));

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // 传感器精度变化时

        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        bluetoothUtil.cancel();
    }

}
