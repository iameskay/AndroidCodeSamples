import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Service that is used to request the shopping list widget views factory
 */
public class ShoppingListWidgetService extends RemoteViewsService {

  public static String EXTRA_WIDGET_TYPE = "EXTRA_WIDGET_TYPE";

  @Override
  public RemoteViewsFactory onGetViewFactory(Intent intent) {
    if (intent != null && intent.getStringExtra(EXTRA_WIDGET_TYPE) != null) {
      if (Utils.compareStrings(intent.getStringExtra(EXTRA_WIDGET_TYPE),
          ShoppingListWidgetDayProvider.TAG))
        return new ShoppingListWidgetDayViewsFactory(getApplicationContext(), intent);
      else return new ShoppingListWidgetNightViewsFactory(getApplicationContext(), intent);
    }

    return null;
  }

}
