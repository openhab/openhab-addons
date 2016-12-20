package org.openhab.binding.isy.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.isy.IsyBindingConstants;
import org.openhab.binding.isy.config.IsyInsteonDeviceConfiguration;
import org.openhab.binding.isy.internal.InsteonAddress;

public class GarageDoorHandler extends IsyHandler {

    @Override
    public void handleUpdate(Object... parameters) {
        InsteonAddress insteonAddress = new InsteonAddress((String) parameters[2]);
        int deviceId = insteonAddress.getDeviceId();
        logger.debug("device id: " + deviceId);

        OnOffType updateValue;
        if ("ST".equals(parameters[0])) {
            if (deviceId == 1) {
                if (Integer.parseInt(parameters[1].toString()) > 0) {
                    updateValue = OnOffType.ON;
                } else {
                    updateValue = OnOffType.OFF;
                }
                updateState(IsyBindingConstants.CHANNEL_GARAGE_SENSOR, updateValue);

            } else if (deviceId == 2) {
                logger.warn("Not handling device id 2, i think relay");
                if (Integer.parseInt(parameters[1].toString()) > 0) {
                    updateValue = OnOffType.ON;
                } else {
                    updateValue = OnOffType.OFF;
                }
                updateState(IsyBindingConstants.CHANNEL_GARAGE_CONTACT, updateValue);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        IsyBridgeHandler bridgeHandler = super.getBridgeHandler();

        IsyInsteonDeviceConfiguration test = getThing().getConfiguration().as(IsyInsteonDeviceConfiguration.class);
        logger.debug("handleCommand");
        if (command.equals(OnOffType.ON)) {
            bridgeHandler.getInsteonClient().turnDeviceOn(test.address);
        } else if (command.equals(OnOffType.OFF)) {
            bridgeHandler.getInsteonClient().turnDeviceOff(test.address);
        } else if (command.equals(RefreshType.REFRESH)) {
            logger.debug("should retrieve state");
        }
    }

    public GarageDoorHandler(Thing thing) {
        super(thing);
    }

}
