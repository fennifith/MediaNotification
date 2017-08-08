package james.medianotification.data;

import android.app.PendingIntent;
import android.content.Context;

public class PlayerData {

    public String name;
    public String packageName;
    public String previousAction;
    public String playPauseAction;
    public String nextAction;
    public String[] actions;

    public PlayerData(String name, String packageName, String previousAction, String playPauseAction, String nextAction, String... actions) {
        this.name = name;
        this.packageName = packageName;
        this.previousAction = previousAction;
        this.playPauseAction = playPauseAction;
        this.nextAction = nextAction;
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

}
