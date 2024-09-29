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
package org.openhab.binding.homematic.internal.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.openhab.binding.homematic.internal.converter.type.DecimalTypeConverter;
import org.openhab.binding.homematic.internal.converter.type.QuantityTypeConverter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;

/**
 * Tests for
 * {@link org.openhab.binding.homematic.internal.converter.type.AbstractTypeConverter#convertToBinding(org.openhab.core.types.Type, HmDatapoint)}.
 *
 * @author Michael Reitler - Initial Contribution
 *
 */
public class ConvertToBindingTest extends BaseConverterTest {

    @Test
    public void testDecimalTypeConverter() throws ConverterException {
        Object convertedValue;
        TypeConverter<?> dTypeConverter = new DecimalTypeConverter();

        // the binding is backwards compatible, so clients may still use DecimalType, even if a unit is used
        floatDp.setUnit("°C");

        convertedValue = dTypeConverter.convertToBinding(new DecimalType(99.9), floatDp);
        assertThat(convertedValue, is(99.9));

        convertedValue = dTypeConverter.convertToBinding(new DecimalType(99.9999999999999999999999999999999), floatDp);
        assertThat(convertedValue, is(100.0));

        convertedValue = dTypeConverter.convertToBinding(new DecimalType(99.0), integerDp);
        assertThat(convertedValue, is(99));

        convertedValue = dTypeConverter.convertToBinding(new DecimalType(99.9), integerDp);
        assertThat(convertedValue, is(99));
    }

    @Test
    public void testQuantityTypeConverter() throws ConverterException {
        Object convertedValue;
        TypeConverter<?> qTypeConverter = new QuantityTypeConverter();
        floatQuantityDp.setUnit("°C");

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<>("99.9 °C"), floatQuantityDp);
        assertThat(convertedValue, is(99.9));

        floatQuantityDp.setUnit("Â°C"); // at some points datapoints come with such unit instead of °C

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<>("451 °F"), floatQuantityDp);
        assertThat(convertedValue, is(232.777778));

        floatQuantityDp.setUnit("km/h");

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<>("70.07 m/s"), floatQuantityDp);
        assertThat(convertedValue, is(252.252));

        integerQuantityDp.setUnit("%");

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<>("99.0 %"), integerQuantityDp);
        assertThat(convertedValue, is(99));

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<>("99.9 %"), integerQuantityDp);
        assertThat(convertedValue, is(99));

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<>("1"), integerQuantityDp);
        assertThat(convertedValue, is(100));

        floatQuantityDp.setUnit("100%"); // not really a unit, but it occurs in homematic datapoints

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<>("99.0 %"), floatQuantityDp);
        assertThat(convertedValue, is(0.99));

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<>("99.9 %"), floatQuantityDp);
        assertThat(convertedValue, is(0.999));

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<>("1"), floatQuantityDp);
        assertThat(convertedValue, is(1.0));

        integerQuantityDp.setUnit("Lux");

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<>("42 lx"), integerQuantityDp);
        assertThat(convertedValue, is(42));
    }

    @Test
    public void testQuantityTypeConverterFailsToConvertDecimalType() {
        QuantityTypeConverter converter = new QuantityTypeConverter();
        assertThrows(ConverterException.class, () -> converter.convertToBinding(new DecimalType(99.9), floatDp));
    }

    @Test
    public void testDecimalTypeConverterFailsToConvertQuantityType() {
        DecimalTypeConverter converter = new DecimalTypeConverter();
        assertThrows(ConverterException.class, () -> converter.convertToBinding(new QuantityType<>("99.9 %"), floatDp));
    }
}
