import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Custom Item View
 */
public class CustomItemView extends RelativeLayout {

  /* *****************
   * INTERFACE METHODS
   * *************** */

  public interface CustomItemCallback {
    void onItemSelected(String itemTitle);
  }

  /* *****************
   * CLASS VARIABLES
   * *************** */

  private int mPosition;
  private String mItemTitle;
  private boolean mIsSelected;
  private CustomItemCallback mItemCallback;
  private ImageView mSelectedImageView;
  private TextView mItemTitleTextView;

  /* ****************
   * CONSTRUCTORS
   * ************** */

  public CustomItemView(Context context) {
    this(context, null);
  }

  public CustomItemView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CustomItemView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize();
  }

  /* ********************
   * CONVENIENCE METHODS
   * ****************** */

  /**
   * Initializes private fields and sets onClickListener
   */
  public void initialize() {
    View view = inflate(getContext(), R.layout.view_selectable_item, this);
    mSelectedImageView = (ImageView) view.findViewById(R.id.selected_image_view);
    mItemTitleTextView = (TextView) view.findViewById(R.id.title_text_view);

    view.findViewById(R.id.selectable_item_layout).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mItemCallback.onItemSelected(mItemTitle);
      }
    });
  }

  /**
   * Binds the item title and ID to the view and sets UI
   *
   * @param itemTitle title of item
   * @param position  model position
   */
  public void bind(String itemTitle, int position, boolean isSelected) {
    LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    this.setLayoutParams(lp);

    mItemTitle = itemTitle;
    mPosition = position;
    mIsSelected = isSelected;

    mItemTitleTextView.setHorizontallyScrolling(false);
  }

  /**
   * Resets the view when the view is recycled
   */
  public void resetView() {
    mItemTitleTextView.setText(mItemTitle);
    mSelectedImageView.setVisibility(mPosition == 0 && mIsSelected ? VISIBLE : INVISIBLE);
  }

  /**
   * Sets item callback
   */
  public void setItemCallback(CustomItemCallback callback) {
    mItemCallback = callback;
  }
}