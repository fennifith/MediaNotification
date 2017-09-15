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
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
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
import james.medianotification.utils.PlayerUtils;
import james.medianotification.utils.PreferenceUtils;
import james.medianotification.utils.RemoteViewsUtils;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

public class NotificationService extends NotificationListenerService {

    public static final String ACTION_UPDATE = "james.medianotification.ACTION_UPDATE";
    public static final String ACTION_UPDATE_RECEIVER = "james.medianotification.ACTION_UPDATE_RECEIVER";
    public static final String ACTION_DELETE = "james.medianotification.ACTION_DELETE";

    private List<PlayerData> players;

    private NotificationManager notificationManager;
    private ActivityManager activityManager;
    private AudioManager audioManager;
    private MediaReceiver mediaReceiver;
    private Target imageTarget;
    private SharedPreferences prefs;

    private String packageName;
    private String appName;
    private Bitmap smallIcon;
    private String title;
    private String subtitle;
    private Bitmap largeIcon;
    private PendingIntent contentIntent;
    private List<NotificationCompat.Action> actions;
    private List<Bitmap> actionIcons;

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
        actionIcons = new ArrayList<>();
        players = PlayerUtils.getPlayers(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_UPDATE:
                    if (isVisible)
                        updateNotification();
                    break;
                case ACTION_UPDATE_RECEIVER:
                    if (isConnected) {
                        onListenerDisconnected();
                        onListenerConnected();
                    }
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
                    actionIcons.clear();

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
            if (player.isEnabled(prefs)) {
                for (String action : player.actions) {
                    filter.addAction(action);
                }
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
        if (!isConnected || (packageName == null && title == null && subtitle == null))
            return;

        Intent deleteIntent = new Intent(this, NotificationService.class);
        deleteIntent.setAction(ACTION_DELETE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "music")
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle(title)
                .setContentText(subtitle)
                .setDeleteIntent(PendingIntent.getService(this, 0, deleteIntent, 0))
                //.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle())
                .setOngoing(isPlaying && !prefs.getBoolean(PreferenceUtils.PREF_ALWAYS_DISMISSIBLE, false))
                .setVisibility(VISIBILITY_PUBLIC);

        if (contentIntent != null)
            builder.setContentIntent(contentIntent);
        else {
            packageName = prefs.getString(PreferenceUtils.PREF_DEFAULT_MUSIC_PLAYER, null);
            if (packageName != null) {
                try {
                    Intent contentIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                    builder.setContentIntent(PendingIntent.getActivity(this, 0, contentIntent, 0));
                } catch (Exception ignored) {
                }

                try {
                    appName = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA).loadLabel(getPackageManager()).toString();
                } catch (Exception ignored) {
                }
            }
        }

        if (appName == null)
            appName = getString(R.string.app_name);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setPriority(NotificationManager.IMPORTANCE_MAX);
        else builder.setPriority(Notification.PRIORITY_MAX);

        for (NotificationCompat.Action action : actions) {
            builder.addAction(action);
        }

        if (smallIcon == null)
            smallIcon = ImageUtils.getVectorBitmap(this, R.drawable.ic_music);

        builder.setCustomContentView(getContentView(true));
        if (actions.size() > 0)
            builder.setCustomBigContentView(getContentView(false));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("music", "Music", NotificationManager.IMPORTANCE_HIGH));
            builder.setChannelId("music");
        }

        notificationManager.notify(948, builder.build());
        isVisible = true;
    }

    private RemoteViews getContentView(boolean isCollapsed) {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), isCollapsed ? R.layout.layout_notification_collapsed : R.layout.layout_notification_expanded);
        remoteViews.setTextViewText(R.id.appName, appName + " \u2022 " + (isPlaying ? "Playing" : "Paused"));
        remoteViews.setTextViewText(R.id.title, title);
        remoteViews.setTextViewText(R.id.subtitle, subtitle);

        remoteViews.setViewVisibility(R.id.largeIcon, prefs.getBoolean(PreferenceUtils.PREF_SHOW_ALBUM_ART, true) ? View.VISIBLE : View.GONE);
        remoteViews.setImageViewBitmap(R.id.largeIcon, largeIcon);
        Palette palette = PaletteUtils.getPalette(this, largeIcon);
        Palette.Swatch swatch = PaletteUtils.getSwatch(this, palette);

        int color = PaletteUtils.getTextColor(this, palette, swatch);
        remoteViews.setInt(R.id.image, "setBackgroundColor", swatch.getRgb());
        remoteViews.setInt(R.id.foregroundImage, "setColorFilter", swatch.getRgb());
        remoteViews.setInt(R.id.arrow, "setColorFilter", color);
        remoteViews.setImageViewBitmap(R.id.smallIcon, ImageUtils.setBitmapColor(smallIcon, color));
        remoteViews.setTextColor(R.id.appName, color);
        remoteViews.setTextColor(R.id.title, color);
        remoteViews.setTextColor(R.id.subtitle, color);

        TypedArray typedArray = obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground});
        int selectableItemBackground = typedArray.getResourceId(0, 0);
        typedArray.recycle();

        remoteViews.setInt(R.id.content, "setBackgroundResource", selectableItemBackground);

        boolean useNotificationIcons = !prefs.getBoolean(PreferenceUtils.PREF_FORCE_MD_ICONS, false) && actionIcons.size() == actions.size();

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

            remoteViews.setViewVisibility(id, View.VISIBLE);
            remoteViews.setImageViewBitmap(id, ImageUtils.setBitmapColor(useNotificationIcons ? actionIcons.get(i) : ImageUtils.getVectorBitmap(this, action.getIcon()), color));
            remoteViews.setInt(id, "setBackgroundResource", selectableItemBackground);
            remoteViews.setOnClickPendingIntent(id, action.getActionIntent());
        }

        return remoteViews;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        if (notification.extras.containsKey(NotificationCompat.EXTRA_MEDIA_SESSION)
                || RemoteViewsUtils.NETEASE_CLOUDMUSIC_PACKAGE_NAME.equals(sbn.getPackageName())) {
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notification.getSmallIcon() != null) {
                    try {
                        smallIcon = ImageUtils.drawableToBitmap(notification.getSmallIcon().loadDrawable(this));
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                    }
                }
	            // Avoid invalid smallIcon bitmap
                if ((smallIcon == null || smallIcon.getWidth() <= 0
                        || smallIcon.getHeight() <= 0
                        || smallIcon.getByteCount() <= 4) && resources != null) {
                    smallIcon = ImageUtils.drawableToBitmap(ResourcesCompat.getDrawable(resources, notification.icon, resources.newTheme()));
                } else {
                    smallIcon = null;
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notification.getLargeIcon() != null)
                largeIcon = ImageUtils.drawableToBitmap(notification.getLargeIcon().loadDrawable(this));
            else if (notification.largeIcon != null)
                largeIcon = notification.largeIcon;
            else largeIcon = null;

            actions.clear();
            actionIcons.clear();

            int actionCount = NotificationCompat.getActionCount(notification);
            for (int i = 0; i < actionCount; i++) {
                NotificationCompat.Action action = NotificationCompat.getAction(notification, i);
                String entryName = "";
                if (resources != null) {
                    try {
                        entryName = resources.getResourceEntryName(action.getIcon());
                    } catch (Exception ignored) {
                    }

                    try {
                        actionIcons.add(ImageUtils.drawableToBitmap(ResourcesCompat.getDrawable(resources, action.getIcon(), resources.newTheme())));
                    } catch (Exception ignored) {
                    }
                }

                int icon = getActionIconRes(i, actionCount, action.getTitle().toString(), entryName);
                PendingIntent intent = action.getActionIntent();

                actions.add(new NotificationCompat.Action.Builder(icon, action.getTitle(), intent).build());
            }

            // Support for Netease Cloudmusic (a online music app in China)
            if (RemoteViewsUtils.NETEASE_CLOUDMUSIC_PACKAGE_NAME.equals(sbn.getPackageName())) {
                List<String> texts = RemoteViewsUtils.findNeteaseMusicCurrentStates(notification);
                Log.i("TAG", "Try to get text: " + texts);
                String toggleIconId = null;
                if (texts != null && texts.size() >= 2) {
                    title = texts.get(0);
                    subtitle = texts.get(1);
                    if (texts.size() >= 3) {
                        toggleIconId = texts.get(2);
                    }
                } else if (texts != null && texts.size() == 1) {
                    toggleIconId = texts.get(0);
                }
                if (RemoteViewsUtils.NETEASE_CLOUDMUSIC_PLAY_ICON_ID.equals(toggleIconId)) {
                    isPlaying = false;
                } else if (RemoteViewsUtils.NETEASE_CLOUDMUSIC_PAUSE_ICON_ID.equals(toggleIconId)) {
                    isPlaying = true;
                    // Set netease cloudmusic as current player
                    if (currentPlayer == null || !"com.netease.cloudmusic".equals(currentPlayer.packageName)) {
                        PlayerData neteasePlayer = null;
                        for (PlayerData player : players) {
                            if ("com.netease.cloudmusic".equals(player.packageName)) {
                                neteasePlayer = player;
                                break;
                            }
                        }
                        currentPlayer = neteasePlayer;
                    }
                }
            }

            updateNotification();

            packageName = sbn.getPackageName();
            if (!setNotificationBlocking(packageName, AppOpsManager.MODE_IGNORED) && prefs.getBoolean(PreferenceUtils.PREF_CANCEL_ORIGINAL_NOTIFICATION, false)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    cancelNotification(sbn.getKey());
                else cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (sbn.getNotification().extras.containsKey(NotificationCompat.EXTRA_MEDIA_SESSION)
                || RemoteViewsUtils.NETEASE_CLOUDMUSIC_PACKAGE_NAME.equals(sbn.getPackageName())) {
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
        if (imageTarget != null)
            Glide.with(this).clear(imageTarget);

        String baseUrl;

        try {
            baseUrl = "http://ws.audioscrobbler.com/2.0/?method=album.getInfo"
                    + "&api_key=" + getString(R.string.last_fm_api_key)
                    + "&album=" + URLEncoder.encode(albumName, "UTF-8")
                    + "&artist=" + URLEncoder.encode(artistName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return;
        }

        new LastFmImageThread(this, baseUrl).start();
    }

    private class MediaReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MediaReceiver", "Action received: " + intent.getAction());

            String action = intent.getAction();
            PlayerData playerData = null;
            if (action != null) {
                for (PlayerData player : players) {
                    if (player.hasAction(action))
                        playerData = player;
                }
            }

            if (intent.hasExtra("playing"))
                isPlaying = intent.getBooleanExtra("playing", false);
            else isPlaying = audioManager.isMusicActive();

            if (playerData != null && playerData.reversePlayPause)
                isPlaying = !isPlaying;

            String track = intent.getStringExtra("track");
            String album = intent.getStringExtra("album");
            String artist = intent.getStringExtra("artist");

            if (prefs.getBoolean(PreferenceUtils.PREF_USE_RECEIVER, false) && (packageName == null || !setNotificationBlocking(packageName, AppOpsManager.MODE_ALLOWED))) {
                if (playerData != null && playerData.clearData) {
                    title = null;
                    subtitle = null;
                    largeIcon = null;
                }

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
                        if (playerData.packageName == null) {
                            if (intent.hasExtra("app-package"))
                                packageName = intent.getStringExtra("app-package");
                            else if (intent.hasExtra("scrobbling_source"))
                                packageName = intent.getStringExtra("scrobbling_source");
                            else packageName = null;

                            if (intent.hasExtra("app-name"))
                                appName = intent.getStringExtra("app-name");
                            else if (packageName != null) {
                                try {
                                    appName = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA).loadLabel(getPackageManager()).toString();
                                } catch (Exception e) {
                                    appName = null;
                                }
                            } else appName = null;
                        } else {
                            appName = playerData.name;
                            packageName = playerData.packageName;
                        }

                        if (packageName != null) {
                            try {
                                contentIntent = PendingIntent.getActivity(context, 0, context.getPackageManager().getLaunchIntentForPackage(packageName), 0);
                            } catch (Exception e) {
                                contentIntent = null;
                            }
                        } else contentIntent = null;
                    } else {
                        appName = null;
                        packageName = null;
                        contentIntent = null;
                    }

                    actions.clear();
                    actionIcons.clear();
                    boolean shouldUseKeyCodes = prefs.getInt(PreferenceUtils.PREF_MEDIA_CONTROLS_METHOD, PreferenceUtils.CONTROLS_METHOD_NONE) != PreferenceUtils.CONTROLS_METHOD_NONE;

                    PendingIntent previousIntent = null;
                    if (playerData != null && playerData.previousIntent != null)
                        previousIntent = playerData.previousIntent;
                    else if (shouldUseKeyCodes) {
                        Intent actionIntent = new Intent(context, ActionReceiver.class);
                        actionIntent.putExtra(ActionReceiver.EXTRA_KEYCODE, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                        actionIntent.putExtra(ActionReceiver.EXTRA_PACKAGE, packageName);
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
                        actionIntent.putExtra(ActionReceiver.EXTRA_PACKAGE, packageName);
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
                        actionIntent.putExtra(ActionReceiver.EXTRA_PACKAGE, packageName);
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
                    Log.d("MediaReceiver", "actions changed, target: " + packageName);
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

            updateNotification();
        }
    }

    private static class LastFmImageThread extends Thread {

        private WeakReference<NotificationService> serviceReference;
        private String url;

        public LastFmImageThread(NotificationService service, String url) {
            serviceReference = new WeakReference<>(service);
            this.url = url;
        }

        @Override
        public void run() {
            String image = null;

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
                    NotificationService service = serviceReference.get();
                    if (service != null)
                        service.largeIcon = null;
                } else {
                    int startIndex = source.indexOf("<image size=\"large\">") + 20;
                    image = source.substring(startIndex, source.indexOf("<", startIndex));
                }
            } catch (Exception ignored) {
            }

            final String imageUrl = image;

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    NotificationService service = serviceReference.get();
                    if (service != null) {
                        if (imageUrl != null) {
                            service.imageTarget = Glide.with(service).asBitmap().load(imageUrl).into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                    NotificationService service = serviceReference.get();
                                    if (service != null) {
                                        service.largeIcon = resource;
                                        service.updateNotification();
                                    }
                                }
                            });
                        } else service.updateNotification();
                    }
                }
            });
        }
    }
}
