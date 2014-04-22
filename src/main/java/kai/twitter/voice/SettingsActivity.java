package kai.twitter.voice;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionbar = getActionBar();
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        preference.registerOnSharedPreferenceChangeListener(this);

        addPreferencesFromResource(R.xml.prefs);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        Intent intent = new Intent();
        intent.putExtra("KEY", key);
        intent.setAction(getResources().getString(R.string.ACTION_CHANGE_PREFERENCE));
        sendBroadcast(intent);
    }
}
