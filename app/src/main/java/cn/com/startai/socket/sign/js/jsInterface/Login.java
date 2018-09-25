package cn.com.startai.socket.sign.js.jsInterface;

import android.os.Looper;
import android.os.Message;

import org.xwalk.core.JavascriptInterface;

import cn.com.startai.socket.mutual.js.bean.MobileLogin;
import cn.com.startai.socket.mutual.js.bean.UserRegister;
import cn.com.startai.socket.sign.js.util.H5Config;
import cn.com.swain.baselib.jsInterface.AbsHandlerJsInterface;
import cn.com.swain169.log.Tlog;

/**
 * author: Guoqiang_Sun
 * date : 2018/6/6 0006
 * desc :
 */
public class Login extends AbsHandlerJsInterface {


    public interface IJSLoginCallBack {

        void onJSThirdLogin(String type);

        void onJSMobileLogin(MobileLogin mLogin);

        void onJSEmailLogin(MobileLogin mLogin);

        void onJSGetMobileLoginCode(String phone);

        void onJSIsLogin();

        void onJSLoginOut();

        void onJSEmailRegister(UserRegister obj);

        void onJSEmailForgot(String email);
    }

    public static final class Method {

        private static final String THIRD_LOGIN = "javascript:thirdPartyLoginResponse($result,'$data')";

        public static String callJsThirdLogin(boolean result, String data) {
            return THIRD_LOGIN.replace("$result", String.valueOf(result)).replace("$data", data);
        }

        private static final String MOBILE_LOGIN = "javascript:mobileLoginResponse($result,'$code')";

        public static String callJsMobileLogin(boolean result, String errorCode) {
            return MOBILE_LOGIN.replace("$result", String.valueOf(result)).replace("$code", String.valueOf(errorCode));
        }


        private static final String EMAIL_LOGIN = "javascript:emailLoginResponse($result,'$code')";

        public static String callJsEmailLogin(boolean result, String errorCode) {
            return EMAIL_LOGIN.replace("$result", String.valueOf(result)).replace("$code", String.valueOf(errorCode));
        }


        private static final String EMAIL_REGISTER_RESPONSE = "javascript:emailSignupResponse($result,'$code')";

        public static String callJsEmailRegister(boolean result, String errorCode) {
            return EMAIL_REGISTER_RESPONSE.replace("$result", String.valueOf(result)).replace("$code", String.valueOf(errorCode));
        }

        private static final String GET_MOBILE_LOGIN_CODE = "javascript:getMobilePhoneCodeResponse($result)";

        public static String callJsGetMobileLoginCodeResult(boolean result) {
            return GET_MOBILE_LOGIN_CODE.replace("$result", String.valueOf(result));
        }

        private static final String GET_IS_LOGIN_RESPONSE = "javascript:isLoginResponse($result)";

        public static String callJsIsLogin(boolean result) {
            return GET_IS_LOGIN_RESPONSE.replace("$result", String.valueOf(result));
        }

        private static final String GET_IS_LOGIN_OUT_RESPONSE = "javascript:logoutUserResponse($result)";

        public static String callJsIsLoginOut(boolean result) {
            return GET_IS_LOGIN_OUT_RESPONSE.replace("$result", String.valueOf(result));
        }

        private static final String GET_IS_EMAIL_FORGOT_RESPONSE = "javascript:emailForgotResponse($result)";

        public static String callJsEmailForgot(boolean result) {
            return GET_IS_EMAIL_FORGOT_RESPONSE.replace("$result", String.valueOf(result));
        }
    }

    public static final String NAME_JSI = "Login";

    private String TAG = H5Config.TAG;

    private final IJSLoginCallBack mCallBack;

    public Login(Looper mLooper, IJSLoginCallBack mCallBack) {
        super(NAME_JSI, mLooper);
        this.mCallBack = mCallBack;
    }

    public static final String TYPE_LOGIN_FACEBOOK = "Facebook";
    public static final String TYPE_LOGIN_TWITTER = "Twitter";
    public static final String TYPE_LOGIN_GOOGLE = "Google";

    @JavascriptInterface
    public void thirdPartyLoginRequest(String type) {
        Tlog.v(TAG, " thirdPartyLoginRequest type:" + type);
        getHandler().obtainMessage(MSG_THIRD_LOGIN, type).sendToTarget();
    }


    @JavascriptInterface
    public void mobileLoginRequest(String phone, String code) {
        Tlog.v(TAG, " mobileLoginRequest phone:" + phone + " code:" + code);

        final MobileLogin mLogin = new MobileLogin();
        mLogin.phone = phone;
        mLogin.code = code;
        getHandler().obtainMessage(MSG_MOBILE_LOGIN, mLogin).sendToTarget();

    }

    @JavascriptInterface
    public void getMobilePhoneCodeRequest(String phone) {
        Tlog.v(TAG, " getMobilePhoneCodeRequest phone:" + phone);

        getHandler().obtainMessage(MSG_GET_MOBILE_LOGIN_CODE, phone).sendToTarget();
    }

    @JavascriptInterface
    public void isLoginRequest() {
        Tlog.v(TAG, " isLoginRequest ");
        getHandler().sendEmptyMessage(MSG_IS_LOGIN);
    }

    @JavascriptInterface
    public void logoutUserRequest() {
        Tlog.v(TAG, " logoutUserRequest ");

        getHandler().sendEmptyMessage(MSG_LOGIN_OUT);
    }


    @JavascriptInterface
    public void emailLoginRequest(String email, String pwd) {
        Tlog.v(TAG, " emailLoginRequest ");

        final MobileLogin mLogin = new MobileLogin();
        mLogin.email = email;
        mLogin.emailPwd = pwd;
        getHandler().obtainMessage(MSG_EMAIL_LOGIN, mLogin).sendToTarget();

    }

    @JavascriptInterface
    public void emailSignupRequest(String email, String pwd, String username) {
        Tlog.v(TAG, " emailSignupRequest ");
        final UserRegister mUserRegister = new UserRegister();
        mUserRegister.email = email;
        mUserRegister.pwd = pwd;
        mUserRegister.username = username;
        getHandler().obtainMessage(MSG_EMAIL_REGISTER, mUserRegister).sendToTarget();
    }

    @JavascriptInterface
    public void emailForgotRequest(String email) {
        Tlog.v(TAG, " emailForgotRequest ");
        getHandler().obtainMessage(MSG_EMAIL_FORGOT, email).sendToTarget();
    }


    private static final int MSG_THIRD_LOGIN = 0x2A;
    private static final int MSG_MOBILE_LOGIN = 0x2B;
    private static final int MSG_GET_MOBILE_LOGIN_CODE = 0x2C;

    private static final int MSG_IS_LOGIN = 0x2D;

    private static final int MSG_LOGIN_OUT = 0x2E;

    private static final int MSG_EMAIL_LOGIN = 0x2F;

    private static final int MSG_EMAIL_REGISTER = 0x30;
    private static final int MSG_EMAIL_FORGOT = 0x31;


    @Override
    protected void handleMessage(Message msg) {

        if (msg.what == MSG_THIRD_LOGIN) {
            if (mCallBack != null) {
                mCallBack.onJSThirdLogin((String) msg.obj);
            }
        } else if (msg.what == MSG_MOBILE_LOGIN) {
            if (mCallBack != null) {
                mCallBack.onJSMobileLogin((MobileLogin) (msg.obj));
            }

        } else if (msg.what == MSG_GET_MOBILE_LOGIN_CODE) {
            if (mCallBack != null) {
                mCallBack.onJSGetMobileLoginCode((String) msg.obj);
            }
        } else if (msg.what == MSG_IS_LOGIN) {

            if (mCallBack != null) {
                mCallBack.onJSIsLogin();
            }

        } else if (msg.what == MSG_LOGIN_OUT) {
            if (mCallBack != null) {
                mCallBack.onJSLoginOut();
            }
        } else if (msg.what == MSG_EMAIL_LOGIN) {
            if (mCallBack != null) {
                mCallBack.onJSEmailLogin((MobileLogin) (msg.obj));
            }
        } else if (msg.what == MSG_EMAIL_REGISTER) {

            if (mCallBack != null) {
                mCallBack.onJSEmailRegister((UserRegister) (msg.obj));
            }

        } else if (msg.what == MSG_EMAIL_FORGOT) {

            if (mCallBack != null) {
                mCallBack.onJSEmailForgot((String) (msg.obj));
            }

        } else {
            Tlog.e(TAG, NAME_JSI + " handleMessage unknown what:" + msg.what);
        }

    }

}
