/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import org.openhab.binding.enocean.internal.messages.ESP3Packet;
import org.openhab.binding.enocean.internal.messages.ESP3PacketFactory;
import org.openhab.binding.enocean.internal.messages.Response;
import org.openhab.binding.enocean.internal.util.EnOceanUtil;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.util.HexUtils;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class EnOceanESP3Transceiver extends EnOceanTransceiver {

    private static final String RX_THREAD_NAME_PREFIX = "OH-binding-enocean-ESP3-RX-";
    private static final AtomicInteger THREAD_NUM = new AtomicInteger();

    // All access must be guarded by "this"
    private @Nullable Thread worker;

    public EnOceanESP3Transceiver(String path, TransceiverErrorListener errorListener,
            ScheduledExecutorService scheduler, @Nullable SerialPortManager serialPortManager) {
        super(path, errorListener, scheduler, serialPortManager);
    }

    enum ReadingState {
        WAIT_FIRST_SYNCBYTE,
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
            byte[] buffer = new byte[ENOCEAN_MAX_DATA + 7]; // 7 = 1 (sync) + 4 (header) + 2 * 1 (CrC8)
            int read;
            ReadingState state = ReadingState.WAIT_FIRST_SYNCBYTE;
            int doRead = 1;
            byte packetType = -1;
            int length = -1;
            int optionalLength = -1;
            int start = -1;
            int pos = 0;
            final Thread thread = Thread.currentThread();
            InputStream is = this.is;
            logger.trace("RX InputStream implementation: {}", is.getClass().getName());
            if (!is.markSupported()) {
                // Use this as a "rough indicator" that the stream isn't buffered
                logger.trace("Wrapping {} in BufferedInputStream", is.getClass().getName());
                is = new BufferedInputStream(is, 0x40000);
            }
            byte[] bytes;

            while (!thread.isInterrupted()) {
                try {
                    read = is.read(buffer, pos, doRead);
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
                            if (buffer[pos] == ESP3Packet.ESP3_SYNC_BYTE) {
                                state = ReadingState.READ_HEADER;
                                doRead = ESP3Packet.ESP3_HEADER_LENGTH + 1;
                                start = pos + 1;
                                is.mark(5);
                                logger.trace("Received sync byte");
                            } else {
                                doRead = 1;
                            }
                            break;
                        case READ_HEADER:
                            bytes = EnOceanUtil.subArray(buffer, start, ESP3Packet.ESP3_HEADER_LENGTH + 1);
                            length = -1;
                            if (ESP3Packet.checkCRC8(bytes, 0, ESP3Packet.ESP3_HEADER_LENGTH)
                                    && (length = ((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff)) > 0) {
                                state = ReadingState.READ_DATA;
                                optionalLength = bytes[2] & 0xff;
                                packetType = bytes[3];
                                doRead = length + optionalLength + 1;
                                start = pos + read;
                                logger.trace(">> Received header, data length {} optional length {} packet type {}",
                                        length, optionalLength, packetType);
                                if (packetType == 3) {
                                    logger.trace(">> Received sub_msg");
                                }
                            } else {
                                state = ReadingState.WAIT_FIRST_SYNCBYTE;
                                doRead = 1;
                                if (length == 0) {
                                    logger.debug("Received header with zero length, assuming false sync byte");
                                } else {
                                    logger.debug("Received header with invalid CrC8, assuming false sync byte");
                                }
                                start = -1;
                                length = -1;
                                try {
                                    is.reset();
                                } catch (IOException e) {
                                    logger.debug(
                                            "Failed to rewind inputstream after failed header, one packet might be lost: {}",
                                            e.getMessage());
                                }
                            }
                            break;
                        case READ_DATA:
                            try {
                                bytes = EnOceanUtil.subArray(buffer, start, length + optionalLength + 1);
                                if (ESP3Packet.checkCRC8(bytes, 0, length + optionalLength)) {
                                    BasePacket packet = ESP3PacketFactory.buildPacket(length, optionalLength,
                                            packetType, bytes);

                                    if (packet != null) {
                                        switch (packet.getPacketType()) {
                                            case COMMON_COMMAND:
                                                logger.debug("Common command: {}",
                                                        HexUtils.bytesToHex(packet.getPayload()));
                                                break;
                                            case EVENT:
                                            case RADIO_ERP1:
                                                informListeners(packet, scheduler);
                                                break;
                                            case RESPONSE:
                                                Response response = (Response) packet;
                                                if (logger.isDebugEnabled()) {
                                                    // Responses do not have optional data
                                                    logger.debug("{} with code {} payload {} received",
                                                            packet.getPacketType().name(),
                                                            response.getResponseType().name(),
                                                            HexUtils.bytesToHex(packet.getPayload()));
                                                }
                                                handleResponse(response, scheduler);
                                                break;
                                            case RADIO_ERP2:
                                            case RADIO_MESSAGE:
                                            case RADIO_SUB_TEL:
                                            case REMOTE_MAN_COMMAND:
                                            case SMART_ACK_COMMAND:
                                            default:
                                                break;
                                        }
                                    } else {
                                        logger.debug("Unknown ESP3Packet: {}", HexUtils.bytesToHex(bytes));
                                    }
                                } else {
                                    logger.debug("Malformed ESP3Packet: {}", HexUtils.bytesToHex(bytes));
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
                            start = -1;
                            length = -1;
                            optionalLength = -1;
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
        return new ESP3Packet(packet).serialize();
    }
}
