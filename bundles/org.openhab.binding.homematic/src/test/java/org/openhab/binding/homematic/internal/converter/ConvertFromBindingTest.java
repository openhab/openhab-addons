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
package org.openhab.binding.homematic.internal.converter;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.openhab.binding.homematic.internal.converter.type.AbstractTypeConverter;
import org.openhab.binding.homematic.internal.model.HmDatapoint;

import tec.uom.se.quantity.QuantityDimension;

/**
 * Tests for {@link AbstractTypeConverter#convertFromBinding(HmDatapoint)}.
 *
 * @author Michael Reitler - Initial Contribution
 *
 */
public class ConvertFromBindingTest extends BaseConverterTest {

    @Test
    public void testDecimalTypeConverter() throws ConverterException {
        State convertedState;
        TypeConverter<?> decimalConverter = ConverterFactory.createConverter("Number");

        // the binding is backwards compatible, so clients may still use DecimalType, even if a unit is used
        floatDp.setUnit("%");

        floatDp.setValue(99.9);
        convertedState = decimalConverter.convertFromBinding(floatDp);
        assertThat(convertedState, instanceOf(DecimalType.class));
        assertThat(((DecimalType) convertedState).doubleValue(), is(99.9));

        floatDp.setValue(77.77777778);
        convertedState = decimalConverter.convertFromBinding(floatDp);
        assertThat(convertedState, instanceOf(DecimalType.class));
        assertThat(((DecimalType) convertedState).doubleValue(), is(77.777778));

        integerDp.setValue(99.0);
        convertedState = decimalConverter.convertFromBinding(integerDp);
        assertThat(convertedState, instanceOf(DecimalType.class));
        assertThat(((DecimalType) convertedState).doubleValue(), is(99.0));

        integerDp.setValue(99.9);
        convertedState = decimalConverter.convertFromBinding(integerDp);
        assertThat(convertedState, instanceOf(DecimalType.class));
        assertThat(((DecimalType) convertedState).doubleValue(), is(99.0));
    }

    @SuppressWarnings("null")
    @Test
    public void testQuantityTypeConverter() throws ConverterException {
        State convertedState;
        TypeConverter<?> temperatureConverter = ConverterFactory.createConverter("Number:Temperature");
        TypeConverter<?> frequencyConverter = ConverterFactory.createConverter("Number:Frequency");
        TypeConverter<?> timeConverter = ConverterFactory.createConverter("Number:Time");

        floatQuantityDp.setValue(10.5);
        floatQuantityDp.setUnit("°C");
        convertedState = temperatureConverter.convertFromBinding(floatQuantityDp);
        assertThat(convertedState, instanceOf(QuantityType.class));
        assertThat(((QuantityType<?>) convertedState).getDimension(), is(QuantityDimension.TEMPERATURE));
        assertThat(((QuantityType<?>) convertedState).doubleValue(), is(10.5));
        assertThat(((QuantityType<?>) convertedState).toUnit(ImperialUnits.FAHRENHEIT).doubleValue(), is(50.9));

        floatQuantityDp.setUnit("Â°C");
        assertThat(((QuantityType<?>) convertedState).getDimension(), is(QuantityDimension.TEMPERATURE));
        assertThat(((QuantityType<?>) convertedState).doubleValue(), is(10.5));

        integerQuantityDp.setValue(50000);
        integerQuantityDp.setUnit("mHz");
        convertedState = frequencyConverter.convertFromBinding(integerQuantityDp);
        assertThat(convertedState, instanceOf(QuantityType.class));
        assertThat(((QuantityType<?>) convertedState).getDimension(),
                is(QuantityDimension.NONE.divide(QuantityDimension.TIME)));
        assertThat(((QuantityType<?>) convertedState).intValue(), is(50000));
        assertThat(((QuantityType<?>) convertedState).toUnit(SmartHomeUnits.HERTZ).intValue(), is(50));

        floatQuantityDp.setValue(0.7);
        floatQuantityDp.setUnit("100%");
        convertedState = timeConverter.convertFromBinding(floatQuantityDp);
        assertThat(convertedState, instanceOf(QuantityType.class));
        assertThat(((QuantityType<?>) convertedState).getDimension(), is(QuantityDimension.NONE));
        assertThat(((QuantityType<?>) convertedState).doubleValue(), is(70.0));
        assertThat(((QuantityType<?>) convertedState).getUnit(), is(SmartHomeUnits.PERCENT));
        assertThat(((QuantityType<?>) convertedState).toUnit(SmartHomeUnits.ONE).doubleValue(), is(0.7));
    }
}
