/**
 * Created with IntelliJ IDEA.
 * User: kenshin wangchen@loopring.org
 * Time: 2018-09-10 下午4:16
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.app.presenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import leaf.prod.app.R;
import leaf.prod.app.fragment.MainFragment;
import leaf.prod.app.manager.BalanceDataManager;
import leaf.prod.app.manager.MarketcapDataManager;
import leaf.prod.app.manager.TokenDataManager;
import leaf.prod.app.receiver.NetworkStateReceiver;
import leaf.prod.app.utils.CurrencyUtil;
import leaf.prod.app.utils.LyqbLogger;
import leaf.prod.app.utils.NetworkUtil;
import leaf.prod.app.utils.SPUtils;
import leaf.prod.app.utils.ToastUtils;
import leaf.prod.app.utils.WalletUtil;
import leaf.prod.walletsdk.model.Network;
import leaf.prod.walletsdk.model.response.data.BalanceResult;
import leaf.prod.walletsdk.model.response.data.MarketcapResult;
import leaf.prod.walletsdk.model.response.data.Token;
import leaf.prod.walletsdk.service.LoopringService;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainFragmentPresenter extends BasePresenter<MainFragment> {

    private static LoopringService loopringService;

    private static Observable<MarketcapResult> marketcapObservable;

    private static Observable<BalanceResult> balanceObservable;

    private Map<String, BalanceResult.Asset> tokenMap = new HashMap<>();

    private TokenDataManager tokenDataManager;

    private MarketcapDataManager marketcapDataManager;

    private BalanceDataManager balanceDataManager;

    private List<BalanceResult.Asset> listAsset = new ArrayList<>(); //  返回的token列表

    private String moneyValue;

    private String address;

    private MainNetworkReceiver mainNetworkReceiver;

    public MainFragmentPresenter(MainFragment view, Context context) {
        super(view, context);
        marketcapDataManager = MarketcapDataManager.getInstance(context);
        tokenDataManager = TokenDataManager.getInstance(context);
        balanceDataManager = BalanceDataManager.getInstance(context);
    }

    public void initObservable() {
        LyqbLogger.log(getAddress());
        if (loopringService == null)
            loopringService = new LoopringService();
        Observable.zip(loopringService.getBalance(getAddress())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()), loopringService.getCustomToken(getAddress())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()), loopringService.getMarketcap(CurrencyUtil.getCurrency(context)
                        .getText())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                CombineObservable::getInstance)
                .subscribe(new Subscriber<CombineObservable>() {
                    @Override
                    public void onCompleted() {
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
                    view.finishRefresh();
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
                    view.finishRefresh();
                } else {
                    balanceObservable = balanceDataManager.getBalanceObservable();
                }
            }, error -> {
            });
        }
    }

    public void initNetworkListener() {
        mainNetworkReceiver = MainNetworkReceiver.getInstance(this);
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
        if (mainNetworkReceiver != null) {
            context.unregisterReceiver(mainNetworkReceiver);
        }
    }

    private void setTokenLegalPrice() {
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
        if (address == null) {
            address = (String) SPUtils.get(Objects.requireNonNull(context), "address", "");
        }
        return address;
    }

    public String getWalletName() {
        return WalletUtil.getCurrentWallet(context).getWalletname();
    }

    public String getMoneyValue() {
        return moneyValue;
    }

    private static class CombineObservable {

        private static CombineObservable combineObservable;

        private BalanceResult balanceResult;

        private List<Token> tokenList;

        private MarketcapResult marketcapResult;

        private CombineObservable() {
        }

        private CombineObservable(BalanceResult balanceResult, List<Token> tokenList, MarketcapResult marketcapResult) {
            this.balanceResult = balanceResult;
            this.tokenList = tokenList;
            this.marketcapResult = marketcapResult;
        }

        public static CombineObservable getInstance(BalanceResult balanceResult, List<Token> tokenList, MarketcapResult marketcapResult) {
            if (combineObservable == null) {
                return new CombineObservable(balanceResult, tokenList, marketcapResult);
            }
            combineObservable.setBalanceResult(balanceResult);
            combineObservable.setMarketcapResult(marketcapResult);
            combineObservable.setTokenList(tokenList);
            return combineObservable;
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

        private static boolean first = true;

        private static MainNetworkReceiver mainNetworkReceiver;

        private MainFragmentPresenter presenter;

        private MainNetworkReceiver(MainFragmentPresenter presenter) {
            this.presenter = presenter;
        }

        public static MainNetworkReceiver getInstance(MainFragmentPresenter presenter) {
            if (mainNetworkReceiver == null) {
                return new MainNetworkReceiver(presenter);
            }
            return mainNetworkReceiver;
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
