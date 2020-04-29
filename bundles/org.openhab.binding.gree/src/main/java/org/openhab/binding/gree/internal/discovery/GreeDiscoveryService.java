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

import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Device discovery creates a thing in the inbox for each vehicle
 * found in the data received from {@link CarNetAccountHandler}.
 *
 * @author John Cunha - Initial contribution
 * @author Markus Michels - Refactoring, adapted to OH 2.5x
 *
 */
@NonNullByDefault
public class GreeDiscoveryService extends AbstractDiscoveryService {
    private static final int TIMEOUT = 10;
    private final Logger logger = LoggerFactory.getLogger(GreeDiscoveryService.class);
    private final @Nullable TranslationProvider i18nProvider;
    private final @Nullable LocaleProvider localeProvider;
    private final String broadcastAddress;

    private GreeDeviceFinder deviceFinder = new GreeDeviceFinder();

    public GreeDiscoveryService(Bundle bundle, @Nullable TranslationProvider i18nProvider,
            @Nullable LocaleProvider localeProvider, @Nullable NetworkAddressService networkAddressService) {
        super(SUPPORTED_THING_TYPES_UIDS, TIMEOUT);
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
        String broadcastAddress = networkAddressService.getConfiguredBroadcastAddress();
        this.broadcastAddress = broadcastAddress != null ? broadcastAddress : "192.168.255.255";
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
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            deviceFinder = new GreeDeviceFinder(broadcastAddress);
            deviceFinder.Scan(clientSocket);
            clientSocket.close();

            logger.debug("GREE {} units discovered", deviceFinder.getScannedDeviceCount());
            createResult(deviceFinder.getDevices());
        } catch (Exception e) {
            logger.debug("Discovery failed", e);
        }
    }

    public void createResult(HashMap<String, GreeAirDevice> deviceList) {
        for (Map.Entry<String, GreeAirDevice> d : deviceList.entrySet()) {
            GreeAirDevice device = d.getValue();
            logger.debug("Discovery for [{}]", device.getName());
            Map<String, Object> properties = new TreeMap<String, Object>();
            properties.put(Thing.PROPERTY_VENDOR, device.getVendor());
            properties.put(Thing.PROPERTY_MODEL_ID, device.getModel());
            properties.put(Thing.PROPERTY_MAC_ADDRESS, device.getId());
            properties.put(PROPERTY_IP, device.getAddress().toString());
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
