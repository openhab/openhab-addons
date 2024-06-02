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
package org.openhab.binding.smartmeter.internal.conformity.negate;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartmeter.internal.MeterValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the Negate Bit property for a specific meter value.
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public class NegateHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NegateHandler.class);

    /**
     * Gets whether negation should be applied for the given <code>negateProperty</code> and the {@link MeterValue}
     * provided by the <code>getObisValueFunction</code>
     *
     * @param negateProperty The negate property (in form {@code <OBIS>:<POSITION>:<BIT_SET>})
     * @param getObisValueFunction The function to get the {@link MeterValue} from an OBIS code.
     * @return whether to negate or not.
     */
    public static boolean shouldNegateState(String negateProperty,
            Function<String, @Nullable MeterValue<?>> getObisValueFunction) {
        NegateBitModel negateModel = NegateBitParser.parseNegateProperty(negateProperty);
        MeterValue<?> value = getObisValueFunction.apply(negateModel.getNegateChannelId());
        boolean isStatus = negateModel.isStatus();
        if (value != null) {
            String status = value.getStatus();
            String stringValue;
            if (isStatus && status != null) {
                stringValue = status;
            } else {
                stringValue = value.getValue();
            }
            boolean negateBit = isNegateSet(stringValue, negateModel.getNegatePosition());

            return negateBit == negateModel.isNegateBit();
        } else {
            return false;
        }
    }

    /**
     * Gets whether the bit at position <code>negatePosition</code> is set or not.
     *
     * @param value The value which must be a number to check the bit
     * @param negatePosition The position to check
     * @return Whether the given bit is set or not
     */
    public static boolean isNegateSet(String value, int negatePosition) {
        long longValue = 0;
        try {
            longValue = (long) Double.parseDouble(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("Failed to parse value: {} when determining isNegateSet, assuming false", value);
            return false;
        }
        return (longValue & (1L << negatePosition)) != 0;
    }
}
