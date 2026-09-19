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
package org.openhab.automation.pidcontroller.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests the integral decay of the PID controller.
 *
 * @author Vlad Kolotov - Initial contribution
 *
 */
@NonNullByDefault
class PIDControllerIntegralDecayTest {
    private static final int LOOP_TIME_MS = 1000;
    private static final double KD_TIME_CONSTANT_SEC = 10;
    private static final double DECAY_TIME_SEC = 100;

    private PIDController createController(double decayTimeSec) {
        return new PIDController(1, 1, 0, KD_TIME_CONSTANT_SEC, Double.NaN, Double.NaN, decayTimeSec, Double.NaN,
                Double.NaN, Double.NaN);
    }

    private double settle(PIDController controller, double input, double setpoint, int invocations) {
        double integralPart = 0;
        for (int i = 0; i < invocations; i++) {
            integralPart = controller.calculate(input, setpoint, LOOP_TIME_MS, LOOP_TIME_MS).getIntegralPart();
        }
        return integralPart;
    }

    @Test
    void constantErrorIsFadedOutWhenEnabled() {
        // the deviation is not growing, so the I-part must not keep accumulating without bound
        double withoutDecay = settle(createController(0), 0, 1, 500);
        double withDecay = settle(createController(DECAY_TIME_SEC), 0, 1, 500);

        assertTrue(withDecay < withoutDecay,
                "the I-part must be smaller with decay enabled, was " + withDecay + " vs " + withoutDecay);
        // it converges towards the decay time constant instead of growing with every invocation
        assertEquals(DECAY_TIME_SEC, withDecay, DECAY_TIME_SEC * 0.1);
    }

    @Test
    void disabledByDefaultKeepsAccumulating() {
        // an unset decay time is passed through as NaN and must not enable the fade-out
        double integralPart = settle(createController(Double.NaN), 0, 1, 100);

        assertEquals(100, integralPart, 0.001);
    }

    @Test
    void zeroDecayTimeKeepsAccumulating() {
        double integralPart = settle(createController(0), 0, 1, 100);

        assertEquals(100, integralPart, 0.001);
    }

    @Test
    void growingDeviationIsNotFadedOut() {
        PIDController withDecay = createController(DECAY_TIME_SEC);
        PIDController withoutDecay = createController(0);

        // the error grows on every invocation, so the integral action is still required
        double withDecayPart = 0;
        double withoutDecayPart = 0;
        for (int i = 1; i <= 50; i++) {
            withDecayPart = withDecay.calculate(0, i, LOOP_TIME_MS, LOOP_TIME_MS).getIntegralPart();
            withoutDecayPart = withoutDecay.calculate(0, i, LOOP_TIME_MS, LOOP_TIME_MS).getIntegralPart();
        }

        assertEquals(withoutDecayPart, withDecayPart, 0.001);
    }

    @Test
    void fadeOutIsSymmetricForNegativeDeviations() {
        double positive = settle(createController(DECAY_TIME_SEC), 0, 1, 500);
        double negative = settle(createController(DECAY_TIME_SEC), 0, -1, 500);

        assertEquals(positive, -negative, 0.001);
    }

    @Test
    void growingNegativeDeviationIsNotFadedOut() {
        PIDController withDecay = createController(DECAY_TIME_SEC);
        PIDController withoutDecay = createController(0);

        double withDecayPart = 0;
        double withoutDecayPart = 0;
        for (int i = 1; i <= 50; i++) {
            withDecayPart = withDecay.calculate(0, -i, LOOP_TIME_MS, LOOP_TIME_MS).getIntegralPart();
            withoutDecayPart = withoutDecay.calculate(0, -i, LOOP_TIME_MS, LOOP_TIME_MS).getIntegralPart();
        }

        assertEquals(withoutDecayPart, withDecayPart, 0.001);
    }

    @Test
    void fadeOutWorksWithoutDerivativeTimeConstant() {
        // the fade-out uses the unfiltered error change, so it does not depend on the derivative configuration
        PIDController withoutKdTimeConstant = new PIDController(1, 1, 0, 0, Double.NaN, Double.NaN, DECAY_TIME_SEC,
                Double.NaN, Double.NaN, Double.NaN);

        assertEquals(DECAY_TIME_SEC, settle(withoutKdTimeConstant, 0, 1, 500), DECAY_TIME_SEC * 0.1);
    }
}
