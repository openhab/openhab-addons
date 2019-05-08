/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.onewire;

import static org.junit.Assert.assertEquals;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.library.dimension.Density;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.junit.Test;
import org.openhab.binding.onewire.internal.Util;

/**
 * Tests cases for {@link Util}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class UtilTest {

    @Test
    public void convertAbsoluteHumidityTest() {
        QuantityType<Temperature> temperature = new QuantityType<>("20 °C");
        QuantityType<Dimensionless> relativeHumidity = new QuantityType<>("75%");

        @SuppressWarnings("unchecked")
        QuantityType<Density> absoluteHumidity = (QuantityType<Density>) Util.calculateAbsoluteHumidity(temperature,
                relativeHumidity);
        assertEquals(12.93, absoluteHumidity.doubleValue(), 0.01);

    }

    @Test
    public void dewPointTest() {
        QuantityType<Temperature> temperature = new QuantityType<>("20 °C");
        QuantityType<Dimensionless> relativeHumidity = new QuantityType<>("75%");

        @SuppressWarnings("unchecked")
        QuantityType<Temperature> dewPoint = (QuantityType<Temperature>) Util.calculateDewpoint(temperature,
                relativeHumidity);
        assertEquals(15.43, dewPoint.doubleValue(), 0.01);
    }
}
