# Consumer ProGuard rules for soundwavecomposelib
# Keep all public classes and methods in the library
-keep public class com.soundwave.compose.lib.** {
    public *;
}

# Keep Compose functions
-keep class com.soundwave.compose.lib.**Kt {
    public *;
}