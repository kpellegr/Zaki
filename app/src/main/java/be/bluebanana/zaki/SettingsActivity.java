package be.bluebanana.zaki;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

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
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @SuppressWarnings("SpellCheckingInspection")
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            Context context = getPreferenceManager().getContext();
            PreferenceScreen screen = getPreferenceManager().getPreferenceScreen();
            ListPreference songPreference = screen.findPreference("song_selection_preference");
            if (songPreference == null) return;

            String[] songs = {"Elevator music", "Weird vibey stuff", "Jazzy"};
            String[] values = {"muzak_1", "muzak_2", "muzak_3" };

            songPreference.setEntries(songs);
            songPreference.setEntryValues(values);
        }


    }
}