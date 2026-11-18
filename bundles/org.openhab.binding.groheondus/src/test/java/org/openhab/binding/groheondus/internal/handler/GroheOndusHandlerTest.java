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
package org.openhab.binding.groheondus.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.groheondus.internal.api.SenseSample;
import org.openhab.binding.groheondus.internal.api.WaterWithdrawal;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.TimeSeries;

/**
 * Tests for the data processing in the appliance handlers.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
class GroheOndusHandlerTest {
    private static final Instant T0 = Instant.parse("2026-11-17T09:00:00Z");

    private static final List<SenseSample> SAMPLES = List.of(new SenseSample(T0, 20.5f, 40f),
            new SenseSample(T0.plusSeconds(3600), null, 41f), new SenseSample(T0.plusSeconds(7200), 21.5f, 42f));

    @Test
    void allSamplesWithValueAreAddedWithOriginalTimestamps() {
        TimeSeries timeSeries = GroheOndusBaseHandler.buildTimeSeries(SAMPLES, SenseSample::timestamp,
                SenseSample::temperature, SIUnits.CELSIUS, null);

        assertEquals(TimeSeries.Policy.ADD, timeSeries.getPolicy());
        List<TimeSeries.Entry> entries = timeSeries.getStates().toList();
        assertEquals(2, entries.size());
        assertEquals(T0, entries.get(0).timestamp());
        assertEquals(new QuantityType<>(20.5f, SIUnits.CELSIUS), entries.get(0).state());
        assertEquals(T0.plusSeconds(7200), entries.get(1).timestamp());
    }

    @Test
    void alreadyPublishedSamplesAreSkipped() {
        TimeSeries timeSeries = GroheOndusBaseHandler.buildTimeSeries(SAMPLES, SenseSample::timestamp,
                SenseSample::humidity, Units.PERCENT, T0.plusSeconds(3600));

        List<TimeSeries.Entry> entries = timeSeries.getStates().toList();
        assertEquals(1, entries.size());
        assertEquals(T0.plusSeconds(7200), entries.get(0).timestamp());
    }

    @Test
    void waterConsumptionIncludesPeriodStartingAtCutoff() {
        Instant midnight = Instant.parse("2026-11-17T23:00:00Z");
        List<WaterWithdrawal> withdrawals = List.of(new WaterWithdrawal(midnight.minusSeconds(3600), 10.1),
                new WaterWithdrawal(midnight, 2.5), new WaterWithdrawal(midnight.plusSeconds(7 * 3600), 1.4));

        assertEquals(new QuantityType<>("3.9 l"),
                GroheOndusSenseGuardHandler.sumWaterConsumption(withdrawals, midnight));
        assertEquals(new QuantityType<>("14.0 l"),
                GroheOndusSenseGuardHandler.sumWaterConsumption(withdrawals, Instant.MIN));
    }

    @Test
    void deviceIsSilentWhenLastReportIsTooOld() {
        Duration maxAge = Duration.ofHours(48);
        Instant now = Instant.parse("2026-11-18T20:00:00Z");

        assertFalse(GroheOndusBaseHandler.isSilent(now.minus(maxAge), now, maxAge));
        assertTrue(GroheOndusBaseHandler.isSilent(now.minus(maxAge).minusSeconds(1), now, maxAge));
        assertTrue(GroheOndusBaseHandler.isSilent(Instant.parse("2026-01-18T18:25:46Z"), now, maxAge));
        assertTrue(GroheOndusBaseHandler.isSilent(null, now, maxAge));
    }
}
