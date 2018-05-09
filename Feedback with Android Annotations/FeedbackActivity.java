import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DimensionRes;
import org.androidannotations.annotations.res.StringArrayRes;
import org.androidannotations.ormlite.annotations.OrmLiteDao;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that is used to represent the FeedbackActivity
 */
// TODO: Generated class is registered
@SuppressLint("Registered")
@EActivity(R.layout.activity_feedback)
@OptionsMenu(R.menu.menu_feedback)
public class FeedbackActivity extends BaseActivity {

  /* *********************
   * CLASS VARIABLES
   * ******************* */

  public static final String TAG = FeedbackActivity.class.getSimpleName();

  public enum FeedbackType {
    GENERAL_FEEDBACK,
    BUG_REPORT,
    FEATURE_REQUEST,
    FLYER_ISSUE,
    REQUEST_A_STORE
  }

  private boolean mShouldFinish = false;

  /**
   * Beans
   */
  @Bean
  BookshelfModel mBookshelfModel;
  @Bean
  EventLoggingService mEventLoggingService;
  @Bean
  CustomJobManager mCustomJobManager;
  @Bean
  UserData mUserData;

  /**
   * Dimension Res
   */
  @DimensionRes(R.dimen.plane_width)
  float mPlaneImageViewWidth;

  /**
   * Drawable Res
   */
  Drawable mIcSend;
  Drawable mIcSendDisabled;

  /**
   * Extras
   */
  @Extra
  @InstanceState
  FeedbackType mFeedbackType;
  @Extra
  @InstanceState
  Long mFlyerID;
  @Extra
  @InstanceState
  String mFlyerTitle;

  /**
   * Instance States
   */
  @InstanceState
  boolean mIsAnimationRunning;
  @InstanceState
  boolean mIsFlyerIssue;

  /**
   * Options Menu Items
   */
  @OptionsMenuItem(R.id.send_options_item)
  MenuItem mSendMenuItem;

  /**
   * OrmLite DAOs
   */
  @OrmLiteDao(helper = DatabaseHelper.class)
  RuntimeExceptionDao<Flyer, Long> mFlyerDao;
  @OrmLiteDao(helper = DatabaseHelper.class)
  RuntimeExceptionDao<Store, Long> mStoreDao;

  /**
   * String Res
   */
  @StringArrayRes(R.array.feedback_types)
  String[] mFeedbackTypes;
  @StringArrayRes(R.array.standardized_feedback_types)
  String[] mStandardizedFeedbackTypes;

  /**
   * System Services
   */
  @SystemService
  InputMethodManager mInputManager;

  /**
   * Views By ID
   */
  @ViewById(R.id.content_layout)
  ScrollView mContentLayout;
  @ViewById(R.id.toolbar)
  Toolbar mToolbar;

  // Layouts
  @ViewById(R.id.basic_feedback_layout)
  LinearLayout mBasicFeedbackLayout;
  @ViewById(R.id.request_a_store_feedback_layout)
  LinearLayout mRequestAStoreFeedbackLayout;
  @ViewById(R.id.flyer_issue_feedback_layout)
  LinearLayout mFlyerIssueFeedbackLayout;

  // Rate
  @ViewById(R.id.rate_layout)
  LinearLayout mRateLayout;
  @ViewById(R.id.rate_text_view)
  TextView mRateTextView;

  // Feedback type
  @ViewById(R.id.feedback_type_layout)
  LinearLayout mFeedbackTypeLayout;
  @ViewById(R.id.feedback_type_text_view)
  TextView mFeedbackTypeTextView;
  @ViewById(R.id.feedback_type_drop_down_image_view)
  ImageView mFeedbackTypeDropDownImageView;

  // Select flyer
  @ViewById(R.id.feedback_flyer_layout)
  LinearLayout mFeedbackFlyerLayout;
  @ViewById(R.id.feedback_flyer_text_view)
  TextView mFeedbackFlyerTextView;
  @ViewById(R.id.feedback_flyer_drop_down_image_view)
  ImageView mFeedbackFlyerDropDownImageView;

  // General feedback
  @ViewById(R.id.name_edit_text)
  TextInputEditText mNameEditText;
  @ViewById(R.id.email_edit_text)
  TextInputEditText mEmailEditText;
  @ViewById(R.id.body_edit_text)
  TextInputEditText mBodyEditText;

  // Request a store
  @ViewById(R.id.store_name_edit_text)
  TextInputEditText mStoreNameEditText;
  @ViewById(R.id.store_disclaimer_image_view)
  ImageView mStoreDisclaimerImageView;

  // Send Animation
  @ViewById(R.id.thanks_layout)
  LinearLayout mThanksLayout;
  @ViewById(R.id.plane_image_view)
  ImageView mPlaneImageView;
  @ViewById(R.id.merci_image_view)
  ImageView mMerciImageView;
  @ViewById(R.id.thanks_image_view)
  ImageView mThanksImageView;

  /* *********************
   * OVERRIDE METHODS
   * ******************* */

  /**
   * Activity Created
   *
   * @param savedInstanceState a mapping from String keys to various values
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTheme(Utils.getActivityTheme());

    if (savedInstanceState == null)
      mIsFlyerIssue = mFeedbackType != null && mFeedbackType == FLYER_ISSUE;
  }

  /**
   * Activity Resumed
   */
  @Override
  public void onResume() {
    super.onResume();
    EventBus.getDefault().register(this);

    // Account error
    if (mUserData.getAccountError()) {
      mShouldFinish = true;
      onBackPressed();
    }
  }

  /**
   * Activity Paused
   */
  @Override
  public void onPause() {
    EventBus.getDefault().unregister(this);
    keyboardVisibility(false);
    super.onPause();
  }

  /**
   * Prepares Toolbar menu
   *
   * @param menu Toolbar menu
   * @return prepare the screen's standard options menu to be displayed
   */
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    setSendIconState();
    return super.onPrepareOptionsMenu(menu);
  }

  /**
   * This is wrapped in a try catch in case of an illegal state exception, will call finish()
   */
  @Override
  public void onBackPressed() {
    // Animation is running so don't allow user to finish activity
    if (!mShouldFinish && mIsAnimationRunning) return;
    try {
      super.onBackPressed();
    } catch (IllegalStateException e) {
      finish();
    }
  }

  /* *********************
   * OPTIONS ITEMS
   * ******************* */

  /**
   * Calls to onBackPressed()
   */
  @OptionsItem(android.R.id.home)
  void onHomeOptionsItemClick() {
    keyboardVisibility(false);
    onBackPressed();
  }

  /**
   * Submits user feedback if valid, else displays error
   */
  @OptionsItem(R.id.send_options_item)
  void onSendOptionsItemClick() {
    if (mFeedbackType == REQUEST_A_STORE) {
      String storeName = Utils.removeWhiteSpace(mStoreNameEditText.getText().toString());

      // Does query match an existing store
      if (doesStoreExist(storeName))
        launchStoreMatchDialog();
      else
        sendRequestAStore(storeName);
    } else {
      String body = mBodyEditText.getText().toString().trim();
      String name = mNameEditText.getText().toString().trim();
      String email = mEmailEditText.getText().toString().trim();

      if (TextUtils.isEmpty(name)) name = null;
      if (TextUtils.isEmpty(email)) email = null;
      if (email != null && !Utils.isValidEmail(email)) {
        mEmailEditText.requestFocus();
        mEmailEditText.setError(getResources().getString(R.string.invalid_email));
        return;
      }

      if (email == null) launchUserActionDialog(FEEDBACK_NO_EMAIL);
      else sendFeedback(body, name, email);
    }
  }

  /* *********************
   * METHODS AFTER VIEWS ARE OBTAINED
   * ******************* */

  /**
   * Setup the activity theme based on user settings (day or night)
   */
  @AfterViews
  void setupTheme() {
    getWindow().setBackgroundDrawableResource(Utils.getForegroundColour());
  }

  /**
   * Setup Toolbar, and home button
   */
  @AfterViews
  void setupToolbar() {
    mToolbar.setTitle("");
    setSupportActionBar(mToolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    mToolbar.setTitle(getResources().getString(R.string.category_feedback));

    setSendIconState();
  }

  /**
   * Setup the user input fields
   */
  @AfterViews
  void setupEditTexts() {
    // Disable Emoji input
    mNameEditText.setFilters(new InputFilter[]{Utils.sEmojiFilter});
    mEmailEditText.setFilters(new InputFilter[]{Utils.sEmojiFilter});
    mBodyEditText.setFilters(new InputFilter[]{Utils.sEmojiFilter});

    // Requires Integer.MAX_VALUE for multiline EditText and custom IME Action (Done)
    mBodyEditText.setHorizontallyScrolling(false);
    mBodyEditText.setMaxLines(Integer.MAX_VALUE);

    // Setup text change listener
    mBodyEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // TODO: Currently not needed for our implementation
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO: Currently not needed for our implementation
      }

      @Override
      public void afterTextChanged(Editable s) {
        // Set Send button UI state
        setSendIconState();
      }
    });

    // Set up editor change (IME) listener to dismiss keyboard on 'Done'
    mBodyEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionID, KeyEvent event) {
        if (actionID == EditorInfo.IME_ACTION_DONE)
          keyboardVisibility(false);
        return false;
      }
    });

    // Disable Emoji input
    mStoreNameEditText.setFilters(new InputFilter[]{Utils.sEmojiFilter});

    // Setup text change listener
    mStoreNameEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // TODO: Currently not needed for our implementation
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO: Currently not needed for our implementation
      }

      @Override
      public void afterTextChanged(Editable s) {
        // Set Send button UI state
        setSendIconState();
      }
    });

    // Set up editor change (IME) listener to dismiss keyboard on 'Done'
    mStoreNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionID, KeyEvent event) {
        if (actionID == EditorInfo.IME_ACTION_DONE)
          keyboardVisibility(false);
        return false;
      }
    });
  }

  /**
   * Setup the activity UI
   */
  @AfterViews
  void setupUI() {
    // Disable rate layout if entering from a flyer
    mRateLayout.setClickable(!mIsFlyerIssue);

    // Gets the app store name
    String appStoreName = Utils.getAppstoreName(getResources());
    if (Utils.isValidString(appStoreName))
      mRateTextView.setText(getResources().getString(R.string.feedback_rate,
          appStoreName));
    else
      mRateTextView.setText(getResources().getString(R.string.feedback_rate_fallback));

    // Populate name and email fields if user is logged in
    if (mUserData.getAuthenticated()) {
      mNameEditText.setText(mUserData.getUserName());
      mEmailEditText.setText(mUserData.getUserEmail());
    }

    // If entering from flyer (FLYER_ISSUE) disable ability to modify feedback type and flyer
    mFeedbackTypeLayout.setClickable(!mIsFlyerIssue);
    mFeedbackTypeDropDownImageView.setVisibility(mIsFlyerIssue ? View.GONE : View.VISIBLE);
    mFeedbackFlyerLayout.setClickable(!mIsFlyerIssue);
    mFeedbackFlyerDropDownImageView.setVisibility(mIsFlyerIssue ? View.GONE : View.VISIBLE);

    // Set colour filters
    mStoreDisclaimerImageView.setColorFilter(Utils.getIconColourFilter());
    mStoreDisclaimerImageView.setAlpha(Utils.getTransparentIconColourFilter());
    if (!mIsFlyerIssue) {
      mFeedbackTypeDropDownImageView.setColorFilter(Utils.getIconColourFilter());
      mFeedbackFlyerDropDownImageView.setColorFilter(Utils.getIconColourFilter());
    }
  }

  /**
   * Resets UI based on feedback type
   */
  @AfterViews
  void resetUI() {
    // Set UI state of flyer issue feedback layout
    mFlyerIssueFeedbackLayout.setVisibility(mFeedbackType == FLYER_ISSUE
        ? View.VISIBLE : View.GONE);
    mFeedbackFlyerTextView.setText(mFlyerTitle);

    // Set UI state of request a store feedback layout
    mRequestAStoreFeedbackLayout.setVisibility(mFeedbackType == REQUEST_A_STORE
        ? View.VISIBLE : View.GONE);

    // Set UI state of basic feedback layout
    mBasicFeedbackLayout.setVisibility(isBasicFeedback()
        || (mFeedbackType == FLYER_ISSUE && Utils.isValidString(mFlyerTitle))
        ? View.VISIBLE : View.GONE);

    if (mFeedbackType == null) mFeedbackTypeTextView.setText("");
    else mFeedbackTypeTextView.setText(mFeedbackTypes[mFeedbackType.ordinal()]);
  }

  /**
   * Reshow the thanks animation if the user rotates the screen
   */
  @AfterViews
  void reRunAnimation() {
    if (mIsAnimationRunning) showThanksContainer();
  }

  /* *********************
   * SUBSCRIBERS
   * ******************* */

  /**
   * Subscriber to the event bus for AccountErrorEvent
   * User's session has been invalidated
   *
   * @param event object holding event data
   */
  // TODO: Event subscription method
  @SuppressWarnings("unused")
  @Subscribe(threadMode = ThreadMode.POSTING)
  public void onAccountErrorEvent(AccountErrorEvent event) {
    mUserData.setAccountError(true);
    finish();
  }

  /**
   * Subscriber to the event bus for FavouriteStoreSyncEvent
   *
   * @param event object holding event data
   */
  // TODO: Event subscription method
  @SuppressWarnings("unused")
  @Subscribe(threadMode = ThreadMode.POSTING)
  public void onFavouriteStoreSyncEvent(FavouriteStoreSyncEvent event) {
    mUserData.setRefreshFavourites(true);
  }

  /**
   * Subscriber to the event bus for FlyerListRefreshEvent
   *
   * @param event object holding event data
   */
  // TODO: Event subscription method
  @SuppressWarnings("unused")
  @Subscribe(threadMode = ThreadMode.POSTING)
  public void onFlyerListRefreshEvent(FlyerListRefreshEvent event) {
    mUserData.setRefreshFlyers(true);
  }

  /**
   * Subscriber to the event bus for FeedbackFlyerSelectedEvent
   *
   * @param event object holding event data
   */
  // TODO: Event subscription method
  @SuppressWarnings("unused")
  @Subscribe(threadMode = ThreadMode.POSTING)
  public void onFeedbackFlyerSelectedEvent(FeedbackFlyerSelectedEvent event) {
    mFlyerTitle = event.getFlyerTitle();
    mFlyerID = null;
    resetUI();
  }

  /**
   * Subscriber to the event bus for FeedbackTypeSelectedEvent
   *
   * @param event object holding event data
   */
  // TODO: Event subscription method
  @SuppressWarnings("unused")
  @Subscribe(threadMode = ThreadMode.POSTING)
  public void onFeedbackTypeSelectedEvent(FeedbackTypeSelectedEvent event) {
    // Reset feedback state if feedback type is changing
    FeedbackType feedbackType = event.getFeedbackType();
    if (mFeedbackType != feedbackType) {
      mFlyerID = null;
      mFlyerTitle = "";
      mBodyEditText.setText("");
      mStoreNameEditText.setText("");
    }

    mFeedbackType = event.getFeedbackType();
    mFeedbackTypeTextView.setText(mFeedbackTypes[mFeedbackType.ordinal()]);
    resetUI();
  }

  /**
   * Subscriber to the event bus for LanguageEvent
   *
   * @param event object holding event data
   */
  // TODO: Event subscription method
  @SuppressWarnings("unused")
  @Subscribe(threadMode = ThreadMode.POSTING)
  public void onLanguageEvent(LanguageEvent event) {
    resetUI();
  }

  /**
   * Subscriber to the event bus for UserActionEvent
   * Responds to user's selection from a confirm action dialog either positive or negative
   *
   * @param event object holding event data
   */
  // TODO: Event subscription method
  @SuppressWarnings("unused")
  @Subscribe(threadMode = ThreadMode.POSTING)
  public void onUserActionEvent(UserActionEvent event) {
    UserActionState state = event.getState();

    if (event.isAction() && state == FEEDBACK_NO_EMAIL) {
      // Sending without email
      sendFeedback(mBodyEditText.getText().toString().trim(),
          mNameEditText.getText().toString().trim(), mEmailEditText.getText().toString().trim());
    } else if (state == FEEDBACK_NO_EMAIL) {
      // User decided to enter email
      // Show the keyboard with delay (Otherwise it won't show)
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          mEmailEditText.requestFocus();
          keyboardVisibility(true);
        }
      }, Constants.DELAY_ACTION);
    } else if (event.isAction() && state == REQUEST_A_STORE_MATCH) {
      Intent intent = SearchActivity_.intent(this)
          .mSearchQuery(mStoreNameEditText.getText().toString().trim())
          .mCloseOnSearch(false)
          .get();
      startActivity(intent);
      finish();
    } else if (state == REQUEST_A_STORE_MATCH) {
      // Send request a store
      sendRequestAStore(mStoreNameEditText.getText().toString().trim());
    }
  }

  /* *********************
   * CLICK LISTENERS
   * ******************* */

  /**
   * Handles a click on the feedback flyer layout
   * Launches FeedbackFlyerDialog
   */
  @Click(R.id.feedback_flyer_layout)
  void onFeedbackFlyerLayoutClick() {
    keyboardVisibility(false);
    launchFeedbackFlyerDialog();
  }

  /**
   * Handles a click on the feedback type layout
   * Launches FeedbackTypeDialog
   */
  @Click(R.id.feedback_type_layout)
  void onFeedbackTypeLayoutClick() {
    keyboardVisibility(false);
    launchFeedbackTypeDialog();
  }

  /**
   * Handles a click on the rate layout
   */
  @Click(R.id.rate_layout)
  void onRateTextViewClick() {
    keyboardVisibility(false);
    mUserData.setShowRateDialog();
    Utils.createRateIntent(this);
  }

  /* *********************
   * DIALOGS
   * ******************* */

  /**
   * Builds and shows the feedback flyer dialog
   */
  private void launchFeedbackFlyerDialog() {
    Fragment frag = getSupportFragmentManager().findFragmentByTag(FeedbackFlyerDialogFragment.TAG);
    if (!FeedbackFlyerDialogFragment.class.isInstance(frag)) {
      FeedbackFlyerDialogFragment feedbackFlyerDialog = FeedbackFlyerDialogFragment_.builder()
          .mFlyerTitle(mFlyerTitle)
          .mFlyerID(mFlyerID)
          .build();
      try {
        feedbackFlyerDialog.show(getSupportFragmentManager(), FeedbackFlyerDialogFragment.TAG);
      } catch (IllegalStateException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Builds and shows the feedback type dialog
   */
  private void launchFeedbackTypeDialog() {
    Fragment frag = getSupportFragmentManager().findFragmentByTag(FeedbackTypeDialogFragment.TAG);
    if (!FeedbackTypeDialogFragment.class.isInstance(frag)) {
      FeedbackTypeDialogFragment feedbackTypeDialog = FeedbackTypeDialogFragment_.builder()
          .mFeedbackType(mFeedbackType)
          .build();
      try {
        feedbackTypeDialog.show(getSupportFragmentManager(), FeedbackTypeDialogFragment.TAG);
      } catch (IllegalStateException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Builds and shows the store match dialog when requesting a store
   */
  private void launchStoreMatchDialog() {
    Fragment frag = getSupportFragmentManager().findFragmentByTag(UserActionDialogFragment.TAG);
    if (!UserActionDialogFragment.class.isInstance(frag)) {
      UserActionDialogFragment storeExistsDialog = UserActionDialogFragment_.builder()
          .mState(REQUEST_A_STORE_MATCH)
          .build();
      keyboardVisibility(false);
      try {
        storeExistsDialog.show(getSupportFragmentManager(), UserActionDialogFragment.TAG);
      } catch (IllegalStateException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Builds and shows the user action dialog
   *
   * @param state initial state of dialog
   */
  private void launchUserActionDialog(UserActionState state) {
    Fragment frag = getSupportFragmentManager().findFragmentByTag(UserActionDialogFragment.TAG);
    if (!UserActionDialogFragment.class.isInstance(frag)) {
      UserActionDialogFragment userActionDialog = UserActionDialogFragment_.builder()
          .mState(state)
          .build();
      keyboardVisibility(false);
      try {
        userActionDialog.show(getSupportFragmentManager(), UserActionDialogFragment.TAG);
      } catch (IllegalStateException e) {
        e.printStackTrace();
      }
    }
  }

  /* *********************
   * CONVENIENCE METHODS
   * ******************* */

  /**
   * Check if feedback type is 'Basic' (Not null, FLYER_ISSUE or REQUEST_A_STORE)
   */
  private boolean isBasicFeedback() {
    return mFeedbackType != null && mFeedbackType != FLYER_ISSUE
        && mFeedbackType != REQUEST_A_STORE;
  }

  /**
   * Set UI state of send MenuItem
   */
  private void setSendIconState() {
    if (mSendMenuItem == null || mBodyEditText == null || mStoreNameEditText == null) return;

    // Get Drawable from VectorDrawable
    if (mIcSend == null || mIcSendDisabled == null) {
      mIcSend = Utils.getDrawableFromVectorDrawable(this, R.drawable.ic_send);
      mIcSendDisabled = Utils.getDrawableFromVectorDrawable(this, R.drawable.ic_send_disabled);
    }

    // Get feedback body
    String body = mFeedbackType == REQUEST_A_STORE
        ? mStoreNameEditText.getText().toString().trim()
        : mBodyEditText.getText().toString().trim();

    boolean isValid = Utils.isValidString(body);
    mSendMenuItem.setEnabled(isValid);
    mSendMenuItem.setIcon(isValid ? mIcSend : mIcSendDisabled);
  }

  /**
   * Sends user feedback
   *
   * @param body  user's feedback message
   * @param name  user's name (nullable)
   * @param email user's email address (nullable)
   */
  private void sendFeedback(String body, String name, String email) {
    // Disable button to prevent user from double clicking
    mSendMenuItem.setEnabled(false);

    // Hide keyboard and send feedback (data and event)
    keyboardVisibility(false);

    // For safety animation has started at this point
    mIsAnimationRunning = true;

    // Append flyer title and flyer ID(s) to body
    if (mFeedbackType == FLYER_ISSUE) {
      // Get flyer ID(s) to send with feedback body
      String flyerIDs;
      if (mFlyerID == null) flyerIDs = mBookshelfModel.getFlyerIDsFromTitle(mFlyerTitle);
      else flyerIDs = Long.toString(mFlyerID);

      // If entering from flyer do NOT modify mFlyerID, otherwise get mFlyerID if singular (no ',')
      if (!mIsFlyerIssue && !flyerIDs.contains(","))
        mFlyerID = Long.parseLong(flyerIDs);

      body += "\n\n<Store Name: " + mFlyerTitle + ">\n";
      body += "<FlyerIDs: " + flyerIDs + ">\n";
    } else {
      body += "\n\n";
    }

    // Append feedback type to body for filtering
    body += "<" + mStandardizedFeedbackTypes[mFeedbackType.ordinal()] + ">\n\n";

    // Log analytic event
    mEventLoggingService.logEvent(
        new FeedbackSentEvent().putType(mFeedbackType == FLYER_ISSUE
            ? FeedbackSentEvent.Type.REPORT_CONTENT
            : FeedbackSentEvent.Type.FEEDBACK));

    // Send feedback (REPORT_CONTENT needs non null Flyer ID)
    mCustomJobManager.addJobInBackground(
        new SendFeedbackJob(mFeedbackType == FLYER_ISSUE && mFlyerID != null
            ? SendFeedbackJob.FeedbackType.REPORT_CONTENT
            : SendFeedbackJob.FeedbackType.SEND_FEEDBACK,
            body, name, email, mFlyerID));

    // Delay call to fade in "Thanks" container
    // Needed to make sure keyboard has been dismissed
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        showThanksContainer();
      }
    }, Constants.DELAY_ACTION);
  }

  /**
   * Query for flyers and check to see if requested store name matches a store that exists
   *
   * @param storeName user's request a store input
   * @return does the requested store match an existing store
   */
  private boolean doesStoreExist(String storeName) {
    try {
      // Normalize store name
      storeName = Utils.normalizeString(storeName);

      // Get current date
      String curDate = Utils.curDate();

      // Store Query
      QueryBuilder<Store, Long> storeQb = mStoreDao.queryBuilder();
      ArrayList<Long> storeIDs = new ArrayList<>(3);
      storeIDs.add(Constants.ANDROID_STORE_ID);
      storeIDs.add(Constants.IOS_STORE_ID);
      storeIDs.add(Constants.BB_STORE_ID);
      Where<Store, Long> storeWhere = storeQb.where();

      // Creating select arg object
      SelectArg selectArg1 = new SelectArg(storeName + "%");
      SelectArg selectArg2 = new SelectArg("%" + storeName + "%");

      // Query against store name simplified
      storeWhere.like(Store.STORE_NAME_SIMPLIFIED, selectArg1);
      storeWhere.like(Store.STORE_NAME_SIMPLIFIED, selectArg2);

      // ORs previous 2 where statements
      storeWhere.or(2);

      // Query against store ID
      storeWhere.notIn(Store.STORE_ID, storeIDs);

      // ANDs previous 2 where statements
      storeWhere.and(2);

      // Flyer Query
      Where<Flyer, Long> query = mFlyerDao.queryBuilder()
          .join(storeQb)
          .distinct()
          .orderBy(Flyer.PRIORITY, true)
          .where().eq(Flyer.ACTIVE, true)
          .and().ge(Flyer.DATE_EXPIRED, curDate);

      // Query for flyers
      final List<Flyer> flyers = mFlyerDao.query(query.prepare());

      return !flyers.isEmpty();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Sends the request a store information feedback
   * Do not need to append feedback type (Subject for these are unique - "Flyer Request Ticket")
   *
   * @param storeName user's request a store input
   */
  private void sendRequestAStore(final String storeName) {
    // Hide keyboard and send feedback (data and event)
    keyboardVisibility(false);

    // For safety animation has started at this point
    mIsAnimationRunning = true;

    // Log analytic event
    mEventLoggingService.logEvent(new FeedbackSentEvent()
        .putType(FeedbackSentEvent.Type.REQUEST_A_STORE));

    // Send request a store
    mCustomJobManager.addJobInBackground(new SendFeedbackJob(
        SendFeedbackJob.FeedbackType.REQUEST_STORE, storeName));

    // Delay call to fade in "Thanks" container
    // Needed to make sure keyboard has been dismissed
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        showThanksContainer();
      }
    }, Constants.DELAY_ACTION);
  }

  /**
   * Shows the "Thanks!" content
   */
  private void showThanksContainer() {
    mContentLayout.setVisibility(View.GONE);
    mPlaneImageView.setX(-mPlaneImageViewWidth + 1);

    // Show the thanks container
    mThanksLayout.setVisibility(View.VISIBLE);

    // Show thanks or merci based on language ID
    boolean isEnglish = Utils.compareStrings(mUserData.getLanguageID(), Constants.EN_LANG_ID);
    mMerciImageView.setVisibility(isEnglish ? View.GONE : View.VISIBLE);
    mThanksImageView.setVisibility(isEnglish ? View.VISIBLE : View.GONE);

    // Start plane animation with delay
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        planeAnimation();
      }
    }, Constants.DELAY_ACTION);
  }

  /**
   * Start plane animation and finish activity on animation end
   */
  private void planeAnimation() {
    // Create fly animation
    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);
    TranslateAnimation flyAnimation = new TranslateAnimation(-mPlaneImageViewWidth,
        metrics.widthPixels + mPlaneImageViewWidth * 2, 0, 0);
    flyAnimation.setDuration(Constants.FLY_ANIMATION);

    // Fly animation listener
    flyAnimation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
        // TODO: Currently not needed for our implementation
      }

      @Override
      public void onAnimationEnd(Animation animation) {
        // Hide plane after animation has finished
        mPlaneImageView.setVisibility(View.GONE);
        // End activity with delay
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            mIsAnimationRunning = false;
            onBackPressed();
          }
        }, Constants.ACTIVITY_DISMISS);
      }

      @Override
      public void onAnimationRepeat(Animation animation) {
        // TODO: Currently not needed for our implementation
      }
    });

    // Start fly animation
    mPlaneImageView.startAnimation(flyAnimation);
  }

  /**
   * Show or hide keyboard depending on state
   *
   * @param isVisible whether or not the keyboard should be visible
   */
  private void keyboardVisibility(boolean isVisible) {
    if (getCurrentFocus() != null) {
      if (isVisible) {
        mInputManager.showSoftInput(getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
      } else {
        mInputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        getCurrentFocus().clearFocus();
      }
    }
  }
}
