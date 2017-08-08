package james.medianotification.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.SystemClock;
import android.view.KeyEvent;

import java.util.List;

public class ActionReceiver extends BroadcastReceiver {

    public static final String EXTRA_KEYCODE = "james.medianotification.EXTRA_KEYCODE";
    public static final String EXTRA_PACKAGE = "james.medianotification.EXTRA_PACKAGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getStringExtra(EXTRA_PACKAGE);
        ComponentName componentName = null;
        int keycode = intent.getIntExtra(EXTRA_KEYCODE, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);

        if (packageName != null) {
            List<ResolveInfo> resolveInfos = context.getPackageManager().queryBroadcastReceivers(new Intent(Intent.ACTION_MEDIA_BUTTON), PackageManager.GET_RESOLVED_FILTER);
            for (ResolveInfo info : resolveInfos) {
                if (packageName.equals(info.resolvePackageName))
                    componentName = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);

            }
        }

        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);

        if (componentName != null)
            downIntent.setComponent(componentName);
        else if (packageName != null)
            downIntent.setPackage(packageName);

        long uptime = SystemClock.uptimeMillis();
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(uptime, uptime, KeyEvent.ACTION_DOWN, keycode, 0));
        context.sendOrderedBroadcast(downIntent, null);

        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);

        if (componentName != null)
            upIntent.setComponent(componentName);
        else if (packageName != null)
            upIntent.setPackage(packageName);

        uptime = SystemClock.uptimeMillis();
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(uptime, uptime, KeyEvent.ACTION_UP, keycode, 0));
        context.sendOrderedBroadcast(upIntent, null);
    }
}
