package leaf.prod.app.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.adapter.ContactListAdapter;
import leaf.prod.app.adapter.NoDataAdapter;
import leaf.prod.app.layout.IndexBarLayout;
import leaf.prod.app.presenter.ContactListPresenter;
import leaf.prod.app.views.RecyclerViewBugLayoutManager;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.manager.LoginDataManager;
import leaf.prod.walletsdk.model.Contact;
import leaf.prod.walletsdk.model.NoDataType;
import leaf.prod.walletsdk.model.UserConfig;
import leaf.prod.walletsdk.util.ChineseCharUtil;

public class ContactListActivity extends BaseActivity {

    @BindView(R.id.title)
    public TitleView title;

    @BindView(R.id.ll_index)
    public IndexBarLayout llIndex;

    @BindView(R.id.ll_search)
    public LinearLayout llSearch;

    @BindView(R.id.tv_cancel)
    public TextView tvCancel;

    @BindView(R.id.iv_left)
    public ImageView ivLeft;

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    @BindView(R.id.et_search)
    public EditText etSearch;

    private ContactListPresenter presenter;

    private ContactListAdapter adapter;

    private NoDataAdapter emptyAdapter;

    public Map<String, Integer> indexMap = new HashMap<>();

    public Map<String, TextView> tvMap = new HashMap<>();

    public RecyclerViewBugLayoutManager layoutManager;

    private InputMethodManager imm;

    private List<Contact> contactList = new ArrayList<>();

    private List<Contact> searchList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_contact_list);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        setSwipeBackEnable(false);
    }

    @Override
    protected void initPresenter() {
        presenter = new ContactListPresenter(this, this);
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.contact_list));
        title.clickLeftGoBack(getWContext());
        title.setMiddleImageButton(R.mipmap.icon_plus, button -> startActivityForResult(new Intent(this, AddContactActivity.class), 1));
        title.setRightImageButton(R.mipmap.icon_search, button -> {
            llIndex.setVisibility(View.INVISIBLE);
            llSearch.setVisibility(View.VISIBLE);
            etSearch.setFocusable(true);
            etSearch.requestFocus();
            imm.showSoftInput(etSearch, 0);
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchList.clear();
                for (Contact contact : contactList) {
                    if (contact.getName().toUpperCase().contains(s.toString().toUpperCase())) {
                        searchList.add(contact);
                    }
                }
                adapter.setNewData(searchList);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void initView() {
        imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        layoutManager = new RecyclerViewBugLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        presenter.initIndexBar();
        adapter = new ContactListAdapter(R.layout.adapter_item_contact_list, contactList);
        emptyAdapter = new NoDataAdapter(R.layout.adapter_item_no_data, null, NoDataType.contact);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                int currentPos = layoutManager.findFirstVisibleItemPosition();
                if (currentPos > 0 && contactList.size() > currentPos) {
                    presenter.highLightBar(contactList.get(currentPos).getTag());
                }
            }
        });
        adapter.setOnItemClickListener((adapter, view, position) -> {
            Intent intent = new Intent();
            intent.putExtra("address", contactList.get(position).getAddress());
            setResult(RESULT_OK, intent);
            finish();
        });
        refreshContacts();
    }

    @Override
    public void initData() {
    }

    @OnClick({R.id.tv_cancel, R.id.iv_left})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
                etSearch.setText("");
                llSearch.setVisibility(View.GONE);
                llIndex.setVisibility(View.VISIBLE);
                adapter.setNewData(contactList);
                break;
            case R.id.iv_left:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refreshContacts();
    }

    private void refreshContacts() {
        loginDataManager = LoginDataManager.getInstance(this);
        UserConfig userConfig = loginDataManager.getLocalUser();
        contactList.clear();
        contactList.addAll(userConfig != null && userConfig.getContacts() != null ? userConfig.getContacts() : Collections
                .emptyList());
        if (contactList.isEmpty()) {
            llIndex.setVisibility(View.INVISIBLE);
            recyclerView.setAdapter(emptyAdapter);
            emptyAdapter.refresh();
        } else {
            llIndex.setVisibility(View.VISIBLE);
            Collections.sort(contactList, (contact, t1) -> contact.getTag().compareTo(t1.getTag()));
            indexMap.clear();
            for (int i = 0; i < contactList.size(); ++i) {
                if (indexMap.get(contactList.get(i).getTag()) == null) {
                    indexMap.put(contactList.get(i).getTag(), i);
                }
            }
            adapter.setNewData(contactList);
            recyclerView.setAdapter(adapter);
            presenter.highLightBar(contactList.get(0).getTag());
        }
    }

    private List<Contact> genLists() {
        List<Contact> list = new ArrayList<>();
        for (int j = 0; j < 27; ++j) {
            for (int i = 0; i < 3; ++i) {
                String name = j != 26 ? (char) ('A' + j) + "_" + i : "#_" + i;
                list.add(Contact.builder()
                        .name(name)
                        .address("0xA64B16a18885F00FA1AD6D3d3100C3E6F1CEf724")
                        .note(name)
                        .tag(ChineseCharUtil.getFirstLetter(name))
                        .build());
            }
        }
        return list;
    }
}
