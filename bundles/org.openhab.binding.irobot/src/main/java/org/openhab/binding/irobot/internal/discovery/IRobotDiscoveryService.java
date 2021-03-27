/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.irobot.internal.discovery;

import java.io.IOException;
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.irobot.internal.IRobotBindingConstants;
import org.openhab.binding.irobot.internal.dto.IdentProtocol;
import org.openhab.binding.irobot.internal.dto.IdentProtocol.IdentData;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetUtil;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * Discovery service for iRobots
 *
 * @author Pavel Fedin - Initial contribution
 *
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.irobot")
@NonNullByDefault
public class IRobotDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(IRobotDiscoveryService.class);
    private final Runnable scanner;
    private @Nullable ScheduledFuture<?> backgroundFuture;

    public IRobotDiscoveryService() {
        super(Collections.singleton(IRobotBindingConstants.THING_TYPE_ROOMBA), 30, true);
        scanner = createScanner();
    }

    @Override
    protected void startBackgroundDiscovery() {
        stopBackgroundScan();
        backgroundFuture = scheduler.scheduleWithFixedDelay(scanner, 0, 60, TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopBackgroundScan();
        super.stopBackgroundDiscovery();
    }

    private void stopBackgroundScan() {
        ScheduledFuture<?> scan = backgroundFuture;

        if (scan != null) {
            scan.cancel(true);
            backgroundFuture = null;
        }
    }

    @Override
    protected void startScan() {
        scheduler.execute(scanner);
    }

    private Runnable createScanner() {
        return () -> {
            long timestampOfLastScan = getTimestampOfLastScan();
            for (InetAddress broadcastAddress : getBroadcastAddresses()) {
                logger.debug("Starting broadcast for {}", broadcastAddress.toString());

                try (DatagramSocket socket = IdentProtocol.sendRequest(broadcastAddress)) {
                    DatagramPacket incomingPacket;

                    while ((incomingPacket = receivePacket(socket)) != null) {
                        discover(incomingPacket);
                    }
                } catch (IOException e) {
                    logger.warn("Error sending broadcast: {}", e.toString());
                }
            }

            removeOlderResults(timestampOfLastScan);
        };
    }

    private List<InetAddress> getBroadcastAddresses() {
        ArrayList<InetAddress> addresses = new ArrayList<>();

        for (String broadcastAddress : NetUtil.getAllBroadcastAddresses()) {
            try {
                addresses.add(InetAddress.getByName(broadcastAddress));
            } catch (UnknownHostException e) {
                // The broadcastAddress is supposed to be raw IP, not a hostname, like 192.168.0.255.
                // Getting UnknownHost on it would be totally strange, some internal system error.
                logger.warn("Error broadcasting to {}: {}", broadcastAddress, e.getMessage());
            }
        }

        return addresses;
    }

    private @Nullable DatagramPacket receivePacket(DatagramSocket socket) {
        try {
            return IdentProtocol.receiveResponse(socket);
        } catch (IOException e) {
            // This is not really an error, eventually we get a timeout
            // due to a loop in the caller
            return null;
        }
    }

    private void discover(DatagramPacket incomingPacket) {
        String host = incomingPacket.getAddress().toString().substring(1);
        String reply = new String(incomingPacket.getData(), StandardCharsets.UTF_8);

        logger.trace("Received IDENT from {}: {}", host, reply);

        IdentProtocol.IdentData ident;

        try {
            ident = IdentProtocol.decodeResponse(reply);
        } catch (JsonParseException e) {
            logger.warn("Malformed IDENT reply from {}!", host);
            return;
        }

        // This check comes from Roomba980-Python
        if (ident.ver < IdentData.MIN_SUPPORTED_VERSION) {
            logger.warn("Found unsupported iRobot \"{}\" version {} at {}", ident.robotname, ident.ver, host);
            return;
        }

        if (ident.product.equals(IdentData.PRODUCT_ROOMBA)) {
            ThingUID thingUID = new ThingUID(IRobotBindingConstants.THING_TYPE_ROOMBA, host.replace('.', '_'));
            DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withProperty("ipaddress", host)
                    .withRepresentationProperty("ipaddress").withLabel("iRobot " + ident.robotname).build();

            thingDiscovered(result);
        }
    }
}
