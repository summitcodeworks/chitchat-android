# NetworkMonitor

An in-house network monitoring library for Android applications, similar to Chucker. This library provides comprehensive monitoring of HTTP requests and WebSocket connections with a beautiful UI for inspection.

## Features

🚀 **HTTP Request/Response Monitoring**
- Intercepts all HTTP traffic using OkHttp interceptor
- Records request/response headers, body, timing, and size
- Supports JSON, XML, and plain text body inspection
- Error tracking and status code analysis

🔌 **WebSocket Monitoring**
- Real-time WebSocket event tracking
- Message sent/received logging
- Connection lifecycle monitoring
- Error and failure tracking

📱 **Rich UI Experience**
- Material Design 3 interface
- Real-time network activity summary
- Searchable request/response logs
- Failed requests filtering
- Detailed request/response inspection

🔔 **Persistent Notifications**
- Shows ongoing network activity in notification
- Tap notification to launch NetworkMonitor UI
- Customizable notification content
- Low-priority, non-intrusive notifications

💾 **Data Persistence**
- Room database for offline log storage
- Automatic cleanup of old logs
- Export functionality (JSON/CSV)
- Share logs with development team

📊 **Analytics & Summary**
- Network usage statistics
- Response time analytics
- Success/failure rate tracking
- Data transfer monitoring

## Installation

### 1. Add the library to your project

In your app-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":networkmonitor"))

    // Required OkHttp dependency
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
```

### 2. Initialize NetworkMonitor

In your `Application` class:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize only in debug builds
        if (BuildConfig.DEBUG) {
            NetworkMonitor.initialize(this)
        }
    }
}
```

### 3. Add HTTP Interceptor

For Retrofit/OkHttp integration:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        networkMonitorInterceptor: NetworkMonitorInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(networkMonitorInterceptor)
            .build()
    }
}
```

### 4. WebSocket Monitoring

For WebSocket monitoring:

```kotlin
val networkMonitor = NetworkMonitor.getInstance()
val listener = networkMonitor?.createWebSocketListener(originalListener)

val webSocket = client.newWebSocket(request, listener)
```

## Usage

### Basic Usage

```kotlin
// Get NetworkMonitor instance
val networkMonitor = NetworkMonitor.getInstance()

// Launch UI manually
networkMonitor?.launchUI()

// Clear all logs
networkMonitor?.clearLogs()

// Check if monitoring is enabled
val isEnabled = networkMonitor?.isEnabled() ?: false

// Enable/disable monitoring
networkMonitor?.setEnabled(true)
```

### Advanced Configuration

```kotlin
// Custom notification management
networkMonitor?.showNotification()
networkMonitor?.hideNotification()

// Get logs count
val logsCount = networkMonitor?.getLogsCount() ?: 0
```

### Integration with Debug Menu

```kotlin
@Composable
fun DebugScreen() {
    val context = LocalContext.current

    DebugCard(
        title = "Network Monitor",
        description = "View HTTP and WebSocket logs",
        onClick = {
            NetworkMonitor.getInstance()?.launchUI()
        }
    )
}
```

## UI Features

### Main Screen
- **HTTP Requests Tab**: All HTTP requests with method, URL, status, and timing
- **WebSocket Tab**: WebSocket events including connections, messages, and errors
- **Summary Tab**: Network usage analytics and statistics
- **Failed Tab**: Only failed requests and errors

### Search & Filter
- Search by URL, method, or response content
- Filter by request type (HTTP/WebSocket)
- Time-based filtering
- Status code filtering

### Export Options
- JSON format with full request/response data
- CSV format for spreadsheet analysis
- Share via email, messaging, or cloud storage
- Include summary statistics

## Database Schema

### NetworkLog Entity
```kotlin
@Entity(tableName = "network_logs")
data class NetworkLog(
    val requestId: String,
    val type: NetworkType,
    val method: String?,
    val url: String,
    val requestHeaders: String?,
    val requestBody: String?,
    val responseCode: Int?,
    val responseHeaders: String?,
    val responseBody: String?,
    val requestTime: Long,
    val responseTime: Long?,
    val duration: Long?,
    val requestSize: Long,
    val responseSize: Long,
    val error: String?,
    val protocol: String?,
    val isSSL: Boolean
)
```

### WebSocketEvent Entity
```kotlin
@Entity(tableName = "websocket_events")
data class WebSocketEvent(
    val connectionId: String,
    val url: String,
    val eventType: WebSocketEventType,
    val message: String?,
    val timestamp: Long,
    val error: String?
)
```

## Permissions

The library requires the following permissions:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## ProGuard/R8

The library includes consumer ProGuard rules automatically. No additional configuration needed.

## Performance Considerations

- **Memory Usage**: Logs are stored in SQLite database with automatic cleanup
- **Performance Impact**: Minimal overhead, designed for debug builds only
- **Storage**: Configurable log retention policy
- **Battery**: Low-priority notifications and efficient database operations

## Troubleshooting

### Common Issues

1. **Notification not showing**
   - Check notification permissions (Android 13+)
   - Verify initialization in Application class

2. **No HTTP requests captured**
   - Ensure NetworkMonitorInterceptor is added to OkHttpClient
   - Check if interceptor is added before other interceptors

3. **WebSocket events not logged**
   - Use NetworkMonitorWebSocketListener wrapper
   - Ensure original listener is passed correctly

### Debug Mode Only

Always wrap NetworkMonitor initialization in debug checks:

```kotlin
if (BuildConfig.DEBUG) {
    NetworkMonitor.initialize(this)
}
```

## Architecture

```
NetworkMonitor Library
├── Core
│   ├── NetworkMonitor (Main entry point)
│   └── NetworkMonitorModule (DI configuration)
├── Interceptor
│   └── NetworkMonitorInterceptor (HTTP monitoring)
├── WebSocket
│   └── NetworkMonitorWebSocketListener (WebSocket monitoring)
├── Database
│   ├── NetworkMonitorDatabase (Room database)
│   ├── NetworkLogDao (HTTP logs)
│   └── WebSocketEventDao (WebSocket events)
├── UI
│   ├── NetworkMonitorScreen (Main UI)
│   ├── NetworkMonitorViewModel (State management)
│   └── NetworkMonitorActivity (Entry activity)
├── Notification
│   └── NetworkMonitorNotificationManager (Notifications)
└── Utils
    └── ExportUtils (Data export functionality)
```

## License

Internal use only - ChitChat Application

## Contributing

This is an internal library. For issues or enhancements, please contact the development team.