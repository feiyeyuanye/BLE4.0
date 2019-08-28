package com.example.blue;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

/**
 * Created by xwxwaa on 2019/8/23.
 */

public class BlueCount {

    BluetoothGattCharacteristic characteristic;
    ConversionUtils conversionUtils;
    private double m;

    public double getM() {
        return m;
    }

    public BlueCount() {
        init();
        this.characteristic = characteristic;
    }

    private final void init() {
        conversionUtils = new ConversionUtils();
    }


    public final void show(String str) {
        String s1 = str.substring(0, 2);
//        Log.e("第一字节十六进制数值--->>", s1);
        int s11 = conversionUtils.LiuZhuanShi(s1);
//        Log.e("第一字节十进制数值--->>", s11 + "");
        String s2 = str.substring(2, 4);
//        Log.e("第二字节十六进制数值--->>", s2);
        int s22 = conversionUtils.LiuZhuanShi(s2);
//        Log.e("第二字节十进制数值--->>", s22 + "");


        m = (double) (s11 + s22 * 256) / (double) 10;

        Log.e("测量结果--->>", m + "");
    }

}
