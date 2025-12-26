/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.onecta.internal.service;

import static org.openhab.binding.onecta.internal.api.Enums.ManagementPoint;
import static org.openhab.binding.onecta.internal.constants.OnectaBridgeConstants.*;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.OnectaConfiguration;
import org.openhab.binding.onecta.internal.api.OnectaConnectionClient;
import org.openhab.binding.onecta.internal.api.dto.units.Unit;
import org.openhab.binding.onecta.internal.handler.OnectaBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Drent - Initial contribution
 *
 */
@Component(scope = ServiceScope.PROTOTYPE, service = DeviceDiscoveryService.class)
@NonNullByDefault
public class DeviceDiscoveryService extends AbstractThingHandlerDiscoveryService<OnectaBridgeHandler> {
    private final Logger logger = LoggerFactory.getLogger(DeviceDiscoveryService.class);
    private static final int REFRESH_MINUTES = 5;

    private Map<ManagementPoint, ThingTypeUID> discoveryMap = new HashMap<>();

    @Nullable
    private final OnectaConnectionClient onectaConnectionClient = OnectaConfiguration.getOnectaConnectionClient();;
    private @Nullable ScheduledFuture<?> backgroundDiscoveryFuture;

    @Activate
    public DeviceDiscoveryService() {
        super(OnectaBridgeHandler.class, SUPPORTED_THING_TYPES, 10);
        discoveryMap.put(ManagementPoint.CLIMATECONTROL, THING_TYPE_CLIMATECONTROL);
        discoveryMap.put(ManagementPoint.GATEWAY, THING_TYPE_GATEWAY);
        discoveryMap.put(ManagementPoint.WATERTANK, THING_TYPE_WATERTANK);
    }

    @Override
    public void initialize() {
        thingHandler.setDiscoveryService(this);
        super.initialize();
    }

    @Override
    public void activate() {
        super.activate(Map.of(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, true));
    }

    @Override
    public void startScan() {
        logger.debug("Starting discovery.");
        scheduler.execute(this::scan);
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting background discovery");
        ScheduledFuture<?> localDiscoveryFuture = backgroundDiscoveryFuture;
        if (localDiscoveryFuture == null || localDiscoveryFuture.isCancelled()) {
            backgroundDiscoveryFuture = scheduler.scheduleWithFixedDelay(this::scan, 0, REFRESH_MINUTES,
                    TimeUnit.MINUTES);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping background discovery");

        ScheduledFuture<?> localDiscoveryFuture = backgroundDiscoveryFuture;
        if (localDiscoveryFuture != null) {
            localDiscoveryFuture.cancel(true);
            backgroundDiscoveryFuture = null;
        }
    }

    protected void scan() {
        logger.debug("Scanning for devices");

        List<Unit> units = onectaConnectionClient.getUnits().getAll();

        for (Unit unit : units) {
            thingDiscover(unit);
        }
    }

    protected void thingDiscover(Unit unit) {
        List<String> discoveredThings = new ArrayList<>();
        String unitId = unit.getId();
        String unitName = unit.findManagementPointsByType(ManagementPoint.CLIMATECONTROL.getValue()).getNameValue();
        unitName = !unitName.isEmpty() ? unitName : unitId;

        for (Map.Entry<ManagementPoint, ThingTypeUID> entry : discoveryMap.entrySet()) {
            ManagementPoint onectaManagementPoint = entry.getKey();
            ThingTypeUID thingTypeUID = entry.getValue();

            if (unit.findManagementPointsByType(onectaManagementPoint.getValue()) != null) {
                discoveredThings.add(thingTypeUID.getId());
                ThingUID bridgeUID = this.thingHandler.getThing().getUID();
                Map<String, Object> properties = new LinkedHashMap<>();
                properties.put("unitID", unitId);

                ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, unitId);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(bridgeUID).withLabel(OnectaConfiguration.getTranslation()
                                .getText("discovery.found.thing.inbox", onectaManagementPoint.getValue(), unitName))
                        .build();

                thingDiscovered(discoveryResult);
                logger.debug("Discovered a onecta {} unit '{}' with ID '{}'", onectaManagementPoint.getValue(),
                        unitName, unitId);
            }
        }

        thingHandler.getThing().setProperty(unitName + " (" + String.join(", ", discoveredThings) + ")", unitId);
    }

    @Override
    protected void thingDiscovered(DiscoveryResult discoveryResult) {
        super.thingDiscovered(discoveryResult);
    }
}
