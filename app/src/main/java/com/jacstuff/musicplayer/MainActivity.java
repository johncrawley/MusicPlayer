package com.jacstuff.musicplayer;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
    private TrackListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        mediaController = new MediaControllerImpl(context, this);
        setupViews();
        mediaController.initPlaylistAndRefreshView();
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
        Log.i("MainActivity", "On options item Selected: id = " + id);
        if(id == R.id.refresh_button) {
            Log.i("MainActivity", "id is the refresh button");
            mediaController.refreshPlaylist();
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


    public void refreshTrackList(List<TrackDetails> trackDetailsList){

        Log.i("MainActivity", "refreshing track list");

        setupRecyclerView(trackDetailsList);
    }

    private void setupRecyclerView(List<TrackDetails> trackDetailsList){
        recyclerView = findViewById(R.id.recyclerView);
        mAdapter = new TrackListAdapter(trackDetailsList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        /*
        mAdapter.setOnItemClickListener(new TrackListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.i("MainActivity", "setupRecyclerView trackListAdapter onItemClicked() !!!");
                Toast.makeText(MainActivity.this, mediaController.getTrackNameAt(position) + " was clicked!", Toast.LENGTH_SHORT).show();
            }
        });
*/
        recyclerView.setAdapter(mAdapter);
    }

    private int previousIndex = 0;

    public void scrollToListPosition(int index){
        Log.i("MainActivity", "scrollToListPosition: scrolling to position: "+  index);
        mAdapter.deselectCurrentlySelectedItem();
        mAdapter.setIndexToScrollTo(index);

        recyclerView.scrollToPosition(calculateIndexWithOffset(index));

    }

    private int getOffset(){
        return Integer.valueOf(getString(R.string.playlist_item_offset));
    }

    private int calculateIndexWithOffset(int index){
        int direction = index > previousIndex ? 1 : -1;
        int offset = getOffset() * direction;
        int indexWithOffset = index + offset;
        if ( indexWithOffset > mediaController.getNumberOfTracks() || indexWithOffset < 0) {
            indexWithOffset = index;
        }
        previousIndex = index;
        return indexWithOffset;
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
                mediaController.refreshPlaylist();
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
