/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.dto.json;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * * Test cases for {@link EnergyStats} Plain Old Java Objects.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class EnergyStatsTest extends AbstractJSONTest {
    public static final double DELTA = 0.0001;

    @Test
    public void currentStateUpdateTest() throws IOException {
        EnergyStats energyStats = getObjectFromJson("EnergyStats.json", EnergyStats.class, gson);
        assertNotNull(energyStats);

        assertEquals(-9999, energyStats.mMValueAmp);
        assertEquals(0.0, energyStats.getScaledAmperage(), DELTA);
        assertEquals(-9999, energyStats.mMValueVolt);
        assertEquals(0.0, energyStats.getScaledVoltage(), DELTA);
        assertEquals(36100, energyStats.mMValuePower);
        assertEquals(361.0, energyStats.getScaledPower(), DELTA);
        assertEquals(17849392, energyStats.mMValueEnergy);
        assertEquals(17849.392, energyStats.getScaledEnergy(), DELTA);
    }

    @Test
    public void currentStateUpdateWithInvalidValuesTest() throws IOException {
        EnergyStats energyStats = getObjectFromJson("EnergyStatsInvalid.json", EnergyStats.class, gson);
        assertNotNull(energyStats);

        assertEquals(-9999, energyStats.mMValueAmp);
        assertEquals(0.0, energyStats.getScaledAmperage(), DELTA);
        assertEquals(-9999, energyStats.mMValueVolt);
        assertEquals(0.0, energyStats.getScaledVoltage(), DELTA);
        assertEquals(-9999, energyStats.mMValuePower);
        assertEquals(0.0, energyStats.getScaledPower(), DELTA);
        assertEquals(91000, energyStats.mMValueEnergy);
        assertEquals(91.0, energyStats.getScaledEnergy(), DELTA);
    }
}
