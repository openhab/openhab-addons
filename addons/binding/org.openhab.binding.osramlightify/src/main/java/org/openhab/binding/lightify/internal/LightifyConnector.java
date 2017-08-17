/*
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.smarthome.core.thing.ThingStatusDetail;

import static org.openhab.binding.osramlightify.LightifyBindingConstants.PROPERTY_CURRENT_ADDRESS;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;

import org.openhab.binding.osramlightify.internal.discovery.LightifyDiscoveryParticipant;

import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;

import org.openhab.binding.osramlightify.internal.messages.LightifyMessage;
import org.openhab.binding.osramlightify.internal.messages.LightifyGatewayFirmwareMessage;
import org.openhab.binding.osramlightify.internal.messages.LightifyListPairedDevicesMessage;

import org.openhab.binding.osramlightify.internal.LightifyTransmitQueue;
import org.openhab.binding.osramlightify.internal.LightifyTransmitQueueSender;

/**
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyConnector extends Thread implements LightifyTransmitQueueSender<LightifyMessage> {

    /**
     * The port the gateway listens on.
     */
    private static final int LIGHTIFY_GATEWAY_PORT = 4000;

    /**
     * How long to block in the start-of-message read before exiting to check
     * if we've been interrupted.
     *
     * The smaller this is the faster we will respond to termination requests
     * but the more CPU we will consume just spinning.
     */
    private static final int PRE_MESSAGE_TIMEOUT = 500; // milliseconds

    /**
     * How long to wait for data while in the middle of reading a message
     * from the gateway.
     *
     * This should never normally be reached. If it does it means the gateway
     * has disappeared without notice in the middle of talking to us.
     */
    private static final int IN_MESSAGE_TIMEOUT = 5000; // milliseconds

    /**
     * How long to pause between reconnect attempts if the gateway does not seem
     * to be responding.
     */
    private static final int RECONNECT_INTERVAL = 10000; // milliseconds

    /**
     * How many times to attempt reconnection before marking the gateway (and all
     * paired devices) offline.
     *
     * This can be expensive and we _do_ queue commands so making this long
     * enough to allow a gateway reboot makes sense.
     */
    private static final int RECONNECT_ATTEMPTS_BEFORE_OFFLINE = 3;

    private static final int INITIAL_BACKOFF = 1000;
    private static final int MAXIMUM_BACKOFF = 15000;

    private final Logger logger = LoggerFactory.getLogger(LightifyConnector.class);

    private LightifyBridgeHandler bridgeHandler;

    private LightifyTransmitQueue<LightifyMessage> transmitQueue = new LightifyTransmitQueue<>(this);

    private int reconnectAttempts = 1;

    private boolean interrupted = false;

    private Socket socket;
    private InputStream in;
    private OutputStream out;

    private int backoff = INITIAL_BACKOFF;
    private int seqNo = 0;

    private class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        public void uncaughtException(Thread thread, Throwable throwable) {
            logger.error("Connector died: ", throwable);
        }
    }

    public LightifyConnector(LightifyBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
        setUncaughtExceptionHandler(new ExceptionHandler());
        start();
    }

    @Override
    public void interrupt() {
        logger.trace("interrupt received");
        interrupted = true;
        super.interrupt();
    }

    private boolean connectToAddr(String addr) {
        try {
            return connectToAddr(InetAddress.getByName(addr));
        } catch (UnknownHostException e) {
        }

        return false;
    }

    private boolean connectToAddr(InetAddress addr) {
        try {
            socket = new Socket(addr, LIGHTIFY_GATEWAY_PORT);
            in = socket.getInputStream();
            out = socket.getOutputStream();
            bridgeHandler.getThing().setProperty(PROPERTY_CURRENT_ADDRESS, addr.getHostAddress());
            return true;
        } catch (IOException ioe) {
            disconnect();
        }

        return false;
    }

    private synchronized boolean connect() {

        logger.debug("Connecting to Lightify gateway...");

        // Try addresses (if any) found by the mDNS discovery first.
        InetAddress[] mDNSAddresses = LightifyDiscoveryParticipant.getInetAddressesFor(bridgeHandler.getThing().getUID());
        if (mDNSAddresses != null) {
            for (InetAddress addr : mDNSAddresses) {
                logger.debug("Connecting to Lightify gateway using mDNS address {}:{}", addr.getHostAddress(), LIGHTIFY_GATEWAY_PORT);
                if (connectToAddr(addr)) {
                    break;
                }
            }
        }

        // If we still don't have a connection try the statically configured address (if any)
        if (socket == null) {
            String address = bridgeHandler.getConfiguration().ipAddress;

            if (address != null && !address.equals("")) {
                logger.debug("Connecting to Lightify gateway using configured address {}:{}", address, LIGHTIFY_GATEWAY_PORT);
                connectToAddr(address);
            }

            // If still nothing try the last known good address. If may be the binding has
            // reloaded and mDNS discovery has yet to do its stuff.
            if (socket == null) {
                address = bridgeHandler.getThing().getProperties().get(PROPERTY_CURRENT_ADDRESS);

                if (address != null && !address.equals("")) {
                    logger.debug("Connecting to Lightify gateway using last used address {}:{}", address, LIGHTIFY_GATEWAY_PORT);
                    connectToAddr(address);
                }
            }
        }

        if (socket != null) {
            logger.debug("Connected");

            seqNo = 0;

            transmitQueueSender(new LightifyGatewayFirmwareMessage());

            // Hold off until we get the response confirming we're talking to the right thing.
            out = null;

            return true;
        }

        return false;
    }

    private synchronized void disconnect() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
            out = null;
            in = null;
            socket = null;
        }
    }

    @Override
    public void run() {
        boolean havePoll = false;
        long nextPoll = System.nanoTime();
        long pollInterval = bridgeHandler.getConfiguration().minPollIntervalNanos;

        logger.debug("Lightify connector started");

        while (!interrupted) {
            int read;

            try {
                if (socket == null && !connect()) {
                    disconnect();

                    if (reconnectAttempts++ == RECONNECT_ATTEMPTS_BEFORE_OFFLINE) {
                        bridgeHandler.setStatusOffline(ThingStatusDetail.COMMUNICATION_ERROR);
                    }

                    try {
                        Thread.sleep(RECONNECT_INTERVAL);
                    } catch (InterruptedException ie) {
                    }

                    continue;
                }

                // First byte times out so we can check for interrupt
                socket.setSoTimeout(PRE_MESSAGE_TIMEOUT);

                if (out != null && !havePoll && System.nanoTime() - nextPoll >= 0) {
                    havePoll = true;
                    bridgeHandler.getDiscoveryService().startScan(null);
                }

                try {
                    read = in.read();
                } catch (SocketTimeoutException ste) {
                    continue;
                }

                if (read > 0) {
                    // The rest is not normally interruptible, but it shouldn't stall
                    // either. So we'll set a long timeout and if it triggers treat
                    // it as an EOF. No need to check for interrupts until the start
                    // of the next message though.
                    socket.setSoTimeout(IN_MESSAGE_TIMEOUT);

                    int messageLength = read;

                    if ((read = in.read()) != -1) {
                        messageLength += (read << 8);

                        ByteBuffer messageBuffer = ByteBuffer.allocate(Short.BYTES + messageLength).order(ByteOrder.LITTLE_ENDIAN);
                        messageBuffer.putShort((short) messageLength);

                        int count = 0;
                        while (count != messageLength && (read = in.read(messageBuffer.array(), Short.BYTES + count, messageLength - count)) != -1) {
                            count += read;
                        }

                        if (count == messageLength) {
                            logger.trace("RX: {}", DatatypeConverter.printHexBinary(messageBuffer.array()));

                            // Process message
                            try {
                                // If we aren't fully up yet then whatever we see is the response to
                                // our (unqueued) firmware probe and serves to confirm we are talking
                                // to a Lightify gateway and can go fully active.
                                if (out == null) {
                                    reconnectAttempts = 1;
                                    out = socket.getOutputStream();

                                    // Make sure the transmit queue is started. If the queue is empty
                                    // the poll below will start it but if we took a connection drop
                                    // while trying to send a request the queue will NOT be empty and
                                    // the poll will NOT trigger a send. Normally with sockets the
                                    // only time you detect a connection drop is when you try and
                                    // send something...
                                    transmitQueue.send();

                                    (new LightifyGatewayFirmwareMessage()).handleResponse(bridgeHandler, messageBuffer);

                                    bridgeHandler.setStatusOnline();

                                    // We _always_ poll on start up.
                                    havePoll = true;
                                    bridgeHandler.getDiscoveryService().startScan(null);
                                } else {
                                    LightifyMessage request = transmitQueue.peek();

                                    // N.B. The only messages the gateway currently sends are responses
                                    // to our requests. However since the protocol is undocumented we
                                    // cannot assume that will not change.
                                    if (LightifyMessage.getSeqNo(messageBuffer) != request.getSeqNo()) {
                                        logger.warn("Sequence number mismatch. Please report this! Got: ", DatatypeConverter.printHexBinary(messageBuffer.array()));

                                        // The best we can do is to disconnect and start over.
                                        disconnect();
                                        continue;
                                    } else if (request.handleResponse(bridgeHandler, messageBuffer)) {
                                        backoff = INITIAL_BACKOFF;
                                        transmitQueue.sendNext();
                                    } else {
                                        logger.debug("handler failed, backoff {}, message {}", backoff, request);

                                        try {
                                            Thread.sleep(backoff);
                                        } catch (InterruptedException ie) {
                                        }

                                        backoff *= 2;

                                        transmitQueue.send();
                                    }

                                    if (request instanceof LightifyListPairedDevicesMessage) {
                                        LightifyListPairedDevicesMessage pollResponse = (LightifyListPairedDevicesMessage) request;

                                        havePoll = false;

                                        if (pollResponse.hasChanges()) {
                                            // If there are changes happening that we didn't initiate we
                                            // poll quickly to track what is happening.
                                            pollInterval = bridgeHandler.getConfiguration().minPollIntervalNanos;
                                        } else {
                                            // If nothing unexpected is happening we grow the poll interval
                                            // in order to reduce load.
                                            long maxPollInterval = bridgeHandler.getConfiguration().maxPollIntervalNanos;
                                            if (maxPollInterval - pollInterval > 0) {
                                                pollInterval *= 2;
                                                if (maxPollInterval - pollInterval < 0) {
                                                    pollInterval = maxPollInterval;
                                                }
                                            }
                                        }

                                        nextPoll = System.nanoTime() + pollInterval;
                                    }
                                }
                            } catch (LightifyException e) {
                                logger.warn("Error", e);
                                transmitQueue.sendNext();
                            }
                        }
                    }
                } else if (read < 0) {
                    logger.debug("EOF on socket");
                    disconnect();
                }
            } catch (IOException e) {
                logger.debug("Error on socket", e);
                disconnect();
            }
        }

        logger.debug("Close");
        disconnect();
        logger.debug("Lightify connector stopped");
    }

    public void sendMessage(LightifyMessage message) {
        transmitQueue.enqueue(message);
    }

    public synchronized boolean transmitQueueSender(LightifyMessage message) {
        if (out != null) {
            try {
                message.setSeqNo(seqNo++);

                if (message.isPoller()) {
                    logger.trace("TX: {}", message);
                } else {
                    logger.debug("TX: {}", message);
                }

                ByteBuffer messageBuffer = message.encodeMessage();

                logger.trace("TX: {}", DatatypeConverter.printHexBinary(messageBuffer.array()));

                out.write(messageBuffer.array());
                out.flush();

            } catch (LightifyMessageTooLongException lmtle) {
                logger.warn("Message too long", lmtle);
                // Discard it and move on.
                return false;

            } catch (IOException ioe) {
                disconnect();
            }
        }

        return true;
    }
}
