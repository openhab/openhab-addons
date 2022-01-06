/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link FixedValueMappingItemConverter} implements mapping conversions for different item-types
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public class FixedValueMappingItemConverter extends AbstractTransformingItemConverter {

    public FixedValueMappingItemConverter(Consumer<State> updateState, Consumer<Command> postCommand,
            @Nullable Consumer<String> sendHttpValue, ValueTransformation stateTransformations,
            ValueTransformation commandTransformations, HttpChannelConfig channelConfig) {
        super(updateState, postCommand, sendHttpValue, stateTransformations, commandTransformations, channelConfig);
    }

    @Override
    protected @Nullable Command toCommand(String value) {
        return null;
    }

    @Override
    public String toString(Command command) {
        String value = channelConfig.commandToFixedValue(command);
        if (value != null) {
            return value;
        }

        throw new IllegalArgumentException(
                "Command type '" + command.toString() + "' not supported or mapping not defined.");
    }

    @Override
    public State toState(String string) {
        State state = channelConfig.fixedValueToState(string);

        return state != null ? state : UnDefType.UNDEF;
    }
}
