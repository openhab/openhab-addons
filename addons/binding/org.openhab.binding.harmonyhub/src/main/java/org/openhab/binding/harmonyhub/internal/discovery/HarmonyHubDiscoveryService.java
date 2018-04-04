/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.harmonyhub.internal.discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.harmonyhub.HarmonyHubBindingConstants;
import org.openhab.binding.harmonyhub.handler.HarmonyHubHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HarmonyHubDiscoveryService} class discovers Harmony hubs and adds the results to the inbox.
 *
 * @author Dan Cunningham - Initial contribution
 */
@Component(immediate = true, service = DiscoveryService.class, configurationPid = "discovery.harmonyhub")
public class HarmonyHubDiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(HarmonyHubDiscoveryService.class);

    // notice the port appended to the end of the string
    private static final String DISCOVERY_STRING = "_logitech-reverse-bonjour._tcp.local.\n%d";
    private static final int DISCOVERY_PORT = 5224;
    private static final int TIMEOUT = 15;
    private static final long REFRESH = 600;

    private ScheduledFuture<?> broadcastFuture;
    private ScheduledFuture<?> timeoutFuture;
    private ServerSocket serverSocket;
    private HarmonyServer server;
    private boolean running;

    private ScheduledFuture<?> discoveryFuture;

    public HarmonyHubDiscoveryService() {
        super(HarmonyHubHandler.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return HarmonyHubHandler.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public void startScan() {
        logger.debug("StartScan called");
        startDiscovery();
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start Harmony Hub background discovery");
        if (discoveryFuture == null || discoveryFuture.isCancelled()) {
            logger.debug("Start Scan");
            discoveryFuture = scheduler.scheduleWithFixedDelay(this::startScan, 0, REFRESH, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop HarmonyHub background discovery");
        if (discoveryFuture != null && !discoveryFuture.isCancelled()) {
            discoveryFuture.cancel(true);
            discoveryFuture = null;
        }
        stopDiscovery();
    }

    /**
     * Starts discovery for Harmony Hubs
     */
    private synchronized void startDiscovery() {
        if (running) {
            return;
        }

        try {
            serverSocket = new ServerSocket(0);
            logger.debug("Creating Harmony server on port {}", serverSocket.getLocalPort());
            server = new HarmonyServer(serverSocket);
            server.start();

            broadcastFuture = scheduler.scheduleWithFixedDelay(() -> {
                sendDiscoveryMessage(String.format(DISCOVERY_STRING, serverSocket.getLocalPort()));
            }, 0, 2, TimeUnit.SECONDS);

            timeoutFuture = scheduler.schedule(this::stopDiscovery, TIMEOUT, TimeUnit.SECONDS);

            running = true;
        } catch (IOException e) {
            logger.error("Could not start Harmony discovery server ", e);
        }
    }

    /**
     * Stops discovery of Harmony Hubs
     */
    private synchronized void stopDiscovery() {
        if (broadcastFuture != null) {
            broadcastFuture.cancel(true);
        }
        if (timeoutFuture != null) {
            broadcastFuture.cancel(true);
        }
        if (server != null) {
            server.setRunning(false);
        }
        try {
            serverSocket.close();
        } catch (Exception e) {
            logger.error("Could not stop harmony discovery socket", e);
        }
        running = false;
    }

    /**
     * Send broadcast message over all active interfaces
     *
     * @param discoverString
     *            String to be used for the discovery
     */
    private void sendDiscoveryMessage(String discoverString) {
        DatagramSocket bcSend = null;
        try {
            bcSend = new DatagramSocket();
            bcSend.setBroadcast(true);
            byte[] sendData = discoverString.getBytes();

            // Broadcast the message over all the network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress[] broadcast = new InetAddress[] { InetAddress.getByName("224.0.0.1"),
                            InetAddress.getByName("255.255.255.255"), interfaceAddress.getBroadcast() };
                    for (InetAddress bc : broadcast) {
                        // Send the broadcast package!
                        if (bc != null) {
                            try {
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, bc,
                                        DISCOVERY_PORT);
                                bcSend.send(sendPacket);
                            } catch (IOException e) {
                                logger.debug("IO error during HarmonyHub discovery: {}", e.getMessage());
                            } catch (Exception e) {
                                logger.debug("{}", e.getMessage(), e);
                            }
                            logger.trace("Request packet sent to: {} Interface: {}", bc.getHostAddress(),
                                    networkInterface.getDisplayName());
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.debug("IO error during HarmonyHub discovery: {}", e.getMessage());
        } finally {
            try {
                if (bcSend != null) {
                    bcSend.close();
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Server which accepts TCP connections from Harmony Hubs during the discovery process
     *
     * @author Dan Cunningham - Initial contribution
     *
     */
    private class HarmonyServer extends Thread {
        private ServerSocket serverSocket;
        private boolean running;
        private List<String> responses = new ArrayList<String>();

        public HarmonyServer(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
            running = true;
        }

        @Override
        public void run() {
            while (running) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String input;
                    while ((input = in.readLine()) != null) {
                        if (!running) {
                            break;
                        }
                        logger.trace("READ {}", input);
                        String propsString = input.replaceAll(";", "\n");
                        propsString = propsString.replaceAll(":", "=");
                        Properties props = new Properties();
                        props.load(new StringReader(propsString));
                        if (!responses.contains(props.getProperty("friendlyName"))) {
                            responses.add(props.getProperty("friendlyName"));
                            hubDiscovered(props.getProperty("ip"),
                                    props.getProperty("host_name").replaceAll("[^A-Za-z0-9\\-_]", ""),
                                    props.getProperty("friendlyName"));
                        }
                    }
                } catch (IOException e) {
                    if (running) {
                        logger.debug("Error connecting with found hub", e);
                    }
                } finally {
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (IOException e) {
                        logger.warn("could not close socket", e);
                    }
                }
            }
        }

        public void setRunning(boolean running) {
            this.running = running;
        }
    }

    private void hubDiscovered(String host, String id, String friendlyName) {
        logger.trace("Adding HarmonyHub {} ({}) at host {}", friendlyName, id, host);
        Map<String, Object> properties = new HashMap<>(2);
        properties.put("name", friendlyName);
        properties.put("host", host);

        ThingUID uid = new ThingUID(HarmonyHubBindingConstants.HARMONY_HUB_THING_TYPE,
                id.replaceAll("[^A-Za-z0-9\\-_]", ""));
        thingDiscovered(DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withLabel("HarmonyHub " + friendlyName).build());
    }
}
