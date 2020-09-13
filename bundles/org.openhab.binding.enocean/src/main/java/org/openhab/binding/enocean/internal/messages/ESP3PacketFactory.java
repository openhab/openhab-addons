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
package org.openhab.binding.enocean.internal.messages;

import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.enocean.internal.EnOceanBindingConstants;
import org.openhab.binding.enocean.internal.messages.BasePacket.ESPPacketType;
import org.openhab.binding.enocean.internal.messages.CCMessage.CCMessageType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class ESP3PacketFactory {

    public static final BasePacket CO_RD_VERSION = new CCMessage(CCMessageType.CO_RD_VERSION);
    public static final BasePacket CO_RD_IDBASE = new CCMessage(CCMessageType.CO_RD_IDBASE);
    public static final BasePacket CO_RD_REPEATER = new CCMessage(CCMessageType.CO_RD_REPEATER);

    public static BasePacket CO_WR_IDBASE(byte[] newId) {
        return new CCMessage(CCMessageType.CO_WR_IDBASE, new byte[] { 7, newId[0], newId[1], newId[2], newId[3] });
    }

    public static BasePacket CO_WR_REPEATER(StringType level) {
        switch (level.toString()) {
            case EnOceanBindingConstants.REPEATERMODE_OFF:
                return new CCMessage(CCMessageType.CO_WR_REPEATER, new byte[] { 9, 0, 0 });
            case EnOceanBindingConstants.REPEATERMODE_LEVEL_1:
                return new CCMessage(CCMessageType.CO_WR_REPEATER, new byte[] { 9, 1, 1 });
            default:
                return new CCMessage(CCMessageType.CO_WR_REPEATER, new byte[] { 9, 1, 2 });
        }
    }

    public static BasePacket BuildPacket(int dataLength, int optionalDataLength, byte packetType, byte[] payload) {
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
