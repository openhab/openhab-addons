package org.openhab.binding.isy.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.isy.IsyBindingConstants;
import org.openhab.binding.isy.config.IsyInsteonDeviceConfiguration;

public class SwitchHandler extends IsyHandler {

    public SwitchHandler(Thing thing) {
        super(thing);
        // super.mDeviceidToChannelMap.put(1, IsyBindingConstants.CHANNEL_SWITCH);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        IsyBridgeHandler bridgeHandler = super.getBridgeHandler();
        IsyInsteonDeviceConfiguration test = getThing().getConfiguration().as(IsyInsteonDeviceConfiguration.class);
        // isy needs device id appended to address
        String isyAddress = test.address + " " + getDeviceIdForChannel(channelUID.getId());
        logger.debug("handleCommand");
        if (command.equals(OnOffType.ON)) {
            bridgeHandler.getInsteonClient().turnDeviceOn(isyAddress);
        } else if (command.equals(OnOffType.OFF)) {
            bridgeHandler.getInsteonClient().turnDeviceOff(isyAddress);
        } else if (command.equals(RefreshType.REFRESH)) {
            logger.debug("should retrieve state");
        }

    }

    @Override
    public void handleUpdate(Object... parameters) {
        if ("ST".equals(parameters[0])) {
            {
                int switchLevel = Integer.parseInt((String) parameters[1]);
                OnOffType updateValue;
                if (switchLevel > 0) {
                    updateValue = OnOffType.ON;
                } else {
                    updateValue = OnOffType.OFF;
                }
                updateState(IsyBindingConstants.CHANNEL_SWITCH, updateValue);
            }
        }
    }

}
