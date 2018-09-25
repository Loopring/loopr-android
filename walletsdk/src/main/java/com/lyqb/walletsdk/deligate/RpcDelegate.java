package com.lyqb.walletsdk.deligate;

import java.util.List;
import java.util.Map;

import com.lyqb.walletsdk.Default;
import com.lyqb.walletsdk.SDK;
import com.lyqb.walletsdk.model.Partner;
import com.lyqb.walletsdk.model.request.RequestWrapper;
import com.lyqb.walletsdk.model.response.ResponseWrapper;
import com.lyqb.walletsdk.model.response.data.BalanceResult;
import com.lyqb.walletsdk.model.response.data.MarketcapResult;
import com.lyqb.walletsdk.model.response.data.Token;
import com.lyqb.walletsdk.model.response.data.TransactionPageWrapper;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface RpcDelegate {

    static RpcDelegate getService(String url) {
        OkHttpClient okHttpClient = SDK.getOkHttpClient();
        Retrofit retrofitClient = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .build();
        return retrofitClient.create(RpcDelegate.class);
    }

    @POST(Default.RELAY_RPC_URL)
    Observable<Map> send(@Body RequestWrapper request);

    @POST(Default.RELAY_RPC_URL)
    Observable<ResponseWrapper<BalanceResult>> getBalance(@Body RequestWrapper request);

    @POST(Default.RELAY_RPC_URL)
    Observable<ResponseWrapper<TransactionPageWrapper>> getTransactions(@Body RequestWrapper request);

    @POST(Default.RELAY_RPC_URL)
    Observable<ResponseWrapper<String>> getNonce(@Body RequestWrapper request);

    @POST(Default.RELAY_RPC_URL)
    Observable<ResponseWrapper<String>> estimateGasPrice(@Body RequestWrapper request);

    @POST(Default.RELAY_RPC_URL)
    Observable<ResponseWrapper<String>> unlockWallet(@Body RequestWrapper request);

    @POST(Default.RELAY_RPC_URL)
    Observable<ResponseWrapper<List<Token>>> getSupportedTokens(@Body RequestWrapper request);

    @POST(Default.RELAY_RPC_URL)
    Observable<ResponseWrapper<MarketcapResult>> getMarketcap(@Body RequestWrapper request);

    @POST(Default.RELAY_RPC_URL)
    Observable<ResponseWrapper<String>> notifyTransactionSubmitted(@Body RequestWrapper request);

    @POST(Default.RELAY_RPC_URL)
    Observable<ResponseWrapper<Partner>> createPartner(@Body RequestWrapper request);

    @POST(Default.PARTNER_ACTIVATE)
    Observable<ResponseWrapper<Partner>> activateInvitation();
}
