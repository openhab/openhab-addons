package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.openhab.binding.omnilink.protocol.AreaAlarmStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.statuses.AreaStatus;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class AreaHandler extends AbstractOmnilinkHandler {
    private Logger logger = LoggerFactory.getLogger(AreaHandler.class);

    public AreaHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand: {}, command: {}", channelUID, command);
        String[] channelParts = channelUID.getAsString().split(UID.SEPARATOR);
        if (command instanceof RefreshType) {
            Futures.addCallback(getOmnilinkBridgeHander().getAreaStatus(Integer.parseInt(channelParts[2])),
                    new FutureCallback<AreaStatus>() {

                        @Override
                        public void onFailure(Throwable arg0) {
                            logger.error("Failure getting status", arg0);
                        }

                        @Override
                        public void onSuccess(AreaStatus status) {
                            logger.debug("handle area status: {}", status);
                            updateState(new ChannelUID(thing.getUID(), OmnilinkBindingConstants.CHANNEL_AREAALARM),
                                    new StringType(AreaAlarmStatus.values()[status.getMode()].toString()));
                        }
                    });
        }
    }

    public void handleAreaEvent(AreaStatus status) {
        updateState(new ChannelUID(thing.getUID(), OmnilinkBindingConstants.CHANNEL_AREAALARM),
                new StringType(AreaAlarmStatus.values()[status.getMode()].toString()));

    }
}
