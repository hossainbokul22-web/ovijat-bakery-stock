# -----------------------------------------------------------
# Ovijat Bakery Stock Management - ProGuard Rules
# -----------------------------------------------------------

# Keep Kotlin metadata and attributes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Room Database Rules
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Keep Entity Data Classes and DAOs
-keep class com.ovijat.bakerystock.data.entity.** { *; }
-keep class com.ovijat.bakerystock.data.dao.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Jetpack Compose Rules
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.ui.** { *; }

# FileProvider Rule for Export
-keep class androidx.core.content.FileProvider { *; }