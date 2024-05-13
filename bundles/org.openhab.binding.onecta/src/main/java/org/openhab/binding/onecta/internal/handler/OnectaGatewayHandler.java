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

import static org.openhab.binding.onecta.internal.OnectaGatewayConstants.*;

import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.OnectaConfiguration;
import org.openhab.binding.onecta.internal.api.Enums;
import org.openhab.binding.onecta.internal.service.DataTransportService;
import org.openhab.core.library.types.*;
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
 * The {@link OnectaGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaGatewayHandler extends BaseThingHandler {

    public static final String DOES_NOT_EXISTS = "Unit not registered at Onecta, unitID does not exists.";
    private final Logger logger = LoggerFactory.getLogger(OnectaGatewayHandler.class);

    private @Nullable OnectaConfiguration config;

    private @Nullable ScheduledFuture<?> pollingJob;

    private final DataTransportService dataTransService;

    public OnectaGatewayHandler(Thing thing) {
        super(thing);
        dataTransService = new DataTransportService(thing.getConfiguration().get("unitID").toString(),
                Enums.ManagementPoint.GATEWAY);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(OnectaConfiguration.class);
        updateStatus(ThingStatus.ONLINE);
    }

    public void refreshDevice() {
        logger.debug("refreshGateway :" + dataTransService.getUnitName());
        dataTransService.refreshUnit();

        if (dataTransService.isAvailable()) {

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

        } else {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR, DOES_NOT_EXISTS);
            getThing().setProperty(PROPERTY_GW_NAME, DOES_NOT_EXISTS);
        }
    }

    private State getDaylightSavingTimeEnabled() {
        try {
            return OnOffType.from(this.dataTransService.getDaylightSavingTimeEnabled());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getFirmwareVerion() {
        try {
            return new StringType(this.dataTransService.getFirmwareVerion());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getIsFirmwareUpdateSupported() {
        try {
            return OnOffType.from(this.dataTransService.getIsFirmwareUpdateSupported());
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

    private State getIsLedEnabled() {
        try {
            return OnOffType.from(this.dataTransService.getIsLedEnabled());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getRegionCode() {
        try {
            return new StringType(this.dataTransService.getRegionCode());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getSerialNumber() {
        try {
            return new StringType(this.dataTransService.getSerialNumber());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getSsid() {
        try {
            return new StringType(this.dataTransService.getSsid());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getTimeZone() {
        try {
            return new StringType(this.dataTransService.getTimeZone());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getWifiConnectionSsid() {
        try {
            return new StringType(this.dataTransService.getWifiConectionSSid());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private State getWifiConnectionStrength() {
        try {
            return new DecimalType(this.dataTransService.getWifiConectionStrength());
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }
}
