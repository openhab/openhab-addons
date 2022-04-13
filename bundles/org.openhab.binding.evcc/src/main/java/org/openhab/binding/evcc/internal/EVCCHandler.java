/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.evcc.internal;

import static org.openhab.binding.evcc.internal.EVCCBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evcc.internal.dto.*;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link EVCCHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class EVCCHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(EVCCHandler.class);
    private final Gson gson = new Gson();
    private @Nullable ScheduledFuture<?> pollingJob;

    private @Nullable EVCCConfiguration config;
    private @Nullable String thingLabel;

    private @Nullable Status status;

    private int numberOfLoadpoints = 0;
    private @Nullable String sitename;
    private @Nullable Boolean batteryConfigured;
    private @Nullable Boolean gridConfigured;
    private @Nullable Boolean pvConfigured;

    public EVCCHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO: handle command

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information:
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
    }

    @Override
    public void initialize() {
        config = getConfigAs(EVCCConfiguration.class);
        thingLabel = getThing().getLabel();
        updateStatus(ThingStatus.UNKNOWN);

        if ("".equals(config.host)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "No host configured");
        } else {
            // Background initialization:
            scheduler.execute(() -> {
                status = getStatus(config.host);
                if (status != null) {
                    sitename = status.getResult().getSiteTitle();
                    numberOfLoadpoints = status.getResult().getLoadpoints().length;
                    logger.info("Found {} loadpoints on site {} (host: {}).", numberOfLoadpoints, sitename,
                            config.host);
                    updateStatus(ThingStatus.ONLINE);
                    batteryConfigured = status.getResult().getBatteryConfigured();
                    gridConfigured = status.getResult().getGridConfigured();
                    pvConfigured = status.getResult().getPvConfigured();
                    pollingJob = scheduler.scheduleWithFixedDelay(this::pollingJob, 0, config.refreshInterval,
                            TimeUnit.SECONDS);
                    refreshOnStartup();
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                            "Failed to contact evcc!");
                }
            });
        }
    }

    private void pollingJob() {
        this.thingLabel = getThing().getLabel();
        this.status = getStatus(config.host);
        numberOfLoadpoints = status.getResult().getLoadpoints().length;
        batteryConfigured = status.getResult().getBatteryConfigured();
        gridConfigured = status.getResult().getGridConfigured();
        pvConfigured = status.getResult().getPvConfigured();
        // updateStatusGeneral();
        for (int i = 0; i < numberOfLoadpoints; i++) {
            // updateStatusLoadpoint(i);
        }
    }

    private void refreshOnStartup() {
        createChannelsGeneral();
        // updateStatusGeneral();
        for (int i = 0; i < this.numberOfLoadpoints; i++) {
            createChannelsLoadpoint("loadpoint" + i);
            // updateStatusLoadpoint(i);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
    }

    // Utility functions
    private void createChannelsGeneral() {
        if (true) {
            createChannel(CHANNEL_BATTERY_POWER, "general", CHANNEL_TYPE_UID_BATTERY_POWER, "Number:Power");
            createChannel(CHANNEL_BATTERY_SOC, "general", CHANNEL_TYPE_UID_BATTERY_SOC, "Number:Dimensionless");
            createChannel(CHANNEL_BATTERY_PRIORITY_SOC, "general", CHANNEL_TYPE_UID_BATTERY_PRIORITY_SOC,
                    "Number:Dimensionless");
        }
        if (true) {
            createChannel(CHANNEL_GRID_POWER, "general", CHANNEL_TYPE_UID_GRID_POWER, "Number:Power");
        }
        createChannel(CHANNEL_HOME_POWER, "general", CHANNEL_TYPE_UID_HOME_POWER, "Number:Power");
        if (true) {
            createChannel(CHANNEL_PV_POWER, "general", CHANNEL_TYPE_UID_PV_POWER, "Number:Power");
        }
    }

    private void createChannelsLoadpoint(String loadpointName) {
        createChannel(CHANNEL_LOADPOINT_ACTIVE_PHASES, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_ACTIVE_PHASES,
                "Number");
        createChannel(CHANNEL_LOADPOINT_CHARGE_CURRENT, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_CHARGE_CURRENT,
                "Number:ElectricCurrent");
        createChannel(CHANNEL_LOADPOINT_CHARGE_DURATION, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_CHARGE_DURATION,
                "Number:Time");
        createChannel(CHANNEL_LOADPOINT_CHARGE_POWER, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_CHARGE_POWER,
                "Number:Power");
        createChannel(CHANNEL_LOADPOINT_CHARGE_REMAINING_DURATION, loadpointName,
                CHANNEL_TYPE_UID_LOADPOINT_CHARGE_REMAINING_DURATION, "Number:Time");
        createChannel(CHANNEL_LOADPOINT_CHARGED_ENERGY, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_CHARGED_ENERGY,
                "Number:Energy");
        createChannel(CHANNEL_LOADPOINT_CHARGING, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_CHARGING, "Switch");
        createChannel(CHANNEL_LOADPOINT_CONNECTED, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_CONNECTED, "Switch");
        createChannel(CHANNEL_LOADPOINT_CONNECTED_DURATION, loadpointName,
                CHANNEL_TYPE_UID_LOADPOINT_CONNECTED_DURATION, "Number:Time");
        createChannel(CHANNEL_LOADPOINT_ENABLED, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_ENABLED, "Switch");
        createChannel(CHANNEL_LOADPOINT_HAS_VEHICLE, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_HAS_VEHICLE, "Switch");
        createChannel(CHANNEL_LOADPOINT_MAX_CURRENT, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_MAX_CURRENT,
                "Number:ElectricCurrent");
        createChannel(CHANNEL_LOADPOINT_MIN_CURRENT, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_MIN_CURRENT,
                "Number:ElectricCurrent");
        createChannel(CHANNEL_LOADPOINT_MIN_SOC, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_MIN_SOC,
                "Number:Dimensionless");
        createChannel(CHANNEL_LOADPOINT_MODE, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_MODE, "String");
        createChannel(CHANNEL_LOADPOINT_PHASES, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_PHASES, "Number");
        createChannel(CHANNEL_LOADPOINT_PV_ACTION, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_PV_ACTION, "String");
        createChannel(CHANNEL_LOADPOINT_PV_REMAINING, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_PV_REMAINING, "Number");
        createChannel(CHANNEL_LOADPOINT_TARGET_SOC, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_TARGET_SOC,
                "Number:Dimensionless");
        createChannel(CHANNEL_LOADPOINT_TARGET_TIME, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_TARGET_TIME,
                "Number:Time");
        createChannel(CHANNEL_LOADPOINT_TARGET_TIME_ACTIVE, loadpointName,
                CHANNEL_TYPE_UID_LOADPOINT_TARGET_TIME_ACTIVE, "Switch");
        createChannel(CHANNEL_LOADPOINT_TITLE, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_TITLE, "String");
        createChannel(CHANNEL_LOADPOINT_VEHICLE_CAPACITY, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_CAPACITY,
                "Number:Energy");
        createChannel(CHANNEL_LOADPOINT_VEHICLE_ODOMETER, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_ODOMETER,
                "Number:Length");
        createChannel(CHANNEL_LOADPOINT_VEHICLE_PRESENT, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_PRESENT,
                "Switch");
        createChannel(CHANNEL_LOADPOINT_VEHICLE_RANGE, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_RANGE,
                "Number:Length");
        createChannel(CHANNEL_LOADPOINT_VEHICLE_SOC, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_SOC,
                "Number:Dimensionless");
        createChannel(CHANNEL_LOADPOINT_VEHICLE_TITLE, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_TITLE,
                "String");
    }

    private void createChannel(String channel, String channelGroupId, ChannelTypeUID channelTypeUID, String itemType) {
        ChannelUID channelToCheck = new ChannelUID(thing.getUID(), channelGroupId, channel);
        if (thing.getChannel(channelToCheck) == null) {
            ThingBuilder thingBuilder = editThing();
            Channel testchannel = ChannelBuilder
                    .create(new ChannelUID(getThing().getUID(), channelGroupId, channel), itemType)
                    .withType(channelTypeUID).build();
            thingBuilder.withChannel(testchannel);
            updateThing(thingBuilder.build());
        }
    }

    /**
     * Make a HTTP request.
     * 
     * @param description request description for logger
     * @param url request URL
     * @param method reguest method, e.g. GET, POST
     * @return the response body or response_code 999 if request faild
     */
    private String httpRequest(@Nullable String description, String url, String method) {
        String response = "";
        try {
            response = HttpUtil.executeUrl(method, HTTPS + url, LONG_CONNECTION_TIMEOUT_MILLISEC);
            logger.info("{} - {}", description, response);
            return response;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", description, e.getMessage());
            return "{\"response_code\":\"999\"}";
        }
    }

    // End utility functions
    // API calls to evcc
    /**
     * Get the status from evcc.
     * 
     * @param host hostname of IP address of the evcc instance
     * @return Status object or null if request failed
     */
    private @Nullable Status getStatus(@Nullable String host) {
        final String reponse = httpRequest("Status", host + EVCC_REST_API + "state", "GET");
        return gson.fromJson(reponse, Status.class);
    }

    /**
     * Get the number of loadpoints.
     * 
     * @param host hostname of IP address of the evcc instance
     * @param status Status object returned from evcc (/api/state)
     * @return number of loadpoints
     */
    private @Nullable Integer getNumberOfLoadpoints(@Nullable String host, @Nullable Status status) {
        return status.getResult().getLoadpoints().length;
    }

    // Loadpoint specific API calls.
    private @Nullable String setMode(@Nullable String host, int loadpoint, String mode) {
        return httpRequest("Set mode of loadpoint " + loadpoint, host + EVCC_REST_API + loadpoint + "/mode/" + mode,
                "POST");
    }

    private @Nullable String setMinSoC(@Nullable String host, int loadpoint, int minSoC) {
        return httpRequest("Set minSoC of loadpoint " + loadpoint,
                host + EVCC_REST_API + loadpoint + "/minsoc/" + minSoC, "POST");
    }

    private @Nullable String setTargetSoC(@Nullable String host, int loadpoint, int targetSoC) {
        return httpRequest("Set targetSoC of loadpoint " + loadpoint,
                host + EVCC_REST_API + loadpoint + "/targetsoc/" + targetSoC, "POST");
    }

    private @Nullable String setPhases(@Nullable String host, int loadpoint, int phases) {
        return httpRequest("Set phases of loadpoint " + loadpoint,
                host + EVCC_REST_API + loadpoint + "/phases/" + phases, "POST");
    }

    private @Nullable String setMinCurrent(@Nullable String host, int loadpoint, int minCurrent) {
        return httpRequest("Set minCurrent of loadpoint " + loadpoint,
                host + EVCC_REST_API + loadpoint + "/mincurrent/" + minCurrent, "POST");
    }

    private @Nullable String setMaxCurrent(@Nullable String host, int loadpoint, int maxCurrent) {
        return httpRequest("Set maxCurrent of loadpoint " + loadpoint,
                host + EVCC_REST_API + loadpoint + "/maxcurrent/" + maxCurrent, "POST");
    }
    // End loadpoint specific API calls
    // End API calls to evcc
}
