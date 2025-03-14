/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaDeviceResponse;

/**
 * Class to implement the BlueGiga command <b>readMultipleResponseEvent</b>.
 * <p>
 * This event is a response to a Read Multiple request.
 * <p>
 * This class provides methods for processing BlueGiga API commands.
 * <p>
 * Note that this code is autogenerated. Manual changes may be overwritten.
 *
 * @author Chris Jackson - Initial contribution of Java code generator
 */
@NonNullByDefault
public class BlueGigaReadMultipleResponseEvent extends BlueGigaDeviceResponse {
    public static final int COMMAND_CLASS = 0x04;
    public static final int COMMAND_METHOD = 0x00;

    /**
     * This array contains the concatenated data from the multiple attributes that have been read,
     * up to 22 bytes.
     * <p>
     * BlueGiga API type is <i>uint8array</i> - Java type is {@link int[]}
     */
    private int[] handles;

    /**
     * Event constructor
     */
    public BlueGigaReadMultipleResponseEvent(int[] inputBuffer) {
        // Super creates deserializer and reads header fields
        super(inputBuffer);

        event = (inputBuffer[0] & 0x80) != 0;

        // Deserialize the fields
        connection = deserializeUInt8();
        handles = deserializeUInt8Array();
    }

    /**
     * This array contains the concatenated data from the multiple attributes that have been read,
     * up to 22 bytes.
     * <p>
     * BlueGiga API type is <i>uint8array</i> - Java type is {@link int[]}
     *
     * @return the current handles as {@link int[]}
     */
    public int[] getHandles() {
        return handles;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BlueGigaReadMultipleResponseEvent [connection=");
        builder.append(connection);
        builder.append(", handles=");
        for (int c = 0; c < handles.length; c++) {
            if (c > 0) {
                builder.append(' ');
            }
            builder.append(String.format("%02X", handles[c]));
        }
        builder.append(']');
        return builder.toString();
    }
}
