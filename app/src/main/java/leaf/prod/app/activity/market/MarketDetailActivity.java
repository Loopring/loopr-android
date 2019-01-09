/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-11-29 2:23 PM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.app.activity.market;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.WindowManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.adapter.ViewPageAdapter;
import leaf.prod.app.fragment.market.MarketDepthFragment;
import leaf.prod.app.fragment.market.MarketHistoryFragment;
import leaf.prod.app.presenter.market.MarketDetailPresenter;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.manager.MarketOrderDataManager;

public class MarketDetailActivity extends BaseActivity {

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.market_tab)
    TabLayout tradeTab;

    @BindView(R.id.view_pager)
    public ViewPager viewPager;

    @BindView(R.id.cl_loading)
    public ConstraintLayout clLoading;

    private List<Fragment> fragments;

    private MarketOrderDataManager orderDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_market_detail);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
        clLoading.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initPresenter() {
        orderDataManager = MarketOrderDataManager.getInstance(this);
        presenter = new MarketDetailPresenter(this, this, orderDataManager.getTradePair());
    }

    @Override
    public void initTitle() {
        title.setBTitle(orderDataManager.getTradePair());
        title.clickLeftGoBack(getWContext());
        title.setDropdownImageButton(R.mipmap.icon_dropdown, button -> {
            getOperation().forward(MarketSelectActivity.class);
        });
    }

    @Override
    public void initView() {
        String[] titles = new String[2];
        titles[0] = getString(R.string.order_submitted);
        titles[1] = getString(R.string.order_completed);
        fragments = new ArrayList<>();
        fragments.add(0, new MarketDepthFragment());
        fragments.add(1, new MarketHistoryFragment());
        setupViewPager(titles);
    }

    private void setupViewPager(String[] titles) {
        viewPager.setAdapter(new ViewPageAdapter(getSupportFragmentManager(), fragments, titles));
        tradeTab.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    MarketDepthFragment fragment = (MarketDepthFragment) fragments.get(position);
                    fragment.updateAdapter();
                } else if (position == 1) {
                    MarketHistoryFragment fragment = (MarketHistoryFragment) fragments.get(position);
                    fragment.updateAdapter();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void initData() {
    }

    public void updateAdapter(int index) {
        if (index == 0) {
            MarketDepthFragment depthFragment = (MarketDepthFragment) fragments.get(0);
            if (depthFragment != null) {
                depthFragment.updateAdapter();
            }
        } else if (index == 1) {
            MarketHistoryFragment historyFragment = (MarketHistoryFragment) fragments.get(1);
            if (historyFragment != null) {
                historyFragment.updateAdapter();
            }
        }
    }
}
