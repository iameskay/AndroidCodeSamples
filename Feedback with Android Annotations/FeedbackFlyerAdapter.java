import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

/**
 * Adapter for feedback flyer
 */
@EBean
public class FeedbackFlyerAdapter extends RecyclerViewBaseAdapter<View> {

  /* *********************
   * CLASS VARIABLES
   * ******************* */

  private boolean mIsSelected;
  private List<String> mFlyerRows;

  /**
   * Root Context
   */
  @RootContext
  Context mContext;

  /* *********************
   * OVERRIDE METHODS
   * ******************* */

  /**
   * Creates the view based on the type
   *
   * @param parent   parent view
   * @param viewType view of the item
   * @return correct view based on type
   */
  @Override
  protected View onCreateItemView(ViewGroup parent, int viewType) {
    return FeedbackFlyerView_.build(mContext);
  }

  /**
   * Binds the view holder with the view
   *
   * @param viewHolder contains the view type for the row
   * @param position   position in the list
   */
  @Override
  public void onBindViewHolder(@NonNull RecyclerViewWrapper<View> viewHolder, int position) {
    FeedbackFlyerView view = (FeedbackFlyerView) viewHolder.getView();
    view.bind(position, mFlyerRows.get(position), mIsSelected);
  }

  /**
   * Number of items in the list
   *
   * @return size of list
   */
  @Override
  public int getItemCount() {
    return (mFlyerRows != null ? mFlyerRows.size() : 0);
  }

  /* *********************
   * SETTERS
   * ******************* */

  /**
   * Sets the flyer rows for the adapter
   *
   * @param isSelected whether or not a previous flyer had been selected
   * @param flyerRows  list of flyers titles in the current and upcoming bookshelves
   */
  public void setFlyerRows(boolean isSelected, List<String> flyerRows) {
    mIsSelected = isSelected;
    mFlyerRows = flyerRows;
  }
}