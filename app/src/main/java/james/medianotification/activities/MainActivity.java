package james.medianotification.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import james.colorpickerdialog.dialogs.ColorPickerDialog;
import james.colorpickerdialog.dialogs.PreferenceDialog;
import james.medianotification.R;
import james.medianotification.services.NotificationService;
import james.medianotification.utils.MarkdownUtils;
import james.medianotification.utils.PreferenceUtils;
import james.medianotification.views.ColorImageView;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION = 1034;

    private ProgressBar progressBar;
    private TextView textView;
    private SwitchCompat mediaNotificationSwitch;
    private AppCompatSpinner colorMethodSpinner;
    private View customColorView;
    private ColorImageView customColor;
    private SwitchCompat highContrastSwitch;
    private View defaultPlayerView;
    private SwitchCompat alwaysDismissibleSwitch;
    private SwitchCompat killProcessSwitch;
    private View mediaControls;
    private View storagePermission;
    private Button storagePermissionButton;
    SwitchCompat albumArtSwitch;
    private SwitchCompat lastFmSwitch;
    private View rootPermission;
    private Button rootPermissionButton;
    private SwitchCompat receiverSwitch;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.textView);
        mediaNotificationSwitch = findViewById(R.id.mediaNotificationSwitch);
        colorMethodSpinner = findViewById(R.id.colorMethodSpinner);
        customColorView = findViewById(R.id.customColorView);
        customColor = findViewById(R.id.customColor);
        highContrastSwitch = findViewById(R.id.highContrastSwitch);
        defaultPlayerView = findViewById(R.id.defaultPlayer);
        alwaysDismissibleSwitch = findViewById(R.id.alwaysDismissibleSwitch);
        killProcessSwitch = findViewById(R.id.killProcessSwitch);
        mediaControls = findViewById(R.id.mediaControls);
        storagePermission = findViewById(R.id.storagePermission);
        storagePermissionButton = findViewById(R.id.storagePermissionButton);
        albumArtSwitch = findViewById(R.id.albumArtSwitch);
        lastFmSwitch = findViewById(R.id.lastFmSwitch);
        rootPermission = findViewById(R.id.rootPermission);
        rootPermissionButton = findViewById(R.id.rootPermissionButton);
        receiverSwitch = findViewById(R.id.receiverSwitch);

        new Thread() {
            @Override
            public void run() {
                final String text;
                try {
                    URL url = new URL("https://raw.githubusercontent.com/TheAndroidMaster/MediaNotification/master/README.md");
                    HttpURLConnection request = (HttpURLConnection) url.openConnection();
                    request.connect();

                    BufferedReader r = new BufferedReader(new InputStreamReader((InputStream) request.getContent()));
                    StringBuilder total = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        total.append(line).append('\n');
                    }

                    text = MarkdownUtils.toHtml(total.toString());
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            textView.setText(String.format(getString(R.string.msg_readme_error), e.getMessage()));
                        }
                    });
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            textView.setText(Html.fromHtml(text, 0));
                        else textView.setText(Html.fromHtml(text));
                    }
                });
            }
        }.start();

        mediaNotificationSwitch.setChecked(NotificationService.isRunning(this));
        mediaNotificationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), REQUEST_NOTIFICATION);
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.array_color_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorMethodSpinner.setAdapter(adapter);
        colorMethodSpinner.setSelection(prefs.getInt(PreferenceUtils.PREF_COLOR_METHOD, 0));
        colorMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                prefs.edit().putInt(PreferenceUtils.PREF_COLOR_METHOD, i).apply();
                updateNotification();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        customColor.setColor(prefs.getInt(PreferenceUtils.PREF_CUSTOM_COLOR, Color.WHITE));
        customColorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ColorPickerDialog(MainActivity.this)
                        .setDefaultPreference(Color.WHITE)
                        .setPreference(prefs.getInt(PreferenceUtils.PREF_CUSTOM_COLOR, Color.WHITE))
                        .setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
                            @Override
                            public void onPreference(PreferenceDialog dialog, Integer preference) {
                                prefs.edit().putInt(PreferenceUtils.PREF_CUSTOM_COLOR, preference).apply();
                                customColor.setColor(preference);
                                updateNotification();
                            }

                            @Override
                            public void onCancel(PreferenceDialog dialog) {
                            }
                        })
                        .show();
            }
        });

        highContrastSwitch.setChecked(prefs.getBoolean(PreferenceUtils.PREF_HIGH_CONTRAST_TEXT, false));
        highContrastSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(PreferenceUtils.PREF_HIGH_CONTRAST_TEXT, b).apply();
                updateNotification();
            }
        });

        defaultPlayerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final List<ResolveInfo> activities = getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0);

                Collections.sort(activities, new Comparator<ResolveInfo>() {
                    @Override
                    public int compare(ResolveInfo t1, ResolveInfo t2) {
                        CharSequence string1, string2;

                        try {
                            string1 = t1.loadLabel(getPackageManager());
                        } catch (Exception e) {
                            string1 = t1.resolvePackageName;
                        }

                        try {
                            string2 = t2.loadLabel(getPackageManager());
                        } catch (Exception e) {
                            string2 = t2.resolvePackageName;
                        }

                        return string1.toString().compareTo(string2.toString());
                    }
                });

                CharSequence[] items = new CharSequence[activities.size()];
                for (int i = 0; i < items.length && i < activities.size(); i++) {
                    ResolveInfo info = activities.get(i);
                    if (info.activityInfo != null) {
                        try {
                            items[i] = info.loadLabel(getPackageManager());
                        } catch (Exception e) {
                            items[i] = info.resolvePackageName;
                        }
                    }
                }

                int selectedItem = -1;
                if (prefs.contains(PreferenceUtils.PREF_DEFAULT_MUSIC_PLAYER)) {
                    String packageName = prefs.getString(PreferenceUtils.PREF_DEFAULT_MUSIC_PLAYER, "");
                    for (int i = 0; i < activities.size(); i++) {
                        if (activities.get(i).activityInfo.packageName.equals(packageName)) {
                            selectedItem = i;
                            break;
                        }
                    }
                }

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.title_default_player)
                        .setSingleChoiceItems(items, selectedItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i >= 0 && i < activities.size()) {
                                    prefs.edit().putString(PreferenceUtils.PREF_DEFAULT_MUSIC_PLAYER, activities.get(i).activityInfo.packageName).apply();
                                    updateNotification();
                                }
                            }
                        })
                        .show();
            }
        });

        alwaysDismissibleSwitch.setChecked(prefs.getBoolean(PreferenceUtils.PREF_ALWAYS_DISMISSIBLE, false));
        alwaysDismissibleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(PreferenceUtils.PREF_ALWAYS_DISMISSIBLE, b).apply();
                updateNotification();
            }
        });

        killProcessSwitch.setChecked(prefs.getBoolean(PreferenceUtils.PREF_FC_ON_DISMISS, false));
        killProcessSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(PreferenceUtils.PREF_FC_ON_DISMISS, b).apply();
            }
        });

        mediaControls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.title_media_controls)
                        .setSingleChoiceItems(R.array.array_control_methods, prefs.getInt(PreferenceUtils.PREF_MEDIA_CONTROLS_METHOD, PreferenceUtils.CONTROLS_METHOD_NONE), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                prefs.edit().putInt(PreferenceUtils.PREF_MEDIA_CONTROLS_METHOD, i).apply();
                            }
                        })
                        .create()
                        .show();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            storagePermission.setVisibility(View.GONE);
        storagePermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        });

        albumArtSwitch.setChecked(prefs.getBoolean(PreferenceUtils.PREF_SHOW_ALBUM_ART, true));
        albumArtSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(PreferenceUtils.PREF_SHOW_ALBUM_ART, b).apply();
                updateNotification();
            }
        });

        lastFmSwitch.setChecked(prefs.getBoolean(PreferenceUtils.PREF_USE_LASTFM, true));
        lastFmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(PreferenceUtils.PREF_USE_LASTFM, b).apply();
            }
        });

        if (ContextCompat.checkSelfPermission(this, "android.permission.UPDATE_APP_OPS_STATS") == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            rootPermission.setVisibility(View.GONE);
        rootPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, "android.permission.UPDATE_APP_OPS_STATS") == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, R.string.msg_permission_granted, Toast.LENGTH_SHORT).show();
                    rootPermission.setVisibility(View.GONE);
                } else
                    Toast.makeText(MainActivity.this, R.string.msg_app_ops_denied, Toast.LENGTH_SHORT).show();
            }
        });

        receiverSwitch.setChecked(prefs.getBoolean(PreferenceUtils.PREF_USE_RECEIVER, false));
        receiverSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(PreferenceUtils.PREF_USE_RECEIVER, b).apply();
            }
        });
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_NOTIFICATION && mediaNotificationSwitch != null)
            mediaNotificationSwitch.setChecked(NotificationService.isRunning(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            storagePermission.setVisibility(View.GONE);
    }

    private void updateNotification() {
        if (NotificationService.isRunning(this)) {
            Intent intent = new Intent(this, NotificationService.class);
            intent.setAction(NotificationService.ACTION_UPDATE);
            startService(intent);
        }
    }
}