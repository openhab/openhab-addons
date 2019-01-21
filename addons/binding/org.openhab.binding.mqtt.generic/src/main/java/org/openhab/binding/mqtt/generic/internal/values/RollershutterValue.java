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
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Implements an rollershutter value.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class RollershutterValue extends Value {
    private final @Nullable String upString;
    private final @Nullable String downString;
    private final @Nullable String stopString;

    /**
     * Creates a new rollershutter value.
     *
     * @param upString The UP value string. This will be compared to MQTT messages.
     * @param downString The DOWN value string. This will be compared to MQTT messages.
     * @param stopString The STOP value string. This will be compared to MQTT messages.
     */
    public RollershutterValue(@Nullable String upString, @Nullable String downString, @Nullable String stopString) {
        super(CoreItemFactory.ROLLERSHUTTER,
                Stream.of(UpDownType.class, StopMoveType.class, PercentType.class, StringType.class)
                        .collect(Collectors.toList()));
        this.upString = upString;
        this.downString = downString;
        this.stopString = stopString;
    }

    @Override
    public void update(Command command) throws IllegalArgumentException {
        if (command instanceof StopMoveType) {
            throw new IllegalStateException("Cannot call update() with StopMoveType");
        } else if (command instanceof PercentType) {
            state = (PercentType) command;
        } else {
            throw new IllegalStateException("Cannot call update() with custom stop/move/up/down");
        }
    }

    /**
     * The stop command will not update the internal state and is posted to the framework.
     * <p>
     * The Up/Down commands (100%/0%) are not updating the state directly and are also
     * posted as percent value to the framework. It is up to the user if the posted values
     * are applied to the item state immediately (autoupdate=true) or not.
     */
    @Override
    public @Nullable Command isPostOnly(Command command) {
        if (command instanceof UpDownType) {
            return command;
        } else if (command instanceof StopMoveType) {
            return command;
        } else if (command instanceof StringType) {
            final String updatedValue = command.toString();
            if (updatedValue.equals(upString)) {
                return UpDownType.UP.as(PercentType.class);
            } else if (updatedValue.equals(downString)) {
                return UpDownType.DOWN.as(PercentType.class);
            } else if (updatedValue.equals(stopString)) {
                return StopMoveType.STOP;
            }
        }
        return null;
    }

    @Override
    public String getMQTTpublishValue() {
        return (state == UnDefType.UNDEF) ? "0" : String.valueOf(((PercentType) state).intValue());
    }
}
