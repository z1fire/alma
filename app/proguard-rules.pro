# Data model is serialized to JSON with kotlinx.serialization; keep it intact.
-keepattributes *Annotation*, InnerClasses
-keep class com.z1fire.alma.data.** { *; }
-keepclassmembers class com.z1fire.alma.** { *** Companion; }
-keepclasseswithmembers class com.z1fire.alma.** { kotlinx.serialization.KSerializer serializer(...); }
