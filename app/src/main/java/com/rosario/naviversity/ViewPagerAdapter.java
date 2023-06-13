package com.rosario.naviversity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0: return new SearchRideFragment();
            case 1: return new CreateRideFragment();
            default: return new SearchRideFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
