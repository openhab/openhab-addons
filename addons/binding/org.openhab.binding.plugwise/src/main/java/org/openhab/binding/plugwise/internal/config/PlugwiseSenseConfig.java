/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.config;

import static com.google.common.base.CaseFormat.*;
import static org.openhab.binding.plugwise.internal.protocol.field.BoundaryAction.OFF_BELOW_ON_ABOVE;
import static org.openhab.binding.plugwise.internal.protocol.field.BoundaryType.NONE;

import java.time.Duration;

import org.openhab.binding.plugwise.internal.protocol.field.BoundaryAction;
import org.openhab.binding.plugwise.internal.protocol.field.BoundaryType;
import org.openhab.binding.plugwise.internal.protocol.field.Humidity;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.openhab.binding.plugwise.internal.protocol.field.Temperature;

/**
 * The {@link PlugwiseScanConfig} class represents the configuration for a Plugwise Sense.
 *
 * @author Wouter Born - Initial contribution
 */
public class PlugwiseSenseConfig {

    private String macAddress;
    private int measurementInterval = 15; // minutes
    private String boundaryType = UPPER_UNDERSCORE.to(LOWER_CAMEL, NONE.name());
    private String boundaryAction = UPPER_UNDERSCORE.to(LOWER_CAMEL, OFF_BELOW_ON_ABOVE.name());
    private int temperatureBoundaryMin = 15; // degrees Celsius
    private int temperatureBoundaryMax = 25; // degrees Celsius
    private int humidityBoundaryMin = 45; // relative humidity (RH)
    private int humidityBoundaryMax = 65; // relative humidity (RH)
    private int wakeupInterval = 1440; // minutes (1 day)
    private int wakeupDuration = 10; // seconds
    private boolean updateConfiguration = true;

    public MACAddress getMACAddress() {
        return new MACAddress(macAddress);
    }

    public Duration getMeasurementInterval() {
        return Duration.ofMinutes(measurementInterval);
    }

    public BoundaryType getBoundaryType() {
        return BoundaryType.valueOf(LOWER_CAMEL.to(UPPER_UNDERSCORE, boundaryType));
    }

    public BoundaryAction getBoundaryAction() {
        return BoundaryAction.valueOf(LOWER_CAMEL.to(UPPER_UNDERSCORE, boundaryAction));
    }

    public Temperature getTemperatureBoundaryMin() {
        return new Temperature(temperatureBoundaryMin);
    }

    public Temperature getTemperatureBoundaryMax() {
        return new Temperature(temperatureBoundaryMax);
    }

    public Humidity getHumidityBoundaryMin() {
        return new Humidity(humidityBoundaryMin);
    }

    public Humidity getHumidityBoundaryMax() {
        return new Humidity(humidityBoundaryMax);
    }

    public Duration getWakeupInterval() {
        return Duration.ofMinutes(wakeupInterval);
    }

    public Duration getWakeupDuration() {
        return Duration.ofSeconds(wakeupDuration);
    }

    public boolean isUpdateConfiguration() {
        return updateConfiguration;
    }

    public boolean equalBoundaryParameters(PlugwiseSenseConfig other) {
        return boundaryType.equals(other.boundaryType) && boundaryAction.equals(other.boundaryAction)
                && temperatureBoundaryMin == other.temperatureBoundaryMin
                && temperatureBoundaryMax == other.temperatureBoundaryMax
                && humidityBoundaryMin == other.humidityBoundaryMin && humidityBoundaryMax == other.humidityBoundaryMax;
    }

    public boolean equalSleepParameters(PlugwiseSenseConfig other) {
        return this.wakeupInterval == other.wakeupInterval && this.wakeupDuration == other.wakeupDuration;
    }

    @Override
    public String toString() {
        return "PlugwiseSenseConfig [macAddress=" + macAddress + ", measurementInterval=" + measurementInterval
                + ", boundaryType=" + boundaryType + ", boundaryAction=" + boundaryAction + ", temperatureBoundaryMin="
                + temperatureBoundaryMin + ", temperatureBoundaryMax=" + temperatureBoundaryMax
                + ", humidityBoundaryMin=" + humidityBoundaryMin + ", humidityBoundaryMax=" + humidityBoundaryMax
                + ", wakeupInterval=" + wakeupInterval + ", wakeupDuration=" + wakeupDuration + ", updateConfiguration="
                + updateConfiguration + "]";
    }

}
