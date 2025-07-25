package com.jacstuff.musicplayer.view.tab;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.view.viewmodel.MainViewModel;

public class TabHelper {

    private final MainViewModel viewModel;
    private final TabLayout tabLayout;
    private SharedPreferences sharedPreferences;
    private TabsViewStateAdapter tabsViewStateAdapter;

    public TabHelper(MainViewModel viewModel, AppCompatActivity activity){
        this.viewModel = viewModel;
        tabLayout = activity.findViewById(R.id.tabLayout);
        ViewPager2 tabViewPager = activity.findViewById(R.id.pager);
        if(tabLayout == null){
            return;
        }
        tabsViewStateAdapter = new TabsViewStateAdapter(activity.getSupportFragmentManager(), activity.getLifecycle());
        tabViewPager.setAdapter(tabsViewStateAdapter);
        setupTabLayout(tabViewPager);
        sharedPreferences = getDefaultSharedPreferences(activity);
    }


    public void onDestroy(){
        if(tabsViewStateAdapter != null) {
            tabsViewStateAdapter = null;
        }
    }


    public void switchToTracksTab(){
      switchToTab(0);
    }


    public void switchToAlbumsTab(){
       switchToTab(3);
    }


    private void switchToTab(int index){
        if(sharedPreferences.getBoolean("autoSwitchTabsAfterPlaylistSelection", true)){
            TabLayout.Tab tab = tabLayout.getTabAt(index);
            if(tab != null){
                new Handler(Looper.getMainLooper()).postDelayed(tab::select, 200);
            }
        }
    }


    private void setupTabLayout(ViewPager2 pager){
        refreshSelectedTabWhenPagerSwiped(pager, tabLayout);
        setupTabSelectedListener(tabLayout, pager);
        initTabSelection(tabLayout);
    }


    private void initTabSelection(TabLayout tabLayout){
        if(viewModel.hasFourthTabBeenInitialized){
            selectTabAtIndex(tabLayout, viewModel.currentTabIndex);
            return;
        }
        viewModel.hasFourthTabBeenInitialized = true;
        //moveToTheLastTabAndBack(tabLayout);
    }


    /*
        For reasons unknown, moving to the 4th tab (out of 5)
         causes the 5th fragment (genre) to load instead of the 4th (albums).
         It only happens on the first click, if no other tabs have first been selected.
         This workaround moves to the last tab, and then back to the first when app starts up.
     */
    private void moveToTheLastTabAndBack(TabLayout tabLayout){
        int previousIndex = viewModel.currentTabIndex;
        selectTabAtIndex(tabLayout, 4);
        new Handler(Looper.getMainLooper())
                .postDelayed(()-> selectTabAtIndex(tabLayout, previousIndex),
                        300);
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


    private void selectTabAtIndex(TabLayout tabLayout, int index){
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


    private void setTabIndexToViewModel(TabLayout tabLayout){
        tabLayout.selectTab(tabLayout.getTabAt(viewModel.currentTabIndex));
    }
}
