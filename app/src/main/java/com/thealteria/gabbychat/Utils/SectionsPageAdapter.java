package com.thealteria.gabbychat.Utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import Fragments.ChatFragment;
import Fragments.FriendsFragment;
import Fragments.RequestFragment;

public class SectionsPageAdapter extends FragmentPagerAdapter {


    public SectionsPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

            case 1:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;

            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 3;
    }

}
