package james.medianotification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    
    public static final String PREFERENCES_NAME = "MediaNotification";
    public static final String ACCESSIBILITY_SWITCH_KEY = "accessibility";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        final SharedPreferences prefs =
                getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        
        LinearLayout switchLayout = findViewById(R.id.accessibility_switch_layout);
        
        final SwitchCompat switchCompat = findViewById(R.id.accessibility_switch);
        switchCompat.setChecked(prefs.getBoolean(ACCESSIBILITY_SWITCH_KEY, false));
        switchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchCompat.toggle();
            }
        });
        
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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