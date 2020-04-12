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
package org.openhab.binding.vallox.internal.se.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vallox.internal.se.ValloxSEConstants;
import org.openhab.binding.vallox.internal.se.telegram.SendQueueItem;
import org.openhab.binding.vallox.internal.se.telegram.Telegram;
import org.openhab.binding.vallox.internal.se.telegram.Telegram.TelegramState;
import org.openhab.binding.vallox.internal.se.telegram.TelegramFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This interface defines methods to receive data from vallox.
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public abstract class ValloxBaseConnector implements ValloxConnector {

    private final Logger logger = LoggerFactory.getLogger(ValloxBaseConnector.class);

    private final Thread telegramProcessorThread = new TelegramProcessor("Vallox Telegram Processor");
    private final Thread sendQueueHandlerThread = new SendQueueHandler("Vallox Send Queue Processor");

    private final List<ValloxEventListener> listeners = new CopyOnWriteArrayList<>();
    private final LinkedList<SendQueueItem> sendQueue = new LinkedList<>();
    protected final ArrayBlockingQueue<Byte> buffer = new ArrayBlockingQueue<>(1024);

    protected @Nullable OutputStream outputStream;
    protected @Nullable InputStream inputStream;

    protected final AtomicBoolean connected = new AtomicBoolean(false);
    protected final AtomicBoolean waitForAckByte = new AtomicBoolean(false);
    protected final AtomicBoolean suspendTraffic = new AtomicBoolean(false);

    private volatile byte ackByte;
    protected byte panelNumber;

    /**
     * Start threads for receiving and processing telegrams
     */
    protected void startProcessorThreads() {
        if (!telegramProcessorThread.isAlive()) {
            telegramProcessorThread.setDaemon(true);
            telegramProcessorThread.start();
        }
        if (!sendQueueHandlerThread.isAlive()) {
            sendQueueHandlerThread.setDaemon(true);
            sendQueueHandlerThread.start();
        }
    }

    /**
     * Stop telegram receive and process threads
     */
    protected void stopProcessorThreads() {
        telegramProcessorThread.interrupt();
        try {
            telegramProcessorThread.join(2000);
        } catch (InterruptedException e) {
            // Do nothing
        }
        sendQueueHandlerThread.interrupt();
        try {
            sendQueueHandlerThread.join(2000);
        } catch (InterruptedException e) {
            // Do nothing
        }
    }

    /**
     * Add listener.
     *
     * @param listener the listener to add
     */
    @Override
    public void addListener(ValloxEventListener listener) {
        if (!listeners.contains(listener)) {
            this.listeners.add(listener);
            logger.debug("Listener registered: {}", listener.toString());
        }
    }

    /**
     * Remove listener.
     *
     * @param listner the listener to remove
     */
    @Override
    public void removeListener(ValloxEventListener listener) {
        this.listeners.remove(listener);
        logger.debug("Listener removed: {}", listener.toString());
    }

    /**
     * Get connection status.
     */
    @Override
    public boolean isConnected() {
        return connected.get();
    }

    /**
     * Get this connectors {@link InputStream}
     *
     * @return {@link InputStream} of this connector
     */
    protected @Nullable InputStream getInputStream() {
        return this.inputStream;
    }

    /**
     * Get this connectors {@link InputStream}
     *
     * @return {@link InputStream} of this connector
     */
    protected @Nullable OutputStream getOutputStream() {
        return this.outputStream;
    }

    /**
     * Stop telegram processing threads
     */
    @Override
    public void close() {
        stopProcessorThreads();
    }

    /**
     * Send received telegram to registered listeners.
     *
     * @param telegram the telegram to send
     */
    public void sendTelegramToListeners(Telegram telegram) {
        listeners.forEach(listener -> listener.telegramReceived(telegram));
    }

    /**
     * Send received error to registered listeners.
     *
     * @param error the error to send
     */
    public void sendErrorToListeners(String error, @Nullable Exception exception) {
        listeners.forEach(listener -> listener.errorOccurred(error, exception));
    }

    /**
     * Parse byte buffer into telegrams. Separate single acknowledged and false bytes from buffer,
     * and after that pass next 6 bytes to telegram creation.
     *
     * @param buffer byte array to parse into telegrams
     * @throws InterruptedException
     */
    private void handleBuffer() throws InterruptedException {
        byte[] localBuffer = new byte[6];
        localBuffer[0] = buffer.take();

        if (localBuffer[0] != ValloxSEConstants.DOMAIN) {
            if (waitForAckByte.get()) {
                ackByte = localBuffer[0];
                waitForAckByte.set(false);
                return;
            }
            return;
        }
        for (int i = 1; i < localBuffer.length; i++) {
            localBuffer[i] = buffer.take();
        }
        createTelegramForListeners(localBuffer);
    }

    /**
     * Form a telegram from bytes and send it to listeners.
     *
     * @param buffer the byte buffer to handle
     */
    private void createTelegramForListeners(byte[] localBuffer) {
        if (!TelegramFactory.isChecksumValid(localBuffer, localBuffer[5])) {
            sendTelegramToListeners(new Telegram(TelegramState.CRC_ERROR, localBuffer));
            return;
        }
        if (localBuffer[3] == ValloxSEConstants.SUSPEND_BYTE) {
            sendTelegramToListeners(new Telegram(TelegramState.SUSPEND));
            suspendTraffic.set(true);
            return;
        }
        if (localBuffer[3] == ValloxSEConstants.RESUME_BYTE) {
            sendTelegramToListeners(new Telegram(TelegramState.RESUME));
            suspendTraffic.set(false);
            return;
        }
        if (localBuffer[2] == panelNumber || localBuffer[2] == ValloxSEConstants.ADDRESS_ALL_PANELS
                || localBuffer[2] == ValloxSEConstants.ADDRESS_PANEL1) {
            sendTelegramToListeners(new Telegram(TelegramState.OK, localBuffer));
        } else {
            sendTelegramToListeners(new Telegram(TelegramState.NOT_FOR_US, localBuffer));
        }
    }

    /**
     * Put telegram into send queue for further processing if we're connected.
     *
     * @param telegram the telegram to put to queue
     */
    @Override
    public void sendTelegram(Telegram telegram) {
        if (connected.get()) {
            sendQueue.add(new SendQueueItem(telegram));
        }
    }

    /**
     * Send one command or poll telegram from send queue
     */
    private void handleSendQueue() {
        if (suspendTraffic.get() || sendQueue.isEmpty()) {
            return;
        }
        SendQueueItem queueItem = sendQueue.removeFirst();
        Telegram telegram = queueItem.getTelegram();
        switch (telegram.state) {
            case POLL:
                writeToOutputStream(telegram);
                break;
            case COMMAND:
                if (queueItem.retry()) {
                    if (telegram.getCheksum() == ackByte) {
                        waitForAckByte.set(false);
                        sendTelegramToListeners(new Telegram(TelegramState.ACK, telegram.bytes));
                    } else {
                        waitForAckByte.set(true);
                        writeToOutputStream(telegram);
                        sendQueue.addFirst(queueItem);
                    }
                } else {
                    sendErrorToListeners("Ack byte not received for telegram: " + telegram.toString(), null);
                }
                break;
            default:
                logger.debug("Unknown telegram in send queue: {}", telegram.state);
                break;
        }
    }

    /**
     * Write telegram bytes to output stream
     *
     * @param telegram the telegram to write
     */
    private void writeToOutputStream(Telegram telegram) {
        OutputStream outputStream = this.outputStream;
        if (outputStream != null) {
            try {
                outputStream.write(telegram.bytes);
                outputStream.flush();
                logger.debug("Wrote {}", telegram);
            } catch (IOException e) {
                sendErrorToListeners("Write to output stream failed, " + e.getMessage(), e);
                sendQueue.clear();
            }
        } else {
            logger.debug("Output stream is null");
        }
    }

    /**
     * {@link Thread} implementation for processing telegrams
     *
     * @author Miika Jukka - Initial contribution
     */
    private class TelegramProcessor extends Thread {
        boolean interrupted = false;

        public TelegramProcessor(String name) {
            super(name);
            setDaemon(true);
        }

        @Override
        public void interrupt() {
            interrupted = true;
            super.interrupt();
        }

        @Override
        public void run() {
            logger.trace("Telegram processor thread started");
            while (!interrupted) {
                try {
                    handleBuffer();
                } catch (InterruptedException e) {
                    sendErrorToListeners("Buffer handling interrupted", e);
                    interrupt();
                }
            }
            logger.trace("Telegram processor thread stopped");
        }
    }

    /**
     * Handle send queue in own thread
     *
     * @author Miika Jukka - Initial contribution
     */
    private class SendQueueHandler extends Thread {
        boolean interrupted = false;

        public SendQueueHandler(String name) {
            super(name);
            setDaemon(true);
        }

        @Override
        public void interrupt() {
            interrupted = true;
            super.interrupt();
        }

        @Override
        public void run() {
            logger.trace("Send queue handler thread started");
            while (!interrupted) {
                try {
                    handleSendQueue();
                    sleep(500);
                } catch (InterruptedException e) {
                    // Time to stop
                    interrupt();
                } catch (NoSuchElementException e) {
                    logger.debug("Send queue handler exception ", e);
                }
            }
            logger.trace("Send queue handler thread stopped");
        }
    }
}
