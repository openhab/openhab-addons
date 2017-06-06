package org.openhab.binding.insteonplm.internal.device.messages;

import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle reply to fanlinc fan speed change command
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class FanLincFanReplyHandler extends NumberMsgHandler {
    private static final Logger logger = LoggerFactory.getLogger(FanLincFanReplyHandler.class);

    FanLincFanReplyHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public int transform(int raw) {
        switch (raw) {
            case 0x00:
                return (0); // off
            case 0x55:
                return (1); // low
            case 0xAA:
                return (2); // medium
            case 0xFF:
                return (3); // high
            default:
                logger.warn("fanlinc got unexpected level: {}", raw);
        }
        return (0); // when in doubt assume to be off
    }
}
