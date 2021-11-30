/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

package org.openhab.binding.freeathomesystem.internal.valuestateconverters;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * The {@link DecimalValueStateConverter} is a value converter for integer values with a specific mask
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class BinaryValueStateConverter implements ValueStateConverter {

    private int maskValue;

    public BinaryValueStateConverter(int mask) {
        maskValue = mask;
    }

    @Override
    public State convertToState(String value) {

        int intValue = Integer.decode(value);
        int result;

        result = intValue & maskValue;

        if (maskValue == result) {
            return OnOffType.ON;
        } else {
            return OnOffType.OFF;
        }
    }
}
