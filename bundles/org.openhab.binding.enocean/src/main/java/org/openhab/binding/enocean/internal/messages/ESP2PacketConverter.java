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

import java.nio.charset.Charset;
import java.util.Arrays;

import org.openhab.binding.enocean.internal.messages.BasePacket.ESPPacketType;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;
import org.openhab.binding.enocean.internal.messages.ESP2Packet.ESP2PacketType;
import org.openhab.binding.enocean.internal.messages.Response.ResponseType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class ESP2PacketConverter {

    protected static Logger logger = LoggerFactory.getLogger(ESP2PacketConverter.class);

    private static final int ESP3PACKET_BASE_LENGTH = ESP3Packet.ESP3_RORG_LENGTH + ESP3Packet.ESP3_SENDERID_LENGTH
            + ESP3Packet.ESP3_STATUS_LENGTH;

    private static BasePacket handleRadioTelegram(int dataLength, byte packetType, byte[] payload) {
        switch (ESP2Packet.ORG.getORG(payload[1])) {
            case _RPS:
                return ESP3PacketFactory.BuildPacket(ESP3PACKET_BASE_LENGTH + RORG.RPS.getDataLength(), 0,
                        ESPPacketType.RADIO_ERP1.getValue(), new byte[] { RORG.RPS.getValue(), payload[2], payload[6],
                                payload[7], payload[8], payload[9], payload[10] });
            case _1BS:
                return ESP3PacketFactory.BuildPacket(ESP3PACKET_BASE_LENGTH + RORG._1BS.getDataLength(), 0,
                        ESPPacketType.RADIO_ERP1.getValue(), new byte[] { RORG._1BS.getValue(), payload[2], payload[6],
                                payload[7], payload[8], payload[9], payload[10] });

            case _4BS:
                return ESP3PacketFactory.BuildPacket(ESP3PACKET_BASE_LENGTH + RORG._4BS.getDataLength(), 0,
                        ESPPacketType.RADIO_ERP1.getValue(), new byte[] { RORG._4BS.getValue(), payload[2], payload[3],
                                payload[4], payload[5], payload[6], payload[7], payload[8], payload[9], payload[10] });
            default:
                logger.debug("Received unsupported ORG: {}", payload[1]);
                return null;
        }
    }

    private static BasePacket handleMessageTelegram(int dataLength, byte packetType, byte[] payload) {
        switch (ESP2Packet.ESP2Response.getResponse(payload[1])) {
            case OK:
                return ESP3PacketFactory.BuildPacket(1, 0, ESPPacketType.RESPONSE.getValue(),
                        new byte[] { ResponseType.RET_OK.getValue() });
            case ERR:
                return ESP3PacketFactory.BuildPacket(1, 0, ESPPacketType.RESPONSE.getValue(),
                        new byte[] { ResponseType.RET_ERROR.getValue() });
            case INF_SW_VERSION: {
                byte[] data = new byte[33];
                Arrays.fill(data, (byte) 0);
                data[0] = ResponseType.RET_OK.getValue();
                System.arraycopy(payload, 1, data, 1, 4);

                byte[] description = "TCM 210".getBytes(Charset.forName("ASCII"));
                System.arraycopy(description, 0, data, 17, description.length);
                return ESP3PacketFactory.BuildPacket(data.length, 0, ESPPacketType.RESPONSE.getValue(), data);
            }
            case UNKOWN: // try to interpret it as a radio telegram
                return handleRadioTelegram(dataLength, packetType, payload);

            case ERR_IDRANGE:
            case ERR_MODEM_DUP_ID:
            case ERR_MODEM_NOTACK:
            case ERR_MODEM_NOTWANTEDACK:
            case ERR_SYNTAX_CHECKSUM:
            case ERR_SYNTAX_HSEQ:
            case ERR_SYNTAX_LENGTH:
            case ERR_SYNTAX_ORG:
            case ERR_TX_IDRANGE:
            case INF_IDBase:
            case INF_MODEM_STATUS:
            case INF_RX_SENSITIVITY:
            default:
                logger.debug("Received unsupported message telegram: {}",
                        ESP2Packet.ESP2Response.getResponse(payload[1]).name());
                return null;
        }
    }

    public static BasePacket BuildPacket(int dataLength, byte packetType, byte[] payload) {
        ESP2PacketType type = ESP2PacketType.getPacketType(packetType);

        switch (type) {
            case Receive_Radio_Telegram: // RRT
                logger.debug("Received ESP2 radio telegram: {}", HexUtils.bytesToHex(payload));
                return handleRadioTelegram(dataLength, packetType, payload);

            case Receive_Message_Telegram: // RMT => Response
                logger.debug("Received ESP2 message telegram: {}", HexUtils.bytesToHex(payload));
                return handleMessageTelegram(dataLength, packetType, payload);

            case Transmit_Radio_Telegram: // TRT
                // This should never happen, as this telegram is just for outbound data
                logger.trace("Received Transmit_Radio_Telegram: {}", HexUtils.bytesToHex(payload));
                break;

            case Transmit_Command_Telegram: // TCT => CommonCommand
                // this should also never happen, as this telegram is also just for outbound data
                // however FAM14 receives periodically 0xABFC messages
                if (payload[1] == (byte) 0xFC) {
                    return null;
                }

                logger.trace("Received Transmit_Command_Telegram: {}", HexUtils.bytesToHex(payload));
                break;
        }

        return null;
    }
}
