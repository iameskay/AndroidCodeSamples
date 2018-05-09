import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import static android.widget.LinearLayout.LayoutParams;

/**
 * This dialog is replacing a spinner
 */
public class CustomItemDialogFragment extends DialogFragment implements CustomItemCallback {

  /* ******************
   * INTERFACE METHODS
   * **************** */

  @Override
  public void onItemSelected(String itemTitle) {
    mItemTitle = itemTitle;

    if (getActivity() != null) {
      ((CustomActivity)  getActivity()).onItemSelected(itemTitle);
    }

    safeDismiss();
  }

  /* ******************
   * CLASS VARIABLES
   * **************** */

  public static final String TAG = CustomItemDialogFragment.class.getSimpleName();

  /**
   * Singleton classes
   */
  private CustomItemAdapter mCustomItemAdapter;

  /**
   * Dimension Res
   */
  private float mDialogWidth;
  private float mItemListHeight;

  /**
   * Fragment Args
   */
  private String mItemTitle;

  /**
   * Instance States
   */
  private RecyclerView mItemRecyclerView;

  /* *********************
   * CONSTRUCTORS
   * ******************* */

  // Empty constructor
  public CustomItemDialogFragment() {}

  /**
   * New instance method that creates dialog and sets arguments
   *
   * @param itemTitle item title
   * @return dialog fragment
   */
  public static CustomItemDialogFragment newInstance(String itemTitle) {
    CustomItemDialogFragment frag = new CustomItemDialogFragment();

    Bundle args = new Bundle();
    args.putString("mItemTitle", itemTitle);
    frag.setArguments(args);

    return frag;
  }

  /* *********************
   * OVERRIDE METHODS
   * ******************* */

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
    getDialog().setCancelable(true);

    return inflater.inflate(R.layout.dialog_custom, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mCustomItemAdapter = new CustomItemAdapter(this);
    mDialogWidth = getResources().getDimension(R.dimen.dialog_width);
    mItemListHeight = getResources().getDimension(R.dimen.dialog_list_height);

    Bundle args = getArguments();
    if (args != null) {
      mItemTitle = args.getString("mItemTitle");
    }

    mItemRecyclerView = (RecyclerView) view.findViewById(R.id.custom_recycler_view);
    setupItemList();
    setupItemListLinearLayout();

    view.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        safeDismiss();
      }
    });
  }

  /**
   * Dialog Resumed
   */
  @Override
  public void onResume() {
    super.onResume();

    try {
      Window window = getDialog().getWindow();

      if (window != null)
        if (mDialogWidth < 0)
          window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        else
          window.setLayout((int) mDialogWidth, LayoutParams.WRAP_CONTENT);
    } catch (NullPointerException e) {
      safeDismiss();
    }
  }

  /**
   * Dialog Paused
   */
  @Override
  public void onPause() {
    super.onPause();
  }


  /* *********************
   * SET UP METHODS
   * ******************* */

  /**
   * Setup Item List
   */
  void setupItemList() {
    // Adding LinearLayoutManager to RecyclerView
    LinearLayoutManager mSelectItemManager = new LinearLayoutManager(getActivity());
    mItemRecyclerView.setLayoutManager(mSelectFlyerManager);

    // Optimizing RecyclerView properties
    mItemRecyclerView.setHasFixedSize(true);

    // Set flag to false so scrollbar can be drawn even with overScrollMode=never
    mItemRecyclerView.setWillNotDraw(false);

    // Scroll bar always visible
    if (Utils.isAtLeastJellyBean()) mItemRecyclerView.setScrollBarFadeDuration(0);

    // Adding adapter to RecyclerView
    mItemRecyclerView.setAdapter(mCustomItemAdapter);
    mCustomItemAdapter.setItemRows(mItemListModel.getItemTitles(mItemTitle),
        Utils.isValidString(mItemTitle));
    mSelectItemAdapter.notifyDataSetChanged();

    // Adding listeners to RecyclerView
    mItemRecyclerView.addOnChildAttachStateChangeListener(
        new RecyclerView.OnChildAttachStateChangeListener() {
          @Override
          public void onChildViewAttachedToWindow(View view) {
            ((CustomItemView) view).resetView();
          }

          @Override
          public void onChildViewDetachedFromWindow(View view) {
            ((CustomItemView) view).resetView();
          }
        });
  }

  /**
   * Setup the item list linear layout height
   */
  void setupItemListLinearLayout() {
    if (mCustomItemAdapter.getItemCount() > 2)
      mItemRecyclerView.post(new Runnable() {
        @Override
        public void run() {
          LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, (int) mItemListHeight);
          mItemRecyclerView.setLayoutParams(lp);
        }
      });
  }

  /* *********************
   * CONVENIENCE METHODS
   * ******************* */

  /**
   * Safely dismiss dialog
   */
  private void safeDismiss() {
    try {
      dismiss();
    } catch (IllegalStateException e) {
      dismissAllowingStateLoss();
    }
  }
}
