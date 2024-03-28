/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;

/**
 * Tests the Broadlink protocol.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkProtocolTest { // NOPMD

    private final Logger mockLogger = Mockito.mock(Logger.class);

    byte[] mac = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, };

    byte[] deviceId = { 0x10, 0x11, 0x12, 0x13 };

    byte[] iv = HexUtils.hexToBytes("562e17996d093d28ddb3ba695a2e6f58");

    byte[] deviceKey = HexUtils.hexToBytes("097628343fe99e23765c1513accf8b02");

    @Test
    public void canBuildMessageWithCorrectChecksums() {
        byte[] payload = {};
        byte[] result = BroadlinkProtocol.buildMessage((byte) 0x0, payload, 0, mac, deviceId, iv, deviceKey, 1234,
                mockLogger);

        assertEquals(56, result.length);

        // bytes 0x34 and 0x35 contain the payload checksum,
        // which given we have an empty payload, should be the initial
        // 0xBEAF
        int payloadChecksum = ((result[0x35] & 0xff) << 8) + (result[0x34] & 0xff);
        assertEquals(0xbeaf, payloadChecksum);

        // bytes 0x20 and 0x21 contain the overall checksum
        int overallChecksum = ((result[0x21] & 0xff) << 8) + (result[0x20] & 0xff);
        assertEquals(0xc549, overallChecksum);
    }
}
