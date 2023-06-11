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
package org.openhab.binding.omnilink.internal.handler.units;

import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.handler.UnitHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedUnitStatus;

/**
 * The {@link UpbRoomHandler} defines some methods that are used to
 * interface with an OmniLink UPB Room. This by extension also defines the
 * OmniPro UPB Room thing that openHAB will be able to pick up and interface with.
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public class UpbRoomHandler extends UnitHandler {
    private final Logger logger = LoggerFactory.getLogger(UpbRoomHandler.class);
    private final int thingID = getThingNumber();
    public @Nullable String number;

    public UpbRoomHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for channel: {}, command: {}", channelUID, command);

        if (command instanceof RefreshType) {
            retrieveStatus().ifPresentOrElse(this::updateChannels, () -> updateStatus(ThingStatus.OFFLINE,
                    ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Received null status update!"));
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_ROOM_SCENE_A:
            case CHANNEL_ROOM_SCENE_B:
            case CHANNEL_ROOM_SCENE_C:
            case CHANNEL_ROOM_SCENE_D:
                if (command instanceof OnOffType) {
                    handleRoomScene(channelUID, (OnOffType) command);
                } else {
                    logger.debug("Invalid command: {}, must be OnOffType", command);
                }
                break;
            case CHANNEL_ROOM_SWITCH:
                if (command instanceof OnOffType) {
                    super.handleOnOff(channelUID, (OnOffType) command);
                } else {
                    logger.debug("Invalid command: {}, must be OnOffType", command);
                }
                break;
            case CHANNEL_ROOM_STATE:
                if (command instanceof DecimalType) {
                    handleRoomState(channelUID, (DecimalType) command);
                } else {
                    logger.debug("Invalid command: {}, must be DecimalType", command);
                }
                break;
            default:
                logger.debug("Unknown channel for UPB Room thing: {}", channelUID);
                super.handleCommand(channelUID, command);
        }
    }

    private void handleRoomScene(ChannelUID channelUID, OnOffType command) {
        logger.debug("handleRoomScene called for channel: {}, command: {}", channelUID, command);
        int linkNum;

        switch (channelUID.getId()) {
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
                logger.warn("Unexpected UPB Room scene: {}", channelUID);
                return;
        }
        int roomNum = (thingID + 7) / 8;
        int param2 = ((roomNum * 6) - 3) + linkNum;
        sendOmnilinkCommand(OnOffType.ON.equals(command) ? CommandMessage.CMD_UNIT_UPB_LINK_ON
                : CommandMessage.CMD_UNIT_UPB_LINK_OFF, 0, param2);
    }

    private void handleRoomState(ChannelUID channelUID, DecimalType command) {
        logger.debug("handleRoomState called for channel: {}, command: {}", channelUID, command);
        final int cmdValue = command.intValue();
        int cmd;
        int param2;

        switch (cmdValue) {
            case 0:
                cmd = CommandMessage.CMD_UNIT_OFF;
                param2 = thingID;
                break;
            case 1:
                cmd = CommandMessage.CMD_UNIT_ON;
                param2 = thingID;
                break;
            case 2:
            case 3:
            case 4:
            case 5:
                cmd = CommandMessage.CMD_UNIT_UPB_LINK_ON;
                /*
                 * A little magic with the link #'s: 0 and 1 are off and on, respectively.
                 * So A ends up being 2, but OmniLink Protocol expects an offset of 0. That
                 * is why we subtract the 2.
                 */
                int roomNum = (thingID + 7) / 8;
                param2 = ((roomNum * 6) - 3) + cmdValue - 2;
                break;
            default:
                logger.warn("Unexpected UPB Room state: {}", Integer.toString(cmdValue));
                return;
        }

        sendOmnilinkCommand(cmd, 0, param2);
    }

    @Override
    public void updateChannels(ExtendedUnitStatus status) {
        logger.debug("updateChannels called for UPB Room status: {}", status);
        int unitStatus = status.getStatus();

        updateState(CHANNEL_ROOM_STATE, new DecimalType(unitStatus));
        updateState(CHANNEL_ROOM_SWITCH, OnOffType.from(unitStatus == 1));
        updateState(CHANNEL_ROOM_SCENE_A, OnOffType.from(unitStatus == 2));
        updateState(CHANNEL_ROOM_SCENE_B, OnOffType.from(unitStatus == 3));
        updateState(CHANNEL_ROOM_SCENE_C, OnOffType.from(unitStatus == 4));
        updateState(CHANNEL_ROOM_SCENE_D, OnOffType.from(unitStatus == 5));
    }
}
