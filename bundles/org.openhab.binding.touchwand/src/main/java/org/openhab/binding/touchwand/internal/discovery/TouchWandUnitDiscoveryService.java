/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.touchwand.internal.discovery;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.touchwand.internal.TouchWandBridgeHandler;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitData;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitFromJson;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link TouchWandUnitDiscoveryService} Discovery service for TouchWand units.
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public class TouchWandUnitDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private static final int SEARCH_TIME_SEC = 10;
    private static final int SCAN_INTERVAL_SEC = 60;
    private static final int LINK_DISCOVERY_SERVICE_INITIAL_DELAY_SEC = 5;
    private static final String[] CONNECTIVITY_OPTIONS = { CONNECTIVITY_KNX, CONNECTIVITY_ZWAVE, CONNECTIVITY_RISCO,
            CONNECTIVITY_PIMA, CONNECTIVITY_ACWAND };
    private @NonNullByDefault({}) TouchWandBridgeHandler touchWandBridgeHandler;
    private final Logger logger = LoggerFactory.getLogger(TouchWandUnitDiscoveryService.class);

    private @Nullable ScheduledFuture<?> scanningJob;

    public TouchWandUnitDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME_SEC, true);
    }

    @Override
    protected void startScan() {
        if (touchWandBridgeHandler.getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Could not scan units while bridge offline");
            return;
        }

        logger.debug("Starting TouchWand discovery on bridge {}", touchWandBridgeHandler.getThing().getUID());
        String response = touchWandBridgeHandler.touchWandClient.cmdListUnits();
        if (response.isEmpty()) {
            return;
        }

        try {
            JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();
            if (jsonArray.isJsonArray()) {
                try {
                    for (JsonElement unit : jsonArray) {
                        TouchWandUnitData touchWandUnit;
                        touchWandUnit = TouchWandUnitFromJson.parseResponse(unit.getAsJsonObject());

                        if (!touchWandBridgeHandler.isAddSecondaryControllerUnits()) {
                            if (!Arrays.asList(CONNECTIVITY_OPTIONS).contains(touchWandUnit.getConnectivity())) {
                                continue;
                            }
                        }
                        String type = touchWandUnit.getType();
                        if (!Arrays.asList(SUPPORTED_TOUCHWAND_TYPES).contains(type)) {
                            logger.debug("Unit discovery skipping unsupported unit type : {} ", type);
                            continue;
                        }
                        switch (type) {
                            case TYPE_WALLCONTROLLER:
                                addDeviceDiscoveryResult(touchWandUnit, THING_TYPE_WALLCONTROLLER);
                                break;
                            case TYPE_SWITCH:
                                addDeviceDiscoveryResult(touchWandUnit, THING_TYPE_SWITCH);
                                break;
                            case TYPE_DIMMER:
                                addDeviceDiscoveryResult(touchWandUnit, THING_TYPE_DIMMER);
                                break;
                            case TYPE_SHUTTER:
                                addDeviceDiscoveryResult(touchWandUnit, THING_TYPE_SHUTTER);
                                break;
                            case TYPE_ALARMSENSOR:
                                addDeviceDiscoveryResult(touchWandUnit, THING_TYPE_ALARMSENSOR);
                                break;
                            case TYPE_BSENSOR:
                                addDeviceDiscoveryResult(touchWandUnit, THING_TYPE_BSENSOR);
                                break;
                            case TYPE_THERMOSTAT:
                                addDeviceDiscoveryResult(touchWandUnit, THING_TYPE_THERMOSTAT);
                                break;
                            default:
                                continue;
                        }
                    }
                } catch (JsonSyntaxException e) {
                    logger.warn("Could not parse unit {}", e.getMessage());
                }
            }
        } catch (JsonSyntaxException msg) {
            logger.warn("Could not parse list units response error:{} ", msg.getMessage());
        }
    }

    @Override
    protected void stopScan() {
        removeOlderResults(getTimestampOfLastScan());
        super.stopScan();
    }

    @Override
    public void activate() {
        super.activate(null);
        removeOlderResults(new Date().getTime(), touchWandBridgeHandler.getThing().getUID());
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime(), touchWandBridgeHandler.getThing().getUID());
        super.deactivate();
    }

    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> localScanningJob = scanningJob;
        if (localScanningJob == null || localScanningJob.isCancelled()) {
            scanningJob = scheduler.scheduleWithFixedDelay(this::startScan, LINK_DISCOVERY_SERVICE_INITIAL_DELAY_SEC,
                    SCAN_INTERVAL_SEC, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> myScanningJob = scanningJob;
        if (myScanningJob != null) {
            myScanningJob.cancel(true);
            scanningJob = null;
        }
    }

    @Override
    public int getScanTimeout() {
        return SEARCH_TIME_SEC;
    }

    private void addDeviceDiscoveryResult(TouchWandUnitData unit, ThingTypeUID typeUID) {
        ThingUID bridgeUID = touchWandBridgeHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(typeUID, bridgeUID, unit.getId().toString());
        Map<String, Object> properties = new HashMap<>();
        properties.put(HANDLER_PROPERTIES_ID, unit.getId().toString());
        properties.put(HANDLER_PROPERTIES_NAME, unit.getName());
        // @formatter:off
        thingDiscovered(DiscoveryResultBuilder.create(thingUID)
                .withThingType(typeUID)
                .withLabel(unit.getName())
                .withBridge(bridgeUID)
                .withProperties(properties)
                .withRepresentationProperty(HANDLER_PROPERTIES_ID)
                .build()
        );
        // @formatter:on
    }

    @Override
    public void setThingHandler(@NonNullByDefault({}) ThingHandler handler) {
        if (handler instanceof TouchWandBridgeHandler) {
            touchWandBridgeHandler = (TouchWandBridgeHandler) handler;
        }
    }

    @Override
    public @NonNull ThingHandler getThingHandler() {
        return touchWandBridgeHandler;
    }
}
