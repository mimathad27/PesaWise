# PesaWise

PesaWise is a financial literacy app. This repository now contains an Android starter app with Jetpack Compose authentication screens and a local SQLite database managed by Room.

## Auth scaffold

- Login and sign-up modes in one Compose screen
- Basic email, password length, required name, and password confirmation validation
- Local account creation and sign-in through a repository and ViewModel
- Room `users` table with a unique email index
- Passwords stored as PBKDF2-HMAC-SHA256 hashes with per-account random salts

This is a local prototype. Password reset, terms links, session persistence, and production authentication should be connected to the app's account service before release. The database holds user records only on the device; uninstalling the app removes them.

## Run

Open this folder in Android Studio and sync Gradle. Use JDK 17 and an Android SDK that includes API 36. The application id is `com.pesawise.app` and the minimum Android version is API 26.
