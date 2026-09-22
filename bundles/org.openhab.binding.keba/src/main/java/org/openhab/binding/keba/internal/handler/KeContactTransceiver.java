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
package org.openhab.binding.keba.internal.handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KeContactTransceiver} is responsible for receiving UDP broadcast messages sent by the KEBA Charging
 * Stations. {@link KeContactHandler} willing to receive these messages have to register themselves with the
 * {@link KeContactTransceiver}
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class KeContactTransceiver {

    public static final int LISTENER_PORT_NUMBER = 7090;
    public static final int LISTENING_INTERVAL = 100;
    public static final int BUFFER_SIZE = 1024;

    private @Nullable DatagramChannel broadcastChannel;
    private @Nullable SelectionKey broadcastKey;
    private @Nullable Selector selector;
    private @Nullable Future<?> transceiverTask;
    private boolean isStarted = false;
    private Set<KeContactHandler> handlers = Collections.synchronizedSet(new HashSet<>());
    private Map<KeContactHandler, DatagramChannel> datagramChannels = Collections.synchronizedMap(new HashMap<>());
    private Map<KeContactHandler, ByteBuffer> buffers = Collections.synchronizedMap(new HashMap<>());
    private Map<KeContactHandler, ReentrantLock> locks = Collections.synchronizedMap(new HashMap<>());
    private Map<KeContactHandler, Boolean> flags = Collections.synchronizedMap(new HashMap<>());
    private Map<KeContactHandler, Boolean> awaitingResponses = Collections.synchronizedMap(new HashMap<>());
    private Map<KeContactHandler, Condition> responseConditions = Collections.synchronizedMap(new HashMap<>());
    private final java.util.concurrent.ScheduledExecutorService transceiverScheduler = ThreadPoolManager
            .getScheduledPool("keba");

    private final Logger logger = LoggerFactory.getLogger(KeContactTransceiver.class);

    public void start() {
        startTransceiver();
    }

    private void startTransceiver() {
        if (!isStarted) {
            logger.debug("Starting the the KEBA KeContact transceiver");
            try {
                selector = Selector.open();

                if (transceiverTask == null) {
                    transceiverTask = transceiverScheduler.submit(transceiverRunnable);
                }

                broadcastChannel = DatagramChannel.open();
                broadcastChannel.socket().bind(new InetSocketAddress(LISTENER_PORT_NUMBER));
                broadcastChannel.configureBlocking(false);

                logger.info("Listening for incoming data on {}", broadcastChannel.getLocalAddress());

                synchronized (selector) {
                    selector.wakeup();
                    broadcastKey = broadcastChannel.register(selector, broadcastChannel.validOps());
                }

                for (KeContactHandler listener : snapshotHandlers()) {
                    establishConnection(listener);
                }

                isStarted = true;
            } catch (ClosedSelectorException | CancelledKeyException | IOException e) {
                logger.error("An exception occurred while registering the selector: {}", e.getMessage());
            }
        }
    }

    public void stop() {
        if (isStarted) {
            for (KeContactHandler listener : snapshotHandlers()) {
                this.removeConnection(listener);
            }

            DatagramChannel localBroadcastChannel = broadcastChannel;
            if (localBroadcastChannel != null) {
                try {
                    localBroadcastChannel.close();
                } catch (IOException e) {
                    logger.error("An exception occurred while closing the broadcast channel on port number {} : '{}'",
                            LISTENER_PORT_NUMBER, e.getMessage(), e);
                }
            }

            Selector localSelector = selector;
            if (localSelector != null) {
                try {
                    localSelector.close();
                } catch (IOException e) {
                    logger.error("An exception occurred while closing the selector: '{}'", e.getMessage(), e);
                }
            }

            logger.debug("Stopping the the KEBA KeContact transceiver");
            Future<?> localTransceiverTask = transceiverTask;
            if (localTransceiverTask != null) {
                localTransceiverTask.cancel(true);
                transceiverTask = null;
            }

            locks.clear();
            flags.clear();
            awaitingResponses.clear();
            responseConditions.clear();

            isStarted = false;
        }
    }

    private void reset() {
        stop();
        isStarted = false;
        start();
    }

    public void registerHandler(KeContactHandler handler) {
        if (handler != null) {
            handlers.add(handler);
            ReentrantLock handlerLock = new ReentrantLock();
            locks.put(handler, handlerLock);
            flags.put(handler, Boolean.FALSE);
            awaitingResponses.put(handler, Boolean.FALSE);
            responseConditions.put(handler, handlerLock.newCondition());

            if (logger.isTraceEnabled()) {
                logger.trace("There are now {} KEBA KeContact handlers registered with the transceiver",
                        handlers.size());
            }

            if (handlers.size() == 1) {
                startTransceiver();
            }

            if (!isConnected(handler)) {
                establishConnection(handler);
            }
        }
    }

    public void unRegisterHandler(KeContactHandler handler) {
        if (handler != null) {
            ReentrantLock handlerLock = locks.get(handler);
            Condition responseCondition = responseConditions.get(handler);
            if (handlerLock != null && responseCondition != null) {
                handlerLock.lock();
                try {
                    flags.put(handler, Boolean.FALSE);
                    awaitingResponses.put(handler, Boolean.FALSE);
                    buffers.remove(handler);
                    responseCondition.signalAll();
                } finally {
                    handlerLock.unlock();
                }
            }
            locks.remove(handler);
            responseConditions.remove(handler);
            handlers.remove(handler);

            if (logger.isTraceEnabled()) {
                logger.trace("There are now {} KEBA KeContact handlers registered with the transceiver",
                        handlers.size());
            }

            if (handlers.isEmpty()) {
                stop();
            }
        }
    }

    protected @Nullable ByteBuffer send(String message, KeContactHandler handler) {
        ReentrantLock handlerLock = locks.get(handler);
        Condition responseCondition = responseConditions.get(handler);

        if (handlerLock != null && responseCondition != null) {
            handlerLock.lock();
            try {
                byte[] messageBytes = message.getBytes(StandardCharsets.US_ASCII);
                ByteBuffer buffer = ByteBuffer.allocate(messageBytes.length);
                buffer.put(messageBytes);

                flags.put(handler, Boolean.TRUE);
                awaitingResponses.put(handler, Boolean.TRUE);
                buffers.put(handler, buffer);

                if (logger.isTraceEnabled()) {
                    logger.trace("{} waiting for a response from '{}'", Thread.currentThread().getName(),
                            handler.getThing().getUID());
                }
                responseCondition.await(KeContactHandler.REPORT_INTERVAL, TimeUnit.MILLISECONDS);
                awaitingResponses.put(handler, Boolean.FALSE);
                return buffers.remove(handler);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                String interruptionMessage = e.getMessage();
                handler.updateStatusFromTransceiver(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        interruptionMessage != null ? interruptionMessage : "Transceiver operation interrupted");
            } finally {
                handlerLock.unlock();
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("The handler for '{}' is not yet registered with the KeContactTransceiver",
                        handler.getThing().getUID());
            }
        }
        return null;
    }

    public Runnable transceiverRunnable = () -> {
        while (true) {
            try {
                synchronized (selector) {
                    try {
                        selector.select(LISTENING_INTERVAL);
                    } catch (IOException e) {
                        logger.error("An exception occurred while selecting: {}", e.getMessage());
                    }

                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey selKey = it.next();
                        it.remove();

                        if (selKey.isValid() && selKey.isWritable()) {
                            DatagramChannel theChannel = (DatagramChannel) selKey.channel();
                            KeContactHandler theHandler = null;
                            boolean error = false;

                            for (KeContactHandler handler : snapshotHandlers()) {
                                if (theChannel.equals(datagramChannels.get(handler))) {
                                    theHandler = handler;
                                    break;
                                }
                            }

                            if (theHandler != null) {
                                ReentrantLock theLock = locks.get(theHandler);
                                Boolean theFlag = flags.get(theHandler);
                                if (theLock != null && theFlag != null && theFlag.equals(Boolean.TRUE)) {
                                    ByteBuffer theBuffer = buffers.remove(theHandler);
                                    flags.put(theHandler, Boolean.FALSE);

                                    if (theBuffer != null) {
                                        try {
                                            theBuffer.rewind();
                                            logger.debug("Sending '{}' on the channel '{}'->'{}'",
                                                    new Object[] { new String(theBuffer.array()),
                                                            theChannel.getLocalAddress(),
                                                            theChannel.getRemoteAddress() });
                                            theChannel.write(theBuffer);
                                        } catch (NotYetConnectedException e) {
                                            theHandler.updateStatusFromTransceiver(ThingStatus.OFFLINE,
                                                    ThingStatusDetail.COMMUNICATION_ERROR,
                                                    "The remote host is not yet connected");
                                            error = true;
                                        } catch (ClosedChannelException e) {
                                            theHandler.updateStatusFromTransceiver(ThingStatus.OFFLINE,
                                                    ThingStatusDetail.COMMUNICATION_ERROR,
                                                    "The connection to the remote host is closed");
                                            error = true;
                                        } catch (IOException e) {
                                            theHandler.updateStatusFromTransceiver(ThingStatus.OFFLINE,
                                                    ThingStatusDetail.COMMUNICATION_ERROR, "An IO exception occurred");
                                            error = true;
                                        }

                                        if (error) {
                                            removeConnection(theHandler);
                                            establishConnection(theHandler);
                                        }
                                    }
                                }
                            }
                        }

                        if (selKey.isValid() && selKey.isReadable()) {
                            int numberBytesRead = 0;
                            InetSocketAddress clientAddress = null;
                            ByteBuffer readBuffer = null;
                            boolean error = false;

                            if (selKey.equals(broadcastKey)) {
                                try {
                                    readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                                    clientAddress = (InetSocketAddress) broadcastChannel.receive(readBuffer);
                                    logger.debug("Received {} from {} on the transceiver listener port ",
                                            new String(readBuffer.array()), clientAddress);
                                    numberBytesRead = readBuffer.position();
                                } catch (IOException e) {
                                    logger.error(
                                            "An exception occurred while receiving data on the transceiver listener port: '{}'",
                                            e.getMessage(), e);
                                    error = true;
                                }

                                if (numberBytesRead == -1) {
                                    error = true;
                                }

                                if (!error) {
                                    readBuffer.flip();
                                    if (readBuffer.remaining() > 0) {
                                        for (KeContactHandler handler : snapshotHandlers()) {
                                            if (clientAddress != null && handler.getIPAddress()
                                                    .equals(clientAddress.getAddress().getHostAddress())) {
                                                ReentrantLock theLock = locks.get(handler);
                                                if (theLock != null
                                                        && Boolean.TRUE.equals(awaitingResponses.get(handler))) {
                                                    buffers.put(handler, copyBuffer(readBuffer));
                                                    awaitingResponses.put(handler, Boolean.FALSE);
                                                    signalResponse(handler);
                                                } else {
                                                    handler.onData(copyBuffer(readBuffer));
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    snapshotHandlers().forEach(listener -> listener.updateStatusFromTransceiver(
                                            ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                            "The transceiver is offline"));
                                    reset();
                                }
                            } else {
                                DatagramChannel theChannel = (DatagramChannel) selKey.channel();
                                KeContactHandler theHandler = null;

                                for (KeContactHandler handlers : snapshotHandlers()) {
                                    if (datagramChannels.get(handlers).equals(theChannel)) {
                                        theHandler = handlers;
                                        break;
                                    }
                                }

                                if (theHandler != null) {
                                    try {
                                        readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                                        numberBytesRead = theChannel.read(readBuffer);
                                        logger.debug("Received {} from {} on the transceiver listener port ",
                                                new String(readBuffer.array()), theChannel.getRemoteAddress());
                                    } catch (NotYetConnectedException e) {
                                        theHandler.updateStatusFromTransceiver(ThingStatus.OFFLINE,
                                                ThingStatusDetail.COMMUNICATION_ERROR,
                                                "The remote host is not yet connected");
                                        error = true;
                                    } catch (PortUnreachableException e) {
                                        theHandler.updateStatusFromTransceiver(ThingStatus.OFFLINE,
                                                ThingStatusDetail.CONFIGURATION_ERROR,
                                                "The remote host is probably not a KEBA KeContact");
                                        error = true;
                                    } catch (IOException e) {
                                        theHandler.updateStatusFromTransceiver(ThingStatus.OFFLINE,
                                                ThingStatusDetail.COMMUNICATION_ERROR, "An IO exception occurred");
                                        error = true;
                                    }

                                    if (numberBytesRead == -1) {
                                        error = true;
                                    }

                                    if (!error) {
                                        readBuffer.flip();
                                        if (readBuffer.remaining() > 0) {
                                            ReentrantLock theLock = locks.get(theHandler);
                                            if (theLock != null
                                                    && Boolean.TRUE.equals(awaitingResponses.get(theHandler))) {
                                                buffers.put(theHandler, copyBuffer(readBuffer));
                                                awaitingResponses.put(theHandler, Boolean.FALSE);
                                                signalResponse(theHandler);
                                            }
                                        }
                                    } else {
                                        removeConnection(theHandler);
                                        establishConnection(theHandler);
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (ClosedSelectorException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    };

    private void signalResponse(KeContactHandler handler) {
        ReentrantLock handlerLock = locks.get(handler);
        Condition responseCondition = responseConditions.get(handler);
        if (handlerLock != null && responseCondition != null) {
            handlerLock.lock();
            try {
                responseCondition.signalAll();
            } finally {
                handlerLock.unlock();
            }
        }
    }

    private Set<KeContactHandler> snapshotHandlers() {
        synchronized (handlers) {
            return new HashSet<>(handlers);
        }
    }

    private static ByteBuffer copyBuffer(ByteBuffer source) {
        ByteBuffer copy = ByteBuffer.allocate(source.remaining());
        copy.put(source.duplicate());
        copy.flip();
        return copy;
    }

    private void establishConnection(KeContactHandler handler) {
        String ipAddress = handler.getIPAddress();
        if (handler.getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR
                && !"".equals(ipAddress)) {
            logger.debug("Establishing the connection to the KEBA KeContact '{}'", handler.getThing().getUID());

            DatagramChannel datagramChannel = null;
            try {
                datagramChannel = DatagramChannel.open();
            } catch (Exception e2) {
                handler.updateStatusFromTransceiver(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "An exception occurred while opening a datagram channel");
            }

            if (datagramChannel != null) {
                datagramChannels.put(handler, datagramChannel);

                try {
                    datagramChannel.configureBlocking(false);
                } catch (IOException e2) {
                    handler.updateStatusFromTransceiver(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "An exception occurred while configuring a datagram channel");
                }

                synchronized (selector) {
                    selector.wakeup();
                    int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
                    try {
                        datagramChannel.register(selector, interestSet);
                    } catch (ClosedChannelException e1) {
                        handler.updateStatusFromTransceiver(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "An exception occurred while registering a selector");
                    }

                    InetSocketAddress remoteAddress = new InetSocketAddress(ipAddress, LISTENER_PORT_NUMBER);

                    try {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Connecting the channel for {} ", remoteAddress);
                        }
                        datagramChannel.connect(remoteAddress);

                        handler.updateStatusFromTransceiver(ThingStatus.ONLINE, ThingStatusDetail.NONE, "");
                    } catch (Exception e) {
                        logger.debug("An exception occurred while connecting connecting to '{}:{}' : {}",
                                new Object[] { ipAddress, LISTENER_PORT_NUMBER, e.getMessage() });
                        handler.updateStatusFromTransceiver(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "An exception occurred while connecting");
                    }
                }
            }
        } else {
            String statusDescription = handler.getThing().getStatusInfo().getDescription();
            handler.updateStatusFromTransceiver(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    statusDescription != null ? statusDescription : "Invalid handler configuration");
        }
    }

    private void removeConnection(KeContactHandler handler) {
        logger.debug("Tearing down the connection to the KEBA KeContact '{}'", handler.getThing().getUID());
        DatagramChannel datagramChannel = datagramChannels.remove(handler);

        if (datagramChannel != null) {
            synchronized (selector) {
                try {
                    datagramChannel.keyFor(selector).cancel();
                    datagramChannel.close();
                    handler.updateStatusFromTransceiver(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "");
                } catch (Exception e) {
                    logger.debug("An exception occurred while closing the datagramchannel for '{}': {}",
                            handler.getThing().getUID(), e.getMessage());
                    handler.updateStatusFromTransceiver(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "An exception occurred while closing the datagramchannel");
                }
            }
        }
    }

    private boolean isConnected(KeContactHandler handler) {
        return datagramChannels.get(handler) != null ? true : false;
    }
}
