package com.jacstuff.musicplayer;

import android.Manifest;
import android.annotation.TargetApi;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jacstuff.musicplayer.list.TrackListAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayerView{

    private Context context;
    private MediaController mediaController;
    private TextView trackTitle, trackAlbum, trackArtist;
    private ImageButton playButton;
    private ImageButton nextTrackButton;
    private String totalTrackTime = "0:00";
    private RecyclerView recyclerView;
    private TrackListAdapter trackListAdapter;
    private int previousIndex = 0;


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        mediaController = new MediaControllerImpl(context, this);
        setupViews();
        mediaController.initPlaylistAndRefreshView();
        //listAudioFiles();
    }


    public void onDestroy(){
        mediaController.finish();
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.refresh_button, menu);
        return true;
    }


    public void notifyCurrentlySelectedTrack(int position){
        mediaController.selectTrack(position);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.refresh_button) {
            mediaController.scanForTracks();
        }
        return super.onOptionsItemSelected(item);
    }


    public void setCoverImage(Bitmap bitmap){
        ImageView coverArt = findViewById(R.id.coverArtImageView);
        coverArt.setImageBitmap(bitmap);
    }


    public void setElapsedTime(String elapsedTime){
        this.setTrackTime(elapsedTime);
    }


    public void setTotalTrackTime(String totalTrackTime){
        this.totalTrackTime = totalTrackTime;
        setTrackTime("0:00");
    }


    private void setTrackTime(String elapsedTime){
        TextView trackTime = findViewById(R.id.trackTime);
        if(trackTime == null){
            return;
        }
        String time = elapsedTime + " / " + this.totalTrackTime;
        trackTime.setText(time);
    }


    public void displayPlaylistRefreshedMessage(){
        String msg = getResources().getString(R.string.playlist_refreshed_message);
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


    public void displayPlaylistRefreshedMessage(int newTrackCount) {
        new Handler(Looper.getMainLooper()).post(() -> displayPlaylistMessage(newTrackCount));
    }


    public void displayPlaylistMessage(int newTrackCount) {
        if(newTrackCount == 0){
            displayPlaylistRefreshedMessage();
            return;
        }
        String msg = newTrackCount > 1 ?
                getResources().getString(R.string.playlist_refreshed_message_new_tracks_count, newTrackCount)
                : getResources().getString(R.string.playlist_refreshed_one_new_track);
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


    private void setupViews(){
        trackTitle = findViewById(R.id.trackTitle);
        trackAlbum = findViewById(R.id.albumTextView);
        trackArtist =findViewById(R.id.artistTextView);
        playButton = findViewById(R.id.playButton);
        nextTrackButton = findViewById(R.id.nextTrackButton);
        ImageButton refreshPlaylistButton = findViewById(R.id.refreshButton);
        setupRecyclerView(mediaController.getTrackDetailsList());

        trackTitle.setOnClickListener(this);
        playButton.setOnClickListener(this);
        nextTrackButton.setOnClickListener(this);
        refreshPlaylistButton.setOnClickListener(this);

        String elapsedTime = "0:00";
        setElapsedTime(elapsedTime);

        playButton.setEnabled(false);
        nextTrackButton.setEnabled(false);
    }


    public void refreshTrackList(List<Track> trackDetailsList){
        setupRecyclerView(trackDetailsList);
    }


    private void setupRecyclerView(List<Track> trackDetailsList){
        recyclerView = findViewById(R.id.recyclerView);
        trackListAdapter = new TrackListAdapter(trackDetailsList, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(trackListAdapter);
    }


    public void scrollToListPosition(int index){
        trackListAdapter.deselectCurrentlySelectedItem();
        trackListAdapter.setIndexToScrollTo(index);
        recyclerView.scrollToPosition(calculateIndexWithOffset(index));
    }


    private int calculateIndexWithOffset(int index){
        int indexWithOffset = getPlaylistItemOffset(index);
        if ( indexWithOffset > mediaController.getNumberOfTracks() || indexWithOffset < 0) {
            indexWithOffset = index;
        }
        previousIndex = index;
        return indexWithOffset;
    }


    private int getPlaylistItemOffset(int index){
        int direction = index > previousIndex ? 1 : -1;
        int offset =  getResources().getInteger(R.integer.playlist_item_offset) * direction ;
        return index + offset;
    }


    public void enableControls(){
        playButton.setEnabled(true);
        nextTrackButton.setEnabled(true);
    }


    public void onClick(View view){
        int id = view.getId();
        switch(id){
            case R.id.playButton:
                mediaController.togglePlay();
                break;

            case R.id.nextTrackButton:
                mediaController.next();
                break;

            case R.id.refreshButton:
                mediaController.scanForTracks();
        }
    }


    public void showPauseIcon(){
        playButton.setImageResource(android.R.drawable.ic_media_pause);
    }


    public void showPlayIcon(){
        playButton.setImageResource(android.R.drawable.ic_media_play);
    }


    public void setTrackInfo(String trackInfo){
        if(trackInfo.isEmpty()){
            trackInfo = getResources().getString(R.string.no_tracks_found);
        }
        this.trackTitle.setText(trackInfo);
    }


    public void setAlbumInfo(String albumInfo){
        this.trackAlbum.setText(albumInfo);
    }


    public void setArtistInfo(String artistInfo){
        this.trackArtist.setText(artistInfo);
    }

}
