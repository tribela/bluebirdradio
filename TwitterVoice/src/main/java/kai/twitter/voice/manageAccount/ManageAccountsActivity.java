package kai.twitter.voice.manageAccount;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import kai.twitter.voice.DbAdapter;
import kai.twitter.voice.LoginActivity;
import kai.twitter.voice.R;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class ManageAccountsActivity extends ActionBarActivity implements View.OnClickListener {

    private Button addAccountButton;
    private ListView listView;
    private List<Twitter> accounts;
    private DbAdapter dbAdapter;
    private AccountAdapter accountAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_accounts);

        accounts = new ArrayList<Twitter>();
        dbAdapter = new DbAdapter(getApplicationContext());

        accountAdapter = new AccountAdapter(this, accounts);

        listView = (ListView) findViewById(R.id.list_accounts);
        listView.setAdapter(accountAdapter);
        listView.setOnItemClickListener(new ItemClickListener());

        addAccountButton = (Button) findViewById(R.id.add_account_button);
        addAccountButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAccounts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_accounts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (R.id.add_account_button == view.getId()) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
    }

    private void refreshAccounts() {
        accounts.clear();
        for (AccessToken token : dbAdapter.getAccounts()) {
            Twitter twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer(getString(R.string.CONSUMER_KEY), getString(R.string.CONSUMER_SECRET));
            twitter.setOAuthAccessToken(token);
            accounts.add(twitter);
        }
        accountAdapter.notifyDataSetChanged();
    }

    private class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ManageAccountsActivity.this);
            builder
                    .setTitle(R.string.title_delete_account)
                    .setMessage(R.string.message_delete_account)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                dbAdapter.deleteAccount(accounts.get(position).getOAuthAccessToken());
                            } catch (TwitterException e) {
                                Log.e("Twitter", "Cannot remove account");
                            }
                            refreshAccounts();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }
}
