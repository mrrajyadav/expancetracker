# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>(...);
}
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.paging.LimitOffsetDataSource

# Hilt
-keep class dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# Google API Client / Drive
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.drive.** { *; }
-keep class com.google.auth.** { *; }
-dontwarn com.google.common.collect.MinMaxPriorityQueue

# Kotlin Serialization
-keepattributes *Annotation*, Enums, Signature
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Firebase
-keep class com.google.firebase.** { *; }
