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
package org.openhab.binding.wifiled.internal.discovery;

import static org.openhab.binding.wifiled.internal.WiFiLEDBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.wifiled.internal.handler.AbstractWiFiLEDDriver;
import org.openhab.binding.wifiled.internal.handler.ClassicWiFiLEDDriver;
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
 * The {@link WiFiLEDDiscoveryService} class implements a service
 * for discovering supported WiFi LED Devices.
 *
 * @author Osman Basha - Initial contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.wifiled")
public class WiFiLEDDiscoveryService extends AbstractDiscoveryService {

    private static final int DEFAULT_BROADCAST_PORT = 48899;
    private static final String DISCOVER_MESSAGE = "HF-A11ASSISTHREAD";
    private Logger logger = LoggerFactory.getLogger(WiFiLEDDiscoveryService.class);

    public WiFiLEDDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 15, true);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start WiFi LED background discovery");
        scheduler.schedule(() -> discover(), 0, TimeUnit.SECONDS);
    }

    @Override
    public void startScan() {
        logger.debug("Start WiFi LED scan");
        discover();
    }

    private synchronized void discover() {
        logger.debug("Try to discover all WiFi LED devices");

        try (DatagramSocket socket = new DatagramSocket(DEFAULT_BROADCAST_PORT)) {
            socket.setBroadcast(true);
            socket.setSoTimeout(5000);

            InetAddress inetAddress = InetAddress.getByName("255.255.255.255");

            // send discover
            byte[] discover = DISCOVER_MESSAGE.getBytes();
            DatagramPacket packet = new DatagramPacket(discover, discover.length, inetAddress, DEFAULT_BROADCAST_PORT);
            socket.send(packet);
            logger.debug("Discover message sent: '{}'", DISCOVER_MESSAGE);

            // wait for responses
            while (true) {
                byte[] rxbuf = new byte[256];
                packet = new DatagramPacket(rxbuf, rxbuf.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    logger.trace("Timeout exceeded. Discovery process ended.");
                    break;
                }

                byte[] data = packet.getData();
                String s = bytesToString(data);
                logger.debug("Discovery response received: '{}' [{}] ", s, ClassicWiFiLEDDriver.bytesToHex(data));

                // 192.168.178.25,ACCF23489C9A,HF-LPB100-ZJ200
                // ^-IP..........,^-MAC.......,^-HOSTNAME.....

                String[] ss = s.split(",");
                if (ss.length < 3) {
                    logger.debug("Ignoring unparseable discovery response: '{}'", s);
                    continue;
                }

                String ip = ss[0];
                String mac = ss[1];
                String name = ss[2];
                logger.debug("Adding a new WiFi LED with IP '{}' and MAC '{}' to inbox", ip, mac);
                Map<String, Object> properties = new HashMap<>();
                properties.put("ip", ip);
                properties.put("protocol", AbstractWiFiLEDDriver.Protocol.LD382A);
                ThingUID uid = new ThingUID(THING_TYPE_WIFILED, mac);

                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(name)
                        .build();
                thingDiscovered(result);
                logger.debug("Thing discovered '{}'", result);
            }
        } catch (IOException e) {
            logger.debug("Device discovery encountered an I/O Exception: {}", e.getMessage(), e);
        }
    }

    private static String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            if (aByte == 0) {
                break;
            }
            sb.append((char) (aByte & 0xFF));
        }

        return sb.toString();
    }
}
