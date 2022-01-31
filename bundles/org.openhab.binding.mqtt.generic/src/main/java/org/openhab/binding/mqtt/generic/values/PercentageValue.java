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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.UnDefType;

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
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private final BigDecimal min;
    private final BigDecimal max;
    private final BigDecimal span;
    private final BigDecimal step;
    private final BigDecimal stepPercent;
    private final @Nullable String onValue;
    private final @Nullable String offValue;

    public PercentageValue(@Nullable BigDecimal min, @Nullable BigDecimal max, @Nullable BigDecimal step,
            @Nullable String onValue, @Nullable String offValue) {
        super(CoreItemFactory.DIMMER, Stream.of(DecimalType.class, QuantityType.class, IncreaseDecreaseType.class,
                OnOffType.class, UpDownType.class, StringType.class).collect(Collectors.toList()));
        this.onValue = onValue;
        this.offValue = offValue;
        this.min = min == null ? BigDecimal.ZERO : min;
        this.max = max == null ? HUNDRED : max;
        if (this.min.compareTo(this.max) >= 0) {
            throw new IllegalArgumentException("Min need to be smaller than max!");
        }
        this.span = this.max.subtract(this.min);
        this.step = step == null ? BigDecimal.ONE : step;
        this.stepPercent = this.step.multiply(HUNDRED).divide(this.span, MathContext.DECIMAL128);
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
            BigDecimal v = ((DecimalType) command).toBigDecimal();
            v = v.subtract(min).multiply(HUNDRED).divide(max.subtract(min), MathContext.DECIMAL128);
            state = new PercentType(v);
        } else //
               // A quantity type need to be converted according to the current min/max values
        if (command instanceof QuantityType) {
            QuantityType<?> qty = ((QuantityType<?>) command).toUnit(Units.PERCENT);
            if (qty != null) {
                BigDecimal v = qty.toBigDecimal();
                v = v.subtract(min).multiply(HUNDRED).divide(max.subtract(min), MathContext.DECIMAL128);
                state = new PercentType(v);
            }
        } else //
               // Increase or decrease by "step"
        if (command instanceof IncreaseDecreaseType) {
            if (((IncreaseDecreaseType) command) == IncreaseDecreaseType.INCREASE) {
                final BigDecimal v = oldvalue.toBigDecimal().add(stepPercent);
                state = v.compareTo(HUNDRED) <= 0 ? new PercentType(v) : PercentType.HUNDRED;
            } else {
                final BigDecimal v = oldvalue.toBigDecimal().subtract(stepPercent);
                state = v.compareTo(BigDecimal.ZERO) >= 0 ? new PercentType(v) : PercentType.ZERO;
            }
        } else //
               // On/Off equals 100 or 0 percent
        if (command instanceof OnOffType) {
            state = ((OnOffType) command) == OnOffType.ON ? PercentType.HUNDRED : PercentType.ZERO;
        } else//
              // Increase or decrease by "step"
        if (command instanceof UpDownType) {
            if (((UpDownType) command) == UpDownType.UP) {
                final BigDecimal v = oldvalue.toBigDecimal().add(stepPercent);
                state = v.compareTo(HUNDRED) <= 0 ? new PercentType(v) : PercentType.HUNDRED;
            } else {
                final BigDecimal v = oldvalue.toBigDecimal().subtract(stepPercent);
                state = v.compareTo(BigDecimal.ZERO) >= 0 ? new PercentType(v) : PercentType.ZERO;
            }
        } else //
               // Check against custom on/off values
        if (command instanceof StringType) {
            if (onValue != null && command.toString().equals(onValue)) {
                state = new PercentType(max);
            } else if (offValue != null && command.toString().equals(offValue)) {
                state = new PercentType(min);
            } else {
                throw new IllegalStateException("Unknown String!");
            }
        } else {
            // We are desperate -> Try to parse the command as number value
            state = PercentType.valueOf(command.toString());
        }
    }

    @Override
    public String getMQTTpublishValue(@Nullable String pattern) {
        if (state == UnDefType.UNDEF) {
            return "";
        }
        // Formula: From percentage to custom min/max: value*span/100+min
        // Calculation need to happen with big decimals to either return a straight integer or a decimal depending on
        // the value.
        BigDecimal value = ((PercentType) state).toBigDecimal().multiply(span).divide(HUNDRED, MathContext.DECIMAL128)
                .add(min).stripTrailingZeros();

        String formatPattern = pattern;
        if (formatPattern == null) {
            formatPattern = "%s";
        }

        return new DecimalType(value).format(formatPattern);
    }

    @Override
    public StateDescriptionFragmentBuilder createStateDescription(boolean readOnly) {
        return super.createStateDescription(readOnly).withMaximum(HUNDRED).withMinimum(BigDecimal.ZERO).withStep(step)
                .withPattern("%s %%");
    }
}
