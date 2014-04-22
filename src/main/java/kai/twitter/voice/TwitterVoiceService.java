package kai.twitter.voice;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

import kai.twitter.voice.manageAccount.ManageAccountsActivity;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by kjwon15 on 2014. 3. 30..
 */
public class TwitterVoiceService extends Service implements OnInitListener {
    private static final String SHOW = "Show";
    private static final String STOP = "Stop";
    private static TwitterVoiceService instance = null;
    private DbAdapter adapter;
    private TextToSpeech tts;
    private List<TwitterStream> streams;
    private Notification notification;
    private SharedPreferences preferences;
    private HeadphoneReceiver headphoneReceiver;
    private PrefChangeListener prefChangeListener;
    private boolean opt_speak_screenname;
    private boolean opt_stop_on_unplugged;

    public static boolean isRunning() {
        return instance != null;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(SHOW)) {
                    showMain();
                } else if (action.equals(STOP)) {
                    this.stopSelf();
                }
            }
        }

        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        instance = this;
        adapter = new DbAdapter(getApplicationContext());
        tts = new TextToSpeech(this, this);
        streams = new ArrayList<TwitterStream>();
        headphoneReceiver = new HeadphoneReceiver();
        makeNotification();
        initConfig();
        registerHeadsetReceiver();
        loginTwitter();
    }

    private void initConfig() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        readConfig();

        prefChangeListener = new PrefChangeListener();
        IntentFilter filter = new IntentFilter();
        filter.addAction(getResources().getString(R.string.ACTION_CHANGE_PREFERENCE));
        registerReceiver(prefChangeListener, filter);
    }

    private void readConfig() {
        opt_speak_screenname = preferences.getBoolean("speak_screenname", false);
        opt_stop_on_unplugged = preferences.getBoolean("stop_on_unplugged", true);

        if (opt_stop_on_unplugged) {
            IntentFilter headphoneFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            registerReceiver(headphoneReceiver, headphoneFilter);
        } else {
            unregisterReceiver(headphoneReceiver);
        }
    }

    private void makeNotification() {
        Intent mainIntent = new Intent(this, this.getClass());
        Intent stopIntent = new Intent(this, this.getClass());
        mainIntent.setAction(SHOW);
        stopIntent.setAction(STOP);
        PendingIntent pMainIntent = PendingIntent.getService(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pStopIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification = new NotificationCompat.Builder(this)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setContentTitle(getText(R.string.app_name))
                .setSmallIcon(R.drawable.ic_stat_notify_service)
                .setContentIntent(pMainIntent)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", pStopIntent)
                .build();

        startForeground(1, notification);
    }

    private void registerHeadsetReceiver() {
        if (opt_stop_on_unplugged) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            registerReceiver(headphoneReceiver, filter);
        }
    }

    private void loginTwitter() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<AccessToken> tokens = adapter.getAccounts();
                if (tokens.isEmpty()) {
                    showManageAccounts();
                    stopSelf();
                    return;
                }

                for (AccessToken token : tokens) {
                    Configuration conf = new ConfigurationBuilder()
                            .setOAuthConsumerKey(getString(R.string.CONSUMER_KEY))
                            .setOAuthConsumerSecret(getString(R.string.CONSUMER_SECRET))
                            .setOAuthAccessToken(token.getToken())
                            .setOAuthAccessTokenSecret(token.getTokenSecret())
                            .build();
                    TwitterStream stream = new TwitterStreamFactory(conf).getInstance();
                    streams.add(stream);
                    stream.addListener(new Listener());
                    stream.user();
                }

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

        if (streams != null) {
            for (TwitterStream stream : streams) {
                stream.shutdown();
            }
            streams.clear();
        }

        stopForeground(true);

        try {
            unregisterReceiver(headphoneReceiver);
        } catch (IllegalArgumentException e) {
            Log.d("Receiver", e.getMessage());
        }

        unregisterReceiver(prefChangeListener);

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

    private void showMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showManageAccounts() {
        Intent intent = new Intent(getApplicationContext(), ManageAccountsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private class PrefChangeListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String name = intent.getAction();
            if (name.equals(getResources().getString(R.string.ACTION_CHANGE_PREFERENCE))) {
                readConfig();
            }
        }
    }

    private class Listener implements StatusListener {

        @Override
        public void onStatus(Status status) {
            String text;
            String name;
            if (status.isRetweet()) {
                Status retweetedStatus = status.getRetweetedStatus();
                if (opt_speak_screenname) {
                    name = retweetedStatus.getUser().getScreenName();
                } else {
                    name = retweetedStatus.getUser().getName();
                }
                text = retweetedStatus.getText();
            } else {
                if (opt_speak_screenname) {
                    name = status.getUser().getScreenName();
                } else {
                    name = status.getUser().getName();
                }
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
