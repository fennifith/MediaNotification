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
import android.util.Log;
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

    public static final String ACTION_UPDATE = "james.medianotification.ACTION_UPDATE";
    public static final String ACTION_DELETE = "james.medianotification.ACTION_DELETE";

    private List<PlayerData> players;

    private NotificationManager notificationManager;
    private ActivityManager activityManager;
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
    private boolean isVisible;
    private boolean isPlaying;
    private PlayerData currentPlayer;
    private int persistence;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaReceiver = new MediaReceiver();

        actions = new ArrayList<>();
        players = new ArrayList<>();

        players.add(new PlayerData(
                getString(R.string.app_name),
                null,
                null,
                null,
                null,
                -1,
                "com.android.music.playstatechanged",
                "com.android.music.playstatechanged.togglepause",
                "com.android.music.playstatechanged.pause",
                "com.android.music.playstatechanged.previous",
                "com.android.music.playstatechanged.next"));

        players.add(new PlayerData(
                getString(R.string.app_name_spotify),
                "com.spotify.music",
                PendingIntent.getBroadcast(this, 0, new Intent("com.spotify.mobile.android.ui.widget.PREVIOUS"), 0),
                PendingIntent.getBroadcast(this, 0, new Intent("com.spotify.mobile.android.ui.widget.PLAY"), 0),
                PendingIntent.getBroadcast(this, 0, new Intent("com.spotify.mobile.android.ui.widget.NEXT"), 0),
                -1,
                "com.spotify.music.playbackstatechanged",
                "com.spotify.music.metadatachanged"
        ));

        players.add(new PlayerData(
                "Phonograph",
                "com.kabouzeid.gramophone",
                1,
                "com.kabouzeid.gramophone.temp_sticky_intent_fix.metachanged",
                "com.kabouzeid.gramophone.temp_sticky_intent_fix.queuechanged",
                "com.kabouzeid.gramophone.temp_sticky_intent_fix.playstatechanged"
        ));

        players.add(new PlayerData(
                "Phonograph",
                "com.kabouzeid.gramophone",
                1,
                "com.kabouzeid.gramophone.metachanged",
                "com.kabouzeid.gramophone.queuechanged",
                "com.kabouzeid.gramophone.playstatechanged"
        ));

        players.add(new PlayerData(
                "HTC Music",
                "com.htc.music",
                "com.htc.music.playstatechanged",
                "com.htc.music.playbackcomplete",
                "com.htc.music.metachanged"
        ));

        players.add(new PlayerData(
                getString(R.string.app_name),
                "fm.last.android",
                "fm.last.android.playstatechanged",
                "fm.last.android.metachanged",
                "fm.last.android.playbackpaused",
                "fm.last.android.playbackcomplete"
        ));

        players.add(new PlayerData(
                "Samsung Music",
                "com.sec.android.app.music",
                "com.sec.android.app.music.playstatechanged"
        ));

        players.add(new PlayerData(
                "Winamp",
                "com.nullsoft.winamp",
                "com.nullsoft.winamp.playstatechanged",
                "com.nullsoft.winamp.metachanged"
        ));

        players.add(new PlayerData(
                "Amazon Music",
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

        players.add(new PlayerData(
                "Napster Music",
                "com.rhapsody",
                "com.rhapsody.playstatechanged"
        ));

        players.add(new PlayerData(
                "PowerAmp",
                "com.maxmpz.audioplayer",
                "com.maxmpz.audioplayer.playstatechanged"));

        players.add(new PlayerData(
                "Apollo",
                "com.andrew.apollo",
                "com.andrew.apollo.playstatechanged"
        ));

        players.add(new PlayerData(
                "PlayerPro",
                "com.tbig.playerpro",
                "com.tbig.playerpro.playstatechanged",
                "com.tbig.playerpro.metachanged"
        ));

        players.add(new PlayerData(
                "PlayerPro Trial",
                "com.tbig.playerprotrial",
                "com.tbig.playerprotrial.playstatechanged",
                "com.tbig.playerprotrial.metachanged"
        ));

        players.add(new PlayerData(
                "LG Music",
                "com.lge.music",
                "com.lge.music.playstatechanged"
        ));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_UPDATE:
                    if (isVisible)
                        updateNotification();
                    break;
                case ACTION_DELETE:
                    isVisible = false;
                    packageName = null;
                    appName = null;
                    smallIcon = null;
                    title = null;
                    subtitle = null;
                    largeIcon = null;
                    contentIntent = null;
                    actions.clear();

                    if (prefs.getBoolean(PreferenceUtils.PREF_FC_ON_DISMISS, false) && packageName != null)
                        activityManager.killBackgroundProcesses(packageName);

                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
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

        Intent deleteIntent = new Intent(this, NotificationService.class);
        deleteIntent.setAction(ACTION_DELETE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "music")
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle(title)
                .setContentText(subtitle)
                .setDeleteIntent(PendingIntent.getService(this, 0, deleteIntent, 0))
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle())
                .setOngoing(isPlaying && !prefs.getBoolean(PreferenceUtils.PREF_ALWAYS_DISMISSIBLE, false));

        if (contentIntent != null)
            builder.setContentIntent(contentIntent);
        else if (prefs.contains(PreferenceUtils.PREF_DEFAULT_MUSIC_PLAYER)) {
            try {
                Intent contentIntent = getPackageManager().getLaunchIntentForPackage(prefs.getString(PreferenceUtils.PREF_DEFAULT_MUSIC_PLAYER, ""));
                builder.setContentIntent(PendingIntent.getActivity(this, 0, contentIntent, 0));
            } catch (Exception ignored) {
            }
        }

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
        isVisible = true;
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
        if (sbn.getNotification().extras.containsKey(NotificationCompat.EXTRA_MEDIA_SESSION)) {
            notificationManager.cancel(948);
            isVisible = false;
        }
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
        if (ContextCompat.checkSelfPermission(this, "android.permission.UPDATE_APP_OPS_STATS") != PackageManager.PERMISSION_GRANTED)
            return false;

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

    private void getAlbumArt(String albumName, String artistName) {
        String baseUrl;

        try {
            baseUrl = "http://ws.audioscrobbler.com/2.0/?method=album.getInfo"
                    + "&api_key=" + getString(R.string.last_fm_api_key)
                    + "&album=" + URLEncoder.encode(albumName, "UTF-8")
                    + "&artist=" + URLEncoder.encode(artistName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return;
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
                    if (source.contains("<lfm status=\"failed\">")) {
                        largeIcon = null;
                    } else {
                        int startIndex = source.indexOf("<image size=\"large\">") + 20;
                        String image = source.substring(startIndex, source.indexOf("<", startIndex));

                        largeIcon = BitmapFactory.decodeStream(new URL(image).openConnection().getInputStream());
                    }
                } catch (Exception ignored) {
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        updateNotification();
                    }
                });
            }
        }.start();
    }

    private class MediaReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("playing"))
                isPlaying = intent.getBooleanExtra("playing", false);
            else isPlaying = audioManager.isMusicActive();

            String track = intent.getStringExtra("track");
            String album = intent.getStringExtra("album");
            String artist = intent.getStringExtra("artist");

            String action = intent.getAction();
            PlayerData playerData = null;
            if (action != null) {
                for (PlayerData player : players) {
                    if (player.hasAction(action))
                        playerData = player;
                }
            }

            if (appName == null)
                appName = context.getString(R.string.app_name);

            if (prefs.getBoolean(PreferenceUtils.PREF_USE_RECEIVER, false) && (packageName == null || !setNotificationBlocking(packageName, AppOpsManager.MODE_ALLOWED))) {
                int trackId = intent.getIntExtra("id", -1);
                if (trackId != -1 && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    try {
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

                        largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), albumArtUri);
                    } catch (Exception e) {
                        if (prefs.getBoolean(PreferenceUtils.PREF_USE_LASTFM, true) && album != null && artist != null)
                            getAlbumArt(album, artist);
                    }
                } else if (prefs.getBoolean(PreferenceUtils.PREF_USE_LASTFM, true) && album != null && artist != null)
                    getAlbumArt(album, artist);

                if (--persistence < 0 || (playerData != null && playerData.persistence > 0)) {
                    if (playerData != null) {
                        appName = playerData.name;
                        packageName = playerData.packageName;
                        if (playerData.packageName != null) {
                            try {
                                contentIntent = PendingIntent.getActivity(context, 0, context.getPackageManager().getLaunchIntentForPackage(playerData.packageName), 0);
                            } catch (Exception e) {
                                contentIntent = null;
                            }
                        } else contentIntent = null;
                    } else contentIntent = null;

                    actions.clear();
                    boolean shouldUseKeyCodes = prefs.getInt(PreferenceUtils.PREF_MEDIA_CONTROLS_METHOD, PreferenceUtils.CONTROLS_METHOD_NONE) != PreferenceUtils.CONTROLS_METHOD_NONE;

                    PendingIntent previousIntent = null;
                    if (playerData != null && playerData.previousIntent != null)
                        previousIntent = playerData.previousIntent;
                    else if (shouldUseKeyCodes) {
                        Intent actionIntent = new Intent(context, ActionReceiver.class);
                        actionIntent.putExtra(ActionReceiver.EXTRA_KEYCODE, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                        previousIntent = PendingIntent.getBroadcast(context, 0, actionIntent, 0);
                    }

                    if (previousIntent != null) {
                        actions.add(new NotificationCompat.Action(
                                R.drawable.ic_skip_previous,
                                "Previous",
                                previousIntent
                        ));
                    }

                    PendingIntent playPauseIntent = null;
                    if (playerData != null && playerData.playPauseIntent != null)
                        playPauseIntent = playerData.playPauseIntent;
                    else if (shouldUseKeyCodes) {
                        Intent actionIntent = new Intent(context, ActionReceiver.class);
                        actionIntent.putExtra(ActionReceiver.EXTRA_KEYCODE, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                        playPauseIntent = PendingIntent.getBroadcast(context, 0, actionIntent, 0);
                    }

                    if (playPauseIntent != null) {
                        actions.add(new NotificationCompat.Action(
                                isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                                isPlaying ? "Pause" : "Play",
                                playPauseIntent
                        ));
                    }

                    PendingIntent nextIntent = null;
                    if (playerData != null && playerData.nextIntent != null)
                        nextIntent = playerData.nextIntent;
                    else if (shouldUseKeyCodes) {
                        Intent actionIntent = new Intent(context, ActionReceiver.class);
                        actionIntent.putExtra(ActionReceiver.EXTRA_KEYCODE, KeyEvent.KEYCODE_MEDIA_NEXT);
                        nextIntent = PendingIntent.getBroadcast(context, 0, actionIntent, 0);
                    }

                    if (nextIntent != null) {
                        actions.add(new NotificationCompat.Action(
                                R.drawable.ic_skip_next,
                                "Next",
                                nextIntent
                        ));
                    }

                    currentPlayer = playerData;
                    Log.d("MediaReceiver", "actions changed");
                } else {
                    if (actions.size() == 3) {
                        NotificationCompat.Action removed = actions.remove(1);
                        actions.add(1, new NotificationCompat.Action(
                                isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                                isPlaying ? "Pause" : "Play",
                                removed.getActionIntent()
                        ));
                    }
                }

                if (playerData != null)
                    persistence = playerData.persistence;

                Log.d("MediaReceiver", intent.getAction() + ", Current player: " + (currentPlayer != null ? currentPlayer.packageName : "null") + ", Sending player: " + (playerData != null ? playerData.packageName : "null") + ", Persistence: " + persistence);
            }

            if (track != null)
                title = track;
            if (artist != null)
                subtitle = artist;
            else if (album != null)
                subtitle = album;

            if (smallIcon == null)
                smallIcon = ImageUtils.getVectorBitmap(context, R.drawable.ic_music);

            updateNotification();
        }
    }
}
