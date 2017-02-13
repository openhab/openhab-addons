package org.openhab.binding.insteonplm.internal.device.commands;

import org.openhab.binding.insteonplm.internal.device.DeviceFeature;

/**
 * Handler to set the thermostat fan mode
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class ThermostatFanModeCommandHandler extends NumberCommandHandler {
    ThermostatFanModeCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public int transform(int cmd) {
        switch (cmd) {
            case 0:
                return (0x08); // fan mode auto
            case 1:
                return (0x07); // fan always on
            default:
                break;
        }
        return (0x08); // when in doubt go auto mode
    }
}
