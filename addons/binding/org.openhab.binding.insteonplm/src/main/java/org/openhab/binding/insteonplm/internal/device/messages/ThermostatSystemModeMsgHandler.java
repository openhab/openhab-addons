package org.openhab.binding.insteonplm.internal.device.messages;

import org.openhab.binding.insteonplm.internal.device.DeviceFeature;

/**
 * Convert system mode field to number 0...4. Insteon has two different
 * conventions for numbering, we use the one of the status update messages
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class ThermostatSystemModeMsgHandler extends NumberMsgHandler {
    ThermostatSystemModeMsgHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public int transform(int raw) {
        switch (raw) {
            case 0:
                return (0); // off
            case 1:
                return (3); // auto
            case 2:
                return (1); // heat
            case 3:
                return (2); // cool
            case 4:
                return (4); // program
            default:
                break;
        }
        return (4); // when in doubt assume to be in "program" mode
    }
}
