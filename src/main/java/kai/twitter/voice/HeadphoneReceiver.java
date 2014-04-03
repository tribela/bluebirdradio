package kai.twitter.voice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by kjwon15 on 2014. 4. 3..
 */
public class HeadphoneReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            // 0: headset plugged.
            // 1: headset unplugged.
            if (state == 0) {
                Log.i("HEADSET", "Headset unplugged");
                Intent service = new Intent(context, TwitterVoiceService.class);
                context.stopService(service);
            }
        }
    }
}
