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
package org.openhab.binding.paradoxalarm.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.paradoxalarm.internal.communication.crypto.EncryptionHandler;
import org.openhab.binding.paradoxalarm.internal.communication.messages.HeaderCommand;
import org.openhab.binding.paradoxalarm.internal.communication.messages.ParadoxIPPacket;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;

/**
 * The {@link TestEncryptionHandler} This test tests various functions from ParadoxUtils class
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
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

    private static final byte[] ENCRYPTION_KEY_BYTES = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10 };
    private static final byte[] ENCRYPTED_EXPECTED2 = { (byte) 0xAA, 0x0A, 0x00, 0x03, 0x09, (byte) 0xF0, 0x00, 0x00,
            0x01, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE,
            (byte) 0xF9, 0x11, 0x5A, (byte) 0xD7, 0x7C, (byte) 0xCB, (byte) 0xF4, 0x75, (byte) 0xB0, 0x49, (byte) 0xC3,
            0x11, 0x1A, 0x41, (byte) 0x94, (byte) 0xE0 };

    @Test
    public void testCreateAndEncryptStartingPacket() {
        ParadoxIPPacket paradoxIPPacket = new ParadoxIPPacket(ENCRYPTION_KEY_BYTES, false)
                .setCommand(HeaderCommand.CONNECT_TO_IP_MODULE);

        EncryptionHandler.getInstance().updateKey(ENCRYPTION_KEY_BYTES);
        paradoxIPPacket.encrypt();

        final byte[] packetBytes = paradoxIPPacket.getBytes();
        ParadoxUtil.printByteArray("Expected=", ENCRYPTED_EXPECTED2);
        ParadoxUtil.printByteArray("Packet=  ", packetBytes);

        assertTrue(Arrays.equals(packetBytes, ENCRYPTED_EXPECTED2));
    }
}
