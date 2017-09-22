package james.medianotification.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import james.medianotification.R;
import james.medianotification.services.NotificationService;
import james.medianotification.utils.PreferenceUtils;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.BaseViewHolder> {

    private Context context;
    private SharedPreferences prefs;
    private PackageManager packageManager;
    private String defaultPackage;
    private boolean isDefaultPackageSupported;
    private boolean isTutorial;
    private List<String> supportedPackages;
    private List<String> allPackages;

    public PlayerAdapter(Context context, List<String> supportedPackages, List<String> allPackages) {
        this.context = context;
        this.supportedPackages = supportedPackages;
        this.allPackages = allPackages;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        packageManager = context.getPackageManager();

        isTutorial = prefs.getBoolean(PreferenceUtils.PREF_TUTORIAL_PLAYERS, true);

        defaultPackage = prefs.getString(PreferenceUtils.PREF_DEFAULT_MUSIC_PLAYER, null);
        if (defaultPackage != null) {
            if (supportedPackages.contains(defaultPackage)) {
                supportedPackages.remove(defaultPackage);
                isDefaultPackageSupported = true;
            } else if (allPackages.contains(defaultPackage))
                allPackages.remove(defaultPackage);
        }
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 3)
            return new TutorialHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tutorial_players, parent, false));
        else
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false));
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, int position) {
        if (holder instanceof TutorialHolder) {
            ((TutorialHolder) holder).tutorialOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isTutorial) {
                        isTutorial = false;
                        prefs.edit().putBoolean(PreferenceUtils.PREF_TUTORIAL_PLAYERS, false).apply();
                        notifyDataSetChanged();
                    }
                }
            });

            return;
        }

        ViewHolder viewHolder = (ViewHolder) holder;
        int viewType = getItemViewType(position);
        String packageName = getPackageName(position);

        PackageInfo info;
        try {
            info = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }

        viewHolder.icon.setImageDrawable(info.applicationInfo.loadIcon(packageManager));
        viewHolder.title.setText(info.applicationInfo.loadLabel(packageManager));

        viewHolder.enabledSwitch.setOnCheckedChangeListener(null);
        viewHolder.enabledSwitch.setChecked(prefs.getBoolean(String.format(Locale.getDefault(), PreferenceUtils.PREF_PLAYER_ENABLED, packageName), true));
        viewHolder.enabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(String.format(Locale.getDefault(), PreferenceUtils.PREF_PLAYER_ENABLED, getPackageName(holder.getAdapterPosition())), b).apply();
                updateNotification();
            }
        });

        boolean isDefault = prefs.getString(PreferenceUtils.PREF_DEFAULT_MUSIC_PLAYER, "").equals(packageName);
        viewHolder.about.setVisibility(View.GONE);
        viewHolder.aboutDefault.setVisibility(View.GONE);
        if (viewType == 0) {
            viewHolder.header.setVisibility(View.VISIBLE);
            viewHolder.headerText.setText(R.string.title_default);
        } else if ((defaultPackage != null && position > 0 && defaultPackage.equals(getPackageName(position - 1))) || position == (isTutorial ? 1 : 0)) {
            viewHolder.header.setVisibility(View.VISIBLE);
            viewHolder.headerText.setText(R.string.title_supported_players);

            if (position == (isTutorial ? 1 : 0) + (defaultPackage != null ? 1 : 0)) {
                viewHolder.aboutDefault.setVisibility(View.VISIBLE);
                viewHolder.aboutDefault.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.title_default_player)
                                .setMessage(R.string.desc_tutorial_players_default)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .show();
                    }
                });
            }
        } else if (position == supportedPackages.size() + (defaultPackage != null ? 1 : 0) + (isTutorial ? 1 : 0)) {
            viewHolder.header.setVisibility(View.VISIBLE);
            viewHolder.headerText.setText(R.string.title_all);

            viewHolder.about.setVisibility(View.VISIBLE);
            viewHolder.about.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.title_supported_players)
                            .setMessage(R.string.desc_tutorial_players_supported)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                }
            });
        } else viewHolder.header.setVisibility(View.GONE);

        viewHolder.openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    context.startActivity(packageManager.getLaunchIntentForPackage(getPackageName(holder.getAdapterPosition())));
                } catch (Exception ignored) {
                }
            }
        });

        viewHolder.defaultButton.setVisibility(isDefault ? View.GONE : View.VISIBLE);
        viewHolder.defaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean tempIsDefaultPackageSupported = isDefaultPackageSupported;
                String tempDefaultPackage = defaultPackage;

                isDefaultPackageSupported = getItemViewType(holder.getAdapterPosition()) == 1;
                defaultPackage = getPackageName(holder.getAdapterPosition());

                if (tempIsDefaultPackageSupported)
                    supportedPackages.add(tempDefaultPackage);
                else allPackages.add(tempDefaultPackage);

                supportedPackages.remove(defaultPackage);
                allPackages.remove(defaultPackage);

                Collections.sort(supportedPackages, new PackageComparator());
                Collections.sort(allPackages, new PackageComparator());

                prefs.edit().putString(PreferenceUtils.PREF_DEFAULT_MUSIC_PLAYER, defaultPackage).apply();
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && isTutorial)
            return 3;
        else if (isTutorial) position--;

        if (position == 0 && defaultPackage != null)
            return 0;
        else if (defaultPackage != null) position--;

        if (position < supportedPackages.size())
            return 1;
        else position -= supportedPackages.size();

        if (position < allPackages.size())
            return 2;

        return -1;
    }

    private String getPackageName(int position) {
        if (position == 0 && isTutorial)
            return null;
        else if (isTutorial) position--;

        if (position == 0 && defaultPackage != null)
            return defaultPackage;
        else if (defaultPackage != null) position--;

        if (position < supportedPackages.size())
            return supportedPackages.get(position);
        else position -= supportedPackages.size();

        if (position < allPackages.size())
            return allPackages.get(position);

        return null;
    }

    @Override
    public int getItemCount() {
        return (defaultPackage != null ? 1 : 0) + supportedPackages.size() + allPackages.size();
    }

    private void updateNotification() {
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(NotificationService.ACTION_UPDATE_RECEIVER);
        context.startService(intent);
    }

    private static class TutorialHolder extends BaseViewHolder {

        private Button tutorialOk;

        public TutorialHolder(View itemView) {
            super(itemView);
            tutorialOk = itemView.findViewById(R.id.tutorialOk);
        }
    }

    private static class ViewHolder extends BaseViewHolder {

        private LinearLayout header;
        private TextView headerText;
        private ImageView about;
        private ImageView icon;
        private TextView title;
        private SwitchCompat enabledSwitch;
        private TextView openButton;
        private TextView defaultButton;
        private ImageView aboutDefault;

        public ViewHolder(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header);
            headerText = itemView.findViewById(R.id.headerText);
            about = itemView.findViewById(R.id.about);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            enabledSwitch = itemView.findViewById(R.id.enabledSwitch);
            openButton = itemView.findViewById(R.id.openButton);
            defaultButton = itemView.findViewById(R.id.defaultButton);
            aboutDefault = itemView.findViewById(R.id.aboutDefault);
        }
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class PackageComparator implements Comparator<String> {

        @Override
        public int compare(String s1, String s2) {
            return s1 != null && s2 != null ? s1.compareTo(s2) : 0;
        }
    }
}
