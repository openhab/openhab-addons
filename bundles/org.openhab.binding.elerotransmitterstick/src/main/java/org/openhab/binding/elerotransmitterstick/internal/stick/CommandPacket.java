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
package org.openhab.binding.elerotransmitterstick.internal.stick;

import org.openhab.core.util.HexUtils;

/**
 * @author Volker Bier - Initial contribution
 */
public class CommandPacket {
    public static final byte EASY_CHECK = (byte) 0x4A;
    public static final byte EASY_SEND = (byte) 0x4C;
    public static final byte EASY_INFO = (byte) 0x4E;

    byte[] data;

    public CommandPacket(byte[] bytes) {
        data = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, data, 0, bytes.length);

        data[bytes.length] = checksum(bytes);
    }

    public byte[] getBytes() {
        return data;
    }

    public long getResponseTimeout() {
        if (isEasyCheck()) {
            return 1000;
        }

        return 4000;
    }

    private byte checksum(byte[] data) {
        long val = 0;

        for (byte b : data) {
            val += b;
        }

        val = val % 256;
        return (byte) (256 - val);
    }

    public boolean isEasyCheck() {
        return data[2] == EASY_CHECK;
    }

    @Override
    public String toString() {
        return HexUtils.bytesToHex(data);
    }
}
