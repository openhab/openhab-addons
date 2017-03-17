package org.openhab.binding.insteonplm.internal.device.commands;

import java.util.Map;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.modem.SendInsteonMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler to set the ramp for an on/off command.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class RampOnOffCommandHandler extends RampCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RampOnOffCommandHandler.class);

    RampOnOffCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, ChannelUID channelId, Command cmd) {
        try {
            if (cmd == OnOffType.ON) {
                double ramptime = conf.getRampTime();
                int ramplevel = getRampLevel(conf, 100);
                byte cmd2 = encode(ramptime, ramplevel);
                SendInsteonMessage m = new SendInsteonMessage(conf.getAddress(), conf.getDefaultFlags(), getOnCmd(),
                        cmd2);
                conf.enqueueMessage(m);
                logger.info("{}: sent ramp on to switch {} time {} level {} cmd1 {}", nm(), conf.getAddress(), ramptime,
                        ramplevel, getOnCmd());
            } else if (cmd == OnOffType.OFF) {
                double ramptime = conf.getRampTime();
                int ramplevel = getRampLevel(conf, 0 /* ignored */);
                byte cmd2 = encode(ramptime, ramplevel);
                SendInsteonMessage m = new SendInsteonMessage(conf.getAddress(), conf.getDefaultFlags(), getOffCmd(),
                        cmd2);
                conf.enqueueMessage(m);
                logger.info("{}: sent ramp off to switch {} time {} cmd1 {}", nm(), conf.getAddress(), ramptime,
                        getOffCmd());
            }
            // expect to get a direct ack after this!
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }

    private int getRampLevel(InsteonThingHandler conf, int defaultValue) {
        Map<String, String> params = conf.getThing().getProperties();
        return params.containsKey("ramplevel") ? Integer.parseInt(params.get("ramplevel")) : defaultValue;
    }
}
