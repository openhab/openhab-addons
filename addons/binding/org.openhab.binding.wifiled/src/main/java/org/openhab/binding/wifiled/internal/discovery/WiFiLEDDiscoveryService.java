/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wifiled.internal.discovery;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.wifiled.handler.AbstractWiFiLEDDriver;
import org.openhab.binding.wifiled.handler.ClassicWiFiLEDDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.wifiled.WiFiLEDBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.wifiled.WiFiLEDBindingConstants.THING_TYPE_WIFILED;

/**
 * The {@link WiFiLEDDiscoveryService} class implements a service
 * for discovering supported WiFi LED Devices.
 *
 * @author Osman Basha - Initial contribution
 */
public class WiFiLEDDiscoveryService extends AbstractDiscoveryService {

    private static final int DEFAULT_BROADCAST_PORT = 48899;

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

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(5000);

            InetAddress inetAddress = InetAddress.getByName("255.255.255.255");

            // send discover
            byte[] discover = "HF-A11ASSISTHREAD".getBytes();
            DatagramPacket packet = new DatagramPacket(discover, discover.length, inetAddress, DEFAULT_BROADCAST_PORT);
            socket.send(packet);
            logger.debug("Disover message sent: '{}'", ClassicWiFiLEDDriver.bytesToHex(discover));

            // wait for responses
            while (true) {
                byte[] rxbuf = new byte[256];
                packet = new DatagramPacket(rxbuf, rxbuf.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    break; // leave the endless loop
                }

                byte[] data = packet.getData();
                String s = bytesToString(data);
                logger.debug("Disover response received: '{}' [{}] ", s, ClassicWiFiLEDDriver.bytesToHex(data));

                // 192.168.178.25,ACCF23489C9A,HF-LPB100-ZJ200
                // ^-IP..........,^-MAC.......,^-HOSTNAME.....

                String[] ss = s.split(",");
                String ip = ss[0];
                String mac = ss[1];
                String name = ss[2];
                logger.debug("Adding a new WiFi LED with IP '{}' and MAC '{}' to inbox", ip, mac);
                Map<String, Object> properties = new HashMap<>();
                properties.put("ip", ip);
                properties.put("protocol", AbstractWiFiLEDDriver.Protocol.LD382A);
                ThingUID uid = new ThingUID(THING_TYPE_WIFILED, mac);

                DiscoveryResult result = DiscoveryResultBuilder.create(uid)
                        .withProperties(properties)
                        .withLabel(name)
                        .build();
                thingDiscovered(result);
                logger.debug("Thing discovered '{}'", result);
            }
        } catch (IOException e) {
            logger.debug("No WiFi LED device found. Diagnostic: {}", e.getMessage());
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
