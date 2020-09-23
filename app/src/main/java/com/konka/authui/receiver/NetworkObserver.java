package com.konka.authui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.konka.authui.util.NetworkUtils;

import java.lang.ref.WeakReference;

public class NetworkObserver {

    private Context mContext;
    private NetworkListener mNetworkListener;
    private NetworkReceiver mNetworkReceiver;

    public NetworkObserver(Context context) {
        this.mContext = context;
    }

    public void setNetworkListener(NetworkListener listener) {
        this.mNetworkListener = listener;
    }

    public interface NetworkListener {
        void onNetworkConnected();

        void onNetworkDisconnected();
    }

    public void registerNetworkReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetworkReceiver = new NetworkReceiver(this);
        mContext.registerReceiver(mNetworkReceiver, intentFilter);
    }

    public void unregisterNetworkReceiver() {
        if (mNetworkReceiver != null) {
            mContext.unregisterReceiver(mNetworkReceiver);
        }
        mNetworkListener = null;
    }

    private static class NetworkReceiver extends BroadcastReceiver {
        private WeakReference<NetworkObserver> mObserver;

        NetworkReceiver(NetworkObserver observer) {
            this.mObserver = new WeakReference<>(observer);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mObserver == null) return;
            if (mObserver.get().mNetworkListener == null) return;
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                if (NetworkUtils.isNetworkAvailable(mObserver.get().mContext)) {
                    mObserver.get().mNetworkListener.onNetworkConnected();
                } else {
                    mObserver.get().mNetworkListener.onNetworkDisconnected();
                }
            }
        }
    }
}
