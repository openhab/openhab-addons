/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.generic.internal.values;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;

/**
 * Implements an on/off boolean value.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class OnOffValue extends Value {
    private final String onString;
    private final String offString;

    /**
     * Creates a switch On/Off type, that accepts "ON", "1" for on and "OFF","0" for off.
     */
    public OnOffValue() {
        super(CoreItemFactory.SWITCH, Stream.of(OnOffType.class, StringType.class).collect(Collectors.toList()));
        this.onString = OnOffType.ON.name();
        this.offString = OnOffType.OFF.name();
    }

    /**
     * Creates a new SWITCH On/Off value.
     *
     * @param onValue The ON value string. This will be compared to MQTT messages.
     * @param offValue The OFF value string. This will be compared to MQTT messages.
     */
    public OnOffValue(@Nullable String onValue, @Nullable String offValue) {
        super(CoreItemFactory.SWITCH, Stream.of(OnOffType.class, StringType.class).collect(Collectors.toList()));
        this.onString = onValue == null ? OnOffType.ON.name() : onValue;
        this.offString = offValue == null ? OnOffType.OFF.name() : offValue;
    }

    @Override
    public void update(Command command) throws IllegalArgumentException {
        if (command instanceof OnOffType) {
            state = (OnOffType) command;
        } else {
            final String updatedValue = command.toString();
            if (onString.equals(updatedValue)) {
                state = OnOffType.ON;
            } else if (offString.equals(updatedValue)) {
                state = OnOffType.OFF;
            } else {
                state = OnOffType.valueOf(updatedValue);
            }
        }
    }

    @Override
    public String getMQTTpublishValue() {
        return (state == OnOffType.ON) ? onString : offString;
    }
}
