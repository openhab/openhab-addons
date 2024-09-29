/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.config.AstroChannelConfig;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Methods to get the value from a property of an object.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Erdoan Hadzhiyusein - Adapted the class to work with the new DateTimeType
 * @author Christoph Weitkamp - Introduced UoM
 */
@NonNullByDefault
public class PropertyUtils {

    /** Constructor */
    private PropertyUtils() {
        throw new IllegalAccessError("Non-instantiable");
    }

    /**
     * Returns the state of the channel.
     */
    public static State getState(ChannelUID channelUID, AstroChannelConfig config, Object instance, ZoneId zoneId)
            throws Exception {
        Object value = getPropertyValue(channelUID, instance);
        if (value == null) {
            return UnDefType.UNDEF;
        } else if (value instanceof State state) {
            return state;
        } else if (value instanceof Calendar cal) {
            GregorianCalendar gregorianCal = (GregorianCalendar) DateTimeUtils.applyConfig(cal, config);
            cal.setTimeZone(TimeZone.getTimeZone(zoneId));
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

    /**
     * Returns the property value from the object instance, nested properties are possible. If the propertyName is for
     * example rise.start, the methods getRise().getStart() are called.
     */
    private static @Nullable Object getPropertyValue(ChannelUID channelUID, Object instance) throws Exception {
        ArrayList<String> properties = new ArrayList<>(List.of(channelUID.getId().split("#")));
        return getPropertyValue(instance, properties);
    }

    /**
     * Iterates through the nested properties and returns the getter value.
     */
    @SuppressWarnings("all")
    private static @Nullable Object getPropertyValue(Object instance, List<String> properties) throws Exception {
        String propertyName = properties.remove(0);
        Method m = instance.getClass().getMethod(toGetterString(propertyName), null);
        Object result = m.invoke(instance, (Object[]) null);
        if (!properties.isEmpty()) {
            Objects.requireNonNull(result);
            return getPropertyValue(result, properties);
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
