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
package org.openhab.binding.oppo.internal.discovery;

import static org.openhab.binding.oppo.internal.OppoBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery class for the Oppo Blu-ray Player line.
 * The player sends SDDP packets continuously for us to discover.
 *
 * @author Tim Roberts - Initial contribution
 * @author Michael Lobstein - Adapted for the Oppo binding
 */

@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.oppo")
public class OppoDiscoveryService extends AbstractDiscoveryService {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_PLAYER);

    private final Logger logger = LoggerFactory.getLogger(OppoDiscoveryService.class);

    /**
     * Address SDDP broadcasts on
     */
    private static final String SDDP_ADDR = "239.255.255.251";

    /**
     * Port number SDDP uses
     */
    private static final int SDDP_PORT = 7624;

    /**
     * SDDP packet should be only 512 in size - make it 600 to give us some room
     */
    private static final int BUFFER_SIZE = 600;

    /**
     * Socket read timeout (in ms) - allows us to shutdown the listening every TIMEOUT
     */
    private static final int TIMEOUT_MS = 1000;

    /**
     * Whether we are currently scanning or not
     */
    private boolean scanning;

    /**
     * The {@link ExecutorService} to run the listening threads on.
     */
    private @Nullable ExecutorService executorService;

    private static final String DISPLAY_NAME_83 = "OPPO BDP-83/93/95";
    private static final String DISPLAY_NAME_103 = "OPPO BDP-103";
    private static final String DISPLAY_NAME_105 = "OPPO BDP-105";

    /**
     * Constructs the discovery class using the thing IDs that we can discover.
     */
    public OppoDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 30, false);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    /**
     * {@inheritDoc}
     *
     * Starts the scan. This discovery will:
     * <ul>
     * <li>Request all the network interfaces</li>
     * <li>For each network interface, create a listening thread using {@link #executorService}</li>
     * <li>Each listening thread will open up a {@link MulticastSocket} using {@link #SDDP_ADDR} and {@link #SDDP_PORT}
     * and
     * will receive any {@link DatagramPacket} that comes in</li>
     * <li>The {@link DatagramPacket} is then investigated to see if is a SDDP packet and will create a new thing from
     * it</li>
     * </ul>
     * The process will continue until {@link #stopScan()} is called.
     */
    @Override
    protected void startScan() {
        if (executorService != null) {
            stopScan();
        }

        logger.debug("Starting Discovery");

        try {
            final InetAddress addr = InetAddress.getByName(SDDP_ADDR);
            final List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            final ExecutorService service = Executors.newFixedThreadPool(networkInterfaces.size());
            executorService = service;

            scanning = true;
            for (final NetworkInterface netint : networkInterfaces) {

                service.execute(() -> {
                    try {
                        MulticastSocket multiSocket = new MulticastSocket(SDDP_PORT);
                        multiSocket.setSoTimeout(TIMEOUT_MS);
                        multiSocket.setNetworkInterface(netint);
                        multiSocket.joinGroup(addr);

                        while (scanning) {
                            DatagramPacket receivePacket = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
                            try {
                                multiSocket.receive(receivePacket);

                                String message = new String(receivePacket.getData(), StandardCharsets.US_ASCII).trim();
                                if (message.length() > 0) {
                                    messageReceive(message);
                                }
                            } catch (SocketTimeoutException e) {
                                // ignore
                            }
                        }

                        multiSocket.close();
                    } catch (IOException e) {
                        if (e.getMessage() != null && !e.getMessage().contains("No IP addresses bound to interface")) {
                            logger.debug("OppoDiscoveryService IOException: {}", e.getMessage(), e);
                        }
                    }
                });
            }
        } catch (IOException e) {
            logger.debug("OppoDiscoveryService IOException: {}", e.getMessage(), e);
        }
    }

    /**
     * SDDP message has the following format
     *
     * <pre>
     * Notify: OPPO Player Start
     * Server IP: 192.168.0.2
     * Server Port: 23
     * Server Name: OPPO UDP-203
     * </pre>
     *
     *
     * @param message possibly null, possibly empty SDDP message
     */
    private void messageReceive(String message) {
        if (message.trim().length() == 0) {
            return;
        }

        String host = null;
        String port = null;
        Integer model = null;
        String displayName = null;

        for (String msg : message.split("\n")) {
            String[] line = msg.split(":");

            if (line.length == 2) {
                if (line[0].contains("Server IP")) {
                    host = line[1].trim();
                }

                if (line[0].contains("Server Port")) {
                    port = line[1].trim();
                }

                if (line[0].contains("Server Name")) {
                    // example: "OPPO UDP-203"
                    // note: Server Name only provided on UDP models, not present on BDP models
                    displayName = line[1].trim();
                }
            } else {
                logger.debug("messageReceive() - Unable to process line: {}", msg);
            }
        }

        // by looking at the port number we can mostly determine what the model number is
        if (host != null && port != null) {
            if (BDP83_PORT.toString().equals(port)) {
                model = MODEL83;
                displayName = DISPLAY_NAME_83;
            } else if (BDP10X_PORT.toString().equals(port)) {
                // The older models do not have the "Server Name" in the discovery packet
                // for the 10x we need to get the DLNA service list page and find modelNumber there
                // in order to determine if this is a BDP-103 or BDP-105
                try {
                    String result = HttpUtil.executeUrl("GET", "http://" + host + ":2870/dmr.xml", 5000);

                    if (result != null && result.contains("<modelName>OPPO BDP-103</modelName>")) {
                        model = MODEL103;
                        displayName = DISPLAY_NAME_103;
                    } else if (result != null && result.contains("<modelName>OPPO BDP-105</modelName>")) {
                        model = MODEL105;
                        displayName = DISPLAY_NAME_105;
                    } else {
                        model = MODEL103;
                        displayName = DISPLAY_NAME_103;
                    }
                } catch (IOException e) {
                    logger.debug("Error getting player DLNA info page: {}", e.getMessage());
                    // the call failed for some reason, just assume we are a 103
                    model = MODEL103;
                    displayName = DISPLAY_NAME_103;
                }
            } else if (BDP20X_PORT.toString().equals(port)) {
                if (displayName != null && displayName.contains(Integer.toString(MODEL203))) {
                    model = MODEL203;
                } else if (displayName != null && displayName.contains(Integer.toString(MODEL205))) {
                    model = MODEL205;
                } else {
                    model = MODEL203;
                    displayName = "Unknown OPPO UDP player";
                }
            }

            if (model != null) {
                ThingUID uid = new ThingUID(THING_TYPE_PLAYER, host.replace(".", "_"));
                HashMap<String, Object> properties = new HashMap<>();
                properties.put("model", model);
                properties.put("host", host);

                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                        .withRepresentationProperty("host").withLabel(displayName + " (" + host + ")").build();

                this.thingDiscovered(result);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Stops the discovery scan. We set {@link #scanning} to false (allowing the listening threads to end naturally
     * within {@link #TIMEOUT) * 5 time then shutdown the {@link #executorService}
     */
    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        ExecutorService service = executorService;
        if (service == null) {
            return;
        }

        scanning = false;

        try {
            service.awaitTermination(TIMEOUT_MS * 5, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        service.shutdown();
        executorService = null;
    }
}
