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
package org.openhab.io.eebus.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openmuc.jeebus.usecase.powerlimitation.controllablesystem.SimpleLimitationConfig;

/**
 * Tests for {@link EEBusLimitationConfigFactory}.
 * <p>
 * {@link SimpleLimitationConfig}'s constructor takes {@code (failsafeDurationMin, failsafeLimit,
 * loadControlLimit, nominalMax)}, so these guard against transposing {@code nominalMax} and
 * {@code failsafeLimit}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class EEBusLimitationConfigFactoryTest {

    @Test
    void lpcMapsNominalMaxAndFailsafeToTheirOwnFields() {
        Map<String, Object> config = Map.of("nominalMax", 11000, "failsafeLimit", 4200, "failsafeDuration", "PT2H");

        SimpleLimitationConfig limitationConfig = EEBusLimitationConfigFactory.lpc(config);

        assertEquals(11000, limitationConfig.getNominalMax().toDouble());
        assertEquals(4200, limitationConfig.getFailsafeLimit().toDouble());
        assertEquals(11000, limitationConfig.getLoadControlLimit().toDouble(), "default limit starts unrestricted");
        assertEquals("PT2H", limitationConfig.getFailsafeDurationMin());
    }

    @Test
    void lppMapsNominalMaxAndFailsafeToTheirOwnFieldsWithNegatedLoadControlLimit() {
        Map<String, Object> config = Map.of("nominalMax", 5000, "failsafeLimit", 1000, "failsafeDuration", "PT1H");

        SimpleLimitationConfig limitationConfig = EEBusLimitationConfigFactory.lpp(config);

        assertEquals(5000, limitationConfig.getNominalMax().toDouble());
        assertEquals(1000, limitationConfig.getFailsafeLimit().toDouble());
        assertEquals(-5000, limitationConfig.getLoadControlLimit().toDouble());
        assertEquals("PT1H", limitationConfig.getFailsafeDurationMin());
    }

    @Test
    void lpcFallsBackToDefaultsWhenConfigIsEmpty() {
        SimpleLimitationConfig limitationConfig = EEBusLimitationConfigFactory.lpc(Map.of());

        assertEquals(EEBusLimitationConfigFactory.DEFAULT_NOMINAL_MAX_WATTS,
                limitationConfig.getNominalMax().toDouble());
        assertEquals(EEBusLimitationConfigFactory.DEFAULT_NOMINAL_MAX_WATTS,
                limitationConfig.getFailsafeLimit().toDouble(), "failsafe defaults to nominalMax when unset");
        assertEquals(EEBusLimitationConfigFactory.DEFAULT_FAILSAFE_DURATION, limitationConfig.getFailsafeDurationMin());
    }

    @Test
    void lppFallsBackToZeroNominalMaxWhenConfigIsEmpty() {
        // Unlike LPC, LPP's default is 0, not DEFAULT_NOMINAL_MAX_WATTS - claiming export capacity
        // that was never configured would misreport this installation's real capability to the CEM.
        SimpleLimitationConfig limitationConfig = EEBusLimitationConfigFactory.lpp(Map.of());

        assertEquals(0, limitationConfig.getNominalMax().toDouble());
        assertEquals(0, limitationConfig.getFailsafeLimit().toDouble(), "failsafe defaults to nominalMax when unset");
        assertEquals(0, limitationConfig.getLoadControlLimit().toDouble());
        assertEquals(EEBusLimitationConfigFactory.DEFAULT_FAILSAFE_DURATION, limitationConfig.getFailsafeDurationMin());
    }
}
