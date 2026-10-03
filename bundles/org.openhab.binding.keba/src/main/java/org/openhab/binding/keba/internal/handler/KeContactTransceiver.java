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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.keba.internal.KebaBindingConstants;
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
    private @Nullable Selector selector;
    private @Nullable Thread transceiverThread;
    private boolean isStarted = false;
    private Set<KeContactHandler> handlers = Objects.requireNonNull(Collections.synchronizedSet(new HashSet<>()));
    private Map<KeContactHandler, @Nullable DatagramChannel> datagramChannels = Objects
            .requireNonNull(Collections.synchronizedMap(new HashMap<>()));
    private Map<KeContactHandler, @Nullable ByteBuffer> buffers = Objects
            .requireNonNull(Collections.synchronizedMap(new HashMap<>()));
    private Map<KeContactHandler, @Nullable ReentrantLock> locks = Objects
            .requireNonNull(Collections.synchronizedMap(new HashMap<>()));
    private Map<KeContactHandler, @Nullable Condition> conditions = Objects
            .requireNonNull(Collections.synchronizedMap(new HashMap<>()));
    private Map<KeContactHandler, @Nullable Boolean> flags = Objects
            .requireNonNull(Collections.synchronizedMap(new HashMap<>()));
    private final Object lifecycleLock = new Object();

    private final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(KeContactTransceiver.class));

    public void start() {
        synchronized (lifecycleLock) {
            if (isStarted) {
                return;
            }

            Selector localSelector;
            try {
                localSelector = Objects.requireNonNull(Selector.open());
            } catch (IOException | RuntimeException e) {
                logger.error("An exception occurred while opening the selector: {}", e.getMessage(), e);
                return;
            }

            DatagramChannel localBroadcastChannel = null;
            Thread localTransceiverThread = null;
            try {
                DatagramChannel openedBroadcastChannel = DatagramChannel.open();
                localBroadcastChannel = openedBroadcastChannel;
                openedBroadcastChannel.socket().bind(new InetSocketAddress(LISTENER_PORT_NUMBER));
                openedBroadcastChannel.configureBlocking(false);
                SelectionKey localBroadcastKey = Objects.requireNonNull(
                        openedBroadcastChannel.register(localSelector, openedBroadcastChannel.validOps()));

                logger.info("Listening for incoming data on {}", openedBroadcastChannel.getLocalAddress());

                for (KeContactHandler listener : handlerSnapshot()) {
                    establishConnection(listener, localSelector);
                }

                localTransceiverThread = new Thread(
                        () -> transceiverRunnable(localSelector, openedBroadcastChannel, localBroadcastKey),
                        "OH-binding-" + KebaBindingConstants.BINDING_ID + "-Transceiver");
                localTransceiverThread.setDaemon(true);

                selector = localSelector;
                broadcastChannel = openedBroadcastChannel;
                transceiverThread = localTransceiverThread;
                isStarted = true;
                localTransceiverThread.start();
                logger.debug("Started the KEBA KeContact transceiver");
            } catch (IOException | RuntimeException e) {
                isStarted = false;
                selector = null;
                broadcastChannel = null;
                transceiverThread = null;
                if (localTransceiverThread != null) {
                    localTransceiverThread.interrupt();
                }
                for (KeContactHandler listener : handlerSnapshot()) {
                    removeConnection(listener, localSelector);
                }
                closeQuietly(localBroadcastChannel);
                closeQuietly(localSelector);
                logger.error("An exception occurred while starting the KEBA KeContact transceiver: {}", e.getMessage(),
                        e);
            }
        }
    }

    public void stop() {
        Thread localTransceiverThread;
        synchronized (lifecycleLock) {
            if (!isStarted) {
                return;
            }
            isStarted = false;
            localTransceiverThread = transceiverThread;
            Selector localSelector = selector;
            DatagramChannel localBroadcastChannel = broadcastChannel;
            transceiverThread = null;
            selector = null;
            broadcastChannel = null;

            if (localSelector != null) {
                for (KeContactHandler listener : handlerSnapshot()) {
                    cancelPendingSend(listener);
                    removeConnection(listener, localSelector);
                }
            } else {
                for (KeContactHandler listener : handlerSnapshot()) {
                    cancelPendingSend(listener);
                    closeQuietly(datagramChannels.remove(listener));
                }
            }
            locks.clear();
            conditions.clear();
            flags.clear();

            if (localTransceiverThread != null) {
                localTransceiverThread.interrupt();
            }
            if (localSelector != null) {
                localSelector.wakeup();
            }
            closeQuietly(localBroadcastChannel);
            closeQuietly(localSelector);
        }

        if (localTransceiverThread != null && !localTransceiverThread.equals(Thread.currentThread())) {
            try {
                localTransceiverThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        logger.debug("Stopped the KEBA KeContact transceiver");
    }

    private void reset() {
        stop();
        start();
    }

    public void registerHandler(KeContactHandler handler) {
        synchronized (lifecycleLock) {
            if (!handlers.add(handler)) {
                return;
            }
            ReentrantLock handlerLock = new ReentrantLock();
            locks.put(handler, handlerLock);
            conditions.put(handler, handlerLock.newCondition());

            if (logger.isTraceEnabled()) {
                logger.trace("There are now {} KEBA KeContact handlers registered with the transceiver",
                        handlers.size());
            }

            if (!isStarted) {
                start();
            } else {
                Selector localSelector = selector;
                if (localSelector != null && !isConnected(handler)) {
                    establishConnection(handler, localSelector);
                }
            }
        }
    }

    public void unRegisterHandler(KeContactHandler handler) {
        boolean stopWhenEmpty;
        synchronized (lifecycleLock) {
            cancelPendingSend(handler);
            locks.remove(handler);
            conditions.remove(handler);
            buffers.remove(handler);
            flags.remove(handler);
            handlers.remove(handler);

            if (logger.isTraceEnabled()) {
                logger.trace("There are now {} KEBA KeContact handlers registered with the transceiver",
                        handlers.size());
            }

            Selector localSelector = selector;
            if (localSelector != null) {
                removeConnection(handler, localSelector);
            }
            stopWhenEmpty = handlers.isEmpty() && isStarted;
        }
        if (stopWhenEmpty) {
            stop();
        }
    }

    private Set<KeContactHandler> handlerSnapshot() {
        synchronized (handlers) {
            return new HashSet<>(handlers);
        }
    }

    private void closeQuietly(@Nullable DatagramChannel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                logger.debug("Failed to close KEBA broadcast channel: {}", e.getMessage());
            }
        }
    }

    private void closeQuietly(@Nullable Selector selector) {
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                logger.debug("Failed to close KEBA selector: {}", e.getMessage());
            }
        }
    }

    private void cancelPendingSend(KeContactHandler handler) {
        ReentrantLock handlerLock = locks.get(handler);
        Condition responseCondition = conditions.get(handler);
        if (handlerLock != null && responseCondition != null) {
            handlerLock.lock();
            try {
                buffers.remove(handler);
                flags.remove(handler);
                responseCondition.signalAll();
            } finally {
                handlerLock.unlock();
            }
        }
    }

    protected @Nullable ByteBuffer send(String message, KeContactHandler handler) {
        ReentrantLock handlerLock = locks.get(handler);
        Condition responseCondition = conditions.get(handler);

        if (handlerLock != null && responseCondition != null) {
            handlerLock.lock();
            try {
                if (!handlerLock.equals(locks.get(handler)) || !responseCondition.equals(conditions.get(handler))
                        || !handlers.contains(handler)) {
                    return null;
                }
                byte[] messageBytes = message.getBytes(StandardCharsets.US_ASCII);
                ByteBuffer buffer = ByteBuffer.allocate(messageBytes.length);
                buffer.put(messageBytes);

                flags.put(handler, Objects.requireNonNull(Boolean.TRUE));
                buffers.put(handler, buffer);

                if (logger.isTraceEnabled()) {
                    logger.trace("{} waiting on handler condition {}", Thread.currentThread().getName(),
                            responseCondition);
                }
                long remainingNanos = TimeUnit.MILLISECONDS.toNanos(KeContactHandler.REPORT_INTERVAL);
                while (buffers.get(handler) == buffer && remainingNanos > 0) {
                    remainingNanos = responseCondition.awaitNanos(remainingNanos);
                }

                return buffers.remove(handler);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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

    private void transceiverRunnable(Selector localSelector, DatagramChannel localBroadcastChannel,
            SelectionKey localBroadcastKey) {
        while (true) {
            try {
                synchronized (localSelector) {
                    try {
                        localSelector.selectNow();
                    } catch (IOException e) {
                        logger.error("An exception occurred while selecting: {}", e.getMessage());
                    }

                    var it = localSelector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey selKey = it.next();
                        it.remove();

                        if (selKey.isValid() && selKey.isWritable()) {
                            DatagramChannel theChannel = (DatagramChannel) selKey.channel();
                            KeContactHandler theHandler = null;
                            boolean error = false;

                            for (KeContactHandler handler : handlerSnapshot()) {
                                if (theChannel.equals(datagramChannels.get(handler))) {
                                    theHandler = handler;
                                    break;
                                }
                            }

                            if (theHandler != null) {
                                ReentrantLock theLock = locks.get(theHandler);
                                Boolean theFlag = flags.get(theHandler);
                                if (theLock != null && theLock.isLocked() && theFlag != null
                                        && theFlag.equals(Boolean.TRUE)) {
                                    ByteBuffer theBuffer = buffers.remove(theHandler);
                                    flags.put(theHandler, Objects.requireNonNull(Boolean.FALSE));

                                    if (theBuffer != null) {
                                        try {
                                            theBuffer.rewind();
                                            logger.debug("Sending '{}' on the channel '{}'->'{}'", new Object[] {
                                                    new String(theBuffer.array(), StandardCharsets.US_ASCII),
                                                    theChannel.getLocalAddress(), theChannel.getRemoteAddress() });
                                            theChannel.write(theBuffer);
                                        } catch (NotYetConnectedException e) {
                                            theHandler.updateStatus(ThingStatus.OFFLINE,
                                                    ThingStatusDetail.COMMUNICATION_ERROR,
                                                    "The remote host is not yet connected");
                                            error = true;
                                        } catch (ClosedChannelException e) {
                                            theHandler.updateStatus(ThingStatus.OFFLINE,
                                                    ThingStatusDetail.COMMUNICATION_ERROR,
                                                    "The connection to the remote host is closed");
                                            error = true;
                                        } catch (IOException e) {
                                            theHandler.updateStatus(ThingStatus.OFFLINE,
                                                    ThingStatusDetail.COMMUNICATION_ERROR, "An IO exception occurred");
                                            error = true;
                                        }

                                        if (error) {
                                            removeConnection(theHandler, localSelector);
                                            establishConnection(theHandler, localSelector);
                                        }
                                    }
                                }
                            }
                        }

                        if (selKey.isValid() && selKey.isReadable()) {
                            int numberBytesRead = 0;
                            boolean error = false;

                            if (selKey.equals(localBroadcastKey)) {
                                ByteBuffer broadcastBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                                InetSocketAddress clientAddress = null;
                                try {
                                    clientAddress = (InetSocketAddress) localBroadcastChannel.receive(broadcastBuffer);
                                    logger.debug("Received {} from {} on the transceiver listener port ",
                                            new String(broadcastBuffer.array(), StandardCharsets.US_ASCII),
                                            clientAddress);
                                    numberBytesRead = broadcastBuffer.position();
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
                                    broadcastBuffer.flip();
                                    if (broadcastBuffer.remaining() > 0) {
                                        for (KeContactHandler handler : handlerSnapshot()) {
                                            if (clientAddress != null && handler.getIPAddress()
                                                    .equals(clientAddress.getAddress().getHostAddress())) {
                                                ReentrantLock theLock = locks.get(handler);
                                                if (theLock != null && theLock.isLocked()) {
                                                    buffers.put(handler, broadcastBuffer);
                                                    Condition responseCondition = conditions.get(handler);
                                                    if (responseCondition != null) {
                                                        theLock.lock();
                                                        try {
                                                            responseCondition.signalAll();
                                                        } finally {
                                                            theLock.unlock();
                                                        }
                                                    }
                                                } else {
                                                    handler.onData(broadcastBuffer);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    handlerSnapshot().forEach(listener -> listener.updateStatus(ThingStatus.OFFLINE,
                                            ThingStatusDetail.COMMUNICATION_ERROR, "The transceiver is offline"));
                                    reset();
                                    return;
                                }
                            } else {
                                DatagramChannel theChannel = (DatagramChannel) selKey.channel();
                                KeContactHandler theHandler = null;

                                for (KeContactHandler handler : handlerSnapshot()) {
                                    DatagramChannel registeredChannel = datagramChannels.get(handler);
                                    if (theChannel.equals(registeredChannel)) {
                                        theHandler = handler;
                                        break;
                                    }
                                }

                                if (theHandler != null) {
                                    ByteBuffer channelBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                                    try {
                                        numberBytesRead = theChannel.read(channelBuffer);
                                        logger.debug("Received {} from {} on the transceiver listener port ",
                                                new String(channelBuffer.array(), StandardCharsets.US_ASCII),
                                                theChannel.getRemoteAddress());
                                    } catch (NotYetConnectedException e) {
                                        theHandler.updateStatus(ThingStatus.OFFLINE,
                                                ThingStatusDetail.COMMUNICATION_ERROR,
                                                "The remote host is not yet connected");
                                        error = true;
                                    } catch (PortUnreachableException e) {
                                        theHandler.updateStatus(ThingStatus.OFFLINE,
                                                ThingStatusDetail.CONFIGURATION_ERROR,
                                                "The remote host is probably not a KEBA KeContact");
                                        error = true;
                                    } catch (IOException e) {
                                        theHandler.updateStatus(ThingStatus.OFFLINE,
                                                ThingStatusDetail.COMMUNICATION_ERROR, "An IO exception occurred");
                                        error = true;
                                    }

                                    if (numberBytesRead == -1) {
                                        error = true;
                                    }

                                    if (!error) {
                                        channelBuffer.flip();
                                        if (channelBuffer.remaining() > 0) {
                                            ReentrantLock theLock = locks.get(theHandler);
                                            if (theLock != null && theLock.isLocked()) {
                                                buffers.put(theHandler, channelBuffer);
                                                Condition responseCondition = conditions.get(theHandler);
                                                if (responseCondition != null) {
                                                    theLock.lock();
                                                    try {
                                                        responseCondition.signalAll();
                                                    } finally {
                                                        theLock.unlock();
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        removeConnection(theHandler, localSelector);
                                        establishConnection(theHandler, localSelector);
                                    }
                                }
                            }
                        }
                    }
                }

                if (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(LISTENING_INTERVAL);
                } else {
                    return;
                }
            } catch (InterruptedException | ClosedSelectorException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void establishConnection(KeContactHandler handler, Selector localSelector) {
        String ipAddress = handler.getIPAddress();
        if (handler.getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR
                && !"".equals(ipAddress)) {
            logger.debug("Establishing the connection to the KEBA KeContact '{}'", handler.getThing().getUID());

            DatagramChannel datagramChannel = null;
            try {
                datagramChannel = DatagramChannel.open();
            } catch (Exception e2) {
                handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "An exception occurred while opening a datagram channel");
            }

            if (datagramChannel != null) {
                datagramChannels.put(handler, datagramChannel);

                try {
                    datagramChannel.configureBlocking(false);
                } catch (IOException e2) {
                    handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "An exception occurred while configuring a datagram channel");
                }

                localSelector.wakeup();
                int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
                try {
                    datagramChannel.register(localSelector, interestSet);
                } catch (ClosedChannelException e1) {
                    handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "An exception occurred while registering a selector");
                }

                InetSocketAddress remoteAddress = new InetSocketAddress(ipAddress, LISTENER_PORT_NUMBER);

                try {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Connecting the channel for {} ", remoteAddress);
                    }
                    datagramChannel.connect(remoteAddress);

                    handler.updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "");
                } catch (Exception e) {
                    logger.debug("An exception occurred while connecting connecting to '{}:{}' : {}",
                            new Object[] { ipAddress, LISTENER_PORT_NUMBER, e.getMessage() });
                    handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "An exception occurred while connecting");
                }
            }
        } else {
            handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    handler.getThing().getStatusInfo().getDescription());
        }
    }

    private void removeConnection(KeContactHandler handler, Selector localSelector) {
        logger.debug("Tearing down the connection to the KEBA KeContact '{}'", handler.getThing().getUID());
        DatagramChannel datagramChannel = datagramChannels.remove(handler);

        if (datagramChannel != null) {
            try {
                SelectionKey key = datagramChannel.keyFor(localSelector);
                if (key != null) {
                    key.cancel();
                }
                datagramChannel.close();
                handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "");
            } catch (Exception e) {
                logger.debug("An exception occurred while closing the datagramchannel for '{}': {}",
                        handler.getThing().getUID(), e.getMessage());
                handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "An exception occurred while closing the datagramchannel");
            }
        }
    }

    private boolean isConnected(KeContactHandler handler) {
        return datagramChannels.get(handler) != null ? true : false;
    }
}
