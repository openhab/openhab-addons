package org.openhab.binding.insteonplm.internal.device.messages;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.MessageHandler;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flexible handler to extract numerical data from messages.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class NumberMsgHandler extends MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(NumberMsgHandler.class);

    private int mask;
    private int rshift = 0;
    private double offset = 0;
    private double factor = 1;
    private String lowByte = "";
    private String highByte = "";

    NumberMsgHandler(DeviceFeature p) {
        super(p);
    }

    public void setMask(String mask) {
        try {
            this.mask = Integer.valueOf(mask);
        } catch (NumberFormatException e) {
            logger.error("Invalid number format {}", e, mask);
        }
    }

    public void setRShift(String mask) {
        try {
            this.rshift = Integer.valueOf(mask);
        } catch (NumberFormatException e) {
            logger.error("Invalid number format {}", e, mask);
        }
    }

    public void setOffset(String offset) {
        try {
            this.offset = Double.valueOf(offset);
        } catch (NumberFormatException e) {
            logger.error("Invalid number format {}", e, mask);
        }
    }

    public void setFactor(String offset) {
        try {
            this.factor = Double.valueOf(offset);
        } catch (NumberFormatException e) {
            logger.error("Invalid number format {}", e, mask);
        }
    }

    public void setLowByte(String val) {
        lowByte = val;
    }

    public void setHighByte(String val) {
        highByte = val;
    }

    @Override
    public void handleMessage(InsteonThingHandler handler, int group, byte cmd1, Message msg, Channel f) {
        try {
            // first do the bit manipulations to focus on the right area
            int rawValue = extractValue(msg, group);
            int cooked = (rawValue & mask) >> rshift;
            // now do an arbitrary transform on the data
            double value = transform(cooked);
            // last, multiply with factor and add an offset
            double dvalue = offset + value * factor;
            handler.updateFeatureState(f, new DecimalType(dvalue));
        } catch (FieldException e) {
            logger.error("error parsing {}: ", msg, e);
        }
    }

    public int transform(int raw) {
        return (raw);
    }

    private int extractValue(Message msg, int group) throws FieldException {
        if (!lowByte.equals("")) {
            logger.error("{} handler misconfigured, missing low_byte!", nm());
            return 0;
        }
        int value = 0;
        if (lowByte.equals("group")) {
            value = group;
        } else {
            value = msg.getByte(lowByte) & 0xFF;
        }
        if (!highByte.equals("")) {
            value |= (msg.getByte(highByte) & 0xFF) << 8;
        }
        return (value);
    }
}
