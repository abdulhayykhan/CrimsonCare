# 🩸 CrimsonCare

```text
  ██████╗██████╗ ██╗███╗   ██╗███████╗██████╗ ███╗   ██╗██████╗  █████╗ ██████╗ ███████╗
 ██╔════╝██╔══██╗██║████╗  ██║██╔════╝██╔═══██╗████╗  ██║██╔════╝ ██╔══██╗██╔══██╗██╔════╝
 ██║     ██████╔╝██║██╔██╗ ██║███████╗██║   ██║██╔██╗ ██║██║      ███████║██████╔╝█████╗  
 ██║     ██╔══██╗██║██║╚██╗██║╚════██║██║   ██║██║╚██╗██║██║      ██╔══██║██╔══██╗██╔══╝  
 ╚██████╗██║  ██║██║██║ ╚████║███████║╚██████╔╝██║ ╚████║╚██████╗ ██║  ██║██║  ██║███████╗
  ╚═════╝╚═╝  ╚═╝╚═╝╚═╝  ╚═══╝╚══════╝ ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝
            A Private, 100% Offline Cycle Tracker & Health Companion
```

![Android](https://img.shields.io/badge/Android-36-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Room DB](https://img.shields.io/badge/Room%20DB-SQLite-4285F4?style=for-the-badge&logo=sqlite&logoColor=white)
![Biometric](https://img.shields.io/badge/Security-Biometric%20%2F%20PIN-red?style=for-the-badge&logo=android&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

---

## 🩸 What is CrimsonCare?

CrimsonCare is a **private, 100% offline, native Android menstrual health companion and cycle tracking application**. Built with modern **Kotlin**, **Jetpack Compose**, and **Room Database** under Material Design 3 guidelines, CrimsonCare empowers users with accurate cycle predictions and health logging without compromising user privacy.

CrimsonCare operates entirely client-side on device with **zero telemetry, zero cloud tracking, and zero third-party network connections**. All personal health data remains strictly encrypted and sandboxed on local storage.

---

## 🌐 Application Previews & Details

| Attribute | Details |
|-----------|---------|
| **Platform** | Android 7.0+ (API Level 24+) |
| **Package Name** | `com.aistudio.crimsoncare.vznwqd` |
| **Architecture** | Modern Android Architecture (MVVM + Repository Pattern) |
| **Privacy Model** | 100% Local Device Storage (Zero Cloud Sync) |

---

## ✨ Feature List

### 🩸 Intelligent Phase & Cycle Predictions
- Automated tracking of cycle days and biological phases: **Menstruation**, **Follicular Phase**, **Fertile Window / Ovulation**, and **Luteal Phase**.
- Dynamic countdown wheel highlighting days remaining until the next predicted cycle.
- Personalized health advice and phase-specific lifestyle recommendations.

### 📝 Comprehensive Daily Health Logging
- Log flow intensity levels (`None`, `Light`, `Medium`, `Heavy`).
- Multi-symptom chip selection covering cramps, bloating, mood swings, fatigue, headaches, acne, and breast tenderness.
- Free-form personal journal notes for qualitative health tracking.

### 📅 Interactive Calendar & Phase Timeline
- Visual monthly calendar grid color-coding biological cycle phases.
- Real-time indicator overlays highlighting logged symptoms, flow levels, and fertile windows.
- Easy retro-logging and date navigation.

### 🔒 Privacy, Biometrics & PIN Lock
- **Biometric Security:** Integrated AndroidX Biometric prompt (Fingerprint / Face Unlock).
- **PIN Lock Backup:** Customizable 4-digit PIN lock screen guarding sensitive health records.
- **Strict Privacy:** Zero cloud storage, tracking scripts, or analytics background tasks.

### 📊 Health Insights & Symptom Analytics
- Interactive cycle length history and average metrics calculation.
- Frequency breakdown charts visualizing recurring symptoms across past cycles.
- Summary stats report tailored for personal health reviews.

### 🔔 Local Discrete Reminders & Notifications
- Automated period predictions schedule discrete local notifications via Android `AlarmManager`.
- Customizable reminder timing (e.g., 1 to 3 days before predicted period start).
- Offline notification triggers maintaining complete privacy.

### 📤 Data Ownership & CSV Export
- **Medical CSV Export:** Instant generation and sharing of cycle history formatted as CSV for doctor visits or personal offline backups.
- **Complete Data Control:** One-click full data wipe and reset capabilities in settings.

---

## 🏗️ Architecture

```text
┌────────────────────────────────────────────────────────────────────────┐
│                               UI LAYER                                 │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ Jetpack Compose / Material Design 3 (Glassmorphism Cards)     │   │
│   │                                                                │   │
│   │  ├─ MainActivity.tsx (App Lock Router & Top-level Navigation) │   │
│   │  ├─ CrimsonDashboard.kt (Circular Status Wheel & Bottom Nav)   │   │
│   │  ├─ DailyLoggerScreen.kt (Flow, Symptoms & Journal Logger)     │   │
│   │  ├─ CalendarScreen.kt (Color-coded Monthly Cycle Grid)         │   │
│   │  ├─ InsightsScreen.kt (Analytics, Trends & CSV Exporter)       │   │
│   │  ├─ SettingsScreen.kt (PIN, Biometrics, Notification Config)   │   │
│   │  └─ AppLockScreen.kt (Biometric Prompt & PIN Validation)       │   │
│   └────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────┬──────────────────────────────────────┘
                                  │
┌─────────────────────────────────▼──────────────────────────────────────┐
│                             VIEW MODEL                                 │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ CrimsonViewModel.kt (StateFlow Manager & ViewModel)            │   │
│   │  ├─ PeriodCalculator.kt (Phase Detection & Cycle Algorithms)   │   │
│   │  └─ CsvExporter.kt (CSV File Generator & Export Provider)      │   │
│   └────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────┬──────────────────────────────────────┘
                                  │
┌─────────────────────────────────▼──────────────────────────────────────┐
│                             DATA LAYER                                 │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ CrimsonRepository.kt (Coroutines & Reactive Flow Streams)      │   │
│   │  ├─ CrimsonDao.kt (Room DAO Queries & Transaction Handlers)    │   │
│   │  ├─ CrimsonDatabase.kt (SQLite Database Instance)             │   │
│   │  ├─ DailyLog.kt & UserSettings.kt (Room Entity Schema)         │   │
│   │  └─ CycleNotificationScheduler.kt (AlarmManager Alarm Engine) │   │
│   └────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

### Client

| Technology | Role |
|------------|------|
| **Kotlin** | Primary Programming Language |
| **Jetpack Compose** | Modern Declarative UI Framework |
| **Material Design 3** | Visual Styling & Glassmorphic Cards |
| **Room Database** | Local SQLite Object-Relational Mapping (ORM) |
| **Kotlin Coroutines & Flow** | Asynchronous State Streams & Reactive Data |
| **AndroidX Biometric** | Hardware-level Fingerprint & Face Authentication |
| **KSP (Kotlin Symbol Processing)** | Code Generation for Room DAO & Entities |

### Testing & Infrastructure

| Tool / Library | Purpose |
|----------------|---------|
| **Robolectric** | Fast local JVM unit testing without emulators |
| **Roborazzi** | Automated screenshot testing and UI regression testing |
| **AlarmManager & BroadcastReceiver** | Offline local notification scheduling |
| **Gradle (Kotlin DSL)** | Build system & configuration management |

---

## ⚙️ How It Works

### 1. Dynamic Cycle Phase Engine
The `PeriodCalculator` evaluates elapsed days since the user's last recorded period start date relative to their configured cycle length (e.g., 28 days) and period duration (e.g., 5 days):
- **Menstruation:** Days 1 to Period Length.
- **Follicular Phase:** Days after period until Fertile Window.
- **Fertile Window & Ovulation:** ~5 days preceding ovulation up to Ovulation Day (typically Cycle Length minus 14 days).
- **Luteal Phase:** Post-ovulation until the start of the next cycle.

### 2. Offline-First Privacy & Local Storage
All entities (`UserSettings` and `DailyLog`) are persisted strictly inside an encrypted local SQLite database via **Room**. Network permissions are non-existent, ensuring zero potential for remote data leakage.

### 3. Local AlarmManager Notification Scheduler
When reminders are enabled in Settings, `CycleNotificationScheduler` calculates the exact trigger timestamp for upcoming cycle events and schedules an offline system alarm via `AlarmManager` targeting `CycleNotificationReceiver`.

---

## 📁 Project Structure

```text
CrimsonCare/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/                   Database Schema & Repository Layer
│   │   │   │   │   ├── CrimsonDao.kt       Room Data Access Object interface
│   │   │   │   │   ├── CrimsonDatabase.kt  Room Database definition
│   │   │   │   │   ├── CrimsonRepository.kt Unified data access layer
│   │   │   │   │   ├── DailyLog.kt          Daily symptoms & flow entity
│   │   │   │   │   └── UserSettings.kt      User preferences & cycle config entity
│   │   │   │   │
│   │   │   │   ├── ui/                     Jetpack Compose UI Screens & ViewModels
│   │   │   │   │   ├── AppLockScreen.kt    Biometric & PIN verification screen
│   │   │   │   │   ├── CalendarScreen.kt   Monthly color-coded cycle calendar
│   │   │   │   │   ├── CrimsonDashboard.kt Primary status wheel & dashboard view
│   │   │   │   │   ├── CrimsonViewModel.kt Reactive StateFlow coordinator
│   │   │   │   │   ├── DailyLoggerScreen.kt Symptom, flow, & notes logger
│   │   │   │   │   ├── InsightsScreen.kt   Cycle statistics & CSV exporter view
│   │   │   │   │   ├── OnboardingScreen.kt First-launch setup wizard
│   │   │   │   │   ├── SettingsScreen.kt   Security & reminder configuration
│   │   │   │   │   └── theme/              M3 Color, Typography & GlassCard components
│   │   │   │   │
│   │   │   │   ├── util/                   Utility Modules
│   │   │   │   │   ├── CsvExporter.kt      CSV export formatter & FileProvider hook
│   │   │   │   │   ├── CycleNotificationScheduler.kt AlarmManager scheduler
│   │   │   │   │   └── PeriodCalculator.kt Phase calculation engine
│   │   │   │   │
│   │   │   │   ├── receiver/               Broadcast Receivers
│   │   │   │   │   └── CycleNotificationReceiver.kt Local notification trigger
│   │   │   │   │
│   │   │   │   └── MainActivity.kt         Root activity & routing logic
│   │   │   │
│   │   │   └── AndroidManifest.xml         Android system manifest
│   │   │
│   │   └── test/                           Local JVM Unit & Roborazzi Screenshot Tests
│   │       ├── ExampleRobolectricTest.kt   Robolectric test suite
│   │       └── GreetingScreenshotTest.kt   Roborazzi screenshot tests
│   │
│   └── build.gradle.kts                    App-level dependencies & build rules
│
├── build.gradle.kts                        Root build configuration
├── settings.gradle.kts                     Gradle settings & project name
├── gradle.properties                       JVM args & Kotlin compiler options
└── README.md                               Project documentation
```

---

## 🚀 Local Setup

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or JDK 11+
- Android SDK 36 (Minimum API Level 24)

### Step 1 — Clone the Repository

```bash
git clone https://github.com/abdulhayykhan/CrimsonCare.git
cd CrimsonCare
```

### Step 2 — Compile and Sync Gradle

```bash
# Verify compilation via Gradle command line
gradle compileDebugSources
```

### Step 3 — Run Tests & Roborazzi Screenshot Verification

```bash
# Run unit tests and Robolectric tests
gradle :app:testDebugUnitTest

# Verify UI Screenshot tests with Roborazzi
gradle :app:verifyRoborazziDebug
```

---

## 📡 Build & Deployment

To build an unsigned debug or signed release APK:

```bash
# Assemble Debug APK
gradle :app:assembleDebug

# Assemble Release APK
gradle :app:assembleRelease
```

The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 License

This project is licensed under the MIT License:

```text
MIT License

Copyright (c) 2026 CrimsonCare Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

**Made with ❤️ by [Abdul Hayy Khan](https://www.linkedin.com/in/abdulhayykhan)**
