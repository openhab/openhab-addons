/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.internal.util;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Methods to get the value from a property of an object.
 * 
 * @author Gerhard Riegler - Initial contribution
 */
public class PropertyUtils {

    /**
     * Returns the state of the channel.
     */
    public static State getState(ChannelUID channelUID, Object instance) throws Exception {
        Object value = getPropertyValue(channelUID, instance);
        if (value == null) {
            return UnDefType.UNDEF;
        } else if (value instanceof Calendar) {
            return new DateTimeType((Calendar) value);
        } else if (value instanceof Number) {
            BigDecimal decimalValue = new BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP);
            return new DecimalType(decimalValue);
        } else if (value instanceof String || value instanceof Enum) {
            return new StringType(value.toString());
        } else {
            throw new RuntimeException("Unsupported value type " + value.getClass().getSimpleName());
        }
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
        if (++nestedIndex < properties.length) {
            return getPropertyValue(result, properties, nestedIndex);
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
