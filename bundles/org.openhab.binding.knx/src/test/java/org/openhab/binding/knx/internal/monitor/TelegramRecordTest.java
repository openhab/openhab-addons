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

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Checks that a handed-out telegram cannot be changed by whoever holds it.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class TelegramRecordTest {

    private static TelegramRecord record(byte[] payload) {
        return new TelegramRecord(7, Instant.parse("2026-01-02T03:04:05.678Z"), new IndividualAddress(1, 1, 5),
                new GroupAddress(2, 3, 4), TelegramType.WRITE, Direction.IN, payload, SecurityState.SECURED, null, null,
                "9.001", "21.5", "°C", null, null, false);
    }

    @Test
    void thePayloadIsCopiedOnTheWayInAndOut() {
        byte[] payload = { 0x0c, 0x1a };
        TelegramRecord record = record(payload);

        payload[0] = 0;
        record.payload()[1] = 0;

        assertArrayEquals(new byte[] { 0x0c, 0x1a }, record.payload());
    }

    @Test
    void theSixReasonsKeepTheirNames() {
        assertEquals(List.of("NO_KEY", "SENDER_NOT_LISTED", "INVALID_CONTROL_FIELD", "SEQUENCE_NUMBER",
                "AUTHENTICATION", "ACCESS"), Arrays.stream(NotDecryptedReason.values()).map(Enum::name).toList());
    }

    @Test
    void aNotDecryptedTelegramCarriesItsReason() {
        TelegramRecord record = new TelegramRecord(8, Instant.EPOCH, new IndividualAddress(1, 1, 6),
                new GroupAddress(2, 3, 5), TelegramType.UNKNOWN, Direction.IN, new byte[0], SecurityState.NOT_DECRYPTED,
                NotDecryptedReason.SEQUENCE_NUMBER, null, null, null, null, null, null, false);

        assertEquals(NotDecryptedReason.SEQUENCE_NUMBER, record.reason());
        assertEquals(0, record.payload().length);
    }
}
