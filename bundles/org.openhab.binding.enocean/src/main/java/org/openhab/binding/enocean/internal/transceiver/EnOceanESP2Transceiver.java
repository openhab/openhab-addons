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
package org.openhab.binding.enocean.internal.transceiver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
@NonNullByDefault
public class EnOceanESP2Transceiver extends EnOceanTransceiver {

    public EnOceanESP2Transceiver(String path, TransceiverErrorListener errorListener,
            ScheduledExecutorService scheduler, @Nullable SerialPortManager serialPortManager) {
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
        byte byteBuffer;

        try {
            readingBuffer[0] = firstByte;
            InputStream localInputStream = inputStream;
            if (localInputStream == null) {
                throw new IOException("could not read from inputstream, it was null");
            }
            bytesRead = localInputStream.read(readingBuffer, 1, localInputStream.available());
            if (bytesRead == -1) {
                throw new IOException("could not read from inputstream");
            }

            Future<?> localReadingTask = readingTask;
            if (localReadingTask == null || localReadingTask.isCancelled()) {
                return;
            }

            bytesRead++;
            for (int p = 0; p < bytesRead; p++) {
                byteBuffer = readingBuffer[p];

                switch (state) {
                    case WaitingForFirstSyncByte:
                        if (byteBuffer == ESP2Packet.ENOCEAN_ESP2_FIRSTSYNC_BYTE) {
                            state = ReadingState.WaitingForSecondSyncByte;
                            logger.trace("Received First Sync Byte");
                        }
                        break;
                    case WaitingForSecondSyncByte:
                        if (byteBuffer == ESP2Packet.ENOCEAN_ESP2_SECONDSYNC_BYTE) {
                            state = ReadingState.ReadingHeader;
                            logger.trace("Received Second Sync Byte");
                        }
                        break;
                    case ReadingHeader: {
                        state = ReadingState.ReadingData;

                        currentPosition = 0;
                        dataBuffer[currentPosition++] = byteBuffer;
                        dataLength = ((dataBuffer[0] & 0xFF) & 0b11111);
                        packetType = (byte) ((dataBuffer[0] & 0xFF) >> 5);

                        logger.trace(">> Received header, data length {} packet type {}", dataLength, packetType);
                    }
                        break;
                    case ReadingData:
                        if (currentPosition == dataLength) {
                            if (ESP2Packet.validateCheckSum(dataBuffer, dataLength, byteBuffer)) {
                                BasePacket packet = ESP2PacketConverter.buildPacket(dataLength, packetType, dataBuffer);
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
                                        byte[] array = Arrays.copyOf(dataBuffer, dataLength);
                                        String packetString = array != null ? HexUtils.bytesToHex(array) : "";
                                        logger.debug("Unknown/unsupported ESP2Packet: {}", packetString);
                                    }
                                }
                            } else {
                                logger.debug("ESP2Packet malformed: {}", HexUtils.bytesToHex(dataBuffer));
                            }

                            state = byteBuffer == ESP2Packet.ENOCEAN_ESP2_FIRSTSYNC_BYTE
                                    ? ReadingState.WaitingForSecondSyncByte
                                    : ReadingState.WaitingForFirstSyncByte;

                            currentPosition = 0;
                            dataLength = packetType = -1;
                        } else {
                            dataBuffer[currentPosition++] = byteBuffer;
                        }
                        break;
                }
            }
        } catch (IOException ioexception) {
            logger.trace("Unable to process message", ioexception);
            TransceiverErrorListener localListener = errorListener;
            if (localListener != null) {
                localListener.errorOccured(ioexception);
            }
            return;
        }
    }

    @Override
    protected byte[] serializePacket(BasePacket packet) throws EnOceanException {
        return new ESP2Packet(packet).serialize();
    }
}
