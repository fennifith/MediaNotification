package james.medianotification.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
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

public class AppsFragment extends BaseFragment {

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apps, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        return view;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.title_players);
    }

    @Override
    public void onSelect() {
        if (recyclerView != null && recyclerView.getAdapter() == null) {
            List<String> supportedPackages = new ArrayList<>();
            for (PlayerData player : PlayerUtils.getPlayers(getContext())) {
                if (!supportedPackages.contains(player.packageName) && player.isInstalled(getContext().getPackageManager()))
                    supportedPackages.add(player.packageName);
            }

            Collections.sort(supportedPackages);

            List<String> allPackages = new ArrayList<>();
            List<ResolveInfo> infos = getContext().getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0);

            for (ResolveInfo info : infos) {
                if (info.activityInfo != null && info.activityInfo.packageName != null && !supportedPackages.contains(info.activityInfo.packageName))
                    allPackages.add(info.activityInfo.packageName);
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new PlayerAdapter(getContext(), supportedPackages, allPackages));
        }
    }

    @Override
    public void onTutorial() {
        recyclerView.setAdapter(null);
        onSelect(); //should probably optimize this but im lazy
    }
}
