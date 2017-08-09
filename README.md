MediaNotification is a slightly unstable attempt to create Android O styled media notifications. Please note that this app does not replace media notifications coming from other apps, but it creates new notifications on its own. The most common methods of use are as follows:

OPTION ONE:
Leave the original music player notifications intact. The app will read its data and create a new notification with fully operational album art and media controls. There should not be any issues using the app this way, provided that the 'Use Broadcast Receiver' switch is disabled. This has been tested on the following music players:
  - Google Chrome (yes, the web browser)
  - Google Play Music
  - Phonograph
  - PlayerPro
  - Poweramp
  - Soundcloud
  - Spotify

OPTION TWO:
Block all notifications from the music players installed on your phone, and enable the switch at the bottom of the settings menu. The app will then obtain all its information from a BroadcastReceiver, and get the album art from either the external storage (if the song is being played locally) or from the last.fm api. When used with this option, the only music player for which the notification still functions completely is Spotify. Most other apps will have problems with the player controls and content intent (when the notification is clicked on). However, there are some apps for which the content intents still function properly:
  - Phonograph

Optionally, you can set a 'Default Music Player' for the notification to open if it cannot obtain a content intent any other way.

OPTION THREE:
Somehow install the app on the system partition of your device, and grant it android.permission.UPDATE_APP_OPS_STATS. This is not possible to do from within the app as the UPDATE_APP_OPS_STATS is protected by the application signature. In other words, you will need to compile a new apk with the same signature as the rest of the system apps.

