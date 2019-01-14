/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elerotransmitterstick.internal.stick;

import org.eclipse.smarthome.core.util.HexUtils;

/**
 * @author Volker Bier - Initial contribution
 */
public class CommandPacket {
    public final static byte EASY_CHECK = (byte) 0x4A;
    public final static byte EASY_SEND = (byte) 0x4C;
    public final static byte EASY_INFO = (byte) 0x4E;

    byte[] data;

    public CommandPacket(byte[] bytes) {
        data = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, data, 0, bytes.length);

        data[bytes.length] = checksum(data);
    }

    public byte[] getBytes() {
        return data;
    }

    public long getResponseTimeout() {
        if (data[2] == EASY_CHECK) {
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

    @Override
    public String toString() {
        return HexUtils.bytesToHex(data);
    }

}
