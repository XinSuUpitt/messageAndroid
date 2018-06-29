## Messaging @ android-7.1.2_r33

### Checkout package
```bash
git clone https://android.googlesource.com/platform/packages/apps/Messaging
git clone https://android.googlesource.com/platform/frameworks/opt/vcard
git clone https://android.googlesource.com/platform/frameworks/opt/chips
git clone https://android.googlesource.com/platform/frameworks/opt/photoviewer
git clone https://android.googlesource.com/platform/external/libphonenumber
git clone https://android.googlesource.com/platform/frameworks/opt/colorpicker
git clone https://android.googlesource.com/platform/frameworks/ex
```

### Checkout branch
```bash
git checkout android-7.1.2_r33
```


```
cd MessageCube_SMS/mobi.messagecube.sdk
cp build/outputs/aar/mobi.messagecube.sdk-release.aar ../Messaging/libs/mobi.messagecube.sdk-release-1.2.0.aar
```


### How to build libframesequence.so
1. checkout giflib
```
git clone https://android.googlesource.com/platform/external/giflib
cd giflib
```

2. Transfer ```Android.bp``` to ```Android.mk```

3. Build giflib 
```
/Users/xiaodoudou/Library/Android/sdk/ndk-bundle/ndk-build NDK_PROJECT_PATH=. APP_BUILD_SCRIPT=./Android.mk
```
It generates static libraris under ```obj``` folder

4. Copy static libraris and head files to framesequence external folder
```
cp config.h ex_framesequence/jni/external/giflib/config.h
cp gif_lib.h ex_framesequence/jni/external/giflib/gif_lib.h
cp obj/local/* ex_framesequence/jni/external/giflib/*
```

5. Update Android.mk in ex_framesequence

Reference: http://blog.csdn.net/haunghui6579/article/details/49968087


### Application installation failed provider name is already used by another application
To install mutiple applications on one Android, we need to change:
1. application id in Gradle
2. Rename com.messagecube.messaging.datamodel.MediaScratchFileProvider to com.messagecube.messaging.datamodel.MediaScratchFileProvider1
Also update
```
public static final String AUTHORITY =
        "com.messagecube.messaging.datamodel.MediaScratchFileProvider1";
```

3. Rename com.messagecube.messaging.datamodel.MessagingContentProvider to com.messagecube.messaging.datamodel.MessagingContentProvider
```
public static final String AUTHORITY =
        "com.messagecube.messaging.datamodel.MessagingContentProvider1";
```

4. Rename com.messagecube.messaging.datamodel.MediaScratchFileProvider to com.messagecube.messaging.datamodel.MediaScratchFileProvider1
```
static final String AUTHORITY = "com.messagecube.messaging.datamodel.MmsFileProvider1";
```

