package cmsc436.feelingsdiary;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NotificationReminderService extends IntentService {
    public final int MILLIS_IN_DAY = 86400000;

    private AlarmManager mAlarmManager;
    private SharedPreferences preferences;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NotificationReminderService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = getSharedPreferences("feelingsdiary", Context.MODE_PRIVATE);

        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getService(
                this,
                0,
                new Intent(this, SendNotificationReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // set alarm to send out intent every day
        mAlarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                MILLIS_IN_DAY,
                MILLIS_IN_DAY,
                pendingIntent
        );
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // TODO - send notification
    }


    public class SendNotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int daysSinceLastEntry = preferences.getInt("lastentry", 0);
            int daysSinceLastNotification = preferences.getInt("lastnotification", 0);

            if (daysSinceLastEntry > 1 && daysSinceLastNotification > 1) {

                // TODO - gather info to

                preferences.edit().putInt("lastnotification", 0).apply();
            } else {
                preferences.edit().putInt("lastnotification", daysSinceLastNotification + 1).apply();
            }

            preferences.edit().putInt("lastentry", daysSinceLastEntry + 1).apply();
        }
    }
}
