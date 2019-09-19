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
package org.openhab.binding.spotify.internal.discovery;

import static org.openhab.binding.spotify.internal.SpotifyBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.spotify.internal.SpotifyAccountHandler;
import org.openhab.binding.spotify.internal.SpotifyBindingConstants;
import org.openhab.binding.spotify.internal.api.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SpotifyDeviceDiscoveryService} queries the Spotify Web API for available devices.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Simplfied code to make call to shared code
 */
@NonNullByDefault
public class SpotifyDeviceDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    // id for device is derived by stripping id of device with this length
    private static final int PLAYER_ID_LENGTH = 4;
    // Only devices can be discovered. A bridge must be manually added.
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_DEVICE);
    // The call to listDevices is fast
    private static final int DISCOVERY_TIME_SECONDS = 10;
    // Check every minute for new devices
    private static final long BACKGROUND_SCAN_REFRESH_MINUTES = 1;

    private final Logger logger = LoggerFactory.getLogger(SpotifyDeviceDiscoveryService.class);

    private @NonNullByDefault({}) SpotifyAccountHandler bridgeHandler;
    private @NonNullByDefault({}) ThingUID bridgeUID;

    private @Nullable ScheduledFuture<?> backgroundFuture;

    public SpotifyDeviceDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIME_SECONDS);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public void activate() {
        Map<String, @Nullable Object> properties = new HashMap<>();
        properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.TRUE);
        super.activate(properties);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof SpotifyAccountHandler) {
            bridgeHandler = (SpotifyAccountHandler) handler;
            bridgeUID = bridgeHandler.getUID();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
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
        if (bridgeHandler.isOnline()) {
            logger.debug("Starting Spotify Device discovery for bridge {}", bridgeUID);
            try {
                bridgeHandler.listDevices().forEach(this::thingDiscovered);
            } catch (RuntimeException e) {
                logger.debug("Finding devices failed with message: {}", e.getMessage(), e);
            }
        }
    }

    private void thingDiscovered(Device device) {
        Map<String, Object> properties = new HashMap<String, Object>();

        properties.put(PROPERTY_SPOTIFY_DEVICE_NAME, device.getName());
        ThingUID thing = new ThingUID(SpotifyBindingConstants.THING_TYPE_DEVICE, bridgeUID,
                device.getId().substring(0, PLAYER_ID_LENGTH));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thing).withBridge(bridgeUID)
                .withProperties(properties).withRepresentationProperty(PROPERTY_SPOTIFY_DEVICE_NAME)
                .withLabel(device.getName()).build();

        thingDiscovered(discoveryResult);
    }
}
