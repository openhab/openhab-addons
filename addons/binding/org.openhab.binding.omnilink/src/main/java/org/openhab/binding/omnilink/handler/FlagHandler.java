package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.statuses.UnitStatus;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

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
        final String[] channelParts = channelUID.getAsString().split(UID.SEPARATOR);
        if (command instanceof RefreshType) {
            logger.debug("Handling refresh, flag id: {}", channelParts[2]);
            Futures.addCallback(getOmnilinkBridgeHander().getUnitStatus(Integer.parseInt(channelParts[2])),
                    new FutureCallback<UnitStatus>() {
                        @Override
                        public void onFailure(Throwable arg0) {
                            logger.error("Failed retrieving status for flag #: {}", channelParts[2], arg0);
                        }

                        @Override
                        public void onSuccess(UnitStatus status) {
                            handleUnitStatus(status);
                        }
                    });
        } else if (command instanceof DecimalType) {
            logger.debug("updating omnilink flag change: {}, command: {}", channelUID, command);
            OmniLinkCmd omniCmd;
            int level = ((DecimalType) command).intValue();
            if (level == 0 || level == 100) {
                omniCmd = level == 0 ? OmniLinkCmd.CMD_UNIT_OFF : OmniLinkCmd.CMD_UNIT_ON;
            } else {
                omniCmd = OmniLinkCmd.CMD_UNIT_SET_COUNTER;
            }
            getOmnilinkBridgeHander().sendOmnilinkCommand(omniCmd.getNumber(), ((DecimalType) command).intValue(),
                    Integer.parseInt(channelParts[2]));
        } else if (command instanceof OnOffType) {
            logger.debug("updating omnilink flag change: {}, command: {}", channelUID, command);
            getOmnilinkBridgeHander().sendOmnilinkCommand(OmniLinkCmd.CMD_UNIT_SET_COUNTER.getNumber(),
                    OnOffType.ON.equals(command) ? 1 : 0, Integer.parseInt(channelParts[2]));
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
}
