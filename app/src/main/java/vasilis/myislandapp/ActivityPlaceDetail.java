package vasilis.myislandapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vasilis.myislandapp.adapter.AdapterImageList;
import vasilis.myislandapp.api.RestAdapter;
import vasilis.myislandapp.api.callbacks.CallBackBeachOverallRating;
import vasilis.myislandapp.api.callbacks.CallBackBeachRating;
import vasilis.myislandapp.api.callbacks.CallbackPlaceDetails;
import vasilis.myislandapp.data.DatabaseHandler;
import vasilis.myislandapp.data.SharedPref;
import vasilis.myislandapp.model.Images;
import vasilis.myislandapp.model.Place;
import vasilis.myislandapp.utils.GPSLocation;

public class ActivityPlaceDetail extends AppCompatActivity {

    private static final String EXTRA_OBJ = "key.EXTRA_OBJ";
    private static final String EXTRA_NOTIF_FLAG = "key.EXTRA_NOTIF_FLAG";
    private Place place = null;
    private ImageLoader imgloader = ImageLoader.getInstance();
    private WebView description = null;
    private View parent_view = null;
    private GoogleMap googleMap;
    private DatabaseHandler db;
    private boolean onProcess = false;
    private boolean isFromNotif = false;
    private Call<CallbackPlaceDetails> callback;
    private Call<CallBackBeachRating> callbackBeachRating;
    private Call<CallBackBeachOverallRating> overallratingcallback;
    private View lyt_progress;
    private View lyt_distance;
    private float distance = -1;
    private Snackbar snackbar;
    private String deviceID;

    public static void navigate(AppCompatActivity activity, View sharedView, Place p) {
        Intent intent = new Intent(activity, ActivityPlaceDetail.class);
        intent.putExtra(EXTRA_OBJ, p);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedView, EXTRA_OBJ);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    public static Intent navigateBase(Context context, Place obj, Boolean from_notif) {
        Intent i = new Intent(context, ActivityPlaceDetail.class);
        i.putExtra(EXTRA_OBJ, obj);
        i.putExtra(EXTRA_NOTIF_FLAG, from_notif);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        place = (Place) getIntent().getSerializableExtra(EXTRA_OBJ);
        if (place.category != 4) {
            setContentView(R.layout.activity_place_details);
        } else {
            setContentView(R.layout.activity_place_details_beach);

        }
        parent_view = findViewById(android.R.id.content);

        if (!imgloader.isInited()) GPSLocation.initImageLoader(this);

        db = new DatabaseHandler(this);
        ViewCompat.setTransitionName(findViewById(R.id.app_bar_layout), EXTRA_OBJ);

        isFromNotif = getIntent().getBooleanExtra(EXTRA_NOTIF_FLAG, false);

        lyt_progress = findViewById(R.id.lyt_progress);
        lyt_distance = findViewById(R.id.lyt_distance);
        imgloader.displayImage(RestAdapter.getURLimgPlace(place.image), (ImageView) findViewById(R.id.image));
        distance = place.distance;
        setupToolbar(place.name);
        initMap();
        loadMore();

        deviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        GPSLocation.systemBarLolipop(this);

    }


    private void displayData(Place p) {
        ((TextView) findViewById(R.id.address)).setText(p.address);
        ((TextView) findViewById(R.id.phone)).setText(p.phone.equals("-") || p.phone.trim().equals("") ? getString(R.string.no_phone_number) : p.phone);
        ((TextView) findViewById(R.id.website)).setText(p.website.equals("-") || p.website.trim().equals("") ? getString(R.string.no_website) : p.website);

        description = findViewById(R.id.description);
        String html_data = "<style>img{max-width:100%;height:auto;} iframe{width:100%;}</style> ";
        html_data += p.description;
        description.getSettings().setBuiltInZoomControls(true);
        description.setBackgroundColor(Color.TRANSPARENT);
        description.setWebChromeClient(new WebChromeClient());
        description.loadData(html_data, "text/html; charset=UTF-8", null);
        description.getSettings().setJavaScriptEnabled(true);
        description.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        if (distance == -1) {
            lyt_distance.setVisibility(View.GONE);
        } else {
            lyt_distance.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.distance)).setText(GPSLocation.getFormatedDistance(distance));
        }

        setImageGallery(db.getListImageByPlaceId(p.id));
    }

    @Override
    protected void onResume() {
        if (!imgloader.isInited()) GPSLocation.initImageLoader(getApplicationContext());
        loadPlaceData();
        if (description != null) description.onResume();
        super.onResume();
    }

    public void clickLayout(View view) {
        switch (view.getId()) {
            case R.id.lyt_address:
                if (!place.isDraft()) {
                    Uri uri = Uri.parse("http://maps.google.com/maps?q=loc:" + place.lat + "," + place.lng);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
                break;
            case R.id.lyt_phone:
                if (!place.isDraft() && !place.phone.equals("-") && !place.phone.trim().equals("")) {
                    GPSLocation.dialNumber(this, place.phone);
                } else {
                    Snackbar.make(parent_view, R.string.fail_dial_number, Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.lyt_website:
                if (!place.isDraft() && !place.website.equals("-") && !place.website.trim().equals("")) {
                    GPSLocation.directUrl(this, place.website);
                } else {
                    Snackbar.make(parent_view, R.string.fail_open_website, Snackbar.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void setImageGallery(List<Images> images) {
        List<Images> new_images = new ArrayList<>();
        final ArrayList<String> new_images_str = new ArrayList<>();
        new_images.add(new Images(place.id, place.image));
        new_images.addAll(images);
        for (Images img : new_images) {
            new_images_str.add(RestAdapter.getURLimgPlace(img.name));
        }

        RecyclerView galleryRecycler = findViewById(R.id.galleryRecycler);
        galleryRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        AdapterImageList adapter = new AdapterImageList(new_images);
        galleryRecycler.setAdapter(adapter);
        adapter.setOnItemClickListener(new AdapterImageList.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String viewModel, int pos) {
                Intent i = new Intent(ActivityPlaceDetail.this, ActivityFullScreenImage.class);
                i.putExtra(ActivityFullScreenImage.EXTRA_POS, pos);
                i.putStringArrayListExtra(ActivityFullScreenImage.EXTRA_IMGS, new_images_str);
                startActivity(i);
            }
        });
    }


    private void setupToolbar(String name) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        ((TextView) findViewById(R.id.toolbar_title)).setText(name);

        final CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setContentScrimColor(new SharedPref(this).getThemeColorInt());
        ((AppBarLayout) findViewById(R.id.app_bar_layout)).addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (collapsing_toolbar.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(collapsing_toolbar)) {
                } else {
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_details, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            backAction();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initMap() {
        if (googleMap == null) {
            MapFragment mapFragment1 = (MapFragment) getFragmentManager().findFragmentById(R.id.mapPlaces);
            mapFragment1.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap gMap) {
                    googleMap = gMap;
                    if (googleMap == null) {
                        Snackbar.make(parent_view, R.string.unable_create_map, Snackbar.LENGTH_SHORT).show();
                    } else {
                        googleMap = GPSLocation.configStaticMap(ActivityPlaceDetail.this, googleMap, place);
                    }
                }
            });
        }

        findViewById(R.id.bt_navigate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent navigation = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + place.lat + "," + place.lng));
                startActivity(navigation);
            }
        });
        findViewById(R.id.bt_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlaceInMap();
            }
        });
        findViewById(R.id.map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlaceInMap();
            }
        });
    }


    private void loadMore() {
        findViewById(R.id.bt_loadMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        findViewById(R.id.bt_loadMoreImages).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        if (place.category == 4) {
            final RatingBar ratingBar = findViewById(R.id.ratingBar);

            findViewById(R.id.bt_sendRating).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int rating = Math.round(ratingBar.getRating());
                    ratingBar.setIsIndicator(true);
                    findViewById(R.id.bt_sendRating).setVisibility(View.GONE);
                    callbackBeachRating = RestAdapter.createAPI().rateBeach(place.id, rating, deviceID);
                    callbackBeachRating.enqueue(new Callback<CallBackBeachRating>() {
                        @Override
                        public void onResponse(Call<CallBackBeachRating> call, Response<CallBackBeachRating> response) {
                            CallBackBeachRating resp = response.body();
                            if (resp != null) {
                                Toast.makeText(getApplicationContext(), getString(R.string.rating_saved), Toast.LENGTH_LONG).show();
                                try {
                                    ((TextView) findViewById(R.id.rating)).setText(resp.overallRating);
                                } catch (Exception e) {
                                    Log.e("ActivityPlaceDetail", e.toString());
                                }
                            } else {
                                try {
                                    ((TextView) findViewById(R.id.rating)).setText(resp.overallRating);
                                } catch (Exception e) {
                                    Log.e("ActivityPlaceDetail", e.toString());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<CallBackBeachRating> call, Throwable t) {
                            if (call != null && !call.isCanceled()) {
                                Log.e("onFailure", t.getMessage());
                                boolean conn = GPSLocation.cekConnection(getApplicationContext());
                                if (conn) {

                                } else {

                                }
                            }
                        }

                    });
                }
            });
        }
    }


    private void openPlaceInMap() {
        Intent intent = new Intent(ActivityPlaceDetail.this, ActivityMaps.class);
        intent.putExtra(ActivityMaps.EXTRA_OBJ, place);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (callback != null && callback.isExecuted()) callback.cancel();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        backAction();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (description != null) description.onPause();
    }

    private void backAction() {
        if (isFromNotif) {
            Intent i = new Intent(this, ActivityMain.class);
            startActivity(i);
        }
        finish();
    }

    private void loadPlaceData() {
        place = db.getPlace(place.id);
        if (place.isDraft()) {
            if (GPSLocation.cekConnection(this)) {
                requestDetailsPlace(place.id);
            } else {
                onFailureRetry(getString(R.string.no_internet));
            }
        } else {
            displayData(place);
        }
        loadoverallRating(place.id);
    }


    private void loadoverallRating(int place_id) {
        overallratingcallback = RestAdapter.createAPI().getBeachOverallRating(place_id);
        overallratingcallback.enqueue(new retrofit2.Callback<CallBackBeachOverallRating>() {
            @Override
            public void onResponse(Call<CallBackBeachOverallRating> call, Response<CallBackBeachOverallRating> response) {
                CallBackBeachOverallRating resp = response.body();
                if (resp != null) {
                    try {
                        ((TextView) findViewById(R.id.rating)).setText(resp.overallRating);
                    } catch (Exception e) {
                        Log.e("ActivityPlaceDetail", e.toString());
                    }
                } else {
                    try {
                        ((TextView) findViewById(R.id.rating)).setText("-");
                    } catch (Exception e) {
                        Log.e("ActivityPlaceDetail", e.toString());
                    }
                }

            }

            @Override
            public void onFailure(Call<CallBackBeachOverallRating> call, Throwable t) {
                if (call != null && !call.isCanceled()) {
                    boolean conn = GPSLocation.cekConnection(ActivityPlaceDetail.this);
                    if (conn) {

                    } else {

                    }
                }
            }
        });
    }
    private void requestDetailsPlace(int place_id) {
        if (onProcess) {
            Snackbar.make(parent_view, R.string.task_running, Snackbar.LENGTH_SHORT).show();
            return;
        }
        onProcess = true;
        showProgressbar(true);
        callback = RestAdapter.createAPI().getPlaceDetails(place_id);
        callback.enqueue(new retrofit2.Callback<CallbackPlaceDetails>() {
            @Override
            public void onResponse(Call<CallbackPlaceDetails> call, Response<CallbackPlaceDetails> response) {
                CallbackPlaceDetails resp = response.body();
                if (resp != null) {
                    place = db.updatePlace(resp.place);
                    displayDataWithDelay(place);
                } else {
                    onFailureRetry(getString(R.string.failed_load_details));
                }

            }

            @Override
            public void onFailure(Call<CallbackPlaceDetails> call, Throwable t) {
                if (call != null && !call.isCanceled()) {
                    boolean conn = GPSLocation.cekConnection(ActivityPlaceDetail.this);
                    if (conn) {
                        onFailureRetry(getString(R.string.failed_load_details));
                    } else {
                        onFailureRetry(getString(R.string.no_internet));
                    }
                }
            }
        });

    }

    private void displayDataWithDelay(final Place resp) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showProgressbar(false);
                onProcess = false;
                displayData(resp);
            }
        }, 1000);
    }

    private void onFailureRetry(final String msg) {
        showProgressbar(false);
        onProcess = false;
        snackbar = Snackbar.make(parent_view, msg, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.RETRY, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPlaceData();
            }
        });
        snackbar.show();
        retryDisplaySnackbar();
    }

    private void retryDisplaySnackbar() {
        if (snackbar != null && !snackbar.isShown()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    retryDisplaySnackbar();
                }
            }, 1000);
        }
    }

    private void showProgressbar(boolean show) {
        lyt_progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
