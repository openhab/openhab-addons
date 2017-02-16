package org.openhab.binding.insteonplm.internal.device.messages;

import org.openhab.binding.insteonplm.internal.device.DeviceFeature;

/**
 * Handle reply to fan mode change command
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class ThermostatFanModeReplyHandler extends NumberMsgHandler {
    ThermostatFanModeReplyHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public int transform(int raw) {
        switch (raw) {
            case 0x08:
                return (0); // auto
            case 0x07:
                return (1); // always on
            default:
                break;
        }
        return (0); // when in doubt assume to be auto mode
    }
}
