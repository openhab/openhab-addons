/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.automower.internal.discovery;

import static org.openhab.binding.automower.internal.AutomowerBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.automower.internal.AutomowerBindingConstants;
import org.openhab.binding.automower.internal.bridge.AutomowerBridgeHandler;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Capabilities;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Mower;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerListResult;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.StayOutZone;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.StayOutZones;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.WorkArea;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AutomowerDiscoveryService} is responsible for discovering new mowers available for the
 * configured app key.
 *
 * @author Markus Pfleger - Initial contribution
 * @author MikeTheTux - Added StayoutZone and WorkArea as well as background discovery
 */
@NonNullByDefault
public class AutomowerDiscoveryService extends AbstractDiscoveryService {

    private final AutomowerBridgeHandler bridgeHandler;
    private final Logger logger = LoggerFactory.getLogger(AutomowerDiscoveryService.class);
    private @Nullable ScheduledExecutorService scheduler;

    public AutomowerDiscoveryService(AutomowerBridgeHandler bridgeHandler) {
        super(Set.of(THING_TYPE_AUTOMOWER), 10, false);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        checkDiscovered();
    }

    public void startBackgroundDiscovery() {
        logger.trace("Starting background discovery scheduler");
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        // Poll bridge every 60 seconds
        scheduler.scheduleWithFixedDelay(() -> checkDiscovered(), 10, 60, TimeUnit.SECONDS);
        this.scheduler = scheduler;
    }

    public void stopBackgroundDiscovery() {
        logger.trace("Stopping background discovery scheduler");
        ScheduledExecutorService scheduler = this.scheduler;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow(); // Stop any running discovery threads
            this.scheduler = null;
        }
    }

    private void checkDiscovered() {
        Optional<MowerListResult> registeredMowers = bridgeHandler.getAutomowers();

        registeredMowers.ifPresent(mowers -> {
            for (Mower mower : mowers.getData()) {
                ThingUID bridgeUID = bridgeHandler.getThing().getUID();
                ThingTypeUID thingTypeUID = THING_TYPE_AUTOMOWER;
                ThingUID mowerThingUid = new ThingUID(THING_TYPE_AUTOMOWER, bridgeUID, mower.getId());

                Map<String, Object> properties = new HashMap<>();
                properties.put(AutomowerBindingConstants.AUTOMOWER_ID, mower.getId());
                properties.put(AutomowerBindingConstants.AUTOMOWER_SERIAL_NUMBER,
                        mower.getAttributes().getSystem().getSerialNumber());
                properties.put(AutomowerBindingConstants.AUTOMOWER_MODEL, mower.getAttributes().getSystem().getModel());
                properties.put(AutomowerBindingConstants.AUTOMOWER_NAME, mower.getAttributes().getSystem().getName());

                properties.put(AutomowerBindingConstants.AUTOMOWER_CAN_CONFIRM_ERROR,
                        (mower.getAttributes().getCapabilities().canConfirmError() ? "yes" : "no"));
                properties.put(AutomowerBindingConstants.AUTOMOWER_HAS_HEADLIGHTS,
                        (mower.getAttributes().getCapabilities().hasHeadlights() ? "yes" : "no"));
                properties.put(AutomowerBindingConstants.AUTOMOWER_HAS_POSITION,
                        (mower.getAttributes().getCapabilities().hasPosition() ? "yes" : "no"));
                properties.put(AutomowerBindingConstants.AUTOMOWER_HAS_STAY_OUT_ZONES,
                        (mower.getAttributes().getCapabilities().hasStayOutZones() ? "yes" : "no"));
                properties.put(AutomowerBindingConstants.AUTOMOWER_HAS_WORK_AREAS,
                        (mower.getAttributes().getCapabilities().hasWorkAreas() ? "yes" : "no"));

                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(mowerThingUid)
                        .withThingType(thingTypeUID).withProperties(properties).withBridge(bridgeUID)
                        .withRepresentationProperty(AutomowerBindingConstants.AUTOMOWER_ID)
                        .withLabel(mower.getAttributes().getSystem().getName()).build();

                thingDiscovered(discoveryResult);

                Capabilities capabilities = mower.getAttributes().getCapabilities();
                if (capabilities.hasStayOutZones()) {
                    StayOutZones stayOutZones = mower.getAttributes().getStayOutZones();
                    if (stayOutZones != null) {
                        for (StayOutZone stayOutZone : stayOutZones.getZones()) {
                            ThingTypeUID zoneThingTypeUID = THING_TYPE_STAYOUTZONE;
                            ThingUID zoneThingUid = new ThingUID(THING_TYPE_STAYOUTZONE, bridgeUID,
                                    stayOutZone.getId());

                            Map<String, Object> zoneProperties = new HashMap<>();
                            zoneProperties.put(AutomowerBindingConstants.AUTOMOWER_ID, mower.getId());
                            zoneProperties.put(AutomowerBindingConstants.AUTOMOWER_STAYOUTZONE_ID, stayOutZone.getId());

                            DiscoveryResult stayoutZoneDiscoveryResult = DiscoveryResultBuilder.create(zoneThingUid)
                                    .withThingType(zoneThingTypeUID).withProperties(zoneProperties)
                                    .withBridge(bridgeUID)
                                    .withRepresentationProperty(AutomowerBindingConstants.AUTOMOWER_STAYOUTZONE_ID)
                                    .withLabel(mower.getAttributes().getSystem().getName() + " - StayoutZone "
                                            + stayOutZone.getName())
                                    .build();

                            thingDiscovered(stayoutZoneDiscoveryResult);
                        }
                    }
                }

                for (WorkArea workArea : mower.getAttributes().getWorkAreas()) {
                    ThingTypeUID areaThingTypeUID = THING_TYPE_WORKAREA;
                    ThingUID areaThingUid = new ThingUID(THING_TYPE_WORKAREA, bridgeUID,
                            mower.getId() + "-" + String.valueOf(workArea.getWorkAreaId()));

                    Map<String, Object> areaProperties = new HashMap<>();
                    areaProperties.put(AutomowerBindingConstants.AUTOMOWER_ID, mower.getId());
                    areaProperties.put(AutomowerBindingConstants.AUTOMOWER_WORKAREA_ID, workArea.getWorkAreaId());

                    String areaName;
                    if (workArea.getWorkAreaId() == 0L && workArea.getName().isBlank()) {
                        areaName = "main area";
                    } else {
                        areaName = workArea.getName();
                    }
                    DiscoveryResult areaDiscoveryResult = DiscoveryResultBuilder.create(areaThingUid)
                            .withThingType(areaThingTypeUID).withProperties(areaProperties).withBridge(bridgeUID)
                            .withRepresentationProperty(AutomowerBindingConstants.AUTOMOWER_WORKAREA_ID)
                            .withLabel(mower.getAttributes().getSystem().getName() + " - WorkArea " + areaName).build();

                    thingDiscovered(areaDiscoveryResult);
                }
            }
        });
    }
}
