/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.openhab.binding.tado.internal.TadoBindingConstants.FanSpeed;
import org.openhab.binding.tado.internal.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.internal.api.ApiException;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.GenericZoneSetting;
import org.openhab.binding.tado.internal.api.model.HotWaterCapabilities;
import org.openhab.binding.tado.internal.api.model.HotWaterZoneSetting;
import org.openhab.binding.tado.internal.api.model.Power;
import org.openhab.binding.tado.internal.api.model.TadoSystemType;
import org.openhab.binding.tado.internal.api.model.TemperatureObject;

/**
 * Builder for incremental creation of hot water zone settings.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class HotWaterZoneSettingsBuilder extends ZoneSettingsBuilder {
    private static final float DEFAULT_TEMPERATURE_C = 50.0f;
    private static final float DEFAULT_TEMPERATURE_F = 122.0f;

    @Override
    public ZoneSettingsBuilder withSwing(boolean swingOn) {
        throw new IllegalArgumentException("Hot Water zones don't support SWING");
    }

    @Override
    public ZoneSettingsBuilder withFanSpeed(FanSpeed fanSpeed) {
        throw new IllegalArgumentException("Hot Water zones don't support FAN SPEED");
    }

    @Override
    public GenericZoneSetting build(ZoneStateProvider zoneStateProvider, GenericZoneCapabilities capabilities)
            throws IOException, ApiException {
        if (mode == HvacMode.OFF) {
            return hotWaterSetting(false);
        }

        HotWaterZoneSetting setting = hotWaterSetting(true);

        if (temperature != null) {
            setting.setTemperature(temperature(temperature, temperatureUnit));
        }

        addMissingSettingParts(setting, zoneStateProvider, (HotWaterCapabilities) capabilities);

        return setting;
    }

    private void addMissingSettingParts(HotWaterZoneSetting setting, ZoneStateProvider zoneStateProvider,
            HotWaterCapabilities capabilities) throws IOException, ApiException {

        if (capabilities.isCanSetTemperature() && setting.getTemperature() == null) {
            TemperatureObject temperatureObject = getCurrentOrDefaultTemperature(zoneStateProvider);
            setting.setTemperature(temperatureObject);
        }
    }

    private TemperatureObject getCurrentOrDefaultTemperature(ZoneStateProvider zoneStateProvider)
            throws IOException, ApiException {
        HotWaterZoneSetting zoneSetting = (HotWaterZoneSetting) zoneStateProvider.getZoneState().getSetting();

        if (zoneSetting != null && zoneSetting.getTemperature() != null) {
            return truncateTemperature(zoneSetting.getTemperature());
        }

        return buildDefaultTemperatureObject(DEFAULT_TEMPERATURE_C, DEFAULT_TEMPERATURE_F);
    }

    private HotWaterZoneSetting hotWaterSetting(boolean powerOn) {
        HotWaterZoneSetting setting = new HotWaterZoneSetting();
        setting.setType(TadoSystemType.HOT_WATER);
        setting.setPower(powerOn ? Power.ON : Power.OFF);
        return setting;
    }
}
