/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.melcloud.internal.handler;

import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.openhab.binding.melcloud.internal.api.json.HeatpumpDeviceStatus;
import org.openhab.binding.melcloud.internal.config.HeatpumpDeviceConfig;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudLoginException;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MelCloudHeatpumpDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Wietse van Buitenen - Initial contribution
 */
public class MelCloudHeatpumpDeviceHandler extends BaseThingHandler {
    private static final long EFFECTIVE_FLAG_POWER = 1L;
    private static final long EFFECTIVE_FLAG_TEMPERATURE_ZONE1 = 8589934720L;
    private static final long EFFECTIVE_FLAG_HOTWATER = 65536L;

    private final Logger logger = LoggerFactory.getLogger(MelCloudHeatpumpDeviceHandler.class);
    private HeatpumpDeviceConfig config;
    private MelCloudAccountHandler melCloudHandler;
    private HeatpumpDeviceStatus heatpumpDeviceStatus;
    private ScheduledFuture<?> refreshTask;

    public MelCloudHeatpumpDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing {} handler.", getThing().getThingTypeUID());

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge Not set");
            return;
        }

        config = getConfigAs(HeatpumpDeviceConfig.class);
        logger.debug("Heatpump device config: {}", config);

        initializeBridge(bridge.getHandler(), bridge.getStatus());
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        if (refreshTask != null) {
            refreshTask.cancel(true);
            refreshTask = null;
        }
        melCloudHandler = null;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        Bridge bridge = getBridge();
        if (bridge != null) {
            initializeBridge(bridge.getHandler(), bridgeStatusInfo.getStatus());
        }
    }

    private void initializeBridge(ThingHandler thingHandler, ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());

        if (thingHandler != null && bridgeStatus != null) {
            melCloudHandler = (MelCloudAccountHandler) thingHandler;

            if (bridgeStatus == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
                startAutomaticRefresh();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command '{}' to channel {}", command, channelUID);

        if (command instanceof RefreshType) {
            logger.debug("Refresh command not supported");
            return;
        }

        if (melCloudHandler == null) {
            logger.warn("No connection to MELCloud available, ignoring command");
            return;
        }

        if (heatpumpDeviceStatus == null) {
            logger.info("No initial data available, bridge is probably offline. Ignoring command");
            return;
        }

        HeatpumpDeviceStatus cmdtoSend = getHeatpumpDeviceStatusCopy(heatpumpDeviceStatus);
        cmdtoSend.setEffectiveFlags(0L);

        switch (channelUID.getId()) {
            case CHANNEL_POWER:
                cmdtoSend.setPower(command == OnOffType.ON);
                cmdtoSend.setEffectiveFlags(EFFECTIVE_FLAG_POWER);
                break;
            case CHANNEL_SET_TEMPERATURE_ZONE1:
                BigDecimal val = null;
                if (command instanceof QuantityType) {
                    QuantityType<Temperature> quantity = new QuantityType<Temperature>(command.toString())
                            .toUnit(CELSIUS);
                    if (quantity != null) {
                        val = quantity.toBigDecimal().setScale(1, RoundingMode.HALF_UP);
                        // round nearest .5
                        double v = Math.round(val.doubleValue() * 2) / 2.0;
                        cmdtoSend.setSetTemperatureZone1(v);
                        cmdtoSend.setEffectiveFlags(EFFECTIVE_FLAG_TEMPERATURE_ZONE1);
                    }
                }
                if (val == null) {
                    logger.debug("Can't convert '{}' to set temperature", command);
                }
                break;
            case CHANNEL_FORCED_HOTWATERMODE:
                cmdtoSend.setForcedHotWaterMode(command == OnOffType.ON);
                cmdtoSend.setEffectiveFlags(EFFECTIVE_FLAG_HOTWATER);
                break;
            default:
                logger.debug("Read-only or unknown channel {}, skipping update", channelUID);
        }

        if (cmdtoSend.getEffectiveFlags() > 0) {
            cmdtoSend.setHasPendingCommand(true);
            cmdtoSend.setDeviceID(config.deviceID);
            try {
                HeatpumpDeviceStatus newHeatpumpDeviceStatus = melCloudHandler.sendHeatpumpDeviceStatus(cmdtoSend);
                updateChannels(newHeatpumpDeviceStatus);
            } catch (MelCloudLoginException e) {
                logger.warn("Command '{}' to channel '{}' failed due to login error, reason {}. ", command, channelUID,
                        e.getMessage(), e);
            } catch (MelCloudCommException e) {
                logger.warn("Command '{}' to channel '{}' failed, reason {}. ", command, channelUID, e.getMessage(), e);
            }
        } else {
            logger.debug("Nothing to send");
        }
    }

    private HeatpumpDeviceStatus getHeatpumpDeviceStatusCopy(HeatpumpDeviceStatus heatpumpDeviceStatus) {
        HeatpumpDeviceStatus copy = new HeatpumpDeviceStatus();
        synchronized (this) {
            copy.setDeviceID(heatpumpDeviceStatus.getDeviceID());
            copy.setEffectiveFlags(heatpumpDeviceStatus.getEffectiveFlags());
            copy.setPower(heatpumpDeviceStatus.getPower());
            copy.setSetTemperatureZone1(heatpumpDeviceStatus.getSetTemperatureZone1());
            copy.setForcedHotWaterMode(heatpumpDeviceStatus.getForcedHotWaterMode());
            copy.setHasPendingCommand(heatpumpDeviceStatus.getHasPendingCommand());
        }
        return copy;
    }

    private void startAutomaticRefresh() {
        if (refreshTask == null || refreshTask.isCancelled()) {
            refreshTask = scheduler.scheduleWithFixedDelay(this::getDeviceDataAndUpdateChannels, 1,
                    config.pollingInterval, TimeUnit.SECONDS);
        }
    }

    private void getDeviceDataAndUpdateChannels() {
        if (melCloudHandler.isConnected()) {
            logger.debug("Update device '{}' channels", getThing().getThingTypeUID());
            try {
                HeatpumpDeviceStatus newHeatpumpDeviceStatus = melCloudHandler
                        .fetchHeatpumpDeviceStatus(config.deviceID, Optional.ofNullable(config.buildingID));
                updateChannels(newHeatpumpDeviceStatus);
            } catch (MelCloudLoginException e) {
                logger.debug("Login error occurred during device '{}' polling, reason {}. ",
                        getThing().getThingTypeUID(), e.getMessage(), e);
            } catch (MelCloudCommException e) {
                logger.debug("Error occurred during device '{}' polling, reason {}. ", getThing().getThingTypeUID(),
                        e.getMessage(), e);
            }
        } else {
            logger.debug("Connection to MELCloud is not open, skipping periodic update");
        }
    }

    private synchronized void updateChannels(HeatpumpDeviceStatus newHeatpumpDeviceStatus) {
        heatpumpDeviceStatus = newHeatpumpDeviceStatus;
        for (Channel channel : getThing().getChannels()) {
            updateChannels(channel.getUID().getId(), heatpumpDeviceStatus);
        }
    }

    private void updateChannels(String channelId, HeatpumpDeviceStatus heatpumpDeviceStatus) {
        switch (channelId) {
            case CHANNEL_POWER:
                updateState(CHANNEL_POWER, OnOffType.from(heatpumpDeviceStatus.getPower()));
                break;
            case CHANNEL_TANKWATERTEMPERATURE:
                updateState(CHANNEL_TANKWATERTEMPERATURE,
                        new DecimalType(heatpumpDeviceStatus.getTankWaterTemperature()));
                break;
            case CHANNEL_SET_TEMPERATURE_ZONE1:
                updateState(CHANNEL_SET_TEMPERATURE_ZONE1,
                        new QuantityType<>(heatpumpDeviceStatus.getSetTemperatureZone1(), SIUnits.CELSIUS));
                break;
            case CHANNEL_ROOM_TEMPERATURE_ZONE1:
                updateState(CHANNEL_ROOM_TEMPERATURE_ZONE1,
                        new DecimalType(heatpumpDeviceStatus.getRoomTemperatureZone1()));
                break;
            case CHANNEL_FORCED_HOTWATERMODE:
                updateState(CHANNEL_FORCED_HOTWATERMODE, OnOffType.from(heatpumpDeviceStatus.getForcedHotWaterMode()));
                break;
            case CHANNEL_LAST_COMMUNICATION:
                updateState(CHANNEL_LAST_COMMUNICATION,
                        new DateTimeType(convertDateTime(heatpumpDeviceStatus.getLastCommunication())));
                break;
            case CHANNEL_NEXT_COMMUNICATION:
                updateState(CHANNEL_NEXT_COMMUNICATION,
                        new DateTimeType(convertDateTime(heatpumpDeviceStatus.getNextCommunication())));
                break;
            case CHANNEL_HAS_PENDING_COMMAND:
                updateState(CHANNEL_HAS_PENDING_COMMAND, OnOffType.from(heatpumpDeviceStatus.getHasPendingCommand()));
                break;
            case CHANNEL_OFFLINE:
                updateState(CHANNEL_OFFLINE, OnOffType.from(heatpumpDeviceStatus.getOffline()));
                break;
        }
    }

    private ZonedDateTime convertDateTime(String dateTime) {
        return ZonedDateTime.ofInstant(LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                ZoneOffset.UTC, ZoneId.systemDefault());
    }
}
