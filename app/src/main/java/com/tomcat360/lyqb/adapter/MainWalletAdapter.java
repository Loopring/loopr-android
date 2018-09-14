package com.tomcat360.lyqb.adapter;

import java.util.List;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lyqb.walletsdk.model.response.data.BalanceResult;
import com.tomcat360.lyqb.R;
import com.tomcat360.lyqb.presenter.BasePresenter;

public class MainWalletAdapter extends BaseQuickAdapter<BalanceResult.Asset, BaseViewHolder> {

    private BasePresenter presenter;

    public MainWalletAdapter(int layoutResId, @Nullable List<BalanceResult.Asset> data, BasePresenter presenter) {
        super(layoutResId, data);
        this.presenter = presenter;
    }

    @Override
    protected void convert(BaseViewHolder helper, BalanceResult.Asset item) {
        helper.setText(R.id.wallet_title, item.getSymbol());
        helper.setText(R.id.wallet_name, item.getSymbol());
        helper.setText(R.id.wallet_money, item.getValue() + "");
        helper.setText(R.id.wallet_count, item.getLegalStr());
        //        if (SPUtils.get(mContext, "coin", "¥").equals("¥")) {
        //            helper.setText(R.id.wallet_count, "¥ " + item.getLegalValue());
        //        } else {
        //            helper.setText(R.id.wallet_count, "$ " + item.getLegalValue());
        //        }
    }
}
