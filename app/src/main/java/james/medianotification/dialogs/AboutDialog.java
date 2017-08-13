package james.medianotification.dialogs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.view.View;
import android.widget.TextView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import james.medianotification.R;
import james.medianotification.adapters.ContributorAdapter;
import james.medianotification.data.ContributorData;

public class AboutDialog extends AppCompatDialog {

    private List<ContributorData> contributors;

    private TextView vukView;
    private RecyclerView contributorView;
    private View githubView;
    private View glideView;

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

        vukView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/105188221378780419527")));
            }
        });

        githubView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TheAndroidMaster/MediaNotification")));
            }
        });

        glideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bumptech/glide")));
            }
        });

        contributors = new ArrayList<>();

        contributorView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        contributorView.setNestedScrollingEnabled(false);
        contributorView.setAdapter(new ContributorAdapter(contributors));

        new Thread() {
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
                        contributorView.getAdapter().notifyDataSetChanged();
                        for (final ContributorData contributor : contributors) {
                            new Thread() {
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
                                            contributorView.getAdapter().notifyItemChanged(contributors.indexOf(contributor));
                                        }
                                    });
                                }
                            }.start();
                        }
                    }
                });
            }
        }.start();
    }
}
