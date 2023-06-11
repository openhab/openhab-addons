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
package org.openhab.binding.mikrotik.internal.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.threeten.extra.Seconds;

/**
 * The {@link RateCalculator} is used to calculate data changing rate as number per second. Has a separate method
 * to get megabits per second rate out of byte number.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RateCalculator {
    public static final int BYTES_IN_MEGABIT = 125000;

    private BigDecimal value;
    float rate;
    LocalDateTime lastUpdated;

    public RateCalculator(BigDecimal initialValue) {
        this.value = initialValue;
        this.lastUpdated = LocalDateTime.now();
        this.rate = 0.0F;
    }

    public float getRate() {
        return this.rate;
    }

    public float getMegabitRate() {
        return getRate() / BYTES_IN_MEGABIT;
    }

    public void update(@Nullable BigDecimal currentValue) {
        if (currentValue != null) {
            synchronized (this) {
                LocalDateTime thisUpdated = LocalDateTime.now();
                Seconds secDiff = Seconds.between(lastUpdated, thisUpdated);
                this.rate = currentValue.subtract(value).floatValue() / secDiff.getAmount();
                this.value = currentValue;
                this.lastUpdated = thisUpdated;
            }
        }
    }

    public void update(@Nullable BigInteger currentValue) {
        BigInteger val = currentValue == null ? BigInteger.ZERO : currentValue;
        this.update(new BigDecimal(val));
    }
}
