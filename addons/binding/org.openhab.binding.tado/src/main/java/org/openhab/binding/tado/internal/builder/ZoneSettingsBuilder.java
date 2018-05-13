/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado.internal.builder;

import java.io.IOException;

import org.openhab.binding.tado.TadoBindingConstants.FanSpeed;
import org.openhab.binding.tado.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.TadoBindingConstants.TemperatureUnit;
import org.openhab.binding.tado.handler.TadoZoneHandler;
import org.openhab.binding.tado.internal.api.TadoClientException;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.GenericZoneSetting;
import org.openhab.binding.tado.internal.api.model.TemperatureObject;

/**
 * Base class for zone settings builder.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public abstract class ZoneSettingsBuilder {
    public static ZoneSettingsBuilder of(TadoZoneHandler zoneHandler) {
        switch (zoneHandler.getZoneType()) {
            case HEATING:
                return new HeatingZoneSettingsBuilder();
            case AIR_CONDITIONING:
                return new AirConditioningZoneSettingsBuilder();
            case HOT_WATER:
                return new HotWaterZoneSettingsBuilder();
            default:
                throw new IllegalArgumentException("Zone type " + zoneHandler.getZoneType() + " unknown");
        }
    }

    protected HvacMode mode = null;
    protected Float temperature = null;
    protected TemperatureUnit temperatureUnit = TemperatureUnit.CELSIUS;
    protected Boolean swing = null;
    protected FanSpeed fanSpeed = null;

    public ZoneSettingsBuilder withMode(HvacMode mode) {
        this.mode = mode;
        return this;
    }

    public ZoneSettingsBuilder withTemperature(Float temperature, TemperatureUnit temperatureUnit) {
        this.temperature = temperature;
        this.temperatureUnit = temperatureUnit;
        return this;
    }

    public ZoneSettingsBuilder withSwing(boolean swingOn) {
        this.swing = swingOn;
        return this;
    }

    public ZoneSettingsBuilder withFanSpeed(FanSpeed fanSpeed) {
        this.fanSpeed = fanSpeed;
        return this;
    }

    public abstract GenericZoneSetting build(ZoneStateProvider zoneStateProvider, GenericZoneCapabilities capabilities)
            throws IOException, TadoClientException;

    protected TemperatureObject truncateTemperature(TemperatureObject temperature) {
        if (temperature == null) {
            return null;
        }

        TemperatureObject temperatureObject = new TemperatureObject();
        if (temperatureUnit == TemperatureUnit.FAHRENHEIT) {
            temperatureObject.setFahrenheit(temperature.getFahrenheit());
        } else {
            temperatureObject.setCelsius(temperature.getCelsius());
        }

        return temperatureObject;
    }

    protected TemperatureObject buildDefaultTemperatureObject(float temperatureCelsius, float temperatureFahrenheit) {
        TemperatureObject temperatureObject = new TemperatureObject();

        if (temperatureUnit == TemperatureUnit.FAHRENHEIT) {
            temperatureObject.setFahrenheit(temperatureFahrenheit);
        } else {
            temperatureObject.setCelsius(temperatureCelsius);
        }

        return temperatureObject;
    }
}
