# SmartPark Pro - Car Parking Management System (Java Swing)

A complete, modern desktop application built with Java for managing parking facilities. It provides visual parking slot monitoring, real-time entry and exit timing, automated fixed-hourly-rate billing calculations, receipt generation, and persistent data storage.

---

## Key Features

1. **Interactive Parking Matrix & Dashboard**:
   - Visual grid of parking slots color-coded by occupancy:
     - 🟢 **Green (Vacant)**: Ready to park. Click directly on any vacant slot to open the entry gate with that slot pre-selected.
     - 🔴 **Red (Occupied)**: Displays the vehicle's license plate number and live elapsed duration. Click directly to jump straight to checkout and billing!
   - Real-time KPI summary: Total Slots, Available Slots, Occupied Slots, Occupancy Rate %, and Today's Revenue.

2. **Vehicle Entry Gate**:
   - Check-in for various vehicle categories:
     - 🚗 Car (Standard 1.0x rate)
     - 🚙 SUV / Van (1.2x rate)
     - 🏍️ Bike / Motorcycle (0.5x rate)
     - 🚚 Truck / Heavy (1.5x rate)
   - Auto-allocation of nearest vacant slot or manual slot choice.
   - Accurate microsecond entry timestamping (`LocalDateTime`).
   - Digital parking ticket issuance with printable ticket modal.

3. **Fixed Hourly Billing & Exit Gate**:
   - Quick vehicle search by license plate, ticket ID, slot number, or quick-select dropdown.
   - **Fixed Hourly Tariff Billing Engine**:
     - Configurable free grace period (e.g. 10 minutes free).
     - Fixed hourly blocks: any fractional hour past grace period is billed as a full hour.
     - Enforces configurable minimum billing hours.
     - Optional vehicle-type multiplier.
   - Payment method recording: Cash, Card, UPI / Online, Waived.
   - Automatic freeing of parking slot upon successful checkout.
   - Formatted, printable receipt dialog.

4. **Audit History & Records**:
   - Searchable, sortable table of all past and active parking sessions.
   - Filter by status (ACTIVE / COMPLETED) or date (Today Only).
   - Instant CSV export of complete parking logs for accounting and audits.

5. **Settings & Customization**:
   - Adjust base hourly rate ($ or ₹ per hour).
   - Modify free grace period in minutes.
   - Modify minimum billable hours.
   - Change currency symbol (`$`, `₹`, `€`, `£`, etc.).
   - Expand or shrink parking capacity (from 6 up to 200 slots).

6. **Local Persistence**:
   - Zero-dependency JSON storage engine in `data/` (`config.json`, `slots.json`, `tickets.json`).
   - All parked cars, history, and settings are preserved across application restarts.

---

## Project Structure

```
carParking/
├── bin/                          # Compiled bytecode classes (.class)
├── data/                         # Local persistent JSON data storage
├── src/
│   └── com/carparking/
│       ├── Main.java             # Main application entry point
│       ├── model/
│       │   ├── VehicleType.java  # Vehicle types, multipliers, icons
│       │   ├── Vehicle.java      # Vehicle entity
│       │   ├── ParkingSlot.java  # Parking slot entity
│       │   ├── ParkingTicket.java# Ticket, duration, and billing metadata
│       │   └── TariffConfig.java # Pricing configuration
│       ├── service/
│       │   ├── BillingCalculator.java # Fixed hour billing engine & grace logic
│       │   ├── StorageService.java    # JSON file persistence
│       │   └── ParkingManager.java    # Core business service & coordinator
│       ├── ui/
│       │   ├── Theme.java        # Modern UI styling, colors, and components
│       │   ├── MainFrame.java    # Shell frame with sidebar & clock
│       │   ├── DashboardPanel.java # KPI cards & interactive slot grid
│       │   ├── EntryPanel.java   # Vehicle entry check-in form
│       │   ├── ExitPanel.java    # Lookup, duration, fixed hour bill & pay
│       │   ├── HistoryPanel.java # Table view, filters, CSV export
│       │   ├── SettingsPanel.java# Pricing & capacity config
│       │   └── ReceiptDialog.java# Printable receipt dialog
│       └── test/
│           └── BillingTest.java  # Automated test harness
├── build.bat                     # Compiles Java files
├── run.bat                       # Starts the application
├── test.bat                      # Runs unit & integration tests
└── package_jar.bat               # Packages into executable CarParkingApp.jar
```

---

## How to Run

### Method 1: Batch Script (One-Click)
Double-click or run from command prompt:
```cmd
run.bat
```
*(If the project is not yet built, `run.bat` automatically runs `build.bat` first).*

### Method 2: Package & Run Executable JAR
To create a standalone runnable `.jar` file:
```cmd
package_jar.bat
java -jar CarParkingApp.jar
```

### Method 3: Run Automated Tests
To run automated test cases verifying grace periods, fixed hourly ceiling billing, vehicle multipliers, and slot vacancy lifecycle:
```cmd
test.bat
```
