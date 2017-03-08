package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.CommandHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.InsteonFlags;
import org.openhab.binding.insteonplm.internal.message.Message;
import org.openhab.binding.insteonplm.internal.message.StandardInsteonMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler to turn the light on or off.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class LightOnOffCommandHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(LightOnOffCommandHandler.class);

    private enum ExtendedData {
        extendedNone,
        extendedCrc1,
        extendedCrc2
    }

    private ExtendedData extended = ExtendedData.extendedNone;
    private byte data1 = 0;
    private byte data2 = 0;
    private byte data3 = 0;

    LightOnOffCommandHandler(DeviceFeature f) {
        super(f);
    }

    public void setExtended(String val) {
        extended = ExtendedData.valueOf(val);
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

    @Override
    public void handleCommand(InsteonThingHandler conf, ChannelUID channelId, Command cmd) {
        try {
            StandardInsteonMessages direc;
            int level = 0x00;
            Message m = null;
            if (cmd == OnOffType.ON) {
                level = getMaxLightLevel(conf, 0xff);
                direc = StandardInsteonMessages.LightOn;
                logger.info("{}: sent msg to switch {} to {}", nm(), conf.getAddress(), level == 0xff ? "on" : level);
            } else if (cmd == OnOffType.OFF) {
                direc = StandardInsteonMessages.LightOff;
                logger.info("{}: sent msg to switch {} off", nm(), conf.getAddress());
            } else {
                logger.error("Invalid command {}", cmd);
                return;
            }
            if (extended != ExtendedData.extendedNone) {
                byte[] data = new byte[] { data1, data2, data3 };
                m = conf.getMessageFactory().makeExtendedMessage(new InsteonFlags(), direc, (byte) level, data,
                        conf.getAddress());
                logger.info("{}: was an extended message for device {}", nm(), conf.getAddress());
                if (extended == ExtendedData.extendedCrc1) {
                    m.setCRC();
                } else if (extended == ExtendedData.extendedCrc2) {
                    m.setCRC2();
                }
            } else {
                m = conf.getMessageFactory().makeStandardMessage(new InsteonFlags(), direc, (byte) level,
                        conf.getInsteonGroup(), conf.getAddress());
            }
            logger.info("Sending message to {}", conf.getAddress());
            conf.enqueueMessage(m);
            // expect to get a direct ack after this!
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
