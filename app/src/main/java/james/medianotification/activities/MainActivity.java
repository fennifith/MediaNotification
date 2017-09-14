package james.medianotification.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import james.medianotification.R;
import james.medianotification.adapters.SimplePagerAdapter;
import james.medianotification.dialogs.AboutDialog;
import james.medianotification.fragments.HelpFragment;
import james.medianotification.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        setSupportActionBar(toolbar);

        tabLayout.setupWithViewPager(viewPager);
        viewPager.setAdapter(new SimplePagerAdapter(this, getSupportFragmentManager(), new SettingsFragment(), new HelpFragment()));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.icon).setVisibility(View.GONE);
            }
        }, 3000);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_github:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TheAndroidMaster/MediaNotification")));
                break;
            case R.id.action_about:
                new AboutDialog(this).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}