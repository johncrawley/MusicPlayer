package com.jacstuff.musicplayer.fragments.albumart;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.DialogFragment;
import com.jacstuff.musicplayer.MainActivity;
import com.jacstuff.musicplayer.R;
import com.jacstuff.musicplayer.view.art.AlbumArtHelper;

public class AlbumArtDialog extends DialogFragment {

    public static final  String BUNDLE_KEY_BITMAP_UPDATE = "bundle_key_bitmap_update";
    public static final String SEND_BITMAP_TO_ALBUM_ART_FRAGMENT = "send_bitmap_to_album_art_fragment";
    private boolean isAlbumArtHelperNull;

    public static AlbumArtDialog newInstance(){
        return new AlbumArtDialog();
    }

    public AlbumArtDialog() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_art, container, false);
        MainActivity mainActivity = getMainActivity();
        if(mainActivity == null){
            return view;
        }
        assignAlbumArt(mainActivity, view);
        return view;
    }


    @Override
    public void onViewCreated(View view,  Bundle savedInstanceState){
        setupFragmentListener();
        if(isAlbumArtHelperNull){
            dismiss();
        }
    }


    private void assignAlbumArt(MainActivity mainActivity, View parentView){
        ImageView albumArtLargeImageView = parentView.findViewById(R.id.albumArtLargeImageView);
        AlbumArtHelper albumArtHelper = mainActivity.getAlbumArtHelper();
        if(albumArtHelper == null){
            isAlbumArtHelperNull = true;
        }
        else{
            albumArtHelper.changeAlbumArtToCurrent(albumArtLargeImageView);
            albumArtLargeImageView.setVisibility(View.VISIBLE);
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private void setupFragmentListener(){
        getParentFragmentManager().setFragmentResultListener(SEND_BITMAP_TO_ALBUM_ART_FRAGMENT, this, (requestKey, bundle) -> {
         //   int visibility =  isBundleUserPlaylistLoaded(bundle) && isItemSelected()? View.VISIBLE : View.INVISIBLE;
        });

    }


    private MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }

}