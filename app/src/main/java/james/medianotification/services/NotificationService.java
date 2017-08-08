package james.medianotification.services;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RemoteViews;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import james.medianotification.R;
import james.medianotification.data.PlayerData;
import james.medianotification.receivers.ActionReceiver;
import james.medianotification.utils.ImageUtils;
import james.medianotification.utils.PaletteUtils;
import james.medianotification.utils.PreferenceUtils;

public class NotificationService extends NotificationListenerService {

    public static final String ACTION_COMMAND = "com.android.music.musicservicecommand";
    public static final String CMD_TOGGLEPAUSE = "togglepause";
    public static final String CMD_STOP = "stop";
    public static final String CMD_PAUSE = "pause";
    public static final String CMD_PLAY = "play";
    public static final String CMD_PREVIOUS = "previous";
    public static final String CMD_NEXT = "next";

    private List<PlayerData> players;

    private NotificationManager notificationManager;
    private AudioManager audioManager;
    private MediaReceiver mediaReceiver;
    private SharedPreferences prefs;

    private String packageName;
    private String appName;
    private Bitmap smallIcon;
    private String title;
    private String subtitle;
    private Bitmap largeIcon;
    private PendingIntent contentIntent;
    private List<NotificationCompat.Action> actions;

    private boolean isConnected;
    private boolean isPlaying;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaReceiver = new MediaReceiver();

        actions = new ArrayList<>();
        players = new ArrayList<>();

        players.add(new PlayerData(
                getString(R.string.app_name),
                null,
                "com.android.music.playstatechanged",
                "com.android.music.playstatechanged.togglepause",
                "com.android.music.playstatechanged.pause",
                "com.android.music.playstatechanged.previous",
                "com.android.music.playstatechanged.next"));

        players.add(new PlayerData(
                getString(R.string.app_name_spotify),
                "com.spotify.music",
                "com.spotify.music.playbackstatechanged",
                "com.spotify.music.metadatachanged"
        ));

        players.add(new PlayerData(
                "HTC Music",
                "com.htc.music",
                "com.htc.music.playstatechanged",
                "com.htc.music.playbackcomplete",
                "com.htc.music.metachanged"
        ));

        players.add(new PlayerData(
                "Last.fm",
                "fm.last.android",
                "fm.last.android.playstatechanged",
                "fm.last.android.metachanged",
                "fm.last.android.playbackpaused",
                "fm.last.android.playbackcomplete"
        ));

        players.add(new PlayerData(getString(R.string.app_name), "com.sec.android.app.music.playstatechanged"));

        players.add(new PlayerData(
                "Winamp",
                "com.nullsoft.winamp",
                "com.nullsoft.winamp.playstatechanged",
                "com.nullsoft.winamp.metachanged"
        ));

        players.add(new PlayerData(
                "Amazon",
                "com.amazon.mp3",
                "com.amazon.mp3.playstatechanged",
                "com.amazon.mp3.metachanged"
        ));

        players.add(new PlayerData(
                "MIUI Player",
                "com.miui.player",
                "com.miui.player.playstatechanged",
                "com.miui.player.playbackcomplete",
                "com.miui.player.metachanged"
        ));

        players.add(new PlayerData(
                "Real",
                "com.real.IMP",
                "com.real.IMP.playstatechanged",
                "com.real.IMP.playbackcomplete",
                "com.real.IMP.metachanged"
        ));

        players.add(new PlayerData(
                "SEMC Music Player",
                "com.sonyericsson.music",
                "com.sonyericsson.music.playbackcontrol.ACTION_TRACK_STARTED",
                "com.sonyericsson.music.playbackcontrol.ACTION_PAUSED",
                "com.sonyericsson.music.TRACK_COMPLETED",
                "com.sonyericsson.music.metachanged",
                "com.sonyericsson.music.playbackcomplete",
                "com.sonyericsson.music.playstatechanged"
        ));

        players.add(new PlayerData(
                "rdio",
                "com.rdio.android",
                "com.rdio.android.metachanged",
                "com.rdio.android.playstatechanged"));

        players.add(new PlayerData(
                "Samsung Music Player",
                "com.samsung.sec.android",
                "com.samsung.sec.android.MusicPlayer.playstatechanged",
                "com.samsung.sec.android.MusicPlayer.playbackcomplete",
                "com.samsung.sec.android.MusicPlayer.metachanged",
                "com.sec.android.app.music.playstatechanged",
                "com.sec.android.app.music.playbackcomplete",
                "com.sec.android.app.music.metachanged"
        ));

        players.add(new PlayerData("Rhapsody", "com.rhapsody.playstatechanged"));

        players.add(new PlayerData("PowerAmp", "com.maxmpz.audioplayer.playstatechanged"));

        players.add(new PlayerData("Apollo", "com.andrew.apollo.playstatechanged"));

        players.add(new PlayerData("PlayerPro", "com.tbig.playerpro.playstatechanged"));

        players.add(new PlayerData("LG Music", "com.lge.music.playstatechanged"));
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        isConnected = true;
        IntentFilter filter = new IntentFilter();
        for (PlayerData player : players) {
            for (String action : player.actions) {
                filter.addAction(action);
            }
        }
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

    public void updateNotification() {
        if (!isConnected)
            return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "music")
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle(title)
                .setContentText(subtitle)
                .setContentIntent(contentIntent)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle())
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        else builder.setPriority(Notification.PRIORITY_HIGH);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
        remoteViews.setTextViewText(R.id.appName, appName);
        remoteViews.setTextViewText(R.id.title, title);
        remoteViews.setTextViewText(R.id.subtitle, subtitle);

        if (smallIcon == null)
            smallIcon = ImageUtils.getVectorBitmap(this, R.drawable.ic_music);

        remoteViews.setImageViewBitmap(R.id.largeIcon, largeIcon);
        Palette.Swatch swatch = PaletteUtils.generateSwatch(this, largeIcon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.setColor(swatch.getRgb());

        int color = PaletteUtils.getTextColor(this, swatch);
        remoteViews.setInt(R.id.background, "setBackgroundColor", swatch.getRgb());
        remoteViews.setInt(R.id.foregroundImage, "setColorFilter", swatch.getRgb());
        remoteViews.setImageViewBitmap(R.id.smallIcon, ImageUtils.setBitmapColor(smallIcon, color));
        remoteViews.setTextColor(R.id.appName, color);
        remoteViews.setTextColor(R.id.title, color);
        remoteViews.setTextColor(R.id.subtitle, color);

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

            NotificationCompat.Action action;
            if (i >= actions.size()) {
                remoteViews.setViewVisibility(id, View.GONE);
                continue;
            } else action = actions.get(i);

            builder.addAction(action);

            remoteViews.setViewVisibility(id, View.VISIBLE);
            remoteViews.setImageViewBitmap(id, ImageUtils.setBitmapColor(ImageUtils.getVectorBitmap(this, action.getIcon()), color));
            remoteViews.setOnClickPendingIntent(id, action.getActionIntent());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("music", "Music", NotificationManager.IMPORTANCE_HIGH));
            builder.setChannelId("music");
        }

        builder.setCustomContentView(remoteViews);
        builder.setCustomBigContentView(remoteViews);

        notificationManager.notify(948, builder.build());
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        if (notification.extras.containsKey(NotificationCompat.EXTRA_MEDIA_SESSION)) {
            Bundle extras = NotificationCompat.getExtras(notification);

            if (extras.containsKey(NotificationCompat.EXTRA_TITLE))
                title = extras.getString(NotificationCompat.EXTRA_TITLE, title);
            if (extras.containsKey(NotificationCompat.EXTRA_TEXT))
                subtitle = extras.getString(NotificationCompat.EXTRA_TEXT, subtitle);

            contentIntent = notification.contentIntent;

            Resources resources = null;
            try {
                resources = getPackageManager().getResourcesForApplication(sbn.getPackageName());
            } catch (PackageManager.NameNotFoundException ignored) {
            }

            if (packageName == null || !packageName.equals(sbn.getPackageName())) {
                appName = sbn.getPackageName();
                try {
                    appName = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(sbn.getPackageName(), PackageManager.GET_META_DATA)).toString();
                } catch (PackageManager.NameNotFoundException ignored) {
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notification.getSmallIcon() != null)
                    smallIcon = ImageUtils.drawableToBitmap(notification.getSmallIcon().loadDrawable(this));
                else if (resources != null)
                    smallIcon = ImageUtils.drawableToBitmap(resources.getDrawable(notification.icon));
                else smallIcon = null;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notification.getLargeIcon() != null)
                largeIcon = ImageUtils.drawableToBitmap(notification.getLargeIcon().loadDrawable(this));
            else if (notification.largeIcon != null)
                largeIcon = notification.largeIcon;
            else largeIcon = null;

            actions.clear();

            int actionCount = NotificationCompat.getActionCount(notification);
            for (int i = 0; i < actionCount; i++) {
                NotificationCompat.Action action = NotificationCompat.getAction(notification, i);
                int icon = getActionIconRes(i, actionCount, action.getTitle().toString(), resources != null ? resources.getResourceEntryName(action.getIcon()) : "");
                PendingIntent intent = action.getActionIntent();

                actions.add(new NotificationCompat.Action.Builder(icon, action.getTitle(), intent).build());
            }

            updateNotification();

            packageName = sbn.getPackageName();
            setNotificationBlocking(packageName, AppOpsManager.MODE_IGNORED);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
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

    private boolean setNotificationBlocking(String packageName, int mode) {
        int uid = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                uid = getPackageManager().getPackageUid(packageName, 0);
            else
                uid = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA).uid;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        try {
            AppOpsManager opsManager = (AppOpsManager) getSystemService(APP_OPS_SERVICE);

            Field postNotificationField = opsManager.getClass().getField("OP_POST_NOTIFICATION");
            postNotificationField.setAccessible(true);
            Integer postNotification = (Integer) postNotificationField.get(null);

            Method setMode = opsManager.getClass().getMethod("setMode", int.class, int.class, String.class, int.class);
            setMode.setAccessible(true);
            setMode.invoke(opsManager, postNotification, uid, packageName, mode);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean contains(String container, String containee) {
        return container != null && containee != null && container.toLowerCase().contains(containee.toLowerCase());
    }

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (NotificationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private class MediaReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("playing"))
                isPlaying = intent.getBooleanExtra("playing", false);
            else isPlaying = audioManager.isMusicActive();

            String action = intent.getAction();
            if (action != null) {
                for (PlayerData player : players) {
                    if (player.hasAction(action)) {
                        appName = player.name;
                        contentIntent = player.getLaunchIntent(context);
                    }
                }
            }

            if (appName == null)
                appName = context.getString(R.string.app_name);

            if (prefs.getBoolean(PreferenceUtils.PREF_USE_RECEIVER, false) && (packageName == null || !setNotificationBlocking(packageName, AppOpsManager.MODE_ALLOWED))) {
                int trackId = intent.getIntExtra("id", -1);
                if (trackId != -1 && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Cursor cursor = getContentResolver().query(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID},
                            MediaStore.Audio.Media._ID + " = " + trackId,
                            null,
                            null
                    );

                    Uri albumArtUri = null;
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            albumArtUri = ContentUris.withAppendedId(
                                    Uri.parse("content://media/external/audio/albumart"),
                                    cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                            );
                        }
                        cursor.close();
                    }

                    try {
                        largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), albumArtUri);
                    } catch (Exception e) {
                        largeIcon = null;
                    }
                } else if (prefs.getBoolean(PreferenceUtils.PREF_USE_LASTFM, true)) {
                    String baseUrl = "http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=" + getString(R.string.last_fm_api_key);

                    try {
                        if (intent.hasExtra("artist"))
                            baseUrl += "&artist=" + URLEncoder.encode(intent.getStringExtra("artist"), "UTF-8");
                    } catch (UnsupportedEncodingException ignored) {
                    }

                    try {
                        if (intent.hasExtra("album"))
                            baseUrl += "&album=" + URLEncoder.encode(intent.getStringExtra("album"), "UTF-8");
                    } catch (UnsupportedEncodingException ignored) {
                    }

                    final String url = baseUrl;

                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
                                request.connect();

                                BufferedReader r = new BufferedReader(new InputStreamReader((InputStream) request.getContent()));
                                StringBuilder total = new StringBuilder();
                                String line;
                                while ((line = r.readLine()) != null) {
                                    total.append(line).append('\n');
                                }

                                String source = total.toString();
                                int startIndex = source.indexOf("<image size=\"large\">") + 20;
                                String image = source.substring(startIndex, source.indexOf("<", startIndex));

                                largeIcon = BitmapFactory.decodeStream(new URL(image).openConnection().getInputStream());
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateNotification();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                largeIcon = null;
                            }
                        }
                    }.start();

                    largeIcon = null;
                }

                actions.clear();

                Intent previous = new Intent(context, ActionReceiver.class);
                previous.putExtra(ActionReceiver.EXTRA_KEYCODE, KeyEvent.KEYCODE_MEDIA_PREVIOUS);

                actions.add(new NotificationCompat.Action(
                        R.drawable.ic_skip_previous,
                        "Previous",
                        PendingIntent.getBroadcast(context, 0, previous, 0)
                ));

                Intent playPause = new Intent(context, ActionReceiver.class);
                playPause.putExtra(ActionReceiver.EXTRA_KEYCODE, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);

                actions.add(new NotificationCompat.Action(
                        isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                        isPlaying ? "Pause" : "Play",
                        PendingIntent.getBroadcast(context, 0, playPause, 0)
                ));

                Intent next = new Intent(context, ActionReceiver.class);
                next.putExtra(ActionReceiver.EXTRA_KEYCODE, KeyEvent.KEYCODE_MEDIA_NEXT);

                actions.add(new NotificationCompat.Action(
                        R.drawable.ic_skip_next,
                        "Next",
                        PendingIntent.getBroadcast(context, 0, next, 0)
                ));
            }

            if (intent.hasExtra("track"))
                title = intent.getStringExtra("track");

            if (intent.hasExtra("album"))
                subtitle = intent.getStringExtra("album");
            else if (intent.hasExtra("artist"))
                subtitle = intent.getStringExtra("artist");

            if (smallIcon == null)
                smallIcon = ImageUtils.getVectorBitmap(context, R.drawable.ic_music);

            updateNotification();
        }
    }
}
