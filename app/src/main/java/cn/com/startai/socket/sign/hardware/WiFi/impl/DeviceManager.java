package cn.com.startai.socket.sign.hardware.WiFi.impl;

import android.os.Handler;
import android.os.Message;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.startai.mqttsdk.StartAI;
import cn.com.startai.mqttsdk.base.StartaiError;
import cn.com.startai.mqttsdk.busi.entity.C_0x8002;
import cn.com.startai.mqttsdk.busi.entity.C_0x8004;
import cn.com.startai.mqttsdk.busi.entity.C_0x8005;
import cn.com.startai.mqttsdk.listener.IOnCallListener;
import cn.com.startai.mqttsdk.mqtt.request.MqttPublishRequest;
import cn.com.startai.socket.db.gen.LanDeviceInfoDao;
import cn.com.startai.socket.db.gen.WanBindingDeviceDao;
import cn.com.startai.socket.db.manager.DBManager;
import cn.com.startai.socket.debuger.Debuger;
import cn.com.startai.socket.global.LooperManager;
import cn.com.startai.socket.mutual.js.bean.WiFiDevice.DisplayDeviceList;
import cn.com.startai.socket.mutual.js.bean.WiFiDevice.LanDeviceInfo;
import cn.com.startai.socket.sign.hardware.IControlWiFi;
import cn.com.startai.socket.sign.hardware.WiFi.bean.WanBindingDevice;
import cn.com.startai.socket.sign.hardware.WiFi.util.LanDeviceLst;
import cn.com.startai.socket.sign.scm.bean.LanBindingDevice;
import cn.com.swain.baselib.app.IApp.IService;
import cn.com.swain169.log.Tlog;

/**
 * author: Guoqiang_Sun
 * date : 2018/7/26 0026
 * desc :
 */
public class DeviceManager implements IService {

    public static final String TAG = "DeviceManager";

    DeviceManager() {
    }

    private static final int MAG_WHAT_DISPLAY_BIND_DEVICE = 0x01;

    private Handler mDisplayHandler;

    @Override
    public void onSCreate() {
        mDisplayHandler = new Handler(LooperManager.getInstance().getWorkLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == MAG_WHAT_DISPLAY_BIND_DEVICE) {
                    String id = (String) msg.obj;
                    displayBindDeviceLst(id);
                }
            }
        };
    }

    @Override
    public void onSResume() {

    }

    @Override
    public void onSPause() {

    }

    @Override
    public void onSDestroy() {
        mDisplayDeviceLst.clear();
        mDiscoveryDeviceLst.clear();
        mDisplayHandler = null;
    }

    @Override
    public void onSFinish() {

    }


    public void onNetworkStateChange() {

        hasRequestBindLst = false;
        mDiscoveryDeviceLst.clear();

    }

    private IControlWiFi.IWiFiResultCallBack mResultCallBack;

    void regWiFiResultCallBack(IControlWiFi.IWiFiResultCallBack mResultCallBack) {
        this.mResultCallBack = mResultCallBack;
    }


    private final IOnCallListener mGetBindingLsn = new IOnCallListener() {
        @Override
        public void onSuccess(MqttPublishRequest mqttPublishRequest) {
            Tlog.e(TAG, " getBindList msg send success ");

        }

        @Override
        public void onFailed(MqttPublishRequest mqttPublishRequest, StartaiError startaiError) {
            Tlog.e(TAG, " getBindList msg send failed " + startaiError.getErrorCode());

            hasRequestBindLst = false;
            if (mResultCallBack != null) {
                mResultCallBack.onResultMsgSendError(String.valueOf(startaiError.getErrorCode()));
            }
        }

        @Override
        public boolean needUISafety() {
            return false;
        }
    };

    private boolean hasRequestBindLst = false;

    /**
     * 查询绑定列表关系
     */
    void queryBindDeviceList(String mid) {

        Tlog.v(TAG, " queryBindDeviceList() " + mid);

        if (!hasRequestBindLst) {
            StartAI.getInstance().getBaseBusiManager().getBindList(mid, 1, mGetBindingLsn);
        }

        if (mDisplayHandler != null) {
            mDisplayHandler.obtainMessage(MAG_WHAT_DISPLAY_BIND_DEVICE, mid).sendToTarget();
        }
    }


    private final LanDeviceLst mDiscoveryDeviceLst = new LanDeviceLst();


    /**
     * 局域网内设备被发现
     */
    void lanDeviceDiscovery(LanDeviceInfo mDevice) {

        if (Debuger.isLogDebug) {
            Tlog.v(TAG, " lanDeviceDiscovery() " + mDevice.toString());
        }

        LanDeviceInfo displayLanDevice = mDisplayDeviceLst.getDisplayDeviceByMac(mDevice.mac);

        String mLastName = null;
        int mLastRSSI = 0;
        if (displayLanDevice != null) {
            mLastName = displayLanDevice.getName();
            mLastRSSI = displayLanDevice.getRssi();
        }

        mDiscoveryDeviceLst.deviceDiscoveryUpdateDevice(mDevice);
        mDisplayDeviceLst.deviceDiscoveryUpdateDevice(mDevice);

        if (displayLanDevice != null) {
            if (Debuger.isLogDebug) {
                Tlog.e(TAG, " lanDeviceDiscovery() updateDisplay:" + displayLanDevice.toString());
            }

            if (!mLastName.equalsIgnoreCase(mDevice.getName())) {
                updateDaoLanDeviceInfo(mDevice);
            }

            if (mLastRSSI != mDevice.rssi || !mLastName.equalsIgnoreCase(mDevice.getName())) {

                if (mResultCallBack != null) {
                    DisplayDeviceList mList = new DisplayDeviceList(displayLanDevice);
                    mResultCallBack.onResultWiFiDeviceListDisplay(mList);
                }

            }

        }

    }

    private synchronized void updateDaoLanDeviceInfo(LanDeviceInfo mDevice) {

        if (mDevice == null || mDevice.mac == null || mDevice.mac.equalsIgnoreCase("")) {
            Tlog.e(TAG, "updateLanDeviceInfoDao()  return: ");
            return;
        }

        LanDeviceInfoDao lanDeviceInfoDao = DBManager.getInstance().getDaoSession().getLanDeviceInfoDao();
        List<LanDeviceInfo> listInfo = lanDeviceInfoDao.queryBuilder().where(LanDeviceInfoDao.Properties.Mac.eq(mDevice.mac)).list();

        if (listInfo.size() > 0) {

            for (LanDeviceInfo mLanDevice : listInfo) {
                if ((mDevice.name != null && !mDevice.name.equalsIgnoreCase(mLanDevice.getName()))
                        || mDevice.bindNeedPwd != mLanDevice.getBindNeedPwd()
                        || mDevice.hasActivate != mLanDevice.getHasActivate()
                        || mDevice.hasRemote != mLanDevice.getHasRemote()
                        || (mDevice.ssid != null && !mDevice.ssid.equalsIgnoreCase(mLanDevice.getSsid()))) {

                    mLanDevice.setBindNeedPwd(mDevice.bindNeedPwd);
                    mLanDevice.setHasActivate(mDevice.hasActivate);
                    mLanDevice.setHasRemote(mDevice.hasRemote);
                    if (mDevice.ssid != null) {
                        mLanDevice.setSsid(mDevice.ssid);
                    }
                    mLanDevice.setName(mDevice.name);

                    lanDeviceInfoDao.update(mLanDevice);
                    Tlog.d(TAG, "updateLanDeviceInfoDao()  update LanDeviceInfo : " + mLanDevice.getId());
                }
            }

        } else {
            Tlog.e(TAG, "updateLanDeviceInfoDao()  listInfo==0 ");
        }

    }


    private final IOnCallListener mBindLsn = new IOnCallListener() {
        @Override
        public void onSuccess(MqttPublishRequest mqttPublishRequest) {
            Tlog.v(TAG, " wanDeviceBind msg send success");
        }

        @Override
        public void onFailed(MqttPublishRequest mqttPublishRequest, StartaiError startaiError) {
            Tlog.e(TAG, " wanDeviceBind msg send failed");

            if (mResultCallBack != null) {
                mResultCallBack.onResultBindDevice(false);
            }

        }

        @Override
        public boolean needUISafety() {
            return false;
        }
    };


    /**
     * 局域内设备被绑定
     */
    synchronized void onDeviceResponseLanBind(boolean result, LanBindingDevice mLanBindingDevice) {

        if (!result) {
            Tlog.e(TAG, " onDeviceResponseLanBind() fail");
            if (mResultCallBack != null) {
                mResultCallBack.onResultBindDevice(false);
            }
            return;
        }

        Tlog.e(TAG, " onDeviceResponseLanBind() success " + mLanBindingDevice.getOmac());
        WanBindingDeviceDao bindingDeviceDao = DBManager.getInstance().getDaoSession().getWanBindingDeviceDao();
        List<WanBindingDevice> listBind = bindingDeviceDao.queryBuilder()
                .where(WanBindingDeviceDao.Properties.Mid.eq(mLanBindingDevice.getMid()),
                        WanBindingDeviceDao.Properties.Oid.eq(mLanBindingDevice.getOid())).list();
        WanBindingDevice mWanBindingDevice = null;

        if (listBind != null && listBind.size() > 0) {
            mWanBindingDevice = listBind.get(0);
        }

        if (mWanBindingDevice == null) {
            mWanBindingDevice = new WanBindingDevice();
            mWanBindingDevice.setMac(mLanBindingDevice.getOmac());
            mWanBindingDevice.setOid(mLanBindingDevice.getOid());
            mWanBindingDevice.setMid(mLanBindingDevice.getMid());
            mWanBindingDevice.setIsAdmin(mLanBindingDevice.getIsAdmin());
            mWanBindingDevice.setCpuInfo(mLanBindingDevice.getCpuInfo());

            long insert = bindingDeviceDao.insert(mWanBindingDevice);
            Tlog.d(TAG, " getWanBindingDeviceDao insert:" + insert);
        } else {
            mWanBindingDevice.setHasBindingByLan(true);
            mWanBindingDevice.setIsAdmin(mLanBindingDevice.getIsAdmin());
            mWanBindingDevice.setCpuInfo(mLanBindingDevice.getCpuInfo());
            bindingDeviceDao.update(mWanBindingDevice);
            Tlog.d(TAG, " getWanBindingDeviceDao has this device:" + mWanBindingDevice.getGid());
        }


        LanDeviceInfoDao lanDeviceInfoDao = DBManager.getInstance().getDaoSession().getLanDeviceInfoDao();
        List<LanDeviceInfo> listInfo = lanDeviceInfoDao.queryBuilder()
                .where(LanDeviceInfoDao.Properties.DeviceID.eq(mLanBindingDevice.getOid())).list();

        LanDeviceInfo mDaoLanDeviceInfo = null;
        if (listInfo.size() > 0) {
            mDaoLanDeviceInfo = listInfo.get(0);
        }

        LanDeviceInfo mDiscoveryDeviceInfo = mDiscoveryDeviceLst.getLanDeviceByMac(mLanBindingDevice.getOmac());

        if (mDaoLanDeviceInfo == null) {

            if (mDiscoveryDeviceInfo == null) {
                mDiscoveryDeviceInfo = new LanDeviceInfo();
            }
            mDiscoveryDeviceInfo.setDeviceID(mLanBindingDevice.getOid());
            mDiscoveryDeviceInfo.setMac(mLanBindingDevice.getOmac());
            if (mDiscoveryDeviceInfo.name == null) {
                mDiscoveryDeviceInfo.name = mLanBindingDevice.getOmac();
            }
            mDiscoveryDeviceInfo.setHasAdmin(mLanBindingDevice.getIsAdmin());
            mDiscoveryDeviceInfo.setCpuInfo(mLanBindingDevice.getCpuInfo());

            long insert = lanDeviceInfoDao.insert(mDiscoveryDeviceInfo);
            Tlog.d(TAG, " lanDeviceInfoDao insert:" + insert);

        } else {

            if (mDiscoveryDeviceInfo != null) {
                mDaoLanDeviceInfo.setHasAdmin(mDiscoveryDeviceInfo.getHasAdmin());
                mDaoLanDeviceInfo.setHasActivate(mDiscoveryDeviceInfo.getHasActivate());
                mDaoLanDeviceInfo.setHasRemote(mDiscoveryDeviceInfo.getHasRemote());
            }

            mDaoLanDeviceInfo.setCpuInfo(mLanBindingDevice.getCpuInfo());
            lanDeviceInfoDao.update(mDaoLanDeviceInfo);
            Tlog.d(TAG, " lanDeviceInfoDao update:" + mDaoLanDeviceInfo.getId());

        }

        StartAI.getInstance().getBaseBusiManager().bind(mLanBindingDevice.getOid(), mBindLsn);

    }


    private final IOnCallListener mUnbindLsn = new IOnCallListener() {
        @Override
        public void onSuccess(MqttPublishRequest mqttPublishRequest) {
            Tlog.e(TAG, " unbindingDevice msg send success ");
        }

        @Override
        public void onFailed(MqttPublishRequest mqttPublishRequest, StartaiError startaiError) {
            Tlog.e(TAG, " unbindingDevice msg send fail " + startaiError.getErrorCode());
            if (mResultCallBack != null) {
                mResultCallBack.onResultUnbind(false, tmpUnbindMac);
            }
        }

        @Override
        public boolean needUISafety() {
            return false;
        }
    };


    private String tmpUnbindMac;

    /**
     * 解绑设备
     */
    void unbindingDevice(String mac, String loginUserID) {

        Tlog.e(TAG, " unbindingDevice " + mac);

        if (loginUserID == null) {
            Tlog.e(TAG, " unbindingDevice loginUserID == null  ");
            if (mResultCallBack != null) {
                mResultCallBack.onResultUnbind(false, mac);
            }
            return;
        }
        tmpUnbindMac = mac;

        LanDeviceInfo sameDisplayDeviceByMac = mDisplayDeviceLst.getDisplayDeviceByMac(mac);
        if (sameDisplayDeviceByMac != null) {
            Tlog.d(TAG, "LanDeviceUnbindDeleteDao() mDisplayDeviceLst remove " + sameDisplayDeviceByMac.getMac());
            mDisplayDeviceLst.remove(sameDisplayDeviceByMac);
        }

        WanBindingDeviceDao bindingDeviceDao = DBManager.getInstance().getDaoSession().getWanBindingDeviceDao();
        List<WanBindingDevice> listBind = bindingDeviceDao.queryBuilder().where(WanBindingDeviceDao.Properties.Mid.eq(loginUserID),
                WanBindingDeviceDao.Properties.Mac.eq(mac)).list();

        if (listBind != null && listBind.size() > 0) {

            for (WanBindingDevice bindingDevice : listBind) {
                String oid = bindingDevice.getOid();

                if (!bindingDevice.getHasBindingByWan()) {

                    Tlog.d(TAG, "LanDeviceUnbindDeleteDao() wanBindingDeviceDao delete " + bindingDevice.getGid());
                    bindingDeviceDao.deleteByKey(bindingDevice.getGid());

                    if (mResultCallBack != null) {
                        mResultCallBack.onResultUnbind(true, mac);
                    }

                } else {

                    bindingDevice.setHasBindingByLan(false);
                    bindingDeviceDao.update(bindingDevice);

                    Tlog.e(TAG, "WanBindingDeviceDao unbindingDevice oid  " + oid);
                    if (oid != null) {
                        StartAI.getInstance().getBaseBusiManager().unBind(oid, mUnbindLsn);
                    }

                }
            }

        } else {
            if (mResultCallBack != null) {
                mResultCallBack.onResultUnbind(true, mac);
            }
        }

    }

    void onDeviceResponseLanUnBind(boolean result, LanBindingDevice mLanBindingDevice, String loginUserID) {
        Tlog.e(TAG, " onDeviceResponseLanUnBind() result:" + result + " " + mLanBindingDevice.getOmac() + " loginUserID:" + loginUserID);
    }


    void onDeviceResponseToken(String mac, int token, String userID) {
        Tlog.e(TAG, " onDeviceResponseToken mac:" + mac + " token:" + token);

        WanBindingDeviceDao bindingDeviceDao = DBManager.getInstance().getDaoSession().getWanBindingDeviceDao();
        List<WanBindingDevice> listBind = bindingDeviceDao.queryBuilder()
                .where(WanBindingDeviceDao.Properties.Mid.eq(userID),
                        WanBindingDeviceDao.Properties.Mac.eq(mac)).list();

        if (listBind.size() > 0) {
            for (WanBindingDevice bindingDevice : listBind)
                if (bindingDevice != null) {
                    bindingDevice.setToken(token);
                    bindingDevice.setTokenInsterTimes(System.currentTimeMillis());
                    bindingDeviceDao.update(bindingDevice);
                }
        }

    }

    void onDeviceResponseConnect(boolean result, String id, String loginUserID) {
        Tlog.e(TAG, " onDeviceResponseConnect result " + result + " id: " + id + " loginUserID:" + loginUserID);
    }

    void onDeviceResponseSleep(boolean result, String id, String loginUserID) {
        Tlog.e(TAG, " onDeviceResponseSleep result " + result + " id: " + id + " loginUserID:" + loginUserID);
    }

    void onDeviceResponseDisconnect(boolean result, String id, String loginUserID) {
        Tlog.e(TAG, " onDeviceResponseDisconnect result " + result + " id: " + id + " loginUserID:" + loginUserID);
    }


    public int getToken(String mac, String userID) {

        WanBindingDeviceDao bindingDeviceDao = DBManager.getInstance().getDaoSession().getWanBindingDeviceDao();
        List<WanBindingDevice> listBind = bindingDeviceDao.queryBuilder()
                .where(WanBindingDeviceDao.Properties.Mid.eq(userID), WanBindingDeviceDao.Properties.Mac.eq(mac)).list();

        if (listBind.size() > 0) {
            WanBindingDevice windingDevice = listBind.get(0);
            return windingDevice.getToken();
        }

        return -1;
    }

    /*********************/


    synchronized void onUnBindResult(C_0x8004.Resp resp, String mid, String beUnbindingId) {

        if (Debuger.isLogDebug) {
            Tlog.d(TAG, " onUnBindResult result:" + String.valueOf(resp) + " mid:" + mid + " beUnbindingId:" + beUnbindingId);
        }

        WanBindingDeviceDao wanBindingDeviceDao = DBManager.getInstance().getDaoSession().getWanBindingDeviceDao();
        List<WanBindingDevice> listBind = wanBindingDeviceDao.queryBuilder().where(WanBindingDeviceDao.Properties.Mid.eq(mid),
                WanBindingDeviceDao.Properties.Oid.eq(beUnbindingId)).list();

        String mac = null;

        if (listBind != null && listBind.size() > 0) {
            for (WanBindingDevice mBindingDevice : listBind) {

                if (mac == null) {
                    mac = mBindingDevice.getMac();
                }

                if (resp.getResult() == 1) {
                    Long gid = mBindingDevice.getGid();
                    wanBindingDeviceDao.deleteByKey(gid);
                    Tlog.v(TAG, " wanBindingDeviceDao deleted " + gid);
                } else {
                    break;
                }
            }
        }


        if (mac == null) {
            mac = tmpUnbindMac;
        }

        if (mac != null && resp.getResult() == 1) {
            LanDeviceInfo sameDisplayDeviceByMac = mDisplayDeviceLst.getDisplayDeviceByMac(mac);
            if (sameDisplayDeviceByMac != null) {
                Tlog.d(TAG, "onUnBindResult() mDisplayDeviceLst remove " + sameDisplayDeviceByMac.getMac());
                mDisplayDeviceLst.remove(sameDisplayDeviceByMac);
            }
        }


        if (resp.getResult() != 1) {
            Tlog.e(TAG, "onUnBindResult fail ; errorMsg:" + resp.getContent().getErrmsg());
            if (mResultCallBack != null) {
                mResultCallBack.onResultUnbind(false, tmpUnbindMac);
            }
        } else {
            Tlog.e(TAG, "onUnBindResult success ; mac:" + mac);
            if (mResultCallBack != null) {
                mResultCallBack.onResultUnbind(true, mac);
            }
        }

    }


    synchronized void onBindResult(C_0x8002.Resp resp, String mid, C_0x8002.Resp.ContentBean.BebindingBean bebinding) {

        if (Debuger.isLogDebug) {
            Tlog.d(TAG, "onBindResult  " + mid + String.valueOf(resp) + "\n" + String.valueOf(bebinding));
        }

        if (resp.getResult() != 1) {
            if (mResultCallBack != null) {
                mResultCallBack.onResultBindDevice(false);
            }
            return;
        }

        WanBindingDeviceDao bindingDeviceDao = DBManager.getInstance().getDaoSession().getWanBindingDeviceDao();
        QueryBuilder<WanBindingDevice> whereWan = bindingDeviceDao.queryBuilder()
                .where(WanBindingDeviceDao.Properties.Mid.eq(mid),
                        WanBindingDeviceDao.Properties.Mac.eq(bebinding.getMac()));

        List<WanBindingDevice> listWan = whereWan.list();
        WanBindingDevice mBindingDevice = null;
        if (listWan != null && listWan.size() > 0) {
            mBindingDevice = listWan.get(0);
            final long gid = mBindingDevice.getGid();

            if (listWan.size() > 1) {
                for (int i = 1; i < listWan.size(); i++) {

                    WanBindingDevice wanBindingDevice = listWan.get(i);
                    Long gid1 = wanBindingDevice.getGid();
                    long gid11 = gid1;
                    if (gid11 != gid) {
                        bindingDeviceDao.deleteByKey(gid1);
                    }
                }
            }

        }

        if (mBindingDevice == null) {
            WanBindingDevice tBindingDevice = WanBindingDevice.memor(bebinding);
            tBindingDevice.setHasBindingByLan(true);
            tBindingDevice.setMid(mid);
            long insert = bindingDeviceDao.insert(tBindingDevice);
            Tlog.d(TAG, " wanBindingDeviceDao insert:" + insert);
        } else {
            mBindingDevice.setOid(bebinding.getId());
            mBindingDevice.setHasBindingByWan(true);
            mBindingDevice.setHasBindingByLan(true);
            bindingDeviceDao.update(mBindingDevice);
            Tlog.d(TAG, " wanBindingDeviceDao update:" + mBindingDevice.getGid());
        }


        final LanDeviceInfo mLanDeviceInfo = new LanDeviceInfo();
        String deviceID = bebinding.getId();

        LanDeviceInfoDao lanDeviceInfoDao = DBManager.getInstance().getDaoSession().getLanDeviceInfoDao();
        List<LanDeviceInfo> listDeviceInfo = lanDeviceInfoDao.queryBuilder().where(LanDeviceInfoDao.Properties.DeviceID.eq(deviceID)).list();

        if (listDeviceInfo.size() > 0) {
            LanDeviceInfo lanDeviceInfo = listDeviceInfo.get(0);
            mLanDeviceInfo.copy(lanDeviceInfo);
        }

        mLanDeviceInfo.setDeviceID(deviceID);
        if (mBindingDevice != null) {
            mLanDeviceInfo.setIsAdmin(mBindingDevice.getIsAdmin());
            mLanDeviceInfo.setIsLanBind(mBindingDevice.getHasBindingByLan());
        }
        mLanDeviceInfo.setMac(bebinding.getMac());
        mLanDeviceInfo.setIsWanBind(true);
        mLanDeviceInfo.checkName();

        Tlog.e(TAG, "onBindResult() mDisplayDeviceLst.putBindingDevice:" + mLanDeviceInfo.toString());
        mDisplayDeviceLst.add(mLanDeviceInfo);


        if (mResultCallBack != null) {
            if (Debuger.isLogDebug) {
                Tlog.e(TAG, "onBindResult() updateDisplay:" + mLanDeviceInfo.toString());
            }
            DisplayDeviceList mLst = new DisplayDeviceList(mLanDeviceInfo);
            mResultCallBack.onResultWiFiDeviceListDisplay(mLst);
        }

        if (mResultCallBack != null) {
            mResultCallBack.onResultBindDevice(true);
        }

    }

    synchronized void onGetBindListResult(C_0x8005.Response response, String mid) {

        if (Debuger.isLogDebug) {
            Tlog.d(TAG, "onGetBindListResult mid:" + mid + " " + String.valueOf(response));
        }

        if (response.getResult() != 1) {

            if (mResultCallBack != null) {
                mResultCallBack.onResultMsgSendError(response.getErrcode());
            }

            return;
        }

        if (mid == null) {
            return;
        }

        hasRequestBindLst = true;

        ArrayList<C_0x8005.Resp.ContentBean> bindList = response.getResp();

        WanBindingDeviceDao bindingDeviceDao = DBManager.getInstance().getDaoSession().getWanBindingDeviceDao();

        List<WanBindingDevice> listWan = bindingDeviceDao.queryBuilder().where(WanBindingDeviceDao.Properties.Mid.eq(mid)).list();

        for (WanBindingDevice mBindingDevice : listWan) {

            boolean serverHas = false;
            C_0x8005.Resp.ContentBean tContentBean = null;
            for (C_0x8005.Resp.ContentBean mContentBean : bindList) {
                if (mContentBean.getId().equals(mBindingDevice.getOid())) {
                    serverHas = true;
                    tContentBean = mContentBean;
                    break;
                }
            }

            if (!serverHas) { // 我有，服务器没有
                if (mBindingDevice.getHasBindingByWan()) {
                    bindingDeviceDao.deleteByKey(mBindingDevice.getGid());
                    Tlog.e(TAG, "onGetBindListResult() deleteWanBindDevice:" + mBindingDevice.toString());
                }
            } else { // 我有，服务器也有
                mBindingDevice.setConnstatus(tContentBean.getConnstatus());// 连接状态是实时刷新的
                mBindingDevice.setHasBindingByWan(true);
                bindingDeviceDao.update(mBindingDevice);
                Tlog.e(TAG, "onGetBindListResult() bindingDeviceDao update:" + mBindingDevice.toString());
            }

        }

        // 我没有，服务器有
        for (C_0x8005.Resp.ContentBean mBean : bindList) {

            Tlog.e(TAG, "mid:" + mid + " oid:" + mBean.getId());

            boolean myHas = false;
            for (WanBindingDevice mBindingDevice : listWan) {
                if (mBean.getId().equals(mBindingDevice.getOid())) {
                    myHas = true;
                    break;
                }
            }

            if (!myHas) {
                WanBindingDevice memor = WanBindingDevice.memor(mBean);
                memor.setHasBindingByWan(true);
                memor.setMid(mid);
                bindingDeviceDao.insert(memor);
                Tlog.e(TAG, "onGetBindListResult() bindingDeviceDao insert:" + memor.toString());
            }

        }


        if (mDisplayHandler != null) {
            mDisplayHandler.obtainMessage(MAG_WHAT_DISPLAY_BIND_DEVICE, mid).sendToTarget();
        }

    }

    /*********************/


    private final DisplayDeviceList mDisplayDeviceLst = new DisplayDeviceList();

    private synchronized void displayBindDeviceLst(String mid) {
        Tlog.v(TAG, "displayBindDeviceLst() " + mid);

        if (mid == null || "".equalsIgnoreCase(mid)) {
            return;
        }

        DBManager.getInstance().getDaoSession().clear();

        final Map<String, LanDeviceInfo> mLanDeviceInfos = new HashMap<>();

        LanDeviceInfoDao lanDeviceInfoDao = DBManager.getInstance().getDaoSession().getLanDeviceInfoDao();

//        Tlog.d(TAG, "displayBindDeviceLst() listDeviceInfo dao size: " + listDeviceInfo.size());

        WanBindingDeviceDao bindingDeviceDao = DBManager.getInstance().getDaoSession().getWanBindingDeviceDao();
        List<WanBindingDevice> listBind = bindingDeviceDao.queryBuilder().where(WanBindingDeviceDao.Properties.Mid.eq(mid)).list();
        Tlog.d(TAG, "displayBindDeviceLst() Bind dao size: " + listBind.size());

        for (WanBindingDevice mBindingDevice : listBind) {

            if (!mBindingDevice.getHasBindingByWan()) {
                if (Debuger.isLogDebug) {
                    Tlog.e(TAG, "displayBindDeviceLst() not bind by wan: " + String.valueOf(mBindingDevice));
                }
                continue;
            }

            LanDeviceInfo mLanDeviceInfo = new LanDeviceInfo();
            String deviceID = mBindingDevice.getOid();

            boolean needInsertLanDeviceInfo = false;
            List<LanDeviceInfo> listDeviceInfo = lanDeviceInfoDao.queryBuilder().where(LanDeviceInfoDao.Properties.DeviceID.eq(deviceID)).list();
            if (listDeviceInfo.size() > 0) {
                LanDeviceInfo lanDeviceInfo = listDeviceInfo.get(0);
                if (Debuger.isLogDebug) {
                    Tlog.i(TAG, "displayBindDeviceLst() copy form LanDeviceInfo: " + String.valueOf(lanDeviceInfo));
                }
                mLanDeviceInfo.copy(lanDeviceInfo);
            } else {
                needInsertLanDeviceInfo = true;
            }

            mLanDeviceInfo.setDeviceID(deviceID);
            mLanDeviceInfo.setMac(mBindingDevice.getMac());
            mLanDeviceInfo.setIsAdmin(mBindingDevice.getIsAdmin());
            mLanDeviceInfo.setIsWanBind(mBindingDevice.getHasBindingByWan());
            mLanDeviceInfo.setIsLanBind(mBindingDevice.getHasBindingByLan());
            mLanDeviceInfo.checkName();
            mLanDeviceInfo.setState(mBindingDevice.getConnstatus() == 1);
            if (mLanDeviceInfo.rssi == 0) {// 表示没有,默认为最小信号
                mLanDeviceInfo.rssi = -100;
            }

            if (needInsertLanDeviceInfo) {
                long insert = lanDeviceInfoDao.insert(mLanDeviceInfo);
                Tlog.d(TAG, "displayBindDeviceLst() lanDeviceInfoDao insert:" + insert);
            }

            mLanDeviceInfos.put(mLanDeviceInfo.getDeviceID(), mLanDeviceInfo);
            if (Debuger.isLogDebug) {
                Tlog.e(TAG, "displayBindDeviceLst() putWanBindDevice: " + String.valueOf(mLanDeviceInfo));
            }

        }

        Map<String, LanDeviceInfo> stringLanDeviceInfoMap = mDisplayDeviceLst.addAll(mLanDeviceInfos);
        if (stringLanDeviceInfoMap != null && stringLanDeviceInfoMap.size() > 0) {
            for (Map.Entry<String, LanDeviceInfo> tmpEntries : stringLanDeviceInfoMap.entrySet()) {
                String mac = tmpEntries.getValue().getMac();
                if (mResultCallBack != null) {
                    Tlog.e(TAG, "displayBindDeviceLst() onResultWiFiDeviceListDisplay() onResultUnbind(" + mac + ")");
                    mResultCallBack.onResultUnbind(true, mac);
                }
            }
            stringLanDeviceInfoMap.clear();
        }

        if (mResultCallBack != null) {
            Tlog.e(TAG, "displayBindDeviceLst() onResultWiFiDeviceListDisplay(mDisplayDeviceLst) ");
            mResultCallBack.onResultWiFiDeviceListDisplay(mDisplayDeviceLst);
        }
    }

    void onLogoutResult(int result) {
        Tlog.d(TAG, " onLogoutResult " + result);
        hasRequestBindLst = false;

        if (result == 1) {
            if (mDisplayHandler != null) {
                mDisplayHandler.removeMessages(MAG_WHAT_DISPLAY_BIND_DEVICE);
            }
            mDisplayDeviceLst.clear();
        }
    }

    void onLoginResult(int result) {
        Tlog.e(TAG, " onLoginResult " + result);
        hasRequestBindLst = false;
    }


    synchronized void onDeviceRename(String mac, String name) {
        Tlog.v(TAG, " onDeviceRename " + mac + " name : " + name);

        LanDeviceInfo mLanDeviceInfo = mDiscoveryDeviceLst.getLanDeviceByMac(mac);
        if (mLanDeviceInfo != null) {
            mLanDeviceInfo.name = name;
        }

        if (name != null && mDisplayDeviceLst.rename(mac, name)) {

            if (mResultCallBack != null) {

                LanDeviceInfo displayDeviceByMac = mDisplayDeviceLst.getDisplayDeviceByMac(mac);

                if (Debuger.isLogDebug) {
                    Tlog.e(TAG, " onDeviceRename() onResultWiFiDeviceListDisplay:" + displayDeviceByMac.toString());
                }
                DisplayDeviceList mLst = new DisplayDeviceList(displayDeviceByMac);
                mResultCallBack.onResultWiFiDeviceListDisplay(mLst);
            }


            LanDeviceInfoDao lanDeviceInfoDao = DBManager.getInstance().getDaoSession().getLanDeviceInfoDao();
            QueryBuilder<LanDeviceInfo> where = lanDeviceInfoDao.queryBuilder().where(
                    LanDeviceInfoDao.Properties.Mac.eq(mac));
            List<LanDeviceInfo> list = where.list();

            if (list != null && list.size() > 0) {
                for (LanDeviceInfo mDaoLanDeviceInfo : list) {
                    mDaoLanDeviceInfo.setName(name);
                    lanDeviceInfoDao.update(mDaoLanDeviceInfo);
                    Tlog.v(TAG, "onDeviceRename() lanDeviceInfoDao update " + mDaoLanDeviceInfo.getId());
                }
            } else {
                // 收到重命名消息，如果设备info里面没有此设备，插入一台。
                if (mLanDeviceInfo != null) {
                    LanDeviceInfo displayDeviceByMac = mDisplayDeviceLst.getDisplayDeviceByMac(mac);
                    if (displayDeviceByMac != null) {
                        mLanDeviceInfo.setDeviceID(displayDeviceByMac.getDeviceID());
                    }
                    if (mLanDeviceInfo.getDeviceID() != null) {
                        lanDeviceInfoDao.insert(mLanDeviceInfo);
                        Tlog.v(TAG, "onDeviceRename() lanDeviceInfoDao insert  " + mLanDeviceInfo.getId());
                    }
                }
            }

        }
    }


    void relaySwitch(String mac, boolean status) {
        Tlog.v(TAG, " relaySwitch() mac:" + mac + " status:" + status);

        LanDeviceInfo mLanDeviceInfo = mDiscoveryDeviceLst.getLanDeviceByMac(mac);
        if (mLanDeviceInfo != null) {
            mLanDeviceInfo.relayState = status;
        }

        LanDeviceInfo sameDisplayDeviceByMac = mDisplayDeviceLst.getDisplayDeviceByMac(mac);
        if (sameDisplayDeviceByMac != null) {
            sameDisplayDeviceByMac.relayState = status;
        }

        if (mResultCallBack != null) {
            mResultCallBack.onResultStateQuickControlRelay(mac, status);
        }

    }

    /*************/


    void onDeviceConnectStatusChange(String userid, int status, String sn) {
        Tlog.i(TAG, "onDeviceConnectStatusChange  sn " + sn + "  status:" + status + " userid:" + userid);

        LanDeviceInfoDao lanDeviceInfoDao = DBManager.getInstance().getDaoSession().getLanDeviceInfoDao();
        List<LanDeviceInfo> listInfo = lanDeviceInfoDao.queryBuilder().where(LanDeviceInfoDao.Properties.DeviceID.eq(sn)).list();

        String mac = null;

        for (int i = 0; i < listInfo.size(); i++) {
            LanDeviceInfo lanDeviceInfo = listInfo.get(i);
            lanDeviceInfo.setState(status == 1);
            lanDeviceInfoDao.update(lanDeviceInfo);
            if (mac == null) {
                mac = lanDeviceInfo.mac;
            }
            Tlog.i(TAG, " onDeviceConnectStatusChange() lanDeviceInfoDao update " + String.valueOf(lanDeviceInfo));
        }

        WanBindingDeviceDao wanBindingDeviceDao = DBManager.getInstance().getDaoSession().getWanBindingDeviceDao();
        List<WanBindingDevice> listWan = wanBindingDeviceDao.queryBuilder().where(WanBindingDeviceDao.Properties.Mid.eq(userid),
                WanBindingDeviceDao.Properties.Oid.eq(sn)).list();
        for (int i = 0; i < listWan.size(); i++) {
            WanBindingDevice wanBindingDevice = listWan.get(i);
            wanBindingDevice.setConnstatus(status);
            wanBindingDeviceDao.update(wanBindingDevice);
            Tlog.i(TAG, " onDeviceConnectStatusChange() wanBindingDeviceDao update " + String.valueOf(wanBindingDevice));
        }

        LanDeviceInfo displayDeviceByFromId = mDisplayDeviceLst.getDisplayDeviceById(sn);

        mDisplayDeviceLst.updateConnectStatus(sn, mac, status == 1);

        if (displayDeviceByFromId != null && mResultCallBack != null) {
            if (Debuger.isLogDebug) {
                Tlog.e(TAG, " onDeviceConnectStatusChange() onResultWiFiDeviceListDisplay:" + displayDeviceByFromId.toString());
            }
            DisplayDeviceList mLst = new DisplayDeviceList(displayDeviceByFromId);
            mResultCallBack.onResultWiFiDeviceListDisplay(mLst);
        }

    }

    /*************/
    public String getMacByIp(String ip) {
        String mac = getDiscoveryMacByIp(ip);
        if (mac != null) {
            return mac;
        }
        return getDisplayMacByIp(ip);
    }

    private String getDiscoveryMacByIp(String ip) {
        LanDeviceInfo lanDeviceMacByIP = mDiscoveryDeviceLst.getLanDeviceByIP(ip);
        return (lanDeviceMacByIP != null ? lanDeviceMacByIP.mac : null);
    }

    private String getDisplayMacByIp(String ip) {
        LanDeviceInfo displayDeviceByIp = mDisplayDeviceLst.getDisplayDeviceByIp(ip);
        return (displayDeviceByIp != null ? displayDeviceByIp.mac : null);
    }

    /*************/

    public String getDisplayDeviceMacByID(String fromId) {
        LanDeviceInfo displayDeviceById = mDisplayDeviceLst.getDisplayDeviceById(fromId);
        return (displayDeviceById != null ? displayDeviceById.mac : null);
    }

    public LanDeviceInfo getDisplayDeviceByMac(String mac) {
        return mDisplayDeviceLst.getDisplayDeviceByMac(mac);
    }

    public LanDeviceInfo getDiscoveryDeviceByMac(String mac) {
        return mDiscoveryDeviceLst.getLanDeviceByMac(mac);
    }



        /*C_0x8001.Req.ContentBean contentBean = new C_0x8001.Req.ContentBean();
        contentBean.setAppid("ae6529f2fc52782a6d75db3259257084");
        contentBean.setApptype("smartOlWifi");
        contentBean.setClientid("SNSNSNSNSNSNSNSNSNSNSNSNSNSNSNSN");
        contentBean.setDomain("startai");
        contentBean.setSn("SNSNSNSNSNSNSNSNSNSNSNSNSNSNSNSN");
        contentBean.setM_ver("Json_1.2.9_9.2.1");

        C_0x8001.Req.ContentBean.FirmwareParamBean firmwareParamBean = new C_0x8001.Req.ContentBean.FirmwareParamBean();
        firmwareParamBean.setBluetoothMac("AA:AA:AA:AA:AA:AA");
        firmwareParamBean.setFirmwareVersion("abc");

        contentBean.setFirmwareParam(firmwareParamBean);

        //代智能硬件激活
        StartAI.getInstance().getBaseBusiManager().hardwareActivate(contentBean, new IOnCallListener() {
            @Override
            public void onSuccess(MqttPublishRequest mqttPublishRequest) {

            }

            @Override
            public void onFailed(MqttPublishRequest mqttPublishRequest, StartaiError startaiError) {

            }

            @Override
            public boolean needUISafety() {
                return false;
            }
        });
*/


}
