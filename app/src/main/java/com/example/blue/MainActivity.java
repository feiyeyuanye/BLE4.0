package com.example.blue;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {
    private List<BluetoothDevice> devicelist = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;
    private static final long SCAN_PERIOD = 10000;//用于设置蓝牙搜索时间
    private boolean flag = true;
    private List<BluetoothDevice> dList = new ArrayList<>();
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic controlCharacteristicl, notifyCharacteristic, batteryCharacteristic;
    private static final int ACCESS_LOCATION = 1;
    Blue a1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {        //第一步  判断是否开启了蓝牙。。没开启则提示打开蓝牙
            bluetoothAdapter.enable();
        }

        BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        //得到蓝牙的适配器
        bluetoothAdapter = bluetoothManager.getAdapter();
        init();
    }

    @SuppressLint("WrongConstant")
    private void getPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            int permissionCheck = 0;
            permissionCheck = this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionCheck += this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions( // 请求授权
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        ACCESS_LOCATION);// 自定义常量,任意整型
            } else {
                // 已经获得权限
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ACCESS_LOCATION:
                if (hasAllPermissionGranted(grantResults)) {
                    Log.e("TAG", "onRequestPermissionsResult: OK");
                } else {
                    Log.e("TAG", "onRequestPermissionsResult: NOT OK");
                }
                break;
        }
    }

    private boolean hasAllPermissionGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }


    private void init() {
        a1 = new Blue(this, bluetoothGatt, controlCharacteristicl, notifyCharacteristic);
        blueReader();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    scaning();
                    break;
                case 2:
                    // 仪器
                    forDelect("");
                    break;
                case 8:
                    //如果开始扫描后，5秒内没有搜索到任何设备。。则重新开始扫描
                    if (dList.size() == 0) {
                        LogUtils.e("没有扫描到设备，从新开始扫描");
                        bluetoothAdapter.stopLeScan(mLeScanCallback);
                        handler.sendEmptyMessageDelayed(1, 1000);
                    }
                    break;
            }
        }
    };

    private void forDelect(String str) {
        LogUtils.e(devicelist.toString() + "--------------------------------------仪器断开，删除之前");
        for (int i = 0; i < devicelist.size(); i++) {
            if (devicelist.get(i).getName().equals(str)) {
                devicelist.remove(i);
            }
        }
        LogUtils.e(devicelist.toString() + "--------------------------------------仪器断开，删除之后");
        handler.sendEmptyMessageDelayed(1, 3500);
        dList.clear();
    }

    public void startScan() {
        flag = true;
//        handler.sendEmptyMessage(1);
        handler.sendEmptyMessageDelayed(1, 1000);
    }
    @Override
    public void onResume() {
        super.onResume();
        startScan();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopScan();
//        if (mReceiver != null) {
//            mContext.unregisterReceiver(mReceiver);
//            mReceiver = null;
//        }
        flag = false;
        a1.onDes();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopScan();
        flag = false;
    }
    public void stopScan() {
        //如果正在扫描，，就停止扫描
//        boolean discovering = bluetoothAdapter.isDiscovering();
//        LogUtils.e("是否正在扫描当中;" + discovering);
//        if (!discovering) {
        bluetoothAdapter.stopLeScan(mLeScanCallback);
        LogUtils.e("停止扫描");
//        }
//        if (discovering) {
//            bluetoothAdapter.cancelDiscovery();
//            LogUtils.e("停止扫描");
//        }
    }

    public void scaning() {
        if (flag) {
            LogUtils.e("开始扫描");
            //最好调用这个过时的方法，因为可以支持很多API
            boolean a = bluetoothAdapter.startLeScan(mLeScanCallback);
            LogUtils.e(a + "------------------------------是否成功开启扫描");
            if (a)
                handler.sendEmptyMessageDelayed(8, 8000);
//            else {
//                bluetoothAdapter.stopLeScan(mLeScanCallback);
//                handler.sendEmptyMessageDelayed(1, 1000);
//            }


//            bluetoothAdapter.startDiscovery();     //开启扫描
//            LogUtils.e(devicelist.size()+"--size");
//            if (devicelist .size()!= 0) {
//                LogUtils.e(devicelist.toString()+"-------------蓝牙集合");
//                handler.sendEmptyMessage(2);
//            }
//            handler.sendEmptyMessageDelayed(1, 5000);
        }
    }
    public void connecting(BluetoothDevice device) {
//        连接前停止搜索
        bluetoothAdapter.stopLeScan(mLeScanCallback);
//        bluetoothAdapter.cancelDiscovery();
        if (flag) {
            LogUtils.e("正在连接");
            String name = device.getName();
            LogUtils.e(name + "----name->1");
            if (name.equals(startWith[0])) {
                a1.init(device);
            }

            LogUtils.e("走完连接方法，重新开始扫描");
            handler.sendEmptyMessageDelayed(1, 2000);
        }
    }
    // 设备名
    String[] startWith = {""};

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!(dList.contains(device))) {
                Log.e("TAG", device.getName() + "-------------- device.getName()");
                Log.e("TAG", device.getAddress() + "--------------device.getAddress()");
                dList.add(device);

                //项目的正式方法，根据唯一的设备地址判别
                if (adress != null && adress.length != 0) {
                    for (int i = 0; i < adress.length; i++) {
                        if (device.getName() != null) {
                            if (adress[i] != null) {
                                //如果已扫描的设备在本地有记录，并且集合不包含。。。
                                //则添加到集合中，并且调用连接方法
                                if (device.getAddress().equals(adress[i]) && !(devicelist.contains(device))) {
                                    devicelist.add(device);
                                    connecting(device);
//                                handler.sendEmptyMessage(2);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    };



    //这个数组是用来保存地址的，主要用来做判断
    private String[] adress;


    //文件流。。。读取内存的蓝牙地址
    public void blueReader() {

        // FileInputStream对文件进行读取
        File file = new File("/storage/emulated/0/BlueAdress.txt");
        // 打开流
        FileInputStream fis = null;
        try {

            fis = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int len = fis.read(buf); // 长度 0, 65
            String str = new String(buf, 0, len);
            StringTokenizer tokener = new StringTokenizer(str, ".");
            String[] result = new String[tokener.countTokens()];
            adress = new String[tokener.countTokens()];
            int i = 0;
            while (tokener.hasMoreTokens()) {
                result[i++] = tokener.nextToken();
            }
            for (int a = 0; a < result.length; a++) {
                String[] s1 = result[a].split(",");
                if (s1[0].equals("")) {   // 仪器
                    adress[a] = s1[1];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
