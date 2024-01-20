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
package org.openhab.binding.growatt.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.growatt.internal.cloud.GrowattCloud;
import org.openhab.binding.growatt.internal.config.GrowattBridgeConfiguration;
import org.openhab.binding.growatt.internal.discovery.GrowattDiscoveryService;
import org.openhab.binding.growatt.internal.dto.GrottDevice;
import org.openhab.binding.growatt.internal.dto.helper.GrottIntegerDeserializer;
import org.openhab.binding.growatt.internal.servlet.GrowattHttpServlet;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link GrowattBridgeHandler} is a bridge handler for accessing Growatt inverters via the Grott application.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrowattBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(GrowattBridgeHandler.class);
    private final Gson gson = new GsonBuilder().registerTypeAdapter(Integer.class, new GrottIntegerDeserializer())
            .create();
    private final GrowattDiscoveryService discoveryService;
    private final Map<String, GrottDevice> inverters = new HashMap<>();
    private final GrowattHttpServlet httpServlet;
    private final HttpClientFactory httpClientFactory;

    private @Nullable GrowattCloud growattCloud;

    public GrowattBridgeHandler(Bridge bridge, GrowattHttpServlet httpServlet, GrowattDiscoveryService discoveryService,
            HttpClientFactory httpClientFactory) {
        super(bridge);
        this.httpServlet = httpServlet;
        this.discoveryService = discoveryService;
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public void dispose() {
        inverters.clear();
        httpServlet.handlerRemove(this);
        discoveryService.putInverters(thing.getUID(), inverters.keySet());
    }

    public GrowattCloud getGrowattCloud() throws IllegalStateException {
        GrowattCloud growattCloud = this.growattCloud;
        if (growattCloud == null) {
            try {
                growattCloud = new GrowattCloud(getConfigAs(GrowattBridgeConfiguration.class), httpClientFactory);
            } catch (Exception e) {
                throw new IllegalStateException("GrowattCloud not created", e);
            }
            this.growattCloud = growattCloud;
        }
        return growattCloud;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // everything is read only so do nothing
    }

    /**
     * Process JSON content posted to the Grott application servlet.
     */
    @SuppressWarnings("null")
    public void handleGrottContent(String json) {
        logger.trace("handleGrottContent() json:{}", json);
        JsonElement jsonElement;
        try {
            jsonElement = JsonParser.parseString(json);
            if (jsonElement.isJsonPrimitive()) {
                // strip double escaping from Grott JSON
                jsonElement = JsonParser.parseString(jsonElement.getAsString());
            }
            if (!jsonElement.isJsonObject()) {
                throw new JsonSyntaxException("Unsupported JSON element type");
            }
        } catch (JsonSyntaxException e) {
            logger.debug("handleGrottContent() invalid JSON '{}'", json, e);
            return;
        }
        try {
            GrottDevice inverter = gson.fromJson(jsonElement, GrottDevice.class);
            if (inverter == null) {
                throw new JsonSyntaxException("Inverter object is null");
            }
            putInverter(inverter);
        } catch (JsonSyntaxException e) {
            logger.debug("handleGrottContent() error parsing JSON '{}'", json, e);
            return;
        }
        getThing().getThings().stream().map(thing -> thing.getHandler())
                .filter(handler -> (handler instanceof GrowattInverterHandler))
                .forEach(handler -> ((GrowattInverterHandler) handler).updateInverters(inverters.values()));
    }

    @Override
    public void initialize() {
        httpServlet.handlerAdd(this);
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Put the given GrottDevice in our inverters map, and notify the discovery service if it was not already there.
     *
     * @param inverter a GrottDevice inverter object.
     */
    private void putInverter(GrottDevice inverter) {
        if (inverters.put(inverter.getDeviceId(), inverter) == null) {
            discoveryService.putInverters(thing.getUID(), inverters.keySet());
        }
    }
}
