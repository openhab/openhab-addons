package org.openhab.binding.insteonplm.types;

import org.eclipse.smarthome.core.library.types.DecimalType;

/**
 * A ramp on/off type. Allows you to set the ramp rate and the on/off
 * value at the same time.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public class RampOnOffType extends DecimalType {
    private static final long serialVersionUID = 2017_02_14_001L;

    protected double ramp;

    RampOnOffType(int level, double ramp) {
        super(level);
        this.ramp = ramp;
    }

    public double getRamp() {
        return ramp;
    }
}
