package com.lyqb.walletsdk;

import com.google.common.collect.ImmutableList;
import com.lyqb.walletsdk.exception.KeystoreSaveException;
import com.lyqb.walletsdk.util.MnemonicUtils;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.HDUtils;
import org.junit.Test;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;

import java.util.List;

public class WalletHelpetTest {

    @Test
    public void createTest() throws KeystoreSaveException {
//        WalletHelper helper = new WalletHelper();
//        String m = "hospital offer drop retreat cook zero mandate cigar orchard three grain seven";
        String m = "door lend source dumb install immune thumb crater ostrich tongue buyer huge";
//        String m = "deal release style gadget hold cannon traffic boat skirt great anxiety sight";

        String dpath = Default.DEFAULT_DPATH;
        byte[] seed = MnemonicUtils.generateSeed(m, "1212");
        List<ChildNumber> childNumberList = HDUtils.parsePath(dpath.replaceAll("\'", "H").toUpperCase());
        DeterministicKey rootKey = HDKeyDerivation.createMasterPrivateKey(seed);

        DeterministicHierarchy hdKey = new DeterministicHierarchy(rootKey);
        DeterministicKey destKey = hdKey.deriveChild(childNumberList, true, true, new ChildNumber(0));

        ECKeyPair ecKeyPair = ECKeyPair.create(destKey.getPrivKey());
        Credentials credentials1 = Credentials.create(ecKeyPair);
        System.out.println(credentials1.toString());


        ImmutableList<ChildNumber> childNumberImmutableList = ImmutableList.copyOf(childNumberList);
        int numChildren = hdKey.getNumChildren(childNumberImmutableList);
        System.out.println(numChildren);

    }
}
