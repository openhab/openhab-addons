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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

import io.calimero.DataUnitBuilder;
import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.KNXAddress;
import io.calimero.KNXTimeoutException;
import io.calimero.Priority;
import io.calimero.cemi.CEMILData;
import io.calimero.device.ProcessCommunicationResponder;
import io.calimero.link.medium.KNXMediumSettings;
import io.calimero.mgmt.TransportLayerImpl;
import io.calimero.process.ProcessCommunicator;
import io.calimero.secure.Keyring;
import io.calimero.secure.SecureApplicationLayer;
import io.calimero.secure.Security;

/**
 * Checks that telegrams to secure group addresses go out the way a KNX Data Secure receiver accepts them.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class DataSecureTest {
    private static final GroupAddress SECURE_GROUP = new GroupAddress(8, 0, 0);
    private static final GroupAddress PLAIN_GROUP = new GroupAddress(1, 0, 0);
    private static final IndividualAddress OPENHAB = new IndividualAddress(1, 2, 3);
    private static final IndividualAddress RECEIVER = new IndividualAddress(1, 1, 42);

    private static final int GROUP_READ = 0x00;
    private static final int GROUP_RESPONSE = 0x40;
    private static final int GROUP_WRITE = 0x80;
    private static final int TOOL_ACCESS = 0x80;

    @Test
    void groupWriteIsEncryptedWithTheGroupKey() throws Exception {
        CapturingLink link = new CapturingLink(OPENHAB);
        ProcessCommunicator communicator = AbstractKNXClient.newProcessCommunicator(link, newSal(link));

        communicator.write(SECURE_GROUP, true);

        byte[] secured = link.lastApdu();
        assertEquals(SecureApplicationLayer.SecureService, DataUnitBuilder.getAPDUService(secured));
        assertEquals(0, secured[2] & TOOL_ACCESS);
        byte[] plain = decrypt(secured);
        assertEquals(GROUP_WRITE, DataUnitBuilder.getAPDUService(plain));
        assertEquals(1, plain[1] & 0x3f);
    }

    @Test
    void groupReadIsEncryptedWithTheGroupKey() throws Exception {
        CapturingLink link = new CapturingLink(OPENHAB);
        ProcessCommunicator communicator = AbstractKNXClient.newProcessCommunicator(link, newSal(link));
        communicator.responseTimeout(Duration.ofMillis(50));

        assertThrows(KNXTimeoutException.class, () -> communicator.readBool(SECURE_GROUP));

        assertEquals(GROUP_READ, DataUnitBuilder.getAPDUService(decrypt(link.lastApdu())));
    }

    @Test
    void readResponseIsEncryptedWithTheGroupKey() throws Exception {
        CapturingLink link = new CapturingLink(OPENHAB);
        try (ProcessCommunicationResponder responder = new ProcessCommunicationResponder(link, newSal(link))) {
            responder.write(SECURE_GROUP, true);
        }

        assertEquals(GROUP_RESPONSE, DataUnitBuilder.getAPDUService(decrypt(link.lastApdu())));
    }

    @Test
    void groupWriteWithoutKeyStaysPlain() throws Exception {
        CapturingLink link = new CapturingLink(OPENHAB);
        ProcessCommunicator communicator = AbstractKNXClient.newProcessCommunicator(link, newSal(link));

        communicator.write(PLAIN_GROUP, true);

        assertEquals(GROUP_WRITE, DataUnitBuilder.getAPDUService(link.lastApdu()));
    }

    @Test
    void sequenceNumberFollowsTheClock() throws Exception {
        CapturingLink link = new CapturingLink(OPENHAB);
        long before = CustomSecureManagement.initialSequenceNumber(Instant.now());
        ProcessCommunicator communicator = AbstractKNXClient.newProcessCommunicator(link, newSal(link));
        long after = CustomSecureManagement.initialSequenceNumber(Instant.now());

        communicator.write(SECURE_GROUP, true);

        long sequenceNumber = sequenceNumber(link.lastApdu());
        assertTrue(sequenceNumber >= before && sequenceNumber <= after,
                sequenceNumber + " not in [" + before + ", " + after + "]");
    }

    @Test
    void concurrentSendsGetDistinctSequenceNumbers() throws Exception {
        CapturingLink link = new CapturingLink(OPENHAB);
        ProcessCommunicator communicator = AbstractKNXClient.newProcessCommunicator(link, newSal(link));
        int threads = 8;
        int writesPerThread = 100;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    for (int i = 0; i < writesPerThread; i++) {
                        communicator.write(SECURE_GROUP, true);
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

        Set<Long> sequenceNumbers = new HashSet<>();
        for (byte[] apdu : link.allApdus()) {
            sequenceNumbers.add(sequenceNumber(apdu));
        }
        assertEquals(threads * writesPerThread, sequenceNumbers.size());
    }

    @Test
    void initialSequenceNumberCountsMillisecondsSince2018() {
        assertEquals(86_400_000L, CustomSecureManagement.initialSequenceNumber(Instant.parse("2018-01-06T00:00:00Z")));
        assertEquals(1L, CustomSecureManagement.initialSequenceNumber(Instant.parse("2017-01-01T00:00:00Z")));
    }

    private static CustomSecureManagement newSal(CapturingLink link) throws Exception {
        return new CustomSecureManagement(new TransportLayerImpl(link), keyringSecurity());
    }

    /** Decrypts like a device whose ETS configuration lists openHAB as a sender of the group. */
    private static byte[] decrypt(byte[] securedApdu) throws Exception {
        Security security = keyringSecurity();
        security.groupSenders(RECEIVER).put(SECURE_GROUP, Set.of(OPENHAB));
        try (SecureApplicationLayer receiver = new SecureApplicationLayer(new CapturingLink(RECEIVER), security)) {
            CEMILData received = new CEMILData(CEMILData.MC_LDATA_IND, OPENHAB, SECURE_GROUP, securedApdu,
                    Priority.LOW);
            byte[] plain = receiver.extract(received).apdu();
            assertNotEquals(0, plain.length, "receiver dropped the telegram");
            return plain;
        }
    }

    private static long sequenceNumber(byte[] securedApdu) {
        long sequenceNumber = 0;
        for (int i = 3; i < 9; i++) {
            sequenceNumber = (sequenceNumber << 8) | (securedApdu[i] & 0xff);
        }
        return sequenceNumber;
    }

    private static Security keyringSecurity() {
        @SuppressWarnings("null")
        String keyringFile = DataSecureTest.class.getClassLoader()
                .getResource("misc" + File.separator + "openhab6.knxkeys").toString();
        Security security = Security.newSecurity();
        security.useKeyring(Keyring.load(keyringFile), "habopen".toCharArray());
        return security;
    }

    /** Records outgoing telegrams instead of looping them back. */
    private static class CapturingLink extends DummyKNXNetworkLink {
        private final IndividualAddress address;
        private final List<byte[]> apdus = Collections.synchronizedList(new ArrayList<>());
        private volatile byte[] lastApdu = new byte[0];

        CapturingLink(IndividualAddress address) {
            this.address = address;
        }

        @Override
        public KNXMediumSettings getKNXMedium() {
            return KNXMediumSettings.create(KNXMediumSettings.MEDIUM_TP1, address);
        }

        @Override
        public void sendRequestWait(@Nullable KNXAddress dst, @Nullable Priority p, byte @Nullable... nsdu) {
            if (nsdu != null) {
                lastApdu = nsdu.clone();
                apdus.add(lastApdu);
            }
        }

        byte[] lastApdu() {
            return lastApdu;
        }

        List<byte[]> allApdus() {
            synchronized (apdus) {
                return new ArrayList<>(apdus);
            }
        }
    }
}
