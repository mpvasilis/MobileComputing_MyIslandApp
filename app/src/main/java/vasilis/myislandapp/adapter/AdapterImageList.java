package vasilis.myislandapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import vasilis.myislandapp.R;
import vasilis.myislandapp.api.RestAdapter;
import vasilis.myislandapp.model.Images;

public class AdapterImageList extends RecyclerView.Adapter<AdapterImageList.ViewHolder> {

    private List<Images> items = new ArrayList<>();
    private ImageLoader imgloader = ImageLoader.getInstance();
    private OnItemClickListener onItemClickListener;

    private int lastPosition = -1;

    public AdapterImageList(List<Images> items) {
        this.items = items;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final String p = items.get(position).getImageUrl();
        imgloader.displayImage(RestAdapter.getURLimgPlace(p), holder.image);
        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onItemClickListener.onItemClick(v, p, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, String viewModel, int pos);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public MaterialRippleLayout lyt_parent;

        public ViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }
}