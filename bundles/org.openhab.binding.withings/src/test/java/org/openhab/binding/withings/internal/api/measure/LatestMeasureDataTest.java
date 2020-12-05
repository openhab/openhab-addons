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
package org.openhab.binding.withings.internal.api.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class LatestMeasureDataTest {

    @Test
    public void testCalculateValue_NegativeUnits() {
        LatestMeasureData person = new LatestMeasureData(measure(83291, -3), measure(183, -2), measure(19075, -3));

        assertEquals(BigDecimal.valueOf(83.3), person.getWeight());
        assertEquals(BigDecimal.valueOf(1.83), person.getHeight());
        assertEquals(BigDecimal.valueOf(19.1), person.getFatMass());
    }

    @Test
    public void testCalculateValue_PositiveUnits() {
        LatestMeasureData person = new LatestMeasureData(measure(83291, 3), measure(183, 2), measure(19075, 3));

        assertEquals(BigDecimal.valueOf(83.3), person.getWeight());
        assertEquals(BigDecimal.valueOf(1.83), person.getHeight());
        assertEquals(BigDecimal.valueOf(19.1), person.getFatMass());
    }

    private Optional<MeasuresResponseDTO.Measure> measure(int value, int unit) {
        return Optional.of(new MeasuresResponseDTO.Measure(value, 8, unit));
    }
}
