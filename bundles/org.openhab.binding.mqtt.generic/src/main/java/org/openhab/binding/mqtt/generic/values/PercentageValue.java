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
package org.openhab.binding.mqtt.generic.values;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

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
        super(CoreItemFactory.DIMMER, List.of(DecimalType.class, QuantityType.class, IncreaseDecreaseType.class,
                OnOffType.class, UpDownType.class, StringType.class));
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
    public PercentType parseCommand(Command command) throws IllegalArgumentException {
        PercentType oldvalue = (state == UnDefType.UNDEF) ? new PercentType() : (PercentType) state;
        // Nothing do to -> We have received a percentage
        if (command instanceof PercentType percent) {
            return percent;
        } else //
               // A decimal type need to be converted according to the current min/max values
        if (command instanceof DecimalType decimal) {
            BigDecimal v = decimal.toBigDecimal();
            v = v.subtract(min).multiply(HUNDRED).divide(max.subtract(min), MathContext.DECIMAL128);
            return new PercentType(v);
        } else //
               // A quantity type need to be converted according to the current min/max values
        if (command instanceof QuantityType quantity) {
            QuantityType<?> qty = quantity.toUnit(Units.PERCENT);
            if (qty != null) {
                BigDecimal v = qty.toBigDecimal();
                v = v.subtract(min).multiply(HUNDRED).divide(max.subtract(min), MathContext.DECIMAL128);
                return new PercentType(v);
            }
            return oldvalue;
        } else //
               // Increase or decrease by "step"
        if (command instanceof IncreaseDecreaseType increaseDecreaseCommand) {
            if (increaseDecreaseCommand == IncreaseDecreaseType.INCREASE) {
                final BigDecimal v = oldvalue.toBigDecimal().add(stepPercent);
                return v.compareTo(HUNDRED) <= 0 ? new PercentType(v) : PercentType.HUNDRED;
            } else {
                final BigDecimal v = oldvalue.toBigDecimal().subtract(stepPercent);
                return v.compareTo(BigDecimal.ZERO) >= 0 ? new PercentType(v) : PercentType.ZERO;
            }
        } else //
               // On/Off equals 100 or 0 percent
        if (command instanceof OnOffType increaseDecreaseCommand) {
            return increaseDecreaseCommand == OnOffType.ON ? PercentType.HUNDRED : PercentType.ZERO;
        } else//
              // Increase or decrease by "step"
        if (command instanceof UpDownType upDownCommand) {
            if (upDownCommand == UpDownType.UP) {
                final BigDecimal v = oldvalue.toBigDecimal().add(stepPercent);
                return v.compareTo(HUNDRED) <= 0 ? new PercentType(v) : PercentType.HUNDRED;
            } else {
                final BigDecimal v = oldvalue.toBigDecimal().subtract(stepPercent);
                return v.compareTo(BigDecimal.ZERO) >= 0 ? new PercentType(v) : PercentType.ZERO;
            }
        } else //
               // Check against custom on/off values
        if (command instanceof StringType) {
            if (onValue != null && command.toString().equals(onValue)) {
                return new PercentType(max);
            } else if (offValue != null && command.toString().equals(offValue)) {
                return new PercentType(min);
            } else {
                throw new IllegalStateException("Unable to parse " + command.toString() + " as a percent.");
            }
        } else {
            // We are desperate -> Try to parse the command as number value
            return PercentType.valueOf(command.toString());
        }
    }

    @Override
    public String getMQTTpublishValue(Command command, @Nullable String pattern) {
        // Formula: From percentage to custom min/max: value*span/100+min
        // Calculation need to happen with big decimals to either return a straight integer or a decimal depending on
        // the value.
        BigDecimal value = ((PercentType) command).toBigDecimal().multiply(span).divide(HUNDRED, MathContext.DECIMAL128)
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
