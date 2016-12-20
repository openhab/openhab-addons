package org.openhab.binding.isy.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.isy.IsyBindingConstants;
import org.openhab.binding.isy.internal.InsteonAddress;

public class MotionHandler extends IsyHandler {

    public MotionHandler(Thing thing) {
        super(thing);
        super.mDeviceidToChannelMap.put(1, IsyBindingConstants.CHANNEL_MOTION_MOTION);
        super.mDeviceidToChannelMap.put(2, IsyBindingConstants.CHANNEL_MOTION_DUSK);
        super.mDeviceidToChannelMap.put(3, IsyBindingConstants.CHANNEL_MOTION_BATTERY);
    }

    @Override
    public void handleUpdate(Object... parameters) {
        InsteonAddress insteonAddress = new InsteonAddress((String) parameters[2]);
        int deviceId = insteonAddress.getDeviceId();
        if ("ST".equals(parameters[0])) {
            updateState(mDeviceidToChannelMap.get(deviceId),
                    IsyHandler.statusValuetoState(Integer.parseInt((String) parameters[1])));
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
    }

}
