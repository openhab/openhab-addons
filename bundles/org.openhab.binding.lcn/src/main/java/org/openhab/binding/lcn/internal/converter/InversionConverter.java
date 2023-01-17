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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.State;

/**
 * Converter for all states representing antonyms.
 *
 * @author Thomas Weiler - Initial Contribution
 */
@NonNullByDefault
public class InversionConverter extends Converter {
    /**
     * Converts a state into its antonym where applicable.
     *
     * @param state to be inverted
     * @return inverted state
     */
    @Override
    public State onStateUpdateFromHandler(State state) {
        State convertedState = state;

        if (state instanceof OpenClosedType) {
            convertedState = state.equals(OpenClosedType.OPEN) ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
        } else if (state instanceof OnOffType) {
            convertedState = state.equals(OnOffType.ON) ? OnOffType.OFF : OnOffType.ON;
        } else if (state instanceof UpDownType) {
            convertedState = state.equals(UpDownType.UP) ? UpDownType.DOWN : UpDownType.UP;
        }

        return convertedState;
    }
}
