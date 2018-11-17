package cmsc436.feelingsdiary;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Random;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class NotificationReminderReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 1;

    // reminder frequency
    private static final int DAILY = 0;
    private static final int FEW_DAYS = 1;
    private static final int WEEKLY = 2;
    private static final int NEVER = 3;

    private SharedPreferences preferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("alarm", "got something");
        preferences = context.getSharedPreferences("feelingsdiary", Context.MODE_PRIVATE);
        int notificationSetting = preferences.getInt("notificationsetting", 0);

        switch(notificationSetting) {
            case DAILY:
                sendNotification(context, 0);
                break;
            case FEW_DAYS:
                sendNotification(context, 2);
                break;
            case WEEKLY:
                sendNotification(context, 6);
                break;
            case NEVER:
                preferences.edit()
                        .putInt("lastentry", preferences.getInt("lastentry", 0) + 1)
                        .apply();
        }
    }

    private void sendNotification(Context context, int minDays) {
        int daysSinceLastEntry = preferences.getInt("lastentry", 0);
        int daysSinceLastNotification = preferences.getInt("lastnotification", 0);

        if (daysSinceLastEntry > minDays && daysSinceLastNotification > minDays) {
            if (daysSinceLastEntry < 7) {
                String[] allMessages = {
                        context.getString(R.string.daily_reminder_1),
                        context.getString(R.string.daily_reminder_2),
                        context.getString(R.string.daily_reminder_3)
                };

                sendNotificationHelper(context, allMessages[new Random().nextInt(3)]);
            } else if (daysSinceLastEntry < 14){
                String[] allMessages = {
                        context.getString(R.string.weekly_reminder_1),
                        context.getString(R.string.weekly_reminder_2),
                };

                sendNotificationHelper(context, allMessages[new Random().nextInt(2)]);
            } else {
                sendNotificationHelper(context, context.getString(R.string.long_time_reminder));
            }

            preferences.edit().putInt("lastnotification", 0).apply();
        } else {
            preferences.edit().putInt("lastnotification", daysSinceLastNotification + 1).apply();
        }

        preferences.edit().putInt("lastentry", daysSinceLastEntry + 1).apply();
    }

    private void sendNotificationHelper(Context context, String message) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelID = context.getString(R.string.channel_id);

        NotificationChannel channel = new NotificationChannel(
                channelID,
                context.getString(R.string.channel_title),
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(context.getString(R.string.channel_description));
        manager.createNotificationChannel(channel);

        Intent resultIntent = new Intent(context, LoginActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(context, channelID)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setChannelId(channelID)
                .setContentText(message)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentIntent(pendingIntent)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_REMINDER)
                .build();

        manager.notify(NOTIFICATION_ID, notification);
    }
}
