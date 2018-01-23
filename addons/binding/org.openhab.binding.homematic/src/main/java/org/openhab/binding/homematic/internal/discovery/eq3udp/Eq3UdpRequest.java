/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.discovery.eq3udp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Generates a UDP request to discover Homematic CCU gateways.
 *
 * @author Gerhard Riegler - Initial contribution
 */

public class Eq3UdpRequest {
    private static final byte UDP_IDENTIFY = 73;
    private static final byte UDP_SEPARATOR = 0;

    private static final int senderId = new Random().nextInt() & 0xFFFFFF;
    private static final String EQ3_DEVICE_TYPE = "eQ3-*";
    private static final String EQ3_SERIAL_NUMBER = "*";

    /**
     * Returns the Eq3 Serialnumber.
     */
    public static String getEq3SerialNumber() {
        return EQ3_SERIAL_NUMBER;
    }

    /**
     * Returns the sender id.
     */
    public static int getSenderId() {
        return senderId;
    }

    /**
     * Creates the UDP request.
     */
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(2);
        for (int i = 2; i >= 0; i--) {
            byte temp = (byte) (senderId >> i * 8 & 0xFF);
            baos.write(temp);
        }
        baos.write(UDP_SEPARATOR);
        baos.write(EQ3_DEVICE_TYPE.getBytes());
        baos.write(UDP_SEPARATOR);
        baos.write(EQ3_SERIAL_NUMBER.getBytes());
        baos.write(UDP_SEPARATOR);
        baos.write(UDP_IDENTIFY);
        return baos.toByteArray();
    }
}
