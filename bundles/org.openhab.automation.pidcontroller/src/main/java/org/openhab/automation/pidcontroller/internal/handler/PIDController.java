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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.pidcontroller.internal.LowpassFilter;

/**
 * The {@link PIDController} provides the necessary methods for retrieving part(s) of the PID calculations
 * and it provides the method for the overall PID calculations. It also resets the PID controller
 *
 * @author George Erhan - Initial contribution
 * @author Hilbrand Bouwkamp - Adapted for new rule engine
 * @author Fabian Wolter - Add T1 to D part, add debugging ability for PID values
 */
@NonNullByDefault
class PIDController {
    private double integralResult;
    private double derivativeResult;
    private double previousError;

    private double kp;
    private double ki;
    private double kd;
    private double derivativeTimeConstantSec;
    private double iMinResult;
    private double iMaxResult;
    private double integralDecayTimeSec;
    private boolean directionalIntegralHold;

    public PIDController(double kpAdjuster, double kiAdjuster, double kdAdjuster, double derivativeTimeConstantSec,
            double iMinValue, double iMaxValue, double integralDecayTimeSec, boolean directionalIntegralHold,
            double previousIntegralPart, double previousDerivativePart, double previousError) {
        this.kp = kpAdjuster;
        this.ki = kiAdjuster;
        this.kd = kdAdjuster;
        this.derivativeTimeConstantSec = derivativeTimeConstantSec;
        this.integralDecayTimeSec = integralDecayTimeSec;
        this.directionalIntegralHold = directionalIntegralHold;
        this.iMinResult = Double.NaN;
        this.iMaxResult = Double.NaN;

        // prepare min/max, restore previous state for the integral result accumulator
        if (Double.isFinite(kiAdjuster) && Math.abs(kiAdjuster) > 0.0) {
            if (Double.isFinite(iMinValue)) {
                this.iMinResult = iMinValue / kiAdjuster;
            }
            if (Double.isFinite(iMaxValue)) {
                this.iMaxResult = iMaxValue / kiAdjuster;
            }
            if (Double.isFinite(previousIntegralPart)) {
                this.integralResult = previousIntegralPart / kiAdjuster;
            }
        }

        // restore previous state for the derivative result accumulator
        if (Double.isFinite(kdAdjuster) && Math.abs(kdAdjuster) > 0.0) {
            if (Double.isFinite(previousDerivativePart)) {
                this.derivativeResult = previousDerivativePart / kdAdjuster;
            }
        }

        // restore previous state for the previous error variable
        if (Double.isFinite(previousError)) {
            this.previousError = previousError;
        }
    }

    public PIDOutputDTO calculate(double input, double setpoint, long lastInvocationMs, int loopTimeMs) {
        return calculate(input, setpoint, lastInvocationMs, loopTimeMs, false);
    }

    public PIDOutputDTO calculate(double input, double setpoint, long lastInvocationMs, int loopTimeMs,
            boolean integralHold) {
        final double lastInvocationSec = lastInvocationMs / 1000d;
        final double error = setpoint - input;
        final double errorChange = error - previousError;

        // derivative T1 calculation
        final double timeQuotient = lastInvocationSec / derivativeTimeConstantSec;
        if (derivativeTimeConstantSec != 0) {
            derivativeResult = LowpassFilter.calculate(derivativeResult, errorChange, timeQuotient);
        }
        previousError = error;

        // integral calculation
        //
        // The hold suspends accumulation while the caller reports that the actuator cannot act on the process, for
        // instance a mixing damper whose supply air is on the wrong side of the room temperature. The controller
        // cannot detect that itself: the output is not saturated and the error is real, but nothing the controller
        // does can reduce it, so integrating simply winds the I-part to its limit and mutes the loop once the plant
        // recovers. What is already accumulated is deliberately kept, because it still describes the steady-state
        // action the process needs; only the growth is suspended.
        //
        // A plain hold is symmetric: it also blocks the step that would bring the accumulator back, so a loop held
        // through a long un-actuatable period stays stuck at whatever it had reached even after the process starts
        // moving the right way again. Directional holding suspends only the step that pushes the accumulator further
        // from zero and lets the opposite step through, which is the conditional-integration form of anti-windup. The
        // caller can then report the plant condition continuously, without having to decide when to stop reporting it
        // for the loop to be able to recover.
        final double integralStep = error * lastInvocationMs / loopTimeMs;
        if (!integralHold) {
            integralResult += integralStep;
        } else if (directionalIntegralHold) {
            // Apply only the part of the step that moves the accumulator towards zero.
            //
            // Comparing the endpoints instead, |result + step| < |result|, rejects a
            // recovery step outright once it is larger than twice the accumulator, because
            // overshooting zero lands further out than it started. The accumulator then
            // sticks at its current value for as long as the hold is asserted, which is the
            // opposite of what directional holding is for. Clamping at zero keeps the
            // intent - never wind further out, always allow unwinding - without that gap.
            if (integralResult > 0 && integralStep < 0) {
                integralResult = Math.max(0, integralResult + integralStep);
            } else if (integralResult < 0 && integralStep > 0) {
                integralResult = Math.min(0, integralResult + integralStep);
            }
        }

        // Integral decay: bleed the accumulator towards zero while the error is no longer moving away from the
        // setpoint.
        //
        // A plain integrator only unwinds on an error of the opposite sign. A process that settles slightly off its
        // setpoint (discrete actuators, or a load the plant cannot fully serve) therefore holds a saturated I-part
        // indefinitely, because the error never changes sign. Decaying releases that accumulation without requiring
        // an overshoot first.
        //
        // The decay is suppressed while the error is still growing in the direction the I-part is already pushing,
        // which is exactly when the integral action is needed. That is tested as the product of the error change and
        // the accumulator: a positive product means both have the same sign, i.e. the deviation is still increasing.
        // Using the product keeps the behaviour symmetric for negative setpoint deviations.
        //
        // The unfiltered error change is used here on purpose. The filtered derivative approaches zero only
        // asymptotically, so it never becomes exactly zero for a constant error, which is the very case this decay
        // addresses.
        if (integralDecayTimeSec > 0 && errorChange * integralResult <= 0) {
            integralResult *= Math.exp(-lastInvocationSec / integralDecayTimeSec);
        }

        if (Double.isFinite(iMinResult)) {
            integralResult = Math.max(integralResult, iMinResult);
        }
        if (Double.isFinite(iMaxResult)) {
            integralResult = Math.min(integralResult, iMaxResult);
        }

        // calculate parts
        final double proportionalPart = kp * error;

        double integralPart = ki * integralResult;

        final double derivativePart = kd * derivativeResult;

        final double output = proportionalPart + integralPart + derivativePart;

        return new PIDOutputDTO(output, proportionalPart, integralPart, derivativePart, error);
    }

    public void setIntegralResult(double integralResult) {
        this.integralResult = integralResult;
    }

    public void setDerivativeResult(double derivativeResult) {
        this.derivativeResult = derivativeResult;
    }
}
