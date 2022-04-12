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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EVCCHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class EVCCHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EVCCHandler.class);

    private @Nullable EVCCConfiguration config;

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

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly, i.e. any network access must be done in
        // the background initialization below.
        // Also, before leaving this method a thing status from one of ONLINE, OFFLINE or UNKNOWN must be set. This
        // might already be the real thing status in case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = true; // <background task with long running initialization here>
            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
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
            response = HttpUtil.executeUrl(method, HTTP + url, LONG_CONNECTION_TIMEOUT_MILLISEC);
            logger.trace("{} - {}", description, response);
            return response;
        } catch (IOException e) {
            logger.trace("IO Exception - {} - {}", description, e.getMessage());
            return "{\"response_code\":\"999\"}";
        }
    }
    // End utility functions
}
