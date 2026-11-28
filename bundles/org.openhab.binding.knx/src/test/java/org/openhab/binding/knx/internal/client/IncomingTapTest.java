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
package org.openhab.binding.knx.internal.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.client.MonitorTestClient.RecordingSecureManagement;
import org.openhab.binding.knx.internal.handler.GroupAddressListener;
import org.openhab.binding.knx.internal.monitor.ConnectionKind;
import org.openhab.binding.knx.internal.monitor.DataType;
import org.openhab.binding.knx.internal.monitor.Direction;
import org.openhab.binding.knx.internal.monitor.IncomingTap;
import org.openhab.binding.knx.internal.monitor.MonitorStatus;
import org.openhab.binding.knx.internal.monitor.SecurityState;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.monitor.TelegramRecord;
import org.openhab.binding.knx.internal.monitor.TelegramType;
import org.openhab.core.library.types.OnOffType;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.link.KNXNetworkLink;
import io.calimero.mgmt.TransportLayerImpl;

/**
 * Checks the listener the group monitor puts on the secure layer for received telegrams.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class IncomingTapTest {
    private static final IndividualAddress OPENHAB = new IndividualAddress(1, 2, 3);
    private static final IndividualAddress SENSOR = new IndividualAddress(1, 1, 42);
    private static final GroupAddress SECURE_GROUP = new GroupAddress(8, 0, 0);
    private static final GroupAddress PLAIN_GROUP = new GroupAddress(1, 0, 0);
    private static final byte[] WRITE_ON = { 0x00, (byte) 0x81 };

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final LinkedBlockingQueue<KNXNetworkLink> links = new LinkedBlockingQueue<>();
    private @Nullable MonitorTestClient client;

    @AfterEach
    void tearDown() {
        MonitorTestClient tmp = client;
        if (tmp != null) {
            tmp.dispose();
        }
        scheduler.shutdownNow();
    }

    private MonitorTestClient connect(TelegramMonitor monitor, FeedLink link) {
        links.add(link);
        MonitorTestClient c = new MonitorTestClient(scheduler, monitor, () -> links.remove());
        client = c;
        c.initialize();
        assertTrue(c.isConnected());
        return c;
    }

    /** A secured telegram as the sensor sends it: the source address is part of what the check code covers. */
    private static byte[] securedBySensor(byte[] plainApdu) throws Exception {
        try (CustomSecureManagement sensor = new CustomSecureManagement(new TransportLayerImpl(new FeedLink(SENSOR)),
                MonitorTestClient.keyringSecurity())) {
            return sensor.secureGroupObject(SENSOR, SECURE_GROUP, plainApdu).orElseThrow();
        }
    }

    private static long taps(RecordingSecureManagement sal) {
        return sal.listeners.stream().filter(IncomingTap.class::isInstance).count();
    }

    @Test
    void aPlainWriteIsAPlainRow() {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        connect(monitor, link);

        link.receive(SENSOR, PLAIN_GROUP, WRITE_ON);

        List<TelegramRecord> records = monitor.records();
        assertEquals(1, records.size());
        TelegramRecord record = records.get(0);
        assertEquals(SENSOR, record.source());
        assertEquals(PLAIN_GROUP, record.destination());
        assertEquals(TelegramType.WRITE, record.type());
        assertEquals(Direction.IN, record.direction());
        assertEquals(SecurityState.PLAIN, record.security());
        assertArrayEquals(new byte[] { 1 }, record.payload());
    }

    @Test
    void aSecuredWriteIsASecuredRowWithThePlainValue() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        connect(monitor, link);

        link.receive(SENSOR, SECURE_GROUP, securedBySensor(WRITE_ON));

        TelegramRecord record = monitor.records().get(0);
        assertEquals(SecurityState.SECURED, record.security());
        assertEquals(TelegramType.WRITE, record.type());
        assertArrayEquals(new byte[] { 1 }, record.payload());
    }

    @Test
    void aPlainTelegramOnAnAddressWithAGroupKeyIsMarkedAndStillDelivered() {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        MonitorTestClient c = connect(monitor, link);
        GroupAddressListener thing = mock(GroupAddressListener.class);
        when(thing.listensTo(SECURE_GROUP)).thenReturn(true);
        c.registerGroupAddressListener(thing);

        link.receive(SENSOR, SECURE_GROUP, WRITE_ON);

        TelegramRecord record = monitor.records().get(0);
        assertEquals(SecurityState.PLAIN, record.security());
        assertTrue(record.plainOnSecuredAddress());
        assertEquals(1, monitor.status().plainOnSecuredAddress());
        verify(thing, timeout(10_000)).onGroupWrite(same(c), eq(SENSOR), eq(SECURE_GROUP), any());
    }

    @Test
    void onlyAPlainTelegramOnASecuredAddressIsMarked() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        connect(monitor, link);

        link.receive(SENSOR, PLAIN_GROUP, WRITE_ON);
        link.receive(SENSOR, SECURE_GROUP, securedBySensor(WRITE_ON));

        assertEquals(List.of(false, false),
                monitor.records().stream().map(TelegramRecord::plainOnSecuredAddress).toList());
        assertEquals(0, monitor.status().plainOnSecuredAddress());
    }

    @Test
    void readsAndResponsesKeepTheirService() {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        connect(monitor, link);

        link.receive(SENSOR, PLAIN_GROUP, (byte) 0x00, (byte) 0x00);
        link.receive(SENSOR, PLAIN_GROUP, (byte) 0x00, (byte) 0x40, (byte) 0x0c, (byte) 0x33);

        List<TelegramRecord> records = monitor.records();
        assertEquals(TelegramType.READ, records.get(0).type());
        assertEquals(0, records.get(0).payload().length);
        assertEquals(TelegramType.RESPONSE, records.get(1).type());
        assertArrayEquals(new byte[] { 0x0c, 0x33 }, records.get(1).payload());
    }

    @Test
    void theConnectionIsInTheStatus() {
        TelegramMonitor monitor = new TelegramMonitor();
        MonitorTestClient c = connect(monitor, new FeedLink(OPENHAB));

        assertEquals(new MonitorStatus.Connection(ConnectionKind.TUNNEL, OPENHAB), monitor.status().connection());

        c.dispose();
        assertNull(monitor.status().connection());
    }

    @Test
    void aChannelOfTheClientDecodesTheValue() {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        MonitorTestClient c = connect(monitor, link);
        GroupAddressListener thing = mock(GroupAddressListener.class);
        when(thing.listensTo(PLAIN_GROUP)).thenReturn(true);
        when(thing.monitorDataType(PLAIN_GROUP)).thenReturn(new DataType("1.001", OnOffType.class));
        c.registerGroupAddressListener(thing);

        link.receive(SENSOR, PLAIN_GROUP, WRITE_ON);

        assertEquals("ON", monitor.records().get(0).value());
    }

    @Test
    void afterAReconnectTheTapIsOnTheNewSecureLayerOnly() {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink first = new FeedLink(OPENHAB);
        MonitorTestClient c = connect(monitor, first);
        FeedLink second = new FeedLink(OPENHAB);
        links.add(second);

        first.open = false;
        c.initialize();

        assertEquals(2, c.secureLayers.size());
        assertEquals(0, taps(c.secureLayers.get(0)), "nothing is left on the old layer");
        assertEquals(1, taps(c.secureLayers.get(1)));
        second.receive(SENSOR, PLAIN_GROUP, WRITE_ON);
        assertEquals(1, monitor.records().size());
    }

    @Test
    void disposeTakesTheTapOff() {
        TelegramMonitor monitor = new TelegramMonitor();
        MonitorTestClient c = connect(monitor, new FeedLink(OPENHAB));

        c.dispose();

        assertEquals(0, taps(c.secureLayers.get(0)));
    }

    @Test
    void aFailingMonitorNeitherStopsDeliveryNorLosesTheTap() {
        TelegramMonitor monitor = new TelegramMonitor(new FailingOnceClock());
        FeedLink link = new FeedLink(OPENHAB);
        MonitorTestClient c = connect(monitor, link);
        GroupAddressListener thing = mock(GroupAddressListener.class);
        when(thing.listensTo(PLAIN_GROUP)).thenReturn(true);
        c.registerGroupAddressListener(thing);

        link.receive(SENSOR, PLAIN_GROUP, WRITE_ON);
        verify(thing, timeout(10_000)).onGroupWrite(same(c), eq(SENSOR), eq(PLAIN_GROUP), any());
        link.receive(SENSOR, PLAIN_GROUP, WRITE_ON);

        assertEquals(1, monitor.records().size(), "the second telegram is recorded");
        assertEquals(1, taps(c.secureLayers.get(0)));
        verify(thing, timeout(10_000).times(2)).onGroupWrite(same(c), eq(SENSOR), eq(PLAIN_GROUP), any());
    }

    /** Throws on its first reading. */
    private static class FailingOnceClock extends Clock {
        private final AtomicInteger readings = new AtomicInteger();

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(@Nullable ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            if (readings.getAndIncrement() == 0) {
                throw new IllegalStateException("made-up failure inside the monitor");
            }
            return Instant.now();
        }
    }
}
