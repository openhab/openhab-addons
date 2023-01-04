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
package org.openhab.binding.tado.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tado.internal.TadoBindingConstants.FanLevel;
import org.openhab.binding.tado.internal.TadoBindingConstants.FanSpeed;
import org.openhab.binding.tado.internal.TadoBindingConstants.HorizontalSwing;
import org.openhab.binding.tado.internal.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.internal.TadoBindingConstants.TemperatureUnit;
import org.openhab.binding.tado.internal.TadoBindingConstants.VerticalSwing;
import org.openhab.binding.tado.internal.api.model.ACFanLevel;
import org.openhab.binding.tado.internal.api.model.ACHorizontalSwing;
import org.openhab.binding.tado.internal.api.model.ACVerticalSwing;
import org.openhab.binding.tado.internal.api.model.AcFanSpeed;
import org.openhab.binding.tado.internal.api.model.AcMode;
import org.openhab.binding.tado.internal.api.model.AcModeCapabilities;
import org.openhab.binding.tado.internal.api.model.AirConditioningCapabilities;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.ManualTerminationCondition;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationCondition;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationConditionTemplate;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationConditionType;
import org.openhab.binding.tado.internal.api.model.TadoModeTerminationCondition;
import org.openhab.binding.tado.internal.api.model.TemperatureObject;
import org.openhab.binding.tado.internal.api.model.TimerTerminationCondition;
import org.openhab.binding.tado.internal.api.model.TimerTerminationConditionTemplate;

/**
 * Utility methods for the conversion of API types.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
@NonNullByDefault
public class TadoApiTypeUtils {

    public static OverlayTerminationCondition getTerminationCondition(OverlayTerminationConditionType type,
            int timerDurationInSeconds) {
        switch (type) {
            case TIMER:
                return timerTermination(timerDurationInSeconds);
            case MANUAL:
                return manualTermination();
            case TADO_MODE:
                return tadoModeTermination();
            default:
                throw new IllegalArgumentException("Unexpected OverlayTerminationConditionType " + type);
        }
    }

    public static OverlayTerminationCondition cleanTerminationCondition(
            OverlayTerminationCondition terminationCondition) {
        OverlayTerminationConditionType conditionType = terminationCondition.getType();

        int timerDuration = 0;
        if (conditionType == OverlayTerminationConditionType.TIMER) {
            Integer duration = ((TimerTerminationCondition) terminationCondition).getRemainingTimeInSeconds();
            if (duration != null) {
                timerDuration = duration.intValue();
            }
        }

        return getTerminationCondition(conditionType, timerDuration);
    }

    public static OverlayTerminationCondition terminationConditionTemplateToTerminationCondition(
            OverlayTerminationConditionTemplate template) {
        OverlayTerminationConditionType conditionType = template.getType();

        int timerDuration = 0;
        if (conditionType == OverlayTerminationConditionType.TIMER) {
            Integer duration = ((TimerTerminationConditionTemplate) template).getDurationInSeconds();
            if (duration != null) {
                timerDuration = duration.intValue();
            }
        }

        return getTerminationCondition(conditionType, timerDuration);
    }

    public static TimerTerminationCondition timerTermination(int durationInSeconds) {
        TimerTerminationCondition terminationCondition = new TimerTerminationCondition();
        terminationCondition.setType(OverlayTerminationConditionType.TIMER);
        terminationCondition.setDurationInSeconds(durationInSeconds);
        return terminationCondition;
    }

    public static ManualTerminationCondition manualTermination() {
        ManualTerminationCondition terminationCondition = new ManualTerminationCondition();
        terminationCondition.setType(OverlayTerminationConditionType.MANUAL);
        return terminationCondition;
    }

    public static TadoModeTerminationCondition tadoModeTermination() {
        TadoModeTerminationCondition terminationCondition = new TadoModeTerminationCondition();
        terminationCondition.setType(OverlayTerminationConditionType.TADO_MODE);
        return terminationCondition;
    }

    public static TemperatureObject temperature(float degree, TemperatureUnit temperatureUnit) {
        TemperatureObject temperature = new TemperatureObject();
        if (temperatureUnit == TemperatureUnit.FAHRENHEIT) {
            temperature.setFahrenheit(degree);
        } else {
            temperature.setCelsius(degree);
        }

        return temperature;
    }

    public static Float getTemperatureInUnit(TemperatureObject temperature, TemperatureUnit temperatureUnit) {
        return temperatureUnit == TemperatureUnit.FAHRENHEIT ? temperature.getFahrenheit() : temperature.getCelsius();
    }

    public static AcMode getAcMode(HvacMode mode) {
        switch (mode) {
            case HEAT:
                return AcMode.HEAT;
            case COOL:
                return AcMode.COOL;
            case FAN:
                return AcMode.FAN;
            case DRY:
                return AcMode.DRY;
            case AUTO:
                return AcMode.AUTO;
            default:
                throw new IllegalArgumentException("Unexpected AcMode " + mode);
        }
    }

    public static AcFanSpeed getAcFanSpeed(FanSpeed fanSpeed) {
        switch (fanSpeed) {
            case AUTO:
                return AcFanSpeed.AUTO;
            case HIGH:
                return AcFanSpeed.HIGH;
            case MIDDLE:
                return AcFanSpeed.MIDDLE;
            case LOW:
                return AcFanSpeed.LOW;
            default:
                throw new IllegalArgumentException("Unexpected AcFanSpeed " + fanSpeed);
        }
    }

    public static ACFanLevel getFanLevel(FanLevel fanLevel) {
        switch (fanLevel) {
            case AUTO:
                return ACFanLevel.AUTO;
            case LEVEL1:
                return ACFanLevel.LEVEL1;
            case LEVEL2:
                return ACFanLevel.LEVEL2;
            case LEVEL3:
                return ACFanLevel.LEVEL3;
            case LEVEL4:
                return ACFanLevel.LEVEL4;
            case LEVEL5:
                return ACFanLevel.LEVEL5;
            case SILENT:
                return ACFanLevel.SILENT;
            default:
                throw new IllegalArgumentException("Unexpected FanLevel " + fanLevel);
        }
    }

    public static ACHorizontalSwing getHorizontalSwing(HorizontalSwing horizontalSwing) {
        switch (horizontalSwing) {
            case LEFT:
                return ACHorizontalSwing.LEFT;
            case MID_LEFT:
                return ACHorizontalSwing.MID_LEFT;
            case MID:
                return ACHorizontalSwing.MID;
            case MID_RIGHT:
                return ACHorizontalSwing.MID_RIGHT;
            case RIGHT:
                return ACHorizontalSwing.RIGHT;
            case ON:
                return ACHorizontalSwing.ON;
            case OFF:
                return ACHorizontalSwing.OFF;
            case AUTO:
                return ACHorizontalSwing.AUTO;
            default:
                throw new IllegalArgumentException("Unexpected HorizontalSwing " + horizontalSwing);
        }
    }

    public static ACVerticalSwing getVerticalSwing(VerticalSwing verticalSwing) {
        switch (verticalSwing) {
            case AUTO:
                return ACVerticalSwing.AUTO;
            case UP:
                return ACVerticalSwing.UP;
            case MID_UP:
                return ACVerticalSwing.MID_UP;
            case MID:
                return ACVerticalSwing.MID;
            case MID_DOWN:
                return ACVerticalSwing.MID_DOWN;
            case DOWN:
                return ACVerticalSwing.DOWN;
            case ON:
                return ACVerticalSwing.ON;
            case OFF:
                return ACVerticalSwing.OFF;
            default:
                throw new IllegalArgumentException("Unexpected VerticalSwing " + verticalSwing);
        }
    }

    public static AcModeCapabilities getModeCapabilities(AcMode acMode,
            @Nullable GenericZoneCapabilities capabilities) {
        AirConditioningCapabilities acCapabilities;

        if (capabilities instanceof AirConditioningCapabilities) {
            acCapabilities = (AirConditioningCapabilities) capabilities;
        } else {
            acCapabilities = new AirConditioningCapabilities();
        }

        switch (acMode) {
            case COOL:
                return acCapabilities.getCOOL();
            case HEAT:
                return acCapabilities.getHEAT();
            case DRY:
                return acCapabilities.getDRY();
            case AUTO:
                return acCapabilities.getAUTO();
            case FAN:
                return acCapabilities.getFAN();
            default:
                throw new IllegalArgumentException("Unexpected AcMode " + acMode);
        }
    }
}
