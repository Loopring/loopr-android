/**
 * Created with IntelliJ IDEA.
 * User: kenshin wangchen@loopring.org
 * Time: 2018-12-05 5:39 PM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.app.presenter;

import android.content.Context;

import com.google.gson.JsonObject;
import com.vondear.rxtool.view.RxToast;

import leaf.prod.app.R;
import leaf.prod.app.activity.P2PConfirmActivity;
import leaf.prod.app.activity.P2PErrorActivity;
import leaf.prod.app.activity.P2PSuccessActivity;
import leaf.prod.app.utils.PasswordDialogUtil;
import leaf.prod.walletsdk.manager.BalanceDataManager;
import leaf.prod.walletsdk.manager.MarketcapDataManager;
import leaf.prod.walletsdk.manager.P2POrderDataManager;
import leaf.prod.walletsdk.manager.TokenDataManager;
import leaf.prod.walletsdk.model.OriginOrder;
import leaf.prod.walletsdk.util.DateUtil;
import leaf.prod.walletsdk.util.NumberUtils;
import leaf.prod.walletsdk.util.WalletUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class P2PConfirmPresenter extends BasePresenter<P2PConfirmActivity> {

    public JsonObject p2pContent;

    private TokenDataManager tokenManager;

    private BalanceDataManager balanceManager;

    private MarketcapDataManager marketManager;

    private P2POrderDataManager p2pOrderManager;

    public P2PConfirmPresenter(P2PConfirmActivity view, Context context) {
        super(view, context);
        tokenManager = TokenDataManager.getInstance(context);
        balanceManager = BalanceDataManager.getInstance(context);
        marketManager = MarketcapDataManager.getInstance(context);
        p2pOrderManager = P2POrderDataManager.getInstance(context);
    }

    public void handleResult() {
        p2pOrderManager.handleResult(p2pContent);
        OriginOrder taker = p2pOrderManager.getOrder();
        if (taker != null) {
            int resourceB = tokenManager.getTokenBySymbol(taker.getTokenB()).getImageResId();
            int resourceS = tokenManager.getTokenBySymbol(taker.getTokenS()).getImageResId();
            String amountB = balanceManager.getFormattedBySymbol(taker.getTokenB(), taker.getAmountBuy());
            String amountS = balanceManager.getFormattedBySymbol(taker.getTokenS(), taker.getAmountSell());
            String currencyB = marketManager.getCurrencyBySymbol(taker.getTokenB(), taker.getAmountBuy());
            String currencyS = marketManager.getCurrencyBySymbol(taker.getTokenS(), taker.getAmountSell());
            String validSince = DateUtil.formatDateTime(taker.getValidS() * 1000, "MM-dd HH:mm");
            String validUntil = DateUtil.formatDateTime(taker.getValidU() * 1000, "MM-dd HH:mm");
            Double price = taker.getAmountSell() / taker.getAmountBuy();
            String priceStr = NumberUtils.format1(price, 6) + " " + taker.getTokenS() + " / " + taker.getTokenB();
            String lrcFee = balanceManager.getFormattedBySymbol("LRC", taker.getLrc());
            String lrcCurrency = marketManager.getCurrencyBySymbol("LRC", taker.getLrc());
            view.ivTokenB.setImageDrawable(context.getResources().getDrawable(resourceB));
            view.ivTokenS.setImageDrawable(context.getResources().getDrawable(resourceS));
            view.tvBuyToken.setText(context.getString(R.string.buy) + " " + taker.getTokenB());
            view.tvSellToken.setText(context.getString(R.string.sell) + " " + taker.getTokenS());
            view.tvBuyAmount.setText(amountB);
            view.tvSellAmount.setText(amountS);
            view.tvBuyPrice.setText(currencyB);
            view.tvSellPrice.setText(currencyS);
            view.tvPrice.setText(priceStr);
            view.tvTradingFee.setText(lrcFee + " LRC ≈ " + lrcCurrency);
            view.tvLiveTime.setText(validSince + " ~ " + validUntil);
        }
    }

    public void processTaker() {
        if (WalletUtil.needPassword(context)) {
            PasswordDialogUtil.showPasswordDialog(context, v -> processTaker(PasswordDialogUtil.getInputPassword()));
        } else {
            processTaker("");
        }
    }

    private void processTaker(String password) {
        try {
            p2pOrderManager.verify(password);
        } catch (Exception e) {
            PasswordDialogUtil.clearPassword();
            RxToast.error(view.getResources().getString(R.string.keystore_psw_error));
            e.printStackTrace();
            return;
        }
        if (!p2pOrderManager.isBalanceEnough()) {
            view.finish();
            PasswordDialogUtil.dismiss(context);
            view.getOperation().forward(P2PErrorActivity.class);
        } else {
            p2pOrderManager.handleInfo()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        view.finish();
                        if (response.getError() == null) {
                            view.getOperation().forward(P2PSuccessActivity.class);
                        } else {
                            String message = p2pOrderManager.getLocaleError(response.getError().getMessage());
                            RxToast.error(message, 5);
                        }
                    });
        }
    }
}
