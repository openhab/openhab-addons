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
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Implements a percentage value. Minimum and maximum are definable.
 *
 * <p>
 * Accepts user updates from a DecimalType, IncreaseDecreaseType and UpDownType.
 * If this is a percent value, PercentType
 * </p>
 * Accepts MQTT state updates as DecimalType, IncreaseDecreaseType and UpDownType
 * StringType with comma separated HSB ("h,s,b"), RGB ("r,g,b") and on, off strings.
 * On, Off strings can be customized.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class PercentageValue extends Value {
    private final double min;
    private final double max;
    private final double span;
    private final double step;
    private final @Nullable String onValue;
    private final @Nullable String offValue;

    public PercentageValue(@Nullable BigDecimal min, @Nullable BigDecimal max, @Nullable BigDecimal step,
            @Nullable String onValue, @Nullable String offValue) {
        super(CoreItemFactory.DIMMER, Stream
                .of(DecimalType.class, IncreaseDecreaseType.class, OnOffType.class, UpDownType.class, StringType.class)
                .collect(Collectors.toList()));
        this.onValue = onValue;
        this.offValue = offValue;
        this.min = min == null ? 0.0 : min.doubleValue();
        this.max = max == null ? 100.0 : max.doubleValue();
        if (this.min >= this.max) {
            throw new IllegalArgumentException("Min need to be smaller than max!");
        }
        this.span = this.max - this.min;
        this.step = step == null ? 1.0 : step.doubleValue();
    }

    @Override
    public void update(Command command) throws IllegalArgumentException {
        PercentType oldvalue = (state == UnDefType.UNDEF) ? new PercentType() : (PercentType) state;
        // Nothing do to -> We have received a percentage
        if (command instanceof PercentType) {
            state = (PercentType) command;
        } else //
               // A decimal type need to be converted according to the current min/max values
        if (command instanceof DecimalType) {
            double v = ((DecimalType) command).doubleValue();
            v = (v - min) * 100.0 / (max - min);
            state = new PercentType(new BigDecimal(v));
        } else //
               // Increase or decrease by "step"
        if (command instanceof IncreaseDecreaseType) {
            if (((IncreaseDecreaseType) command) == IncreaseDecreaseType.INCREASE) {
                final double v = oldvalue.doubleValue() + step;
                state = new PercentType(new BigDecimal(v <= max ? v : max));
            } else {
                double v = oldvalue.doubleValue() - step;
                state = new PercentType(new BigDecimal(v >= min ? v : min));
            }
        } else //
               // On/Off equals 100 or 0 percent
        if (command instanceof OnOffType) {
            state = ((OnOffType) command) == OnOffType.ON ? PercentType.HUNDRED : PercentType.ZERO;
        } else//
              // Increase or decrease by "step"
        if (command instanceof UpDownType) {
            if (((UpDownType) command) == UpDownType.UP) {
                final double v = oldvalue.doubleValue() + step;
                state = new PercentType(new BigDecimal(v <= max ? v : max));
            } else {
                final double v = oldvalue.doubleValue() - step;
                state = new PercentType(new BigDecimal(v >= min ? v : min));
            }
        } else //
               // Check against custom on/off values
        if (command instanceof StringType) {
            if (onValue != null && command.toString().equals(onValue)) {
                state = new PercentType(new BigDecimal(max));
            } else if (offValue != null && command.toString().equals(offValue)) {
                state = new PercentType(new BigDecimal(min));
            } else {
                throw new IllegalStateException("Unknown String!");
            }
        } else {
            // We are desperate -> Try to parse the command as number value
            state = PercentType.valueOf(command.toString());
        }
    }

    @Override
    public String getMQTTpublishValue() {
        if (state == UnDefType.UNDEF) {
            return "";
        }
        // Formular: From percentage to custom min/max: value*span/100+min
        // Calculation need to happen with big decimals to either return a straight integer or a decimal depending on
        // the value.
        return ((PercentType) state).toBigDecimal().multiply(BigDecimal.valueOf(span)).divide(BigDecimal.valueOf(100))
                .add(BigDecimal.valueOf(min)).toString();
    }

    @Override
    public StateDescription createStateDescription(String unit, boolean readOnly) {
        return new StateDescription(new BigDecimal(min), new BigDecimal(max), new BigDecimal(step),
                "%s " + unit.replace("%", "%%"), readOnly, Collections.emptyList());
    }
}
