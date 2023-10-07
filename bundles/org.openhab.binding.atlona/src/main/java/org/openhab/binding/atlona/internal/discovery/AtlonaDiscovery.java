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
package org.openhab.binding.atlona.internal.discovery;

import static org.openhab.binding.atlona.internal.AtlonaBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.atlona.internal.pro3.AtlonaPro3Config;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery class for the Atlona PRO3 line. The PRO3 line uses SDDP (simple device discovery protocol) for discovery
 * (similar to UPNP but defined by Control4). The user should start the discovery process in openhab and then log into
 * the switch, go to the Network options and press the SDDP button (which initiates the SDDP conversation).
 *
 * @author Tim Roberts - Initial contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.atlona")
public class AtlonaDiscovery extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(AtlonaDiscovery.class);

    /**
     * Address SDDP broadcasts on
     */
    private static final String SDDP_ADDR = "239.255.255.250";

    /**
     * Port number SDDP uses
     */
    private static final int SDDP_PORT = 1902;

    /**
     * SDDP packet should be only 512 in size - make it 600 to give us some room
     */
    private static final int BUFFER_SIZE = 600;

    /**
     * Socket read timeout (in ms) - allows us to shutdown the listening every TIMEOUT
     */
    private static final int TIMEOUT = 1000;

    /**
     * Whether we are currently scanning or not
     */
    private boolean scanning;

    /**
     * The {@link ExecutorService} to run the listening threads on.
     */
    private ExecutorService executorService;

    /**
     * Constructs the discovery class using the thing IDs that we can discover.
     */
    public AtlonaDiscovery() {
        super(Collections.unmodifiableSet(
                Stream.of(THING_TYPE_PRO3_44M, THING_TYPE_PRO3_66M, THING_TYPE_PRO3_88M, THING_TYPE_PRO3_1616M)
                        .collect(Collectors.toSet())),
                30, false);
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

            executorService = Executors.newFixedThreadPool(networkInterfaces.size());
            scanning = true;
            for (final NetworkInterface netint : networkInterfaces) {

                executorService.execute(() -> {
                    try {
                        MulticastSocket multiSocket = new MulticastSocket(SDDP_PORT);
                        multiSocket.setSoTimeout(TIMEOUT);
                        multiSocket.setNetworkInterface(netint);
                        multiSocket.joinGroup(addr);

                        while (scanning) {
                            DatagramPacket receivePacket = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
                            try {
                                multiSocket.receive(receivePacket);

                                String message = new String(receivePacket.getData()).trim();
                                if (message.length() > 0) {
                                    messageReceive(message);
                                }
                            } catch (SocketTimeoutException e) {
                                // ignore
                            }
                        }

                        multiSocket.close();
                    } catch (Exception e) {
                        if (!e.getMessage().contains("No IP addresses bound to interface")) {
                            logger.debug("Error getting ip addresses: {}", e.getMessage(), e);
                        }
                    }
                });
            }
        } catch (IOException e) {
            logger.debug("Error getting ip addresses: {}", e.getMessage(), e);
        }
    }

    /**
     * SDDP message has the following format
     *
     * <pre>
     * NOTIFY ALIVE SDDP/1.0
     * From: "192.168.1.30:1902"
     * Host: "AT-UHD-PRO3-88M_B898B0030F4D"
     * Type: "AT-UHD-PRO3-88M"
     * Max-Age: 1800
     * Primary-Proxy: "avswitch"
     * Proxies: "avswitch"
     * Manufacturer: "Atlona"
     * Model: "AT-UHD-PRO3-88M"
     * Driver: "avswitch_Atlona_AT-UHD-PRO3-88M_IP.c4i"
     * Config-URL: "http://192.168.1.30/"
     * </pre>
     *
     * First parse the manufacturer, host, model and IP address from the message. For the "Host" field, we parse out the
     * serial #. For the From field, we parse out the IP address (minus the port #). If we successfully found all four
     * and the manufacturer is "Atlona" and it's a model we recognize, we then create our thing from it.
     *
     * @param message possibly null, possibly empty SDDP message
     */
    private void messageReceive(String message) {
        if (message == null || message.trim().length() == 0) {
            return;
        }

        String host = null;
        String model = null;
        String from = null;
        String manufacturer = null;

        for (String msg : message.split("\r\n")) {
            int idx = msg.indexOf(':');
            if (idx > 0) {
                String name = msg.substring(0, idx);

                if ("Host".equalsIgnoreCase(name)) {
                    host = msg.substring(idx + 1).trim().replace("\"", "");
                    int sep = host.indexOf('_');
                    if (sep >= 0) {
                        host = host.substring(sep + 1);
                    }
                } else if ("Model".equalsIgnoreCase(name)) {
                    model = msg.substring(idx + 1).trim().replace("\"", "");
                } else if ("Manufacturer".equalsIgnoreCase(name)) {
                    manufacturer = msg.substring(idx + 1).trim().replace("\"", "");
                } else if ("From".equalsIgnoreCase(name)) {
                    from = msg.substring(idx + 1).trim().replace("\"", "");
                    int sep = from.indexOf(':');
                    if (sep >= 0) {
                        from = from.substring(0, sep);
                    }
                }
            }

        }

        if (!"Atlona".equalsIgnoreCase(manufacturer)) {
            return;
        }

        if (host != null && model != null && from != null) {
            ThingTypeUID typeId = null;
            if ("AT-UHD-PRO3-44M".equalsIgnoreCase(model)) {
                typeId = THING_TYPE_PRO3_44M;
            } else if ("AT-UHD-PRO3-66M".equalsIgnoreCase(model)) {
                typeId = THING_TYPE_PRO3_66M;
            } else if ("AT-UHD-PRO3-88M".equalsIgnoreCase(model)) {
                typeId = THING_TYPE_PRO3_88M;
            } else if ("AT-UHD-PRO3-1616M".equalsIgnoreCase(model)) {
                typeId = THING_TYPE_PRO3_1616M;
            } else {
                logger.warn("Unknown model #: {}", model);
            }

            if (typeId != null) {
                logger.debug("Creating binding for {} ({})", model, from);
                ThingUID j = new ThingUID(typeId, host);

                Map<String, Object> properties = new HashMap<>(1);
                properties.put(AtlonaPro3Config.IP_ADDRESS, from);
                DiscoveryResult result = DiscoveryResultBuilder.create(j).withProperties(properties)
                        .withLabel(model + " (" + from + ")").build();
                thingDiscovered(result);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Stops the discovery scan. We set {@link #scanning} to false (allowing the listening threads to end naturally
     * within {@link #TIMEOUT} * 5 time then shutdown the {@link ExecutorService}
     */
    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        if (executorService == null) {
            return;
        }

        scanning = false;

        try {
            executorService.awaitTermination(TIMEOUT * 5, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        executorService.shutdown();
        executorService = null;
    }
}
