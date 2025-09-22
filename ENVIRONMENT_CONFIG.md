# Environment Configuration

The ChitChat app now supports easy switching between local and production environments for both API and WebSocket connections.

## Environments

### Local
- **API URL**: `http://192.168.0.152:9101/`
- **WebSocket URL**: `ws://192.168.0.152:9101/`

### Production
- **API URL**: `http://65.1.185.194:9101/`
- **WebSocket URL**: `ws://65.1.185.194:9101/`

## How to Switch Environments

### Method 1: Environment Selector UI
1. Open the app and go to the Auth/Login screen
2. Look for the environment selector button in the top-right corner
3. Click on it to see a dialog with available environments
4. Select either "Local" or "Production"
5. The setting is automatically saved and will persist across app restarts

### Method 2: Programmatically (for developers)
```kotlin
// In any class with access to EnvironmentManager
@Inject
lateinit var environmentManager: EnvironmentManager

// Switch to local environment
environmentManager.setEnvironment(Environment.LOCAL)

// Switch to production environment
environmentManager.setEnvironment(Environment.PRODUCTION)

// Get current environment
val currentEnv = environmentManager.currentEnvironment.value
```

## Technical Implementation

### Files Added/Modified:
1. **Environment.kt** - Defines available environments with their URLs
2. **EnvironmentManager.kt** - Manages environment selection and persistence
3. **EnvironmentSelector.kt** - UI component for environment selection
4. **EnvironmentViewModel.kt** - ViewModel for environment management
5. **NetworkModule.kt** - Updated to use dynamic environment URLs
6. **WebSocket clients** - Updated to use dynamic environment URLs
7. **AuthScreen.kt** - Added environment selector UI

### Key Features:
- ✅ Simple enum-based environment configuration
- ✅ Persistent environment selection (saved in SharedPreferences)
- ✅ Real-time environment switching
- ✅ Unified configuration for both API and WebSocket URLs
- ✅ Clean UI for easy environment switching
- ✅ Automatic dependency injection throughout the app

### WebSocket Endpoints:
- **Messages**: `{baseUrl}ws/messages`
- **Calls**: `{baseUrl}ws/calls`
- **Status**: `{baseUrl}ws/status`

## Adding New Environments

To add a new environment:

1. Add a new entry to the `Environment` enum in `Environment.kt`:
```kotlin
STAGING(
    displayName = "Staging",
    apiBaseUrl = "http://staging.server.com:9101/",
    webSocketBaseUrl = "ws://staging.server.com:9101/"
)
```

2. The environment will automatically appear in the UI selector.

## Notes

- The environment setting persists across app restarts
- Default environment is LOCAL
- Environment changes take effect immediately for new network requests
- WebSocket connections will use the new environment on next connection