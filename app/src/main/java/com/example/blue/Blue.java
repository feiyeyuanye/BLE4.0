package com.example.blue;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Created by xwxwaa on 2019/8/23.
 */

public class Blue {
    Context mContext;
    BluetoothGatt bluetoothGatt;
    BluetoothDevice device;
    BluetoothGattCharacteristic controlCharacteristicl, notifyCharacteristic;
    // 数据处理
    BlueCount blueCount;
    String uuidFa;
    String uuidShou;
    String s;
    boolean isConnecting = false;
    // 工具类
    ConversionUtils conversionUtils;

    public Blue(final Context mContext, BluetoothGatt bluetoothGatt, final BluetoothGattCharacteristic controlCharacteristicl, BluetoothGattCharacteristic notifyCharacteristic) {
        this.mContext = mContext;
        this.bluetoothGatt = bluetoothGatt;
        this.controlCharacteristicl = controlCharacteristicl;
        this.notifyCharacteristic = notifyCharacteristic;
    }


    // 回调监听
    private static OnErWenListener onErWenListener;

    public interface OnErWenListener {
        void onErWenListener(int position);
    }

    public void setOnErWenListener(OnErWenListener onErWenListener) {
        this.onErWenListener = onErWenListener;
    }

    public void onDes() {
        if (isConnecting) {  //如果未连接。
            bluetoothGatt.disconnect();
            LogUtils.e("耳温仪已断开蓝牙连接");
        }
    }

    public void init(final BluetoothDevice device) {
//        y = 1;
        onErWenListener.onErWenListener(1);
        this.device = device;
        conversionUtils = new ConversionUtils();
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (device == null) {
                    return;
                }
                bluetoothGatt = device.connectGatt(mContext, false, gattCallback);
                if (bluetoothGatt == null) {
                    return;
                }
                uuidShou = "";
                uuidFa = "";
            }
        }.start();
    }

    private Handler handler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isConnecting) {
                LogUtils.e("-----》发送指令；");
                byte[] byte2 = conversionUtils.hexStringToBytes("");//APP给蓝牙设备发送指令
                controlCharacteristicl.setValue(byte2);
                bluetoothGatt.writeCharacteristic(controlCharacteristicl);
//                handler1.sendEmptyMessageDelayed(1, 4000);
            }
        }
    };


    //连接4.0蓝牙的回调接口
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        /**
         * 连接状态改变的回调
         *
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, final int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            //更新UI必须用在主线程
            switch (newState) {
                case BluetoothGatt.STATE_CONNECTED:
                    LogUtils.e("连接状态：已连接");
                    isConnecting = true;
                    onErWenListener.onErWenListener(2);
                    //当连接成果以后，开启这个服务，就可以通信了
                    bluetoothGatt.discoverServices();
                    break;
                case BluetoothGatt.STATE_CONNECTING:
                    LogUtils.e("连接状态：正在连接");
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    LogUtils.e("连接状态：断开连接");
                    bluetoothGatt.close();
                    onErWenListener.onErWenListener(3);
                    isConnecting = false;
                    break;
                case BluetoothGatt.STATE_DISCONNECTING:
                    LogUtils.e("连接状态：正在断开连接");
                    onErWenListener.onErWenListener(10);  //无效，不会走这个方法
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = bluetoothGatt.getServices();
                for (BluetoothGattService bluetoothGattService : services) {
//                    Log.e("====================》", " server:" + bluetoothGattService.getUuid().toString());
                    List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
                    for (BluetoothGattCharacteristic bluetoothGattCharacteristic : characteristics) {
//                        Log.e("====================》", " charac:" + bluetoothGattCharacteristic.getUuid().toString());
//                        int permission = bluetoothGattCharacteristic.getPermissions();
//                        LogUtils.e("---->char permission:" + Utils.getCharPermission(permission));
//                        int property = bluetoothGattCharacteristic.getProperties();
//                        LogUtils.e("---->char property:" + Utils.getCharPropertie(property));

                        // 这个UUID要根据蓝牙设备来定    APP---》蓝牙设备
//                        LogUtils.e(uuidFa + "----1");
                        if (bluetoothGattCharacteristic.getUuid().toString().equals(uuidFa)) {
//                            LogUtils.e(uuidFa + "----2");
                            LogUtils.e("准备发送指令");
                            controlCharacteristicl = bluetoothGattCharacteristic;
                            handler1.sendEmptyMessageDelayed(1, 1000);
//                            handler.sendEmptyMessage(3);
                            //  蓝牙设备---》APP
//                            LogUtils.e(uuidShou + "----1");
                        } else if (bluetoothGattCharacteristic.getUuid().toString().equals(uuidShou)) {
//                            LogUtils.e(uuidShou + "----2");
                            LogUtils.e("准备接收数据");
                            bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
                            notifyCharacteristic = bluetoothGattCharacteristic;
                            //enableNotification(true, notifyCharacteristic);必须执行，不执行的话，APP无法从蓝牙设备获得数据
                            enableNotification(true, notifyCharacteristic);
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS)
                LogUtils.e("onCharRead " + gatt.getDevice().getName()
                        + " read "
                        + characteristic.getUuid().toString()
                        + " -> "
                        + Utils.bytesToHexString(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            ConversionUtils conversionUtils = new ConversionUtils();
            LogUtils.e("onCharWrite " + gatt.getDevice().getName()
                    + " write "
                    + characteristic.getUuid().toString()
                    + " -> "
                    + conversionUtils.Bytes2HexString(characteristic.getValue()));
        }

        /**
         * 这个方法用于APP接收蓝牙设备发送的数据
         *
         * @param gatt
         * @param characteristic
         */

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
//            LogUtils.e("APP准备接收数据");
            if (characteristic.getUuid().toString().equals(notifyCharacteristic.getUuid().toString())) {
//                if (doi) {
//                    byte[] value = characteristic.getValue();
//                    String str = conversionUtils.Bytes2HexString(value);
//                    String str1 = str.substring(10, 12);
//                    if (str1.equals("65")) {
//                        doi = false;
//                        erWen(characteristic);
//                    }
//                }
                erWen(characteristic);

            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public String toString() {
            return super.toString();
        }
    };

    private boolean enableNotification(boolean enable, BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt == null || characteristic == null)
            return false;
        if (!bluetoothGatt.setCharacteristicNotification(characteristic, enable))
            return false;
        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if (clientConfig == null)
            return false;
        if (enable) {
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        LogUtils.e(bluetoothGatt.writeDescriptor(clientConfig) + "---------->>>>>>>>>bluetoothGatt.writeDescriptor(clientConfig)");
        return bluetoothGatt.writeDescriptor(clientConfig);
    }

    double wen;

    public double wen() {
        return wen;
    }

    // 数据处理
    public void erWen(BluetoothGattCharacteristic characteristic) {
        blueCount = new BlueCount();
        byte[] value = characteristic.getValue();
        String str = conversionUtils.Bytes2HexString(value);
        Log.e("接收到的16进制字符串--->>", str);
        if (str.equals("")){
            if (isConnecting) {  //如果未连接。
                bluetoothGatt.disconnect();
                LogUtils.e("耳温仪已断开蓝牙连接");
                return;
            }
        }
        String str1 = str.substring(10, 12);
        Log.e("截取后的字符串--->>", str1);
        if (str1.equals("")) {
            String str2 = str.substring(12, 16);
            Log.e("拿到的数据--->>", str2);
            blueCount.show(str2);
        }else {
//            handler1.sendEmptyMessageDelayed(1, 4000);
//            return;
        }

        final double n = blueCount.getM();
//        LogUtils.e("开始下一次的延迟发送");
//        handler.sendEmptyMessageDelayed(1, 500);

        if (n != 0.0) {

        }
    }


}

