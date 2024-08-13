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
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;

/**
 * Implements an Open/Closed boolean value.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class OpenCloseValue extends Value {
    private final Set<String> openStates;
    private final Set<String> closedStates;
    private final String openCommand;
    private final String closedCommand;

    /**
     * Creates a CONTACT Open/Closed value.
     */
    public OpenCloseValue() {
        this(null, null);
    }

    /**
     * Creates a new CONTACT Open/Closed value.
     *
     * @param openValues The ON values as comma-separate string. These will be compared to MQTT messages. Defaults to
     *            "OPEN" if null.
     * @param closedValues The OFF values as comma-separated string. These will be compared to MQTT messages. Defaults
     *            to "CLOSED" if null.
     */
    public OpenCloseValue(@Nullable String openValues, @Nullable String closedValues) {
        this(openValues, closedValues, null, null);
    }

    public OpenCloseValue(@Nullable String openValues, @Nullable String closedValues, @Nullable String openCommand,
            @Nullable String closedCommand) {
        this(openValues == null ? new String[] { OpenClosedType.OPEN.name() } : openValues.split(","),
                closedValues == null ? new String[] { OpenClosedType.CLOSED.name() } : closedValues.split(","),
                openCommand, closedCommand);
    }

    /**
     * Creates a new CONTACT Open/Closed value.
     *
     * @param openStates The list of valid OPEN value strings. This will be compared to MQTT messages.
     * @param closedStates The list of valid CLOSED value strings. This will be compared to MQTT messages.
     * @param openCommand The OPEN value string. This will be sent in MQTT messages. Defaults to the first openState if
     *            null.
     * @param closedCommand The CLOSED value string. This will be sent in MQTT messages. Defaults to the first
     *            closedState if null.
     */
    public OpenCloseValue(String[] openStates, String[] closedStates, @Nullable String openCommand,
            @Nullable String closedCommand) {
        super(CoreItemFactory.CONTACT, List.of(OpenClosedType.class, StringType.class));
        this.openStates = Stream.of(openStates).filter(not(String::isBlank)).collect(Collectors.toSet());
        this.closedStates = Stream.of(closedStates).filter(not(String::isBlank)).collect(Collectors.toSet());
        this.openCommand = openCommand == null ? openStates[0] : openCommand;
        this.closedCommand = closedCommand == null ? closedStates[0] : closedCommand;
    }

    @Override
    public OpenClosedType parseCommand(Command command) throws IllegalArgumentException {
        if (command instanceof OpenClosedType openClosed) {
            return openClosed;
        } else {
            final String updatedValue = command.toString();
            if (openStates.contains(updatedValue)) {
                return OpenClosedType.OPEN;
            } else if (closedStates.contains(updatedValue)) {
                return OpenClosedType.CLOSED;
            } else {
                return OpenClosedType.valueOf(updatedValue);
            }
        }
    }

    @Override
    public String getMQTTpublishValue(Command command, @Nullable String pattern) {
        String formatPattern = pattern;
        if (formatPattern == null) {
            formatPattern = "%s";
        }

        return String.format(formatPattern, command == OpenClosedType.OPEN ? openCommand : closedCommand);
    }
}
