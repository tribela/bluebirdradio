package kai.twitter.voice;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.app.ActionBar;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.xml.prefs);
    }
}
