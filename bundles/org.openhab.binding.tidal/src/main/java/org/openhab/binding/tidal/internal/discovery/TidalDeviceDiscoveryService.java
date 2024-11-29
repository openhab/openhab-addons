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
package org.openhab.binding.tidal.internal.discovery;

import static org.openhab.binding.tidal.internal.TidalBindingConstants.PROPERTY_TIDAL_DEVICE_NAME;
import static org.openhab.binding.tidal.internal.TidalBindingConstants.THING_TYPE_DEVICE;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tidal.internal.TidalAccountHandler;
import org.openhab.binding.tidal.internal.TidalBindingConstants;
import org.openhab.binding.tidal.internal.api.model.Device;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TidalDeviceDiscoveryService} queries the Tidal Web API for available devices.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Simplfied code to make call to shared code
 */
@Component(scope = ServiceScope.PROTOTYPE, service = TidalDeviceDiscoveryService.class)
@NonNullByDefault
public class TidalDeviceDiscoveryService extends AbstractThingHandlerDiscoveryService<TidalAccountHandler> {

    // id for device is derived by stripping id of device with this length
    private static final int PLAYER_ID_LENGTH = 4;
    // Only devices can be discovered. A bridge must be manually added.
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_DEVICE);
    // The call to listDevices is fast
    private static final int DISCOVERY_TIME_SECONDS = 10;
    // Check every minute for new devices
    private static final long BACKGROUND_SCAN_REFRESH_MINUTES = 1;
    // Time to life for discovered things.
    private static final long TTL_SECONDS = Duration.ofHours(1).toSeconds();

    private final Logger logger = LoggerFactory.getLogger(TidalDeviceDiscoveryService.class);
    private @NonNullByDefault({}) ThingUID bridgeUID;

    private @Nullable ScheduledFuture<?> backgroundFuture;

    public TidalDeviceDiscoveryService() {
        super(TidalAccountHandler.class, SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIME_SECONDS);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public void activate() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.TRUE);
        super.activate(properties);
    }

    @Override
    public void initialize() {
        bridgeUID = thingHandler.getUID();
        super.initialize();
    }

    @Override
    protected synchronized void startBackgroundDiscovery() {
        stopBackgroundDiscovery();
        backgroundFuture = scheduler.scheduleWithFixedDelay(this::startScan, BACKGROUND_SCAN_REFRESH_MINUTES,
                BACKGROUND_SCAN_REFRESH_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    protected synchronized void stopBackgroundDiscovery() {
        if (backgroundFuture != null) {
            backgroundFuture.cancel(true);
            backgroundFuture = null;
        }
    }

    @Override
    protected void startScan() {
        // If the bridge is not online no other thing devices can be found, so no reason to scan at this moment.
        removeOlderResults(getTimestampOfLastScan());
        if (thingHandler.isOnline()) {
            logger.debug("Starting Tidal Device discovery for bridge {}", bridgeUID);
            try {
                thingHandler.listDevices().forEach(this::thingDiscovered);
            } catch (final RuntimeException e) {
                logger.debug("Finding devices failed with message: {}", e.getMessage(), e);
            }
        }
    }

    private void thingDiscovered(Device device) {
        final Map<String, Object> properties = new HashMap<>();

        properties.put(PROPERTY_TIDAL_DEVICE_NAME, device.getName());
        final ThingUID thing = new ThingUID(TidalBindingConstants.THING_TYPE_DEVICE, bridgeUID,
                device.getId().substring(0, PLAYER_ID_LENGTH));

        final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thing).withBridge(bridgeUID)
                .withProperties(properties).withRepresentationProperty(PROPERTY_TIDAL_DEVICE_NAME)
                .withTTL(TTL_SECONDS).withLabel(device.getName()).build();

        thingDiscovered(discoveryResult);
    }
}
