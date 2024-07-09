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
package org.openhab.binding.pentair.internal.parser;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PentairIntelliChlorPacket } class extends the PentairPacket class to add specific items for the
 * IntelliChlor packet format since it does not follow the standard packet format.
 *
 * @author Jeff James - initial contribution
 *
 */
@NonNullByDefault
public class PentairIntelliChlorPacket extends PentairBasePacket {
    public static final int DEST = 2;
    public static final int ACTION = 3;

    // Set Generate %
    public static final int SALTOUTPUT = 4;

    // Response to set Generate %
    public static final int SALINITY = 4;
    public static final int STATUS = 5;

    // Response to get version
    public static final int VERSION = 4;
    public static final int NAME = 5;

    public static int getPacketDataLength(int command) {
        int length = -1;

        switch (command) {
            case 0x03: // Response to version
                length = 17;
                break;
            case 0x00: // Get status of Chlorinator
            case 0x11: // Set salt output level (from controller->chlorinator)
            case 0x14:
                length = 1;
                break;
            case 0x01: // Response to Get Status
            case 0x12: // status update with salinity and status
                length = 2;
                break;
        }

        return length;
    }

    public PentairIntelliChlorPacket(byte[] buf, int length) {
        super(buf, length);
    }

    public int getVersion() {
        if (this.getByte(ACTION) != 0x03) {
            return -1;
        }

        return buf[VERSION] & 0xFF;
    }

    public int getDest() {
        return buf[DEST];
    }

    public String getName() {
        if (this.getByte(ACTION) != 0x03) {
            return "";
        }

        String name = new String(buf, NAME, 16, StandardCharsets.UTF_8);

        return name;
    }

    /*
     * Salt Output is available only in packets where the action is 0x11. This is packet sent from the
     * controller to the chlorinator to set the salt output to a specific level.
     */
    public int getSaltOutput() {
        return (this.getByte(ACTION) == 0x11) ? (buf[SALTOUTPUT] & 0xFF) : -1;
    }

    // Salinity and LED status are sent on a packet with action is 0x12. This is sent from the chlorinator.
    public int getSalinity() {
        return (this.getByte(ACTION) == 0x12) ? (buf[SALINITY] & 0xFF) * 50 : -1;
    }

    public boolean getOk() {
        return (this.getByte(ACTION) == 0x12) ? ((buf[STATUS] & 0xFF) == 0) || ((buf[STATUS] & 0xFF) == 0x80) : false;
    }

    public boolean getLowFlow() {
        return (this.getByte(ACTION) == 0x12) ? (buf[STATUS] & 0x01) != 0 : false;
    }

    public boolean getLowSalt() {
        return (this.getByte(ACTION) == 0x12) ? (buf[STATUS] & 0x02) != 0 : false;
    }

    public boolean getVeryLowSalt() {
        return (this.getByte(ACTION) == 0x12) ? (buf[STATUS] & 0x04) != 0 : false;
    }

    public boolean getHighCurrent() {
        return (this.getByte(ACTION) == 0x12) ? (buf[STATUS] & 0x08) != 0 : false;
    }

    public boolean getCleanCell() {
        return (this.getByte(ACTION) == 0x12) ? (buf[STATUS] & 0x10) != 0 : false;
    }

    public boolean getLowVoltage() {
        return (this.getByte(ACTION) == 0x12) ? (buf[STATUS] & 0x20) != 0 : false;
    }

    public boolean getLowWaterTemp() {
        return (this.getByte(ACTION) == 0x12) ? (buf[STATUS] & 0x40) != 0 : false;
    }
}
