package kai.twitter.voice.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import kai.twitter.voice.R;

/**
 * Created by kjwon15 on 2014. 5. 18..
 */
public class CustomToast {

    public static Toast makeText(Context context, String message, int duration) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View toastRoot = inflater.inflate(R.layout.toast_custom, null);
        TextView textView = (TextView) toastRoot.findViewById(R.id.text);
        textView.setText(message);

        Toast toast = new Toast(context);
        toast.setView(toastRoot);
        toast.setDuration(duration);
        return toast;
    }
}
