package com.lyqb.walletsdk.listener;

import android.util.Log;

import com.google.gson.JsonElement;
import com.lyqb.walletsdk.Default;
import com.lyqb.walletsdk.model.request.param.BalanceParam;
import com.lyqb.walletsdk.model.response.data.BalanceResult;

import rx.Observable;
import rx.subjects.PublishSubject;

public class BalanceListener extends AbstractListener<BalanceResult, BalanceParam> {

    public static final String TAG = "balance";

    private final PublishSubject<BalanceResult> subject = PublishSubject.create();

    @Override
    protected void registerEventHandler() {
        socket.on("balance_res", objects -> {
            JsonElement element = extractPayload(objects);
            BalanceResult balanceResult = gson.fromJson(element, BalanceResult.class);
            subject.onNext(balanceResult);
        });
        socket.on("balance_end", data -> {
//            System.out.println(Arrays.toString(data));
            subject.onCompleted();
        });
    }

    @Override
    public Observable<BalanceResult> start() {
        return subject;
    }

    @Override
    public void stop() {
        socket.emit("balance_end", null, args -> subject.onCompleted());
    }

    @Override
    public void send(BalanceParam param) {
        String json = gson.toJson(param);
        socket.emit("balance_req", json);
    }

    public void queryByOwner(String owner) {
        BalanceParam param = BalanceParam.builder()
                .owner(owner)
                .delegateAddress(Default.DELEGATE_ADDRESS)
                .build();
        send(param);
    }
}
