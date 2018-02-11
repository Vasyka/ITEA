package com.productions.itea.motivatedev;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 09.02.2018.
 */
class ScreenSlidePagerAdapter extends FragmentPagerAdapter {


    private final List<Fragment> lstFragment = new ArrayList<>();
    private final List<String> lstTitles = new ArrayList<>();


    ScreenSlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        return lstFragment.get(position);

    }


    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return lstTitles.get(position);
    }

    @Override
    public int getCount() {

        return lstTitles.size();
    }


    public void AddFragment(Fragment fragment, String title) {

        lstFragment.add(fragment);
        lstTitles.add(title);

    }

}
