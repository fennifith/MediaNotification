package james.medianotification.adapters;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import james.medianotification.fragments.BaseFragment;

public class SimplePagerAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {

    private Context context;
    private BaseFragment[] fragments;

    public SimplePagerAdapter(Context context, ViewPager viewPager, FragmentManager fm, BaseFragment... fragments) {
        super(fm);
        this.context = context;
        this.fragments = fragments;
        viewPager.addOnPageChangeListener(this);
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        fragments[position].onSelect();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
