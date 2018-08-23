package com.tomcat360.lyqb.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.tomcat360.lyqb.R;
import com.tomcat360.lyqb.utils.LyqbLogger;
import com.tomcat360.lyqb.utils.ToastUtils;
import com.tomcat360.lyqb.views.TitleView;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class ShareActivity extends BaseActivity {

    @BindView(R.id.title)
    TitleView title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_share);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initTitle() {
        title.setBTitle("Up Wallet推广");
        title.clickLeftGoBack(getWContext());
        title.setRightImageButton(R.mipmap.icon_share, new TitleView.OnRightButtonClickListener() {
            @Override
            public void onClick(View button) {
                ShareActivityPermissionsDispatcher.showWriteWithPermissionCheck(ShareActivity.this);
            }
        });
    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {

    }

    // 单个权限
    // @NeedsPermission(Manifest.permission.CAMERA)
    // 多个权限
    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showWrite() {
        uShare();
//        ToastUtils.toast("jinlaile ");
    }

    // 用户拒绝授权回调（可选）
    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showDeniedForWrite() {
        Toast.makeText(ShareActivity.this, "权限已拒绝", Toast.LENGTH_SHORT).show();
    }

    // 用户勾选了“不再提醒”时调用（可选）
    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showNeverAskForWrite() {
        Toast.makeText(ShareActivity.this, "权限已拒绝，不在提醒", Toast.LENGTH_SHORT).show();
    }


    private void uShare() {
        UMWeb umWeb = new UMWeb("https://m.zhaoyunlicai.com/weekPayNo");
        umWeb.setTitle("赵云理财喊您领周薪啦！");//标题
        umWeb.setThumb(new UMImage(ShareActivity.this, R.mipmap.icon_share));  //缩略图
        umWeb.setDescription("挑战月薪制，日薪不是梦！");//描述


        ShareAction shareAction = new ShareAction(ShareActivity.this);
        shareAction
                .setDisplayList(SHARE_MEDIA.QQ,
//                        SHARE_MEDIA.QZONE,
                        SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE,
                        SHARE_MEDIA.SINA)//传入平台
                .setCallback(umShareListener)
                .withMedia(umWeb)
                .open();


    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ShareActivityPermissionsDispatcher.onRequestPermissionsResult(this,requestCode,grantResults);

    }

    private UMShareListener umShareListener = new UMShareListener() {
        /**
         * @descrption 分享开始的回调
         * @param platform 平台类型
         */
        @Override
        public void onStart(SHARE_MEDIA platform) {

        }

        /**
         * @descrption 分享成功的回调
         * @param platform 平台类型
         */
        @Override
        public void onResult(SHARE_MEDIA platform) {
            Toast.makeText(ShareActivity.this, "成功了", Toast.LENGTH_LONG).show();
        }

        /**
         * @descrption 分享失败的回调
         * @param platform 平台类型
         * @param t 错误原因
         */
        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            if (t.getMessage().contains("2008")) {//错误码
                Toast.makeText(ShareActivity.this, "分享失败:没有安装该应用", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ShareActivity.this, "分享失败:" + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        /**
         * @descrption 分享取消的回调
         * @param platform 平台类型
         */
        @Override
        public void onCancel(SHARE_MEDIA platform) {
//            Toast.makeText(getContext(), "取消了", Toast.LENGTH_LONG).show();

        }
    };
}