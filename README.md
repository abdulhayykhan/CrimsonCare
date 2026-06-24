# CrimsonCare

CrimsonCare is a private, 100% offline, secure menstrual health companion and period tracking application built natively using **Kotlin**, **Jetpack Compose**, and **Room Database** under Material Design 3 guidelines.

## Key Design & Architecture Highlights

- **100% Offline & Private**: Zero external network connections, trackers, or cloud sync services. All health logs are stored locally on the secure SQLite database via Android Room.
- **Local Settings Store**: Intelligently maintains personalized settings like the date of your last period, average cycle length, and average period length.
- **Dynamic Prediction Model**: Calculates your current cycle day, determines your biological phase (Menstruation, Follicular, Fertile/Ovulation, Luteal), and counts down to the predicted start of your next period.
- **Responsive Fluid UI**: Features a beautiful circular canvas progress wheel, Native Date Pickers, active cycle-phase theme tints, and interactive logging cards.

## Setup & Implementation (Phase 1)

### 1. Database & Entities
- **UserSettings**: Single row table storing average cycle/period lengths and last recorded period start date in `YYYY-MM-DD` format.
- **DailyLog**: Capture daily symptoms (cramps, bloating, mood swings, fatigue) and flow intensities (None, Light, Medium, Heavy) linked via database primary key date indexes.

### 2. Live Predictions
- **Current Cycle Day**: Elapsed days since the last period start date.
- **Next Period Prediction**: Automated timeline mapping using calculated cycle offsets.

---

## License

This project is licensed under the MIT License:

```
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

*CrimsonCare is designed and maintained with care by [Abdul Hayy Khan](https://www.linkedin.com/in/abdulhayykhan).*
