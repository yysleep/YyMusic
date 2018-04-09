package com.yy.sleep.music.tool;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.List;

/**
 * Created by archermind on 17-6-9.
 * @author yysleep
 */
public class TapPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragments;
    private boolean mIsNeedTitle;

    public TapPagerAdapter(FragmentManager fm, List<Fragment> fragments, boolean isNeedTitle) {
        super(fm);
        mFragments = fragments;
        mIsNeedTitle = isNeedTitle;

    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = mFragments.get(0);
                break;
            case 1:
                fragment = mFragments.get(1);
                break;
            case 2:
                fragment = mFragments.get(2);
                break;
            default:
                fragment = mFragments.get(0);
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (!mIsNeedTitle)
            return null;

        String title;
        switch (position) {
            case 0:
                title = "本地";
                break;
            case 1:
                title = "收藏";
                break;
            case 2:
                title = "发现";
                break;
            default:
                title = "默认";
                break;
        }
        return title;
    }
}
