//package com.lyqb.walletsdk.singleton;
//
//import com.lyqb.walletsdk.Default;
//
//import org.web3j.protocol.Web3j;
//import org.web3j.protocol.Web3jFactory;
//import org.web3j.protocol.http.HttpService;
//
//public class Web3jInstance {
//    private static final Web3j client;
//
//    static {
//        HttpService httpService = new HttpService(Default.ETH_RPC_URL);
//        client = Web3jFactory.build(httpService);
//    }
//
//    public static Web3j getInstance() {
//        return client;
//    }
//
//    private Web3jInstance() {
//    }
//}
