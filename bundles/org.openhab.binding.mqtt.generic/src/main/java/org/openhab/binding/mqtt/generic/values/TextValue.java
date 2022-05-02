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
package org.openhab.binding.mqtt.generic.values;

import static java.util.function.Predicate.not;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandDescriptionBuilder;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;

/**
 * Implements a text/string value. Allows to restrict the incoming value to a set of states.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class TextValue extends Value {
    private final @Nullable Set<String> states;

    /**
     * Create a string value with a limited number of allowed states.
     *
     * @param states Allowed states. Empty states are filtered out. If the resulting set is empty, all string values
     *            will be allowed.
     */
    public TextValue(String[] states) {
        super(CoreItemFactory.STRING, List.of(StringType.class));
        Set<String> s = Stream.of(states).filter(not(String::isBlank)).collect(Collectors.toSet());
        if (!s.isEmpty()) {
            this.states = s;
        } else {
            this.states = null;
        }
    }

    public TextValue() {
        super(CoreItemFactory.STRING, List.of(StringType.class));
        this.states = null;
    }

    @Override
    public void update(Command command) throws IllegalArgumentException {
        final Set<String> states = this.states;
        String valueStr = command.toString();
        if (states != null && !states.contains(valueStr)) {
            throw new IllegalArgumentException("Value " + valueStr + " not within range");
        }
        state = new StringType(valueStr);
    }

    /**
     * @return valid states. Can be null.
     */
    public @Nullable Set<String> getStates() {
        return states;
    }

    @Override
    public StateDescriptionFragmentBuilder createStateDescription(boolean readOnly) {
        StateDescriptionFragmentBuilder builder = super.createStateDescription(readOnly);
        final Set<String> states = this.states;
        if (states != null) {
            for (String state : states) {
                builder = builder.withOption(new StateOption(state, state));
            }
        }
        return builder;
    }

    @Override
    public CommandDescriptionBuilder createCommandDescription() {
        CommandDescriptionBuilder builder = super.createCommandDescription();
        final Set<String> commands = this.states;
        if (states != null) {
            for (String command : commands) {
                builder = builder.withCommandOption(new CommandOption(command, command));
            }
        }
        return builder;
    }
}
