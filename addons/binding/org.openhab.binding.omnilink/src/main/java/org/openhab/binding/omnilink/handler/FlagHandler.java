package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.UnitStatus;

public class FlagHandler extends AbstractOmnilinkHandler implements UnitHandler {

    public FlagHandler(Thing thing) {
        super(thing);
    }

    private Logger logger = LoggerFactory.getLogger(FlagHandler.class);

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        super.handleUpdate(channelUID, newState);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for channel:{}, command:{}", channelUID, command);

        int flagID = getThingNumber();
        if (command instanceof DecimalType) {
            logger.debug("updating omnilink flag change: {}, command: {}", channelUID, command);
            OmniLinkCmd omniCmd;
            int level = ((DecimalType) command).intValue();
            if (level == 0 || level == 100) {
                omniCmd = level == 0 ? OmniLinkCmd.CMD_UNIT_OFF : OmniLinkCmd.CMD_UNIT_ON;
            } else {
                omniCmd = OmniLinkCmd.CMD_UNIT_SET_COUNTER;
            }
            try {
                getOmnilinkBridgeHander().sendOmnilinkCommand(omniCmd.getNumber(), ((DecimalType) command).intValue(),
                        flagID);
            } catch (NumberFormatException | OmniInvalidResponseException | OmniUnknownMessageTypeException
                    | BridgeOfflineException e) {
                logger.debug("Could not send command to omnilink: {}", e);
            }
        } else if (command instanceof OnOffType) {
            logger.debug("updating omnilink flag change: {}, command: {}", channelUID, command);
            try {
                getOmnilinkBridgeHander().sendOmnilinkCommand(OmniLinkCmd.CMD_UNIT_SET_COUNTER.getNumber(),
                        OnOffType.ON.equals(command) ? 1 : 0, flagID);
            } catch (NumberFormatException | OmniInvalidResponseException | OmniUnknownMessageTypeException
                    | BridgeOfflineException e) {
                logger.debug("Could not send command to omnilink: {}", e);
            }
        } else {
            logger.warn("Must handle command: {}", command);
        }
    }

    @Override
    public void handleUnitStatus(UnitStatus unitStatus) {
        logger.debug("need to handle status update{}", unitStatus);
        updateState(OmnilinkBindingConstants.CHANNEL_FLAG,
                DecimalType.valueOf(Integer.toString(unitStatus.getStatus())));
        updateState(OmnilinkBindingConstants.CHANNEL_FLAGSWITCH,
                unitStatus.getStatus() == 0 ? OnOffType.OFF : OnOffType.ON);

    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channel linked: {}", channelUID);
        try {
            int flagId = getThingNumber();
            ObjectStatus objStatus = getOmnilinkBridgeHander().requestObjectStatus(Message.OBJ_TYPE_UNIT, flagId,
                    flagId, false);
            handleUnitStatus((UnitStatus) objStatus.getStatuses()[0]);

        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Unexpected exception refreshing unit:", e);
        }
    }
}
