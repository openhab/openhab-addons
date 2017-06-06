package org.openhab.binding.insteonplm.internal.device.commands;

import java.util.Arrays;

import org.openhab.binding.insteonplm.internal.device.CommandHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.StandardInsteonMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic command handler for ramp commands.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public abstract class RampCommandHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RampCommandHandler.class);

    private static double[] halfRateRampTimes = new double[] { 0.1, 0.3, 2, 6.5, 19, 23.5, 28, 32, 38.5, 47, 90, 150,
            210, 270, 360, 480 };

    private StandardInsteonMessages onCmd = StandardInsteonMessages.LightOnWithRamp;
    private StandardInsteonMessages offCmd = StandardInsteonMessages.LightOffWithRamp;

    RampCommandHandler(DeviceFeature f) {
        super(f);
        // Can't process parameters here because they are set after constructor is invoked.
        // Unfortunately, this means we can't declare the onCmd, offCmd to be final.
    }

    public void setOnCmd(String on) {
        try {
            onCmd = StandardInsteonMessages.fromByte(Integer.valueOf(on));
        } catch (NumberFormatException e) {
            logger.error("Unable to parse {}", e, on);
        }

    }

    public void setOffCmd(String off) {
        try {
            offCmd = StandardInsteonMessages.fromByte(Integer.valueOf(off));
        } catch (NumberFormatException e) {
            logger.error("Unable to parse {}", e, off);
        }

    }

    protected final StandardInsteonMessages getOnCmd() {
        return onCmd;
    }

    protected final StandardInsteonMessages getOffCmd() {
        return offCmd;
    }

    protected byte encode(double ramptimeSeconds, int ramplevel) throws FieldException {
        if (ramplevel < 0 || ramplevel > 100) {
            throw new FieldException("ramplevel must be in the range 0-100 (inclusive)");
        }

        ramplevel = (int) Math.round(ramplevel / (100.0 / 15.0));

        if (ramptimeSeconds < 0) {
            throw new FieldException("ramptime must be greater than 0");
        }

        int ramptime;

        int insertionPoint = Arrays.binarySearch(halfRateRampTimes, ramptimeSeconds);
        if (insertionPoint > 0) {
            ramptime = 15 - insertionPoint;
        } else {
            insertionPoint = -insertionPoint - 1;
            if (insertionPoint == 0) {
                ramptime = 15;
            } else {
                double d1 = Math.abs(halfRateRampTimes[insertionPoint - 1] - ramptimeSeconds);
                double d2 = Math.abs(halfRateRampTimes[insertionPoint] - ramptimeSeconds);
                ramptime = 15 - (d1 > d2 ? insertionPoint : insertionPoint - 1);
                logger.debug("ramp encoding: time {} insert {} d1 {} d2 {} ramp {}", ramptimeSeconds, insertionPoint,
                        d1, d2, ramptime);
            }
        }

        return (byte) (((ramplevel & 0x0f) << 4) | (ramptime & 0xf));
    }
}
