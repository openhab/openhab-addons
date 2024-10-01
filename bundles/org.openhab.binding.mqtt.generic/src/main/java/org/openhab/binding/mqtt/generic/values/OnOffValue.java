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
package org.openhab.binding.mqtt.generic.values;

import static java.util.function.Predicate.not;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandDescriptionBuilder;
import org.openhab.core.types.CommandOption;

/**
 * Implements an on/off boolean value.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class OnOffValue extends Value {
    private final Set<String> onStates;
    private final Set<String> offStates;
    private final String onCommand;
    private final String offCommand;

    /**
     * Creates a switch On/Off type, that accepts "ON" for on and "OFF" for off.
     */
    public OnOffValue() {
        this(OnOffType.ON.name(), OnOffType.OFF.name());
    }

    /**
     * Creates a new SWITCH On/Off value.
     *
     * values send in messages will be the same as those expected in incoming messages
     *
     * @param onValue The ON value string. This will be compared to MQTT messages. Defaults to "ON".
     * @param offValue The OFF value string. This will be compared to MQTT messages. Defaults to "OFF".
     */
    public OnOffValue(@Nullable String onValue, @Nullable String offValue) {
        this(onValue, offValue, onValue, offValue);
    }

    /**
     * Creates a new SWITCH On/Off value.
     *
     * @param onState The ON value string. This will be compared to MQTT messages. Defaults to onCommand if null, or
     *            "ON" if both are null.
     * @param offState The OFF value string. This will be compared to MQTT messages. Defaults to offComamand if null, or
     *            "OFF" if both are null.
     * @param onCommand The ON value string. This will be send in MQTT messages. Defaults to onState if null, or "ON" if
     *            both are null.
     * @param offCommand The OFF value string. This will be send in MQTT messages. Defaults to offCommand if null, or
     *            "OFF" if both are null.
     */
    public OnOffValue(@Nullable String onState, @Nullable String offState, @Nullable String onCommand,
            @Nullable String offCommand) {
        this(new String[] { defaultArgument(onState, onCommand, OnOffType.ON.name()) },
                new String[] { defaultArgument(offState, offCommand, OnOffType.OFF.name()) },
                defaultArgument(onCommand, onState, OnOffType.ON.name()),
                defaultArgument(offCommand, offState, OnOffType.OFF.name()));
    }

    /**
     * Creates a new SWITCH On/Off value.
     *
     * @param onStates A list of valid ON value strings. This will be compared to MQTT messages.
     * @param offStates A list of valid OFF value strings. This will be compared to MQTT messages.
     * @param onCommand The ON value string. This will be send in MQTT messages.
     * @param offCommand The OFF value string. This will be send in MQTT messages.
     */
    public OnOffValue(String[] onStates, String[] offStates, String onCommand, String offCommand) {
        super(CoreItemFactory.SWITCH, List.of(OnOffType.class, StringType.class));
        this.onStates = Stream.of(onStates).filter(not(String::isBlank)).collect(Collectors.toSet());
        this.offStates = Stream.of(offStates).filter(not(String::isBlank)).collect(Collectors.toSet());
        this.onCommand = onCommand;
        this.offCommand = offCommand;
    }

    @Override
    public OnOffType parseCommand(Command command) throws IllegalArgumentException {
        if (command instanceof OnOffType onOffCommand) {
            return onOffCommand;
        } else {
            final String updatedValue = command.toString();
            if (onStates.contains(updatedValue)) {
                return OnOffType.ON;
            } else if (offStates.contains(updatedValue)) {
                return OnOffType.OFF;
            } else {
                return OnOffType.valueOf(updatedValue);
            }
        }
    }

    @Override
    public String getMQTTpublishValue(Command command, @Nullable String pattern) {
        String formatPattern = pattern;
        if (formatPattern == null) {
            formatPattern = "%s";
        }

        return String.format(formatPattern, command == OnOffType.ON ? onCommand : offCommand);
    }

    @Override
    public CommandDescriptionBuilder createCommandDescription() {
        CommandDescriptionBuilder builder = super.createCommandDescription();
        builder = builder.withCommandOption(new CommandOption(onCommand, onCommand));
        builder = builder.withCommandOption(new CommandOption(offCommand, offCommand));
        return builder;
    }

    private static String defaultArgument(@Nullable String arg1, @Nullable String arg2, String defaultValue) {
        String result = arg1;
        if (result == null) {
            result = arg2;
        }
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }
}
