package com.tomcat360.lyqb.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.tomcat360.lyqb.R;
import com.tomcat360.lyqb.adapter.TokenListAdapter;
import com.tomcat360.lyqb.adapter.TokenListSearchAdapter;
import com.tomcat360.lyqb.utils.MyViewUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TokenListSearchActivity extends BaseActivity {


    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.left_btn)
    ImageView leftBtn;
    @BindView(R.id.et_search)
    EditText etSearch;
    @BindView(R.id.right_text)
    TextView rightText;
    private TokenListSearchAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_token_list_search);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        MyViewUtils.hideInput(getWindow().getDecorView());

    }

    @Override
    public void initTitle() {

    }

    @Override
    public void initView() {

        final Drawable drawable= getResources().getDrawable(R.mipmap.icon_search_small);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count != 0) {
                    etSearch.setCompoundDrawables(null, null, null, null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    etSearch.setCompoundDrawables(drawable, null, null, null);

                }
            }
        });
    }

    @Override
    public void initData() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<String> list = new ArrayList<>();
        
        mAdapter = new TokenListSearchAdapter(R.layout.adapter_item_token_list, list);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

            }
        });

    }

    @OnClick({R.id.left_btn, R.id.right_text})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.left_btn:
                finish();
                break;
            case R.id.right_text:
                finish();
                break;
        }
    }
}
