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
package org.openhab.binding.http.internal.converter;

import java.math.BigDecimal;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.http.internal.config.HttpChannelConfig;
import org.openhab.binding.http.internal.transform.ValueTransformation;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link DimmerItemConverter} implements {@link org.openhab.core.library.items.DimmerItem} conversions
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public class DimmerItemConverter extends AbstractTransformingItemConverter {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private State state = UnDefType.UNDEF;

    public DimmerItemConverter(Consumer<State> updateState, Consumer<Command> postCommand,
            @Nullable Consumer<String> sendHttpValue, ValueTransformation stateTransformations,
            ValueTransformation commandTransformations, HttpChannelConfig channelConfig) {
        super(updateState, postCommand, sendHttpValue, stateTransformations, commandTransformations, channelConfig);
        this.channelConfig = channelConfig;
    }

    @Override
    protected @Nullable Command toCommand(String value) {
        return null;
    }

    @Override
    public String toString(Command command) {
        String string = channelConfig.commandToFixedValue(command);
        if (string != null) {
            return string;
        }

        if (command instanceof PercentType percentCommand) {
            return percentCommand.toString();
        }

        throw new IllegalArgumentException("Command type '" + command.toString() + "' not supported");
    }

    @Override
    public State toState(String string) {
        State newState = UnDefType.UNDEF;

        if (string.equals(channelConfig.onValue)) {
            newState = PercentType.HUNDRED;
        } else if (string.equals(channelConfig.offValue)) {
            newState = PercentType.ZERO;
        } else if (string.equals(channelConfig.increaseValue) && state instanceof PercentType brightnessState) {
            BigDecimal newBrightness = brightnessState.toBigDecimal().add(channelConfig.step);
            if (HUNDRED.compareTo(newBrightness) < 0) {
                newBrightness = HUNDRED;
            }
            newState = new PercentType(newBrightness);
        } else if (string.equals(channelConfig.decreaseValue) && state instanceof PercentType brightnessState) {
            BigDecimal newBrightness = brightnessState.toBigDecimal().subtract(channelConfig.step);
            if (BigDecimal.ZERO.compareTo(newBrightness) > 0) {
                newBrightness = BigDecimal.ZERO;
            }
            newState = new PercentType(newBrightness);
        } else {
            try {
                BigDecimal value = new BigDecimal(string);
                if (value.compareTo(PercentType.HUNDRED.toBigDecimal()) > 0) {
                    value = PercentType.HUNDRED.toBigDecimal();
                }
                if (value.compareTo(PercentType.ZERO.toBigDecimal()) < 0) {
                    value = PercentType.ZERO.toBigDecimal();
                }
                newState = new PercentType(value);
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }

        state = newState;
        return newState;
    }
}
