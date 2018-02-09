/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.openhab.binding.nest.NestBindingConstants.*;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.nest.internal.config.NestStructureConfiguration;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.Structure.HomeAwayState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deals with the structures on the Nest API, turning them into a thing in openHAB.
 *
 * @author David Bennett - initial contribution
 * @author Wouter Born - Handle channel refresh command
 */
public class NestStructureHandler extends NestBaseHandler<Structure> {
    private final Logger logger = LoggerFactory.getLogger(NestStructureHandler.class);

    public NestStructureHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected State getChannelState(ChannelUID channelUID, Structure structure) {
        switch (channelUID.getId()) {
            case CHANNEL_AWAY:
                return getAsStringTypeOrNull(structure.getAway());
            case CHANNEL_CO_ALARM_STATE:
                return getAsStringTypeOrNull(structure.getCoAlarmState());
            case CHANNEL_COUNTRY_CODE:
                return getAsStringTypeOrNull(structure.getCountryCode());
            case CHANNEL_ETA_BEGIN:
                return getAsDateTimeTypeOrNull(structure.getEtaBegin());
            case CHANNEL_PEAK_PERIOD_END_TIME:
                return getAsDateTimeTypeOrNull(structure.getPeakPeriodEndTime());
            case CHANNEL_PEAK_PERIOD_START_TIME:
                return getAsDateTimeTypeOrNull(structure.getPeakPeriodStartTime());
            case CHANNEL_POSTAL_CODE:
                return getAsStringTypeOrNull(structure.getPostalCode());
            case CHANNEL_RUSH_HOUR_REWARDS_ENROLLMENT:
                return getAsOnOffType(structure.isRushHourRewardsEnrollement());
            case CHANNEL_SMOKE_ALARM_STATE:
                return getAsStringTypeOrNull(structure.getSmokeAlarmState());
            case CHANNEL_TIME_ZONE:
                return getAsStringTypeOrNull(structure.getTimeZone());
            default:
                logger.error("Unsupported channelId '{}'", channelUID.getId());
                return UnDefType.UNDEF;
        }
    }

    @Override
    public String getId() {
        return getStructureId();
    }

    private String getStructureId() {
        return getConfigAs(NestStructureConfiguration.class).structureId;
    }

    /**
     * Handles updating the details on this structure by sending the request all the way
     * to Nest.
     *
     * @param channelUID the channel to update
     * @param command the command to apply
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (REFRESH.equals(command)) {
            if (getLastUpdate() != null) {
                updateState(channelUID, getChannelState(channelUID, getLastUpdate()));
            }
        } else if (CHANNEL_AWAY.equals(channelUID.getId())) {
            // Change the home/away state.
            if (command instanceof StringType) {
                StringType cmd = (StringType) command;
                // Set the mode to be the cmd value.
                addUpdateRequest(NEST_STRUCTURE_UPDATE_URL, "away", HomeAwayState.valueOf(cmd.toString()));
            }
        }
    }

    @Override
    public void onNewNestStructureData(Structure structure) {
        if (isNotHandling(structure)) {
            logger.debug("Structure {} is not handling update for {}", getStructureId(), structure.getStructureId());
            return;
        }

        logger.debug("Updating structure {}", structure.getStructureId());

        setLastUpdate(structure);
        updateChannels(structure);
        updateStatus(ThingStatus.ONLINE);
    }

}
