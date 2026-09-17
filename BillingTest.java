package com.carparking.test;

import com.carparking.model.*;
import com.carparking.service.BillingCalculator;
import com.carparking.service.ParkingManager;
import com.carparking.service.StorageService;

import java.io.File;
import java.time.LocalDateTime;

public class BillingTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  RUNNING CAR PARKING BILLING AUTOMATED TESTS     ");
        System.out.println("==================================================");

        int passed = 0;
        int failed = 0;

        try {
            // Test 1: Grace period (<= 10 mins free)
            TariffConfig config = new TariffConfig("$", 20.0, 10, 1);
            LocalDateTime entry = LocalDateTime.of(2026, 9, 18, 10, 0, 0);
            LocalDateTime exitGrace = LocalDateTime.of(2026, 9, 18, 10, 8, 0); // 8 mins
            ParkingTicket t1 = new ParkingTicket("T1", new Vehicle("CAR-001", VehicleType.CAR, "Red", "John", "123"), "A-01", entry);

            BillingCalculator.BillingBreakdown b1 = BillingCalculator.calculate(t1, exitGrace, config);
            assertEquals("Grace period amount", 0.0, b1.totalAmount);
            assertEquals("Grace period hours", 0L, b1.billableHours);
            assertTrue("Grace period flag", b1.gracePeriodApplies);
            System.out.println("✓ Test 1 Passed: Grace period free exit verified.");
            passed++;

            // Test 2: Exact 1 Hour
            LocalDateTime exit1Hr = LocalDateTime.of(2026, 9, 18, 11, 0, 0); // 60 mins
            BillingCalculator.BillingBreakdown b2 = BillingCalculator.calculate(t1, exit1Hr, config);
            assertEquals("1 Hour amount", 20.0, b2.totalAmount);
            assertEquals("1 Hour billable hours", 1L, b2.billableHours);
            System.out.println("✓ Test 2 Passed: Exactly 1 hour billed 1x rate.");
            passed++;

            // Test 3: Fractional hour round-up (65 mins -> 2 hours fixed charge)
            LocalDateTime exit65Min = LocalDateTime.of(2026, 9, 18, 11, 5, 0); // 65 mins
            BillingCalculator.BillingBreakdown b3 = BillingCalculator.calculate(t1, exit65Min, config);
            assertEquals("65 mins amount", 40.0, b3.totalAmount);
            assertEquals("65 mins billable hours", 2L, b3.billableHours);
            System.out.println("✓ Test 3 Passed: Fixed hour ceil (65 mins = 2 hours) verified.");
            passed++;

            // Test 4: Minimum billable hours past grace (15 mins -> 1 hour minimum)
            LocalDateTime exit15Min = LocalDateTime.of(2026, 9, 18, 10, 15, 0); // 15 mins
            BillingCalculator.BillingBreakdown b4 = BillingCalculator.calculate(t1, exit15Min, config);
            assertEquals("15 mins amount", 20.0, b4.totalAmount);
            assertEquals("15 mins billable hours", 1L, b4.billableHours);
            System.out.println("✓ Test 4 Passed: Minimum 1 hour charge past grace period verified.");
            passed++;

            // Test 5: Vehicle Type Multipliers (Bike 0.5x, SUV 1.2x, Truck 1.5x)
            ParkingTicket tBike = new ParkingTicket("TB", new Vehicle("BK-001", VehicleType.BIKE, "Black", "Sam", "123"), "A-02", entry);
            BillingCalculator.BillingBreakdown bBike = BillingCalculator.calculate(tBike, exit65Min, config); // 2 hrs @ 10/hr = 20
            assertEquals("Bike amount for 2 hrs", 20.0, bBike.totalAmount);

            ParkingTicket tSuv = new ParkingTicket("TS", new Vehicle("SUV-001", VehicleType.SUV, "White", "Alex", "123"), "A-03", entry);
            BillingCalculator.BillingBreakdown bSuv = BillingCalculator.calculate(tSuv, exit65Min, config); // 2 hrs @ 24/hr = 48
            assertEquals("SUV amount for 2 hrs", 48.0, bSuv.totalAmount);

            ParkingTicket tTruck = new ParkingTicket("TT", new Vehicle("TRK-001", VehicleType.TRUCK, "Blue", "Dan", "123"), "A-04", entry);
            BillingCalculator.BillingBreakdown bTruck = BillingCalculator.calculate(tTruck, exit65Min, config); // 2 hrs @ 30/hr = 60
            assertEquals("Truck amount for 2 hrs", 60.0, bTruck.totalAmount);
            System.out.println("✓ Test 5 Passed: Vehicle type multipliers (Bike 0.5x, SUV 1.2x, Truck 1.5x) verified.");
            passed++;

            // Test 6: Multi-day parking (25 hours)
            LocalDateTime exit25Hrs = LocalDateTime.of(2026, 9, 19, 11, 0, 0); // 25 hours
            BillingCalculator.BillingBreakdown b6 = BillingCalculator.calculate(t1, exit25Hrs, config);
            assertEquals("25 Hours billable hours", 25L, b6.billableHours);
            assertEquals("25 Hours amount", 500.0, b6.totalAmount);
            System.out.println("✓ Test 6 Passed: Multi-day 25 hours calculation verified.");
            passed++;

            // Test 7: ParkingManager End-to-End Entry and Checkout
            File testDir = new File("build_test_data");
            testDir.mkdirs();
            StorageService testStorage = new StorageService("build_test_data");
            ParkingManager pm = new ParkingManager(testStorage);

            int initialAvail = pm.getAvailableSlotsCount();
            Vehicle testCar = new Vehicle("TEST-999", VehicleType.CAR, "Silver", "Tester", "555-1234");
            ParkingTicket issuedTicket = pm.parkVehicle(testCar, null, LocalDateTime.now().minusHours(3));
            assertTrue("Ticket issued", issuedTicket != null);
            assertEquals("Slot occupied count incremented", initialAvail - 1, pm.getAvailableSlotsCount());

            ParkingTicket completed = pm.checkoutVehicle("TEST-999", LocalDateTime.now(), "CASH");
            assertEquals("Checkout completed status", "COMPLETED", completed.getStatus());
            assertEquals("Slot returned to available", initialAvail, pm.getAvailableSlotsCount());
            System.out.println("✓ Test 7 Passed: Full entry, allocation, and checkout lifecycle verified.");
            passed++;

            // Clean up test directory
            deleteDirectory(testDir);

        } catch (Throwable e) {
            System.err.println("✗ TEST FAILED: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        System.out.println("==================================================");
        System.out.printf("  RESULTS: %d PASSED, %d FAILED%n", passed, failed);
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertEquals(String desc, double expected, double actual) {
        if (Math.abs(expected - actual) > 0.001) {
            throw new AssertionError(String.format("%s: expected %.2f but was %.2f", desc, expected, actual));
        }
    }

    private static void assertEquals(String desc, long expected, long actual) {
        if (expected != actual) {
            throw new AssertionError(String.format("%s: expected %d but was %d", desc, expected, actual));
        }
    }

    private static void assertEquals(String desc, Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError(String.format("%s: expected %s but was %s", desc, expected, actual));
        }
    }

    private static void assertTrue(String desc, boolean condition) {
        if (!condition) {
            throw new AssertionError(desc + ": condition was false");
        }
    }

    private static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) deleteDirectory(f);
            }
        }
        dir.delete();
    }
}
