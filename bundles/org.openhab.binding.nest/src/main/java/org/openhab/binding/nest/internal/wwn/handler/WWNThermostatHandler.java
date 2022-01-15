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
package org.openhab.binding.nest.internal.wwn.handler;

import static org.openhab.binding.nest.internal.wwn.WWNBindingConstants.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.wwn.dto.WWNThermostat;
import org.openhab.binding.nest.internal.wwn.dto.WWNThermostat.Mode;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WWNThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels for the thermostat.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Handle channel refresh command
 */
@NonNullByDefault
public class WWNThermostatHandler extends WWNBaseHandler<WWNThermostat> {
    private final Logger logger = LoggerFactory.getLogger(WWNThermostatHandler.class);

    public WWNThermostatHandler(Thing thing) {
        super(thing, WWNThermostat.class);
    }

    @Override
    protected State getChannelState(ChannelUID channelUID, WWNThermostat thermostat) {
        switch (channelUID.getId()) {
            case CHANNEL_CAN_COOL:
                return getAsOnOffTypeOrNull(thermostat.isCanCool());
            case CHANNEL_CAN_HEAT:
                return getAsOnOffTypeOrNull(thermostat.isCanHeat());
            case CHANNEL_ECO_MAX_SET_POINT:
                return getAsQuantityTypeOrNull(thermostat.getEcoTemperatureHigh(), thermostat.getTemperatureUnit());
            case CHANNEL_ECO_MIN_SET_POINT:
                return getAsQuantityTypeOrNull(thermostat.getEcoTemperatureLow(), thermostat.getTemperatureUnit());
            case CHANNEL_FAN_TIMER_ACTIVE:
                return getAsOnOffTypeOrNull(thermostat.isFanTimerActive());
            case CHANNEL_FAN_TIMER_DURATION:
                return getAsQuantityTypeOrNull(thermostat.getFanTimerDuration(), Units.MINUTE);
            case CHANNEL_FAN_TIMER_TIMEOUT:
                return getAsDateTimeTypeOrNull(thermostat.getFanTimerTimeout());
            case CHANNEL_HAS_FAN:
                return getAsOnOffTypeOrNull(thermostat.isHasFan());
            case CHANNEL_HAS_LEAF:
                return getAsOnOffTypeOrNull(thermostat.isHasLeaf());
            case CHANNEL_HUMIDITY:
                return getAsQuantityTypeOrNull(thermostat.getHumidity(), Units.PERCENT);
            case CHANNEL_LAST_CONNECTION:
                return getAsDateTimeTypeOrNull(thermostat.getLastConnection());
            case CHANNEL_LOCKED:
                return getAsOnOffTypeOrNull(thermostat.isLocked());
            case CHANNEL_LOCKED_MAX_SET_POINT:
                return getAsQuantityTypeOrNull(thermostat.getLockedTempMax(), thermostat.getTemperatureUnit());
            case CHANNEL_LOCKED_MIN_SET_POINT:
                return getAsQuantityTypeOrNull(thermostat.getLockedTempMin(), thermostat.getTemperatureUnit());
            case CHANNEL_MAX_SET_POINT:
                return getAsQuantityTypeOrNull(thermostat.getTargetTemperatureHigh(), thermostat.getTemperatureUnit());
            case CHANNEL_MIN_SET_POINT:
                return getAsQuantityTypeOrNull(thermostat.getTargetTemperatureLow(), thermostat.getTemperatureUnit());
            case CHANNEL_MODE:
                return getAsStringTypeOrNull(thermostat.getMode());
            case CHANNEL_PREVIOUS_MODE:
                Mode previousMode = thermostat.getPreviousHvacMode() != null ? thermostat.getPreviousHvacMode()
                        : thermostat.getMode();
                return getAsStringTypeOrNull(previousMode);
            case CHANNEL_STATE:
                return getAsStringTypeOrNull(thermostat.getHvacState());
            case CHANNEL_SET_POINT:
                return getAsQuantityTypeOrNull(thermostat.getTargetTemperature(), thermostat.getTemperatureUnit());
            case CHANNEL_SUNLIGHT_CORRECTION_ACTIVE:
                return getAsOnOffTypeOrNull(thermostat.isSunlightCorrectionActive());
            case CHANNEL_SUNLIGHT_CORRECTION_ENABLED:
                return getAsOnOffTypeOrNull(thermostat.isSunlightCorrectionEnabled());
            case CHANNEL_TEMPERATURE:
                return getAsQuantityTypeOrNull(thermostat.getAmbientTemperature(), thermostat.getTemperatureUnit());
            case CHANNEL_TIME_TO_TARGET:
                return getAsQuantityTypeOrNull(thermostat.getTimeToTarget(), Units.MINUTE);
            case CHANNEL_USING_EMERGENCY_HEAT:
                return getAsOnOffTypeOrNull(thermostat.isUsingEmergencyHeat());
            default:
                logger.error("Unsupported channelId '{}'", channelUID.getId());
                return UnDefType.UNDEF;
        }
    }

    /**
     * Handle the command to do things to the thermostat, this will change the
     * value of a channel by sending the request to Nest.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (REFRESH.equals(command)) {
            WWNThermostat lastUpdate = getLastUpdate();
            if (lastUpdate != null) {
                updateState(channelUID, getChannelState(channelUID, lastUpdate));
            }
        } else if (CHANNEL_FAN_TIMER_ACTIVE.equals(channelUID.getId())) {
            if (command instanceof OnOffType) {
                // Update fan timer active to the command value
                addUpdateRequest("fan_timer_active", command == OnOffType.ON);
            }
        } else if (CHANNEL_FAN_TIMER_DURATION.equals(channelUID.getId())) {
            if (command instanceof QuantityType) {
                // Update fan timer duration to the command value
                QuantityType<Time> minuteQuantity = ((QuantityType<Time>) command).toUnit(Units.MINUTE);
                if (minuteQuantity != null) {
                    addUpdateRequest("fan_timer_duration", minuteQuantity.intValue());
                }
            }
        } else if (CHANNEL_MAX_SET_POINT.equals(channelUID.getId())) {
            if (command instanceof QuantityType) {
                // Update maximum set point to the command value
                addTemperatureUpdateRequest("target_temperature_high_c", "target_temperature_high_f",
                        (QuantityType<Temperature>) command);
            }
        } else if (CHANNEL_MIN_SET_POINT.equals(channelUID.getId())) {
            if (command instanceof QuantityType) {
                // Update minimum set point to the command value
                addTemperatureUpdateRequest("target_temperature_low_c", "target_temperature_low_f",
                        (QuantityType<Temperature>) command);
            }
        } else if (CHANNEL_MODE.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                // Update the HVAC mode to the command value
                addUpdateRequest("hvac_mode", Mode.valueOf(((StringType) command).toString()));
            }
        } else if (CHANNEL_SET_POINT.equals(channelUID.getId())) {
            if (command instanceof QuantityType) {
                // Update set point to the command value
                addTemperatureUpdateRequest("target_temperature_c", "target_temperature_f",
                        (QuantityType<Temperature>) command);
            }
        }
    }

    private void addUpdateRequest(String field, Object value) {
        addUpdateRequest(NEST_THERMOSTAT_UPDATE_PATH, field, value);
    }

    private void addTemperatureUpdateRequest(String celsiusField, String fahrenheitField,
            QuantityType<Temperature> quantity) {
        Unit<Temperature> unit = getTemperatureUnit(quantity.getUnit());
        BigDecimal value = quantityToRoundedTemperature(quantity, unit);
        if (value != null) {
            addUpdateRequest(NEST_THERMOSTAT_UPDATE_PATH, unit == CELSIUS ? celsiusField : fahrenheitField, value);
        }
    }

    private Unit<Temperature> getTemperatureUnit(Unit<Temperature> fallbackUnit) {
        WWNThermostat lastUpdate = getLastUpdate();
        if (lastUpdate != null && lastUpdate.getTemperatureUnit() != null) {
            return lastUpdate.getTemperatureUnit();
        }

        return fallbackUnit;
    }

    private @Nullable BigDecimal quantityToRoundedTemperature(QuantityType<Temperature> quantity,
            Unit<Temperature> unit) throws IllegalArgumentException {
        QuantityType<Temperature> temparatureQuantity = quantity.toUnit(unit);
        if (temparatureQuantity == null) {
            return null;
        }

        BigDecimal value = temparatureQuantity.toBigDecimal();
        BigDecimal increment = CELSIUS == unit ? new BigDecimal("0.5") : new BigDecimal("1");
        BigDecimal divisor = value.divide(increment, 0, RoundingMode.HALF_UP);
        return divisor.multiply(increment);
    }

    @Override
    protected void update(@Nullable WWNThermostat oldThermostat, WWNThermostat thermostat) {
        logger.debug("Updating {}", getThing().getUID());

        updateLinkedChannels(oldThermostat, thermostat);
        updateProperty(PROPERTY_FIRMWARE_VERSION, thermostat.getSoftwareVersion());

        ThingStatus newStatus = thermostat.isOnline() == null ? ThingStatus.UNKNOWN
                : thermostat.isOnline() ? ThingStatus.ONLINE : ThingStatus.OFFLINE;
        if (newStatus != thing.getStatus()) {
            updateStatus(newStatus);
        }
    }
}
