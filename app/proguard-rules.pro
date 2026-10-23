# Veyra ProGuard Rules

# Keep Kotlinx Serialization
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep Room entities and DAOs
-keep class dev.whaltermc.veyra.data.model.** { *; }
-keep class dev.whaltermc.veyra.data.db.** { *; }

# Keep ViewModels
-keep class dev.whaltermc.veyra.viewmodel.** { *; }

# Keep AuthManager
-keep class dev.whaltermc.veyra.auth.** { *; }

# Keep LaunchManager and JreManager
-keep class dev.whaltermc.veyra.launch.** { *; }
-keep class dev.whaltermc.veyra.jvm.** { *; }

# Keep RenderBackendManager
-keep class dev.whaltermc.veyra.render.** { *; }

# Keep Coil
-keep class coil.** { *; }

# Keep OkHttp/Retrofit/Moshi
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keep class com.squareup.moshi.** { *; }

# Keep Kotlinx Coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Kotlinx Datetime
-keep class kotlinx.datetime.** { *; }

# Keep AndroidX
-keep class androidx.** { *; }

# Keep Google Guava (ListenableFuture)
-keep class com.google.common.** { *; }