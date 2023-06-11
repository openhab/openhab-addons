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
import org.openhab.binding.enocean.internal.messages.ESP3Packet;
import org.openhab.binding.enocean.internal.messages.ESP3PacketFactory;
import org.openhab.binding.enocean.internal.messages.Response;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.util.HexUtils;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class EnOceanESP3Transceiver extends EnOceanTransceiver {

    public EnOceanESP3Transceiver(String path, TransceiverErrorListener errorListener,
            ScheduledExecutorService scheduler, SerialPortManager serialPortManager) {
        super(path, errorListener, scheduler, serialPortManager);
    }

    enum ReadingState {
        WaitingForSyncByte,
        ReadingHeader,
        ReadingData
    }

    byte[] dataBuffer = new byte[ENOCEAN_MAX_DATA];
    ReadingState state = ReadingState.WaitingForSyncByte;
    int currentPosition = 0;
    int dataLength = -1;
    int optionalLength = -1;
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
                    case WaitingForSyncByte:
                        if (_byte == ESP3Packet.ESP3_SYNC_BYTE) {
                            state = ReadingState.ReadingHeader;
                            logger.trace("Received Sync Byte");
                        }
                        break;
                    case ReadingHeader:
                        if (currentPosition == ESP3Packet.ESP3_HEADER_LENGTH) {
                            if (ESP3Packet.checkCRC8(dataBuffer, ESP3Packet.ESP3_HEADER_LENGTH, _byte)
                                    && ((dataBuffer[0] & 0xFF) << 8) + (dataBuffer[1] & 0xFF)
                                            + (dataBuffer[2] & 0xFF) > 0) {
                                state = ReadingState.ReadingData;

                                dataLength = ((dataBuffer[0] & 0xFF << 8) | (dataBuffer[1] & 0xFF));
                                optionalLength = dataBuffer[2] & 0xFF;
                                packetType = dataBuffer[3];
                                currentPosition = 0;

                                if (packetType == 3) {
                                    logger.trace("Received sub_msg");
                                }

                                logger.trace(">> Received header, data length {} optional length {} packet type {}",
                                        dataLength, optionalLength, packetType);
                            } else {
                                // check if we find a sync byte in current buffer
                                int copyFrom = -1;
                                for (int i = 0; i < ESP3Packet.ESP3_HEADER_LENGTH; i++) {
                                    if (dataBuffer[i] == ESP3Packet.ESP3_SYNC_BYTE) {
                                        copyFrom = i + 1;
                                        break;
                                    }
                                }

                                if (copyFrom != -1) {
                                    System.arraycopy(dataBuffer, copyFrom, dataBuffer, 0,
                                            ESP3Packet.ESP3_HEADER_LENGTH - copyFrom);
                                    state = ReadingState.ReadingHeader;
                                    currentPosition = ESP3Packet.ESP3_HEADER_LENGTH - copyFrom;
                                    dataBuffer[currentPosition++] = _byte;
                                } else {
                                    currentPosition = 0;
                                    state = _byte == ESP3Packet.ESP3_SYNC_BYTE ? ReadingState.ReadingHeader
                                            : ReadingState.WaitingForSyncByte;
                                }
                                logger.trace("CrC8 header check not successful");
                            }
                        } else {
                            dataBuffer[currentPosition++] = _byte;
                        }
                        break;
                    case ReadingData:
                        if (currentPosition == dataLength + optionalLength) {
                            if (ESP3Packet.checkCRC8(dataBuffer, dataLength + optionalLength, _byte)) {
                                state = ReadingState.WaitingForSyncByte;
                                BasePacket packet = ESP3PacketFactory.BuildPacket(dataLength, optionalLength,
                                        packetType, dataBuffer);

                                if (packet != null) {
                                    switch (packet.getPacketType()) {
                                        case COMMON_COMMAND:
                                            logger.debug("Common command: {}",
                                                    HexUtils.bytesToHex(packet.getPayload()));
                                            break;
                                        case EVENT:
                                        case RADIO_ERP1:
                                            informListeners(packet);
                                            break;
                                        case RADIO_ERP2:
                                            break;
                                        case RADIO_MESSAGE:
                                            break;
                                        case RADIO_SUB_TEL:
                                            break;
                                        case REMOTE_MAN_COMMAND:
                                            break;
                                        case RESPONSE: {
                                            Response response = (Response) packet;
                                            logger.debug("{} with code {} payload {} received",
                                                    packet.getPacketType().name(), response.getResponseType().name(),
                                                    HexUtils.bytesToHex(packet.getPayload())); // Responses do not have
                                                                                               // optional data
                                            handleResponse(response);
                                        }
                                            break;
                                        case SMART_ACK_COMMAND:
                                            break;
                                        default:
                                            break;
                                    }
                                } else {
                                    logger.trace("Unknown ESP3Packet: {}", HexUtils
                                            .bytesToHex(Arrays.copyOf(dataBuffer, dataLength + optionalLength)));
                                }
                            } else {
                                state = _byte == ESP3Packet.ESP3_SYNC_BYTE ? ReadingState.ReadingHeader
                                        : ReadingState.WaitingForSyncByte;
                                logger.trace("ESP3Packet malformed: {}",
                                        HexUtils.bytesToHex(Arrays.copyOf(dataBuffer, dataLength + optionalLength)));
                            }

                            currentPosition = 0;
                            dataLength = optionalLength = packetType = -1;
                        } else {
                            dataBuffer[currentPosition++] = _byte;
                        }
                        break;
                }
            }
        } catch (IOException ioexception) {
            errorListener.ErrorOccured(ioexception);
            return;
        }
    }

    @Override
    protected byte[] serializePacket(BasePacket packet) throws EnOceanException {
        return new ESP3Packet(packet).serialize();
    }
}
