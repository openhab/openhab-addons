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
package org.openhab.binding.http.internal.converter;

import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.http.internal.config.HttpChannelConfig;
import org.openhab.binding.http.internal.transform.ValueTransformation;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link GenericItemConverter} implements simple conversions for different item types
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class GenericItemConverter extends AbstractTransformingItemConverter {
    private final Function<String, State> toState;

    public GenericItemConverter(Function<String, State> toState, Consumer<State> updateState,
            Consumer<Command> postCommand, @Nullable Consumer<String> sendHttpValue,
            ValueTransformation stateTransformations, ValueTransformation commandTransformations,
            HttpChannelConfig channelConfig) {
        super(updateState, postCommand, sendHttpValue, stateTransformations, commandTransformations, channelConfig);
        this.toState = toState;
    }

    @Override
    protected State toState(String value) {
        try {
            return toState.apply(value);
        } catch (IllegalArgumentException e) {
            return UnDefType.UNDEF;
        }
    }

    @Override
    protected @Nullable Command toCommand(String value) {
        return null;
    }

    @Override
    protected String toString(Command command) {
        return command.toString();
    }
}
