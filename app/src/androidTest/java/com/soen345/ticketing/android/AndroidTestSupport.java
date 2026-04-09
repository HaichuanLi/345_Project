package com.soen345.ticketing.android;

import androidx.test.platform.app.InstrumentationRegistry;

import java.lang.reflect.Field;

final class AndroidTestSupport {
    private AndroidTestSupport() {
    }

    interface CheckedRunnable {
        void run() throws Throwable;
    }

    static void waitForAssertion(CheckedRunnable assertion) {
        waitForAssertion(10000, assertion);
    }

    static void waitForAssertion(long timeoutMillis, CheckedRunnable assertion) {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        Throwable lastFailure = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                assertion.run();
                return;
            } catch (Throwable failure) {
                lastFailure = failure;
                sleep(200);
            }
        }
        AssertionError error = new AssertionError("Timed out waiting for assertion");
        if (lastFailure != null) {
            error.initCause(lastFailure);
        }
        throw error;
    }

    static void resetTicketingDataProvider() {
        try {
            clearField("eventRepository");
            clearField("reservationRepository");
            clearField("confirmationService");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to reset TicketingDataProvider", e);
        }
    }

    private static void clearField(String fieldName) throws ReflectiveOperationException {
        Field field = TicketingDataProvider.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, null);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
