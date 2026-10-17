# ProGuard / R8 Rules for TDlxHT Security & Obfuscation
# Protects application logic, obfuscates class names, and prevents reverse engineering

# Preserve Kotlin Serialization DTOs for Supabase Sync
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keep class com.example.data.service.Cloud* { *; }
-keepclassmembers class com.example.data.service.Cloud* { *; }

# Preserve Room Database Entities and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Preserve Supabase Kotlin SDK
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# Preserve AndroidX Compose runtime & animations
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Ktor HTTP Engine (used by Supabase)
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Preserve Line Numbers for Crash Reporting without revealing source code structure
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
