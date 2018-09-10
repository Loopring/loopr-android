package com.tomcat360.lyqb.adapter;

import java.util.List;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.tomcat360.lyqb.R;

import android.support.annotation.Nullable;

/**
 *
 */
public class MnemonicWordHintAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public MnemonicWordHintAdapter(int layoutResId, @Nullable List<String> data) {

        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {

        helper.setText(R.id.mnemonic_word, Integer.valueOf(helper.getPosition()) + 1 + "." + item);

    }
}
