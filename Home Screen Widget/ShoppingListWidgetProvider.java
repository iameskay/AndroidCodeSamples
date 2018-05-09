import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.widget.RemoteViews;

/**
 * Base Widget Provider Class that behaves like a Broadcast Receiver for shopping list widget
 */
public abstract class ShoppingListWidgetProvider extends AppWidgetProvider {

  /* *********************
   * CLASS VARIABLES
   * ******************* */

  public static final String TAG = ShoppingListWidgetProvider.class.getSimpleName();

  private final static String EXTRA_WIDGET_ITEM_CLICK = "EXTRA_WIDGET_ITEM_CLICK";

  private ShoppingListModel mShoppingListModel = null;

  /* *********************
   * OVERRIDE METHODS
   * ******************* */

  @Override
  public void onReceive(final Context context, Intent intent) {
    // CLick action
    if (intent != null && intent.getAction() != null &&
        intent.getAction().equals(EXTRA_WIDGET_ITEM_CLICK)) {
      if (intent.getBooleanExtra(EXTRA_WIDGET_IS_MANUAL_ITEM, false)) {
        // Launch Manual Item Card Activity
        Intent manualItemCardIntent = new Intent(context, ManualItemViewerActivity_.class);
        if (intent.getSerializableExtra(M_UUID_EXTRA) != null)
          manualItemCardIntent.putExtra(M_UUID_EXTRA, intent.getSerializableExtra(M_UUID_EXTRA));
        if (intent.getStringExtra(M_ITEM_TITLE_EXTRA) != null)
          manualItemCardIntent.putExtra(M_ITEM_TITLE_EXTRA,
              intent.getStringExtra(M_ITEM_TITLE_EXTRA));
        manualItemCardIntent.putExtra(M_USER_ACTION_EXTRA, SAVE_ACTION);
        manualItemCardIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
            Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(manualItemCardIntent);
        return;
      } else {
        // Launch Flyer Activity
        int statusID = intent.getIntExtra(EXTRA_STATUS_ID, Constants.INVALID_ID);
        if (statusID != Constants.EXPIRED && statusID != Constants.DISABLED && statusID !=
            Constants.OUT_OF_REGION && statusID != Constants.INVALID_ID) {
          Intent flyerIntent = new Intent(context, FlyerActivity_.class);
          flyerIntent.putExtra(M_FLYER_ID_EXTRA, intent.getLongExtra(M_FLYER_ID_EXTRA,
              Constants.NO_ID));
          flyerIntent.putExtra(M_IS_FROM_ITEM_EXTRA, true);
          if (intent.getParcelableExtra(M_ITEM_EXTRA) != null)
            flyerIntent.putExtra(M_ITEM_EXTRA, intent.getParcelableExtra(M_ITEM_EXTRA));
          flyerIntent.putExtra(M_SOURCE_ID_EXTRA, Constants.SRC_SHOPPING_LIST);
          flyerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
          context.startActivity(flyerIntent);
          return;
        } else {
          // Launch shopping list if item is expired/out of region/disabled or view more is clicked
          Intent ShoppingListIntent = new Intent(context, ShoppingListActivity_.class);
          ShoppingListIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
          context.startActivity(ShoppingListIntent);
          return;
        }
      }
    }

    // Update action
    if (intent != null && intent.getAction() != null && Utils.compareStrings(intent.getAction(),
        AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
      AppWidgetManager manager = AppWidgetManager.getInstance(context.getApplicationContext());
      ComponentName widget = new ComponentName(context.getApplicationContext(),
          getInheritingClassObject());
      int[] widgetIDs = manager.getAppWidgetIds(widget);
      if (widgetIDs != null && widgetIDs.length > 0) onUpdate(context, manager, widgetIDs);
    }
  }

  @Override
  public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
                       final int[] appWidgetIds) {
    if (mShoppingListModel == null)
      mShoppingListModel = ShoppingListModel_.getInstance_(context);
    mShoppingListModel.refreshResultsInBg(true);

    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        afterDelay(context, appWidgetManager, appWidgetIds);
      }
    }, 2000);
  }

  /* *********************
   * CONVENIENCE METHODS
   * ******************* */

  /**
   * Called after shoppping list is refreshed in background to notify widget of changes
   *
   * @param context calling context
   * @param appWidgetManager AppWidgetManager instance to use
   * @param appWidgetIds widget IDs that need to be processed
   */
  private void afterDelay(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    // Loop for multiple widgets
    for (int widgetID : appWidgetIds) {

      // Create service intent to return remote views factory
      Intent serviceIntent = new Intent(context, ShoppingListWidgetService.class);
      serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
      serviceIntent.putExtra(EXTRA_WIDGET_TYPE, getClassTag());
      serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

      // Retrieve widget layout
      RemoteViews widget = new RemoteViews(context.getPackageName(), getWidgetLayout());
      widget.setRemoteAdapter(R.id.widget_list, serviceIntent);

      //Set empty view
      widget.setEmptyView(R.id.widget_list, R.id.widget_empty_list_layout);

      // Set intent to launch Shopping List Activity
      Intent titleClickIntent = new Intent(context, ShoppingListActivity_.class);
      titleClickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      titleClickIntent.setComponent(new ComponentName(context.getPackageName(),
          ShoppingListActivity_.class.getName()));
      PendingIntent titlePendingIntent = PendingIntent.getActivity(context, 0,
          titleClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
      widget.setOnClickPendingIntent(R.id.widget_frame, titlePendingIntent);

      // Set intent to launch search activity
      Intent searchClickIntent = new Intent(context, SearchActivity_.class);
      searchClickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      searchClickIntent.setComponent(new ComponentName(context.getPackageName(),
          SearchActivity_.class.getName()));
      PendingIntent searchPendingIntent = PendingIntent.getActivity(context, 0,
          searchClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
      widget.setOnClickPendingIntent(R.id.widget_search_layout, searchPendingIntent);

      // Set intent template for list items
      Intent listItemIntent = new Intent(context, getInheritingClassObject());
      listItemIntent.setAction(EXTRA_WIDGET_ITEM_CLICK);
      PendingIntent listItemPendingIntent = PendingIntent.getBroadcast(context, 0,
          listItemIntent, PendingIntent.FLAG_UPDATE_CURRENT);
      widget.setPendingIntentTemplate(R.id.widget_list, listItemPendingIntent);

      // Notifies widget list to invalidate data and call onDataSetChanged
      appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, R.id.widget_list);
      // Set remote views to use for the specified ID
      appWidgetManager.updateAppWidget(widgetID, widget);
    }
  }

  /**
   * Returns appropriate widget layout resource ID (implemented in subclasses)
   *
   * @return layout resource ID
   */
  public abstract int getWidgetLayout();

  /**
   * Returns class tag (implemented in subclasses)
   *
   * @return class tag
   */
  public abstract String getClassTag();

  /**
   * Returns inheriting class object (implemented in subclasses)
   *
   * @return inheriting class object
   */
  public abstract Class getInheritingClassObject();

}
