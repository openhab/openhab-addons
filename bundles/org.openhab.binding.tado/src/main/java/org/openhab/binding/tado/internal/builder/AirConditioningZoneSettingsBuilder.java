/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.tado.internal.api.TadoApiTypeUtils.*;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tado.internal.TadoBindingConstants.FanLevel;
import org.openhab.binding.tado.internal.TadoBindingConstants.FanSpeed;
import org.openhab.binding.tado.internal.TadoBindingConstants.HorizontalSwing;
import org.openhab.binding.tado.internal.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.internal.TadoBindingConstants.TemperatureUnit;
import org.openhab.binding.tado.internal.TadoBindingConstants.VerticalSwing;
import org.openhab.binding.tado.internal.api.ApiException;
import org.openhab.binding.tado.internal.api.TadoApiTypeUtils;
import org.openhab.binding.tado.internal.api.model.ACFanLevel;
import org.openhab.binding.tado.internal.api.model.ACHorizontalSwing;
import org.openhab.binding.tado.internal.api.model.ACVerticalSwing;
import org.openhab.binding.tado.internal.api.model.AcFanSpeed;
import org.openhab.binding.tado.internal.api.model.AcMode;
import org.openhab.binding.tado.internal.api.model.AcModeCapabilities;
import org.openhab.binding.tado.internal.api.model.CoolingZoneSetting;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.GenericZoneSetting;
import org.openhab.binding.tado.internal.api.model.IntRange;
import org.openhab.binding.tado.internal.api.model.Power;
import org.openhab.binding.tado.internal.api.model.TadoSystemType;
import org.openhab.binding.tado.internal.api.model.TemperatureObject;
import org.openhab.binding.tado.internal.api.model.TemperatureRange;

/**
 *
 *
 * @author Dennis Frommknecht - Initial contribution
 */
@NonNullByDefault
public class AirConditioningZoneSettingsBuilder extends ZoneSettingsBuilder {
    private static final AcMode DEFAULT_MODE = AcMode.COOL;
    private static final float DEFAULT_TEMPERATURE_C = 20.0f;
    private static final float DEFAULT_TEMPERATURE_F = 68.0f;

    private static final String VALUE_NOT_ALLOWED_FORMAT_STRING = "Device does not allow setting '%s' to value '%s' when it is in '%s' mode.";

    @Override
    public GenericZoneSetting build(ZoneStateProvider zoneStateProvider, GenericZoneCapabilities genericCapabilities)
            throws IOException, ApiException {
        if (mode == HvacMode.OFF) {
            return coolingSetting(false);
        }

        CoolingZoneSetting newSetting = coolingSetting(true);

        HvacMode mode = this.mode;
        if (mode != null) {
            newSetting.setMode(getAcMode(mode));
        }

        Float temperature = this.temperature;
        if (temperature != null) {
            newSetting.setTemperature(temperature(temperature, temperatureUnit));
        }

        if (swing != null) {
            newSetting.setSwing(swing ? Power.ON : Power.OFF);
        }

        if (light != null) {
            newSetting.setLight(light ? Power.ON : Power.OFF);
        }

        FanSpeed fanSpeed = this.fanSpeed;
        if (fanSpeed != null) {
            newSetting.setFanSpeed(getAcFanSpeed(fanSpeed));
        }

        /*
         * In the latest API release Tado introduced extra AC settings that have an open ended list of possible
         * supported state values. And for any particular device, its specific list of supported values is available
         * via its 'capabilities' structure. So before setting a new value, we check if the respective new value is in
         * this capabilities list. And if not, an exception is thrown.
         */
        AcMode acMode = newSetting.getMode();
        AcModeCapabilities acCapabilities = TadoApiTypeUtils.getModeCapabilities(acMode, genericCapabilities);

        FanLevel fanLevel = this.fanLevel;
        if (fanLevel != null) {
            ACFanLevel acFanLevel = getFanLevel(fanLevel);
            List<ACFanLevel> acFanLevels = acCapabilities.getFanLevel();
            if (acFanLevels == null || !acFanLevels.contains(acFanLevel)) {
                throw new IllegalArgumentException(String.format(VALUE_NOT_ALLOWED_FORMAT_STRING,
                        acFanLevel.getClass().getName(), acFanLevel.name(), acMode.name()));
            }
            newSetting.setFanLevel(acFanLevel);
        }

        HorizontalSwing horizontalSwing = this.horizontalSwing;
        if (horizontalSwing != null) {
            ACHorizontalSwing acHorizontalSwing = getHorizontalSwing(horizontalSwing);
            List<ACHorizontalSwing> acHorizontalSwings = acCapabilities.getHorizontalSwing();
            if (acHorizontalSwings == null || !acHorizontalSwings.contains(acHorizontalSwing)) {
                throw new IllegalArgumentException(String.format(VALUE_NOT_ALLOWED_FORMAT_STRING,
                        acHorizontalSwing.getClass().getName(), acHorizontalSwing.name(), acMode.name()));
            }
            newSetting.setHorizontalSwing(acHorizontalSwing);
        }

        VerticalSwing verticalSwing = this.verticalSwing;
        if (verticalSwing != null) {
            ACVerticalSwing acVerticalSwing = getVerticalSwing(verticalSwing);
            List<ACVerticalSwing> acVerticalSwings = acCapabilities.getVerticalSwing();
            if (acVerticalSwings == null || !acVerticalSwings.contains(acVerticalSwing)) {
                throw new IllegalArgumentException(String.format(VALUE_NOT_ALLOWED_FORMAT_STRING,
                        acVerticalSwing.getClass().getName(), acVerticalSwing.name(), acMode.name()));
            }
            newSetting.setVerticalSwing(acVerticalSwing);
        }

        addMissingSettingParts(zoneStateProvider, genericCapabilities, newSetting);

        return newSetting;
    }

    private void addMissingSettingParts(ZoneStateProvider zoneStateProvider,
            GenericZoneCapabilities genericCapabilities, CoolingZoneSetting newSetting)
            throws IOException, ApiException {
        if (newSetting.getMode() == null) {
            AcMode targetMode = getCurrentOrDefaultAcMode(zoneStateProvider);
            newSetting.setMode(targetMode);
        }

        AcModeCapabilities capabilities = getModeCapabilities(newSetting.getMode(), genericCapabilities);

        TemperatureRange temperatures = capabilities.getTemperatures();
        if (temperatures != null && newSetting.getTemperature() == null) {
            newSetting.setTemperature(getCurrentOrDefaultTemperature(zoneStateProvider, temperatures));
        }

        List<AcFanSpeed> fanSpeeds = capabilities.getFanSpeeds();
        if (fanSpeeds != null && !fanSpeeds.isEmpty() && newSetting.getFanSpeed() == null) {
            newSetting.setFanSpeed(getCurrentOrDefaultFanSpeed(zoneStateProvider, fanSpeeds));
        }

        List<Power> swings = capabilities.getSwings();
        if (swings != null && !swings.isEmpty() && newSetting.getSwing() == null) {
            newSetting.setSwing(getCurrentOrDefaultSwing(zoneStateProvider, swings));
        }

        /*
         * In the latest API release Tado introduced extra AC settings that don't have explicit pre-defined default
         * values, so for such settings we just carry over the setting's prior value (i.e. the value may be null)
         */
        CoolingZoneSetting oldSetting = (CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting();
        if (newSetting.getFanLevel() == null) {
            newSetting.setFanLevel(oldSetting.getFanLevel());
        }
        if (newSetting.getHorizontalSwing() == null) {
            newSetting.setHorizontalSwing(oldSetting.getHorizontalSwing());
        }
        if (newSetting.getVerticalSwing() == null) {
            newSetting.setVerticalSwing(oldSetting.getVerticalSwing());
        }
        if (newSetting.getLight() == null) {
            newSetting.setLight(oldSetting.getLight());
        }
    }

    private AcMode getCurrentOrDefaultAcMode(ZoneStateProvider zoneStateProvider) throws IOException, ApiException {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting();

        return zoneSetting.getMode() != null ? zoneSetting.getMode() : DEFAULT_MODE;
    }

    private TemperatureObject getCurrentOrDefaultTemperature(ZoneStateProvider zoneStateProvider,
            TemperatureRange temperatureRanges) throws IOException, ApiException {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting();

        Float defaultTemperature = temperatureUnit == TemperatureUnit.FAHRENHEIT ? DEFAULT_TEMPERATURE_F
                : DEFAULT_TEMPERATURE_C;
        Float temperature = (zoneSetting != null && zoneSetting.getTemperature() != null)
                ? getTemperatureInUnit(zoneSetting.getTemperature(), temperatureUnit)
                : defaultTemperature;
        IntRange temperatureRange = temperatureUnit == TemperatureUnit.FAHRENHEIT ? temperatureRanges.getFahrenheit()
                : temperatureRanges.getCelsius();

        Float finalTemperature = temperatureRange.getMax() >= temperature && temperatureRange.getMin() <= temperature
                ? temperature
                : temperatureRange.getMax();

        return temperature(finalTemperature, temperatureUnit);
    }

    private AcFanSpeed getCurrentOrDefaultFanSpeed(ZoneStateProvider zoneStateProvider, List<AcFanSpeed> fanSpeeds)
            throws IOException, ApiException {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting();

        if (zoneSetting.getFanSpeed() != null && fanSpeeds.contains(zoneSetting.getFanSpeed())) {
            return zoneSetting.getFanSpeed();
        }

        return fanSpeeds.get(0);
    }

    private Power getCurrentOrDefaultSwing(ZoneStateProvider zoneStateProvider, List<Power> swings)
            throws IOException, ApiException {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting();

        if (zoneSetting.getSwing() != null && swings.contains(zoneSetting.getSwing())) {
            return zoneSetting.getSwing();
        }

        return swings.get(0);
    }

    private CoolingZoneSetting coolingSetting(boolean powerOn) {
        CoolingZoneSetting setting = new CoolingZoneSetting();
        setting.setType(TadoSystemType.AIR_CONDITIONING);
        setting.setPower(powerOn ? Power.ON : Power.OFF);
        return setting;
    }
}
