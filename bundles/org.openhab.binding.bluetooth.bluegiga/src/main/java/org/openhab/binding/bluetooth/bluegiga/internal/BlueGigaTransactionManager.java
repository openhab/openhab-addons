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
package org.openhab.binding.bluetooth.bluegiga.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides transaction management and queuing of {@link BlueGigaCommand} frames.
 *
 * @author Pauli Anttila - Initial contribution
 *
 */
@NonNullByDefault
public class BlueGigaTransactionManager implements BlueGigaSerialEventListener {

    private static final int TRANSACTION_TIMEOUT_PERIOD_MS = 100;

    private final Logger logger = LoggerFactory.getLogger(BlueGigaTransactionManager.class);

    /**
     * Unique transaction id for request and response correlation
     */
    private AtomicInteger transactionId = new AtomicInteger();

    /**
     * Ongoing transaction id. If not present, no ongoing transaction.
     */
    private volatile Optional<Integer> ongoingTransactionId = Optional.empty();

    /**
     * Transaction listeners are used internally to correlate the commands and responses
     */
    private final List<BluetoothListener<? extends BlueGigaResponse>> transactionListeners = new CopyOnWriteArrayList<>();

    /**
     * The event listeners will be notified of any asynchronous events
     */
    private final Set<BlueGigaEventListener> eventListeners = new CopyOnWriteArraySet<>();

    private final Queue<BlueGigaUniqueCommand> sendQueue = new LinkedList<>();
    private final ScheduledExecutorService executor;
    private final BlueGigaSerialHandler serialHandler;

    private @Nullable Future<?> transactionTimeoutTimer;

    /**
     * Internal interface for transaction listeners.
     */
    interface BluetoothListener<T extends BlueGigaResponse> {
        boolean transactionEvent(BlueGigaResponse response, int transactionId);

        boolean transactionTimeout(int transactionId);
    }

    public BlueGigaTransactionManager(BlueGigaSerialHandler serialHandler, ScheduledExecutorService executor) {
        this.serialHandler = serialHandler;
        this.executor = executor;
        serialHandler.addEventListener(this);
    }

    /**
     * Close transaction manager.
     */
    public void close() {
        serialHandler.removeEventListener(this);
        cancelTransactionTimer();
        sendQueue.clear();
        transactionListeners.clear();
        eventListeners.clear();
        logger.debug("Closed");
    }

    private void startTransactionTimer() {
        transactionTimeoutTimer = executor.schedule(() -> {
            notifyTransactionTimeout(ongoingTransactionId);
        }, TRANSACTION_TIMEOUT_PERIOD_MS, TimeUnit.MILLISECONDS);
    }

    private void cancelTransactionTimer() {
        if (transactionTimeoutTimer != null) {
            transactionTimeoutTimer.cancel(true);
            transactionTimeoutTimer = null;
        }
    }

    private void sendNextFrame() {
        getNextFrame().ifPresent(frame -> {
            cancelTransactionTimer();
            logger.debug("Send frame #{}: {}", frame.getTransactionId(), frame.getMessage());
            ongoingTransactionId = Optional.of(frame.getTransactionId());
            serialHandler.sendFrame(frame.getMessage());
            startTransactionTimer();
        });
    }

    @SuppressWarnings({ "null", "unused" })
    private Optional<BlueGigaUniqueCommand> getNextFrame() {
        while (!sendQueue.isEmpty()) {
            BlueGigaUniqueCommand frame = sendQueue.poll();
            if (frame != null) {
                if (frame.getMessage() != null) {
                    return Optional.of(frame);
                } else {
                    logger.debug("Null message found from queue, skip it");
                    continue;
                }
            } else {
                logger.debug("Null frame found from queue, skip it");
                continue;
            }
        }
        return Optional.empty();
    }

    /**
     * Add a {@link BlueGigaUniqueCommand} frame to the send queue. The sendQueue is a
     * FIFO queue. This method queues a {@link BlueGigaCommand} frame without
     * waiting for a response.
     *
     * @param transaction
     *            {@link BlueGigaUniqueCommand}
     */
    public void queueFrame(BlueGigaUniqueCommand request) {
        logger.trace("Queue TX BLE frame: {}", request);
        sendQueue.add(request);
        logger.trace("TX BLE queue size: {}", sendQueue.size());
    }

    private void sendNextTransactionIfNoOngoing() {
        synchronized (this) {
            logger.trace("Send next transaction if no ongoing");
            if (!ongoingTransactionId.isPresent()) {
                sendNextFrame();
            }
        }
    }

    private void clearOngoingTransactionAndSendNext() {
        synchronized (this) {
            logger.trace("Clear ongoing transaction and send next frame from queue");
            ongoingTransactionId = Optional.empty();
            sendNextFrame();
        }
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
        class TransactionWaiter implements Callable<T>, BluetoothListener<T> {
            private volatile boolean complete;
            private Optional<BlueGigaResponse> response = Optional.empty();
            private BlueGigaUniqueCommand query = new BlueGigaUniqueCommand(bleCommand,
                    transactionId.getAndIncrement());

            @SuppressWarnings("unchecked")
            @Override
            public T call() throws TimeoutException {
                // Register a listener
                addTransactionListener(this);

                // Send the transaction
                queueFrame(query);
                sendNextTransactionIfNoOngoing();

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
                executor.submit(BlueGigaTransactionManager.this::clearOngoingTransactionAndSendNext);

                if (response.isPresent()) {
                    return (T) response.get();
                } else {
                    throw new TimeoutException("No response from BlueGiga controller");
                }
            }

            @Override
            public boolean transactionEvent(BlueGigaResponse bleResponse, int transactionId) {
                logger.trace("Expected transactionId: {}, received transactionId: {}", query.getTransactionId(),
                        transactionId);

                if (transactionId != query.getTransactionId()) {
                    logger.trace("Ignore frame as received transaction Id {} doesn't match expected transaction Id {}.",
                            transactionId, query.getTransactionId());
                    return false;
                }

                logger.trace("Expected frame: {}, received frame: {}", expected.getSimpleName(), bleResponse);

                if (bleCommand instanceof BlueGigaDeviceCommand && bleResponse instanceof BlueGigaDeviceResponse) {
                    BlueGigaDeviceCommand devCommand = (BlueGigaDeviceCommand) bleCommand;
                    BlueGigaDeviceResponse devResponse = (BlueGigaDeviceResponse) bleResponse;

                    logger.trace("Expected connection id: {}, received connection id: {}", devCommand.getConnection(),
                            devResponse.getConnection());

                    if (devCommand.getConnection() != devResponse.getConnection()) {
                        logger.trace("Ignore response as received connection id {} doesn't match expected id {}.",
                                devResponse.getConnection(), devCommand.getConnection());
                        return false;
                    }
                }

                if (!expected.isInstance(bleResponse)) {
                    logger.trace("Ignoring {} frame which has not been requested.",
                            bleResponse.getClass().getSimpleName());
                    return false;
                }

                // Response received, notify waiter
                response = Optional.of(bleResponse);
                complete = true;
                logger.debug("Received frame #{}: {}", transactionId, bleResponse);
                synchronized (this) {
                    notify();
                }
                return true;
            }

            @Override
            public boolean transactionTimeout(int transactionId) {
                if (transactionId != query.getTransactionId()) {
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
     * @throws BlueGigaException when any error occurred
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

    public void addEventListener(BlueGigaEventListener listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(BlueGigaEventListener listener) {
        eventListeners.remove(listener);
    }

    @Override
    public void bluegigaFrameReceived(BlueGigaResponse event) {
        if (event.isEvent()) {
            notifyEventListeners(event);
        } else {
            notifyTransactionComplete(event);
        }
    }

    /**
     * Notify any event listeners when we receive a response.
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

    /**
     * Notify any internal transaction listeners when we receive a response.
     *
     * @param response
     *            the response data received
     */
    private void notifyTransactionComplete(final BlueGigaResponse response) {
        ongoingTransactionId.ifPresent(id -> {
            boolean processed = false;
            for (BluetoothListener<? extends BlueGigaResponse> listener : transactionListeners) {
                if (listener.transactionEvent(response, id)) {
                    processed = true;
                }
            }
            if (!processed) {
                logger.debug("No listener found for received response: {}", response);
            }
        });
    }

    private void notifyTransactionTimeout(final Optional<Integer> transactionId) {
        transactionId.ifPresent(id -> {
            boolean processed = false;
            for (BluetoothListener<? extends BlueGigaResponse> listener : transactionListeners) {
                if (listener.transactionTimeout(id)) {
                    processed = true;
                }
            }
            if (!processed) {
                logger.debug("No listener found for transaction timeout event, transaction id {}", id);
            }
        });
    }
}
