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
package org.openhab.binding.tacmi.internal.coe;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tacmi.internal.message.AnalogMessage;
import org.openhab.binding.tacmi.internal.message.DigitalMessage;
import org.openhab.binding.tacmi.internal.message.Message;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TACmiCoEBridgeHandler} is the handler for a smarthomatic Bridge and
 * connects it to the framework. All {@link TACmiHandler}s use the
 * {@link TACmiCoEBridgeHandler} to execute the actual commands.
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
public class TACmiCoEBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(TACmiCoEBridgeHandler.class);

    /**
     * Port the C.M.I. uses for COE-Communication - this cannot be changed.
     */
    private static final int COE_PORT = 5441;

    /**
     * Connection socket
     */
    private @Nullable DatagramSocket coeSocket = null;

    private @Nullable ReceiveThread receiveThread;

    private @Nullable ScheduledFuture<?> timeoutCheckFuture;

    private final Collection<TACmiHandler> registeredCMIs = new HashSet<>();

    public TACmiCoEBridgeHandler(final Bridge br) {
        super(br);
    }

    /**
     * Thread which receives all data from the bridge.
     */
    private class ReceiveThread extends Thread {
        private final Logger logger = LoggerFactory.getLogger(ReceiveThread.class);

        ReceiveThread(String threadName) {
            super(threadName);
        }

        @Override
        public void run() {
            final DatagramSocket coeSocket = TACmiCoEBridgeHandler.this.coeSocket;
            if (coeSocket == null) {
                logger.warn("coeSocket is NULL - Reader disabled!");
                return;
            }
            while (!isInterrupted()) {
                final byte[] receiveData = new byte[14];

                try {
                    final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        coeSocket.receive(receivePacket);
                    } catch (final SocketTimeoutException te) {
                        logger.trace("Receive timeout on CoE socket, retrying ...");
                        continue;
                    }

                    final byte[] data = receivePacket.getData();
                    Message message;
                    if (data[1] > 0 && data[1] < 9) {
                        message = new AnalogMessage(data);
                    } else if (data[1] == 0 || data[1] == 9) {
                        message = new DigitalMessage(data);
                    } else {
                        logger.debug("Invalid message received");
                        continue;
                    }
                    logger.debug("{}", message.toString());

                    final InetAddress remoteAddress = receivePacket.getAddress();
                    final int node = message.canNode;
                    boolean found = false;
                    for (final TACmiHandler cmi : registeredCMIs) {
                        if (cmi.isFor(remoteAddress, node)) {
                            cmi.handleCoE(message);
                            found = true;
                        }
                    }
                    if (!found) {
                        logger.debug("Received CoE-Packet from {} Node {} and we don't have a Thing for!",
                                remoteAddress, node);
                    }
                } catch (final IOException e) {
                    if (isInterrupted()) {
                        return;
                    }
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Error processing data: " + e.getMessage());

                } catch (RuntimeException e) {
                    // we catch runtime exceptions here to prevent the receiving thread to stop accidentally if
                    // something like a IllegalStateException or NumberFormatExceptions are thrown. This indicates a bug
                    // or a situation / setup I'm not thinking of ;)
                    if (isInterrupted()) {
                        return;
                    }
                    logger.error("Error processing data: {}", e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Periodically check for timeouts on the registered / active CoE channels
     */
    private void checkForTimeouts() {
        for (final TACmiHandler cmi : registeredCMIs) {
            cmi.checkForTimeout();
        }
    }

    @Override
    public void initialize() {
        try {
            final DatagramSocket coeSocket = new DatagramSocket(COE_PORT);
            coeSocket.setBroadcast(true);
            coeSocket.setSoTimeout(330000); // 300 sec is default resent-time; so we wait 330 secs
            this.coeSocket = coeSocket;
        } catch (final SocketException e) {
            // logged by framework via updateStatus
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Failed to create UDP-Socket for C.M.I. CoE bridge. Reason: " + e.getMessage());
            return;
        }

        ReceiveThread reciveThreadNN = new ReceiveThread("OH-binding-" + getThing().getUID().getAsString());
        reciveThreadNN.setDaemon(true);
        reciveThreadNN.start();
        this.receiveThread = reciveThreadNN;

        ScheduledFuture<?> timeoutCheckFuture = this.timeoutCheckFuture;
        if (timeoutCheckFuture == null || timeoutCheckFuture.isCancelled()) {
            this.timeoutCheckFuture = scheduler.scheduleWithFixedDelay(this::checkForTimeouts, 1, 1, TimeUnit.SECONDS);
        }

        updateStatus(ThingStatus.ONLINE);
    }

    public void sendData(final byte[] pkt, final @Nullable InetAddress cmiAddress) throws IOException {
        final DatagramPacket packet = new DatagramPacket(pkt, pkt.length, cmiAddress, COE_PORT);
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
            // just forward it to the registered handlers...
            for (final TACmiHandler cmi : registeredCMIs) {
                cmi.handleCommand(channelUID, command);
            }
        } else {
            logger.debug("No bridge commands defined.");
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
        // clean up the timeout check
        ScheduledFuture<?> timeoutCheckFuture = this.timeoutCheckFuture;
        if (timeoutCheckFuture != null) {
            timeoutCheckFuture.cancel(true);
            this.timeoutCheckFuture = null;
        }

        // clean up the receive thread
        ReceiveThread receiveThread = this.receiveThread;
        if (receiveThread != null) {
            receiveThread.interrupt(); // just interrupt it so when the socketException throws it's flagged as
                                       // interrupted.
        }

        @Nullable
        DatagramSocket sock = this.coeSocket;
        if (sock != null && !sock.isClosed()) {
            sock.close();
            this.coeSocket = null;
        }
        if (receiveThread != null) {
            receiveThread.interrupt();
            try {
                // it should join quite quick as we already closed the socket which should have the receiver thread
                // caused to stop.
                receiveThread.join(250);
            } catch (final InterruptedException e) {
                logger.debug("Unexpected interrupt in receiveThread.join(): {}", e.getMessage(), e);
            }
            this.receiveThread = null;
        }
        super.dispose();
    }
}
