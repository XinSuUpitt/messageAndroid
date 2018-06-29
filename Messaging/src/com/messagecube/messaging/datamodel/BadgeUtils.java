package com.messagecube.messaging.datamodel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static android.os.Looper.getMainLooper;


// TODO: How to display count of notifications in app launcher icon
// https://stackoverflow.com/questions/17565307/how-to-display-count-of-notifications-in-app-launcher-icon
public class BadgeUtils {

    public static final String PREFS_NAME = "ConversationInfoList";
    public static final String UNREAD_KEY = "unread";

    public static void removeConversationIdInBadgeInBackgroundThread(Context context, String conversationId) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        HashSet<String> conversationIds = (HashSet<String>) settings.getStringSet(UNREAD_KEY, new HashSet<String>());

        System.out.println(conversationIds.toString());

        conversationIds.remove(conversationId);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(UNREAD_KEY, conversationIds);
        editor.commit();

        setBadgeSamsung(context, conversationIds.size());
        setBadgeSony(context, conversationIds.size());
    }

    public static void setBadge(Context context) {
        MessageNotificationState.ConversationInfoList conversationInfoList = MessageNotificationState.createConversationInfoList();

        if (conversationInfoList == null) {
            System.err.println("setBadge() conversationInfoList == null");

        } else {
            HashSet<String> conversationIds = new HashSet<String>();
            for (MessageNotificationState.ConversationLineInfo convInfo : conversationInfoList.mConvInfos) {
                conversationIds.add(convInfo.mConversationId);
            }
            
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putStringSet(UNREAD_KEY, conversationIds);
            editor.commit();

            setBadgeSamsung(context, conversationIds.size());
            setBadgeSony(context, conversationIds.size());
        }
    }

    public static void clearBadge(Context context) {
        setBadgeSamsung(context, 0);
        clearBadgeSony(context);
    }


    private static void setBadgeSamsung(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }

    private static void setBadgeSony(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }

        Intent intent = new Intent();
        intent.setAction("mobi.messagecube.app.action.UPDATE_BADGE");
        intent.putExtra("mobi.messagecube.app.intent.extra.badge.ACTIVITY_NAME", launcherClassName);
        intent.putExtra("mobi.messagecube.app.intent.extra.badge.SHOW_MESSAGE", true);
        intent.putExtra("mobi.messagecube.app.intent.extra.badge.MESSAGE", String.valueOf(count));
        intent.putExtra("mobi.messagecube.app.intent.extra.badge.PACKAGE_NAME", context.getPackageName());

        context.sendBroadcast(intent);
    }


    private static void clearBadgeSony(Context context) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }

        Intent intent = new Intent();
        intent.setAction("mobi.messagecube.app.action.UPDATE_BADGE");
        intent.putExtra("mobi.messagecube.app.intent.extra.badge.ACTIVITY_NAME", launcherClassName);
        intent.putExtra("mobi.messagecube.app.intent.extra.badge.SHOW_MESSAGE", false);
        intent.putExtra("mobi.messagecube.app.intent.extra.badge.MESSAGE", String.valueOf(0));
        intent.putExtra("mobi.messagecube.app.intent.extra.badge.PACKAGE_NAME", context.getPackageName());

        context.sendBroadcast(intent);
    }

    private static String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }


}
