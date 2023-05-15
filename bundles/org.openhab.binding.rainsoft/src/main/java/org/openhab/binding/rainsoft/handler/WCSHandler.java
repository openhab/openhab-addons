/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.rainsoft.handler;

import static org.openhab.binding.rainsoft.RainSoftBindingConstants.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openhab.binding.rainsoft.internal.RainSoftDeviceRegistry;
import org.openhab.binding.rainsoft.internal.data.WCS;
import org.openhab.binding.rainsoft.internal.errors.DeviceNotFoundException;
import org.openhab.binding.rainsoft.internal.errors.IllegalDeviceClassException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The handler for a Water Conditioning System.
 *
 * @author Ben Rosenblum - Initial contribution
 *
 */

public class WCSHandler extends RainSoftDeviceHandler {

    private final Map<String, Object> channelStateMap = new HashMap<>(2);

    public WCSHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WCS handler");
        super.initialize();

        RainSoftDeviceRegistry registry = RainSoftDeviceRegistry.getInstance();
        String id = getThing().getUID().getId();
        if (registry.isInitialized()) {
            try {
                linkDevice(id, WCS.class);
                updateStatus(ThingStatus.ONLINE);
            } catch (DeviceNotFoundException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Device with id '" + id + "' not found");
            } catch (IllegalDeviceClassException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Device with id '" + id + "' of wrong type");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Waiting for RainSoftAccount to initialize");
        }

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
        if (this.refreshJob == null) {
            Configuration config = getThing().getConfiguration();
            Integer refreshInterval = ((BigDecimal) config.get("refreshInterval")).intValueExact();
            startAutomaticRefresh(refreshInterval);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void refreshState() {
        // TODO Auto-generated method stub
    }

    private void updateChannelState(String channelId, Object value) {
        if (channelStateMap.get(channelId) != null) {
            if (!channelStateMap.get(channelId).equals(value)) {
                logger.trace("WCSHandler - updateChannelState - Channel state updated for {} - {}", channelId,
                        ((State) value).toString());
                updateState(channelId, (State) value);
                channelStateMap.put(channelId, value);
            } else {
                logger.trace("WCSHandler - updateChannelState - Ignoring unchanged state for {} - {}", channelId,
                        ((State) value).toString());
            }
        } else {
            logger.trace("WCSHandler - updateChannelState - Channel state created for {} - {}", channelId,
                    ((State) value).toString());
            updateState(channelId, (State) value);
            channelStateMap.put(channelId, value);
        }
    }

    @Override
    protected void minuteTick() {
        logger.debug("WCS Handler - minuteTick");
        if (device == null) {
            initialize();
        }

        if (device != null) {
            try {
                String deviceInfo = device.getDeviceInfo();
                String waterUsage = device.getWaterUsage();
                String saltUsage = device.getSaltUsage();

                logger.trace("WCSHandler - minuteTick - deviceInfo: {}", deviceInfo);
                logger.trace("WCSHandler - minuteTick - waterUsage: {}", waterUsage);
                logger.trace("WCSHandler - minuteTick - saltUsage: {}", saltUsage);

                if ((deviceInfo != null) && (!deviceInfo.isEmpty())) {
                    updateChannelState(CHANNEL_STATUS_SYSTEMSTATUS, new StringType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("systemStatusName").toString()));
                    updateChannelState(CHANNEL_STATUS_STATUSCODE, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("systemStatusCode").toString()));
                    updateChannelState(CHANNEL_STATUS_STATUSASOF,
                            new DateTimeType(((JSONObject) new JSONParser().parse(deviceInfo)).get("asOf").toString()));
                    updateChannelState(CHANNEL_STATUS_REGENTIME, new DateTimeType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("regenTime").toString()));
                    updateChannelState(CHANNEL_STATUS_LASTREGEN, new DateTimeType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("lastRegenDate").toString()));
                    updateChannelState(CHANNEL_STATUS_AIRPURGEHOUR, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("airPurgeHour").toString()));
                    updateChannelState(CHANNEL_STATUS_AIRPURGEMINUTE, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("airPurgeMinute").toString()));
                    updateChannelState(CHANNEL_STATUS_FLTREGENTIME, new DateTimeType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("fltRegenTime").toString()));
                    updateChannelState(CHANNEL_STATUS_MAXSALT, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("maxSalt").toString()));
                    updateChannelState(CHANNEL_STATUS_SALTLBS, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("saltLbs").toString()));
                    updateChannelState(CHANNEL_STATUS_CAPACITYREMAINING, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("capacityRemaining").toString()));
                    if (((JSONObject) new JSONParser().parse(deviceInfo)).get("isVacationMode").toString()
                            .equals("true")) {
                        updateChannelState(CHANNEL_STATUS_VACATIONMODE, OnOffType.ON);
                    } else {
                        updateChannelState(CHANNEL_STATUS_VACATIONMODE, OnOffType.OFF);
                    }
                    updateChannelState(CHANNEL_STATUS_HARDNESS, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("hardness").toString()));
                    updateChannelState(CHANNEL_STATUS_PRESSURE, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("pressure").toString()));
                    updateChannelState(CHANNEL_STATUS_IRONLEVEL, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("ironLevel").toString()));
                    updateChannelState(CHANNEL_STATUS_DRAINFLOW, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("drainFlow").toString()));
                    updateChannelState(CHANNEL_STATUS_AVGMONTHSALT, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("averageMonthlySalt").toString()));
                    updateChannelState(CHANNEL_STATUS_DAILYWATERUSE, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("dailyWaterUse").toString()));
                    updateChannelState(CHANNEL_STATUS_REGENS28DAY, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("regens28Day").toString()));
                    updateChannelState(CHANNEL_STATUS_WATER28DAY, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("water28Day").toString()));
                    updateChannelState(CHANNEL_STATUS_ENDOFDAY, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("endOfDay").toString()));
                    updateChannelState(CHANNEL_STATUS_SALT28DAY, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("salt28Day").toString()));
                    updateChannelState(CHANNEL_STATUS_FLOWSINCEREGEN, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("flowSinceLastRegen").toString()));
                    updateChannelState(CHANNEL_STATUS_LIFETIMEFLOW, new DecimalType(
                            ((JSONObject) new JSONParser().parse(deviceInfo)).get("lifeTimeFlow").toString()));
                }

                if ((waterUsage != null) && (saltUsage != null) && (!waterUsage.isEmpty()) && (!saltUsage.isEmpty())) {
                    String dailyWaterUsage = ((JSONObject) new JSONParser().parse(waterUsage)).get("dailyUsage")
                            .toString();
                    String dailySaltUsage = ((JSONObject) new JSONParser().parse(saltUsage)).get("dailyUsage")
                            .toString();
                    String dailyWaterUsageLabels = ((JSONObject) new JSONParser().parse(dailyWaterUsage)).get("labels")
                            .toString();
                    String dailyWaterUsageData = ((JSONObject) new JSONParser().parse(dailyWaterUsage)).get("data")
                            .toString();
                    String dailySaltUsageData = ((JSONObject) new JSONParser().parse(dailySaltUsage)).get("data")
                            .toString();
                    JSONArray dailyWaterUsageLabelsArray = ((JSONArray) new JSONParser().parse(dailyWaterUsageLabels));
                    JSONArray dailyWaterUsageDataArray = ((JSONArray) new JSONParser().parse(dailyWaterUsageData));
                    JSONArray dailySaltUsageDataArray = ((JSONArray) new JSONParser().parse(dailySaltUsageData));
                    logger.debug("WCSHandler - minuteTick - dailyWaterUsageLabels - {}", dailyWaterUsageLabels);
                    logger.debug("WCSHandler - minuteTick - dailyWaterUsageData - {}", dailyWaterUsageData);
                    logger.debug("WCSHandler - minuteTick - dailySaltUsageData - {}", dailySaltUsageData);
                    for (int i = 0; i < 28; i++) {
                        String label = dailyWaterUsageLabelsArray.get(i).toString();
                        String water = dailyWaterUsageDataArray.get(i).toString();
                        String salt = dailySaltUsageDataArray.get(i).toString();
                        logger.trace("WCSHandler - minuteTick - dailyArrays - {} {} {}", label, water, salt);
                        String labelChannel = "usage#day" + i + "date";
                        String waterChannel = "usage#day" + i + "water";
                        String saltChannel = "usage#day" + i + "salt";
                        updateChannelState(labelChannel, new StringType(label));
                        updateChannelState(waterChannel, new DecimalType(water));
                        updateChannelState(saltChannel, new DecimalType(salt));
                    }
                }
            } catch (ParseException e1) {
                logger.debug("WCSHandler - RestClient reported ParseException trying getAuthenticatedProfile: {}",
                        e1.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Invalid response from rainsoft.com");
            }
        }
    }
}
