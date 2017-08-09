MediaNotification is a slightly unstable attempt to create Android O styled media notifications. Please note that this app does not replace media notifications coming from other apps, but it creates new notifications on its own. The most common methods of use are as follows:
1. Leave the original music player notifications intact. The app will read its data and create a new notification with fully operational album art and media controls.
2. Block all notifications from the music players installed on your phone, and enable the switch at the bottom of the settings menu. The app will then obtain all its information from a BroadcastReceiver, and get the album art from either the external storage (if the song is being played locally) or from the last.fm api.
3. Somehow install the app on the system partition of your device, and grant it android.permission.UPDATE_APP_OPS_STATS. This is not possible to do from within the app as the UPDATE_APP_OPS_STATS is protected by the application signature. In other words, you will need to compile a new apk with the same signature as the rest of the system apps.

This app works properly for all the above options with the following music players:
- Spotify

These music players all work fine, apart from a few issues with player controls when used with the second option (above):
- Google Chrome (yes, the web browser)
- Google Play Music
- Phonograph
- PlayerPro
- Poweramp
- Soundcloud
