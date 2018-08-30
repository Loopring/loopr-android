package com.tomcat360.lyqb.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lyqb.walletsdk.listener.BalanceListener;
import com.lyqb.walletsdk.model.response.data.BalanceResult;
import com.lyqb.walletsdk.model.response.data.SupportedToken;
import com.tomcat360.lyqb.R;
import com.tomcat360.lyqb.activity.ActivityScanerCode;
import com.tomcat360.lyqb.activity.ReceiveActivity;
import com.tomcat360.lyqb.activity.SendActivity;
import com.tomcat360.lyqb.activity.TokenListActivity;
import com.tomcat360.lyqb.activity.WalletDetailActivity;
import com.tomcat360.lyqb.adapter.MainWalletAdapter;
import com.tomcat360.lyqb.net.ResponseFunc;
import com.tomcat360.lyqb.net.ResponseSupportFunc;
import com.tomcat360.lyqb.utils.ButtonClickUtil;
import com.tomcat360.lyqb.utils.LyqbLogger;
import com.tomcat360.lyqb.utils.NumberUtils;
import com.tomcat360.lyqb.utils.SPUtils;
import com.tomcat360.lyqb.utils.ToastUtils;
import com.tomcat360.lyqb.view.APP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 *
 */
public class MainFragment extends BaseFragment {

    Unbinder unbinder;
    @BindView(R.id.title_text)
    TextView titleText;
    @BindView(R.id.right_btn)
    ImageView rightBtn;
    @BindView(R.id.wallet_count)
    TextView walletCount;
    @BindView(R.id.wallet_address)
    TextView walletAddress;
    @BindView(R.id.wallet_qrcode)
    ImageView walletQrcode;
    @BindView(R.id.wallet_scan_tv)
    TextView walletScanTv;
    @BindView(R.id.wallet_receive_tv)
    TextView walletReceiveTv;
    @BindView(R.id.wallet_send_tv)
    TextView walletSendTv;
    @BindView(R.id.wallet_trades_tv)
    TextView walletTradesTv;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.ll_scan)
    LinearLayout llScan;
    @BindView(R.id.ll_receive)
    LinearLayout llReceive;
    @BindView(R.id.ll_send)
    LinearLayout llSend;
    @BindView(R.id.ll_trade)
    LinearLayout llTrade;
    @BindView(R.id.menu_scan)
    LinearLayout menuScan;
    @BindView(R.id.menu_add_assets)
    LinearLayout menuAddAssets;
    @BindView(R.id.menu_wallet)
    LinearLayout menuWallet;
    @BindView(R.id.menu_transaction)
    LinearLayout menuTransaction;
    @BindView(R.id.ll_menu)
    LinearLayout llMenu;

    private MainWalletAdapter mAdapter;

    private boolean showMenu = false;  //判断menu是否显示
    private static int REQUEST_CODE = 1;  //二维码扫一扫code
    private double moneyValue = 0;  //钱包总金额
    private BalanceListener balanceListener = new BalanceListener();

    private boolean flag = true; //第一次进入
    private String address;
    private List<BalanceResult.Token> listToken; //  返回的token列表
    private List<BalanceResult.Token> listChooseToken = new ArrayList<>(); //  选中的token列表
    private List<String> listChooseSymbol; //  选择展示的token名字symbol

    public final static int BALANCE_SUCCESS = 1;
    @SuppressLint("HandlerLeak")
    Handler handlerBalance = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BALANCE_SUCCESS:


                default:

                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 布局导入
        layout = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initPresenter() {

        address = (String) SPUtils.get(getContext(), "address", "");

    }

    @Override
    protected void initView() {

        walletAddress.setText(address);
    }

    @Override
    protected void initData() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new MainWalletAdapter(R.layout.adapter_item_wallet, null);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (showMenu) {
                    llMenu.setVisibility(View.GONE);
                    showMenu = false;
                } else {
                    getOperation().forward(WalletDetailActivity.class);
                }
            }
        });

        initToken();

    }

    private void initToken() {
        LyqbLogger.log(address);

        Observable<BalanceResult> observable = balanceListener.start();
        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BalanceResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BalanceResult balanceResult) {
                        LyqbLogger.log(balanceResult.getTokens().toString());
                        if (balanceResult.getTokens() != null) {
                            listToken = balanceResult.getTokens();
                            Collections.sort(listToken,new Comparator<BalanceResult.Token>(){

                                /**
                                 * 对集合进行排列，在token列表中按字母顺序排列
                                 * 返回负数表示：o1 小于o2，
                                 * 返回0 表示：o1和o2相等，
                                 * 返回正数表示：o1大于o2。
                                 */
                                public int compare(BalanceResult.Token o1, BalanceResult.Token o2) {

                                    if(o1.getSymbol().compareTo(o2.getSymbol()) > 0){
                                        return 1;
                                    }
                                    if(o1.getSymbol().compareTo(o2.getSymbol()) == 0){
                                        return 0;
                                    }
                                    return -1;
                                }
                            });
                            APP.setListToken(listToken);
                            listChooseToken.clear();
                            listChooseSymbol = SPUtils.getDataList(getContext(), "choose_token");
                            for (int i = 0 ;i<listToken.size();i++){
                                moneyValue = listToken.get(i).getBalance().doubleValue();
                                if (listChooseSymbol.contains(listToken.get(i).getSymbol())){
                                    if (listToken.get(i).getSymbol().equals("ETH")){
                                        listChooseToken.add(0,listToken.get(i));
                                    }else if (listToken.get(i).getSymbol().equals("WETH")){
                                        if (listChooseSymbol.contains("ETH")){
                                            listChooseToken.add(1,listToken.get(i));
                                        } else {
                                            listChooseToken.add(0,listToken.get(i));
                                        }
                                    }else {
                                        listChooseToken.add(listToken.get(i));
                                    }
//                                    LyqbLogger.log(listChooseToken.toString());
                                }
                            }
                            walletCount.setText(SPUtils.get(getContext(),"coin","¥")+NumberUtils.numberformat(moneyValue));
                            mAdapter.setNewData(listChooseToken);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
        balanceListener.queryByOwner(address);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (flag){
            flag = false;
        }else {
            if (listToken != null) {
                listChooseToken.clear();
                listChooseSymbol = SPUtils.getDataList(getContext(), "choose_token");
                for (int i = 0; i < listToken.size(); i++) {
                    if (listChooseSymbol.contains(listToken.get(i).getSymbol())) {
                        if (listToken.get(i).getSymbol().equals("ETH")) {
                            listChooseToken.add(0, listToken.get(i));
                        } else if (listToken.get(i).getSymbol().equals("WETH")) {
                            if (listChooseSymbol.contains("ETH")) {
                                listChooseToken.add(1, listToken.get(i));
                            } else {
                                listChooseToken.add(0, listToken.get(i));
                            }
                        } else {
                            listChooseToken.add(listToken.get(i));
                        }
                    }
                }
                mAdapter.setNewData(listChooseToken);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }


    @OnClick({R.id.ll_scan, R.id.ll_receive, R.id.ll_send, R.id.ll_trade, R.id.menu_scan, R.id.menu_add_assets, R.id.menu_wallet, R.id.menu_transaction, R.id.right_btn, R.id.ll_main})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_scan:  //scan 按钮
                if (showMenu) {
                    llMenu.setVisibility(View.GONE);
                    showMenu = false;
                } else {
                    if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
                        startActivityForResult(new Intent(getContext(), ActivityScanerCode.class), REQUEST_CODE);
                    }
                }
                break;
            case R.id.ll_receive://receive 按钮
                if (showMenu) {
                    llMenu.setVisibility(View.GONE);
                    showMenu = false;
                } else {
                    getOperation().forward(ReceiveActivity.class);

                }
                break;
            case R.id.ll_send://send转出 按钮
                if (showMenu) {
                    llMenu.setVisibility(View.GONE);
                    showMenu = false;
                } else {
                    getOperation().forward(SendActivity.class);

                }
                break;
            case R.id.ll_trade://trade 按钮
                break;
            case R.id.right_btn:  //右上角添加按钮
                llMenu.setVisibility(View.VISIBLE);
                showMenu = true;
                break;
            case R.id.ll_main://主布局页面，主要实现点击关闭menu页面
                llMenu.setVisibility(View.GONE);
                showMenu = false;
                break;
            case R.id.menu_scan:
                if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
                    startActivityForResult(new Intent(getContext(), ActivityScanerCode.class), REQUEST_CODE);
                }
                break;
            case R.id.menu_add_assets:
                getOperation().forward(TokenListActivity.class);
                break;
            case R.id.menu_wallet:
                break;
            case R.id.menu_transaction:
                break;

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {

                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                String result = bundle.getString("result");
                LyqbLogger.log(result);
            }
        }
    }
}
