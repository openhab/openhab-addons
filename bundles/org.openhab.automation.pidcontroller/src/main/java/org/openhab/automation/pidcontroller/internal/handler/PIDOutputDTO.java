/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

/**
 *
 * @author Fabian Wolter - Initial Contribution
 */
public class PIDOutputDTO {
    private double output;
    private double proportionalPart;
    private double integralPart;
    private double derivativePart;
    private double error;

    public PIDOutputDTO(double output, double proportionalPart, double integralPart, double derivativePart,
            double error) {
        this.output = output;
        this.proportionalPart = proportionalPart;
        this.integralPart = integralPart;
        this.derivativePart = derivativePart;
        this.error = error;
    }

    public double getOutput() {
        return output;
    }

    public double getProportionalPart() {
        return proportionalPart;
    }

    public double getIntegralPart() {
        return integralPart;
    }

    public double getDerivativePart() {
        return derivativePart;
    }

    public double getError() {
        return error;
    }
}
