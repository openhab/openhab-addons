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
package org.openhab.binding.enocean.internal.transceiver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.EnOceanException;
import org.openhab.binding.enocean.internal.messages.BasePacket;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;
import org.openhab.binding.enocean.internal.messages.ESP2Packet;
import org.openhab.binding.enocean.internal.messages.ESP2Packet.ESP2PacketType;
import org.openhab.binding.enocean.internal.messages.ESP2PacketConverter;
import org.openhab.binding.enocean.internal.messages.Response;
import org.openhab.binding.enocean.internal.util.EnOceanUtil;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.util.HexUtils;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class EnOceanESP2Transceiver extends EnOceanTransceiver {

    private static final String RX_THREAD_NAME_PREFIX = "OH-binding-enocean-ESP2-RX-";
    private static final AtomicInteger THREAD_NUM = new AtomicInteger();

    // All access must be guarded by "this"
    private @Nullable Thread worker;

    public EnOceanESP2Transceiver(String path, TransceiverErrorListener errorListener,
            ScheduledExecutorService scheduler, @Nullable SerialPortManager serialPortManager) {
        super(path, errorListener, scheduler, serialPortManager);
    }

    enum ReadingState {
        WAIT_FIRST_SYNCBYTE,
        WAIT_SECOND_SYNCBYTE,
        READ_HEADER,
        READ_DATA
    }

    @Override
    public void startReceiving(ScheduledExecutorService scheduler) {
        Thread worker;
        InputStream is;
        synchronized (this) {
            worker = this.worker;
            if (worker != null && worker.isAlive()) {
                worker.interrupt();
            }

            is = inputStream;
            if (is == null) {
                this.worker = worker = null;
            } else {
                this.worker = worker = new Thread(new Receiver(is, errorListener, scheduler),
                        RX_THREAD_NAME_PREFIX + THREAD_NUM.incrementAndGet());
                worker.setUncaughtExceptionHandler((t, e) -> {
                    logger.warn("Uncaught exception in EnOceanSerialTransceiver RX thread ({}): {}", t.getName(),
                            e.getMessage());
                    logger.trace("", e);
                    TransceiverErrorListener listener = this.errorListener;
                    if (listener != null) {
                        scheduler.execute(() -> listener.errorOccurred(e));
                    }
                });
            }
        }
        if (worker == null) {
            logger.warn("Cannot read from null stream");
            TransceiverErrorListener errorListener = this.errorListener;
            if (errorListener != null) {
                IOException e = new IOException("Cannot read from null stream");
                scheduler.execute(() -> errorListener.errorOccurred(e));
            }
        } else {
            worker.start();
            logger.info("EnOceanSerialTransceiver RX thread ({}) started", worker.getName());
        }
    }

    @Override
    protected void shutDownRx() {
        Thread worker;
        synchronized (this) {
            worker = this.worker;
            this.worker = null;
        }
        if (worker != null && worker.isAlive()) {
            worker.interrupt();
        }
    }

    private class Receiver implements Runnable {

        private final InputStream is;
        private final @Nullable TransceiverErrorListener errorListener;
        private final ScheduledExecutorService scheduler;

        public Receiver(InputStream is, @Nullable TransceiverErrorListener errorListener,
                ScheduledExecutorService scheduler) {
            this.is = is;
            this.errorListener = errorListener;
            this.scheduler = scheduler;
        }

        @Override
        public void run() {
            byte[] bytes = new byte[64];
            int read;
            ReadingState state = ReadingState.WAIT_FIRST_SYNCBYTE;
            int doRead = 1;
            byte packetType = -1;
            int packetLength = -1;
            int packetStart = -1;
            int pos = 0;
            final Thread thread = Thread.currentThread();
            InputStream is = this.is;
            logger.trace("RX InputStream implementation: {}", is.getClass().getName());
            if (!is.markSupported()) {
                // Use this as a "rough indicator" that the stream isn't buffered
                logger.trace("Wrapping {} in BufferedInputStream", is.getClass().getName());
                is = new BufferedInputStream(is, ENOCEAN_MAX_DATA);
            }
            byte[] packetBytes;

            while (!thread.isInterrupted()) {
                try {
                    read = is.read(bytes, pos, doRead);
                } catch (IOException e) {
                    logger.debug("Unable to read from serial port: {}", e.getMessage());
                    logger.trace("", e);
                    TransceiverErrorListener errorListener = this.errorListener;
                    if (errorListener != null && !thread.isInterrupted()) {
                        // We don't want to take the Thing offline if an IOException is thrown when the port is closed,
                        // which is why we check isInterrupted()
                        errorListener.errorOccurred(e);
                    }
                    break;
                }
                if (read <= 0) {
                    // Unlike regular InputStreams, the serial port streams occasionally returns -1 even if the
                    // stream is still "alive", so just accept it and try to read again. Add a short backoff to
                    // avoid a tight loop and high CPU usage if this happens repeatedly.
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }
                doRead -= read;
                if (doRead == 0) {
                    switch (state) {
                        case WAIT_FIRST_SYNCBYTE:
                            if (bytes[pos] == ESP2Packet.ENOCEAN_ESP2_FIRSTSYNC_BYTE) {
                                state = ReadingState.WAIT_SECOND_SYNCBYTE;
                                logger.trace("Received first sync byte");
                            }
                            doRead = 1;
                            break;
                        case WAIT_SECOND_SYNCBYTE:
                            if (bytes[pos] == ESP2Packet.ENOCEAN_ESP2_SECONDSYNC_BYTE) {
                                state = ReadingState.READ_HEADER;
                                logger.trace("Received second sync byte");
                            } else {
                                state = ReadingState.WAIT_FIRST_SYNCBYTE;
                                logger.trace(
                                        "Received non-matching second sync byte ({}) - first sync byte was a false positive",
                                        EnOceanUtil.byteToHex(bytes[pos]));
                            }
                            doRead = 1;
                            break;
                        case READ_HEADER:
                            doRead = bytes[pos] & 0x1f;
                            if (doRead == 0) {
                                state = ReadingState.WAIT_FIRST_SYNCBYTE;
                                doRead = 1;
                                logger.debug(">> Received header with zero length, ignoring packet");
                            } else {
                                state = ReadingState.READ_DATA;
                                packetStart = pos;
                                packetLength = (byte) doRead;
                                packetType = (byte) ((bytes[pos] & 0xff) >> 5);
                                logger.trace(">> Received header, data length {} packet type {}", doRead, packetType);
                            }
                            break;
                        case READ_DATA:
                            try {
                                packetBytes = EnOceanUtil.subArray(bytes, packetStart, packetLength + 1);
                                if (ESP2Packet.validateCheckSum(packetBytes, 0, packetLength)) {
                                    if (packetBytes[1] == ESP2Packet.ENOCEAN_ESP2_INTERNAL_COMMAND_BYTE) {
                                        // Internal commands have a structure that we can't decode,
                                        // and they shouldn't be of interest to us
                                        if (logger.isTraceEnabled()) {
                                            logger.trace("Skipping internal command ESP2Packet: {}",
                                                    HexUtils.bytesToHex(packetBytes));
                                        }
                                    } else {
                                        BasePacket packet = ESP2PacketConverter
                                                .buildPacket(ESP2PacketType.getPacketType(packetType), packetBytes);
                                        if (packet != null) {
                                            switch (packet.getPacketType()) {
                                                case RADIO_ERP1:
                                                    ERP1Message msg = (ERP1Message) packet;
                                                    if (logger.isDebugEnabled()) {
                                                        logger.debug("Converted to: {} with RORG {} for {}",
                                                                packet.getPacketType().name(), msg.getRORG().name(),
                                                                HexUtils.bytesToHex(msg.getSenderId()));
                                                    }

                                                    if (msg.getRORG() != RORG.Unknown) {
                                                        informListeners(msg, scheduler);
                                                    } else {
                                                        logger.debug("Received unknown RORG");
                                                    }
                                                    break;
                                                case RESPONSE:
                                                    Response response = (Response) packet;
                                                    logger.debug("Converted to: {} with code {}",
                                                            packet.getPacketType().name(),
                                                            response.getResponseType().name());

                                                    handleResponse(response, scheduler);
                                                    break;
                                                default:
                                                    logger.debug("Not handling packet of type {}",
                                                            packet.getPacketType());
                                                    break;
                                            }
                                        } else if (logger.isDebugEnabled()) {
                                            logger.debug("Unknown/unsupported ESP2Packet: {}",
                                                    HexUtils.bytesToHex(packetBytes));
                                        }
                                    }
                                } else {
                                    logger.debug("Malformed ESP2Packet: {}", HexUtils.bytesToHex(packetBytes));
                                }
                            } catch (RuntimeException e) {
                                logger.debug("Unable to process message: {}", e.getMessage());
                                logger.trace("", e);
                                TransceiverErrorListener errorListener = this.errorListener;
                                if (errorListener != null && !thread.isInterrupted()) {
                                    // We don't want to take the Thing offline if a RuntimeException is thrown while
                                    // the Receiver is terminating, which is why we check isInterrupted()
                                    errorListener.errorOccurred(e);
                                }
                            }
                            state = ReadingState.WAIT_FIRST_SYNCBYTE;
                            doRead = 1;
                            packetStart = -1;
                            packetLength = -1;
                            packetType = -1;
                            break;
                    }
                }

                // Not checking for overflow here because it should be impossible, and should it happen
                // throwing an IndexOutOfBoundsException above is just as good as anything we can do here.
                pos = state == ReadingState.WAIT_FIRST_SYNCBYTE ? 0 : pos + read;
            }

            logger.info("Shutting down EnOceanSerialTransceiver RX thread ({})", thread.getName());
        }
    }

    @Override
    protected byte[] serializePacket(BasePacket packet) throws EnOceanException {
        return new ESP2Packet(packet).serialize();
    }
}
