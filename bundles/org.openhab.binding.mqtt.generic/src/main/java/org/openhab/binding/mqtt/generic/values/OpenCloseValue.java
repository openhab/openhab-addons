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
     * @param openValues The list of OPEN value strings. These will be compared to MQTT messages. Defaults to "OPEN" if
     *            null.
     * @param closedValues The list of CLOSED value strings. These will be compared to MQTT messages. Defaults to
     *            "CLOSED" if null.
     */
    public OpenCloseValue(@Nullable List<String> openValues, @Nullable List<String> closedValues) {
        this(openValues, closedValues, null, null);
    }

    /**
     * Creates a new CONTACT Open/Closed value.
     *
     * @param openStates The list of OPEN value strings. These will be compared to MQTT messages. Defaults to "OPEN" if
     *            null.
     * @param closedStates The list of CLOSED value strings. These will be compared to MQTT messages. Defaults to
     *            "CLOSED" if null.
     * @param openCommand The OPEN value string. This will be sent in MQTT messages. Defaults to the first openState if
     *            null.
     * @param closedCommand The CLOSED value string. This will be sent in MQTT messages. Defaults to the first
     *            closedState if null.
     */
    public OpenCloseValue(@Nullable List<String> openStates, @Nullable List<String> closedStates,
            @Nullable String openCommand, @Nullable String closedCommand) {
        super(CoreItemFactory.CONTACT, List.of(OpenClosedType.class, StringType.class));
        this.openStates = openStates == null ? Set.of(OpenClosedType.OPEN.name())
                : openStates.stream().filter(not(String::isBlank)).collect(Collectors.toSet());
        this.closedStates = closedStates == null ? Set.of(OpenClosedType.CLOSED.name())
                : closedStates.stream().filter(not(String::isBlank)).collect(Collectors.toSet());
        this.openCommand = openCommand == null ? (openStates == null ? OpenClosedType.OPEN.name() : openStates.get(0))
                : openCommand;
        this.closedCommand = closedCommand == null
                ? (closedStates == null ? OpenClosedType.CLOSED.name() : closedStates.get(0))
                : closedCommand;
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
