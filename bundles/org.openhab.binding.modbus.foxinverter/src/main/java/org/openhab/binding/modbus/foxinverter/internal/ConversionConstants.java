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
package org.openhab.binding.modbus.foxinverter.internal;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants for converting values.
 *
 * @author Sönke Küper - Initial contribution
 * @author Holger Friedrich - Carry over from SunGrow binding, additions
 */
@NonNullByDefault
final class ConversionConstants {

    private ConversionConstants() {
    }

    /**
     * Multiplicand for 0.1.
     */
    static final BigDecimal DIV_BY_TEN = new BigDecimal(BigInteger.ONE, 1);
    static final BigDecimal DIV_BY_HUNDRED = new BigDecimal(BigInteger.ONE, 2);
    static final BigDecimal DIV_BY_THOUSAND = new BigDecimal(BigInteger.ONE, 3);
}
