/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.keba.handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KeContactBroadcastListener} is responsible for receiving UDP broadcast messages sent by the KEBA Charging
 * Stations. {@link KeContactHandler} willing to receive these messages have to register themselves with the
 * {@link KeContactBroadcastListener}
 *
 * @author Karel Goderis - Initial contribution
 */

class KeContactBroadcastListener {

    private static final String KEBA_HANDLER_THREADPOOL_NAME = "Keba";
    public static final int LISTENER_PORT_NUMBER = 7090;
    public static final int LISTENING_INTERVAL = 100;

    private DatagramChannel broadcastChannel;
    private SelectionKey broadcastKey;
    private Selector broadcastListener;
    private Thread broadcastThread;
    private boolean isStarted = false;
    private Set<KeContactHandler> listeners = Collections.synchronizedSet(new HashSet<KeContactHandler>());

    private final Logger logger = LoggerFactory.getLogger(KeContactBroadcastListener.class);

    public void start() {
        if (!isStarted) {
            logger.debug("Starting the Keba broadcast listener");
            try {
                broadcastChannel = DatagramChannel.open();
                broadcastChannel.socket().bind(new InetSocketAddress(LISTENER_PORT_NUMBER));
                broadcastChannel.configureBlocking(false);

                broadcastListener = Selector.open();

                logger.info("Listening for incoming data on {}", broadcastChannel.getLocalAddress());

                synchronized (broadcastListener) {
                    broadcastListener.wakeup();
                    broadcastKey = broadcastChannel.register(broadcastListener, broadcastChannel.validOps());
                }

                if (broadcastThread == null) {
                    broadcastThread = new Thread(broadcastRunnable, "ESH-Keba-BroadcastListener");
                    broadcastThread.start();
                }

                isStarted = true;
            } catch (ClosedSelectorException | CancelledKeyException | IOException e) {
                logger.error("An exception occurred while registering the selector: {}", e.getMessage());
            }
        }
    }

    public void stop() {
        if (isStarted) {
            logger.debug("Stopping the Keba broadcast listener");
            if (broadcastThread != null) {
                broadcastThread.interrupt();
            }

            try {
                broadcastChannel.close();
            } catch (IOException e) {
                logger.error("An exception occurred while closing the broadcast channel on port number {} ({})",
                        LISTENER_PORT_NUMBER, e.getMessage());
            }

            isStarted = false;
        }
    }

    private void reset() {
        try {
            broadcastChannel.close();
        } catch (IOException e) {
            logger.error("An exception occurred while closing the broadcast channel on port number {} ({})",
                    LISTENER_PORT_NUMBER, e.getMessage());
        }

        isStarted = false;

        start();
    }

    public void registerHandler(KeContactHandler handler) {
        if (handler != null) {
            listeners.add(handler);

            if (logger.isTraceEnabled()) {
                logger.trace("There are now {} Keba handlers registered with the broadcast listener", listeners.size());
            }

            if (listeners.size() == 1) {
                start();
            }
        }
    }

    public void unRegisterHandler(KeContactHandler handler) {
        if (handler != null) {
            listeners.remove(handler);

            if (logger.isTraceEnabled()) {
                logger.trace("There are now {} Keba handlers registered with the broadcast listener", listeners.size());
            }

            if (listeners.size() == 0) {
                stop();
            }
        }
    }

    public Runnable broadcastRunnable = new Runnable() {

        @Override
        public void run() {
            while (true) {
                try {
                    ByteBuffer readBuffer = null;
                    InetSocketAddress clientAddress = null;

                    SelectionKey theSelectionKey = broadcastChannel.keyFor(broadcastListener);

                    if (theSelectionKey != null) {

                        synchronized (broadcastListener) {
                            broadcastListener.selectNow();
                        }

                        Iterator<SelectionKey> it = broadcastListener.selectedKeys().iterator();
                        while (it.hasNext()) {
                            SelectionKey selKey = it.next();
                            it.remove();
                            if (selKey.isValid() && selKey.isReadable() && selKey == theSelectionKey) {

                                readBuffer = ByteBuffer.allocate(KeContactHandler.BUFFER_SIZE);
                                int numberBytesRead = 0;

                                if (selKey == broadcastKey) {
                                    clientAddress = (InetSocketAddress) broadcastChannel.receive(readBuffer);
                                    logger.debug("Received {} from {} on the broadcast listener port ",
                                            new String(readBuffer.array()), clientAddress);
                                    numberBytesRead = readBuffer.position();
                                }

                                if (numberBytesRead != -1) {
                                    readBuffer.flip();
                                }
                            }
                        }
                    }

                    if (readBuffer != null && readBuffer.remaining() > 0) {
                        for (KeContactHandler handler : listeners) {
                            if (clientAddress != null
                                    && handler.getIPAddress().equals(clientAddress.getAddress().getHostAddress())) {
                                handler.onRead(readBuffer);
                            }
                        }
                    }

                    if (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(LISTENING_INTERVAL);
                    } else {
                        return;
                    }

                } catch (IOException e) {
                    logger.error("An exception occurred while receiving data on the broadcast listener port: '{}'",
                            e.getMessage());
                    listeners.forEach(listener -> listener.setBroadcastListenerStatus(false));
                    reset();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    };
}
