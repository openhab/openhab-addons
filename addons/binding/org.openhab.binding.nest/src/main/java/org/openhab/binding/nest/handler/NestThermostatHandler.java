/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import static org.eclipse.smarthome.core.library.unit.SIUnits.CELSIUS;
import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;
import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.openhab.binding.nest.NestBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.UnitProvider;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.nest.internal.data.Thermostat;
import org.openhab.binding.nest.internal.data.Thermostat.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NestThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels for the thermostat.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Handle channel refresh command
 */
@NonNullByDefault
public class NestThermostatHandler extends NestBaseHandler<Thermostat> {
    private final Logger logger = LoggerFactory.getLogger(NestThermostatHandler.class);

    private final UnitProvider unitProvider;

    public NestThermostatHandler(Thing thing, UnitProvider unitProvider) {
        super(thing, Thermostat.class);
        this.unitProvider = unitProvider;
    }

    @Override
    protected State getChannelState(ChannelUID channelUID, Thermostat thermostat) {
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
                return getAsQuantityTypeOrNull(thermostat.getFanTimerDuration(), SmartHomeUnits.MINUTE);
            case CHANNEL_FAN_TIMER_TIMEOUT:
                return getAsDateTimeTypeOrNull(thermostat.getFanTimerTimeout());
            case CHANNEL_HAS_FAN:
                return getAsOnOffTypeOrNull(thermostat.isHasFan());
            case CHANNEL_HAS_LEAF:
                return getAsOnOffTypeOrNull(thermostat.isHasLeaf());
            case CHANNEL_HUMIDITY:
                return getAsQuantityTypeOrNull(thermostat.getHumidity(), SmartHomeUnits.PERCENT);
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
                return getAsQuantityTypeOrNull(thermostat.getTimeToTarget(), SmartHomeUnits.MINUTE);
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
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (REFRESH.equals(command)) {
            Thermostat lastUpdate = getLastUpdate();
            if (lastUpdate != null) {
                updateState(channelUID, getChannelState(channelUID, lastUpdate));
            }
        } else if (CHANNEL_FAN_TIMER_ACTIVE.equals(channelUID.getId())) {
            if (command instanceof OnOffType) {
                // Update fan timer active to the command value
                addUpdateRequest("fan_timer_active", command == OnOffType.ON);
            }
        } else if (CHANNEL_FAN_TIMER_DURATION.equals(channelUID.getId())) {
            // Update fan timer duration to the command value
            QuantityType<Time> quantity = commandToQuantityType(command, SmartHomeUnits.MINUTE);
            QuantityType<Time> minuteQuantity = quantity.toUnit(SmartHomeUnits.MINUTE);
            if (minuteQuantity != null) {
                addUpdateRequest("fan_timer_duration", minuteQuantity.intValue());
            }
        } else if (CHANNEL_MAX_SET_POINT.equals(channelUID.getId())) {
            // Update maximum set point to the command value
            addTemperatureUpdateRequest("target_temperature_high_c", "target_temperature_high_f", command);
        } else if (CHANNEL_MIN_SET_POINT.equals(channelUID.getId())) {
            // Update minimum set point to the command value
            addTemperatureUpdateRequest("target_temperature_low_c", "target_temperature_low_f", command);
        } else if (CHANNEL_MODE.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                // Update the HVAC mode to the command value
                addUpdateRequest("hvac_mode", Mode.valueOf(((StringType) command).toString()));
            }
        } else if (CHANNEL_SET_POINT.equals(channelUID.getId())) {
            // Update set point to the command value
            addTemperatureUpdateRequest("target_temperature_c", "target_temperature_f", command);
        }
    }

    private void addUpdateRequest(String field, Object value) {
        addUpdateRequest(NEST_THERMOSTAT_UPDATE_PATH, field, value);
    }

    private void addTemperatureUpdateRequest(String celsiusField, String fahrenheitField, Command command) {
        Unit<Temperature> unit = getTemperatureUnit();
        QuantityType<Temperature> quantity = commandToQuantityType(command, unit);
        BigDecimal value = quantityToRoundedTemperature(quantity, unit);
        if (value != null) {
            addUpdateRequest(NEST_THERMOSTAT_UPDATE_PATH, unit == CELSIUS ? celsiusField : fahrenheitField, value);
        }
    }

    private Unit<Temperature> getTemperatureUnit() {
        Thermostat lastUpdate = getLastUpdate();
        if (lastUpdate != null && lastUpdate.getTemperatureUnit() != null) {
            return lastUpdate.getTemperatureUnit();
        }

        Unit<Temperature> systemTemperatureUnit = unitProvider.getUnit(Temperature.class);
        if (systemTemperatureUnit != null) {
            return systemTemperatureUnit;
        }

        return CELSIUS;
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
    protected void update(Thermostat oldThermostat, Thermostat thermostat) {
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
