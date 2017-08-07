package james.medianotification;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    public static final String PREFERENCES_NAME = "MediaNotification";
    public static final String ACCESSIBILITY_SWITCH_KEY = "accessibility";

    private LinearLayout accessibilitySwitchLayout;
    private SwitchCompat accessibilitySwitch;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        accessibilitySwitchLayout = findViewById(R.id.accessibilitySwitchLayout);
        accessibilitySwitch = findViewById(R.id.accessibilitySwitch);

        accessibilitySwitch.setChecked(prefs.getBoolean(ACCESSIBILITY_SWITCH_KEY, false));
        accessibilitySwitchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accessibilitySwitch.toggle();
            }
        });

        accessibilitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(ACCESSIBILITY_SWITCH_KEY, b).apply();
                Intent service = new Intent(getApplicationContext(), NotificationService.class);
                service.putExtra("restart", true);
                startService(service);
            }
        });

    }

}