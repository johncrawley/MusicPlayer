package com.jacstuff.musicplayer.fragments;


import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


public abstract class PersistentPagerAdapter<T extends Fragment> extends FragmentPagerAdapter {
    private SparseArray<T> registeredFragments = new SparseArray<T>();

    public PersistentPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public T instantiateItem(ViewGroup container, int position) {
        T fragment = (T)super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public T getRegisteredFragment(ViewGroup container, int position) {
        T existingInstance = registeredFragments.get(position);
        if (existingInstance != null) {
            return existingInstance;
        } else {
            return instantiateItem(container, position);
        }
    }
}