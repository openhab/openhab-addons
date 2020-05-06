/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package tests;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.paradoxalarm.internal.communication.crypto.EncryptionHandler;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;

/**
 * The {@link TestEncryptionHandler} This test tests various functions from ParadoxUtils class
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class TestEncryptionHandler {

    private static final String INPUT_STRING = "My test string for encryption.";
    private static final String KEY = "MyKeyToEncrypt";

    @Test
    public void testEncryptDecryptString() {
        EncryptionHandler.getInstance().updateKey(ParadoxUtil.getBytesFromString(KEY));

        byte[] originalBytes = ParadoxUtil.getBytesFromString(INPUT_STRING);
        ParadoxUtil.printByteArray("Original=", originalBytes);

        byte[] encrypted = EncryptionHandler.getInstance().encrypt(originalBytes);
        assertNotEquals(originalBytes, encrypted);

        byte[] decrypted = EncryptionHandler.getInstance().decrypt(encrypted);
        byte[] result = decrypted.length != originalBytes.length ? Arrays.copyOf(decrypted, originalBytes.length)
                : decrypted;
        ParadoxUtil.printByteArray("Result=", result);
        assertEquals(originalBytes.length, result.length);

        assertEquals(INPUT_STRING, new String(result));
    }
}
