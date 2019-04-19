/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.handler;

import java.util.Optional;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
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
 * @author craigh
 *
 */
public class UpbRoomHandler extends UnitHandler {

    private Logger logger = LoggerFactory.getLogger(UpbRoomHandler.class);

    public UpbRoomHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand, channel id: {}, command: {}", channelUID, command);

        String channelID = channelUID.getId();

        int unitNum = getThingNumber();

        if (channelID.startsWith("scene") && OnOffType.ON.equals(command)) {

            int param1 = 0;
            int linkNum = -1;
            switch (channelID) {
                case "scene_a":
                    linkNum = 0;
                    break;
                case "scene_b":
                    linkNum = 1;
                    break;
                case "scene_c":
                    linkNum = 2;
                    break;
                case "scene_d":
                    linkNum = 3;
                    break;
                default:
                    logger.error("Unexpected scene: {}", channelUID);
            }
            if (linkNum > -1) {
                int roomNum = (unitNum + 7) / 8;
                int param2 = ((roomNum * 6) - 3) + linkNum;

                sendOmnilinkCommand(OmniLinkCmd.CMD_UNIT_UPB_LINK_ON.getNumber(), param1, param2);

            }
        } else if (OmnilinkBindingConstants.CHANNEL_ROOM_SWITCH.equals(channelID)) {
            super.handleOnOff(command, unitNum);
        } else if (OmnilinkBindingConstants.CHANNEL_ROOM_ON.equals(channelID) && OnOffType.ON.equals(command)) {
            int cmd = OmniLinkCmd.CMD_UNIT_ON.getNumber();
            sendOmnilinkCommand(cmd, 0, unitNum);
        } else if (OmnilinkBindingConstants.CHANNEL_ROOM_OFF.equals(channelID) && OnOffType.ON.equals(command)) {
            sendOmnilinkCommand(OmniLinkCmd.CMD_UNIT_OFF.getNumber(), 0, unitNum);

        } else if (OmnilinkBindingConstants.CHANNEL_ROOM_STATE.equals(channelID)) {
            int cmd = -1;
            int param2 = -1;
            if (command instanceof DecimalType) {
                int cmdValue = ((DecimalType) command).intValue();
                switch (cmdValue) {
                    case 0:
                        cmd = OmniLinkCmd.CMD_UNIT_OFF.getNumber();
                        param2 = unitNum;
                        break;
                    case 1:
                        cmd = OmniLinkCmd.CMD_UNIT_ON.getNumber();
                        param2 = unitNum;
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        cmd = OmniLinkCmd.CMD_UNIT_UPB_LINK_ON.getNumber();
                        // little magic with link #. 0 and 1 are off, on. So A ends up being 2, but omnilink expects
                        // offset of 0. Thats why subtracting the 2
                        int roomNum = (unitNum + 7) / 8;
                        param2 = ((roomNum * 6) - 3) + cmdValue - 2;
                        break;

                }
                if (cmd > -1 && param2 > -1) {
                    sendOmnilinkCommand(cmd, 0, param2);

                } else {
                    logger.debug("Not sending message for scene, cmd: {}, param2: {}", cmd, param2);
                }
            }
        }
        // cmd = OmniLinkCmd.CMD_UNIT_UPB_LINK_ON.getNumber();

        // getOmnilinkBridgeHander().sendOmnilinkCommand(OmniLinkCmd.CMD_UNIT_UPB_LINK_ON, param1, param2);
    }

    @Override
    public void updateChannels(UnitStatus unitStatus) {

        updateState(OmnilinkBindingConstants.CHANNEL_ROOM_STATE, new DecimalType(unitStatus.getStatus()));
        // the on/off and scene buttons are only 1 can be on
        if (unitStatus.getStatus() != 0) {
            updateState(OmnilinkBindingConstants.CHANNEL_ROOM_OFF, OnOffType.OFF);
        }
        if (unitStatus.getStatus() != 1) {
            updateState(OmnilinkBindingConstants.CHANNEL_ROOM_ON, OnOffType.OFF);
        }
        if (unitStatus.getStatus() != 2) {
            updateState(OmnilinkBindingConstants.CHANNEL_ROOM_SCENE_A, OnOffType.OFF);
        }
        if (unitStatus.getStatus() != 3) {
            updateState(OmnilinkBindingConstants.CHANNEL_ROOM_SCENE_B, OnOffType.OFF);
        }
        if (unitStatus.getStatus() != 4) {
            updateState(OmnilinkBindingConstants.CHANNEL_ROOM_SCENE_C, OnOffType.OFF);
        }
        if (unitStatus.getStatus() != 5) {
            updateState(OmnilinkBindingConstants.CHANNEL_ROOM_SCENE_D, OnOffType.OFF);
        }

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

    @Override
    public void handleUnitStatus(UnitStatus unitStatus) {
        updateChannels(unitStatus);
    }

}
