# PesaWise

PesaWise is a financial literacy app. This Android app uses Jetpack Compose, Firebase Authentication, and Room persistence components.

## Firebase authentication

- Login and sign-up modes in one Compose screen
- Email validation, password checks, loading and error states
- Firebase Authentication handles account creation, sign-in, and password-reset emails
- Firebase stores the account profile and keeps the signed-in session across app launches
- The signed-in app has Expenses and Account tabs, with password recovery and logout in Account

### Firebase setup for teammates

1. Keep `app/google-services.json` in the project. It contains Firebase app identifiers and is required to build the Android app.
2. In the Firebase Console, open **Authentication → Sign-in method** and enable **Email/Password**.
3. Sync Gradle in Android Studio and run the app.

The Android package name registered with Firebase is `com.pesawise.app`. Password reset uses Firebase's configured email template and reset link. Local Room account records are no longer used for authentication.

## Run

Open this folder in Android Studio and sync Gradle. Use JDK 17 and an Android SDK that includes API 36. The application id is `com.pesawise.app` and the minimum Android version is API 26.


## Expense Tracking Explained

The Expense Tracking page helps users record and review what they spend.

### Features
- **Header:** clean "Expense Tracking" title.
- **Upload Bank / M-Pesa Statement:** tap the card to simulate importing a statement. Transactions are auto-categorized by keyword (e.g. KPLC → Utilities, Uber → Transport).
- **Total Tracked Expense:** shows the total spent (KSh) and the number of expenses, updating instantly.
- **Category filter chips:** All, Housing, Food & Groceries, Utilities, Transport, Entertainment and Transfers.
- **Add Expense:** a floating button opens a dialog for a title, amount and category. The new expense appears in the list and the total updates.
- **Transactions list:** color-coded category icons, names, dates and formatted amounts, newest first.

### Files
| File | Purpose |
|---|---|
| `data/Expense.kt` | Expense model and category enum (label, icon, color) |
| `ui/ExpenseViewModel.kt` | State, filtering, totals, add expense, simulated import |
| `ui/ExpenseTrackingScreen.kt` | The page UI and the Add Expense dialog |

### Current limitations
- Expenses are stored in memory only, so they reset when the app closes. Room persistence is planned.
- Statement upload is simulated with sample data. No real file is read.
- Expenses are not yet linked to a user account.
- There is no log out button yet.

### Setup note
The category icons need `androidx.compose.material:material-icons-extended` in `build.gradle.kts (Module :app)`.
