/**
 * Created with IntelliJ IDEA.
 * User: kenshin wangchen@loopring.org
 * Time: 2018-09-10 下午4:16
 * Cooperation: loopring.org 路印协议基金会
 */
package com.tomcat360.lyqb.presenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.lyqb.walletsdk.model.Network;
import com.lyqb.walletsdk.model.response.data.BalanceResult;
import com.lyqb.walletsdk.model.response.data.MarketcapResult;
import com.lyqb.walletsdk.model.response.data.Token;
import com.lyqb.walletsdk.service.LoopringService;
import com.tomcat360.lyqb.R;
import com.tomcat360.lyqb.fragment.MainFragment;
import com.tomcat360.lyqb.manager.BalanceDataManager;
import com.tomcat360.lyqb.manager.MarketcapDataManager;
import com.tomcat360.lyqb.manager.TokenDataManager;
import com.tomcat360.lyqb.receiver.NetworkStateReceiver;
import com.tomcat360.lyqb.utils.CurrencyUtil;
import com.tomcat360.lyqb.utils.LyqbLogger;
import com.tomcat360.lyqb.utils.NetworkUtil;
import com.tomcat360.lyqb.utils.SPUtils;
import com.tomcat360.lyqb.utils.ToastUtils;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainFragmentPresenter extends BasePresenter<MainFragment> {

    private Map<String, BalanceResult.Asset> tokenMap = new HashMap<>();

    private TokenDataManager tokenDataManager;

    private MarketcapDataManager marketcapDataManager;

    private BalanceDataManager balanceDataManager;

    private List<BalanceResult.Asset> listAsset = new ArrayList<>(); //  返回的token列表

    private String moneyValue;

    private static LoopringService loopringService;

    private String address;

    private static Observable<MarketcapResult> marketcapObservable;

    private static Observable<BalanceResult> balanceObservable;

    public void initObservable() {
        LyqbLogger.log(getAddress());
        if (loopringService == null)
            loopringService = new LoopringService();
        Observable.zip(loopringService.getBalance(getAddress())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()), loopringService.getSupportedToken()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()), loopringService.getMarketcap(CurrencyUtil.getCurrency(context)
                        .getText())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                CombineObservable::getInstance)
                .subscribe(new Subscriber<CombineObservable>() {
                    @Override
                    public void onCompleted() {
                        view.hideLoading();
                        view.finishRefresh();
                    }

                    @Override
                    public void onError(Throwable e) {
                        handleNetworkError();
                    }

                    @Override
                    public void onNext(CombineObservable combineObservable) {
                        marketcapDataManager.setMarketcapResult(combineObservable.getMarketcapResult());
                        tokenDataManager.mergeTokens(combineObservable.getTokenList());
                        balanceDataManager.mergeAssets(combineObservable.getBalanceResult());
                        setTokenLegalPrice();
                        unsubscribe();
                    }
                });
    }

    public void initPushService() {
        if (marketcapObservable == null) {
            marketcapDataManager.getObservable().subscribe(marketcapResult -> {
                if (marketcapObservable != null) {
                    marketcapDataManager.setMarketcapResult(marketcapResult);
                    balanceDataManager.mergeAssets(balanceDataManager.getBalance());
                    setTokenLegalPrice();
                } else
                    marketcapObservable = marketcapDataManager.getObservable();
            }, error -> {
            });
        }
        if (balanceObservable == null) {
            balanceDataManager.getObservable().subscribe(balanceResult -> {
                if (balanceObservable != null) {
                    balanceDataManager.mergeAssets(balanceResult);
                    setTokenLegalPrice();
                } else {
                    balanceObservable = balanceDataManager.getBalanceObservable();
                }
            }, error -> {
            });
        }
    }

    public void initNetworkListener() {
        MainNetworkReceiver mainNetworkReceiver = MainNetworkReceiver.getInstance(this);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mainNetworkReceiver, intentFilter);
    }

    public void handleNetworkError() {
        if (NetworkUtil.getNetWorkState(context) == Network.NETWORK_NONE) {
            ToastUtils.toast(context.getResources().getString(R.string.network_error));
            view.finishRefresh();
        }
    }

    public void destroy() {
        if (marketcapObservable != null) {
            marketcapObservable.unsubscribeOn(Schedulers.io());
            marketcapObservable = null;
        }
        if (balanceObservable != null) {
            balanceObservable.unsubscribeOn(Schedulers.io());
            balanceObservable = null;
        }
    }

    public MainFragmentPresenter(MainFragment view, Context context) {
        super(view, context);
        marketcapDataManager = MarketcapDataManager.getInstance(context);
        tokenDataManager = TokenDataManager.getInstance(context);
        balanceDataManager = BalanceDataManager.getInstance(context);
    }

    private void setTokenLegalPrice() {
        LyqbLogger.log(balanceDataManager.getAssets().toString());
        //        LyqbLogger.log(tokenDataManager.getTokens().toString());
        //        LyqbLogger.log(marketcapDataManager.getMarketcapResult().toString());
        for (BalanceResult.Asset asset : balanceDataManager.getAssets()) {
            tokenMap.put(asset.getSymbol(), asset);
        }
        Collections.sort(balanceDataManager.getAssets(), (o1, o2) -> Double.compare(o2.getLegalValue(), o1.getLegalValue()));
        List<BalanceResult.Asset> listChooseAsset = new ArrayList<>();
        List<String> listChooseSymbol = SPUtils.getDataList(this.context, "choose_token");
        double amount = 0;
        for (String symbol : listChooseSymbol) {
            listChooseAsset.add(tokenMap.get(symbol));
            amount += tokenMap.get(symbol).getLegalValue();
        }
        for (BalanceResult.Asset asset : balanceDataManager.getAssets()) {
            if (!listChooseSymbol.contains(asset.getSymbol()) && asset.getLegalValue() != 0) {
                listChooseAsset.add(asset);
                amount += asset.getLegalValue();
            }
        }
        moneyValue = CurrencyUtil.format(context, amount);
        SPUtils.put(this.context, "amount", moneyValue);
        listAsset = listChooseAsset;
        view.getmAdapter().setNewData(listChooseAsset);
        view.setWalletCount(moneyValue);
        view.getmAdapter().notifyDataSetChanged();
    }

    public List<BalanceResult.Asset> getListAsset() {
        return listAsset;
    }

    public String getAddress() {
        if (address == null)
            address = (String) SPUtils.get(Objects.requireNonNull(context), "address", "");
        return address;
    }

    public String getMoneyValue() {
        return moneyValue;
    }

    private static class CombineObservable {

        private static CombineObservable combineObservable;

        private BalanceResult balanceResult;

        private List<Token> tokenList;

        private MarketcapResult marketcapResult;

        public static CombineObservable getInstance(BalanceResult balanceResult, List<Token> tokenList, MarketcapResult marketcapResult) {
            if (combineObservable == null) {
                return new CombineObservable(balanceResult, tokenList, marketcapResult);
            }
            combineObservable.setBalanceResult(balanceResult);
            combineObservable.setMarketcapResult(marketcapResult);
            combineObservable.setTokenList(tokenList);
            return combineObservable;
        }

        private CombineObservable() {
        }

        private CombineObservable(BalanceResult balanceResult, List<Token> tokenList, MarketcapResult marketcapResult) {
            this.balanceResult = balanceResult;
            this.tokenList = tokenList;
            this.marketcapResult = marketcapResult;
        }

        public BalanceResult getBalanceResult() {
            return balanceResult;
        }

        public void setBalanceResult(BalanceResult balanceResult) {
            this.balanceResult = balanceResult;
        }

        public List<Token> getTokenList() {
            return tokenList;
        }

        public void setTokenList(List<Token> tokenList) {
            this.tokenList = tokenList;
        }

        public MarketcapResult getMarketcapResult() {
            return marketcapResult;
        }

        public void setMarketcapResult(MarketcapResult marketcapResult) {
            this.marketcapResult = marketcapResult;
        }
    }

    private static class MainNetworkReceiver extends NetworkStateReceiver {

        private MainFragmentPresenter presenter;

        private static boolean first = true;

        private static MainNetworkReceiver mainNetworkReceiver;

        public static MainNetworkReceiver getInstance(MainFragmentPresenter presenter) {
            if (mainNetworkReceiver == null) {
                return new MainNetworkReceiver(presenter);
            }
            return mainNetworkReceiver;
        }

        private MainNetworkReceiver(MainFragmentPresenter presenter) {
            this.presenter = presenter;
        }

        @Override
        public void doNetWorkNone() {
            ToastUtils.toast(presenter.context.getResources().getString(R.string.network_error));
        }

        @Override
        public void doNetWorkWifi() {
            if (first) {
                first = false;
                return;
            }
            presenter.initObservable();
        }

        @Override
        public void doNetWorkMobile() {
            doNetWorkWifi();
        }
    }
}
