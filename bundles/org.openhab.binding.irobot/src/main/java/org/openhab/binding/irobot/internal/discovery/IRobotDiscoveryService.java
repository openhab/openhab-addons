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
package org.openhab.binding.irobot.internal.discovery;

import static org.openhab.binding.irobot.internal.IRobotBindingConstants.THING_TYPE_ROOMBA;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.UDP_PORT;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.UNKNOWN;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.irobot.internal.dto.MQTTProtocol.DiscoveryResponse;
import org.openhab.binding.irobot.internal.utils.LoginRequester;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetUtil;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

/**
 * Discovery service for iRobots. The {@link LoginRequester#getBlid} and
 * {@link IRobotDiscoveryService} are heavily related to each other.
 *
 * @author Pavel Fedin - Initial contribution
 * @author Alexander Falkenstern - Add support for I7 series
 *
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.irobot")
public class IRobotDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(IRobotDiscoveryService.class);

    private final Gson gson = new Gson();

    private final Runnable scanner;
    private @Nullable ScheduledFuture<?> backgroundFuture;

    public IRobotDiscoveryService() {
        super(Collections.singleton(THING_TYPE_ROOMBA), 30, true);

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
            Set<String> robots = new HashSet<>();
            long timestampOfLastScan = getTimestampOfLastScan();
            for (InetAddress broadcastAddress : getBroadcastAddresses()) {
                logger.debug("Starting broadcast for {}", broadcastAddress.toString());

                final byte[] bRequest = "irobotmcs".getBytes(StandardCharsets.UTF_8);
                DatagramPacket request = new DatagramPacket(bRequest, bRequest.length, broadcastAddress, UDP_PORT);
                try (DatagramSocket socket = new DatagramSocket()) {
                    socket.setSoTimeout(1000); // One second
                    socket.setReuseAddress(true);
                    socket.setBroadcast(true);
                    socket.send(request);

                    byte @Nullable [] reply = null;
                    while ((reply = receive(socket)) != null) {
                        robots.add(new String(reply, StandardCharsets.UTF_8));
                    }
                } catch (IOException exception) {
                    logger.debug("Error sending broadcast: {}", exception.toString());
                }
            }

            for (final String json : robots) {

                JsonReader jsonReader = new JsonReader(new StringReader(json));
                DiscoveryResponse msg = gson.fromJson(jsonReader, DiscoveryResponse.class);

                // Only firmware version 2 and above are supported via MQTT, therefore check it
                if ((msg.ver != null) && (Integer.parseInt(msg.ver) > 1) && "mqtt".equalsIgnoreCase(msg.proto)) {
                    final String address = msg.ip;
                    final String mac = msg.mac;
                    final String sku = msg.sku;
                    if (!address.isEmpty() && !sku.isEmpty() && !mac.isEmpty()) {
                        ThingUID thingUID = new ThingUID(THING_TYPE_ROOMBA, mac.replace(":", ""));
                        DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID);
                        builder = builder.withProperty("mac", mac).withRepresentationProperty("mac");
                        builder = builder.withProperty("ipaddress", address);

                        String name = msg.robotname;
                        builder = builder.withLabel("iRobot " + (!name.isEmpty() ? name : UNKNOWN));
                        thingDiscovered(builder.build());
                    }
                }
            }

            removeOlderResults(timestampOfLastScan);
        };
    }

    private byte @Nullable [] receive(DatagramSocket socket) {
        try {
            final byte[] bReply = new byte[1024];
            DatagramPacket reply = new DatagramPacket(bReply, bReply.length);
            socket.receive(reply);
            return Arrays.copyOfRange(reply.getData(), reply.getOffset(), reply.getLength());
        } catch (IOException exception) {
            // This is not really an error, eventually we get a timeout due to a loop in the caller
            return null;
        }
    }

    private List<InetAddress> getBroadcastAddresses() {
        ArrayList<InetAddress> addresses = new ArrayList<>();

        for (String broadcastAddress : NetUtil.getAllBroadcastAddresses()) {
            try {
                addresses.add(InetAddress.getByName(broadcastAddress));
            } catch (UnknownHostException exception) {
                // The broadcastAddress is supposed to be raw IP, not a hostname, like 192.168.0.255.
                // Getting UnknownHost on it would be totally strange, some internal system error.
                logger.warn("Error broadcasting to {}: {}", broadcastAddress, exception.getMessage());
            }
        }

        return addresses;
    }
}
