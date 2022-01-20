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
package org.openhab.binding.http.internal.converter;

import java.math.BigDecimal;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.http.internal.config.HttpChannelConfig;
import org.openhab.binding.http.internal.transform.ValueTransformation;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link RollershutterItemConverter} implements {@link org.openhab.core.library.items.RollershutterItem}
 * conversions
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public class RollershutterItemConverter extends AbstractTransformingItemConverter {
    private final HttpChannelConfig channelConfig;

    public RollershutterItemConverter(Consumer<State> updateState, Consumer<Command> postCommand,
            @Nullable Consumer<String> sendHttpValue, ValueTransformation stateTransformations,
            ValueTransformation commandTransformations, HttpChannelConfig channelConfig) {
        super(updateState, postCommand, sendHttpValue, stateTransformations, commandTransformations, channelConfig);
        this.channelConfig = channelConfig;
    }

    @Override
    public String toString(Command command) {
        String string = channelConfig.commandToFixedValue(command);
        if (string != null) {
            return string;
        }

        if (command instanceof PercentType) {
            final String downValue = channelConfig.downValue;
            final String upValue = channelConfig.upValue;
            if (command.equals(PercentType.HUNDRED) && downValue != null) {
                return downValue;
            } else if (command.equals(PercentType.ZERO) && upValue != null) {
                return upValue;
            } else {
                return ((PercentType) command).toString();
            }
        }

        throw new IllegalArgumentException("Command type '" + command.toString() + "' not supported");
    }

    @Override
    protected @Nullable Command toCommand(String string) {
        if (string.equals(channelConfig.upValue)) {
            return UpDownType.UP;
        } else if (string.equals(channelConfig.downValue)) {
            return UpDownType.DOWN;
        } else if (string.equals(channelConfig.moveValue)) {
            return StopMoveType.MOVE;
        } else if (string.equals(channelConfig.stopValue)) {
            return StopMoveType.STOP;
        }

        return null;
    }

    @Override
    public State toState(String string) {
        try {
            BigDecimal value = new BigDecimal(string);
            if (value.compareTo(PercentType.HUNDRED.toBigDecimal()) > 0) {
                return PercentType.HUNDRED;
            }
            if (value.compareTo(PercentType.ZERO.toBigDecimal()) < 0) {
                return PercentType.ZERO;
            }
        } catch (NumberFormatException e) {
            // ignore
        }

        return UnDefType.UNDEF;
    }
}
