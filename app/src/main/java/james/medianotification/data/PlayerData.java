package james.medianotification.data;

import android.app.PendingIntent;
import android.content.Context;

public class PlayerData {

    public String name;
    public String packageName;
    public PendingIntent previousIntent;
    public PendingIntent playPauseIntent;
    public PendingIntent nextIntent;
    public String[] actions;
    public int persistence = -1;

    public PlayerData(String name, String packageName, String... actions) {
        this.name = name;
        this.packageName = packageName;
        this.actions = actions;
    }

    public PlayerData(String name, String packageName, int persistence, String... actions) {
        this.name = name;
        this.packageName = packageName;
        this.persistence = persistence;
        this.actions = actions;
    }

    public PlayerData(String name, String packageName, PendingIntent previousIntent, PendingIntent playPauseIntent, PendingIntent nextIntent, String... actions) {
        this.name = name;
        this.packageName = packageName;
        this.previousIntent = previousIntent;
        this.playPauseIntent = playPauseIntent;
        this.nextIntent = nextIntent;
        this.actions = actions;
    }

    public PlayerData(String name, String packageName, PendingIntent previousIntent, PendingIntent playPauseIntent, PendingIntent nextIntent, int persistence, String... actions) {
        this.name = name;
        this.packageName = packageName;
        this.previousIntent = previousIntent;
        this.playPauseIntent = playPauseIntent;
        this.nextIntent = nextIntent;
        this.persistence = persistence;
        this.actions = actions;
    }

    public PendingIntent getLaunchIntent(Context context) {
        try {
            return PendingIntent.getActivity(context, 0, context.getPackageManager().getLaunchIntentForPackage(packageName), 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean hasAction(String action) {
        for (String string : actions) {
            if (string.equals(action))
                return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlayerData && ((PlayerData) obj).packageName != null && packageName != null && ((PlayerData) obj).packageName.equals(packageName);
    }
}
