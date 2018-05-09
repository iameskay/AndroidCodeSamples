import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

/**
 * Service to post notifications
 */
public class NotificationService extends JobIntentService {

  /* *********************
   * CLASS VARIABLES
   * ******************* */

  public static final String TAG = NotificationService.class.getSimpleName();

  // IDs and Source request codes
  private static final int NOTIFICATION_JOB_ID = 12121;
  private static final int NOTIFICATION_INTENT_SRC = 12122;
  private static final int NOTIFICATION_ID = 12123;
  private static final String NOTIFICATION_CONTENT_CHANNEL_ID = "App updates";

  private int mPrimaryColour;
  private UserData mUserData;
  private String mChannelName;

  /* *********************
   * OVERRIDE METHODS
   * ******************* */

  @Override
  public void onCreate() {
    super.onCreate();
    mUserData = UserData_.getInstance_(this);
    mPrimaryColour = getResources().getColor(R.color.primary);
    mChannelName = getResources().getString(R.string.notification_content_channel);
  }

  /**
   * Posts the notification to the notification center
   *
   * @param intent operation to be performed
   */
  @Override
  protected void onHandleWork(@NonNull Intent intent) {
    if (!mUserData.getShowNotifications()) {
      Log.d(TAG, "Received notification start when shouldn't have");
      NotificationHelper.cancelNotifications(this.getApplicationContext(),
          NotificationService.class);
      return;
    }

    // Get current date
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

    // Set notification string based on day of week (THURSDAY or FRIDAY)
    String notificationText = dayOfWeek == Calendar.FRIDAY
        ? getResources().getString(R.string.notification_text_friday)
        : getResources().getString(R.string.notification_text_thursday);

    NotificationManager notificationManager
        = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    if (notificationManager != null) {
      // Create NotificationChannel for Android O+ devices
      if (Utils.isAtLeastO())
        notificationManager.createNotificationChannel(
            new NotificationChannel(NOTIFICATION_CONTENT_CHANNEL_ID, mChannelName,
                NotificationManager.IMPORTANCE_DEFAULT));

      // Build notification
      NotificationCompat.Builder mBuilder =
          new NotificationCompat.Builder(this, NOTIFICATION_CONTENT_CHANNEL_ID)
              .setSmallIcon(R.drawable.ic_notification)
              .setContentTitle(getResources().getString(R.string.app_name))
              .setPriority(NotificationCompat.PRIORITY_DEFAULT)
              .setContentText(notificationText)
              .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
              .setColor(mPrimaryColour)
              .setAutoCancel(true);

      // Creates an explicit intent for an Activity in the app
      Intent resultIntent = BookshelfActivity_.intent(this).mIsOpenFromNotification(true).get();
      resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

      PendingIntent pendingNotificationIntent =
          PendingIntent.getActivity(this.getApplicationContext(), NOTIFICATION_INTENT_SRC,
              resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

      mBuilder.setContentIntent(pendingNotificationIntent);

      // NOTIFICATION_ID allows us to update the notification later on
      notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
  }

  /* *********************
   * CONVENIENCE METHODS
   * ******************* */

  /**
   * Convenience method for enqueuing work in to this service
   *
   * @param context app context (Activity or Application)
   * @param intent  operation to be performed
   */
  public static void enqueueWork(Context context, Intent intent) {
    enqueueWork(context, NotificationService.class, NOTIFICATION_JOB_ID, intent);
  }
}