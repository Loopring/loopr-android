package leaf.prod.app.activity.infomation;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.vondear.rxtool.view.RxToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.adapter.infomation.NewsInfoDetailAdapter;
import leaf.prod.app.utils.ButtonClickUtil;
import leaf.prod.app.utils.LyqbLogger;
import leaf.prod.app.utils.ShareUtil;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.manager.NewsDataManager;
import leaf.prod.walletsdk.model.response.crawler.IndexResult;
import leaf.prod.walletsdk.model.response.crawler.News;
import leaf.prod.walletsdk.service.CrawlerService;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NewsInfoActivity extends BaseActivity {

    @BindView(R.id.title)
    public TitleView title;

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    @BindView(R.id.tv_share)
    public TextView tvShare;

    @BindView(R.id.tv_pre)
    public TextView tvPre;

    @BindView(R.id.tv_next)
    public TextView tvNext;

    private NewsInfoDetailAdapter adapter;

    private static CrawlerService crawlerService;

    private News news;

    private List<News> newsList = new ArrayList();

    private int position = 1;

    private LinearLayoutManager layoutManager;

    private NewsDataManager newsDataManager;

    private LinearSnapHelper pagerSnapHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_news_info);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        if (crawlerService == null) {
            crawlerService = new CrawlerService();
        }
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.news));
        title.clickLeftGoBack(getWContext());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void initView() {
        newsDataManager = NewsDataManager.getInstance(this);
        news = (News) getIntent().getSerializableExtra("data");
        News preNews = newsDataManager.getPreNews(news);
        if (preNews == null) {
            position = 0;
        } else {
            newsList.add(preNews);
        }
        newsList.add(news);
        News nextNews = newsDataManager.getNextNews(news);
        if (nextNews != null) {
            newsList.add(newsDataManager.getNextNews(news));
        }
    }

    @Override
    public void initData() {
        layoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        pagerSnapHelper = new LinearSnapHelper();
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NewsInfoDetailAdapter(R.layout.adapter_news_info, newsList, this);
        recyclerView.setAdapter(adapter);
        pagerSnapHelper.attachToRecyclerView(recyclerView);
        layoutManager.scrollToPositionWithOffset(position, 0);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    @OnClick({R.id.cl_share, R.id.tv_pre, R.id.tv_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cl_share:
                if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
                    ShareUtil.uShareUrl(this, news.getTitle(), news.getUrl(), " ", new UMShareListener() {
                        @Override
                        public void onStart(SHARE_MEDIA platform) {
                        }

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResult(SHARE_MEDIA platform) {
                            RxToast.success(getResources().getString(R.string.share_success));
                            crawlerService.confirmForward(news.getUuid())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Subscriber<IndexResult>() {
                                        @Override
                                        public void onCompleted() {
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            LyqbLogger.log(e.getMessage());
                                        }

                                        @Override
                                        public void onNext(IndexResult indexResult) {
                                            tvShare.setText(getString(R.string.news_share) + " " + indexResult.getForwardNum());
                                        }
                                    });
                        }

                        @Override
                        public void onError(SHARE_MEDIA platform, Throwable t) {
                            if (t.getMessage().contains("2008")) {//错误码
                                RxToast.error(getResources().getString(R.string.share_failed_no_app));
                            } else {
                                RxToast.error(getResources().getString(R.string.share_failed, t.getMessage()));
                            }
                        }

                        @Override
                        public void onCancel(SHARE_MEDIA platform) {
                        }
                    });
                }
                break;
        }
    }

    public void goPre() {
        if (position == 0) {
            RxToast.error("到顶了");
            return;
        }
        position = adapter.goPre(position);
        newsList = adapter.getNewsList();
        //        recyclerView.smoothScrollToPosition(position);
        layoutManager.scrollToPositionWithOffset(position, 0);
    }

    public void goNext() {
        if (position == newsList.size() - 1) {
            RxToast.error("到底了");
            return;
        }
        position = adapter.goNext(position);
        newsList = adapter.getNewsList();
        //        recyclerView.smoothScrollToPosition(position);
        layoutManager.scrollToPositionWithOffset(position, 0);
    }
}
