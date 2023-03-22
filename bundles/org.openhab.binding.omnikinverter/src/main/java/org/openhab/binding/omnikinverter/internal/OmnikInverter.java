/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.omnikinverter.internal;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Hans van den Bogert - Initial Contribution
 *
 */
@NonNullByDefault
public class OmnikInverter {

    private final int serialNumber;
    private final String host;
    private final int port;
    private final byte[] magicPacket;

    public OmnikInverter(String host, int port, int serialNumber) {
        this.host = host;
        this.port = port;
        this.serialNumber = serialNumber;
        this.magicPacket = generateMagicPacket();
    }

    public OmnikInverterMessage pullCurrentStats() throws IOException {
        byte[] magicPacket = this.magicPacket;
        byte[] returnMessage = new byte[1024];

        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(5000);
            socket.getOutputStream().write(magicPacket);
            socket.getInputStream().read(returnMessage);

            return new OmnikInverterMessage(returnMessage);
        }
    }

    private byte[] generateMagicPacket() {
        ByteBuffer serialByteBuffer = ByteBuffer.allocate(8).putInt(serialNumber).putInt(serialNumber);
        byte[] serialBytes = serialByteBuffer.array();

        // reverse array
        for (int i = 0; i < serialBytes.length / 2; i++) {
            byte temp = serialBytes[i];
            serialBytes[i] = serialBytes[serialBytes.length - i - 1];
            serialBytes[serialBytes.length - i - 1] = temp;
        }

        byte checksumCount = 115;
        for (byte b : serialBytes) {
            checksumCount += (char) b;
        }

        byte[] result = new byte[16];
        System.arraycopy(new byte[] { 0x68, 0x02, 0x40, 0x30 }, 0, result, 0, 4);
        System.arraycopy(serialBytes, 0, result, 4, 8);
        System.arraycopy(new byte[] { 0x01, 0x00, checksumCount, 0x16 }, 0, result, 12, 4);

        return result;
    }
}
