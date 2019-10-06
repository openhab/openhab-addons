/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.internal.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mob41.blapi.BLDevice;

import static org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.*;

/**
 * The {@link BroadlinkDiscoveryService} is responsible for discovering Broadlink devices through Broadcast.
 *
 * @author Florian Mueller - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.broadlink")
public class BroadlinkDiscoveryService extends AbstractDiscoveryService {


    private final Logger logger = LoggerFactory.getLogger(BroadlinkDiscoveryService.class);

    private @Nullable NetworkAddressService networkAddressService;

    private static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(FLOUREON_THERMOSTAT_THING_TYPE, UNKNOWN_BROADLINK_THING_TYPE).collect(Collectors.toSet()));
    private static final int DISCOVERY_TIMEOUT = 30;

    public BroadlinkDiscoveryService(@Nullable Set<ThingTypeUID> supportedThingTypes, int timeout) throws IllegalArgumentException {
        super(supportedThingTypes, timeout);
    }

    public BroadlinkDiscoveryService() {
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
            if(sourceAddress!= null){
                logger.debug("Using source address {} for sending out broadcast request.", sourceAddress);
                blDevices = BLDevice.discoverDevices(sourceAddress,0,DISCOVERY_TIMEOUT*1000);
            }else{
                blDevices = BLDevice.discoverDevices(DISCOVERY_TIMEOUT*1000);
            }

        } catch (IOException e) {
            logger.error("Error while trying to discover broadlink devices: ",e);
        }
        logger.info("Discovery service found {} broadlink devices." , blDevices.length);

        for (BLDevice dev : blDevices){
            logger.debug("Broadlink device {} of type {} with Host {} and MAC {}",
                    dev.getDeviceDescription(),
                    Integer.toHexString(dev.getDeviceType()),
                    dev.getHost(),
                    dev.getMac());

            ThingUID thingUID;
            String id = dev.getHost().replaceAll("\\.","-");
            logger.debug("Device ID with IP address replacement: {}",id);
            try {
                id = getHostnameWithoutDomain(InetAddress.getByName(dev.getHost()).getHostName());
                logger.debug("Device ID with DNS name: {}",id);
            } catch (UnknownHostException e) {
                logger.warn("Discovered device with IP {} does not have a DNS name, using IP as thing UID.",dev.getHost());
            }

            switch (dev.getDeviceDescription()){
                case "Floureon Thermostat":
                    thingUID = new ThingUID(FLOUREON_THERMOSTAT_THING_TYPE, id );
                    break;
                case "Hysen Thermostat":
                    thingUID = new ThingUID(HYSEN_THERMOSTAT_THING_TYPE, id );
                    break;
                case "Environmental Sensor":
                    thingUID = new ThingUID(A1_ENVIRONMENTAL_SENSOR_THING_TYPE, id );
                    break;
                default:
                    thingUID = new ThingUID(UNKNOWN_BROADLINK_THING_TYPE, id  );
            }


            Map<String, Object> properties = new HashMap<>();
            properties.put(BroadlinkBindingConstants.HOST, dev.getHost());
            properties.put(BroadlinkBindingConstants.MAC, dev.getMac().getMacString());
            properties.put(BroadlinkBindingConstants.DESCRIPTION, dev.getDeviceDescription());

            logger.debug("Property map: {}",properties);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder
                    .create(thingUID)
                    .withProperties(properties)
                    .withLabel(dev.getDeviceDescription()+ " ("+id+")")
                    .withRepresentationProperty(BroadlinkBindingConstants.HOST)
                    .build();

            thingDiscovered(discoveryResult);

        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.trace("Starting background scan for Broadlink devices");
        startScan();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.trace("Stopping background scan for Broadlink devices");
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
    @NonNullByDefault({})
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

    private String getHostnameWithoutDomain(String hostname){
        String broadlinkRegex = "broadlink-oem(-[a-z,0-9]{2}){4}.*";
        if(hostname.matches(broadlinkRegex)) {
            String[] dotSeparatedString = hostname.split("\\.");
            logger.debug("Found original broadlink DNS name {}, removing domain",hostname);
            return dotSeparatedString[0];
        }else{
            logger.debug("DNS name does not match original broadlink name: {}, using it without modification. ",hostname);
            return hostname;
        }
    }

}
