package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.InsteonThing;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler to set the ramp for a percentage.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class RampPercentHandler extends RampCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RampPercentHandler.class);

    RampPercentHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, Command cmd, InsteonThing dev) {
        try {
            PercentType pc = (PercentType) cmd;
            double ramptime = getRampTime(conf, 0);
            int level = pc.intValue();
            if (level > 0) { // make light on message with given level
                level = getMaxLightLevel(conf, level);
                byte cmd2 = encode(ramptime, level);
                Msg m = dev.makeStandardMessage((byte) 0x0f, getOnCmd(), cmd2);
                dev.enqueueMessage(m, getFeature());
                logger.info("{}: sent msg to set {} to {} with {} second ramp time.", nm(), dev.getAddress(), level,
                        ramptime);
            } else { // switch off
                Msg m = dev.makeStandardMessage((byte) 0x0f, getOffCmd(), (byte) 0x00);
                dev.enqueueMessage(m, getFeature());
                logger.info("{}: sent msg to set {} to zero by switching off with {} ramp time.", nm(),
                        dev.getAddress(), ramptime);
            }
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
