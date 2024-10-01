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
package org.openhab.binding.freeathome.internal.valuestateconverter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * The {@link DecimalValueStateConverter} is a value converter for boolean values
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class BooleanValueStateConverter implements ValueStateConverter {

    @Override
    public State convertToState(String value) {
        return OnOffType.from(value);
    }

    @Override
    public String convertToValueString(State state) {
        if (state.equals(OnOffType.ON)) {
            return "1";
        }

        if (state.equals(OnOffType.OFF)) {
            return "0";
        }

        return "";
    }
}
