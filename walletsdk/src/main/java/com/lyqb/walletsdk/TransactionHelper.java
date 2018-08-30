package com.lyqb.walletsdk;

import com.lyqb.walletsdk.exception.TransactionException;
import com.lyqb.walletsdk.model.TransactionObject;
import com.lyqb.walletsdk.service.EthereumService;
import com.lyqb.walletsdk.service.LoopringService;
import com.lyqb.walletsdk.util.SignUtils;

import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class TransactionHelper {

    private static LoopringService loopringService = new LoopringService();
    private static EthereumService ethereumService = new EthereumService();

    public static TransactionObject createTransaction(String from, String to, BigInteger weiValue) {
        return createTransaction(from, to, weiValue, "");
    }

    public static TransactionObject createTransaction(String from, String to, BigInteger weiValue, String data) {
        String nonceStr = loopringService.getNonce(from).toBlocking().single();
        String gasPriceStr = loopringService.getEstimateGasPrice().toBlocking().single();

        BigInteger nonce = Numeric.toBigInt(Numeric.cleanHexPrefix(nonceStr));
        BigInteger gasPrice = Numeric.toBigInt(Numeric.cleanHexPrefix(gasPriceStr));

        TransactionObject transactionObject = createTransaction(((byte) 1), from, to, nonce, gasPrice, BigInteger.ZERO, weiValue, data);
        BigInteger estimateGasLimit = ethereumService.estimateGasLimit(transactionObject);
        transactionObject.setGasLimit(estimateGasLimit);
        return transactionObject;
    }

    public static TransactionObject createTransaction(byte chainId, String from, String to, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimited, BigInteger weiValue, String data) {
        return new TransactionObject(
                chainId,
                from,
                to,
                nonce,
                gasPrice,
                gasLimited,
                weiValue,
                data
        );
    }

    @Deprecated
    public static String sendTransaction(TransactionObject transactionObject, String privatKey) throws TransactionException {
        String signedTransaction = SignUtils.signTransaction(transactionObject, privatKey);

        String txHash = ethereumService.sendRawTransaction(signedTransaction);
        String txHashReply = loopringService.notifyTransactionSubmitted(txHash, transactionObject).toBlocking().single();
        if (txHash.equals(txHashReply)) {
            return txHash;
        } else {
            throw new TransactionException("txHashReply not equals txHash, check transactionObject.");
        }
    }
}
