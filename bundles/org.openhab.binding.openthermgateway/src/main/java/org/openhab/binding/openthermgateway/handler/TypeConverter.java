package org.openhab.binding.openthermgateway.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;

/**
 * The {@link TypeConverter} is used to convert simple values to SmartHome types
 * sent to one of the channels.
 *
 * @author Arjen Korevaar
 */
public class TypeConverter {
    /*
     * DateTimeType
     * DecimalType
     * HSBType
     * OnOffType
     * OpenClosedType
     * PercentType
     * PlayPauseType
     * PointType
     * RawType
     * RewindFastforwardType
     * StringListType
     * StringType
     * UnDefType
     * UpDownType
     */

    public static DecimalType toDecimalType(float value) {
        return new DecimalType(value);
    }

    public static OnOffType toOnOffType(boolean value) {
        return value ? OnOffType.ON : OnOffType.OFF;
    }

    public static PercentType toPercentType(int value) {
        return new PercentType(value);
    }

    public static StringType toStringType(String value) {
        return new StringType(value);
    }
}
