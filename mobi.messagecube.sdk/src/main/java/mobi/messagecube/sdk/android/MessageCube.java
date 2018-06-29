package mobi.messagecube.sdk.android;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import mobi.messagecube.sdk.openAPI.FacebookNativeAdProcess;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class MessageCube {

    static boolean hasInitialized = false;

    static MessageCubePackageInfo messageCubePackageInfo;

    static boolean isAuthorized = false;
    static String application_id;
    static String key1;
    static String key2;

    static String facebookNativeAdId;

    public static void with(Context context, String var1, String var2) {
        hasInitialized = true;

        // Get build/version number of Android application
        try {
            String packageName = context.getPackageName();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(packageName, 0);

            messageCubePackageInfo = new MessageCubePackageInfo(pInfo, packageName);

            System.out.println("packageName ... ");
            System.out.println(packageName);

            String version = pInfo.versionName;
            System.out.println(version);

            application_id = packageName;
            key1 = var1;
            key2 = var2;

            // Get Facebook native ad id
            getFacebookNativeAdIdFromCloud();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Init Mixpanel to track user data.
        String projectToken = "1f9b4107070551d59f285c8cf2195ccd"; // e.g.: "1ef7e30d2a58d27f4b90c42e31d6d7ad"
        MixpanelAPI mixpanel = MixpanelAPI.getInstance(context, projectToken);
    }

    public static boolean getHasInitialized() {
        return hasInitialized;
    }

    public static void auth() {

    }

    public static String getApplicationId() {
        return application_id;
    }

    public static String getKey1() {
        return key1;
    }

    public static String getKey2() {
        return key2;
    }

    public static void getFacebookNativeAdIdFromCloud() {

        FacebookNativeAdProcess facebookNativeAdProcess = new FacebookNativeAdProcess() {
            @Override
            public void onSuccess(String nativeAdId) {
                facebookNativeAdId = nativeAdId;
            }

            @Override
            public void onFailure() {
                super.onFailure();
            }
        };

        facebookNativeAdProcess.sendGet();
    }

    public static String getFacebookNativeAdId() {
        return facebookNativeAdId;
    }

}
