package kai.twitter.voice;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class LoginActivity extends Activity {

    private WebView webview;
    private Twitter twitter;
    private AccessToken acToken;
    private RequestToken rqToken;

    private final static Uri CALLBACK_URL = Uri.parse("bluebird://callback");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_login);


        webview = (WebView) findViewById(R.id.login_webview);

        webview.setWebViewClient(new WebViewClient() {
            DbAdapter adapter = new DbAdapter(getApplicationContext());

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                Uri uri = Uri.parse(url);

                if (uri.getScheme().equals(CALLBACK_URL.getScheme()) &&
                        uri.getAuthority().equals(CALLBACK_URL.getAuthority())) {
                    String oauth_verifier = uri.getQueryParameter("oauth_verifier");
                    if (oauth_verifier != null) {
                        try {
                            acToken = new AsyncGetAccessToken().execute(oauth_verifier).get();
                            adapter.insertAccount(acToken);
                        } catch (Exception e) {
                            String errorMsg = getString(R.string.unable_get_oauth_token);
                            String msg = e.getMessage();
                            if (msg == null) {
                                msg = errorMsg;
                            }
                            Log.e("Twitter", e.getMessage());
                            Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                    finish();
                }
            }
        });

        try {
            rqToken = new AsyncRequestTokenUrl().execute().get();
            if (rqToken == null) {
                Toast.makeText(getApplicationContext(), "Unable to Request OAuth token", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            webview.loadUrl(rqToken.getAuthorizationURL());
        } catch (InterruptedException e) {
            Log.e("Twitter", e.getMessage());
        } catch (ExecutionException e) {
            Log.e("Twitter", e.getMessage());
        }
    }

    private class AsyncRequestTokenUrl extends AsyncTask<String, Void, RequestToken> {
        @Override
        protected RequestToken doInBackground(String... strings) {
            try {
                twitter = new TwitterFactory().getInstance();
                twitter.setOAuthConsumer(getString(R.string.CONSUMER_KEY), getString(R.string.CONSUMER_SECRET));
                RequestToken token = twitter.getOAuthRequestToken(CALLBACK_URL.toString());
                return token;
            } catch (TwitterException e) {
                Log.e("Twitter", e.getMessage());
            }
            return null;
        }
    }

    private class AsyncGetAccessToken extends AsyncTask<String, Void, AccessToken> {
        @Override
        protected AccessToken doInBackground(String... strings) {
            try {
                AccessToken token = twitter.getOAuthAccessToken(rqToken, strings[0]);
                return token;
            } catch (TwitterException e) {
                Log.e("Twitter", e.getMessage());
            }
            return null;
        }
    }

}
