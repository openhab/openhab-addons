/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.transceiver;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

import org.openhab.binding.enocean.internal.EnOceanException;
import org.openhab.binding.enocean.internal.messages.BasePacket;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;
import org.openhab.binding.enocean.internal.messages.ESP2Packet;
import org.openhab.binding.enocean.internal.messages.ESP2PacketConverter;
import org.openhab.binding.enocean.internal.messages.Response;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.util.HexUtils;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class EnOceanESP2Transceiver extends EnOceanTransceiver {

    public EnOceanESP2Transceiver(String path, TransceiverErrorListener errorListener,
            ScheduledExecutorService scheduler, SerialPortManager serialPortManager) {
        super(path, errorListener, scheduler, serialPortManager);
    }

    enum ReadingState {
        WaitingForFirstSyncByte,
        WaitingForSecondSyncByte,
        ReadingHeader,
        ReadingData
    }

    byte[] dataBuffer = new byte[ESP2Packet.ESP_PACKET_LENGTH];
    ReadingState state = ReadingState.WaitingForFirstSyncByte;
    int currentPosition = 0;
    int dataLength = -1;
    byte packetType = -1;

    @Override
    protected void processMessage(byte firstByte) {
        byte[] readingBuffer = new byte[ENOCEAN_MAX_DATA];
        int bytesRead = -1;
        byte _byte;

        try {
            readingBuffer[0] = firstByte;

            bytesRead = this.inputStream.read(readingBuffer, 1, inputStream.available());
            if (bytesRead == -1) {
                throw new IOException("could not read from inputstream");
            }

            if (readingTask == null || readingTask.isCancelled()) {
                return;
            }

            bytesRead++;
            for (int p = 0; p < bytesRead; p++) {
                _byte = readingBuffer[p];

                switch (state) {
                    case WaitingForFirstSyncByte:
                        if (_byte == ESP2Packet.ENOCEAN_ESP2_FIRSTSYNC_BYTE) {
                            state = ReadingState.WaitingForSecondSyncByte;
                            logger.trace("Received First Sync Byte");
                        }
                        break;
                    case WaitingForSecondSyncByte:
                        if (_byte == ESP2Packet.ENOCEAN_ESP2_SECONDSYNC_BYTE) {
                            state = ReadingState.ReadingHeader;
                            logger.trace("Received Second Sync Byte");
                        }
                        break;
                    case ReadingHeader: {
                        state = ReadingState.ReadingData;

                        currentPosition = 0;
                        dataBuffer[currentPosition++] = _byte;
                        dataLength = ((dataBuffer[0] & 0xFF) & 0b11111);
                        packetType = (byte) ((dataBuffer[0] & 0xFF) >> 5);

                        logger.trace(">> Received header, data length {} packet type {}", dataLength, packetType);
                    }
                        break;
                    case ReadingData:
                        if (currentPosition == dataLength) {
                            if (ESP2Packet.validateCheckSum(dataBuffer, dataLength, _byte)) {
                                BasePacket packet = ESP2PacketConverter.BuildPacket(dataLength, packetType, dataBuffer);
                                if (packet != null) {
                                    switch (packet.getPacketType()) {
                                        case RADIO_ERP1: {
                                            ERP1Message msg = (ERP1Message) packet;
                                            logger.debug("Converted to: {} with RORG {} for {}",
                                                    packet.getPacketType().name(), msg.getRORG().name(),
                                                    HexUtils.bytesToHex(msg.getSenderId()));

                                            if (msg.getRORG() != RORG.Unknown) {
                                                informListeners(msg);
                                            } else {
                                                logger.debug("Received unknown RORG");
                                            }
                                        }
                                            break;
                                        case RESPONSE: {
                                            Response response = (Response) packet;
                                            logger.debug("Converted to: {} with code {}", packet.getPacketType().name(),
                                                    response.getResponseType().name());

                                            handleResponse(response);
                                        }
                                            break;
                                        default:
                                            break;
                                    }
                                } else {
                                    if (dataBuffer[1] != (byte) 0xFC) {
                                        logger.debug("Unknown/unsupported ESP2Packet: {}",
                                                HexUtils.bytesToHex(Arrays.copyOf(dataBuffer, dataLength)));
                                    }
                                }
                            } else {
                                logger.debug("ESP2Packet malformed: {}", HexUtils.bytesToHex(dataBuffer));
                            }

                            state = _byte == ESP2Packet.ENOCEAN_ESP2_FIRSTSYNC_BYTE
                                    ? ReadingState.WaitingForSecondSyncByte
                                    : ReadingState.WaitingForFirstSyncByte;

                            currentPosition = 0;
                            dataLength = packetType = -1;
                        } else {
                            dataBuffer[currentPosition++] = _byte;
                        }
                        break;
                }
            }
        } catch (

        IOException ioexception) {
            errorListener.ErrorOccured(ioexception);
            return;
        }
    }

    @Override
    protected byte[] serializePacket(BasePacket packet) throws EnOceanException {
        return new ESP2Packet(packet).serialize();
    }
}
