import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DimensionRes;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static android.widget.LinearLayout.LayoutParams;

/**
 * This dialog is displayed when the user is selecting a flyer to be submitted with FLYER_ISSUE
 * feedback (This dialog is used in place of a spinner)
 */
@EFragment(R.layout.dialog_feedback_flyer)
public class FeedbackFlyerDialogFragment extends DialogFragment {

  /* ******************
   * CLASS VARIABLES
   * **************** */

  public static final String TAG = FeedbackFlyerDialogFragment.class.getSimpleName();

  /**
   * Beans
   */
  @Bean
  BookshelfModel mBookshelfModel;
  @Bean
  FeedbackFlyerAdapter mFeedbackFlyerAdapter;
  @Bean
  UserData mUserData;

  /**
   * Dimension Res
   */
  @DimensionRes(R.dimen.dialog_width)
  float mDialogWidth;
  @DimensionRes(R.dimen.dialog_list_height)
  float mFlyerListHeight;

  /**
   * Fragment Args
   */
  @FragmentArg
  @InstanceState
  String mFlyerTitle;
  @FragmentArg
  @InstanceState
  Long mFlyerID;

  /**
   * Instance States
   */
  @InstanceState
  String mLanguageID;

  /**
   * Views By ID
   */
  @ViewById(R.id.feedback_flyer_recycler_view)
  RecyclerView mFeedbackFlyerRecyclerView;

  /* *********************
   * OVERRIDE METHODS
   * ******************* */

  /**
   * Dialog Created
   *
   * @param savedInstanceState a mapping from String keys to various values
   * @return created dialog
   */
  @Override
  @NonNull
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setCancelable(true);
    dialog.setCanceledOnTouchOutside(true);

    mLanguageID = mUserData.getLanguageID();

    return dialog;
  }

  /**
   * Dialog Resumed
   */
  @Override
  public void onResume() {
    super.onResume();
    EventBus.getDefault().register(this);

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
    EventBus.getDefault().unregister(this);
    super.onPause();
  }

  /* *********************
   * AFTER VIEWS
   * ******************* */

  /**
   * Setup flyer RecyclerView
   */
  @AfterViews
  void setupFlyerRecyclerView() {
    // Adding LinearLayoutManager to RecyclerView
    LinearLayoutManager feedbackFlyerManager = new LinearLayoutManager(getActivity());
    mFeedbackFlyerRecyclerView.setLayoutManager(feedbackFlyerManager);

    // Optimizing RecyclerView properties
    mFeedbackFlyerRecyclerView.setHasFixedSize(true);

    // Set flag to false so scrollbar can be drawn even with overScrollMode=never
    mFeedbackFlyerRecyclerView.setWillNotDraw(false);

    // Scroll bar always visible
    mFeedbackFlyerRecyclerView.setScrollBarFadeDuration(0);

    // Adding adapter to RecyclerView
    mFeedbackFlyerAdapter.setFlyerRows(Utils.isValidString(mFlyerTitle),
        mBookshelfModel.getFlyerTitles(mFlyerTitle));
    mFeedbackFlyerRecyclerView.setAdapter(mFeedbackFlyerAdapter);
    mFeedbackFlyerAdapter.notifyDataSetChanged();

    // Setup the flyer list RecyclerView height
    if (mFeedbackFlyerAdapter.getItemCount() > 2)
      mFeedbackFlyerRecyclerView.post(new Runnable() {
        @Override
        public void run() {
          LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, (int) mFlyerListHeight);
          mFeedbackFlyerRecyclerView.setLayoutParams(lp);
        }
      });
  }

  /* *********************
   * SUBSCRIBERS
   * ******************* */

  /**
   * Subscriber to the event bus for FeedbackFlyerSelectedEvent
   * Dismisses dialog
   *
   * @param event object holding event data
   */
  // TODO: Event subscription method
  @SuppressWarnings("unused")
  @Subscribe(threadMode = ThreadMode.POSTING)
  public void onFeedbackFlyerSelectedEvent(FeedbackFlyerSelectedEvent event) {
    safeDismiss();
  }

  /* *********************
   * CLICK LISTENERS
   * ******************* */

  /**
   * Handles click on cancel button
   */
  @Click(R.id.cancel_button)
  void onCancelButtonClick() {
    safeDismiss();
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