package org.openhab.binding.insteonplm.internal.device.messages;

import org.openhab.binding.insteonplm.internal.device.DeviceFeature;

/**
 * Handle reply to system mode change command
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class ThermostatSystemModeReplyHandler extends NumberMsgHandler {
    ThermostatSystemModeReplyHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public int transform(int raw) {
        switch (raw) {
            case 0x09:
                return (0); // off
            case 0x04:
                return (1); // heat
            case 0x05:
                return (2); // cool
            case 0x06:
                return (3); // auto
            case 0x0A:
                return (4); // program
            default:
                break;
        }
        return (4); // when in doubt assume to be in "program" mode
    }
}
