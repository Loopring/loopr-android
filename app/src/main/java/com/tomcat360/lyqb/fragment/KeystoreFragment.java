package com.tomcat360.lyqb.fragment;


import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.tomcat360.lyqb.R;
import com.tomcat360.lyqb.utils.FileUtils;
import com.tomcat360.lyqb.utils.ToastUtils;

import org.json.JSONException;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 *
 */
public class KeystoreFragment extends BaseFragment {

    Unbinder unbinder;
    @BindView(R.id.tv_keystore)
    TextView tvKeystore;
    @BindView(R.id.btn_copy_keystore)
    Button btnCopyKeystore;

    public String keystore;
    public String filename;

    public final static int KEYSTORE_SUCCESS = 1;
    public final static int ERROR_ONE = 2;
    public final static int ERROR_TWO = 3;
    @SuppressLint("HandlerLeak")
    Handler handlerCreate = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case KEYSTORE_SUCCESS:
                    tvKeystore.setText(keystore);
                    break;
                case ERROR_ONE:
                    ToastUtils.toast("本地文件读取失败，请重试");
                    break;
                case ERROR_TWO:
                    ToastUtils.toast("本地文件JSON解析失败，请重试");
                    break;

            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 布局导入
        layout = inflater.inflate(R.layout.fragment_keystore, container, false);
        unbinder = ButterKnife.bind(this, layout);
        if (isAdded()) {
            filename = getArguments().getString("filename");
        }
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                     keystore = FileUtils.getKeystoreFromSD(getContext(),filename);
                     handlerCreate.sendEmptyMessage(KEYSTORE_SUCCESS);
                } catch (IOException e) {
                    handlerCreate.sendEmptyMessage(ERROR_ONE);
                    e.printStackTrace();
                } catch (JSONException e) {
                    handlerCreate.sendEmptyMessage(ERROR_TWO);
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @OnClick(R.id.btn_copy_keystore)
    public void onViewClicked() {
        // 从API11开始android推荐使用android.content.ClipboardManager
        // 为了兼容低版本我们这里使用旧版的android.text.ClipboardManager，虽然提示deprecated，但不影响使用。
        ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        // 将文本内容放到系统剪贴板里。
        cm.setText(tvKeystore.getText());
        ToastUtils.toast("复制成功");
    }


}
