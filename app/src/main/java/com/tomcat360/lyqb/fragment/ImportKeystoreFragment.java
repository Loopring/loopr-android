package com.tomcat360.lyqb.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lyqb.walletsdk.WalletHelper;
import com.lyqb.walletsdk.exception.IllegalCredentialException;
import com.lyqb.walletsdk.exception.KeystoreSaveException;
import com.lyqb.walletsdk.service.LooprHttpService;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.tomcat360.lyqb.R;
import com.tomcat360.lyqb.activity.MainActivity;
import com.tomcat360.lyqb.model.eventbusData.KeystoreData;
import com.tomcat360.lyqb.net.G;
import com.tomcat360.lyqb.utils.ButtonClickUtil;
import com.tomcat360.lyqb.utils.DialogUtil;
import com.tomcat360.lyqb.utils.FileUtils;
import com.tomcat360.lyqb.utils.LyqbLogger;
import com.tomcat360.lyqb.utils.SPUtils;
import com.tomcat360.lyqb.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;


/**
 *
 */
public class ImportKeystoreFragment extends BaseFragment {

    Unbinder unbinder;
    @BindView(R.id.et_keystore)
    MaterialEditText etKeystore;
    @BindView(R.id.et_password)
    MaterialEditText etPassword;
    @BindView(R.id.btn_unlock)
    Button btnUnlock;

    private String address;//钱包地址
    private LooprHttpService looprHttpService;

    public final static int MNEMONIC_SUCCESS = 1;
    public final static int CREATE_SUCCESS = 2;
    @SuppressLint("HandlerLeak")
    Handler handlerCreate = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MNEMONIC_SUCCESS:
                    hideProgress();
                    Bundle bundle = msg.getData();
                    String filename = (String) bundle.get("filename");
                    LyqbLogger.log(filename);
//                    getAddress();
                    break;
                case CREATE_SUCCESS:  //获取keystore中的address成功后，调用解锁钱包方法（unlockWallet）
                    LyqbLogger.log(address);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            LyqbLogger.log("22222222"+address);
                            looprHttpService.unlockWallet(address)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Subscriber<String>() {
                                        @Override
                                        public void onCompleted() {
                                            hideProgress();

                                            DialogUtil.showWalletCreateResultDialog(getContext(), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    DialogUtil.dialog.dismiss();
//                                                    AppManager.finishAll();
                                                    getOperation().forward(MainActivity.class);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            ToastUtils.toast("创建失败，请重试");
                                            hideProgress();
                                        }

                                        @Override
                                        public void onNext(String s) {
                                        }
                                    });
                        }
                    }).start();
                    break;
            }
        }
    };

    public void getAddress() {
        new Thread() {
            @Override
            public void run() {
                Message msg = handlerCreate.obtainMessage();
                try {
                    address = FileUtils.getFileFromSD(getContext());
                } catch (IOException e) {
                    hideProgress();
                    ToastUtils.toast("本地文件读取失败，请重试");
                    e.printStackTrace();
                } catch (JSONException e) {
                    hideProgress();
                    ToastUtils.toast("本地文件JSON解析失败，请重试");
                    e.printStackTrace();
                }
                msg.obj = address;
                msg.what = CREATE_SUCCESS;
                handlerCreate.sendMessage(msg);
            }
        }.start();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 布局导入
        layout = inflater.inflate(R.layout.fragment_import_keystore, container, false);
        unbinder = ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    protected void initPresenter() {

    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        looprHttpService = new LooprHttpService(G.RELAY_URL);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(KeystoreData event) {
        /**
         * 将扫描的结果存到输入框中
         */
        etKeystore.setText(event.getKeystory());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btn_unlock)
    public void onViewClicked() {
        if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
            if (TextUtils.isEmpty(etKeystore.getText().toString())) {
                ToastUtils.toast("请输入keystore文件");
                return;
            }
            if (etPassword.getText().toString().length() < 6) {
                ToastUtils.toast("请输入6位以上密码");
                return;
            }
            if (TextUtils.isEmpty(etPassword.getText().toString())) {
                ToastUtils.toast("请输入keystore密码");
                return;
            }
            unlockWallet();
        }
    }

    /**
     * 生成钱包
     */
    private void unlockWallet() {
        showProgress("加载中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String fileName = null;
                try {
                    fileName = WalletHelper.importFromKeystore(etKeystore.getText().toString(),etPassword.getText().toString(), FileUtils.getKeyStoreLocation());
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("filename", fileName);
                    SPUtils.put(getContext(),"filename",fileName);
                    LyqbLogger.log(fileName);
                    msg.setData(bundle);
                    msg.what = MNEMONIC_SUCCESS;
                    handlerCreate.sendMessage(msg);
                } catch (KeystoreSaveException e) {
                    ToastUtils.toast("钱包创建失败");
                    hideProgress();
                    e.printStackTrace();
                } catch (IllegalCredentialException e) {
                    ToastUtils.toast("身份验证失败");
                    hideProgress();
                    e.printStackTrace();
                }

            }
        }).start();
    }


}
