# Keep rules for the Pindou app.

# kotlinx.serialization: keep generated serializers and @Serializable members.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer {
    *;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
    @kotlinx.serialization.SerialName <fields>;
}

# Models persisted to disk / bundled as JSON assets must keep their serializers.
-keep class com.pindou.core.model.** { *; }

# ONNX Runtime: JNI entry points and session/tensor types are reached natively.
-keep class ai.onnxruntime.** { *; }
-keepclassmembers class ai.onnxruntime.** { *; }
-dontwarn ai.onnxruntime.**
