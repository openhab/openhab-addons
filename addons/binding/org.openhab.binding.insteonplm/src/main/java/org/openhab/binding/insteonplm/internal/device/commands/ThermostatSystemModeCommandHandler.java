package org.openhab.binding.insteonplm.internal.device.commands;

import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler to set the thermostat system mode
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class ThermostatSystemModeCommandHandler extends NumberCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ThermostatSystemModeCommandHandler.class);

    ThermostatSystemModeCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public int transform(int cmd) {
        switch (cmd) {
            case 0:
                return (0x09); // off
            case 1:
                return (0x04); // heat
            case 2:
                return (0x05); // cool
            case 3:
                return (0x06); // auto (aka manual auto)
            case 4:
                return (0x0A); // program (aka auto)
            default:
                break;
        }
        return (0x0A); // when in doubt go to program
    }
}
