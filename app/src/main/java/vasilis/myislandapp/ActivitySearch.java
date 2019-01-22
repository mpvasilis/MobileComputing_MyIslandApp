package vasilis.myislandapp;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import vasilis.myislandapp.adapter.AdapterPlaceGrid;
import vasilis.myislandapp.adapter.AdapterSuggestionSearch;
import vasilis.myislandapp.data.DatabaseHandler;
import vasilis.myislandapp.model.Place;
import vasilis.myislandapp.utils.SpacingItemDecoration;
import vasilis.myislandapp.utils.Tools;

public class ActivitySearch extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar actionBar;
    private EditText et_search;
    private ImageButton bt_clear;
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().length() == 0) {
                bt_clear.setVisibility(View.GONE);
            } else {
                bt_clear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };
    private View parent_view;
    private RecyclerView recyclerView;
    private AdapterPlaceGrid mAdapter;
    private RecyclerView recyclerSuggestion;
    private AdapterSuggestionSearch mAdapterSuggestion;
    private LinearLayout lyt_suggestion;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        parent_view = findViewById(android.R.id.content);
        db = new DatabaseHandler(this);

        initComponent();
        setupToolbar();
    }

    private void initComponent() {
        lyt_suggestion = findViewById(R.id.lyt_suggestion);
        et_search = findViewById(R.id.et_search);
        et_search.addTextChangedListener(textWatcher);

        bt_clear = findViewById(R.id.bt_clear);
        bt_clear.setVisibility(View.GONE);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerSuggestion = findViewById(R.id.recyclerSuggestion);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(Tools.getGridSpanCount(this), StaggeredGridLayoutManager.VERTICAL));
        recyclerView.addItemDecoration(new SpacingItemDecoration(Tools.getGridSpanCount(this), Tools.dpToPx(this, 4), true));


        recyclerSuggestion.setLayoutManager(new LinearLayoutManager(this));
        recyclerSuggestion.setHasFixedSize(true);

        //set data and list adapter
        mAdapter = new AdapterPlaceGrid(this, recyclerView, new ArrayList<Place>());
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new AdapterPlaceGrid.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Place obj) {
                ActivityPlaceDetail.navigate(ActivitySearch.this, v.findViewById(R.id.lyt_content), obj);
            }
        });

        //set data and list adapter suggestion
        mAdapterSuggestion = new AdapterSuggestionSearch(this);
        recyclerSuggestion.setAdapter(mAdapterSuggestion);
        showSuggestionSearch();
        mAdapterSuggestion.setOnItemClickListener(new AdapterSuggestionSearch.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String viewModel, int pos) {
                et_search.setText(viewModel);
                lyt_suggestion.setVisibility(View.GONE);
                hideKeyboard();
                searchAction();
            }
        });

        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search.setText("");
                mAdapter.resetListData();
                showNotFoundView();
            }
        });

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard();
                    searchAction();
                    return true;
                }
                return false;
            }
        });

        et_search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showSuggestionSearch();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return false;
            }
        });

        showNotFoundView();
    }

    private void showSuggestionSearch() {
        mAdapterSuggestion.refreshItems();
        lyt_suggestion.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        mAdapter.notifyDataSetChanged();
        super.onResume();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // for system bar in lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.grey_medium));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void searchAction() {
        lyt_suggestion.setVisibility(View.GONE);
        showNotFoundView();
        final String query = et_search.getText().toString().trim();
        if (!query.equals("")) {
            mAdapterSuggestion.addSearchHistory(query);
            mAdapter.resetListData();
            mAdapter.insertData(Tools.filterItemsWithDistance(this, db.searchBeaches(query)));
            showNotFoundView();
        } else {
            Toast.makeText(this, R.string.please_fill, Toast.LENGTH_SHORT).show();
        }
    }

    private void showNotFoundView() {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        if (mAdapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }
}
