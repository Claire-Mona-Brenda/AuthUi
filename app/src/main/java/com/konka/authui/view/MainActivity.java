package com.konka.authui.view;

import android.arch.lifecycle.Lifecycle;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.TextView;

import com.konka.authui.AuthContract;
import com.konka.authui.AuthStatusType;
import com.konka.authui.Constants;
import com.konka.authui.R;
import com.konka.authui.dialog.AuthStatusDialog;
import com.konka.authui.dialog.AccountDialog;
import com.konka.authui.presenter.AuthPresenter;

import java.text.MessageFormat;

import butterknife.BindView;

public class MainActivity extends BaseActivity implements AuthContract.View {

    @BindView(R.id.tv_auth_status)
    TextView mTvAuthStatus;

    private AuthPresenter mPresenter;

    private AuthStatusDialog mAuthStatusDialog;
    private AccountDialog mAccountDialog;

    private String mIpAddress;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void setup() {
        mPresenter = new AuthPresenter(this);
        getLifecycle().addObserver(mPresenter);
    }

    /**
     * 连接成功设置ip显示
     */
    @Override
    public void setIpAddress(String ipAddress) {
        mIpAddress = ipAddress;
        mTvAuthStatus.setText(TextUtils.isEmpty(ipAddress) ? getString(R.string.network_no_ip) :
                MessageFormat.format(getString(R.string.network_ip_address), ipAddress));
    }

    /**
     * 网络连接状态
     */
    @Override
    public void networkConnectStatus(int status) {
        if (status == Constants.NetworkStatus.CONNECTED) {
            dismissAuthStatusDialog();
        } else {
            showAuthStatusDialog(Constants.AuthStatusType.NONE_IP, null);
        }
    }

    /**
     * DNS连接状态
     */
    @Override
    public void dnsConnectStatus(int status) {
        if (status == Constants.PingStatus.SUCCESS) {
            dismissAuthStatusDialog();
        } else {
            showAuthStatusDialog(Constants.AuthStatusType.PUBLIC_NETWORK_ERROR, mIpAddress);
        }
    }

    /**
     * RMS连接状态
     */
    @Override
    public void rmsConnectStatus(int status) {
        if (status == Constants.RmsStatus.CONNECTED) {
            dismissAuthStatusDialog();
        } else {
            showAuthStatusDialog(Constants.AuthStatusType.RMS_CONNECT_ERROR, mIpAddress);
        }
    }

    /**
     * 账户密码验证状态
     */
    @Override
    public void accountValidStatus(int status) {
        if (status == Constants.AccountStatus.VALID) {
            dismissAuthStatusDialog();
            // TM会自动拉起验证和启动Launcher，直接结束界面
            finish();
        } else {
            showAuthStatusDialog(Constants.AuthStatusType.LICENSE_ERROR, mIpAddress);
        }

    }

    /**
     * RMS平台帐号密码牌照方下发失败更新界面
     */
    @Override
    public void accountInvalid() {
        mTvAuthStatus.setText(R.string.account_invalid);
    }

    private void showAuthStatusDialog(@AuthStatusType final int statusType, String ipAddress) {
        dismissAuthStatusDialog();

        mAuthStatusDialog = new AuthStatusDialog()
                .statusType(statusType)
                .ipAddress(ipAddress)
                .setOnAuthStatusListener(new AuthStatusDialog.OnAuthStatusListener() {
                    @Override
                    public void onCancel() {
                        if (statusType == Constants.AuthStatusType.PUBLIC_NETWORK_ERROR) {
                            mPresenter.checkDNS();
                        } else if (statusType == Constants.AuthStatusType.RMS_CONNECT_ERROR
                                || statusType == Constants.AuthStatusType.LICENSE_ERROR) {
                            dismissAuthStatusDialog();

                            mPresenter.checkNetwork();

                        }
                    }

                    @Override
                    public void onConfirm() {
                        if (statusType == Constants.AuthStatusType.RMS_CONNECT_ERROR
                                || statusType == Constants.AuthStatusType.LICENSE_ERROR) {
                            showAccountDialog();
                        }
                    }
                });
        mAuthStatusDialog.show(getSupportFragmentManager(), "");
    }

    private void showAccountDialog() {
        dismissAccountDialog();

        mAccountDialog = new AccountDialog();
        mAccountDialog.setOnAccountListener(new AccountDialog.OnAccountListener() {
            @Override
            public void onConfirmAccount(String password) {
                dismissAccountDialog();

                startActivityForResult(new Intent(
                        MainActivity.this, LoginActivity.class),
                        Constants.RequestCode.REQUEST_CODE_LOGIN);
            }

            @Override
            public void onCancel() {
                dismissAccountDialog();
            }
        }).show(getSupportFragmentManager(), "");
    }

    private void dismissAuthStatusDialog() {
        if (isAuthStatusDialogShowing()) {
            mAuthStatusDialog.cancelRetryNetworkConnect();
            mAuthStatusDialog.dismiss();
            mAuthStatusDialog = null;
        }
    }

    private void dismissAccountDialog() {
        if (isAccountDialogShowing()) {
            mAccountDialog.dismiss();
            mAccountDialog = null;
        }
    }

    private boolean isAuthStatusDialogShowing() {
        return mAuthStatusDialog != null;
    }

    private boolean isAccountDialogShowing() {
        return mAccountDialog != null;
    }
}
