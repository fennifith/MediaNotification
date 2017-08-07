package james.medianotification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.ColorInt;
import android.support.v4.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.RemoteViews;

public class NotificationService extends NotificationListenerService {

    public static final String ACTION_PLAYSTATE_CHANGED = "com.android.music.playstatechanged";
    public static final String ACTION_TOGGLEPAUSE = "com.android.music.musicservicecommand.togglepause";
    public static final String ACTION_PAUSE = "com.android.music.musicservicecommand.pause";
    public static final String ACTION_PREVIOUS = "com.android.music.musicservicecommand.previous";
    public static final String ACTION_NEXT = "com.android.music.musicservicecommand.next";
    public static final String CMD_TOGGLEPAUSE = "togglepause";
    public static final String CMD_STOP = "stop";
    public static final String CMD_PAUSE = "pause";
    public static final String CMD_PLAY = "play";
    public static final String CMD_PREVIOUS = "previous";
    public static final String CMD_NEXT = "next";

    private NotificationManager manager;
    private MediaReceiver mediaReceiver;
    private SharedPreferences prefs;

    private boolean isConnected;
    private boolean isPlaying;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mediaReceiver = new MediaReceiver();
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        isConnected = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAYSTATE_CHANGED);
        filter.addAction(ACTION_TOGGLEPAUSE);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_PREVIOUS);
        filter.addAction(ACTION_NEXT);
        registerReceiver(mediaReceiver, filter);
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        if (isConnected) {
            unregisterReceiver(mediaReceiver);
            isConnected = false;
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Notification notification = sbn.getNotification();
        if (notification.extras.containsKey(NotificationCompat.EXTRA_MEDIA_SESSION)) {
            Bundle extras = NotificationCompat.getExtras(notification);

            String appName = sbn.getPackageName();
            try {
                appName = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(sbn.getPackageName(), PackageManager.GET_META_DATA)).toString();
            } catch (PackageManager.NameNotFoundException ignored) {
            }

            CharSequence title = extras.getCharSequence(NotificationCompat.EXTRA_TITLE);
            CharSequence text = extras.getCharSequence(NotificationCompat.EXTRA_TEXT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "music")
                    .setSmallIcon(R.drawable.ic_music)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(notification.contentIntent)
                    .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle())
                    .setCategory(NotificationCompat.getCategory(notification))
                    .setOngoing(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
            else builder.setPriority(Notification.PRIORITY_HIGH);

            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
            remoteViews.setTextViewText(R.id.appName, appName);
            remoteViews.setTextViewText(R.id.title, title);
            remoteViews.setTextViewText(R.id.subtitle, text);

            Palette.Swatch swatch = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notification.getLargeIcon() != null) {
                Bitmap bitmap = ImageUtils.drawableToBitmap(notification.getLargeIcon().loadDrawable(this));
                builder.setLargeIcon(bitmap);
                remoteViews.setImageViewBitmap(R.id.largeIcon, bitmap);
                swatch = PaletteUtils.getBestPaletteSwatchFrom(bitmap);
            } else if (notification.largeIcon != null) {
                builder.setLargeIcon(notification.largeIcon);
                remoteViews.setImageViewBitmap(R.id.largeIcon, notification.largeIcon);
                swatch = PaletteUtils.getBestPaletteSwatchFrom(notification.largeIcon);
            }

            if (swatch != null) {
                int titleTextColor = getTitleTextColor(swatch);
                int bodyTextColor = getBodyTextColor(swatch);
                remoteViews.setInt(R.id.background, "setBackgroundColor", swatch.getRgb());
                remoteViews.setInt(R.id.foregroundImage, "setColorFilter", swatch.getRgb());
                remoteViews.setTextColor(R.id.appName, titleTextColor);
                remoteViews.setTextColor(R.id.title, titleTextColor);
                remoteViews.setTextColor(R.id.subtitle, bodyTextColor);
                remoteViews.setInt(R.id.smallIcon, "setColorFilter", titleTextColor);
                remoteViews.setInt(R.id.first, "setColorFilter", titleTextColor);
                remoteViews.setInt(R.id.second, "setColorFilter", titleTextColor);
                remoteViews.setInt(R.id.third, "setColorFilter", titleTextColor);
                remoteViews.setInt(R.id.fourth, "setColorFilter", titleTextColor);
                remoteViews.setInt(R.id.fifth, "setColorFilter", titleTextColor);
            }

            Resources resources = null;
            try {
                resources = getPackageManager().getResourcesForApplication(sbn.getPackageName());
            } catch (PackageManager.NameNotFoundException ignored) {
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notification.getSmallIcon() != null)
                remoteViews.setImageViewIcon(R.id.smallIcon, notification.getSmallIcon());
            else if (resources != null)
                remoteViews.setImageViewBitmap(R.id.smallIcon, ImageUtils.drawableToBitmap(resources.getDrawable(notification.icon)));
            else remoteViews.setImageViewResource(R.id.smallIcon, R.drawable.ic_music);

            int actionCount = NotificationCompat.getActionCount(notification);
            for (int i = 0; i < 5; i++) {
                int id = -1;
                switch (i) {
                    case 0:
                        id = R.id.first;
                        break;
                    case 1:
                        id = R.id.second;
                        break;
                    case 2:
                        id = R.id.third;
                        break;
                    case 3:
                        id = R.id.fourth;
                        break;
                    case 4:
                        id = R.id.fifth;
                        break;
                }

                if (id == -1)
                    break;

                if (i >= actionCount) {
                    remoteViews.setViewVisibility(id, View.GONE);
                    continue;
                }

                NotificationCompat.Action action = NotificationCompat.getAction(notification, i);
                int icon = getActionIconRes(i, actionCount, action.getTitle().toString(), resources != null ? resources.getResourceEntryName(action.getIcon()) : "");
                PendingIntent intent = action.getActionIntent();

                builder.addAction(new NotificationCompat.Action.Builder(icon, action.getTitle(), intent).build());

                remoteViews.setViewVisibility(id, View.VISIBLE);
                remoteViews.setImageViewResource(id, icon);
                remoteViews.setOnClickPendingIntent(id, intent);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                builder.setColor(notification.color);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel(new NotificationChannel("music", "Music", NotificationManager.IMPORTANCE_HIGH));
                builder.setChannelId("music");
            }

            builder.setCustomContentView(remoteViews);
            builder.setCustomBigContentView(remoteViews);

            manager.notify(948, builder.build());
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    private int getActionIconRes(int i, int actionCount, String... names) {
        for (String name : names) {
            if (contains(name, "play"))
                return contains(name, "pause") ? (isPlaying ? R.drawable.ic_pause : R.drawable.ic_play) : R.drawable.ic_play;
            else if (contains(name, "pause"))
                return R.drawable.ic_pause;
            else if (contains(name, "prev"))
                return R.drawable.ic_skip_previous;
            else if (contains(name, "next"))
                return R.drawable.ic_skip_next;
            else if (contains(name, "stop"))
                return R.drawable.ic_stop;
            else if (contains(name, "down") || contains(name, "dislike") || contains(name, "unfavorite") || contains(name, "un-favorite"))
                return R.drawable.ic_thumb_down;
            else if (contains(name, "up") || contains(name, "like") || contains(name, "favorite"))
                return R.drawable.ic_thumb_up;
            else if (contains(name, "add"))
                return R.drawable.ic_add;
            else if (contains(name, "added") || contains(name, "check") || contains(name, "new"))
                return R.drawable.ic_check;
        }

        if (actionCount == 5) {
            if (i == 0)
                return R.drawable.ic_thumb_up;
            else if (i == 1)
                return R.drawable.ic_skip_previous;
            else if (i == 2)
                return isPlaying ? R.drawable.ic_pause : R.drawable.ic_play;
            else if (i == 3)
                return R.drawable.ic_skip_next;
            else if (i == 4)
                return R.drawable.ic_thumb_down;
        } else if (actionCount == 4) {
            if (i == 0)
                return R.drawable.ic_skip_previous;
            else if (i == 1)
                return R.drawable.ic_stop;
            else if (i == 2)
                return isPlaying ? R.drawable.ic_pause : R.drawable.ic_play;
            else if (i == 3)
                return R.drawable.ic_skip_next;
        } else if (actionCount == 3) {
            if (i == 0)
                return R.drawable.ic_skip_previous;
            else if (i == 1)
                return isPlaying ? R.drawable.ic_pause : R.drawable.ic_play;
            else if (i == 2)
                return R.drawable.ic_skip_next;
        }

        return R.drawable.ic_music;
    }

    private boolean contains(String container, String containee) {
        return container != null && containee != null && container.toLowerCase().contains(containee.toLowerCase());
    }

    @ColorInt
    private int getTitleTextColor(Palette.Swatch swatch) {
        boolean accessibilityEnabled = prefs.getBoolean(MainActivity.ACCESSIBILITY_SWITCH_KEY, false);
        return accessibilityEnabled ? (ColorUtils.isColorLight(swatch.getRgb()) ? Color.parseColor("#de000000") : Color.parseColor("#ffffffff")) : swatch.getBodyTextColor();
    }

    @ColorInt
    private int getBodyTextColor(Palette.Swatch swatch) {
        boolean accessibilityEnabled = prefs.getBoolean(MainActivity.ACCESSIBILITY_SWITCH_KEY, false);
        return accessibilityEnabled ? (ColorUtils.isColorLight(swatch.getRgb()) ? Color.parseColor("#8a000000") : Color.parseColor("#80ffffff")) : swatch.getTitleTextColor();
    }

    private class MediaReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction() != null ? intent.getAction() : "", cmd =
                    intent.hasExtra("command") ? intent.getStringExtra("command") : "";
            if (action.equals(ACTION_TOGGLEPAUSE) || cmd.equals(CMD_TOGGLEPAUSE) ||
                    (action.equals(ACTION_PLAYSTATE_CHANGED) && cmd.equals("")))
                isPlaying = !isPlaying;
            else isPlaying = action.equals(ACTION_PREVIOUS) || cmd.equals(CMD_PREVIOUS) ||
                    action.equals(ACTION_NEXT) || cmd.equals(CMD_NEXT) || cmd.equals(CMD_PLAY);
        }
    }
}
