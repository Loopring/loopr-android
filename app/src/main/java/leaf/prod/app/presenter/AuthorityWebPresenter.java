/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-11-12 11:45 AM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.app.presenter;

import java.io.IOException;
import java.math.BigInteger;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.Sign;
import org.web3j.tx.RawTransactionManager;
import org.web3j.utils.Numeric;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vondear.rxtool.view.RxToast;

import leaf.prod.app.R;
import leaf.prod.app.activity.AuthorityWebActivity;
import leaf.prod.app.activity.MainActivity;
import leaf.prod.app.model.ImportWalletType;
import leaf.prod.app.model.WalletEntity;
import leaf.prod.app.utils.FileUtils;
import leaf.prod.app.utils.LyqbLogger;
import leaf.prod.app.utils.WalletUtil;
import leaf.prod.walletsdk.SDK;
import leaf.prod.walletsdk.model.CancelOrder;
import leaf.prod.walletsdk.model.QRCodeType;
import leaf.prod.walletsdk.model.ScanWebQRCode;
import leaf.prod.walletsdk.model.SignStatus;
import leaf.prod.walletsdk.model.request.param.NotifyScanParam;
import leaf.prod.walletsdk.model.request.param.NotifyStatusParam;
import leaf.prod.walletsdk.service.LoopringService;
import leaf.prod.walletsdk.util.KeystoreUtils;
import leaf.prod.walletsdk.util.MnemonicUtils;
import leaf.prod.walletsdk.util.SignUtils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AuthorityWebPresenter extends BasePresenter<AuthorityWebActivity> {

    private AlertDialog passwordDialog;

    private static LoopringService loopringService = new LoopringService();

    private static Gson gson = new Gson();

    private String value;

    private QRCodeType type;

    private Credentials credentials;

    private String owner;

    public AuthorityWebPresenter(AuthorityWebActivity view, Context context, String info, QRCodeType type) {
        super(view, context);
        this.type = type;
        this.owner = WalletUtil.getCurrentAddress(context);
        this.value = gson.fromJson(info, ScanWebQRCode.class).getValue();
    }

    public void showPasswordDialog() {
        if (passwordDialog == null) {
            final AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context, R.style.DialogTheme);
            View passwordView = LayoutInflater.from(context).inflate(R.layout.dialog_put_password, null);
            builder.setView(passwordView);
            final EditText passwordInput = passwordView.findViewById(R.id.password_input);
            passwordView.findViewById(R.id.cancel).setOnClickListener(v -> passwordDialog.dismiss());
            passwordView.findViewById(R.id.confirm).setOnClickListener(v -> {
                try {
                    generateCredentials(passwordInput.getText().toString().trim());
                    handle();
                } catch (Exception e) {
                    passwordInput.setText("");
                    RxToast.error(context.getResources().getString(R.string.authority_login_error));
                }
            });
            builder.setCancelable(true);
            passwordDialog = builder.create();
            passwordDialog.setCancelable(true);
            passwordDialog.setCanceledOnTouchOutside(true);
        }
        passwordDialog.show();
    }

    private void generateCredentials(String password) {
        try {
            WalletEntity walletEntity = WalletUtil.getCurrentWallet(context);
            if (walletEntity != null && walletEntity.getWalletType() != null && walletEntity.getWalletType() == ImportWalletType.MNEMONIC) {
                LyqbLogger.log(walletEntity.toString());
                credentials = MnemonicUtils.calculateCredentialsFromMnemonic(walletEntity.getMnemonic(), walletEntity.getdPath(), password);
            } else {
                String keystore = FileUtils.getKeystoreFromSD(context);
                credentials = KeystoreUtils.unlock(password, keystore);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handle() {
        switch (type) {
            case LOGIN:
                handleLogin();
                break;
            case APPROVE:
                handleApprove();
                break;
            case CONVERT:
                handleConvert();
                break;
            case ORDER:
                handleOrder();
                break;
            case CANCEL_ORDER:
                handleCancel();
                break;
        }
    }

    private NotifyStatusParam.NotifyBody getStatus(SignStatus status) {
        return NotifyStatusParam.NotifyBody.builder()
                .hash(value)
                .status(status.name())
                .build();
    }

    private NotifyScanParam.SignParam getSignParam() {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        Sign.SignatureData sig = SignUtils.genSignMessage(credentials, timeStamp);
        return NotifyScanParam.SignParam.builder().timestamp(timeStamp).owner(owner)
                .r(Numeric.toHexStringNoPrefix(sig.getR()))
                .s(Numeric.toHexStringNoPrefix(sig.getS()))
                .v(BigInteger.valueOf(sig.getV()))
                .build();
    }

    private void handleApprove() {
    }

    private void handleConvert() {
        if (value != null) {
            NotifyStatusParam.NotifyBody received = getStatus(SignStatus.received);
            loopringService.notifyStatus(received, owner)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap((Func1<String, Observable<String>>) result -> loopringService.getSignMessage(value))
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap((Func1<String, Observable<String>>) rawTx -> {
                        signAndSendRawTx(rawTx);
                        return loopringService.notifyStatus(getStatus(SignStatus.accept), owner);
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (passwordDialog != null) {
                                ((TextView) passwordDialog.findViewById(R.id.password_input)).setText("");
                            }
                            RxToast.error(context.getResources().getString(R.string.authority_login_error));
                        }

                        @Override
                        public void onNext(String s) {
                            RxToast.success(context.getResources().getString(R.string.authority_login_success));
                            view.finish();
                            view.getOperation().forward(MainActivity.class);
                        }
                    });
        }
    }

    private void handleOrder() {
    }

    private void handleCancel() {
        if (value != null) {
            NotifyStatusParam.NotifyBody received = getStatus(SignStatus.received);
            loopringService.notifyStatus(received, owner)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap((Func1<String, Observable<String>>) result -> loopringService.getSignMessage(value))
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap((Func1<String, Observable<String>>) this::signAndCancel)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap((Func1<String, Observable<String>>) result -> loopringService.notifyStatus(getStatus(SignStatus.accept), owner))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (passwordDialog != null) {
                                ((TextView) passwordDialog.findViewById(R.id.password_input)).setText("");
                            }
                            RxToast.error(context.getResources().getString(R.string.authority_login_error));
                        }

                        @Override
                        public void onNext(String s) {
                            RxToast.success(context.getResources().getString(R.string.authority_login_success));
                            view.finish();
                            view.getOperation().forward(MainActivity.class);
                        }
                    });
        }
    }

    private Observable<String> signAndCancel(String cancel) {
        CancelOrder cancelOrder = gson.fromJson(cancel, CancelOrder.class);
        if (cancelOrder.isValid()) {
            return loopringService.cancelOrderFlex(cancelOrder, getSignParam());
        }
        return null;
    }

    private void signAndSendRawTx(String rawTx) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(rawTx).getAsJsonObject().getAsJsonObject("tx");
            BigInteger gasPrice = Numeric.toBigInt(json.get("gasPrice").getAsString());
            BigInteger gasLimit = Numeric.toBigInt(json.get("gasLimit").getAsString());
            String to = json.get("to").getAsString();
            String data = json.get("data").getAsString();
            BigInteger value = Numeric.toBigInt(json.get("value").getAsString());
            RawTransactionManager transactionManager = new RawTransactionManager(SDK.getWeb3j(), credentials, SDK.CHAIN_ID);
            transactionManager.sendTransaction(gasPrice, gasLimit, to, data, value);
        } catch (IOException e) {
            if (passwordDialog != null) {
                ((TextView) passwordDialog.findViewById(R.id.password_input)).setText("");
            }
            RxToast.error(context.getResources().getString(R.string.authority_login_error));
        }
    }

    public void handleLogin() {
        try {
            loopringService.notifyScanLogin(getSignParam(), owner, value)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (passwordDialog != null) {
                                ((TextView) passwordDialog.findViewById(R.id.password_input)).setText("");
                            }
                            RxToast.error(context.getResources().getString(R.string.authority_login_error));
                        }

                        @Override
                        public void onNext(String s) {
                            RxToast.success(context.getResources().getString(R.string.authority_login_success));
                            view.finish();
                            view.getOperation().forward(MainActivity.class);
                        }
                    });
        } catch (Exception e) {
            if (passwordDialog != null) {
                ((TextView) passwordDialog.findViewById(R.id.password_input)).setText("");
            }
            RxToast.error(context.getResources().getString(R.string.authority_login_error));
        }
    }
}
