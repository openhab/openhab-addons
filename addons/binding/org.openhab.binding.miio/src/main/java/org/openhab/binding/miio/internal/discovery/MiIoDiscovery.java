/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.internal.discovery;

import static org.openhab.binding.miio.MiIoBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.miio.internal.Message;
import org.openhab.binding.miio.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MiIoDiscovery} is responsible for discovering new Xiaomi Mi IO devices
 * and their token
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
public class MiIoDiscovery extends AbstractDiscoveryService implements ExtendedDiscoveryService {

    /** The refresh interval for background discovery */
    private static final long SEARCH_INTERVAL = 600;
    private static final int BUFFER_LENGTH = 1024;
    private static final int DISCOVERY_TIME = 10;
    private DiscoveryServiceCallback discoveryServiceCallback;

    private ScheduledFuture<?> miIoDiscoveryJob;
    protected DatagramSocket clientSocket;
    private Thread socketReceiveThread;
    Set<String> responseIps = new HashSet<String>();

    private final Logger logger = LoggerFactory.getLogger(MiIoDiscovery.class);

    public MiIoDiscovery() throws IllegalArgumentException {
        super(DISCOVERY_TIME);
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
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
        for (String broadcastAdress : getBroadcastAddresses()) {
            sendDiscoveryRequest(broadcastAdress);
        }
    }

    private void discovered(String ip, byte[] response) {
        logger.trace("Discovery responses from : {}:{}", ip, Utils.getSpacedHex(response));
        Message msg = new Message(response);
        String token = Utils.getHex(msg.getChecksum());
        String id = Utils.getHex(msg.getDeviceId());
        String label = "Xiaomi Mi IO Device " + id + " (" + Long.parseUnsignedLong(id, 16) + ")";
        ThingUID uid = new ThingUID(THING_TYPE_MIIO, id);
        // Test if the device is already known by specific ThingTypes. In that case don't use the generic thingType
        for (ThingTypeUID typeU : NONGENERIC_THING_TYPES_UIDS) {
            ThingUID thingUID = new ThingUID(typeU, id);
            Thing existingThing = discoveryServiceCallback.getExistingThing(thingUID);
            if (existingThing != null) {
                logger.trace("Mi IO device {} already exist as thing {}: {}.", id, thingUID.toString(),
                        existingThing.getLabel());
                uid = thingUID;
                break;
            }
            DiscoveryResult dr = discoveryServiceCallback.getExistingDiscoveryResult(thingUID);
            if (dr != null) {
                logger.debug("Mi IO device {} already discovered as type '{}': {}", id, dr.getThingTypeUID(),
                        dr.getLabel());
                uid = thingUID;
                label = dr.getLabel();
                break;
            }
        }
        logger.debug("Discovered Mi IO Device {} ({}) at {} as {}", id, Long.parseUnsignedLong(id, 16), ip, uid);
        if (IGNORED_TOLKENS.contains(token)) {
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

    /**
     * @return broadcast addresses for all interfaces
     */
    private Set<String> getBroadcastAddresses() {
        HashSet<String> broadcastAddresses = new HashSet<String>();
        try {
            broadcastAddresses.add("224.0.0.1");
            broadcastAddresses.add("224.0.0.50");
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                try {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                        continue;
                    }
                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        String address = interfaceAddress.getBroadcast().getHostAddress();
                        if (address != null) {
                            broadcastAddresses.add(address);
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        } catch (Exception e) {
            logger.trace("Error collecting broadcast addresses: {}", e.getMessage(), e);
        }
        return broadcastAddresses;
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
                                logger.debug("Error submitting discovered Mi Io device at {}", hostAddress, e);
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
