/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.evnotify.internal;

import static org.openhab.binding.evnotify.internal.EVNotifyBindingConstants.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evnotify.api.ApiVersion;
import org.openhab.binding.evnotify.api.ChargingData;
import org.openhab.binding.evnotify.api.EVNotifyClient;
import org.openhab.binding.evnotify.api.v2.EVNotifyClientImpl;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
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
 * The {@link EVNotifyHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Schmidt - Initial contribution
 */
@NonNullByDefault
public class EVNotifyHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EVNotifyHandler.class);

    private @Nullable EVNotifyConfiguration config;

    private List<String> allChannels = new ArrayList<>();

    private @Nullable EVNotifyClient client;

    private @Nullable ScheduledFuture<?> refreshJob;

    public EVNotifyHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if (CHANNEL_1.equals(channelUID.getId())) {
        // if (command instanceof RefreshType) {
        // // TODO: handle data refresh
        // }

        // TODO: handle command

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information:
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
        // }
    }

    @Override
    public void initialize() {
        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.
        config = getConfigAs(EVNotifyConfiguration.class);

        // use client for configured version
        ApiVersion apiVersion = ApiVersion.getApiVersion(config.version);
        switch (apiVersion) {
            case V2:
                client = new EVNotifyClientImpl(config.aKey, config.token, HttpClient.newHttpClient());
            case V3:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        String.format("'%s' not implemented yet.", apiVersion.name()));
        }

        allChannels = getThing().getChannels().stream().map(channel -> channel.getUID().getId())
                .collect(Collectors.toList());

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        startAutomaticRefresh();
        logger.debug("Finished initializing!");
    }

    private void updateChannelsAndStatus(@Nullable ChargingData chargingData, @Nullable String message) {
        if (chargingData == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
            allChannels.forEach(channel -> updateState(channel, UnDefType.UNDEF));
        } else {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            allChannels.forEach(channel -> updateState(channel, getValue(channel, chargingData)));
        }
    }

    private State getValue(String channelId, ChargingData chargingData) {
        switch (channelId) {
            case STATE_OF_HEALTH:
                if (chargingData.getStateOfHealth() == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(chargingData.getStateOfHealth(), Units.PERCENT);
            case CHARGING:
                if (chargingData.isCharging() == null) {
                    return UnDefType.UNDEF;
                }
                return chargingData.isCharging() ? OnOffType.ON : OnOffType.OFF;
            case RAPID_CHARING_PORT:
                if (chargingData.isRapidChargePort() == null) {
                    return UnDefType.UNDEF;
                }
                return chargingData.isRapidChargePort() ? OnOffType.ON : OnOffType.OFF;
            case NORMAL_CHARING_PORT:
                if (chargingData.isNormalChargePort() == null) {
                    return UnDefType.UNDEF;
                }
                return chargingData.isNormalChargePort() ? OnOffType.ON : OnOffType.OFF;
            case SLOW_CHARING_PORT:
                if (chargingData.isSlowChargePort() == null) {
                    return UnDefType.UNDEF;
                }
                return chargingData.isSlowChargePort() ? OnOffType.ON : OnOffType.OFF;
            case AUX_BATTERY_VOLTAGE:
                if (chargingData.getAuxBatteryVoltage() == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(chargingData.getAuxBatteryVoltage(), Units.VOLT);
            case DC_BATTERY_VOLTAGE:
                if (chargingData.getDcBatteryVoltage() == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(chargingData.getDcBatteryVoltage(), Units.VOLT);
            case DC_BATTERY_CURRENT:
                if (chargingData.getDcBatteryCurrent() == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(chargingData.getDcBatteryCurrent(), Units.AMPERE);
            case DC_BATTERY_POWER:
                if (chargingData.getDcBatteryPower() == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(chargingData.getDcBatteryPower(), Units.WATT);
            case CUMULATIVE_ENERGY_CHARGED:
                if (chargingData.getCumulativeEnergyCharged() == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(chargingData.getCumulativeEnergyCharged(), Units.KILOWATT_HOUR);
            case CUMULATIVE_ENERGY_DISCHARGED:
                if (chargingData.getCumulativeEnergyDischarged() == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(chargingData.getCumulativeEnergyDischarged(), Units.KILOWATT_HOUR);
            case BATTERY_MAX_TEMPERATURE:
                if (chargingData.getBatteryMaxTemperature() == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(chargingData.getBatteryMaxTemperature(), SIUnits.CELSIUS);
            case BATTERY_MIN_TEMPERATURE:
                if (chargingData.getBatteryMinTemperature() == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(chargingData.getBatteryMinTemperature(), SIUnits.CELSIUS);
            case BATTERY_INLET_TEMPERATURE:
                if (chargingData.getBatteryInletTemperature() == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(chargingData.getBatteryInletTemperature(), SIUnits.CELSIUS);
            case EXTERNAL_TEMPERATURE:
                if (chargingData.getExternalTemperature() == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(chargingData.getExternalTemperature(), SIUnits.CELSIUS);
            case LAST_EXTENDED:
                if (chargingData.getLastExtended() == null) {
                    return UnDefType.UNDEF;
                }
                return new StringType(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(chargingData.getLastExtended()));
        }
        return UnDefType.UNDEF;
    }

    private void refresh() {
        // Request new EVNotify data
        try {
            if (client != null) {
                ChargingData chargingData = client.getCarChargingData();
                updateChannelsAndStatus(chargingData, null);
            }

        } catch (InterruptedException | IOException e) {
            updateChannelsAndStatus(null, e.getMessage());
        }
    }

    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            if (config != null) {
                int delay = config.refreshInterval;
                logger.debug("Running refresh job with delay {} s", delay);
                refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, delay, TimeUnit.SECONDS);
            }

        }
    }
}
