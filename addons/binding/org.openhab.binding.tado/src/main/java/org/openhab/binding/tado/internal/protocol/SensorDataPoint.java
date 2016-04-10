package org.openhab.binding.tado.internal.protocol;

import java.math.BigDecimal;

public class SensorDataPoint {
    public String type;
    public String timestamp;

    public BigDecimal celsius;
    public BigDecimal fahrenheit;
    public BigDecimal percentage;

    public BigDecimal getValue(boolean useCelsius) {
        if (this.type.equals("PERCENTAGE")) {
            return percentage;
        } else if (this.type.equals("TEMPERATURE")) {
            return useCelsius ? celsius : fahrenheit;
        }

        return new BigDecimal(0);
    }
}
