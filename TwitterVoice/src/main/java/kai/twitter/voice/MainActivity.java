package kai.twitter.voice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import kai.twitter.voice.manageAccount.ManageAccountsActivity;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    private CompoundButton startServiceToggle;
    private DbAdapter adapter;
    private AdView adView;
    private ServiceReceiver serviceReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new DbAdapter(getApplicationContext());

        startServiceToggle = (CompoundButton) findViewById(R.id.switch_start_service);
        startServiceToggle.setOnClickListener(this);

        serviceReceiver = new ServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(getResources().getString(R.string.ACTION_SERVICE_TOGGLE));
        registerReceiver(serviceReceiver, filter);

        //Create an ad.
        adView = (AdView) findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        adView.loadAd(adRequest);

    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startServiceToggle.setChecked(TwitterVoiceService.isRunning());
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(serviceReceiver);

        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (R.id.action_settings == item.getItemId()) {
            Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(settings);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (startServiceToggle.getId() == view.getId()) {
            Intent serviceIntent = new Intent(getApplicationContext(), TwitterVoiceService.class);
            if (startServiceToggle.isChecked()) {
                if (adapter.getAccounts().isEmpty()) {
                    Intent accountManageIntent = new Intent(getApplicationContext(), ManageAccountsActivity.class);
                    startActivity(accountManageIntent);
                } else {
                    startService(serviceIntent);
                }
            } else {
                stopService(serviceIntent);
            }
        }
    }

    private class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean started = intent.getBooleanExtra("STARTED", false);
            startServiceToggle.setChecked(started);
        }
    }
}
