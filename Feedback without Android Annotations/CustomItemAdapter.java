import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Adapter for custom views
 */
public class CustomItemAdapter extends RecyclerView.Adapter<CustomItemAdapter.ViewHolder> {

  /* *********************
   * CLASS VARIABLES
   * ******************* */

  private List<String> mItemRows;
  private boolean mIsSelected;
  private CustomItemCallback mItemCallback;

  /**
   * Custom view holder class
   */
  public class ViewHolder extends RecyclerView.ViewHolder {
    private CustomItemView customView;

    private ViewHolder(View v) {
      super(v);
      customView = (CustomItemView) v;
    }

    public CustomItemView getCustomView() {
      return customView;
    }
  }

  /* *********************
   * CONSTRUCTORS
   * ******************* */

  public CustomItemAdapter(CustomFragment fragment) {
    if (fragment instanceof CustomItemCallback)
      mItemCallback = (CustomItemCallback) fragment;
    else
      EventBus.getDefault().post(new DialogSafeDismissEvent());
  }

  /* *********************
   * OVERRIDE METHODS
   * ******************* */

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    CustomItemView itemView = new CustomItemView(parent.getContext());
    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    itemView.setLayoutParams(lp);

    return new ViewHolder(itemView);
  }

  /**
   * Binds the view holder with the view
   *
   * @param viewHolder contains the view type for the row
   * @param position   position in the list
   */
  @Override
  public void onBindViewHolder(ViewHolder viewHolder, int position) {
    viewHolder.getCustomView().bind(mItemRows.get(position), position, mIsSelected);
    viewHolder.getCustomView().setItemCallback(mItemCallback);
  }

  /**
   * Number of items in the list
   *
   * @return size of list
   */
  @Override
  public int getItemCount() {
    return (mItemRows != null ? mItemRows.size() : 0);
  }

  /* *********************
   * SETTERS
   * ******************* */

  /**
   * Sets the item rows for the adapter
   *
   * @param itemRows list of item titles
   */
  public void setItemRows(List<String> itemRows, boolean isSelected) {
    mIsSelected = isSelected;
    mItemRows = itemRows;
  }
}