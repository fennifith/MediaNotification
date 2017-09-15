package james.medianotification.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import james.medianotification.R;
import james.medianotification.adapters.PlayerAdapter;
import james.medianotification.data.PlayerData;
import james.medianotification.utils.PlayerUtils;
import james.medianotification.utils.PreferenceUtils;

public class AppsFragment extends BaseFragment {

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apps, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);

        List<String> packages = new ArrayList<>();
        for (PlayerData player : PlayerUtils.getPlayers(getContext())) {
            if (!packages.contains(player.packageName) && player.isInstalled(getContext().getPackageManager()))
                packages.add(player.packageName);
        }

        Collections.sort(packages);
        int dividerPosition = packages.size();

        List<ResolveInfo> activities = getContext().getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0);

        for (ResolveInfo info : activities) {
            if (info.activityInfo != null && !packages.contains(info.activityInfo.packageName))
                packages.add(info.activityInfo.packageName);
        }

        String defaultPackage = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(PreferenceUtils.PREF_DEFAULT_MUSIC_PLAYER, null);
        if (defaultPackage != null) {
            if (packages.contains(defaultPackage))
                packages.remove(defaultPackage);

            packages.add(0, defaultPackage);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new PlayerAdapter(getContext(), dividerPosition, packages));

        return view;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.title_players);
    }
}
