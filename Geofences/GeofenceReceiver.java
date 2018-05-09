public class GeofenceReceiver extends BroadcastReceiver {

  /* *********************
   * CLASS VARIABLES
   * ******************* */

  public static final String TAG = GeofenceReceiver.class.getSimpleName();

  /* *********************
   * OVERRIDE METHODS
   * ******************* */

  /**
   * Receives incoming intents
   *
   * @param context the application context
   * @param intent  sent by Location Services. This Intent is provided to Location
   *                Services (inside a PendingIntent) when addGeofences() is called
   */
  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "Received geofence broadcast");

    // This will either directly start the service (when running on pre-O platforms) or
    // enqueue work for it as a job (when running on O and later)
    GeofenceService.enqueueWork(context, intent);
  }
}