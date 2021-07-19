package com.example.pdfdemo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class PreferenceHelper {
    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
        return isConnected;
    }
}
