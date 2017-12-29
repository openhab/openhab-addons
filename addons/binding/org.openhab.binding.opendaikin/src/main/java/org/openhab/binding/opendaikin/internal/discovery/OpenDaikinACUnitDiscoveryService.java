/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opendaikin.internal.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.core.net.NetUtil;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.opendaikin.OpenDaikinBindingConstants;
import org.openhab.binding.opendaikin.internal.OpenDaikinWebTargets;
import org.openhab.binding.opendaikin.internal.config.OpenDaikinConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for Daikin AC units.
 *
 * @author Tim Waterhouse <tim@timwaterhouse.com> - Initial contribution
 *
 */
public class OpenDaikinACUnitDiscoveryService extends AbstractDiscoveryService {
    private static final String UDP_PACKET_CONTENTS = "DAIKIN_UDP/common/basic_info";
    private static final int REMOTE_UDP_PORT = 30050;

    private Logger logger = LoggerFactory.getLogger(OpenDaikinACUnitDiscoveryService.class);

    private final Runnable scanner;
    private ScheduledFuture<?> backgroundFuture;

    private DiscoveryServiceCallback discoveryServiceCallback;

    public OpenDaikinACUnitDiscoveryService() {
        super(Collections.singleton(OpenDaikinBindingConstants.THING_TYPE_AC_UNIT), 600, true);
        scanner = createScanner();
    }

    @Override
    protected void startScan() {
        scheduler.execute(scanner);
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting background discovery");

        if (backgroundFuture != null && !backgroundFuture.isDone()) {
            backgroundFuture.cancel(true);
            backgroundFuture = null;
        }
        backgroundFuture = scheduler.scheduleWithFixedDelay(scanner, 0, 60, TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (backgroundFuture != null && !backgroundFuture.isDone()) {
            backgroundFuture.cancel(true);
            backgroundFuture = null;
        }

        super.stopBackgroundDiscovery();
    }

    private Runnable createScanner() {
        return () -> {
            long timestampOfLastScan = getTimestampOfLastScan();
            for (InetAddress broadcastAddress : getBroadcastAddresses()) {
                logger.debug("Starting broadcast for {}", broadcastAddress.toString());

                try (DatagramSocket socket = new DatagramSocket()) {
                    socket.setBroadcast(true);
                    socket.setReuseAddress(true);
                    byte[] packetContents = UDP_PACKET_CONTENTS.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket packet = new DatagramPacket(packetContents, packetContents.length, broadcastAddress,
                            REMOTE_UDP_PORT);

                    // Send before listening in case the port isn't bound until here.
                    socket.send(packet);

                    while (receivePacketAndDiscover(socket)) {
                    }
                } catch (Exception e) {
                    // Nothing to do here - the host couldn't be found, likely because it doesn't exist
                }
            }

            removeOlderResults(timestampOfLastScan);
        };
    }

    private boolean receivePacketAndDiscover(DatagramSocket socket) {
        try {
            // Use a one byte buffer since we don't really care about the contents.
            byte[] buffer = new byte[1];
            DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
            socket.setSoTimeout(1000 /* one second */);
            socket.receive(incomingPacket);

            String host = incomingPacket.getAddress().toString().substring(1);
            logger.debug("Received packet from {}", host);
            new OpenDaikinWebTargets(ClientBuilder.newClient(), host).getControlInfo();

            ThingUID thingUID = new ThingUID(OpenDaikinBindingConstants.THING_TYPE_AC_UNIT, host.replace('.', '_'));
            DiscoveryResult result = DiscoveryResultBuilder.create(thingUID)
                    .withProperty(OpenDaikinConfiguration.HOST, host).withLabel("Daikin AC Unit (" + host + ")")
                    .build();

            logger.debug("Successfully discovered host {}", host);
            thingDiscovered(result);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private List<InetAddress> getBroadcastAddresses() {
        ArrayList<InetAddress> addresses = new ArrayList<>();

        for (String broadcastAddress : NetUtil.getAllBroadcastAddresses()) {
            try {
                addresses.add(InetAddress.getByName(broadcastAddress));
            } catch (UnknownHostException e) {
            }
        }

        return addresses;
    }
}
