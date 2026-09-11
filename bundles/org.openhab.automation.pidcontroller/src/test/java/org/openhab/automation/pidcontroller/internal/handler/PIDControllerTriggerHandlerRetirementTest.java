/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.automation.pidcontroller.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Retirement must not race an in-flight calculation.
 *
 * <p>
 * The handler needs an OSGi BundleContext and a live ItemRegistry to construct, which is more
 * than a unit test can reasonably stand up, so this exercises the locking contract directly:
 * the calculation and the retirement must serialise on one monitor. Without that, a
 * calculation that has already passed its callback check goes on to write the inspector Items
 * and call triggered(), so a handler retired mid-cycle can still publish a stale output, and
 * two overlapping invocations can corrupt the non-thread-safe PIDController.
 *
 * @author Vlad Kolotoff - Initial contribution
 */
@NonNullByDefault
public class PIDControllerTriggerHandlerRetirementTest {

    /**
     * Models the handler: a guarded, multi-step calculation and a retirement that clears the
     * guard. Both synchronize on the same monitor, as calculate()/setCallback()/dispose() do.
     */
    private static class SerialisedHandler {
        private final Object monitor = new Object();
        private volatile boolean retired;
        final AtomicBoolean publishedAfterRetirement = new AtomicBoolean();

        void calculate(CountDownLatch reachedGuard, CountDownLatch releaseAfterGuard) {
            synchronized (monitor) {
                if (retired) {
                    return;
                }
                reachedGuard.countDown();
                try {
                    // Stand in for the work between the guard and the publication: reading the
                    // items, running the controller, updating the inspectors.
                    releaseAfterGuard.await(2, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (retired) {
                    publishedAfterRetirement.set(true);
                }
            }
        }

        void retire() {
            synchronized (monitor) {
                retired = true;
            }
        }
    }

    @Test
    void retirementWaitsForAnInFlightCalculation() throws Exception {
        SerialisedHandler handler = new SerialisedHandler();
        CountDownLatch reachedGuard = new CountDownLatch(1);
        CountDownLatch releaseAfterGuard = new CountDownLatch(1);

        Thread calculating = new Thread(() -> handler.calculate(reachedGuard, releaseAfterGuard));
        calculating.start();
        assertTrue(reachedGuard.await(2, TimeUnit.SECONDS), "the calculation should reach its guard");

        // Retire while the calculation sits between its guard and its publication. This is the
        // window the unsynchronized version left open.
        Thread retiring = new Thread(handler::retire);
        retiring.start();

        // The retirement must block on the monitor rather than take effect mid-calculation.
        retiring.join(200);
        assertTrue(retiring.isAlive(), "retirement must wait for the in-flight calculation, not race it");

        releaseAfterGuard.countDown();
        calculating.join(2000);
        retiring.join(2000);

        assertFalse(handler.publishedAfterRetirement.get(),
                "a calculation must not observe retirement partway through and publish anyway");
    }
}
