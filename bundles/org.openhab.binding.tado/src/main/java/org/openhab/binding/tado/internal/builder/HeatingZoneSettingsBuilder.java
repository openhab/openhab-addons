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

import static org.openhab.binding.tado.internal.api.TadoApiTypeUtils.temperature;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tado.internal.TadoBindingConstants.FanLevel;
import org.openhab.binding.tado.internal.TadoBindingConstants.FanSpeed;
import org.openhab.binding.tado.internal.TadoBindingConstants.HorizontalSwing;
import org.openhab.binding.tado.internal.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.internal.TadoBindingConstants.VerticalSwing;
import org.openhab.binding.tado.internal.api.ApiException;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.GenericZoneSetting;
import org.openhab.binding.tado.internal.api.model.HeatingZoneSetting;
import org.openhab.binding.tado.internal.api.model.Power;
import org.openhab.binding.tado.internal.api.model.TadoSystemType;
import org.openhab.binding.tado.internal.api.model.TemperatureObject;

/**
 * Builder for incremental creation of heating zone settings.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
@NonNullByDefault
public class HeatingZoneSettingsBuilder extends ZoneSettingsBuilder {
    private static final float DEFAULT_TEMPERATURE_C = 22.0f;
    private static final float DEFAULT_TEMPERATURE_F = 72.0f;

    @Override
    public ZoneSettingsBuilder withSwing(boolean swingOn) {
        throw new IllegalArgumentException("Heating zones don't support SWING");
    }

    @Override
    public ZoneSettingsBuilder withLight(boolean lightOn) {
        throw new IllegalArgumentException("Heating zones don't support LIGHT");
    }

    @Override
    public ZoneSettingsBuilder withFanSpeed(FanSpeed fanSpeed) {
        throw new IllegalArgumentException("Heating zones don't support FAN SPEED");
    }

    @Override
    public ZoneSettingsBuilder withFanLevel(FanLevel fanLevel) {
        throw new IllegalArgumentException("Heating zones don't support FAN LEVEL");
    }

    @Override
    public ZoneSettingsBuilder withHorizontalSwing(HorizontalSwing horizontalSwing) {
        throw new IllegalArgumentException("Heating zones don't support HORIZONTAL SWING");
    }

    @Override
    public ZoneSettingsBuilder withVerticalSwing(VerticalSwing verticalSwing) {
        throw new IllegalArgumentException("Heating zones don't support VERTICAL SWING");
    }

    @Override
    public GenericZoneSetting build(ZoneStateProvider zoneStateProvider, GenericZoneCapabilities capabilities)
            throws IOException, ApiException {
        if (mode == HvacMode.OFF) {
            return heatingSetting(false);
        }

        HeatingZoneSetting setting = heatingSetting(true);

        Float temperature = this.temperature;
        if (temperature != null) {
            setting.setTemperature(temperature(temperature, temperatureUnit));
        }

        addMissingSettingParts(setting, zoneStateProvider);

        return setting;
    }

    private void addMissingSettingParts(HeatingZoneSetting setting, ZoneStateProvider zoneStateProvider)
            throws IOException, ApiException {
        if (setting.getTemperature() == null) {
            TemperatureObject temperatureObject = getCurrentOrDefaultTemperature(zoneStateProvider);
            setting.setTemperature(temperatureObject);
        }
    }

    private TemperatureObject getCurrentOrDefaultTemperature(ZoneStateProvider zoneStateProvider)
            throws IOException, ApiException {
        HeatingZoneSetting zoneSetting = (HeatingZoneSetting) zoneStateProvider.getZoneState().getSetting();

        if (zoneSetting != null && zoneSetting.getTemperature() != null) {
            return truncateTemperature(zoneSetting.getTemperature());
        }

        return buildDefaultTemperatureObject(DEFAULT_TEMPERATURE_C, DEFAULT_TEMPERATURE_F);
    }

    private HeatingZoneSetting heatingSetting(boolean powerOn) {
        HeatingZoneSetting setting = new HeatingZoneSetting();
        setting.setType(TadoSystemType.HEATING);
        setting.setPower(powerOn ? Power.ON : Power.OFF);
        return setting;
    }
}
