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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;

/**
 * Implements a rollershutter value.
 * <p>
 * The stop, up and down strings have multiple purposes.
 * For one if those strings are received via MQTT they are recognised as corresponding commands
 * and also posted as Commands to the framework.
 * And if a user commands an Item->Channel to perform Stop the corresponding string is send. For Up,Down
 * the percentage 0 and 100 is send.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class RollershutterValue extends Value {
    private final @Nullable String upString;
    private final @Nullable String downString;
    private final String stopString;
    private boolean nextIsStop = false; // If set: getMQTTpublishValue will return the stop string

    /**
     * Creates a new rollershutter value.
     *
     * @param upString The UP value string. This will be compared to MQTT messages.
     * @param downString The DOWN value string. This will be compared to MQTT messages.
     * @param stopString The STOP value string. This will be compared to MQTT messages.
     */
    public RollershutterValue(@Nullable String upString, @Nullable String downString, @Nullable String stopString) {
        super(CoreItemFactory.ROLLERSHUTTER,
                List.of(UpDownType.class, StopMoveType.class, PercentType.class, StringType.class));
        this.upString = upString;
        this.downString = downString;
        this.stopString = stopString == null ? StopMoveType.STOP.name() : stopString;
    }

    @Override
    public void update(Command command) throws IllegalArgumentException {
        nextIsStop = false;
        if (command instanceof StopMoveType) {
            nextIsStop = (((StopMoveType) command) == StopMoveType.STOP);
            return;
        } else if (command instanceof UpDownType) {
            state = ((UpDownType) command) == UpDownType.UP ? PercentType.ZERO : PercentType.HUNDRED;
            return;
        } else if (command instanceof PercentType) {
            state = (PercentType) command;
            return;
        } else if (command instanceof StringType) {
            final String updatedValue = command.toString();
            if (updatedValue.equals(upString)) {
                state = PercentType.ZERO;
                return;
            } else if (updatedValue.equals(downString)) {
                state = PercentType.HUNDRED;
                return;
            } else if (updatedValue.equals(stopString)) {
                nextIsStop = true;
                return;
            }
        }
        throw new IllegalStateException("Cannot call update() with " + command.toString());
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
    public String getMQTTpublishValue(@Nullable String pattern) {
        final String upString = this.upString;
        final String downString = this.downString;
        if (this.nextIsStop) {
            this.nextIsStop = false;
            return stopString;
        } else if (state instanceof PercentType) {
            if (state.equals(PercentType.HUNDRED) && downString != null) {
                return downString;
            } else if (state.equals(PercentType.ZERO) && upString != null) {
                return upString;
            } else {
                return String.valueOf(((PercentType) state).intValue());
            }
        } else {
            return "UNDEF";
        }
    }
}
