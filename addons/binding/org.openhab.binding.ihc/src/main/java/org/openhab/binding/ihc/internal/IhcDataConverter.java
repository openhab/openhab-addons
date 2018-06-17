/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.ihc.ws.projectfile.IhcEnumValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSBooleanValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSDateValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSEnumValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSFloatingPointValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSIntegerValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSResourceValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSTimeValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSTimerValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSWeekdayValue;

/**
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcDataConverter {

    /**
     * Convert IHC data type to openHAB data type.
     *
     * @param itemType OpenHAB data type class
     * @param value IHC data value
     *
     * @return openHAB {@link State}
     */
    public static State convertResourceValueToState(String acceptedItemType, WSResourceValue value)
            throws NumberFormatException {
        return convertResourceValueToState(acceptedItemType, value, false);
    }

    /**
     * Convert IHC data type to openHAB data type.
     *
     * @param itemType OpenHAB data type class
     * @param value IHC data value
     * @param inverted Invert value
     *
     * @return openHAB {@link State}
     */
    public static State convertResourceValueToState(String acceptedItemType, WSResourceValue value, boolean inverted)
            throws NumberFormatException {

        State state = UnDefType.UNDEF;

        if ("Number".equals(acceptedItemType)) {

            if (value instanceof WSFloatingPointValue) {
                // state = new
                // DecimalType(((WSFloatingPointValue)value).getFloatingPointValue());

                // Controller send floating point value as a double value (>10 decimals)
                // (22.299999237060546875), so let's round value to have max 2
                // decimals
                double d = ((WSFloatingPointValue) value).getFloatingPointValue();
                BigDecimal bd = new BigDecimal(d).setScale(2, RoundingMode.HALF_EVEN);
                state = new DecimalType(bd);
            }

            else if (value instanceof WSBooleanValue) {
                state = new DecimalType(((WSBooleanValue) value).isValue() ? 1 : 0);
            }

            else if (value instanceof WSIntegerValue) {
                state = new DecimalType(((WSIntegerValue) value).getInteger());
            }

            else if (value instanceof WSTimerValue) {
                state = new DecimalType(((WSTimerValue) value).getMilliseconds());
            }

            else if (value instanceof WSEnumValue) {
                state = new DecimalType(((WSEnumValue) value).getEnumValueID());
            }

            else if (value instanceof WSWeekdayValue) {
                state = new DecimalType(((WSWeekdayValue) value).getWeekdayNumber());
            }

            else {
                throw new NumberFormatException("Can't convert " + value.getClass().toString() + " to NumberItem");
            }

        } else if ("Dimmer".equals(acceptedItemType)) {

            if (value instanceof WSIntegerValue) {
                state = new PercentType(((WSIntegerValue) value).getInteger());

            } else {
                throw new NumberFormatException("Can't convert " + value.getClass().toString() + " to NumberItem");
            }

        } else if ("Switch".equals(acceptedItemType)) {

            if (value instanceof WSBooleanValue) {
                if (((WSBooleanValue) value).isValue()) {
                    state = inverted == false ? OnOffType.ON : OnOffType.OFF;
                } else {
                    state = inverted == false ? OnOffType.OFF : OnOffType.ON;
                }
            } else {
                throw new NumberFormatException("Can't convert " + value.getClass().toString() + " to SwitchItem");
            }

        } else if ("Contact".equals(acceptedItemType)) {

            if (value instanceof WSBooleanValue) {
                if (((WSBooleanValue) value).isValue()) {
                    state = inverted == false ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                } else {
                    state = inverted == false ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
                }
            } else {
                throw new NumberFormatException("Can't convert " + value.getClass().toString() + " to ContactItem");
            }

        } else if ("DateTime".equals(acceptedItemType)) {

            if (value instanceof WSDateValue) {

                Calendar cal = WSDateTimeToCalendar((WSDateValue) value, null);
                state = new DateTimeType(cal);

            } else if (value instanceof WSTimeValue) {

                Calendar cal = WSDateTimeToCalendar(null, (WSTimeValue) value);
                state = new DateTimeType(cal);

            } else {

                throw new NumberFormatException("Can't convert " + value.getClass().toString() + " to DateTimeItem");
            }

        } else if ("String".equals(acceptedItemType)) {

            if (value instanceof WSEnumValue) {

                state = new StringType(((WSEnumValue) value).getEnumName());

            } else {

                throw new NumberFormatException("Can't convert " + value.getClass().toString() + " to StringItem");
            }

        } else if ("Rollershutter".equals(acceptedItemType)) {

            if (value instanceof WSIntegerValue) {
                state = new PercentType(((WSIntegerValue) value).getInteger());
            } else {
                throw new NumberFormatException("Can't convert " + value.getClass().toString() + " to NumberItem");
            }
        }

        return state;
    }

    private static Calendar WSDateTimeToCalendar(WSDateValue date, WSTimeValue time) {

        Calendar cal = new GregorianCalendar(1900, 01, 01);

        if (date != null) {
            short year = date.getYear();
            short month = date.getMonth();
            short day = date.getDay();

            cal.set(year, month - 1, day, 0, 0, 0);
        }

        if (time != null) {
            int hour = time.getHours();
            int minute = time.getMinutes();
            int second = time.getSeconds();

            cal.set(1900, 0, 1, hour, minute, second);
        }

        return cal;
    }

    /**
     * Convert openHAB data type to IHC data type.
     *
     * @param type openHAB data type
     * @param value
     * @param enumValues
     *
     * @return IHC data type
     */
    public static WSResourceValue convertCommandToResourceValue(Command type, WSResourceValue value,
            ArrayList<IhcEnumValue> enumValues) {
        return convertCommandToResourceValue(type, value, enumValues, false);
    }

    /**
     * Convert openHAB data type to IHC data type.
     *
     * @param type openHAB data type
     * @param value
     * @param enumValues
     * @param inverted Invert value
     *
     * @return IHC data type
     */
    public static WSResourceValue convertCommandToResourceValue(Command type, WSResourceValue value,
            ArrayList<IhcEnumValue> enumValues, boolean inverted) {

        if (type instanceof DecimalType) {

            if (value instanceof WSFloatingPointValue) {

                double newVal = ((DecimalType) type).doubleValue();
                double max = ((WSFloatingPointValue) value).getMaximumValue();
                double min = ((WSFloatingPointValue) value).getMinimumValue();

                if (newVal >= min && newVal <= max) {
                    ((WSFloatingPointValue) value).setFloatingPointValue(newVal);
                } else {
                    throw new NumberFormatException(
                            "Value is not between accetable limits (min=" + min + ", max=" + max + ")");
                }

            } else if (value instanceof WSBooleanValue) {

                ((WSBooleanValue) value).setValue(((DecimalType) type).intValue() > 0 ? true : false);

            } else if (value instanceof WSIntegerValue) {

                int newVal = ((DecimalType) type).intValue();
                int max = ((WSIntegerValue) value).getMaximumValue();
                int min = ((WSIntegerValue) value).getMinimumValue();

                if (newVal >= min && newVal <= max) {
                    ((WSIntegerValue) value).setInteger(newVal);
                } else {
                    throw new NumberFormatException(
                            "Value is not between accetable limits (min=" + min + ", max=" + max + ")");
                }

            } else if (value instanceof WSTimerValue) {

                ((WSTimerValue) value).setMilliseconds(((DecimalType) type).longValue());

            } else if (value instanceof WSWeekdayValue) {

                ((WSWeekdayValue) value).setWeekdayNumber(((DecimalType) type).intValue());

            } else {

                throw new NumberFormatException("Can't convert DecimalType to " + value.getClass());

            }

        } else if (type instanceof OnOffType) {

            if (value instanceof WSBooleanValue) {

                boolean valON = inverted == false ? true : false;
                boolean valOFF = inverted == false ? false : true;

                ((WSBooleanValue) value).setValue(type == OnOffType.ON ? valON : valOFF);

            } else if (value instanceof WSIntegerValue) {

                int newVal = type == OnOffType.ON ? 100 : 0;
                int max = ((WSIntegerValue) value).getMaximumValue();
                int min = ((WSIntegerValue) value).getMinimumValue();

                if (newVal >= min && newVal <= max) {
                    ((WSIntegerValue) value).setInteger(newVal);
                } else {
                    throw new NumberFormatException(
                            "Value is not between accetable limits (min=" + min + ", max=" + max + ")");
                }

            } else {

                throw new NumberFormatException("Can't convert OnOffType to " + value.getClass());

            }
        } else if (type instanceof OpenClosedType) {
            boolean valON = inverted == false ? true : false;
            boolean valOFF = inverted == false ? false : true;

            ((WSBooleanValue) value).setValue(type == OpenClosedType.OPEN ? valON : valOFF);

        } else if (type instanceof DateTimeType) {

            if (value instanceof WSDateValue) {
                Calendar c = ((DateTimeType) type).getCalendar();

                short year = (short) c.get(Calendar.YEAR);
                byte month = (byte) (c.get(Calendar.MONTH) + 1);
                byte day = (byte) c.get(Calendar.DAY_OF_MONTH);

                ((WSDateValue) value).setYear(year);
                ((WSDateValue) value).setMonth(month);
                ((WSDateValue) value).setDay(day);

            } else if (value instanceof WSTimeValue) {
                Calendar c = ((DateTimeType) type).getCalendar();

                int hours = c.get(Calendar.HOUR_OF_DAY);
                int minutes = c.get(Calendar.MINUTE);
                int seconds = c.get(Calendar.SECOND);

                ((WSTimeValue) value).setHours(hours);
                ((WSTimeValue) value).setMinutes(minutes);
                ((WSTimeValue) value).setSeconds(seconds);

            } else {

                throw new NumberFormatException("Can't convert DateTimeItem to " + value.getClass());

            }

        } else if (type instanceof StringType) {

            if (value instanceof WSEnumValue) {

                if (enumValues != null) {
                    boolean found = false;

                    for (IhcEnumValue item : enumValues) {

                        if (item.name.equals(type.toString())) {

                            ((WSEnumValue) value).setEnumValueID(item.id);
                            ((WSEnumValue) value).setEnumName(type.toString());
                            found = true;
                            break;
                        }
                    }

                    if (found == false) {
                        throw new NumberFormatException("Can't find enum value for string " + type.toString());
                    }
                } else {
                    throw new NumberFormatException("Enum list is null");
                }
            } else {
                throw new NumberFormatException("Can't convert StringType to " + value.getClass());
            }

        } else if (type instanceof PercentType) {

            if (value instanceof WSIntegerValue) {

                int newVal = ((DecimalType) type).intValue();
                int max = ((WSIntegerValue) value).getMaximumValue();
                int min = ((WSIntegerValue) value).getMinimumValue();

                if (newVal >= min && newVal <= max) {
                    ((WSIntegerValue) value).setInteger(newVal);
                } else {
                    throw new NumberFormatException(
                            "Value is not between accetable limits (min=" + min + ", max=" + max + ")");
                }

            } else {

                throw new NumberFormatException("Can't convert PercentType to " + value.getClass());

            }

        } else if (type instanceof UpDownType) {

            if (value instanceof WSBooleanValue) {

                ((WSBooleanValue) value).setValue(type == UpDownType.DOWN ? true : false);

            } else if (value instanceof WSIntegerValue) {

                int newVal = type == UpDownType.DOWN ? 100 : 0;
                int max = ((WSIntegerValue) value).getMaximumValue();
                int min = ((WSIntegerValue) value).getMinimumValue();

                if (newVal >= min && newVal <= max) {
                    ((WSIntegerValue) value).setInteger(newVal);
                } else {
                    throw new NumberFormatException(
                            "Value is not between accetable limits (min=" + min + ", max=" + max + ")");
                }

            } else {

                throw new NumberFormatException("Can't convert UpDownType to " + value.getClass());
            }

        } else {

            throw new NumberFormatException("Can't convert " + type.getClass().toString());
        }

        return value;
    }
}