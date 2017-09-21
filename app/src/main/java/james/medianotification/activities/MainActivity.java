package james.medianotification.activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import james.medianotification.MediaNotification;
import james.medianotification.R;
import james.medianotification.adapters.SimplePagerAdapter;
import james.medianotification.dialogs.AboutDialog;
import james.medianotification.fragments.AppsFragment;
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
        viewPager.setAdapter(new SimplePagerAdapter(this, viewPager, getSupportFragmentManager(), new SettingsFragment(), new AppsFragment(), new HelpFragment()));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.icon).setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), getWindow().getStatusBarColor(), ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark));
                    animator.setInterpolator(new AccelerateInterpolator());
                    animator.setDuration(500);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                getWindow().setStatusBarColor((int) valueAnimator.getAnimatedValue());
                        }
                    });
                    animator.start();
                }
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
            case R.id.action_tutorial:
                ((MediaNotification) getApplicationContext()).showTutorial();
                break;
            case R.id.action_about:
                new AboutDialog(this).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}