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
package org.openhab.binding.philipsair.internal.connection;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the CoAP message encryption of {@link PhilipsAirCoapCipher}.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class PhilipsAirCoapCipherTest {

    private final Logger logger = LoggerFactory.getLogger(PhilipsAirCoapCipherTest.class);

    @Test
    public void encryptedMessageIsDecrypted() {
        String message = "{\"state\":{\"reported\":{\"pwr\":\"1\"}}}";

        String encrypted = PhilipsAirCoapCipher.encryptedMsg(message, 0x1234ABCDL, logger);

        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("1234ABCD"));
        assertEquals(message, PhilipsAirCoapCipher.decryptMsg(encrypted, logger));
    }

    @Test
    public void shortMessageIsRejected() {
        assertEquals("", PhilipsAirCoapCipher.decryptMsg("", logger));
        assertEquals("", PhilipsAirCoapCipher.decryptMsg("{\"status\":\"success\"}", logger));
        assertEquals("", PhilipsAirCoapCipher.decryptMsg("1234ABCD" + "0".repeat(63), logger));
    }

    @Test
    public void invalidMessageIsNotDecrypted() {
        assertEquals("Could not decrypt", PhilipsAirCoapCipher.decryptMsg("1234ABCD" + "XY" + "0".repeat(64), logger));
    }
}
