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

import static org.openhab.binding.irobot.internal.IRobotBindingConstants.*;

import java.io.IOException;
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
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetUtil;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;

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

    private ParseContext jsonParser;

    private final Runnable scanner;
    private @Nullable ScheduledFuture<?> backgroundFuture;

    public IRobotDiscoveryService() {
        super(Collections.singleton(THING_TYPE_ROOMBA), 30, true);

        Configuration.ConfigurationBuilder builder = Configuration.builder();
        builder = builder.jsonProvider(new GsonJsonProvider());
        builder = builder.mappingProvider(new GsonMappingProvider());
        builder = builder.options(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS);
        jsonParser = JsonPath.using(builder.build());

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

            /*
             * JSON of the following contents (addresses are undisclosed):
             * @formatter:off
             * {
             *   "ver":"3",
             *   "hostname":"Roomba-<blid>",
             *   "robotname":"Roomba",
             *   "robotid":"<blid>", --> available on some models only
             *   "ip":"XXX.XXX.XXX.XXX",
             *   "mac":"XX:XX:XX:XX:XX:XX",
             *   "sw":"v2.4.6-3",
             *   "sku":"R981040",
             *   "nc":0,
             *   "proto":"mqtt",
             *   "cap":{
             *     "pose":1,
             *     "ota":2,
             *     "multiPass":2,
             *     "carpetBoost":1,
             *     "pp":1,
             *     "binFullDetect":1,
             *     "langOta":1,
             *     "maps":1,
             *     "edge":1,
             *     "eco":1,
             *     "svcConf":1
             *   }
             * }
             * @formatter:on
             */
            for (final String json : robots) {
                DocumentContext document = jsonParser.parse(json);

                // Only firmware version 2 and above are supported via MQTT, therefore check it
                final @Nullable Integer version = document.read("$.ver", Integer.class);
                final @Nullable String protocol = document.read("$.proto", String.class);
                if ((version != null) && (version > 1) && "mqtt".equalsIgnoreCase(protocol)) {
                    final String address = document.read("$.ip", String.class);
                    final String mac = document.read("$.mac", String.class);
                    final String sku = document.read("$.sku", String.class);
                    if (!address.isEmpty() && !sku.isEmpty() && !mac.isEmpty()) {
                        ThingUID thingUID = new ThingUID(THING_TYPE_ROOMBA, mac.replace(":", ""));
                        DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID);
                        builder = builder.withProperty("mac", mac).withRepresentationProperty("mac");

                        Models model = null;
                        if (sku.regionMatches(true, 0, "M", 0, 1)) {
                            model = Models.BRAAVA_M_SERIES;
                        } else if (sku.regionMatches(true, 0, "E", 0, 1)) {
                            model = Models.ROOMBA_E_SERIES;
                        } else if (sku.regionMatches(true, 0, "I", 0, 1)) {
                            model = Models.ROOMBA_I_SERIES;
                        } else if (sku.regionMatches(true, 0, "R", 0, 1)) {
                            model = Models.ROOMBA_9_SERIES;
                        } else if (sku.regionMatches(true, 0, "S", 0, 1)) {
                            model = Models.ROOMBA_S_SERIES;
                        }
                        builder = builder.withProperty("family", model != null ? model.toString() : UNKNOWN);
                        builder = builder.withProperty("address", address);

                        String name = document.read("$.robotname", String.class);
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
