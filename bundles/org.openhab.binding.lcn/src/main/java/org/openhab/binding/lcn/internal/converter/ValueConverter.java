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

import java.util.function.Function;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;

/**
 * Base class for all LCN variable value converters.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class ValueConverter extends Converter {
    private @Nullable final Unit<?> unit;
    private final Function<Long, Double> toHuman;
    private final Function<Double, Long> toNative;

    public ValueConverter(@Nullable Unit<?> unit, Function<Long, Double> toHuman, Function<Double, Long> toNative) {
        this.unit = unit;
        this.toHuman = toHuman;
        this.toNative = toNative;
    }

    /**
     * Converts the given human readable value into the native LCN value.
     *
     * @param humanReadableValue the value to convert
     * @return the native value
     */
    protected long toNative(double humanReadableValue) {
        return toNative.apply(humanReadableValue);
    }

    /**
     * Converts the given native LCN value into a human readable value.
     *
     * @param nativeValue the value to convert
     * @return the human readable value
     */
    protected double toHumanReadable(long nativeValue) {
        return toHuman.apply(nativeValue);
    }

    /**
     * Converts a human readable value into LCN native value.
     *
     * @param humanReadable value to convert
     * @return the native LCN value
     */
    @Override
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
    @Override
    public DecimalType onCommandFromItem(QuantityType<?> quantityType) throws LcnException {
        Unit<?> localUnit = unit;
        if (localUnit == null) {
            return onCommandFromItem(quantityType.doubleValue());
        }

        QuantityType<?> quantityInBaseUnit = quantityType.toUnit(localUnit);

        if (quantityInBaseUnit != null) {
            return onCommandFromItem(quantityInBaseUnit.doubleValue());
        } else {
            throw new LcnException(quantityType + ": Incompatible Channel unit configured: " + localUnit);
        }
    }

    /**
     * Converts a state update from the Thing into a human readable unit.
     *
     * @param state from the Thing
     * @return human readable State
     * @throws LcnException
     */
    @Override
    public State onStateUpdateFromHandler(State state) throws LcnException {
        if (state instanceof DecimalType) {
            Unit<?> localUnit = unit;
            if (localUnit != null) {
                return QuantityType.valueOf(toHumanReadable(((DecimalType) state).longValue()), localUnit);
            }

            return state;
        } else {
            throw new LcnException("Unexpected state type: Was " + state.getClass().getSimpleName()
                    + " but expected DecimalType: " + state);
        }
    }
}
