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
package org.openhab.binding.ventaair.internal.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ventaair.internal.VentaAirBindingConstants;
import org.openhab.binding.ventaair.internal.message.Header;
import org.openhab.binding.ventaair.internal.message.Message;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.util.HexUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.ventaair")
public class VentaDeviceDiscovery extends AbstractDiscoveryService {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(VentaAirBindingConstants.THING_TYPE_LW60T);
    // defined as int, because AbstractDiscoveryService wants and int and not long as provided by Duration.getSeconds()
    private static final int MANUAL_DISCOVERY_TIME = 30;

    private final Logger logger = LoggerFactory.getLogger(VentaDeviceDiscovery.class);

    private final Duration TIME_BETWEEN_SCANS = Duration.ofSeconds(30);

    private @Nullable ScheduledFuture<?> scanJob = null;

    public VentaDeviceDiscovery() {
        super(SUPPORTED_THING_TYPES_UIDS, MANUAL_DISCOVERY_TIME, true);
    }

    @Override
    protected void startScan() {
        logger.debug("startScan() pressed");
        findDevices();
    }

    @Override
    protected void startBackgroundDiscovery() {
        super.startBackgroundDiscovery();
        logger.debug("startBackgroundDiscovery()");

        ScheduledFuture<?> localScanJob = scanJob;
        if (localScanJob != null && !localScanJob.isCancelled()) {
            logger.debug("Canceling old scan job");
            localScanJob.cancel(true);
        }

        logger.debug("Scheduling new scan job");
        scanJob = scheduler.scheduleWithFixedDelay(this::findDevices, 5, TIME_BETWEEN_SCANS.getSeconds(),
                TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        super.stopBackgroundDiscovery();
        logger.debug("stopBackgroundDiscovery()");

        ScheduledFuture<?> localScanJob = scanJob;
        if (localScanJob != null && !localScanJob.isCancelled()) {
            logger.debug("Canceling scan job");
            localScanJob.cancel(true);
        }
        scanJob = null;
    }

    private void findDevices() {
        byte[] buf = new byte[512];
        try (DatagramSocket socket = new DatagramSocket(VentaAirBindingConstants.PORT)) {

            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            Message m = parseDiscoveryPaket(packet.getData());
            if (m == null) {
                logger.debug("Received broken discovery packet data={}", HexUtils.bytesToHex(packet.getData(), ", "));
                return;
            }

            logger.debug("Found device with: IP={} Mac={} and device type={}", m.getHeader().getIpAdress(),
                    m.getHeader().getMacAdress(), m.getHeader().getDeviceType());

            ThingTypeUID thingTypeUID;
            switch (m.getHeader().getDeviceType()) {
                case 4:
                    thingTypeUID = VentaAirBindingConstants.THING_TYPE_LW60T;
                    break;
                default:
                    thingTypeUID = VentaAirBindingConstants.THING_TYPE_GENERIC;
                    break;
            }
            createDiscoveryResult(thingTypeUID, m.getHeader());

        } catch (IOException e) {
            logger.debug("Exception while opening port or trying to parse discovery data from device.", e);
        }
    }

    private @Nullable Message parseDiscoveryPaket(byte[] packet) {
        Gson gson = new Gson();
        Message msg = null;

        String packetAsString = new String(packet);

        String[] lines = packetAsString.split("\n");
        if (lines.length >= 3) {
            String input = lines[2];
            int end = input.lastIndexOf("}"); // strip padding bytes added by the device
            if (end > 0) {
                String rawJSONstring = input.substring(0, end + 1);
                try {
                    msg = gson.fromJson(rawJSONstring, Message.class);
                } catch (JsonSyntaxException e) {
                    logger.debug("Received invalid JSON data={}", rawJSONstring, e);
                }
            }
        }
        return msg;
    }

    private void createDiscoveryResult(ThingTypeUID thingTypeUID, Header messageHeader) {
        String ipAddress = messageHeader.getIpAdress();
        String macAddress = messageHeader.getMacAdress();
        int deviceType = messageHeader.getDeviceType();

        ThingUID uid = new ThingUID(thingTypeUID, ipAddress.replace(".", "_"));
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("ipAddress", ipAddress);
        properties.put("macAddress", macAddress);
        properties.put("deviceType", deviceType);

        String typeLabel = thingTypeUID.getId().toUpperCase();

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withRepresentationProperty("macAddress")
                .withProperties(properties).withLabel(typeLabel + " (IP=" + ipAddress + " Mac=" + macAddress + ")")
                .build();

        this.thingDiscovered(result);
    }
}
