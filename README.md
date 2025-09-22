# ChitChat - Android Chat Application

A comprehensive chat application built with **Material 3**, **Jetpack Compose**, and **Clean Architecture** that integrates with the ChitChat backend APIs.

## 🚀 Features

- **Material 3 Design**: Modern, expressive UI with Material 3 components
- **Real-time Messaging**: WebSocket integration for instant messaging
- **Firebase Authentication**: Phone number-based authentication
- **Voice & Video Calls**: WebRTC-based calling functionality
- **Status Updates**: Share text, images, and videos as status
- **Group Chats**: Create and manage group conversations
- **Media Sharing**: Upload and share images, videos, documents
- **Push Notifications**: Real-time notifications for messages and calls
- **Offline Support**: Local data caching with Room database

## 🏗️ Architecture

The app follows **Clean Architecture** principles with:

### Data Layer
- **Room Database**: Local data storage with entities and DAOs
- **Retrofit**: REST API communication
- **WebSocket**: Real-time messaging with OkHttp
- **Repository Pattern**: Data source abstraction

### Domain Layer
- **Use Cases**: Business logic encapsulation
- **Domain Models**: Core business entities
- **Repository Interfaces**: Data source contracts

### Presentation Layer
- **MVVM**: Model-View-ViewModel pattern
- **Jetpack Compose**: Declarative UI
- **Material 3**: Modern design system
- **Navigation Component**: Type-safe navigation

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Database**: Room
- **Networking**: Retrofit + OkHttp
- **Real-time**: WebSocket
- **Authentication**: Firebase Auth
- **Image Loading**: Coil
- **Navigation**: Navigation Component
- **Async**: Coroutines + Flow
- **Build System**: Gradle with Version Catalogs

## 📱 Screenshots

*Screenshots will be added once the app is built and running*

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Android SDK 24+
- Kotlin 2.0.21+
- JDK 11+

### Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ChitChat
   ```

2. **Firebase Configuration**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Enable Authentication with Phone Number provider
   - Download `google-services.json` and place it in the `app/` directory
   - Update the package name in Firebase console to match `com.summitcodeworks.chitchat`

3. **Backend Configuration**
   - Update the base URL in `NetworkModule.kt`:
     ```kotlin
     private const val BASE_URL = "http://65.1.185.194:9101/"
     ```
   - For local development, use:
     ```kotlin
     private const val BASE_URL = "http://localhost:9101/"
     ```

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

### Project Structure

```
app/
├── src/main/java/com/summitcodeworks/chitchat/
│   ├── data/
│   │   ├── local/           # Room database
│   │   ├── remote/          # API services & DTOs
│   │   ├── repository/      # Repository implementations
│   │   └── mapper/          # Data mappers
│   ├── domain/
│   │   ├── model/           # Domain models
│   │   └── usecase/         # Use cases
│   ├── presentation/
│   │   ├── screen/          # Compose screens
│   │   ├── viewmodel/       # ViewModels
│   │   ├── state/           # UI state classes
│   │   └── navigation/      # Navigation setup
│   ├── di/                  # Dependency injection
│   └── ui/theme/            # Material 3 theme
```

## 🔧 Configuration

### API Endpoints

The app integrates with the following ChitChat backend services:

- **Base URL**: `http://65.1.185.194:9101/`
- **Authentication**: Firebase token-based
- **WebSocket**: `ws://65.1.185.194:9101/ws/messages`

### Permissions

The app requires the following permissions:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## 📚 API Integration

### Authentication Flow

1. User enters phone number
2. Firebase sends OTP
3. User verifies OTP
4. App sends Firebase ID token to backend
5. Backend returns JWT token for API calls

### WebSocket Events

```kotlin
// Message types
NEW_MESSAGE, MESSAGE_READ, MESSAGE_DELIVERED
USER_TYPING, CALL_INITIATED, CALL_ANSWERED
USER_ONLINE, USER_OFFLINE, NEW_STATUS
```

### Key API Endpoints

- `POST /api/users/authenticate` - Firebase authentication
- `GET /api/messages/conversation/{userId}` - Get messages
- `POST /api/messages/send` - Send message
- `POST /api/calls/initiate` - Start call
- `POST /api/status/create` - Create status

## 🎨 UI Components

### Material 3 Theme

- **Primary**: WhatsApp Green (`#075E54`)
- **Secondary**: Light Green (`#25D366`)
- **Background**: Light Gray (`#FAFAFA`)
- **Surface**: White (`#FFFFFF`)

### Key Screens

1. **Splash Screen**: App loading with branding
2. **Authentication**: Phone number verification
3. **Home Screen**: Chat list with tabs
4. **Chat Screen**: Real-time messaging
5. **Status Screen**: View and create status
6. **Calls Screen**: Call history and management

## 🔒 Security

- **JWT Authentication**: Secure API communication
- **Firebase Security**: Phone number verification
- **Data Encryption**: Room database encryption
- **Network Security**: HTTPS for production

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run lint checks
./gradlew lint
```

## 📦 Dependencies

### Core Libraries
- `androidx.compose.bom:2024.10.00`
- `androidx.compose.material3`
- `androidx.navigation:navigation-compose:2.7.6`
- `androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0`

### Architecture
- `com.google.dagger:hilt-android:2.48`
- `androidx.room:room-runtime:2.6.1`
- `androidx.room:room-ktx:2.6.1`

### Network
- `com.squareup.retrofit2:retrofit:2.9.0`
- `com.squareup.okhttp3:okhttp:4.12.0`
- `com.google.code.gson:gson:2.10.1`

### Firebase
- `com.google.firebase:firebase-bom:32.7.0`
- `com.google.firebase:firebase-auth-ktx`
- `com.google.firebase:firebase-messaging-ktx`

### Utilities
- `io.coil-kt:coil-compose:2.5.0`
- `androidx.work:work-runtime-ktx:2.9.0`
- `com.google.accompanist:accompanist-permissions:0.32.0`

## 🚀 Deployment

### Build Variants

- **Debug**: Development with logging
- **Release**: Production optimized

### Signing

```bash
# Generate signed APK
./gradlew assembleRelease

# Generate AAB for Play Store
./gradlew bundleRelease
```

## 🌐 API Integration

### REST API Endpoints
The app integrates with the complete ChitChat backend API including User Management, Messaging, Groups, Calls, Status Updates, Media, and Notifications.

### WebSocket Integration
Real-time features via WebSocket including messaging, call signaling, status updates, group events, user presence, and notifications.

### Error Handling
Comprehensive error handling with network error classification, retry logic, offline support, and user-friendly error messages.

### Data Synchronization
Local-first approach with background sync, conflict resolution, and real-time updates via WebSocket.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the [Wiki](wiki-url) for documentation
- Review the [FAQ](faq-url) for common questions

## 🔮 Roadmap

- [ ] Voice message recording
- [ ] Message reactions and replies
- [ ] Dark theme optimization
- [ ] Message encryption
- [ ] File sharing improvements
- [ ] Group admin controls
- [ ] Message scheduling
- [ ] Chat backup/restore

---

**Built with ❤️ using Jetpack Compose and Material 3**
