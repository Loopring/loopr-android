/**
 * Created with IntelliJ IDEA.
 * User: kenshin wangchen@loopring.org
 * Time: 2018-09-11 下午6:32
 * Cooperation: loopring.org 路印协议基金会
 */
package com.lyqb.walletsdk.model;

import java.text.NumberFormat;
import java.util.Locale;

public enum Currency {
    CNY("CNY", "￥", Locale.CHINA), USD("USD", "$", Locale.US);

    private String text;

    private String symbol;

    private Locale locale;

    Currency(String text, String symbol, Locale locale) {
        this.text = text;
        this.symbol = symbol;
        this.locale = locale;
    }

    public String getText() {
        return text;
    }

    public String getSymbol() {
        return symbol;
    }

    public Locale getLocale() {
        return locale;
    }
}
