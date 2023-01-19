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
package org.openhab.binding.globalcache.internal.discovery;

import static org.openhab.binding.globalcache.internal.GlobalCacheBindingConstants.*;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GlobalCacheDiscoveryService} class implements a service
 * for discovering the GlobalCache devices.
 *
 * @author Mark Hilbush - Initial contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.globalcache")
public class GlobalCacheDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(GlobalCacheDiscoveryService.class);

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> gcDiscoveryJob;

    // Discovery parameters
    public static final boolean BACKGROUND_DISCOVERY_ENABLED = true;
    public static final int BACKGROUND_DISCOVERY_DELAY = 10;

    private NetworkAddressService networkAddressService;

    private boolean terminate;

    public GlobalCacheDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 0, BACKGROUND_DISCOVERY_ENABLED);
        gcDiscoveryJob = null;
        terminate = false;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void activate(Map<String, Object> configProperties) {
        logger.debug("Globalcache discovery service activated");
        super.activate(configProperties);
    }

    @Override
    protected void deactivate() {
        logger.debug("Globalcache discovery service deactivated");
        stopBackgroundDiscovery();
        super.deactivate();
    }

    @Override
    @Modified
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (gcDiscoveryJob == null) {
            terminate = false;
            logger.debug("Starting background discovery job in {} seconds", BACKGROUND_DISCOVERY_DELAY);
            gcDiscoveryJob = scheduledExecutorService.schedule(this::discover, BACKGROUND_DISCOVERY_DELAY,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (gcDiscoveryJob != null) {
            logger.debug("Canceling background discovery job");
            terminate = true;
            gcDiscoveryJob.cancel(false);
            gcDiscoveryJob = null;
        }
    }

    @Override
    public void startScan() {
    }

    @Override
    public void stopScan() {
    }

    private synchronized void discover() {
        logger.debug("Discovery job is running");

        MulticastListener gcMulticastListener;

        try {
            gcMulticastListener = new MulticastListener(networkAddressService.getPrimaryIpv4HostAddress());
        } catch (SocketException se) {
            logger.error("Discovery job got Socket exception creating multicast socket: {}", se.getMessage());
            return;
        } catch (IOException ioe) {
            logger.error("Discovery job got IO exception creating multicast socket: {}", ioe.getMessage());
            return;
        }

        while (!terminate) {
            boolean beaconReceived;
            try {
                // Wait for a discovery beacon.
                beaconReceived = gcMulticastListener.waitForBeacon();
            } catch (IOException ioe) {
                logger.debug("Discovery job got exception waiting for beacon: {}", ioe.getMessage());
                beaconReceived = false;
            }

            if (beaconReceived) {
                // We got a discovery beacon. Process it as a potential new thing
                Map<String, Object> properties = new HashMap<>();

                properties.put(THING_PROPERTY_IP, gcMulticastListener.getIPAddress());
                properties.put(THING_PROPERTY_MAC, gcMulticastListener.getMACAddress());
                properties.put(THING_PROPERTY_UID, gcMulticastListener.getUID());
                properties.put(Thing.PROPERTY_VENDOR, gcMulticastListener.getVendor());
                properties.put(Thing.PROPERTY_MODEL_ID, gcMulticastListener.getModel());
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, gcMulticastListener.getSoftwareRevision());
                properties.put(Thing.PROPERTY_HARDWARE_VERSION, gcMulticastListener.getHardwareRevision());
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, gcMulticastListener.getSerialNumber());

                logger.trace("Device of type {} discovered with MAC {} and IP {}", gcMulticastListener.getModel(),
                        gcMulticastListener.getMACAddress(), gcMulticastListener.getIPAddress());

                ThingTypeUID typeUID = gcMulticastListener.getThingTypeUID();
                if (typeUID != null) {
                    ThingUID uid = new ThingUID(typeUID, gcMulticastListener.getSerialNumber());
                    logger.trace("Creating discovery result for: {}, type={}, IP={}", uid,
                            gcMulticastListener.getModel(), gcMulticastListener.getIPAddress());
                    thingDiscovered(DiscoveryResultBuilder.create(uid).withProperties(properties)
                            .withRepresentationProperty(THING_PROPERTY_MAC)
                            .withLabel(gcMulticastListener.getVendor() + " " + gcMulticastListener.getModel()).build());
                }
            }
        }
        gcMulticastListener.shutdown();
        logger.debug("Discovery job is exiting");
    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }
}
