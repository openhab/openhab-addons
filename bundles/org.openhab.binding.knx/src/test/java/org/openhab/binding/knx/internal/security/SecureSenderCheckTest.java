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
package org.openhab.binding.knx.internal.security;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Checks when {@link SecureSenderCheck} finds that the address openHAB sends from is not linked in ETS.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class SecureSenderCheckTest {
    private static final IndividualAddress LINKED_BACKBONE = new IndividualAddress(1, 0, 128);
    private static final IndividualAddress UNLINKED_TUNNEL = new IndividualAddress(1, 1, 2);
    private static final IndividualAddress NOT_IN_KEYRING = new IndividualAddress(1, 1, 99);
    private static final GroupAddress LINKED = new GroupAddress(8, 0, 0);
    private static final GroupAddress NOT_LINKED = new GroupAddress(8, 4, 1);
    private static final GroupAddress PLAIN = new GroupAddress(1, 0, 0);

    private static SecureSenderCheck check(IndividualAddress own) {
        return new SecureSenderCheck(new KeyringFacts(KeyringTestData.load("openhab6.knxkeys")), own);
    }

    @Test
    void linkedAddressHasNothingToSay() {
        SecureSenderCheck check = check(LINKED_BACKBONE);

        assertNull(check.statusDescription());
        assertFalse(check.noteSend(LINKED));
        assertFalse(check.noteSend(PLAIN));
        assertNull(check.statusDescription());
    }

    @Test
    void addressLinkedToNothingIsFoundAtConnect() {
        SecureSenderCheck check = check(UNLINKED_TUNNEL);

        assertEquals("@text/warning.knx-secure-link-address-unlinked [\"1.1.2\"]", check.statusDescription());
    }

    @Test
    void addressMissingInKeyringIsFoundAtConnect() {
        SecureSenderCheck check = check(NOT_IN_KEYRING);

        assertEquals("@text/warning.knx-secure-link-address-unknown [\"1.1.99\"]", check.statusDescription());
    }

    @Test
    void unlinkedGroupIsReportedOncePerAddress() {
        SecureSenderCheck check = check(LINKED_BACKBONE);

        assertTrue(check.noteSend(NOT_LINKED));
        assertFalse(check.noteSend(NOT_LINKED));
        assertEquals("@text/warning.knx-secure-link-groups-unlinked [\"1.0.128\",\"8/4/1\"]",
                check.statusDescription());
    }

    @Test
    void plainGroupIsNeverReported() {
        SecureSenderCheck check = check(UNLINKED_TUNNEL);

        assertFalse(check.noteSend(PLAIN));
        assertEquals("@text/warning.knx-secure-link-address-unlinked [\"1.1.2\"]", check.statusDescription());
    }

    @Test
    void groupsAreListedInAddressOrderAndAddedToTheConnectText() {
        SecureSenderCheck check = check(UNLINKED_TUNNEL);

        assertTrue(check.noteSend(NOT_LINKED));
        assertTrue(check.noteSend(LINKED));
        assertEquals("@text/warning.knx-secure-link-address-unlinked-groups [\"1.1.2\",\"8/0/0; 8/4/1\"]",
                check.statusDescription());
    }

    @Test
    void missingAddressIsExtendedWithTheGroupsToo() {
        SecureSenderCheck check = check(NOT_IN_KEYRING);

        assertTrue(check.noteSend(LINKED));
        assertEquals("@text/warning.knx-secure-link-address-unknown-groups [\"1.1.99\",\"8/0/0\"]",
                check.statusDescription());
    }

    @Test
    void longListIsCut(@TempDir Path dir) throws Exception {
        StringBuilder groups = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            groups.append(KeyringTestData.groupWithKey(9, 0, i)).append('\n');
        }
        KeyringFacts facts = new KeyringFacts(KeyringTestData.synthetic(dir, "", groups.toString()));
        SecureSenderCheck check = new SecureSenderCheck(facts, NOT_IN_KEYRING);

        for (int i = 5; i >= 0; i--) {
            assertTrue(check.noteSend(new GroupAddress(9, 0, i)));
        }

        assertEquals(
                "@text/warning.knx-secure-link-address-unknown-groups [\"1.1.99\",\"9/0/0; 9/0/1; 9/0/2; 9/0/3; ...\"]",
                check.statusDescription());
    }

    /** The arguments the way openHAB core's thing status translation reads them: split at every comma. */
    private static String[] argumentsAsCoreParsesThem(@Nullable String description) {
        assertNotNull(description);
        String[] parts = description.split("\\s+", 2);
        return Arrays.stream(parts[1].replaceAll("\\[|\\]|\"", "").split(",")).filter(a -> !a.trim().isEmpty())
                .map(String::trim).toArray(String[]::new);
    }

    @Test
    void everyListedGroupSurvivesTheCoreParsing() {
        SecureSenderCheck check = check(LINKED_BACKBONE);
        assertTrue(check.noteSend(NOT_LINKED));
        assertEquals(2, argumentsAsCoreParsesThem(check.statusDescription()).length);

        SecureSenderCheck unlinked = check(UNLINKED_TUNNEL);
        assertTrue(unlinked.noteSend(NOT_LINKED));
        assertTrue(unlinked.noteSend(LINKED));

        String[] arguments = argumentsAsCoreParsesThem(unlinked.statusDescription());
        assertEquals(2, arguments.length, Arrays.toString(arguments));
        assertTrue(arguments[1].contains("8/0/0") && arguments[1].contains("8/4/1"), arguments[1]);
    }

    @Test
    void aCutListKeepsItsMarkerThroughTheCoreParsing(@TempDir Path dir) throws Exception {
        StringBuilder groups = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            groups.append(KeyringTestData.groupWithKey(9, 0, i)).append('\n');
        }
        SecureSenderCheck check = new SecureSenderCheck(
                new KeyringFacts(KeyringTestData.synthetic(dir, "", groups.toString())), NOT_IN_KEYRING);
        for (int i = 0; i < 6; i++) {
            assertTrue(check.noteSend(new GroupAddress(9, 0, i)));
        }

        String[] arguments = argumentsAsCoreParsesThem(check.statusDescription());

        assertEquals(2, arguments.length, Arrays.toString(arguments));
        assertTrue(arguments[1].contains("9/0/3") && arguments[1].endsWith("..."), arguments[1]);
        assertFalse(arguments[1].contains("9/0/4"), arguments[1]);
    }

    @Test
    void severalThreadsReportAnAddressOnce() throws Exception {
        SecureSenderCheck check = check(LINKED_BACKBONE);
        int threads = 8;
        AtomicInteger reported = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    for (int i = 0; i < 100; i++) {
                        if (check.noteSend(NOT_LINKED)) {
                            reported.incrementAndGet();
                        }
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

        assertEquals(1, reported.get());
    }
}
