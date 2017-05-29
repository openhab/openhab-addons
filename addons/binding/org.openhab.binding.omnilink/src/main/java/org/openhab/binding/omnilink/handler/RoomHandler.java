package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.statuses.UnitStatus;

public class RoomHandler extends AbstractOmnilinkHandler implements UnitHandler {

    private Logger logger = LoggerFactory.getLogger(RoomHandler.class);

    public RoomHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand, channel id: {}, command: {}", channelUID, command);
        String[] channelParts = channelUID.getAsString().split(UID.SEPARATOR);
        int roomNum = Integer.parseInt(channelParts[2]);

        if (RefreshType.REFRESH.equals(command)) {
            logger.debug("Must implement refresh: {} ", channelUID);
            return;
        }
        if (channelParts[3].startsWith("scene") && OnOffType.ON.equals(command)) {

            int param1 = 0;
            int linkNum = -1;
            switch (channelParts[3]) {
                case "scenea":
                    linkNum = 0;
                    break;
                case "sceneb":
                    linkNum = 1;
                    break;
                case "scenec":
                    linkNum = 2;
                    break;
                case "scened":
                    linkNum = 3;
                    break;
                default:
                    logger.error("Unexpected scene: {}", channelUID);
            }
            if (linkNum > -1) {
                int param2 = ((roomNum * 6) - 3) + linkNum;
                getOmnilinkBridgeHander().sendOmnilinkCommand(OmniLinkCmd.CMD_UNIT_UPB_LINK_ON.getNumber(), param1,
                        param2);
            }
        } else if (OmnilinkBindingConstants.CHANNEL_ROOM_SWITCH.equals(channelParts[3])) {
            int cmd;
            if (OnOffType.ON.equals(command)) {
                cmd = OmniLinkCmd.CMD_UNIT_ON.getNumber();
            } else {
                cmd = OmniLinkCmd.CMD_UNIT_OFF.getNumber();
            }
            getOmnilinkBridgeHander().sendOmnilinkCommand(cmd, 0, roomNum);
        } else if (OmnilinkBindingConstants.CHANNEL_ROOM_ON.equals(channelParts[3]) && OnOffType.ON.equals(command)) {
            int cmd;
            cmd = OmniLinkCmd.CMD_UNIT_ON.getNumber();
            getOmnilinkBridgeHander().sendOmnilinkCommand(cmd, 0, roomNum);
        } else if (OmnilinkBindingConstants.CHANNEL_ROOM_OFF.equals(channelParts[3]) && OnOffType.ON.equals(command)) {
            getOmnilinkBridgeHander().sendOmnilinkCommand(OmniLinkCmd.CMD_UNIT_OFF.getNumber(), 0, roomNum);
        } else if (OmnilinkBindingConstants.CHANNEL_ROOM_STATE.equals(channelParts[3])) {
            int cmd = -1;
            int param2 = -1;
            if (command instanceof DecimalType) {
                int cmdValue = ((DecimalType) command).intValue();
                switch (cmdValue) {
                    case 0:
                        cmd = OmniLinkCmd.CMD_UNIT_OFF.getNumber();
                        param2 = roomNum;
                        break;
                    case 1:
                        cmd = OmniLinkCmd.CMD_UNIT_ON.getNumber();
                        param2 = roomNum;
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        // little magic with link #. 0 and 1 are off, on. So A ends up being 2, but omnilink expects
                        // offset of 0. Thats why subtracting the 2
                        param2 = ((roomNum * 6) - 3) + cmdValue - 2;
                        break;

                }
                if (cmd > -1 && param2 > -1) {
                    getOmnilinkBridgeHander().sendOmnilinkCommand(cmd, 0, param2);
                }
            }
        }
        // cmd = OmniLinkCmd.CMD_UNIT_UPB_LINK_ON.getNumber();

        // getOmnilinkBridgeHander().sendOmnilinkCommand(OmniLinkCmd.CMD_UNIT_UPB_LINK_ON, param1, param2);
    }

    @Override
    public void handleUnitStatus(UnitStatus unitStatus) {
        logger.debug("need to handle status update{}", unitStatus);
        updateState(OmnilinkBindingConstants.CHANNEL_ROOM_SWITCH,
                unitStatus.getStatus() == UNIT_ON ? OnOffType.ON : OnOffType.OFF);
        updateState(OmnilinkBindingConstants.CHANNEL_ROOM_STATE, new DecimalType(unitStatus.getStatus()));

    }

}
