/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado.internal.builder;

import static org.openhab.binding.tado.internal.api.TadoApiTypeUtils.temperature;

import java.io.IOException;

import org.openhab.binding.tado.TadoBindingConstants.FanSpeed;
import org.openhab.binding.tado.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.internal.api.TadoClientException;
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
public class HeatingZoneSettingsBuilder extends ZoneSettingsBuilder {
    private static final float DEFAULT_TEMPERATURE_C = 22.0f;
    private static final float DEFAULT_TEMPERATURE_F = 72.0f;

    @Override
    public ZoneSettingsBuilder withSwing(boolean swingOn) {
        throw new IllegalArgumentException("Heating zones don't support SWING");
    }

    @Override
    public ZoneSettingsBuilder withFanSpeed(FanSpeed fanSpeed) {
        throw new IllegalArgumentException("Heating zones don't support FAN SPEED");
    }

    @Override
    public GenericZoneSetting build(ZoneStateProvider zoneStateProvider, GenericZoneCapabilities capabilities)
            throws IOException, TadoClientException {
        if (mode == HvacMode.OFF) {
            return heatingSetting(false);
        }

        HeatingZoneSetting setting = heatingSetting(true);

        if (temperature != null) {
            setting.setTemperature(temperature(temperature, temperatureUnit));
        }

        addMissingSettingParts(setting, zoneStateProvider);

        return setting;
    }

    private void addMissingSettingParts(HeatingZoneSetting setting, ZoneStateProvider zoneStateProvider)
            throws IOException, TadoClientException {
        if (setting.getTemperature() == null) {
            TemperatureObject temperatureObject = getCurrentOrDefaultTemperature(zoneStateProvider);
            setting.setTemperature(temperatureObject);
        }
    }

    private TemperatureObject getCurrentOrDefaultTemperature(ZoneStateProvider zoneStateProvider)
            throws IOException, TadoClientException {
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
