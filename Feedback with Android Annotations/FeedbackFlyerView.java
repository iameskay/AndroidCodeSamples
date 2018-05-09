import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

/**
 * Class that is used to represent the feedback flyer view
 */
// TODO: Common "view_selectable_item" layout
@EViewGroup(R.layout.view_selectable_item)
public class FeedbackFlyerView extends RelativeLayout {

  /* *****************
   * CLASS VARIABLES
   * *************** */

  private String mFlyerTitle;

  /**
   * Views By ID
   */
  @ViewById(R.id.selected_image_view)
  ImageView mSelectedImageView;
  @ViewById(R.id.title_text_view)
  TextView mFlyerTitleTextView;

  /* ****************
   * CONSTRUCTORS
   * ************** */

  public FeedbackFlyerView(Context context) {
    super(context);
  }

  public FeedbackFlyerView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public FeedbackFlyerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  /* ********************
   * CLICK LISTENERS
   * ****************** */

  /**
   * Handles a click on the selectable item layout
   */
  @Click(R.id.selectable_item_layout)
  void onSelectableItemLayoutClick() {
    EventBus.getDefault().post(new FeedbackFlyerSelectedEvent(mFlyerTitle));
  }

  /* ********************
   * CONVENIENCE METHODS
   * ****************** */

  /**
   * Binds the position, flyer title, and is selected to the view
   *
   * @param position   model position
   * @param flyerTitle title of flyer
   * @param isSelected whether or not a previous flyer had been selected
   */
  public void bind(int position, String flyerTitle, boolean isSelected) {
    LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    this.setLayoutParams(lp);

    mFlyerTitle = flyerTitle;

    mFlyerTitleTextView.setHorizontallyScrolling(false);

    mSelectedImageView.setVisibility(position == 0 && isSelected ? VISIBLE : INVISIBLE);
    mFlyerTitleTextView.setText(mFlyerTitle);
  }
}