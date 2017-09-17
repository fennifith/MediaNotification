package james.medianotification.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import james.medianotification.R;
import james.medianotification.services.NotificationService;
import james.medianotification.utils.PreferenceUtils;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.ViewHolder> {

    private Context context;
    private SharedPreferences prefs;
    private PackageManager packageManager;
    private String defaultPackage;
    private boolean isDefaultPackageSupported;
    private List<String> supportedPackages;
    private List<String> allPackages;

    public PlayerAdapter(Context context, List<String> supportedPackages, List<String> allPackages) {
        this.context = context;
        this.supportedPackages = supportedPackages;
        this.allPackages = allPackages;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        packageManager = context.getPackageManager();

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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        String packageName = getPackageName(position);

        PackageInfo info;
        try {
            info = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }

        holder.icon.setImageDrawable(info.applicationInfo.loadIcon(packageManager));
        holder.title.setText(info.applicationInfo.loadLabel(packageManager));

        holder.enabledSwitch.setOnCheckedChangeListener(null);
        holder.enabledSwitch.setChecked(prefs.getBoolean(String.format(Locale.getDefault(), PreferenceUtils.PREF_PLAYER_ENABLED, packageName), true));
        holder.enabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(String.format(Locale.getDefault(), PreferenceUtils.PREF_PLAYER_ENABLED, getPackageName(holder.getAdapterPosition())), b).apply();
                updateNotification();
            }
        });

        boolean isDefault = prefs.getString(PreferenceUtils.PREF_DEFAULT_MUSIC_PLAYER, "").equals(packageName);
        if (defaultPackage != null && position == 0) {
            holder.header.setVisibility(View.VISIBLE);
            holder.headerText.setText(R.string.title_default);
        } else if ((defaultPackage != null && position == 1) || position == 0) {
            holder.header.setVisibility(View.VISIBLE);
            holder.headerText.setText(R.string.title_supported_players);
        } else if (position == supportedPackages.size() + (defaultPackage != null ? 1 : 0)) {
            holder.header.setVisibility(View.VISIBLE);
            holder.headerText.setText(R.string.title_all);
        } else holder.header.setVisibility(View.GONE);

        holder.openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    context.startActivity(packageManager.getLaunchIntentForPackage(getPackageName(holder.getAdapterPosition())));
                } catch (Exception ignored) {
                }
            }
        });

        holder.defaultButton.setVisibility(isDefault ? View.GONE : View.VISIBLE);
        holder.defaultButton.setOnClickListener(new View.OnClickListener() {
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

                Collections.sort(supportedPackages);
                Collections.sort(allPackages);

                prefs.edit().putString(PreferenceUtils.PREF_DEFAULT_MUSIC_PLAYER, defaultPackage).apply();
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout header;
        private TextView headerText;
        private ImageView icon;
        private TextView title;
        private SwitchCompat enabledSwitch;
        private TextView openButton;
        private TextView defaultButton;

        public ViewHolder(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header);
            headerText = itemView.findViewById(R.id.headerText);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            enabledSwitch = itemView.findViewById(R.id.enabledSwitch);
            openButton = itemView.findViewById(R.id.openButton);
            defaultButton = itemView.findViewById(R.id.defaultButton);
        }
    }
}
