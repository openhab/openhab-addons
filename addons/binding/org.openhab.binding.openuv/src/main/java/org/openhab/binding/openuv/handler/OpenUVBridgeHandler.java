/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openuv.handler;

import static org.openhab.binding.openuv.OpenUVBindingConstants.BASE_URL;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.openuv.OpenUVBindingConstants;
import org.openhab.binding.openuv.json.OpenUVJsonResponse;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

/**
 * {@link OpenUVBridgeHandler} is the handler for OpenUV API and connects it
 * to the webservice.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class OpenUVBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(OpenUVBridgeHandler.class);

    private final Gson gson;
    private @Nullable String apikey;
    private Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private String error = "";
    private @Nullable String statusDescr;

    public OpenUVBridgeHandler(Bridge bridge) {
        super(bridge);
        gson = new GsonBuilder()
                .registerTypeAdapter(DecimalType.class,
                        (JsonDeserializer<DecimalType>) (json, type, jsonDeserializationContext) -> DecimalType
                                .valueOf(json.getAsJsonPrimitive().getAsString()))
                .registerTypeAdapter(ZonedDateTime.class,
                        (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime
                                .parse(json.getAsJsonPrimitive().getAsString()))
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    @Override
    public void initialize() {
        boolean validConfig = false;

        logger.debug("Initializing OpenUV API bridge handler.");
        Configuration config = getThing().getConfiguration();

        OpenUVJsonResponse result = null;
        String errorDetail = null;

        // Check if an api key has been provided during the bridge creation
        if (StringUtils.trimToNull((String) config.get(OpenUVBindingConstants.APIKEY)) == null) {
            error += " Parameter 'apikey' must be configured.";
            statusDescr = "Missing API key";
        } else {
            setApikey((String) config.get(OpenUVBindingConstants.APIKEY));
            // Check if the provided api key is valid for use with the OpenUV service
            try {
                // Run the HTTP request and get the JSON response
                String response = null;

                try {
                    Properties header = new Properties();
                    header.put("x-access-token", getApikey());

                    response = HttpUtil.executeUrl("GET", BASE_URL + "?lat=0&lng=0", header, null, null, 2000);
                    logger.debug("apiResponse = {}", response);
                    // Map the JSON response to an object
                    result = gson.fromJson(response, OpenUVJsonResponse.class);
                    if ((result.getError() != null) && (result.getError().equals("Invalid API Key"))) {
                        error = "API key has to be fixed";
                        errorDetail = result.getError();
                        statusDescr = "Error : Invalid Key";
                    } else {
                        validConfig = true;
                    }

                } catch (IllegalArgumentException e) {
                    errorDetail = e.getMessage();
                    statusDescr = "@text/offline.uri-error";
                }
            } catch (IOException e) {
                error = "Error running OpenUV API request";
                errorDetail = e.getMessage();
                statusDescr = "@text/offline.comm-error-running-request";
            }
        }

        // Updates the thing status accordingly
        if (validConfig) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            error = error.trim();
            logger.debug("Disabling thing '{}': Error '{}': {}", getThing().getUID(), error, errorDetail);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, statusDescr);
        }
    }

    public @Nullable String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public Gson getGson() {
        return this.gson;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // not needed
    }

    public Map<ThingUID, @Nullable ServiceRegistration<?>> getDiscoveryServiceRegs() {
        return discoveryServiceRegs;
    }

    public void setDiscoveryServiceRegs(Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs) {
        this.discoveryServiceRegs = discoveryServiceRegs;
    }

    @Override
    public void handleRemoval() {
        // removes the old registration service associated to the bridge, if existing
        ServiceRegistration<?> dis = this.getDiscoveryServiceRegs().get(this.getThing().getUID());
        if (null != dis) {
            dis.unregister();
        }
        super.handleRemoval();
    }

}
