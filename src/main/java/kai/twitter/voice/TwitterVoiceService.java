package kai.twitter.voice;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;
import android.speech.tts.TextToSpeech.OnInitListener;

import java.util.Properties;

import twitter4j.DirectMessage;
import twitter4j.HttpClientConfiguration;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by kjwon15 on 2014. 3. 30..
 */
public class TwitterVoiceService extends Service implements OnInitListener {
    private TextToSpeech tts;
    private TwitterStream stream;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        tts = new TextToSpeech(this, this);
        loginTwitter();
    }

    private void loginTwitter() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                ConfigurationBuilder cb = new ConfigurationBuilder();
                cb.setOAuthConsumerKey((String) getText(R.string.CONSUMER_KEY))
                        .setOAuthConsumerSecret((String) (getText(R.string.CONSUMER_SECRET)))
                        .setOAuthAccessToken("268233806-fPzlywABx7sPjWUjPv24imwSrSPqKtoYqcQLYUdJ")
                        .setOAuthAccessTokenSecret("wRL0QbxD7JOu3N1h2f79IPtCKMy0b9o8PLsOvXi8bFZXF");

                Configuration conf = cb.build();

                stream = new TwitterStreamFactory(conf).getInstance();
                stream.addListener(new Listener());
                stream.user();

            }
        }).start();
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if (stream != null) {
            stream.shutdown();
        }

        Toast.makeText(getApplicationContext(),
                getText(R.string.service_stopped),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Toast.makeText(getApplicationContext(),
                    getText(R.string.service_started),
                    Toast.LENGTH_SHORT).show();
        } else {
            Log.e("TTS", "Initialize failed");
            Toast.makeText(getApplicationContext(),
                    getText(R.string.initialize_failed),
                    Toast.LENGTH_SHORT).show();
            this.stopSelf();
        }
    }

    private class Listener implements StatusListener {

        @Override
        public void onStatus(Status status) {
            String text;
            String screenName;
            if(status.isRetweet()) {
                Status retweetedStatus = status.getRetweetedStatus();
                screenName = retweetedStatus.getUser().getScreenName();
                text = retweetedStatus.getText();
            } else {
                screenName = status.getUser().getScreenName();
                text = status.getText();
            }
            String message = String.format("%s: %s", screenName, text);
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

        }

        @Override
        public void onTrackLimitationNotice(int i) {

        }

        @Override
        public void onScrubGeo(long l, long l2) {

        }

        @Override
        public void onStallWarning(StallWarning stallWarning) {

        }

        @Override
        public void onException(Exception e) {

        }
    }
}
