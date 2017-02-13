package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.InsteonThing;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Msg;
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
    public void handleCommand(InsteonThingHandler conf, Command cmd, InsteonThing dev) {
        try {
            if (cmd == OnOffType.ON) {
                double ramptime = getRampTime(conf, 0);
                int ramplevel = getRampLevel(conf, 100);
                byte cmd2 = encode(ramptime, ramplevel);
                Msg m = dev.makeStandardMessage((byte) 0x0f, getOnCmd(), cmd2, getGroup(conf));
                dev.enqueueMessage(m, getFeature());
                logger.info("{}: sent ramp on to switch {} time {} level {} cmd1 {}", nm(), dev.getAddress(), ramptime,
                        ramplevel, getOnCmd());
            } else if (cmd == OnOffType.OFF) {
                double ramptime = getRampTime(conf, 0);
                int ramplevel = getRampLevel(conf, 0 /* ignored */);
                byte cmd2 = encode(ramptime, ramplevel);
                Msg m = dev.makeStandardMessage((byte) 0x0f, getOffCmd(), cmd2, getGroup(conf));
                dev.enqueueMessage(m, getFeature());
                logger.info("{}: sent ramp off to switch {} time {} cmd1 {}", nm(), dev.getAddress(), ramptime,
                        getOffCmd());
            }
            // expect to get a direct ack after this!
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }

    private int getRampLevel(InsteonThingHandler conf, int defaultValue) {
        Map<String, String> params = conf.getThing().getProperties();
        return params.containsKey("ramplevel") ? Integer.parseInt(params.get("ramplevel")) : defaultValue;
    }
}
