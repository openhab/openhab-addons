package org.openhab.binding.insteonplm.internal.device.commands;

import org.openhab.binding.insteonplm.internal.device.DeviceFeature;

/**
 * Handler to set the fanlinc fan mode
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class FanLincFanCommandHandler extends NumberCommandHandler {
    FanLincFanCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public int transform(int cmd) {
        switch (cmd) {
            case 0:
                return (0x00); // fan off
            case 1:
                return (0x55); // fan low
            case 2:
                return (0xAA); // fan medium
            case 3:
                return (0xFF); // fan high
            default:
                break;
        }
        return (0x00); // all other modes are "off"
    }
}
