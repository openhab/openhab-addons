/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import javax.measure.quantity.Energy;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.config.EnOceanChannelTotalusageConfig;

/**
 *
 * @author Dominik Vorreiter - initial contribution
 *
 */
public abstract class EEPHelper {
    public static State validateTotalUsage(State value, State currentState, Configuration config) {
        EnOceanChannelTotalusageConfig c = config.as(EnOceanChannelTotalusageConfig.class);

        if (c.validateValue && (value instanceof QuantityType) && (currentState instanceof QuantityType)) {
            @SuppressWarnings("unchecked")
            QuantityType<Energy> newValue = value.as(QuantityType.class);

            if (newValue != null) {
                newValue = newValue.toUnit(SmartHomeUnits.KILOWATT_HOUR);
            }

            @SuppressWarnings("unchecked")
            QuantityType<Energy> oldValue = currentState.as(QuantityType.class);

            if (oldValue != null) {
                oldValue = oldValue.toUnit(SmartHomeUnits.KILOWATT_HOUR);
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
}
