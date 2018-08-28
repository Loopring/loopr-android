package com.tomcat360.lyqb.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.tomcat360.lyqb.R;
import com.tomcat360.lyqb.adapter.TokenListAdapter;
import com.tomcat360.lyqb.utils.SPUtils;
import com.tomcat360.lyqb.views.TitleView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TokenListActivity extends BaseActivity {

    @BindView(R.id.title)
    TitleView title;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    private TokenListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_token_list);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.tokens));
        title.clickLeftGoBack(getWContext());
        title.setRightImageButton(R.mipmap.icon_search, new TitleView.OnRightButtonClickListener() {
            @Override
            public void onClick(View button) {
                getOperation().forward(TokenListSearchActivity.class);
            }
        });
    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<String> list = new ArrayList<>();
        mAdapter = new TokenListAdapter(R.layout.adapter_item_token_list, null);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

            }
        });


        getToken();
    }

    private void getToken() {
        String address = (String) SPUtils.get(this, "address", "");
//        Observable<BalanceResult> balance =APP.getLooprSocket().getBalance(address);
//        balance.observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<BalanceResult>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onNext(BalanceResult balanceResult) {
//                        LyqbLogger.log(balanceResult.toString());
//                        mAdapter.setNewData(balanceResult.getTokens());
//                    }
//                });
    }
}
