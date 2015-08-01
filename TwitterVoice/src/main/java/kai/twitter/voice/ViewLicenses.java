package kai.twitter.voice;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class ViewLicenses extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_licenses);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView headerView = (TextView) findViewById(R.id.licensesHeaderView);
        TextView mainView = (TextView) findViewById(R.id.licensesMainView);


        readText("notes.txt", headerView);
        readText("APACHE_LICENSE_2.txt", mainView);
    }

    private void readText(String filename, TextView headerView) {
        try {
            InputStream noteIn = getAssets().open(filename);
            int size = noteIn.available();
            byte[] buffer = new byte[size];
            noteIn.read(buffer);
            noteIn.close();
            String text = new String(buffer);
            headerView.setText(text);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("READ", e.getMessage());
        }
    }
}
