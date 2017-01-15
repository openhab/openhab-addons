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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.wink.handler.WinkHub2Handler;
import org.openhab.binding.wink.internal.WinkHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WinkDeviceDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(WinkDeviceDiscoveryService.class);
    private WinkHub2Handler hubHandler;

    // REST API variables
    protected Client winkClient = ClientBuilder.newClient();
    protected WebTarget winkTarget = winkClient.target(WINK_URI);
    protected WebTarget winkDevicesTarget = winkTarget.path(WINK_DEVICES_REQUEST_PATH);

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

    protected class Request implements Runnable {

        private String request;
        private String payLoad;
        private WebTarget target;

        public Request(String request, String payLoad, WebTarget target) {
            this.request = request;
            this.payLoad = payLoad;
            this.target = target;
        }

        @Override
        public void run() {
            try {

                String result = "";

                // if (isAwake() && getThing().getStatus() == ThingStatus.ONLINE) {
                result = invokeAndParse(request, payLoad, target);
                // }

                if (result != null && result != "") {
                    // parseAndUpdate(request, payLoad, result);
                }
            } catch (Exception e) {
                logger.error("An exception occurred while executing a request to the hub: '{}'", e.getMessage());
            }
        }
    }

    protected void addLightBulb(JsonObject lightBulbDescription) {
        String uuid = lightBulbDescription.get("uuid").toString().replaceAll("\"", "");
        String device_name = lightBulbDescription.get("name").toString();
        ThingUID hubUID = this.hubHandler.getThing().getUID();
        ThingUID uid = new ThingUID(THING_TYPE_LIGHT_BULB, hubUID, uuid);

        JsonObject pubnubBlob = lightBulbDescription.get("subscription").getAsJsonObject().get("pubnub")
                .getAsJsonObject();

        Map<String, Object> properties = new HashMap<>();
        properties.put(WINK_DEVICE_ID, lightBulbDescription.get("light_bulb_id").toString().replaceAll("\"", ""));
        properties.put(WINK_PUBNUB_SUBSCRIBE_KEY, pubnubBlob.get("subscribe_key").toString().replaceAll("\"", ""));
        properties.put(WINK_PUBNUB_CHANNEL, pubnubBlob.get("channel").toString().replaceAll("\"", ""));

        logger.info(properties.toString());

        // DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(hubUID).withLabel(device_name)
        // .withProperties(properties).build();

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(device_name).withProperties(properties)
                .withBridge(hubUID).build();

        thingDiscovered(result);

        logger.info("Discovered {}", uid);
    }

    protected void enumerateDevices(String hubReplyJSON) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(hubReplyJSON).getAsJsonObject();
        JsonElement data_blob = jsonObject.get("data");
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
                addLightBulb(element.getAsJsonObject());
            }
        }
    }

    protected String invokeAndParse(String command, String payLoad, WebTarget target) {
        if (this.hubHandler.getHubConfig() != null) {
            Response response;

            if (payLoad != null) {
            } else {
                if (command != null) {
                } else {
                    response = target.request(MediaType.APPLICATION_JSON_TYPE)
                            .header("Authorization", "Bearer " + this.hubHandler.getHubConfig().access_token).get();
                    enumerateDevices(response.readEntity(String.class));
                }
            }

            // JsonParser parser = new JsonParser();

            /*
             * if (response != null && response.getStatus() == 200) {
             * try {
             * JsonObject jsonObject = parser.parse(response.readEntity(String.class)).getAsJsonObject();
             * logger.trace("Request : {}:{}:{} yields {}", new Object[] { command, payLoad, target.toString(),
             * jsonObject.get("response").toString() });
             * return jsonObject.get("response").toString();
             * } catch (Exception e) {
             * logger.error("An exception occurred while invoking a REST request : '{}'", e.getMessage());
             * }
             * } else {
             * logger.error("An error occurred while communicating with the vehicle during request {} : {}:{}",
             * new Object[] { command, (response != null) ? response.getStatus() : "",
             * (response != null) ? response.getStatusInfo() : "No Response" });
             *
             * intervalErrors++;
             * if (intervalErrors >= MAXIMUM_ERRORS_IN_INTERVAL) {
             * logger.warn("Reached the maximum number of errors ({}) for the current interval ({} seconds)",
             * MAXIMUM_ERRORS_IN_INTERVAL, ERROR_INTERVAL_SECONDS);
             * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
             * return null;
             * }
             *
             * if ((System.currentTimeMillis() - intervalTimestamp) > 1000 * ERROR_INTERVAL_SECONDS) {
             * logger.trace("Resetting the error counter. ({} errors in the last interval)", intervalErrors);
             * intervalTimestamp = System.currentTimeMillis();
             * intervalErrors = 0;
             * }
             * }
             */
        }

        return null;
    }

    private void readDeviceDatabase() throws IOException {
        Request request = new Request(null, null, winkDevicesTarget);
        request.run();
    }

}
