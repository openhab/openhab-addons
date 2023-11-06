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
package org.openhab.binding.http.internal.converter;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.http.internal.config.HttpChannelConfig;
import org.openhab.binding.http.internal.transform.ValueTransformation;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link NumberItemConverter} implements {@link org.openhab.core.library.items.NumberItem} conversions
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class NumberItemConverter extends AbstractTransformingItemConverter {

    public NumberItemConverter(Consumer<State> updateState, Consumer<Command> postCommand,
            @Nullable Consumer<String> sendHttpValue, ValueTransformation stateTransformations,
            ValueTransformation commandTransformations, HttpChannelConfig channelConfig) {
        super(updateState, postCommand, sendHttpValue, stateTransformations, commandTransformations, channelConfig);
    }

    @Override
    protected @Nullable Command toCommand(String value) {
        return null;
    }

    @Override
    protected State toState(String value) {
        String trimmedValue = value.trim();
        if (!trimmedValue.isEmpty()) {
            try {
                if (channelConfig.unit != null) {
                    // we have a given unit - use that
                    return new QuantityType<>(trimmedValue + " " + channelConfig.unit);
                } else {
                    try {
                        // try if we have a simple number
                        return new DecimalType(trimmedValue);
                    } catch (IllegalArgumentException e1) {
                        // not a plain number, maybe with unit?
                        return new QuantityType<>(trimmedValue);
                    }
                }
            } catch (IllegalArgumentException e) {
                // finally failed
            }
        }
        return UnDefType.UNDEF;
    }

    @Override
    protected String toString(Command command) {
        return command.toString();
    }
}
