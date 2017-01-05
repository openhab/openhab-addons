/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
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
public class XiaomiItemDiscoveryService extends AbstractDiscoveryService implements XiaomiItemUpdateListener {

    private static final int DISCOVERY_TIMEOUT_SEC = 10;
    private final XiaomiBridgeHandler xiaomiBridgeHandler;

    private final Logger logger = LoggerFactory.getLogger(XiaomiItemDiscoveryService.class);

    public XiaomiItemDiscoveryService(XiaomiBridgeHandler xiaomiBridgeHandler) {
        super(DISCOVERY_TIMEOUT_SEC);
        this.xiaomiBridgeHandler = xiaomiBridgeHandler;
    }

    @Override
    protected void startScan() {
        logger.debug("Start scan for items");
        xiaomiBridgeHandler.discoverItems();
        waitUntilEnded();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    public void activate() {
        xiaomiBridgeHandler.registerItemListener(this); // this will as well get us all items
    }

    @Override
    public void deactivate() {
        super.deactivate();
        xiaomiBridgeHandler.unregisterItemListener(this);
    }

    public void onHandlerRemoved() {
        removeOlderResults(new Date().getTime());
    }

    private void waitUntilEnded() {
        final Semaphore discoveryEndedLock = new Semaphore(0);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                discoveryEndedLock.release();
            }
        }, DISCOVERY_TIMEOUT_SEC, TimeUnit.SECONDS);
        try {
            discoveryEndedLock.acquire();
        } catch (InterruptedException e) {
            logger.error("Discovery problem", e);
        }
    }

    @Override
    public void onItemUpdate(String sid, String command, JsonObject data) {
        if (command.equals("read_ack") || command.equals("report") || command.equals("heartbeat")) {
            String model = data.get("model").getAsString();
            logger.debug("Detected Xiaomi smart device - sid: {} model: {}", sid, model);

            ThingTypeUID thingType = getThingTypeForModel(model);
            if (thingType == null) {
                logger.error("Unknown discovered model: {}", model);
                return;
            }

            Map<String, Object> properties = new HashMap<>(1);
            properties.put(ITEM_ID, sid);

            ThingUID thingUID = new ThingUID(thingType, sid);
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(thingType).withProperties(properties)
                    .withRepresentationProperty(ITEM_ID).withLabel(getLabelForModel(model))
                    .withBridge(xiaomiBridgeHandler.getThing().getUID()).build());
        }
    }

    @Override
    public String getItemId() {
        return null;
    }
}
