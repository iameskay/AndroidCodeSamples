import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.greenrobot.eventbus.EventBus;

/**
 * Class that is used to represent the feedback type view
 */
// TODO: Common "view_selectable_item" layout
@EViewGroup(R.layout.view_selectable_item)
public class FeedbackTypeView extends RelativeLayout {

  /* *****************
   * CLASS VARIABLES
   * *************** */

  private FeedbackType mFeedbackType;

  /**
   * String Res
   */
  @StringArrayRes(R.array.feedback_types)
  String[] mFeedbackTypes;

  /**
   * Views By ID
   */
  @ViewById(R.id.selected_image_view)
  ImageView mSelectedImageView;
  @ViewById(R.id.title_text_view)
  TextView mFeedbackTextView;

  /* ****************
   * CONSTRUCTORS
   * ************** */

  public FeedbackTypeView(Context context) {
    super(context);
  }

  public FeedbackTypeView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public FeedbackTypeView(Context context, AttributeSet attrs, int defStyleAttr) {
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
    EventBus.getDefault().post(new FeedbackTypeSelectedEvent(mFeedbackType));
  }

  /* ********************
   * CONVENIENCE METHODS
   * ****************** */

  /**
   * Binds the position and feedback type to the view
   *
   * @param position     model position
   * @param feedbackType feedback type
   * @param isSelected   whether or not a previous feedback type had been selected
   */
  public void bind(int position, FeedbackType feedbackType, boolean isSelected) {
    LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    this.setLayoutParams(lp);

    mFeedbackType = feedbackType;

    mFeedbackTextView.setHorizontallyScrolling(false);

    mSelectedImageView.setVisibility(position == 0 && isSelected ? VISIBLE : INVISIBLE);
    mFeedbackTextView.setText(mFeedbackTypes[mFeedbackType.ordinal()]);
  }
}