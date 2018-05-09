import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

/**
 * Adapter for feedback type
 */
@EBean
public class FeedbackTypeAdapter extends RecyclerViewBaseAdapter<View> {

  /* *********************
   * CLASS VARIABLES
   * ******************* */

  private boolean mIsSelected;
  private List<FeedbackType> mFeedbackTypes;

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
    return FeedbackTypeView_.build(mContext);
  }

  /**
   * Binds the view holder with the view
   *
   * @param viewHolder contains the view type for the row
   * @param position   position in the list
   */
  @Override
  public void onBindViewHolder(@NonNull RecyclerViewWrapper<View> viewHolder, int position) {
    FeedbackTypeView view = (FeedbackTypeView) viewHolder.getView();
    view.bind(position, mFeedbackTypes.get(position), mIsSelected);
  }

  /**
   * Number of items in the list
   *
   * @return size of list
   */
  @Override
  public int getItemCount() {
    return (mFeedbackTypes != null ? mFeedbackTypes.size() : 0);
  }

  /* *********************
   * SETTERS
   * ******************* */

  /**
   * Sets the feedback type rows for the adapter
   *
   * @param isSelected    whether or not a previous feedback type had been selected
   * @param feedbackTypes list of feedback types
   */
  public void setFeedbackList(boolean isSelected, List<FeedbackType> feedbackTypes) {
    mIsSelected = isSelected;
    mFeedbackTypes = feedbackTypes;
  }
}