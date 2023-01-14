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
package org.openhab.binding.pilight.internal.discovery;

import static org.openhab.binding.pilight.internal.PilightBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pilight.internal.PilightHandlerFactory;
import org.openhab.binding.pilight.internal.dto.Config;
import org.openhab.binding.pilight.internal.dto.DeviceType;
import org.openhab.binding.pilight.internal.dto.Status;
import org.openhab.binding.pilight.internal.handler.PilightBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PilightDeviceDiscoveryService} discovers pilight devices after a bridge thing has been created and
 * connected to the pilight daemon. Things are discovered periodically in the background or after a manual trigger.
 *
 * @author Niklas Dörfler - Initial contribution
 */
@NonNullByDefault
public class PilightDeviceDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = PilightHandlerFactory.SUPPORTED_THING_TYPES_UIDS;

    private static final int AUTODISCOVERY_SEARCH_TIME_SEC = 10;
    private static final int AUTODISCOVERY_BACKGROUND_SEARCH_INTERVAL_SEC = 60 * 10;

    private final Logger logger = LoggerFactory.getLogger(PilightDeviceDiscoveryService.class);

    private @Nullable PilightBridgeHandler pilightBridgeHandler;
    private @Nullable ThingUID bridgeUID;

    private @Nullable ScheduledFuture<?> backgroundDiscoveryJob;
    private CompletableFuture<Config> configFuture;
    private CompletableFuture<List<Status>> statusFuture;

    public PilightDeviceDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, AUTODISCOVERY_SEARCH_TIME_SEC);
        configFuture = new CompletableFuture<>();
        statusFuture = new CompletableFuture<>();
    }

    @Override
    protected void startScan() {
        if (pilightBridgeHandler != null) {
            configFuture = new CompletableFuture<>();
            statusFuture = new CompletableFuture<>();

            configFuture.thenAcceptBoth(statusFuture, (config, allStatus) -> {
                removeOlderResults(getTimestampOfLastScan(), bridgeUID);
                config.getDevices().forEach((deviceId, device) -> {
                    if (this.pilightBridgeHandler != null) {
                        final Optional<Status> status = allStatus.stream()
                                .filter(s -> s.getDevices().contains(deviceId)).findFirst();

                        final ThingTypeUID thingTypeUID;
                        final String typeString;

                        if (status.isPresent()) {
                            if (status.get().getType().equals(DeviceType.SWITCH)) {
                                thingTypeUID = new ThingTypeUID(BINDING_ID, THING_TYPE_SWITCH.getId());
                                typeString = "Switch";
                            } else if (status.get().getType().equals(DeviceType.DIMMER)) {
                                thingTypeUID = new ThingTypeUID(BINDING_ID, THING_TYPE_DIMMER.getId());
                                typeString = "Dimmer";
                            } else if (status.get().getType().equals(DeviceType.VALUE)) {
                                thingTypeUID = new ThingTypeUID(BINDING_ID, THING_TYPE_GENERIC.getId());
                                typeString = "Generic";
                            } else if (status.get().getType().equals(DeviceType.CONTACT)) {
                                thingTypeUID = new ThingTypeUID(BINDING_ID, THING_TYPE_CONTACT.getId());
                                typeString = "Contact";
                            } else {
                                thingTypeUID = new ThingTypeUID(BINDING_ID, THING_TYPE_GENERIC.getId());
                                typeString = "Generic";
                            }
                        } else {
                            thingTypeUID = new ThingTypeUID(BINDING_ID, THING_TYPE_GENERIC.getId());
                            typeString = "Generic";
                        }

                        final @Nullable PilightBridgeHandler pilightBridgeHandler = this.pilightBridgeHandler;
                        if (pilightBridgeHandler != null) {
                            final ThingUID thingUID = new ThingUID(thingTypeUID,
                                    pilightBridgeHandler.getThing().getUID(), deviceId);

                            final Map<String, Object> properties = new HashMap<>();
                            properties.put(PROPERTY_NAME, deviceId);

                            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                                    .withThingType(thingTypeUID).withProperties(properties).withBridge(bridgeUID)
                                    .withRepresentationProperty(PROPERTY_NAME)
                                    .withLabel("Pilight " + typeString + " Device '" + deviceId + "'").build();

                            thingDiscovered(discoveryResult);
                        }
                    }
                });
            });

            final @Nullable PilightBridgeHandler pilightBridgeHandler = this.pilightBridgeHandler;
            if (pilightBridgeHandler != null) {
                pilightBridgeHandler.refreshConfigAndStatus();
            }
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        configFuture.cancel(true);
        statusFuture.cancel(true);
        if (bridgeUID != null) {
            removeOlderResults(getTimestampOfLastScan(), bridgeUID);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start Pilight device background discovery");
        final @Nullable ScheduledFuture<?> backgroundDiscoveryJob = this.backgroundDiscoveryJob;
        if (backgroundDiscoveryJob == null || backgroundDiscoveryJob.isCancelled()) {
            this.backgroundDiscoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, 20,
                    AUTODISCOVERY_BACKGROUND_SEARCH_INTERVAL_SEC, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop Pilight device background discovery");
        final @Nullable ScheduledFuture<?> backgroundDiscoveryJob = this.backgroundDiscoveryJob;
        if (backgroundDiscoveryJob != null) {
            backgroundDiscoveryJob.cancel(true);
            this.backgroundDiscoveryJob = null;
        }
    }

    @Override
    public void setThingHandler(final ThingHandler handler) {
        if (handler instanceof PilightBridgeHandler) {
            this.pilightBridgeHandler = (PilightBridgeHandler) handler;
            final @Nullable PilightBridgeHandler pilightBridgeHandler = this.pilightBridgeHandler;
            if (pilightBridgeHandler != null) {
                bridgeUID = pilightBridgeHandler.getThing().getUID();
            }
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return pilightBridgeHandler;
    }

    @Override
    public void activate() {
        super.activate(null);
        final @Nullable PilightBridgeHandler pilightBridgeHandler = this.pilightBridgeHandler;
        if (pilightBridgeHandler != null) {
            pilightBridgeHandler.registerDiscoveryListener(this);
        }
    }

    @Override
    public void deactivate() {
        if (bridgeUID != null) {
            removeOlderResults(getTimestampOfLastScan(), bridgeUID);
        }

        final @Nullable PilightBridgeHandler pilightBridgeHandler = this.pilightBridgeHandler;
        if (pilightBridgeHandler != null) {
            pilightBridgeHandler.unregisterDiscoveryListener();
        }

        super.deactivate();
    }

    /**
     * Method used to get pilight device config into the discovery class.
     *
     * @param config config to get
     */
    public void setConfig(Config config) {
        configFuture.complete(config);
    }

    /**
     * Method used to get pilight device status list into the discovery class.
     *
     * @param status list of status objects
     */
    public void setStatus(List<Status> status) {
        statusFuture.complete(status);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }
}
