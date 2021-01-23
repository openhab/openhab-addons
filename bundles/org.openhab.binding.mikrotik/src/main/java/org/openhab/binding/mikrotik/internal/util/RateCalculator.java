package org.openhab.binding.mikrotik.internal.util;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

@NonNullByDefault
public class RateCalculator {
    public static final int BYTES_IN_MEGABIT = 125000;

    private BigDecimal value;
    // BigDecimal rate;
    float rate;
    DateTime lastUpdated;

    public RateCalculator(BigDecimal initialValue) {
        this.value = initialValue;
        this.lastUpdated = DateTime.now();
        // this.rate = new BigDecimal(0);
        this.rate = 0.0F;
    }

    public float getRate() {
        return this.rate;
    }

    public float getMegabitRate() {
        return getRate() / BYTES_IN_MEGABIT;
    }

    public void update(BigDecimal currentValue) {
        synchronized (this) {
            DateTime thisUpdated = DateTime.now();
            Seconds secDiff = Seconds.secondsBetween(lastUpdated, thisUpdated);
            // this.rate = currentValue.subtract(value).divide(secDiff.getSeconds());
            this.rate = currentValue.subtract(value).floatValue() / secDiff.getSeconds();
            this.value = currentValue;
            this.lastUpdated = thisUpdated;
        }
    }

    public void update(BigInteger currentValue) {
        this.update(new BigDecimal(currentValue));
    }
}
