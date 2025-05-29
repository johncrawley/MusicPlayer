package com.jacstuff.musicplayer;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.jacstuff.musicplayer.settings.MinNumberInputFilter;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            setupMinNumberOfTracksForMainArtistPreference();
            setupNumberOfRandomTracksToAddPreference();
            setupTracksPathnamePreference();
        }


        private void setupMinNumberOfTracksForMainArtistPreference(){
            var editTextPreference = findPref("minimumNumberOfTracksForMainArtist");
            editTextPreference.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                int maxLengthOfInput = 2;
                editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLengthOfInput)});
            });
        }


        private void setupNumberOfRandomTracksToAddPreference(){
            var pref = findPref("numberOfRandomTracksToAdd");
            pref.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                int maxLengthOfInput = 3;
                editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLengthOfInput)
                    , new MinNumberInputFilter(1, 100)
                });
            });
        }


        @NonNull
        private androidx.preference.EditTextPreference findPref(String key){
            EditTextPreference pref = getPreferenceManager().findPreference(key);
            assert pref != null;
            return pref;
        }


        private void setupTracksPathnamePreference(){
            androidx.preference.EditTextPreference editTextPreference = getPreferenceManager().findPreference("tracksPathnameString_1");
            assert editTextPreference != null;
            editTextPreference.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                int maxLengthOfInput = 25;
                editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLengthOfInput)});
            });
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}