# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn android.media.LoudnessCodecController
-dontwarn android.media.LoudnessCodecController$OnLoudnessCodecUpdateListener


-keep class com.facebook.infer.annotation.Nullsafe { *; }
-keep class com.facebook.infer.annotation.Nullsafe$Mode { *; }

-keep class com.ibm.icu.text.Bidi { *; }
-keep class com.zhihu.matisse.R$plurals { *; }

-keep class com.google.common.reflect.TypeToken { *; }
-keep class com.google.gson.internal.** { *; }
-keep class com.cam.scanner.scantopdf.android.util.PrefManager{ *; }
-keep class com.google.gson.Gson { *; }
-keep class com.google.gson.GsonBuilder { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.reflect.** { *; }
-keep class com.google.api.client.** { *; }
-keep class com.google.api.client.googleapis.json.** { *; }
-keep class com.google.api.client.json.** { *; }
-keep class com.google.api.client.http.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.google.common.** { *; }

-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

-keep class com.sharefiles.shareapps.filetransfer.shareit.transfer.** { *; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

-keep class * extends com.google.api.client.json.GenericJson {
*;
}
-keep class com.google.api.services.drive.** {
*;
}

-keep class com.cam.scanner.scantopdf.android.drive.** { *; }
-keep class com.cam.scanner.scantopdf.android.rest.** { *; }
-keep class com.cam.scanner.scantopdf.android.barcodereader.model.** { *; }
-keep class com.cam.scanner.scantopdf.android.models.** { *; }
-keep class com.cam.scanner.scantopdf.android.rest.** { *; }


