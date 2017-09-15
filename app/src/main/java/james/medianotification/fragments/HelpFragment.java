package james.medianotification.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import james.medianotification.R;
import james.medianotification.utils.MarkdownUtils;
import ru.noties.markwon.Markwon;

public class HelpFragment extends BaseFragment {

    private ProgressBar progressBar;
    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        textView = view.findViewById(R.id.textView);

        new ReadmeThread(this).start();

        return view;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.title_help);
    }

    private static class ReadmeThread extends Thread {

        private WeakReference<HelpFragment> fragmentReference;

        public ReadmeThread(HelpFragment fragment) {
            fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        public void run() {
            final String text;
            try {
                URL url = new URL("https://raw.githubusercontent.com/TheAndroidMaster/MediaNotification/master/README.md");
                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                request.connect();

                BufferedReader r = new BufferedReader(new InputStreamReader((InputStream) request.getContent()));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                }

                text = MarkdownUtils.toHtml(total.toString());
            } catch (final Exception e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        HelpFragment fragment = fragmentReference.get();
                        if (fragment != null && fragment.textView != null && fragment.progressBar != null) {
                            fragment.textView.setText(String.format(fragment.getString(R.string.msg_readme_error), e.getMessage()));
                            fragment.progressBar.setVisibility(View.GONE);
                        }
                    }
                });
                return;
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    HelpFragment fragment = fragmentReference.get();
                    if (fragment != null && fragment.textView != null && fragment.progressBar != null) {
                        Markwon.setMarkdown(fragment.textView, text);
                        fragment.progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}
