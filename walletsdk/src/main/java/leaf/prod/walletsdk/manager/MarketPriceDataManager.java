package leaf.prod.walletsdk.manager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import leaf.prod.walletsdk.model.Ticker;
import leaf.prod.walletsdk.model.TradingPair;
import leaf.prod.walletsdk.model.UserConfig;
import leaf.prod.walletsdk.service.LoopringService;
import leaf.prod.walletsdk.util.CurrencyUtil;
import leaf.prod.walletsdk.util.NumberUtils;
import leaf.prod.walletsdk.util.StringUtils;

public class MarketPriceDataManager {

    private Context context;

    private boolean isFiltering;

    private List<Ticker> tickers;

    private List<Ticker> filteredTickers;

    protected LoopringService loopringService;

    private static MarketPriceDataManager marketDataManager;

    private MarketPriceDataManager(Context context) {
        this.context = context;
        this.isFiltering = false;
        this.tickers = new ArrayList<>();
        this.filteredTickers = new ArrayList<>();
        this.loopringService = new LoopringService();
    }

    public static MarketPriceDataManager getInstance(Context context) {
        if (marketDataManager == null) {
            marketDataManager = new MarketPriceDataManager(context);
        }
        return marketDataManager;
    }

    public void convertTickers(List<Ticker> tickers) {
        this.tickers.clear();
        for (Ticker ticker : tickers) {
            Ticker newTicker = Ticker.builder()
                    .market(ticker.getMarket())
                    .exchange(ticker.getExchange())
                    .tradingPair(getTradingPair(ticker))
                    .balanceShown(NumberUtils.format1(ticker.getLast(), ticker.getDecimals()))
                    .vol(ticker.getVol())
                    .change(getChange(ticker.getChange()))
                    .tag(ticker.getTag())
                    .market(ticker.getMarket())
                    .decimals(ticker.getDecimals())
                    .currencyShown(getCurrency(ticker))
                    .open(ticker.getOpen())
                    .close(ticker.getClose())
                    .last(ticker.getLast())
                    .low(ticker.getLow())
                    .high(ticker.getHigh())
                    .buy(ticker.getBuy())
                    .sell(ticker.getSell())
                    .build();
            this.tickers.add(newTicker);
        }
    }

    private TradingPair getTradingPair(Ticker ticker) {
        String[] tokens = ticker.getMarket().split("-");
        String tradingPair = tokens[0] + "-" + tokens[1];
        return TradingPair.builder().tokenA(tokens[0]).tokenB(tokens[1]).description(tradingPair).build();
    }

    private String getChange(String data) {
        String result = "--";
        if (data != null && !StringUtils.isEmpty(data)) {
            if (data.startsWith("-")) {
                result = "↓" + data.replace("-", "");
            } else {
                result = "↑" + data;
            }
        }
        return result;
    }

    private String getCurrency(Ticker ticker) {
        String tokenA = ticker.getMarket().split("-")[0];
        Double price = MarketcapDataManager.getInstance(context).getPriceBySymbol(tokenA);
        return CurrencyUtil.format(context, price);
    }

    public LoopringService getLoopringService() {
        return loopringService;
    }

    public List<Ticker> getTickers() {
        return tickers;
    }

    public List<Ticker> getTickersBy(String token) {
        List<Ticker> result = new ArrayList<>();
        List<Ticker> tickers = this.isFiltering ? this.filteredTickers : this.tickers;
        for (Ticker ticker : tickers) {
            if (ticker.getTradingPair().getTokenB().equalsIgnoreCase(token)) {
                result.add(ticker);
            }
        }
        return result;
    }

    private Ticker getTickersBy(TradingPair pair) {
        Ticker result = null;
        List<Ticker> tickers = this.isFiltering ? this.filteredTickers : this.tickers;
        for (Ticker ticker : tickers) {
            if (ticker.getTradingPair().equals(pair)) {
                result = ticker;
            }
        }
        return result;
    }

    public List<Ticker> getFavTickers() {
        List<Ticker> result = new ArrayList<>();
        UserConfig config = LoginDataManager.getInstance(context).getLocalUser();
        if (config != null) {
            for (TradingPair pair : config.getFavMarkets()) {
                if (getTickersBy(pair) != null) {
                    result.add(getTickersBy(pair));
                }
            }
        }
        return result;
    }

    public void setFiltering(boolean filtering) {
        isFiltering = filtering;
    }

    public void setFilteredTickers(List<Ticker> filteredTickers) {
        this.filteredTickers = filteredTickers;
    }
}
