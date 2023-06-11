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
package org.openhab.binding.avmfritz.internal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link HeatingModel} methods.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HeatingModelTest {

    private static final BigDecimal BIGDECIMAL_FOURTEEN = new BigDecimal("14.0");
    private static final BigDecimal BIGDECIMAL_FOURTEEN_POINT_FIVE = new BigDecimal("14.5");

    @Test
    public void validateTemperatureConversionFromCelsius() {
        assertEquals(BigDecimal.ZERO, HeatingModel.fromCelsius(null));
        assertEquals(HeatingModel.TEMP_FRITZ_MIN, HeatingModel.fromCelsius(BigDecimal.ONE));
        assertEquals(HeatingModel.TEMP_FRITZ_MIN, HeatingModel.fromCelsius(new BigDecimal("7.5")));
        assertEquals(new BigDecimal("16.00"), HeatingModel.fromCelsius(HeatingModel.TEMP_CELSIUS_MIN));
        assertEquals(new BigDecimal("28.00"), HeatingModel.fromCelsius(BIGDECIMAL_FOURTEEN));
        assertEquals(new BigDecimal("29.00"), HeatingModel.fromCelsius(BIGDECIMAL_FOURTEEN_POINT_FIVE));
        assertEquals(new BigDecimal("56.00"), HeatingModel.fromCelsius(HeatingModel.TEMP_CELSIUS_MAX));
        assertEquals(HeatingModel.TEMP_FRITZ_MAX, HeatingModel.fromCelsius(new BigDecimal("28.5")));
        assertEquals(HeatingModel.TEMP_FRITZ_MAX, HeatingModel.fromCelsius(new BigDecimal("35")));
    }

    @Test
    public void validateTemperatureConversionToCelsius() {
        assertEquals(BigDecimal.ZERO, HeatingModel.toCelsius(null));
        assertEquals(BIGDECIMAL_FOURTEEN, HeatingModel.toCelsius(new BigDecimal("28")));
        assertEquals(BIGDECIMAL_FOURTEEN_POINT_FIVE, HeatingModel.toCelsius(new BigDecimal("29")));
        assertEquals(new BigDecimal("6.0"), HeatingModel.toCelsius(HeatingModel.TEMP_FRITZ_OFF));
        assertEquals(new BigDecimal("30.0"), HeatingModel.toCelsius(HeatingModel.TEMP_FRITZ_ON));
    }

    @Test
    public void validateTemperatureNormalization() {
        assertEquals(BIGDECIMAL_FOURTEEN, HeatingModel.normalizeCelsius(BIGDECIMAL_FOURTEEN));
        assertEquals(BIGDECIMAL_FOURTEEN, HeatingModel.normalizeCelsius(new BigDecimal("13.9")));
        assertEquals(BIGDECIMAL_FOURTEEN, HeatingModel.normalizeCelsius(new BigDecimal("14.1")));
        assertEquals(BIGDECIMAL_FOURTEEN_POINT_FIVE, HeatingModel.normalizeCelsius(new BigDecimal("14.4")));
        assertEquals(BIGDECIMAL_FOURTEEN_POINT_FIVE, HeatingModel.normalizeCelsius(new BigDecimal("14.6")));
    }

    @Test
    public void validateGetRadiatorModeReturnsValidMode() {
        HeatingModel heatingModel = new HeatingModel();
        assertEquals(MODE_UNKNOWN, heatingModel.getRadiatorMode());

        heatingModel.setTsoll(BigDecimal.ONE);
        assertEquals(MODE_ON, heatingModel.getRadiatorMode());

        heatingModel.setKomfort(BigDecimal.ONE);
        assertEquals(MODE_COMFORT, heatingModel.getRadiatorMode());
    }
}
