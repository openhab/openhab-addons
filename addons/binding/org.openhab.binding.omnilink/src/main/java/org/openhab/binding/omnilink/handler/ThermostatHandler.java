package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ThermostatStatus;

public class ThermostatHandler extends AbstractOmnilinkHandler {

    private Logger logger = LoggerFactory.getLogger(ThermostatHandler.class);

    public ThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub
    }

    public void handleThermostatStatus(ThermostatStatus status) {
        logger.debug("Thermostat Status {}", status);

    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channel linked: {}", channelUID);
        String[] channelParts = channelUID.getAsString().split(UID.SEPARATOR);
        int unitId = Integer.parseInt(channelParts[2]);

        try {
            ObjectStatus objStatus = getOmnilinkBridgeHander().requestObjectStatus(Message.OBJ_TYPE_THERMO, unitId,
                    unitId, false);
            handleThermostatStatus((ThermostatStatus) objStatus.getStatuses()[0]);

        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Unexpected exception refreshing unit:", e);
        }

    }

}
