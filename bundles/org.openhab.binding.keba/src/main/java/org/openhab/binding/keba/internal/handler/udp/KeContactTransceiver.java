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
package org.openhab.binding.keba.internal.handler.udp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
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
 * @author Michael Weger - UDP request lifecycle correction
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
    private Map<KeContactHandler, PendingRequest> requests = Objects
            .requireNonNull(Collections.synchronizedMap(new HashMap<>()));
    private Map<KeContactHandler, @Nullable ReentrantLock> locks = Objects
            .requireNonNull(Collections.synchronizedMap(new HashMap<>()));
    private Map<KeContactHandler, @Nullable Condition> conditions = Objects
            .requireNonNull(Collections.synchronizedMap(new HashMap<>()));
    private final Object lifecycleLock = new Object();
    private final int listenerPort;
    private final int remotePort;

    private final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(KeContactTransceiver.class));

    private static final class PendingRequest {
        final ByteBuffer outbound;
        boolean sent;
        boolean completed;
        @Nullable
        ByteBuffer reply;

        PendingRequest(ByteBuffer outbound) {
            this.outbound = outbound;
        }
    }

    public KeContactTransceiver() {
        this(LISTENER_PORT_NUMBER, LISTENER_PORT_NUMBER);
    }

    KeContactTransceiver(int listenerPort, int remotePort) {
        this.listenerPort = listenerPort;
        this.remotePort = remotePort;
    }

    int getLocalPort() {
        DatagramChannel channel = broadcastChannel;
        return channel == null ? -1 : channel.socket().getLocalPort();
    }

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
                openedBroadcastChannel.socket().bind(new InetSocketAddress(listenerPort));
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
        stop(false);
    }

    void stop(boolean onlyIfEmpty) {
        Thread localTransceiverThread;
        synchronized (lifecycleLock) {
            if (!isStarted || onlyIfEmpty && !handlers.isEmpty()) {
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
            requests.clear();

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
            boolean added = handlers.add(handler);
            if (added || locks.get(handler) == null || conditions.get(handler) == null) {
                ReentrantLock handlerLock = new ReentrantLock();
                locks.put(handler, handlerLock);
                conditions.put(handler, handlerLock.newCondition());
            }

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
        synchronized (lifecycleLock) {
            cancelPendingSend(handler);
            locks.remove(handler);
            conditions.remove(handler);
            requests.remove(handler);
            handlers.remove(handler);

            if (logger.isTraceEnabled()) {
                logger.trace("There are now {} KEBA KeContact handlers registered with the transceiver",
                        handlers.size());
            }

            Selector localSelector = selector;
            if (localSelector != null) {
                removeConnection(handler, localSelector);
            }
        }
        stop(true);
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
                PendingRequest request = requests.remove(handler);
                if (request != null) {
                    request.completed = true;
                }
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
            @Nullable
            PendingRequest request = null;
            try {
                long remainingNanos = TimeUnit.MILLISECONDS.toNanos(KeContactHandler.REPORT_INTERVAL);
                while (requests.containsKey(handler) && remainingNanos > 0) {
                    remainingNanos = responseCondition.awaitNanos(remainingNanos);
                }
                if (!handlerLock.equals(locks.get(handler)) || !responseCondition.equals(conditions.get(handler))
                        || !handlers.contains(handler) || requests.containsKey(handler)) {
                    return null;
                }
                byte[] messageBytes = message.getBytes(StandardCharsets.US_ASCII);
                ByteBuffer buffer = ByteBuffer.allocate(messageBytes.length);
                buffer.put(messageBytes);

                request = new PendingRequest(buffer);
                requests.put(handler, request);

                if (logger.isTraceEnabled()) {
                    logger.trace("{} waiting on handler condition {}", Thread.currentThread().getName(),
                            responseCondition);
                }
                remainingNanos = TimeUnit.MILLISECONDS.toNanos(KeContactHandler.REPORT_INTERVAL);
                while (!request.completed && remainingNanos > 0) {
                    remainingNanos = responseCondition.awaitNanos(remainingNanos);
                }

                return request.reply;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } finally {
                if (request != null) {
                    requests.remove(handler, request);
                    responseCondition.signalAll();
                }
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

    private @Nullable ByteBuffer takeOutbound(KeContactHandler handler) {
        ReentrantLock handlerLock = locks.get(handler);
        if (handlerLock == null) {
            return null;
        }
        handlerLock.lock();
        try {
            PendingRequest request = requests.get(handler);
            if (!handlerLock.equals(locks.get(handler)) || request == null || request.sent
                    || !handlers.contains(handler)) {
                return null;
            }
            request.sent = true;
            return request.outbound;
        } finally {
            handlerLock.unlock();
        }
    }

    private void receiveData(KeContactHandler handler, ByteBuffer data) {
        ReentrantLock handlerLock = locks.get(handler);
        Condition responseCondition = conditions.get(handler);
        if (handlerLock == null || responseCondition == null) {
            return;
        }
        handlerLock.lock();
        try {
            if (!handlerLock.equals(locks.get(handler)) || !responseCondition.equals(conditions.get(handler))) {
                return;
            }
            PendingRequest request = requests.get(handler);
            if (request != null && request.sent) {
                request.reply = data;
                request.completed = true;
                requests.remove(handler, request);
                responseCondition.signalAll();
                return;
            }
        } finally {
            handlerLock.unlock();
        }
        if (handlers.contains(handler)) {
            handler.onData(data);
        }
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
                                ByteBuffer theBuffer = takeOutbound(theHandler);
                                if (theBuffer != null) {
                                    try {
                                        theBuffer.rewind();
                                        logger.debug("Sending '{}' on the channel '{}'->'{}'",
                                                new Object[] { new String(theBuffer.array(), StandardCharsets.US_ASCII),
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
                                            if (clientAddress != null
                                                    && matchesRemoteAddress(handler, clientAddress.getAddress())) {
                                                receiveData(handler,
                                                        Objects.requireNonNull(broadcastBuffer.duplicate()));
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
                                            receiveData(theHandler, channelBuffer);
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
            } catch (CancelledKeyException e) {
                if (Thread.currentThread().isInterrupted() || !localSelector.isOpen()) {
                    return;
                }
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

                InetSocketAddress remoteAddress = new InetSocketAddress(ipAddress, remotePort);

                try {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Connecting the channel for {} ", remoteAddress);
                    }
                    datagramChannel.connect(remoteAddress);

                    handler.updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "");
                } catch (Exception e) {
                    logger.debug("An exception occurred while connecting connecting to '{}:{}' : {}",
                            new Object[] { ipAddress, remotePort, e.getMessage() });
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

    private boolean matchesRemoteAddress(KeContactHandler handler, @Nullable InetAddress sourceAddress) {
        DatagramChannel channel = datagramChannels.get(handler);
        if (sourceAddress == null || channel == null) {
            return false;
        }
        try {
            SocketAddress remoteAddress = channel.getRemoteAddress();
            return remoteAddress instanceof InetSocketAddress socketAddress
                    && sourceAddress.equals(socketAddress.getAddress());
        } catch (IOException e) {
            logger.debug("Could not resolve the connected UDP peer for '{}': {}", handler.getThing().getUID(),
                    e.getMessage());
            return false;
        }
    }

    private boolean isConnected(KeContactHandler handler) {
        return datagramChannels.get(handler) != null ? true : false;
    }
}
