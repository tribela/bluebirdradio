package kai.twitter.voice;

import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;

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
