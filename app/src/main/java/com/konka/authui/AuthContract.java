package com.konka.authui;

public interface AuthContract {

    interface View {
        /**
         * 连接成功设置ip显示
         */
        void setIpAddress(String ipAddress);

        /**
         * 网络连接状态
         */
        void networkConnectStatus(int status);

        /**
         * DNS连接状态
         */
        void dnsConnectStatus(int status);

        /**
         * RMS连接状态
         */
        void rmsConnectStatus(int status);

        /**
         * 账户密码验证状态
         */
        void accountValidStatus(int status);

        /**
         * RMS平台帐号密码牌照方下发失败更新界面
         */
        void accountInvalid();
    }

    interface Presenter {
        /**
         * 注册网络监听
         */
        void registerNetwork();

        /**
         * 解注册网络监听
         */
        void unregisterNetwork();
    }
}
