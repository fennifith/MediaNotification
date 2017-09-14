package james.medianotification.adapters;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import james.medianotification.fragments.BaseFragment;

public class SimplePagerAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private BaseFragment[] fragments;

    public SimplePagerAdapter(Context context, FragmentManager fm, BaseFragment... fragments) {
        super(fm);
        this.context = context;
        this.fragments = fragments;
    }

    @Override
    public BaseFragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragments[position].getTitle(context);
    }

    @Override
    public int getCount() {
        return fragments.length;
    }
}
