/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal.converter;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for S0 counter value converters.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class S0Converter extends ValueConverter {
    private final Logger logger = LoggerFactory.getLogger(S0Converter.class);
    protected double pulsesPerKwh;

    public S0Converter(@Nullable Object parameter) {
        super(Units.WATT, n -> 0d, h -> 0L);

        if (parameter == null) {
            pulsesPerKwh = 1000;
            logger.debug("Pulses per kWh not set. Assuming 1000 imp./kWh.");
        } else if (parameter instanceof BigDecimal decimalValue) {
            pulsesPerKwh = decimalValue.doubleValue();
        } else {
            logger.warn("Could not parse 'pulses', unexpected type, should be float or integer: {}", parameter);
        }
    }

    @Override
    public long toNative(double value) {
        return Math.round(value * pulsesPerKwh / 1000);
    }

    @Override
    public double toHumanReadable(long value) {
        return value / pulsesPerKwh * 1000;
    }
}
