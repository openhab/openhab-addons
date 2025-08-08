/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.generic.values;

import static java.util.function.Predicate.not;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandDescriptionBuilder;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;

/**
 * Implements a text/string value. Allows to restrict the incoming value to a set of states.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class TextValue extends Value {
    private final @Nullable Map<String, String> states;
    private final @Nullable Map<String, String> commands;
    private final @Nullable Map<String, String> stateLabels;
    private final @Nullable Map<String, String> commandLabels;

    protected @Nullable String nullValue = null;

    /**
     * Create a string value with a limited number of allowed states and commands.
     *
     * @param states Allowed states. The key is the value that is received from MQTT,
     *            and the value is how matching values will be presented in openHAB.
     * @param commands Allowed commands. The key is the value that will be received by
     *            openHAB, and the value is how matching commands will be sent to MQTT.
     * @param stateLabels Labels for the states in the StateDescription. If a state is not found in this map, the state
     *            itself is used as label.
     *            Keys are the openHAB state, not the MQTT state.
     * @param commandLabels Labels for the commands in the CommandDescription. If a command is not found in this map,
     *            the command itself is used as label.
     */
    public TextValue(Map<String, String> states, Map<String, String> commands, Map<String, String> stateLabels,
            Map<String, String> commandLabels) {
        super(CoreItemFactory.STRING, List.of(StringType.class));
        if (!states.isEmpty()) {
            this.states = new LinkedHashMap(states);
        } else {
            this.states = null;
        }
        if (!commands.isEmpty()) {
            this.commands = new LinkedHashMap(commands);
        } else {
            this.commands = null;
        }
        if (!stateLabels.isEmpty()) {
            this.stateLabels = Map.copyOf(stateLabels);
        } else {
            this.stateLabels = null;
        }
        if (!commandLabels.isEmpty()) {
            this.commandLabels = Map.copyOf(commandLabels);
        } else {
            this.commandLabels = null;
        }
    }

    /**
     * Create a string value with a limited number of allowed states and commands.
     *
     * @param states Allowed states. The key is the value that is received from MQTT,
     *            and the value is how matching values will be presented in openHAB.
     * @param commands Allowed commands. The key is the value that will be received by
     *            openHAB, and the value is how matching commands will be sent to MQTT.
     */
    public TextValue(Map<String, String> states, Map<String, String> commands) {
        super(CoreItemFactory.STRING, List.of(StringType.class));
        if (!states.isEmpty()) {
            this.states = new LinkedHashMap(states);
        } else {
            this.states = null;
        }
        if (!commands.isEmpty()) {
            this.commands = new LinkedHashMap(commands);
        } else {
            this.commands = null;
        }
        this.stateLabels = null;
        this.commandLabels = null;
    }

    /**
     * Create a string value with a limited number of allowed states and commands.
     *
     * @param states Allowed states. Empty states are filtered out. If the resulting set is empty, all string values
     *            will be allowed.
     * @param commands Allowed commands. Empty commands are filtered out. If the resulting set is empty, all string
     *            values will be allowed.
     */
    public TextValue(String[] states, String[] commands) {
        super(CoreItemFactory.STRING, List.of(StringType.class));
        Map<String, String> s = Stream.of(states).filter(not(String::isBlank))
                .collect(Collectors.toMap(str -> str, str -> str, (a, b) -> a, LinkedHashMap::new));
        if (!s.isEmpty()) {
            this.states = s;
        } else {
            this.states = null;
        }
        Map<String, String> c = Stream.of(commands).filter(not(String::isBlank))
                .collect(Collectors.toMap(str -> str, str -> str, (a, b) -> a, LinkedHashMap::new));
        if (!c.isEmpty()) {
            this.commands = c;
        } else {
            this.commands = null;
        }
        this.stateLabels = null;
        this.commandLabels = null;
    }

    /**
     * Create a string value with a limited number of allowed states.
     *
     * @param states Allowed states. Empty states are filtered out. If the resulting set is empty, all string values
     *            will be allowed. This same array is also used for allowed commands.
     */
    public TextValue(String[] states) {
        this(states, states);
    }

    public TextValue() {
        super(CoreItemFactory.STRING, List.of(StringType.class));
        this.states = null;
        this.commands = null;
        this.stateLabels = null;
        this.commandLabels = null;
    }

    public void setNullValue(@Nullable String nullValue) {
        this.nullValue = nullValue;
    }

    @Override
    public StringType parseCommand(Command command) throws IllegalArgumentException {
        final Map<String, String> commands = this.commands;
        String valueStr = command.toString();
        if (commands != null) {
            if (!commands.containsKey(valueStr)) {
                throw new IllegalArgumentException("Value " + valueStr + " not within range");
            }
            return new StringType(commands.get(valueStr));
        }
        return new StringType(valueStr);
    }

    @Override
    public State parseMessage(Command command) throws IllegalArgumentException {
        if (command instanceof StringType string && string.toString().equals(nullValue)) {
            return UnDefType.NULL;
        }

        final Map<String, String> states = this.states;
        String valueStr = command.toString();
        if (states != null) {
            if (!states.containsKey(valueStr)) {
                if (valueStr.isEmpty()) {
                    return UnDefType.NULL;
                } else {
                    throw new IllegalArgumentException("Value " + valueStr + " not within range");
                }
            } else {
                return new StringType(states.get(valueStr));
            }
        }
        return new StringType(valueStr);
    }

    /**
     * @return valid states. Can be null.
     */
    public @Nullable Map<String, String> getStates() {
        return states;
    }

    @Override
    public StateDescriptionFragmentBuilder createStateDescription(boolean readOnly) {
        StateDescriptionFragmentBuilder builder = super.createStateDescription(readOnly);
        final Map<String, String> states = this.states;
        if (states != null) {
            states.forEach((ohState, mqttState) -> {
                String label = ohState;
                if (stateLabels != null) {
                    label = stateLabels.getOrDefault(ohState, ohState);
                }
                builder.withOption(new StateOption(ohState, label));
            });
        }
        return builder;
    }

    @Override
    public CommandDescriptionBuilder createCommandDescription() {
        CommandDescriptionBuilder builder = super.createCommandDescription();
        final Map<String, String> commands = this.commands;
        if (commands != null) {
            for (String command : commands.keySet()) {
                String label = command;
                if (commandLabels != null) {
                    label = commandLabels.getOrDefault(command, command);
                }
                builder.withCommandOption(new CommandOption(command, label));
            }
        }
        return builder;
    }
}
