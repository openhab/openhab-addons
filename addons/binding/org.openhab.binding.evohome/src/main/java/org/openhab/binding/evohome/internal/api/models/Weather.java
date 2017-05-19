package org.openhab.binding.evohome.internal.api.models;

import java.math.BigDecimal;

public class Weather {

    private String condition;
    private BigDecimal temperature;
    private String units;
    private BigDecimal humidity;
    private String phrase;

    public String getCondition() {
        return condition;
    }

    public BigDecimal getHumidity() {
        return humidity;
    }

    public String getPhrase() {
        return phrase;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public String getUnits() {
        return units;
    }

    @Override
    public String toString() {
        return "Condition[" + condition + "] Temperature[" + temperature + "] units[" + units + "] humidity[" + humidity
                + "] phrase[" + phrase + "]";
    }
}
