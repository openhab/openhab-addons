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
package org.openhab.binding.tibber.internal.pulse.stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibber.internal.pulse.Fixtures;
import org.openhab.binding.tibber.internal.pulse.stream.StreamClient.BackoffWaiter;
import org.openhab.binding.tibber.internal.pulse.stream.StreamClient.Timing;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.types.UnDefType;

/**
 * Tests the stream scenarios that need the handler layer: a real stream client against the local bridge double,
 * driven by the bridge handler with a meter handler below it. Covers "At most one stream per bridge", "Shutdown and
 * reconfiguration" (connection spec) and the status scenarios of the thing-lifecycle spec on a real connection.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(60)
class BridgeHandlerStreamTest extends StreamTestSupport {

    /** No real delays between attempts; the loop still ends when the client is stopped. */
    private static final BackoffWaiter IMMEDIATE = (delay, stopped) -> stopped.getCount() == 0;
    private static final BackoffWaiter REAL = (delay, stopped) -> stopped.await(delay.toMillis(),
            java.util.concurrent.TimeUnit.MILLISECONDS);

    private @Nullable HandlerHarness harness;

    @AfterEach
    void disposeHandlers() {
        HandlerHarness h = harness;
        if (h != null) {
            h.dispose();
        }
    }

    private HandlerHarness start(int port, Timing timing, BackoffWaiter waiter) {
        HandlerHarness created = new HandlerHarness("bridge.example", PASSWORD, Fixtures.DEVICE_ID,
                HandlerHarness.factory(this, () -> port, timing, waiter));
        harness = created;
        created.initialize();
        return created;
    }

    private static boolean isOnline(ThingStatusInfo info) {
        return info.getStatus() == ThingStatus.ONLINE;
    }

    private static boolean is(ThingStatusInfo info, ThingStatus status, ThingStatusDetail detail) {
        return info.getStatus() == status && info.getStatusDetail() == detail;
    }

    @Test
    void whenTheBridgeStreamsReadingsThenTheBridgeAndTheMeterGoOnlineAndTheChannelsShowTheValues() throws Exception {
        FakeBridge fake = bridge(FakeBridge.sending(validWire()));

        HandlerHarness h = start(fake.port(), LONG_TIMING, REAL);

        HandlerHarness.await(h.bridgeStatuses, BridgeHandlerStreamTest::isOnline, WAIT_MILLIS);
        HandlerHarness.await(h.meterStatuses, BridgeHandlerStreamTest::isOnline, WAIT_MILLIS);
        assertEquals(new QuantityType<>("1523 W"),
                h.awaitMeterState(CHANNEL_POWER, s -> s instanceof QuantityType<?>, WAIT_MILLIS));
        assertEquals(1, fake.accepted());
        assertEquals(Fixtures.METER_ID, h.meter.getProperties().get(PROPERTY_METER_ID));
        assertEquals(Fixtures.DEVICE_ID, h.meter.getProperties().get(PROPERTY_DEVICE_ID));
    }

    @Test
    void whenInitializationReturnsThenTheBridgeIsUnknownAndNoReadingIsNeeded() throws Exception {
        FakeBridge fake = bridge(FakeBridge.silent());

        HandlerHarness h = start(fake.port(), LONG_TIMING, REAL);

        assertEquals(ThingStatus.UNKNOWN, h.bridge.getStatus());
        HandlerHarness.await(h.meterStatuses, i -> i.getStatus() == ThingStatus.UNKNOWN, WAIT_MILLIS);
    }

    @Test
    void whenThePasswordIsRejectedThenTheBridgeIsOfflineWithConfigurationErrorAndNoSecondRequestIsSent()
            throws Exception {
        FakeBridge fake = bridge(FakeBridge.answeringThenClosing("HTTP/1.1 401 Unauthorized\r\n\r\n"));

        HandlerHarness h = start(fake.port(), LONG_TIMING, IMMEDIATE);

        ThingStatusInfo info = HandlerHarness.await(h.bridgeStatuses,
                i -> is(i, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR), WAIT_MILLIS);
        assertEquals(TEXT_PASSWORD_REJECTED, info.getDescription());
        assertTrue(awaitThreadEnd("OH-binding-tibber-" + h.bridge.getUID(), WAIT_MILLIS),
                "the client gives up after a rejected password");
        assertEquals(1, fake.accepted());
        assertFalse(String.valueOf(info.getDescription()).contains(PASSWORD));
    }

    @Test
    void whenTheBridgeIsUnreachableThenTheBridgeIsOfflineWithCommunicationErrorAndTheMeterFollows() throws Exception {
        HandlerHarness h = start(closedPort(), LONG_TIMING, IMMEDIATE);

        HandlerHarness.await(h.bridgeStatuses, i -> is(i, ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR),
                WAIT_MILLIS);
        HandlerHarness.await(h.meterStatuses, i -> is(i, ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE),
                WAIT_MILLIS);
        assertEquals(TEXT_UNREACHABLE, h.bridge.getStatusInfo().getDescription());
    }

    @Test
    void whenTheStreamIsClosedByTheBridgeThenItIsOfflineAndTheMeterIsOnlineAgainAfterTheFirstReadingOfTheNewStream()
            throws Exception {
        FakeBridge fake = bridge(FakeBridge.sendingThenClosing(validWire()), FakeBridge.sending(validWire()));

        HandlerHarness h = start(fake.port(), LONG_TIMING, IMMEDIATE);

        HandlerHarness.await(h.bridgeStatuses, i -> is(i, ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR),
                WAIT_MILLIS);
        HandlerHarness.await(h.meterStatuses, i -> is(i, ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE),
                WAIT_MILLIS);
        ThingStatusInfo online = HandlerHarness.await(h.bridgeStatuses, BridgeHandlerStreamTest::isOnline, WAIT_MILLIS);
        assertNull(online.getDescription(), "the description of the earlier error is cleared");
        HandlerHarness.await(h.meterStatuses, BridgeHandlerStreamTest::isOnline, WAIT_MILLIS);
        assertEquals(2, fake.accepted());
    }

    @Test
    void whenTheStreamIsSilentThenTheBridgeIsOfflineAndNoMeterIsEverOnline() throws Exception {
        FakeBridge fake = bridge(FakeBridge.silent());
        Timing quick = new Timing(Duration.ofSeconds(5), Duration.ofMillis(300), Duration.ofSeconds(2));

        HandlerHarness h = start(fake.port(), quick, IMMEDIATE);

        ThingStatusInfo info = HandlerHarness.await(h.bridgeStatuses,
                i -> is(i, ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR), WAIT_MILLIS);
        assertEquals(TEXT_SILENT, info.getDescription());
        assertTrue(h.meterStatuses.stream().noneMatch(BridgeHandlerStreamTest::isOnline));
    }

    @Test
    void whenTheBridgeIsDisposedAndInitializedTenTimesThenNeverMoreThanOneConnectionIsOpen() throws Exception {
        FakeBridge fake = bridge(FakeBridge.sending(validWire()));
        HandlerHarness h = start(fake.port(), LONG_TIMING, REAL);

        for (int cycle = 1; cycle <= 10; cycle++) {
            assertTrue(fake.awaitRequests(cycle, WAIT_MILLIS));
            h.bridgeHandler.dispose();
            assertTrue(fake.awaitNoOpenConnections(WAIT_MILLIS), "the stream is closed when dispose returns");
            if (cycle < 10) {
                h.bridgeHandler.initialize();
            }
        }

        assertEquals(1, fake.maxOpen());
        assertEquals(10, fake.accepted());
    }

    @Test
    void whenTheBridgeIsDisposedThenTheReaderThreadIsGoneAndNothingIsPublishedAfterwards() throws Exception {
        FakeBridge fake = bridge(FakeBridge.sending(validWire()));
        HandlerHarness h = start(fake.port(), LONG_TIMING, REAL);
        HandlerHarness.await(h.bridgeStatuses, BridgeHandlerStreamTest::isOnline, WAIT_MILLIS);

        h.bridgeHandler.dispose();
        int published = h.bridgeStatuses.size();

        assertFalse(isAlive("OH-binding-tibber-" + h.bridge.getUID()));
        assertTrue(fake.awaitNoOpenConnections(WAIT_MILLIS));
        assertEquals(published, h.bridgeStatuses.size());
    }

    @Test
    void whenThePasswordIsChangedToOneTheBridgeRejectsThenTheOldStreamIsClosedAndTheBridgeShowsTheConfigurationError()
            throws Exception {
        FakeBridge fake = bridge(FakeBridge.sending(validWire()),
                FakeBridge.answeringThenClosing("HTTP/1.1 401 Unauthorized\r\n\r\n"));
        HandlerHarness h = start(fake.port(), LONG_TIMING, REAL);
        HandlerHarness.await(h.bridgeStatuses, BridgeHandlerStreamTest::isOnline, WAIT_MILLIS);

        h.bridgeHandler.handleConfigurationUpdate(Map.of("password", "another-password"));

        HandlerHarness.await(h.bridgeStatuses, i -> is(i, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR),
                WAIT_MILLIS);
        assertTrue(fake.awaitNoOpenConnections(WAIT_MILLIS));
        assertEquals(1, fake.maxOpen());
        assertEquals(2, fake.accepted());
    }

    @Test
    void whenTheHostnameIsChangedThenTheStreamToTheOldHostIsClosedAndAStreamToTheNewHostIsOpened() throws Exception {
        FakeBridge hostA = bridge(FakeBridge.sending(validWire()));
        FakeBridge hostB = bridge(FakeBridge.sending(validWire()));
        AtomicInteger selected = new AtomicInteger(hostA.port());
        HandlerHarness h = new HandlerHarness("bridge-a.example", PASSWORD, Fixtures.DEVICE_ID,
                HandlerHarness.factory(this, selected::get, LONG_TIMING, REAL));
        harness = h;
        h.initialize();
        HandlerHarness.await(h.bridgeStatuses, BridgeHandlerStreamTest::isOnline, WAIT_MILLIS);
        selected.set(hostB.port());

        h.bridgeHandler.handleConfigurationUpdate(Map.of("hostname", "bridge-b.example"));

        assertTrue(hostA.awaitNoOpenConnections(WAIT_MILLIS));
        assertTrue(hostB.awaitRequests(1, WAIT_MILLIS));
        assertEquals(1, hostA.accepted());
        assertEquals(1, hostB.accepted());
    }

    @Test
    void whenTheStaleTimeoutIsChangedThenTheStreamStaysOpen() throws Exception {
        FakeBridge fake = bridge(FakeBridge.sending(validWire()));
        HandlerHarness h = start(fake.port(), LONG_TIMING, REAL);
        HandlerHarness.await(h.meterStatuses, BridgeHandlerStreamTest::isOnline, WAIT_MILLIS);

        h.meterHandler.handleConfigurationUpdate(Map.of("staleTimeout", 20));

        assertEquals(1, fake.accepted());
        assertEquals(1, fake.maxOpen());
        assertEquals(UnDefType.UNDEF, h.awaitMeterState(CHANNEL_POWER, s -> s == UnDefType.UNDEF, WAIT_MILLIS));
        // The counter keeps its value across the re-initialization.
        assertTrue(h.meterState(CHANNEL_ENERGY_IMPORT) instanceof QuantityType);
        assertFalse(fake.awaitNoOpenConnections(200), "the stream is still open");
    }
}
