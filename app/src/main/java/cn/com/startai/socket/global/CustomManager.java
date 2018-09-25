package cn.com.startai.socket.global;

import android.app.Application;

import cn.com.startai.socket.sign.scm.util.MySocketSecureKey;
import cn.com.swain.baselib.app.IApp.IApp;
import cn.com.swain.support.protocolEngine.ProtocolBuild;
import cn.com.swain.support.protocolEngine.utils.SocketSecureKey;
import cn.com.swain169.log.Tlog;

/**
 * author: Guoqiang_Sun
 * date : 2018/6/4 0004
 * desc :
 */
public class CustomManager implements IApp {

    private CustomManager() {
    }

    private static final class ClassHolder {
        private static final CustomManager FLAVORS = new CustomManager();
    }

    public static CustomManager getInstance() {
        return ClassHolder.FLAVORS;
    }

    private boolean isSmartPlugProject = false;
    private boolean isWiFiSocketProject = false;
    private boolean isBleSocketProject = false;

    public boolean isSmartPlug() {
        return isSmartPlugProject;
    }

    public boolean isWiFiSocket() {
        return isWiFiSocketProject;
    }

    public boolean isBleSocket() {
        return isBleSocketProject;
    }

    private volatile byte CUSTOM;
    private volatile byte PRODUCT;
    private volatile byte PROTOCOL_VERSION;

    public byte getCustom() {
        return CUSTOM;
    }

    public byte getProduct() {
        return PRODUCT;
    }

    public byte getProtocolVersion() {
        return PROTOCOL_VERSION;
    }

    public void initBleSocketProject() {
        Tlog.i(" is Ble socket project ");
        this.isBleSocketProject = true;
    }

    public void initWiFiSocketProject() {
        Tlog.i(" is WiFi socket project ");
        this.isWiFiSocketProject = true;
    }

    public void initSmartPlugProject() {
        Tlog.i(" is smart plug project ");
        this.isSmartPlugProject = true;
    }

    @Override
    public void init(Application app) {
        Tlog.i("CustomManager init : ");


        this.CUSTOM = SocketSecureKey.Custom.CUSTOM_WAN;


        if (isBleSocket()) {
            PRODUCT = SocketSecureKey.Custom.PRODUCT_BLE_SOCKET;
        } else if (isWiFiSocket()) {
            PRODUCT = SocketSecureKey.Custom.PRODUCT_WIFI_SOCKET;
        } else if (isSmartPlug()) {
            PRODUCT = MySocketSecureKey.MCustom.PRODUCT_SMART_SOCKET;
        } else {
            PRODUCT = SocketSecureKey.Custom.PRODUCT_BLE_SOCKET;
        }

        if (isBleSocket()) {
            PROTOCOL_VERSION = ProtocolBuild.VERSION.VERSION_0;
        } else {
            PROTOCOL_VERSION = ProtocolBuild.VERSION.VERSION_SEQ;
        }

    }


}
