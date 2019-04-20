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
package org.openhab.binding.mqtt.generic.values;

import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Implements a color value.
 *
 * <p>
 * Accepts user updates from a HSBType, OnOffType and StringType.
 * </p>
 * Accepts MQTT state updates as OnOffType and a
 * StringType with comma separated HSB ("h,s,b"), RGB ("r,g,b") and on, off strings.
 * On, Off strings can be customized.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ColorValue extends Value {
    private final boolean isRGB;
    private final String onValue;
    private final String offValue;
    private final int onBrightness;

    /**
     * Creates a non initialized color value.
     *
     * @param isRGB True if this is an RGB color value instead of a HSB one.
     * @param onValue The ON value string. This will be compared to MQTT messages.
     * @param offValue The OFF value string. This will be compared to MQTT messages.
     * @param onBrightness When receiving a ON command, the brightness percentage is set to this value
     */
    public ColorValue(boolean isRGB, @Nullable String onValue, @Nullable String offValue, int onBrightness) {
        super(CoreItemFactory.COLOR,
                Stream.of(OnOffType.class, PercentType.class, StringType.class).collect(Collectors.toList()));

        if (onBrightness > 100) {
            throw new IllegalArgumentException("Brightness parameter must be <= 100");
        }

        this.isRGB = isRGB;
        this.onValue = onValue == null ? "ON" : onValue;
        this.offValue = offValue == null ? "OFF" : offValue;
        this.onBrightness = onBrightness;
    }

    /**
     * Updates the color value.
     *
     * @return Returns the color value as HSB/HSV string (hue, saturation, brightness) eg. "60, 100, 100".
     *         If rgb is enabled, an RGB string (red,green,blue) will be returned instead. red,green,blue are within
     *         [0,255].
     */
    @Override
    public void update(Command command) throws IllegalArgumentException {
        HSBType oldvalue = (state == UnDefType.UNDEF) ? new HSBType() : (HSBType) state;
        if (command instanceof HSBType) {
            state = (HSBType) command;
        } else if (command instanceof OnOffType) {
            OnOffType boolValue = ((OnOffType) command);
            PercentType minOn = new PercentType(Math.max(oldvalue.getBrightness().intValue(), onBrightness));
            state = new HSBType(oldvalue.getHue(), oldvalue.getSaturation(),
                    boolValue == OnOffType.ON ? minOn : new PercentType(0));
        } else if (command instanceof PercentType) {
            state = new HSBType(oldvalue.getHue(), oldvalue.getSaturation(), (PercentType) command);
        } else {
            final String updatedValue = command.toString();
            if (onValue.equals(updatedValue)) {
                PercentType minOn = new PercentType(Math.max(oldvalue.getBrightness().intValue(), onBrightness));
                state = new HSBType(oldvalue.getHue(), oldvalue.getSaturation(), minOn);
            } else if (offValue.equals(updatedValue)) {
                state = new HSBType(oldvalue.getHue(), oldvalue.getSaturation(), new PercentType(0));
            } else if (isRGB) {
                String[] split = updatedValue.split(",");
                if (split.length != 3) {
                    throw new IllegalArgumentException(updatedValue + " is not a valid RGB syntax");
                }
                state = HSBType.fromRGB(Integer.parseInt(split[0]), Integer.parseInt(split[1]),
                        Integer.parseInt(split[2]));
            } else {
                state = new HSBType(updatedValue);
            }
        }
    }

    private static BigDecimal factor = new BigDecimal(2.5);

    @Override
    public String getMQTTpublishValue() {
        if (state == UnDefType.UNDEF) {
            return "";
        }

        if (isRGB) {
            PercentType[] rgb = ((HSBType) state).toRGB();
            StringBuilder b = new StringBuilder();
            b.append(rgb[0].toBigDecimal().multiply(factor).intValue());
            b.append(',');
            b.append(rgb[1].toBigDecimal().multiply(factor).intValue());
            b.append(',');
            b.append(rgb[2].toBigDecimal().multiply(factor).intValue());
            return b.toString();
        } else {
            return state.toString();
        }
    }
}
