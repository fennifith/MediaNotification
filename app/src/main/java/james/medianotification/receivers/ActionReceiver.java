package james.medianotification.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.SystemClock;
import android.view.KeyEvent;

public class ActionReceiver extends BroadcastReceiver {

    public static final String EXTRA_KEYCODE = "james.medianotification.EXTRA_KEYCODE";

    @Override
    public void onReceive(Context context, Intent intent) {
        int keycode = intent.getIntExtra(EXTRA_KEYCODE, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        long uptime = SystemClock.uptimeMillis();
        audioManager.dispatchMediaKeyEvent(new KeyEvent(uptime, uptime, KeyEvent.ACTION_DOWN, keycode, 0));
        audioManager.dispatchMediaKeyEvent(new KeyEvent(uptime, uptime, KeyEvent.ACTION_UP, keycode, 0));
    }
}
