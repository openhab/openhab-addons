/**
 * Copyright (c) 2015-2020 Contributors to the openHAB project
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
package org.openhab.binding.tacmi.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.tacmi.internal.message.AnalogMessage;
import org.openhab.binding.tacmi.internal.message.DigitalMessage;
import org.openhab.binding.tacmi.internal.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TACmiCoEBridgeHandler} is the handler for a smarthomatic Bridge and
 * connects it to the framework. All {@link TACmiHandler}s use the
 * {@link TACmiCoEBridgeHandler} to execute the actual commands.
 *
 * @author Christian Niessner (marvkis) - Initial contribution
 */
public class TACmiCoEBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(TACmiCoEBridgeHandler.class);

    /**
     * Port the C.M.I. uses for COE-Communication - this cannot be changed.
     */
    private static final int coePort = 5441;

    /**
     * Connection socket
     */
    private @Nullable DatagramSocket coeSocket = null;

    private ReceiveThread receiveThread;

    private MonitorThread monitor;

    private final Collection<TACmiHandler> registeredCMIs = new HashSet<>();

    public TACmiCoEBridgeHandler(final Bridge br) {
        super(br);
    }

    /**
     * Thread which receives all data from the bridge.
     */
    private class ReceiveThread extends Thread {
        private final Logger logger = LoggerFactory.getLogger(ReceiveThread.class);

        ReceiveThread() {
            super("tacmi TA C.M.I. CoE ReceiveThread");
        }

        @Override
        public void run() {
            try {
                @Nullable
                final DatagramSocket coeSocket = TACmiCoEBridgeHandler.this.coeSocket;
                if (coeSocket == null) {
                    logger.error("coeSocket is NULL - Reader disabled!");
                    return;
                }
                while (!isInterrupted()) {
                    final byte[] receiveData = new byte[14];
                    final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        coeSocket.receive(receivePacket);
                    } catch (final SocketTimeoutException te) {
                        logger.info("Receive timeout on CoE socket, retrying ...");
                        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        // "Receive timeout on CoE socket, retrying ...");
                        continue;
                    }

                    try {
                        final byte[] data = receivePacket.getData();
                        Message message;
                        if (data[1] > 0 && data[1] < 9) {
                            logger.debug("Processing analog message");
                            message = new AnalogMessage(data);
                        } else if (data[1] == 0 || data[1] == 9) {
                            logger.debug("Processing digital message");
                            message = new DigitalMessage(data);
                        } else {
                            logger.debug("Invalid message received");
                            continue;
                        }
                        logger.debug(message.toString());

                        final InetAddress remoteAddress = receivePacket.getAddress();
                        final int node = message.canNode;
                        boolean found = false;
                        for (final TACmiHandler cmi : registeredCMIs) {
                            if (cmi.isFor(remoteAddress, node)) {
                                cmi.handleCoE(message);
                                found = true;
                            }
                        }
                        if (!found)
                            logger.info("Received CoE-Packet from {} Node {} and we don't have a Thing for!",
                                    remoteAddress, node);
                    } catch (final Throwable t) {
                        logger.error("Error processing data: " + t.getMessage(), t);
                    }
                }
                logger.debug("ReceiveThread exiting.");
            } catch (final Throwable t) {
                if (isInterrupted())
                    return;
                logger.error("Fatal error processing data: " + t.getMessage(), t);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error processing data: " + t.getMessage());
            }
        }
    }

    /**
     * Thread which periodically polls status of the bridge.
     */
    private class MonitorThread extends Thread {

        MonitorThread() {
            super("tacmi TA C.M.I. CoE MonitorThread");
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    synchronized (this) {
                        this.wait(1000);
                    }
                    for (final TACmiHandler cmi : registeredCMIs) {
                        cmi.monitor();
                    }
                } catch (final InterruptedException e) {
                    // we got interrupted
                    break;
                }
            }
            logger.debug("MonitorThread exiting.");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing TA C.M.I. CoE bridge handler");
        try {
            final DatagramSocket coeSocket = new DatagramSocket(coePort);
            coeSocket.setBroadcast(true);
            coeSocket.setSoTimeout(330000); // 300 sec is default resent-time; so we wait 330 secs
            this.coeSocket = coeSocket;
        } catch (final SocketException e) {
            logger.error("Failed to create UDP-Socket for C.M.I. CoE bridge. Reason: " + e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Failed to create UDP-Socket for C.M.I. CoE bridge. Reason: " + e.getMessage());
            return;
        }

        // workaround for issue #92: getHandler() returns NULL after
        // configuration update. :
        getThing().setHandler(this);

        this.receiveThread = new ReceiveThread();
        this.receiveThread.start();

        this.monitor = new MonitorThread();
        this.monitor.start();

        updateStatus(ThingStatus.ONLINE);
    }

    public void sendData(final byte[] pkt, final @Nullable InetAddress cmiAddress) throws IOException {
        final DatagramPacket packet = new DatagramPacket(pkt, pkt.length, cmiAddress, coePort);
        @Nullable
        DatagramSocket sock = this.coeSocket;
        if (sock == null) {
            throw new IOException("Socket is closed!");
        }
        sock.send(packet);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refresh command received.");
            /*
             * for (Device device : devices) device.refreshStatus();
             */
            for (final TACmiHandler cmi : registeredCMIs) {
                cmi.handleCommand(channelUID, command);
            }
        } else {
            logger.warn("No bridge commands defined.");
        }
    }

    protected void registerCMI(final TACmiHandler handler) {
        this.registeredCMIs.add(handler);
    }

    protected void unregisterCMI(final TACmiHandler handler) {
        this.registeredCMIs.remove(handler);
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        if (monitor != null) {
            monitor.interrupt();
            try {
                monitor.join();
            } catch (final InterruptedException e) {
                logger.info("Unexpected interrupt in monitor.join(): " + e.getMessage(), e);
            }
            monitor = null;
        }
        if (receiveThread != null)
            receiveThread.interrupt(); // just interrupt it so when the socketException throws it's flagged as
                                       // interrupted.

        @Nullable
        DatagramSocket sock = this.coeSocket;
        if (sock != null && !sock.isClosed()) {
            sock.close();
            this.coeSocket = null;
        }
        if (receiveThread != null) {
            receiveThread.interrupt();
            try {
                receiveThread.join();
            } catch (final InterruptedException e) {
                logger.info("Unexpected interrupt in receiveThread.join(): " + e.getMessage(), e);
            }
            receiveThread = null;
        }
        super.dispose();
    }
}
