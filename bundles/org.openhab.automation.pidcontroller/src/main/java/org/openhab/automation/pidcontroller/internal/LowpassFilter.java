/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.automation.pidcontroller.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Realizes a first-order FIR low pass filter. To keep code complexity low, it is implemented as moving average (all
 * FIR coefficients are set to normalized ones).
 *
 * The exponential decaying function is used for the calculation (see https://en.wikipedia.org/wiki/Time_constant). That
 * means the output value is approx. 63% of the input value after one time constant and approx. 99% after 5 time
 * constants.
 *
 * @author Fabian Wolter - Initial contribution
 *
 */
@NonNullByDefault
public class LowpassFilter {
    /**
     * Executes one low pass filter step.
     *
     * @param lastOutput the current filter value (result of the last invocation)
     * @param newValue the just sampled value
     * @param timeQuotient quotient of the current time and the time constant
     * @return the new filter value
     */
    public static double calculate(double lastOutput, double newValue, double timeQuotient) {
        double output = lastOutput * Math.exp(-timeQuotient);
        output += newValue * (1 - Math.exp(-timeQuotient));

        return output;
    }
}
