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
    private final double outputLowerLimit;
    private final double outputUpperLimit;

    private double integralResult;
    private double derivativeResult;
    private double previousError;
    private double output;

    private double kp;
    private double ki;
    private double kd;
    private double derivativeTimeConstantSec;

    public PIDController(double outputLowerLimit, double outputUpperLimit, double kpAdjuster, double kiAdjuster,
            double kdAdjuster, double derivativeTimeConstantSec) {
        this.outputLowerLimit = outputLowerLimit;
        this.outputUpperLimit = outputUpperLimit;
        this.kp = kpAdjuster;
        this.ki = kiAdjuster;
        this.kd = kdAdjuster;
        this.derivativeTimeConstantSec = derivativeTimeConstantSec;
    }

    public PIDOutputDTO calculate(double input, double setpoint, long lastInvocationMs) {
        final double lastInvocationSec = lastInvocationMs / 1000d;
        final double error = setpoint - input;

        // derivative T1 calculation
        final double timeQuotient = lastInvocationSec / derivativeTimeConstantSec;
        if (derivativeTimeConstantSec != 0) {
            derivativeResult = LowpassFilter.calculate(derivativeResult, error - previousError, timeQuotient);
            previousError = error;
        }

        // integral calculation
        integralResult += error * lastInvocationSec;
        // limit to output limits
        if (ki != 0) {
            final double maxIntegral = outputUpperLimit / ki;
            final double minIntegral = outputLowerLimit / ki;
            integralResult = Math.min(maxIntegral, Math.max(minIntegral, integralResult));
        }

        // calculate parts
        final double proportionalPart = kp * error;
        final double integralPart = ki * integralResult;
        final double derivativePart = kd * derivativeResult;
        output = proportionalPart + integralPart + derivativePart;

        // limit output value
        output = Math.min(outputUpperLimit, Math.max(outputLowerLimit, output));

        return new PIDOutputDTO(output, proportionalPart, integralPart, derivativePart, error);
    }
}
