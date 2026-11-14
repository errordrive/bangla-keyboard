# ProGuard / R8 Rules for BanglaKeyboard

# Keep native methods and classes with native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class com.openbangla.keyboard.engine.** { *; }

# Keep Room DAOs and Entities
-keep class androidx.room.** { *; }
-keep class com.openbangla.keyboard.data.db.** { *; }

# Keep Compose runtime
-keep class androidx.compose.** { *; }

# Google Mobile Ads
-keep public class com.google.android.gms.ads.** {
   public *;
}

# Google Play Billing
-keep class com.android.billingclient.api.** { *; }

# ML Kit Translation
-keep class com.google.mlkit.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
