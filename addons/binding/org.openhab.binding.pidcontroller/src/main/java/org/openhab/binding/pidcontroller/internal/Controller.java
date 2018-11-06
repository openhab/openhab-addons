/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

/**
 *
 * @author George Erhan - Initial contribution
 */

package org.openhab.binding.pidcontroller.internal;

import static org.openhab.binding.pidcontroller.PIDControllerBindingConstants.PID_RANGE_DEFAULT;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Controller} provides the necessary methods for retrieving part(s) of the PID calculations
 * and it provides the method for the overall PID calculations. It also resets the PID controller
 *
 * @author George Erhan - Initial contribution
 */

public class Controller {
    private Logger logger = LoggerFactory.getLogger(Controller.class);

    public int ControllerOutput;
    private BigDecimal Derivativeresult = BigDecimal.valueOf(0);
    public BigDecimal Proportionalresult = BigDecimal.valueOf(0);
    public BigDecimal Integralresult = BigDecimal.valueOf(0);
    public BigDecimal previousError = BigDecimal.valueOf(0);
    public BigDecimal Error = BigDecimal.valueOf(0);
    public BigDecimal maxIntegral = BigDecimal.valueOf(0);
    public BigDecimal Output = BigDecimal.valueOf(0);
    private BigDecimal Ku;
    private BigDecimal Kp;
    private BigDecimal Ki;
    private BigDecimal Kd;

    public BigDecimal PIDCalculation(BigDecimal PIDinput, BigDecimal PIDsetpoint, int LoopTime,
            BigDecimal PIDOutputLowerLimit, BigDecimal PIDOutputUpperLimit, BigDecimal Kpadjuster,
            BigDecimal Kiadjuster, BigDecimal Kdadjuster) {

        Ku = PIDOutputUpperLimit.subtract(PIDOutputLowerLimit).divide(BigDecimal.valueOf(PID_RANGE_DEFAULT));
        Kp = Kpadjuster.multiply(Ku);
        Ki = Kiadjuster.multiply(Ku.multiply(BigDecimal.valueOf(2)).divide(BigDecimal.valueOf(LoopTime)));
        Kd = Kdadjuster.multiply(Ku.multiply(BigDecimal.valueOf(LoopTime)));
        BigDecimal maxIntegral = PIDOutputUpperLimit.abs().subtract((Kp.multiply(Proportionalresult).abs())).divide(Ki);

        Error = PIDsetpoint.subtract(PIDinput);
        logger.debug("The calculated error is: {}", Error.floatValue());
        Proportionalresult = Error;
        logger.debug("Proportional part: {}", Kp.multiply(Proportionalresult));
        Integralresult = Integralresult.add(Error.multiply(BigDecimal.valueOf(LoopTime)));

        if (Integralresult.abs().compareTo(maxIntegral.abs()) == 1) {

            if (Output.compareTo(BigDecimal.valueOf(0)) == -1) {
                Integralresult = maxIntegral.negate();
            } else {
                Integralresult = maxIntegral;
            }
        }
        if ((Integralresult.compareTo(BigDecimal.valueOf(0)) == -1 && Error.compareTo(BigDecimal.valueOf(0)) == 1)
                || (Integralresult.compareTo(BigDecimal.valueOf(0)) == 1
                        && Error.compareTo(BigDecimal.valueOf(0)) == -1)) {
            Integralresult = BigDecimal.valueOf(0);
        }
        logger.debug("Integral part: {}", Ki.multiply(Integralresult));
        Derivativeresult = Error.subtract(previousError).divide(BigDecimal.valueOf(LoopTime));
        logger.debug("Derivative part: {}", Kd.multiply(Derivativeresult));
        Output = Kp.multiply(Proportionalresult).add(Ki.multiply(Integralresult)).add(Kd.multiply(Derivativeresult));
        previousError = Error;
        return Output;

    }

    public BigDecimal getProportionalpart() {
        BigDecimal proportional = Kp.multiply(Proportionalresult);
        return proportional;
    }

    public BigDecimal getIntegralpart() {
        BigDecimal integral = Ki.multiply(Integralresult);
        return integral;
    }

    public BigDecimal getDerivativepart() {
        BigDecimal derivative = Kd.multiply(Derivativeresult);
        return derivative;
    }

    public void ResetPID() {
        Derivativeresult = BigDecimal.valueOf(0);
        Proportionalresult = BigDecimal.valueOf(0);
        Integralresult = BigDecimal.valueOf(0);
        previousError = BigDecimal.valueOf(0);
        Error = BigDecimal.valueOf(0);
    }

}
