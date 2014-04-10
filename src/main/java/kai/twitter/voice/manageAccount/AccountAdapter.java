package kai.twitter.voice.manageAccount;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.ExecutionException;

import kai.twitter.voice.R;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

/**
 * Created by kjwon15 on 2014. 4. 10..
 */
public class AccountAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    List<Twitter> accounts;
    int layout = R.layout.list_account;

    public AccountAdapter(Context context, List<Twitter> accounts) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.accounts = accounts;
        this.layout = layout;
    }

    @Override
    public int getCount() {
        return accounts.size();
    }

    @Override
    public Object getItem(int position) {
        return accounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final int pos = position;
        if (view == null) {
            view = inflater.inflate(layout, parent, false);
        }
        TextView name = (TextView) view.findViewById(R.id.account_name);
        TextView loggedIn = (TextView) view.findViewById(R.id.logged_in);
        Twitter account = accounts.get(position);

        try {
            String screenName = new AsyncGetScreenName().execute(account).get();
            if (screenName != null) {
                view.setBackgroundColor(Color.TRANSPARENT);
                name.setText(screenName);
                loggedIn.setText(context.getString(R.string.logged_in));
            } else {
                view.setBackgroundColor(Color.GRAY);
                loggedIn.setText(context.getString(R.string.not_logged_in));
            }
        } catch (Exception e) {
            view.setBackgroundColor(Color.GRAY);
            loggedIn.setText(context.getString(R.string.not_logged_in));
            Log.e("Twitter", e.getMessage());
        }

        return view;
    }

    private class AsyncGetScreenName extends AsyncTask<Twitter, Void, String> {

        @Override
        protected String doInBackground(Twitter... arguments) {
            Twitter twitter = arguments[0];
            try {
                return twitter.getScreenName();
            } catch (TwitterException e) {
                return null;
            }
        }
    }
}
