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
package org.openhab.binding.knx.internal.monitor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor.NotDecryptedWarning;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Checks the counters, the sender bookkeeping and the warn-once state of secured telegrams that were not decrypted.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class NotDecryptedBookkeepingTest {
    private static final IndividualAddress SENSOR = new IndividualAddress(1, 1, 42);
    private static final IndividualAddress OTHER = new IndividualAddress(1, 1, 43);
    private static final GroupAddress GROUP = new GroupAddress(8, 0, 0);
    private static final GroupAddress OTHER_GROUP = new GroupAddress(8, 0, 1);

    @Test
    void aReasonLibraryTypeMapsToTheReasonsOfTheFixedList() {
        assertEquals(NotDecryptedReason.INVALID_CONTROL_FIELD, NotDecryptedReason.ofLibraryErrorType(1));
        assertEquals(NotDecryptedReason.SEQUENCE_NUMBER, NotDecryptedReason.ofLibraryErrorType(2));
        assertEquals(NotDecryptedReason.AUTHENTICATION, NotDecryptedReason.ofLibraryErrorType(3));
        assertEquals(NotDecryptedReason.ACCESS, NotDecryptedReason.ofLibraryErrorType(4));
        assertNull(NotDecryptedReason.ofLibraryErrorType(0));
        assertNull(NotDecryptedReason.ofLibraryErrorType(5));
        assertNull(NotDecryptedReason.ofLibraryErrorType(-1));
    }

    @Test
    void eachCallIsOneRowAndOneCountForItsReason() {
        TelegramMonitor monitor = new TelegramMonitor();

        monitor.notDecrypted(SENSOR, GROUP, NotDecryptedReason.SEQUENCE_NUMBER, a -> false);
        monitor.notDecrypted(SENSOR, GROUP, NotDecryptedReason.SEQUENCE_NUMBER, a -> false);
        monitor.notDecrypted(SENSOR, OTHER_GROUP, NotDecryptedReason.NO_KEY, a -> false);

        MonitorStatus status = monitor.status();
        assertEquals(2L, status.notDecrypted().getOrDefault(NotDecryptedReason.SEQUENCE_NUMBER, 0L));
        assertEquals(1L, status.notDecrypted().getOrDefault(NotDecryptedReason.NO_KEY, 0L));
        assertNull(status.notDecrypted().get(NotDecryptedReason.ACCESS));
        assertEquals(3, status.notDecryptedTotal());
        assertEquals(3, status.received());
        assertEquals(3, monitor.records().size());
    }

    @Test
    void theRowHasNoPayloadAndNoType() {
        TelegramMonitor monitor = new TelegramMonitor();

        monitor.notDecrypted(SENSOR, GROUP, NotDecryptedReason.AUTHENTICATION, a -> false);

        TelegramRecord row = monitor.records().get(0);
        assertEquals(SecurityState.NOT_DECRYPTED, row.security());
        assertEquals(NotDecryptedReason.AUTHENTICATION, row.reason());
        assertEquals(TelegramType.UNKNOWN, row.type());
        assertEquals(Direction.IN, row.direction());
        assertEquals(0, row.payload().length);
        assertNull(row.value());
        assertEquals(SENSOR, row.source());
        assertEquals(GROUP, row.destination());
    }

    @Test
    void aReasonOutsideTheKnownOnesIsRecordedWithoutReason() {
        TelegramMonitor monitor = new TelegramMonitor();

        monitor.notDecrypted(SENSOR, GROUP, null, a -> false);

        TelegramRecord row = monitor.records().get(0);
        assertEquals(SecurityState.NOT_DECRYPTED, row.security());
        assertNull(row.reason());
        assertEquals(1, monitor.status().notDecryptedOtherReason());
        assertEquals(1, monitor.status().notDecryptedTotal());
    }

    @Test
    void theLastValueOfAnAddressSurvivesANotDecryptedTelegram() {
        TelegramMonitor monitor = new TelegramMonitor();
        monitor.received(SENSOR, GROUP, TelegramType.WRITE, new byte[] { 1 }, SecurityState.SECURED);

        monitor.notDecrypted(SENSOR, GROUP, NotDecryptedReason.AUTHENTICATION, a -> false);

        TelegramRecord last = monitor.lastTelegrams().get(GROUP);
        assertNotNull(last);
        assertEquals(SecurityState.SECURED, last.security());
    }

    @Test
    void sizeZeroKeepsNoRowsButStillCounts() {
        TelegramMonitor monitor = new TelegramMonitor();
        monitor.resize(0);

        monitor.notDecrypted(SENSOR, GROUP, NotDecryptedReason.ACCESS, a -> false);

        assertEquals(List.of(), monitor.records());
        assertEquals(1, monitor.status().notDecryptedTotal());
        assertEquals(List.of(SENSOR), monitor.notDecryptedSenders().get(GROUP));
    }

    @Test
    void aWarningComesOncePerAddressAndReason() {
        TelegramMonitor monitor = new TelegramMonitor();

        NotDecryptedWarning first = monitor.notDecrypted(SENSOR, GROUP, NotDecryptedReason.SEQUENCE_NUMBER, a -> true);
        NotDecryptedWarning again = monitor.notDecrypted(SENSOR, GROUP, NotDecryptedReason.SEQUENCE_NUMBER, a -> true);
        NotDecryptedWarning otherReason = monitor.notDecrypted(SENSOR, GROUP, NotDecryptedReason.AUTHENTICATION,
                a -> true);
        NotDecryptedWarning otherAddress = monitor.notDecrypted(SENSOR, OTHER_GROUP, NotDecryptedReason.SEQUENCE_NUMBER,
                a -> true);

        assertNotNull(first);
        assertEquals(GROUP, first.address());
        assertEquals(NotDecryptedReason.SEQUENCE_NUMBER, first.reason());
        assertEquals(List.of(SENSOR), first.senders());
        assertNull(again);
        assertNotNull(otherReason);
        assertNotNull(otherAddress);
    }

    @Test
    void aWarningNeedsAChannelOnTheAddressAndWaitsForOne() {
        TelegramMonitor monitor = new TelegramMonitor();

        assertNull(monitor.notDecrypted(SENSOR, GROUP, NotDecryptedReason.NO_KEY, a -> false));
        NotDecryptedWarning later = monitor.notDecrypted(OTHER, GROUP, NotDecryptedReason.NO_KEY, a -> true);

        assertNotNull(later);
        assertEquals(List.of(SENSOR, OTHER), later.senders());
        assertEquals(2, monitor.status().notDecryptedTotal());
    }

    @Test
    void theChannelCheckIsNotAskedOnceTheWarningWasGiven() {
        TelegramMonitor monitor = new TelegramMonitor();
        AtomicInteger asked = new AtomicInteger();
        monitor.notDecrypted(SENSOR, GROUP, NotDecryptedReason.NO_KEY, a -> {
            asked.incrementAndGet();
            return true;
        });

        monitor.notDecrypted(SENSOR, GROUP, NotDecryptedReason.NO_KEY, a -> {
            asked.incrementAndGet();
            return true;
        });

        assertEquals(1, asked.get());
    }

    @Test
    void aFailingChannelCheckKeepsTheRowAndTheCount() {
        TelegramMonitor monitor = new TelegramMonitor();

        assertThrows(IllegalStateException.class,
                () -> monitor.notDecrypted(SENSOR, GROUP, NotDecryptedReason.NO_KEY, a -> {
                    throw new IllegalStateException("made-up failure in a channel");
                }));

        assertEquals(1, monitor.records().size());
        assertEquals(1, monitor.status().notDecryptedTotal());
        assertNotNull(monitor.notDecrypted(SENSOR, GROUP, NotDecryptedReason.NO_KEY, a -> true));
    }

    @Test
    void resetStartsCountersSendersAndWarningsAfreshButKeepsTheRows() {
        TelegramMonitor monitor = new TelegramMonitor();
        monitor.notDecrypted(SENSOR, GROUP, NotDecryptedReason.NO_KEY, a -> true);

        monitor.resetNotDecrypted();

        assertEquals(0, monitor.status().notDecryptedTotal());
        assertEquals(0, monitor.status().notDecryptedOtherReason());
        assertEquals(1, monitor.records().size());
        assertTrue(monitor.notDecryptedSenders().isEmpty());
        assertNotNull(monitor.notDecrypted(SENSOR, GROUP, NotDecryptedReason.NO_KEY, a -> true));
    }

    @Test
    void theSenderMapHoldsAtMostSixteenSendersPerAddressAndCountsTheRest() {
        TelegramMonitor monitor = new TelegramMonitor();
        int senders = TelegramMonitor.MAX_FAILED_SENDERS + 5;

        for (int i = 0; i < senders; i++) {
            monitor.notDecrypted(new IndividualAddress(1, 1, i + 1), GROUP, NotDecryptedReason.SEQUENCE_NUMBER,
                    a -> false);
        }

        assertEquals(TelegramMonitor.MAX_FAILED_SENDERS,
                monitor.notDecryptedSenders().getOrDefault(GROUP, List.of()).size());
        assertEquals(senders, monitor.status().notDecryptedTotal());
    }

    @Test
    void theSenderMapHoldsAtMostTwoHundredFiftySixAddressesAndCountsTheRest() {
        TelegramMonitor monitor = new TelegramMonitor();
        int addresses = TelegramMonitor.MAX_FAILED_ADDRESSES + 10;
        List<NotDecryptedWarning> warnings = new ArrayList<>();

        for (int i = 0; i < addresses; i++) {
            NotDecryptedWarning warning = monitor.notDecrypted(SENSOR, new GroupAddress(0x0800 + i),
                    NotDecryptedReason.NO_KEY, a -> true);
            if (warning != null) {
                warnings.add(warning);
            }
        }

        assertEquals(TelegramMonitor.MAX_FAILED_ADDRESSES, monitor.notDecryptedSenders().size());
        assertEquals(addresses, monitor.status().notDecryptedTotal());
        assertEquals(addresses, warnings.size(), "an address past the bound is still warned about");
        assertEquals(List.of(SENSOR), warnings.get(addresses - 1).senders());
    }
}
