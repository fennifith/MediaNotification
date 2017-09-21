package james.medianotification;

import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import james.colorpickerdialog.ColorPicker;
import james.medianotification.utils.PreferenceUtils;

public class MediaNotification extends ColorPicker {

    private List<TutorialListener> listeners;

    @Override
    public void onCreate() {
        super.onCreate();
        listeners = new ArrayList<>();
    }

    public void showTutorial() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(PreferenceUtils.PREF_TUTORIAL, true)
                .putBoolean(PreferenceUtils.PREF_TUTORIAL_PLAYERS, true)
                .apply();

        for (TutorialListener listener : listeners) {
            listener.onTutorial();
        }
    }

    public void addListener(TutorialListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TutorialListener listener) {
        listeners.remove(listener);
    }

    public interface TutorialListener {
        void onTutorial();
    }

}
