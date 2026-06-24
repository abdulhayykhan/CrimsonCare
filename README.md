# CrimsonCare

**CrimsonCare** is a secure, **100% offline** menstrual health companion and period tracking application. Built natively for Android using **Kotlin**, **Jetpack Compose**, and **Room Database**, it strictly adheres to **Material Design 3** guidelines.

## Core Philosophy: Absolute Privacy

Health data belongs strictly on the device. **CrimsonCare** operates with **zero network connections**, no third-party trackers, and no cloud sync services. All health logs are stored locally on a secure SQLite database via Android Room.

## Key Features

* **Dynamic Prediction Engine**: Automatically calculates the current cycle day, maps biological phases (Menstruation, Follicular, Ovulation, Luteal), and counts down to the next predicted period.
* **Fluid UI/UX**: Features an interactive circular canvas progress wheel, native Material 3 date pickers, phase-aware theme tinting, and responsive logging cards.
* **Local Settings Management**: Intelligently maintains personalized baselines, including the last cycle date, average cycle length, and average flow duration.

## Development Roadmap & Architecture

The application is architected across five distinct phases to ensure modularity.

### Phase 1: Foundation & Database

* **UserSettings Entity**: Single-row table storing cycle baselines in `YYYY-MM-DD` format.
* **DailyLog Entity**: Primary key date-indexed table capturing daily flow intensity (None, Light, Medium, Heavy) and symptoms (cramps, bloating, mood swings, fatigue).
* **Dashboard Logic**: Live cycle day calculation and prediction mapping via cycle offsets.

### Phase 2: Logging & Calendar

* Implementation of the isolated **Daily Logger** screen.
* Integration of the interactive **Calendar View** for historical cycle tracking.

### Phase 3: Analytics & Export

* Generation of local cycle **Insights** and analytics.
* **CSV Data Export** functionality to allow secure sharing with healthcare providers.

### Phase 4: Onboarding & Alerts

* Initial user **Onboarding flow** for baseline configuration.
* Fully local, offline **Notification system** for upcoming cycle alerts.

### Phase 5: Security & Polish

* App-level security implementation using **Biometric/PIN lock**.
* Complete visual polish with robust **Material 3 Dark Mode** support.

## Technical Stack

* **Language**: Kotlin
* **UI Toolkit**: Jetpack Compose
* **Local Storage**: Room Database (SQLite)
* **Design System**: Material Design 3

---

## 📄 License

This project is open-source and available for educational and commercial use under the MIT License.

---

**Made with ❤️ by [Abdul Hayy Khan](https://www.linkedin.com/in/abdulhayykhan/)**
