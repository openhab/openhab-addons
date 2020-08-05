/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.intesis.internal.handler;

import static org.eclipse.smarthome.core.thing.Thing.*;
import static org.openhab.binding.intesis.internal.IntesisBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.intesis.internal.IntesisConfiguration;
import org.openhab.binding.intesis.internal.api.IntesisHomeHttpApi;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.AuthenticateData;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.dpval;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * The {@link IntesisHomeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class IntesisHomeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(IntesisHomeHandler.class);
    // private final HttpClient httpClient;
    private final IntesisHomeHttpApi api;
    private IntesisConfiguration config = new IntesisConfiguration();

    private @Nullable ScheduledFuture<?> refreshJob;

    private int refreshInterval = 30;
    private String ipAddress = "";
    private String password = "";
    private String sessionId = "";

    Gson gson = new Gson();

    public IntesisHomeHandler(final Thing thing, final HttpClient httpClient) {
        super(thing);
        this.api = new IntesisHomeHttpApi(config, httpClient);
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING);
        final IntesisConfiguration config = getConfigAs(IntesisConfiguration.class);
        ipAddress = config.ipAddress;
        password = config.password;
        if (ipAddress.isEmpty() || password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }

        // start background initialization:
        scheduler.schedule(() -> {
            try {
                String contentString = "{\"command\":\"getinfo\",\"data\":\"\"}";
                String response = api.postRequest(ipAddress, contentString);
                logger.trace("getInfo response : {}", response);
                if (response != null && !response.isEmpty()) {
                    boolean success = IntesisHomeJSonDTO.getSuccess(response);
                    logger.trace("success response : {}", success);
                    if (success) {
                        JsonElement devInfoNode = IntesisHomeJSonDTO.getData(response).get("info");
                        info devInfo = gson.fromJson(devInfoNode, info.class);
                        Map<String, String> properties = new HashMap<>(5);
                        properties.put(PROPERTY_VENDOR, "Intesis");
                        properties.put(PROPERTY_MODEL_ID, devInfo.deviceModel);
                        properties.put(PROPERTY_SERIAL_NUMBER, devInfo.sn);
                        properties.put(PROPERTY_FIRMWARE_VERSION, devInfo.fwVersion);
                        properties.put(PROPERTY_MAC_ADDRESS, devInfo.wlanSTAMAC);
                        updateProperties(properties);

                        contentString = "{\"command\":\"login\",\"data\":{\"username\":\"Admin\",\"password\":\""
                                + password + "\"}}";
                        response = api.postRequest(ipAddress, contentString);
                        if (response != null && !response.isEmpty()) {
                            success = IntesisHomeJSonDTO.getSuccess(response);
                            if (success) {
                                JsonElement idNode = IntesisHomeJSonDTO.getData(response).get("id");
                                AuthenticateData auth = gson.fromJson(idNode, AuthenticateData.class);
                                sessionId = auth.sessionID;
                                if (!sessionId.isEmpty()) {
                                    logger.trace("sessionID : {}", sessionId);
                                    updateStatus(ThingStatus.ONLINE);
                                } else {
                                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                                }
                                refreshJob = scheduler.scheduleWithFixedDelay(this::getAllUidValues, 0, refreshInterval,
                                        TimeUnit.SECONDS);
                            } else {
                                updateStatus(ThingStatus.OFFLINE);
                            }
                        }
                    } else {
                        updateStatus(ThingStatus.OFFLINE);
                    }
                }
            } catch (Exception e) {
            }
        }, 2, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("IntesisHomeHandler disposed.");
        final ScheduledFuture<?> refreshJob = this.refreshJob;

        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
        try {
            String contentString = "{\"command\":\"logout\",\"data\":{\"sessionID\":\"" + sessionId + "\"}}";
            api.postRequest(ipAddress, contentString);
        } catch (Exception e) {
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        int uid = 0;
        int value = 0;
        String channelId = channelUID.getId();
        if (command instanceof RefreshType) {
            // The thing is updated by the scheduled automatic refresh so do nothing here.
        } else {
            switch (channelId) {
                case POWER_CHANNEL:
                    uid = 1;
                    value = command.equals(OnOffType.OFF) ? 0 : 1;
                    break;
                case MODE_CHANNEL:
                    uid = 2;
                    value = Integer.parseInt(command.toString());
                    break;
                case WINDSPEED_CHANNEL:
                    uid = 4;
                    value = Integer.parseInt(command.toString());
                    break;
                case SWINGUD_CHANNEL:
                    uid = 5;
                    value = Integer.parseInt(command.toString());
                    break;
                case SWINGLR_CHANNEL:
                    uid = 6;
                    value = Integer.parseInt(command.toString());
                    break;
                case TEMP_CHANNEL:
                    uid = 9;
                    value = (Integer.parseInt(command.toString().replace(" °C", ""))) * 10;
                    break;
            }
        }
        if (uid != 0) {
            String contentString = "{\"command\":\"setdatapointvalue\",\"data\":{\"sessionID\":\"" + sessionId
                    + "\", \"uid\":" + uid + ",\"value\":" + value + "}}";
            String response = api.postRequest(ipAddress, contentString);
            if (response != null && !response.isEmpty()) {
                boolean success = IntesisHomeJSonDTO.getSuccess(response);
                if (!success) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    String sessionString = "{\"command\":\"login\",\"data\":{\"username\":\"Admin\",\"password\":\""
                            + password + "\"}}";
                    response = api.postRequest(ipAddress, sessionString);
                    if (response != null && !response.isEmpty()) {
                        success = IntesisHomeJSonDTO.getSuccess(response);
                        if (success) {
                            JsonElement idNode = IntesisHomeJSonDTO.getData(response).get("id");
                            AuthenticateData auth = gson.fromJson(idNode, AuthenticateData.class);
                            sessionId = auth.sessionID;
                            updateStatus(ThingStatus.ONLINE);
                            response = api.postRequest(ipAddress, contentString);
                            success = IntesisHomeJSonDTO.getSuccess(response);
                            if (!success) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                            }
                        } else {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                        }
                    }
                } else {
                    updateStatus(ThingStatus.ONLINE);
                }

            }
        }
    }

    /**
     * Update device status and all channels
     */
    public void getAllUidValues() {
        logger.debug("Polling IntesisHome device for actuall status");
        if (thing.getStatus() != ThingStatus.ONLINE) {
            String sessionString = "{\"command\":\"login\",\"data\":{\"username\":\"Admin\",\"password\":\"" + password
                    + "\"}}";
            String response = api.postRequest(ipAddress, sessionString);
            if (response != null && !response.isEmpty()) {
                boolean success = IntesisHomeJSonDTO.getSuccess(response);
                if (success) {
                    JsonElement idNode = IntesisHomeJSonDTO.getData(response).get("id");
                    AuthenticateData auth = gson.fromJson(idNode, AuthenticateData.class);
                    sessionId = auth.sessionID;
                    updateStatus(ThingStatus.ONLINE);
                }
            }
        }
        String contentString = "{\"command\":\"getdatapointvalue\",\"data\":{\"sessionID\":\"" + sessionId
                + "\", \"uid\":\"all\"}}";
        String response = api.postRequest(ipAddress, contentString);
        if (response != null && !response.isEmpty()) {
            boolean success = IntesisHomeJSonDTO.getSuccess(response);
            if (!success) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            } else {
                JsonElement dpvalNode = IntesisHomeJSonDTO.getData(response).get("dpval");
                dpval[] uid = gson.fromJson(dpvalNode, dpval[].class);
                int value = uid[0].value;

                updateState(POWER_CHANNEL, String.valueOf(value).equals("0") ? OnOffType.OFF : OnOffType.ON);
                State stateValue = new DecimalType(uid[1].value);
                updateState(MODE_CHANNEL, stateValue);
                stateValue = new DecimalType(uid[2].value);
                updateState(WINDSPEED_CHANNEL, stateValue);
                stateValue = new DecimalType(uid[3].value);
                updateState(SWINGUD_CHANNEL, stateValue);
                int unit = (uid[4].value) / 10;
                stateValue = QuantityType.valueOf(unit, SIUnits.CELSIUS);
                updateState(TEMP_CHANNEL, stateValue);
                unit = (uid[5].value) / 10;
                stateValue = QuantityType.valueOf(unit, SIUnits.CELSIUS);
                updateState(RETURNTEMP_CHANNEL, stateValue);
                unit = (uid[12].value) / 10;
                stateValue = QuantityType.valueOf(unit, SIUnits.CELSIUS);
                updateState(OUTDOORTEMP_CHANNEL, stateValue);
            }
        }
    }
}
