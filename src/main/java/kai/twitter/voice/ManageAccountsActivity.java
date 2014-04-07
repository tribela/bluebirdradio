package kai.twitter.voice;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import twitter4j.auth.AccessToken;

public class ManageAccountsActivity extends ActionBarActivity implements View.OnClickListener {

    private Button addAccountButton;
    private ListView listView;
    private List<String> accounts;
    private DbAdapter dbAdapter;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_accounts);

        accounts = new ArrayList<String>();
        dbAdapter = new DbAdapter(getApplicationContext());

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, accounts);

        listView = (ListView) findViewById(R.id.list_accounts);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new ItemClickListener());

        addAccountButton = (Button) findViewById(R.id.add_account_button);
        addAccountButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        accounts.clear();
        for (AccessToken token : dbAdapter.getAccounts()) {
            accounts.add(token.getToken());
        }
        arrayAdapter.notifyDataSetChanged();
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

    private class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ManageAccountsActivity.this);
            builder
                    .setTitle("Title")
                    .setMessage("Message")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //TODO: delete acount.
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }
}
