package vasilis.myislandapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import vasilis.myislandapp.data.DatabaseHandler;
import vasilis.myislandapp.data.SharedPref;
import vasilis.myislandapp.fragment.FragmentCategory;
import vasilis.myislandapp.utils.Tools;

public class ActivityMain extends AppCompatActivity {


    static ActivityMain activityMain;
    static boolean active = false;
    public ActionBar actionBar;
    public Toolbar toolbar;
    private ImageLoader imgloader = ImageLoader.getInstance();
    private int cat[];
    private FloatingActionButton fab;
    private NavigationView navigationView;
    private DatabaseHandler db;
    private SharedPref sharedPref;
    private RelativeLayout nav_header_lyt;
    private long exitTime = 0;

    public static ActivityMain getInstance() {
        return activityMain;
    }

    public static void animateFab(final boolean hide) {
        FloatingActionButton f_ab = activityMain.findViewById(R.id.fab);
        int moveY = hide ? (2 * f_ab.getHeight()) : 0;
        f_ab.animate().translationY(moveY).setStartDelay(100).setDuration(400).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activityMain = this;

        if (!imgloader.isInited()) Tools.initImageLoader(this);
        fab = findViewById(R.id.fab);
        db = new DatabaseHandler(this);
        sharedPref = new SharedPref(this);

        initToolbar();
        initDrawerMenu();
        prepareImageLoader();
        cat = getResources().getIntArray(R.array.id_category);

        // first drawer view
        onItemSelected(R.id.nav_all, getString(R.string.title_nav_all));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ActivityMain.this, ActivitySearch.class);
                startActivity(i);
            }
        });

        // for system bar in lollipop
        Tools.systemBarLolipop(this);
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        Tools.setActionBarColor(this, actionBar);
    }

    private void initDrawerMenu() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                return onItemSelected(item.getItemId(), item.getTitle().toString());
            }
        });

        View nav_header = navigationView.getHeaderView(0);
        nav_header_lyt = nav_header.findViewById(R.id.nav_header_lyt);
        nav_header_lyt.setBackgroundColor(Tools.colorBrighter(sharedPref.getThemeColorInt()));

        (nav_header.findViewById(R.id.menu_nav_map)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ActivityMaps.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START);
        } else {
            doExitApp();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Tools.aboutAction(ActivityMain.this);
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onItemSelected(int id, String title) {
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        switch (id) {
            case R.id.nav_all:
                fragment = new FragmentCategory();
                bundle.putInt(FragmentCategory.TAG_CATEGORY, -1);
                actionBar.setTitle(title);
                break;

            case R.id.nav_sights:
                fragment = new FragmentCategory();
                bundle.putInt(FragmentCategory.TAG_CATEGORY, cat[0]);
                actionBar.setTitle(title);
                break;
            case R.id.nav_food:
                fragment = new FragmentCategory();
                bundle.putInt(FragmentCategory.TAG_CATEGORY, cat[1]);
                actionBar.setTitle(title);
                break;
            case R.id.nav_hotels:
                fragment = new FragmentCategory();
                bundle.putInt(FragmentCategory.TAG_CATEGORY, cat[2]);
                actionBar.setTitle(title);
                break;
            case R.id.nav_beaches:
                fragment = new FragmentCategory();
                bundle.putInt(FragmentCategory.TAG_CATEGORY, cat[3]);
                actionBar.setTitle(title);
                break;
            default:
                break;

        }

        if (fragment != null) {
            fragment.setArguments(bundle);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_content, fragment);
            fragmentTransaction.commit();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void doExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, R.string.press_again_exit_app, Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    private void prepareImageLoader() {
        Tools.initImageLoader(this);
    }

    @Override
    protected void onResume() {
        if (!imgloader.isInited()) Tools.initImageLoader(this);
        if (actionBar != null) {
            Tools.setActionBarColor(this, actionBar);
            Tools.systemBarLolipop(this);
        }
        if (nav_header_lyt != null) {
            nav_header_lyt.setBackgroundColor(Tools.colorBrighter(sharedPref.getThemeColorInt()));
        }
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        active = false;
    }


}
