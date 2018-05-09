import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper to register geofences
 */
@EBean(scope = EBean.Scope.Singleton)
public class GeofenceHelper {

  /* *********************
   * CLASS VARIABLES
   * ******************* */

  public static final String TAG = GeofenceHelper.class.getSimpleName();

  private ArrayList<Geofence> mGeofenceList = new ArrayList<>();
  private GeofencingClient mGeofencingClient;

  /**
   * Bean
   */
  @Bean
  UserData mUserData;

  /**
   * Prefs
   */
  @Pref
  SessionPrefs_ mSessionPrefs;

  /**
   * Root Context
   */
  @RootContext
  Context mContext;

  /* *********************
   * CONVENIENCE METHODS
   * ******************* */

  /**
   * Updates geofences if available
   *
   * @param isReboot are we entering from a device reboot
   */
  public void updateGeofences(final boolean isReboot) {
    Log.d(TAG, "Updating geofences");

    // First unregister geofences
    unregisterGeofences(mUserData.isGeofenceAvailable()
        && mUserData.getAndUpdateLocationAuthStatusID() == Constants.LOCATION_GRANTED);
  }

  /**
   * Unregister geofences with OS
   *
   * @param registerGeofences whether or not to register geofences
   */
  public void unregisterGeofences(final boolean registerGeofences) {
    if (mGeofencingClient == null)
      mGeofencingClient = LocationServices.getGeofencingClient(mContext);
    mUserData.setGeofenceRegistered(false);

    // Create list of geofence IDs
    final List<String> geofencesIDs = new ArrayList<>();
    for (Geofence geofence : mGeofenceList) {
      geofencesIDs.add(geofence.getRequestId());
    }

    // Unregister existing geofences
    if (!geofencesIDs.isEmpty()) {
      mGeofencingClient.removeGeofences(geofencesIDs)
          .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              if (task.isSuccessful()) {
                mGeofenceList.clear();
                Log.d(TAG, geofencesIDs.size() + " Geofences Unregistered");
              }
            }
          });
    }

    if (registerGeofences) {
      registerGeofences();
      return;
    }
  }

  /**
   * Register geofences with OS
   */
  @SuppressLint("MissingPermission")
  private void registerGeofences() {
    if (mUserData.isGeofenceAvailable()
        && mUserData.getAndUpdateLocationAuthStatusID() == Constants.LOCATION_GRANTED) {
        if (mGeofenceList.isEmpty()) populateGeofenceList();

      // Add geofences to be monitored
      if (!mGeofenceList.isEmpty()) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER
            | GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(mGeofenceList);

        // TODO: permission is checked at top level
        Intent intent = new Intent(mContext, GeofenceReceiver.class);
        mGeofencingClient.addGeofences(builder.build(),
            PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
            .addOnCompleteListener(new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                  Log.d(TAG, mGeofenceList.size() + " Geofences Registered");
                  mUserData.setGeofenceRegistered(task.isSuccessful());
                }
              }
            });

        return;
      }
    }
  }


  /**
   * Hard code geofence data
   */
  private void populateGeofenceList() {
    if (isTestingGeofences()) {
      Log.d(TAG, "Populating Geofence List");

      final HashMap<String, LatLng> TEST_LOCATIONS = new HashMap<>();

      TEST_LOCATIONS.put("Geofence 1", new LatLng(LAT_1, LONG_1));
      TEST_LOCATIONS.put("Geofence 2", new LatLng(LAT_2, LONG_2));
      TEST_LOCATIONS.put("Geofence 3", new LatLng(LAT_3, LONG_3));
      TEST_LOCATIONS.put("Geofence 4", new LatLng(LAT_4, LONG_4));
      TEST_LOCATIONS.put("Geofence 5", new LatLng(LAT_5, LONG_5));
      TEST_LOCATIONS.put("Geofence 6", new LatLng(LAT_6, LONG_6));
      TEST_LOCATIONS.put("Geofence 7", new LatLng(LAT_7, LONG_7));
      TEST_LOCATIONS.put("Geofence 8", new LatLng(LAT_8, LONG_8));
      TEST_LOCATIONS.put("Geofence 9", new LatLng(LAT_9, LONG_9));

      for (Map.Entry<String, LatLng> entry : TEST_LOCATIONS.entrySet()) {

        mGeofenceList.add(new Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId(entry.getKey())

            // Set the circular region of this geofence.
            .setCircularRegion(entry.getValue().latitude, entry.getValue().longitude,
                GEOFENCE_RADIUS_IN_METERS)

            // Set the expiration duration of the geofence. This geofence gets automatically
            // removed after this period of time.
            .setExpirationDuration(Geofence.NEVER_EXPIRE)

            // Set the transition types of interest. Alerts are only generated for these transitions
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)

            .setNotificationResponsiveness(GEOFENCE_RESPONSE_TIME)

            .setLoiteringDelay(DWELL_TIME)

            // Create the geofence.
            .build());
      }
    }
  }
}
