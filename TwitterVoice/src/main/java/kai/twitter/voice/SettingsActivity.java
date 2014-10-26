package kai.twitter.voice;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;

public class SettingsActivity extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        preference.registerOnSharedPreferenceChangeListener(this);

        getFragmentManager()
        .beginTransaction()
        .replace(android.R.id.content, new SettingsFragment())
        .commit();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        Intent intent = new Intent();
        intent.putExtra("KEY", key);
        intent.setAction(getResources().getString(R.string.ACTION_CHANGE_PREFERENCE));
        sendBroadcast(intent);
    }

    private class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.prefs);
        }
    }
}
