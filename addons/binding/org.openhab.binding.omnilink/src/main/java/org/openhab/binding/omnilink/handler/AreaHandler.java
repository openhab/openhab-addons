package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.openhab.binding.omnilink.protocol.AreaAlarmStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.statuses.AreaStatus;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class AreaHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(AreaHandler.class);

    public AreaHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand: {}, command: {}", channelUID, command);
        String[] channelParts = channelUID.getAsString().split(UID.SEPARATOR);
        if (command instanceof RefreshType) {
            Futures.addCallback(
                    ((OmnilinkBridgeHandler) getBridge().getHandler()).getAreaStatus(Integer.parseInt(channelParts[2])),
                    new FutureCallback<AreaStatus>() {

                        @Override
                        public void onFailure(Throwable arg0) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void onSuccess(AreaStatus status) {
                            logger.debug("must handle area status: {}", status);
                            updateState(new ChannelUID(thing.getUID(), OmnilinkBindingConstants.CHANNEL_AREAALARM),
                                    new StringType(AreaAlarmStatus.values()[status.getMode()].toString()));
                            // handleUnitStatus(status);
                        }
                    });
        }
    }

    public void handleAreaEvent(AreaStatus status) {
        updateState(new ChannelUID(thing.getUID(), OmnilinkBindingConstants.CHANNEL_AREAALARM),
                new StringType(AreaAlarmStatus.values()[status.getMode()].toString()));

    }
}
