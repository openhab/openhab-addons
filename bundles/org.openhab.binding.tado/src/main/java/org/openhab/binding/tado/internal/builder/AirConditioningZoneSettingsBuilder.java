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

import org.openhab.binding.tado.internal.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.internal.TadoBindingConstants.TemperatureUnit;
import org.openhab.binding.tado.internal.api.ApiException;
import org.openhab.binding.tado.internal.api.model.ACFanLevel;
import org.openhab.binding.tado.internal.api.model.ACHorizontalSwing;
import org.openhab.binding.tado.internal.api.model.ACVerticalSwing;
import org.openhab.binding.tado.internal.api.model.AcFanSpeed;
import org.openhab.binding.tado.internal.api.model.AcMode;
import org.openhab.binding.tado.internal.api.model.AcModeCapabilities;
import org.openhab.binding.tado.internal.api.model.AirConditioningCapabilities;
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
public class AirConditioningZoneSettingsBuilder extends ZoneSettingsBuilder {
    private static final AcMode DEFAULT_MODE = AcMode.COOL;
    private static final float DEFAULT_TEMPERATURE_C = 20.0f;
    private static final float DEFAULT_TEMPERATURE_F = 68.0f;

    @Override
    public GenericZoneSetting build(ZoneStateProvider zoneStateProvider, GenericZoneCapabilities genericCapabilities)
            throws IOException, ApiException {
        if (mode == HvacMode.OFF) {
            return coolingSetting(false);
        }

        CoolingZoneSetting setting = coolingSetting(true);
        setting.setMode(getAcMode(mode));
        if (temperature != null) {
            setting.setTemperature(temperature(temperature, temperatureUnit));
        }

        if (swing != null) {
            setting.setSwing(swing ? Power.ON : Power.OFF);
        }

        if (light != null) {
            setting.setLight(light ? Power.ON : Power.OFF);
        }

        if (fanSpeed != null) {
            setting.setFanSpeed(getAcFanSpeed(fanSpeed));
        }

        if (fanLevel != null) {
            setting.setFanLevel(getFanLevel(fanLevel));
        }

        if (horizontalSwing != null) {
            setting.setHorizontalSwing(getHorizontalSwing(horizontalSwing));
        }

        if (verticalSwing != null) {
            setting.setVerticalSwing(getVerticalSwing(verticalSwing));
        }

        addMissingSettingParts(zoneStateProvider, genericCapabilities, setting);

        return setting;
    }

    private void addMissingSettingParts(ZoneStateProvider zoneStateProvider,
            GenericZoneCapabilities genericCapabilities, CoolingZoneSetting setting) throws IOException, ApiException {
        if (setting.getMode() == null) {
            AcMode targetMode = getCurrentOrDefaultAcMode(zoneStateProvider);
            setting.setMode(targetMode);
        }

        AcModeCapabilities capabilities = getModeCapabilities((AirConditioningCapabilities) genericCapabilities,
                setting.getMode());

        TemperatureRange temperatures = capabilities.getTemperatures();
        if (temperatures != null && setting.getTemperature() == null) {
            setting.setTemperature(getCurrentOrDefaultTemperature(zoneStateProvider, temperatures));
        }

        List<AcFanSpeed> fanSpeeds = capabilities.getFanSpeeds();
        if (fanSpeeds != null && !fanSpeeds.isEmpty() && setting.getFanSpeed() == null) {
            setting.setFanSpeed(getCurrentOrDefaultFanSpeed(zoneStateProvider, fanSpeeds));
        }

        List<Power> swings = capabilities.getSwings();
        if (swings != null && !swings.isEmpty() && setting.getSwing() == null) {
            setting.setSwing(getCurrentOrDefaultSwing(zoneStateProvider, swings));
        }

        // Tado confusingly calls the List / getter method 'fanLevel' / 'getFanLevel()' without 's'
        List<ACFanLevel> fanLevels = capabilities.getFanLevel();
        if (fanLevels != null && !fanLevels.isEmpty() && setting.getFanLevel() == null) {
            setting.setFanLevel(getCurrentOrDefaultFanLevel(zoneStateProvider, fanLevels));
        }

        // Tado confusingly calls the List / getter method 'horizontalSwing' / 'getHorizontalSwing()' without 's'
        List<ACHorizontalSwing> horizontalSwings = capabilities.getHorizontalSwing();
        if (horizontalSwings != null && !horizontalSwings.isEmpty() && setting.getHorizontalSwing() == null) {
            setting.setHorizontalSwing(getCurrentOrDefaultHorizontalSwing(zoneStateProvider, horizontalSwings));
        }

        // Tado confusingly calls the List / getter method 'verticalSwing' / 'getVerticalSwing()' without 's'
        List<ACVerticalSwing> verticalSwings = capabilities.getVerticalSwing();
        if (verticalSwings != null && !verticalSwings.isEmpty() && setting.getVerticalSwing() == null) {
            setting.setVerticalSwing(getCurrentOrDefaultVerticalSwing(zoneStateProvider, verticalSwings));
        }

        // Tado confusingly calls the List / getter method 'light' / 'getLight()' without 's'
        List<Power> lights = capabilities.getLight();
        if (lights != null && !lights.isEmpty() && setting.getLight() == null) {
            setting.setLight(getCurrentOrDefaultLight(zoneStateProvider, lights));
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

    private Power getCurrentOrDefaultLight(ZoneStateProvider zoneStateProvider, List<Power> lights)
            throws IOException, ApiException {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting();

        if (zoneSetting.getLight() != null && lights.contains(zoneSetting.getLight())) {
            return zoneSetting.getLight();
        }

        return lights.get(0);
    }

    private ACFanLevel getCurrentOrDefaultFanLevel(ZoneStateProvider zoneStateProvider, List<ACFanLevel> fanLevels)
            throws IOException, ApiException {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting();

        if (zoneSetting.getFanLevel() != null && fanLevels.contains(zoneSetting.getFanLevel())) {
            return zoneSetting.getFanLevel();
        }

        return fanLevels.get(0);
    }

    private ACHorizontalSwing getCurrentOrDefaultHorizontalSwing(ZoneStateProvider zoneStateProvider,
            List<ACHorizontalSwing> horizontalSwings) throws IOException, ApiException {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting();

        if (zoneSetting.getHorizontalSwing() != null && horizontalSwings.contains(zoneSetting.getHorizontalSwing())) {
            return zoneSetting.getHorizontalSwing();
        }

        return horizontalSwings.get(0);
    }

    private ACVerticalSwing getCurrentOrDefaultVerticalSwing(ZoneStateProvider zoneStateProvider,
            List<ACVerticalSwing> verticalSwings) throws IOException, ApiException {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting();

        if (zoneSetting.getVerticalSwing() != null && verticalSwings.contains(zoneSetting.getVerticalSwing())) {
            return zoneSetting.getVerticalSwing();
        }

        return verticalSwings.get(0);
    }

    private CoolingZoneSetting coolingSetting(boolean powerOn) {
        CoolingZoneSetting setting = new CoolingZoneSetting();
        setting.setType(TadoSystemType.AIR_CONDITIONING);
        setting.setPower(powerOn ? Power.ON : Power.OFF);
        return setting;
    }
}
