/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado.internal.builder;

import static org.openhab.binding.tado.internal.api.TadoApiTypeUtils.*;

import java.util.List;

import org.openhab.binding.tado.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.TadoBindingConstants.TemperatureUnit;
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
import org.openhab.binding.tado.internal.api.model.ZoneState;

/**
 *
 *
 * @author Dennis Frommknecht - Iniital contribution
 */
public class AirConditioningZoneSettingsBuilder extends ZoneSettingsBuilder {
    private static final AcMode DEFAULT_MODE = AcMode.COOL;
    private static final float DEFAULT_TEMPERATURE_C = 20.0f;
    private static final float DEFAULT_TEMPERATURE_F = 68.0f;

    @Override
    public GenericZoneSetting build(ZoneState zoneState, GenericZoneCapabilities genericCapabilities) {
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

        if (fanSpeed != null) {
            setting.setFanSpeed(getAcFanSpeed(fanSpeed));
        }

        addMissingSettingParts(zoneState, genericCapabilities, setting);

        return setting;
    }

    private void addMissingSettingParts(ZoneState zoneState, GenericZoneCapabilities genericCapabilities,
            CoolingZoneSetting setting) {

        if (setting.getMode() == null) {
            AcMode targetMode = getCurrentOrDefaultAcMode(zoneState);
            setting.setMode(targetMode);
        }

        AcModeCapabilities capabilities = getModeCapabilities((AirConditioningCapabilities) genericCapabilities,
                setting.getMode());

        if (capabilities.getTemperatures() != null && setting.getTemperature() == null) {
            TemperatureObject targetTemperature = getCurrentOrDefaultTemperature(zoneState,
                    capabilities.getTemperatures());
            setting.setTemperature(targetTemperature);
        }

        if (capabilities.getFanSpeeds() != null && !capabilities.getFanSpeeds().isEmpty()
                && setting.getFanSpeed() == null) {
            AcFanSpeed fanSpeed = getCurrentOrDefaultFanSpeed(zoneState, capabilities.getFanSpeeds());
            setting.setFanSpeed(fanSpeed);
        }

        if (capabilities.getSwings() != null && !capabilities.getSwings().isEmpty() && setting.getSwing() == null) {
            Power swing = getCurrentOrDefaultSwing(zoneState, capabilities.getSwings());
            setting.setSwing(swing);
        }
    }

    private AcMode getCurrentOrDefaultAcMode(ZoneState zoneState) {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneState.getSetting();

        return zoneSetting.getMode() != null ? zoneSetting.getMode() : DEFAULT_MODE;
    }

    private TemperatureObject getCurrentOrDefaultTemperature(ZoneState zoneState, TemperatureRange temperatureRanges) {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneState.getSetting();

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

    private AcFanSpeed getCurrentOrDefaultFanSpeed(ZoneState zoneState, List<AcFanSpeed> fanSpeeds) {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneState.getSetting();

        if (zoneSetting.getFanSpeed() != null && fanSpeeds.contains(zoneSetting.getFanSpeed())) {
            return zoneSetting.getFanSpeed();
        }

        return fanSpeeds.get(0);
    }

    private Power getCurrentOrDefaultSwing(ZoneState zoneState, List<Power> swings) {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneState.getSetting();

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
