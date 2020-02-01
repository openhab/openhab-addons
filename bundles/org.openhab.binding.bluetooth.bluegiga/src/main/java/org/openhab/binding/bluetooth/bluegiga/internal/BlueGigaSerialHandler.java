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
package org.openhab.binding.bluetooth.bluegiga.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main handler class for interacting with the BlueGiga serial API. This class provides transaction management and
 * queuing of of data, and conversion of packets from the serial stream into command and response classes.
 *
 * @author Chris Jackson - Initial contribution and API
 * @author Pauli Anttila - Message correlation
 *
 */
public class BlueGigaSerialHandler {

    private static final int BLE_MAX_LENGTH = 64;
    private static final int TRANSACTION_TIMEOUT_PERIOD_MS = 100;

    private final Logger logger = LoggerFactory.getLogger(BlueGigaSerialHandler.class);

    /**
     * Unique transaction id for request and response correlation
     */
    private AtomicInteger transactionId = new AtomicInteger();

    /**
     * Ongoing transaction id. If null, no ongoing transaction.
     */
    private volatile Integer ongoingTransactionId = null;

    private Future<?> transactionTimeoutTimer;

    /**
     * The portName portName output stream.
     */
    private final OutputStream outputStream;
    private final Queue<BlueGigaUniqueCommand> sendQueue = new LinkedList<BlueGigaUniqueCommand>();
    private final Timer timer = new Timer();
    private Thread parserThread = null;
    private final ScheduledExecutorService executor = ThreadPoolManager.getScheduledPool("BlueGigaSerialHandler");

    /**
     * Transaction listeners are used internally to correlate the commands and responses
     */
    private final List<BluetoothListener<? extends BlueGigaResponse>> transactionListeners = new CopyOnWriteArrayList<>();

    /**
     * The event listeners will be notified of any asynchronous events
     */
    private final Set<BlueGigaEventListener> eventListeners = new CopyOnWriteArraySet<>();

    /**
     * The event listeners will be notified of any life-cycle events of the handler.
     */
    private final Set<BlueGigaHandlerListener> handlerListeners = new CopyOnWriteArraySet<>();

    /**
     * Flag reflecting that parser has been closed and parser parserThread
     * should exit.
     */
    private boolean close = false;

    public BlueGigaSerialHandler(final InputStream inputStream, final OutputStream outputStream) {
        this.outputStream = outputStream;

        final int framecheckParams[] = new int[] { 0x00, 0x7F, 0xC0, 0xF8, 0xE0 };

        parserThread = new Thread("BlueGigaBLEHandler") {
            @Override
            public void run() {
                int exceptionCnt = 0;
                logger.debug("BlueGiga BLE thread started");
                int[] inputBuffer = new int[BLE_MAX_LENGTH];
                int inputCount = 0;
                int inputLength = 0;

                while (!close) {
                    try {
                        int val = inputStream.read();
                        if (val == -1) {
                            continue;
                        }

                        inputBuffer[inputCount++] = val;

                        if (inputCount < 4) {
                            // The BGAPI protocol has no packet framing, and no error detection, so we do a few
                            // sanity checks on the header to try and allow resyncronisation should there be an
                            // error.
                            // Byte 0: Check technology type is bluetooth and high length is 0
                            // Byte 1: Check length is less than 64 bytes
                            // Byte 2: Check class ID is less than 8
                            // Byte 3: Check command ID is less than 16
                            if ((val & framecheckParams[inputCount]) != 0) {
                                logger.debug("BlueGiga framing error byte {} = {}", inputCount, val);
                                inputCount = 0;
                                continue;
                            }
                        } else if (inputCount == 4) {
                            // Process the header to get the length
                            inputLength = inputBuffer[1] + (inputBuffer[0] & 0x02 << 8) + 4;
                            if (inputLength > 64) {
                                logger.error("BLE length larger than 64 bytes ({})", inputLength);
                            }
                        }
                        if (inputCount == inputLength) {
                            // End of packet reached - process
                            BlueGigaResponse responsePacket = BlueGigaResponsePackets.getPacket(inputBuffer);

                            if (logger.isTraceEnabled()) {
                                logger.trace("BLE RX: {}", printHex(inputBuffer, inputLength));
                                logger.trace("BLE RX: {}", responsePacket);
                            }
                            if (responsePacket != null) {
                                if (responsePacket.isEvent()) {
                                    notifyEventListeners(responsePacket);
                                } else {
                                    notifyTransactionComplete(responsePacket);
                                }
                            }

                            inputCount = 0;
                        }

                    } catch (final IOException e) {
                        logger.error("BlueGiga BLE IOException: ", e);

                        if (exceptionCnt++ > 10) {
                            logger.error("BlueGiga BLE exception count exceeded");
                            close = true;
                            notifyClosed(e);
                        }
                    }
                }
                logger.debug("BlueGiga BLE exited.");
            }
        };

        parserThread.setDaemon(true);
        parserThread.start();
        int tries = 0;
        // wait until the daemon thread kicks off, e.g. when it is ready to receive any commands
        while (parserThread.getState() == Thread.State.NEW) {
            try {
                Thread.sleep(100);
                tries++;
                if (tries > 10) {
                    throw new IllegalStateException("BlueGiga handler thread failed to start");
                }
            } catch (InterruptedException ignore) {
                /* ignore */ }
        }
    }

    /**
     * Requests parser thread to shutdown. Waits forever while the parser thread is getting shut down.
     */
    public void close() {
        close(0);
    }

    /**
     * Requests parser thread to shutdown. Waits specified milliseconds while the parser thread is getting shut down.
     *
     * @param timeout milliseconds to wait
     */
    public void close(long timeout) {
        close = true;
        cancelTransactionTimer();
        executor.shutdownNow();
        timer.cancel();
        try {
            parserThread.interrupt();
            parserThread.join(timeout);
        } catch (InterruptedException e) {
            logger.warn("Interrupted in packet parser thread shutdown join.");
        }
    }

    /**
     * Checks if parser thread is alive.
     *
     * @return true if parser thread is alive.
     */
    public boolean isAlive() {
        return parserThread != null && parserThread.isAlive() && !close;
    }

    private void sendFrame(BlueGigaUniqueCommand bleFrame) {
        // Send the data
        BlueGigaCommand frame = bleFrame.getMessage();
        ongoingTransactionId = bleFrame.getTransactionId();
        logger.debug("sendFrame: ongoingTransactionId = {}, frame={}", ongoingTransactionId, bleFrame);
        cancelTransactionTimer();
        try {
            int[] payload = frame.serialize();
            if (logger.isTraceEnabled()) {
                logger.trace("BLE TX: {}", printHex(payload, payload.length));
            }
            for (int b : payload) {
                outputStream.write(b);
            }
            startTransactionTimer();

        } catch (IOException e) {
            throw new BlueGigaException("Error sending BLE frame", e);
        }
    }

    private void startTransactionTimer() {
        transactionTimeoutTimer = executor.schedule(() -> {
            notifyTransactionTimeout(ongoingTransactionId);
        }, TRANSACTION_TIMEOUT_PERIOD_MS * 2, TimeUnit.MILLISECONDS);
    }

    private void cancelTransactionTimer() {
        if (transactionTimeoutTimer != null) {
            transactionTimeoutTimer.cancel(true);
        }
    }

    private void sendNextFrame() {
        BlueGigaUniqueCommand nextFrame = sendQueue.poll();
        if (nextFrame == null) {
            // Nothing to send
            logger.debug("sendNextFrame: nothing to send");
            return;
        }
        logger.debug("sendNextFrame: {}", nextFrame);
        sendFrame(nextFrame);
    }

    /**
     * Add a {@link BlueGigaCommand} frame to the send queue. The sendQueue is a
     * FIFO queue. This method queues a {@link BlueGigaCommand} frame without
     * waiting for a response.
     *
     * @param transaction
     *            {@link BlueGigaCommand}
     */
    public void queueFrame(BlueGigaUniqueCommand request) {
        logger.debug("Queue TX BLE frame: {}", request);
        checkIfAlive();
        sendQueue.add(request);
        logger.debug("TX BLE queue size: {}", sendQueue.size());
        sendNextTransactionIfNoOngoing();
    }

    private void sendNextTransactionIfNoOngoing() {
        synchronized (this) {
            logger.debug("Send next transaction if no ongoing");
            if (ongoingTransactionId == null) {
                sendNextFrame();
            }
        }
    }

    private void clearOngoingTransactionAndSendNext() {
        synchronized (this) {
            logger.debug("Clear ongoing transaction and send next message from queue");
            ongoingTransactionId = null;
            sendNextFrame();
        }
    }

    /**
     * Notify any transaction listeners when we receive a response.
     *
     * @param response
     *            the response data received
     * @return true if the response was processed
     */
    private boolean notifyTransactionComplete(final BlueGigaResponse response) {
        boolean processed = false;

        for (BluetoothListener<? extends BlueGigaResponse> listener : transactionListeners) {
            if (listener.transactionEvent(response)) {
                processed = true;
            }
        }

        return processed;
    }

    private boolean notifyTransactionTimeout(final Integer transactionId) {
        boolean processed = false;

        for (BluetoothListener<? extends BlueGigaResponse> listener : transactionListeners) {
            if (listener.transactionTimeout(transactionId)) {
                processed = true;
            }
        }

        return processed;
    }

    private void addTransactionListener(BluetoothListener<? extends BlueGigaResponse> listener) {
        if (transactionListeners.contains(listener)) {
            return;
        }

        transactionListeners.add(listener);
    }

    private void removeTransactionListener(BluetoothListener<?> listener) {
        transactionListeners.remove(listener);
    }

    /**
     * Sends an BlueGiga request without waiting for the response.
     *
     * @param bleCommand {@link BlueGigaCommand}
     * @return response {@link Future} {@link BlueGigaResponse}
     */
    private <T extends BlueGigaResponse> Future<T> sendBleRequestAsync(final BlueGigaCommand bleCommand,
            final Class<T> expected) {
        checkIfAlive();
        class TransactionWaiter implements Callable<T>, BluetoothListener<T> {
            private volatile boolean complete;
            private BlueGigaResponse response;
            BlueGigaUniqueCommand query = new BlueGigaUniqueCommand(bleCommand, transactionId.getAndIncrement());

            @SuppressWarnings("unchecked")
            @Override
            public T call() throws TimeoutException {
                // Register a listener
                addTransactionListener(this);

                // Send the transaction
                queueFrame(query);

                // Wait transaction completed or timeout
                synchronized (this) {
                    while (!complete) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            complete = true;
                        }
                    }
                }

                cancelTransactionTimer();

                // Remove the listener
                removeTransactionListener(this);

                // Send next transaction if any
                executor.submit(new Runnable() {

                    @Override
                    public void run() {
                        clearOngoingTransactionAndSendNext();
                    }
                });

                if (response == null) {
                    throw new TimeoutException("No response from BlueGiga controller");
                }
                return (T) response;
            }

            @Override
            public boolean transactionEvent(BlueGigaResponse bleResponse) {
                logger.debug("Expected transactionId: {}, ongoingTransactionId: {}", query.getTransactionId(),
                        ongoingTransactionId);

                if (ongoingTransactionId != query.getTransactionId()) {
                    logger.debug("Ignore response as ongoingTransactionId {} doesn't match expected transactionId {}.",
                            ongoingTransactionId, query.getTransactionId());
                    return false;
                }

                logger.debug("Expected response: {}, Received response: {}", expected.getSimpleName(), bleResponse);

                if (bleCommand instanceof BlueGigaDeviceCommand && bleResponse instanceof BlueGigaDeviceResponse) {
                    BlueGigaDeviceCommand devCommand = (BlueGigaDeviceCommand) bleCommand;
                    BlueGigaDeviceResponse devResponse = (BlueGigaDeviceResponse) bleResponse;

                    logger.debug("Expected connection id: {}, Response connection id: {}", devCommand.getConnection(),
                            devResponse.getConnection());

                    if (devCommand.getConnection() != devResponse.getConnection()) {
                        logger.debug("Ignore response as response connection id {} doesn't match expected id {}.",
                                devResponse.getConnection(), devCommand.getConnection());
                        return false;
                    }
                }

                if (!expected.isInstance(bleResponse)) {
                    // ignoring response if it was not requested
                    logger.debug("Ignoring {} response which has not been requested.",
                            bleResponse.getClass().getSimpleName());
                    return false;
                }

                // Response received, notify waiter
                response = bleResponse;
                complete = true;
                synchronized (this) {
                    notify();
                }
                return true;
            }

            @Override
            public boolean transactionTimeout(Integer transactionId) {
                if (ongoingTransactionId != query.getTransactionId()) {
                    return false;
                }
                logger.debug("Timeout, no response received for transaction {}", query.getTransactionId());
                complete = true;
                synchronized (this) {
                    notify();
                }
                return true;
            }
        }

        Callable<T> worker = new TransactionWaiter();
        return executor.submit(worker);
    }

    /**
     * Sends a {@link BlueGigaCommand} request to the NCP and waits for the response for specified period of time.
     * The response is correlated with the request and the returned {@link BlueGigaResponse}
     * contains the request and response data.
     *
     * @param bleCommand {@link BlueGigaCommand}
     * @param timeout milliseconds to wait until {@link TimeoutException} is thrown
     * @return response {@link BlueGigaResponse}
     * @throws TimeoutException when specified timeout exceeds
     */
    public <T extends BlueGigaResponse> T sendTransaction(BlueGigaCommand bleCommand, Class<T> expected, long timeout)
            throws BlueGigaException {
        Future<T> futureResponse = sendBleRequestAsync(bleCommand, expected);
        try {
            return futureResponse.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            futureResponse.cancel(true);
            throw new BlueGigaException(String.format("Error sending BLE transaction: %s", e.getMessage()), e);
        }
    }

    /**
     * Notify any transaction listeners when we receive a response.
     * This uses a separate thread to separate the processing of the event.
     *
     * @param response the response data received
     * @return true if the response was processed
     */
    private void notifyEventListeners(final BlueGigaResponse response) {
        // Notify the listeners
        for (final BlueGigaEventListener listener : eventListeners) {
            executor.submit(() -> listener.bluegigaEventReceived(response));
        }
    }

    public void addEventListener(BlueGigaEventListener listener) {
        eventListeners.add(listener);
    }

    /**
     * Adds a handler listener.
     *
     * @param listener a new handler listener
     */
    public void addHandlerListener(BlueGigaHandlerListener listener) {
        handlerListeners.add(listener);
    }

    public void removeEventListener(BlueGigaEventListener listener) {
        eventListeners.remove(listener);
    }

    public void removeHandlerListener(BlueGigaHandlerListener listener) {
        handlerListeners.remove(listener);
    }

    private String printHex(int[] data, int len) {
        StringBuilder builder = new StringBuilder();

        for (int cnt = 0; cnt < len; cnt++) {
            builder.append(String.format("%02X ", data[cnt]));
        }

        return builder.toString();
    }

    private void checkIfAlive() {
        if (!isAlive()) {
            throw new IllegalStateException("Bluegiga handler is dead. Most likely because of IO errors. "
                    + "Re-initialization of the BlueGigaSerialHandler is required.");
        }
    }

    /**
     * Notify handler event listeners that the handler was bluegigaClosed due to an error specified as an argument.
     *
     * @param reason the reason to bluegigaClosed
     */
    private void notifyClosed(Exception reason) {
        // It should be safe enough not to use the NotificationService as this is a fatal error, no any further actions
        // can be done with the handler, a new handler should be re-created
        // There is another reason why NotificationService can't be used - the listeners should be notified immediately
        for (BlueGigaHandlerListener listener : handlerListeners) {
            try {
                listener.bluegigaClosed(reason);
            } catch (Exception ex) {
                logger.warn("Execution error of a BlueGigaHandlerListener listener.", ex);
            }
        }
    }

    interface BluetoothListener<T extends BlueGigaResponse> {
        boolean transactionEvent(BlueGigaResponse response);

        boolean transactionTimeout(Integer transactionId);
    }
}
