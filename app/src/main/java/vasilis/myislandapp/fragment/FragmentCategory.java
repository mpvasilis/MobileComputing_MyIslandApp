package vasilis.myislandapp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import vasilis.myislandapp.ActivityMain;
import vasilis.myislandapp.ActivityPlaceDetail;
import vasilis.myislandapp.R;
import vasilis.myislandapp.adapter.AdapterPlaceGrid;
import vasilis.myislandapp.api.RestAdapter;
import vasilis.myislandapp.api.callbacks.CallbackListPlace;
import vasilis.myislandapp.data.DatabaseHandler;
import vasilis.myislandapp.data.SharedPref;
import vasilis.myislandapp.data.ThisApplication;
import vasilis.myislandapp.model.Place;
import vasilis.myislandapp.utils.Tools;
import vasilis.myislandapp.widget.SpacingItemDecoration;

public class FragmentCategory extends Fragment {

    public static String TAG_CATEGORY = "key.TAG_CATEGORY";

    private int count_total = 0;
    private int category_id;

    private View root_view;
    private RecyclerView recyclerView;
    private View lyt_progress;
    private View lyt_not_found;
    private TextView text_progress;
    private Snackbar snackbar_retry;

    private DatabaseHandler db;
    private SharedPref sharedPref;
    private AdapterPlaceGrid adapter;

    private Call<CallbackListPlace> callback;
    private boolean onProcess = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_category, null);

        // activate fragment menu
        setHasOptionsMenu(true);

        db = new DatabaseHandler(getActivity());
        sharedPref = new SharedPref(getActivity());
        category_id = getArguments().getInt(TAG_CATEGORY);

        recyclerView = root_view.findViewById(R.id.recycler);
        lyt_progress = root_view.findViewById(R.id.lyt_progress);
        lyt_not_found = root_view.findViewById(R.id.lyt_not_found);
        text_progress = root_view.findViewById(R.id.text_progress);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(Tools.getGridSpanCount(getActivity()), StaggeredGridLayoutManager.VERTICAL));
        recyclerView.addItemDecoration(new SpacingItemDecoration(Tools.getGridSpanCount(getActivity()), Tools.dpToPx(getActivity(), 4), true));

        //set data and list adapter
        adapter = new AdapterPlaceGrid(getActivity(), recyclerView, new ArrayList<Place>());
        recyclerView.setAdapter(adapter);

        // on item list clicked
        adapter.setOnItemClickListener(new AdapterPlaceGrid.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Place obj) {
                ActivityPlaceDetail.navigate((ActivityMain) getActivity(), v.findViewById(R.id.lyt_content), obj);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView v, int state) {
                super.onScrollStateChanged(v, state);
                if (state == RecyclerView.SCROLL_STATE_DRAGGING || state == RecyclerView.SCROLL_STATE_SETTLING) {
                    ActivityMain.animateFab(true);
                } else {
                    ActivityMain.animateFab(false);
                }
            }
        });
        startLoadMoreAdapter();
        return root_view;
    }

    @Override
    public void onDestroyView() {
        if (snackbar_retry != null) snackbar_retry.dismiss();
        if (callback != null && callback.isExecuted()) {
            callback.cancel();
        }
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (sharedPref.isRefreshPlaces() || db.getPlacesSize() == 0) {
            actionRefresh(sharedPref.getLastPlacePage());
        } else {
            startLoadMoreAdapter();
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_category, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            ThisApplication.getInstance().setLocation(null);
            sharedPref.setLastPlacePage(1);
            sharedPref.setRefreshPlaces(true);
            text_progress.setText("");
            if (snackbar_retry != null) snackbar_retry.dismiss();
            actionRefresh(sharedPref.getLastPlacePage());
        }
        return super.onOptionsItemSelected(item);
    }

    private void startLoadMoreAdapter() {
        adapter.resetListData();
        List<Place> items = db.getPlacesByPage(category_id, 50, 0);
        adapter.insertData(items);
        showNoItemView();
        final int item_count = db.getPlacesSize(category_id);
        // detect when scroll reach bottom
        adapter.setOnLoadMoreListener(new AdapterPlaceGrid.OnLoadMoreListener() {
            @Override
            public void onLoadMore(final int current_page) {
                if (item_count > adapter.getItemCount() && current_page != 0) {
                    displayDataByPage(current_page);
                } else {
                    adapter.setLoaded();
                }
            }
        });
    }

    private void displayDataByPage(final int next_page) {
        adapter.setLoading();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Place> items = db.getPlacesByPage(category_id, 50, (next_page * 50));
                adapter.insertData(items);
                showNoItemView();
            }
        }, 500);
    }

    // checking some condition before perform refresh data
    private void actionRefresh(int page_no) {
        boolean conn = Tools.cekConnection(getActivity());
        if (conn) {
            if (!onProcess) {
                onRefresh(page_no);
            } else {
                Snackbar.make(root_view, R.string.task_running, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            onFailureRetry(page_no, getString(R.string.no_internet));
        }
    }

    private void onRefresh(final int page_no) {
        onProcess = true;
        showProgress(onProcess);
        callback = RestAdapter.createAPI().getPlacesByPage(page_no, 50, 0);
        callback.enqueue(new retrofit2.Callback<CallbackListPlace>() {
            @Override
            public void onResponse(Call<CallbackListPlace> call, Response<CallbackListPlace> response) {
                CallbackListPlace resp = response.body();
                if (resp != null) {
                    count_total = resp.count_total;
                    if (page_no == 1) db.refreshTablePlace();
                    db.insertListPlace(resp.places);  // save result into database
                    sharedPref.setLastPlacePage(page_no + 1);
                    delayNextRequest(page_no);
                    String str_progress = String.format(getString(R.string.load_of), (page_no * 50), count_total);
                    text_progress.setText(str_progress);
                } else {
                    onFailureRetry(page_no, getString(R.string.refresh_failed));
                }
            }

            @Override
            public void onFailure(Call<CallbackListPlace> call, Throwable t) {
                if (call != null && !call.isCanceled()) {
                    Log.e("onFailure", t.getMessage());
                    boolean conn = Tools.cekConnection(getActivity());
                    if (conn) {
                        onFailureRetry(page_no, getString(R.string.refresh_failed));
                    } else {
                        onFailureRetry(page_no, getString(R.string.no_internet));
                    }
                }
            }
        });
    }

    private void showProgress(boolean show) {
        if (show) {
            lyt_progress.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            lyt_not_found.setVisibility(View.GONE);
        } else {
            lyt_progress.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showNoItemView() {
        if (adapter.getItemCount() == 0) {
            lyt_not_found.setVisibility(View.VISIBLE);
        } else {
            lyt_not_found.setVisibility(View.GONE);
        }
    }

    private void onFailureRetry(final int page_no, String msg) {
        onProcess = false;
        showProgress(onProcess);
        showNoItemView();
        startLoadMoreAdapter();
        snackbar_retry = Snackbar.make(root_view, msg, Snackbar.LENGTH_INDEFINITE);
        snackbar_retry.setAction(R.string.RETRY, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionRefresh(page_no);
            }
        });
        snackbar_retry.show();
    }

    private void delayNextRequest(final int page_no) {
        if (count_total == 0) {
            onFailureRetry(page_no, getString(R.string.refresh_failed));
            return;
        }
        if ((page_no * 50) > count_total) { // when all data loaded
            onProcess = false;
            showProgress(onProcess);
            startLoadMoreAdapter();
            sharedPref.setRefreshPlaces(false);
            text_progress.setText("");
            Snackbar.make(root_view, R.string.load_success, Snackbar.LENGTH_LONG).show();
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onRefresh(page_no + 1);
            }
        }, 500);
    }
}
