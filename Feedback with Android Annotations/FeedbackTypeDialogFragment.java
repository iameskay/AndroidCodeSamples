import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;
import android.widget.LinearLayout;

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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static android.widget.LinearLayout.LayoutParams;

/**
 * This dialog is displayed when the user is selecting feedback type
 * (This dialog is used in place of a spinner)
 */
@EFragment(R.layout.dialog_feedback_type)
public class FeedbackTypeDialogFragment extends DialogFragment {

  /* ******************
   * CLASS VARIABLES
   * **************** */

  public static final String TAG = FeedbackTypeDialogFragment.class.getSimpleName();

  /**
   * Beans
   */
  @Bean
  FeedbackTypeAdapter mFeedbackTypeAdapter;

  /**
   * Dimension Res
   */
  @DimensionRes(R.dimen.dialog_width)
  float mDialogWidth;
  @DimensionRes(R.dimen.dialog_list_height)
  float mFeedbackListHeight;

  /**
   * Fragment Args
   */
  @FragmentArg
  @InstanceState
  FeedbackType mFeedbackType;

  /**
   * Views By ID
   */
  @ViewById(R.id.feedback_type_recycler_view)
  RecyclerView mFeedbackTypeRecyclerView;

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
          window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        else
          window.setLayout((int) mDialogWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
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
   * Setup feedback type RecyclerView
   */
  @AfterViews
  void setupFeedbackTypesRecyclerView() {
    // Adding LinearLayoutManager to RecyclerView
    LinearLayoutManager mSelectFeedbackManager = new LinearLayoutManager(getActivity());
    mFeedbackTypeRecyclerView.setLayoutManager(mSelectFeedbackManager);

    // Optimizing RecyclerView properties
    mFeedbackTypeRecyclerView.setHasFixedSize(true);

    // Set flag to false so scrollbar can be drawn even with overScrollMode=never
    mFeedbackTypeRecyclerView.setWillNotDraw(false);

    // Scroll bar always visible
    mFeedbackTypeRecyclerView.setScrollBarFadeDuration(0);

    // Get feedback types and move selected feedback type to first position
    List<FeedbackType> feedbackTypes = new ArrayList<>(EnumSet.allOf(FeedbackType.class));
    if (mFeedbackType != null) {
      feedbackTypes.remove(mFeedbackType.ordinal());
      feedbackTypes.add(0, mFeedbackType);
    }
    mFeedbackTypeAdapter.setFeedbackList(mFeedbackType != null, feedbackTypes);

    // Adding adapter to RecyclerView
    mFeedbackTypeRecyclerView.setAdapter(mFeedbackTypeAdapter);
    mFeedbackTypeAdapter.notifyDataSetChanged();

    // Setup the feedback types RecyclerView height
    if (mFeedbackTypeAdapter.getItemCount() > 2)
      mFeedbackTypeRecyclerView.post(new Runnable() {
        @Override
        public void run() {
          LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, (int) mFeedbackListHeight);
          mFeedbackTypeRecyclerView.setLayoutParams(lp);
        }
      });
  }

  /* *********************
   * SUBSCRIBERS
   * ******************* */

  /**
   * Subscriber to the event bus for FeedbackTypeSelectedEvent
   * Dismisses dialog
   *
   * @param event object holding event data
   */
  // TODO: Event subscription method
  @SuppressWarnings("unused")
  @Subscribe(threadMode = ThreadMode.POSTING)
  public void onFeedbackTypeSelectedEvent(FeedbackTypeSelectedEvent event) {
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