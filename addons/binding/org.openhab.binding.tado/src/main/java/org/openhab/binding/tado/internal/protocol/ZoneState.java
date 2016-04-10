package org.openhab.binding.tado.internal.protocol;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.OnOffType;

public class ZoneState {
    public LinkState link;
    public SensorData sensorDataPoints;
    public String tadoMode;
    public String overlayType;
    public ZoneSetting setting;

    public OverlayState overlay;

    // Maybe change to boolean for manual and boolean for zone power?

    public String getMode() {
        if (overlayType != null && overlayType.equals("MANUAL")) {
            return "MANUAL";
        } else {
            return tadoMode;
        }
    }

    @Override
    public String toString() {
        return "Link State: " + link.state + " | Temperature: " + sensorDataPoints.insideTemperature.celsius
                + " | Humidity: " + sensorDataPoints.humidity.percentage;
    }

    public OnOffType getLinkState() {
        if (link != null && link.state != null) {
            return link.state.equals("ON") ? OnOffType.ON : OnOffType.OFF;
        } else {
            return OnOffType.OFF;
        }
    }

    public OnOffType getHeatingState() {
        if (setting != null && setting.power != null && setting.power.equals("ON")) {
            return OnOffType.ON;
        } else {
            return OnOffType.OFF;
        }
    }

    public BigDecimal getTargetTemperature(boolean useCelsius) {
        if (setting != null && setting.temperature != null) {
            return setting.temperature.getValue(useCelsius);
        } else {
            return new BigDecimal(0);
        }
    }

    public BigDecimal getInsideTemperature(boolean useCelsius) {
        if (sensorDataPoints != null && sensorDataPoints.insideTemperature != null) {
            return sensorDataPoints.insideTemperature.getValue(useCelsius);
        } else {
            return new BigDecimal(0);
        }
    }

    public BigDecimal getHumidity() {
        if (sensorDataPoints != null && sensorDataPoints.humidity != null) {
            return sensorDataPoints.humidity.percentage;
        } else {
            return new BigDecimal(0);
        }
    }
}
