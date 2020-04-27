/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.astro.internal.util;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.astro.internal.config.AstroChannelConfig;

/**
 * Methods to get the value from a property of an object.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Erdoan Hadzhiyusein - Adapted the class to work with the new DateTimeType
 * @author Christoph Weitkamp - Introduced UoM
 */
public class PropertyUtils {

    /** Constructor */
    private PropertyUtils() {
        throw new IllegalAccessError("Non-instantiable");
    }

    private static TimeZoneProvider timeZoneProvider;

    /**
     * Returns the state of the channel.
     */
    public static State getState(ChannelUID channelUID, AstroChannelConfig config, Object instance) throws Exception {
        Object value = getPropertyValue(channelUID, instance);
        if (value == null) {
            return UnDefType.UNDEF;
        } else if (value instanceof State) {
            return (State) value;
        } else if (value instanceof Calendar) {
            Calendar cal = (Calendar) value;
            GregorianCalendar gregorianCal = (GregorianCalendar) DateTimeUtils.applyConfig(cal, config);
            cal.setTimeZone(TimeZone.getTimeZone(timeZoneProvider.getTimeZone()));
            ZonedDateTime zoned = gregorianCal.toZonedDateTime().withFixedOffsetZone();
            return new DateTimeType(zoned);
        } else if (value instanceof Number) {
            BigDecimal decimalValue = new BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP);
            return new DecimalType(decimalValue);
        } else if (value instanceof String || value instanceof Enum) {
            return new StringType(value.toString());
        } else {
            throw new IllegalStateException("Unsupported value type " + value.getClass().getSimpleName());
        }
    }

    public static void setTimeZone(TimeZoneProvider zone) {
        PropertyUtils.timeZoneProvider = zone;
    }

    public static void unsetTimeZone() {
        PropertyUtils.timeZoneProvider = null;
    }

    /**
     * Returns the property value from the object instance, nested properties are possible. If the propertyName is for
     * example rise.start, the methods getRise().getStart() are called.
     */
    public static Object getPropertyValue(ChannelUID channelUID, Object instance) throws Exception {
        String[] properties = StringUtils.split(channelUID.getId(), "#");
        return getPropertyValue(instance, properties, 0);
    }

    /**
     * Iterates through the nested properties and returns the getter value.
     */
    @SuppressWarnings("all")
    private static Object getPropertyValue(Object instance, String[] properties, int nestedIndex) throws Exception {
        String propertyName = properties[nestedIndex];
        Method m = instance.getClass().getMethod(toGetterString(propertyName), null);
        Object result = m.invoke(instance, (Object[]) null);
        if (nestedIndex + 1 < properties.length) {
            return getPropertyValue(result, properties, nestedIndex + 1);
        }
        return result;
    }

    /**
     * Converts the string to a getter property.
     */
    private static String toGetterString(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append("get");
        sb.append(Character.toTitleCase(str.charAt(0)));
        sb.append(str.substring(1));
        return sb.toString();
    }
}
