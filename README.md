# CrimsonCare

A secure, 100% offline period and menstrual health tracking application.

## Architecture

* **Framework**: Native Android built with Kotlin and Jetpack Compose.
* **Database**: Android Room (SQLite). Stores all cycle metrics and daily logs locally.
* **State Management**: MVVM (Model-View-ViewModel) architecture.
* **Security**: Zero network connectivity. App-level Biometric/PIN lock integration.

## Features

* **Dynamic Predictions**: Automated phase mapping (Menstruation, Follicular, Ovulation, Luteal) based on local data offsets.
* **Smart Alerts**: Proactive local notifications for upcoming periods, PMS, and fertile windows using `AlarmManager`.
* **Glassmorphism UI**: Custom forced-light theme with translucent cards and blurred gradients under Material Design 3 guidelines.
* **Offline Data Export**: Generates local CSV files directly to the device's public Downloads folder for healthcare visits.

## Security

Operates in a strict offline sandbox. No cloud sync, no tracking, and no external API calls. Authentication is handled via local AndroidX Biometrics.

## Local Setup

**Prerequisites**: Android Studio, JDK 17+.

1. **Clone repository**:

```bash
git clone <repository-url>
cd CrimsonCare

```

2. **Open Project**:
Launch Android Studio and select **Open**. Navigate to the `CrimsonCare` directory.
3. **Sync Gradle**:
Allow Android Studio to download necessary AndroidX, Room, and Compose dependencies.
4. **Run application**:
Connect a physical Android device via USB (enable USB debugging) or start an AVD emulator. Click **Run**.

## Deployment Strategy (Sideloading)

1. Open Android Studio.
2. Navigate to **Build** > **Generate Signed Bundle / APK**.
3. Select **APK** and click Next.
4. **Keystore**: Provide an existing Keystore or create a new one to sign the app.
5. **Build Variant**: Select the **release** variant.
6. Build the APK.
7. Transfer the generated `.apk` file to the target Android device and install it manually via the file manager.

## Local Data Models

* `UserSettings` (Entity): Single-row table maintaining `last_period_date`, `avg_cycle_length`, and `avg_period_duration`.
* `DailyLog` (Entity): Date-indexed records storing `flow_level` (None, Light, Medium, Heavy) and serialized `symptoms` lists.
* `CrimsonDao`: Exposes SQLite queries for cycle histories, analytics, and symptom frequencies.
* `PeriodCalculator`: Pure Kotlin utility for calculating phase offsets and future prediction markers based on database history.

## Project Structure

```text
CrimsonCare/
├── app/src/main/java/com/example/
│   ├── data/             # Room Database, Entities, DAO, Repository
│   ├── ui/               # Jetpack Compose Screens (Dashboard, Settings, Calendar)
│   ├── ui/theme/         # Glassmorphism Colors, Typography, Theme
│   ├── util/             # Phase Calculator, CSV Exporter, Notification Scheduler
│   └── receiver/         # AlarmManager BroadcastReceivers
├── app/src/main/res/
│   ├── mipmap-*/         # Adaptive App Icons (Moon/Leaf Motif)
│   └── values/           # Strings, XML themes
└── build.gradle.kts      # Gradle configuration (Room, Compose, Biometrics)

```

## 📄 License

This project is open-source and available for educational and commercial use under the MIT License.

---

**Made with ❤️ by [Abdul Hayy Khan](https://www.google.com/search?q=https://www.linkedin.com/in/abdulhayykhan)**
