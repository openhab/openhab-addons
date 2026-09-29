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
package org.openhab.binding.solaredge.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solaredge.internal.model.AggregatePeriod;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Tests Monitoring API V2 cycle ordering before channel publication.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class PublicApiV2DataTest {

    @Test
    public void calculatesConsumptionFromEnergyBalance() {
        assertEquals(594.3, PublicApiV2Data.calculateConsumption(0, 1.75, 3.95, 0, 596.5), 0.0001);
        assertEquals(0, PublicApiV2Data.calculateConsumption(0, 1, 2, 0, 0));
    }

    @Test
    public void calculatesSelfConsumptionAndCoverage() {
        assertEquals(600, PublicApiV2Data.calculateSelfConsumption(1500, 100, 800), 0.0001);
        assertEquals(75, PublicApiV2Data.calculateCoverage(600, 800), 0.0001);
        assertEquals(0, PublicApiV2Data.calculateCoverage(0, 0), 0.0001);
    }

    @Test
    public void derivesLiveStatuses() {
        assertEquals("Active", PublicApiV2Data.activeStatus(1));
        assertEquals("Idle", PublicApiV2Data.activeStatus(0));
        assertEquals("Active", PublicApiV2Data.activeStatus(100, 0));
        assertEquals("Active", PublicApiV2Data.activeStatus(0, 100));
        assertEquals("Idle", PublicApiV2Data.activeStatus(0, 0));
    }

    @Test
    public void derivesCriticalBatteryStateOnlyFromKnownLevel() {
        assertEquals(new StringType("true"), PublicApiV2Data.batteryCriticalState(9.9, 10));
        assertEquals(new StringType("false"), PublicApiV2Data.batteryCriticalState(10.0, 10));
        assertEquals(UnDefType.UNDEF, PublicApiV2Data.batteryCriticalState(null, 10));
    }

    @Test
    public void ignoresOlderLiveResponsesBeforePublishingChannels() {
        AtomicInteger updates = new AtomicInteger();
        PublicApiV2Data data = new PublicApiV2Data(mock(ChannelProvider.class), ignored -> updates.incrementAndGet(),
                () -> 10);
        Map<Channel, State> values = Map.of(mock(Channel.class), mock(State.class));

        data.updateProduction(2, values, 100.0);
        int currentUpdates = updates.get();
        data.updateGrid(1, values, 10.0, 0.0, null);
        data.updateStorage(1, values, 0.0, 0.0, null);

        assertEquals(currentUpdates, updates.get());
    }

    @Test
    public void tracksAggregatePeriodsIndependently() {
        AtomicInteger updates = new AtomicInteger();
        PublicApiV2Data data = new PublicApiV2Data(mock(ChannelProvider.class), ignored -> updates.incrementAndGet(),
                () -> 10);
        Map<Channel, State> values = Map.of(mock(Channel.class), mock(State.class));

        data.updateAggregateProduction(3, AggregatePeriod.DAY, values, 100.0);
        data.updateAggregateGrid(2, AggregatePeriod.DAY, values, 1.0, 0.0, null);
        data.updateAggregateProduction(2, AggregatePeriod.YEAR, values, 100.0);

        assertEquals(2, updates.get());
    }
}
