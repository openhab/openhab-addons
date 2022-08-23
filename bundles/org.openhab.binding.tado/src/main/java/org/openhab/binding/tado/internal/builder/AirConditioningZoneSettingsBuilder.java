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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final String STATE_VALUE_NOT_SUPPORTED = "Your a/c unit does not support '{}:{}' when in state '{}:{}', (supported values: [{}]).";
    private Logger logger = LoggerFactory.getLogger(AirConditioningZoneSettingsBuilder.class);

    @Override
    public GenericZoneSetting build(ZoneStateProvider zoneStateProvider, GenericZoneCapabilities genericCapabilities)
            throws IOException, ApiException {
        if (mode == HvacMode.OFF) {
            return coolingSetting(false);
        }

        CoolingZoneSetting newSetting = coolingSetting(true);

        AcMode targetMode;
        HvacMode mode = this.mode;
        if (mode != null) {
            targetMode = getAcMode(mode);
            newSetting.setMode(targetMode);
        } else {
            // if mode not changing, so the reference is the current (or default) mode
            targetMode = getCurrentOrDefaultAcMode(zoneStateProvider);
        }

        Boolean swing = this.swing;
        if (swing != null) {
            newSetting.setSwing(swing.booleanValue() ? Power.ON : Power.OFF);
        }

        Boolean light = this.light;
        if (light != null) {
            newSetting.setLight(light.booleanValue() ? Power.ON : Power.OFF);
        }

        FanSpeed fanSpeed = this.fanSpeed;
        if (fanSpeed != null) {
            newSetting.setFanSpeed(getAcFanSpeed(fanSpeed));
        }

        /*
         * In the latest API release Tado introduced extra AC settings that have an open ended list of possible
         * supported state values. And for any particular device, its specific list of supported values is available
         * via its 'capabilities' structure. So before setting a new value, we check if the respective new value is in
         * the capabilities list that corresponds to the target AC mode. And if not, a warning message is logged.
         */
        AcModeCapabilities targetModeCapabilities = TadoApiTypeUtils.getModeCapabilities(targetMode,
                genericCapabilities);

        Float temperature = this.temperature;
        if (temperature != null) {
            IntRange range = null;
            boolean valid = false;
            TemperatureRange caps = targetModeCapabilities.getTemperatures();
            if (caps != null) {
                range = temperatureUnit == TemperatureUnit.CELSIUS ? caps.getCelsius() : caps.getFahrenheit();
                valid = (range != null) && (range.getMin() <= temperature) && (temperature <= range.getMax());
            }
            if (valid) {
                newSetting.setTemperature(temperature(temperature, temperatureUnit));
            } else {
                logger.warn(STATE_VALUE_NOT_SUPPORTED, "Target Temperature", temperature,
                        targetMode.getClass().getSimpleName(), targetMode,
                        range == null ? "none" : String.format("%d..%d", range.getMin(), range.getMax()));
            }
        }

        FanLevel fanLevel = this.fanLevel;
        if (fanLevel != null) {
            ACFanLevel targetFanLevel = getFanLevel(fanLevel);
            List<ACFanLevel> targetFanLevels = targetModeCapabilities.getFanLevel();
            if (targetFanLevels != null && targetFanLevels.contains(targetFanLevel)) {
                newSetting.setFanLevel(targetFanLevel);
            } else {
                logger.warn(STATE_VALUE_NOT_SUPPORTED, targetFanLevel.getClass().getSimpleName(), targetFanLevel,
                        targetMode.getClass().getSimpleName(), targetMode, targetFanLevels);
            }
        }

        HorizontalSwing horizontalSwing = this.horizontalSwing;
        if (horizontalSwing != null) {
            ACHorizontalSwing targetHorizontalSwing = getHorizontalSwing(horizontalSwing);
            List<ACHorizontalSwing> targetHorizontalSwings = targetModeCapabilities.getHorizontalSwing();
            if (targetHorizontalSwings != null && targetHorizontalSwings.contains(targetHorizontalSwing)) {
                newSetting.setHorizontalSwing(targetHorizontalSwing);
            } else {
                logger.warn(STATE_VALUE_NOT_SUPPORTED, targetHorizontalSwing.getClass().getSimpleName(),
                        targetHorizontalSwing, targetMode.getClass().getSimpleName(), targetMode,
                        targetHorizontalSwings);
            }
        }

        VerticalSwing verticalSwing = this.verticalSwing;
        if (verticalSwing != null) {
            ACVerticalSwing targetVerticalSwing = getVerticalSwing(verticalSwing);
            List<ACVerticalSwing> targetVerticalSwings = targetModeCapabilities.getVerticalSwing();
            if (targetVerticalSwings != null && targetVerticalSwings.contains(targetVerticalSwing)) {
                newSetting.setVerticalSwing(targetVerticalSwing);
            } else {
                logger.warn(STATE_VALUE_NOT_SUPPORTED, targetVerticalSwing.getClass().getSimpleName(),
                        targetVerticalSwing, targetMode.getClass().getSimpleName(), targetMode, targetVerticalSwings);
            }
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

        AcModeCapabilities targetCapabilities = getModeCapabilities(newSetting.getMode(), genericCapabilities);

        TemperatureRange temperatures = targetCapabilities.getTemperatures();
        if (temperatures != null && newSetting.getTemperature() == null) {
            newSetting.setTemperature(getCurrentOrDefaultTemperature(zoneStateProvider, temperatures));
        }

        List<AcFanSpeed> fanSpeeds = targetCapabilities.getFanSpeeds();
        if (fanSpeeds != null && !fanSpeeds.isEmpty() && newSetting.getFanSpeed() == null) {
            newSetting.setFanSpeed(getCurrentOrDefaultFanSpeed(zoneStateProvider, fanSpeeds));
        }

        List<Power> swings = targetCapabilities.getSwings();
        if (swings != null && !swings.isEmpty() && newSetting.getSwing() == null) {
            newSetting.setSwing(getCurrentOrDefaultSwing(zoneStateProvider, swings));
        }

        List<ACFanLevel> fanLevels = targetCapabilities.getFanLevel();
        if (fanLevels != null && !fanLevels.isEmpty() && newSetting.getFanLevel() == null) {
            newSetting.setFanLevel(getCurrentOrDefaultFanLevel(zoneStateProvider, fanLevels));
        }

        List<ACHorizontalSwing> horizontalSwings = targetCapabilities.getHorizontalSwing();
        if (horizontalSwings != null && !horizontalSwings.isEmpty() && newSetting.getHorizontalSwing() == null) {
            newSetting.setHorizontalSwing(getCurrentOrDefaultHorizontalSwing(zoneStateProvider, horizontalSwings));
        }

        List<ACVerticalSwing> verticalSwings = targetCapabilities.getVerticalSwing();
        if (verticalSwings != null && !verticalSwings.isEmpty() && newSetting.getVerticalSwing() == null) {
            newSetting.setVerticalSwing(getCurrentOrDefaultVerticalSwing(zoneStateProvider, verticalSwings));
        }

        List<Power> lights = targetCapabilities.getLight();
        if (lights != null && !lights.isEmpty() && newSetting.getLight() == null) {
            newSetting.setLight(getCurrentOrDefaultLight(zoneStateProvider, lights));
        }
    }

    private AcMode getCurrentOrDefaultAcMode(ZoneStateProvider zoneStateProvider) throws IOException, ApiException {
        AcMode acMode = ((CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting()).getMode();
        return acMode != null ? acMode : DEFAULT_MODE;
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
        AcFanSpeed fanSpeed = ((CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting()).getFanSpeed();
        return (fanSpeed != null) && fanSpeeds.contains(fanSpeed) ? fanSpeed : fanSpeeds.get(0);
    }

    private Power getCurrentOrDefaultSwing(ZoneStateProvider zoneStateProvider, List<Power> swings)
            throws IOException, ApiException {
        Power swing = ((CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting()).getSwing();
        return (swing != null) && swings.contains(swing) ? swing : swings.get(0);
    }

    private ACFanLevel getCurrentOrDefaultFanLevel(ZoneStateProvider zoneStateProvider, List<ACFanLevel> fanLevels)
            throws IOException, ApiException {
        ACFanLevel fanLevel = ((CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting()).getFanLevel();
        return (fanLevel != null) && fanLevels.contains(fanLevel) ? fanLevel : fanLevels.get(0);
    }

    private ACVerticalSwing getCurrentOrDefaultVerticalSwing(ZoneStateProvider zoneStateProvider,
            List<ACVerticalSwing> vertSwings) throws IOException, ApiException {
        ACVerticalSwing vertSwing = ((CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting())
                .getVerticalSwing();
        return (vertSwing != null) && vertSwings.contains(vertSwing) ? vertSwing : vertSwings.get(0);
    }

    private ACHorizontalSwing getCurrentOrDefaultHorizontalSwing(ZoneStateProvider zoneStateProvider,
            List<ACHorizontalSwing> horzSwings) throws IOException, ApiException {
        ACHorizontalSwing horzSwing = ((CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting())
                .getHorizontalSwing();
        return (horzSwing != null) && horzSwings.contains(horzSwing) ? horzSwing : horzSwings.get(0);
    }

    private Power getCurrentOrDefaultLight(ZoneStateProvider zoneStateProvider, List<Power> lights)
            throws IOException, ApiException {
        Power light = ((CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting()).getLight();
        return (light != null) && lights.contains(light) ? light : lights.get(0);
    }

    private CoolingZoneSetting coolingSetting(boolean powerOn) {
        CoolingZoneSetting setting = new CoolingZoneSetting();
        setting.setType(TadoSystemType.AIR_CONDITIONING);
        setting.setPower(powerOn ? Power.ON : Power.OFF);
        return setting;
    }
}
