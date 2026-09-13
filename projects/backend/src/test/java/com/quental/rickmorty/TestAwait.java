package com.quental.rickmorty;

import java.time.Duration;
import java.util.function.BooleanSupplier;

/** Bounded polling for asynchronous assertions without adding Awaitility (economy of dependencies). */
public final class TestAwait {

    private TestAwait() {
    }

    public static void until(BooleanSupplier condition, Duration timeout, String description) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Interrupted while waiting for: " + description, ex);
            }
        }
        throw new AssertionError("Timed out after " + timeout + " waiting for: " + description);
    }
}
