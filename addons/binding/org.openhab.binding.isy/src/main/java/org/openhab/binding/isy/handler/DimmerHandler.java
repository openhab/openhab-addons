package org.openhab.binding.isy.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.isy.IsyBindingConstants;
import org.openhab.binding.isy.config.IsyInsteonDeviceConfiguration;

public class DimmerHandler extends IsyHandler {

    public DimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        IsyBridgeHandler bridgeHandler = super.getBridgeHandler();

        IsyInsteonDeviceConfiguration test = getThing().getConfiguration().as(IsyInsteonDeviceConfiguration.class);
        logger.debug("handleCommand");
        String channelId = channelUID.getId();
        if (command instanceof PercentType) {
            if (IsyBindingConstants.CHANNEL_LIGHTLEVEL.equals(channelId)) {
                int commandValue = ((PercentType) command).intValue();
                if (commandValue == 0) {
                    bridgeHandler.getInsteonClient().changeNodeState("DOF", Integer.toString(0), test.address);
                } else {
                    bridgeHandler.getInsteonClient().changeNodeState("DON", Integer.toString(commandValue * 255 / 100),
                            test.address);
                }
            }
        } else if (command instanceof OnOffType) {
            if (command.equals(OnOffType.ON)) {
                bridgeHandler.getInsteonClient().turnDeviceOn(test.address);
            } else if (command.equals(OnOffType.OFF)) {
                bridgeHandler.getInsteonClient().turnDeviceOff(test.address);
            }
        }

    }

    @Override
    public void handleUpdate(Object... parameters) {

        logger.debug("handle update");
        if ("ST".equals(parameters[0])) {
            {
                int switchLevel = Integer.parseInt((String) parameters[1]);
                State updateValue;
                if (switchLevel > 0) {
                    updateValue = new PercentType(switchLevel * 100 / 255);
                } else {
                    updateValue = OnOffType.OFF;
                }
                updateState(IsyBindingConstants.CHANNEL_LIGHTLEVEL, updateValue);
            }
        }
    }
}
