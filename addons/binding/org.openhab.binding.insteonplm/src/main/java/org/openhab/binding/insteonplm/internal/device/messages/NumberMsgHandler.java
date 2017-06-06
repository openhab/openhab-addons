package org.openhab.binding.insteonplm.internal.device.messages;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.MessageHandler;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.StandardInsteonMessages;
import org.openhab.binding.insteonplm.internal.message.modem.StandardMessageReceived;
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
    public void handleMessage(InsteonThingHandler handler, int group, StandardMessageReceived msg, Channel f) {
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

    private int extractValue(StandardMessageReceived msg, int group) throws FieldException {
        if (!lowByte.equals("")) {
            logger.error("{} handler misconfigured, missing low_byte!", nm());
            return 0;
        }
        int value = 0;
        if (lowByte.equals("group")) {
            value = group;
        } else {
            // Turn into a get call on the object.
            value = getValue(msg, lowByte);
        }
        if (!highByte.equals("")) {
            value |= (getValue(msg, highByte) & 0xFF) << 8;

        }
        return (value);
    }

    private byte getValue(StandardMessageReceived obj, String ref) {
        Method method;
        try {
            int index = 0;
            if (ref.startsWith("Data")) {
                // Pull the index off.
                String num = ref.substring(6, ref.length() - 2);
                index = Integer.valueOf(num);
                ref = "Data";
            }
            method = StandardMessageReceived.class.getMethod("get" + ref);
            Object ret = method.invoke(obj);
            if (ret instanceof StandardInsteonMessages) {
                return ((StandardInsteonMessages) ret).getCmd1();
            } else if (ret instanceof byte[]) {
                return ((byte[]) ret)[index];
            } else if (ret.getClass().isPrimitive()) {
                return (byte) ret;
            } else {
                logger.error("Unknown return value {} on {} calling {}", ret, obj, ref);
                return 0;
            }
        } catch (NoSuchMethodException | SecurityException e) {
            logger.error("Unable to find method get{} on {}", ref, obj);
            return 0;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.error("Unable to call method get{} on {}", ref, obj);
            return 0;
        }
    }
}
