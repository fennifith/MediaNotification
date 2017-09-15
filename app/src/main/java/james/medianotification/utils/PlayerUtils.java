package james.medianotification.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import james.medianotification.R;
import james.medianotification.data.PlayerData;

public class PlayerUtils {

    public static List<PlayerData> getPlayers(Context context) {
        List<PlayerData> players = new ArrayList<>();

        players.add(new PlayerData(
                context.getString(R.string.app_name),
                null,
                "com.android.music.playstatechanged",
                "com.android.music.playstatechanged.togglepause",
                "com.android.music.playstatechanged.pause",
                "com.android.music.playstatechanged.previous",
                "com.android.music.playstatechanged.next",
                "com.android.music.metachanged",
                "com.android.music.statechanged"));

        players.add(new PlayerData(
                "Spotify",
                "com.spotify.music",
                PendingIntent.getBroadcast(context, 0, new Intent("com.spotify.mobile.android.ui.widget.PREVIOUS"), 0),
                PendingIntent.getBroadcast(context, 0, new Intent("com.spotify.mobile.android.ui.widget.PLAY"), 0),
                PendingIntent.getBroadcast(context, 0, new Intent("com.spotify.mobile.android.ui.widget.NEXT"), 0),
                "com.spotify.music.playbackstatechanged",
                "com.spotify.music.metadatachanged"
        ));

        players.add(new PlayerData(
                        "Phonograph",
                        "com.kabouzeid.gramophone",
                        "com.kabouzeid.gramophone.temp_sticky_intent_fix.metachanged",
                        "com.kabouzeid.gramophone.temp_sticky_intent_fix.queuechanged",
                        "com.kabouzeid.gramophone.temp_sticky_intent_fix.playstatechanged"
                )
                        .setPersistence(1)
        );

        players.add(new PlayerData(
                        "Phonograph",
                        "com.kabouzeid.gramophone",
                        "com.kabouzeid.gramophone.metachanged",
                        "com.kabouzeid.gramophone.queuechanged",
                        "com.kabouzeid.gramophone.playstatechanged"
                )
                        .setPersistence(1)
        );

        players.add(new PlayerData(
                "Timber",
                "naman14.timber",
                "com.naman14.timber.playstatechanged",
                "com.naman14.timber.metachanged"
        ));

        players.add(new PlayerData(
                "Jockey",
                "com.marverenic.music",
                "marverenic.jockey.player.REFRESH"
        ));

        players.add(new PlayerData(
                        "NewPipe",
                        "org.schabi.newpipe",
                        null,
                        PendingIntent.getBroadcast(context, 0, new Intent("org.schabi.newpipe.player.PopupVideoPlayer.PLAY_PAUSE"), 0),
                        null,
                        "org.schabi.newpipe.player.PopupVideoPlayer.CLOSE",
                        "org.schabi.newpipe.player.PopupVideoPlayer.PLAY_PAUSE",
                        "org.schabi.newpipe.player.PopupVideoPlayer.OPEN_DETAIL",
                        "org.schabi.newpipe.player.PopupVideoPlayer.REPEAT"
                )
                        .setClearData(true)
                        .setReversePlayPause(true)
        );

        players.add(new PlayerData(
                        "NewPipe",
                        "org.schabi.newpipe",
                        PendingIntent.getBroadcast(context, 0, new Intent("org.schabi.newpipe.player.BackgroundPlayer.ACTION_FAST_REWIND"), 0),
                        PendingIntent.getBroadcast(context, 0, new Intent("org.schabi.newpipe.player.BackgroundPlayer.PLAY_PAUSE"), 0),
                        PendingIntent.getBroadcast(context, 0, new Intent("org.schabi.newpipe.player.BackgroundPlayer.ACTION_FAST_FORWARD"), 0),
                        "org.schabi.newpipe.player.BackgroundPlayer.CLOSE",
                        "org.schabi.newpipe.player.BackgroundPlayer.PLAY_PAUSE",
                        "org.schabi.newpipe.player.BackgroundPlayer.OPEN_DETAIL",
                        "org.schabi.newpipe.player.BackgroundPlayer.REPEAT",
                        "org.schabi.newpipe.player.BackgroundPlayer.ACTION_FAST_REWIND",
                        "org.schabi.newpipe.player.BackgroundPlayer.ACTION_FAST_FORWARD"
                )
                        .setClearData(true)
                        .setReversePlayPause(true)
        );

        players.add(new PlayerData(
                "HTC Music",
                "com.htc.music",
                "com.htc.music.playstatechanged",
                "com.htc.music.playbackcomplete",
                "com.htc.music.metachanged"
        ));

        players.add(new PlayerData(
                null,
                "fm.last.android",
                "fm.last.android.playstatechanged",
                "fm.last.android.metachanged",
                "fm.last.android.playbackpaused",
                "fm.last.android.playbackcomplete"
        ));

        players.add(new PlayerData(
                null,
                null,
                "com.adam.aslfms.notify.playstatechanged"
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

        players.add(new PlayerData("Netease Cloudmusic", "com.netease.cloudmusic"));

        return players;
    }

}
