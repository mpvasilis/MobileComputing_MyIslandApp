package vasilis.myislandapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import vasilis.myislandapp.R;
import vasilis.myislandapp.api.RestAdapter;
import vasilis.myislandapp.model.Place;
import vasilis.myislandapp.utils.GPSLocation;

public class AdapterPlaceGrid extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private boolean loading;

    private Context ctx;
    private List<Place> items = new ArrayList<>();
    private ImageLoader imgloader = ImageLoader.getInstance();

    private OnLoadMoreListener onLoadMoreListener;
    private OnItemClickListener onItemClickListener;

    private int lastPosition = -1;
    private boolean clicked = false;


    public AdapterPlaceGrid(Context ctx, RecyclerView view, List<Place> items) {
        this.ctx = ctx;
        this.items = items;
        if (!imgloader.isInited()) GPSLocation.initImageLoader(ctx);
        lastItemViewDetector(view);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
            vh = new ViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder vItem = (ViewHolder) holder;
            final Place p = items.get(position);
            vItem.title.setText(p.name);
            imgloader.displayImage(RestAdapter.getURLimgPlace(p.image), vItem.image, GPSLocation.getGridOption());

            if (p.distance == -1) {
                vItem.lyt_distance.setVisibility(View.GONE);
            } else {
                vItem.lyt_distance.setVisibility(View.VISIBLE);
                vItem.distance.setText(GPSLocation.getFormatedDistance(p.distance));
            }

            vItem.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (!clicked && onItemClickListener != null) {
                        clicked = true;
                        onItemClickListener.onItemClick(v, p);
                    }
                }
            });
            clicked = false;
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
        if (getItemViewType(position) == VIEW_PROG) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        } else {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(false);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return this.items.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void insertData(List<Place> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null);
            notifyItemInserted(getItemCount() - 1);
        }
    }

    public void resetListData() {
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = getLastVisibleItem(layoutManager.findLastVisibleItemPositions(null));
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        int current_page = getItemCount() / 50;
                        onLoadMoreListener.onLoadMore(current_page);
                        loading = true;
                    }
                }
            });
        }
    }

    private int getLastVisibleItem(int[] into) {
        int last_idx = into[0];
        for (int i : into) {
            if (last_idx < i) last_idx = i;
        }
        return last_idx;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Place viewModel);
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar1);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView image;
        public TextView distance;
        public LinearLayout lyt_distance;
        public MaterialRippleLayout lyt_parent;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            image = v.findViewById(R.id.image);
            distance = v.findViewById(R.id.distance);
            lyt_distance = v.findViewById(R.id.lyt_distance);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }

}