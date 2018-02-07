package com.example.root.motivationapp;

import android.content.Intent;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class PagesActivity extends FragmentActivity
        implements
        MyTasksFragment.OnMyTasksFragmentInteractionListener,
        MyGroupsFragment.OnMyGroupsFragmentInteractionListener {

    private static final int NUM_PAGES = 4;

    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    ListView tasksList;

    public static final ArrayList<String> tasks = new ArrayList<String>();

    ArrayAdapter<String> tasksAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pages);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        tasksList = (ListView) findViewById(R.id.taskList);

        tasksAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, tasks);

        tasksList.setAdapter(tasksAdapter);

    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    @Override
    public void onBtnPressed() {
        Intent intent = new Intent(PagesActivity.this, TaskEditingActivity.class);
        intent.putExtra("taskName", "default");
        startActivity(intent);
    }
    @Override
    protected void onResume(){
        super.onResume();
        tasksAdapter.notifyDataSetChanged();
    }
    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position){
                case 0: return new MyTasksFragment();
                case 1: return new MyGroupsFragment();
                case 2: return new TrophiesFragment();
                case 3: return new SolvedTasksFragment();
                default: return new MyTasksFragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position){
                case 0: return "Мой профиль";
                case 1: return "Мои группы";
                case 2: return "Мои награды";
                case 3: return "Выполненные задания";
                default: return "0";
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}
