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
package org.openhab.binding.enocean.internal.messages;

import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.enocean.internal.EnOceanBindingConstants;
import org.openhab.binding.enocean.internal.messages.ESP3Packet.ESPPacketType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class ESP3PacketFactory {

    public final static ESP3Packet CO_RD_VERSION = new CCMessage(1, 0, new byte[] { 3 });
    public final static ESP3Packet CO_RD_IDBASE = new CCMessage(1, 0, new byte[] { 8 });
    public final static ESP3Packet CO_RD_REPEATER = new CCMessage(1, 0, new byte[] { 10 });

    public static ESP3Packet CO_WR_IDBASE(byte[] newId) {
        return new CCMessage(5, 0, new byte[] { 7, newId[0], newId[1], newId[2], newId[3] });
    }

    public static ESP3Packet CO_WR_REPEATER(StringType level) {
        switch (level.toString()) {
            case EnOceanBindingConstants.REPEATERMODE_OFF:
                return new CCMessage(3, 0, new byte[] { 9, 0, 0 });
            case EnOceanBindingConstants.REPEATERMODE_LEVEL_1:
                return new CCMessage(3, 0, new byte[] { 9, 1, 1 });
            default:
                return new CCMessage(3, 0, new byte[] { 9, 1, 2 });
        }
    }

    public static ESP3Packet CO_WR_SUBTEL(boolean enable) {
        if (enable) {
            return new CCMessage(2, 0, new byte[] { 17, 1 });
        } else {
            return new CCMessage(2, 0, new byte[] { 17, 0 });
        }
    }

    public static ESP3Packet BuildPacket(int dataLength, int optionalDataLength, byte packetType, byte[] payload) {
        ESPPacketType type = ESPPacketType.getPacketType(packetType);

        switch (type) {
            case RESPONSE:
                return new Response(dataLength, optionalDataLength, payload);
            case RADIO_ERP1:
                return new ERP1Message(dataLength, optionalDataLength, payload);
            default:
                return null;
        }
    }
}
