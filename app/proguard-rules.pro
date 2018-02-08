# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/jonas/android-sdk-linux/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# App
-dontwarn com.nononsenseapps.feeder.**
-keep class com.nononsenseapps.feeder.** { *; }
-keep interface com.nononsenseapps.feeder.** { *; }

-dontwarn com.nononsenseapps.jsonfeed.**
-keep class com.nononsenseapps.jsonfeed.** { *; }
-keep interface com.nononsenseapps.jsonfeed.** { *; }

-dontwarn com.nononsenseapps.filepicker.**
-keep class com.nononsenseapps.filepicker.** { *; }
-keep interface com.nononsenseapps.filepicker.** { *; }

# JodaTime
-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

# OkHttp
-keep class okio.** { *; }
-keep interface okio.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Glide
-keep class com.bumptech.** { *; }
-keep interface com.bumptech.** { *; }

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Moshi
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *

# Jsoup
-keep class org.jsoup.** { *; }
-keep interface org.jsoup.** { *; }
-keep class org.jdom2.** { *; }
-dontwarn org.jaxen.**
-dontwarn javax.**

# Tagsoup
-keep class org.ccil.cowan.** { *; }
-keep interface org.ccil.cowan.** { *; }

# Rome
-keep class com.rometools.** { *; }
-keep interface com.rometools.** { *; }

# Bah
-dontwarn org.slf4j.**
-dontwarn sun.misc.**

# Kotlin
-keep class kotlin.Metadata { *; }
-keep class kotlin.** { *; }
-keep interface kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep interface kotlinx.** { *; }
