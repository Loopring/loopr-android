/**
 * Created with IntelliJ IDEA.
 * User: kenshin wangchen@loopring.org
 * Time: 2018-09-10 下午4:16
 * Cooperation: loopring.org 路印协议基金会
 */
package com.tomcat360.lyqb.presenter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android.content.Context;

import com.lyqb.walletsdk.listener.BalanceListener;
import com.lyqb.walletsdk.model.response.data.BalanceResult;
import com.lyqb.walletsdk.model.response.data.MarketcapResult;
import com.lyqb.walletsdk.model.response.data.Token;
import com.lyqb.walletsdk.util.UnitConverter;
import com.tomcat360.lyqb.fragment.MainFragment;
import com.tomcat360.lyqb.manager.MarketcapDataManager;
import com.tomcat360.lyqb.manager.TokenDataManager;
import com.tomcat360.lyqb.utils.CurrencyUtil;
import com.tomcat360.lyqb.utils.LyqbLogger;
import com.tomcat360.lyqb.utils.SPUtils;
import com.tomcat360.lyqb.view.APP;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainFragmentPresenter extends BasePresenter<MainFragment> {

    private Map<String, BalanceResult.Asset> tokenMap = new HashMap<>();

    private MarketcapDataManager marketcapDataManager;

    private TokenDataManager tokenDataManager;

    private BalanceListener balanceListener = new BalanceListener();

    private List<BalanceResult.Asset> listAsset = new ArrayList<>(); //  返回的token列表

    private List<Token> tokenList = new ArrayList<>();

    private MarketcapResult marketcapResult;

    private String moneyValue;

    private static Observable<BalanceResult> balanceObservable;

    private static Observable<MarketcapResult> marketcapObservable;

    private static Observable<CombineObservable> refreshObservable;

    private static Observable<CombineObservable> createObservable;

    private String address;

    public void initObservable() {
        listAsset.clear();
        marketcapResult = null;
        if (balanceObservable == null) {
            balanceObservable = balanceListener.start()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            balanceObservable.subscribe(balanceResult -> {
                if (!tokenList.isEmpty() && marketcapResult != null && balanceResult.getTokens() != null) {
                    setTokenLegalPrice(balanceResult.getTokens(), tokenList, marketcapResult);
                }
            });
            balanceListener.queryByOwner(address);
        } else {
            balanceListener.queryByOwner(address);
        }
        if (marketcapObservable == null) {
            marketcapObservable = marketcapDataManager.getObservable();
            marketcapObservable.subscribe(marketcapResult -> {
                if (!tokenList.isEmpty() && !listAsset.isEmpty()) {
                    setTokenLegalPrice(listAsset, tokenList, marketcapResult);
                }
            });
        } else {
            marketcapDataManager.refresh();
        }
        if (createObservable == null) {
            createObservable = Observable.zip(balanceObservable, tokenDataManager.getTokenObservable(), marketcapDataManager
                    .getObservable(), (balanceResult, tokens, marketcap) ->
                    CombineObservable.getInstance(balanceResult.getTokens(), tokens, marketcap));
            createObservable.subscribe(combineObservable -> {
                tokenList = combineObservable.getTokenList();
                marketcapResult = combineObservable.getMarketcapResult();
                setTokenLegalPrice(combineObservable.getAssetList(), combineObservable.getTokenList(), combineObservable
                        .getMarketcapResult());
                view.hideLoading();
            }, error -> {
                view.hideLoading();
            });
        } else if (refreshObservable == null) {
            refreshObservable = Observable.zip(balanceObservable, marketcapDataManager.getObservable(), (balanceResult, marketcapResult) -> CombineObservable
                    .getInstance(balanceResult.getTokens(), tokenList, marketcapResult));
            refreshObservable.subscribe(combineObservable -> {
                if (!tokenList.isEmpty()) {
                    marketcapResult = combineObservable.getMarketcapResult();
                    setTokenLegalPrice(combineObservable.getAssetList(), combineObservable.getTokenList(), combineObservable
                            .getMarketcapResult());
                }
                view.hideLoading();
            }, error -> {
                view.hideLoading();
            });
        }
    }

    public void destroy() {
        tokenList.clear();
        listAsset.clear();
        marketcapResult = null;
        createObservable = null;
        refreshObservable = null;
        balanceObservable = null;
    }

    public MainFragmentPresenter(MainFragment view, Context context) {
        super(view, context);
        marketcapDataManager = MarketcapDataManager.getInstance(context);
        tokenDataManager = TokenDataManager.getInstance(context);
    }

    private void setTokenLegalPrice(List<BalanceResult.Asset> assetList, List<Token> tokenList, MarketcapResult marketcapResult) {
        LyqbLogger.log(assetList.toString());
        LyqbLogger.log(tokenList.toString());
        LyqbLogger.log(marketcapResult.toString());
        TokenDataManager manager = TokenDataManager.getInstance(context);
        for (BalanceResult.Asset asset : assetList) {
            Token supportedToken = manager.getTokenBySymbol(asset.getSymbol());
            Double legalPrice = getLegalPriceBySymbol(marketcapResult, asset.getSymbol());
            if (asset.getSymbol().equals("ETH"))
                asset.setValue(UnitConverter.weiToEth(asset.getBalance().toPlainString()).doubleValue());
            if (!asset.getBalance().equals(BigDecimal.ZERO) && supportedToken != null && legalPrice != 0) {
                double value = asset.getBalance().divide(supportedToken.getDecimals()).doubleValue();
                asset.setValue(Double.parseDouble(String.format("%." + (String.valueOf(legalPrice.intValue())
                        .length() + 2) + "f", value)));
            }
            if (legalPrice != 0 && asset.getValue() != 0) {
                asset.setLegalValue(legalPrice * asset.getValue());
                asset.setLegalStr(CurrencyUtil.format(context, asset.getLegalValue()));
            }
            tokenMap.put(asset.getSymbol(), asset);
        }
        Collections.sort(assetList, (o1, o2) -> Double.compare(o2.getLegalValue(), o1.getLegalValue()));
        APP.setListAsset(assetList);
        List<BalanceResult.Asset> listChooseAsset = new ArrayList<>();
        List<String> listChooseSymbol = SPUtils.getDataList(this.context, "choose_token");
        double amount = 0;
        for (String symbol : listChooseSymbol) {
            listChooseAsset.add(tokenMap.get(symbol));
            amount += tokenMap.get(symbol).getLegalValue();
        }
        for (BalanceResult.Asset asset : assetList) {
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

        private List<BalanceResult.Asset> assetList;

        private List<Token> tokenList;

        private MarketcapResult marketcapResult;

        public static CombineObservable getInstance(List<BalanceResult.Asset> assetList, List<Token> tokenList, MarketcapResult marketcapResult) {
            if (combineObservable == null) {
                return new CombineObservable(assetList, tokenList, marketcapResult);
            }
            combineObservable.setAssetList(assetList);
            combineObservable.setMarketcapResult(marketcapResult);
            combineObservable.setTokenList(tokenList);
            return combineObservable;
        }

        private CombineObservable() {
        }

        public CombineObservable(List<BalanceResult.Asset> balanceResult, List<Token> tokenList, MarketcapResult marketcapResult) {
            this.assetList = balanceResult;
            this.tokenList = tokenList;
            this.marketcapResult = marketcapResult;
        }

        public List<BalanceResult.Asset> getAssetList() {
            return assetList;
        }

        public void setAssetList(List<BalanceResult.Asset> assetList) {
            this.assetList = assetList;
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
}
