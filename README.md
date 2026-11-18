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
