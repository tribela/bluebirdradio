package kai.twitter.voice;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by kjwon15 on 2014. 3. 30..
 */
public class TwitterVoiceService extends Service implements OnInitListener {
    private static TwitterVoiceService instance = null;
    private TextToSpeech tts;
    private TwitterStream stream;
    private NotificationManager notificationManager;
    private Notification notification;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        instance = this;
        tts = new TextToSpeech(this, this);
        makeNotification();
        loginTwitter();
    }

    @SuppressLint("NewApi")
    private void makeNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, this.getClass());
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        notification = new Notification.Builder(this)
                .setContentTitle(getText(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_launcher, "Stop", pIntent)
                .build();

        notificationManager.notify(0, notification);
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
        instance = null;
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if (stream != null) {
            stream.shutdown();
        }

        if (notificationManager != null) {
            notificationManager.cancelAll();
        }

        Toast.makeText(getApplicationContext(),
                getText(R.string.service_stopped),
                Toast.LENGTH_SHORT).show();
    }

    public static boolean isRunning() {
        return instance != null;
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
            tts.speak(message, TextToSpeech.QUEUE_ADD, null);
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
