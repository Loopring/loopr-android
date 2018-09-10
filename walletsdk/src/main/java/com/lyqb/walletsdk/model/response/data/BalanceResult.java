package com.lyqb.walletsdk.model.response.data;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class BalanceResult {
    private String delegateAddress;
    private String owner;
    private List<Asset> assets;

    @Data
    public static class Asset {
        private String symbol;
        private BigDecimal balance;
        private BigDecimal allowance;
        private double value;
        private double legalValue;
    }
}
