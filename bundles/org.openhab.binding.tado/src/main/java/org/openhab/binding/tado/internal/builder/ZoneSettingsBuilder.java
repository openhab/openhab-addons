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
package org.openhab.binding.tado.internal.builder;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tado.internal.TadoBindingConstants.FanLevel;
import org.openhab.binding.tado.internal.TadoBindingConstants.FanSpeed;
import org.openhab.binding.tado.internal.TadoBindingConstants.HorizontalSwing;
import org.openhab.binding.tado.internal.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.internal.TadoBindingConstants.TemperatureUnit;
import org.openhab.binding.tado.internal.TadoBindingConstants.VerticalSwing;
import org.openhab.binding.tado.internal.TadoBindingConstants.ZoneType;
import org.openhab.binding.tado.internal.api.ApiException;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.GenericZoneSetting;
import org.openhab.binding.tado.internal.api.model.TemperatureObject;
import org.openhab.binding.tado.internal.handler.TadoZoneHandler;

/**
 * Base class for zone settings builder.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
@NonNullByDefault
public abstract class ZoneSettingsBuilder {

    public static ZoneSettingsBuilder of(TadoZoneHandler zoneHandler) {
        ZoneType zoneType = zoneHandler.getZoneType();

        switch (zoneType) {
            case HEATING:
                return new HeatingZoneSettingsBuilder();
            case AIR_CONDITIONING:
                return new AirConditioningZoneSettingsBuilder();
            case HOT_WATER:
                return new HotWaterZoneSettingsBuilder();
            default:
                throw new IllegalArgumentException("Zone type " + zoneType + " unknown");
        }
    }

    protected TemperatureUnit temperatureUnit = TemperatureUnit.CELSIUS;
    protected @Nullable Float temperature;
    protected @Nullable HvacMode mode;
    protected @Nullable Boolean swing;
    protected @Nullable Boolean light;
    protected @Nullable FanSpeed fanSpeed;
    protected @Nullable FanLevel fanLevel;
    protected @Nullable HorizontalSwing horizontalSwing;
    protected @Nullable VerticalSwing verticalSwing;

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

    public ZoneSettingsBuilder withFanLevel(FanLevel fanLevel) {
        this.fanLevel = fanLevel;
        return this;
    }

    public ZoneSettingsBuilder withHorizontalSwing(HorizontalSwing horizontalSwing) {
        this.horizontalSwing = horizontalSwing;
        return this;
    }

    public ZoneSettingsBuilder withVerticalSwing(VerticalSwing verticalSwing) {
        this.verticalSwing = verticalSwing;
        return this;
    }

    public abstract GenericZoneSetting build(ZoneStateProvider zoneStateProvider, GenericZoneCapabilities capabilities)
            throws IOException, ApiException;

    protected TemperatureObject truncateTemperature(TemperatureObject temperature) {
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

    public ZoneSettingsBuilder withLight(boolean lightOn) {
        this.light = lightOn;
        return this;
    }
}
