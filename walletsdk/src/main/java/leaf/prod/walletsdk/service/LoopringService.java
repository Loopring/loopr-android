package leaf.prod.walletsdk.service;

import java.util.List;

import org.web3j.crypto.RawTransaction;
import org.web3j.utils.Numeric;
import com.google.common.collect.Maps;

import leaf.prod.walletsdk.Default;
import leaf.prod.walletsdk.SDK;
import leaf.prod.walletsdk.deligate.RpcDelegate;
import leaf.prod.walletsdk.model.CancelOrder;
import leaf.prod.walletsdk.model.Partner;
import leaf.prod.walletsdk.model.TransactionSignature;
import leaf.prod.walletsdk.model.request.RequestWrapper;
import leaf.prod.walletsdk.model.request.param.AddTokenParam;
import leaf.prod.walletsdk.model.request.param.BalanceParam;
import leaf.prod.walletsdk.model.request.param.CancelOrderParam;
import leaf.prod.walletsdk.model.request.param.GetSignParam;
import leaf.prod.walletsdk.model.request.param.MarketcapParam;
import leaf.prod.walletsdk.model.request.param.NonceParam;
import leaf.prod.walletsdk.model.request.param.NotifyScanParam;
import leaf.prod.walletsdk.model.request.param.NotifyStatusParam;
import leaf.prod.walletsdk.model.request.param.NotifyTransactionSubmitParam;
import leaf.prod.walletsdk.model.request.param.PartnerParam;
import leaf.prod.walletsdk.model.request.param.TransactionParam;
import leaf.prod.walletsdk.model.request.param.UnlockWallet;
import leaf.prod.walletsdk.model.response.ResponseWrapper;
import leaf.prod.walletsdk.model.response.data.BalanceResult;
import leaf.prod.walletsdk.model.response.data.MarketcapResult;
import leaf.prod.walletsdk.model.response.data.Token;
import leaf.prod.walletsdk.model.response.data.TransactionPageWrapper;
import rx.Observable;

public class LoopringService {

    private RpcDelegate rpcDelegate;

    public LoopringService() {
        String url = SDK.relayBase();
        rpcDelegate = RpcDelegate.getService(url);
    }

    public Observable<String> getNonce(String owner) {
        NonceParam nonceParamParam = NonceParam.builder()
                .owner(owner)
                .build();
        RequestWrapper request = new RequestWrapper("loopring_getNonce", nonceParamParam);
        return rpcDelegate.getNonce(request).map(ResponseWrapper::getResult);
    }

    public Observable<String> getEstimateGasPrice() {
        RequestWrapper request = new RequestWrapper("loopring_getEstimateGasPrice", Maps.newHashMap());
        return rpcDelegate.estimateGasPrice(request).map(ResponseWrapper::getResult);
    }

    public Observable<String> notifyCreateWallet(String owner) {
        UnlockWallet unlockWallet = UnlockWallet.builder()
                .owner(owner)
                .build();
        RequestWrapper request = new RequestWrapper("loopring_unlockWallet", unlockWallet);
        Observable<ResponseWrapper<String>> observable = rpcDelegate.unlockWallet(request);
        return observable.map(ResponseWrapper::getResult);
    }

    public Observable<BalanceResult> getBalance(String owner) {
        BalanceParam param = BalanceParam.builder()
                .delegateAddress(Default.DELEGATE_ADDRESS)
                .owner(owner)
                .build();
        RequestWrapper request = new RequestWrapper("loopring_getBalance", param);
        return rpcDelegate.getBalance(request).map(ResponseWrapper::getResult);
    }

    public Observable<TransactionPageWrapper> getTransactions(String owner, String symbol, int pageIndex, int pageSize) {
        TransactionParam param = TransactionParam.builder()
                .owner(owner)
                .symbol(symbol)
                .pageIndex(pageIndex)
                .pageSize(pageSize)
                .build();
        RequestWrapper request = new RequestWrapper("loopring_getTransactions", param);
        Observable<ResponseWrapper<TransactionPageWrapper>> observable = rpcDelegate.getTransactions(request);
        return observable.map(ResponseWrapper::getResult);
    }

    /**
     * use getCustomToken instead: getCustomToken = getSupportedToken + customToken
     */
    @Deprecated
    public Observable<List<Token>> getSupportedToken() {
        RequestWrapper request = new RequestWrapper("loopring_getSupportedTokens", Maps.newHashMap());
        Observable<ResponseWrapper<List<Token>>> observable = rpcDelegate.getSupportedTokens(request);
        return observable.map(ResponseWrapper::getResult);
    }

    public Observable<MarketcapResult> getMarketcap(String currency) {
        RequestWrapper request = new RequestWrapper("loopring_getPriceQuote", MarketcapParam.builder()
                .currency(currency)
                .build());
        return rpcDelegate.getMarketcap(request).map(ResponseWrapper::getResult);
    }

    public Observable<MarketcapResult> getPriceQuoteByToken(String currency, String token) {
        RequestWrapper request = new RequestWrapper("loopring_getPriceQuoteByToken", MarketcapParam.builder()
                .currency(currency)
                .token(token)
                .build());
        return rpcDelegate.getMarketcap(request).map(ResponseWrapper::getResult);
    }

    public Observable<String> notifyTransactionSubmitted(RawTransaction rawTransaction, String from, String txHash, TransactionSignature signature) {
        NotifyTransactionSubmitParam notifyTransactionSubmitParam = NotifyTransactionSubmitParam.builder()
                .hash(txHash)
                .nonce(Numeric.toHexStringWithPrefix(rawTransaction.getNonce()))
                .to(rawTransaction.getTo())
                .value(Numeric.toHexStringWithPrefix(rawTransaction.getValue()))
                .gasPrice(Numeric.toHexStringWithPrefix(rawTransaction.getGasPrice()))
                .gas(Numeric.toHexStringWithPrefix(rawTransaction.getGasLimit()))
                .input(rawTransaction.getData())
                .from(from)
                .build();
        RequestWrapper request = new RequestWrapper("loopring_notifyTransactionSubmitted", notifyTransactionSubmitParam);
        Observable<ResponseWrapper<String>> observable = rpcDelegate.notifyTransactionSubmitted(request);
        return observable.map(ResponseWrapper::getResult);
    }

    public Observable<String> notifyScanLogin(NotifyScanParam.SignParam signParam, String owner, String uuid) {
        NotifyScanParam notifyScanParam = NotifyScanParam.builder().owner(owner).uuid(uuid).sign(signParam).build();
        RequestWrapper request = new RequestWrapper("loopring_notifyScanLogin", notifyScanParam);
        Observable<ResponseWrapper<String>> observable = rpcDelegate.notifyScanLogin(request);
        return observable.map(ResponseWrapper::getResult);
    }

    public Observable<String> notifyStatus(NotifyStatusParam.NotifyBody body, String owner) {
        NotifyStatusParam notifyStatusParam = NotifyStatusParam.builder().owner(owner).body(body).build();
        RequestWrapper request = new RequestWrapper("loopring_notifyCirculr", notifyStatusParam);
        Observable<ResponseWrapper<String>> observable = rpcDelegate.notifyStatus(request);
        return observable.map(ResponseWrapper::getResult);
    }

    public Observable<String> getSignMessage(String hash) {
        GetSignParam getSignParam = GetSignParam.builder().key(hash).build();
        RequestWrapper request = new RequestWrapper("loopring_getTempStore", getSignParam);
        Observable<ResponseWrapper<String>> observable = rpcDelegate.getSignMessage(request);
        return observable.map(ResponseWrapper::getResult);
    }

    public Observable<Partner> createPartner(String owner) {
        RequestWrapper request = new RequestWrapper("loopring_createCityPartner", PartnerParam.builder()
                .walletAddress(owner));
        return rpcDelegate.createPartner(request).map(ResponseWrapper::getResult);
    }

    public Observable<Partner> activateInvitation() {
        Observable<ResponseWrapper<Partner>> observable = rpcDelegate.activateInvitation();
        return observable.map(ResponseWrapper::getResult);
    }

    public Observable<String> addCustomToken(String owner, String address, String symbol, String decimals) {
        AddTokenParam param = AddTokenParam.builder()
                .owner(owner)
                .address(address)
                .symbol(symbol)
                .decimals(decimals)
                .build();
        RequestWrapper request = new RequestWrapper("loopring_addCustomToken", param);
        Observable<ResponseWrapper<String>> observable = rpcDelegate.addCustomToken(request);
        return observable.map(ResponseWrapper::getResult);
    }

    public Observable<List<Token>> getCustomToken(String owner) {
        com.lyqb.walletsdk.model.request.param.GetTokenParam param = com.lyqb.walletsdk.model.request.param.GetTokenParam
                .builder()
                .owner(owner)
                .build();
        RequestWrapper request = new RequestWrapper("loopring_getCustomTokens", param);
        Observable<ResponseWrapper<List<Token>>> observable = rpcDelegate.getCustomToken(request);
        return observable.map(ResponseWrapper::getResult);
    }

    public Observable<String> cancelOrderFlex(CancelOrder cancelOrder, NotifyScanParam.SignParam signParam) {
        CancelOrderParam cancelParam = CancelOrderParam.builder()
                .type(cancelOrder.getType().name())
                .cutoff(cancelOrder.getCutoff())
                .tokenB(cancelOrder.getTokenB())
                .tokenS(cancelOrder.getTokenS())
                .orderHash(cancelOrder.getOrderHash())
                .sign(signParam)
                .build();
        RequestWrapper request = new RequestWrapper("loopring_flexCancelOrder", cancelParam);
        Observable<ResponseWrapper<String>> observable = rpcDelegate.cancelOrderFlex(request);
        return observable.map(ResponseWrapper::getResult);
    }
}
