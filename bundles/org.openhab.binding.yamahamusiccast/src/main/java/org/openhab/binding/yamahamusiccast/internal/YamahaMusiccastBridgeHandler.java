/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.yamahamusiccast.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yamahamusiccast.internal.dto.UdpMessage;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link YamahaMusiccastBridgeHandler} is responsible for dispatching UDP events to linked Things.
 *
 * @author Lennert Coopman - Initial contribution
 */
@NonNullByDefault
public class YamahaMusiccastBridgeHandler extends BaseBridgeHandler {
    private Gson gson = new Gson();
    private final Logger logger = LoggerFactory.getLogger(YamahaMusiccastBridgeHandler.class);
    private String threadname = getThing().getUID().getAsString();
    private @Nullable ExecutorService executor;
    private @Nullable Future<?> eventListenerJob;
    private static final int UDP_PORT = 41100;
    private static final int SOCKET_TIMEOUT_MILLISECONDS = 3000;
    private static final int BUFFER_SIZE = 5120;
    private @Nullable DatagramSocket socket;

    private void receivePackets() {
        try {
            DatagramSocket s = new DatagramSocket(null);
            s.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
            s.setReuseAddress(true);
            InetSocketAddress address = new InetSocketAddress(UDP_PORT);
            s.bind(address);
            socket = s;
            logger.trace("UDP Listener got socket on port {} with timeout {}", UDP_PORT, SOCKET_TIMEOUT_MILLISECONDS);
        } catch (SocketException e) {
            logger.trace("UDP Listener got SocketException: {}", e.getMessage(), e);
            socket = null;
            return;
        }

        DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
        DatagramSocket localSocket = socket;
        while (localSocket != null) {
            try {
                localSocket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                String trackingID = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
                logger.trace("Received packet: {} (Tracking: {})", received, trackingID);
                handleUDPEvent(received, trackingID);
            } catch (SocketTimeoutException e) {
                // Nothing to do on socket timeout
            } catch (IOException e) {
                logger.trace("UDP Listener got IOException waiting for datagram: {}", e.getMessage());
                localSocket = null;
            }
        }
        logger.trace("UDP Listener exiting");
    }

    public YamahaMusiccastBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
        executor = Executors.newSingleThreadExecutor(new NamedThreadFactory(threadname));
        Future<?> localEventListenerJob = eventListenerJob;
        ExecutorService localExecutor = executor;
        if (localEventListenerJob == null || localEventListenerJob.isCancelled()) {
            if (localExecutor != null) {
                localEventListenerJob = localExecutor.submit(this::receivePackets);
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        Future<?> localEventListenerJob = eventListenerJob;
        ExecutorService localExecutor = executor;
        if (localEventListenerJob != null) {
            localEventListenerJob.cancel(true);
            localEventListenerJob = null;
        }
        if (localExecutor != null) {
            localExecutor.shutdownNow();
            localExecutor = null;
        }
    }

    public void handleUDPEvent(String json, String trackingID) {
        String udpDeviceId = "";
        Bridge bridge = (Bridge) thing;
        for (Thing thing : bridge.getThings()) {
            ThingStatusInfo statusInfo = thing.getStatusInfo();
            switch (statusInfo.getStatus()) {
                case ONLINE:
                    logger.trace("Thing Status: ONLINE - {}", thing.getLabel());
                    YamahaMusiccastHandler handler = (YamahaMusiccastHandler) thing.getHandler();
                    if (handler != null) {
                        logger.trace("UDP: {} - {} ({} - Tracking: {})", json, handler.getDeviceId(), thing.getLabel(),
                                trackingID);

                        @Nullable
                        UdpMessage targetObject = gson.fromJson(json, UdpMessage.class);
                        if (targetObject != null) {
                            udpDeviceId = targetObject.getDeviceId();
                            if (udpDeviceId.equals(handler.getDeviceId())) {
                                handler.processUDPEvent(json, trackingID);
                            }
                        }
                    }
                    break;
                default:
                    logger.trace("Thing Status: NOT ONLINE - {} (Tracking: {})", thing.getLabel(), trackingID);
                    break;
            }
        }
    }
}
