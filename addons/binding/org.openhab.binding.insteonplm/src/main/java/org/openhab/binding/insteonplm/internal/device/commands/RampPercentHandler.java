package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.InsteonFlags;
import org.openhab.binding.insteonplm.internal.message.Message;
import org.openhab.binding.insteonplm.types.RampOnOffType;
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
    public void handleCommand(InsteonThingHandler conf, ChannelUID channelId, Command cmd) {
        try {
            RampOnOffType ramp = (RampOnOffType) cmd;
            double ramptime = ramp.getRamp();
            int level = ramp.intValue();
            if (level > 0) { // make light on message with given level
                level = getMaxLightLevel(conf, level);
                byte cmd2 = encode(ramptime, level);
                Message m = conf.getMessageFactory().makeStandardMessage(new InsteonFlags(), getOnCmd(), cmd2,
                        conf.getAddress());
                conf.enqueueMessage(m);
                logger.info("{}: sent msg to set {} to {} with {} second ramp time.", nm(), conf.getAddress(), level,
                        ramptime);
            } else { // switch off
                Message m = conf.getMessageFactory().makeStandardMessage(new InsteonFlags(), getOffCmd(), (byte) 0x00,
                        conf.getAddress());
                conf.enqueueMessage(m);
                logger.info("{}: sent msg to set {} to zero by switching off with {} ramp time.", nm(),
                        conf.getAddress(), ramptime);
            }
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
