import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;

/**
 * Class that is used to interface with the FusedLocationProvider to find the latitude and longitude
 * of the device
 */
@EBean(scope = EBean.Scope.Singleton)
public class FusedLocationUtils {

  /* *********************
   * CLASS VARIABLES
   * ******************* */

  public static final String TAG = FusedLocationUtils.class.getSimpleName();

  // Entry point for Fused Location Provider API
  // The FLP Client is used in place of FLP API so we don't have to deal with google api client
  // Play services connections for us are handles automatically
  private FusedLocationProviderClient mFusedLocationClient;

  /**
   * Bean
   */
  @Bean
  GeofenceHelper mGeofenceHelper;
  @Bean
  UserData mUserData;

  /* *********************
   * CONVENIENCE METHODS
   * ******************* */

  /**
   * Request a single location update to new SimpleLocationListener
   *
   * @param context   app context (Activity)
   * @param postEvent whether or not to post GPS event
   */
  public void requestLocation(Context context, boolean postEvent) {
    if (context != null) {
      mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
      requestLocation(new CustomLocationCallback(postEvent));
      return;
    }

    EventBus.getDefault().post(new GpsLocationEvent(null));
  }

  /**
   * Request a location update to the supplied listener
   *
   * @param locationCallback callback to receive location request updates
   * @throws SecurityException not caught in the method
   */
  private void requestLocation(CustomLocationCallback locationCallback)
      throws SecurityException {
    // Create location request (new Constructor is hidden, use create())
    LocationRequest locationRequest = LocationRequest.create();
    locationRequest.setInterval(UPDATE_INTERVAL);
    locationRequest.setFastestInterval(FASTEST_INTERVAL);
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);

    // TODO: This is for foreground requests, use diff params for background
    mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
  }

  /* *********************
   * INNER CLASSES
   * ******************* */

  /**
   * Callback to receive location request updates
   */
  public class CustomLocationCallback extends LocationCallback {

    boolean iEnabled = true;
    boolean iPostEvent;

    CustomLocationCallback(boolean postEvent) {
      iPostEvent = postEvent;
    }

    /**
     * Location result callback
     *
     * @param locationResult object holding location result data
     */
    // TODO: With FLP, we can handle multiple locations
    @Override
    public void onLocationResult(LocationResult locationResult) {
      if (iEnabled) {
        iEnabled = false;

        // Get reference to old latitude and longitude
        float oldLat = mUserData.getActualLatitude();
        float oldLong = mUserData.getActualLongitude();

        // Incoming location result
        Location location = locationResult.getLastLocation();
        if (location.getLatitude() != 0.0f && location.getLongitude() != 0.0f) {
          if (iPostEvent) EventBus.getDefault().post(new GpsLocationEvent(location));
          mUserData.setActualLocationData((float) location.getLatitude(),
              (float) location.getLongitude());
        } else {
          mUserData.setActualLocationData(mUserData.getLatitude(), mUserData.getLongitude());
        }

        if (mUserData.isGeofenceAvailable()) {
          // Update geofences when not posting GpsLocationEvent
          if (!iPostEvent && mUserData.shouldUpdateGeofences(oldLat, oldLong))
            mGeofenceHelper.updateGeofences(false);
        } else {
          mGeofenceHelper.unregisterGeofences(false, false);
        }
      }
    }

    /**
     * Location availability result
     *
     * @param locationAvailability object holding location availability data
     */
    @Override
    public void onLocationAvailability(LocationAvailability locationAvailability) {
      Log.d(TAG, "Is Location Available: " + locationAvailability.isLocationAvailable());
    }
  }
}
