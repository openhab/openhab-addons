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
package org.openhab.persistence.jdbc.internal.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Queue;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Calculates the average/mean of a number series.
 *
 * @author Helmut Lehmeyer - Initial contribution
 */
@NonNullByDefault
public class MovingAverage {

    private final Queue<BigDecimal> win = new LinkedList<>();
    private final int period;
    private BigDecimal sum = BigDecimal.ZERO;

    public MovingAverage(int period) {
        assert period > 0 : "Period must be a positive integer";
        this.period = period;
    }

    public void add(Double num) {
        add(new BigDecimal(num));
    }

    public void add(Long num) {
        add(new BigDecimal(num));
    }

    public void add(Integer num) {
        add(new BigDecimal(num));
    }

    public void add(BigDecimal num) {
        sum = sum.add(num);
        win.add(num);
        if (win.size() > period) {
            sum = sum.subtract(win.remove());
        }
    }

    public BigDecimal getAverage() {
        if (win.isEmpty()) {
            return BigDecimal.ZERO; // technically the average is undefined
        }
        BigDecimal divisor = BigDecimal.valueOf(win.size());
        return sum.divide(divisor, 2, RoundingMode.HALF_UP);
    }

    public double getAverageDouble() {
        return getAverage().doubleValue();
    }

    public int getAverageInteger() {
        return getAverage().intValue();
    }
}
