/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.ws980wifi.internal.discovery;

import static org.openhab.binding.ws980wifi.internal.ws980wifiBindingConstants.THING_TYPE_WS980WIFI;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ws980wifi.internal.ws980wifiBindingConstants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ws980wifiDiscoveryService} is responsible for discovering ws980wifi devices through
 * Broadcast.
 *
 * @author Joerg Dokupil - Initial contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.ws980wifi")
@NonNullByDefault

public class ws980wifiDiscoveryService extends AbstractDiscoveryService {

    private final Logger log = LoggerFactory.getLogger(ws980wifiDiscoveryService.class);
    private final NetworkAddressService networkAddressService;
    private static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Set.of(THING_TYPE_WS980WIFI);
    private static final int DISCOVERY_TIMEOUT_SECONDS = 30;
    private @Nullable ScheduledFuture<?> backgroundDiscoveryFuture;

    @Activate
    public ws980wifiDiscoveryService(@Reference NetworkAddressService networkAddressService) {
        super(DISCOVERABLE_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SECONDS);
        this.networkAddressService = networkAddressService;
    }

    private void createScanner() {
        long timestampOfLastScan = getTimestampOfLastScan();
        ws980wifi[] ws980wifiDevices = new ws980wifi[0]; // Array of devices with length zero, easy to rotate through
        try {
            @Nullable
            InetAddress sourceAddress = getIpAddress(); // gets the ip address of the local host
            if (sourceAddress != null) {
                log.debug("Scanner: Using source address {} for sending out broadcast request.", sourceAddress);
                ws980wifiDevices = ws980wifi.discoverDevices(10000, sourceAddress);
            } else {
                log.debug("Error: no address of the local host! Scan aborted...");
            }
        } catch (Exception e) {
            log.debug("Error while trying to discover ws980wifi devices: {}", e.getMessage());
        }
        for (ws980wifi dev : ws980wifiDevices) {
            log.debug("Scanner: identified ws980wifi device {}", dev.getHost());
            ThingUID thingUID;
            String id = dev.getHost().replaceAll("\\.", "-");
            log.debug("Device ID with IP address replacement: {}", id);
            try {
                id = getHostnameWithoutDomain(InetAddress.getByName(dev.getHost()).getHostName());
                log.debug("Device ID with DNS name: {}", id);
            } catch (UnknownHostException e) {
                log.debug("Discovered device with IP {} does not have a DNS name, using IP as thing UID.",
                        dev.getHost());
            }
            log.debug("Scanner: Will create device ID with Name: {}", id);
            thingUID = new ThingUID(THING_TYPE_WS980WIFI, id);
            log.debug("Scanner: New ThingUID {} created", thingUID);
            Map<String, Object> properties = new HashMap<>();
            properties.put(ws980wifiBindingConstants.HOST, dev.getHost());
            properties.put(Thing.PROPERTY_MAC_ADDRESS, dev.getMac());
            properties.put(ws980wifiBindingConstants.DESCRIPTION, dev.getDescription());
            properties.put(ws980wifiBindingConstants.IP, dev.getIP());
            properties.put(ws980wifiBindingConstants.PORT, dev.getPort().toString());
            log.debug("Scanner: New Properties Map created: {}", properties);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withLabel(dev.getDescription() + " (" + id + ")")
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
            thingDiscovered(discoveryResult);
            log.debug("---> discovered device {} in Thing Discovery Result Liste inserted", thingUID);
        }
        removeOlderResults(timestampOfLastScan);
    }

    @Override
    protected void startScan() {
        scheduler.execute(this::createScanner);
    }

    @Override
    protected void startBackgroundDiscovery() {
        log.debug("*** Starting background scan for WS980WiFi devices");
        ScheduledFuture<?> currentBackgroundDiscoveryFuture = backgroundDiscoveryFuture;
        if (currentBackgroundDiscoveryFuture != null) {
            currentBackgroundDiscoveryFuture.cancel(true);
        }
        backgroundDiscoveryFuture = scheduler.scheduleWithFixedDelay(this::createScanner, 0, 60, TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        log.debug("*** Stopping background scan for WS980WiFi devices");
        @Nullable
        ScheduledFuture<?> backgroundDiscoveryFuture = this.backgroundDiscoveryFuture;
        if (backgroundDiscoveryFuture != null && !backgroundDiscoveryFuture.isCancelled()) {
            if (backgroundDiscoveryFuture.cancel(true)) {
                this.backgroundDiscoveryFuture = null;
            }
        }
        stopScan();
    }

    private @Nullable InetAddress getIpAddress() {
        return getIpFromNetworkAddressService().orElse(null);
    }

    /**
     * Uses openHAB's NetworkAddressService to determine the local primary network interface.
     *
     * @return local ip or <code>empty</code> if configured primary IP is not set or could not be parsed.
     */
    private Optional<InetAddress> getIpFromNetworkAddressService() {
        String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
        if (ipAddress == null) {
            log.warn("No network interface could be found.");
            return Optional.empty();
        }
        try {
            return Optional.of(InetAddress.getByName(ipAddress));
        } catch (UnknownHostException e) {
            log.warn("Configured primary IP cannot be parsed: {} Details: {}", ipAddress, e.getMessage());
            return Optional.empty();
        }
    }

    private String getHostnameWithoutDomain(String hostname) {
        String[] dotSeparatedString = hostname.split("\\.");
        return dotSeparatedString[0].replaceAll("\\.", "-");
    }
}
