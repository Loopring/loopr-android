package leaf.prod.app.presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.content.Context;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.xw.repo.BubbleSeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import leaf.prod.app.R;
import leaf.prod.app.fragment.P2PTradeFragment;
import leaf.prod.walletsdk.manager.BalanceDataManager;
import leaf.prod.walletsdk.manager.MarketcapDataManager;
import leaf.prod.walletsdk.manager.P2POrderDataManager;
import leaf.prod.walletsdk.model.response.relay.BalanceResult;
import leaf.prod.walletsdk.util.NumberUtils;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-11-29 5:42 PM
 * Cooperation: loopring.org 路印协议基金会
 */
public class P2PTradePresenter extends BasePresenter<P2PTradeFragment> {

    @BindView(R.id.first_token)
    TextView firstTokenView;

    @BindView(R.id.second_token)
    TextView secondTokenView;

    @BindView(R.id.token_s_price)
    TextView tokenSPrice;

    @BindView(R.id.token_b_price)
    TextView tokenBPrice;

    @BindView(R.id.sell_token)
    TextView sellTokenView;

    @BindView(R.id.buy_token)
    TextView buyTokenView;

    @BindView(R.id.seek_bar)
    BubbleSeekBar seekBar;

    @BindView(R.id.hint_text)
    TextView hintText;

    @BindView(R.id.sell_amount)
    MaterialEditText sellAmount;

    @BindView(R.id.buy_amount)
    MaterialEditText buyAmount;

    private String sellToken = "WETH", buyToken = "LRC", sellPrice = "0", buyPrice = "0";

    private List<TextView> intervalList;

    private MarketcapDataManager marketcapDataManager;

    private BalanceDataManager balanceDataManager;

    private P2POrderDataManager p2POrderDataManager;

    private OptionsPickerView datePickerView, miniCountPickerView;

    public P2PTradePresenter(P2PTradeFragment view, Context context) {
        super(view, context);
        ButterKnife.bind(this, Objects.requireNonNull(view.getView()));
        marketcapDataManager = MarketcapDataManager.getInstance(context);
        balanceDataManager = BalanceDataManager.getInstance(context);
        //        p2POrderDataManager = P2POrderDataManager.getInstance(context);
        initTokens("WETH", "LRC");
    }

    public void initTokens(String first, String second) {
        sellToken = first.isEmpty() ? sellToken : first;
        buyToken = second.isEmpty() ? buyToken : second;
        firstTokenView.setText(sellToken);
        secondTokenView.setText(buyToken);
        sellTokenView.setText(sellToken);
        buyTokenView.setText(buyToken);
        Double tokenPrice1 = marketcapDataManager.getPriceBySymbol(sellToken), tokenPrice2 = marketcapDataManager.getPriceBySymbol(buyToken);
        sellPrice = NumberUtils.format1(tokenPrice1 / tokenPrice2, 8);
        buyPrice = NumberUtils.format1(tokenPrice2 / tokenPrice1, 8);
        tokenSPrice.setText(" 1 " + sellToken + " ≈ " + sellPrice + " " + buyToken);
        tokenBPrice.setText("1 " + buyToken + " ≈ " + buyPrice + " " + sellToken);
        hintText.setText(view.getResources()
                .getString(R.string.available_balance, balanceDataManager.getAssetBySymbol(sellToken).getValueShown()));
    }

    public void switchToken() {
        String tToken = sellToken, tPrice = sellPrice;
        firstTokenView.setText(buyToken);
        secondTokenView.setText(sellToken);
        tokenBPrice.setText(" 1 " + sellToken + " ≈ " + sellPrice + " " + buyToken);
        tokenSPrice.setText("1 " + buyToken + " ≈ " + buyPrice + " " + sellToken);
        sellTokenView.setText(buyToken);
        buyTokenView.setText(sellToken);
        sellToken = buyToken;
        buyToken = tToken;
        sellPrice = buyPrice;
        buyPrice = tPrice;
        sellAmount.setText("");
        buyAmount.setText("");
        seekBar.setProgress(0);
    }

    /**
     * 金额拖动条
     */
    public void initSeekbar() {
        seekBar.setProgress(0);
        seekBar.setCustomSectionTextArray((sectionCount, array) -> {
            array.clear();
            array.put(0, "0%");
            array.put(1, "25%");
            array.put(2, "50%");
            array.put(3, "75%");
            array.put(4, "100%");
            return array;
        });
        seekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                BalanceResult.Asset asset = balanceDataManager.getAssetBySymbol(sellToken);
                sellAmount.setText(NumberUtils.format1(asset.getValue() * progressFloat / 100, asset.getPrecision()));
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
            }
        });
    }

    public void setInterval(int index) {
        if (intervalList == null) {
            intervalList = new ArrayList<>();
            intervalList.add(view.getView().findViewById(R.id.one_hour));
            intervalList.add(view.getView().findViewById(R.id.one_day));
            intervalList.add(view.getView().findViewById(R.id.one_month));
            intervalList.add(view.getView().findViewById(R.id.custom));
        }
        List<Integer> dates = new ArrayList<>();
        List<String> dateTypes = new ArrayList<>();
        for (int i = 1; i <= 24; ++i) {
            dates.add(i);
        }
        dateTypes.add(view.getResources().getString(R.string.hour, ""));
        dateTypes.add(view.getResources().getString(R.string.day, ""));
        dateTypes.add(view.getResources().getString(R.string.month, ""));
        for (int i = 0; i < intervalList.size(); ++i) {
            intervalList.get(i)
                    .setTextColor(i == index ? view.getResources().getColor(R.color.colorWhite) : view.getResources()
                            .getColor(R.color.colorNineText));
        }
        if (index == 3) {
            if (datePickerView == null) {
                datePickerView = new OptionsPickerBuilder(context, (options1, options2, options3, v) -> {
                }).setBgColor(view.getResources().getColor(R.color.colorTitleBac))
                        .setTitleBgColor(view.getResources().getColor(R.color.colorTitleBac))
                        .setTitleText(view.getResources().getString(R.string.expiry_date))
                        .setTitleColor(view.getResources().getColor(R.color.colorNineText))
                        .setCancelColor(view.getResources().getColor(R.color.colorNineText))
                        .setDividerColor(view.getResources().getColor(R.color.colorBg))
                        .setTextColorCenter(view.getResources().getColor(R.color.colorWhite))
                        .setTitleSize(14)
                        .setSubCalSize(14)
                        .setContentTextSize(15)
                        .setLineSpacingMultiplier(2)
                        .setOptionsSelectChangeListener((options1, options2, options3) -> {
                            dates.clear();
                            int amount = options2 == 0 ? 24 : (options2 == 1 ? 30 : 12);
                            for (int i = 1; i <= amount; ++i) {
                                dates.add(i);
                            }
                            datePickerView.setSelectOptions(0, options2);
                            datePickerView.setNPicker(dates, dateTypes, null);
                        })
                        .build();
                datePickerView.setNPicker(dates, dateTypes, null);
            }
            datePickerView.show();
        }
    }

    public void calMarketSell() {
        try {
            Double amountS = Double.parseDouble(sellAmount.getText().toString());
            Double priceS = Double.parseDouble(sellPrice);
            int precision = balanceDataManager.getAssetBySymbol(buyToken).getPrecision();
            buyAmount.setText(NumberUtils.format1(amountS * priceS, precision));
        } catch (Exception e) {
            buyAmount.setText("0");
        }
    }
}
