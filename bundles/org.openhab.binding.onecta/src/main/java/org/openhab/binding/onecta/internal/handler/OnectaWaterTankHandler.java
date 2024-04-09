/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.onecta.internal.OnectaDeviceConstants.*;
import static org.openhab.binding.onecta.internal.OnectaWaterTankConstants.*;

import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.OnectaConfiguration;
import org.openhab.binding.onecta.internal.api.Enums;
import org.openhab.binding.onecta.internal.service.ChannelsRefreshDelay;
import org.openhab.binding.onecta.internal.service.DataTransportService;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
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
public class OnectaWaterTankHandler extends BaseThingHandler {

    public static final String DOES_NOT_EXISTS = "Unit not registered at Onecta, unitID does not exists.";
    private final Logger logger = LoggerFactory.getLogger(OnectaWaterTankHandler.class);

    private @Nullable OnectaConfiguration config;

    private @Nullable ScheduledFuture<?> pollingJob;

    private final DataTransportService dataTransService;
    private @Nullable ChannelsRefreshDelay channelsRefreshDelay;

    public OnectaWaterTankHandler(Thing thing) {
        super(thing);
        dataTransService = new DataTransportService(thing.getConfiguration().get("unitID").toString(),
                Enums.ManagementPoint.WATERTANK);
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
                        dataTransService.setPowerFulModeOnOff(Enums.OnOff.valueOf(command.toString()));
                    }
                    break;
                case CHANNEL_HWT_SETTEMP:
                    if (command instanceof QuantityType) {
                        dataTransService.setCurrentTankTemperatureSet(((QuantityType<?>) command).floatValue());
                    }
                    break;
            }

            updateStatus(ThingStatus.ONLINE);
        } catch (Exception ex) {
            // catch exceptions and handle it in your binding
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(OnectaConfiguration.class);
        channelsRefreshDelay = new ChannelsRefreshDelay(
                Long.parseLong(thing.getConfiguration().get("refreshDelay").toString()) * 1000);

        updateStatus(ThingStatus.ONLINE);
    }

    public void refreshDevice() {
        logger.debug("refreshWatertank :" + dataTransService.getUnitName());
        dataTransService.refreshUnit();

        if (dataTransService.isAvailable()) {

            // getThing().setLabel(String.format("Daikin Onecta Unit (%s)", dataTransService.getUnitName()));
            getThing().setProperty(PROPERTY_HWT_NAME, dataTransService.getUnitName());

            if (channelsRefreshDelay.isDelayPassed(CHANNEL_HWT_POWER))
                updateState(CHANNEL_HWT_POWER, getCurrentOnOff());
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_AC_OPERATIONMODE))
                updateState(CHANNEL_AC_OPERATIONMODE, getCurrentOperationMode());

            updateState(CHANNEL_HWT_ERRORCODE, getErrorState());
            updateState(CHANNEL_HWT_IS_IN_EMERGENCY_STATE, getIsInEmergencyState());
            updateState(CHANNEL_HWT_IS_IN_ERROR_STATE, getIsInErrorState());
            updateState(CHANNEL_HWT_IS_IN_INSTALLER_STATE, getIsInInstallerState());
            updateState(CHANNEL_HWT_IS_IN_WARNING_STATE, getIsInWarningState());

            updateState(CHANNEL_HWT_IS_HOLIDAY_MODE_ACTIVE, getIsHolidayModeActive());
            updateState(CHANNEL_HWT_POWERFUL_MODE, getPowerFulMode());

            updateState(CHANNEL_HWT_HEATUP_MODE, getHeatupMode());
            updateState(CHANNEL_HWT_TANK_TEMPERATURE, getTankTemperatur());
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_HWT_SETTEMP))
                updateState(CHANNEL_HWT_SETTEMP, getCurrentTankTemperatureSet());
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_HWT_SETTEMP_MIN))
                updateState(CHANNEL_HWT_SETTEMP_MIN, getCurrentTankTemperatureSetMin());
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_HWT_SETTEMP_MAX))
                updateState(CHANNEL_HWT_SETTEMP_MAX, getCurrentTankTemperatureSetMax());
            if (channelsRefreshDelay.isDelayPassed(CHANNEL_HWT_SETTEMP_STEP))
                updateState(CHANNEL_HWT_SETTEMP_STEP, getCurrentTankTemperatureSetStep());

            updateState(CHANNEL_HWT_SETPOINT_MODE, getSetpointMode());

        } else {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR, DOES_NOT_EXISTS);
            getThing().setProperty(PROPERTY_HWT_NAME, DOES_NOT_EXISTS);
        }
    }

    private State getCurrentOnOff() {
        try {
            if (dataTransService.getPowerOnOff() != null) {
                return OnOffType.from(dataTransService.getPowerOnOff());
            } else {
                return UnDefType.UNDEF;
            }
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getCurrentOperationMode() {
        try {
            return new StringType(dataTransService.getCurrentOperationMode().toString());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getSetpointMode() {
        try {
            return new StringType(dataTransService.getSetpointMode().toString());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getIsHolidayModeActive() {
        try {
            return OnOffType.from(dataTransService.getIsHolidayModeActive());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getTankTemperatur() {
        try {
            return new DecimalType(dataTransService.getTankTemperature());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getHeatupMode() {
        try {
            return new StringType(this.dataTransService.getHeatupMode().toString());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getIsInErrorState() {
        try {
            return OnOffType.from(this.dataTransService.getIsInErrorState());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getErrorState() {
        try {
            return new StringType(this.dataTransService.getErrorCode());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getIsInEmergencyState() {
        try {
            return OnOffType.from(this.dataTransService.getIsInEmergencyState());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getIsInInstallerState() {
        try {
            return OnOffType.from(this.dataTransService.getIsInInstallerState());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getIsInWarningState() {
        try {
            return OnOffType.from(this.dataTransService.getIsInWarningState());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getPowerFulMode() {
        try {
            return OnOffType.from(this.dataTransService.getPowerFulModeOnOff());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getCurrentTankTemperatureSet() {
        try {
            return new DecimalType(dataTransService.getCurrentTankTemperatureSet());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getCurrentTankTemperatureSetMin() {
        try {
            return new DecimalType(dataTransService.getCurrentTankTemperatureSetMin());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getCurrentTankTemperatureSetMax() {
        try {
            return new DecimalType(dataTransService.getCurrentTankTemperatureSetMax());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getCurrentTankTemperatureSetStep() {
        try {
            return new DecimalType(dataTransService.getCurrentTankTemperatureSetStep());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }
}
