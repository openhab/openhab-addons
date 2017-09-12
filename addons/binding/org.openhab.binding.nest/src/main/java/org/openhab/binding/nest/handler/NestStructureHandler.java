/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nest.internal.data.Camera;
import org.openhab.binding.nest.internal.data.SmokeDetector;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.Thermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_AWAY;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_COUNTRY_CODE;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_CO_ALARM_STATE;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_ETA_BEGIN;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_PEAK_PERIOD_END_TIME;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_PEAK_PERIOD_START_TIME;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_POSTAL_CODE;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_RUSH_HOUR_REWARDS_ENROLLMENT;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_SMOKE_ALARM_STATE;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_TIME_ZONE;
import static org.openhab.binding.nest.NestBindingConstants.NEST_STRUCTURE_UPDATE_URL;
import static org.openhab.binding.nest.NestBindingConstants.PROPERTY_ID;

/**
 * Deals with the structures on the nest api, turning them into a thing in openhab.
 *
 * @author David Bennett - initial contribution
 */
public class NestStructureHandler extends NestBaseHandler {
    private Logger logger = LoggerFactory.getLogger(NestStructureHandler.class);

    public NestStructureHandler(@NonNull Thing thing) {
        super(thing);
    }

    /**
     * Handles updating the details on this structure by sending the request all the way
     * to nest.
     *
     * @param channelUID the channel to update
     * @param command    the command to apply
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_AWAY)) {
            // Change the home/away state.
            if (command instanceof StringType) {
                StringType cmd = (StringType) command;
                // Set the mode to be the cmd value.
                addUpdateRequest(NEST_STRUCTURE_UPDATE_URL, "away", cmd.toString());
            }
        }
    }

    @Override
    public void onNewNestThermostatData(Thermostat thermostat) {
        // ignore we are not a thermostat handler
    }

    @Override
    public void onNewNestCameraData(Camera camera) {
        // ignore we are note a camera handler
    }

    @Override
    public void onNewNestSmokeDetectorData(SmokeDetector smokeDetector) {
        // ignore we are note a smoke sensor
    }

    @Override
    public void onNewNestStructureData(Structure structure) {
        logger.debug("Updating structure {}", structure.getStructureId());
        updateState(CHANNEL_RUSH_HOUR_REWARDS_ENROLLMENT,
                structure.isRushHourRewardsEnrollement() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_COUNTRY_CODE,           getAsStringTypeOrNull(structure.getCountryCode()));
        updateState(CHANNEL_POSTAL_CODE,            getAsStringTypeOrNull(structure.getPostalCode()));
        updateState(CHANNEL_PEAK_PERIOD_START_TIME, getAsDateTimeTypeOrNull(structure.getPeakPeriodStartTime()));
        updateState(CHANNEL_PEAK_PERIOD_END_TIME,   getAsDateTimeTypeOrNull(structure.getPeakPeriodEndTime()));
        updateState(CHANNEL_TIME_ZONE,              getAsStringTypeOrNull(structure.getTimeZone()));
        updateState(CHANNEL_ETA_BEGIN,              getAsDateTimeTypeOrNull(structure.getEtaBegin()));
        updateState(CHANNEL_CO_ALARM_STATE,         getAsStringTypeOrNull(structure.getCoAlarmState()));
        updateState(CHANNEL_SMOKE_ALARM_STATE,      getAsStringTypeOrNull(structure.getSmokeAlarmState()));
        updateState(CHANNEL_AWAY,                   getAsStringTypeOrNull(structure.getAway()));

        updateStatus(ThingStatus.ONLINE);

        // Setup the properties for this structure.
        updateProperty(PROPERTY_ID, structure.getStructureId());
    }
}
