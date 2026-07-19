# kotlinx.serialization keeps generated serializers off the shrinker's radar.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class ai.vcmirror.data.** {
    *** Companion;
}
-keepclasseswithmembers class ai.vcmirror.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
