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

import static org.openhab.binding.pidcontroller.PIDControllerBindingConstants.PIDrangeDefault;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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

    public BigDecimal PIDCalculation(BigDecimal PIDinput, BigDecimal PIDsetpoint, int LoopTime,
            BigDecimal PIDOutputLowerLimit, BigDecimal PIDOutputUpperLimit, BigDecimal Kpadjuster,
            BigDecimal Kiadjuster, BigDecimal Kdadjuster) {

        BigDecimal Ku = PIDOutputUpperLimit.subtract(PIDOutputLowerLimit).divide(BigDecimal.valueOf(PIDrangeDefault));
        BigDecimal Kp = Kpadjuster.multiply(Ku);
        BigDecimal Ki = Kiadjuster.multiply(Ku.multiply(BigDecimal.valueOf(2)).divide(BigDecimal.valueOf(LoopTime)));
        BigDecimal Kd = Kiadjuster.multiply(Ku.multiply(BigDecimal.valueOf(LoopTime)));
        BigDecimal maxIntegral = PIDOutputUpperLimit.abs().subtract((Kp.multiply(Proportionalresult).abs())).divide(Ki);

        Error = PIDsetpoint.subtract(PIDinput);
        logger.debug("Eroarea: {}", Error.floatValue());
        Proportionalresult = Error;

        Integralresult = Integralresult.add(Error.multiply(BigDecimal.valueOf(LoopTime)));
        logger.debug("Integral: {}", Integralresult);
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
        logger.debug("Partea integrala: {}", Integralresult);
        logger.debug("Eroarea: {}", Error);
        Derivativeresult = Error.subtract(previousError).divide(BigDecimal.valueOf(LoopTime));
        Output = Kp.multiply(Proportionalresult).add(Ki.multiply(Integralresult)).add(Kd.multiply(Derivativeresult));
        previousError = Error;
        return Output;

    }

    public BigDecimal getProportionalpart() {
        BigDecimal proportional = Proportionalresult;
        return proportional;
    }

    public BigDecimal getIntegralpart() {
        BigDecimal integral = Integralresult;
        return integral;
    }

    public BigDecimal getDerivativepart() {
        BigDecimal derivative = Derivativeresult;
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