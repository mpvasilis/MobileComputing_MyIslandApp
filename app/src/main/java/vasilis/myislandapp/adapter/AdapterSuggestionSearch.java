package vasilis.myislandapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vasilis.myislandapp.R;


public class AdapterSuggestionSearch extends RecyclerView.Adapter<AdapterSuggestionSearch.ViewHolder> {

    private static final String SEARCH_HISTORY_KEY = "_SEARCH_HISTORY_KEY";
    private static final int MAX_HISTORY_ITEMS = 5;

    private List<String> items = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private SharedPreferences prefs;

    public AdapterSuggestionSearch(Context context) {
        prefs = context.getSharedPreferences("PREF_RECENT_SEARCH", Context.MODE_PRIVATE);
        this.items = getSearchHistory();
        Collections.reverse(this.items);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggestion, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String p = items.get(position);
        final int pos = position;
        holder.title.setText(p);
        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onItemClickListener.onItemClick(v, p, pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void refreshItems() {
        this.items = getSearchHistory();
        Collections.reverse(this.items);
        notifyDataSetChanged();
    }

    public void addSearchHistory(String s) {
        SearchObject searchObject = new SearchObject(getSearchHistory());
        searchObject.items.remove(s);
        searchObject.items.add(s);
        if (searchObject.items.size() > MAX_HISTORY_ITEMS) searchObject.items.remove(0);
        String json = new Gson().toJson(searchObject, SearchObject.class);
        prefs.edit().putString(SEARCH_HISTORY_KEY, json).apply();
    }

    private List<String> getSearchHistory() {
        String json = prefs.getString(SEARCH_HISTORY_KEY, "");
        if (json.equals("")) return new ArrayList<>();
        SearchObject searchObject = new Gson().fromJson(json, SearchObject.class);
        return searchObject.items;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, String viewModel, int pos);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public LinearLayout lyt_parent;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }

    private class SearchObject implements Serializable {
        public List<String> items = new ArrayList<>();

        public SearchObject(List<String> items) {
            this.items = items;
        }
    }
}