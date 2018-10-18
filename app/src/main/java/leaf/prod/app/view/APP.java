package leaf.prod.app.view;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;

import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;
import com.vondear.rxtool.RxTool;

import leaf.prod.app.utils.AndroidUtils;
import leaf.prod.app.utils.LanguageUtil;
import leaf.prod.app.utils.SPUtils;
import leaf.prod.walletsdk.SDK;

public class APP extends Application {

    private static APP mInstance;

    String appVersion;

    static {
        //        PlatformConfig.setWeixin("wx8303b66b243b4aa3", "c96e252e26a7f62f833d7c545ab3d098");
        PlatformConfig.setWeixin("wx408da040efe1505d", "5380342e5cb9ca73b5e9b3ebe29600a1");
        PlatformConfig.setQQZone("1106526050", "ldwhPFfveaNiOVUb");
        //        PlatformConfig.setQQZone("100424468", "c7394704798a158208a74ab60104f0ba");
        PlatformConfig.setSinaWeibo("799587641", "09dfbfdc9cada2e8db0813bd298078c6", "http://sns.whalecloud.com");
    }

    public static APP getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        RxTool.init(this);
        appVersion = AndroidUtils.getVersionName(this);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        mInstance = this;
        /*
         * 通过language的状态来判断是否设置了显示英文还是中文，1为英文，2为中文,0为未设置，显示系统默认
         */
        if ((int) SPUtils.get(this, "language", 0) == 1) {
            LanguageUtil.changeLanguage(this, "en");
        } else if ((int) SPUtils.get(this, "language", 0) == 2) {
            LanguageUtil.changeLanguage(this, "zh");
        }
        /**
         * umeng分享
         * */
        UMShareAPI.get(this);
        /**
         * umeng统计
         */
        // TODO: 2018/10/17 应用企业化, 申请secretkey，否则不能推送
        UMConfigure.init(this, "5bc69f03f1f5569916000271", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "l4pdsyhzabhj5bipej3iimmw6ospj5ux");
        /**
         * umeng推送
         */
//        PushAgent.getInstance(this).register(new IUmengRegisterCallback() {
//            @Override
//            public void onSuccess(String s) {
//                LyqbLogger.log("umeng push: " + s);
//            }
//
//            @Override
//            public void onFailure(String s, String s1) {
//            }
//        });
        Config.DEBUG = true;
        SDK.initSDK();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public boolean hasLogin() {
        boolean flag = "1".equals(SPUtils.get(getApplicationContext(), "isLogin", "0"));
        return flag;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        /**
         * 设置 当手机字体大小变化时，APP重新设置为默认字体大小，防止不适配
         */
        if (newConfig.fontScale != 1) {
            getResources();
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res.getConfiguration().fontScale != 1) {//非默认值
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();//设置默认
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }
}
