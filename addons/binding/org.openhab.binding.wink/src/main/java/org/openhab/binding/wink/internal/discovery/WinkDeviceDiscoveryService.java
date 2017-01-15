/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.internal.discovery;

import static org.openhab.binding.wink.WinkBindingConstants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.wink.handler.WinkHub2Handler;
import org.openhab.binding.wink.handler.WinkHub2Handler.RequestCallback;
import org.openhab.binding.wink.internal.WinkHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WinkDeviceDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(WinkDeviceDiscoveryService.class);
    private WinkHub2Handler hubHandler;

    public WinkDeviceDiscoveryService(WinkHub2Handler hubHandler) throws IllegalArgumentException {
        super(WinkHandlerFactory.DISCOVERABLE_DEVICE_TYPES_UIDS, 10);

        this.hubHandler = hubHandler;
    }

    private ScheduledFuture<?> scanTask;

    @Override
    protected void startScan() {
        if (this.scanTask == null || this.scanTask.isDone()) {
            this.scanTask = scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        readDeviceDatabase();
                    } catch (Exception e) {
                        logger.error("Error scanning for devices", e);

                        if (scanListener != null) {
                            scanListener.onErrorOccurred(e);
                        }
                    }
                }
            }, 0, TimeUnit.SECONDS);
        }
    }

    protected void addWinkDevice(ThingTypeUID thinkType, JsonObject deviceDescription, String thingIdField) {
        String uuid = deviceDescription.get("uuid").toString().replaceAll("\"", "");
        String deviceName = deviceDescription.get("name").toString();
        ThingUID hubUID = this.hubHandler.getThing().getUID();
        ThingUID uid = new ThingUID(thinkType, hubUID, uuid);

        Map<String, Object> properties = new HashMap<>();
        properties.put(WINK_DEVICE_ID, deviceDescription.get(thingIdField).toString().replaceAll("\"", ""));

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(deviceName).withProperties(properties)
                .withBridge(hubUID).build();

        thingDiscovered(result);

        logger.info("Discovered {}", uid);
    }

    protected void enumerateDevices(JsonObject hubResponse) {
        JsonElement data_blob = hubResponse.get("data");
        if (data_blob == null) {
            logger.error("Empty data blob");
            return;
        }
        Iterator<JsonElement> data_blob_iter = data_blob.getAsJsonArray().iterator();
        while (data_blob_iter.hasNext()) {
            JsonElement element = data_blob_iter.next();
            if (!element.isJsonObject()) {
                continue;
            }
            if (element.getAsJsonObject().get("light_bulb_id") != null) {
                addWinkDevice(THING_TYPE_LIGHT_BULB, element.getAsJsonObject(), "light_bulb_id");
            }
        }
    }

    private class listDevicesCallback implements RequestCallback {
        private WinkDeviceDiscoveryService discoveryService;

        public listDevicesCallback(WinkDeviceDiscoveryService discoveryService) {
            this.discoveryService = discoveryService;
        }

        @Override
        public void parseRequestResult(String jsonResult) {
            JsonParser parser = new JsonParser();
            discoveryService.enumerateDevices(parser.parse(jsonResult).getAsJsonObject());
        }
    }

    private void readDeviceDatabase() throws IOException {
        try {
            hubHandler.getConfigFromServer("users/me/wink_devices", new listDevicesCallback(this));
        } catch (IOException e) {
            logger.error("Error while querying the hub for the devices.", e);
        }
    }

}
