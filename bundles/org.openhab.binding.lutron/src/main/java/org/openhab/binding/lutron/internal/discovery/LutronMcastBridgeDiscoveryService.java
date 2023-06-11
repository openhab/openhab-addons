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
package org.openhab.binding.lutron.internal.discovery;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LutronMcastBridgeDiscoveryService} finds RadioRA 2 Main Repeaters and HomeWorks QS
 * Processors on the network using multicast.
 *
 * @author Allan Tong - Initial contribution
 * @author Bob Adair - Renamed and added bridge properties
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.lutron")
public class LutronMcastBridgeDiscoveryService extends AbstractDiscoveryService {

    private static final int SCAN_INTERVAL_MINUTES = 30;
    private static final int SCAN_TIMEOUT_MS = 2000;

    private static final Set<ThingTypeUID> BRIDGE_TYPE_UID = Collections.singleton(THING_TYPE_IPBRIDGE);

    private static final String GROUP_ADDRESS = "224.0.37.42";
    private static final byte[] QUERY_DATA = "<LUTRON=1>".getBytes(StandardCharsets.US_ASCII);
    private static final int QUERY_DEST_PORT = 2647;
    private static final Pattern BRIDGE_PROP_PATTERN = Pattern.compile("<([^=>]+)=([^>]*)>");
    private static final String PRODFAM_RA2 = "RadioRA2";
    private static final String PRODFAM_HWQS = "Gulliver";

    private static final String DEFAULT_LABEL = "RadioRA2 MainRepeater";

    private final Logger logger = LoggerFactory.getLogger(LutronMcastBridgeDiscoveryService.class);

    private @Nullable ScheduledFuture<?> scanTask;
    private @Nullable ScheduledFuture<?> backgroundScan;

    public LutronMcastBridgeDiscoveryService() {
        super(BRIDGE_TYPE_UID, 5);
    }

    @Override
    protected void startScan() {
        this.scanTask = scheduler.schedule(new RepeaterScanner(), 0, TimeUnit.SECONDS);
    }

    @Override
    protected void stopScan() {
        super.stopScan();

        if (this.scanTask != null) {
            this.scanTask.cancel(true);
        }
    }

    @Override
    public void abortScan() {
        super.abortScan();

        if (this.scanTask != null) {
            this.scanTask.cancel(true);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (this.backgroundScan == null) {
            this.backgroundScan = scheduler.scheduleWithFixedDelay(new RepeaterScanner(), 1, SCAN_INTERVAL_MINUTES,
                    TimeUnit.MINUTES);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (this.backgroundScan != null) {
            this.backgroundScan.cancel(true);
            this.backgroundScan = null;
        }
    }

    private class RepeaterScanner implements Runnable {
        @Override
        public void run() {
            try {
                queryForRepeaters();
            } catch (InterruptedException e) {
                logger.info("Bridge device scan interrupted");
            } catch (IOException e) {
                logger.warn("Communication error during bridge scan: {}", e.getMessage());
            }
        }

        private void queryForRepeaters() throws IOException, InterruptedException {
            logger.debug("Scanning for Lutron bridge devices using multicast");

            InetAddress group = InetAddress.getByName(GROUP_ADDRESS);

            try (MulticastSocket socket = new MulticastSocket()) {
                socket.setSoTimeout(SCAN_TIMEOUT_MS);
                socket.joinGroup(group);

                try {
                    // Try to ensure that joinGroup has taken effect. Without this delay, the query
                    // packet ends up going out before the group join.
                    Thread.sleep(1000);

                    socket.send(new DatagramPacket(QUERY_DATA, QUERY_DATA.length, group, QUERY_DEST_PORT));

                    byte[] buf = new byte[4096];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);

                    try {
                        while (!Thread.interrupted()) {
                            socket.receive(packet);
                            createBridge(packet);
                        }

                        logger.info("Bridge device scan interrupted");
                    } catch (SocketTimeoutException e) {
                        logger.trace(
                                "Timed out waiting for multicast response. Presumably all bridge devices have already responded.");
                    }
                } finally {
                    socket.leaveGroup(group);
                }
            }
        }

        private void createBridge(DatagramPacket packet) {
            // Check response for the list of properties reported by the device. At a
            // minimum the IP address and serial number are needed in order to create
            // the bridge.
            String data = new String(packet.getData(), packet.getOffset(), packet.getLength(),
                    StandardCharsets.US_ASCII);

            Matcher matcher = BRIDGE_PROP_PATTERN.matcher(data);
            Map<String, String> bridgeProperties = new HashMap<>();

            while (matcher.find()) {
                bridgeProperties.put(matcher.group(1), matcher.group(2));
                logger.trace("Bridge property: {} : {}", matcher.group(1), matcher.group(2));
            }

            String ipAddress = bridgeProperties.get("IPADDR");
            String serialNumber = bridgeProperties.get("SERNUM");
            String productFamily = bridgeProperties.get("PRODFAM");
            String productType = bridgeProperties.get("PRODTYPE");
            String codeVersion = bridgeProperties.get("CODEVER");
            String macAddress = bridgeProperties.get("MACADDR");

            if (ipAddress != null && !ipAddress.trim().isEmpty() && serialNumber != null
                    && !serialNumber.trim().isEmpty()) {
                Map<String, Object> properties = new HashMap<>();

                properties.put(HOST, ipAddress);
                properties.put(SERIAL_NUMBER, serialNumber);

                if (PRODFAM_RA2.equals(productFamily)) {
                    properties.put(PROPERTY_PRODFAM, "RadioRA 2");
                } else if (PRODFAM_HWQS.equals(productFamily)) {
                    properties.put(PROPERTY_PRODFAM, "HomeWorks QS");
                } else {
                    if (productFamily != null) {
                        properties.put(PROPERTY_PRODFAM, productFamily);
                    }
                }

                if (productType != null && !productType.trim().isEmpty()) {
                    properties.put(PROPERTY_PRODTYP, productType);
                }
                if (codeVersion != null && !codeVersion.trim().isEmpty()) {
                    properties.put(Thing.PROPERTY_FIRMWARE_VERSION, codeVersion);
                }
                if (macAddress != null && !macAddress.trim().isEmpty()) {
                    properties.put(Thing.PROPERTY_MAC_ADDRESS, macAddress);
                }

                ThingUID uid = new ThingUID(THING_TYPE_IPBRIDGE, serialNumber);
                String label = generateLabel(productFamily, productType);
                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(label).withProperties(properties)
                        .withRepresentationProperty(SERIAL_NUMBER).build();

                thingDiscovered(result);

                logger.debug("Discovered Lutron bridge device {}", uid);
            }
        }

        private String generateLabel(@Nullable String productFamily, @Nullable String productType) {
            if (productFamily != null && !productFamily.trim().isEmpty() && productType != null
                    && !productType.trim().isEmpty()) {
                return productFamily + " " + productType;
            }

            return DEFAULT_LABEL;
        }
    }
}
