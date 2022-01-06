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
package org.openhab.binding.mihome.internal.discovery;

import static org.openhab.binding.mihome.internal.ModelMapper.*;
import static org.openhab.binding.mihome.internal.XiaomiGatewayBindingConstants.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.mihome.internal.XiaomiItemUpdateListener;
import org.openhab.binding.mihome.internal.handler.XiaomiBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Discovery service for items/sensors.
 *
 * @author Patrick Boos - Initial contribution
 */
public class XiaomiItemDiscoveryService extends AbstractDiscoveryService implements XiaomiItemUpdateListener {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_GATEWAY,
            THING_TYPE_SENSOR_HT, THING_TYPE_SENSOR_AQARA_WEATHER_V1, THING_TYPE_SENSOR_MOTION,
            THING_TYPE_SENSOR_AQARA_MOTION, THING_TYPE_SENSOR_SWITCH, THING_TYPE_SENSOR_AQARA_SWITCH,
            THING_TYPE_SENSOR_MAGNET, THING_TYPE_SENSOR_AQARA_LOCK, THING_TYPE_SENSOR_AQARA_MAGNET,
            THING_TYPE_SENSOR_CUBE, THING_TYPE_SENSOR_AQARA_VIBRATION, THING_TYPE_SENSOR_AQARA1,
            THING_TYPE_SENSOR_AQARA2, THING_TYPE_SENSOR_GAS, THING_TYPE_SENSOR_SMOKE, THING_TYPE_SENSOR_WATER,
            THING_TYPE_ACTOR_AQARA1, THING_TYPE_ACTOR_AQARA2, THING_TYPE_ACTOR_PLUG, THING_TYPE_ACTOR_AQARA_ZERO1,
            THING_TYPE_ACTOR_AQARA_ZERO2, THING_TYPE_ACTOR_CURTAIN, THING_TYPE_BASIC));

    private static final int DISCOVERY_TIMEOUT_SEC = 30;
    private final XiaomiBridgeHandler xiaomiBridgeHandler;

    private final Logger logger = LoggerFactory.getLogger(XiaomiItemDiscoveryService.class);

    public XiaomiItemDiscoveryService(XiaomiBridgeHandler xiaomiBridgeHandler) {
        super(SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT_SEC, true);
        this.xiaomiBridgeHandler = xiaomiBridgeHandler;
        xiaomiBridgeHandler.registerItemListener(this);
    }

    @Override
    public void startScan() {
        logger.debug("Start scan for items");
        xiaomiBridgeHandler.registerItemListener(this); // this will as well get us all items
        xiaomiBridgeHandler.discoverItems(TimeUnit.SECONDS.toMillis(DISCOVERY_TIMEOUT_SEC));
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void deactivate() {
        super.deactivate();
        xiaomiBridgeHandler.unregisterItemListener(this);
    }

    @Override
    public int getScanTimeout() {
        return DISCOVERY_TIMEOUT_SEC;
    }

    public void onHandlerRemoved() {
        removeOlderResults(new Date().getTime());
    }

    @Override
    public void onItemUpdate(String sid, String command, JsonObject data) {
        if (command.equals("read_ack") || command.equals("report") || command.equals("heartbeat")) {
            String model = data.get("model").getAsString();

            ThingTypeUID thingType = getThingTypeForModel(model);
            String modelLabel = getLabelForModel(model);
            if (thingType == null) {
                logger.warn("Discovered unsupported device with id \"{}\" -> Creating Basic Device Thing", model);
                thingType = THING_TYPE_BASIC;
                modelLabel = String.format("Unsupported Xiaomi MiHome Device \"%s\"", model);
            }

            Map<String, Object> properties = new HashMap<>(1);
            properties.put(ITEM_ID, sid);

            ThingUID bridgeUID = xiaomiBridgeHandler.getThing().getUID();
            ThingUID thingUID = new ThingUID(thingType, bridgeUID, sid);

            logger.debug("Discovered device - sid: {} model: {}", sid, model);
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(thingType).withProperties(properties)
                    .withRepresentationProperty(ITEM_ID).withLabel(modelLabel).withBridge(bridgeUID).build());
        }
    }

    @Override
    public String getItemId() {
        // The discovery service is not bound to a specific device
        return null;
    }
}
