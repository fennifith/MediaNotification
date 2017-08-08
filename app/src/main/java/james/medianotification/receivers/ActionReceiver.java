package james.medianotification.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.KeyEvent;

public class ActionReceiver extends BroadcastReceiver {

    public static final String EXTRA_KEYCODE = "james.medianotification.EXTRA_KEYCODE";

    @Override
    public void onReceive(Context context, Intent intent) {
        long uptime = SystemClock.uptimeMillis();

        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(uptime, uptime, KeyEvent.ACTION_DOWN, intent.getIntExtra(EXTRA_KEYCODE, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE), 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        context.sendOrderedBroadcast(downIntent, null);
    }
}
