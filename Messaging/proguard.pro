#  Copyright (C) 2015 The Android Open Source Project
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
# Keep enough data for stack traces
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,*Annotation*

# Keep classes and methods that have the guava @VisibleForTesting annotation
-keep @com.google.common.annotations.VisibleForTesting class *
-keepclassmembers class * {
  @com.google.common.annotations.VisibleForTesting *;
}

# Keep methods that have the @VisibleForAnimation annotation
-keep interface com.android.messaging.annotation.VisibleForAnimation
-keepclassmembers class * {
  @com.android.messaging.annotation.VisibleForAnimation *;
}

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.app.Fragment
-keep public class com.android.vcard.* { *; }

-keep class android.support.v4.* { *; }
-keep class android.support.v4.*.* { *; }
-keep class android.support.v7.* { *; }
-keep class android.support.v7.*.* { *; }

# Keep rastermill classes that need to be accessed from native code (JNI)
-keep class android.support.rastermill.** { *; }

# Preserve the name of the getCaller method so it can find itself in the stack trace it generates
-keepclassmembers public class com.android.messaging.util.DebugUtils {
  public static java.lang.StackTraceElement getCaller(...);
}

# Keep the static fields of referenced inner classes of auto-generated R classes, in case we
# access those fields by reflection (e.g. EmojiMarkup)
-keepclassmembers class **.R$* {
    public static <fields>;
}

-dontwarn com.google.common.**
-dontwarn com.google.errorprone.**
-dontwarn com.squareup.picasso.**
-dontwarn okio.**

-keep public class * extends mobi.messagecube.sdk.cards.CardsUpdater