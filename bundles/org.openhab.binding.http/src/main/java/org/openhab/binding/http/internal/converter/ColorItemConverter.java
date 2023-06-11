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
package org.openhab.binding.http.internal.converter;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.http.internal.config.HttpChannelConfig;
import org.openhab.binding.http.internal.transform.ValueTransformation;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link ColorItemConverter} implements {@link org.openhab.core.library.items.ColorItem} conversions
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public class ColorItemConverter extends AbstractTransformingItemConverter {
    private static final BigDecimal BYTE_FACTOR = BigDecimal.valueOf(2.55);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final Pattern TRIPLE_MATCHER = Pattern.compile("(\\d+),(\\d+),(\\d+)");

    private State state = UnDefType.UNDEF;

    public ColorItemConverter(Consumer<State> updateState, Consumer<Command> postCommand,
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

        if (command instanceof HSBType) {
            HSBType newState = (HSBType) command;
            state = newState;
            return hsbToString(newState);
        } else if (command instanceof PercentType && state instanceof HSBType) {
            HSBType newState = new HSBType(((HSBType) state).getBrightness(), ((HSBType) state).getSaturation(),
                    (PercentType) command);
            state = newState;
            return hsbToString(newState);
        }

        throw new IllegalArgumentException("Command type '" + command.toString() + "' not supported");
    }

    @Override
    public State toState(String string) {
        State newState = UnDefType.UNDEF;
        if (string.equals(channelConfig.onValue)) {
            if (state instanceof HSBType) {
                newState = new HSBType(((HSBType) state).getHue(), ((HSBType) state).getSaturation(),
                        PercentType.HUNDRED);
            } else {
                newState = HSBType.WHITE;
            }
        } else if (string.equals(channelConfig.offValue)) {
            if (state instanceof HSBType) {
                newState = new HSBType(((HSBType) state).getHue(), ((HSBType) state).getSaturation(), PercentType.ZERO);
            } else {
                newState = HSBType.BLACK;
            }
        } else if (string.equals(channelConfig.increaseValue) && state instanceof HSBType) {
            BigDecimal newBrightness = ((HSBType) state).getBrightness().toBigDecimal().add(channelConfig.step);
            if (HUNDRED.compareTo(newBrightness) < 0) {
                newBrightness = HUNDRED;
            }
            newState = new HSBType(((HSBType) state).getHue(), ((HSBType) state).getSaturation(),
                    new PercentType(newBrightness));
        } else if (string.equals(channelConfig.decreaseValue) && state instanceof HSBType) {
            BigDecimal newBrightness = ((HSBType) state).getBrightness().toBigDecimal().subtract(channelConfig.step);
            if (BigDecimal.ZERO.compareTo(newBrightness) > 0) {
                newBrightness = BigDecimal.ZERO;
            }
            newState = new HSBType(((HSBType) state).getHue(), ((HSBType) state).getSaturation(),
                    new PercentType(newBrightness));
        } else {
            Matcher matcher = TRIPLE_MATCHER.matcher(string);
            if (matcher.matches()) {
                switch (channelConfig.colorMode) {
                    case RGB:
                        int r = Integer.parseInt(matcher.group(1));
                        int g = Integer.parseInt(matcher.group(2));
                        int b = Integer.parseInt(matcher.group(3));
                        newState = HSBType.fromRGB(r, g, b);
                        break;
                    case HSB:
                        newState = new HSBType(string);
                        break;
                }
            }
        }

        state = newState;
        return newState;
    }

    private String hsbToString(HSBType state) {
        switch (channelConfig.colorMode) {
            case RGB:
                PercentType[] rgb = state.toRGB();
                return String.format("%1$d,%2$d,%3$d", rgb[0].toBigDecimal().multiply(BYTE_FACTOR).intValue(),
                        rgb[1].toBigDecimal().multiply(BYTE_FACTOR).intValue(),
                        rgb[2].toBigDecimal().multiply(BYTE_FACTOR).intValue());
            case HSB:
                return state.toString();
        }
        throw new IllegalStateException("Invalid colorMode setting");
    }

    public enum ColorMode {
        RGB,
        HSB
    }
}
