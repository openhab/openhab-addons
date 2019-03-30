/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.handler;

import static org.openhab.binding.omnilink.OmnilinkBindingConstants.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.UnitStatus;

/**
 *
 * @author Craig Hamilton
 *
 */
public class UnitHandler extends AbstractOmnilinkStatusHandler<UnitStatus> {

    public UnitHandler(Thing thing) {
        super(thing);
    }

    private final static Logger logger = LoggerFactory.getLogger(UnitHandler.class);

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_UNIT_LEVEL:
            case CHANNEL_UNIT_SWITCH:
                handleUnitLevel(command);
                break;
            case CHANNEL_UNIT_ON_FOR_SECONDS:
            case CHANNEL_UNIT_OFF_FOR_SECONDS:
            case CHANNEL_UNIT_ON_FOR_MINUTES:
            case CHANNEL_UNIT_OFF_FOR_MINUTES:
            case CHANNEL_UNIT_ON_FOR_HOURS:
            case CHANNEL_UNIT_OFF_FOR_HOURS:
                handleUnitDuration(channelUID.getId(), (DecimalType) command);
                break;
            default:
                logger.warn("Unhandled command on channel ID {}", channelUID.getId());
        }

    }

    private void handleUnitDuration(@NonNull String id, @NonNull DecimalType command) {
        final int unitId = getThingNumber();
        final OmniLinkCmd omniCmd;
        if (id == CHANNEL_UNIT_ON_FOR_SECONDS || id == CHANNEL_UNIT_ON_FOR_MINUTES || id == CHANNEL_UNIT_ON_FOR_HOURS) {
            omniCmd = OmniLinkCmd.CMD_UNIT_ON;
        } else if (id == CHANNEL_UNIT_OFF_FOR_SECONDS || id == CHANNEL_UNIT_OFF_FOR_MINUTES
                || id == CHANNEL_UNIT_OFF_FOR_HOURS) {
            omniCmd = OmniLinkCmd.CMD_UNIT_OFF;
        } else {
            throw new IllegalArgumentException();
        }

        final int duration;
        if (id == CHANNEL_UNIT_ON_FOR_SECONDS || id == CHANNEL_UNIT_OFF_FOR_SECONDS) {
            duration = command.intValue();
        } else if (id == CHANNEL_UNIT_ON_FOR_MINUTES || id == CHANNEL_UNIT_OFF_FOR_MINUTES) {
            duration = command.intValue() + 100;
        } else if (id == CHANNEL_UNIT_ON_FOR_HOURS || id == CHANNEL_UNIT_OFF_FOR_HOURS) {
            duration = command.intValue() + 200;
        } else {
            throw new IllegalArgumentException();
        }

        sendOmnilinkCommand(omniCmd.getNumber(), duration, unitId);

    }

    private void handleUnitLevel(Command command) {

        final int unitId = getThingNumber();

        if (command instanceof OnOffType) {
            handleOnOff(command, unitId);
        } else {
            logger.warn("Received unexpected command type: {}", command);
        }

    }

    protected void handleOnOff(Command command, final int unitId) {
        final OmniLinkCmd omniCmd = command == OnOffType.ON ? OmniLinkCmd.CMD_UNIT_ON : OmniLinkCmd.CMD_UNIT_OFF;
        sendOmnilinkCommand(omniCmd.getNumber(), 0, unitId);

    }

    @Override
    public void updateChannels(UnitStatus unitStatus) {
        logger.debug("Handling status update{}", unitStatus);

        int status = unitStatus.getStatus();
        int level = 0;
        if (status == Status.UNIT_ON) {
            level = 100;
        } else if ((status >= Status.UNIT_SCENE_A) && (status <= Status.UNIT_SCENE_L)) {
            level = 100;
        } else if ((status >= Status.UNIT_LEVEL_0) && (status <= Status.UNIT_LEVEL_100)) {
            level = status - Status.UNIT_LEVEL_0;
        }

        State newState = PercentType.valueOf(Integer.toString(level));

        logger.debug("handle Unit Status Change to: {}", newState);
        updateState(CHANNEL_UNIT_LEVEL, newState);

    }

    @Override
    protected Optional<UnitStatus> retrieveStatus() {
        try {
            int unitId = getThingNumber();
            ObjectStatus objStatus = getOmnilinkBridgeHandler().requestObjectStatus(Message.OBJ_TYPE_UNIT, unitId,
                    unitId, false);
            return Optional.of((UnitStatus) objStatus.getStatuses()[0]);

        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Unexpected exception refreshing unit:", e);
            return Optional.empty();
        }
    }

    public void handleUnitStatus(UnitStatus unitStatus) {
        updateChannels(unitStatus);
    }

    private static class Status {
        private static final int UNIT_OFF = 0;
        private static final int UNIT_ON = 1;
        private static final int UNIT_SCENE_A = 2;
        private static final int UNIT_SCENE_L = 13;
        private static final int UNIT_LEVEL_0 = 100;
        private static final int UNIT_LEVEL_100 = 200;
    }

}
