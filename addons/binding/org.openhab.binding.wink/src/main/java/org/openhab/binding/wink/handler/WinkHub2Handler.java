/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.handler;

import static org.openhab.binding.wink.WinkBindingConstants.WINK_URI;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.wink.config.WinkHub2Config;
import org.openhab.binding.wink.internal.discovery.WinkDeviceDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * TODO: The {@link WinkHub2Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sebastien Marchand - Initial contribution
 */
public class WinkHub2Handler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(WinkHub2Handler.class);

    public WinkHub2Handler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.error("The hub doesn't support any command!");
    }

    @Override
    public void initialize() {
        this.config = getThing().getConfiguration().as(WinkHub2Config.class);
        if (validConfiguration()) {
            WinkDeviceDiscoveryService discovery = new WinkDeviceDiscoveryService(this);

            this.bundleContext.registerService(DiscoveryService.class, discovery, null);

            this.scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    // TODO
                    // connect();
                }
            }, 0, TimeUnit.SECONDS);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    private boolean validConfiguration() {
        if (this.config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hub configuration missing");
            return false;
        } else if (StringUtils.isEmpty(this.config.access_token)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "access_token not specified");
            return false;
        } else if (StringUtils.isEmpty(this.config.refresh_token)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "refresh_token not specified");
            return false;
        }
        return true;
    }

    public WinkHub2Config getHubConfig() {
        return this.config;
    }

    private WinkHub2Config config;

    // REST API variables
    protected Client winkClient = ClientBuilder.newClient();
    protected WebTarget winkTarget = winkClient.target(WINK_URI);

    public interface RequestCallback {
        public void parseRequestResult(JsonObject resultJson);

        public void OnError(String error);
    }

    protected class Request implements Runnable {
        private WebTarget target;
        private RequestCallback callback;
        private String payLoad;

        public Request(String targetPath, RequestCallback callback, String payLoad) {
            this.target = winkTarget.path(targetPath);
            this.callback = callback;
            this.payLoad = payLoad;
        }

        protected String checkForFailure(JsonObject jsonResult) {
            if (jsonResult.get("data").isJsonNull()) {
                return jsonResult.get("errors").getAsString();
            }
            return null;
        }

        @Override
        public void run() {
            try {
                String result = invokeAndParse(this.target, this.payLoad);
                logger.trace("Hub replied with: {}", result);
                JsonParser parser = new JsonParser();
                JsonObject resultJson = parser.parse(result).getAsJsonObject();

                String errors = checkForFailure(resultJson);
                if (errors != null) {

                }
                callback.parseRequestResult(resultJson);
            } catch (Exception e) {
                logger.error("An exception occurred while executing a request to the hub: '{}'", e.getMessage());
            }
        }
    }

    protected String invokeAndParse(WebTarget target, String payLoad) {
        if (this.config != null) {
            Response response;

            logger.trace("Requesting the hub for: {}", target.toString());
            if (payLoad != null) {
                logger.trace("Request payload: {}", payLoad.toString());
                response = target.request(MediaType.APPLICATION_JSON_TYPE)
                        .header("Authorization", "Bearer " + this.config.access_token).put(Entity.json(payLoad));
            } else {
                response = target.request(MediaType.APPLICATION_JSON_TYPE)
                        .header("Authorization", "Bearer " + this.config.access_token).get();
            }
            return response.readEntity(String.class);
        }
        return null;
    }

    public void sendRequestToServer(String deviceRequestPath, RequestCallback callback) throws IOException {
        sendRequestToServer(deviceRequestPath, callback, null);
    }

    public void sendRequestToServer(String deviceRequestPath, RequestCallback callback, String payLoad)
            throws IOException {
        Request request = new Request(deviceRequestPath, callback, payLoad);
        request.run();
    }
}
