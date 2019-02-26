/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.net.NetUtil;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.miio.internal.Message;
import org.openhab.binding.miio.internal.Utils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MiIoDiscovery} is responsible for discovering new Xiaomi Mi IO devices
 * and their token
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.miio")
public class MiIoDiscovery extends AbstractDiscoveryService {

    /** The refresh interval for background discovery */
    private static final long SEARCH_INTERVAL = 600;
    private static final int BUFFER_LENGTH = 1024;
    private static final int DISCOVERY_TIME = 10;

    private ScheduledFuture<?> miIoDiscoveryJob;
    protected DatagramSocket clientSocket;
    private Thread socketReceiveThread;
    Set<String> responseIps = new HashSet<String>();

    private final Logger logger = LoggerFactory.getLogger(MiIoDiscovery.class);

    public MiIoDiscovery() throws IllegalArgumentException {
        super(DISCOVERY_TIME);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start Xiaomi Mi IO background discovery");
        if (miIoDiscoveryJob == null || miIoDiscoveryJob.isCancelled()) {
            miIoDiscoveryJob = scheduler.scheduleWithFixedDelay(() -> discover(), 0, SEARCH_INTERVAL, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop Xiaomi  Mi IO background discovery");
        if (miIoDiscoveryJob != null && !miIoDiscoveryJob.isCancelled()) {
            miIoDiscoveryJob.cancel(true);
            miIoDiscoveryJob = null;
        }
    }

    @Override
    protected void deactivate() {
        stopReceiverThreat();
        if (clientSocket != null) {
            clientSocket.close();
            clientSocket = null;
        }
        super.deactivate();
    }

    @Override
    protected void startScan() {
        logger.debug("Start Xiaomi Mi IO discovery");
        getSocket();
        logger.debug("Discovery using socket on port {}", clientSocket.getLocalPort());
        discover();
    }

    private void discover() {
        startReceiverThreat();
        responseIps = new HashSet<String>();
        HashSet<String> broadcastAddresses = new HashSet<String>();
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
        ThingUID uid = new ThingUID(THING_TYPE_MIIO, id);
        logger.debug("Discovered Mi Device {} ({}) at {} as {}", id, Long.parseUnsignedLong(id, 16), ip, uid);
        if (IGNORED_TOKENS.contains(token)) {
            logger.debug(
                    "No token discovered for device {}. For options how to get the token, check the binding readme.",
                    id);
            thingDiscovered(DiscoveryResultBuilder.create(uid).withProperty(PROPERTY_HOST_IP, ip)
                    .withProperty(PROPERTY_DID, id).withRepresentationProperty(id).withLabel(label).build());
        } else {
            logger.debug("Discovered token for device {}: {}", id, token);
            thingDiscovered(DiscoveryResultBuilder.create(uid).withProperty(PROPERTY_HOST_IP, ip)
                    .withProperty(PROPERTY_DID, id).withProperty(PROPERTY_TOKEN, token).withRepresentationProperty(id)
                    .withLabel(label + " with token").build());
        }
    }

    synchronized DatagramSocket getSocket() {
        if (clientSocket != null && clientSocket.isBound()) {
            return clientSocket;
        }
        try {
            logger.debug("Getting new socket for discovery");
            DatagramSocket clientSocket = new DatagramSocket();
            clientSocket.setReuseAddress(true);
            clientSocket.setBroadcast(true);
            this.clientSocket = clientSocket;
            return clientSocket;
        } catch (Exception e) {
            logger.debug("Error getting socket for discovery: {}", e.getMessage(), e);
        }
        return null;
    }

    private void closeSocket() {
        if (clientSocket == null) {
            return;
        }
        clientSocket.close();
        clientSocket = null;
    }

    private void sendDiscoveryRequest(String ipAddress) {
        try {
            byte[] sendData = DISCOVER_STRING;
            logger.trace("Discovery sending ping to {} from {}:{}", ipAddress, getSocket().getLocalAddress(),
                    getSocket().getLocalPort());
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipAddress),
                    PORT);
            for (int i = 1; i <= 1; i++) {
                getSocket().send(sendPacket);
            }
        } catch (Exception e) {
            logger.trace("Discovery on {} error: {}", ipAddress, e.getMessage());
        }
    }

    /**
     * starts the {@link ReceiverThread} thread
     */
    private synchronized void startReceiverThreat() {
        stopReceiverThreat();
        socketReceiveThread = new ReceiverThread();
        socketReceiveThread.start();
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
            logger.debug("Starting discovery receiver thread for socket on port {}", getSocket().getLocalPort());
            receiveData(getSocket());
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
