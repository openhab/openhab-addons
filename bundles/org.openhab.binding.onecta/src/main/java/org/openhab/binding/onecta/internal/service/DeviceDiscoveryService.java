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

import static org.openhab.binding.onecta.internal.OnectaBridgeConstants.*;
import static org.openhab.binding.onecta.internal.api.Enums.ManagementPoint;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.api.Enums;
import org.openhab.binding.onecta.internal.api.OnectaConnectionClient;
import org.openhab.binding.onecta.internal.api.dto.units.Unit;
import org.openhab.binding.onecta.internal.handler.OnectaBridgeHandler;
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
 *
 * @author Alexander Drent - Initial contribution
 *
 */
@Component(scope = ServiceScope.PROTOTYPE, service = DeviceDiscoveryService.class)
@NonNullByDefault
public class DeviceDiscoveryService extends AbstractThingHandlerDiscoveryService<OnectaBridgeHandler> {
    private final Logger logger = LoggerFactory.getLogger(DeviceDiscoveryService.class);
    private static final int REFRESH_MINUTES = 1;
    public static final String PROPERTY_DISCOVERED = "discovered";

    @Nullable
    private final OnectaConnectionClient onectaConnectionClient = new OnectaConnectionClient();
    private @Nullable ScheduledFuture<?> backgroundDiscoveryFuture;

    public DeviceDiscoveryService() {
        super(OnectaBridgeHandler.class, SUPPORTED_THING_TYPES, 10);
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
        logger.debug("Start discovery.");
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
            thingDiscover(unit, ManagementPoint.CLIMATECONTROL, THING_TYPE_CLIMATECONTROL);
            thingDiscover(unit, ManagementPoint.GATEWAY, THING_TYPE_GATEWAY);
            thingDiscover(unit, ManagementPoint.WATERTANK, THING_TYPE_WATERTANK);
            thingDiscover(unit, ManagementPoint.INDOORUNIT, THING_TYPE_INDOORUNIT);
        }
    }

    protected void thingDiscover(Unit unit, Enums.ManagementPoint onectaManagementPoint, ThingTypeUID thingTypeUID) {
        if (unit.findManagementPointsByType(onectaManagementPoint.getValue()) != null) {
            ThingUID bridgeUID = this.thingHandler.getThing().getUID();
            String unitId = unit.getId();
            String unitName = unit.findManagementPointsByType(ManagementPoint.CLIMATECONTROL.getValue()).getNameValue();
            unitName = !unitName.isEmpty() ? unitName : unitId;
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("unitID", unitId);

            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, unitId);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(bridgeUID)
                    .withLabel(String.format("Daikin Onecta (%s) (%s)", onectaManagementPoint.getValue(), unitName))
                    .build();

            thingDiscovered(discoveryResult);
            logger.debug("Discovered a onecta {} thing with ID '{}' '{}'", onectaManagementPoint.getValue(), unitId,
                    unitName);
            this.thingHandler.getThing().setProperty(
                    String.format("%s %s (%s)", PROPERTY_DISCOVERED, onectaManagementPoint.getValue(), unitName),
                    unitId);
        }
    }

    @Override
    protected void thingDiscovered(DiscoveryResult discoveryResult) {
        super.thingDiscovered(discoveryResult);
    }
}
