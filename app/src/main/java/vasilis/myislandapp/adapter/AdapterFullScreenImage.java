package vasilis.myislandapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import vasilis.myislandapp.R;
import vasilis.myislandapp.utils.TouchImageView;

public class AdapterFullScreenImage extends PagerAdapter {

    private Activity act;
    private List<String> imagePaths;
    private LayoutInflater inflater;
    private ImageLoader imgloader = ImageLoader.getInstance();

    public AdapterFullScreenImage(Activity activity, List<String> imagePaths) {
        this.act = activity;
        this.imagePaths = imagePaths;
    }

    @Override
    public int getCount() {
        return this.imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TouchImageView imgDisplay;
        inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.item_fullscreen_image, container, false);

        imgDisplay = viewLayout.findViewById(R.id.imgDisplay);

        imgloader.displayImage(imagePaths.get(position), imgDisplay);
        container.addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);

    }

}
