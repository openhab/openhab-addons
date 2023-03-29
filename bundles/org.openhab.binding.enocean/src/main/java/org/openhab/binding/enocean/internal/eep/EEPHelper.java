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
package org.openhab.binding.enocean.internal.eep;

import javax.measure.Unit;
import javax.measure.quantity.Energy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanChannelTotalusageConfig;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dominik Vorreiter - initial contribution
 *
 */
@NonNullByDefault
public abstract class EEPHelper {
    private static final Logger logger = LoggerFactory.getLogger(EEPHelper.class);

    public static State validateTotalUsage(State value, @Nullable State currentState, Configuration config) {
        EnOceanChannelTotalusageConfig c = config.as(EnOceanChannelTotalusageConfig.class);

        if (c.validateValue && (value instanceof QuantityType) && (currentState instanceof QuantityType)) {
            @SuppressWarnings("unchecked")
            QuantityType<Energy> newValue = value.as(QuantityType.class);

            if (newValue != null) {
                newValue = newValue.toUnit(Units.KILOWATT_HOUR);
            }

            @SuppressWarnings("unchecked")
            QuantityType<Energy> oldValue = currentState.as(QuantityType.class);

            if (oldValue != null) {
                oldValue = oldValue.toUnit(Units.KILOWATT_HOUR);
            }

            if ((newValue != null) && (oldValue != null)) {
                if (newValue.compareTo(oldValue) < 0) {
                    if ((oldValue.subtract(newValue).doubleValue() < 1.0)) {
                        return UnDefType.UNDEF;
                    }
                } else {
                    if (newValue.subtract(oldValue).doubleValue() > 10.0) {
                        return UnDefType.UNDEF;
                    }
                }
            }
        }

        return value;
    }

    public static boolean validateUnscaledValue(int unscaledValue, double unscaledMin, double unscaledMax) {
        if (unscaledValue < unscaledMin) {
            logger.debug("Unscaled value ({}) lower than the minimum allowed ({})", unscaledValue, unscaledMin);
            return false;
        } else if (unscaledValue > unscaledMax) {
            logger.debug("Unscaled value ({}) bigger than the maximum allowed ({})", unscaledValue, unscaledMax);
            return false;
        }

        return true;
    }

    public static State calculateState(int unscaledValue, double scaledMin, double scaledMax, double unscaledMin,
            double unscaledMax, Unit<?> unit) {

        if (!validateUnscaledValue(unscaledValue, unscaledMin, unscaledMax)) {
            return UnDefType.UNDEF;
        }

        double scaledValue = scaledMin + ((unscaledValue * (scaledMax - scaledMin)) / (unscaledMax - unscaledMin));
        return new QuantityType<>(scaledValue, unit);
    }
}
