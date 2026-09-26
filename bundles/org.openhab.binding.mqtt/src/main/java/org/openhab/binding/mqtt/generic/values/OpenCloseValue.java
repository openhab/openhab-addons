/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import org.openhab.binding.mqtt.generic.IgnoreType;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;

/**
 * Implements an open/close boolean value.
 *
 * @author David Graeff - Initial contribution
 * @author Leo Siepel - State-only Contact handling
 */
@NonNullByDefault
public class OpenCloseValue extends Value {
    private final String openString;
    private final String closeString;

    /**
     * Creates a contact Open/Close type.
     */
    public OpenCloseValue() {
        super(CoreItemFactory.CONTACT, List.of(StringType.class));
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
        super(CoreItemFactory.CONTACT, List.of(StringType.class));
        this.openString = openValue == null ? OpenClosedType.OPEN.name() : openValue;
        this.closeString = closeValue == null ? OpenClosedType.CLOSED.name() : closeValue;
    }

    @Override
    public Command parseCommand(Command command) throws IllegalArgumentException {
        throw new IllegalArgumentException("Contact channels do not support commands");
    }

    @Override
    public Type parseMessage(Command command) throws IllegalArgumentException {
        if (command instanceof StringType string) {
            if (string.toString().equals(ignoreValue)) {
                return IgnoreType.SENTINEL;
            } else if (string.toString().equals(nullValue) || string.toString().isEmpty()) {
                return UnDefType.NULL;
            }
        }

        String value = command.toString();
        if (openString.equals(value)) {
            return OpenClosedType.OPEN;
        } else if (closeString.equals(value)) {
            return OpenClosedType.CLOSED;
        }
        return OpenClosedType.valueOf(value);
    }
}
