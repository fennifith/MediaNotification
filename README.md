MediaNotification is a slightly unstable attempt to create Android O styled media notifications. Please note that this app does not replace media notifications coming from other apps, but it creates new notifications on its own. The most common methods of use are as follows:

OPTION ONE:
Leave the original music player notifications intact. The app will read its data and create a new notification with fully operational album art and media controls. There should not be any issues using the app this way, provided that the 'Use Broadcast Receiver' switch is disabled. This has been tested and is working properly on the following music players:
  - Bandcamp
  - BlackPlayer
  - Google Chrome (yes, the web browser)
  - Google Play Music
  - Jair Player
  - Phonograph
  - PlayerPro
  - Pulsar
  - Poweramp
  - Shuttle
  - Soundcloud
  - Spotify

OPTION TWO:
Block all notifications from the music players installed on your phone, and enable the switch at the bottom of the settings menu. The app will then obtain all its information from a BroadcastReceiver, and get the album art from either the external storage (if the song is being played locally) or from the last.fm api. When used with this option, the only music player for which the notification still functions completely is Spotify. Most other apps will have problems with the player controls (player controls for unsupported apps are disabled by default, check the 'Media Controls Method' setting) and content intents (when the notification is clicked on). However, there are a few apps for which the content intents still function properly:
  - BlackPlayer
  - Phonograph
  - Timber
  - Jockey
  - Jair Player
  - Pulsar
  - NewPipe

As a sort of workaround, it is possible to set a 'Default Music Player' for the notification to open if it cannot obtain a content intent any other way.

IMPORTANT: The following music players require their settings to be modified in order to work properly:
  - Shuttle: Turn on last.fm scrobbling (you don't need to download a scrobbler).
  - AIMP: The 'integrate to lock screen' setting must be enabled
  - Jair Player: 'Enable Scrobbling' at the bottom of the settings menu. You will need to install the scrobbler app in order for this to work (uninstalling it will disable the setting).
  - NewPipe: support for this player does exist, but is very limited. It is reccomended to turn on 'Use external audio player' in NewPipe's settings menu instead.
  - BlackPlayer: Turn on the 'Scrobble Music' setting (at the bottom of the 'Metadata' section). You do not need to have a scrobbler installed.

OPTION THREE:
Somehow install the app on the system partition of your device, and grant it android.permission.UPDATE_APP_OPS_STATS. This is not possible to do from within the app as the UPDATE_APP_OPS_STATS is protected by the application signature. In other words, you will need to compile a new apk with the same signature as the rest of the system apps.

---

Unfortunately, this app does not function at all when used with the following music players. This is simply because they either do not use the MediaPlayer API, or they do use it and have prevented other apps from intercepting any information from it.
  - iHeartRadio
  - Rocket Player
