package kai.twitter.voice.tweetFilter;

import java.util.Date;
import java.util.HashMap;

import twitter4j.Status;

/**
 * Created by kjwon15 on 2014. 5. 5..
 */
public class StatusManager {
    private HashMap<Long, Date> statuses = new HashMap<Long, Date>();
    private int lifeTime;

    public StatusManager(int lifeTime) {
        this.lifeTime = lifeTime * 1000;
    }

    public boolean isSpoken(Status status) {
        long statusId;
        if (status.isRetweet()) {
            statusId = status.getRetweetedStatus().getId();
        } else {
            statusId = status.getId();
        }

        cleanStatuses(status.getCreatedAt());

        if (statuses.containsKey(statusId)) {
            statuses.put(statusId, status.getCreatedAt());
            return true;
        } else {
            statuses.put(statusId, status.getCreatedAt());
            return false;
        }
    }

    private void cleanStatuses(Date now) {
        for (long statusId : statuses.keySet()) {
            if ((now.getTime() - statuses.get(statusId).getTime()) > lifeTime) {
                statuses.remove(statusId);
            }
        }
    }
}
