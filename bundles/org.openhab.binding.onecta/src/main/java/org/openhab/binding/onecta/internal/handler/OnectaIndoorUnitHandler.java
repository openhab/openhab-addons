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

import static org.openhab.binding.onecta.internal.OnectaIndoorUnitConstants.*;

import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.OnectaConfiguration;
import org.openhab.binding.onecta.internal.api.Enums;
import org.openhab.binding.onecta.internal.service.DataTransportService;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
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
 * The {@link OnectaIndoorUnitHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaIndoorUnitHandler extends BaseThingHandler {

    public static final String DOES_NOT_EXISTS = "Unit not registered at Onecta, unitID does not exists.";
    private final Logger logger = LoggerFactory.getLogger(OnectaIndoorUnitHandler.class);

    private @Nullable OnectaConfiguration config;

    private @Nullable ScheduledFuture<?> pollingJob;

    private final DataTransportService dataTransService;

    public OnectaIndoorUnitHandler(Thing thing) {
        super(thing);
        dataTransService = new DataTransportService(thing.getConfiguration().get("unitID").toString(),
                Enums.ManagementPoint.INDOORUNIT);
    }

    @Override
    public void initialize() {
        config = getConfigAs(OnectaConfiguration.class);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public void refreshDevice() {
        logger.debug("refreshIndoorUnit :" + dataTransService.getUnitName());
        dataTransService.refreshUnit();

        if (dataTransService.isAvailable()) {

            getThing().setProperty(PROPERTY_IDU_NAME, dataTransService.getModelInfo());

            updateState(CHANNEL_IDU_MODELINFO, getModelInfo());
            updateState(CHANNEL_IDU_SOFTWAREVERSION, getSoftwareVerion());
            updateState(CHANNEL_IDU_EEPROMVERSION, getEepromVerion());
            updateState(CHANNEL_IDU_ISKEEPDRY, getDryKeepSetting());
            updateState(CHANNEL_IDU_FANSPEED, getFanMotorRotationSpeed());
            updateState(CHANNEL_IDU_DELTAD, getDeltaD());
            updateState(CHANNEL_IDU_HEATEXCHANGETEMP, getHeatExchangerTemperature());
            updateState(CHANNEL_IDU_SUCTIONTEMP, getSuctionTemperature());

        } else {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR, DOES_NOT_EXISTS);
            getThing().setProperty(PROPERTY_IDU_NAME, DOES_NOT_EXISTS);
        }
    }

    private State getModelInfo() {
        try {
            return new StringType(this.dataTransService.getModelInfo());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getSoftwareVerion() {
        try {
            return new StringType(dataTransService.getSoftwareVersion());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getEepromVerion() {
        try {
            return new StringType(dataTransService.getEepromVerion());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getDryKeepSetting() {
        try {
            return OnOffType.from(dataTransService.getDryKeepSetting());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getFanMotorRotationSpeed() {
        try {
            return new DecimalType(dataTransService.getFanMotorRotationSpeed());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getDeltaD() {
        try {
            return new DecimalType(dataTransService.getDeltaD());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getHeatExchangerTemperature() {
        try {
            return new DecimalType(dataTransService.getHeatExchangerTemperature());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getSuctionTemperature() {
        try {
            return new DecimalType(dataTransService.getSuctionTemperature());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }
}
