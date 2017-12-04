/**
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
import org.openhab.binding.osramlightify.internal.messages.LightifyListGroupsMessage;
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
     * The maximum time we wait for data from the gateway before breaking off to
     * check for interrupts or to see if it is time to poll for state updates.
     *
     * The smaller this is the more cpu we will use but the faster we will respond
     * to requests to stop the connector thread (for instance when reloading the
     * binding). It MUST be less than SafeMethodCaller.DEFAULT_TIMEOUT in the core.
     */
    private static final int MAX_PRE_MESSAGE_TIMEOUT = 4800; // milliseconds
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

    private static final int MAXIMUM_RESENDS = 5;

    private final Logger logger = LoggerFactory.getLogger(LightifyConnector.class);

    private LightifyBridgeHandler bridgeHandler;

    private LightifyTransmitQueue<LightifyMessage> transmitQueue = new LightifyTransmitQueue<>(this);

    private int reconnectAttempts = 1;

    private boolean interrupted = false;

    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private LightifyListPairedDevicesMessage initialPoll;

    private final LightifyListGroupsMessage listGroups = new LightifyListGroupsMessage();
    private final LightifyListPairedDevicesMessage listPairedDevices = new LightifyListPairedDevicesMessage();
    private boolean havePoll = false;
    private long nextPoll;
    private long pollInterval;

    private int resendCount = 0;
    private int seqNo = 0;

    private class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        public void uncaughtException(Thread thread, Throwable throwable) {
            logger.error("Connector died: ", throwable);
        }
    }

    public LightifyConnector(LightifyBridgeHandler bridgeHandler) {
        setName(getClass().getSimpleName() + "(" + bridgeHandler.getThing().getUID() + ")");
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

            resendCount = 0;

            // First message MUST be a LIST_PAIRED_DEVICES. Sometimes we just get 0x16 status responses
            // to everything we send until a LIST_PAIRED_DEVICES. It isn't clear what 0x16 means or
            // why a LIST_PAIRED_DEVICES clears the condition. Currently this has only been observed
            // at the start of a connection.
            initialPoll = listPairedDevices;
            transmitMessage(listPairedDevices.discovery(true));
            havePoll = true;

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
        nextPoll = System.nanoTime();
        long nextDiscovery= nextPoll;
        boolean doDiscovery = true;
        pollInterval = bridgeHandler.getConfiguration().minPollIntervalNanos;

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

                long timeout = bridgeHandler.getConfiguration().maxPollIntervalNanos;

                if (!havePoll) {
                    long now = System.nanoTime();
                    if (nextPoll - now <= 0) {
                        havePoll = true;
                        doDiscovery = (nextDiscovery - now <= 0);
                        if (doDiscovery) {
                            bridgeHandler.sendMessage(listGroups);
                        }
                        bridgeHandler.sendMessage(listPairedDevices.discovery(doDiscovery));
                    } else {
                        timeout = nextPoll - now;
                    }
                }

                // First byte times out so we can check for interrupt and do state/discovery polls
                int intTimeout = (int) TimeUnit.NANOSECONDS.toMillis(timeout);
                if (intTimeout <= 0) {
                    intTimeout = 1;
                } else if (intTimeout > MAX_PRE_MESSAGE_TIMEOUT) {
                    intTimeout = MAX_PRE_MESSAGE_TIMEOUT;
                }
                socket.setSoTimeout(intTimeout);

                try {
                    read = in.read();
                } catch (SocketTimeoutException ste) {
                    // If we were waiting for a response to an initial poll things are bad.
                    // Maybe this isn't a Lightify gateway? (Which can happen if we reuse
                    // the last known address rather than waiting for mDNS)
                    if (initialPoll != null) {
                        disconnect();
                    }
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
                                LightifyMessage request = (initialPoll != null ? initialPoll : transmitQueue.peek());

                                if (request == null) {
                                    logger.debug("No request but RX: {}", DatatypeConverter.printHexBinary(messageBuffer.array()));
                                } else

                                // N.B. The only messages the gateway currently sends are responses
                                // to our requests. However since the protocol is undocumented we
                                // cannot assume that will not change.
                                if (LightifyMessage.getSeqNo(messageBuffer) != request.getSeqNo()) {
                                    logger.warn("Sequence number mismatch. Please report this! Got: {} for: {}", DatatypeConverter.printHexBinary(messageBuffer.array()), request);

                                    // The best we can do is to disconnect and start over.
                                    disconnect();
                                    continue;
                                } else if (request.handleResponse(bridgeHandler, messageBuffer)) {
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

                                        long now = System.nanoTime();
                                        nextPoll = now + pollInterval;
                                        if (doDiscovery) {
                                            doDiscovery = false;
                                            nextDiscovery = now + bridgeHandler.getConfiguration().discoveryIntervalNanos;
                                        }
                                    }

                                    resendCount = 0;

                                    if (initialPoll == null) {
                                        transmitQueue.sendNext();
                                    } else {
                                        initialPoll = null;
                                        reconnectAttempts = 1;

                                        // Make sure the transmit queue is started. If the queue is empty
                                        // it will be started when the next request is queued but if we
                                        // took a connection drop while trying to send a request the queue
                                        // will NOT be empty and sending will NOT be triggered.
                                        transmitQueue.send();

                                        // Log the gateway firmware at the earliest opportunity.
                                        bridgeHandler.sendMessage(new LightifyGatewayFirmwareMessage());

                                        bridgeHandler.setStatusOnline();
                                    }
                                } else if (initialPoll != null) {
                                    disconnect();
                                } else {
                                    if (resendCount++ == MAXIMUM_RESENDS) {
                                        if (request instanceof LightifyListPairedDevicesMessage) {
                                            havePoll = false;
                                            pollInterval = bridgeHandler.getConfiguration().minPollIntervalNanos;
                                            nextPoll = System.nanoTime() + pollInterval;
                                        }

                                        resendCount = 0;
                                        transmitQueue.sendNext();
                                    } else {
                                        transmitQueue.send();
                                    }
                                }
                            } catch (LightifyException e) {
                                logger.debug("RX: {}", DatatypeConverter.printHexBinary(messageBuffer.array()));
                                logger.warn("Error", e);
                                if (initialPoll == null) {
                                    transmitQueue.sendNext();
                                } else {
                                    disconnect();
                                }
                            }
                        } else {
                            logger.debug("Short message RX: wanted {} bytes, got {}: {}", messageLength, count, DatatypeConverter.printHexBinary(messageBuffer.array()));
                        }
                    }
                }

                if (read < 0) {
                    logger.debug("EOF on socket");
                    disconnect();
                }
            } catch (SocketTimeoutException ste) {
                logger.debug("Timeout reading socket", ste);
                disconnect();
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

    private boolean transmitMessage(LightifyMessage message) {
        try {
            message.setSeqNo(seqNo++);

            ByteBuffer messageBuffer = message.encodeMessage();

            if (message.isPoller()) {
                logger.trace("TX: {}", message);
            } else {
                logger.debug("TX: {}", message);

                // While it is possible for a thread other than the connector thread to do a
                // transmit this only happens when a message is added to an empty queue.
                // And if the queue is empty then the connector thread cannot be processing
                // a response and cannot be attempting to update either pollInterval or
                // nextPoll at the same time as us. Therefore we do not need any synchronization.
                // N.B. The synchronization on the method synchronizes with connect/disconnect
                // but not the connector thread's actions.
                pollInterval = bridgeHandler.getConfiguration().minPollIntervalNanos;
                nextPoll = System.nanoTime() + pollInterval;
            }

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

        return true;
    }

    public synchronized boolean transmitQueueSender(LightifyMessage message) {
        if (out != null && initialPoll == null) {
            return transmitMessage(message);
        }

        return true;
    }
}
