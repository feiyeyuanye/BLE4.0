package com.example.blue;

import android.util.Log;

public class LogUtils {
    public static final boolean isDebug = true;

    public static final void e(String msg) {
        if (isDebug)
            Log.e("As", "---------->>" + msg);
    }

    public static final void e(String... msg) {
        if (isDebug) {
            for (int i = 0; i < msg.length; i++) {
                Log.e("MathBox", "------->>" + "msg");
            }
        }
    }

    public static final void e(int msg) {
        if (isDebug)
            Log.e("As", "---------->>" + msg + "");
    }

    public static final void e(boolean msg) {
        if (isDebug)
            Log.e("As", "---------->>" + msg + "");
    }

    public static final void e(Object msg) {
        if (isDebug)
            Log.e("As", "---------->>" + msg.toString());
    }
}
