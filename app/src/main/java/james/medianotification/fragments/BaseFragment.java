package james.medianotification.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import james.medianotification.MediaNotification;

public abstract class BaseFragment extends Fragment implements MediaNotification.TutorialListener {

    private MediaNotification mediaNotification;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaNotification = (MediaNotification) getContext().getApplicationContext();
        mediaNotification.addListener(this);
    }

    @Override
    public void onDestroy() {
        mediaNotification.removeListener(this);
        super.onDestroy();
    }

    public abstract String getTitle(Context context);

    public void onSelect() {

    }

    @Override
    public void onTutorial() {

    }
}
