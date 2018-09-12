package com.tomcat360.lyqb.presenter;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;

import com.lyqb.walletsdk.model.response.data.MarketcapResult;
import com.lyqb.walletsdk.model.response.data.Token;
import com.tomcat360.lyqb.service.DataManager;

public class BasePresenter<V, T extends BroadcastReceiver> {

    protected V view;

    protected T broacastReceiver;

    protected Context context;

    protected DataManager dataManager;

    public BasePresenter(V view, Context context) {
        this.attachView(view);
        this.context = context;
        this.dataManager = DataManager.getInstance(context);
    }

    public BasePresenter(V view, Context context, T broacastReceiver) {
        this.attachView(view);
        this.context = context;
        this.broacastReceiver = broacastReceiver;
        this.dataManager = DataManager.getInstance(context, broacastReceiver);
    }

    public void attachView(V view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public boolean isViewAttached() {
        return view != null;
    }

    public Double getLegalPriceBySymbol(String symbol) {
        Double result = null;
        List<MarketcapResult.Token> tokens = dataManager.getMarketcapResult().getTokens();
        for (MarketcapResult.Token token : tokens) {
            if (token.getSymbol().equalsIgnoreCase(symbol)) {
                result = token.getPrice();
            }
        }
        return result;
    }

    public Token getTokenBySymbol(String symbol) {
        Token result = null;
        for (Token token : dataManager.getTokens()) {
            if (token.getSymbol().equalsIgnoreCase(symbol)) {
                result = token;
            }
        }
        return result;
    }

    public Token getTokenByProtocol(String protocol) {
        Token result = null;
        for (Token token : dataManager.getTokens()) {
            if (token.getProtocol().equalsIgnoreCase(protocol)) {
                result = token;
            }
        }
        return result;
    }

    public void destroy() {
        if (broacastReceiver != null)
            dataManager.removeBroadcast(broacastReceiver);
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
