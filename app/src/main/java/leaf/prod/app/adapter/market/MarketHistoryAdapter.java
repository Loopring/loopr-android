package leaf.prod.app.adapter.market;

import java.util.List;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import leaf.prod.app.R;
import leaf.prod.walletsdk.model.OrderFill;
import leaf.prod.walletsdk.util.DateUtil;
import leaf.prod.walletsdk.util.NumberUtils;

public class MarketHistoryAdapter extends BaseQuickAdapter<OrderFill, BaseViewHolder> {

    public MarketHistoryAdapter(int layoutResId, @Nullable List<OrderFill> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, OrderFill orderFill) {
        if (orderFill == null)  { return; }
        helper.setText(R.id.tv_price, NumberUtils.format1(orderFill.getPrice(), 8));
        helper.setText(R.id.tv_amount, NumberUtils.format7(orderFill.getAmount(), 0, 2));
        helper.setText(R.id.tv_time, DateUtil.formatDateTime(orderFill.getCreateTime() * 1000, "MM-dd HH:mm"));
    }
}