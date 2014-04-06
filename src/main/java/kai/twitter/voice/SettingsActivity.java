package kai.twitter.voice;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.app.ActionBar;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionbar = getActionBar();
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        addPreferencesFromResource(R.xml.prefs);
    }
}
