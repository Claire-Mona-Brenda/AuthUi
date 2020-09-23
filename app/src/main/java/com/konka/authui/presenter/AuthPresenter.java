package com.konka.authui.presenter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.konka.authui.AuthContract;
import com.konka.authui.Constants;
import com.konka.authui.receiver.NetworkObserver;
import com.konka.authui.util.NetworkUtils;
import com.konka.authui.util.PropertyUtils;
import com.mapi.netutil.NetInfo;
import com.mapi.netutil.NetUtil;
import com.mapi.netutil.NetView;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public class AuthPresenter extends BasePresenter<AuthContract.View>
        implements AuthContract.Presenter, NetworkObserver.NetworkListener {
    private static final String TAG = "AuthPresenter";
    private NetworkObserver mNetworkObserver;
    private Disposable mCheckDNSDisposable;

    public AuthPresenter(AuthContract.View view) {
        super(view);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private void handleMultiError() {
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                Log.i(TAG, throwable.getMessage());
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    @Override
    public void registerNetwork() {
        Context context = (Context) mView;
        mNetworkObserver = new NetworkObserver(context);
        mNetworkObserver.registerNetworkReceiver();
        mNetworkObserver.setNetworkListener(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @Override
    public void unregisterNetwork() {
        if (mNetworkObserver != null) {
            mNetworkObserver.unregisterNetworkReceiver();
        }
    }

    @Override
    public void onNetworkConnected() {
        NetUtil netUtil = NetUtil.getInstance((Context) mView);
        netUtil.NetUtil_SetNetView(new NetView() {
            @Override
            public void Event_EthConnectMode(int mode, Boolean connect, NetInfo netInfo) {
                if (connect && netInfo != null && !TextUtils.isEmpty(netInfo.getIpaddr())) {
                    mView.setIpAddress(netInfo.getIpaddr());
                    mView.networkConnectStatus(Constants.NetworkStatus.CONNECTED);

                    // ping dns
                    checkDNS();
                }
            }
        });
        netUtil.getEthConnectMode();
    }

    // 在启动时网线就没有连接，不会回调该方法，需要通过delay延时后做网络检测
    @Override
    public void onNetworkDisconnected() {
        mView.setIpAddress(null);
        mView.networkConnectStatus(Constants.NetworkStatus.DISCONNECTED);
    }

    // 启动界面时直接调用即使网线已连接情况仍然会判断为false
    // 需要启动一个延时后再判断
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void checkNetworkDelay() {
        addObserver(Observable.just(new Object()).delay(10L, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        checkNetwork();
                    }
                }));
    }

    public void checkNetwork() {
        if (NetworkUtils.isNetworkAvailable((Context) mView)) {
            onNetworkConnected();
        } else {
            onNetworkDisconnected();
        }
    }

    public void checkDNS() {
        if (mCheckDNSDisposable != null && !mCheckDNSDisposable.isDisposed()) {
            mCheckDNSDisposable.dispose();
            removeObserver(mCheckDNSDisposable);
        }

        mCheckDNSDisposable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                // ping 3次超时时间3s
                Process process = Runtime.getRuntime().exec("ping -c 3 -w "
                        + Constants.RETRY_CONNECT_DNS_SECONDS + " " + Constants.PING_DNS);
                emitter.onNext(process.waitFor());
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.from(Looper.myLooper()))
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer pingStatus) {
                        mView.dnsConnectStatus(pingStatus);
                        if (pingStatus == Constants.PingStatus.SUCCESS) {
                            // 网络连接正常，检查RMS连接
                            checkRms();
                        }
                    }
                });
        addObserver(mCheckDNSDisposable);
    }

    private void checkRms() {
        String result = PropertyUtils.isRMSConnected(Constants.RMS_FIRST_BOOT_KEY, Constants.RMS_DISCONNECTED);
        if (TextUtils.equals(result, Constants.RMS_CONNECTED)) {
            mView.rmsConnectStatus(Constants.RmsStatus.CONNECTED);

            // RMS连接正常，检查账号密码
            checkAccount();
        } else {
            mView.rmsConnectStatus(Constants.RmsStatus.DISCONNECTED);
        }
    }

    private void checkAccount() {
        String username = PropertyUtils.getProperty(Constants.PROPERTY_USERNAME);
        String password = PropertyUtils.getProperty(Constants.PROPERTY_PASSWORD);
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            mView.accountValidStatus(Constants.AccountStatus.VALID);
        } else {
            mView.accountInvalid();
            mView.accountValidStatus(Constants.AccountStatus.INVALID);
        }
    }
}
