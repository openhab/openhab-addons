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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.handler.GroupAddressListener;
import org.openhab.binding.knx.internal.monitor.Direction;
import org.openhab.binding.knx.internal.monitor.MonitorStatus;
import org.openhab.binding.knx.internal.monitor.NotDecryptedReason;
import org.openhab.binding.knx.internal.monitor.SecurityState;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.monitor.TelegramRecord;
import org.openhab.binding.knx.internal.monitor.TelegramType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Type;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.KNXAddress;
import io.calimero.KNXException;
import io.calimero.link.KNXLinkClosedException;
import io.calimero.link.KNXNetworkLink;
import io.calimero.mgmt.TransportLayerImpl;
import io.calimero.secure.KnxSecureException;
import io.calimero.secure.Security;
import io.calimero.secure.SecurityControl;

/**
 * Checks that a secured telegram that was not decrypted shows once in the group monitor, with the reason, wherever
 * the library or the binding found it.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class NotDecryptedTest {
    private static final IndividualAddress OPENHAB = new IndividualAddress(1, 2, 3);
    private static final IndividualAddress SENSOR = new IndividualAddress(1, 1, 42);
    private static final IndividualAddress STRANGER = new IndividualAddress(1, 1, 99);
    private static final GroupAddress SECURE_GROUP = new GroupAddress(8, 0, 0);
    private static final GroupAddress NO_KEY_GROUP = new GroupAddress(9, 7, 7);
    private static final byte[] WRITE_ON = { 0x00, (byte) 0x81 };

    private static final int SCF_TOOL_ACCESS = 0x90;
    private static final int SCF_UNKNOWN_ALGORITHM = 0x20;
    private static final int SCF_SYSTEM_BROADCAST_WITHOUT_TOOL_ACCESS = 0x18;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final LinkedBlockingQueue<KNXNetworkLink> links = new LinkedBlockingQueue<>();
    private final SortedMap<String, Long> unknownGA = new TreeMap<>();
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
        MonitorTestClient c = new MonitorTestClient(scheduler, monitor, () -> links.remove(), unknownGA);
        client = c;
        c.initialize();
        assertTrue(c.isConnected());
        return c;
    }

    /** A device whose secured telegrams carry rising sequence numbers, as the sensor's would. */
    private static CustomSecureManagement sensor() throws KNXLinkClosedException {
        return new CustomSecureManagement(new TransportLayerImpl(new FeedLink(SENSOR)),
                MonitorTestClient.keyringSecurity());
    }

    private static byte[] secured(CustomSecureManagement sensor) throws Exception {
        return sensor.secureGroupObject(SENSOR, SECURE_GROUP, WRITE_ON).orElseThrow();
    }

    private static byte[] withScf(byte[] apdu, int scf) {
        byte[] changed = apdu.clone();
        changed[2] = (byte) scf;
        return changed;
    }

    private static byte[] withLastByteFlipped(byte[] apdu) {
        byte[] changed = apdu.clone();
        changed[changed.length - 1] ^= 0x55;
        return changed;
    }

    private static List<TelegramRecord> notDecrypted(TelegramMonitor monitor) {
        return monitor.records().stream().filter(r -> r.security() == SecurityState.NOT_DECRYPTED).toList();
    }

    private static void assertOneRow(TelegramMonitor monitor, GroupAddress destination,
            @Nullable NotDecryptedReason reason) {
        assertEquals(1, monitor.records().size(), "one telegram gives one row");
        TelegramRecord row = monitor.records().get(0);
        assertEquals(SecurityState.NOT_DECRYPTED, row.security());
        assertEquals(reason, row.reason());
        assertEquals(TelegramType.UNKNOWN, row.type());
        assertEquals(Direction.IN, row.direction());
        assertEquals(0, row.payload().length);
        assertEquals(SENSOR, row.source());
        assertEquals(destination, row.destination());
        MonitorStatus status = monitor.status();
        assertEquals(1, status.notDecryptedTotal(), "one telegram gives one count");
        if (reason != null) {
            assertEquals(1L, status.notDecrypted().getOrDefault(reason, 0L));
        }
    }

    @Test
    void aSequenceNumberThatIsNotAboveTheLastOneIsSequenceNumber() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        connect(monitor, link);
        try (CustomSecureManagement sensor = sensor()) {
            byte[] frame = secured(sensor);

            link.receive(SENSOR, SECURE_GROUP, frame);
            link.receive(SENSOR, SECURE_GROUP, frame);
        }

        List<TelegramRecord> rows = monitor.records();
        assertEquals(2, rows.size());
        assertEquals(SecurityState.SECURED, rows.get(0).security());
        assertEquals(NotDecryptedReason.SEQUENCE_NUMBER, rows.get(1).reason());
        assertEquals(1, monitor.status().notDecryptedTotal());
    }

    @Test
    void aChangedFrameIsAuthentication() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        connect(monitor, link);
        try (CustomSecureManagement sensor = sensor()) {
            link.receive(SENSOR, SECURE_GROUP, withLastByteFlipped(secured(sensor)));
        }

        assertOneRow(monitor, SECURE_GROUP, NotDecryptedReason.AUTHENTICATION);
    }

    @Test
    void anUnknownAlgorithmIsInvalidControlField() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        connect(monitor, link);
        try (CustomSecureManagement sensor = sensor()) {
            link.receive(SENSOR, SECURE_GROUP, withScf(secured(sensor), SCF_UNKNOWN_ALGORITHM));
        }

        assertOneRow(monitor, SECURE_GROUP, NotDecryptedReason.INVALID_CONTROL_FIELD);
    }

    @Test
    void toolAccessOnAGroupAddressIsAccess() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        connect(monitor, link);
        try (CustomSecureManagement sensor = sensor()) {
            link.receive(SENSOR, SECURE_GROUP, withScf(secured(sensor), SCF_TOOL_ACCESS));
        }

        assertOneRow(monitor, SECURE_GROUP, NotDecryptedReason.ACCESS);
    }

    @Test
    void anAddressWithoutAGroupKeyIsNoKey() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        connect(monitor, link);
        assertFalse(MonitorTestClient.keyringSecurity().groupKeys().containsKey(NO_KEY_GROUP));
        try (CustomSecureManagement sensor = sensor()) {
            link.receive(SENSOR, NO_KEY_GROUP, secured(sensor));
        }

        assertOneRow(monitor, NO_KEY_GROUP, NotDecryptedReason.NO_KEY);
    }

    @Test
    void aMissingGroupKeyStillCountsInUnknownGaAsBefore() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        connect(monitor, link);
        try (CustomSecureManagement sensor = sensor()) {
            link.receive(SENSOR, NO_KEY_GROUP, secured(sensor));
            link.receive(SENSOR, NO_KEY_GROUP, secured(sensor));
        }

        assertEquals(Set.of(" 9/7/  7  secure: missing group key, cannot decrypt"), unknownGA.keySet());
        assertEquals(2L, unknownGA.values().iterator().next());
        assertEquals(2, monitor.status().notDecryptedTotal());
    }

    @Test
    void aSenderTheKeyringDoesNotListIsDecryptedAndDeliveredWithTheSecureLayerAsBuilt() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        MonitorTestClient c = connect(monitor, link);
        GroupAddressListener thing = mock(GroupAddressListener.class);
        when(thing.listensTo(SECURE_GROUP)).thenReturn(true);
        c.registerGroupAddressListener(thing);
        try (CustomSecureManagement stranger = new CustomSecureManagement(
                new TransportLayerImpl(new FeedLink(STRANGER)), MonitorTestClient.keyringSecurity())) {
            link.receive(STRANGER, SECURE_GROUP, stranger.secureGroupObject(STRANGER, SECURE_GROUP, WRITE_ON).get());
        }

        List<TelegramRecord> rows = monitor.records();
        assertEquals(1, rows.size(), "one telegram gives one row");
        assertEquals(SecurityState.SECURED, rows.get(0).security());
        assertEquals(0, monitor.status().notDecryptedTotal());
        verify(thing, timeout(10_000)).onGroupWrite(same(c), eq(STRANGER), eq(SECURE_GROUP), any());
    }

    @Test
    void aSenderTheSecureLayerDropsIsSenderNotListedOnce() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        MonitorTestClient c = connect(monitor, link);
        c.secureLayers.get(0).security().groupSenders(OPENHAB).put(SECURE_GROUP, Set.of(SENSOR));
        try (CustomSecureManagement stranger = new CustomSecureManagement(
                new TransportLayerImpl(new FeedLink(STRANGER)), MonitorTestClient.keyringSecurity())) {
            link.receive(STRANGER, SECURE_GROUP, stranger.secureGroupObject(STRANGER, SECURE_GROUP, WRITE_ON).get());
        }

        assertEquals(1, monitor.records().size(), "one telegram gives one row");
        TelegramRecord row = monitor.records().get(0);
        assertEquals(NotDecryptedReason.SENDER_NOT_LISTED, row.reason());
        assertEquals(STRANGER, row.source());
        assertEquals(1, monitor.status().notDecryptedTotal());
    }

    @Test
    void aListedSenderIsNotReportedWhenTheSecureLayerHasASenderList() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        MonitorTestClient c = connect(monitor, link);
        c.secureLayers.get(0).security().groupSenders(OPENHAB).put(SECURE_GROUP, Set.of(SENSOR));
        try (CustomSecureManagement sensor = sensor()) {
            link.receive(SENSOR, SECURE_GROUP, secured(sensor));
        }

        assertEquals(List.of(SecurityState.SECURED), monitor.records().stream().map(TelegramRecord::security).toList());
        assertEquals(0, monitor.status().notDecryptedTotal());
    }

    @Test
    void toolAccessWithAnUnlistedSenderIsOnlyAccess() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        MonitorTestClient c = connect(monitor, link);
        c.secureLayers.get(0).security().groupSenders(OPENHAB).put(SECURE_GROUP, Set.of(STRANGER));
        try (CustomSecureManagement sensor = sensor()) {
            link.receive(SENSOR, SECURE_GROUP, withScf(secured(sensor), SCF_TOOL_ACCESS));
        }

        assertOneRow(monitor, SECURE_GROUP, NotDecryptedReason.ACCESS);
    }

    @Test
    void aShortFrameWithoutAGroupKeyIsOnlyAuthentication() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        connect(monitor, link);
        byte[] shortFrame;
        try (CustomSecureManagement sensor = sensor()) {
            shortFrame = java.util.Arrays.copyOf(secured(sensor), 10);
        }

        link.receive(SENSOR, NO_KEY_GROUP, shortFrame);

        assertOneRow(monitor, NO_KEY_GROUP, NotDecryptedReason.AUTHENTICATION);
    }

    @Test
    void anUnknownAlgorithmWithoutAGroupKeyIsOnlyInvalidControlField() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        connect(monitor, link);
        try (CustomSecureManagement sensor = sensor()) {
            link.receive(SENSOR, NO_KEY_GROUP, withScf(secured(sensor), SCF_UNKNOWN_ALGORITHM));
        }

        assertOneRow(monitor, NO_KEY_GROUP, NotDecryptedReason.INVALID_CONTROL_FIELD);
    }

    @Test
    void aSystemBroadcastFlagWithoutToolAccessIsDroppedByTheLibraryWithoutARow() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        connect(monitor, link);
        try (CustomSecureManagement sensor = sensor()) {
            byte[] frame = withScf(secured(sensor), SCF_SYSTEM_BROADCAST_WITHOUT_TOOL_ACCESS);
            link.receive(SENSOR, SECURE_GROUP, frame);
            link.receive(SENSOR, NO_KEY_GROUP, frame);
        }

        assertEquals(List.of(), monitor.records());
        assertEquals(0, monitor.status().notDecryptedTotal());
    }

    @Test
    void aFrameFromOurOwnAddressIsNotReported() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        connect(monitor, link);
        try (CustomSecureManagement sensor = sensor()) {
            link.receive(OPENHAB, NO_KEY_GROUP, secured(sensor));
        }

        assertEquals(List.of(), monitor.records());
    }

    @Test
    void theLibraryCountersStillCount() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        MonitorTestClient c = connect(monitor, link);
        try (CustomSecureManagement sensor = sensor()) {
            link.receive(SENSOR, SECURE_GROUP, withLastByteFlipped(secured(sensor)));
        }

        assertEquals(1, c.secureLayers.get(0).failureCounterOf(3));
        assertEquals(0, c.secureLayers.get(0).failureCounterOf(1));
    }

    @Test
    void anErrorTypeOutsideOneToFourDoesNotThrowAndStillGivesARow() {
        TelegramMonitor monitor = new TelegramMonitor();
        MonitorTestClient c = connect(monitor, new FeedLink(OPENHAB));
        CustomSecureManagement sal = c.secureLayers.get(0);

        for (int errorType : new int[] { 0, 5, 99, -1 }) {
            assertDoesNotThrow(() -> sal.securityFailure(errorType, i -> i + 1, SENSOR, SECURE_GROUP, 0x80, 7));
        }

        assertEquals(4, monitor.records().size());
        assertTrue(monitor.records().stream()
                .allMatch(r -> r.security() == SecurityState.NOT_DECRYPTED && r.reason() == null));
        assertEquals(4, monitor.status().notDecryptedOtherReason());
        assertEquals(0, monitor.status().notDecrypted().size());
    }

    @Test
    void onlyGroupDestinationsOtherThanZeroGiveARow() {
        TelegramMonitor monitor = new TelegramMonitor();
        MonitorTestClient c = connect(monitor, new FeedLink(OPENHAB));
        CustomSecureManagement sal = c.secureLayers.get(0);

        sal.securityFailure(3, i -> i + 1, SENSOR, new IndividualAddress(1, 1, 50), 0, 7);
        sal.securityFailure(3, i -> i + 1, SENSOR, GroupAddress.Broadcast, 0x80, 7);
        sal.securityFailure(3, i -> i + 1, SENSOR, SECURE_GROUP, 0x80, 7);

        assertEquals(1, monitor.records().size());
        assertEquals(SECURE_GROUP, monitor.records().get(0).destination());
    }

    @Test
    void aFailureOnTheReceivePathIsARowEvenFromOurOwnAddress() {
        TelegramMonitor monitor = new TelegramMonitor();
        MonitorTestClient c = connect(monitor, new FeedLink(OPENHAB));

        c.secureLayers.get(0).securityFailure(3, i -> i + 1, OPENHAB, SECURE_GROUP, 0x80, 7);

        assertEquals(1, notDecrypted(monitor).size());
    }

    @Test
    void aFailureWhileSecuringOurOwnTelegramIsNoReceivedRow() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        links.add(link);
        FailingClient c = new FailingClient(scheduler, monitor, () -> links.remove());
        client = c;
        c.initialize();

        assertThrows(KNXException.class, () -> c.writeToKNX(new SecuredOnSpec()));

        assertTrue(c.failing.get().hookCalls > 0, "the library's hook was called while securing");
        assertEquals(List.of(), notDecrypted(monitor));
        assertEquals(0, monitor.status().notDecryptedTotal());
        assertTrue(monitor.records().stream().allMatch(r -> r.direction() == Direction.OUT));
    }

    @Test
    void theOutgoingFailureStillCountsInTheLibraryCounters() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        links.add(link);
        FailingClient c = new FailingClient(scheduler, monitor, () -> links.remove());
        client = c;
        c.initialize();

        assertThrows(KNXException.class, () -> c.writeToKNX(new SecuredOnSpec()));

        assertEquals(c.failing.get().hookCalls, c.failing.get().failureCounterOf(3));
    }

    @Test
    void theNotDecryptedCountersStartAfreshAtEveryConnectAndTheRowsStay() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink first = new FeedLink(OPENHAB);
        MonitorTestClient c = connect(monitor, first);
        try (CustomSecureManagement sensor = sensor()) {
            first.receive(SENSOR, SECURE_GROUP, withLastByteFlipped(secured(sensor)));
        }
        assertEquals(1, monitor.status().notDecryptedTotal());
        assertFalse(monitor.notDecryptedSenders().isEmpty());
        FeedLink second = new FeedLink(OPENHAB);
        links.add(second);

        first.open = false;
        c.initialize();

        assertEquals(0, monitor.status().notDecryptedTotal());
        assertTrue(monitor.notDecryptedSenders().isEmpty());
        assertEquals(1, notDecrypted(monitor).size(), "the rows are not reset");
    }

    @Test
    void aFailureInTheMonitorDoesNotStopTheNextTelegram() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        MonitorTestClient c = connect(monitor, link);
        GroupAddressListener thing = mock(GroupAddressListener.class);
        when(thing.listensTo(any())).thenThrow(new IllegalStateException("made-up failure in a channel"));
        c.registerGroupAddressListener(thing);
        try (CustomSecureManagement sensor = sensor()) {
            link.receive(SENSOR, SECURE_GROUP, withLastByteFlipped(secured(sensor)));
            link.receive(SENSOR, SECURE_GROUP, withScf(secured(sensor), SCF_UNKNOWN_ALGORITHM));
        }

        assertEquals(2, notDecrypted(monitor).size());
    }

    private static class FailingClient extends MonitorTestClient {
        final AtomicReference<FailingSecureManagement> failing = new AtomicReference<>();

        FailingClient(ScheduledExecutorService scheduler, TelegramMonitor monitor,
                java.util.function.Supplier<KNXNetworkLink> links) {
            super(scheduler, monitor, links);
        }

        @Override
        CustomSecureManagement newSecureManagement(TransportLayerImpl transportLayer) throws KNXLinkClosedException {
            FailingSecureManagement sal = new FailingSecureManagement(transportLayer, openhabSecurity);
            failing.set(sal);
            return sal;
        }
    }

    /** Fails while securing, as the library does when its cipher fails, calling the hook with our own address. */
    private static class FailingSecureManagement extends MonitorTestClient.RecordingSecureManagement {
        volatile int hookCalls;

        FailingSecureManagement(TransportLayerImpl transportLayer, Security security) throws KNXLinkClosedException {
            super(transportLayer, security);
        }

        @Override
        @org.eclipse.jdt.annotation.NonNullByDefault({})
        public Optional<byte[]> secureData(IndividualAddress src, KNXAddress dst, byte[] apdu,
                SecurityControl securityCtrl) {
            hookCalls++;
            securityFailure(3, i -> i + 1, src, dst, 0x80, 1);
            throw new KnxSecureException("made-up failure while securing");
        }
    }

    private static class SecuredOnSpec implements OutboundSpec {
        @Override
        public String getDPT() {
            return "1.001";
        }

        @Override
        public GroupAddress getGroupAddress() {
            return SECURE_GROUP;
        }

        @Override
        public Type getValue() {
            return OnOffType.ON;
        }

        @Override
        public boolean matchesDestination(GroupAddress groupAddress) {
            return SECURE_GROUP.equals(groupAddress);
        }
    }
}
