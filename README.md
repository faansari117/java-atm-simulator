# Metro Apex Bank - Production-Ready ATM Simulation Application

An enterprise-grade, high-fidelity Automated Teller Machine (ATM) simulation application engineered in Java using modern Swing architecture. Built with strict separation of concerns, decoupling core banking transactional business logic from the graphical presentation layer.

---

## 🏛 1. Architectural Highlights & Overview

The application is structured following Clean Architecture / MVC principles:

```
d:\Demo Project\
├── src\
│   └── com\atm\
│       ├── Main.java                          # Application Entry Point & Look & Feel Setup
│       ├── model\                             # Domain Entities & Account Models
│       │   ├── AccountType.java               # Enum: CHECKING and SAVINGS
│       │   ├── Account.java                   # Thread-safe account balance & daily limit tracking
│       │   ├── TransactionType.java           # Enum: DEPOSIT, WITHDRAWAL, TRANSFER_OUT/IN, etc.
│       │   ├── TransactionRecord.java         # Immutable audit ledger entry model
│       │   └── Customer.java                  # Cardholder identity, PIN hash, 3-strike counter
│       ├── exception\                         # Explicit Custom Exception Topology
│       │   ├── ATMException.java              # Base checked exception
│       │   ├── InvalidPinException.java       # PIN mismatch with attempt counter & lockout state
│       │   ├── InsufficientFundsException.java# Requested amount vs available balance
│       │   ├── DailyLimitExceededException.java # Daily $1,000.00 withdrawal ceiling breach
│       │   ├── InvalidTransactionException.java # Negative amounts, zero, invalid inputs
│       │   └── AccountLockedException.java    # Terminal lockout upon 3 strikes
│       ├── service\                           # Decoupled Banking Core Services
│       │   ├── BankingService.java            # Service contract interface
│       │   ├── BankingServiceImpl.java        # Core business rules, synchronization & limits
│       │   └── LedgerService.java             # Real-time ledger logging & mini-statements
│       ├── ui\                                # GUI & Presentation Layer (Java Swing)
│       │   ├── ATMFrame.java                  # Window coordinator with dynamic CardLayout
│       │   ├── UITheme.java                   # Dark slate banking palette, tokens & fonts
│       │   ├── component\                     # Reusable GUI Components
│       │   │   ├── RoundedPanel.java          # Anti-aliased rounded card panels
│       │   │   ├── ModernButton.java          # Smooth hover/click states & color variants
│       │   │   ├── NumericKeypadPanel.java    # Realistic 0-9 ATM physical keypad
│       │   │   └── HeaderPanel.java           # Branding, live clock, 256-bit encryption badge
│       │   └── panels\                        # Dynamic Card Panel System
│       │       ├── AuthPanel.java             # Screen 1: PIN keypad, status box & 3-strike meter
│       │       ├── DashboardPanel.java        # Screen 2: Multi-account matrix & 8-action grid
│       │       ├── TransactionPanel.java      # Screen 3: Universal deposit/withdraw/transfer form
│       │       └── LedgerPanel.java           # Screen 4: Scrollable audit ledger & thermal receipt
│       └── test\                              # Automated Test Suite
│           └── ATMServiceTest.java            # Unit & integration verification of banking rules
├── bin\                                       # Compiled class files
├── atm-simulator.jar                          # Standalone executable JAR
├── run.bat                                    # One-click launcher script
├── build.bat                                  # Source compiler & JAR packaging script
├── test.bat                                   # Automated test suite runner script
└── README.md                                  # Complete engineering documentation
```

---

## 🖥 2. GUI & Dynamic Card Panel System

The application runs a fluid `CardLayout` manager inside `ATMFrame`, swapping screens seamlessly without opening multiple disjointed windows:

### **Screen 1: Authentication Panel (`AuthPanel`)**
- **Smart Chip Reader Bar:** Visual indicator displaying inserted card `4532 •••• •••• 8921`.
- **Dynamic Security Status Box:** Real-time feedback showing current security status (`Security Attempts: [ ● ● ● ] - 3 remaining`).
- **Secure PIN Field:** Centered `JPasswordField` with large, crisp masked characters.
- **On-Screen Numeric Keypad:** Realistic 3x4 physical ATM keypad (keys 1–9, `0`, `CLEAR`, `⌫ Backspace`, plus `ENTER / SUBMIT`).
- **3-Strike Lockout Engine:** On the 3rd consecutive failed attempt, the screen transitions to a crimson lockout state, disables all input components, and displays a safety dialog informing that the card is retained. Includes an administrative reset button for testing convenience.

### **Screen 2: Main Dashboard Panel (`DashboardPanel`)**
- **Customer Greeting & Telemetry:** Cardholder welcome banner and card status badge.
- **Multi-Account Matrix Widgets:**
  - **Checking Account Card:** Displays live available balance (`CHK-89210-01`).
  - **Savings Account Card:** Displays high-yield savings balance (`SAV-89210-02`).
  - **Daily Withdrawal Allowance Card:** Real-time meter displaying remaining daily cash quota out of the $1,000.00 ceiling, with a dynamic color-coded progress bar.
- **Action Button Grid:**
  1. `[ 🔍 Check Balance ]` - Instant liquid asset inquiry dialog.
  2. `[ 💰 Deposit Funds ]` - Credits Checking or Savings account.
  3. `[ 💵 Withdraw Cash ]` - Dispenses physical cash within balance and daily quota limits.
  4. `[ ⇄ Transfer Funds ]` - Atomic inter-allocation cross-transfer.
  5. `[ 📄 Mini-Statement ]` - Displays formatted records of the last 5 transactions.
  6. `[ 📜 Full Ledger ]` - Complete chronological audit trail.
  7. `[ 🔒 Change PIN ]` - Interactive 4-digit credential modification.
  8. `[ ⏻ End Session / Exit ]` - Secure logout and simulated card ejection.

### **Screen 3: Transaction Execution Panel (`TransactionPanel`)**
- **Universal Financial Form:** Adapts controls based on mode (`DEPOSIT`, `WITHDRAW`, `TRANSFER`, `PIN_CHANGE`).
- **Dynamic Account Dropdowns:** Channel selectors showing live balance previews for source and destination accounts.
- **Quick Preset Chips:** One-click shortcut buttons for `$20`, `$50`, `$100`, `$200`, and `$500`.
- **Integrated Touch Keypad:** Keypad appends digits and decimals to the active input field.
- **Custom Exception Interception:** Catches and translates checked business exceptions into clear, styled dialog boxes (`JOptionPane.showMessageDialog`).

### **Screen 4: Ledger Display Panel (`LedgerPanel`)**
- **Scrollable Monospace Ledger:** Terminal-styled `JTextArea` displaying tabular columns:
  `Date & Time | Ref ID | Operation | Accounts | Amount | Balance After | Status`
- **Filter Toolbar:** Toggle between `Mini-Statement (Last 5)`, `Full Ledger`, `Checking Only`, or `Savings Only`.
- **Thermal Receipt Printer Simulation:** Pops up an authentic thermal paper receipt with transaction logs, terminal IDs, and asset summaries.
- **Summary Statistics:** Live aggregate metrics tracking total deposits processed, total cash dispensed, and total logged events.

---

## ⚙️ 3. Core Subsystems & Business Rule Constraints

1. **Multi-Account Matrix:**
   - Independent tracking for Checking and Savings balances using `BigDecimal` to ensure zero floating-point rounding errors.
2. **State Security Engine:**
   - 3-strike sequential gatekeeper.
   - Strike 1: Warning issued, 2 attempts remaining.
   - Strike 2: Elevated warning, 1 attempt remaining.
   - Strike 3: Terminal lockdown, card retained, GUI input elements disabled.
3. **Statutory Daily Withdrawal Limit ($1,000.00):**
   - Tracks cumulative daily cash withdrawals per calendar day.
   - Automatically resets when calendar date changes.
   - Blocks withdrawals exceeding the remaining quota with `DailyLimitExceededException`.
4. **Cross-Allocation Transfer Utilities:**
   - Atomic debit from source and credit to destination with synchronized thread locking.
   - Internal transfers do not consume the physical ATM cash dispenser daily allowance.
   - Automatically produces paired `TRANSFER_OUT` and `TRANSFER_IN` audit records.
5. **Programmatic Boundary Validation:**
   - Rejects non-positive amounts (`<= $0.00`).
   - Rejects fractions beyond 2 decimal places.
   - Rejects transfers between identical source and destination accounts.
   - Enforces 4-digit numeric validation for PIN updates.

---

## 🚨 4. Custom Exception Topology

| Exception | Extends | Trigger Condition |
| :--- | :--- | :--- |
| `ATMException` | `Exception` | Base checked exception for all ATM operations |
| `InvalidPinException` | `ATMException` | Incorrect PIN supplied during login or PIN change |
| `InsufficientFundsException` | `ATMException` | Withdrawal or transfer exceeds account balance |
| `DailyLimitExceededException`| `ATMException` | Withdrawal exceeds $1,000.00 daily limit quota |
| `InvalidTransactionException`| `ATMException` | Non-positive amount, same-account transfer, or malformed PIN |
| `AccountLockedException` | `ATMException` | Attempting actions while terminal/account is locked |

---

## 🚀 5. How to Run, Test, and Build

### Quick Start (Double-Click on Windows)
- **Launch GUI:** Double-click `run.bat`
- **Run Automated Tests:** Double-click `test.bat`
- **Recompile & Package:** Double-click `build.bat`

### Command Line Execution
```powershell
# Run the application
java -jar atm-simulator.jar

# Or run directly via compiled classes
java -cp bin com.atm.Main

# Run the automated unit/integration test suite
java -cp bin com.atm.test.ATMServiceTest
```

---

## 🔑 6. Demo Testing Credentials

- **Cardholder:** Alexander Mercer
- **Card Number:** `4532 •••• •••• 8921`
- **Default PIN:** `1234`
- **Initial Checking Balance:** `$2,450.00`
- **Initial Savings Balance:** `$8,900.00`
- **Daily Withdrawal Ceiling:** `$1,000.00`
- *(Tip: Try typing wrong PINs 3 times to test the 3-strike lockout, then use the on-screen "Unlock Terminal (Admin)" button to restore access).*
