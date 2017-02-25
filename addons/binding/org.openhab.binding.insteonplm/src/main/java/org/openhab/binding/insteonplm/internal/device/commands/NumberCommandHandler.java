package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.CommandHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler that sends a command with a numerical value to a device.
 * The handler is very parameterizable so it can be reused for different devices.
 * First used for setting thermostat parameters.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */

public class NumberCommandHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(NumberCommandHandler.class);

    private int factor = 1;
    private byte cmd1 = 0;
    private byte cmd2 = 0;
    private byte data1;
    private byte data2;
    private byte data3;
    private String fieldForValue = null;

    private enum ExtendedData {
        extendedNone,
        extendedCrc1,
        extendedCrc2
    }

    ExtendedData extended = ExtendedData.extendedNone;

    NumberCommandHandler(DeviceFeature f) {
        super(f);
    }

    public void setExtended(String val) {
        extended = ExtendedData.valueOf(val);
    }

    public void setFactor(String factorStr) {
        try {
            factor = Integer.valueOf(factorStr);
        } catch (NumberFormatException e) {
            logger.error("Unable to parse {}", e, factorStr);
        }

    }

    public void setCmd1(String factor) {
        try {
            cmd1 = Byte.valueOf(factor);
        } catch (NumberFormatException e) {
            logger.error("Unable to parse {}", e, factor);
        }

    }

    public void setCmd2(String factor) {
        try {
            cmd2 = Byte.valueOf(factor);
        } catch (NumberFormatException e) {
            logger.error("Unable to parse {}", e, factor);
        }

    }

    public void setValue(String value) {
        fieldForValue = value;
    }

    public void setData1(String val) {
        try {
            data1 = Byte.valueOf(val);
        } catch (NumberFormatException e) {
            logger.error("Unable to read {}", e, val);
        }
    }

    public void setData2(String val) {
        try {
            data2 = Byte.valueOf(val);
        } catch (NumberFormatException e) {
            logger.error("Unable to read {}", e, val);
        }
    }

    public void setData3(String val) {
        try {
            data3 = Byte.valueOf(val);
        } catch (NumberFormatException e) {
            logger.error("Unable to read {}", e, val);
        }
    }

    public int transform(int cmd) {
        return (cmd);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, ChannelUID channelId, Command cmd) {
        try {
            int dc = transform(((DecimalType) cmd).intValue());
            //
            // determine what level should be, and what field it should be in
            //
            int ilevel = dc * factor;
            byte level = (byte) (ilevel > 255 ? 0xFF : ((ilevel < 0) ? 0 : ilevel));
            //
            // figure out what cmd1, cmd2, d1, d2, d3 are supposed to be
            // to form a proper message
            //
            Message m = null;
            if (extended != ExtendedData.extendedNone) {
                byte[] data = new byte[] { data1, data2, data3 };
                m = conf.getMessageFactory().makeExtendedMessage((byte) 0x0f, cmd1, cmd2, data, conf.getAddress());
                m.setByte(fieldForValue, level);
                if (extended == ExtendedData.extendedCrc1) {
                    m.setCRC();
                } else if (extended == ExtendedData.extendedCrc2) {
                    m.setCRC2();
                }
            } else {
                m = conf.getMessageFactory().makeStandardMessage((byte) 0x0f, cmd1, cmd2, conf.getAddress());
                m.setByte(fieldForValue, level);
            }
            conf.enqueueMessage(m);
            logger.info("{}: sent msg to change level to {}", nm(), ((DecimalType) cmd).intValue());
            m = null;
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
