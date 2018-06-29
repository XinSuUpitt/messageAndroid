package com.messagecube.messaging.ui.mediapicker;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.Explode;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.ex.chips.RecipientEntry;
import com.google.common.annotations.VisibleForTesting;
import com.messagecube.messaging.R;
import com.messagecube.messaging.datamodel.DataModel;
import com.messagecube.messaging.datamodel.action.ActionMonitor;
import com.messagecube.messaging.datamodel.action.GetOrCreateConversationAction;
import com.messagecube.messaging.datamodel.binding.Binding;
import com.messagecube.messaging.datamodel.binding.BindingBase;
import com.messagecube.messaging.datamodel.data.ContactListItemData;
import com.messagecube.messaging.datamodel.data.ContactPickerData;
import com.messagecube.messaging.datamodel.data.ParticipantData;
import com.messagecube.messaging.ui.BugleActionBarActivity;
import com.messagecube.messaging.ui.CustomHeaderPagerViewHolder;
import com.messagecube.messaging.ui.CustomHeaderViewPager;
import com.messagecube.messaging.ui.animation.ViewGroupItemVerticalExplodeAnimation;
import com.messagecube.messaging.ui.contact.AllContactsListViewHolder;
import com.messagecube.messaging.ui.contact.ContactDropdownLayouter;
import com.messagecube.messaging.ui.contact.ContactListItemView;
import com.messagecube.messaging.ui.contact.ContactPickerFragment;
import com.messagecube.messaging.ui.contact.ContactRecipientAdapter;
import com.messagecube.messaging.ui.contact.ContactRecipientAutoCompleteView;
import com.messagecube.messaging.ui.contact.FrequentContactsListViewHolder;
import com.messagecube.messaging.util.Assert;
import com.messagecube.messaging.util.ContactUtil;
import com.messagecube.messaging.util.ImeUtil;
import com.messagecube.messaging.util.LogUtil;
import com.messagecube.messaging.util.OsUtil;
import com.messagecube.messaging.util.PhoneUtils;
import com.messagecube.messaging.util.UiUtils;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by suxin on 10/12/17.
 */

public class VcardActivity  extends BugleActionBarActivity implements ContactPickerData.ContactPickerDataListener,
        ContactListItemView.HostInterface, ContactRecipientAutoCompleteView.ContactChipsChangeListener, Toolbar.OnMenuItemClickListener,
        GetOrCreateConversationAction.GetOrCreateConversationActionListener {

    public static final int MODE_UNDEFINED = 0;

    public static final int MODE_PICK_INITIAL_CONTACT = 1;

    public static final int MODE_CHIPS_ONLY = 2;

    public static final int MODE_PICK_MORE_CONTACTS = 3;

    public static final int MODE_PICK_MAX_PARTICIPANTS = 4;

    public interface VcardActivityHost {
        void onGetOrCreateNewConversation(String conversationId);
        void onBackButtonPressed();
    }

    @VisibleForTesting
    final Binding<ContactPickerData> mBinding = BindingBase.createBinding(this);

    private VcardActivityHost mHost;
    private ContactRecipientAutoCompleteView mRecipientTextView;
    private CustomHeaderViewPager mCustomHeaderViewPager;
    private AllContactsListViewHolder mAllContactsListViewHolder;
    private FrequentContactsListViewHolder mFrequentContactsListViewHolder;
    private View mRootView;
    private View mPendingExplodeView;
    private View mComposeDivider;
    private Toolbar mToolbar;
    private int mContactPickingMode = MODE_UNDEFINED;
    private RecipientEntry recipientEntry;
    private Set<String> mSelectedPhoneNumbers = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vcard_fragment);
        mAllContactsListViewHolder = new AllContactsListViewHolder(this, this);
        mFrequentContactsListViewHolder = new FrequentContactsListViewHolder(this, this);
        mRecipientTextView = (ContactRecipientAutoCompleteView)
                findViewById(R.id.recipient_text_view);
        mRecipientTextView.setThreshold(0);
        mRecipientTextView.setDropDownAnchor(R.id.compose_contact_divider);

        mRecipientTextView.setContactChipsListener(this);
        mRecipientTextView.setDropdownChipLayouter(new ContactDropdownLayouter(this.getLayoutInflater(),
                this, this));
        mRecipientTextView.setAdapter(new ContactRecipientAdapter(this, this));
        mRecipientTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before,
                                      final int count) {
            }

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count,
                                          final int after) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                updateTextInputButtonsVisibility();
            }
        });

        final CustomHeaderPagerViewHolder[] viewHolders = {
                mFrequentContactsListViewHolder,
                mAllContactsListViewHolder };

        mCustomHeaderViewPager = (CustomHeaderViewPager) findViewById(R.id.contact_pager);
        mCustomHeaderViewPager.setViewHolders(viewHolders);
        mCustomHeaderViewPager.setViewPagerTabHeight(CustomHeaderViewPager.DEFAULT_TAB_STRIP_SIZE);
        mCustomHeaderViewPager.setBackgroundColor(getResources()
                .getColor(R.color.contact_picker_background));

        mCustomHeaderViewPager.setCurrentItem(0);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_light);
        mToolbar.setNavigationContentDescription(R.string.back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mHost.onBackButtonPressed();
                finish();
            }
        });

        mToolbar.inflateMenu(R.menu.compose_menu);
        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.setVisibility(View.GONE);

        mComposeDivider = findViewById(R.id.compose_contact_divider);
        mRootView = findViewById(R.id.root);
        if (ContactUtil.hasReadContactsPermission()) {
            mBinding.bind(DataModel.get().createContactPickerData(this, this));
            mBinding.getData().init(getLoaderManager(), mBinding);
        }

        getSupportActionBar().hide();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onMenuItemClick(final MenuItem menuItem) {
        return false;
    }

    @Override
    public void onAllContactsCursorUpdated(final Cursor data) {
        mBinding.ensureBound();
        mAllContactsListViewHolder.onContactsCursorUpdated(data);
    }

    @Override
    public void onFrequentContactsCursorUpdated(final Cursor data) {
        mBinding.ensureBound();
        mFrequentContactsListViewHolder.onContactsCursorUpdated(data);
        if (data != null && data.getCount() == 0) {
            mCustomHeaderViewPager.setCurrentItem(1);
        }
    }

    @Override
    public void onContactListItemClicked(final ContactListItemData item,
                                         final ContactListItemView view) {
        if (!isContactSelected(item)) {
            if (mContactPickingMode == MODE_PICK_INITIAL_CONTACT) {
                mPendingExplodeView = view;
            }
            mRecipientTextView.appendRecipientEntry(item.getRecipientEntry());
            recipientEntry = item.getRecipientEntry();
        } else if (mContactPickingMode != MODE_PICK_INITIAL_CONTACT) {
            mRecipientTextView.removeRecipientEntry(item.getRecipientEntry());
        }
    }

    @Override
    public boolean isContactSelected(final ContactListItemData item) {
        return mSelectedPhoneNumbers != null &&
                mSelectedPhoneNumbers.contains(PhoneUtils.getDefault().getCanonicalBySystemLocale(
                        item.getRecipientEntry().getDestination()));
    }

    public void setHost(final VcardActivityHost host) {
        mHost = host;
    }

    private void showImeKeyboard() {
        Assert.notNull(mRecipientTextView);
        mRecipientTextView.requestFocus();

        UiUtils.doOnceAfterLayoutChange(mRootView, new Runnable() {
            @Override
            public void run() {
                if (getParent() != null) {
                    ImeUtil.get().showImeKeyboard(getParent(), mRecipientTextView);
                }
            }
        });
        mRecipientTextView.invalidate();
    }

    private void updateTextInputButtonsVisibility() {
        final Menu menu = mToolbar.getMenu();
        final MenuItem keypadToggleItem = menu.findItem(R.id.action_ime_dialpad_toggle);
        final MenuItem deleteTextItem = menu.findItem(R.id.action_delete_text);
        if (mContactPickingMode == MODE_PICK_INITIAL_CONTACT) {
            if (TextUtils.isEmpty(mRecipientTextView.getText())) {
                deleteTextItem.setVisible(false);
                keypadToggleItem.setVisible(true);
            } else {
                deleteTextItem.setVisible(true);
                keypadToggleItem.setVisible(false);
            }
        } else {
            deleteTextItem.setVisible(false);
            keypadToggleItem.setVisible(false);
        }
    }

    private void maybeGetOrCreateConversation() {
        final ArrayList<ParticipantData> participants =
                mRecipientTextView.getRecipientParticipantDataForConversationCreation();
        if (ContactPickerData.isTooManyParticipants(participants.size())) {
            UiUtils.showToast(R.string.too_many_participants);
        } else if (participants.size() > 0 && mMonitor == null) {
            mMonitor = GetOrCreateConversationAction.getOrCreateConversation(participants,
                    null, this);
        }
    }

    @Override
    public void onContactChipsChanged(final int oldCount, final int newCount) {

        String contactInfo = "Contact Card\n\n";

        System.out.println("Contact is: + " + mRecipientTextView.getText().toString());
        for (int i = 0; i < mRecipientTextView.getRecipientParticipantDataForConversationCreation().size(); i++) {
            System.out.println("Contact is: + " + mRecipientTextView.getRecipientParticipantDataForConversationCreation().get(i).getFullName());
            contactInfo += "Name: ";
            contactInfo += mRecipientTextView.getRecipientParticipantDataForConversationCreation().get(i).getFullName();
            contactInfo += "\nPhone Number: ";
            contactInfo += mRecipientTextView.getRecipientParticipantDataForConversationCreation().get(i).getDisplayDestination();
        }

        mRecipientTextView.removeRecipientEntry(recipientEntry);

        SharedPreferences sharedPreferences = getSharedPreferences("VcardInfo", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("contactString", contactInfo).apply();
        invalidateContactLists();
        invalidateActionBar();
        finish();
    }

    @Override
    public void onInvalidContactChipsPruned(final int prunedCount) {
        Assert.isTrue(prunedCount > 0);
        UiUtils.showToast(R.plurals.add_invalid_contact_error, prunedCount);
    }

    @Override
    public void onEntryComplete() {
        if (mContactPickingMode == MODE_PICK_INITIAL_CONTACT ||
                mContactPickingMode == MODE_PICK_MORE_CONTACTS ||
                mContactPickingMode == MODE_PICK_MAX_PARTICIPANTS) {
            maybeGetOrCreateConversation();
        }
    }

    private void invalidateContactLists() {
        mAllContactsListViewHolder.invalidateList();
        mFrequentContactsListViewHolder.invalidateList();
    }

    private void toggleContactListItemsVisibilityForPendingTransition(final boolean show) {
        if (!OsUtil.isAtLeastL()) {
            // Explode animation is not supported pre-L.
            return;
        }
        mAllContactsListViewHolder.toggleVisibilityForPendingTransition(show, mPendingExplodeView);
        mFrequentContactsListViewHolder.toggleVisibilityForPendingTransition(show,
                mPendingExplodeView);
    }


    @Override
    public void onContactCustomColorLoaded(final ContactPickerData data) {
        mBinding.ensureBound(data);
        invalidateContactLists();
    }

    public void updateActionBar(final ActionBar actionBar) {
        actionBar.hide();
        UiUtils.setStatusBarColor(this,
                getResources().getColor(R.color.compose_notification_bar_background));
    }

    private GetOrCreateConversationAction.GetOrCreateConversationActionMonitor mMonitor;

    @Override
    @Assert.RunsOnMainThread
    public void onGetOrCreateConversationSucceeded(final ActionMonitor monitor,
                                                   final Object data, final String conversationId) {
        Assert.isTrue(monitor == mMonitor);
        Assert.isTrue(conversationId != null);

        mRecipientTextView.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_CLASS_TEXT);
        mHost.onGetOrCreateNewConversation(conversationId);

        mMonitor = null;
    }

    @Override
    @Assert.RunsOnMainThread
    public void onGetOrCreateConversationFailed(final ActionMonitor monitor,
                                                final Object data) {
        Assert.isTrue(monitor == mMonitor);
        LogUtil.e(LogUtil.BUGLE_TAG, "onGetOrCreateConversationFailed");
        mMonitor = null;
    }
}
