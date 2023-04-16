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
package org.openhab.binding.mqtt.generic.values;

import java.util.List;

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
    private final String onState;
    private final String offState;
    private final String onCommand;
    private final String offCommand;

    /**
     * Creates a switch On/Off type, that accepts "ON", "1" for on and "OFF","0" for off.
     */
    public OnOffValue() {
        this(OnOffType.ON.name(), OnOffType.OFF.name());
    }

    /**
     * Creates a new SWITCH On/Off value.
     *
     * values send in messages will be the same as those expected in incomming messages
     *
     * @param onValue The ON value string. This will be compared to MQTT messages.
     * @param offValue The OFF value string. This will be compared to MQTT messages.
     */
    public OnOffValue(@Nullable String onValue, @Nullable String offValue) {
        this(onValue, offValue, onValue, offValue);
    }

    /**
     * Creates a new SWITCH On/Off value.
     *
     * @param onState The ON value string. This will be compared to MQTT messages.
     * @param offState The OFF value string. This will be compared to MQTT messages.
     * @param onCommand The ON value string. This will be send in MQTT messages.
     * @param offCommand The OFF value string. This will be send in MQTT messages.
     */
    public OnOffValue(@Nullable String onState, @Nullable String offState, @Nullable String onCommand,
            @Nullable String offCommand) {
        super(CoreItemFactory.SWITCH, List.of(OnOffType.class, StringType.class));
        this.onState = onState == null ? OnOffType.ON.name() : onState;
        this.offState = offState == null ? OnOffType.OFF.name() : offState;
        this.onCommand = onCommand == null ? OnOffType.ON.name() : onCommand;
        this.offCommand = offCommand == null ? OnOffType.OFF.name() : offCommand;
    }

    @Override
    public OnOffType parseCommand(Command command) throws IllegalArgumentException {
        if (command instanceof OnOffType) {
            return (OnOffType) command;
        } else {
            final String updatedValue = command.toString();
            if (onState.equals(updatedValue)) {
                return OnOffType.ON;
            } else if (offState.equals(updatedValue)) {
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
}
