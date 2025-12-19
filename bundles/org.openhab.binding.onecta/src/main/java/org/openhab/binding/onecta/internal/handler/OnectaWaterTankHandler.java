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
import static org.openhab.binding.onecta.internal.constants.OnectaClimateControlConstants.CHANNEL_AC_ENERGY_HEATING_CURRENT_DAY;
import static org.openhab.binding.onecta.internal.constants.OnectaClimateControlConstants.CHANNEL_AC_ENERGY_HEATING_CURRENT_YEAR;
import static org.openhab.binding.onecta.internal.constants.OnectaWaterTankConstants.*;

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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
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
 * The {@link OnectaWaterTankHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaWaterTankHandler extends AbstractOnectaHandler {

    private final Logger logger = LoggerFactory.getLogger(OnectaWaterTankHandler.class);

    private @Nullable OnectaConfiguration config;

    private @Nullable ScheduledFuture<?> pollingJob;

    private final DataTransportService dataTransService;
    private @Nullable ChannelsRefreshDelay channelsRefreshDelay;

    public OnectaWaterTankHandler(Thing thing) {
        super(thing);
        dataTransService = new DataTransportService(getUnitID(), Enums.ManagementPoint.WATERTANK);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        try {
            channelsRefreshDelay.add(channelUID.getId());
            switch (channelUID.getId()) {
                case CHANNEL_HWT_POWER:
                    if (command instanceof OnOffType) {
                        dataTransService.setPowerOnOff(Enums.OnOff.valueOf(command.toString()));
                    }
                    break;
                case CHANNEL_HWT_POWERFUL_MODE:
                    if (command instanceof OnOffType) {
                        dataTransService.setPowerfulModeOnOff(Enums.OnOff.valueOf(command.toString()));
                    }
                    break;
                case CHANNEL_HWT_SETTEMP:
                    if (command instanceof QuantityType) {
                        dataTransService.setCurrentTankTemperatureSet(((QuantityType<?>) command).floatValue());
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
        thing.setProperty(PROPERTY_HWT_NAME, "");
    }

    @Override
    public void refreshDevice() {
        dataTransService.refreshUnit();

        if (dataTransService.isAvailable()) {
            logger.debug("refreshDevice : {}, {}", dataTransService.getManagementPointType(),
                    dataTransService.getUnitName());

            updateStatus(ThingStatus.ONLINE);
            getThing().setProperty(PROPERTY_HWT_NAME, dataTransService.getUnitName());

            if (channelsRefreshDelay.isDelayPassed(CHANNEL_HWT_POWER)) {
                updateState(CHANNEL_HWT_POWER, getCurrentOnOff());
            }
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_HWT_OPERATION_MODE)) {
                updateState(CHANNEL_HWT_OPERATION_MODE, getCurrentOperationMode());
            }

            updateState(CHANNEL_HWT_ERRORCODE, getErrorState());
            updateState(CHANNEL_HWT_IS_IN_EMERGENCY_STATE, getIsInEmergencyState());
            updateState(CHANNEL_HWT_IS_IN_ERROR_STATE, getIsInErrorState());
            updateState(CHANNEL_HWT_IS_IN_INSTALLER_STATE, getIsInInstallerState());
            updateState(CHANNEL_HWT_IS_IN_WARNING_STATE, getIsInWarningState());

            updateState(CHANNEL_HWT_IS_HOLIDAY_MODE_ACTIVE, getIsHolidayModeActive());
            updateState(CHANNEL_HWT_POWERFUL_MODE, getPowerfulMode());

            updateState(CHANNEL_HWT_HEATUP_MODE, getHeatupMode());
            updateState(CHANNEL_HWT_TANK_TEMPERATURE, getTankTemperatur());
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_HWT_SETTEMP)) {
                updateState(CHANNEL_HWT_SETTEMP, getCurrentTankTemperatureSet());
            }
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_HWT_SETTEMP_MIN)) {
                updateState(CHANNEL_HWT_SETTEMP_MIN, getCurrentTankTemperatureSetMin());
            }
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_HWT_SETTEMP_MAX)) {
                updateState(CHANNEL_HWT_SETTEMP_MAX, getCurrentTankTemperatureSetMax());
            }
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_HWT_SETTEMP_STEP)) {
                updateState(CHANNEL_HWT_SETTEMP_STEP, getCurrentTankTemperatureSetStep());
            }

            updateState(CHANNEL_HWT_SETPOINT_MODE, getSetpointMode());

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
        } else {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                    OnectaConfiguration.getTranslation().getText("unknown.unitid-not-exists"));
            getThing().setProperty(PROPERTY_HWT_NAME,
                    OnectaConfiguration.getTranslation().getText("unknown.unitid-not-exists"));
        }
    }

    private State getCurrentOnOff() {
        return TypeHandler.onOffType(dataTransService.getPowerOnOff());
    }

    private State getCurrentOperationMode() {
        return TypeHandler.stringType(dataTransService.getCurrentOperationMode());
    }

    private State getSetpointMode() {
        return TypeHandler.stringType(dataTransService.getSetpointMode());
    }

    private State getIsHolidayModeActive() {
        return TypeHandler.onOffType(dataTransService.getIsHolidayModeActive());
    }

    private State getTankTemperatur() {
        return TypeHandler.decimalType(dataTransService.getTankTemperature());
    }

    private State getHeatupMode() {
        return TypeHandler.stringType(this.dataTransService.getHeatupMode());
    }

    private State getIsInErrorState() {
        return TypeHandler.onOffType(this.dataTransService.getIsInErrorState());
    }

    private State getErrorState() {
        return TypeHandler.stringType(this.dataTransService.getErrorCode());
    }

    private State getIsInEmergencyState() {
        return TypeHandler.onOffType(this.dataTransService.getIsInEmergencyState());
    }

    private State getIsInInstallerState() {
        return TypeHandler.onOffType(this.dataTransService.getIsInInstallerState());
    }

    private State getIsInWarningState() {
        return TypeHandler.onOffType(this.dataTransService.getIsInWarningState());
    }

    private State getPowerfulMode() {
        return TypeHandler.onOffType(this.dataTransService.getPowerfulModeOnOff());
    }

    private State getCurrentTankTemperatureSet() {
        return TypeHandler.decimalType(dataTransService.getCurrentTankTemperatureSet());
    }

    private State getCurrentTankTemperatureSetMin() {
        return TypeHandler.decimalType(dataTransService.getCurrentTankTemperatureSetMin());
    }

    private State getCurrentTankTemperatureSetMax() {
        return TypeHandler.decimalType(dataTransService.getCurrentTankTemperatureSetMax());
    }

    private State getCurrentTankTemperatureSetStep() {
        return TypeHandler.decimalType(dataTransService.getCurrentTankTemperatureSetStep());
    }

    private Boolean isFirst2HourOfYear() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return localDateTime.getDayOfYear() == 1 && (localDateTime.getHour() == 0 || localDateTime.getHour() == 1);
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

    private int getCurrentDayOfWeek() {
        LocalDate today = LocalDate.now();
        return today.getDayOfWeek().getValue() - 1;
    }
}
