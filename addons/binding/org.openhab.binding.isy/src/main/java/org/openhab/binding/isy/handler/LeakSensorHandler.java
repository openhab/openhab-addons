package org.openhab.binding.isy.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.isy.IsyBindingConstants;
import org.openhab.binding.isy.internal.InsteonAddress;

public class LeakSensorHandler extends IsyHandler {

    public LeakSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleUpdate(Object... parameters) {
        InsteonAddress insteonAddress = new InsteonAddress((String) parameters[2]);
        int deviceId = insteonAddress.getDeviceId();
        String channel = null;
        // dry 1, wet 2, heartbeat 4
        switch (deviceId) {
            case 1:
                channel = IsyBindingConstants.CHANNEL_LEAK_DRY;
                break;
            case 2:
                channel = IsyBindingConstants.CHANNEL_LEAK_WET;
                break;
            case 4:
                channel = IsyBindingConstants.CHANNEL_LEAK_HEARTBEAT;
                break;
            default:
                logger.error("Invalid device id: " + parameters[2]);
        }
        logger.debug("device id: " + deviceId);

        OnOffType updateValue;
        if ("ST".equals(parameters[0])) {
            if (Integer.parseInt(parameters[1].toString()) > 0) {
                updateValue = OnOffType.ON;
            } else {
                updateValue = OnOffType.OFF;
            }
            updateState(channel, updateValue);

        }
    }

}
