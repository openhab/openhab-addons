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

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.ZERO;

import java.security.InvalidParameterException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.EnOceanException;
import org.openhab.binding.enocean.internal.messages.BasePacket.ESPPacketType;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class ESP2Packet {

    public static final byte ENOCEAN_ESP2_FIRSTSYNC_BYTE = (byte) 0xA5;
    public static final byte ENOCEAN_ESP2_SECONDSYNC_BYTE = 0x5A;

    private static final int ESP2_SYNC_BYTE_LENGTH = 2;
    private static final int ESP2_HEADER_LENGTH = 1;
    private static final int ESP2_DATA_LENGTH = 4;
    private static final int ESP2_SENDERID_LENGTH = 4;
    private static final int ESP2_STATUS_LENGTH = 1;
    private static final int ESP2_CHECKSUM_LENGTH = 1;

    public static final int ESP2_ORG_LENGTH = 1;

    public static final int ESP_PACKET_LENGTH = ESP2_ORG_LENGTH + ESP2_DATA_LENGTH + ESP2_SENDERID_LENGTH
            + ESP2_STATUS_LENGTH + ESP2_CHECKSUM_LENGTH;

    public enum ORG {
        UNKOWN((byte) 0x00),
        _RPS((byte) 0x05),
        _1BS((byte) 0x06),
        _4BS((byte) 0x07);

        private byte value;

        private ORG(byte value) {
            this.value = value;
        }

        public static ORG getORG(byte org) {
            for (ORG o : ORG.values()) {
                if (o.value == org) {
                    return o;
                }
            }

            return UNKOWN;
        }
    }

    public enum ESP2Response {

        UNKOWN((byte) 0),
        OK((byte) 0x58),
        ERR((byte) 0x19),
        INF_IDBase((byte) 0x98),
        INF_RX_SENSITIVITY((byte) 0x88),
        INF_MODEM_STATUS((byte) 0xA8),
        INF_SW_VERSION((byte) 0x8C),
        ERR_MODEM_NOTWANTEDACK((byte) 0x28),
        ERR_MODEM_NOTACK((byte) 0x29),
        ERR_MODEM_DUP_ID((byte) 0x0C),
        ERR_SYNTAX_HSEQ((byte) 0x08),
        ERR_SYNTAX_LENGTH((byte) 0x09),
        ERR_SYNTAX_CHECKSUM((byte) 0x0A),
        ERR_SYNTAX_ORG((byte) 0x0B),
        ERR_TX_IDRANGE((byte) 0x22),
        ERR_IDRANGE((byte) 0x1A);

        private byte value;

        private ESP2Response(byte value) {
            this.value = value;
        }

        public static ESP2Response getResponse(byte response) {
            for (ESP2Response p : ESP2Response.values()) {
                if (p.value == response) {
                    return p;
                }
            }

            return UNKOWN;
        }
    }

    public enum ESP2PacketType {
        Receive_Radio_Telegram((byte) 0x00),
        Transmit_Radio_Telegram((byte) 0x03),
        Receive_Message_Telegram((byte) 0x04),
        Transmit_Command_Telegram((byte) 0x05);

        private byte value;

        private ESP2PacketType(byte value) {
            this.value = value;
        }

        public static ESP2PacketType getPacketType(byte packetType) {
            for (ESP2PacketType p : ESP2PacketType.values()) {
                if (p.value == packetType) {
                    return p;
                }
            }

            throw new InvalidParameterException("Unknown ESP2 PacketType value");
        }
    }

    protected BasePacket basePacket;

    public ESP2Packet(BasePacket basePacket) {
        this.basePacket = basePacket;
    }

    private ESP2PacketType convertToESP2PacketType(ESPPacketType espPacketType) {
        switch (espPacketType) {
            case COMMON_COMMAND:
                return ESP2PacketType.Transmit_Command_Telegram;
            case RADIO_ERP1:
            case RADIO_ERP2:
                return ESP2PacketType.Transmit_Radio_Telegram;
            case RESPONSE: // Response is not intended for outbound data (at least for ESP2)
            default:
                throw new IllegalArgumentException("ESPPacketType not supported");
        }
    }

    private byte convertToESP2ORG(RORG esp3RORG) {
        switch (esp3RORG) {
            case RPS:
                return ORG._RPS.value;
            case _1BS:
                return ORG._1BS.value;
            case _4BS:
                return ORG._4BS.value;
            default:
                throw new InvalidParameterException("RORG is not supported by ESP2");
        }
    }

    private byte[] getOrgAndDataBytes(BasePacket basePacket) {
        byte[] data = new byte[ESP2_ORG_LENGTH + ESP2_DATA_LENGTH];
        Arrays.fill(data, ZERO);

        if (basePacket.getPacketType() == ESPPacketType.RADIO_ERP1) {
            ERP1Message message = (ERP1Message) basePacket;
            data[0] = convertToESP2ORG(message.getRORG());
            System.arraycopy(message.getPayload(ESP2_ORG_LENGTH, message.getRORG().getDataLength()), 0, data,
                    ESP2_ORG_LENGTH, message.getRORG().getDataLength());
        } else if (basePacket.getPacketType() == ESPPacketType.COMMON_COMMAND) {
            CCMessage message = (CCMessage) basePacket;
            switch (message.getCCMessageType()) {
                case CO_RD_IDBASE:
                    data[0] = 0x58;
                    break;
                case CO_RD_VERSION:
                    data[0] = 0x4B;
                    break;
                case CO_WR_IDBASE:
                    data[0] = 0x18;
                    System.arraycopy(message.getPayload(ESP2_ORG_LENGTH, 4), 0, data, ESP2_ORG_LENGTH, 4);
                    break;
                default:
                    throw new InvalidParameterException("CCMessage is not supported by ESP2");
            }
        }

        return data;
    }

    private byte[] getSenderId(BasePacket basePacket) {
        byte[] data = new byte[4];
        Arrays.fill(data, ZERO);

        if (basePacket.getPacketType() == ESPPacketType.RADIO_ERP1) {
            ERP1Message message = (ERP1Message) basePacket;
            data = message.getSenderId();
        }

        return data;
    }

    private byte getStatus(BasePacket basePacket) {
        if (basePacket.getPacketType() == ESPPacketType.RADIO_ERP1) {
            ERP1Message message = (ERP1Message) basePacket;
            return message.getPayload(ESP2_ORG_LENGTH + message.getRORG().getDataLength() + ESP2_SENDERID_LENGTH,
                    ESP2_STATUS_LENGTH)[0];
        }

        return ZERO;
    }

    private byte calcCheckSum(byte[] data, int offset, int length) {
        int checkSum = 0;
        for (int i = 0; i < length; i++) {
            checkSum += (data[offset + i] & 0xff);
        }

        return (byte) (checkSum & 0xff);
    }

    public byte[] serialize() throws EnOceanException {
        try {
            byte[] result = new byte[ESP2_SYNC_BYTE_LENGTH + ESP2_HEADER_LENGTH + ESP2_ORG_LENGTH + ESP2_DATA_LENGTH
                    + ESP2_SENDERID_LENGTH + ESP2_STATUS_LENGTH + ESP2_CHECKSUM_LENGTH];
            Arrays.fill(result, ZERO);

            result[0] = ENOCEAN_ESP2_FIRSTSYNC_BYTE;
            result[1] = ENOCEAN_ESP2_SECONDSYNC_BYTE;
            result[2] = (byte) (((convertToESP2PacketType(basePacket.getPacketType()).value << 5) + (ESP_PACKET_LENGTH))
                    & 0xff);

            System.arraycopy(getOrgAndDataBytes(basePacket), 0, result, ESP2_SYNC_BYTE_LENGTH + ESP2_HEADER_LENGTH,
                    ESP2_ORG_LENGTH + ESP2_DATA_LENGTH);

            System.arraycopy(getSenderId(basePacket), 0, result,
                    ESP2_SYNC_BYTE_LENGTH + ESP2_HEADER_LENGTH + ESP2_ORG_LENGTH + ESP2_DATA_LENGTH,
                    ESP2_SENDERID_LENGTH);

            result[ESP2_SYNC_BYTE_LENGTH + ESP2_HEADER_LENGTH + ESP2_ORG_LENGTH + ESP2_DATA_LENGTH
                    + ESP2_SENDERID_LENGTH] = getStatus(basePacket);

            result[ESP2_SYNC_BYTE_LENGTH + ESP2_HEADER_LENGTH + ESP2_ORG_LENGTH + ESP2_DATA_LENGTH
                    + ESP2_SENDERID_LENGTH + ESP2_STATUS_LENGTH] = calcCheckSum(result, ESP2_SYNC_BYTE_LENGTH,
                            ESP2_HEADER_LENGTH + ESP2_ORG_LENGTH + ESP2_DATA_LENGTH + ESP2_SENDERID_LENGTH
                                    + ESP2_STATUS_LENGTH);

            return result;
        } catch (Exception e) {
            throw new EnOceanException(e.getMessage());
        }
    }

    public static boolean validateCheckSum(byte[] data, int length, byte checkSum) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += (data[i] & 0xff);
        }

        return (sum & 0xff) == (checkSum & 0xff);
    }
}
