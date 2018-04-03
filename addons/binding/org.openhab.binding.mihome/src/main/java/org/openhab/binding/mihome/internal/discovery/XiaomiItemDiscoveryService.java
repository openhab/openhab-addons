/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.internal.discovery;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.ITEM_ID;
import static org.openhab.binding.mihome.internal.ModelMapper.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mihome.handler.XiaomiBridgeHandler;
import org.openhab.binding.mihome.internal.XiaomiItemUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Discovery service for items/sensors.
 *
 * @author Patrick Boos - Initial contribution
 */
public class XiaomiItemDiscoveryService extends AbstractDiscoveryService
        implements XiaomiItemUpdateListener, ExtendedDiscoveryService {

    private static final int DISCOVERY_TIMEOUT_SEC = 10;
    private final XiaomiBridgeHandler xiaomiBridgeHandler;
    private DiscoveryServiceCallback discoveryServiceCallback;

    private final Logger logger = LoggerFactory.getLogger(XiaomiItemDiscoveryService.class);

    public XiaomiItemDiscoveryService(XiaomiBridgeHandler xiaomiBridgeHandler) {
        super(DISCOVERY_TIMEOUT_SEC);
        this.xiaomiBridgeHandler = xiaomiBridgeHandler;
        xiaomiBridgeHandler.registerItemListener(this);
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    @Override
    public void startScan() {
        logger.debug("Start scan for items");
        xiaomiBridgeHandler.registerItemListener(this); // this will as well get us all items
        xiaomiBridgeHandler.discoverItems();
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
            if (thingType == null) {
                logger.debug("Unknown discovered model: {}", model);
                return;
            }

            Map<String, Object> properties = new HashMap<>(1);
            properties.put(ITEM_ID, sid);

            ThingUID thingUID = new ThingUID(thingType, sid);

            if (discoveryServiceCallback.getExistingThing(thingUID) == null) {
                logger.debug("Detected Xiaomi smart device - sid: {} model: {}", sid, model);
                thingDiscovered(
                        DiscoveryResultBuilder.create(thingUID).withThingType(thingType).withProperties(properties)
                                .withRepresentationProperty(ITEM_ID).withLabel(getLabelForModel(model))
                                .withBridge(xiaomiBridgeHandler.getThing().getUID()).build());
            }
        }
    }

    @Override
    public String getItemId() {
        // The discovery service is not bound to a specific device
        return null;
    }
}
