/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.orbitbhyve.internal.discovery;

import static org.openhab.binding.orbitbhyve.internal.OrbitBhyveBindingConstants.THING_TYPE_SPRINKLER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.orbitbhyve.internal.handler.OrbitBhyveBridgeHandler;
import org.openhab.binding.orbitbhyve.internal.model.OrbitBhyveDevice;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OrbitBhyveDiscoveryService} discovers sprinklers
 * associated with your Orbit B-Hyve cloud account.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = OrbitBhyveDiscoveryService.class)
@NonNullByDefault
public class OrbitBhyveDiscoveryService extends AbstractThingHandlerDiscoveryService<OrbitBhyveBridgeHandler> {
    private final Logger logger = LoggerFactory.getLogger(OrbitBhyveDiscoveryService.class);

    private @Nullable ScheduledFuture<?> discoveryJob;

    private static final int DISCOVERY_TIMEOUT_SEC = 10;
    private static final int DISCOVERY_REFRESH_SEC = 1800;

    public OrbitBhyveDiscoveryService() {
        super(OrbitBhyveBridgeHandler.class, DISCOVERY_TIMEOUT_SEC);
        logger.debug("Creating discovery service");
    }

    @Override
    protected void startScan() {
        runDiscovery();
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting Orbit B-Hyve background discovery");

        ScheduledFuture<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob == null || localDiscoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::runDiscovery, 10, DISCOVERY_REFRESH_SEC,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping Orbit B-Hyve background discovery");
        ScheduledFuture<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob != null && !localDiscoveryJob.isCancelled()) {
            localDiscoveryJob.cancel(true);
        }
    }

    private synchronized void runDiscovery() {
        if (ThingStatus.ONLINE == thingHandler.getThing().getStatus()) {
            List<OrbitBhyveDevice> devices = thingHandler.getDevices();
            logger.debug("Discovered total of {} devices", devices.size());
            for (OrbitBhyveDevice device : devices) {
                sprinklerDiscovered(device);
            }
        }
    }

    private void sprinklerDiscovered(OrbitBhyveDevice device) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("id", device.getId());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, device.getFwVersion());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, device.getHwVersion());
        properties.put(Thing.PROPERTY_MAC_ADDRESS, device.getMacAddress());
        properties.put(Thing.PROPERTY_MODEL_ID, device.getType());
        properties.put("Zones", device.getNumStations());
        properties.put("Active zones", device.getZones().size());

        ThingUID thingUID = new ThingUID(THING_TYPE_SPRINKLER, thingHandler.getThing().getUID(), device.getId());

        logger.debug("Detected a/an {} - label: {} id: {}", THING_TYPE_SPRINKLER.getId(), device.getName(),
                device.getId());
        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_SPRINKLER)
                .withProperties(properties).withRepresentationProperty("id").withLabel(device.getName())
                .withBridge(thingHandler.getThing().getUID()).build());
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return Set.of(THING_TYPE_SPRINKLER);
    }
}
