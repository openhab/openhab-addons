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
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;

/**
 * Implements an open/close boolean value.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class OpenCloseValue extends Value {
    private final String openString;
    private final String closeString;

    /**
     * Creates a contact Open/Close type.
     */
    public OpenCloseValue() {
        super(CoreItemFactory.CONTACT, List.of(OpenClosedType.class, StringType.class));
        this.openString = OpenClosedType.OPEN.name();
        this.closeString = OpenClosedType.CLOSED.name();
    }

    /**
     * Creates a new contact Open/Close value.
     *
     * @param openValue The ON value string. This will be compared to MQTT messages.
     * @param closeValue The OFF value string. This will be compared to MQTT messages.
     */
    public OpenCloseValue(@Nullable String openValue, @Nullable String closeValue) {
        super(CoreItemFactory.CONTACT, List.of(OpenClosedType.class, StringType.class));
        this.openString = openValue == null ? OpenClosedType.OPEN.name() : openValue;
        this.closeString = closeValue == null ? OpenClosedType.CLOSED.name() : closeValue;
    }

    @Override
    public OpenClosedType parseCommand(Command command) throws IllegalArgumentException {
        if (command instanceof OpenClosedType) {
            return (OpenClosedType) command;
        } else {
            final String updatedValue = command.toString();
            if (openString.equals(updatedValue)) {
                return OpenClosedType.OPEN;
            } else if (closeString.equals(updatedValue)) {
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

        return String.format(formatPattern, command == OpenClosedType.OPEN ? openString : closeString);
    }
}
