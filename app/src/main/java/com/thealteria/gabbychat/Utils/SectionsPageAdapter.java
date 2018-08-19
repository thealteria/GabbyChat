package com.thealteria.gabbychat.Utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.thealteria.gabbychat.Fragments.ChatFragment;
import com.thealteria.gabbychat.Fragments.FriendsFragment;
import com.thealteria.gabbychat.Fragments.RequestFragment;

public class SectionsPageAdapter extends FragmentPagerAdapter {


    public SectionsPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return new RequestFragment();

            case 1:
                return new ChatFragment();

            case 2:
                return new FriendsFragment();

            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 3;
    }

}
