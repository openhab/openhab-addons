/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.io.UnsupportedEncodingException;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

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

public class KeContactTransceiver {

    public static final int LISTENER_PORT_NUMBER = 7090;
    public static final int LISTENING_INTERVAL = 100;
    public static final int BUFFER_SIZE = 1024;

    private DatagramChannel broadcastChannel;
    private SelectionKey broadcastKey;
    private Selector selector;
    private Thread transceiverThread;
    private boolean isStarted = false;
    private Set<KeContactHandler> handlers = Collections.synchronizedSet(new HashSet<>());
    private Map<KeContactHandler, DatagramChannel> datagramChannels = Collections.synchronizedMap(new HashMap<>());
    private Map<KeContactHandler, ByteBuffer> buffers = Collections.synchronizedMap(new HashMap<>());
    private Map<KeContactHandler, ReentrantLock> locks = Collections.synchronizedMap(new HashMap<>());
    private Map<KeContactHandler, Boolean> flags = Collections.synchronizedMap(new HashMap<>());

    private final Logger logger = LoggerFactory.getLogger(KeContactTransceiver.class);

    public void start() {
        if (!isStarted) {
            logger.debug("Starting the the KEBA KeContact transceiver");
            try {
                selector = Selector.open();

                if (transceiverThread == null) {
                    transceiverThread = new Thread(transceiverRunnable,
                            "OH-binding-" + KebaBindingConstants.BINDING_ID + "-Transceiver");
                    transceiverThread.start();
                }

                broadcastChannel = DatagramChannel.open();
                broadcastChannel.socket().bind(new InetSocketAddress(LISTENER_PORT_NUMBER));
                broadcastChannel.configureBlocking(false);

                logger.info("Listening for incoming data on {}", broadcastChannel.getLocalAddress());

                synchronized (selector) {
                    selector.wakeup();
                    broadcastKey = broadcastChannel.register(selector, broadcastChannel.validOps());
                }

                for (KeContactHandler listener : handlers) {
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
            for (KeContactHandler listener : handlers) {
                this.removeConnection(listener);
            }

            try {
                broadcastChannel.close();
            } catch (IOException e) {
                logger.error("An exception occurred while closing the broadcast channel on port number {} : '{}'",
                        LISTENER_PORT_NUMBER, e.getMessage(), e);
            }

            try {
                selector.close();
            } catch (IOException e) {
                logger.error("An exception occurred while closing the selector: '{}'", e.getMessage(), e);
            }

            logger.debug("Stopping the the KEBA KeContact transceiver");
            if (transceiverThread != null) {
                transceiverThread.interrupt();
                try {
                    transceiverThread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                transceiverThread = null;
            }

            locks.clear();
            flags.clear();

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
            locks.put(handler, new ReentrantLock());

            if (logger.isTraceEnabled()) {
                logger.trace("There are now {} KEBA KeContact handlers registered with the transceiver",
                        handlers.size());
            }

            if (handlers.size() == 1) {
                start();
            }

            if (!isConnected(handler)) {
                establishConnection(handler);
            }
        }
    }

    public void unRegisterHandler(KeContactHandler handler) {
        if (handler != null) {
            locks.remove(handler);
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

    protected ByteBuffer send(String message, KeContactHandler handler) {
        ReentrantLock handlerLock = locks.get(handler);

        if (handlerLock != null) {
            handlerLock.lock();
            try {
                ByteBuffer buffer = ByteBuffer.allocate(message.getBytes().length);
                buffer.put(message.getBytes("ASCII"));

                flags.put(handler, Boolean.TRUE);
                buffers.put(handler, buffer);

                synchronized (handlerLock) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("{} waiting on handerLock {}", Thread.currentThread().getName(),
                                handlerLock.toString());
                    }
                    handlerLock.wait(KeContactHandler.REPORT_INTERVAL);
                }

                return buffers.remove(handler);
            } catch (UnsupportedEncodingException | InterruptedException e) {
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

    public Runnable transceiverRunnable = () -> {
        while (true) {
            try {
                synchronized (selector) {
                    try {
                        selector.selectNow();
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

                            for (KeContactHandler handler : handlers) {
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
                                        for (KeContactHandler handler : handlers) {
                                            if (clientAddress != null && handler.getIPAddress()
                                                    .equals(clientAddress.getAddress().getHostAddress())) {
                                                ReentrantLock theLock = locks.get(handler);
                                                if (theLock != null && theLock.isLocked()) {
                                                    buffers.put(handler, readBuffer);
                                                    synchronized (theLock) {
                                                        if (logger.isTraceEnabled()) {
                                                            logger.trace("{} notifyall on handerLock {}",
                                                                    Thread.currentThread().getName(),
                                                                    theLock.toString());
                                                        }
                                                        theLock.notifyAll();
                                                    }
                                                } else {
                                                    handler.onData(readBuffer);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    handlers.forEach(listener -> listener.updateStatus(ThingStatus.OFFLINE,
                                            ThingStatusDetail.COMMUNICATION_ERROR, "The transceiver is offline"));
                                    reset();
                                }
                            } else {
                                DatagramChannel theChannel = (DatagramChannel) selKey.channel();
                                KeContactHandler theHandler = null;

                                for (KeContactHandler handlers : handlers) {
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
                                        readBuffer.flip();
                                        if (readBuffer.remaining() > 0) {
                                            ReentrantLock theLock = locks.get(theHandler);
                                            if (theLock != null && theLock.isLocked()) {
                                                buffers.put(theHandler, readBuffer);
                                                synchronized (theLock) {
                                                    theLock.notifyAll();
                                                }
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
    };

    private void establishConnection(KeContactHandler handler) {
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

                synchronized (selector) {
                    selector.wakeup();
                    int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
                    try {
                        datagramChannel.register(selector, interestSet);
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
            }
        } else {
            handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    handler.getThing().getStatusInfo().getDescription());
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
                    handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "");
                } catch (Exception e) {
                    logger.debug("An exception occurred while closing the datagramchannel for '{}': {}",
                            handler.getThing().getUID(), e.getMessage());
                    handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "An exception occurred while closing the datagramchannel");
                }
            }
        }
    }

    private boolean isConnected(KeContactHandler handler) {
        return datagramChannels.get(handler) != null ? true : false;
    }
}
