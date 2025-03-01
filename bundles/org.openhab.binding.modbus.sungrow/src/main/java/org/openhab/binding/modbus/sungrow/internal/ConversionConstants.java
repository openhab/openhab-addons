/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sungrow.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants for converting values.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
final class ConversionConstants {

    private ConversionConstants() {
    }

    /**
     * Multiplicand for 0.001.
     */
    static final BigDecimal DIV_BY_THOUSAND = new BigDecimal(BigInteger.ONE, 3);

    /**
     * Multiplicand for 0.01.
     */
    static final BigDecimal DIV_BY_HUNDRED = new BigDecimal(BigInteger.ONE, 2);

    /**
     * Multiplicand for 0.1.
     */
    static final BigDecimal DIV_BY_TEN = new BigDecimal(BigInteger.ONE, 1);

    /**
     * Multiplicand for 1.
     */
    static final BigDecimal ONE = BigDecimal.ONE;
    /**
     * Multiplicand for 10.
     */
    static final BigDecimal MULTI_BY_TEN = new BigDecimal(BigInteger.ONE, -1);

    /**
     * Multiplicand for 100.
     */
    static final BigDecimal MULTI_BY_HUNDRED = new BigDecimal(BigInteger.ONE, -2);

    /**
     * Multiplicand for 1.000.
     */
    static final BigDecimal MULTI_BY_THOUSAND = new BigDecimal(BigInteger.ONE, -3);

    /**
     * Value conversion from Celsius to Kelvin.
     */
    static final Function<BigDecimal, BigDecimal> CELSIUS_TO_KELVIN = (BigDecimal celsius) -> celsius
            .add(new BigDecimal("273.15"));
}
