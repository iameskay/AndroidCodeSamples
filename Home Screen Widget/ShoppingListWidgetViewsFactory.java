import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Interface for adapter between remote ListView and data
 */
public abstract class ShoppingListWidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {

  /* *********************
   * CLASS VARIABLES
   * ******************* */

  private String TAG = ShoppingListWidgetViewsFactory.class.getSimpleName();

  public final static String EXTRA_WIDGET_IS_MANUAL_ITEM = "EXTRA_WIDGET_IS_MANUAL_ITEM";

  private int MAX_ITEMS = 25;

  // Flyer Activity Extras
  public final static String M_IS_FROM_ITEM_EXTRA = "mIsFromItem";
  public final static String M_SOURCE_ID_EXTRA = "mSourceID";
  public final static String M_ITEM_EXTRA = "mItem";
  public final static String M_FLYER_ID_EXTRA = "mFlyerID";
  public final static String EXTRA_STATUS_ID = "EXTRA_STATUS_ID";

  // Manual Item Viewer Activity Extras
  public final static String M_USER_ACTION_EXTRA = "mUserAction";
  public final static String M_ITEM_TITLE_EXTRA = "mItemTitle";
  public final static String M_UUID_EXTRA = "mUUID";

  private Context mContext;
  @SuppressWarnings("FieldCanBeLocal")
  private int mWidgetID;
  private static List<ShoppingListRow> mWidgetListRows;

  // Database
  private RuntimeExceptionDao<Flyer, Long> mFlyerDao;
  private DatabaseHelper mDatabaseHelper;

  // Singletons
  private PicassoUtils mPicassoUtils;
  private ShoppingListModel mShoppingListModel;
  private UserData mUserData;

  /* *********************
   * CONSTRUCTORS
   * ******************* */

  public ShoppingListWidgetViewsFactory(Context context, Intent intent) {
    mContext = context;
    mWidgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID);
  }

  /* *********************
   * OVERRIDE METHODS
   * ******************* */

  @Override
  public void onCreate() {
    mShoppingListModel = ShoppingListModel_.getInstance_(mContext);
    mPicassoUtils = PicassoUtils_.getInstance_(mContext);
    mUserData = UserData_.getInstance_(mContext);

    // Database
    mDatabaseHelper = OpenHelperManager.getHelper(mContext, DatabaseHelper.class);
    try {
      //noinspection unchecked
      mFlyerDao = new RuntimeExceptionDao<>(((Dao<Flyer, Long>)
          mDatabaseHelper.getDao(Flyer.class)));
    } catch (final SQLException e) {
      Log.e(TAG, "Could not create DAO mFlyerDAO", e);
    }
  }

  @Override
  public RemoteViews getViewAt(int position) {
    String languageID;

    if (Utils.compareStrings(mUserData.getDefaultLanguageID(), Constants.D_LANG_ID)) {
      // App language is set to default
      if (Utils.compareStrings(Resources.getSystem().getConfiguration().locale.getLanguage(),
          Locale.FRENCH.getLanguage())) languageID = Constants.FR_LANG_ID;
      else languageID = Constants.EN_LANG_ID;
    } else {
      // App language is set to French or English
      if (Utils.compareStrings(mUserData.getLanguageID(), Constants.FR_LANG_ID))
        languageID = Constants.FR_LANG_ID;
      else languageID = Constants.EN_LANG_ID;
    }

    ShoppingItem shoppingItem = mWidgetListRows.get(position).getShoppingItem();

    if (position == MAX_ITEMS) {
      // View more
      RemoteViews widgetItem = new RemoteViews(mContext.getPackageName(),
          getWidgetItemLayout(false, position));

      widgetItem.setTextViewText(R.id.widget_view_more_text_view,
          mContext.getResources().getString(Utils.compareStrings(languageID, Constants.FR_LANG_ID) ?
              R.string.widget_view_more_fr : R.string.widget_view_more_en));

      // Set click intent
      Intent fillIntent = new Intent();
      fillIntent.putExtra(EXTRA_WIDGET_IS_MANUAL_ITEM, false);
      widgetItem.setOnClickFillInIntent(R.id.widget_view_more_layout, fillIntent);

      return widgetItem;
    } else if (mWidgetListRows.get(position).getType() == ShoppingListRow.ITEM) {
      // Shopping list item
      RemoteViews widgetItem = new RemoteViews(mContext.getPackageName(),
          getWidgetItemLayout(false, position));

      // Set title
      widgetItem.setTextViewText(R.id.widget_title_text_view, Utils.getItemTitle(
          shoppingItem.getItem(), languageID));

      // Set store
      widgetItem.setTextViewText(R.id.widget_store_text_view, mWidgetListRows.get(position)
          .getStore().getStoreName());

      // Set price
      String price = Utils.getPriceAsText(shoppingItem.getItem(), languageID);
      if (Utils.isValidString(price)) {
        widgetItem.setTextViewText(R.id.widget_price_text_view, price);
        widgetItem.setViewVisibility(R.id.widget_price_text_view, View.VISIBLE);
      } else {
        widgetItem.setViewVisibility(R.id.widget_price_text_view, View.INVISIBLE);
      }

      // Get postal match boolean
      boolean doPostalsMatch = Utils.compareStrings(formatPostalCode(shoppingItem.getPostalCode()),
          formatPostalCode(Utils.getPostalCode()));

      // Get flyer
      Flyer flyer = mFlyerDao.queryForId(shoppingItem.getItem().getFlyerID());

      // Set tag
      String tag = Utils.getShoppingItemTag(mContext.getResources(), shoppingItem,
          flyer, doPostalsMatch, languageID);
      boolean showTag = Utils.isValidString(tag);
      if (showTag) {
        widgetItem.setTextViewText(R.id.widget_tag_text_view, tag);
        widgetItem.setTextColor(R.id.widget_tag_text_view, getTagColour(mContext, tag, languageID));
      }
      widgetItem.setViewVisibility(R.id.widget_tag_text_view, showTag ? View.VISIBLE : View.GONE);

      // Get item image Url
      final String imageUrl = ImageUtils.ImageType.ITEM.getUrl(
          shoppingItem.getItem().getItemID(),true, shoppingItem.getItem().getAssetVersion(),
          shoppingItem.getItem().getItemFocusAssetUrl());

      // Set item image
      boolean showImage = Utils.isValidString(imageUrl);
      if (showImage) {
        // Picasso request
        try {
          Bitmap itemBitmap = mPicassoUtils.getPicasso().load(imageUrl).get();

          // Need mutable bitmap to set grey colour filter
          Bitmap mutableItemBitmap = itemBitmap.copy(Bitmap.Config.ARGB_8888, true);
          Paint p = new Paint();
          ColorMatrixColorFilter filter = Utils.getGreyColourFilter(
              Utils.getStatusID(mContext.getResources(), shoppingItem, flyer, doPostalsMatch));
          p.setColorFilter(filter);
          Canvas canvas = new Canvas(mutableItemBitmap);
          canvas.drawBitmap(mutableItemBitmap, 0, 0, p);
          widgetItem.setImageViewBitmap(R.id.widget_item_image_view, mutableItemBitmap);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      widgetItem.setViewVisibility(R.id.widget_item_image_view, showImage ? View.VISIBLE :
          View.INVISIBLE);

      // Set store divider
      widgetItem.setViewVisibility(R.id.widget_store_divider, isDividerNeeded(position) ?
          View.VISIBLE : View.GONE);

      // Set click intent
      Intent fillIntent = new Intent();
      fillIntent.putExtra(EXTRA_WIDGET_IS_MANUAL_ITEM, false);
      fillIntent.putExtra(M_FLYER_ID_EXTRA, shoppingItem.getItem().getFlyerID());
      fillIntent.putExtra(M_IS_FROM_ITEM_EXTRA, true);
      fillIntent.putExtra(M_ITEM_EXTRA, shoppingItem.getItem());
      fillIntent.putExtra(M_SOURCE_ID_EXTRA, Constants.SRC_SHOPPING_LIST);
      fillIntent.putExtra(EXTRA_STATUS_ID, Utils.getStatusID(mContext.getResources(), shoppingItem,
          flyer, doPostalsMatch));
      widgetItem.setOnClickFillInIntent(R.id.widget_item_layout, fillIntent);

      return widgetItem;
    } else {
      // manual item
      RemoteViews widgetItem = new RemoteViews(mContext.getPackageName(),
          getWidgetItemLayout(true, position));

      // Set title
      widgetItem.setTextViewText(R.id.widget_title_text_view, shoppingItem.getManualItem()
          .getTitle());

      // Set Store
      if (Utils.compareStrings(mWidgetListRows.get(position).getStore().getStoreName(),
          mContext.getString(R.string.my_list_en)) || Utils.compareStrings(mWidgetListRows
          .get(position).getStore().getStoreName(), mContext.getString(R.string.my_list_fr))) {
        widgetItem.setViewVisibility(R.id.widget_store_text_view, View.GONE);
      } else {
        widgetItem.setTextViewText(R.id.widget_store_text_view, mWidgetListRows.get(position)
            .getStore().getStoreName());
        widgetItem.setViewVisibility(R.id.widget_store_text_view, View.VISIBLE);
      }

      // Set store divider
      widgetItem.setViewVisibility(R.id.widget_store_divider, isDividerNeeded(position) ?
          View.VISIBLE : View.GONE);

      // Set click intent
      Intent fillIntent = new Intent();
      fillIntent.putExtra(EXTRA_WIDGET_IS_MANUAL_ITEM, true);
      fillIntent.putExtra(M_UUID_EXTRA, shoppingItem.getManualItem().getManualItemUUID());
      fillIntent.putExtra(M_ITEM_TITLE_EXTRA, shoppingItem.getManualItem().getTitle());
      fillIntent.putExtra(M_USER_ACTION_EXTRA, SAVE_ACTION);
      widgetItem.setOnClickFillInIntent(R.id.widget_manual_item_layout, fillIntent);

      return widgetItem;
    }
  }

  @Override
  public RemoteViews getLoadingView() {
    return null;
  }

  @Override
  public int getViewTypeCount() {
    return 3;
  }

  @Override
  public int getCount() {
    return mWidgetListRows.size();
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  @Override
  public void onDataSetChanged() {
    getShoppingListItems();
  }

  @Override
  public void onDestroy() {
    OpenHelperManager.releaseHelper();
    mDatabaseHelper = null;
  }

  /* *********************
   * CONVENIENCE METHODS
   * ******************* */

  /**
   * Updates widget rows to match current shopping list rows
   */
  private void getShoppingListItems() {
    List<ShoppingListRow> widgetRows = new ArrayList<>();
    List<ShoppingListRow> shoppingListRows = mShoppingListModel.getRows();

    Utils.d(TAG, "Size of shopping list rows: " + Integer.toString(shoppingListRows.size()));

    for (ShoppingListRow row : shoppingListRows) {
      if (row.getType() != ShoppingListRow.HEADER && row.getType() != ShoppingListRow.FOOTER
          && widgetRows.size() <= MAX_ITEMS) {
        widgetRows.add(row);
      }
    }

    Utils.d(TAG, "Size of widget rows: " + Integer.toString(widgetRows.size()));

    mWidgetListRows = widgetRows;
  }

  /**
   * Determines if a store divider is needed to separate items
   *
   * @param position position of widget item
   * @return whether or not a store divider is needed
   */
  private boolean isDividerNeeded(int position) {
    return position != 0 && !(Utils.compareStrings(
        mWidgetListRows.get(position - 1).getStore().getStoreName(),
        mWidgetListRows.get(position).getStore().getStoreName()));
  }

  /**
   * Returns layout resource id for widget item (implemented in subclasses)
   *
   * @param isManualItem if item is a manual item
   * @return layout resource id
   */
  public abstract int getWidgetItemLayout(boolean isManualItem, int position);

  /**
   * Returns tag colour for list item
   *
   * @param context application context
   * @param tag item tag
   * @param languageID language ID
   * @return colour resource for item tag
   */
  public int getTagColour(Context context, String tag, String languageID) {
    return Utils.getTagUrgencyTextColour(mContext, tag, languageID);
  }

}