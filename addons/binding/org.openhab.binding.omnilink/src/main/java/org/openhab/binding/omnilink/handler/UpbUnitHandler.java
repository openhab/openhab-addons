/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
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
public class UpbUnitHandler extends AbstractOmnilinkStatusHandler<UnitStatus> implements UnitHandler {

    @SuppressWarnings("serial")
    private final static Map<Type, OmniLinkCmd> sCommandMappingMap = Collections
            .unmodifiableMap(new HashMap<Type, OmniLinkCmd>() {
                {
                    put(IncreaseDecreaseType.INCREASE, OmniLinkCmd.CMD_UNIT_UPB_BRIGHTEN_STEP_1);
                    put(IncreaseDecreaseType.DECREASE, OmniLinkCmd.CMD_UNIT_UPB_DIM_STEP_1);
                    put(OnOffType.ON, OmniLinkCmd.CMD_UNIT_ON);
                    put(OnOffType.OFF, OmniLinkCmd.CMD_UNIT_OFF);
                }

            });

    public UpbUnitHandler(Thing thing) {
        super(thing);
    }

    private Logger logger = LoggerFactory.getLogger(UpbUnitHandler.class);

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.debug("handleCommand called");
        OmniLinkCmd omniCmd;
        int unitId = getThingNumber();

        if (command instanceof PercentType) {
            int lightLevel = ((PercentType) command).intValue();
            if (lightLevel == 0 || lightLevel == 100) {
                omniCmd = lightLevel == 0 ? OmniLinkCmd.CMD_UNIT_OFF : OmniLinkCmd.CMD_UNIT_ON;
            } else {
                omniCmd = OmniLinkCmd.CMD_UNIT_PERCENT;
            }
            try {
                getOmnilinkBridgeHander().sendOmnilinkCommand(omniCmd.getNumber(), ((PercentType) command).intValue(),
                        unitId);
            } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
                logger.debug("received exception handling command", e);
            }

        } else {
            omniCmd = sCommandMappingMap.get(command);
            try {
                getOmnilinkBridgeHander().sendOmnilinkCommand(omniCmd.getNumber(), 0, unitId);
            } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
                logger.debug("received exception handling command", e);
            }
        }

    }

    @Override
    public void updateChannels(UnitStatus unitStatus) {

        // TODO is dimmer, or just simple switch
        // assuming dimmer right now.
        logger.debug("Handling status update{}", unitStatus);
        int status = unitStatus.getStatus();
        int level = 0;
        if (status == UNIT_ON) {
            level = 100;
        } else if ((status >= UNIT_SCENE_A) && (status <= UNIT_SCENE_L)) {
            level = 100;
        } else if ((status >= UNIT_LEVEL_0) && (status <= UNIT_LEVEL_100)) {
            level = status - UNIT_LEVEL_0;
        }

        State newState = PercentType.valueOf(Integer.toString(level));

        logger.debug("handle Unit Status Change to: {}", newState);
        updateState(OmnilinkBindingConstants.CHANNEL_UNIT_LEVEL, newState);

    }

    @Override
    protected Optional<UnitStatus> retrieveStatus() {
        try {
            int unitId = getThingNumber();
            ObjectStatus objStatus = getOmnilinkBridgeHander().requestObjectStatus(Message.OBJ_TYPE_UNIT, unitId,
                    unitId, false);
            return Optional.of((UnitStatus) objStatus.getStatuses()[0]);

        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Unexpected exception refreshing unit:", e);
            return Optional.empty();
        }
    }

    @Override
    public void handleUnitStatus(UnitStatus unitStatus) {
        updateChannels(unitStatus);
    }

}
