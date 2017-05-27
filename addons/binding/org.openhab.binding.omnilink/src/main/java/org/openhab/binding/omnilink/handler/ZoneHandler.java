package org.openhab.binding.omnilink.handler;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.smarthome.core.types.RefreshType;

import com.digitaldan.jomnilinkII.MessageTypes.statuses.ZoneStatus;


public class ZoneHandler extends AbstractOmnilinkHandler {
    private Logger logger = LoggerFactory.getLogger(ZoneHandler.class);

    public ZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("must handle command");
        if (command instanceof RefreshType) {
          String[] channelParts = channelUID.getAsString().split(UID.SEPARATOR);
          logger.debug("Zone '{}' got REFRESH command", thing.getLabel());
            Futures.addCallback(getOmnilinkBridgeHander().getZoneStatus(Integer.parseInt(channelParts[2])),
                    new FutureCallback<ZoneStatus>() {
                        @Override
                        public void onFailure(Throwable arg0) {
                            logger.error("Error refreshing unit status", arg0);
                        }

                        @Override
                        public void onSuccess(ZoneStatus status) {
                            handleZoneStatus(status);
                        }
                    });
        }
    }

    public void handleZoneStatus(ZoneStatus status) {
        State newState = status.getStatus() == 1 ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
        logger.debug("handle Zone Status Change to: " + newState);
        updateState(OmnilinkBindingConstants.CHANNEL_CONTACTSENSOR, newState);

    }
}
