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
package org.openhab.binding.fronius.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.ScheduleType;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.TimeOfUseRecord;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.firmware.types.SemverVersion;

/**
 * Tests for {@link FroniusBatteryControl}.
 *
 * @author Christian Jonak-Möchel - Initial contribution
 */
@NonNullByDefault
class FroniusBatteryControlTest {

    /**
     * Simulates the time of use settings of an inverter. Optionally, the first read returns its snapshot only after
     * another write or a timeout, so that a concurrent modification gets the chance to interleave with the reader's
     * read-modify-write sequence.
     */
    private static class FakeInverter extends FroniusConfigApiClient {
        private final CountDownLatch firstReadStarted = new CountDownLatch(1);
        private final CountDownLatch written = new CountDownLatch(1);
        private final AtomicBoolean firstRead;
        private volatile String timeOfUse;

        FakeInverter(String timeOfUse, boolean interleaveFirstRead) {
            super(new FroniusHttpUtil(), new HttpClient());
            this.timeOfUse = timeOfUse;
            this.firstRead = new AtomicBoolean(interleaveFirstRead);
        }

        @Override
        public String executeRequest(FroniusConfigApiEndpoint endpoint, HttpMethod method, URI uri,
                @Nullable String body) throws FroniusCommunicationException {
            if (method == HttpMethod.POST && body != null) {
                timeOfUse = body;
                written.countDown();
                return "{\"writeSuccess\":[\"timeofuse\"]}";
            }
            String snapshot = timeOfUse;
            if (firstRead.getAndSet(false)) {
                firstReadStarted.countDown();
                try {
                    written.await(500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new FroniusCommunicationException("Interrupted", e);
                }
            }
            return snapshot;
        }
    }

    @Test
    void concurrentModificationsOfTheChannelAndActionPathsDoNotUndoEachOther() throws Exception {
        FakeInverter inverter = new FakeInverter("{\"timeofuse\":[]}", true);
        SemverVersion firmwareVersion = SemverVersion.fromString("1.41.10");
        // the battery thing (channels) and the deprecated powerinverter actions use separate instances
        FroniusBatteryControl channelControl = new FroniusBatteryControl(inverter, firmwareVersion, "http", "fronius",
                "customer", "secret");
        FroniusBatteryControl actionControl = new FroniusBatteryControl(inverter, firmwareVersion, "http", "fronius",
                "customer", "secret");
        AtomicReference<@Nullable Exception> backgroundFailure = new AtomicReference<>();

        Thread channelCommand = new Thread(() -> {
            try {
                channelControl.setAllTimeSchedule(ScheduleType.CHARGE_MAX, new QuantityType<>(2000, Units.WATT));
            } catch (FroniusCommunicationException | FroniusUnauthorizedException | IllegalArgumentException e) {
                backgroundFailure.set(e);
            }
        });
        Thread action = new Thread(() -> {
            try {
                actionControl.addSchedule(LocalTime.of(22, 0), LocalTime.of(23, 59), ScheduleType.DISCHARGE_MAX,
                        new QuantityType<>(1000, Units.WATT));
            } catch (FroniusCommunicationException | FroniusUnauthorizedException | IllegalArgumentException e) {
                backgroundFailure.set(e);
            }
        });

        channelCommand.start();
        assertTrue(inverter.firstReadStarted.await(5, TimeUnit.SECONDS));
        action.start();
        channelCommand.join(10000);
        action.join(10000);

        assertFalse(channelCommand.isAlive());
        assertFalse(action.isAlive());
        assertNull(backgroundFailure.get());
        Set<ScheduleType> scheduleTypes = Arrays.stream(channelControl.getTimeOfUse().records())
                .map(TimeOfUseRecord::scheduleType).collect(Collectors.toSet());
        assertEquals(Set.of(ScheduleType.CHARGE_MAX, ScheduleType.DISCHARGE_MAX), scheduleTypes);
    }

    @Test
    void entriesWithAnInvalidTimeDoNotBreakAddingSchedules() throws Exception {
        // the inverter web UI saves "NaN:NaN" as end time when 24:00 is entered
        FakeInverter inverter = new FakeInverter("""
                {"timeofuse":[{"Active":true,"Power":500,"ScheduleType":"DISCHARGE_MIN",
                "TimeTable":{"Start":"00:00","End":"NaN:NaN"},
                "Weekdays":{"Mon":true,"Tue":true,"Wed":true,"Thu":true,"Fri":true,"Sat":true,"Sun":true}}]}
                """, false);
        FroniusBatteryControl control = new FroniusBatteryControl(inverter, SemverVersion.fromString("1.41.10"), "http",
                "fronius", "customer", "secret");

        control.addSchedule(LocalTime.of(22, 0), LocalTime.of(23, 59), ScheduleType.DISCHARGE_MAX,
                new QuantityType<>(1000, Units.WATT));

        assertEquals(List.of("NaN:NaN", "23:59"),
                Arrays.stream(control.getTimeOfUse().records()).map(record -> record.timeTable().end()).toList());
    }
}
