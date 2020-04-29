/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.gree.internal.discovery;

import static org.openhab.binding.gree.internal.GreeBindingConstants.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.gree.internal.GreeException;
import org.openhab.binding.gree.internal.GreeTranslationProvider;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link GreeDiscoveryService} implements the device discovery service. UDP broadtcast ius used to find the devices on
 * the local subnet.
 *
 * @author Markus Michels - Initial contribution
 *
 */
@NonNullByDefault
public class GreeDiscoveryService extends AbstractDiscoveryService {
    private static final int TIMEOUT = 10;
    private final Logger logger = LoggerFactory.getLogger(GreeDiscoveryService.class);
    private final GreeTranslationProvider messages;
    private final String broadcastAddress;

    private GreeDeviceFinder deviceFinder = new GreeDeviceFinder();

    public GreeDiscoveryService(Bundle bundle, GreeTranslationProvider messages, String broadcastAddress) {
        super(SUPPORTED_THING_TYPES_UIDS, TIMEOUT);
        this.messages = messages;
        this.broadcastAddress = !broadcastAddress.isEmpty() ? broadcastAddress : "192.168.255.255";
        logger.debug("Auto-detected broadcast IP = {}", this.broadcastAddress);
    }

    public void activate() {
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.trace("Starting background scan");
        startScan();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.trace("Stopping background scan");
        stopScan();
    }

    @Override
    protected void startScan() {
        Optional<DatagramSocket> clientSocket = Optional.empty();
        try {
            clientSocket = Optional.of(new DatagramSocket());
            deviceFinder = new GreeDeviceFinder(broadcastAddress);
            deviceFinder.scan(clientSocket, true);

            int count = deviceFinder.getScannedDeviceCount();
            logger.info("{}", messages.get("discovery.result", count));
            if (count > 0) {
                logger.debug("Adding uinits to Inbox");
                createResult(deviceFinder.getDevices());
            }
        } catch (IOException e) {
            logger.debug("{}",
                    new GreeException(e, "I/O exception while scanning the network for GREE devices").toString());
        } catch (GreeException e) {
            logger.debug("Discovery failed: {}", e.toString());
        } finally {
            if (clientSocket.isPresent()) {
                clientSocket.get().close();
            }
        }
    }

    public void createResult(HashMap<String, GreeAirDevice> deviceList) {
        for (Map.Entry<String, GreeAirDevice> d : deviceList.entrySet()) {
            GreeAirDevice device = d.getValue();
            String ipAddress = device.getAddress().getHostAddress();
            logger.debug("{}", messages.get("discovery.newunit", device.getName(), ipAddress, device.getId()));
            Map<String, Object> properties = new TreeMap<String, Object>();
            properties.put(Thing.PROPERTY_VENDOR, device.getVendor());
            properties.put(Thing.PROPERTY_MODEL_ID, device.getModel());
            properties.put(Thing.PROPERTY_MAC_ADDRESS, device.getId());
            properties.put(PROPERTY_IP, ipAddress);
            properties.put(PROPERTY_BROADCAST, broadcastAddress);
            ThingUID thingUID = new ThingUID(THING_TYPE_GREEAIRCON, device.getId());
            DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(device.getId()).withLabel(device.getName()).build();
            thingDiscovered(result);
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        removeOlderResults(getTimestampOfLastScan());
    }
}
