/**
 * Created with IntelliJ IDEA.
 * User: kenshin wangchen@loopring.org
 * Time: 2018-11-19 5:39 PM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.walletsdk.manager;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import org.web3j.crypto.Credentials;
import org.web3j.utils.Numeric;

import leaf.prod.walletsdk.model.OriginOrder;
import leaf.prod.walletsdk.model.SignedBody;
import leaf.prod.walletsdk.service.LoopringService;
import leaf.prod.walletsdk.util.NumberUtils;
import leaf.prod.walletsdk.util.SignUtils;
import leaf.prod.walletsdk.util.WalletUtil;

public class OrderDataManager {

    protected String owner;

    protected Credentials credentials;

    protected Context context;

    protected GasDataManager gas;

    protected TokenDataManager token;

    protected BalanceDataManager balance;

    protected Map<String, Double> balanceInfo;

    protected LoopringService loopringService;

    OrderDataManager(Context context) {
        this.context = context;
        this.balanceInfo = new HashMap<>();
        this.loopringService = new LoopringService();
        this.owner = WalletUtil.getCurrentAddress(context);
        this.gas = GasDataManager.getInstance(context);
        this.token = TokenDataManager.getInstance(context);
        this.balance = BalanceDataManager.getInstance(context);
    }

    public OriginOrder signOrder(Credentials credentials, OriginOrder order) {
        byte[] encoded = encodeOrder(order);
        SignedBody signedBody = SignUtils.genSignMessage(credentials, encoded);
        String r = Numeric.toHexStringNoPrefix(signedBody.getSig().getR());
        String s = Numeric.toHexStringNoPrefix(signedBody.getSig().getS());
        Integer v = (int) signedBody.getSig().getV();
        order.setHash(signedBody.getHash());
        order.setR(r);
        order.setS(s);
        order.setV(v);
        return order;
    }

    private byte[] encodeOrder(OriginOrder order) {
        byte[] array = Numeric.hexStringToByteArray(order.getDelegate());
        array = NumberUtils.append(array, Numeric.hexStringToByteArray(order.getOwner()));
        array = NumberUtils.append(array, Numeric.hexStringToByteArray(order.getOwner()));
        array = NumberUtils.append(array, Numeric.hexStringToByteArray(order.getTokenS()));
        array = NumberUtils.append(array, Numeric.hexStringToByteArray(order.getTokenB()));
        array = NumberUtils.append(array, Numeric.hexStringToByteArray(order.getWalletAddress()));
        array = NumberUtils.append(array, Numeric.hexStringToByteArray(order.getAuthAddr()));
        array = NumberUtils.append(array, Numeric.hexStringToByteArray(order.getAmountS()));
        array = NumberUtils.append(array, Numeric.hexStringToByteArray(order.getAmountB()));
        array = NumberUtils.append(array, Numeric.hexStringToByteArray(order.getValidSince()));
        array = NumberUtils.append(array, Numeric.hexStringToByteArray(order.getValidUntil()));
        array = NumberUtils.append(array, Numeric.hexStringToByteArray(order.getLrcFee()));
        byte[] temp = order.getBuyNoMoreThanAmountB() ? new byte[]{1} : new byte[]{0};
        array = NumberUtils.append(array, temp);
        temp = new byte[]{order.getMarginSplitPercentage().byteValue()};
        array = NumberUtils.append(array, temp);
        return array;
    }

    protected Double getLRCFrozenFromServer() {
        String valueInWei = loopringService.getFrozenLRCFee(owner).toBlocking().single();
        return token.getDoubleFromWei("LRC", valueInWei);
    }

    protected Double getAllowanceFromServer(String symbol) {
        String valueInWei = loopringService.getEstimatedAllocatedAllowance(owner, symbol).toBlocking().single();
        return token.getDoubleFromWei(symbol, valueInWei);
    }
}
