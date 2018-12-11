package leaf.prod.app.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scwang.smartrefresh.layout.api.RefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import leaf.prod.app.R;
import leaf.prod.app.activity.P2PRecordDetailActivity;
import leaf.prod.app.adapter.NoDataAdapter;
import leaf.prod.app.adapter.P2PRecordAdapter;
import leaf.prod.app.utils.LyqbLogger;
import leaf.prod.walletsdk.manager.P2POrderDataManager;
import leaf.prod.walletsdk.model.Order;
import leaf.prod.walletsdk.model.OrderType;
import leaf.prod.walletsdk.model.response.relay.PageWrapper;
import leaf.prod.walletsdk.util.WalletUtil;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class P2PRecordsFragment extends BaseFragment {

    Unbinder unbinder;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.refresh_layout)
    RefreshLayout refreshLayout;

    private P2PRecordAdapter recordAdapter;

    private NoDataAdapter emptyAdapter;

    private P2POrderDataManager p2pManager;

    private List<Order> orderList = new ArrayList<>();

    private int currentPageIndex = 1, pageSize = 20, totalCount = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 布局导入
        layout = inflater.inflate(R.layout.fragment_p2p_records, container, false);
        unbinder = ButterKnife.bind(this, layout);
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            refreshOrders(1);
        });
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void initData() {
        p2pManager = P2POrderDataManager.getInstance(getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recordAdapter = new P2PRecordAdapter(R.layout.adapter_item_p2p_record, null, this);
        recordAdapter.setOnLoadMoreListener(() -> {
            if (recordAdapter.getData().size() >= totalCount) {
                recordAdapter.loadMoreEnd();
            } else {
                refreshOrders(currentPageIndex + 1);
            }
        }, recyclerView);
        recyclerView.setAdapter(recordAdapter);
        refreshOrders(1);
        recordAdapter.setOnItemClickListener((adapter, view, position) -> {
            getOperation().addParameter("order", orderList.get(position));
            getOperation().forward(P2PRecordDetailActivity.class);
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void refreshOrders(int page) {
        currentPageIndex = page == 0 ? currentPageIndex : page;
        p2pManager.getLoopringService()
                .getOrders(WalletUtil.getCurrentAddress(getContext()), OrderType.P2P.getDescription(), currentPageIndex, pageSize)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PageWrapper<Order>>() {
                    @Override
                    public void onCompleted() {
                        refreshLayout.finishRefresh(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        LyqbLogger.log(e.getMessage());
                        recyclerView.setAdapter(emptyAdapter);
                        emptyAdapter.refresh();
                        refreshLayout.finishRefresh(true);
                        recordAdapter.loadMoreFail();
                        unsubscribe();
                    }

                    @Override
                    public void onNext(PageWrapper<Order> orderPageWrapper) {
                        totalCount = orderPageWrapper.getTotal();
                        orderList = orderPageWrapper.getData();
                        if (currentPageIndex == 1) {
                            recordAdapter.setNewData(orderList);
                        } else {
                            recordAdapter.addData(orderList);
                        }
                        refreshLayout.finishRefresh(true);
                        recordAdapter.loadMoreComplete();
                        unsubscribe();
                    }
                });
    }
}
