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
package org.openhab.binding.broadlinkthermostat.internal.discovery;

import static org.openhab.binding.broadlinkthermostat.internal.BroadlinkThermostatBindingConstants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlinkthermostat.internal.BroadlinkThermostatBindingConstants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mob41.blapi.BLDevice;

/**
 * The {@link BroadlinkThermostatDiscoveryService} is responsible for discovering Broadlinkthermostat devices through
 * Broadcast.
 *
 * @author Florian Mueller - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.broadlinkthermostat")
@NonNullByDefault
public class BroadlinkThermostatDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(BroadlinkThermostatDiscoveryService.class);

    private @Nullable NetworkAddressService networkAddressService;

    private static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(FLOUREON_THERMOSTAT_THING_TYPE, UNKNOWN_BROADLINKTHERMOSTAT_THING_TYPE).collect(Collectors.toSet()));
    private static final int DISCOVERY_TIMEOUT = 30;

    public BroadlinkThermostatDiscoveryService(@Nullable Set<ThingTypeUID> supportedThingTypes, int timeout)
            throws IllegalArgumentException {
        super(supportedThingTypes, timeout);
    }

    public BroadlinkThermostatDiscoveryService() {
        super(DISCOVERABLE_THING_TYPES_UIDS, DISCOVERY_TIMEOUT);
    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    @Override
    protected void startScan() {

        BLDevice[] blDevices = new BLDevice[0];
        try {
            InetAddress sourceAddress = getIpAddress();
            if (sourceAddress != null) {
                logger.debug("Using source address {} for sending out broadcast request.", sourceAddress);
                blDevices = BLDevice.discoverDevices(sourceAddress, 0, DISCOVERY_TIMEOUT * 1000);
            } else {
                blDevices = BLDevice.discoverDevices(DISCOVERY_TIMEOUT * 1000);
            }

        } catch (IOException e) {
            logger.error("Error while trying to discover broadlinkthermostat devices: ", e);
        }
        logger.info("Discovery service found {} broadlinkthermostat devices.", blDevices.length);

        for (BLDevice dev : blDevices) {
            logger.debug("Broadlinkthermostat device {} of type {} with Host {} and MAC {}", dev.getDeviceDescription(),
                    Integer.toHexString(dev.getDeviceType()), dev.getHost(), dev.getMac());

            ThingUID thingUID;
            String id = dev.getHost().replaceAll("\\.", "-");
            logger.debug("Device ID with IP address replacement: {}", id);
            try {
                id = getHostnameWithoutDomain(InetAddress.getByName(dev.getHost()).getHostName());
                logger.debug("Device ID with DNS name: {}", id);
            } catch (UnknownHostException e) {
                logger.warn("Discovered device with IP {} does not have a DNS name, using IP as thing UID.",
                        dev.getHost());
            }

            switch (dev.getDeviceDescription()) {
                case "Floureon Thermostat":
                    thingUID = new ThingUID(FLOUREON_THERMOSTAT_THING_TYPE, id);
                    break;
                case "Hysen Thermostat":
                    thingUID = new ThingUID(HYSEN_THERMOSTAT_THING_TYPE, id);
                    break;
                default:
                    thingUID = new ThingUID(UNKNOWN_BROADLINKTHERMOSTAT_THING_TYPE, id);
            }

            Map<String, Object> properties = new HashMap<>();
            properties.put(BroadlinkThermostatBindingConstants.HOST, dev.getHost());
            properties.put(BroadlinkThermostatBindingConstants.MAC, dev.getMac().getMacString());
            properties.put(BroadlinkThermostatBindingConstants.DESCRIPTION, dev.getDeviceDescription());

            logger.debug("Property map: {}", properties);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withLabel(dev.getDeviceDescription() + " (" + id + ")")
                    .withRepresentationProperty(BroadlinkThermostatBindingConstants.HOST).build();

            thingDiscovered(discoveryResult);

        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.trace("Starting background scan for Broadlinkthermostat devices");
        startScan();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.trace("Stopping background scan for Broadlinkthermostat devices");
        stopScan();
    }

    private @Nullable InetAddress getIpAddress() {
        return getIpFromNetworkAddressService().orElse(null);
    }

    /**
     * Uses OpenHAB's NetworkAddressService to determine the local primary network interface.
     *
     * @return local ip or <code>empty</code> if configured primary IP is not set or could not be parsed.
     */
    private Optional<InetAddress> getIpFromNetworkAddressService() {
        NetworkAddressService service = networkAddressService;
        if (service == null) {
            throw new IllegalStateException(
                    "NetworkAddressService must be bound before getIpFromNetworkAddressService can be called.");
        }

        String ipAddress = service.getPrimaryIpv4HostAddress();
        if (ipAddress == null) {
            logger.warn("No network interface could be found.");
            return Optional.empty();
        }
        try {
            return Optional.of(InetAddress.getByName(ipAddress));
        } catch (UnknownHostException e) {
            logger.warn("Configured primary IP cannot be parsed: {} Details: {}", ipAddress, e.getMessage());
            return Optional.empty();
        }
    }

    private String getHostnameWithoutDomain(String hostname) {
        String broadlinkthermostatRegex = "Broadlink-OEM(-[A-Z,a-z,0-9]{2}){4}.*";
        if (hostname.matches(broadlinkthermostatRegex)) {
            String[] dotSeparatedString = hostname.split("\\.");
            logger.debug("Found original broadlink DNS name {}, removing domain", hostname);
            return dotSeparatedString[0].replaceAll("\\.", "-");
        } else {
            logger.debug("DNS name does not match original broadlink name: {}, using it without modification. ",
                    hostname);
            return hostname.replaceAll("\\.", "-");
        }
    }
}
