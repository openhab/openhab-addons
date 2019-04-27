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
package org.openhab.binding.openuv.internal.handler;

import static org.openhab.binding.openuv.internal.OpenUVBindingConstants.BASE_URL;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.openuv.internal.OpenUVBindingConstants;
import org.openhab.binding.openuv.internal.json.OpenUVResponse;
import org.openhab.binding.openuv.internal.json.OpenUVResult;
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
    private static final String ERROR_QUOTA_EXCEEDED = "Daily API quota exceeded";
    private static final String ERROR_NO_API_KEY = "No API Key provided";
    private static final String ERROR_WRONG_KEY = "User with API Key not found";

    private static final int REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(30);

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(DecimalType.class,
                    (JsonDeserializer<DecimalType>) (json, type, jsonDeserializationContext) -> DecimalType
                            .valueOf(json.getAsJsonPrimitive().getAsString()))
            .registerTypeAdapter(ZonedDateTime.class,
                    (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime
                            .parse(json.getAsJsonPrimitive().getAsString()))
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final Properties header = new Properties();

    public OpenUVBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing OpenUV API bridge handler.");
        Configuration config = getThing().getConfiguration();
        String apiKey = (String) config.get(OpenUVBindingConstants.APIKEY);
        if (StringUtils.trimToNull(apiKey) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Parameter 'apikey' must be configured.");
        } else {
            header.put("x-access-token", apiKey);
            initiateConnexion();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            initiateConnexion();
        } else {
            logger.debug("The OpenUV bridge only handles Refresh command and not '{}'", command);
        }
    }

    private void initiateConnexion() {
        // Check if the provided api key is valid for use with the OpenUV service
        getUVData("0", "0", null);
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
        ServiceRegistration<?> dis = getDiscoveryServiceRegs().get(getThing().getUID());
        if (dis != null) {
            dis.unregister();
        }
        super.handleRemoval();
    }

    public @Nullable OpenUVResult getUVData(String latitude, String longitude, @Nullable String altitude) {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL).append("?lat=").append(latitude).append("&lng=")
                .append(longitude);

        if (altitude != null) {
            urlBuilder.append("&alt=").append(altitude);
        }
        String errorMessage = null;
        try {
            String jsonData = HttpUtil.executeUrl("GET", urlBuilder.toString(), header, null, null, REQUEST_TIMEOUT);
            OpenUVResponse uvResponse = gson.fromJson(jsonData, OpenUVResponse.class);
            if (uvResponse.getError() == null) {
                updateStatus(ThingStatus.ONLINE);
                return uvResponse.getResult();
            } else {
                errorMessage = uvResponse.getError();
            }
        } catch (IOException e) {
            errorMessage = e.getMessage();
        }

        if (errorMessage.startsWith(ERROR_QUOTA_EXCEEDED)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);
            LocalDateTime tomorrowMidnight = tomorrow.atStartOfDay().plusMinutes(2);

            logger.warn("Quota Exceeded, going OFFLINE for today, will retry at : {} ", tomorrowMidnight);
            scheduler.schedule(this::initiateConnexion,
                    Duration.between(LocalDateTime.now(), tomorrowMidnight).toMinutes(), TimeUnit.MINUTES);

        } else if (errorMessage.startsWith(ERROR_WRONG_KEY)) {
            logger.error("Error occured during API query : {}", errorMessage);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMessage);
        }
        return null;
    }
}
