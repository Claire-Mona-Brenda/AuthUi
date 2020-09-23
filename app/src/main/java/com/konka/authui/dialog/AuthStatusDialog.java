package com.konka.authui.dialog;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.konka.authui.AuthStatusType;
import com.konka.authui.Constants;
import com.konka.authui.R;
import com.konka.authui.view.LoginActivity;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class AuthStatusDialog extends BaseDialogFragment {
    @BindView(R.id.tv_status_content)
    TextView mTvStatusContent;

    TextView mBtnConfirm;

    TextView mBtnCancel;

    @AuthStatusType
    private int mStatusType;
    private String mIpAddress;

    private Disposable mDisposable;
    private OnAuthStatusListener mOnAuthStatusListener;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_auth_status;
    }

    @Override
    protected void setup(View view) {
        // 用ButterKnife绑定点击事件时存在点击响应相反错误问题，暂不知原因，用原生查找view解决
        mBtnConfirm = view.findViewById(R.id.btn_auth_confirm);
        mBtnCancel = view.findViewById(R.id.btn_auth_cancel);
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStatusType == Constants.AuthStatusType.NONE_IP
                        || mStatusType == Constants.AuthStatusType.PUBLIC_NETWORK_ERROR) {
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setComponent(
                            new ComponentName(Constants.NETWORK_SETTING_PKG
                                    , Constants.NETWORK_SETTING_UI));
                    if (getContext() != null) {
                        getContext().startActivity(intent);
                    }
                } else {
                    if (mOnAuthStatusListener != null) {
                        mOnAuthStatusListener.onConfirm();
                    }
                }
            }
        });
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStatusType == Constants.AuthStatusType.NONE_IP
                        || mStatusType == Constants.AuthStatusType.PUBLIC_NETWORK_ERROR) {
                    retryConnectOrRestore(
                            mStatusType == Constants.AuthStatusType.NONE_IP
                                    ? Constants.RETRY_CONNECT_IP_SECONDS
                                    : Constants.RETRY_CONNECT_DNS_SECONDS);
                }

                if (mStatusType != Constants.AuthStatusType.NONE_IP
                        && mOnAuthStatusListener != null) {
                    mOnAuthStatusListener.onCancel();
                }
            }
        });

        updateContent(mStatusType);
    }

    public AuthStatusDialog statusType(@AuthStatusType int statusType) {
        this.mStatusType = statusType;
        return this;
    }

    public AuthStatusDialog ipAddress(String ipAddress) {
        this.mIpAddress = ipAddress;
        return this;
    }

    public AuthStatusDialog setOnAuthStatusListener(OnAuthStatusListener listener) {
        this.mOnAuthStatusListener = listener;
        return this;
    }

    public void updateContent(@AuthStatusType int statusType) {
        String statusContent = "";
        String confirm = "";
        String cancel = "";
        if (statusType == Constants.AuthStatusType.NONE_IP) {
            statusContent = getStrings(R.string.dialog_none_ip);
            confirm = getStrings(R.string.dialog_network_setting);
            cancel = getStrings(R.string.dialog_recheck);
        } else if (statusType == Constants.AuthStatusType.PUBLIC_NETWORK_ERROR) {
            statusContent = MessageFormat.format(
                    getStrings(R.string.dialog_public_network_error), mIpAddress);
            confirm = getStrings(R.string.dialog_network_setting);
            cancel = getStrings(R.string.dialog_recheck);
        } else if (statusType == Constants.AuthStatusType.RMS_CONNECT_ERROR) {
            statusContent = MessageFormat.format(
                    getStrings(R.string.dialog_rms_connect_error), mIpAddress);
            confirm = getStrings(R.string.account_login);
            cancel = getStrings(R.string.dialog_cancel);
        } else if (statusType == Constants.AuthStatusType.LICENSE_ERROR) {
            statusContent = MessageFormat.format(
                    getStrings(R.string.dialog_license_error), mIpAddress);
            confirm = getStrings(R.string.account_login);
            cancel = getStrings(R.string.dialog_cancel);
        }

        mTvStatusContent.setText(statusContent);

        mBtnConfirm.setText(confirm);
        mBtnConfirm.setFocusable(true);
        mBtnConfirm.setVisibility(View.VISIBLE);

        mBtnCancel.setText(cancel);
        mBtnCancel.setFocusable(true);
        mBtnCancel.setVisibility(View.VISIBLE);
    }

    private void retryConnectOrRestore(long retrySecond) {
        // 正在重新检测，重新恢复显示
        if (mDisposable != null && !mDisposable.isDisposed()) {
            cancelRetryNetworkConnect();
            updateContent(mStatusType);
        } else {
            retryConnect(retrySecond);
        }
    }

    private void retryConnect(final long retrySecond) {
        mDisposable = Observable.interval(0, 1L, TimeUnit.SECONDS)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long countDownSecond) {
                        return retrySecond - countDownSecond;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) {
                        mBtnConfirm.setVisibility(View.GONE);
                        mBtnConfirm.setFocusable(false);
                        mBtnCancel.setText(getStrings(R.string.dialog_cancel));
                        mBtnCancel.requestFocus();
                    }
                })
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long currSecond) {
                        if (currSecond <= 0) {
                            cancelRetryNetworkConnect();
                            updateContent(mStatusType);
                            return;
                        }

                        String res = "";
                        if (mStatusType == Constants.AuthStatusType.NONE_IP) {
                            res = MessageFormat.format(
                                    getStrings(R.string.dialog_reconnect_ip), currSecond);
                        } else if (mStatusType == Constants.AuthStatusType.PUBLIC_NETWORK_ERROR) {
                            res = MessageFormat.format(
                                    getStrings(R.string.dialog_reconnect_dns), mIpAddress, currSecond);
                        }
                        mTvStatusContent.setText(res);
                    }
                });
    }

    public void cancelRetryNetworkConnect() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    @Override
    protected boolean onKeyListener(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) return true;
        return super.onKeyListener(dialog, keyCode, event);
    }

    public interface OnAuthStatusListener {

        void onCancel();

        void onConfirm();
    }
}
