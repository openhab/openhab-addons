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
package org.openhab.binding.pilight.internal.discovery;

import static org.openhab.binding.pilight.internal.PilightBindingConstants.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pilight.internal.dto.Config;
import org.openhab.binding.pilight.internal.dto.DeviceType;
import org.openhab.binding.pilight.internal.dto.Status;
import org.openhab.binding.pilight.internal.handler.PilightBridgeHandler;
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
 * The {@link PilightDeviceDiscoveryService} discovers pilight devices after a bridge thing has been created and
 * connected to the pilight daemon. Things are discovered periodically in the background or after a manual trigger.
 *
 * @author Niklas Dörfler - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = PilightDeviceDiscoveryService.class)
@NonNullByDefault
public class PilightDeviceDiscoveryService extends AbstractThingHandlerDiscoveryService<PilightBridgeHandler> {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_CONTACT, THING_TYPE_DIMMER,
            THING_TYPE_GENERIC, THING_TYPE_SWITCH);

    private static final int AUTODISCOVERY_SEARCH_TIME_SEC = 10;
    private static final int AUTODISCOVERY_BACKGROUND_SEARCH_INITIAL_DELAY_SEC = 20;
    private static final int AUTODISCOVERY_BACKGROUND_SEARCH_INTERVAL_SEC = 10 * 60; // 10 minutes

    private final Logger logger = LoggerFactory.getLogger(PilightDeviceDiscoveryService.class);

    private @Nullable ScheduledFuture<?> backgroundDiscoveryJob;
    private CompletableFuture<Config> configFuture;
    private CompletableFuture<List<Status>> statusFuture;

    public PilightDeviceDiscoveryService() {
        super(PilightBridgeHandler.class, SUPPORTED_THING_TYPES_UIDS, AUTODISCOVERY_SEARCH_TIME_SEC);
        configFuture = new CompletableFuture<>();
        statusFuture = new CompletableFuture<>();
    }

    @Override
    protected void startScan() {
        configFuture = new CompletableFuture<>();
        statusFuture = new CompletableFuture<>();

        configFuture.thenAcceptBoth(statusFuture, (config, allStatus) -> {
            removeOlderResults(getTimestampOfLastScan(), thingHandler.getThing().getUID());
            config.getDevices().forEach((deviceId, device) -> {
                final Optional<Status> status = allStatus.stream().filter(s -> s.getDevices().contains(deviceId))
                        .findFirst();

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

                final ThingUID thingUID = new ThingUID(thingTypeUID, thingHandler.getThing().getUID(), deviceId);

                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                        .withProperties(Map.of(PROPERTY_NAME, deviceId)).withBridge(thingHandler.getThing().getUID())
                        .withRepresentationProperty(PROPERTY_NAME)
                        .withLabel("Pilight " + typeString + " Device '" + deviceId + "'").build();

                thingDiscovered(discoveryResult);
            });
        });

        thingHandler.refreshConfigAndStatus();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        configFuture.cancel(true);
        statusFuture.cancel(true);
        removeOlderResults(getTimestampOfLastScan(), thingHandler.getThing().getUID());
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start Pilight device background discovery");
        final @Nullable ScheduledFuture<?> backgroundDiscoveryJob = this.backgroundDiscoveryJob;
        if (backgroundDiscoveryJob == null || backgroundDiscoveryJob.isCancelled()) {
            this.backgroundDiscoveryJob = scheduler.scheduleWithFixedDelay(this::startScan,
                    AUTODISCOVERY_BACKGROUND_SEARCH_INITIAL_DELAY_SEC, AUTODISCOVERY_BACKGROUND_SEARCH_INTERVAL_SEC,
                    TimeUnit.SECONDS);
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
    public void initialize() {
        removeOlderResults(Instant.now().toEpochMilli(), thingHandler.getThing().getUID());
        thingHandler.registerDiscoveryListener(this);

        super.initialize();
        super.modified(Map.of(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY,
                thingHandler.isBackgroundDiscoveryEnabled()));
    }

    @Override
    public void dispose() {
        super.dispose();
        removeOlderResults(Instant.now().toEpochMilli(), thingHandler.getThing().getUID());
        thingHandler.unregisterDiscoveryListener();
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
}
