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

import static org.openhab.binding.onecta.internal.OnectaIndoorUnitConstants.*;

import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.OnectaConfiguration;
import org.openhab.binding.onecta.internal.api.Enums;
import org.openhab.binding.onecta.internal.service.DataTransportService;
import org.openhab.binding.onecta.internal.type.TypeHandler;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OnectaIndoorUnitHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaIndoorUnitHandler extends AbstractOnectaHandler {

    private final Logger logger = LoggerFactory.getLogger(OnectaIndoorUnitHandler.class);

    private @Nullable OnectaConfiguration config;

    private @Nullable ScheduledFuture<?> pollingJob;

    private final DataTransportService dataTransService;

    public OnectaIndoorUnitHandler(Thing thing) {
        super(thing);
        dataTransService = new DataTransportService(getUnitID(), Enums.ManagementPoint.INDOORUNIT);
    }

    @Override
    public void initialize() {
        config = getConfigAs(OnectaConfiguration.class);
        if (dataTransService.isAvailable()) {
            refreshDevice();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void refreshDevice() {
        dataTransService.refreshUnit();

        if (dataTransService.isAvailable()) {
            logger.debug("refreshDevice : {}, {}", dataTransService.getManagementPointType(),
                    dataTransService.getUnitName());

            updateStatus(ThingStatus.ONLINE);

            getThing().setProperty(PROPERTY_IDU_MODELINFO, getModelInfo().toString());
            getThing().setProperty(PROPERTY_IDU_SOFTWAREVERSION, getSoftwareVerion().toString());
            getThing().setProperty(PROPERTY_IDU_EEPROMVERSION, getEepromVerion().toString());

            updateState(CHANNEL_IDU_ISKEEPDRY, getDryKeepSetting());
            updateState(CHANNEL_IDU_FANSPEED, getFanMotorRotationSpeed());
            updateState(CHANNEL_IDU_DELTAD, getDeltaD());
            updateState(CHANNEL_IDU_HEATEXCHANGETEMP, getHeatExchangerTemperature());
            updateState(CHANNEL_IDU_SUCTIONTEMP, getSuctionTemperature());
        } else {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                    OnectaConfiguration.getTranslation().getText("unknown.unitid-not-exists"));
        }
    }

    private State getModelInfo() {
        return TypeHandler.stringType(this.dataTransService.getModelInfo());
    }

    private State getSoftwareVerion() {
        return TypeHandler.stringType(dataTransService.getSoftwareVersion());
    }

    private State getEepromVerion() {
        return TypeHandler.stringType(dataTransService.getEepromVerion());
    }

    private State getDryKeepSetting() {
        return TypeHandler.onOffType(dataTransService.getDryKeepSetting());
    }

    private State getFanMotorRotationSpeed() {
        return TypeHandler.decimalType(dataTransService.getFanMotorRotationSpeed());
    }

    private State getDeltaD() {
        return TypeHandler.decimalType(dataTransService.getDeltaD());
    }

    private State getHeatExchangerTemperature() {
        return TypeHandler.decimalType(dataTransService.getHeatExchangerTemperature());
    }

    private State getSuctionTemperature() {
        return TypeHandler.decimalType(dataTransService.getSuctionTemperature());
    }
}
