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
package org.openhab.binding.solarmax.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarmax.internal.connector.SolarMaxConnectionSettings;

/**
 * The {@link SolarMaxFailureCounterTest} tests when a thing goes OFFLINE after failed polls.
 *
 * @author Christian Schreiner - Initial contribution
 */
@NonNullByDefault
public class SolarMaxFailureCounterTest {

    @Test
    public void staysOnlineBelowThreshold() {
        SolarMaxFailureCounter counter = new SolarMaxFailureCounter(3);

        assertFalse(counter.recordFailure());
        assertFalse(counter.recordFailure());
        assertEquals(2, counter.getFailuresInARow());
    }

    @Test
    public void goesOfflineAtThresholdAndStaysOffline() {
        SolarMaxFailureCounter counter = new SolarMaxFailureCounter(3);

        counter.recordFailure();
        counter.recordFailure();
        assertTrue(counter.recordFailure());
        assertTrue(counter.recordFailure());
        assertTrue(counter.isThresholdReached());
    }

    @Test
    public void successStartsOver() {
        SolarMaxFailureCounter counter = new SolarMaxFailureCounter(3);

        counter.recordFailure();
        counter.recordFailure();
        counter.recordSuccess();

        assertEquals(0, counter.getFailuresInARow());
        assertFalse(counter.recordFailure());
        assertFalse(counter.recordFailure());
        assertTrue(counter.recordFailure());
    }

    @Test
    public void singleFailuresBetweenSuccessesNeverGoOffline() {
        // the pattern seen on a SolarMax 10MT2 behind a WLAN bridge: one poll fails, the next one works
        SolarMaxFailureCounter counter = new SolarMaxFailureCounter(3);

        for (int i = 0; i < 100; i++) {
            assertFalse(counter.recordFailure());
            counter.recordSuccess();
        }
    }

    @Test
    public void thresholdBelowOneMeansOfflineOnFirstFailure() {
        assertTrue(new SolarMaxFailureCounter(0).recordFailure());
        assertTrue(new SolarMaxFailureCounter(-5).recordFailure());
        assertTrue(new SolarMaxFailureCounter(1).recordFailure());
    }

    @Test
    public void configurationDefaults() {
        SolarMaxConfiguration config = new SolarMaxConfiguration();

        assertEquals(15, config.refreshInterval);
        assertEquals(3000, config.timeout);
        assertEquals(1, config.retries);
        assertEquals(500, config.retryDelay);
        assertEquals(3, config.offlineAfterFailures);

        SolarMaxConnectionSettings settings = config.getConnectionSettings();
        assertEquals(new SolarMaxConnectionSettings(3000, 1, 500, 15000), settings);
    }

    @Test
    public void invalidSettingsAreCorrected() {
        assertEquals(new SolarMaxConnectionSettings(1, 0, 0, 1000), new SolarMaxConnectionSettings(0, -1, -1, 1000));
    }
}
