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
package org.openhab.binding.enocean.internal.messages;

import java.security.InvalidParameterException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.eep.Base.UTEResponse;
import org.openhab.binding.enocean.internal.eep.Base._1BSMessage;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class ERP1Message extends BasePacket {

    // these are just ESP3 RORGs, ESP2 ORGs are converted by ESP2PacketConverter
    public enum RORG {
        Unknown((byte) 0x00, 0),
        RPS((byte) 0xF6, 1),
        _1BS((byte) 0xD5, 1),
        _4BS((byte) 0xA5, 4),
        VLD((byte) 0xD2, -1),
        // ADT(0xA6, -1),
        UTE((byte) 0xD4, -1),
        SIG((byte) 0xD0, -1),
        MSC((byte) 0xD1, -1);

        private byte value;
        private int dataLength;

        RORG(byte value, int dataLength) {
            this.value = value;
            this.dataLength = dataLength;
        }

        public byte getValue() {
            return this.value;
        }

        public int getDataLength() {
            return dataLength;
        }

        public static RORG getRORG(byte value) {
            for (RORG t : RORG.values()) {
                if (t.value == value) {
                    return t;
                }
            }

            throw new InvalidParameterException("Unknown choice");
        }
    }

    RORG rorg;

    byte[] senderId;
    boolean teachIn;

    public ERP1Message(int dataLength, int optionalDataLength, byte[] payload) {
        super(dataLength, optionalDataLength, ESPPacketType.RADIO_ERP1, payload);

        teachIn = false;
        senderId = new byte[0];
        try {
            rorg = RORG.getRORG(payload[0]);

            switch (rorg) {
                case RPS:
                    if (dataLength >= 6) {
                        senderId = Arrays.copyOfRange(payload, 2, 6);
                        teachIn = false;
                    }
                    break;
                case _1BS:
                    if (dataLength >= 6) {
                        senderId = Arrays.copyOfRange(payload, 2, 6);
                        teachIn = ((_1BSMessage.TEACHIN_BIT & payload[1]) == 0);
                    }
                    break;
                case _4BS:
                    if (dataLength >= 9) {
                        senderId = Arrays.copyOfRange(payload, 5, 9);
                        teachIn = (_4BSMessage.TEACHIN_BIT & payload[4]) == 0;
                    }
                    break;
                case VLD:
                    teachIn = false;
                    senderId = Arrays.copyOfRange(payload, dataLength - 5, dataLength - 1);
                    break;
                case UTE:
                    if (dataLength >= 6) {
                        teachIn = (payload[1] & UTEResponse.TEACHIN_MASK) == 0
                                || (payload[1] & UTEResponse.TEACHIN_MASK) == UTEResponse.TEACHIN_NPTSPECIFIED;
                        senderId = Arrays.copyOfRange(payload, dataLength - 5, dataLength - 1);
                    }
                    break;
                case SIG:
                case MSC:
                    teachIn = false;
                    senderId = Arrays.copyOfRange(payload, dataLength - 5, dataLength - 1);
                    break;
                default:
                    rorg = RORG.Unknown;
            }

        } catch (Exception e) {
            rorg = RORG.Unknown;
            senderId = new byte[0];
        }
    }

    public RORG getRORG() {
        return rorg;
    }

    public byte[] getSenderId() {
        return senderId;
    }

    public boolean getIsTeachIn() {
        return teachIn;
    }
}
