# Expense Tracker

An Android application built with **Jetpack Compose**, **Kotlin**, **Room**, **Hilt**, and **Firebase**.

## 📱 Features

- **Expense Tracking:** Log, organize, and categorize daily expenses easily.
- **Modern UI:** Built using Jetpack Compose and Material Design 3.
- **Local Persistence:** Powered by Room database for fast offline capability.
- **Firebase Authentication & Google Sign-In:** Secure login with Google Identity credentials.
- **Cloud Backup:** Integration with Google Drive API for data backup and synchronization.
- **Dependency Injection:** Clean architecture powered by Hilt (Dagger).
- **Background Operations:** WorkManager for scheduled background sync tasks.

## 🛠 Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- **Database:** [Room](https://developer.android.com/training/data-storage/room)
- **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Authentication:** [Firebase Auth](https://firebase.google.com/products/auth) & Google Identity API
- **Cloud Storage:** Google Drive API
- **Navigation:** Navigation Compose
- **Asynchronous Operations:** Kotlin Coroutines & Flow
- **Background Processing:** WorkManager

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- JDK 17
- Android SDK 29+ (Target SDK: 35)

### Setup Instructions

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/your-username/expense-tracker.aspx
   cd expense-tracker
   ```

2. **Firebase Setup:**
   - Create a project on the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with package name `com.baburamtechnologies.expensetracker`.
   - Download `google-services.json` and place it into the `app/` directory.

3. **Open & Build in Android Studio:**
   - Open Android Studio and choose **Open an Existing Project**.
   - Select the project folder.
   - Sync Gradle and Run on an emulator or physical device.

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
