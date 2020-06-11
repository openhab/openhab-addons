/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Hans van den Bogert - Initial Contribution
 *
 */
@NonNullByDefault
public class OmnikInverter {

    private int serialNumber;
    private String host;
    private int port;
    private byte[] magicPacket;

    public OmnikInverter(String host, int port, int serialNumber) throws IOException {
        this.host = host;
        this.port = port;
        this.serialNumber = serialNumber;
        this.magicPacket = generateMagicPacket();
    }

    public OmnikInverterMessage pullCurrentStats() throws UnknownHostException, IOException {
        byte[] magicPacket = this.magicPacket;
        byte[] returnMessage = new byte[1024];

        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(5000);
            socket.getOutputStream().write(magicPacket);
            socket.getInputStream().read(returnMessage);

            return new OmnikInverterMessage(returnMessage);
        }
    }

    private byte[] generateMagicPacket() throws IOException {
        byte[] magic = { 0x68, 0x02, 0x40, 0x30 };

        ByteBuffer serialByteBuffer = ByteBuffer.allocate(8).putInt(serialNumber).putInt(serialNumber);
        byte[] serialBytes = serialByteBuffer.array();
        // Reverse serialBytes in a very mutable way.
        ArrayUtils.reverse(serialBytes);

        byte checksumCount = 115;
        for (byte b : serialBytes) {
            checksumCount += (char) b;
        }

        byte[] checksum = ByteBuffer.allocate(1).put(checksumCount).array();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(magic);
            outputStream.write(serialBytes);
            outputStream.write(0x01);
            outputStream.write(0x00);
            outputStream.write(checksum);
            outputStream.write(0x16);

            return outputStream.toByteArray();
        }
    }
}
