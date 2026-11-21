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

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler.CommandExtensionData;
import org.openhab.binding.knx.internal.monitor.ConnectionKind;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.security.KeyringFacts;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;

import io.calimero.CloseEvent;
import io.calimero.DataUnitBuilder;
import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.KNXAddress;
import io.calimero.Priority;
import io.calimero.datapoint.Datapoint;
import io.calimero.datapoint.StateDP;
import io.calimero.link.KNXNetworkLink;
import io.calimero.link.medium.KNXMediumSettings;
import io.calimero.link.medium.TPSettings;
import io.calimero.secure.Keyring;
import io.calimero.secure.SecureApplicationLayer;
import io.calimero.secure.Security;

/**
 * A secured device obeys only senders that ETS linked to the group address. Checks that openHAB says so on the bridge
 * status while still sending everything.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class SecureLinkWarningTest {
    private static final IndividualAddress LINKED = new IndividualAddress(1, 0, 128);
    private static final IndividualAddress UNLINKED = new IndividualAddress(1, 1, 2);
    private static final IndividualAddress NOT_IN_KEYRING = new IndividualAddress(1, 1, 99);
    private static final GroupAddress LINKED_GA = new GroupAddress(8, 0, 0);
    private static final GroupAddress OTHER_LINKED_GA = new GroupAddress(8, 4, 0);
    private static final GroupAddress NOT_LINKED_GA = new GroupAddress(8, 4, 1);
    private static final GroupAddress PLAIN_GA = new GroupAddress(1, 0, 0);

    private static final String UNLINKED_TEXT = "@text/warning.knx-secure-link-address-unlinked [\"1.1.2\"]";

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private @Nullable TestClient client;

    @AfterEach
    void tearDown() {
        TestClient tmp = client;
        if (tmp != null) {
            tmp.dispose();
        }
        scheduler.shutdownNow();
    }

    @Test
    void linkedInterfaceSaysNothing() throws Exception {
        Setup s = connect(LINKED, keyring());

        s.client.writeToKNX(spec(LINKED_GA));
        s.client.writeToKNX(spec(OTHER_LINKED_GA));
        s.client.writeToKNX(spec(PLAIN_GA));

        assertEquals(List.of("ONLINE"), s.status.events());
        assertEquals(3, s.link.frames().size());
    }

    @Test
    void unlinkedInterfaceIsReportedOnConnect() throws Exception {
        Setup s = connect(UNLINKED, keyring());

        assertEquals(List.of("ONLINE " + UNLINKED_TEXT), s.status.events());
    }

    @Test
    void unlinkedInterfaceIsReportedOncePerAddressAndEverythingStillGoesOut() throws Exception {
        Setup s = connect(UNLINKED, keyring());
        s.status.events().clear();

        for (int i = 0; i < 3; i++) {
            s.client.writeToKNX(spec(LINKED_GA));
        }
        s.client.writeToKNX(spec(OTHER_LINKED_GA));
        s.client.writeToKNX(spec(PLAIN_GA));

        assertEquals(
                List.of("ONLINE @text/warning.knx-secure-link-address-unlinked-groups [\"1.1.2\",\"8/0/0\"]",
                        "ONLINE @text/warning.knx-secure-link-address-unlinked-groups [\"1.1.2\",\"8/0/0; 8/4/0\"]"),
                s.status.events());
        assertEquals(5, s.link.frames().size(), "no telegram is held back");
        assertEquals(4, s.link.frames().stream().filter(SecureLinkWarningTest::isSecured).count());
    }

    @Test
    void groupLinkedToOtherInterfaceIsReported() throws Exception {
        Setup s = connect(LINKED, keyring());

        s.client.writeToKNX(spec(NOT_LINKED_GA));
        s.client.writeToKNX(spec(NOT_LINKED_GA));

        assertEquals(List.of("ONLINE", "ONLINE @text/warning.knx-secure-link-groups-unlinked [\"1.0.128\",\"8/4/1\"]"),
                s.status.events());
        assertEquals(2, s.link.frames().size());
    }

    @Test
    void readResponseIsChecked() throws Exception {
        Setup s = connect(LINKED, keyring());

        s.client.respondToKNX(spec(NOT_LINKED_GA));

        assertEquals("ONLINE @text/warning.knx-secure-link-groups-unlinked [\"1.0.128\",\"8/4/1\"]",
                s.status.events().get(1));
        assertEquals(1, s.link.frames().size());
    }

    @Test
    void readRequestIsChecked() throws Exception {
        Setup s = connect(LINKED, keyring());

        Datapoint datapoint = new StateDP(NOT_LINKED_GA, "made up", 0, "1.001");
        s.client.readDatapoint(datapoint);

        assertEquals("ONLINE", s.status.next());
        assertEquals("ONLINE @text/warning.knx-secure-link-groups-unlinked [\"1.0.128\",\"8/4/1\"]", s.status.next());
        assertTrue(isSecured(s.link.nextFrame()), "the read request went out");
    }

    @Test
    void clientReportsTheAddressItSendsFrom() throws Exception {
        Setup s = connect(LINKED, keyring());

        assertEquals(Optional.of(LINKED), s.client.ownAddress());
        s.client.dispose();
        assertEquals(Optional.empty(), s.client.ownAddress());
    }

    @Test
    void addressMissingInKeyringIsReported() throws Exception {
        Setup s = connect(NOT_IN_KEYRING, keyring());

        assertEquals(List.of("ONLINE @text/warning.knx-secure-link-address-unknown [\"1.1.99\"]"), s.status.events());
    }

    @Test
    void withoutKeyringNothingChanges() throws Exception {
        Setup s = connect(UNLINKED, null);

        s.client.writeToKNX(spec(LINKED_GA));

        assertEquals(List.of("ONLINE"), s.status.events());
        assertEquals(1, s.link.frames().size());
    }

    @Test
    void keyringWithoutSecuredGroupAddressesChangesNothing() throws Exception {
        KeyringFacts noGroups = new KeyringFacts(loadKeyring("openhab6-minimal-sipif.knxkeys"));
        Setup s = connect(new IndividualAddress(1, 1, 2), noGroups);

        s.client.writeToKNX(spec(LINKED_GA));

        assertEquals(List.of("ONLINE"), s.status.events());
    }

    @Test
    void addressTheRouterAssignedCountsBeforeTheConfiguredOne() throws Exception {
        KNXMediumSettings settings = new TPSettings(NOT_IN_KEYRING);
        assertEquals(NOT_IN_KEYRING, AbstractKNXClient.ownAddressOf(settings));

        VarHandle assigned = MethodHandles.privateLookupIn(KNXMediumSettings.class, MethodHandles.lookup())
                .findVarHandle(KNXMediumSettings.class, "assigned", Supplier.class);
        assigned.setVolatile(settings, (Supplier<Optional<IndividualAddress>>) () -> Optional.of(LINKED));

        assertEquals(LINKED, AbstractKNXClient.ownAddressOf(settings));
    }

    @Test
    void warningIsGoneAfterAReconnectWithTheProblemFixed() throws Exception {
        AtomicReference<@Nullable KeyringFacts> facts = new AtomicReference<>(keyring());
        Setup s = connectWith(UNLINKED, facts::get);
        s.client.writeToKNX(spec(LINKED_GA));
        assertEquals("ONLINE @text/warning.knx-secure-link-address-unlinked-groups [\"1.1.2\",\"8/0/0\"]",
                s.status.events().get(s.status.events().size() - 1));

        // the link is lost; the keyring exported after fixing ETS has no secured group address in it any more
        facts.set(new KeyringFacts(loadKeyring("openhab6-minimal-sipif.knxkeys")));
        s.link.open = false;
        s.status.events().clear();
        s.client.initialize();

        assertEquals(List.of("ONLINE"), s.status.events());
        s.client.writeToKNX(spec(LINKED_GA));
        assertEquals(List.of("ONLINE"), s.status.events());
    }

    @Test
    void aSendThatStartedBeforeTheLinkWasLostDoesNotBringTheBridgeBackOnline() throws Exception {
        // the read job runs once right after connecting and then only after an hour
        Setup s = connectWith(LINKED, () -> keyring(), 3_600_000);
        assertTrue(s.client.readJobHasRun.await(10, TimeUnit.SECONDS));
        s.status.events().clear();

        s.link.open = false;
        s.client.linkClosed(new CloseEvent(s.link, CloseEvent.INTERNAL, "link lost"));
        // the send had passed its connection check just before the link went away
        s.client.pretendConnectedOnceFor.set(Thread.currentThread());
        s.client.writeToKNX(spec(NOT_LINKED_GA));

        assertEquals(1, s.status.events().size(), s.status.events().toString());
        assertTrue(s.status.events().get(0).startsWith("OFFLINE COMMUNICATION_ERROR"), s.status.events().toString());
    }

    @Test
    void aFailingStatusCallbackDoesNotHoldBackTheTelegram() throws Exception {
        Setup s = connect(LINKED, keyring());
        s.status.failOnWarning = true;

        s.client.writeToKNX(spec(NOT_LINKED_GA));

        assertEquals(1, s.link.frames().size(), "the telegram still goes out");
    }

    @Test
    void aValueThatIsNeverSentRaisesNoWarning() throws Exception {
        Setup s = connect(LINKED, keyring());

        s.client.writeToKNX(spec(NOT_LINKED_GA, UnDefType.NULL));

        assertEquals(0, s.link.frames().size());
        assertEquals(List.of("ONLINE"), s.status.events());
    }

    @Test
    void severalThreadsReportAnAddressOnce() throws Exception {
        Setup s = connect(LINKED, keyring());
        int threads = 8;
        int writesPerThread = 25;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    for (int i = 0; i < writesPerThread; i++) {
                        s.client.writeToKNX(spec(NOT_LINKED_GA));
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdownNow();
        }

        assertEquals(List.of("ONLINE", "ONLINE @text/warning.knx-secure-link-groups-unlinked [\"1.0.128\",\"8/4/1\"]"),
                s.status.events());
        assertEquals(threads * writesPerThread, s.link.frames().size());
    }

    private Setup connect(IndividualAddress own, @Nullable KeyringFacts facts) {
        return connectWith(own, () -> facts);
    }

    private Setup connectWith(IndividualAddress own, Supplier<@Nullable KeyringFacts> facts) {
        return connectWith(own, facts, 50);
    }

    private Setup connectWith(IndividualAddress own, Supplier<@Nullable KeyringFacts> facts, int readingPause) {
        RecordingLink link = new RecordingLink(own);
        StatusRecorder status = new StatusRecorder();
        TestClient c = new TestClient(link, facts, status, readingPause);
        client = c;
        c.initialize();
        return new Setup(c, link, status);
    }

    private static KeyringFacts keyring() {
        return new KeyringFacts(loadKeyring("openhab6.knxkeys"));
    }

    private static boolean isSecured(byte[] apdu) {
        return DataUnitBuilder.getAPDUService(apdu) == SecureApplicationLayer.SecureService;
    }

    private static OutboundSpec spec(GroupAddress groupAddress) {
        return spec(groupAddress, OnOffType.ON);
    }

    private static OutboundSpec spec(GroupAddress groupAddress, Type value) {
        return new OutboundSpec() {
            @Override
            public String getDPT() {
                return "1.001";
            }

            @Override
            public GroupAddress getGroupAddress() {
                return groupAddress;
            }

            @Override
            public Type getValue() {
                return value;
            }

            @Override
            public boolean matchesDestination(GroupAddress other) {
                return groupAddress.equals(other);
            }
        };
    }

    private record Setup(TestClient client, RecordingLink link, StatusRecorder status) {
    }

    private class TestClient extends AbstractKNXClient {
        private final RecordingLink link;
        final CountDownLatch readJobHasRun = new CountDownLatch(1);
        private final Thread owner = Thread.currentThread();
        final AtomicReference<@Nullable Thread> pretendConnectedOnceFor = new AtomicReference<>();

        TestClient(RecordingLink link, Supplier<@Nullable KeyringFacts> facts, StatusRecorder status,
                int readingPause) {
            super(0, new ThingUID("knx", "ip", "test"), 1, readingPause, 1, scheduler,
                    new CommandExtensionData(new TreeMap<>()), new TelegramMonitor(), keyringSecurity(), facts, status);
            this.link = link;
        }

        @Override
        public boolean isConnected() {
            boolean connected = pretendConnectedOnceFor.compareAndSet(Thread.currentThread(), null)
                    || super.isConnected();
            if (!owner.equals(Thread.currentThread())) {
                // only after the check: a test that closes the link earlier makes the read job reconnect
                readJobHasRun.countDown();
            }
            return connected;
        }

        @Override
        protected KNXNetworkLink establishConnection() {
            link.open = true;
            return link;
        }

        @Override
        protected ConnectionKind connectionKind() {
            return ConnectionKind.TUNNEL;
        }
    }

    private static Security keyringSecurity() {
        Security security = Security.newSecurity();
        security.useKeyring(loadKeyring("openhab6.knxkeys"), "habopen".toCharArray());
        return security;
    }

    /** Records outgoing telegrams instead of looping them back. */
    private static class RecordingLink extends DummyKNXNetworkLink {
        private final List<byte[]> frames = Collections.synchronizedList(new ArrayList<>());
        private final LinkedBlockingQueue<byte[]> sent = new LinkedBlockingQueue<>();
        volatile IndividualAddress address;
        volatile boolean open = true;

        RecordingLink(IndividualAddress address) {
            this.address = address;
        }

        @Override
        public KNXMediumSettings getKNXMedium() {
            return KNXMediumSettings.create(KNXMediumSettings.MEDIUM_TP1, address);
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void sendRequestWait(@Nullable KNXAddress dst, @Nullable Priority p, byte @Nullable... nsdu) {
            if (nsdu != null) {
                frames.add(nsdu.clone());
                sent.add(nsdu.clone());
            }
        }

        byte[] nextFrame() throws InterruptedException {
            byte[] frame = sent.poll(10, TimeUnit.SECONDS);
            assertNotNull(frame, "no telegram within 10 seconds");
            return frame;
        }

        List<byte[]> frames() {
            synchronized (frames) {
                return new ArrayList<>(frames);
            }
        }
    }

    private static class StatusRecorder implements StatusUpdateCallback {
        private final List<String> events = Collections.synchronizedList(new ArrayList<>());
        private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

        @Override
        public void updateStatus(ThingStatus status) {
            record(status.toString());
        }

        @Override
        public void updateStatus(ThingStatusDetail statusDetail, String description) {
            record("OFFLINE " + statusDetail + " " + description);
        }

        @Override
        public void updateStatus(ThingStatus status, @Nullable String description) {
            record(status + (description == null ? "" : " " + description));
        }

        volatile boolean failOnWarning;

        private void record(String event) {
            if (failOnWarning && event.contains("@text/")) {
                throw new IllegalStateException("status callback failed");
            }
            events.add(event);
            queue.add(event);
        }

        List<String> events() {
            return events;
        }

        String next() throws InterruptedException {
            String event = queue.poll(10, TimeUnit.SECONDS);
            assertNotNull(event, "no status update within 10 seconds");
            return event;
        }
    }

    @SuppressWarnings("null")
    private static Keyring loadKeyring(String name) {
        return Keyring.load(
                SecureLinkWarningTest.class.getClassLoader().getResource("misc" + File.separator + name).toString());
    }
}
