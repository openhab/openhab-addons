/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import static org.openhab.binding.nest.NestBindingConstants.*;

import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.internal.NestUpdateRequest;
import org.openhab.binding.nest.internal.data.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deals with the structures on the nest api, turning them into a thing in openhab.
 *
 * @author David Bennett - initial contribution
 */
public class NestStructureHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(NestStructureHandler.class);
    private Structure lastData;

    public NestStructureHandler(Thing thing) {
        super(thing);
    }

    /**
     * Update the structure from the data we received from nest.
     *
     * @param structure The structure data to update with
     */
    public void updateStructure(Structure structure) {
        logger.debug("Updating structure {}", structure.getStructureId());
        if (lastData == null || !lastData.equals(structure)) {
            updateState(CHANNEL_RUSH_HOUR_REWARDS_ENROLLMENT,
                    structure.isRushHourRewardsEnrollement() ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_COUNTRY_CODE, new StringType(structure.getCountryCode()));
            updateState(CHANNEL_POSTAL_CODE, new StringType(structure.getPostalCode()));

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            if (structure.getPeakPeriodStartTime() != null) {
                cal.setTime(structure.getPeakPeriodStartTime());
                updateState(CHANNEL_PEAK_PERIOD_START_TIME, new DateTimeType(cal));
            }
            if (structure.getPeakPeriodEndTime() != null) {
                cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                cal.setTime(structure.getPeakPeriodEndTime());
                updateState(CHANNEL_PEAK_PERIOD_END_TIME, new DateTimeType(cal));
            }
            updateState(CHANNEL_TIME_ZONE, new StringType(structure.getTimeZone()));
            if (structure.getEtaBegin() != null) {
                cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                cal.setTime(structure.getEtaBegin());
                updateState(CHANNEL_ETA_BEGIN, new DateTimeType(cal));
            }
            if (structure.getCoAlarmState() != null) {
                updateState(CHANNEL_CO_ALARM_STATE, new StringType(structure.getCoAlarmState().toString()));
            } else {
                logger.error("Cannot get co alarm state {} on {}", CHANNEL_CO_ALARM_STATE, getThing().getLabel());
            }
            if (structure.getSmokeAlarmState() != null) {
                updateState(CHANNEL_SMOKE_ALARM_STATE, new StringType(structure.getSmokeAlarmState().toString()));
            } else {
                logger.error("Cannot get smoke alarm state {} on {}", CHANNEL_SMOKE_ALARM_STATE, getThing().getLabel());
            }

            updateState(CHANNEL_AWAY, new StringType(structure.getAway().toString()));

            updateStatus(ThingStatus.ONLINE);

            // Setup the properties for this structure.
            updateProperty(PROPERTY_ID, structure.getStructureId());
        } else {
            logger.debug("Nothing to update, same as before.");
        }
    }

    /**
     * Handles updating the details on this structure by sending the request all the way
     * to nest.
     *
     * @param channelUID the channel to update
     * @param command the command to apply
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_AWAY)) {
            // Change the home/away state.
            if (command instanceof StringType) {
                StringType cmd = (StringType) command;
                // Set the mode to be the cmd value.
                addUpdateRequest("away", cmd.toString());
            }
        }

    }

    /**
     * Creates the url to set a specific value on the thermostat.
     */
    private void addUpdateRequest(String field, Object value) {
        String structId = getThing().getProperties().get(NestBindingConstants.PROPERTY_ID);
        StringBuilder builder = new StringBuilder().append(NestBindingConstants.NEST_STRUCTURE_UPDATE_URL)
                .append(structId);
        NestUpdateRequest request = new NestUpdateRequest();
        request.setUpdateUrl(builder.toString());
        request.addValue(field, value);
        NestBridgeHandler bridge = (NestBridgeHandler) getBridge();
        bridge.addUpdateRequest(request);
    }

}
