/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.webthing.internal.link;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Helper class to create a TypeConverter
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
class TypeConverters {

    /**
     * create a TypeConverter for a given Item type and property type
     *
     * @param itemType the item type
     * @param propertyType the property type
     * @return the type converter
     */
    static TypeConverter create(String itemType, String propertyType) {
        switch (itemType.toLowerCase(Locale.ENGLISH)) {
            case "switch":
                return new SwitchTypeConverter();
            case "dimmer":
                return new DimmerTypeConverter();
            case "contact":
                return new ContactTypeConverter();
            case "color":
                return new ColorTypeConverter();
            case "number":
                if (propertyType.toLowerCase(Locale.ENGLISH).equals("integer")) {
                    return new IntegerTypeConverter();
                } else {
                    return new NumberTypeConverter();
                }
            default:
                return new StringTypeConverter();
        }
    }

    private static boolean toBoolean(Object propertyValue) {
        return Boolean.parseBoolean(propertyValue.toString());
    }

    private static BigDecimal toDecimal(Object propertyValue) {
        return new BigDecimal(propertyValue.toString());
    }

    private static final class ColorTypeConverter implements TypeConverter {

        @Override
        public Command toStateCommand(Object propertyValue) {
            var value = propertyValue.toString();
            if (!value.contains("#")) {
                value = "#" + value;
            }
            Color rgb = Color.decode(value);
            return HSBType.fromRGB(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
        }

        @Override
        public Object toPropertyValue(State state) {
            var hsb = ((HSBType) state);

            // Get HSB values
            Float hue = hsb.getHue().floatValue();
            Float saturation = hsb.getSaturation().floatValue();
            Float brightness = hsb.getBrightness().floatValue();

            // Convert HSB to RGB and then to HTML hex
            Color rgb = Color.getHSBColor(hue / 360, saturation / 100, brightness / 100);
            return String.format("#%02x%02x%02x", rgb.getRed(), rgb.getGreen(), rgb.getBlue());
        }
    }

    private static final class SwitchTypeConverter implements TypeConverter {

        @Override
        public Command toStateCommand(Object propertyValue) {
            return toBoolean(propertyValue) ? OnOffType.ON : OnOffType.OFF;
        }

        @Override
        public Object toPropertyValue(State state) {
            return state == OnOffType.ON;
        }
    }

    private static final class ContactTypeConverter implements TypeConverter {

        @Override
        public Command toStateCommand(Object propertyValue) {
            return toBoolean(propertyValue) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
        }

        @Override
        public Object toPropertyValue(State state) {
            return state == OpenClosedType.OPEN;
        }
    }

    private static final class DimmerTypeConverter implements TypeConverter {

        @Override
        public Command toStateCommand(Object propertyValue) {
            return new PercentType(toDecimal(propertyValue));
        }

        @Override
        public Object toPropertyValue(State state) {
            return ((DecimalType) state).toBigDecimal().intValue();
        }
    }

    private static final class NumberTypeConverter implements TypeConverter {

        @Override
        public Command toStateCommand(Object propertyValue) {
            return new DecimalType(toDecimal(propertyValue));
        }

        @Override
        public Object toPropertyValue(State state) {
            return ((DecimalType) state).doubleValue();
        }
    }

    private static final class IntegerTypeConverter implements TypeConverter {

        @Override
        public Command toStateCommand(Object propertyValue) {
            return new DecimalType(toDecimal(propertyValue));
        }

        @Override
        public Object toPropertyValue(State state) {
            return ((DecimalType) state).intValue();
        }
    }

    private static final class StringTypeConverter implements TypeConverter {

        @SuppressWarnings("unchecked")
        @Override
        public Command toStateCommand(Object propertyValue) {
            String textValue = propertyValue.toString();
            if (propertyValue instanceof Collection) {
                textValue = ((Collection<Object>) propertyValue).stream()
                        .reduce("", (entry1, entry2) -> entry1.toString() + "\n" + entry2.toString()).toString();
            }
            return StringType.valueOf(textValue);
        }

        @Override
        public Object toPropertyValue(State state) {
            return state.toString();
        }
    }
}
