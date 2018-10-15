package leaf.prod.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.manager.MarketcapDataManager;
import leaf.prod.app.model.WalletEntity;
import leaf.prod.app.utils.CurrencyUtil;
import leaf.prod.app.utils.SPUtils;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.model.Currency;
import leaf.prod.walletsdk.model.response.data.MarketcapResult;
import leaf.prod.walletsdk.service.LoopringService;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CurrencyActivity extends BaseActivity {

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.ll_cny)
    LinearLayout llCny;

    @BindView(R.id.ll_usd)
    LinearLayout llUsd;

    @BindView(R.id.iv_cny_check)
    ImageView ivCnyCheck;

    @BindView(R.id.iv_usd_check)
    ImageView ivUsdCheck;

    private LoopringService loopringService;

    private MarketcapDataManager marketcapDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_currency);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.set_money_type));
        title.clickLeftGoBack(getWContext());
    }

    @Override
    public void initView() {
        if (SPUtils.get(this, "coin", "CNY").equals("CNY")) {
            ivCnyCheck.setVisibility(View.VISIBLE);
            ivUsdCheck.setVisibility(View.GONE);
        } else {
            ivCnyCheck.setVisibility(View.GONE);
            ivUsdCheck.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void initData() {
        loopringService = new LoopringService();
        marketcapDataManager = MarketcapDataManager.getInstance(this);
    }

    @OnClick({R.id.ll_cny, R.id.ll_usd})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_cny:
                SPUtils.put(this, "isRecreate", true);
                SPUtils.put(this, "coin", "CNY");  //保存货币显示，
                updateWalletList(Currency.CNY);
                ivCnyCheck.setVisibility(View.VISIBLE);
                ivUsdCheck.setVisibility(View.GONE);
                break;
            case R.id.ll_usd:
                SPUtils.put(this, "isRecreate", true);
                SPUtils.put(this, "coin", "USD");
                updateWalletList(Currency.USD);
                ivCnyCheck.setVisibility(View.GONE);
                ivUsdCheck.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateWalletList(Currency currency) {
        loopringService.getMarketcap(currency.getText())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(marketcapResult -> {
                    double price = 0;
                    for (MarketcapResult.Token token : marketcapResult.getTokens()) {
                        if (token.getSymbol().equals("ETH")) {
                            price = token.getPrice();
                            break;
                        }
                    }
                    List<WalletEntity> walletEntityList = SPUtils.getWalletDataList(this, "walletlist", WalletEntity.class), result = new ArrayList();
                    for (WalletEntity walletEntity : walletEntityList) {
                        walletEntity.setAmount(walletEntity.getAmount() / marketcapDataManager.getPriceBySymbol("ETH") * price);
                        walletEntity.setAmountShow(CurrencyUtil.format(this, walletEntity.getAmount()));
                        result.add(walletEntity);
                    }
                    SPUtils.setDataList(this, "walletlist", result);
                });
    }
}
