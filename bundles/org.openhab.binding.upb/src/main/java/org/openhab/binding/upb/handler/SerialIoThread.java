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
package org.openhab.binding.upb.handler;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.TooManyListenersException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.NamedThreadFactory;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.openhab.binding.upb.handler.UPBIoHandler.CmdStatus;
import org.openhab.binding.upb.internal.message.MessageBuilder;
import org.openhab.binding.upb.internal.message.UPBMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event loop for serial communications. Handles sending and receiving UPB messages.
 *
 * @author Marcus Better - Initial contribution
 *
 */
@NonNullByDefault
public class SerialIoThread extends Thread implements SerialPortEventListener {
    private static final int WRITE_QUEUE_LENGTH = 128;
    private static final int ACK_TIMEOUT_MS = 500;
    private static final byte[] ENABLE_MESSAGE_MODE_CMD = { 0x17, 0x70, 0x02, (byte) 0x8e, 0x0d };

    private final Logger logger = LoggerFactory.getLogger(SerialIoThread.class);
    private final byte[] buffer = new byte[512];
    private final MessageListener listener;
    // Single-threaded executor for writes that serves to serialize writes.
    private final ExecutorService writeExecutor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(WRITE_QUEUE_LENGTH), new NamedThreadFactory("upb-serial", true));
    private final SerialPort serialPort;

    private int bufferLength = 0;
    private volatile @Nullable WriteRunnable currentWrite;
    private volatile boolean done;

    public SerialIoThread(final SerialPort serialPort, final MessageListener listener) {
        this.serialPort = serialPort;
        this.listener = listener;
    }

    @Override
    public void serialEvent(final SerialPortEvent event) {
        try {
            logger.trace("RXTX library CPU load workaround, sleep forever");
            Thread.sleep(Long.MAX_VALUE);
        } catch (final InterruptedException e) {
            // ignore
        }
    }

    @Override
    public void run() {
        // RXTX serial port library causes high CPU load
        // Start event listener, which will just sleep and slow down event loop
        try {
            serialPort.addEventListener(this);
        } catch (final TooManyListenersException e) {
            logger.warn("serial port setup failed", e);
            return;
        }
        serialPort.notifyOnDataAvailable(true);

        final byte[] buffer = new byte[256];
        try (final InputStream in = serialPort.getInputStream()) {
            enterMessageMode();
            int len;
            while (!done && (len = in.read(buffer)) >= 0) {
                addData(buffer, len);
            }
        } catch (final Exception e) {
            logger.warn("Exception in UPB read thread", e);
        } finally {
            logger.debug("shutting down receive thread");
            shutdownAndAwaitTermination(writeExecutor);
            serialPort.removeEventListener();
            try {
                serialPort.close();
            } catch (final Exception e) {
                // ignore
            }
        }
        logger.debug("UPB read thread stopped");
    }

    private void addData(final byte[] data, final int length) {
        if (bufferLength + length > buffer.length) {
            // buffer overflow, discard entire buffer
            bufferLength = 0;
        }
        System.arraycopy(data, 0, buffer, bufferLength, length);
        bufferLength += length;
        interpretBuffer();
    }

    private int findMessageLength(final byte[] buffer) {
        for (int i = 0; i < bufferLength; i++) {
            if (buffer[i] == 13) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Attempts to interpret any messages that may be contained in the buffer.
     */
    private void interpretBuffer() {
        int messageLength = findMessageLength(buffer);

        while (messageLength != -1) {
            final String message = new String(Arrays.copyOfRange(buffer, 0, messageLength), US_ASCII);
            logger.debug("UPB Message: {}", message);

            final int remainingBuffer = bufferLength - messageLength - 1;
            if (remainingBuffer > 0) {
                System.arraycopy(buffer, messageLength + 1, buffer, 0, remainingBuffer);
            }
            bufferLength = remainingBuffer;
            handleMessage(UPBMessage.fromString(message));
            messageLength = findMessageLength(buffer);
        }
    }

    private void handleMessage(final UPBMessage msg) {
        final WriteRunnable writeRunnable = currentWrite;
        switch (msg.getType()) {
            case ACK:
                if (writeRunnable != null) {
                    writeRunnable.ackReceived(true);
                }
                break;
            case NAK:
                if (writeRunnable != null) {
                    writeRunnable.ackReceived(false);
                }
                break;
            case ACCEPT:
                break;
            case ERROR:
                logger.info("received ERROR response from PIM");
                break;
            default:
                // ignore
        }
        listener.incomingMessage(msg);
    }

    public CompletionStage<CmdStatus> enqueue(final MessageBuilder msg) {
        final CompletableFuture<CmdStatus> completion = new CompletableFuture<>();
        final Runnable task = new WriteRunnable(msg.build(), completion);
        try {
            writeExecutor.execute(task);
        } catch (final RejectedExecutionException e) {
            completion.completeExceptionally(e);
        }
        return completion;
    }

    // puts the PIM is in message mode
    private void enterMessageMode() {
        try {
            serialPort.getOutputStream().write(ENABLE_MESSAGE_MODE_CMD);
            serialPort.getOutputStream().flush();
        } catch (final IOException e) {
            logger.warn("error setting message mode", e);
        }
    }

    void shutdownAndAwaitTermination(final ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
                    logger.warn("executor did not terminate");
                }
            }
        } catch (final InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void terminate() {
        done = true;
        try {
            serialPort.close();
        } catch (final Exception e) {
            logger.warn("failed to close serial port", e);
        }
    }

    private class WriteRunnable implements Runnable {
        private static final int MAX_RETRIES = 3;

        private final String msg;
        private final CompletableFuture<CmdStatus> completion;
        private final CountDownLatch ackLatch = new CountDownLatch(1);

        private @Nullable Boolean ack;

        public WriteRunnable(final String msg, final CompletableFuture<CmdStatus> completion) {
            this.msg = msg;
            this.completion = completion;
        }

        // called by reader thread on ACK or NAK
        public void ackReceived(final boolean ack) {
            if (logger.isDebugEnabled()) {
                if (ack) {
                    logger.debug("ACK received");
                } else {
                    logger.debug("NAK received");
                }
            }
            this.ack = ack;
            ackLatch.countDown();
        }

        @Override
        public void run() {
            currentWrite = this;
            try {
                logger.debug("Writing bytes: {}", msg);
                final OutputStream out = serialPort.getOutputStream();
                for (int tries = 0; tries < MAX_RETRIES && ack == null; tries++) {
                    out.write(0x14);
                    out.write(msg.getBytes(US_ASCII));
                    out.write(0x0d);
                    out.flush();
                    final boolean acked = ackLatch.await(ACK_TIMEOUT_MS, MILLISECONDS);
                    if (acked) {
                        break;
                    }
                    logger.debug("ack timed out, retrying ({} of {})", tries + 1, MAX_RETRIES);
                }
                if (ack == null) {
                    logger.debug("write not acked");
                    completion.complete(CmdStatus.WRITE_FAILED);
                } else if (ack) {
                    completion.complete(CmdStatus.ACK);
                } else {
                    completion.complete(CmdStatus.NAK);
                }
            } catch (final Exception e) {
                logger.warn("error writing message", e);
                completion.complete(CmdStatus.WRITE_FAILED);
            }
        }
    }
}
