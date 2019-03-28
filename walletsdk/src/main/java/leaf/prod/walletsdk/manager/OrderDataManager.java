/**
 * Created with IntelliJ IDEA.
 * User: kenshin wangchen@loopring.org
 * Time: 2018-11-19 5:39 PM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.walletsdk.manager;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import leaf.prod.walletsdk.Default;
import leaf.prod.walletsdk.Erc20TransactionManager;
import leaf.prod.walletsdk.R;
import leaf.prod.walletsdk.Transfer;
import leaf.prod.walletsdk.model.RandomWallet;
import leaf.prod.walletsdk.model.order.RawOrder;
import leaf.prod.walletsdk.model.response.RelayError;
import leaf.prod.walletsdk.model.response.RelayResponseWrapper;
import leaf.prod.walletsdk.model.transaction.SignedBody;
import leaf.prod.walletsdk.service.RelayService;
import leaf.prod.walletsdk.util.SignUtils;
import leaf.prod.walletsdk.util.WalletUtil;
import lombok.Getter;
import lombok.Setter;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

@Getter
@Setter
public abstract class OrderDataManager {

    protected String owner;

    // token symbol, e.g. weth
    protected String tokenSell;

    // token symbol, e.g. lrc
    protected String tokenBuy;

    // e.g. lrc-weth
    protected String tradePair;

    protected Context context;

    protected GasDataManager gas;

    protected Credentials credentials;

    protected TokenDataManager token;

    protected BalanceDataManager balance;

    protected Map<String, Double> balanceInfo;

    protected RelayService relayService;

    OrderDataManager(Context context) {
        this.context = context;
        this.balanceInfo = new HashMap<>();
        this.relayService = new RelayService();
        this.owner = WalletUtil.getCurrentAddress(context);
        this.gas = GasDataManager.getInstance(context);
        this.token = TokenDataManager.getInstance(context);
        this.balance = BalanceDataManager.getInstance(context);
    }

    public RawOrder constructOrder(Double amountBuy, Double amountSell, Integer validS, Integer validU) {
        RawOrder order = null;
        try {
            String tokenBuy = token.getTokenBySymbol(getTokenBuy()).getProtocol();
            String tokenSell = token.getTokenBySymbol(getTokenSell()).getProtocol();
            String amountB = Numeric.toHexStringWithPrefix(token.getWeiFromDouble(getTokenBuy(), amountBuy));
            String amountS = Numeric.toHexStringWithPrefix(token.getWeiFromDouble(getTokenSell(), amountSell));
            String validSince = Numeric.toHexStringWithPrefix(BigInteger.valueOf(validS));
            String validUntil = Numeric.toHexStringWithPrefix(BigInteger.valueOf(validU));
            RandomWallet randomWallet = WalletUtil.getRandomWallet();
            order = RawOrder.builder()
                    .owner(WalletUtil.getCurrentAddress(context)).market(tradePair)
                    .tokenS(getTokenSell()).tokenSell(tokenSell).tokenB(getTokenBuy()).tokenBuy(tokenBuy)
                    .amountS(amountS).amountSell(amountSell).amountB(amountB).amountBuy(amountBuy)
                    .validS(validS).validSince(validSince).validU(validU).validUntil(validUntil)
                    .lrc(0d).lrcFee(Numeric.toHexStringWithPrefix(BigInteger.ZERO))
                    .walletAddress(PartnerDataManager.getInstance(context).getWalletAddress())
                    .authAddr(randomWallet.getAddress()).authPrivateKey(randomWallet.getPrivateKey())
                    .buyNoMoreThanAmountB(false).marginSplitPercentage("0x32").margin(50).powNonce(1)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return order;
    }

    public RawOrder signOrder(RawOrder order) {
        String encoded = encodeOrder(order);
        byte[] hash = Hash.sha3(Numeric.hexStringToByteArray(encoded));
        SignedBody signedBody = SignUtils.genSignMessage(credentials, hash);
        String r = Numeric.toHexStringNoPrefix(signedBody.getSig().getR());
        String s = Numeric.toHexStringNoPrefix(signedBody.getSig().getS());
        Integer v = (int) signedBody.getSig().getV();
        order.setHash(signedBody.getHash());
        order.setR(r);
        order.setS(s);
        order.setV(Numeric.toHexStringWithPrefix(BigInteger.valueOf(v)));
        return order;
    }

    private String encodeOrder(RawOrder order) {
        List<? extends Type<? extends Serializable>> types = Arrays.asList(
                new Uint256(Numeric.toBigInt(order.getAmountS())),
                new Uint256(Numeric.toBigInt(order.getAmountB())),
                new Uint256(Numeric.toBigInt(order.getValidSince())),
                new Uint256(Numeric.toBigInt(order.getValidUntil())),
                new Uint256(Numeric.toBigInt(order.getLrcFee()))
        );
        String data = Numeric.cleanHexPrefix(order.getDelegate());
        data += Numeric.cleanHexPrefix(order.getOwner());
        data += Numeric.cleanHexPrefix(order.getTokenSell());
        data += Numeric.cleanHexPrefix(order.getTokenBuy());
        data += Numeric.cleanHexPrefix(order.getWalletAddress());
        data += Numeric.cleanHexPrefix(order.getAuthAddr());
        for (Type<? extends Serializable> type : types) {
            data += TypeEncoder.encode(type);
        }
        data += order.getBuyNoMoreThanAmountB() ? "01" : "00";
        data += Numeric.cleanHexPrefix(order.getMarginSplitPercentage());
        return data;
    }

    public Observable<RelayResponseWrapper> handleInfo() {
        Observable<RelayResponseWrapper> result;
        if (needApprove()) {
            result = approve().observeOn(Schedulers.io())
                    .flatMap((Func1<String, Observable<RelayResponseWrapper>>) hash -> {
                        if (!hash.equals("failed")) {
                            return submit();
                        }
                        RelayError failed = RelayError.builder()
                                .message(context.getString(R.string.approve_failed)).build();
                        RelayResponseWrapper<Object> response = RelayResponseWrapper.builder().error(failed).build();
                        return Observable.just(response);
                    });
        } else {
            result = submit();
        }
        return result;
    }

    public boolean isBalanceEnough() {
        for (String s : balanceInfo.keySet()) {
            if (s.startsWith("MINUS_")) {
                return false;
            }
        }
        return true;
    }

    private boolean needApprove() {
        for (String s : balanceInfo.keySet()) {
            if (s.startsWith("GAS_")) {
                return true;
            }
        }
        return false;
    }

    private Observable<String> approve() {
        Observable<String> result = Observable.just("failed");
        for (Map.Entry<String, Double> entry : balanceInfo.entrySet()) {
            if (entry.getKey().startsWith("GAS_")) {
                if (entry.getValue() != 1 && entry.getValue() != 2) {
                    continue;
                }
                String token = entry.getKey().split("_")[1];
                if (entry.getValue() == 1) {
                    result = approveOnce(token);
                } else if (entry.getValue() == 2) {
                    result = approveTwice(token);
                }
            }
        }
        return result;
    }

    protected Observable<String> approve(String symbol, Double value) {
        Transfer transfer = new Transfer(credentials);
        String contract = token.getTokenBySymbol(symbol).getProtocol();
        BigInteger amount = token.getWeiFromDouble(symbol, value);
        BigInteger gasPrice = gas.getCustomizeGasPriceInWei().toBigInteger();
        BigInteger gasLimit = gas.getGasLimitByType("approve");
        return transfer.erc20(contract, gasPrice, gasLimit)
                .approve(credentials, contract, Default.DELEGATE_ADDRESS, amount);
    }

    private Observable<String> approveOnce(String symbol) {
        Transfer transfer = new Transfer(credentials);
        String contract = token.getTokenBySymbol(symbol).getProtocol();
        BigInteger value = token.getWeiFromDouble(symbol, (double) Integer.MAX_VALUE);
        BigInteger gasPrice = gas.getCustomizeGasPriceInWei().toBigInteger();
        BigInteger gasLimit = gas.getGasLimitByType("approve");
        return transfer.erc20(contract, gasPrice, gasLimit)
                .approve(credentials, contract, Default.DELEGATE_ADDRESS, value);
    }

    private Observable<String> approveTwice(String symbol) {
        Transfer transfer = new Transfer(credentials);
        String contract = token.getTokenBySymbol(symbol).getProtocol();
        BigInteger value = BigInteger.ZERO;
        BigInteger gasPrice = gas.getCustomizeGasPriceInWei().toBigInteger();
        BigInteger gasLimit = gas.getGasLimitByType("approve");
        final Erc20TransactionManager manager = transfer.erc20(contract, gasPrice, gasLimit);
        return manager.approve(credentials, contract, Default.DELEGATE_ADDRESS, value)
                .observeOn(Schedulers.io())
                .flatMap((Func1<String, Observable<String>>) s -> {
                    BigInteger value1 = token.getWeiFromDouble(symbol, (double) Integer.MAX_VALUE);
                    return manager.approve(credentials, contract, Default.DELEGATE_ADDRESS, value1);
                });
    }

    protected abstract Observable<RelayResponseWrapper> submit();
}
