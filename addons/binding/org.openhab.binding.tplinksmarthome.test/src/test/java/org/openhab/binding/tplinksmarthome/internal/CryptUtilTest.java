/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

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
        assertEquals("Crypting should result in same string", TEST_STRING,
                CryptUtil.decrypt(CryptUtil.encrypt(TEST_STRING), TEST_STRING.length()));
    }

    /**
     * Test round trip of encrypt and decrypt with length that should return the same value.
     *
     * @throws IOException exception in case device not reachable
     */
    @Test
    public void testCryptWithLength() throws IOException {
        try (final ByteArrayInputStream is = new ByteArrayInputStream(CryptUtil.encryptWithLength(TEST_STRING))) {
            assertEquals("Crypting should result in same string", TEST_STRING, CryptUtil.decryptWithLength(is));
        }
    }

}
