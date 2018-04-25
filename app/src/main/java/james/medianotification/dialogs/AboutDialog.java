package james.medianotification.dialogs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.view.View;
import android.widget.TextView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import james.medianotification.BuildConfig;
import james.medianotification.R;
import james.medianotification.adapters.ContributorAdapter;
import james.medianotification.data.ContributorData;
import james.medianotification.utils.PreferenceUtils;

public class AboutDialog extends AppCompatDialog {

    private TextView vukView;
    private RecyclerView contributorView;
    private View githubView;
    private View glideView;
    private View markwonView;

    private SharedPreferences prefs;
    private List<ContributorData> contributors;

    public AboutDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_about);

        vukView = findViewById(R.id.vuk);
        contributorView = findViewById(R.id.contributors);
        githubView = findViewById(R.id.github);
        glideView = findViewById(R.id.glide);
        markwonView = findViewById(R.id.markwon);

        vukView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/105188221378780419527")));
            }
        });

        githubView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://jfenn.me/redirects/?t=github&d=MediaNotification")));
            }
        });

        glideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bumptech/glide")));
            }
        });

        markwonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/noties/Markwon")));
            }
        });

        contributors = new ArrayList<>();

        contributorView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        contributorView.setNestedScrollingEnabled(false);
        contributorView.setAdapter(new ContributorAdapter(contributors));

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getInt(PreferenceUtils.PREF_CONTRIBUTOR_VERSION, 0) == BuildConfig.VERSION_CODE) {
            for (int i = 0; i < prefs.getInt(PreferenceUtils.PREF_CONTRIBUTOR_LENGTH, 0); i++) {
                contributors.add(new ContributorData(
                        prefs.getString(String.format(Locale.getDefault(), PreferenceUtils.PREF_CONTRIBUTORS, PreferenceUtils.PREF_CONTRIBUTOR_NAME, i), null),
                        prefs.getString(String.format(Locale.getDefault(), PreferenceUtils.PREF_CONTRIBUTORS, PreferenceUtils.PREF_CONTRIBUTOR_IMAGE, i), null),
                        prefs.getString(String.format(Locale.getDefault(), PreferenceUtils.PREF_CONTRIBUTORS, PreferenceUtils.PREF_CONTRIBUTOR_URL, i), null)
                ));
            }
        } else new ContributorsThread(this, contributors).start();
    }

    private static class ContributorsThread extends Thread {

        private WeakReference<AboutDialog> dialogReference;
        private List<ContributorData> contributors;

        public ContributorsThread(AboutDialog dialog, List<ContributorData> contributors) {
            dialogReference = new WeakReference<>(dialog);
            this.contributors = contributors;
        }

        @Override
        public void run() {
            try {
                HttpURLConnection request = (HttpURLConnection) new URL("https://api.github.com/repos/TheAndroidMaster/MediaNotification/contributors").openConnection();
                request.connect();

                JsonReader reader = new JsonReader(new InputStreamReader((InputStream) request.getContent()));
                reader.setLenient(true);
                reader.beginArray();
                reader.skipValue();
                while (reader.hasNext()) {
                    reader.beginObject();
                    String name = null, imageUrl = null, url = null;
                    while (reader.hasNext()) {
                        switch (reader.nextName()) {
                            case "login":
                                name = reader.nextString();
                                break;
                            case "avatar_url":
                                imageUrl = reader.nextString();
                                break;
                            case "html_url":
                                url = reader.nextString();
                                break;
                            default:
                                reader.skipValue();
                        }
                    }
                    contributors.add(new ContributorData(name, imageUrl, url));
                    reader.endObject();
                }
                reader.endArray();
            } catch (Exception ignored) {
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    AboutDialog dialog = dialogReference.get();
                    if (dialog != null) {
                        dialog.contributorView.getAdapter().notifyDataSetChanged();
                        for (final ContributorData contributor : contributors) {
                            new ContributorThread(dialog, contributor).start();
                        }
                    }
                }
            });
        }
    }

    private static class ContributorThread extends Thread {

        private WeakReference<AboutDialog> dialogReference;
        private ContributorData contributor;

        public ContributorThread(AboutDialog dialog, ContributorData contributor) {
            dialogReference = new WeakReference<>(dialog);
            this.contributor = contributor;
        }

        @Override
        public void run() {
            try {
                HttpURLConnection request = (HttpURLConnection) new URL("https://api.github.com/users/" + contributor.name).openConnection();
                request.connect();

                JsonReader reader = new JsonReader(new InputStreamReader((InputStream) request.getContent()));
                reader.setLenient(true);
                reader.beginObject();
                while (reader.hasNext()) {
                    switch (reader.nextName()) {
                        case "name":
                            String name = reader.nextString();
                            if (name != null)
                                contributor.name = name;
                            break;
                        case "blog":
                            String blog = reader.nextString();
                            if (blog != null)
                                contributor.url = blog;
                            break;
                        default:
                            reader.skipValue();
                    }
                }
                reader.endObject();
            } catch (Exception ignored) {
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    AboutDialog dialog = dialogReference.get();
                    if (dialog != null) {
                        int index = dialog.contributors.indexOf(contributor);
                        dialog.contributorView.getAdapter().notifyItemChanged(index);

                        dialog.prefs.edit()
                                .putString(String.format(Locale.getDefault(), PreferenceUtils.PREF_CONTRIBUTORS, PreferenceUtils.PREF_CONTRIBUTOR_NAME, index), contributor.name)
                                .putString(String.format(Locale.getDefault(), PreferenceUtils.PREF_CONTRIBUTORS, PreferenceUtils.PREF_CONTRIBUTOR_IMAGE, index), contributor.imageUrl)
                                .putString(String.format(Locale.getDefault(), PreferenceUtils.PREF_CONTRIBUTORS, PreferenceUtils.PREF_CONTRIBUTOR_URL, index), contributor.url)
                                .putInt(PreferenceUtils.PREF_CONTRIBUTOR_LENGTH, Math.max(dialog.prefs.getInt(PreferenceUtils.PREF_CONTRIBUTOR_LENGTH, 0), index + 1))
                                .putInt(PreferenceUtils.PREF_CONTRIBUTOR_VERSION, BuildConfig.VERSION_CODE)
                                .apply();
                    }
                }
            });
        }
    }
}
