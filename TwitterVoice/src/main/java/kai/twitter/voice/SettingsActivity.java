package kai.twitter.voice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

public class SettingsActivity extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        preference.registerOnSharedPreferenceChangeListener(this);

        getFragmentManager()
        .beginTransaction()
                .replace(R.id.preference, new SettingsFragment())
        .commit();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        Intent intent = new Intent();
        intent.putExtra("KEY", key);
        intent.setAction(getResources().getString(R.string.ACTION_CHANGE_PREFERENCE));
        sendBroadcast(intent);
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.prefs);
        }
    }
}
