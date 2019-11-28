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

package org.openhab.binding.touchwand.internal.discovery;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.touchwand.internal.TouchWandBridgeHandler;
import org.openhab.binding.touchwand.internal.data.TouchWandShutterSwitchUnitData;
import org.openhab.binding.touchwand.internal.data.TouchWandUnitData;
import org.openhab.binding.touchwand.internal.data.TouchWandUnitDataWallController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link TouchWandUnitDiscoveryService} Discovery service for units.
 *
 * @author Roie Geron - Initial contribution
 */
public class TouchWandUnitDiscoveryService extends AbstractDiscoveryService {

    private static final int SEARCH_TIME = 10;
    private static final int SCAN_INTERVAL = 60;
    private static final int LINK_DISCOVERY_SERVICE_INITIAL_DELAY = 5;
    private static final String[] switchOptions = { "zwave", "knx" };

    private ScheduledFuture<?> scanningJob;

    private final TouchWandUnitScan scanningRunnable;

    private final Logger logger = LoggerFactory.getLogger(TouchWandUnitDiscoveryService.class);

    private final TouchWandBridgeHandler touchWandBridgeHandler;

    public TouchWandUnitDiscoveryService(TouchWandBridgeHandler touchWandBridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME, true);
        this.touchWandBridgeHandler = touchWandBridgeHandler;
        removeOlderResults(getTimestampOfLastScan(), touchWandBridgeHandler.getThing().getUID());
        this.scanningRunnable = new TouchWandUnitScan();
        this.activate();
    }

    @Override
    protected void startScan() {
        if (touchWandBridgeHandler.touchWandClient == null) {
            logger.warn("Could not scan units without bridge handler {}");
            return;
        }

        if (touchWandBridgeHandler.getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Could not scan units while bridge offline");
            return;
        }

        logger.debug("Starting TouchWand discovery on bridge {}", touchWandBridgeHandler.getThing().getUID());
        String response = touchWandBridgeHandler.touchWandClient.cmdListUnits();
        if (response == null) {
            return;
        }

        logger.debug("Recieved list units respose {}", response);
        JsonParser jsonParser = new JsonParser();
        try {
            JsonArray jsonArray = jsonParser.parse(response).getAsJsonArray();
            if (jsonArray.isJsonArray()) {
                try {
                    for (JsonElement unit : jsonArray) {
                        Gson gson = new Gson();
                        JsonObject unitObj = unit.getAsJsonObject();
                        TouchWandUnitData touchWandUnit;
                        String type = unitObj.get("type").getAsString();
                        if (type.equals("WallController")) {
                            touchWandUnit = gson.fromJson(unitObj, TouchWandUnitDataWallController.class);
                        } else {
                            touchWandUnit = gson.fromJson(unitObj, TouchWandShutterSwitchUnitData.class);
                        }

                        if (!touchWandBridgeHandler.isAddSecondaryControllerUnits()) {
                            if (!Arrays.asList(switchOptions).contains(touchWandUnit.getConnectivity())) {
                                logger.debug("Skipped secondary controller unit id {} name {}", touchWandUnit.getId(),
                                        touchWandUnit.getName());
                                continue;
                            }
                        }
                        if (touchWandUnit.getType().equals("Switch")) {
                            addDeviceDiscoveryResult(touchWandUnit, THING_TYPE_SWITCH);
                        } else if (touchWandUnit.getType().equals("shutter")) {
                            addDeviceDiscoveryResult(touchWandUnit, THING_TYPE_SHUTTER);
                        }
                        logger.debug("id is {} name {} type {} connectivity {}", touchWandUnit.getId(),
                                touchWandUnit.getName(), touchWandUnit.getType(), touchWandUnit.getConnectivity());
                    }
                } catch (JsonSyntaxException e) {
                    logger.warn("Could not parse unit {}", e.getMessage());
                }
            }
        } catch (JsonSyntaxException msg) {
            logger.warn("Could not parse list units response {}", msg);
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    public void activate() {
        super.activate(null);
        removeOlderResults(new Date().getTime(), touchWandBridgeHandler.getThing().getUID());
        logger.debug("activate discovery service");
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime(), touchWandBridgeHandler.getThing().getUID());
        super.deactivate();
        logger.debug("deactivate discovery services");
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start TouchWand units background discovery");
        if (scanningJob == null || scanningJob.isCancelled()) {
            scanningJob = scheduler.scheduleWithFixedDelay(scanningRunnable, LINK_DISCOVERY_SERVICE_INITIAL_DELAY,
                    SCAN_INTERVAL, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop TouchWand device units discovery");
        if (scanningJob != null && !scanningJob.isCancelled()) {
            scanningJob.cancel(true);
            scanningJob = null;
        }
    }

    @NonNullByDefault
    public class TouchWandUnitScan implements Runnable {
        @Override
        public void run() {
            startScan();
        }
    }

    @Override
    public int getScanTimeout() {
        return SEARCH_TIME;
    }

    private void addDeviceDiscoveryResult(TouchWandUnitData unit, ThingTypeUID typeUID) {
        ThingUID bridgeUID = touchWandBridgeHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(typeUID, bridgeUID, unit.getId().toString());
        Map<String, Object> properties = new HashMap<>();
        properties.put("id", unit.getId());
        properties.put("name", unit.getName());
        // @formatter:off
        thingDiscovered(DiscoveryResultBuilder.create(thingUID)
                .withThingType(typeUID)
                .withLabel(unit.getName())
                .withBridge(bridgeUID)
                .withProperties(properties)
                .withRepresentationProperty(unit.getId().toString())
                .build()
        );
        // @formatter:on
    }

}
