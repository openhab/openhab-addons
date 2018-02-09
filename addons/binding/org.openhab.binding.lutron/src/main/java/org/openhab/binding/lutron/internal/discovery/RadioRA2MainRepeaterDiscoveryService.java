/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.discovery;

import static org.openhab.binding.lutron.LutronBindingConstants.*;

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

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RadioRA2MainRepeaterDiscoveryService} finds RadioRA2 Main Repeaters on the network.
 *
 * @author Allan Tong - Initial contribution
 */
public class RadioRA2MainRepeaterDiscoveryService extends AbstractDiscoveryService {

    private static final Set<ThingTypeUID> BRIDGE_TYPE_UID = Collections.singleton(THING_TYPE_IPBRIDGE);

    private static final String GROUP_ADDRESS = "224.0.37.42";
    private static final byte[] QUERY_DATA = "<LUTRON=1>".getBytes(StandardCharsets.US_ASCII);
    private static final int QUERY_DEST_PORT = 2647;
    private static final Pattern BRIDGE_PROP_PATTERN = Pattern.compile("<([^=>]+)=([^>]*)>");

    private static final String DEFAULT_LABEL = "RadioRA2 MainRepeater";

    private final Logger logger = LoggerFactory.getLogger(RadioRA2MainRepeaterDiscoveryService.class);

    private ScheduledFuture<?> scanTask;
    private ScheduledFuture<?> backgroundScan;

    public RadioRA2MainRepeaterDiscoveryService() {
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
            this.backgroundScan = scheduler.scheduleWithFixedDelay(new RepeaterScanner(), 1, 30 * 60, TimeUnit.SECONDS);
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
                logger.info("Main repeater scan interrupted");
            } catch (IOException e) {
                logger.error("Communication error during bridge scan", e);
            }
        }

        private void queryForRepeaters() throws IOException, InterruptedException {
            logger.debug("Scanning for RadioRA2 main repeaters");

            InetAddress group = InetAddress.getByName(GROUP_ADDRESS);

            try (MulticastSocket socket = new MulticastSocket()) {
                socket.setSoTimeout(2000);
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

                        logger.info("Main repeater scan interrupted");
                    } catch (SocketTimeoutException e) {
                        logger.debug("Timed out waiting for response; presumably all repeaters have already responded");
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
            }

            String ipAddress = bridgeProperties.get("IPADDR");
            String serialNumber = bridgeProperties.get("SERNUM");
            String productFamily = bridgeProperties.get("PRODFAM");
            String productType = bridgeProperties.get("PRODTYPE");

            if (StringUtils.isNotBlank(ipAddress) && StringUtils.isNotBlank(serialNumber)) {
                Map<String, Object> properties = new HashMap<>();

                properties.put(HOST, ipAddress);
                properties.put(SERIAL_NUMBER, serialNumber);

                ThingUID uid = new ThingUID(THING_TYPE_IPBRIDGE, serialNumber);
                String label = generateLabel(productFamily, productType);
                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(label).withProperties(properties)
                        .withRepresentationProperty(SERIAL_NUMBER).build();

                thingDiscovered(result);

                logger.debug("Discovered main repeater {}", uid);
            }
        }

        private String generateLabel(String productFamily, String productType) {
            if (StringUtils.isNotBlank(productFamily) && StringUtils.isNotBlank(productType)) {
                return productFamily + " " + productType;
            }

            return DEFAULT_LABEL;
        }
    }
}
