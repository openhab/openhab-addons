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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;

/**
 * Base class for all converters.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class Converter {
    /**
     * Converts a state update from the Thing into a human readable representational State.
     *
     * @param state from the Thing
     * @return human readable representational State
     * @throws LcnException
     */
    public State onStateUpdateFromHandler(State state) throws LcnException {
        return state;
    }

    /**
     * Converts a human readable value into LCN native value.
     *
     * @param humanReadable value to convert
     * @return the native LCN value
     */
    public DecimalType onCommandFromItem(double humanReadable) {
        return new DecimalType(humanReadable);
    }

    /**
     * Converts a human readable value into LCN native value.
     *
     * @param humanReadable value to convert
     * @return the native LCN value
     * @throws LcnException when the value could not be converted to the base unit
     */
    public DecimalType onCommandFromItem(QuantityType<?> quantityType) throws LcnException {
        return onCommandFromItem(quantityType.doubleValue());
    }
}
