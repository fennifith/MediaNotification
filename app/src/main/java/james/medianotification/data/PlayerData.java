package james.medianotification.data;

import android.app.PendingIntent;
import android.content.Context;

public class PlayerData {

    public String name;
    public String packageName;
    public String[] actions;

    public PlayerData(String name, String packageName, String... actions) {
        this.name = name;
        this.packageName = packageName;
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
