/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.ahamodel;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

/**
 * Tests for {@link HeatingModel} methods.
 *
 * @author Christoph Weitkamp - Initial contribution
 *
 */
public class HeatingModelTest {

    @Test
    public void validateTemperatureConversion() {
        assertEquals(BigDecimal.ZERO, HeatingModel.fromCelsius(null));
        assertEquals(HeatingModel.TEMP_FRITZ_MIN, HeatingModel.fromCelsius(BigDecimal.ONE));
        assertEquals(HeatingModel.TEMP_FRITZ_MIN, HeatingModel.fromCelsius(new BigDecimal(7.5)));
        assertEquals(new BigDecimal(16), HeatingModel.fromCelsius(new BigDecimal(8)));
        assertEquals(new BigDecimal(28), HeatingModel.fromCelsius(new BigDecimal(14)));
        assertEquals(new BigDecimal(29), HeatingModel.fromCelsius(new BigDecimal(14.5)));
        assertEquals(new BigDecimal(56), HeatingModel.fromCelsius(new BigDecimal(28)));
        assertEquals(HeatingModel.TEMP_FRITZ_MAX, HeatingModel.fromCelsius(new BigDecimal(28.5)));
        assertEquals(HeatingModel.TEMP_FRITZ_MAX, HeatingModel.fromCelsius(new BigDecimal(30)));

        assertEquals(BigDecimal.ZERO, HeatingModel.toCelsius(null));
        assertEquals(new BigDecimal("14.0"), HeatingModel.toCelsius(new BigDecimal(28)));
        assertEquals(new BigDecimal("14.5"), HeatingModel.toCelsius(new BigDecimal(29)));
        assertEquals(new BigDecimal("6.0"), HeatingModel.toCelsius(new BigDecimal(253)));
        assertEquals(new BigDecimal("30.0"), HeatingModel.toCelsius(new BigDecimal(254)));
    }
}
