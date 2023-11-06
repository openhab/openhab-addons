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
package org.openhab.binding.gree.internal.discovery;

import static org.openhab.binding.gree.internal.GreeBindingConstants.*;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gree.internal.GreeException;
import org.openhab.binding.gree.internal.GreeTranslationProvider;
import org.openhab.binding.gree.internal.handler.GreeAirDevice;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
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
@Component(service = DiscoveryService.class, configurationPid = "discovery.gree")
public class GreeDiscoveryService extends AbstractDiscoveryService {
    private static final int TIMEOUT_SEC = 10;
    private final Logger logger = LoggerFactory.getLogger(GreeDiscoveryService.class);
    private final GreeDeviceFinder deviceFinder;
    private final GreeTranslationProvider messages;
    private final String broadcastAddress;

    @Activate
    public GreeDiscoveryService(@Reference GreeDeviceFinder deviceFinder,
            @Reference NetworkAddressService networkAddressService,
            @Reference GreeTranslationProvider translationProvider, @Nullable Map<String, Object> configProperties) {
        super(SUPPORTED_THING_TYPES_UIDS, TIMEOUT_SEC);
        this.messages = translationProvider;
        this.deviceFinder = deviceFinder;
        String ip = networkAddressService.getConfiguredBroadcastAddress();
        broadcastAddress = ip != null ? ip : "";
        activate(configProperties);
    }

    @Override
    @Modified
    protected void modified(@Nullable Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    protected void startBackgroundDiscovery() {
        // It's very unusual that a new unit gets installed frequently so we run the discovery once when the binding is
        // started, but not frequently
        scheduler.execute(this::startScan);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
    }

    @Override
    protected void startScan() {
        try (DatagramSocket clientSocket = new DatagramSocket()) {
            deviceFinder.scan(clientSocket, broadcastAddress, true);

            int count = deviceFinder.getScannedDeviceCount();
            logger.debug("{}", messages.get("discovery.result", count));
            if (count > 0) {
                logger.debug("Adding uinits to Inbox");
                createResult(deviceFinder.getDevices());
            }
        } catch (GreeException e) {
            logger.info("Discovery: {}", messages.get("discovery.exception", e.getMessageString()));
        } catch (SocketException | RuntimeException e) {
            logger.warn("Discovery: {}", messages.get("discovery.exception", "RuntimeException"), e);
        }
    }

    public void createResult(Map<String, GreeAirDevice> deviceList) {
        for (GreeAirDevice device : deviceList.values()) {
            String ipAddress = device.getAddress().getHostAddress();
            logger.debug("{}", messages.get("discovery.newunit", device.getName(), ipAddress, device.getId()));
            Map<String, Object> properties = new HashMap<>();
            properties.put(Thing.PROPERTY_VENDOR, device.getVendor());
            properties.put(Thing.PROPERTY_MODEL_ID, device.getModel());
            properties.put(Thing.PROPERTY_MAC_ADDRESS, device.getId());
            properties.put(PROPERTY_IP, ipAddress);
            properties.put(PROPERTY_BROADCAST, broadcastAddress);
            ThingUID thingUID = new ThingUID(THING_TYPE_GREEAIRCON, device.getId());
            DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).withLabel(device.getName()).build();
            thingDiscovered(result);
        }
    }

    @Override
    public void deactivate() {
        removeOlderResults(getTimestampOfLastScan());
        super.deactivate();
    }
}
