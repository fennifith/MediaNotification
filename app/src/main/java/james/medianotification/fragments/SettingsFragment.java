package james.medianotification.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import james.colorpickerdialog.dialogs.ColorPickerDialog;
import james.colorpickerdialog.dialogs.PreferenceDialog;
import james.medianotification.R;
import james.medianotification.services.NotificationService;
import james.medianotification.utils.PreferenceUtils;
import james.medianotification.views.ColorImageView;

public class SettingsFragment extends BaseFragment {

    private static final int REQUEST_NOTIFICATION = 1034;

    private SwitchCompat mediaNotificationSwitch;
    private AppCompatSpinner colorMethodSpinner;
    private View customColorView;
    private ColorImageView customColor;
    private SwitchCompat inverseTextSwitch;
    private SwitchCompat highContrastSwitch;
    private SwitchCompat forceMdIcons;
    private View defaultPlayerView;
    private SwitchCompat alwaysDismissibleSwitch;
    private SwitchCompat killProcessSwitch;
    private SwitchCompat cancelOriginalNotificationSwitch;
    private View mediaControls;
    private View storagePermission;
    private Button storagePermissionButton;
    private SwitchCompat albumArtSwitch;
    private SwitchCompat lastFmSwitch;
    private View rootPermission;
    private Button rootPermissionButton;
    private SwitchCompat receiverSwitch;

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mediaNotificationSwitch = view.findViewById(R.id.mediaNotificationSwitch);
        colorMethodSpinner = view.findViewById(R.id.colorMethodSpinner);
        customColorView = view.findViewById(R.id.customColorView);
        customColor = view.findViewById(R.id.customColor);
        inverseTextSwitch = view.findViewById(R.id.inverseTextSwitch);
        highContrastSwitch = view.findViewById(R.id.highContrastSwitch);
        forceMdIcons = view.findViewById(R.id.forceMdIcons);
        defaultPlayerView = view.findViewById(R.id.defaultPlayer);
        alwaysDismissibleSwitch = view.findViewById(R.id.alwaysDismissibleSwitch);
        killProcessSwitch = view.findViewById(R.id.killProcessSwitch);
        cancelOriginalNotificationSwitch = view.findViewById(R.id.cancelOriginalNotificationSwitch);
        mediaControls = view.findViewById(R.id.mediaControls);
        storagePermission = view.findViewById(R.id.storagePermission);
        storagePermissionButton = view.findViewById(R.id.storagePermissionButton);
        albumArtSwitch = view.findViewById(R.id.albumArtSwitch);
        lastFmSwitch = view.findViewById(R.id.lastFmSwitch);
        rootPermission = view.findViewById(R.id.rootPermission);
        rootPermissionButton = view.findViewById(R.id.rootPermissionButton);
        receiverSwitch = view.findViewById(R.id.receiverSwitch);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        mediaNotificationSwitch.setChecked(NotificationService.isRunning(getContext()));
        mediaNotificationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), REQUEST_NOTIFICATION);
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.array_color_methods, android.R.layout.simple_spinner_item);
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
                new ColorPickerDialog(getContext())
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

        inverseTextSwitch.setChecked(prefs.getBoolean(PreferenceUtils.PREF_INVERSE_TEXT_COLORS, true));
        inverseTextSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(PreferenceUtils.PREF_INVERSE_TEXT_COLORS, b).apply();
                if (b && highContrastSwitch.isChecked())
                    highContrastSwitch.setChecked(false);
                else updateNotification();
            }
        });

        highContrastSwitch.setChecked(prefs.getBoolean(PreferenceUtils.PREF_HIGH_CONTRAST_TEXT, false));
        highContrastSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(PreferenceUtils.PREF_HIGH_CONTRAST_TEXT, b).apply();
                if (b && inverseTextSwitch.isChecked())
                    inverseTextSwitch.setChecked(false);
                else updateNotification();
            }
        });

        forceMdIcons.setChecked(prefs.getBoolean(PreferenceUtils.PREF_FORCE_MD_ICONS, false));
        forceMdIcons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(PreferenceUtils.PREF_FORCE_MD_ICONS, b).apply();
            }
        });

        defaultPlayerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final List<ResolveInfo> activities = getContext().getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0);

                Collections.sort(activities, new Comparator<ResolveInfo>() {
                    @Override
                    public int compare(ResolveInfo t1, ResolveInfo t2) {
                        CharSequence string1, string2;

                        try {
                            string1 = t1.loadLabel(getContext().getPackageManager());
                        } catch (Exception e) {
                            string1 = t1.resolvePackageName;
                        }

                        try {
                            string2 = t2.loadLabel(getContext().getPackageManager());
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
                            items[i] = info.loadLabel(getContext().getPackageManager());
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

                new AlertDialog.Builder(getContext())
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

        cancelOriginalNotificationSwitch.setChecked(prefs.getBoolean(PreferenceUtils.PREF_CANCEL_ORIGINAL_NOTIFICATION, false));
        cancelOriginalNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(PreferenceUtils.PREF_CANCEL_ORIGINAL_NOTIFICATION, b).apply();
            }
        });

        mediaControls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
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

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            storagePermission.setVisibility(View.GONE);
        storagePermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        });

        albumArtSwitch.setChecked(prefs.getBoolean(PreferenceUtils.PREF_SHOW_ALBUM_ART, true));
        albumArtSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(PreferenceUtils.PREF_SHOW_ALBUM_ART, b).apply();
                if (!b && lastFmSwitch.isChecked())
                    lastFmSwitch.setChecked(false);
                updateNotification();
            }
        });

        lastFmSwitch.setChecked(prefs.getBoolean(PreferenceUtils.PREF_USE_LASTFM, true));
        lastFmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(PreferenceUtils.PREF_USE_LASTFM, b).apply();
                if (b && !albumArtSwitch.isChecked())
                    albumArtSwitch.setChecked(true);
            }
        });

        if (ContextCompat.checkSelfPermission(getContext(), "android.permission.UPDATE_APP_OPS_STATS") == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            rootPermission.setVisibility(View.GONE);
        rootPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getContext(), "android.permission.UPDATE_APP_OPS_STATS") == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), R.string.msg_permission_granted, Toast.LENGTH_SHORT).show();
                    rootPermission.setVisibility(View.GONE);
                } else
                    Toast.makeText(getContext(), R.string.msg_app_ops_denied, Toast.LENGTH_SHORT).show();
            }
        });

        boolean isReceiver = prefs.getBoolean(PreferenceUtils.PREF_USE_RECEIVER, false);
        receiverSwitch.setChecked(isReceiver);
        mediaControls.setVisibility(isReceiver ? View.VISIBLE : View.GONE);
        receiverSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(PreferenceUtils.PREF_USE_RECEIVER, b).apply();
                mediaControls.setVisibility(b ? View.VISIBLE : View.GONE);
            }
        });

        return view;
    }

    private void updateNotification() {
        if (NotificationService.isRunning(getContext())) {
            Intent intent = new Intent(getContext(), NotificationService.class);
            intent.setAction(NotificationService.ACTION_UPDATE);
            getContext().startService(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_NOTIFICATION && mediaNotificationSwitch != null)
            mediaNotificationSwitch.setChecked(NotificationService.isRunning(getContext()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            storagePermission.setVisibility(View.GONE);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.title_settings);
    }
}
