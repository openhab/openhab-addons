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
package org.openhab.binding.nest.internal.sdm.handler;

import static org.openhab.binding.nest.internal.sdm.SDMBindingConstants.*;
import static org.openhab.core.library.unit.ImperialUnits.FAHRENHEIT;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.PERCENT;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.sdm.SDMBindingConstants;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMSetFanTimerRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMSetThermostatCoolSetpointRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMSetThermostatEcoModeRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMSetThermostatHeatSetpointRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMSetThermostatModeRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMSetThermostatRangeSetpointRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMEvent;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMDeviceSettingsTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMFanTimerMode;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMFanTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMHumidityTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMTemperatureTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatEcoMode;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatEcoTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatHvacTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatMode;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatModeTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatTemperatureSetpointTrait;
import org.openhab.binding.nest.internal.sdm.exception.FailedSendingSDMDataException;
import org.openhab.binding.nest.internal.sdm.exception.InvalidSDMAccessTokenException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SDMThermostatHandler} handles state updates and commands for SDM thermostat devices.
 *
 * @author Brian Higginbotham - Initial contribution
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMThermostatHandler extends SDMBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(SDMThermostatHandler.class);

    public SDMThermostatHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                delayedRefresh();
            } else if (CHANNEL_CURRENT_ECO_MODE.equals(channelUID.getId())) {
                if (command instanceof StringType) {
                    SDMThermostatEcoMode mode = SDMThermostatEcoMode.valueOf(command.toString());
                    executeDeviceCommand(new SDMSetThermostatEcoModeRequest(mode));
                    delayedRefresh();
                }
            } else if (CHANNEL_CURRENT_MODE.equals(channelUID.getId())) {
                if (command instanceof StringType) {
                    SDMThermostatMode mode = SDMThermostatMode.valueOf(command.toString());
                    executeDeviceCommand(new SDMSetThermostatModeRequest(mode));
                    delayedRefresh();
                }
            } else if (CHANNEL_FAN_TIMER_MODE.equals(channelUID.getId())) {
                if (command instanceof OnOffType) {
                    if ((OnOffType) command == OnOffType.ON) {
                        executeDeviceCommand(new SDMSetFanTimerRequest(SDMFanTimerMode.ON, getFanTimerDuration()));
                    } else {
                        executeDeviceCommand(new SDMSetFanTimerRequest(SDMFanTimerMode.OFF));
                    }
                    delayedRefresh();
                }
            } else if (CHANNEL_FAN_TIMER_TIMEOUT.equals(channelUID.getId())) {
                if (command instanceof DateTimeType) {
                    Duration duration = Duration.between(ZonedDateTime.now(),
                            ((DateTimeType) command).getZonedDateTime());
                    executeDeviceCommand(new SDMSetFanTimerRequest(SDMFanTimerMode.ON, duration));
                    delayedRefresh();
                }
            } else if (CHANNEL_MAXIMUM_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof QuantityType) {
                    BigDecimal minTemperature = getMinTemperature();
                    if (minTemperature != null) {
                        setTargetTemperature(new QuantityType<>(minTemperature, CELSIUS),
                                (QuantityType<Temperature>) command);
                        delayedRefresh();
                    }
                }
            } else if (CHANNEL_MINIMUM_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof QuantityType) {
                    BigDecimal maxTemperature = getMaxTemperature();
                    if (maxTemperature != null) {
                        setTargetTemperature((QuantityType<Temperature>) command,
                                new QuantityType<>(maxTemperature, CELSIUS));
                        delayedRefresh();
                    }
                }
            } else if (CHANNEL_TARGET_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof QuantityType) {
                    setTargetTemperature((QuantityType<Temperature>) command);
                    delayedRefresh();
                }
            }
        } catch (FailedSendingSDMDataException | InvalidSDMAccessTokenException e) {
            logger.debug("Exception while handling {} command for {}: {}", command, thing.getUID(), e.getMessage());
        }
    }

    @Override
    protected void updateStateWithTraits(SDMTraits traits) {
        logger.debug("Refreshing channels for: {}", thing.getUID());
        super.updateStateWithTraits(traits);

        SDMHumidityTrait humidity = traits.humidity;
        if (humidity != null) {
            updateState(CHANNEL_AMBIENT_HUMIDITY, new QuantityType<>(humidity.ambientHumidityPercent, PERCENT));
        }

        SDMTemperatureTrait temperature = traits.temperature;
        if (temperature != null) {
            updateState(CHANNEL_AMBIENT_TEMPERATURE, temperatureToState(temperature.ambientTemperatureCelsius));
        }

        SDMThermostatModeTrait thermostatMode = traits.thermostatMode;
        if (thermostatMode != null) {
            updateState(CHANNEL_CURRENT_MODE, new StringType(thermostatMode.mode.name()));
        }

        SDMThermostatEcoTrait thermostatEco = traits.thermostatEco;
        if (thermostatEco != null) {
            updateState(CHANNEL_CURRENT_ECO_MODE, new StringType(thermostatEco.mode.name()));
        }

        SDMFanTrait fan = traits.fan;
        if (fan != null) {
            updateState(CHANNEL_FAN_TIMER_MODE, fan.timerMode == SDMFanTimerMode.ON ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_FAN_TIMER_TIMEOUT, fan.timerTimeout == null ? UnDefType.NULL
                    : new DateTimeType(fan.timerTimeout.withZoneSameInstant(timeZoneProvider.getTimeZone())));
        }

        SDMThermostatHvacTrait thermostatHvac = traits.thermostatHvac;
        if (thermostatHvac != null) {
            updateState(CHANNEL_HVAC_STATUS, new StringType(thermostatHvac.status.name()));
        }

        BigDecimal maxTemperature = getMaxTemperature();
        if (maxTemperature != null) {
            updateState(CHANNEL_MAXIMUM_TEMPERATURE, temperatureToState(getMaxTemperature()));
        }

        BigDecimal minTemperature = getMinTemperature();
        if (minTemperature != null) {
            updateState(CHANNEL_MINIMUM_TEMPERATURE, temperatureToState(minTemperature));
        }

        BigDecimal targetTemperature = getTargetTemperature();
        if (targetTemperature != null) {
            updateState(CHANNEL_TARGET_TEMPERATURE, temperatureToState(targetTemperature));
        }
    }

    private Duration getFanTimerDuration() {
        long seconds = 900;

        Channel channel = getThing().getChannel(SDMBindingConstants.CHANNEL_FAN_TIMER_MODE);
        if (channel != null) {
            Configuration configuration = channel.getConfiguration();
            Object fanTimerDuration = configuration.get(SDMBindingConstants.CONFIG_PROPERTY_FAN_TIMER_DURATION);
            if (fanTimerDuration instanceof BigDecimal) {
                seconds = ((BigDecimal) fanTimerDuration).longValue();
            }
        }

        return Duration.ofSeconds(seconds);
    }

    private @Nullable BigDecimal getMinTemperature() {
        SDMThermostatEcoTrait thermostatEco = device.traits.thermostatEco;
        if (thermostatEco != null && thermostatEco.mode == SDMThermostatEcoMode.MANUAL_ECO) {
            return thermostatEco.heatCelsius;
        }

        SDMThermostatTemperatureSetpointTrait thermostatTemperatureSetpoint = device.traits.thermostatTemperatureSetpoint;
        SDMThermostatModeTrait thermostatMode = device.traits.thermostatMode;
        if (thermostatMode != null && thermostatMode.mode == SDMThermostatMode.HEATCOOL) {
            return thermostatTemperatureSetpoint.heatCelsius;
        }

        return null;
    }

    private @Nullable BigDecimal getMaxTemperature() {
        SDMThermostatEcoTrait thermostatEco = device.traits.thermostatEco;
        if (thermostatEco != null && thermostatEco.mode == SDMThermostatEcoMode.MANUAL_ECO) {
            return thermostatEco.coolCelsius;
        }

        SDMThermostatTemperatureSetpointTrait thermostatTemperatureSetpoint = device.traits.thermostatTemperatureSetpoint;
        SDMThermostatModeTrait thermostatMode = device.traits.thermostatMode;
        if (thermostatMode != null && thermostatMode.mode == SDMThermostatMode.HEATCOOL) {
            return thermostatTemperatureSetpoint.coolCelsius;
        }

        return null;
    }

    private @Nullable BigDecimal getTargetTemperature() {
        SDMThermostatEcoTrait thermostatEco = device.traits.thermostatEco;
        if (thermostatEco != null && thermostatEco.mode == SDMThermostatEcoMode.MANUAL_ECO) {
            return null;
        }

        SDMThermostatTemperatureSetpointTrait thermostatTemperatureSetpoint = device.traits.thermostatTemperatureSetpoint;
        SDMThermostatModeTrait thermostatMode = device.traits.thermostatMode;
        if (thermostatMode != null) {
            if (thermostatMode.mode == SDMThermostatMode.COOL) {
                return thermostatTemperatureSetpoint.coolCelsius;
            }
            if (thermostatMode.mode == SDMThermostatMode.HEAT) {
                return thermostatTemperatureSetpoint.heatCelsius;
            }
        }

        return null;
    }

    @Override
    public void onEvent(SDMEvent event) {
        super.onEvent(event);

        SDMTraits traits = getTraitsForUpdate(event);
        if (traits == null) {
            return;
        }

        updateStateWithTraits(traits);

        SDMThermostatTemperatureSetpointTrait thermostatTemperatureSetpoint = traits.thermostatTemperatureSetpoint;
        if (thermostatTemperatureSetpoint != null) {
            BigDecimal coolCelsius = thermostatTemperatureSetpoint.coolCelsius;
            BigDecimal heatCelsius = thermostatTemperatureSetpoint.heatCelsius;
            if (coolCelsius != null && heatCelsius != null) {
                updateState(CHANNEL_MINIMUM_TEMPERATURE, temperatureToState(heatCelsius));
                updateState(CHANNEL_MAXIMUM_TEMPERATURE, temperatureToState(coolCelsius));
            }
        }

        SDMThermostatEcoTrait thermostatEco = traits.thermostatEco;
        if (thermostatEco != null) {
            if (thermostatEco.mode == SDMThermostatEcoMode.MANUAL_ECO) {
                updateState(CHANNEL_MINIMUM_TEMPERATURE, temperatureToState(thermostatEco.heatCelsius));
                updateState(CHANNEL_MAXIMUM_TEMPERATURE, temperatureToState(thermostatEco.coolCelsius));
            }
        }
    }

    private void setTargetTemperature(QuantityType<Temperature> value)
            throws FailedSendingSDMDataException, InvalidSDMAccessTokenException {
        logger.debug("setThermostatTargetTemperature value={}", value);
        SDMThermostatModeTrait thermostatMode = device.traits.thermostatMode;
        if (thermostatMode.mode == SDMThermostatMode.COOL) {
            executeDeviceCommand(new SDMSetThermostatCoolSetpointRequest(toCelsiusBigDecimal(value)));
        } else if (thermostatMode.mode == SDMThermostatMode.HEAT) {
            executeDeviceCommand(new SDMSetThermostatHeatSetpointRequest(toCelsiusBigDecimal(value)));
        } else {
            throw new IllegalStateException("INVALID use case for setThermostatTargetTemperature");
        }
    }

    private void setTargetTemperature(QuantityType<Temperature> minValue, QuantityType<Temperature> maxValue)
            throws FailedSendingSDMDataException, InvalidSDMAccessTokenException {
        logger.debug("setThermostatTargetTemperature minValue={} maxValue={}", minValue, maxValue);
        SDMThermostatModeTrait thermostatMode = device.traits.thermostatMode;
        if (thermostatMode.mode == SDMThermostatMode.HEATCOOL) {
            executeDeviceCommand(new SDMSetThermostatRangeSetpointRequest(toCelsiusBigDecimal(minValue),
                    toCelsiusBigDecimal(maxValue)));
        } else {
            throw new IllegalStateException("INVALID use case for setThermostatTargetTemperature");
        }
    }

    protected State temperatureToState(@Nullable BigDecimal value) {
        if (value == null) {
            return UnDefType.NULL;
        }

        QuantityType<Temperature> temperature = new QuantityType<>(value, CELSIUS);

        if (getDeviceTemperatureUnit() == FAHRENHEIT) {
            QuantityType<Temperature> converted = temperature.toUnit(FAHRENHEIT);
            return converted == null ? UnDefType.NULL : converted;
        }

        return temperature;
    }

    private Unit<Temperature> getDeviceTemperatureUnit() {
        SDMDeviceSettingsTrait deviceSettings = device.traits.deviceSettings;
        if (deviceSettings == null) {
            return CELSIUS;
        }

        switch (deviceSettings.temperatureScale) {
            case CELSIUS:
                return CELSIUS;
            case FAHRENHEIT:
                return FAHRENHEIT;
            default:
                return CELSIUS;
        }
    }

    private BigDecimal toCelsiusBigDecimal(QuantityType<Temperature> temperature) {
        QuantityType<Temperature> celsiusTemperature = temperature.toUnit(CELSIUS);
        if (celsiusTemperature == null) {
            throw new IllegalArgumentException(
                    String.format("Temperature '%s' cannot be converted to Celsius unit", temperature));
        }
        return celsiusTemperature.toBigDecimal();
    }
}
