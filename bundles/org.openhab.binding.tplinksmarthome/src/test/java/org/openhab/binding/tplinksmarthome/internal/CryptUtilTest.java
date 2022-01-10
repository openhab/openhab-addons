/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.tplinksmarthome.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link CryptUtil} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class CryptUtilTest {

    private static final String TEST_STRING = "This is just a message";

    /**
     * Test round trip of encrypt and decrypt that should return the same value.
     *
     * @throws IOException exception in case device not reachable
     */
    @Test
    public void testCrypt() throws IOException {
        assertEquals(TEST_STRING, CryptUtil.decrypt(CryptUtil.encrypt(TEST_STRING), TEST_STRING.length()),
                "Crypting should result in same string");
    }

    /**
     * Test round trip of encrypt and decrypt with length that should return the same value.
     *
     * @throws IOException exception in case device not reachable
     */
    @Test
    public void testCryptWithLength() throws IOException {
        try (final ByteArrayInputStream is = new ByteArrayInputStream(CryptUtil.encryptWithLength(TEST_STRING))) {
            assertEquals(TEST_STRING, CryptUtil.decryptWithLength(is), "Crypting should result in same string");
        }
    }
}
