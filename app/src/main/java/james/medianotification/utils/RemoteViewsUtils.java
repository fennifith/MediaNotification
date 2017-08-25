package james.medianotification.utils;

import android.app.Notification;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RemoteViewsUtils {

    public static final String NETEASE_CLOUDMUSIC_PACKAGE_NAME = "com.netease.cloudmusic";
    public static final String NETEASE_CLOUDMUSIC_PAUSE_ICON_ID = "2130838663";
    public static final String NETEASE_CLOUDMUSIC_PLAY_ICON_ID = "2130838665";
    public static final int NETEASE_CLOUDMUSIC_NOTIFICATION_LAYOUT_ID = 2130903535;

    /**
     * Find the name, artists and album name of current music from Netease cloudmusic app
     * (Package name: com.netease.cloudmusic)
     *
     * @param notification Netease cloudmusic notification
     * @return data strings
     */
    public static @Nullable List<String> findNeteaseMusicCurrentStates(Notification notification) {
        // We have to extract the information from the view
        RemoteViews views = notification.bigContentView;
        if (views == null) views = notification.contentView;
        if (views == null) return null;

        // Use reflection to examine the m_actions member of the given RemoteViews object.
        // It's not pretty, but it works.
        List<String> text = new ArrayList<>();
        try {
            Field field = views.getClass().getDeclaredField("mActions");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field.get(views);

            // Find the setText() reflection actions
            for (Parcelable p : actions) {
                Parcel parcel = Parcel.obtain();
                p.writeToParcel(parcel, 0);
                parcel.setDataPosition(0);

                // The tag tells which type of action it is (2 is ReflectionAction, from the source)
                int tag = parcel.readInt();
                if (tag != 2) continue;

                // View ID
                parcel.readInt();

                String methodName = parcel.readString();
                Log.i("TAG", methodName);
                if ("setText".equals(methodName)) {
                    // Parameter type (10 = Character Sequence)
                    parcel.readInt();

                    // Store the actual string
                    String t = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel).toString().trim();
                    text.add(t);
                } else if ("setImageResource".equals(methodName)) {
                    parcel.readInt();

                    text.add(String.valueOf(parcel.readInt()));
                }

                parcel.recycle();
            }
        } catch (Exception e) {
            Log.e("NotificationClassifier", e.toString());
        }

        return text;
    }

}
