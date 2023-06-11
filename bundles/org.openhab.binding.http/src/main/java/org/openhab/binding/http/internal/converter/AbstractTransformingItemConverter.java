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
import org.openhab.binding.http.internal.config.HttpChannelMode;
import org.openhab.binding.http.internal.http.Content;
import org.openhab.binding.http.internal.transform.ValueTransformation;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link AbstractTransformingItemConverter} is a base class for an item converter with transformations
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractTransformingItemConverter implements ItemValueConverter {
    private final Consumer<State> updateState;
    private final Consumer<Command> postCommand;
    private final @Nullable Consumer<String> sendHttpValue;
    private final ValueTransformation stateTransformations;
    private final ValueTransformation commandTransformations;

    protected HttpChannelConfig channelConfig;

    public AbstractTransformingItemConverter(Consumer<State> updateState, Consumer<Command> postCommand,
            @Nullable Consumer<String> sendHttpValue, ValueTransformation stateTransformations,
            ValueTransformation commandTransformations, HttpChannelConfig channelConfig) {
        this.updateState = updateState;
        this.postCommand = postCommand;
        this.sendHttpValue = sendHttpValue;
        this.stateTransformations = stateTransformations;
        this.commandTransformations = commandTransformations;
        this.channelConfig = channelConfig;
    }

    @Override
    public void process(Content content) {
        if (channelConfig.mode != HttpChannelMode.WRITEONLY) {
            stateTransformations.apply(content.getAsString()).ifPresent(transformedValue -> {
                Command command = toCommand(transformedValue);
                if (command != null) {
                    postCommand.accept(command);
                } else {
                    updateState.accept(toState(transformedValue));
                }
            });
        } else {
            throw new IllegalStateException("Write-only channel");
        }
    }

    @Override
    public void send(Command command) {
        Consumer<String> sendHttpValue = this.sendHttpValue;
        if (sendHttpValue != null && channelConfig.mode != HttpChannelMode.READONLY) {
            commandTransformations.apply(toString(command)).ifPresent(sendHttpValue);
        } else {
            throw new IllegalStateException("Read-only channel");
        }
    }

    /**
     * check if this converter received a value that needs to be sent as command
     *
     * @param value the value
     * @return the command or null
     */
    protected abstract @Nullable Command toCommand(String value);

    /**
     * convert the received value to a state
     *
     * @param value the value
     * @return the state that represents the value of UNDEF if conversion failed
     */
    protected abstract State toState(String value);

    /**
     * convert a command to a string
     *
     * @param command the command
     * @return the string representation of the command
     */
    protected abstract String toString(Command command);

    @FunctionalInterface
    public interface Factory {
        ItemValueConverter create(Consumer<State> updateState, Consumer<Command> postCommand,
                @Nullable Consumer<String> sendHttpValue, ValueTransformation stateTransformations,
                ValueTransformation commandTransformations, HttpChannelConfig channelConfig);
    }
}
