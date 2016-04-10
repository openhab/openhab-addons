package org.openhab.binding.tado.internal.protocol;

import java.math.BigDecimal;

public class Temperature {
    public BigDecimal celsius;
    public BigDecimal fahrenheit;

    public BigDecimal getValue(boolean useCelsius) {
        return useCelsius ? celsius : fahrenheit;
    }
}
