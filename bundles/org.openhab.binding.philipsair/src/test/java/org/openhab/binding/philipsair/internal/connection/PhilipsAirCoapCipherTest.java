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

import java.util.Locale;

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

    // Known answers, produced independently of the binding with Python (hashlib and cryptography):
    // key and iv are the two halves of the upper case hex MD5 of "JiangPan" + counter (used as ASCII text), the
    // message is encrypted with AES-128-CBC and PKCS7 padding, and the message is counter + upper case hex of the
    // cipher text + upper case hex SHA-256 of counter + cipher text.
    private static final String REPORTED_PLAIN = "{\"state\":{\"reported\":{\"pwr\":\"1\",\"om\":\"s\",\"pm25\":8}}}";
    private static final String REPORTED_ENCRYPTED = "000000FFE4EB7B77734E91061CEDB8DEA5110C9BBCE5E325C7B73597EE3B68CC053E66802D1DCEB81ADE7BAB89416C0B1FE2D12DAB4F854E4CD6654DC43A8B2D3450779455CC1461DA95A7D97B862B23DC1A636857B4FCB7CF3B2B15A2BB134D42D1198C";
    private static final String COMMAND_PLAIN = "{\"state\":{\"desired\":{\"pwr\":\"0\"}}}";
    private static final String COMMAND_ENCRYPTED = "00ABCDEFCAE74E0E019C4A7A71CDEC3ECC9988239F20BD77BABCC970BD22EAF57208F80360788EF41B6425A6568460C3C53BB78AAA304AFFDE7D3552977B8C719BF6C45D52310B7DBD207BEEAF5C3E35A9624E44";

    @Test
    public void encryptedMessageIsDecrypted() {
        String message = "{\"state\":{\"reported\":{\"pwr\":\"1\"}}}";

        String encrypted = PhilipsAirCoapCipher.encryptedMsg(message, 0x1234ABCDL, logger);

        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("1234ABCD"));
        assertEquals(message, PhilipsAirCoapCipher.decryptMsg(encrypted, logger));
    }

    @Test
    public void knownMessageIsDecrypted() {
        assertEquals(REPORTED_PLAIN, PhilipsAirCoapCipher.decryptMsg(REPORTED_ENCRYPTED, logger));
        assertEquals(COMMAND_PLAIN, PhilipsAirCoapCipher.decryptMsg(COMMAND_ENCRYPTED, logger));
    }

    @Test
    public void knownMessageIsEncrypted() {
        assertEquals(REPORTED_ENCRYPTED, PhilipsAirCoapCipher.encryptedMsg(REPORTED_PLAIN, 0xFFL, logger));
        assertEquals(COMMAND_ENCRYPTED, PhilipsAirCoapCipher.encryptedMsg(COMMAND_PLAIN, 0xABCDEFL, logger));
    }

    @Test
    public void shortMessageIsRejected() {
        assertEquals("", PhilipsAirCoapCipher.decryptMsg("", logger));
        assertEquals("", PhilipsAirCoapCipher.decryptMsg("{\"status\":\"success\"}", logger));
        assertEquals("", PhilipsAirCoapCipher.decryptMsg("1234ABCD" + "0".repeat(63), logger));
    }

    @Test
    public void invalidMessageIsNotDecrypted() {
        assertEquals("", PhilipsAirCoapCipher.decryptMsg("1234ABCD" + "XY" + "0".repeat(64), logger));
    }

    @Test
    public void corruptedMessageIsRejected() {
        String encrypted = PhilipsAirCoapCipher.encryptedMsg("{\"state\":{\"reported\":{\"pwr\":\"1\"}}}", 0x1234ABCDL,
                logger);
        assertNotNull(encrypted);
        char last = encrypted.charAt(encrypted.length() - 1);
        String corrupted = encrypted.substring(0, encrypted.length() - 1) + (last == '0' ? '1' : '0');

        assertEquals("", PhilipsAirCoapCipher.decryptMsg(corrupted, logger));
        String lowerCaseHash = encrypted.substring(0, encrypted.length() - 64)
                + encrypted.substring(encrypted.length() - 64).toLowerCase(Locale.ROOT);
        assertNotEquals("", PhilipsAirCoapCipher.decryptMsg(lowerCaseHash, logger));
    }
}
