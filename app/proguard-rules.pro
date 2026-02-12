# Disable warning for optional security providers (tidak dipakai, aman diabaikan)
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# Keep Coil image loader classes (karena kamu pakai coil di dependencies)
-keep class coil.** { *; }
-keep interface coil.** { *; }

# Keep custom widgets to avoid obfuscation (biar view dari package ini ga error)
#-keep class id.vern.wearlevelinsight.widget.** { *; }
