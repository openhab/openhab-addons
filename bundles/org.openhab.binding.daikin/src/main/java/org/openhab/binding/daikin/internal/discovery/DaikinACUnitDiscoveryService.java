/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.daikin.internal.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.daikin.internal.DaikinBindingConstants;
import org.openhab.binding.daikin.internal.DaikinCommunicationForbiddenException;
import org.openhab.binding.daikin.internal.DaikinHttpClientFactory;
import org.openhab.binding.daikin.internal.DaikinWebTargets;
import org.openhab.binding.daikin.internal.api.InfoParser;
import org.openhab.binding.daikin.internal.config.DaikinConfiguration;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetUtil;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for Daikin AC units.
 *
 * @author Tim Waterhouse - Initial contribution
 * @author Paul Smedley - Modifications to support Airbase Controllers
 *
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.daikin")
@NonNullByDefault
public class DaikinACUnitDiscoveryService extends AbstractDiscoveryService {
    private static final String UDP_PACKET_CONTENTS = "DAIKIN_UDP/common/basic_info";
    private static final int REMOTE_UDP_PORT = 30050;

    private Logger logger = LoggerFactory.getLogger(DaikinACUnitDiscoveryService.class);

    private @Nullable HttpClient httpClient;
    private @Nullable ScheduledFuture<?> backgroundFuture;

    public DaikinACUnitDiscoveryService() {
        super(Set.of(DaikinBindingConstants.THING_TYPE_AC_UNIT), 600, true);
    }

    @Override
    protected void startScan() {
        scheduler.execute(this::scanner);
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.trace("Starting background discovery");

        if (backgroundFuture != null && !backgroundFuture.isDone()) {
            backgroundFuture.cancel(true);
            backgroundFuture = null;
        }
        backgroundFuture = scheduler.scheduleWithFixedDelay(this::scanner, 0, 60, TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (backgroundFuture != null && !backgroundFuture.isDone()) {
            backgroundFuture.cancel(true);
            backgroundFuture = null;
        }

        super.stopBackgroundDiscovery();
    }

    private void scanner() {
        long timestampOfLastScan = getTimestampOfLastScan();
        for (InetAddress broadcastAddress : getBroadcastAddresses()) {
            logger.trace("Starting broadcast for {}", broadcastAddress.toString());

            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                socket.setReuseAddress(true);
                byte[] packetContents = UDP_PACKET_CONTENTS.getBytes(StandardCharsets.UTF_8);
                DatagramPacket packet = new DatagramPacket(packetContents, packetContents.length, broadcastAddress,
                        REMOTE_UDP_PORT);

                // Send before listening in case the port isn't bound until here.
                socket.send(packet);

                // receivePacketAndDiscover will return false if no packet is received after 1 second
                while (receivePacketAndDiscover(socket)) {
                }
            } catch (Exception e) {
                // Nothing to do here - the host couldn't be found, likely because it doesn't exist
            }
        }

        removeOlderResults(timestampOfLastScan);
    }

    private boolean receivePacketAndDiscover(DatagramSocket socket) {
        try {
            byte[] buffer = new byte[512];
            DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
            socket.setSoTimeout(1000 /* one second */);
            socket.receive(incomingPacket);

            String host = incomingPacket.getAddress().toString().substring(1);
            String data = new String(incomingPacket.getData(), 0, incomingPacket.getLength(), "US-ASCII");
            logger.trace("Received packet from {}: {}", host, data);

            Map<String, String> parsedData = InfoParser.parse(data);
            Boolean secure = "1".equals(parsedData.get("en_secure"));
            String thingId = Optional.ofNullable(parsedData.get("ssid")).orElse(host.replace(".", "_"));
            String mac = Optional.ofNullable(parsedData.get("mac")).orElse("");
            String uuid = mac.isEmpty() ? UUID.randomUUID().toString()
                    : UUID.nameUUIDFromBytes(mac.getBytes()).toString();

            DaikinWebTargets webTargets = new DaikinWebTargets(httpClient, host, secure, null);
            boolean found = false;

            // look for Daikin controller
            try {
                found = "OK".equals(webTargets.getBasicInfo().ret);
            } catch (DaikinCommunicationForbiddenException e) {
                // At this point, we don't have the adapter's key nor a uuid
                // so we're getting a Forbidden error
                // let's discover it and let the user configure the Key
                found = true;
            }
            if (found) {
                ThingUID thingUID = new ThingUID(DaikinBindingConstants.THING_TYPE_AC_UNIT, thingId);
                DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(DaikinConfiguration.HOST, host).withLabel("Daikin AC Unit (" + host + ")")
                        .withProperty(DaikinConfiguration.SECURE, secure)
                        .withRepresentationProperty(DaikinConfiguration.HOST);
                if (secure) {
                    resultBuilder = resultBuilder.withProperty(DaikinConfiguration.UUID, uuid);
                }
                DiscoveryResult result = resultBuilder.build();

                logger.trace("Successfully discovered host {}", host);
                thingDiscovered(result);
                return true;
            }
            // look for Daikin Airbase controller
            if ("OK".equals(webTargets.getAirbaseBasicInfo().ret)) {
                ThingUID thingUID = new ThingUID(DaikinBindingConstants.THING_TYPE_AIRBASE_AC_UNIT, thingId);
                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(DaikinConfiguration.HOST, host).withLabel("Daikin Airbase AC Unit (" + host + ")")
                        .withRepresentationProperty(DaikinConfiguration.HOST).build();

                logger.trace("Successfully discovered host {}", host);
                thingDiscovered(result);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        // Shouldn't get here unless we don't detect a controller.
        // Return true to continue with the next packet, which comes from another adapter
        return true;
    }

    private List<InetAddress> getBroadcastAddresses() {
        ArrayList<InetAddress> addresses = new ArrayList<>();

        for (String broadcastAddress : NetUtil.getAllBroadcastAddresses()) {
            try {
                addresses.add(InetAddress.getByName(broadcastAddress));
            } catch (UnknownHostException e) {
                logger.debug("Error broadcasting to {}", broadcastAddress, e);
            }
        }

        return addresses;
    }

    @Reference
    protected void setDaikinHttpClientFactory(final DaikinHttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getHttpClient();
    }
}
