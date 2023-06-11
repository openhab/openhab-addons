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
package org.openhab.binding.nest.internal.wwn.handler;

import static org.openhab.binding.nest.internal.wwn.WWNBindingConstants.*;
import static org.openhab.core.types.RefreshType.REFRESH;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.wwn.config.WWNStructureConfiguration;
import org.openhab.binding.nest.internal.wwn.dto.WWNStructure;
import org.openhab.binding.nest.internal.wwn.dto.WWNStructure.HomeAwayState;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deals with the structures on the WWN API, turning them into a thing in openHAB.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Handle channel refresh command
 */
@NonNullByDefault
public class WWNStructureHandler extends WWNBaseHandler<WWNStructure> {
    private final Logger logger = LoggerFactory.getLogger(WWNStructureHandler.class);

    private @Nullable String structureId;

    public WWNStructureHandler(Thing thing) {
        super(thing, WWNStructure.class);
    }

    @Override
    protected State getChannelState(ChannelUID channelUID, WWNStructure structure) {
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
                return getAsOnOffTypeOrNull(structure.isRhrEnrollment());
            case CHANNEL_SECURITY_STATE:
                return getAsStringTypeOrNull(structure.getWwnSecurityState());
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
        String localStructureId = structureId;
        if (localStructureId == null) {
            localStructureId = getConfigAs(WWNStructureConfiguration.class).structureId;
            structureId = localStructureId;
        }
        return localStructureId;
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
            WWNStructure lastUpdate = getLastUpdate();
            if (lastUpdate != null) {
                updateState(channelUID, getChannelState(channelUID, lastUpdate));
            }
        } else if (CHANNEL_AWAY.equals(channelUID.getId())) {
            // Change the home/away state.
            if (command instanceof StringType) {
                StringType cmd = (StringType) command;
                // Set the mode to be the cmd value.
                addUpdateRequest(NEST_STRUCTURE_UPDATE_PATH, "away", HomeAwayState.valueOf(cmd.toString()));
            }
        }
    }

    @Override
    protected void update(@Nullable WWNStructure oldStructure, WWNStructure structure) {
        logger.debug("Updating {}", getThing().getUID());

        updateLinkedChannels(oldStructure, structure);

        if (ThingStatus.ONLINE != thing.getStatus()) {
            updateStatus(ThingStatus.ONLINE);
        }
    }
}
