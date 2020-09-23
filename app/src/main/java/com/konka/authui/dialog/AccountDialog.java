package com.konka.authui.dialog;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.huawei.iptv.tm.AesUtil;
import com.konka.authui.Constants;
import com.konka.authui.R;
import com.konka.authui.util.EditUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class AccountDialog extends BaseDialogFragment {

    @BindView(R.id.et_password)
    EditText mEtPassword;

    @OnClick(R.id.btn_account_confirm)
    void confirmAccount() {
        String password = mEtPassword.getText().toString().trim();
        if (!TextUtils.isEmpty(password)) {
            try {
                String encryptPassword = AesUtil.encrypt(password);
                if (isPasswordValid(encryptPassword)) {
                    dismiss();
                    if (mOnAccountListener != null) {
                        mOnAccountListener.onConfirmAccount(password);
                    }
                } else {
                    Toast.makeText(getContext(), R.string.toast_password_invalid, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.btn_cancel)
    void cancel() {
        if (mOnAccountListener != null) {
            mOnAccountListener.onCancel();
        }
    }

    private OnAccountListener mOnAccountListener;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_account;
    }

    @Override
    protected void setup(View view) {
        mEtPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                    mEtPassword.setText(EditUtils.getEditSubstring(mEtPassword));
                    mEtPassword.setSelection(mEtPassword.getText().length());
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected int resizeDialogWidth() {
        return (int) (getResources().getDisplayMetrics().widthPixels * 0.3);
    }

    private boolean isPasswordValid(String encryptPassword) {
        try {
            String encryptVerifyPassword = AesUtil.encrypt(Constants.VERIFY_PASSWORD);
            return TextUtils.equals(encryptPassword, encryptVerifyPassword);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public AccountDialog setOnAccountListener(OnAccountListener listener) {
        this.mOnAccountListener = listener;
        return this;
    }

    public interface OnAccountListener {
        void onConfirmAccount(String password);
        void onCancel();
    }
}
