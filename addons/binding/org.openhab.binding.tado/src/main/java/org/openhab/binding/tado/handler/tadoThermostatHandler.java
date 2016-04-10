/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado.handler;

import static org.openhab.binding.tado.tadoBindingConstants.*;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.tado.internal.protocol.AuthResponse;
import org.openhab.binding.tado.internal.protocol.Home;
import org.openhab.binding.tado.internal.protocol.Zone;
import org.openhab.binding.tado.internal.protocol.ZoneState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

/**
 * The {@link tadoThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ben Woodford - Initial contribution
 */
public class tadoThermostatHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(tadoThermostatHandler.class);

    protected String accessToken;
    protected String refreshToken;
    protected long tokenExpiration = 0;

    protected Client tadoClient = ClientBuilder.newClient();
    protected WebTarget tadoTarget = tadoClient.target(API_URI);
    protected WebTarget authTarget = tadoTarget.path(ACCESS_TOKEN_URI);
    protected WebTarget homesTarget = tadoTarget.path(API_VERSION).path(HOMES);
    protected WebTarget homeTarget = homesTarget.path(HOME_ID_PATH);
    protected WebTarget zonesTarget = homeTarget.path(ZONES);
    protected WebTarget zoneTarget = zonesTarget.path(ZONE_ID_PATH);
    protected WebTarget stateTarget = zoneTarget.path(STATE);

    private JsonParser parser = new JsonParser();
    protected Gson gson = new Gson();

    ScheduledFuture<?> updateJob;

    public tadoThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        /*
         * if (channelUID.getId().equals(CHANNEL_1)) {
         * // TODO: handle command
         *
         * // Note: if communication with thing fails for some reason,
         * // indicate that by setting the status with detail information
         * // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
         * // "Could not control device at IP address x.x.x.x");
         * }
         */
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        connect();

        scheduleTasks();
    }

    protected void scheduleTasks() {
        if (updateJob == null || updateJob.isCancelled()) {
            int refresh = ((BigDecimal) getConfig().get(REFRESH_INTERVAL)).intValue();

            updateJob = scheduler.scheduleAtFixedRate(updateService, 0, refresh, TimeUnit.SECONDS);
        }
    }

    private void connect() {
        logger.trace("Authenticating with Tado Servers");

        ThingStatusDetail authResult = authenticate((String) getConfig().get(EMAIL),
                (String) getConfig().get(PASSWORD));

        if (authResult != ThingStatusDetail.NONE) {
            updateStatus(ThingStatus.OFFLINE, authResult);
            return;
        }

        Home home = getHome();

        if (home != null) {
            logger.trace("Got associated home: {}", home.id);
        } else {
            logger.info("Failed to retrieve Home ID " + getConfig().get(HOME_ID));
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        Zone zone = getZone();

        if (zone != null) {
            logger.trace("Got associated zone: {}", zone.id);
        } else {
            logger.info("Failed to retrieve Zone ID " + getConfig().get(ZONE_ID));
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        ZoneState zoneState = getZoneState();

        if (zoneState != null) {
            logger.trace("Got Zone State: {}", zoneState);
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.info("Failed to retrirve Zone State for Zone " + getConfig().get(ZONE_ID));
            return;
        }
    }

    protected Builder prepareWebTargetRequest(WebTarget target) {
        if (tokenExpiration < System.currentTimeMillis() - (30 * 1000)) {
            logger.trace("Refreshing Bearer Token");
            refreshToken();
        }

        return target.request(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "Bearer " + accessToken);
    }

    protected Home getHome() {
        int homeId = ((BigDecimal) getConfig().get(HOME_ID)).intValue();
        Response response = prepareWebTargetRequest(homeTarget.resolveTemplate("homeId", homeId)).get();

        logger.trace("Querying Home : Response : {} ", response.getStatusInfo());

        JsonParser parser = new JsonParser();

        Home home = gson.fromJson(parser.parse(response.readEntity(String.class)).getAsJsonObject(), Home.class);

        return home;
    }

    protected Zone getZone() {
        int homeId = ((BigDecimal) getConfig().get(HOME_ID)).intValue();
        Response response = prepareWebTargetRequest(zonesTarget.resolveTemplate("homeId", homeId)).get();

        logger.trace("Querying Home {} Zone List : Response : {} ", homeId, response.getStatusInfo());

        JsonParser parser = new JsonParser();

        Zone[] zoneArray = gson.fromJson(parser.parse(response.readEntity(String.class)).getAsJsonArray(),
                Zone[].class);

        int zoneId = ((BigDecimal) getConfig().get(ZONE_ID)).intValue();

        for (int i = 0; i < zoneArray.length; i++) {
            if (zoneArray[i].id == zoneId) {
                return zoneArray[i];
            }
        }

        return null;
    }

    protected ZoneState getZoneState() {
        int homeId = ((BigDecimal) getConfig().get(HOME_ID)).intValue();
        int zoneId = ((BigDecimal) getConfig().get(ZONE_ID)).intValue();

        Response response = prepareWebTargetRequest(
                stateTarget.resolveTemplate("homeId", homeId).resolveTemplate("zoneId", zoneId)).get();

        logger.trace("Querying Home {}, Zone {} State : Response : {} ", homeId, zoneId, response.getStatus());

        if (response.getStatus() == 200 && response.hasEntity()) {

            ZoneState zoneState = gson.fromJson(parser.parse(response.readEntity(String.class)).getAsJsonObject(),
                    ZoneState.class);

            logger.trace("Got zone state: " + zoneState);

            return zoneState;
        } else {
            return null;
        }
    }

    protected void updateStateFromZone(ZoneState zoneState) {
        logger.trace("Updating Zone State");
        boolean useCelsius = (boolean) getConfig().get(USE_CELSIUS);
        updateState(CHANNEL_MODE, new StringType(zoneState.getMode()));
        updateState(CHANNEL_LINK_STATE, zoneState.getLinkState());

        updateState(CHANNEL_HUMIDITY, new PercentType(zoneState.sensorDataPoints.humidity.percentage));
        updateState(CHANNEL_INSIDE_TEMPERATURE, new DecimalType(zoneState.getInsideTemperature(useCelsius)));

        updateState(CHANNEL_HEATING_STATE, zoneState.getHeatingState());
        updateState(CHANNEL_TARGET_TEMPERATURE, new DecimalType(zoneState.getTargetTemperature(useCelsius)));
    }

    private Runnable updateService = new Runnable() {
        @Override
        public void run() {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                ZoneState state = null;
                try {
                    state = getZoneState();
                } catch (Exception e) {
                    updateState(CHANNEL_SERVER_STATUS, OnOffType.OFF);
                    logger.trace("Failed to retrieve zone state");
                }

                if (state == null) {
                    updateState(CHANNEL_SERVER_STATUS, OnOffType.OFF);
                    logger.trace("Failed to retrieve zone state");
                } else {
                    updateState(CHANNEL_SERVER_STATUS, OnOffType.ON);
                    updateStateFromZone(state);
                }
            }
        }
    };

    @Override
    public void dispose() {
        logger.trace("Disposing of Tado Handler for {}", getThing().getUID());

        if (updateJob != null && !updateJob.isCancelled()) {
            updateJob.cancel(true);
            updateJob = null;
        }
    }

    public boolean refreshToken() {
        Response response = authTarget.queryParam("client_id", CLIENT_ID).queryParam("grant_type", "refresh_token")
                .queryParam("refresh_token", refreshToken).request().get();

        logger.trace("Authenticating: Response : {}", response.getStatusInfo());

        if (response != null) {
            if (response.getStatus() == 200 && response.hasEntity()) {
                String responsePayload = response.readEntity(String.class);
                AuthResponse readObject = gson.fromJson(parser.parse(responsePayload).getAsJsonObject(),
                        AuthResponse.class);

                return saveToken(readObject);
            } else if (response.getStatus() == 401) {
                return false;
            } else if (response.getStatus() == 503) {
                return false;
            }
        }

        return true;
    }

    public boolean saveToken(AuthResponse result) {
        if (result.access_token == null) {
            logger.debug("Missing Access Token from Authentication/Refresh Response");
            return false;
        }
        accessToken = result.access_token;
        logger.trace("Authenticating : Setting Access Code to : {}", accessToken);

        if (result.expires_in == 0) {
            logger.debug("Missing Expiration Time from Authentication/Refresh Response");
            return false;
        }

        tokenExpiration = System.currentTimeMillis() + (result.expires_in * 1000);
        logger.trace("Authenticating : Setting Token Expiration to : {}", tokenExpiration);

        if (result.refresh_token == null) {
            logger.debug("Missing Refresh Token from Authentication or Refresh Response");
            return false;
        }

        refreshToken = result.refresh_token;
        logger.trace("Authenticating : Setting Refresh Token to : {}", refreshToken);

        return true;
    }

    protected ThingStatusDetail authenticate(String email, String password) {
        Response response = authTarget.queryParam("username", email).queryParam("password", password)
                .queryParam("client_id", CLIENT_ID).queryParam("grant_type", "password")
                .queryParam("scope", "home.user").request().get();

        logger.trace("Authenticating: Response : {}", response.getStatusInfo());

        if (response != null) {
            if (response.getStatus() == 200 && response.hasEntity()) {
                String responsePayload = response.readEntity(String.class);
                AuthResponse readObject = gson.fromJson(parser.parse(responsePayload).getAsJsonObject(),
                        AuthResponse.class);

                if (!saveToken(readObject)) {
                    return ThingStatusDetail.COMMUNICATION_ERROR;
                } else {
                    return ThingStatusDetail.NONE;
                }
            } else if (response.getStatus() == 401) {
                return ThingStatusDetail.CONFIGURATION_ERROR;
            } else if (response.getStatus() == 503) {
                return ThingStatusDetail.COMMUNICATION_ERROR;
            }
        }

        return ThingStatusDetail.CONFIGURATION_ERROR;
    }
}
