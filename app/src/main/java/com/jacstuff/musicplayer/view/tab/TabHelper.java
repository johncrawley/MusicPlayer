package com.jacstuff.musicplayer.view.tab;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.jacstuff.musicplayer.view.viewmodel.MainViewModel;

public class TabHelper {

    private final MainViewModel viewModel;

    public TabHelper(MainViewModel viewModel){
        this.viewModel = viewModel;
    }

    public void setupTabLayout(TabLayout tabLayout, ViewPager2 pager){
        refreshSelectedTabWhenPagerSwiped(pager, tabLayout);
        setupTabSelectedListener(tabLayout, pager);
        setTabToIndex(tabLayout, viewModel.currentTabIndex);
    }


    private void refreshSelectedTabWhenPagerSwiped(ViewPager2 pager, TabLayout tabLayout){
        RecyclerView pagerRecyclerView = (RecyclerView) pager.getChildAt(0);
        pagerRecyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                if(velocityX > 0){
                    incrementCurrentlySelectedTab(tabLayout);
                }
                else{
                    decrementCurrentlySelectedTab(tabLayout);
                }
                return false;
            }
        });
    }


    private void setupTabSelectedListener(TabLayout tabLayout, ViewPager2 pager){
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
                viewModel.currentTabIndex = tab.getPosition();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }


    private void setTabToIndex(TabLayout tabLayout, int index){
        tabLayout.selectTab(tabLayout.getTabAt(index));
    }


    private void decrementCurrentlySelectedTab(TabLayout tabLayout){
        if(tabLayout.getSelectedTabPosition() > 0){
            viewModel.currentTabIndex = tabLayout.getSelectedTabPosition() - 1;
            setTabIndexToViewModel(tabLayout);
        }
    }


    private void incrementCurrentlySelectedTab(TabLayout tabLayout){
        if(tabLayout.getSelectedTabPosition() < tabLayout.getTabCount()-1){
            viewModel.currentTabIndex = tabLayout.getSelectedTabPosition() + 1;
            setTabIndexToViewModel(tabLayout);
        }
    }

    public void setTabIndexToViewModel(TabLayout tabLayout){
        tabLayout.selectTab(tabLayout.getTabAt(viewModel.currentTabIndex));
    }
}
