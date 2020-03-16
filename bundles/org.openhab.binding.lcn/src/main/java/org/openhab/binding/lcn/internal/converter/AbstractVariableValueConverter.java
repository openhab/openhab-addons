/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all LCN variable value converters.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public abstract class AbstractVariableValueConverter {
    private final Logger logger = LoggerFactory.getLogger(AbstractVariableValueConverter.class);

    /**
     * Gets the Profile's Unit.
     *
     * @return the Unit
     */
    protected abstract Unit<?> getUnitType();

    /**
     * Converts the given human readable value into the native LCN value.
     *
     * @param humanReadableValue the value to convert
     * @return the native value
     */
    protected abstract int toNative(double humanReadableValue);

    /**
     * Converts the given native LCN value into a human readable value.
     *
     * @param nativeValue the value to convert
     * @return the human readable value
     */
    protected abstract double toHumanReadable(long nativeValue);

    /**
     * Converts a human readable value into LCN native value.
     *
     * @param humanReadable value to convert
     * @return the native LCN value
     */
    public DecimalType onCommandFromItem(double humanReadable) {
        return new DecimalType(toNative(humanReadable));
    }

    /**
     * Converts a human readable value into LCN native value.
     *
     * @param humanReadable value to convert
     * @return the native LCN value
     * @throws LcnException when the value could not be converted to the base unit
     */
    public DecimalType onCommandFromItem(QuantityType<?> quantityType) throws LcnException {
        QuantityType<?> quantityInBaseUnit = quantityType.toUnit(getUnitType());

        if (quantityInBaseUnit != null) {
            return onCommandFromItem(quantityInBaseUnit.doubleValue());
        } else {
            throw new LcnException(quantityType + ": Incompatible unit: " + getUnitType());
        }
    }

    /**
     * Converts a state update from the Thing into a human readable unit.
     *
     * @param state from the Thing
     * @return human readable State
     */
    public State onStateUpdateFromHandler(State state) {
        State result = state;

        if (state instanceof DecimalType) {
            result = QuantityType.valueOf(toHumanReadable(((DecimalType) state).longValue()), getUnitType());
        } else {
            logger.warn("Unexpected state type: {}", state.getClass().getSimpleName());
        }

        return result;
    }
}
