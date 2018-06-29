/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.messagecube.messaging.ui.conversation;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.messagecube.messaging.R;
import com.messagecube.messaging.datamodel.DataModel;
import com.messagecube.messaging.datamodel.data.ConversationMessageData;
import com.messagecube.messaging.datamodel.data.MessageData;
import com.messagecube.messaging.datamodel.data.MessagePartData;
import com.messagecube.messaging.datamodel.data.SubscriptionListData.SubscriptionListEntry;
import com.messagecube.messaging.datamodel.media.ImageRequestDescriptor;
import com.messagecube.messaging.datamodel.media.MessagePartImageRequestDescriptor;
import com.messagecube.messaging.datamodel.media.UriImageRequestDescriptor;
import com.messagecube.messaging.sms.MmsUtils;
import com.messagecube.messaging.ui.AsyncImageView;
import com.messagecube.messaging.ui.AsyncImageView.AsyncImageViewDelayLoader;
import com.messagecube.messaging.ui.AudioAttachmentView;
import com.messagecube.messaging.ui.ContactIconView;
import com.messagecube.messaging.ui.ConversationDrawables;
import com.messagecube.messaging.ui.MultiAttachmentLayout;
import com.messagecube.messaging.ui.MultiAttachmentLayout.OnAttachmentClickListener;
import com.messagecube.messaging.ui.PersonItemView;
import com.messagecube.messaging.ui.UIIntents;
import com.messagecube.messaging.ui.VideoThumbnailView;
import com.messagecube.messaging.util.AccessibilityUtil;
import com.messagecube.messaging.util.Assert;
import com.messagecube.messaging.util.AvatarUriUtil;
import com.messagecube.messaging.util.ContentType;
import com.messagecube.messaging.util.ImageUtils;
import com.messagecube.messaging.util.OsUtil;
import com.messagecube.messaging.util.PhoneUtils;
import com.messagecube.messaging.util.UiUtils;
import com.messagecube.messaging.util.YouTubeUtil;
import com.bumptech.glide.Glide;
import com.google.common.base.Predicate;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mobi.messagecube.sdk.MessageItemParser;
import mobi.messagecube.sdk.bingsearch.WebSearchView;
import mobi.messagecube.sdk.openAPI.BaseClass;
import mobi.messagecube.sdk.openAPI.MessageCubeParams;
import mobi.messagecube.sdk.openAPI.MessageType;
import mobi.messagecube.sdk.openAPI.ReceiveProcess;
import mobi.messagecube.sdk.openAPI.WebSearchDetailView;

/**
 * The view for a single entry in a conversation.
 */
public class ConversationMessageView extends FrameLayout implements View.OnClickListener,
        View.OnLongClickListener, OnAttachmentClickListener {
    public interface ConversationMessageViewHost {
        boolean onAttachmentClick(ConversationMessageView view, MessagePartData attachment,
                Rect imageBounds, boolean longPress);
        SubscriptionListEntry getSubscriptionEntryForSelfParticipant(String selfParticipantId,
                boolean excludeDefault);
    }

    private final ConversationMessageData mData;

    private LinearLayout mMessageAttachmentsView;
    private MultiAttachmentLayout mMultiAttachmentView;
    private AsyncImageView mMessageImageView;
    private TextView mMessageTextView;
    private ImageView mContactCardView;
    private boolean mMessageTextHasLinks;
    private boolean mMessageHasYouTubeLink;
    private TextView mStatusTextView;
    private TextView mTitleTextView;
    private TextView mMmsInfoTextView;
    private LinearLayout mMessageTitleLayout;
    private TextView mSenderNameTextView;
    private ContactIconView mContactIconView;
    private ConversationMessageBubbleView mMessageBubble;

    private ConversationMessageSearchScrollView mSearchScrollView;

    // TODO: MessageCube Search Result
    protected WebSearchView[] mWebSearches = new WebSearchView[10];

    protected LinearLayout mWebSearchLinearLayout0;

    protected WebSearchView mWebSearch0;
    protected WebSearchView mWebSearch1;
    protected WebSearchView mWebSearch2;
    protected WebSearchView mWebSearch3;
    protected WebSearchView mWebSearch4;
    protected WebSearchView mWebSearch5;
    protected WebSearchView mWebSearch6;
    protected WebSearchView mWebSearch7;
    protected WebSearchView mWebSearch8;
    protected WebSearchView mWebSearch9;

    private ConversationMessageForwardView mSearchForwardView;
    private LinearLayout mSearchScrollLinearLayoutForward;
    protected WebSearchView mWebSearchForward;

    private View mSubjectView;
    private TextView mSubjectLabel;
    private TextView mSubjectText;
    private View mDeliveredBadge;
    private ViewGroup mMessageMetadataView;
    private ViewGroup mMessageTextAndInfoView;
    private TextView mSimNameView;

    private boolean mOneOnOne;
    private ConversationMessageViewHost mHost;

    public ConversationMessageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        // TODO: we should switch to using Binding and DataModel factory methods.
        mData = new ConversationMessageData();
    }

    @Override
    protected void onFinishInflate() {
        mContactIconView = (ContactIconView) findViewById(R.id.conversation_icon);
        mContactIconView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                ConversationMessageView.this.performLongClick();
                return true;
            }
        });

        mMessageAttachmentsView = (LinearLayout) findViewById(R.id.message_attachments);
        mMultiAttachmentView = (MultiAttachmentLayout) findViewById(R.id.multiple_attachments);
        mMultiAttachmentView.setOnAttachmentClickListener(this);

        mMessageImageView = (AsyncImageView) findViewById(R.id.message_image);
        mMessageImageView.setOnClickListener(this);
        mMessageImageView.setOnLongClickListener(this);

        mMessageTextView = (TextView) findViewById(R.id.message_text);
        mMessageTextView.setOnClickListener(this);
        IgnoreLinkLongClickHelper.ignoreLinkLongClick(mMessageTextView, this);

        mContactCardView = (ImageView) findViewById(R.id.contact_card);
        mContactCardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//                getContext().startActivity(intent);
                ArrayList<String> tmpList = getContactInfo(mMessageTextView.getText().toString());
                System.out.println("tmpList is: " + tmpList);
                Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                intent.putExtra(ContactsContract.Intents.Insert.NAME, tmpList.size() > 0 ? tmpList.get(0) : "")
                        .putExtra(ContactsContract.Intents.Insert.PHONE, tmpList.size() > 1 ? tmpList.get(1) : "");
                getContext().startActivity(intent);
            }
        });


        mStatusTextView = (TextView) findViewById(R.id.message_status);
        mTitleTextView = (TextView) findViewById(R.id.message_title);
        mMmsInfoTextView = (TextView) findViewById(R.id.mms_info);
        mMessageTitleLayout = (LinearLayout) findViewById(R.id.message_title_layout);
        mSenderNameTextView = (TextView) findViewById(R.id.message_sender_name);
        mMessageBubble = (ConversationMessageBubbleView) findViewById(R.id.message_content);
        mSubjectView = findViewById(R.id.subject_container);
        mSubjectLabel = (TextView) mSubjectView.findViewById(R.id.subject_label);
        mSubjectText = (TextView) mSubjectView.findViewById(R.id.subject_text);
        mDeliveredBadge = findViewById(R.id.smsDeliveredBadge);
        mMessageMetadataView = (ViewGroup) findViewById(R.id.message_metadata);
        mMessageTextAndInfoView = (ViewGroup) findViewById(R.id.message_text_and_info);
        mSimNameView = (TextView) findViewById(R.id.sim_name);

        mSearchScrollView = (ConversationMessageSearchScrollView) findViewById(R.id.search_scroll);

        // TODO: this dons't work as what we expect.
        mWebSearchLinearLayout0 = (LinearLayout) findViewById(R.id.webSearch_0);

        mWebSearch0 = new WebSearchView();
        mWebSearch0.mImageView = (ImageView) findViewById(R.id.webSearch_0_image);
        mWebSearch0.mProfileImageView = (ImageView) findViewById(R.id.webSearch_0_profile_image);
        mWebSearch0.nameTextView = (TextView) findViewById(R.id.webSearch_0_name);
        mWebSearch0.mProductPrice = (TextView) findViewById(R.id.webSearch_0_price);
        mWebSearch0.mSaleProductPrice = (TextView) findViewById(R.id.webSearch_0_salePrice);
        mWebSearch0.snippetTextView = (TextView) findViewById(R.id.webSearch_0_snippet);
        mWebSearch0.mRatingBar = (RatingBar) findViewById(R.id.web_search_0_ratingBar);
        mWebSearch0.mYelpCategory = (TextView) findViewById(R.id.webSearch_0_yelp_category);

        // TODO: (rename these, could be shorten.)
        mWebSearch0.add();

        mWebSearch1 = new WebSearchView();
        mWebSearch1.mImageView = (ImageView) findViewById(R.id.webSearch_1_image);
        mWebSearch1.mProfileImageView = (ImageView) findViewById(R.id.webSearch_1_profile_image);
        mWebSearch1.nameTextView = (TextView) findViewById(R.id.webSearch_1_name);
        mWebSearch1.mProductPrice = (TextView) findViewById(R.id.webSearch_1_price);
        mWebSearch1.mSaleProductPrice = (TextView) findViewById(R.id.webSearch_1_salePrice);
        mWebSearch1.snippetTextView = (TextView) findViewById(R.id.webSearch_1_snippet);
        mWebSearch1.mRatingBar = (RatingBar) findViewById(R.id.web_search_1_ratingBar);
        mWebSearch1.mYelpCategory = (TextView) findViewById(R.id.webSearch_1_yelp_category);

        mWebSearch1.add();

        mWebSearch2 = new WebSearchView();
        mWebSearch2.mImageView = (ImageView) findViewById(R.id.webSearch_2_image);
        mWebSearch2.mProfileImageView = (ImageView) findViewById(R.id.webSearch_2_profile_image);
        mWebSearch2.nameTextView = (TextView) findViewById(R.id.webSearch_2_name);
        mWebSearch2.mProductPrice = (TextView) findViewById(R.id.webSearch_2_price);
        mWebSearch2.mSaleProductPrice = (TextView) findViewById(R.id.webSearch_2_salePrice);
        mWebSearch2.snippetTextView = (TextView) findViewById(R.id.webSearch_2_snippet);
        mWebSearch2.mRatingBar = (RatingBar) findViewById(R.id.web_search_2_ratingBar);
        mWebSearch2.mYelpCategory = (TextView) findViewById(R.id.webSearch_2_yelp_category);

        mWebSearch2.add();

        mWebSearch3 = new WebSearchView();
        mWebSearch3.mImageView = (ImageView) findViewById(R.id.webSearch_3_image);
        mWebSearch3.mProfileImageView = (ImageView) findViewById(R.id.webSearch_3_profile_image);
        mWebSearch3.nameTextView = (TextView) findViewById(R.id.webSearch_3_name);
        mWebSearch3.snippetTextView = (TextView) findViewById(R.id.webSearch_3_snippet);
        mWebSearch3.mProductPrice = (TextView) findViewById(R.id.webSearch_3_price);
        mWebSearch3.mSaleProductPrice = (TextView) findViewById(R.id.webSearch_3_salePrice);
        mWebSearch3.mRatingBar = (RatingBar) findViewById(R.id.web_search_3_ratingBar);
        mWebSearch3.mYelpCategory = (TextView) findViewById(R.id.webSearch_3_yelp_category);

        mWebSearch3.add();

        mWebSearch4 = new WebSearchView();
        mWebSearch4.mImageView = (ImageView) findViewById(R.id.webSearch_4_image);
        mWebSearch4.mProfileImageView = (ImageView) findViewById(R.id.webSearch_4_profile_image);
        mWebSearch4.nameTextView = (TextView) findViewById(R.id.webSearch_4_name);
        mWebSearch4.snippetTextView = (TextView) findViewById(R.id.webSearch_4_snippet);
        mWebSearch4.mProductPrice = (TextView) findViewById(R.id.webSearch_4_price);
        mWebSearch4.mSaleProductPrice = (TextView) findViewById(R.id.webSearch_4_salePrice);
        mWebSearch4.mRatingBar = (RatingBar) findViewById(R.id.web_search_4_ratingBar);
        mWebSearch4.mYelpCategory = (TextView) findViewById(R.id.webSearch_4_yelp_category);

        mWebSearch4.add();

        mWebSearch5 = new WebSearchView();
        mWebSearch5.mImageView = (ImageView) findViewById(R.id.webSearch_5_image);
        mWebSearch5.mProfileImageView = (ImageView) findViewById(R.id.webSearch_5_profile_image);
        mWebSearch5.nameTextView = (TextView) findViewById(R.id.webSearch_5_name);
        mWebSearch5.mProductPrice = (TextView) findViewById(R.id.webSearch_5_price);
        mWebSearch5.mSaleProductPrice = (TextView) findViewById(R.id.webSearch_5_salePrice);
        mWebSearch5.snippetTextView = (TextView) findViewById(R.id.webSearch_5_snippet);
        mWebSearch5.mRatingBar = (RatingBar) findViewById(R.id.web_search_5_ratingBar);
        mWebSearch5.mYelpCategory = (TextView) findViewById(R.id.webSearch_5_yelp_category);

        mWebSearch5.add();

        mWebSearch6 = new WebSearchView();
        mWebSearch6.mImageView = (ImageView) findViewById(R.id.webSearch_6_image);
        mWebSearch6.mProfileImageView = (ImageView) findViewById(R.id.webSearch_6_profile_image);
        mWebSearch6.nameTextView = (TextView) findViewById(R.id.webSearch_6_name);
        mWebSearch6.mProductPrice = (TextView) findViewById(R.id.webSearch_6_price);
        mWebSearch6.mSaleProductPrice = (TextView) findViewById(R.id.webSearch_6_salePrice);
        mWebSearch6.snippetTextView = (TextView) findViewById(R.id.webSearch_6_snippet);
        mWebSearch6.mRatingBar = (RatingBar) findViewById(R.id.web_search_6_ratingBar);
        mWebSearch6.mYelpCategory = (TextView) findViewById(R.id.webSearch_6_yelp_category);

        mWebSearch6.add();

        mWebSearch7 = new WebSearchView();
        mWebSearch7.mImageView = (ImageView) findViewById(R.id.webSearch_7_image);
        mWebSearch7.mProfileImageView = (ImageView) findViewById(R.id.webSearch_7_profile_image);
        mWebSearch7.nameTextView = (TextView) findViewById(R.id.webSearch_7_name);
        mWebSearch7.mProductPrice = (TextView) findViewById(R.id.webSearch_7_price);
        mWebSearch7.mSaleProductPrice = (TextView) findViewById(R.id.webSearch_7_salePrice);
        mWebSearch7.snippetTextView = (TextView) findViewById(R.id.webSearch_7_snippet);
        mWebSearch7.mRatingBar = (RatingBar) findViewById(R.id.web_search_7_ratingBar);
        mWebSearch7.mYelpCategory = (TextView) findViewById(R.id.webSearch_7_yelp_category);

        mWebSearch7.add();

        mWebSearch8 = new WebSearchView();
        mWebSearch8.mImageView = (ImageView) findViewById(R.id.webSearch_8_image);
        mWebSearch8.mProfileImageView = (ImageView) findViewById(R.id.webSearch_8_profile_image);
        mWebSearch8.nameTextView = (TextView) findViewById(R.id.webSearch_8_name);
        mWebSearch8.snippetTextView = (TextView) findViewById(R.id.webSearch_8_snippet);
        mWebSearch8.mProductPrice = (TextView) findViewById(R.id.webSearch_8_price);
        mWebSearch8.mSaleProductPrice = (TextView) findViewById(R.id.webSearch_8_salePrice);
        mWebSearch8.mRatingBar = (RatingBar) findViewById(R.id.web_search_8_ratingBar);
        mWebSearch8.mYelpCategory = (TextView) findViewById(R.id.webSearch_8_yelp_category);

        mWebSearch8.add();

        mWebSearch9 = new WebSearchView();
        mWebSearch9.mImageView = (ImageView) findViewById(R.id.webSearch_9_image);
        mWebSearch9.mProfileImageView = (ImageView) findViewById(R.id.webSearch_9_profile_image);
        mWebSearch9.nameTextView = (TextView) findViewById(R.id.webSearch_9_name);
        mWebSearch9.snippetTextView = (TextView) findViewById(R.id.webSearch_9_snippet);
        mWebSearch9.mProductPrice = (TextView) findViewById(R.id.webSearch_9_price);
        mWebSearch9.mSaleProductPrice = (TextView) findViewById(R.id.webSearch_9_salePrice);
        mWebSearch9.mRatingBar = (RatingBar) findViewById(R.id.web_search_9_ratingBar);
        mWebSearch9.mYelpCategory = (TextView) findViewById(R.id.webSearch_9_yelp_category);

        mWebSearch9.add();

        mWebSearches[0] = mWebSearch0;
        mWebSearches[1] = mWebSearch1;
        mWebSearches[2] = mWebSearch2;
        mWebSearches[3] = mWebSearch3;
        mWebSearches[4] = mWebSearch4;
        mWebSearches[5] = mWebSearch5;
        mWebSearches[6] = mWebSearch6;
        mWebSearches[7] = mWebSearch7;
        mWebSearches[8] = mWebSearch8;
        mWebSearches[9] = mWebSearch9;

        mSearchForwardView = (ConversationMessageForwardView) findViewById(R.id.forward_view);
        mSearchScrollLinearLayoutForward = (LinearLayout) findViewById(R.id.search_scroll_linear_layout_forward);
        mWebSearchForward = new WebSearchView();
        mWebSearchForward.mImageView = (ImageView) findViewById(R.id.webSearch_0_image_forward);
        mWebSearchForward.mProfileImageView = (ImageView) findViewById(R.id.webSearch_0_profile_image_forward);
        mWebSearchForward.nameTextView = (TextView) findViewById(R.id.webSearch_0_name_forward);
        mWebSearchForward.mProductPrice = (TextView) findViewById(R.id.webSearch_0_price_forward);
        mWebSearchForward.mSaleProductPrice = (TextView) findViewById(R.id.webSearch_0_salePrice_forward);
        mWebSearchForward.snippetTextView = (TextView) findViewById(R.id.webSearch_0_snippet_forward);
        mWebSearchForward.mRatingBar = (RatingBar) findViewById(R.id.web_search_0_ratingBar_forward);
        mWebSearchForward.mYelpCategory = (TextView) findViewById(R.id.webSearch_0_yelp_category_forward);

        mWebSearchForward.add();
    }

    private ArrayList<String> getContactInfo(String infoString) {
        ArrayList<String> list = new ArrayList<String>();
        int idx_Name = infoString.indexOf("Name:");
        int idx_Phone = infoString.indexOf("Phone Number:");
        String name = (idx_Name + 5 >= infoString.length() || idx_Phone >= infoString.length()) ? "" : infoString.substring(idx_Name + 5, idx_Phone).trim();
        String phoneNumber = (idx_Phone + 13 >= infoString.length()) ? "" : infoString.substring(idx_Phone + 13).trim();
        list.add(name);
        list.add(phoneNumber);
        return list;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int horizontalSpace = MeasureSpec.getSize(widthMeasureSpec);
        final int iconSize = getResources()
                .getDimensionPixelSize(R.dimen.conversation_message_contact_icon_size);

        final int unspecifiedMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        final int iconMeasureSpec = MeasureSpec.makeMeasureSpec(iconSize, MeasureSpec.EXACTLY);

        mContactIconView.measure(iconMeasureSpec, iconMeasureSpec);

        final int arrowWidth =
                getResources().getDimensionPixelSize(R.dimen.message_bubble_arrow_width);

        // We need to subtract contact icon width twice from the horizontal space to get
        // the max leftover space because we want the message bubble to extend no further than the
        // starting position of the message bubble in the opposite direction.
        final int maxLeftoverSpace = horizontalSpace - mContactIconView.getMeasuredWidth() * 2
                - arrowWidth - getPaddingLeft() - getPaddingRight();
        final int messageContentWidthMeasureSpec = MeasureSpec.makeMeasureSpec(maxLeftoverSpace,
                MeasureSpec.AT_MOST);

        mMessageBubble.measure(messageContentWidthMeasureSpec, unspecifiedMeasureSpec);

        final int verticalSpace = MeasureSpec.getSize(heightMeasureSpec);
        final int searchScrollHeight = 700;

        // TODO: Define the search scroll measure spec
        // The value here is different from the value in xml.
        final int searchScrollWithMeasureSpec1 = MeasureSpec.makeMeasureSpec(horizontalSpace, MeasureSpec.EXACTLY);
        final int searchScrollWithMeasureSpec2 = MeasureSpec.makeMeasureSpec(searchScrollHeight, MeasureSpec.EXACTLY);
        mSearchScrollView.measure(searchScrollWithMeasureSpec1, unspecifiedMeasureSpec);

        // TODO: Define the search forward measure spec
        final int searchForwardWithMeasureSpec1 = MeasureSpec.makeMeasureSpec(horizontalSpace, MeasureSpec.EXACTLY);
        final int searchForwardWithMeasureSpec2 = MeasureSpec.makeMeasureSpec(searchScrollHeight, MeasureSpec.EXACTLY);
        mSearchForwardView.measure(searchForwardWithMeasureSpec1, unspecifiedMeasureSpec);

        int maxHeight = Math.max(mContactIconView.getMeasuredHeight(),
                mMessageBubble.getMeasuredHeight());

        // TODO: Add 700 height if it's a message cube format.
        final String text = mData.getText();
        if (text != null) {
            if (MessageItemParser.isComplicatedWeb(text) || MessageItemParser.isForwardMessage(text)) {
                maxHeight = maxHeight + mSearchScrollView.getMeasuredHeight();
            } else {
                // TODO: set padding between message texts
                // maxHeight = maxHeight + 50;
            }
        }

        setMeasuredDimension(horizontalSpace, maxHeight + getPaddingBottom() + getPaddingTop());
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right,
            final int bottom) {
        final boolean isRtl = AccessibilityUtil.isLayoutRtl(this);

        final int iconWidth = mContactIconView.getMeasuredWidth();
        final int iconHeight = mContactIconView.getMeasuredHeight();
        final int iconTop = getPaddingTop();
        final int contentWidth = (right -left) - iconWidth - getPaddingLeft() - getPaddingRight();
        final int contentHeight = mMessageBubble.getMeasuredHeight();
        final int contentTop = iconTop;

        final int iconLeft;
        final int contentLeft;
        if (mData.getIsIncoming()) {
            if (isRtl) {
                iconLeft = (right - left) - getPaddingRight() - iconWidth;
                contentLeft = iconLeft - contentWidth;
            } else {
                iconLeft = getPaddingLeft();
                contentLeft = iconLeft + iconWidth;
            }
        } else {
            // TODO: Remove outgoing contract icon
            mContactIconView.setVisibility(View.GONE);
            if (isRtl) {
                iconLeft = getPaddingLeft();
                contentLeft = iconLeft + iconWidth;
            } else {
                iconLeft = (right - left) + getPaddingRight() - (iconWidth/2);
                contentLeft = iconLeft - contentWidth;
            }
        }

        mContactIconView.layout(iconLeft, iconTop, iconLeft + iconWidth, iconTop + iconHeight);

        mMessageBubble.layout(contentLeft, contentTop, contentLeft + contentWidth,
                contentTop + contentHeight);

        final String text = mData.getText();
        if (text != null) {
            if (MessageItemParser.isComplicatedWeb(text)) {
                // TODO: Define the layout of the search scroll.
                final int maxHeight = Math.max(iconTop + iconHeight, contentTop + contentHeight);
                mSearchScrollView.layout(left, maxHeight + 20, right, maxHeight + 20 + 700);

            } else if (MessageItemParser.isForwardMessage(text)) {
                // TODO: Define the layout of the search forward view.
                // android.widget.LinearLayout$LayoutParams cannot be cast to android.widget.FrameLayout$LayoutParams
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(80, FrameLayout.LayoutParams.WRAP_CONTENT);
                if (mData.getIsIncoming()) {
                    params.gravity = Gravity.LEFT;
                } else {
                    params.gravity = Gravity.RIGHT;
                }
                mSearchScrollLinearLayoutForward.setLayoutParams(params);

                final int maxHeight = Math.max(iconTop + iconHeight, contentTop + contentHeight);
                mSearchForwardView.layout(left, maxHeight + 20, right, maxHeight + 20 + 700);
            }
        }
    }

 //   @Override
 //   protected void onDetachedFromWindow() {
 //       super.onDetachedFromWindow();

        // TODO: Fix Fabric #146
        // java.lang.IllegalArgumentException: You cannot start a load for a destroyed activity
   //     for(WebSearchView webSearchView : mWebSearches) {
   //         if (webSearchView.mImageView != null) {
   //             Glide.clear(webSearchView.mImageView);
   //         }

   //         if (webSearchView.mProfileImageView != null) {
    //            Glide.clear(webSearchView.mProfileImageView);
     //       }
     //   }
  //  }

    /**
     * Fills in the data associated with this view.
     *
     * @param cursor The cursor from a MessageList that this view is in, pointing to its entry.
     */
    public void bind(final Cursor cursor) {
        bind(cursor, true, null);
    }

    /**
     * Fills in the data associated with this view.
     *
     * @param cursor The cursor from a MessageList that this view is in, pointing to its entry.
     * @param oneOnOne Whether this is a 1:1 conversation
     */
    public void bind(final Cursor cursor,
            final boolean oneOnOne, final String selectedMessageId) {
        mOneOnOne = oneOnOne;

        // Update our UI model
        mData.bind(cursor);
        setSelected(TextUtils.equals(mData.getMessageId(), selectedMessageId));

        // Update text and image content for the view.
        updateViewContent();

        // Update colors and layout parameters for the view.
        updateViewAppearance();

        updateContentDescription();
    }

    public void setHost(final ConversationMessageViewHost host) {
        mHost = host;
    }

    /**
     * Sets a delay loader instance to manage loading / resuming of image attachments.
     */
    public void setImageViewDelayLoader(final AsyncImageViewDelayLoader delayLoader) {
        Assert.notNull(mMessageImageView);
        mMessageImageView.setDelayLoader(delayLoader);
        mMultiAttachmentView.setImageViewDelayLoader(delayLoader);
    }

    public ConversationMessageData getData() {
        return mData;
    }

    /**
     * Returns whether we should show simplified visual style for the message view (i.e. hide the
     * avatar and bubble arrow, reduce padding).
     */
    private boolean shouldShowSimplifiedVisualStyle() {
        return mData.getCanClusterWithPreviousMessage();
    }

    /**
     * Returns whether we need to show message bubble arrow. We don't show arrow if the message
     * contains media attachments or if shouldShowSimplifiedVisualStyle() is true.
     */
    private boolean shouldShowMessageBubbleArrow() {
        return !shouldShowSimplifiedVisualStyle()
                && !(mData.hasAttachments() || mMessageHasYouTubeLink);
    }

    /**
     * Returns whether we need to show a message bubble for text content.
     */
    private boolean shouldShowMessageTextBubble() {
        if (mData.hasText()) {
            return true;
        }
        final String subjectText = MmsUtils.cleanseMmsSubject(getResources(),
                mData.getMmsSubject());
        if (!TextUtils.isEmpty(subjectText)) {
            return true;
        }
        return false;
    }

    private void updateViewContent() {
        updateMessageContent();
        int titleResId = -1;
        int statusResId = -1;
        String statusText = null;
        switch(mData.getStatus()) {
            case MessageData.BUGLE_STATUS_INCOMING_AUTO_DOWNLOADING:
            case MessageData.BUGLE_STATUS_INCOMING_MANUAL_DOWNLOADING:
            case MessageData.BUGLE_STATUS_INCOMING_RETRYING_AUTO_DOWNLOAD:
            case MessageData.BUGLE_STATUS_INCOMING_RETRYING_MANUAL_DOWNLOAD:
                titleResId = R.string.message_title_downloading;
                statusResId = R.string.message_status_downloading;
                break;

            case MessageData.BUGLE_STATUS_INCOMING_YET_TO_MANUAL_DOWNLOAD:
                if (!OsUtil.isSecondaryUser()) {
                    titleResId = R.string.message_title_manual_download;
                    if (isSelected()) {
                        statusResId = R.string.message_status_download_action;
                    } else {
                        statusResId = R.string.message_status_download;
                    }
                }
                break;

            case MessageData.BUGLE_STATUS_INCOMING_EXPIRED_OR_NOT_AVAILABLE:
                if (!OsUtil.isSecondaryUser()) {
                    titleResId = R.string.message_title_download_failed;
                    statusResId = R.string.message_status_download_error;
                }
                break;

            case MessageData.BUGLE_STATUS_INCOMING_DOWNLOAD_FAILED:
                if (!OsUtil.isSecondaryUser()) {
                    titleResId = R.string.message_title_download_failed;
                    if (isSelected()) {
                        statusResId = R.string.message_status_download_action;
                    } else {
                        statusResId = R.string.message_status_download;
                    }
                }
                break;

            case MessageData.BUGLE_STATUS_OUTGOING_YET_TO_SEND:
            case MessageData.BUGLE_STATUS_OUTGOING_SENDING:
                statusResId = R.string.message_status_sending;
                break;

            case MessageData.BUGLE_STATUS_OUTGOING_RESENDING:
            case MessageData.BUGLE_STATUS_OUTGOING_AWAITING_RETRY:
                statusResId = R.string.message_status_send_retrying;
                break;

            case MessageData.BUGLE_STATUS_OUTGOING_FAILED_EMERGENCY_NUMBER:
                statusResId = R.string.message_status_send_failed_emergency_number;
                break;

            case MessageData.BUGLE_STATUS_OUTGOING_FAILED:
                // don't show the error state unless we're the default sms app
                if (PhoneUtils.getDefault().isDefaultSmsApp()) {
                    if (isSelected()) {
                        statusResId = R.string.message_status_resend;
                    } else {
                        statusResId = MmsUtils.mapRawStatusToErrorResourceId(
                                mData.getStatus(), mData.getRawTelephonyStatus());
                    }
                    break;
                }
                // FALL THROUGH HERE

            case MessageData.BUGLE_STATUS_OUTGOING_COMPLETE:
            case MessageData.BUGLE_STATUS_INCOMING_COMPLETE:
            default:
                if (!mData.getCanClusterWithNextMessage()) {
                    statusText = mData.getFormattedReceivedTimeStamp();
                }
                break;
        }

        final boolean titleVisible = (titleResId >= 0);
        if (titleVisible) {
            final String titleText = getResources().getString(titleResId);
            mTitleTextView.setText(titleText);

            final String mmsInfoText = getResources().getString(
                    R.string.mms_info,
                    Formatter.formatFileSize(getContext(), mData.getSmsMessageSize()),
                    DateUtils.formatDateTime(
                            getContext(),
                            mData.getMmsExpiry(),
                            DateUtils.FORMAT_SHOW_DATE |
                            DateUtils.FORMAT_SHOW_TIME |
                            DateUtils.FORMAT_NUMERIC_DATE |
                            DateUtils.FORMAT_NO_YEAR));
            mMmsInfoTextView.setText(mmsInfoText);
            mMessageTitleLayout.setVisibility(View.VISIBLE);
        } else {
            mMessageTitleLayout.setVisibility(View.GONE);
        }

        final String subjectText = MmsUtils.cleanseMmsSubject(getResources(),
                mData.getMmsSubject());
        final boolean subjectVisible = !TextUtils.isEmpty(subjectText);

        final boolean senderNameVisible = !mOneOnOne && !mData.getCanClusterWithNextMessage()
                && mData.getIsIncoming();
        if (senderNameVisible) {
            mSenderNameTextView.setText(mData.getSenderDisplayName());
            mSenderNameTextView.setVisibility(View.VISIBLE);
        } else {
            mSenderNameTextView.setVisibility(View.GONE);
        }

        if (statusResId >= 0) {
            statusText = getResources().getString(statusResId);
        }

        // We set the text even if the view will be GONE for accessibility
        mStatusTextView.setText(statusText);
        final boolean statusVisible = !TextUtils.isEmpty(statusText);
        if (statusVisible) {
            mStatusTextView.setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setVisibility(View.GONE);
        }

        final boolean deliveredBadgeVisible =
                mData.getStatus() == MessageData.BUGLE_STATUS_OUTGOING_DELIVERED;
        mDeliveredBadge.setVisibility(deliveredBadgeVisible ? View.VISIBLE : View.GONE);

        // Update the sim indicator.
        final boolean showSimIconAsIncoming = mData.getIsIncoming() &&
                (!mData.hasAttachments() || shouldShowMessageTextBubble());
        final SubscriptionListEntry subscriptionEntry =
                mHost.getSubscriptionEntryForSelfParticipant(mData.getSelfParticipantId(),
                        true /* excludeDefault */);
        final boolean simNameVisible = subscriptionEntry != null &&
                !TextUtils.isEmpty(subscriptionEntry.displayName) &&
                !mData.getCanClusterWithNextMessage();
        if (simNameVisible) {
            final String simNameText = mData.getIsIncoming() ? getResources().getString(
                    R.string.incoming_sim_name_text, subscriptionEntry.displayName) :
                        subscriptionEntry.displayName;
            mSimNameView.setText(simNameText);
            mSimNameView.setTextColor(showSimIconAsIncoming ? getResources().getColor(
                    R.color.timestamp_text_incoming) : subscriptionEntry.displayColor);
            mSimNameView.setVisibility(VISIBLE);
        } else {
            mSimNameView.setText(null);
            mSimNameView.setVisibility(GONE);
        }

        final boolean metadataVisible = senderNameVisible || statusVisible
                || deliveredBadgeVisible || simNameVisible;
        mMessageMetadataView.setVisibility(metadataVisible ? View.VISIBLE : View.GONE);

        final boolean messageTextAndOrInfoVisible = titleVisible || subjectVisible
                || mData.hasText() || metadataVisible;
        mMessageTextAndInfoView.setVisibility(
                messageTextAndOrInfoVisible ? View.VISIBLE : View.GONE);

        if (shouldShowSimplifiedVisualStyle()) {
            mContactIconView.setVisibility(View.GONE);
            mContactIconView.setImageResourceUri(null);
        } else {
            mContactIconView.setVisibility(View.VISIBLE);
            final Uri avatarUri = AvatarUriUtil.createAvatarUri(
                    mData.getSenderProfilePhotoUri(),
                    mData.getSenderFullName(),
                    mData.getSenderNormalizedDestination(),
                    mData.getSenderContactLookupKey());
            mContactIconView.setImageResourceUri(avatarUri, mData.getSenderContactId(),
                    mData.getSenderContactLookupKey(), mData.getSenderNormalizedDestination());
        }
    }

    private void updateMessageContent() {
        // We must update the text before the attachments since we search the text to see if we
        // should make a preview youtube image in the attachments
        updateMessageText();
        updateMessageAttachments();
        updateMessageSubject();
        mMessageBubble.bind(mData);
    }

    private void updateMessageAttachments() {
        // Bind video, audio, and VCard attachments. If there are multiple, they stack vertically.
        bindAttachmentsOfSameType(sVideoFilter,
                R.layout.message_video_attachment, mVideoViewBinder, VideoThumbnailView.class);
        bindAttachmentsOfSameType(sAudioFilter,
                R.layout.message_audio_attachment, mAudioViewBinder, AudioAttachmentView.class);
        bindAttachmentsOfSameType(sVCardFilter,
                R.layout.message_vcard_attachment, mVCardViewBinder, PersonItemView.class);

        // Bind image attachments. If there are multiple, they are shown in a collage view.
        final List<MessagePartData> imageParts = mData.getAttachments(sImageFilter);
        if (imageParts.size() > 1) {
            Collections.sort(imageParts, sImageComparator);
            mMultiAttachmentView.bindAttachments(imageParts, null, imageParts.size());
            mMultiAttachmentView.setVisibility(View.VISIBLE);
        } else {
            mMultiAttachmentView.setVisibility(View.GONE);
        }

        // In the case that we have no image attachments and exactly one youtube link in a message
        // then we will show a preview.
        String youtubeThumbnailUrl = null;
        String originalYoutubeLink = null;
        if (mMessageTextHasLinks && imageParts.size() == 0) {
            CharSequence messageTextWithSpans = mMessageTextView.getText();
            final URLSpan[] spans = ((Spanned) messageTextWithSpans).getSpans(0,
                    messageTextWithSpans.length(), URLSpan.class);
            for (URLSpan span : spans) {
                String url = span.getURL();
                String youtubeLinkForUrl = YouTubeUtil.getYoutubePreviewImageLink(url);
                if (!TextUtils.isEmpty(youtubeLinkForUrl)) {
                    if (TextUtils.isEmpty(youtubeThumbnailUrl)) {
                        // Save the youtube link if we don't already have one
                        youtubeThumbnailUrl = youtubeLinkForUrl;
                        originalYoutubeLink = url;
                    } else {
                        // We already have a youtube link. This means we have two youtube links so
                        // we shall show none.
                        youtubeThumbnailUrl = null;
                        originalYoutubeLink = null;
                        break;
                    }
                }
            }
        }
        // We need to keep track if we have a youtube link in the message so that we will not show
        // the arrow
        mMessageHasYouTubeLink = !TextUtils.isEmpty(youtubeThumbnailUrl);

        // We will show the message image view if there is one attachment or one youtube link
        if (imageParts.size() == 1 || mMessageHasYouTubeLink) {
            // Get the display metrics for a hint for how large to pull the image data into
            final WindowManager windowManager = (WindowManager) getContext().
                    getSystemService(Context.WINDOW_SERVICE);
            final DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);

            final int iconSize = getResources()
                    .getDimensionPixelSize(R.dimen.conversation_message_contact_icon_size);
            final int desiredWidth = displayMetrics.widthPixels - iconSize - iconSize;

            if (imageParts.size() == 1) {
                final MessagePartData imagePart = imageParts.get(0);
                // If the image is big, we want to scale it down to save memory since we're going to
                // scale it down to fit into the bubble width. We don't constrain the height.
                final ImageRequestDescriptor imageRequest =
                        new MessagePartImageRequestDescriptor(imagePart,
                                desiredWidth,
                                MessagePartData.UNSPECIFIED_SIZE,
                                false);
                adjustImageViewBounds(imagePart);
                mMessageImageView.setImageResourceId(imageRequest);
                mMessageImageView.setTag(imagePart);
            } else {
                // Youtube Thumbnail image
                final ImageRequestDescriptor imageRequest =
                        new UriImageRequestDescriptor(Uri.parse(youtubeThumbnailUrl), desiredWidth,
                            MessagePartData.UNSPECIFIED_SIZE, true /* allowCompression */,
                            true /* isStatic */, false /* cropToCircle */,
                            ImageUtils.DEFAULT_CIRCLE_BACKGROUND_COLOR /* circleBackgroundColor */,
                            ImageUtils.DEFAULT_CIRCLE_STROKE_COLOR /* circleStrokeColor */);
                mMessageImageView.setImageResourceId(imageRequest);
                mMessageImageView.setTag(originalYoutubeLink);
            }
            mMessageImageView.setVisibility(View.VISIBLE);
        } else {
            mMessageImageView.setImageResourceId(null);
            mMessageImageView.setVisibility(View.GONE);
        }

        // Show the message attachments container if any of its children are visible
        boolean attachmentsVisible = false;
        for (int i = 0, size = mMessageAttachmentsView.getChildCount(); i < size; i++) {
            final View attachmentView = mMessageAttachmentsView.getChildAt(i);
            if (attachmentView.getVisibility() == View.VISIBLE) {
                attachmentsVisible = true;
                break;
            }
        }
        mMessageAttachmentsView.setVisibility(attachmentsVisible ? View.VISIBLE : View.GONE);
    }

    private void bindAttachmentsOfSameType(final Predicate<MessagePartData> attachmentTypeFilter,
            final int attachmentViewLayoutRes, final AttachmentViewBinder viewBinder,
            final Class<?> attachmentViewClass) {
        final LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        // Iterate through all attachments of a particular type (video, audio, etc).
        // Find the first attachment index that matches the given type if possible.
        int attachmentViewIndex = -1;
        View existingAttachmentView;
        do {
            existingAttachmentView = mMessageAttachmentsView.getChildAt(++attachmentViewIndex);
        } while (existingAttachmentView != null &&
                !(attachmentViewClass.isInstance(existingAttachmentView)));

        for (final MessagePartData attachment : mData.getAttachments(attachmentTypeFilter)) {
            View attachmentView = mMessageAttachmentsView.getChildAt(attachmentViewIndex);
            if (!attachmentViewClass.isInstance(attachmentView)) {
                attachmentView = layoutInflater.inflate(attachmentViewLayoutRes,
                        mMessageAttachmentsView, false /* attachToRoot */);
                attachmentView.setOnClickListener(this);
                attachmentView.setOnLongClickListener(this);
                mMessageAttachmentsView.addView(attachmentView, attachmentViewIndex);
            }
            viewBinder.bindView(attachmentView, attachment);
            attachmentView.setTag(attachment);
            attachmentView.setVisibility(View.VISIBLE);
            attachmentViewIndex++;
        }
        // If there are unused views left over, unbind or remove them.
        while (attachmentViewIndex < mMessageAttachmentsView.getChildCount()) {
            final View attachmentView = mMessageAttachmentsView.getChildAt(attachmentViewIndex);
            if (attachmentViewClass.isInstance(attachmentView)) {
                mMessageAttachmentsView.removeViewAt(attachmentViewIndex);
            } else {
                // No more views of this type; we're done.
                break;
            }
        }
    }

    private void updateMessageSubject() {
        final String subjectText = MmsUtils.cleanseMmsSubject(getResources(),
                mData.getMmsSubject());
        final boolean subjectVisible = !TextUtils.isEmpty(subjectText);

        if (subjectVisible) {
            mSubjectText.setText(subjectText);
            mSubjectView.setVisibility(View.VISIBLE);
        } else {
            mSubjectView.setVisibility(View.GONE);
        }
    }

    private void updateMessageText() {
        final String text = mData.getText();
        if (!TextUtils.isEmpty(text)) {

            if (MessageItemParser.isComplicatedWeb(text)) {
                // TODO: Text is a complicated MessageCube message format.
                String keyword = MessageItemParser.getSearchKeywordFromText(text);
                String postalCode = MessageItemParser.getSearchPostalCodeFromForwardMessage(text);

                String senderNumber = mData.getSenderNormalizedDestination();
                // TODO: It's unclear how to get the receiver number.
                String receiverNumber = mData.getMessageId() + "-" + mData.getParticipantId();
                long sentTimeStamp = mData.getSentTimeStamp();

                mMessageTextView.setText("Search: " + keyword);
                mMessageTextView.setVisibility(View.VISIBLE);

                mSearchScrollView.setVisibility(VISIBLE);
                mSearchForwardView.setVisibility(GONE);

                final ConversationMessageView pointer = this;

                MessageCubeParams messageCubeParams = new MessageCubeParams(this.getContext(), keyword, postalCode, postalCode, senderNumber, receiverNumber, sentTimeStamp);
                ReceiveProcess receiveProcess = new ReceiveProcess() {
                    @Override
                    public void onSuccess(String textBody, final ArrayList<BaseClass> baseClasses) {

                        final ArrayList<String> jsonStrings = new ArrayList<String>();
                        for(BaseClass baseClass : baseClasses) {
                            jsonStrings.add(baseClass.jsonString);
                        }

                        pointer.post(new Runnable() {
                            @Override
                            public void run() {
                                for(int i = 0; i < Math.min(mWebSearches.length, baseClasses.size()); i++ ) {

                                    if (i > 9) {
                                        break;
                                    }

                                    final WebSearchView webSearchView = mWebSearches[i];

                                    BaseClass result = baseClasses.get(i);

                                    // Define text
                                    webSearchView.nameTextView.setText(result.name);
                                    webSearchView.snippetTextView.setText(result.snippet);

                                    // Some views don't have image views, then we hide image views.
                                    if (webSearchView.mImageView != null) {
                                        if (result.imageUrl.length() > 0) {

                                            webSearchView.mImageView.setVisibility(View.VISIBLE);

                                            if (pointer.getContext() != null) {
                                                Glide.with(pointer.getContext()).load(result.imageUrl).into(webSearchView.mImageView);
                                            }

                                            // click
                                            webSearchView.mImageView.setOnClickListener(new View.OnClickListener() {
                                                BaseClass webSearchResult;

                                                // short display
                                                public void onClick(View v) {
                                                    WebSearchDetailView.click(pointer, webSearchView, webSearchResult, jsonStrings, MessageType.REGULAR);
                                                }

                                                public View.OnClickListener setParams(BaseClass webSearchResult) {
                                                    this.webSearchResult = webSearchResult;
                                                    return this;
                                                }
                                            }.setParams(result));

                                        } else {
                                            webSearchView.mImageView.setVisibility(View.GONE);
                                        }
                                    }

                                    if (webSearchView.mProfileImageView != null) {
                                        Object profileImageUrlObject = result.optional.get("profile_image_url");

                                        if (profileImageUrlObject != null) {
                                            String profileImageUrl = profileImageUrlObject.toString();

                                            webSearchView.mProfileImageView.setVisibility(View.VISIBLE);
                                            if (pointer.getContext() != null) {
                                                Glide.with(pointer.getContext()).load(result.imageUrl).into(webSearchView.mImageView);
                                            }

                                            // click
                                            webSearchView.mProfileImageView.setOnClickListener(new View.OnClickListener() {
                                                BaseClass webSearchResult;

                                                // short display
                                                public void onClick(View v) {
                                                    WebSearchDetailView.click(pointer, webSearchView, webSearchResult, jsonStrings, MessageType.REGULAR);
                                                }

                                                public View.OnClickListener setParams(BaseClass webSearchResult) {
                                                    this.webSearchResult = webSearchResult;
                                                    return this;
                                                }
                                            }.setParams(result));

                                            // long click
                                            webSearchView.mProfileImageView.setOnLongClickListener(new View.OnLongClickListener() {
                                                BaseClass webSearchResult;

                                                @Override
                                                public boolean onLongClick(View v) {
                                                    /*
                                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                                                    alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            clickCopyOrForwardMessage(which, webSearchResult);
                                                        }
                                                    }).create().show();
                                                    */
                                                    return true;
                                                }

                                                public View.OnLongClickListener setParams(BaseClass webSearchResult) {
                                                    this.webSearchResult = webSearchResult;
                                                    return this;
                                                }
                                            }.setParams(result));

                                        } else {
                                            webSearchView.mProfileImageView.setVisibility(View.GONE);
                                        }
                                    }

                                    // Define click actions
                                    for(TextView textView : webSearchView.textViews) {
                                        // click
                                        textView.setOnClickListener(new View.OnClickListener() {
                                            BaseClass webSearchResult;

                                            @Override
                                            public void onClick(View v) {
                                                WebSearchDetailView.click(pointer, webSearchView, webSearchResult, jsonStrings, MessageType.REGULAR);
                                            }

                                            public View.OnClickListener setParams(BaseClass webSearchResult) {
                                                this.webSearchResult = webSearchResult;
                                                return this;
                                            }
                                        }.setParams(result));

                                        // long click
                                        textView.setOnLongClickListener(new View.OnLongClickListener() {
                                            BaseClass webSearchResult;

                                            @Override
                                            public boolean onLongClick(View v) {
                                                /*
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                                                alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        clickCopyOrForwardMessage(which, webSearchResult);
                                                    }
                                                }).create().show();
                                                */
                                                return true;
                                            }

                                            public View.OnLongClickListener setParams(BaseClass webSearchResult) {
                                                this.webSearchResult = webSearchResult;
                                                return this;
                                            }
                                        }.setParams(result));
                                    }

                                    // Optional UI
                                    if (result.apiSource.equals("yelp")) {

                                        // EXAMPLE: hide UI.
                                        webSearchView.snippetTextView.setVisibility(View.GONE);
                                        webSearchView.mProductPrice.setVisibility(View.GONE);
                                        webSearchView.mSaleProductPrice.setVisibility(View.GONE);

                                        try {
                                            String ratingString = result.optional.get("rating").toString();
                                            float rating = Float.valueOf(ratingString.trim());
                                            webSearchView.mRatingBar.setRating(rating);
                                            webSearchView.mRatingBar.setIsIndicator(true);
                                            webSearchView.mRatingBar.setVisibility(View.VISIBLE);

                                            webSearchView.mYelpCategory.setVisibility(View.VISIBLE);

                                            if (webSearchView.mYelpCategory != null) {
                                                // TODO: This is not a perfect SDK design.
                                                Object categoriesObject = result.optional.get("categories");
                                                JSONArray categories = (JSONArray) categoriesObject;

                                                if (categories.length() > 0) {
                                                    webSearchView.mYelpCategory.setText(categories.getString(0));
                                                } else {
                                                    webSearchView.mYelpCategory.setVisibility(View.GONE);
                                                }
                                            }

                                        } catch (Exception nfe) {
                                            System.out.println("NumberFormatException: " + nfe.getMessage());
                                            webSearchView.mRatingBar.setVisibility(View.GONE);
                                            if (webSearchView.mYelpCategory != null) {
                                                webSearchView.mYelpCategory.setVisibility(View.GONE);
                                            }
                                        }

                                    }  else if (result.apiSource.equals("amazon") || result.apiSource.equals("expedia_hotel")) {
                                        webSearchView.snippetTextView.setVisibility(View.GONE);
                                        webSearchView.mYelpCategory.setVisibility(View.GONE);
                                        webSearchView.mRatingBar.setVisibility(View.GONE);
                                        webSearchView.nameTextView.setMaxLines(1);
                                        webSearchView.nameTextView.setEllipsize(TextUtils.TruncateAt.END);

                                        // Reset UI first
                                        webSearchView.mProductPrice.setText("");
                                        webSearchView.mSaleProductPrice.setText("");

                                        if (result.optional.containsKey("price")) {
                                            String price = result.optional.get("price").toString();
                                            if (price != null) {
                                                webSearchView.mProductPrice.setText(price);
                                                webSearchView.mProductPrice.setVisibility(View.VISIBLE);
                                            }
                                        }
                                        if (result.optional.containsKey("salePrice")) {
                                            String salePrice = result.optional.get("salePrice").toString();
                                            if (salePrice != null) {
                                                webSearchView.mSaleProductPrice.setText(salePrice);
                                                webSearchView.mSaleProductPrice.setVisibility(View.VISIBLE);
                                                webSearchView.mProductPrice.setPaintFlags(webSearchView.mProductPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                            }
                                        }

                                    } else if (result.apiSource.equals("youtube")){
                                        webSearchView.snippetTextView.setVisibility(View.VISIBLE);
                                        webSearchView.snippetTextView.setMaxLines(2);

                                    } else if (result.apiSource.equals("bing")){
                                        webSearchView.snippetTextView.setVisibility(View.VISIBLE);
                                        webSearchView.snippetTextView.setMaxLines(20);
                                        webSearchView.mRatingBar.setVisibility(View.GONE);
                                        webSearchView.mYelpCategory.setVisibility(View.GONE);

                                    }
                                    else {
                                        // TODO: This check could be removed.
                                        if (webSearchView.mRatingBar != null) {
                                            webSearchView.mRatingBar.setVisibility(View.GONE);
                                        }

                                        if (webSearchView.mYelpCategory != null) {
                                            webSearchView.mYelpCategory.setVisibility(View.GONE);
                                        }

                                        if (webSearchView.mProductPrice != null) {
                                            webSearchView.mProductPrice.setVisibility(View.GONE);
                                        }

                                        if (webSearchView.mSaleProductPrice != null) {
                                            webSearchView.mSaleProductPrice.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure() {
                        pointer.post(new Runnable() {
                            @Override
                            public void run() {
                                mSearchScrollView.setVisibility(GONE);
                            }
                        });
                    }
                };
                receiveProcess.sendGet(messageCubeParams);


            } else if (MessageItemParser.isForwardMessage(text)) {
                // TODO: Text is a forward MessageCube message format.
                String keyword = MessageItemParser.getSearchKeywordFromForwardMessage(text);
                String postalCode = MessageItemParser.getSearchPostalCodeFromForwardMessage(text);

                String senderNumber = mData.getSenderNormalizedDestination();
                // TODO: It's unclear how to get the receiver number.
                String receiverNumber = mData.getMessageId() + "-" + mData.getParticipantId();
                long sentTimeStamp = mData.getSentTimeStamp();

                final int index = MessageItemParser.getForwardIndex(text);

                mMessageTextView.setText("Forward Search: " + keyword);
                mMessageTextView.setVisibility(View.VISIBLE);

                mSearchScrollView.setVisibility(GONE);
                mSearchForwardView.setVisibility(VISIBLE);

                final ConversationMessageView pointer = this;

                MessageCubeParams messageCubeParams = new MessageCubeParams(this.getContext(), keyword, postalCode, postalCode, senderNumber, receiverNumber, sentTimeStamp);
                ReceiveProcess receiveProcess = new ReceiveProcess() {
                    @Override
                    public void onSuccess(String textBody, final ArrayList<BaseClass> baseClasses) {

                        final ArrayList<String> jsonStrings = new ArrayList<String>();
                        for(BaseClass baseClass : baseClasses) {
                            jsonStrings.add(baseClass.jsonString);
                        }

                        pointer.post(new Runnable() {
                            @Override
                            public void run() {

                                BaseClass result = new BaseClass();
                                Boolean foundResult = false;

                                for (BaseClass tmp : baseClasses) {
                                    if (tmp.index == index) {
                                        result = tmp;
                                        foundResult = true;
                                        break;
                                    }
                                }

                                if (!foundResult) {
                                    // TODO: Fabric #180.
                                    // Hide the search forward view if no forward item is found.
                                    mSearchForwardView.setVisibility(GONE);
                                    return;
                                }

                                // Define text
                                mWebSearchForward.nameTextView.setText(result.name);
                                mWebSearchForward.snippetTextView.setText(result.snippet);

                                // Some views don't have image views, then we hide image views.
                                if (mWebSearchForward.mImageView != null) {
                                    if (result.imageUrl.length() > 0) {

                                        mWebSearchForward.mImageView.setVisibility(View.VISIBLE);

                                        if (pointer.getContext() != null) {
                                            Glide.with(pointer.getContext()).load(result.imageUrl).into(mWebSearchForward.mImageView);
                                        }

                                        // click
                                        mWebSearchForward.mImageView.setOnClickListener(new View.OnClickListener() {
                                            BaseClass webSearchResult;

                                            // short display
                                            public void onClick(View v) {
                                                WebSearchDetailView.click(pointer, mWebSearchForward, webSearchResult, jsonStrings, MessageType.FORWARD);
                                            }

                                            public View.OnClickListener setParams(BaseClass webSearchResult) {
                                                this.webSearchResult = webSearchResult;
                                                return this;
                                            }
                                        }.setParams(result));

                                    } else {
                                        mWebSearchForward.mImageView.setVisibility(View.GONE);
                                    }
                                }

                                if (mWebSearchForward.mProfileImageView != null) {
                                    Object profileImageUrlObject = result.optional.get("profile_image_url");

                                    if (profileImageUrlObject != null) {
                                        String profileImageUrl = profileImageUrlObject.toString();

                                        mWebSearchForward.mProfileImageView.setVisibility(View.VISIBLE);
                                        if (pointer.getContext() != null) {
                                            Glide.with(pointer.getContext()).load(result.imageUrl).into(mWebSearchForward.mImageView);
                                        }

                                        // click
                                        mWebSearchForward.mProfileImageView.setOnClickListener(new View.OnClickListener() {
                                            BaseClass webSearchResult;

                                            // short display
                                            public void onClick(View v) {
                                                WebSearchDetailView.click(pointer, mWebSearchForward, webSearchResult, jsonStrings, MessageType.FORWARD);
                                            }

                                            public View.OnClickListener setParams(BaseClass webSearchResult) {
                                                this.webSearchResult = webSearchResult;
                                                return this;
                                            }
                                        }.setParams(result));

                                        // long click
                                        mWebSearchForward.mProfileImageView.setOnLongClickListener(new View.OnLongClickListener() {
                                            BaseClass webSearchResult;

                                            @Override
                                            public boolean onLongClick(View v) {
                                                    /*
                                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                                                    alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            clickCopyOrForwardMessage(which, webSearchResult);
                                                        }
                                                    }).create().show();
                                                    */
                                                return true;
                                            }

                                            public View.OnLongClickListener setParams(BaseClass webSearchResult) {
                                                this.webSearchResult = webSearchResult;
                                                return this;
                                            }
                                        }.setParams(result));

                                    } else {
                                        mWebSearchForward.mProfileImageView.setVisibility(View.GONE);
                                    }
                                }

                                // Define click actions
                                for(TextView textView : mWebSearchForward.textViews) {
                                    // click
                                    textView.setOnClickListener(new View.OnClickListener() {
                                        BaseClass webSearchResult;

                                        @Override
                                        public void onClick(View v) {
                                            WebSearchDetailView.click(pointer, mWebSearchForward, webSearchResult, jsonStrings, MessageType.FORWARD);
                                        }

                                        public View.OnClickListener setParams(BaseClass webSearchResult) {
                                            this.webSearchResult = webSearchResult;
                                            return this;
                                        }
                                    }.setParams(result));

                                    // long click
                                    textView.setOnLongClickListener(new View.OnLongClickListener() {
                                        BaseClass webSearchResult;

                                        @Override
                                        public boolean onLongClick(View v) {
                                                /*
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                                                alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        clickCopyOrForwardMessage(which, webSearchResult);
                                                    }
                                                }).create().show();
                                                */
                                            return true;
                                        }

                                        public View.OnLongClickListener setParams(BaseClass webSearchResult) {
                                            this.webSearchResult = webSearchResult;
                                            return this;
                                        }
                                    }.setParams(result));
                                }

                                // Optional UI
                                if (result.apiSource.equals("yelp")) {

                                    // EXAMPLE: hide UI.
                                    mWebSearchForward.snippetTextView.setVisibility(View.GONE);
                                    mWebSearchForward.mProductPrice.setVisibility(View.GONE);
                                    mWebSearchForward.mSaleProductPrice.setVisibility(View.GONE);

                                    try {
                                        String ratingString = result.optional.get("rating").toString();
                                        float rating = Float.valueOf(ratingString.trim());
                                        mWebSearchForward.mRatingBar.setRating(rating);
                                        mWebSearchForward.mRatingBar.setIsIndicator(true);
                                        mWebSearchForward.mRatingBar.setVisibility(View.VISIBLE);

                                        mWebSearchForward.mYelpCategory.setVisibility(View.VISIBLE);

                                        if (mWebSearchForward.mYelpCategory != null) {
                                            // TODO: This is not a perfect SDK design.
                                            Object categoriesObject = result.optional.get("categories");
                                            JSONArray categories = (JSONArray) categoriesObject;

                                            if (categories.length() > 0) {
                                                mWebSearchForward.mYelpCategory.setText(categories.getString(0));
                                            } else {
                                                mWebSearchForward.mYelpCategory.setVisibility(View.GONE);
                                            }
                                        }

                                    } catch (Exception nfe) {
                                        System.out.println("NumberFormatException: " + nfe.getMessage());
                                        mWebSearchForward.mRatingBar.setVisibility(View.GONE);
                                        if (mWebSearchForward.mYelpCategory != null) {
                                            mWebSearchForward.mYelpCategory.setVisibility(View.GONE);
                                        }
                                    }
                                }  else if (result.apiSource.equals("amazon") || result.apiSource.equals("expedia_hotel")) {
                                    mWebSearchForward.snippetTextView.setVisibility(View.GONE);
                                    mWebSearchForward.mYelpCategory.setVisibility(View.GONE);
                                    mWebSearchForward.mRatingBar.setVisibility(View.GONE);
                                    mWebSearchForward.nameTextView.setMaxLines(1);
                                    mWebSearchForward.nameTextView.setEllipsize(TextUtils.TruncateAt.END);

                                    // Reset UI first
                                    mWebSearchForward.mProductPrice.setText("");
                                    mWebSearchForward.mSaleProductPrice.setText("");

                                    if (result.optional.containsKey("price")) {
                                        String price = result.optional.get("price").toString();
                                        if (price != null) {
                                            mWebSearchForward.mProductPrice.setText(price);
                                            mWebSearchForward.mProductPrice.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    if (result.optional.containsKey("salePrice")) {
                                        String salePrice = result.optional.get("salePrice").toString();
                                        if (salePrice != null) {
                                            mWebSearchForward.mSaleProductPrice.setText(salePrice);
                                            mWebSearchForward.mSaleProductPrice.setVisibility(View.VISIBLE);
                                            mWebSearchForward.mProductPrice.setPaintFlags(mWebSearchForward.mProductPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                        }
                                    }
                                } else if (result.apiSource.equals("youtube")){
                                    mWebSearchForward.snippetTextView.setMaxLines(2);
                                }
                                else {
                                    // TODO: This check could be removed.
                                    if (mWebSearchForward.mRatingBar != null) {
                                        mWebSearchForward.mRatingBar.setVisibility(View.GONE);
                                    }

                                    if (mWebSearchForward.mYelpCategory != null) {
                                        mWebSearchForward.mYelpCategory.setVisibility(View.GONE);
                                    }

                                    if (mWebSearchForward.mProductPrice != null) {
                                        mWebSearchForward.mProductPrice.setVisibility(View.GONE);
                                    }

                                    if (mWebSearchForward.mSaleProductPrice != null) {
                                        mWebSearchForward.mSaleProductPrice.setVisibility(View.GONE);
                                    }
                                }

                            }
                        });
                    }

                    @Override
                    public void onFailure() {
                        pointer.post(new Runnable() {
                            @Override
                            public void run() {
                                mSearchForwardView.setVisibility(GONE);
                            }
                        });
                    }
                };
                receiveProcess.sendGet(messageCubeParams);


            } else {
                // TODO: Regular message
                mMessageTextView.setText(text);
                // Linkify phone numbers, web urls, emails, and map addresses to allow users to
                // click on them and take the default intent.
                mMessageTextHasLinks = Linkify.addLinks(mMessageTextView, Linkify.ALL);
                mMessageTextView.setVisibility(View.VISIBLE);

                mSearchScrollView.setVisibility(GONE);
                mSearchForwardView.setVisibility(GONE);
            }

        } else {
            // Text is empty
            mMessageTextView.setVisibility(View.GONE);
            mSearchScrollView.setVisibility(GONE);
            mSearchForwardView.setVisibility(GONE);
            mMessageTextHasLinks = false;
        }
    }


    private void updateViewAppearance() {
        final Resources res = getResources();
        final ConversationDrawables drawableProvider = ConversationDrawables.get();
        final boolean incoming = mData.getIsIncoming();
        final boolean outgoing = !incoming;
        final boolean showArrow =  shouldShowMessageBubbleArrow();

        final int messageTopPaddingClustered =
                res.getDimensionPixelSize(R.dimen.message_padding_same_author);
        final int messageTopPaddingDefault =
                res.getDimensionPixelSize(R.dimen.message_padding_default);
        final int arrowWidth = res.getDimensionPixelOffset(R.dimen.message_bubble_arrow_width);
        final int messageTextMinHeightDefault = res.getDimensionPixelSize(
                R.dimen.conversation_message_contact_icon_size);
        final int messageTextLeftRightPadding = res.getDimensionPixelOffset(
                R.dimen.message_text_left_right_padding);
        final int textTopPaddingDefault = res.getDimensionPixelOffset(
                R.dimen.message_text_top_padding);
        final int textBottomPaddingDefault = res.getDimensionPixelOffset(
                R.dimen.message_text_bottom_padding);

        // These values depend on whether the message has text, attachments, or both.
        // We intentionally don't set defaults, so the compiler will tell us if we forget
        // to set one of them, or if we set one more than once.
        final int contentLeftPadding, contentRightPadding;
        final Drawable textBackground;
        final int textMinHeight;
        final int textTopMargin;
        final int textTopPadding, textBottomPadding;
        final int textLeftPadding, textRightPadding;

        if (mData.hasAttachments()) {
            if (shouldShowMessageTextBubble()) {
                // Text and attachment(s)
                contentLeftPadding = incoming ? arrowWidth : 0;
                contentRightPadding = outgoing ? arrowWidth : 0;
                textBackground = drawableProvider.getBubbleDrawable(
                        isSelected(),
                        incoming,
                        false /* needArrow */,
                        mData.hasIncomingErrorStatus());
                textMinHeight = messageTextMinHeightDefault;
                textTopMargin = messageTopPaddingClustered;
                textTopPadding = textTopPaddingDefault;
                textBottomPadding = textBottomPaddingDefault;
                textLeftPadding = messageTextLeftRightPadding;
                textRightPadding = messageTextLeftRightPadding;
            } else {
                // Attachment(s) only
                contentLeftPadding = incoming ? arrowWidth : 0;
                contentRightPadding = outgoing ? arrowWidth : 0;
                textBackground = null;
                textMinHeight = 0;
                textTopMargin = 0;
                textTopPadding = 0;
                textBottomPadding = 0;
                textLeftPadding = 0;
                textRightPadding = 0;
            }
        } else {
            // Text only
            contentLeftPadding = (!showArrow && incoming) ? arrowWidth : 0;
            contentRightPadding = (!showArrow && outgoing) ? arrowWidth : 0;
            textBackground = drawableProvider.getBubbleDrawable(
                    isSelected(),
                    incoming,
                    shouldShowMessageBubbleArrow(),
                    mData.hasIncomingErrorStatus());
            textMinHeight = messageTextMinHeightDefault;
            textTopMargin = 0;
            textTopPadding = textTopPaddingDefault;
            textBottomPadding = textBottomPaddingDefault;
            if (showArrow && incoming) {
                textLeftPadding = messageTextLeftRightPadding + arrowWidth;
            } else {
                textLeftPadding = messageTextLeftRightPadding;
            }
            if (showArrow && outgoing) {
                textRightPadding = messageTextLeftRightPadding + arrowWidth;
            } else {
                textRightPadding = messageTextLeftRightPadding;
            }
        }

        // These values do not depend on whether the message includes attachments
        final int gravity = incoming ? (Gravity.START | Gravity.CENTER_VERTICAL) :
                (Gravity.END | Gravity.CENTER_VERTICAL);
        final int messageTopPadding = shouldShowSimplifiedVisualStyle() ?
                messageTopPaddingClustered : messageTopPaddingDefault;
        final int metadataTopPadding = res.getDimensionPixelOffset(
                R.dimen.message_metadata_top_padding);

        // Update the message text/info views
        ImageUtils.setBackgroundDrawableOnView(mMessageTextAndInfoView, textBackground);
        mMessageTextAndInfoView.setMinimumHeight(textMinHeight);
        final LinearLayout.LayoutParams textAndInfoLayoutParams =
                (LinearLayout.LayoutParams) mMessageTextAndInfoView.getLayoutParams();
        textAndInfoLayoutParams.topMargin = textTopMargin;

        if (UiUtils.isRtlMode()) {
            // Need to switch right and left padding in RtL mode
            mMessageTextAndInfoView.setPadding(textRightPadding, textTopPadding, textLeftPadding,
                    textBottomPadding);
            mMessageBubble.setPadding(contentRightPadding, 0, contentLeftPadding, 0);
        } else {
            mMessageTextAndInfoView.setPadding(textLeftPadding, textTopPadding, textRightPadding,
                    textBottomPadding);
            mMessageBubble.setPadding(contentLeftPadding, 0, contentRightPadding, 0);
        }

        // Update the message row and message bubble views
        setPadding(getPaddingLeft(), messageTopPadding, getPaddingRight(), 0);
        mMessageBubble.setGravity(gravity);
        updateMessageAttachmentsAppearance(gravity);

        mMessageMetadataView.setPadding(0, metadataTopPadding, 0, 0);

        updateTextAppearance();

        requestLayout();
    }

    private void updateContentDescription() {
        StringBuilder description = new StringBuilder();

        Resources res = getResources();
        String separator = res.getString(R.string.enumeration_comma);

        // Sender information
        boolean hasPlainTextMessage = !(TextUtils.isEmpty(mData.getText()) ||
                mMessageTextHasLinks);
        if (mData.getIsIncoming()) {
            int senderResId = hasPlainTextMessage
                ? R.string.incoming_text_sender_content_description
                : R.string.incoming_sender_content_description;
            description.append(res.getString(senderResId, mData.getSenderDisplayName()));
        } else {
            int senderResId = hasPlainTextMessage
                ? R.string.outgoing_text_sender_content_description
                : R.string.outgoing_sender_content_description;
            description.append(res.getString(senderResId));
        }

        if (mSubjectView.getVisibility() == View.VISIBLE) {
            description.append(separator);
            description.append(mSubjectText.getText());
        }

        if (mMessageTextView.getVisibility() == View.VISIBLE) {
            if (mMessageTextView.getText().toString().contains("Contact Card")
                    && mMessageTextView.getText().toString().contains("Name:")
                    && mMessageTextView.getText().toString().contains("Phone Number")) {
                mContactCardView.setVisibility(VISIBLE);
            } else {
                mContactCardView.setVisibility(GONE);
            }
        }

        if (mMessageTextView.getVisibility() == View.VISIBLE) {
            // If the message has hyperlinks, we will let the user navigate to the text message so
            // that the hyperlink can be clicked. Otherwise, the text message does not need to
            // be reachable.
            if (mMessageTextHasLinks) {
                mMessageTextView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            } else {
                mMessageTextView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                description.append(separator);
                description.append(mMessageTextView.getText());
            }
        }

        if (mMessageTitleLayout.getVisibility() == View.VISIBLE) {
            description.append(separator);
            description.append(mTitleTextView.getText());

            description.append(separator);
            description.append(mMmsInfoTextView.getText());
        }

        if (mStatusTextView.getVisibility() == View.VISIBLE) {
            description.append(separator);
            description.append(mStatusTextView.getText());
        }

        if (mSimNameView.getVisibility() == View.VISIBLE) {
            description.append(separator);
            description.append(mSimNameView.getText());
        }

        if (mDeliveredBadge.getVisibility() == View.VISIBLE) {
            description.append(separator);
            description.append(res.getString(R.string.delivered_status_content_description));
        }

        setContentDescription(description);
    }

    private void updateMessageAttachmentsAppearance(final int gravity) {
        mMessageAttachmentsView.setGravity(gravity);

        // Tint image/video attachments when selected
        final int selectedImageTint = getResources().getColor(R.color.message_image_selected_tint);
        if (mMessageImageView.getVisibility() == View.VISIBLE) {
            if (isSelected()) {
                mMessageImageView.setColorFilter(selectedImageTint);
            } else {
                mMessageImageView.clearColorFilter();
            }
        }
        if (mMultiAttachmentView.getVisibility() == View.VISIBLE) {
            if (isSelected()) {
                mMultiAttachmentView.setColorFilter(selectedImageTint);
            } else {
                mMultiAttachmentView.clearColorFilter();
            }
        }
        for (int i = 0, size = mMessageAttachmentsView.getChildCount(); i < size; i++) {
            final View attachmentView = mMessageAttachmentsView.getChildAt(i);
            if (attachmentView instanceof VideoThumbnailView
                    && attachmentView.getVisibility() == View.VISIBLE) {
                final VideoThumbnailView videoView = (VideoThumbnailView) attachmentView;
                if (isSelected()) {
                    videoView.setColorFilter(selectedImageTint);
                } else {
                    videoView.clearColorFilter();
                }
            }
        }

        // If there are multiple attachment bubbles in a single message, add some separation.
        final int multipleAttachmentPadding =
                getResources().getDimensionPixelSize(R.dimen.message_padding_same_author);

        boolean previousVisibleView = false;
        for (int i = 0, size = mMessageAttachmentsView.getChildCount(); i < size; i++) {
            final View attachmentView = mMessageAttachmentsView.getChildAt(i);
            if (attachmentView.getVisibility() == View.VISIBLE) {
                final int margin = previousVisibleView ? multipleAttachmentPadding : 0;
                ((LinearLayout.LayoutParams) attachmentView.getLayoutParams()).topMargin = margin;
                // updateViewAppearance calls requestLayout() at the end, so we don't need to here
                previousVisibleView = true;
            }
        }
    }

    private void updateTextAppearance() {
        int messageColorResId;
        int statusColorResId = -1;
        int infoColorResId = -1;
        int timestampColorResId;
        int subjectLabelColorResId;
        if (isSelected()) {
            messageColorResId = R.color.message_text_color_incoming;
            statusColorResId = R.color.message_action_status_text;
            infoColorResId = R.color.message_action_info_text;
            if (shouldShowMessageTextBubble()) {
                timestampColorResId = R.color.message_action_timestamp_text;
                subjectLabelColorResId = R.color.message_action_timestamp_text;
            } else {
                // If there's no text, the timestamp will be shown below the attachments,
                // against the conversation view background.
                timestampColorResId = R.color.timestamp_text_outgoing;
                subjectLabelColorResId = R.color.timestamp_text_outgoing;
            }
        } else {
            messageColorResId = (mData.getIsIncoming() ?
                    R.color.message_text_color_incoming : R.color.message_text_color_outgoing);
            statusColorResId = messageColorResId;
            infoColorResId = R.color.timestamp_text_incoming;
            switch(mData.getStatus()) {

                case MessageData.BUGLE_STATUS_OUTGOING_FAILED:
                case MessageData.BUGLE_STATUS_OUTGOING_FAILED_EMERGENCY_NUMBER:
                    timestampColorResId = R.color.message_failed_timestamp_text;
                    subjectLabelColorResId = R.color.timestamp_text_outgoing;
                    break;

                case MessageData.BUGLE_STATUS_OUTGOING_YET_TO_SEND:
                case MessageData.BUGLE_STATUS_OUTGOING_SENDING:
                case MessageData.BUGLE_STATUS_OUTGOING_RESENDING:
                case MessageData.BUGLE_STATUS_OUTGOING_AWAITING_RETRY:
                case MessageData.BUGLE_STATUS_OUTGOING_COMPLETE:
                case MessageData.BUGLE_STATUS_OUTGOING_DELIVERED:
                    timestampColorResId = R.color.timestamp_text_outgoing;
                    subjectLabelColorResId = R.color.timestamp_text_outgoing;
                    break;

                case MessageData.BUGLE_STATUS_INCOMING_EXPIRED_OR_NOT_AVAILABLE:
                case MessageData.BUGLE_STATUS_INCOMING_DOWNLOAD_FAILED:
                    messageColorResId = R.color.message_text_color_incoming_download_failed;
                    timestampColorResId = R.color.message_download_failed_timestamp_text;
                    subjectLabelColorResId = R.color.message_text_color_incoming_download_failed;
                    statusColorResId = R.color.message_download_failed_status_text;
                    infoColorResId = R.color.message_info_text_incoming_download_failed;
                    break;

                case MessageData.BUGLE_STATUS_INCOMING_AUTO_DOWNLOADING:
                case MessageData.BUGLE_STATUS_INCOMING_MANUAL_DOWNLOADING:
                case MessageData.BUGLE_STATUS_INCOMING_RETRYING_AUTO_DOWNLOAD:
                case MessageData.BUGLE_STATUS_INCOMING_RETRYING_MANUAL_DOWNLOAD:
                case MessageData.BUGLE_STATUS_INCOMING_YET_TO_MANUAL_DOWNLOAD:
                    timestampColorResId = R.color.message_text_color_incoming;
                    subjectLabelColorResId = R.color.message_text_color_incoming;
                    infoColorResId = R.color.timestamp_text_incoming;
                    break;

                case MessageData.BUGLE_STATUS_INCOMING_COMPLETE:
                default:
                    timestampColorResId = R.color.timestamp_text_incoming;
                    subjectLabelColorResId = R.color.timestamp_text_incoming;
                    infoColorResId = -1; // Not used
                    break;
            }
        }
        final int messageColor = getResources().getColor(messageColorResId);
        mMessageTextView.setTextColor(messageColor);
        mMessageTextView.setLinkTextColor(messageColor);
        mSubjectText.setTextColor(messageColor);
        if (statusColorResId >= 0) {
            mTitleTextView.setTextColor(getResources().getColor(statusColorResId));
        }
        if (infoColorResId >= 0) {
            mMmsInfoTextView.setTextColor(getResources().getColor(infoColorResId));
        }
        if (timestampColorResId == R.color.timestamp_text_incoming &&
                mData.hasAttachments() && !shouldShowMessageTextBubble()) {
            timestampColorResId = R.color.timestamp_text_outgoing;
        }
        mStatusTextView.setTextColor(getResources().getColor(timestampColorResId));

        mSubjectLabel.setTextColor(getResources().getColor(subjectLabelColorResId));
        mSenderNameTextView.setTextColor(getResources().getColor(timestampColorResId));
    }

    /**
     * If we don't know the size of the image, we want to show it in a fixed-sized frame to
     * avoid janks when the image is loaded and resized. Otherwise, we can set the imageview to
     * take on normal layout params.
     */
    private void adjustImageViewBounds(final MessagePartData imageAttachment) {
        Assert.isTrue(ContentType.isImageType(imageAttachment.getContentType()));
        final ViewGroup.LayoutParams layoutParams = mMessageImageView.getLayoutParams();
        if (imageAttachment.getWidth() == MessagePartData.UNSPECIFIED_SIZE ||
                imageAttachment.getHeight() == MessagePartData.UNSPECIFIED_SIZE) {
            // We don't know the size of the image attachment, enable letterboxing on the image
            // and show a fixed sized attachment. This should happen at most once per image since
            // after the image is loaded we then save the image dimensions to the db so that the
            // next time we can display the full size.
            layoutParams.width = getResources()
                    .getDimensionPixelSize(R.dimen.image_attachment_fallback_width);
            layoutParams.height = getResources()
                    .getDimensionPixelSize(R.dimen.image_attachment_fallback_height);
            mMessageImageView.setScaleType(ScaleType.CENTER_CROP);
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            // ScaleType.CENTER_INSIDE and FIT_CENTER behave similarly for most images. However,
            // FIT_CENTER works better for small images as it enlarges the image such that the
            // minimum size ("android:minWidth" etc) is honored.
            mMessageImageView.setScaleType(ScaleType.FIT_CENTER);
        }
    }

    @Override
    public void onClick(final View view) {
        final Object tag = view.getTag();
        if (tag instanceof MessagePartData) {
            final Rect bounds = UiUtils.getMeasuredBoundsOnScreen(view);
            onAttachmentClick((MessagePartData) tag, bounds, false /* longPress */);
        } else if (tag instanceof String) {
            // Currently the only object that would make a tag of a string is a youtube preview
            // image
            UIIntents.get().launchBrowserForUrl(getContext(), (String) tag);
        }
    }

    @Override
    public boolean onLongClick(final View view) {
        if (view == mMessageTextView) {
            // Preemptively handle the long click event on message text so it's not handled by
            // the link spans.
            return performLongClick();
        }

        final Object tag = view.getTag();
        if (tag instanceof MessagePartData) {
            final Rect bounds = UiUtils.getMeasuredBoundsOnScreen(view);
            return onAttachmentClick((MessagePartData) tag, bounds, true /* longPress */);
        }

        return false;
    }

    @Override
    public boolean onAttachmentClick(final MessagePartData attachment,
            final Rect viewBoundsOnScreen, final boolean longPress) {
        return mHost.onAttachmentClick(this, attachment, viewBoundsOnScreen, longPress);
    }

    public ContactIconView getContactIconView() {
        return mContactIconView;
    }

    // Sort photos in MultiAttachLayout in the same order as the ConversationImagePartsView
    static final Comparator<MessagePartData> sImageComparator = new Comparator<MessagePartData>(){
        @Override
        public int compare(final MessagePartData x, final MessagePartData y) {
            return x.getPartId().compareTo(y.getPartId());
        }
    };

    static final Predicate<MessagePartData> sVideoFilter = new Predicate<MessagePartData>() {
        @Override
        public boolean apply(final MessagePartData part) {
            return part.isVideo();
        }
    };

    static final Predicate<MessagePartData> sAudioFilter = new Predicate<MessagePartData>() {
        @Override
        public boolean apply(final MessagePartData part) {
            return part.isAudio();
        }
    };

    static final Predicate<MessagePartData> sVCardFilter = new Predicate<MessagePartData>() {
        @Override
        public boolean apply(final MessagePartData part) {
            return part.isVCard();
        }
    };

    static final Predicate<MessagePartData> sImageFilter = new Predicate<MessagePartData>() {
        @Override
        public boolean apply(final MessagePartData part) {
            return part.isImage();
        }
    };

    interface AttachmentViewBinder {
        void bindView(View view, MessagePartData attachment);
        void unbind(View view);
    }

    final AttachmentViewBinder mVideoViewBinder = new AttachmentViewBinder() {
        @Override
        public void bindView(final View view, final MessagePartData attachment) {
            ((VideoThumbnailView) view).setSource(attachment, mData.getIsIncoming());
        }

        @Override
        public void unbind(final View view) {
            ((VideoThumbnailView) view).setSource((Uri) null, mData.getIsIncoming());
        }
    };

    final AttachmentViewBinder mAudioViewBinder = new AttachmentViewBinder() {
        @Override
        public void bindView(final View view, final MessagePartData attachment) {
            final AudioAttachmentView audioView = (AudioAttachmentView) view;
            audioView.bindMessagePartData(attachment, mData.getIsIncoming(), isSelected());
            audioView.setBackground(ConversationDrawables.get().getBubbleDrawable(
                    isSelected(), mData.getIsIncoming(), false /* needArrow */,
                    mData.hasIncomingErrorStatus()));
        }

        @Override
        public void unbind(final View view) {
            ((AudioAttachmentView) view).bindMessagePartData(null, mData.getIsIncoming(), false);
        }
    };

    final AttachmentViewBinder mVCardViewBinder = new AttachmentViewBinder() {
        @Override
        public void bindView(final View view, final MessagePartData attachment) {
            final PersonItemView personView = (PersonItemView) view;
            personView.bind(DataModel.get().createVCardContactItemData(getContext(),
                    attachment));
            personView.setBackground(ConversationDrawables.get().getBubbleDrawable(
                    isSelected(), mData.getIsIncoming(), false /* needArrow */,
                    mData.hasIncomingErrorStatus()));
            final int nameTextColorRes;
            final int detailsTextColorRes;
            if (isSelected()) {
                nameTextColorRes = R.color.message_text_color_incoming;
                detailsTextColorRes = R.color.message_text_color_incoming;
            } else {
                nameTextColorRes = mData.getIsIncoming() ? R.color.message_text_color_incoming
                        : R.color.message_text_color_outgoing;
                detailsTextColorRes = mData.getIsIncoming() ? R.color.timestamp_text_incoming
                        : R.color.timestamp_text_outgoing;
            }
            personView.setNameTextColor(getResources().getColor(nameTextColorRes));
            personView.setDetailsTextColor(getResources().getColor(detailsTextColorRes));
        }

        @Override
        public void unbind(final View view) {
            ((PersonItemView) view).bind(null);
        }
    };

    /**
     * A helper class that allows us to handle long clicks on linkified message text view (i.e. to
     * select the message) so it's not handled by the link spans to launch apps for the links.
     */
    private static class IgnoreLinkLongClickHelper implements OnLongClickListener, OnTouchListener {
        private boolean mIsLongClick;
        private final OnLongClickListener mDelegateLongClickListener;

        /**
         * Ignore long clicks on linkified texts for a given text view.
         * @param textView the TextView to ignore long clicks on
         * @param longClickListener a delegate OnLongClickListener to be called when the view is
         *        long clicked.
         */
        public static void ignoreLinkLongClick(final TextView textView,
                @Nullable final OnLongClickListener longClickListener) {
            final IgnoreLinkLongClickHelper helper =
                    new IgnoreLinkLongClickHelper(longClickListener);
            textView.setOnLongClickListener(helper);
            textView.setOnTouchListener(helper);
        }

        private IgnoreLinkLongClickHelper(@Nullable final OnLongClickListener longClickListener) {
            mDelegateLongClickListener = longClickListener;
        }

        @Override
        public boolean onLongClick(final View v) {
            // Record that this click is a long click.
            mIsLongClick = true;
            if (mDelegateLongClickListener != null) {
                return mDelegateLongClickListener.onLongClick(v);
            }
            return false;
        }

        @Override
        public boolean onTouch(final View v, final MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_UP && mIsLongClick) {
                // This touch event is a long click, preemptively handle this touch event so that
                // the link span won't get a onClicked() callback.
                mIsLongClick = false;
                return true;
            }

            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mIsLongClick = false;
            }
            return false;
        }
    }

}
