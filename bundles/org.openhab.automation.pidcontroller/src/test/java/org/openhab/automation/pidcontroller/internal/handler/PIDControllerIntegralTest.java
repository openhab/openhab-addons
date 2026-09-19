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
class PIDControllerIntegralTest {
    private static final int LOOP_TIME_MS = 1000;
    private static final double KD_TIME_CONSTANT_SEC = 10;
    private static final double DECAY_TIME_SEC = 100;

    private PIDController createController(double decayTimeSec) {
        return createController(decayTimeSec, false);
    }

    private PIDController createController(double decayTimeSec, boolean directionalHold) {
        return new PIDController(1, 1, 0, KD_TIME_CONSTANT_SEC, Double.NaN, Double.NaN, decayTimeSec, directionalHold,
                Double.NaN, Double.NaN, Double.NaN);
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
    void holdSuspendsAccumulationButKeepsTheValue() {
        PIDController controller = createController(0);
        // build up a steady-state I-part
        for (int i = 0; i < 50; i++) {
            controller.calculate(0, 1, LOOP_TIME_MS, LOOP_TIME_MS);
        }
        double beforeHold = controller.calculate(0, 1, LOOP_TIME_MS, LOOP_TIME_MS).getIntegralPart();

        // while held the I-part must not grow, even though the error persists
        double held = beforeHold;
        for (int i = 0; i < 50; i++) {
            held = controller.calculate(0, 1, LOOP_TIME_MS, LOOP_TIME_MS, true).getIntegralPart();
        }
        assertEquals(beforeHold, held, 0.001, "the I-part must keep its value while held");

        // and it resumes from there, not from zero
        double resumed = controller.calculate(0, 1, LOOP_TIME_MS, LOOP_TIME_MS).getIntegralPart();
        assertTrue(resumed > held, "accumulation must resume once the hold is released");
    }

    @Test
    void holdStillAllowsTheDecayToRelease() {
        PIDController controller = createController(DECAY_TIME_SEC);
        for (int i = 0; i < 100; i++) {
            controller.calculate(0, 1, LOOP_TIME_MS, LOOP_TIME_MS);
        }
        double start = controller.calculate(0, 1, LOOP_TIME_MS, LOOP_TIME_MS, true).getIntegralPart();
        double later = start;
        for (int i = 0; i < 50; i++) {
            later = controller.calculate(0, 1, LOOP_TIME_MS, LOOP_TIME_MS, true).getIntegralPart();
        }
        assertTrue(later < start, "a configured decay must still fade the held I-part");
    }

    @Test
    void holdDoesNotAffectTheProportionalPart() {
        PIDController controller = createController(0);
        double free = controller.calculate(0, 1, LOOP_TIME_MS, LOOP_TIME_MS).getProportionalPart();
        double held = controller.calculate(0, 1, LOOP_TIME_MS, LOOP_TIME_MS, true).getProportionalPart();

        assertEquals(free, held, 0.001);
    }

    @Test
    void fadeOutWorksWithoutDerivativeTimeConstant() {
        // the fade-out uses the unfiltered error change, so it does not depend on the derivative configuration
        PIDController withoutKdTimeConstant = new PIDController(1, 1, 0, 0, Double.NaN, Double.NaN, DECAY_TIME_SEC,
                false, Double.NaN, Double.NaN, Double.NaN);

        assertEquals(DECAY_TIME_SEC, settle(withoutKdTimeConstant, 0, 1, 500), DECAY_TIME_SEC * 0.1);
    }

    @Test
    void directionalHoldStillBlocksGrowth() {
        PIDController controller = createController(0, true);
        double held = settle(controller, 0, 1, 10);
        for (int i = 0; i < 50; i++) {
            held = controller.calculate(0, 1, LOOP_TIME_MS, LOOP_TIME_MS, true).getIntegralPart();
        }
        double after = controller.calculate(0, 1, LOOP_TIME_MS, LOOP_TIME_MS, true).getIntegralPart();
        assertEquals(held, after, 0.001, "a step away from zero must still be suspended");
    }

    @Test
    void directionalHoldLetsTheIPartComeBack() {
        PIDController controller = createController(0, true);
        // wind the I-part up on a positive error
        double wound = settle(controller, 0, 1, 20);
        assertTrue(wound > 0, "precondition: the I-part is positive");

        // the error reverses while the hold is still reported: the accumulator must unwind
        double recovering = wound;
        for (int i = 0; i < 5; i++) {
            recovering = controller.calculate(1, 0, LOOP_TIME_MS, LOOP_TIME_MS, true).getIntegralPart();
        }
        assertTrue(recovering < wound, "a step towards zero must pass through a directional hold");
    }

    @Test
    void aSymmetricHoldBlocksTheRecoveryStep() {
        // The contrast that motivates the directional form: with the plain hold the same reversed
        // error cannot bring the accumulator back, so the loop stays stuck at the held value.
        PIDController controller = createController(0, false);
        double wound = settle(controller, 0, 1, 20);
        double stuck = wound;
        for (int i = 0; i < 5; i++) {
            stuck = controller.calculate(1, 0, LOOP_TIME_MS, LOOP_TIME_MS, true).getIntegralPart();
        }
        assertEquals(wound, stuck, 0.001, "a symmetric hold blocks the recovery step too");
    }

    @Test
    void directionalHoldDoesNotOvershootThroughZero() {
        PIDController controller = createController(0, true);
        double wound = settle(controller, 0, 1, 3);
        assertTrue(wound > 0, "precondition: the I-part is positive");

        // A reversed error large enough to cross zero must not be able to wind the accumulator up
        // on the far side while the hold is still active, but it must still unwind: a single step
        // bigger than twice the accumulator used to be rejected whole, leaving it stuck.
        double crossed = controller.calculate(100, 0, LOOP_TIME_MS, LOOP_TIME_MS, true).getIntegralPart();
        assertEquals(0, crossed, 0.001, "one oversized recovery step must unwind to zero, not be rejected");

        for (int i = 0; i < 100; i++) {
            crossed = controller.calculate(100, 0, LOOP_TIME_MS, LOOP_TIME_MS, true).getIntegralPart();
        }
        assertEquals(0, crossed, 0.001, "and must not wind up on the far side while still held");
    }

    @Test
    void directionalHoldUnwindsGraduallyWhenTheStepIsSmall() {
        PIDController controller = createController(0, true);
        double wound = settle(controller, 0, 1, 5);
        assertTrue(wound > 0, "precondition: the I-part is positive");

        // A recovery step smaller than the accumulator is applied in full, so the unwind is
        // gradual rather than a jump to zero.
        double first = controller.calculate(0.5, 0, LOOP_TIME_MS, LOOP_TIME_MS, true).getIntegralPart();
        assertTrue(first < wound, "the accumulator must move towards zero");
        assertTrue(first > 0, "and must not jump past zero when the step does not reach it");
    }
}
