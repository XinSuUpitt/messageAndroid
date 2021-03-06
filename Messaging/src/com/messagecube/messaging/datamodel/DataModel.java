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

package com.messagecube.messaging.datamodel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import com.messagecube.messaging.Factory;
import com.messagecube.messaging.datamodel.action.Action;
import com.messagecube.messaging.datamodel.action.ActionService;
import com.messagecube.messaging.datamodel.action.BackgroundWorker;
import com.messagecube.messaging.datamodel.data.BlockedParticipantsData;
import com.messagecube.messaging.datamodel.data.BlockedParticipantsData.BlockedParticipantsDataListener;
import com.messagecube.messaging.datamodel.data.ContactListItemData;
import com.messagecube.messaging.datamodel.data.ContactPickerData;
import com.messagecube.messaging.datamodel.data.ContactPickerData.ContactPickerDataListener;
import com.messagecube.messaging.datamodel.data.ConversationData;
import com.messagecube.messaging.datamodel.data.ConversationData.ConversationDataListener;
import com.messagecube.messaging.datamodel.data.ConversationListData;
import com.messagecube.messaging.datamodel.data.ConversationListData.ConversationListDataListener;
import com.messagecube.messaging.datamodel.data.DraftMessageData;
import com.messagecube.messaging.datamodel.data.GalleryGridItemData;
import com.messagecube.messaging.datamodel.data.LaunchConversationData;
import com.messagecube.messaging.datamodel.data.LaunchConversationData.LaunchConversationDataListener;
import com.messagecube.messaging.datamodel.data.MediaPickerData;
import com.messagecube.messaging.datamodel.data.MessagePartData;
import com.messagecube.messaging.datamodel.data.ParticipantData;
import com.messagecube.messaging.datamodel.data.ParticipantListItemData;
import com.messagecube.messaging.datamodel.data.PeopleAndOptionsData;
import com.messagecube.messaging.datamodel.data.PeopleAndOptionsData.PeopleAndOptionsDataListener;
import com.messagecube.messaging.datamodel.data.PeopleOptionsItemData;
import com.messagecube.messaging.datamodel.data.SettingsData;
import com.messagecube.messaging.datamodel.data.SettingsData.SettingsDataListener;
import com.messagecube.messaging.datamodel.data.SubscriptionListData;
import com.messagecube.messaging.datamodel.data.VCardContactItemData;
import com.messagecube.messaging.util.Assert.DoesNotRunOnMainThread;
import com.messagecube.messaging.util.ConnectivityUtil;

public abstract class DataModel {
    private String mFocusedConversation;
    private boolean mConversationListScrolledToNewestConversation;

    public static DataModel get() {
        return Factory.get().getDataModel();
    }

    public static final void startActionService(final Action action) {
        get().getActionService().startAction(action);
    }

    public static final void scheduleAction(final Action action,
            final int code, final long delayMs) {
        get().getActionService().scheduleAction(action, code, delayMs);
    }

    public abstract ConversationListData createConversationListData(final Context context,
            final ConversationListDataListener listener, final boolean archivedMode);

    public abstract ConversationData createConversationData(final Context context,
            final ConversationDataListener listener, final String conversationId);

    public abstract ContactListItemData createContactListItemData();

    public abstract ContactPickerData createContactPickerData(final Context context,
            final ContactPickerDataListener listener);

    public abstract MediaPickerData createMediaPickerData(final Context context);

    public abstract GalleryGridItemData createGalleryGridItemData();

    public abstract LaunchConversationData createLaunchConversationData(
            LaunchConversationDataListener listener);

    public abstract PeopleOptionsItemData createPeopleOptionsItemData(final Context context);

    public abstract PeopleAndOptionsData createPeopleAndOptionsData(final String conversationId,
            final Context context, final PeopleAndOptionsDataListener listener);

    public abstract VCardContactItemData createVCardContactItemData(final Context context,
            final MessagePartData data);

    public abstract VCardContactItemData createVCardContactItemData(final Context context,
            final Uri vCardUri);

    public abstract ParticipantListItemData createParticipantListItemData(
            final ParticipantData participant);

    public abstract BlockedParticipantsData createBlockedParticipantsData(Context context,
            BlockedParticipantsDataListener listener);

    public abstract SubscriptionListData createSubscriptonListData(Context context);

    public abstract SettingsData createSettingsData(Context context, SettingsDataListener listener);

    public abstract DraftMessageData createDraftMessageData(String conversationId);

    public abstract ActionService getActionService();

    public abstract BackgroundWorker getBackgroundWorkerForActionService();

    @DoesNotRunOnMainThread
    public abstract DatabaseWrapper getDatabase();

    // Allow DataModel to coordinate with activity lifetime events.
    public abstract void onActivityResume();

    abstract void onCreateTables(final SQLiteDatabase db);

    public void setFocusedConversation(final String conversationId) {
        mFocusedConversation = conversationId;
    }

    public boolean isFocusedConversation(final String conversationId) {
        return !TextUtils.isEmpty(mFocusedConversation)
                && TextUtils.equals(mFocusedConversation, conversationId);
    }

    public void setConversationListScrolledToNewestConversation(
            final boolean scrolledToNewestConversation) {
        mConversationListScrolledToNewestConversation = scrolledToNewestConversation;
    }

    public boolean isConversationListScrolledToNewestConversation() {
        return mConversationListScrolledToNewestConversation;
    }

    /**
     * If a new message is received in the specified conversation, will the user be able to
     * observe it in some UI within the app?
     * @param conversationId conversation with the new incoming message
     */
    public boolean isNewMessageObservable(final String conversationId) {
        return isConversationListScrolledToNewestConversation()
                || isFocusedConversation(conversationId);
    }

    public abstract void onApplicationCreated();

    public abstract ConnectivityUtil getConnectivityUtil();

    public abstract SyncManager getSyncManager();
}
