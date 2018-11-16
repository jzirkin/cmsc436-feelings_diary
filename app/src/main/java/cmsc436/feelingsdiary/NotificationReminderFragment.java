package cmsc436.feelingsdiary;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Random;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class NotificationReminderFragment extends Fragment {
    private final String ACTION_NOTIFICATION = "cmsc436.feelingsdiary.action.SEND_NOTIFICATION";
    private final int MILLIS_IN_DAY = 86400000;
    private final int NOTIFICATION_ID = 1;

    // reminder frequency
    private final int DAILY = 0;
    private final int FEW_DAYS = 1;
    private final int WEEKLY = 2;
    private final int NEVER = 3;

    private AlarmManager mAlarmManager;
    private SharedPreferences preferences;

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        preferences = context.getSharedPreferences("feelingsdiary", Context.MODE_PRIVATE);

        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent();
        intent.setAction(ACTION_NOTIFICATION);
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // set alarm to send out intent every day
        mAlarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                MILLIS_IN_DAY,
                MILLIS_IN_DAY,
                pendingIntent
        );
    }

    public class SendNotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int notificationSetting = preferences.getInt("notificationsetting", 0);

            switch(notificationSetting) {
                case DAILY:
                    sendNotification(1);
                    break;
                case FEW_DAYS:
                    sendNotification(3);
                    break;
                case WEEKLY:
                    sendNotification(7);
                    break;
                case NEVER:
                    preferences.edit()
                            .putInt("lastentry", preferences.getInt("lastentry", 0) + 1)
                            .apply();
            }
        }

        private void sendNotification(int minDays) {
            int daysSinceLastEntry = preferences.getInt("lastentry", 0);
            int daysSinceLastNotification = preferences.getInt("lastnotification", 0);

            if (daysSinceLastEntry > minDays && daysSinceLastNotification > minDays) {
                if (daysSinceLastEntry < 7) {
                    String[] allMessages = {
                            getString(R.string.daily_reminder_1),
                            getString(R.string.daily_reminder_2),
                            getString(R.string.daily_reminder_3)
                    };

                    sendNotificationHelper(allMessages[new Random().nextInt(3)]);
                } else if (daysSinceLastEntry < 14){
                    String[] allMessages = {
                            getString(R.string.weekly_reminder_1),
                            getString(R.string.weekly_reminder_2),
                    };

                    sendNotificationHelper(allMessages[new Random().nextInt(2)]);
                } else {
                    sendNotificationHelper(getString(R.string.long_time_reminder));
                }

                preferences.edit().putInt("lastnotification", 0).apply();
            } else {
                preferences.edit().putInt("lastnotification", daysSinceLastNotification + 1).apply();
            }

            preferences.edit().putInt("lastentry", daysSinceLastEntry + 1).apply();
        }

        private void sendNotificationHelper(String message) {
            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            String channelID = getString(R.string.channel_id);

            NotificationChannel channel = new NotificationChannel(
                    channelID,
                    getString(R.string.channel_title),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(getString(R.string.channel_description));
            manager.createNotificationChannel(channel);

            Intent resultIntent = new Intent(context, LoginActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            PendingIntent pendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new Notification.Builder(context, channelID)
                    .setSmallIcon(null)
                    .setChannelId(channelID)
                    .setContentText(message)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentIntent(pendingIntent)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .build();

            manager.notify(NOTIFICATION_ID, notification);
        }
    }
}
