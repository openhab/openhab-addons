/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.onecta.internal.handler;

import static org.openhab.binding.onecta.internal.constants.OnectaClimateControlConstants.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.OnectaConfiguration;
import org.openhab.binding.onecta.internal.api.Enums;
import org.openhab.binding.onecta.internal.service.ChannelsRefreshDelay;
import org.openhab.binding.onecta.internal.service.DataTransportService;
import org.openhab.binding.onecta.internal.type.TypeHandler;
import org.openhab.core.library.types.*;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OnectaDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaDeviceHandler extends AbstractOnectaHandler {
    public static final String PROPERTY_AC_NAME = "name";
    private final Logger logger = LoggerFactory.getLogger(OnectaDeviceHandler.class);

    private @Nullable OnectaConfiguration config;

    private @Nullable ScheduledFuture<?> pollingJob;

    private final DataTransportService dataTransService;
    private @Nullable ChannelsRefreshDelay channelsRefreshDelay;

    public OnectaDeviceHandler(Thing thing) {
        super(thing);
        this.dataTransService = new DataTransportService(getUnitID(), Enums.ManagementPoint.CLIMATECONTROL);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        try {
            channelsRefreshDelay.add(channelUID.getId());
            switch (channelUID.getId()) {
                case CHANNEL_AC_POWER:
                    if (command instanceof OnOffType) {
                        dataTransService.setPowerOnOff(Enums.OnOff.valueOf(command.toString()));
                    }
                    break;
                case CHANNEL_AC_POWERFULMODE:
                    if (command instanceof OnOffType) {
                        dataTransService.setPowerfulModeOnOff(Enums.OnOff.valueOf(command.toString()));
                    }
                    break;
                case CHANNEL_AC_OPERATIONMODE:
                    if (command instanceof StringType) {
                        dataTransService.setCurrentOperationMode(Enums.OperationMode.valueOf(command.toString()));
                    }
                    break;
                case CHANNEL_AC_TEMP:
                    if (command instanceof QuantityType) {
                        dataTransService.setCurrentTemperatureSet(((QuantityType<?>) command).floatValue());
                    }
                    break;
                case CHANNEL_AC_FANSPEED:
                    if (command instanceof StringType) {
                        dataTransService.setFanSpeed(Enums.FanSpeed.valueOf(command.toString()));
                    }
                    break;
                case CHANNEL_AC_FANMOVEMENT:
                    if (command instanceof StringType) {
                        dataTransService.setCurrentFanDirection(Enums.FanMovement.valueOf(command.toString()));
                    }
                    break;
                case CHANNEL_AC_FANMOVEMENT_HOR:
                    if (command instanceof StringType) {
                        dataTransService.setCurrentFanDirectionHor(Enums.FanMovementHor.valueOf(command.toString()));
                    }
                    break;
                case CHANNEL_AC_FANMOVEMENT_VER:
                    if (command instanceof StringType) {
                        dataTransService.setCurrentFanDirectionVer(Enums.FanMovementVer.valueOf(command.toString()));
                    }
                    break;
                case CHANNEL_AC_ECONOMODE:
                    if (command instanceof OnOffType) {
                        dataTransService.setEconoMode(Enums.OnOff.valueOf(command.toString()));
                    }
                    break;
                case CHANNEL_AC_STREAMER:
                    if (command instanceof OnOffType) {
                        dataTransService.setStreamerMode(Enums.OnOff.valueOf(command.toString()));
                    }
                    break;
                case CHANNEL_AC_HOLIDAYMODE:
                    if (command instanceof OnOffType) {
                        dataTransService.setHolidayMode(Enums.OnOff.valueOf(command.toString()));
                    }
                    break;
                case CHANNEL_AC_SETPOINT_LEAVINGWATER_OFFSET:
                    if (command instanceof QuantityType) {
                        dataTransService.setSetpointLeavingWaterOffset(((QuantityType<?>) command).intValue());
                    }
                    break;
                case CHANNEL_AC_SETPOINT_LEAVINGWATER_TEMP:
                    if (command instanceof QuantityType) {
                        dataTransService.setSetpointLeavingWaterTemperature(((QuantityType<?>) command).intValue());
                    }
                    break;
            }

            updateStatus(ThingStatus.ONLINE);
        } catch (RuntimeException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(OnectaConfiguration.class);
        channelsRefreshDelay = new ChannelsRefreshDelay(
                Long.parseLong(thing.getConfiguration().get("refreshDelay").toString()) * 1000);
        if (dataTransService.isAvailable()) {
            refreshDevice();
        }
        thing.setProperty(CHANNEL_AC_NAME, "");
    }

    @Override
    public void refreshDevice() {
        dataTransService.refreshUnit();

        if (dataTransService.isAvailable()) {
            logger.debug("refreshDevice : {}, {}", dataTransService.getManagementPointType(),
                    dataTransService.getUnitName());

            updateStatus(ThingStatus.ONLINE);
            getThing().setProperty(PROPERTY_AC_NAME, dataTransService.getUnitName());

            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_POWER)) {
                updateState(CHANNEL_AC_POWER, getPowerOnOff());
            }
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_POWERFULMODE)) {
                updateState(CHANNEL_AC_POWERFULMODE, getPowerfulMode());
            }
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_OPERATIONMODE)) {
                updateState(CHANNEL_AC_OPERATIONMODE, getCurrentOperationMode());
            }

            // Set Temp
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_TEMP)) {
                updateState(CHANNEL_AC_TEMP, getCurrentTemperatureSet());
            }
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_TEMPMIN)) {
                updateState(CHANNEL_AC_TEMPMIN, getCurrentTemperatureSetMin());
            }
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_TEMPMAX)) {
                updateState(CHANNEL_AC_TEMPMAX, getCurrentTemperatureSetMax());
            }
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_TEMPSTEP)) {
                updateState(CHANNEL_AC_TEMPSTEP, getCurrentTemperatureSetStep());
            }

            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_SETPOINT_LEAVINGWATER_OFFSET)) {
                updateState(CHANNEL_AC_SETPOINT_LEAVINGWATER_OFFSET, getSetpointLeavingWaterOffset());
            }
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_SETPOINT_LEAVINGWATER_TEMP)) {
                updateState(CHANNEL_AC_SETPOINT_LEAVINGWATER_TEMP, getSetpointLeavingWaterTemperature());
            }

            // Fan
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_FANMOVEMENT)) {
                updateState(CHANNEL_AC_FANMOVEMENT, getCurrentFanDirection());
            }
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_FANSPEED)) {
                updateState(CHANNEL_AC_FANSPEED, getCurrentFanspeed());
            }
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_ECONOMODE)) {
                updateState(CHANNEL_AC_ECONOMODE, getEconoMode());
            }
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_STREAMER)) {
                updateState(CHANNEL_AC_STREAMER, getStreamerMode());
            }

            updateState(CHANNEL_INDOOR_TEMP, getIndoorTemperature());
            updateState(CHANNEL_OUTDOOR_TEMP, getOutdoorTemperature());
            updateState(CHANNEL_LEAVINGWATER_TEMP, getLeavingWaterTemperature());
            updateState(CHANNEL_INDOOR_HUMIDITY, getIndoorHumidity());
            updateState(CHANNEL_AC_TIMESTAMP, getTimeStamp());

            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_FANMOVEMENT_HOR)) {
                updateState(CHANNEL_AC_FANMOVEMENT_HOR, getCurrentFanDirectionHor());
            }
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_FANMOVEMENT_VER)) {
                updateState(CHANNEL_AC_FANMOVEMENT_VER, getCurrentFanDirectionVer());
            }

            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_HOLIDAYMODE)) {
                updateState(CHANNEL_AC_HOLIDAYMODE, getHolidayMode());
            }
            // Energy consumption Cooling Day
            if (dataTransService.getConsumptionCoolingDay() != null) {
                for (int i = 0; i < dataTransService.getConsumptionCoolingDay().length; i++) {
                    updateState(String.format(CHANNEL_AC_ENERGY_COOLING_DAY, i),
                            dataTransService.getConsumptionCoolingDay()[i] == null ? UnDefType.UNDEF
                                    : new DecimalType(dataTransService.getConsumptionCoolingDay()[i]));
                }
            }
            // Energy consumption Cooling Week
            if (dataTransService.getConsumptionCoolingWeek() != null) {
                for (int i = 0; i < dataTransService.getConsumptionCoolingWeek().length; i++) {
                    updateState(String.format(CHANNEL_AC_ENERGY_COOLING_WEEK, i),
                            dataTransService.getConsumptionCoolingWeek()[i] == null ? UnDefType.UNDEF
                                    : new DecimalType(dataTransService.getConsumptionCoolingWeek()[i]));
                }
            }
            // Energy consumption Cooling Month
            if (dataTransService.getConsumptionCoolingMonth() != null) {
                for (int i = 0; i < dataTransService.getConsumptionCoolingMonth().length; i++) {
                    updateState(String.format(CHANNEL_AC_ENERGY_COOLING_MONTH, i),
                            dataTransService.getConsumptionCoolingMonth()[i] == null ? UnDefType.UNDEF
                                    : new DecimalType(dataTransService.getConsumptionCoolingMonth()[i]));
                }
            }

            // Energy consumption Heating Day
            if (dataTransService.getConsumptionHeatingDay() != null) {
                for (int i = 0; i < dataTransService.getConsumptionHeatingDay().length; i++) {
                    updateState(String.format(CHANNEL_AC_ENERGY_HEATING_DAY, i),
                            dataTransService.getConsumptionHeatingDay()[i] == null ? UnDefType.UNDEF
                                    : new DecimalType(dataTransService.getConsumptionHeatingDay()[i]));
                }
            }
            // Energy consumption Heating Week
            if (dataTransService.getConsumptionHeatingWeek() != null) {
                for (int i = 0; i < dataTransService.getConsumptionHeatingWeek().length; i++) {
                    updateState(String.format(CHANNEL_AC_ENERGY_HEATING_WEEK, i),
                            dataTransService.getConsumptionHeatingWeek()[i] == null ? UnDefType.UNDEF
                                    : new DecimalType(dataTransService.getConsumptionHeatingWeek()[i]));
                }
            }
            // Energy consumption Heating Month
            if (dataTransService.getConsumptionHeatingMonth() != null) {
                for (int i = 0; i < dataTransService.getConsumptionHeatingMonth().length; i++) {
                    updateState(String.format(CHANNEL_AC_ENERGY_HEATING_MONTH, i),
                            dataTransService.getConsumptionHeatingMonth()[i] == null ? UnDefType.UNDEF
                                    : new DecimalType(dataTransService.getConsumptionHeatingMonth()[i]));
                }
            }
            // calculate current day and year energy consumption
            updateState(CHANNEL_AC_ENERGY_HEATING_CURRENT_DAY, getEnergyHeatingCurrentDay());
            updateState(CHANNEL_AC_ENERGY_HEATING_CURRENT_YEAR, getEnergyHeatingCurrentYear());
            updateState(CHANNEL_AC_ENERGY_COOLING_CURRENT_DAY, getEnergyCoolingCurrentDay());
            updateState(CHANNEL_AC_ENERGY_COOLING_CURRENT_YEAR, getEnergyCoolingCurrentYear());

        } else {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                    OnectaConfiguration.getTranslation().getText("unknown.unitid-not-exists"));
            getThing().setProperty(PROPERTY_AC_NAME,
                    OnectaConfiguration.getTranslation().getText("unknown.unitid-not-exists"));
        }
    }

    private State getRawData() {
        return TypeHandler.stringType(dataTransService.getRawData());
    }

    private State getPowerOnOff() {
        return TypeHandler.onOffType(dataTransService.getPowerOnOff());
    }

    private State getPowerfulMode() {
        return TypeHandler.onOffType(dataTransService.getPowerfulModeOnOff());
    }

    private State getCurrentOperationMode() {
        return TypeHandler.stringType(dataTransService.getCurrentOperationMode());
    }

    private State getCurrentFanspeed() {
        return TypeHandler.stringType(dataTransService.getCurrentFanspeed());
    }

    private State getCurrentTemperatureSet() {
        return TypeHandler.decimalType(dataTransService.getCurrentTemperatureSet());
    }

    private State getSetpointLeavingWaterTemperature() {
        return TypeHandler.decimalType(dataTransService.getSetpointLeavingWaterTemperature());
    }

    private State getSetpointLeavingWaterOffset() {
        return TypeHandler.decimalType(dataTransService.getSetpointLeavingWaterOffset());
    }

    private State getCurrentTemperatureSetMin() {
        return TypeHandler.decimalType(dataTransService.getCurrentTemperatureSetMin());
    }

    private State getCurrentTemperatureSetMax() {
        return TypeHandler.decimalType(dataTransService.getCurrentTemperatureSetMax());
    }

    private State getCurrentTemperatureSetStep() {
        return TypeHandler.decimalType(dataTransService.getCurrentTemperatureSetStep());
    }

    private State getOutdoorTemperature() {
        return TypeHandler.decimalType(dataTransService.getOutdoorTemperature());
    }

    private State getIndoorTemperature() {
        return TypeHandler.decimalType(dataTransService.getIndoorTemperature());
    }

    private State getLeavingWaterTemperature() {
        return TypeHandler.decimalType(dataTransService.getLeavingWaterTemperature());
    }

    private State getIndoorHumidity() {
        return TypeHandler.decimalType(dataTransService.getIndoorHumidity());
    }

    private State getTimeStamp() {
        return TypeHandler.dateTimeType(dataTransService.getTimeStamp());
    }

    private State getEconoMode() {
        return TypeHandler.onOffType(dataTransService.getEconoMode());
    }

    private State getStreamerMode() {
        return TypeHandler.onOffType(dataTransService.getStreamerMode());
    }

    private State getCurrentFanDirectionHor() {
        return TypeHandler.stringType(dataTransService.getCurrentFanDirectionHor());
    }

    private State getCurrentFanDirectionVer() {
        return TypeHandler.stringType(dataTransService.getCurrentFanDirectionVer());
    }

    private State getCurrentFanDirection() {
        return TypeHandler.stringType(dataTransService.getCurrentFanDirection());
    }

    private State getHolidayMode() {
        return TypeHandler.onOffType(dataTransService.getHolidayMode());
    }

    private int getCurrentDayOfWeek() {
        LocalDate today = LocalDate.now();
        return today.getDayOfWeek().getValue() - 1;
    }

    private State getEnergyHeatingCurrentDay() {
        State state = Optional.ofNullable(dataTransService.getConsumptionHeatingWeek())
                .filter(consumptionArray -> consumptionArray.length > 7 + getCurrentDayOfWeek()) //
                .map(consumptionArray -> consumptionArray[7 + getCurrentDayOfWeek()]) //
                .map(TypeHandler::decimalType) //
                .orElse(UnDefType.UNDEF); //
        if (state == null) {
            state = UnDefType.UNDEF;
        }
        return state;
    }

    private State getEnergyCoolingCurrentDay() {
        State state = Optional.ofNullable(dataTransService.getConsumptionCoolingWeek())
                .filter(consumptionArray -> consumptionArray.length > 7 + getCurrentDayOfWeek()) //
                .map(consumptionArray -> consumptionArray[7 + getCurrentDayOfWeek()]) //
                .map(TypeHandler::decimalType) //
                .orElse(UnDefType.UNDEF); //
        if (state == null) {
            state = UnDefType.UNDEF;
        }
        return state;
    }

    private Boolean isFirst2HourOfYear() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return localDateTime.getDayOfYear() == 1 && (localDateTime.getHour() == 0 || localDateTime.getHour() == 1);
    }

    private State getEnergyCoolingCurrentYear() {
        double total = 0;
        try {
            if (!isFirst2HourOfYear()) {
                for (int i = 12; i <= 23; i++) {
                    if (dataTransService.getConsumptionCoolingMonth()[i] != null) {
                        total += dataTransService.getConsumptionCoolingMonth()[i];
                    }
                }
            }
            return new DecimalType(Math.round(total * 10) / 10D);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            return UnDefType.UNDEF;
        }
    }

    private State getEnergyHeatingCurrentYear() {
        double total = 0;
        try {
            if (!isFirst2HourOfYear()) {
                for (int i = 12; i <= 23; i++) {
                    if (dataTransService.getConsumptionHeatingMonth()[i] != null) {
                        total += dataTransService.getConsumptionHeatingMonth()[i];
                    }
                }
            }
            return TypeHandler.decimalType(Math.round(total * 10) / 10D);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            return UnDefType.UNDEF;
        }
    }
}
