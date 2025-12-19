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

import static org.openhab.binding.onecta.internal.constants.OnectaGatewayConstants.*;

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
 * The {@link OnectaGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaGatewayHandler extends AbstractOnectaHandler {

    private final Logger logger = LoggerFactory.getLogger(OnectaGatewayHandler.class);

    private @Nullable OnectaConfiguration config;

    private @Nullable ScheduledFuture<?> pollingJob;

    private final DataTransportService dataTransService;

    public OnectaGatewayHandler(Thing thing) {
        super(thing);
        dataTransService = new DataTransportService(getUnitID(), Enums.ManagementPoint.GATEWAY);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(OnectaConfiguration.class);
        if (dataTransService.isAvailable()) {
            refreshDevice();
        }
        thing.setProperty(PROPERTY_GW_NAME, "");
    }

    @Override
    public void refreshDevice() {
        dataTransService.refreshUnit();

        if (dataTransService.isAvailable()) {
            logger.debug("refreshDevice : {}, {}", dataTransService.getManagementPointType(),
                    dataTransService.getUnitName());
            updateStatus(ThingStatus.ONLINE);
            getThing().setProperty(PROPERTY_GW_NAME, dataTransService.getUnitName());

            updateState(CHANNEL_GW_DAYLIGHTSAVINGENABLED, getDaylightSavingTimeEnabled());
            updateState(CHANNEL_GW_FIRMWAREVERSION, getFirmwareVerion());
            updateState(CHANNEL_GW_IS_FIRMWAREUPDATE_SUPPORTED, getIsFirmwareUpdateSupported());
            updateState(CHANNEL_GW_IS_IN_ERROR_STATE, getIsInErrorState());
            updateState(CHANNEL_GW_LED_ENABLED, getIsLedEnabled());
            updateState(CHANNEL_GW_REGION_CODE, getRegionCode());
            updateState(CHANNEL_GW_SERIAL_NUMBER, getSerialNumber());
            updateState(CHANNEL_GW_SSID, getSsid());
            updateState(CHANNEL_GW_TIME_ZONE, getTimeZone());
            updateState(CHANNEL_GW_WIFICONNENTION_SSID, getWifiConnectionSsid());
            updateState(CHANNEL_GW_WIFICONNENTION_STRENGTH, getWifiConnectionStrength());
            updateState(CHANNEL_GW_IP_ADDRESS, getIpAddress());
            updateState(CHANNEL_GW_MODEL_INFO, getModelInfo());
            updateState(CHANNEL_GW_MAC_ADDRESS, getMacAddress());

        } else {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                    OnectaConfiguration.getTranslation().getText("unknown.unitid-not-exists"));
            getThing().setProperty(PROPERTY_GW_NAME,
                    OnectaConfiguration.getTranslation().getText("unknown.unitid-not-exists"));
        }
    }

    private State getDaylightSavingTimeEnabled() {
        return TypeHandler.onOffType(this.dataTransService.getDaylightSavingTimeEnabled());
    }

    private State getFirmwareVerion() {
        return TypeHandler.stringType(this.dataTransService.getFirmwareVerion());
    }

    private State getIsFirmwareUpdateSupported() {
        return TypeHandler.onOffType(this.dataTransService.getIsFirmwareUpdateSupported());
    }

    private State getIsInErrorState() {
        return TypeHandler.onOffType(this.dataTransService.getIsInErrorState());
    }

    private State getIsLedEnabled() {
        return TypeHandler.onOffType(this.dataTransService.getIsLedEnabled());
    }

    private State getRegionCode() {
        return TypeHandler.stringType(this.dataTransService.getRegionCode());
    }

    private State getSerialNumber() {
        return TypeHandler.stringType(this.dataTransService.getSerialNumber());
    }

    private State getSsid() {
        return TypeHandler.stringType(this.dataTransService.getSsid());
    }

    private State getTimeZone() {
        return TypeHandler.stringType(this.dataTransService.getTimeZone());
    }

    private State getWifiConnectionSsid() {
        return TypeHandler.stringType(this.dataTransService.getWifiConectionSSid());
    }

    private State getWifiConnectionStrength() {
        return TypeHandler.decimalType(this.dataTransService.getWifiConectionStrength());
    }

    private State getModelInfo() {
        return TypeHandler.stringType(this.dataTransService.getModelInfo());
    }

    private State getIpAddress() {
        return TypeHandler.stringType(this.dataTransService.getIpAddress());
    }

    private State getMacAddress() {
        return TypeHandler.stringType(this.dataTransService.getMacAddress());
    }
}
