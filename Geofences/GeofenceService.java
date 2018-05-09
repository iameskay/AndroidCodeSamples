import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that handles geofences
 */
public class GeofenceService extends JobIntentService {

  /* *********************
   * CLASS VARIABLES
   * ******************* */

  public static final String TAG = GeofenceService.class.getSimpleName();

  private static final int GEOFENCE_JOB_ID = 1000;

  private static final String GEOFENCE_NOTIFICATION_CHANNEL_ID = "Geofence Updates";

  /* *********************
   * OVERRIDE METHODS
   * ******************* */

  /**
   * Helper for processing work that has been enqueued for a job/service
   *
   * @param intent the intent describing the work to be processed
   */
  @Override
  protected void onHandleWork(@NonNull Intent intent) {
    GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
    if (!geofencingEvent.hasError()) {
      // Get the transition type
      int geofenceTransition = geofencingEvent.getGeofenceTransition();

      // Get the geofences that were triggered. A single event can trigger multiple geofences
      List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

      // Check that the reported transition was of interest
      if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER
          || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
          || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

        int actionID;
        switch (geofenceTransition) {
          case Geofence.GEOFENCE_TRANSITION_ENTER:
            actionID = Constants.GEOFENCE_ENTER;
            break;
          case Geofence.GEOFENCE_TRANSITION_EXIT:
            actionID = Constants.GEOFENCE_EXIT;
            break;
          default:
            actionID = Constants.GEOFENCE_DWELL;
        }

        // Get the transition details as a String.
        String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
            triggeringGeofences);

        Log.d(TAG, geofenceTransitionDetails);

        NotificationManager mNotificationManager = (NotificationManager)
            getSystemService(Context.NOTIFICATION_SERVICE);

        if (Utils.isAtLeastO()) {
          NotificationChannel mChannel = new NotificationChannel(GEOFENCE_NOTIFICATION_CHANNEL_ID,
              "Geofence Updates",
              NotificationManager.IMPORTANCE_HIGH);
          if (mNotificationManager != null)
            mNotificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(this, GEOFENCE_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_location_primary)
                .setContentTitle(geofenceTransitionDetails)
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(geofenceTransitionDetails));

        if (mNotificationManager != null) mNotificationManager.notify(0, mBuilder.build());
      }
    }
  }

  /* *********************
   * CONVENIENCE METHODS
   * ******************* */

  /**
   * Convenience method for enqueuing work in to this service.
   *
   * @param context app context (Activity or Application)
   * @param intent  operation to be performed
   */
  public static void enqueueWork(Context context, Intent intent) {
    enqueueWork(context, GeofenceService.class, GEOFENCE_JOB_ID, intent);
  }


  /**
   * Gets transition details and returns them as a formatted string for testing
   *
   * @param geofenceTransition  The ID of the geofence transition.
   * @param triggeringGeofences The geofence(s) triggered.
   * @return The transition details formatted as String.
   */
  private String getGeofenceTransitionDetails(int geofenceTransition,
                                              List<Geofence> triggeringGeofences) {

    String geofenceTransitionString;

    switch (geofenceTransition) {
      case Geofence.GEOFENCE_TRANSITION_ENTER:
        geofenceTransitionString = "Entered Geofence";
        break;
      case Geofence.GEOFENCE_TRANSITION_EXIT:
        geofenceTransitionString = "Exited Geofence";
        break;
      case Geofence.GEOFENCE_TRANSITION_DWELL:
        geofenceTransitionString = "Dwelling in Geofence";
        break;
      default:
        geofenceTransitionString = "Unknown transition";
    }

    // Get the Ids of each geofence that was triggered.
    ArrayList<String> triggeringGeofencesIds = new ArrayList<>();

    for (Geofence geofence : triggeringGeofences) {
      triggeringGeofencesIds.add(geofence.getRequestId());
    }

    String triggeringGeofencesString = TextUtils.join(", ", triggeringGeofencesIds);

    return geofenceTransitionString + ": " + triggeringGeofencesString;
  }
}
