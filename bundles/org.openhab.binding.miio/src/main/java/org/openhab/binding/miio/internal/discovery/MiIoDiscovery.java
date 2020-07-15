/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.discovery;

import static org.openhab.binding.miio.internal.MiIoBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.net.NetUtil;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.miio.internal.Message;
import org.openhab.binding.miio.internal.Utils;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.binding.miio.internal.cloud.CloudDeviceDTO;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MiIoDiscovery} is responsible for discovering new Xiaomi Mi IO devices
 * and their token
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.miio")
public class MiIoDiscovery extends AbstractDiscoveryService {

    /** The refresh interval for background discovery */
    private static final long SEARCH_INTERVAL = 600;
    private static final int BUFFER_LENGTH = 1024;
    private static final int DISCOVERY_TIME = 10;

    private @Nullable ScheduledFuture<?> miIoDiscoveryJob;
    protected @Nullable DatagramSocket clientSocket;
    private @Nullable Thread socketReceiveThread;
    private Set<String> responseIps = new HashSet<>();

    private final Logger logger = LoggerFactory.getLogger(MiIoDiscovery.class);
    private final CloudConnector cloudConnector;

    @Activate
    public MiIoDiscovery(@Reference CloudConnector cloudConnector) throws IllegalArgumentException {
        super(DISCOVERY_TIME);
        this.cloudConnector = cloudConnector;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start Xiaomi Mi IO background discovery");
        final @Nullable ScheduledFuture<?> miIoDiscoveryJob = this.miIoDiscoveryJob;
        if (miIoDiscoveryJob == null || miIoDiscoveryJob.isCancelled()) {
            this.miIoDiscoveryJob = scheduler.scheduleWithFixedDelay(this::discover, 0, SEARCH_INTERVAL,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop Xiaomi  Mi IO background discovery");
        final @Nullable ScheduledFuture<?> miIoDiscoveryJob = this.miIoDiscoveryJob;
        if (miIoDiscoveryJob != null) {
            miIoDiscoveryJob.cancel(true);
            this.miIoDiscoveryJob = null;
        }
    }

    @Override
    protected void deactivate() {
        stopReceiverThreat();
        final DatagramSocket clientSocket = this.clientSocket;
        if (clientSocket != null) {
            clientSocket.close();
        }
        this.clientSocket = null;
        super.deactivate();
    }

    @Override
    protected void startScan() {
        logger.debug("Start Xiaomi Mi IO discovery");
        final DatagramSocket clientSocket = getSocket();
        if (clientSocket != null) {
            logger.debug("Discovery using socket on port {}", clientSocket.getLocalPort());
            discover();
        } else {
            logger.debug("Discovery not started. Client DatagramSocket null");
        }
    }

    private void discover() {
        startReceiverThreat();
        responseIps = new HashSet<>();
        HashSet<String> broadcastAddresses = new HashSet<>();
        broadcastAddresses.add("224.0.0.1");
        broadcastAddresses.add("224.0.0.50");
        broadcastAddresses.addAll(NetUtil.getAllBroadcastAddresses());
        for (String broadcastAdress : broadcastAddresses) {
            sendDiscoveryRequest(broadcastAdress);
        }
    }

    private void discovered(String ip, byte[] response) {
        logger.trace("Discovery responses from : {}:{}", ip, Utils.getSpacedHex(response));
        Message msg = new Message(response);
        String token = Utils.getHex(msg.getChecksum());
        String id = Utils.getHex(msg.getDeviceId());
        String label = "Xiaomi Mi Device " + id + " (" + Long.parseUnsignedLong(id, 16) + ")";
        String country = "";
        boolean isOnline = false;
        if (cloudConnector.isConnected()) {
            cloudConnector.getDevicesList();
            CloudDeviceDTO cloudInfo = cloudConnector.getDeviceInfo(id);
            if (cloudInfo != null) {
                logger.debug("Cloud Info: {}", cloudInfo);
                token = cloudInfo.getToken();
                label = cloudInfo.getName() + " " + id + " (" + Long.parseUnsignedLong(id, 16) + ")";
                country = cloudInfo.getServer();
                isOnline = cloudInfo.getIsOnline();
            }
        }
        ThingUID uid = new ThingUID(THING_TYPE_MIIO, id);
        logger.debug("Discovered Mi Device {} ({}) at {} as {}", id, Long.parseUnsignedLong(id, 16), ip, uid);
        DiscoveryResultBuilder dr = DiscoveryResultBuilder.create(uid).withProperty(PROPERTY_HOST_IP, ip)
                .withProperty(PROPERTY_DID, id);
        if (IGNORED_TOKENS.contains(token)) {
            logger.debug(
                    "No token discovered for device {}. For options how to get the token, check the binding readme.",
                    id);
            dr = dr.withRepresentationProperty(PROPERTY_DID).withLabel(label);
        } else {
            logger.debug("Discovered token for device {}: {}", id, token);
            dr = dr.withProperty(PROPERTY_TOKEN, token).withRepresentationProperty(PROPERTY_DID)
                    .withLabel(label + " with token");
        }
        if (!country.isEmpty() && isOnline) {
            dr = dr.withProperty(PROPERTY_CLOUDSERVER, country);
        }
        thingDiscovered(dr.build());
    }

    synchronized @Nullable DatagramSocket getSocket() {
        DatagramSocket clientSocket = this.clientSocket;
        if (clientSocket != null && clientSocket.isBound()) {
            return clientSocket;
        }
        try {
            logger.debug("Getting new socket for discovery");
            clientSocket = new DatagramSocket();
            clientSocket.setReuseAddress(true);
            clientSocket.setBroadcast(true);
            this.clientSocket = clientSocket;
            return clientSocket;
        } catch (SocketException | SecurityException e) {
            logger.debug("Error getting socket for discovery: {}", e.getMessage());
        }
        return null;
    }

    private void closeSocket() {
        final @Nullable DatagramSocket clientSocket = this.clientSocket;
        if (clientSocket != null) {
            clientSocket.close();
        } else {
            return;
        }
        this.clientSocket = null;
    }

    private void sendDiscoveryRequest(String ipAddress) {
        final @Nullable DatagramSocket socket = getSocket();
        if (socket != null) {
            try {
                byte[] sendData = DISCOVER_STRING;
                logger.trace("Discovery sending ping to {} from {}:{}", ipAddress, socket.getLocalAddress(),
                        socket.getLocalPort());
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName(ipAddress), PORT);
                for (int i = 1; i <= 1; i++) {
                    socket.send(sendPacket);
                }
            } catch (IOException e) {
                logger.trace("Discovery on {} error: {}", ipAddress, e.getMessage());
            }
        }
    }

    /**
     * starts the {@link ReceiverThread} thread
     */
    private synchronized void startReceiverThreat() {
        final Thread srt = socketReceiveThread;
        if (srt != null) {
            if (srt.isAlive() && !srt.isInterrupted()) {
                return;
            }
        }
        stopReceiverThreat();
        Thread socketReceiveThread = new ReceiverThread();
        socketReceiveThread.start();
        this.socketReceiveThread = socketReceiveThread;
    }

    /**
     * Stops the {@link ReceiverThread} thread
     */
    private synchronized void stopReceiverThreat() {
        if (socketReceiveThread != null) {
            closeSocket();
            socketReceiveThread = null;
        }
    }

    /**
     * The thread, which waits for data and submits the unique results addresses to the discovery results
     *
     */
    private class ReceiverThread extends Thread {
        @Override
        public void run() {
            DatagramSocket socket = getSocket();
            if (socket != null) {
                logger.debug("Starting discovery receiver thread for socket on port {}", socket.getLocalPort());
                receiveData(socket);
            }
        }

        /**
         * This method waits for data and submits the unique results addresses to the discovery results
         *
         * @param socket - The multicast socket to (re)use
         */
        private void receiveData(DatagramSocket socket) {
            DatagramPacket receivePacket = new DatagramPacket(new byte[BUFFER_LENGTH], BUFFER_LENGTH);
            try {
                while (true) {
                    logger.trace("Thread {} waiting for data on port {}", this, socket.getLocalPort());
                    socket.receive(receivePacket);
                    String hostAddress = receivePacket.getAddress().getHostAddress();
                    logger.trace("Received {} bytes response from {}:{} on Port {}", receivePacket.getLength(),
                            hostAddress, receivePacket.getPort(), socket.getLocalPort());

                    byte[] messageBuf = Arrays.copyOfRange(receivePacket.getData(), receivePacket.getOffset(),
                            receivePacket.getOffset() + receivePacket.getLength());
                    if (logger.isTraceEnabled()) {
                        Message miIoResponse = new Message(messageBuf);
                        logger.trace("Discovery response received from {} DeviceID: {}\r\n{}", hostAddress,
                                Utils.getHex(miIoResponse.getDeviceId()), miIoResponse.toSting());
                    }
                    if (!responseIps.contains(hostAddress)) {
                        scheduler.schedule(() -> {
                            try {
                                discovered(hostAddress, messageBuf);
                            } catch (Exception e) {
                                logger.debug("Error submitting discovered Mi IO device at {}", hostAddress, e);
                            }
                        }, 0, TimeUnit.SECONDS);
                    }
                    responseIps.add(hostAddress);
                }
            } catch (SocketException e) {
                logger.debug("Receiver thread received SocketException: {}", e.getMessage());
            } catch (IOException e) {
                logger.trace("Receiver thread was interrupted");
            }
            logger.debug("Receiver thread ended");
        }
    }
}
