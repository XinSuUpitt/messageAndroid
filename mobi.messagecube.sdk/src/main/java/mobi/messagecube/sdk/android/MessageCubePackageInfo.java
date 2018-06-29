package mobi.messagecube.sdk.android;

import android.content.pm.PackageInfo;

public class MessageCubePackageInfo {

    public PackageInfo pInfo;
    public String mPackageName;

    public MessageCubePackageInfo(PackageInfo pInfo, String packageName) {
        this.pInfo = pInfo;
        this.mPackageName = packageName;
    }

}
