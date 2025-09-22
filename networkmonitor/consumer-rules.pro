# Add project specific ProGuard rules here.
# NetworkMonitor library consumer ProGuard rules

# Keep NetworkMonitor public API
-keep class com.summitcodeworks.networkmonitor.NetworkMonitor { *; }
-keep class com.summitcodeworks.networkmonitor.interceptor.NetworkMonitorInterceptor { *; }
-keep class com.summitcodeworks.networkmonitor.websocket.NetworkMonitorWebSocketListener { *; }

# Keep data models
-keep class com.summitcodeworks.networkmonitor.model.** { *; }

# Keep Room entities
-keep class com.summitcodeworks.networkmonitor.database.** { *; }

# Gson rules for data serialization
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation