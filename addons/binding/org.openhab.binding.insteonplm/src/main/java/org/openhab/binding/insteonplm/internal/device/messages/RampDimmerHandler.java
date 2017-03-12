package org.openhab.binding.insteonplm.internal.device.messages;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.MessageHandler;
import org.openhab.binding.insteonplm.internal.message.modem.StandardMessageReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This message handler processes replies to Ramp ON/OFF commands.
 * Currently, it's been tested for the 2672-222 LED Bulb. Other
 * devices may use a different pair of commands (0x2E, 0x2F). This
 * handler and the command handler will need to be extended to support
 * those devices.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class RampDimmerHandler extends MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(RampDimmerHandler.class);
    private byte onCmd;
    private byte offCmd;

    RampDimmerHandler(DeviceFeature p) {
        super(p);
        // Can't process parameters here because they are set after constructor is invoked.
        // Unfortunately, this means we can't declare the onCmd, offCmd to be final.
    }

    public void setOnCmd(String on) {
        try {
            onCmd = Byte.valueOf(on);
        } catch (NumberFormatException e) {
            logger.error("Unable to parse {}", e, on);
        }

    }

    public void setOffCmd(String off) {
        try {
            offCmd = Byte.valueOf(off);
        } catch (NumberFormatException e) {
            logger.error("Unable to parse {}", e, off);
        }

    }

    @Override
    public void handleMessage(InsteonThingHandler handler, int group, StandardMessageReceived msg, Channel f) {
        if (msg.getCmd1().getCmd1() == onCmd) {
            int level = getLevel(msg);
            logger.info("{}: device {} was switched on using ramp to level {}.", nm(), handler.getAddress(), level);
            if (level == 100) {
                handler.updateFeatureState(f, OnOffType.ON);
            } else {
                // The publisher will convert an ON at level==0 to an OFF.
                // However, this is not completely accurate since a ramp
                // off at level == 0 may not turn off the dimmer completely
                // (if I understand the Insteon docs correctly). In any
                // case,
                // it would be an odd scenario to turn ON a light at level
                // == 0
                // rather than turn if OFF.
                handler.updateFeatureState(f, new PercentType(level));
            }
        } else if (msg.getCmd1().getCmd1() == offCmd) {
            logger.info("{}: device {} was switched off using ramp.", nm(), handler.getAddress());
            handler.updateFeatureState(f, OnOffType.OFF);
        }
    }

    private int getLevel(StandardMessageReceived msg) {
        byte cmd2 = msg.getCmd2();
        return (int) Math.round(((cmd2 >> 4) & 0x0f) * (100 / 15d));
    }
}
