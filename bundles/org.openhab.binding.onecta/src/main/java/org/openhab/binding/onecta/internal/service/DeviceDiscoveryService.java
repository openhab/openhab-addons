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
package org.openhab.binding.onecta.internal.service;

import static org.openhab.binding.onecta.internal.OnectaBridgeConstants.*;
import static org.openhab.binding.onecta.internal.OnectaGatewayConstants.*;
import static org.openhab.binding.onecta.internal.api.Enums.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.api.Enums;
import org.openhab.binding.onecta.internal.api.OnectaConnectionClient;
import org.openhab.binding.onecta.internal.api.dto.units.Unit;
import org.openhab.binding.onecta.internal.exception.DaikinCommunicationException;
import org.openhab.binding.onecta.internal.handler.OnectaBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Drent - Initial contribution
 *
 */
public class DeviceDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(DeviceDiscoveryService.class);
    @Nullable
    private OnectaBridgeHandler bridgeHandler = null;
    private final OnectaConnectionClient onectaConnectionClient = new OnectaConnectionClient();

    public DeviceDiscoveryService(OnectaBridgeHandler bridgeHandler) throws IllegalArgumentException {
        super(20);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    public void startScan() {
        logger.debug("startScan.");
        if (bridgeHandler == null) {
            return;
        }
        // Trigger no scan if offline
        if (bridgeHandler.getThing().getStatus() == ThingStatus.OFFLINE) {
            return;
        }

        try {
            onectaConnectionClient.refreshUnitsData();
            List<Unit> units = onectaConnectionClient.getUnits().getAll();
            for (Unit unit : units) {
                thingDiscover(unit, ManagementPoint.CLIMATECONTROL, THING_TYPE_CLIMATECONTROL);
                thingDiscover(unit, ManagementPoint.GATEWAY, THING_TYPE_GATEWAY);
                thingDiscover(unit, ManagementPoint.WATERTANK, THING_TYPE_WATERTANK);
                thingDiscover(unit, ManagementPoint.INDOORUNIT, THING_TYPE_INDOORUNIT);
            }
        } catch (DaikinCommunicationException e) {
            logger.error("Error in DiscoveryService", e);
        }
    }

    protected void thingDiscover(Unit unit, Enums.ManagementPoint onectaManagementPoint, ThingTypeUID thingTypeUID) {

        if (unit.findManagementPointsByType(onectaManagementPoint.getValue()) != null) {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            String unitId = unit.getId();
            String unitName = unit.findManagementPointsByType(ManagementPoint.CLIMATECONTROL.getValue()).getNameValue();
            unitName = !unitName.isEmpty() ? unitName : unitId;
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("unitID", unitId);

            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, unitId);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(bridgeHandler.getThing().getUID())
                    .withLabel(String.format("Daikin Onecta (%s) (%s)", onectaManagementPoint.getValue(), unitName))
                    .build();

            thingDiscovered(discoveryResult);
            logger.info("Discovered a onecta {} thing with ID '{}' '{}'", onectaManagementPoint.getValue(), unitId,
                    unitName);
            bridgeHandler.getThing().setProperty(
                    String.format("%s %s (%s)", PROPERTY_GW_DISCOVERED, onectaManagementPoint.getValue(), unitName),
                    unitId);
        }
    }

    @Override
    protected void thingDiscovered(DiscoveryResult discoveryResult) {
        super.thingDiscovered(discoveryResult);
    }
}
