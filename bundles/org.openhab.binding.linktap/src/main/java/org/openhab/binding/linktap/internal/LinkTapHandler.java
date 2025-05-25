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
package org.openhab.binding.linktap.internal;

import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.*;
import static org.openhab.binding.linktap.protocol.frames.DismissAlertReq.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linktap.protocol.frames.AlertStateReq;
import org.openhab.binding.linktap.protocol.frames.DeviceCmdReq;
import org.openhab.binding.linktap.protocol.frames.DismissAlertReq;
import org.openhab.binding.linktap.protocol.frames.EndpointDeviceResponse;
import org.openhab.binding.linktap.protocol.frames.LockReq;
import org.openhab.binding.linktap.protocol.frames.PauseWateringPlanReq;
import org.openhab.binding.linktap.protocol.frames.StartWateringReq;
import org.openhab.binding.linktap.protocol.frames.WaterMeterStatus;
import org.openhab.binding.linktap.protocol.http.InvalidParameterException;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LinkTapHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class LinkTapHandler extends PollingDeviceHandler {

    private static final String DEFAULT_INST_WATERING_VOL_LIMIT = "0";
    private static final String DEFAULT_INST_WATERING_TIME_LIMIT = "15";
    private static final List<String> READBACK_DISABLED_CHANNELS = List.of(DEVICE_CHANNEL_OH_VOLUME_LIMIT,
            DEVICE_CHANNEL_OH_DURATION_LIMIT);

    private final Logger logger = LoggerFactory.getLogger(LinkTapHandler.class);
    private final Storage<String> strStore;
    private final Object pausePlanLock = new Object();

    private volatile boolean pausePlanActive = false;

    private @Nullable ScheduledFuture<?> pausePlanFuture = null;

    public LinkTapHandler(Thing thing, Storage<String> strStore, TranslationProvider translationProvider,
            LocaleProvider localeProvider) {
        super(thing, translationProvider, localeProvider);
        this.strStore = strStore;
    }

    /**
     * Abstract method implementations from PollingDeviceHandler
     * required for the lifecycle of LinkTap devices.
     */

    @Override
    protected void runStartInit() {
        try {
            if (config.enableAlerts) {
                sendRequest(new AlertStateReq(0, true));
            }

            final String[] chansToRefresh = new String[] { DEVICE_CHANNEL_PAUSE_PLAN_OVERRIDE,
                    DEVICE_CHANNEL_PAUSE_PLAN_EXPIRES, DEVICE_CHANNEL_OH_DURATION_LIMIT,
                    DEVICE_CHANNEL_OH_VOLUME_LIMIT };
            for (String chanId : chansToRefresh) {
                final Channel pausePlanChan = getThing().getChannel(chanId);
                if (pausePlanChan != null) {
                    handleCommand(pausePlanChan.getUID(), RefreshType.REFRESH);
                }
            }

            final String pausePlanState = strStore.get(DEVICE_CHANNEL_PAUSE_PLAN_OVERRIDE);
            if (OnOffType.ON.toString().equals(pausePlanState)) {
                scheduleRenewPlanPause();
            }

        } catch (final InvalidParameterException ipe) {
            logger.warn("{}", getLocalizedText("bug-report.failed-alert-enable", getThing().getLabel()));
        }
    }

    @Override
    protected void registerDevice() {
        LinkTapBridgeHandler.DEV_ID_LOOKUP.registerItem(registeredDeviceId, this, () -> {
        });
    }

    @Override
    protected void deregisterDevice() {
        LinkTapBridgeHandler.DEV_ID_LOOKUP.deregisterItem(registeredDeviceId, this, () -> {
        });
    }

    @Override
    protected String getPollResponseData() {
        try {
            return sendRequest(new DeviceCmdReq(CMD_UPDATE_WATER_TIMER_STATUS));
        } catch (final InvalidParameterException ipe) {
            logger.warn("{}", getLocalizedText("bug-report.poll-failure", getThing().getLabel()));
            return "";
        }
    }

    @Override
    protected void processPollResponseData(String data) {
        processCommand3(data);
    }

    /**
     * OpenHab handlers
     */

    @Override
    public void dispose() {
        cancelPlanPauseRenew();
        super.dispose();
    }

    private void scheduleRenewPlanPause() {
        synchronized (pausePlanLock) {
            cancelPlanPauseRenew();
            pausePlanFuture = scheduler.scheduleWithFixedDelay(this::requestPlanPause, 0, 55, TimeUnit.MINUTES);
            pausePlanActive = true;
        }
    }

    private boolean isPlanPauseActive() {
        return pausePlanActive;
    }

    private void cancelPlanPauseRenew() {
        synchronized (pausePlanLock) {
            ScheduledFuture<?> ref = pausePlanFuture;
            if (ref != null) {
                ref.cancel(false);
                pausePlanFuture = null;
            }
            pausePlanActive = false;
        }
    }

    private void requestPlanPause() {
        try {
            final String respRaw = sendRequest(new PauseWateringPlanReq(1.0));
            final EndpointDeviceResponse devResp = GSON.fromJson(respRaw, EndpointDeviceResponse.class);
            if (devResp != null && devResp.isSuccess()) {
                final DateTimeType expiryTime = new DateTimeType(LocalDateTime.now().plusHours(1).toString());
                strStore.put(DEVICE_CHANNEL_PAUSE_PLAN_EXPIRES, expiryTime.format(null));
                updateState(DEVICE_CHANNEL_PAUSE_PLAN_EXPIRES, expiryTime);
            }
        } catch (final InvalidParameterException ignored) {
            logger.warn("{}", getLocalizedText("bug-report.pause-plan-failure", getThing().getLabel()));
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        scheduler.submit(() -> {
            try {
                if (command instanceof RefreshType) {
                    switch (channelUID.getId()) {
                        case DEVICE_CHANNEL_PAUSE_PLAN_EXPIRES: {
                            final String savedVal = strStore.get(DEVICE_CHANNEL_PAUSE_PLAN_EXPIRES);
                            if (savedVal != null) {
                                updateState(DEVICE_CHANNEL_PAUSE_PLAN_EXPIRES, new DateTimeType(savedVal));
                            }
                        }
                            break;
                        case DEVICE_CHANNEL_OH_DURATION_LIMIT: {
                            final String savedVal = strStore.get(DEVICE_CHANNEL_OH_DURATION_LIMIT);
                            if (savedVal != null) {
                                updateState(DEVICE_CHANNEL_OH_DURATION_LIMIT,
                                        new QuantityType<>(Integer.valueOf(savedVal), Units.SECOND));
                            } else {
                                updateState(DEVICE_CHANNEL_OH_DURATION_LIMIT, new QuantityType<>(15, Units.SECOND));
                            }
                        }
                            break;
                        case DEVICE_CHANNEL_OH_VOLUME_LIMIT: {
                            final String savedVal = strStore.get(DEVICE_CHANNEL_OH_VOLUME_LIMIT);
                            if (savedVal != null) {
                                updateState(DEVICE_CHANNEL_OH_VOLUME_LIMIT,
                                        new QuantityType<>(Integer.valueOf(savedVal), Units.LITRE));
                            } else {
                                updateState(DEVICE_CHANNEL_OH_VOLUME_LIMIT, new QuantityType<>(10, Units.LITRE));
                            }
                        }
                            break;
                        case DEVICE_CHANNEL_PAUSE_PLAN_OVERRIDE:
                            final String savedVal = strStore.get(DEVICE_CHANNEL_PAUSE_PLAN_OVERRIDE);
                            updateState(DEVICE_CHANNEL_OH_VOLUME_LIMIT,
                                    OnOffType.ON.toString().equals(savedVal) ? OnOffType.ON : OnOffType.OFF);
                            break;
                        default:
                            pollForUpdate(false);
                    }
                } else if (command instanceof QuantityType quantityCommand) {
                    int targetValue = quantityCommand.intValue();
                    switch (channelUID.getId()) {
                        case DEVICE_CHANNEL_OH_DURATION_LIMIT:
                            strStore.put(DEVICE_CHANNEL_OH_DURATION_LIMIT, String.valueOf(targetValue));
                            break;
                        case DEVICE_CHANNEL_OH_VOLUME_LIMIT:
                            strStore.put(DEVICE_CHANNEL_OH_VOLUME_LIMIT, String.valueOf(targetValue));
                            break;
                    }
                } else if (command instanceof StringType) {
                    switch (channelUID.getId()) {
                        case DEVICE_CHANNEL_CHILD_LOCK: {
                            sendRequest(new LockReq(Integer.valueOf(command.toString())));
                        }
                            break;
                    }
                } else if (command instanceof OnOffType) {
                    // Alert dismiss events below
                    switch (channelUID.getId()) {
                        case DEVICE_CHANNEL_PAUSE_PLAN_OVERRIDE:
                            strStore.put(DEVICE_CHANNEL_PAUSE_PLAN_OVERRIDE, command.toString());
                            if (OnOffType.ON.equals(command)) {
                                scheduleRenewPlanPause();
                            } else {
                                cancelPlanPauseRenew();
                            }
                            break;
                        case DEVICE_CHANNEL_ACTIVE_WATERING:
                            if (OnOffType.ON.equals(command)) {
                                String volLimit = strStore.get(DEVICE_CHANNEL_OH_VOLUME_LIMIT);
                                if (volLimit == null) {
                                    volLimit = DEFAULT_INST_WATERING_VOL_LIMIT;
                                }
                                String durLimit = strStore.get(DEVICE_CHANNEL_OH_DURATION_LIMIT);
                                if (durLimit == null) {
                                    durLimit = DEFAULT_INST_WATERING_TIME_LIMIT;
                                }
                                sendRequest(
                                        new StartWateringReq(Integer.parseInt(durLimit), Integer.parseInt(volLimit)));
                            } else if (OnOffType.OFF.equals(command)) {
                                sendRequest(new DeviceCmdReq(CMD_IMMEDIATE_WATER_STOP));
                            }
                        case DEVICE_CHANNEL_FALL_STATUS: // 1
                            if (OnOffType.OFF.equals(command)) {
                                sendRequest(new DismissAlertReq(ALERT_DEVICE_FALL));
                            }
                            break;
                        case DEVICE_CHANNEL_SHUTDOWN_FAILURE: // 2
                            if (OnOffType.OFF.equals(command)) {
                                sendRequest(new DismissAlertReq(ALERT_VALVE_SHUTDOWN_FAIL));
                            }
                            break;
                        case DEVICE_CHANNEL_WATER_CUT: // 3
                            if (OnOffType.OFF.equals(command)) {
                                sendRequest(new DismissAlertReq(ALERT_WATER_CUTOFF));
                            }
                            break;
                        case DEVICE_CHANNEL_HIGH_FLOW: // 4
                            if (OnOffType.OFF.equals(command)) {
                                sendRequest(new DismissAlertReq(ALERT_UNEXPECTED_HIGH_FLOW));
                            }
                            break;
                        case DEVICE_CHANNEL_LOW_FLOW: // 5
                            if (OnOffType.OFF.equals(command)) {
                                sendRequest(new DismissAlertReq(ALERT_UNEXPECTED_LOW_FLOW));
                            }
                            break;
                    }
                }
                if (!READBACK_DISABLED_CHANNELS.contains(channelUID.getId())) {
                    requestReadbackPoll();
                }
            } catch (final InvalidParameterException ipe) {
                logger.warn("{}",
                        getLocalizedText("warning.parameter-not-accepted", getThing().getLabel(), channelUID.getId()));
            }
        });
    }

    /**
     * LinkTap communication protocol handlers
     */

    public void processDeviceCommand(final int commandId, final String frame) {
        receivedDataPush();
        logger.debug("{} processing device request with command {}", this.getThing().getLabel(), commandId);

        switch (commandId) {
            case CMD_UPDATE_WATER_TIMER_STATUS:
                // Store the latest value in the cache - to prevent unnecessary polls
                lastPollResultCache.putValue(frame);
                processCommand3(frame);
                break;
            case CMD_NOTIFICATION_WATERING_SKIPPED:
            case CMD_RAINFALL_DATA:
            case CMD_DATETIME_SYNC:
                logger.trace("No implementation for command {} for processing the Device request", commandId);
        }
    }

    private void processCommand3(final String request) {
        // There are three different formats that can arrive in this method:
        // -> Unsolicited with is a WaterMeterStatus.DeviceStatus payload
        // -> Solicited with a WaterMeterStatus payload (*)
        // -> Solicited with a WaterMeterStatus payload within an array
        // (*) A GSON plugin normalises the non array wrapped version to the array based version
        // This is handled below before the normalised processing takes place.
        WaterMeterStatus.DeviceStatus devStatus;
        {
            WaterMeterStatus mStatus = GSON.fromJson(request, WaterMeterStatus.class);
            if (mStatus == null) {
                return;
            }

            if (!mStatus.deviceStatuses.isEmpty()) {
                devStatus = mStatus.deviceStatuses.get(0);
            } else {
                devStatus = GSON.fromJson(request, WaterMeterStatus.DeviceStatus.class);
            }
            if (devStatus == null) {
                return;
            }
        }

        // Normalized processing below which uses devStatus

        final LinkTapBridgeHandler bridgeHandler = (LinkTapBridgeHandler) getBridgeHandler();
        String volumeUnit = "L";
        if (bridgeHandler != null) {
            String volumeUnitProp = bridgeHandler.getThing().getProperties().get(BRIDGE_PROP_VOL_UNIT);
            if (volumeUnitProp != null) {
                volumeUnit = volumeUnitProp;
            }
        }
        String prevPlanId = strStore.get(DEVICE_CHANNEL_WATER_PLAN_ID);
        if (prevPlanId == null) {
            prevPlanId = "0";
        }
        final String currPlanId = String.valueOf(devStatus.planSerialNo);
        if (isPlanPauseActive() && !prevPlanId.equals(currPlanId)) {
            scheduleRenewPlanPause();
        }
        updateState(DEVICE_CHANNEL_WATER_PLAN_ID, new StringType(String.valueOf(devStatus.planSerialNo)));
        strStore.put(DEVICE_CHANNEL_WATER_PLAN_ID, String.valueOf(devStatus.planSerialNo));

        final Integer planModeRaw = devStatus.planMode;
        if (planModeRaw != null) {
            updateState(DEVICE_CHANNEL_WATERING_MODE, new StringType(WateringMode.values()[planModeRaw].getDesc()));
        }

        final Integer childLockRaw = devStatus.childLock;
        if (childLockRaw != null) {
            updateState(DEVICE_CHANNEL_CHILD_LOCK, new StringType(ChildLockMode.values()[childLockRaw].getDesc()));
        }

        updateOnOffValue(DEVICE_CHANNEL_IS_MANUAL_MODE, devStatus.isManualMode);
        updateOnOffValue(DEVICE_CHANNEL_ACTIVE_WATERING, devStatus.isWatering);
        updateOnOffValue(DEVICE_CHANNEL_RF_LINKED, devStatus.isRfLinked);
        updateOnOffValue(DEVICE_CHANNEL_FLM_LINKED, devStatus.isFlmPlugin);
        updateOnOffValue(DEVICE_CHANNEL_FALL_STATUS, devStatus.isFall);
        updateOnOffValue(DEVICE_CHANNEL_SHUTDOWN_FAILURE, devStatus.isBroken);
        updateOnOffValue(DEVICE_CHANNEL_HIGH_FLOW, devStatus.isLeak);
        updateOnOffValue(DEVICE_CHANNEL_LOW_FLOW, devStatus.isClog);
        updateOnOffValue(DEVICE_CHANNEL_FINAL_SEGMENT, devStatus.isFinal);
        updateOnOffValue(DEVICE_CHANNEL_WATER_CUT, devStatus.isCutoff);

        final Integer signal = devStatus.signal;
        if (signal != null) {
            updateState(DEVICE_CHANNEL_SIGNAL, new QuantityType<>(signal, Units.PERCENT));
        }

        final Integer battery = devStatus.battery;
        if (battery != null) {
            updateState(DEVICE_CHANNEL_BATTERY, new QuantityType<>(battery, Units.PERCENT));
        }

        final Integer totalDuration = devStatus.totalDuration;
        if (totalDuration != null) {
            updateState(DEVICE_CHANNEL_TOTAL_DURATION, new QuantityType<>(totalDuration, Units.SECOND));
        }

        final Integer remainDuration = devStatus.remainDuration;
        if (remainDuration != null) {
            updateState(DEVICE_CHANNEL_REMAIN_DURATION, new QuantityType<>(remainDuration, Units.SECOND));
        }

        final Integer failsafeDuration = devStatus.failsafeDuration;
        if (failsafeDuration != null) {
            updateState(DEVICE_CHANNEL_FAILSAFE_DURATION, new QuantityType<>(failsafeDuration, Units.SECOND));
        }

        final Double speed = devStatus.speed;
        if (speed != null) {
            updateState(DEVICE_CHANNEL_FLOW_RATE, new QuantityType<>(speed,
                    "L".equals(volumeUnit) ? Units.LITRE_PER_MINUTE : ImperialUnits.GALLON_PER_MINUTE));
        }

        final Double volume = devStatus.volume;
        if (volume != null) {
            updateState(DEVICE_CHANNEL_CURRENT_VOLUME,
                    new QuantityType<>(volume, "L".equals(volumeUnit) ? Units.LITRE : ImperialUnits.GALLON_LIQUID_US));
        }

        final Double volumeLimit = devStatus.volumeLimit;
        if (volumeLimit != null) {
            updateState(DEVICE_CHANNEL_FAILSAFE_VOLUME, new QuantityType<>(volumeLimit,
                    "L".equals(volumeUnit) ? Units.LITRE : ImperialUnits.GALLON_LIQUID_US));
        }
    }

    private void updateOnOffValue(final String channelName, final @Nullable Boolean value) {
        if (value != null) {
            updateState(channelName, OnOffType.from(value));
        }
    }

    @Override
    public void handleBridgeDataUpdated() {
        switch (getThing().getStatus()) {
            case ONLINE:
                if (!initPending) {
                    logger.trace("Handling new bridge data for {} not required already online and processed",
                            getThing().getLabel());
                    return;
                }
            case OFFLINE:
            case UNKNOWN:
                logger.trace("Handling new bridge data for {}", getThing().getLabel());
                final LinkTapBridgeHandler bridge = (LinkTapBridgeHandler) getBridgeHandler();
                if (bridge != null) {
                    if (bridge.getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                        return;
                    }
                    initAfterBridge(bridge);
                }
                break;
            default:
                logger.trace("Handling new bridge data for {} not required", getThing().getLabel());
        }
    }
}
