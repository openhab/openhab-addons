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
package org.openhab.binding.pilight.internal.discovery;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pilight.internal.PilightBindingConstants;
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
 * The {@link PilightBridgeDiscoveryService} is responsible for discovering new pilight daemons on the network
 * by sending a ssdp multicast request via udp.
 *
 * @author Niklas Dörfler - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.pilight")
public class PilightBridgeDiscoveryService extends AbstractDiscoveryService {

    private static final int AUTODISCOVERY_SEARCH_TIME_SEC = 5;
    private static final int AUTODISCOVERY_BACKGROUND_SEARCH_INTERVAL_SEC = 60 * 10;

    private static final String SSDP_DISCOVERY_REQUEST_MESSAGE = "M-SEARCH * HTTP/1.1\r\n"
            + "Host:239.255.255.250:1900\r\n" + "ST:urn:schemas-upnp-org:service:pilight:1\r\n"
            + "Man:\"ssdp:discover\"\r\n" + "MX:3\r\n\r\n";
    public static final String SSDP_MULTICAST_ADDRESS = "239.255.255.250";
    public static final int SSDP_PORT = 1900;
    public static final int SSDP_WAIT_TIMEOUT = 2000; // in milliseconds

    private final Logger logger = LoggerFactory.getLogger(PilightBridgeDiscoveryService.class);

    private @Nullable ScheduledFuture<?> backgroundDiscoveryJob;

    public PilightBridgeDiscoveryService() throws IllegalArgumentException {
        super(getSupportedThingTypeUIDs(), AUTODISCOVERY_SEARCH_TIME_SEC, true);
    }

    public static Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(PilightBindingConstants.THING_TYPE_BRIDGE);
    }

    @Override
    protected void startScan() {
        logger.debug("Pilight bridge discovery scan started");
        removeOlderResults(getTimestampOfLastScan());
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nic : interfaces) {
                Enumeration<InetAddress> inetAddresses = nic.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        DatagramSocket ssdp = new DatagramSocket(
                                new InetSocketAddress(inetAddress.getHostAddress(), 0));
                        byte[] buff = SSDP_DISCOVERY_REQUEST_MESSAGE.getBytes(StandardCharsets.UTF_8);
                        DatagramPacket sendPack = new DatagramPacket(buff, buff.length);
                        sendPack.setAddress(InetAddress.getByName(SSDP_MULTICAST_ADDRESS));
                        sendPack.setPort(SSDP_PORT);
                        ssdp.send(sendPack);
                        ssdp.setSoTimeout(SSDP_WAIT_TIMEOUT);

                        boolean loop = true;
                        while (loop) {
                            DatagramPacket recvPack = new DatagramPacket(new byte[1024], 1024);
                            ssdp.receive(recvPack);
                            byte[] recvData = recvPack.getData();

                            final Scanner scanner = new Scanner(new ByteArrayInputStream(recvData),
                                    StandardCharsets.UTF_8);
                            loop = scanner.findAll("Location:([0-9.]+):(.*)").peek(matchResult -> {
                                final String server = matchResult.group(1);
                                final Integer port = Integer.parseInt(matchResult.group(2));
                                final String bridgeName = server.replace(".", "") + "" + port;

                                logger.debug("Found pilight daemon at {}:{}", server, port);

                                Map<String, Object> properties = new HashMap<>();
                                properties.put(PilightBindingConstants.PROPERTY_IP_ADDRESS, server);
                                properties.put(PilightBindingConstants.PROPERTY_PORT, port);
                                properties.put(PilightBindingConstants.PROPERTY_NAME, bridgeName);

                                ThingUID uid = new ThingUID(PilightBindingConstants.THING_TYPE_BRIDGE, bridgeName);

                                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                                        .withRepresentationProperty(PilightBindingConstants.PROPERTY_NAME)
                                        .withLabel("Pilight Bridge (" + server + ")").build();

                                thingDiscovered(result);
                            }).count() == 0;
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (e.getMessage() != null && !"Receive timed out".equals(e.getMessage())) {
                logger.warn("Unable to enumerate the local network interfaces {}", e.getMessage());
            }
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start Pilight device background discovery");
        final @Nullable ScheduledFuture<?> backgroundDiscoveryJob = this.backgroundDiscoveryJob;
        if (backgroundDiscoveryJob == null || backgroundDiscoveryJob.isCancelled()) {
            this.backgroundDiscoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, 5,
                    AUTODISCOVERY_BACKGROUND_SEARCH_INTERVAL_SEC, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop Pilight device background discovery");
        final @Nullable ScheduledFuture<?> backgroundDiscoveryJob = this.backgroundDiscoveryJob;
        if (backgroundDiscoveryJob != null) {
            backgroundDiscoveryJob.cancel(true);
            this.backgroundDiscoveryJob = null;
        }
    }
}
