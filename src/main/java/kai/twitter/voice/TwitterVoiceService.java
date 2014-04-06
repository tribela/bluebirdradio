package kai.twitter.voice;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

/**
 * Created by kjwon15 on 2014. 3. 30..
 */
public class TwitterVoiceService extends Service implements OnInitListener {
    private static TwitterVoiceService instance = null;

    private TextToSpeech tts;
    private TwitterStream stream;
    private Notification notification;
    private SharedPreferences preferences;
    private HeadphoneReceiver receiver;

    private static final String SHOW = "Show";
    private static final String STOP = "Stop";

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(SHOW)) {
                Intent main = new Intent(getApplicationContext(), MainActivity.class);
                main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(main);
            } else if (action.equals(STOP)) {
                this.stopSelf();
            }
        }

        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        instance = this;
        tts = new TextToSpeech(this, this);
        receiver = new HeadphoneReceiver();
        makeNotification();
        registerHeadsetReceiver();
        loginTwitter();
    }

    private void makeNotification() {
        Intent mainIntent = new Intent(this, this.getClass());
        Intent stopIntent = new Intent(this, this.getClass());
        mainIntent.setAction(SHOW);
        stopIntent.setAction(STOP);
        PendingIntent pMainIntent = PendingIntent.getService(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pStopIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification = new NotificationCompat.Builder(this)
                .setContentTitle(getText(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pMainIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_launcher, "Stop", pStopIntent)
                .build();

        startForeground(1, notification);
    }

    private void registerHeadsetReceiver() {
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (preferences.getBoolean("stop_on_unplugged", true)) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            registerReceiver(receiver, filter);
        }
    }

    private void loginTwitter() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                AccessToken token = new AccessToken(
                        "268233806-fPzlywABx7sPjWUjPv24imwSrSPqKtoYqcQLYUdJ",
                        "wRL0QbxD7JOu3N1h2f79IPtCKMy0b9o8PLsOvXi8bFZXF");

                Twitter twitter = TwitterFactory.getSingleton();
                twitter.setOAuthConsumer(getString(R.string.CONSUMER_KEY),
                        getString(R.string.CONSUMER_SECRET));
                twitter.setOAuthAccessToken(token);

                stream = new TwitterStreamFactory(twitter.getConfiguration()).getInstance();
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

        stopForeground(true);

        unregisterReceiver(receiver);

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
            String name;
            if(status.isRetweet()) {
                Status retweetedStatus = status.getRetweetedStatus();
                name = retweetedStatus.getUser().getName();
                text = retweetedStatus.getText();
            } else {
                name = status.getUser().getName();
                text = status.getText();
            }
            String message = String.format("%s: %s", name, text);
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
