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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    private final List<ValloxEventListener> listeners = new CopyOnWriteArrayList<>();
    private final LinkedList<SendQueueItem> sendQueue = new LinkedList<>();
    protected final ArrayBlockingQueue<Byte> buffer = new ArrayBlockingQueue<>(1024);

    protected ScheduledExecutorService scheduler;
    protected @Nullable OutputStream outputStream;
    protected @Nullable InputStream inputStream;
    protected @Nullable ScheduledFuture<?> sendQueueHandler;

    protected byte ackByte;
    protected byte panelNumber;
    protected boolean connected = false;
    protected boolean waitForAckByte = false;
    protected boolean suspendTraffic = false;

    public ValloxBaseConnector(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
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
        return connected;
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
     * Stop queue handler
     */
    @Override
    public void close() {
        if (sendQueueHandler != null) {
            sendQueueHandler.cancel(true);
            sendQueueHandler = null;
            sendQueue.clear();
            logger.debug("Send queue handler stopped");
        }
    }

    /**
     * Send received telegram to registered listeners.
     *
     * @param telegram the telegram to send
     */
    public void sendTelegramToListeners(Telegram telegram) {
        for (ValloxEventListener listener : listeners) {
            try {
                listener.telegramReceived(telegram);
            } catch (Exception e) {
                logger.debug("Event listener invoking error: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Send received error to registered listeners.
     *
     * @param error the error to send
     */
    public void sendErrorToListeners(String error, @Nullable Exception exception) {
        for (ValloxEventListener listener : listeners) {
            try {
                listener.errorOccurred(error, exception);
            } catch (Exception e) {
                logger.debug("Event listener invoking error: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Parse byte buffer into telegrams. Separate single acknowledged and false bytes from buffer,
     * and after that pass next 6 bytes to telegram creation.
     *
     * @param buffer byte array to parse into telegrams
     * @throws InterruptedException
     */
    protected void handleBuffer() throws InterruptedException {
        while (buffer.size() > 6) {
            byte[] localBuffer = new byte[6];
            localBuffer[0] = buffer.take();

            if (localBuffer[0] != ValloxSEConstants.DOMAIN) {
                if (waitForAckByte) {
                    ackByte = localBuffer[0];
                    waitForAckByte = false;
                    return;
                }
                return;
            }
            for (int i = 1; i < localBuffer.length; i++) {
                localBuffer[i] = buffer.take();
            }
            createTelegramForListeners(localBuffer);
        }
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
            suspendTraffic = true;
            return;
        }
        if (localBuffer[3] == ValloxSEConstants.RESUME_BYTE) {
            sendTelegramToListeners(new Telegram(TelegramState.RESUME));
            suspendTraffic = false;
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
     * Put telegram into send queue and make sure job is scheduled to handle send queue.
     *
     * @param telegram the telegram to put to queue
     */
    @SuppressWarnings("null") // sendQueueHandler.isCancelled()
    @Override
    public void sendTelegram(Telegram telegram) {
        sendQueue.add(new SendQueueItem(telegram));
        if (sendQueueHandler == null || sendQueueHandler.isCancelled()) {
            sendQueueHandler = scheduler.scheduleWithFixedDelay(this::handleSendQueue, 0, 500, TimeUnit.MILLISECONDS);
            logger.debug("Send queue handler started");
        }
    }

    /**
     * Send one command or poll telegram from send queue
     */
    private void handleSendQueue() {
        if (suspendTraffic || sendQueue.isEmpty()) {
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
                        waitForAckByte = false;
                        sendTelegramToListeners(new Telegram(TelegramState.ACK, ackByte));
                    } else {
                        waitForAckByte = true;
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
    public void writeToOutputStream(Telegram telegram) {
        OutputStream outputStream = this.outputStream;
        if (outputStream != null) {
            try {
                outputStream.write(telegram.bytes);
                outputStream.flush();
                logger.debug("Wrote {}", telegram);
            } catch (IOException e) {
                sendErrorToListeners("Write to output stream failed, " + e.getMessage(), e);
            }
        } else {
            logger.debug("Output stream is null");
        }
    }
}
