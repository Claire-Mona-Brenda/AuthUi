package com.konka.authui.view;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.huawei.iptv.tm.AesUtil;
import com.konka.authui.Constants;
import com.konka.authui.R;
import com.konka.authui.util.EditUtils;
import com.konka.authui.util.PropertyUtils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {
    @BindView(R.id.et_account)
    EditText mEtAccount;

    @BindView(R.id.et_password)
    EditText mEtPassword;

    @OnClick(R.id.btn_login)
    void login() {
        String username = mEtAccount.getText().toString().trim();
        String password = mEtPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.toast_username_password_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // username和password的字段在数据库是分开的
            // name=username value=xxxxx:key是username,value才是要存储的值
            // name=password value=xxxxx:key是password,value才是要存储的值
            ContentValues nameValue = new ContentValues();
            nameValue.put("value", username);
            getContentResolver().update(Uri.parse(Constants.URI),
                    nameValue, "name=?", new String[]{"username"});

            ContentValues passwordValue = new ContentValues();
            passwordValue.put("value", AesUtil.encrypt(password));
            getContentResolver().update(Uri.parse(Constants.URI),
                    passwordValue, "name=?", new String[]{"password"});
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        // 发送广播调起TM认证，保存认为本次已进行验证，认证失败则通过TM的的手动输入完成
        PropertyUtils.setProperty(Constants.AUTH_STATUS, Constants.AUTH_STATUS_YES);

        sendBroadcast(new Intent(Constants.ACTION_REAUTH));
    }

    @OnClick(R.id.btn_reset)
    void reset() {
        mEtAccount.setText("");
        mEtPassword.setText("");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void setup() {
        mEtAccount.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                    mEtAccount.setText(EditUtils.getEditSubstring(mEtAccount));
                    mEtAccount.setSelection(mEtAccount.getText().length());
                    return true;
                }
                return false;
            }
        });
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

        try {
            String username = PropertyUtils.getProperty(Constants.PROPERTY_USERNAME);
            String password = PropertyUtils.getProperty(Constants.PROPERTY_PASSWORD);
            if (!TextUtils.isEmpty(password)) {
                password = AesUtil.decrypt(password);
            }
            mEtAccount.setText(TextUtils.isEmpty(username) ? username : "");
            mEtPassword.setText(TextUtils.isEmpty(password) ? password : "");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
    }
}
