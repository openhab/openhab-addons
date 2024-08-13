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
package org.openhab.binding.linktap.internal;

import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.BRIDGE_PROP_VOL_UNIT;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.CHILD_LOCK_MODE;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_ACTIVE_WATERING;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_BATTERY;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_CHILD_LOCK;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_CURRENT_VOLUME;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_FAILSAFE_DURATION;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_FAILSAFE_VOLUME;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_FALL_STATUS;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_FINAL_SEGMENT;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_FLM_LINKED;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_FLOW_RATE;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_HIGH_FLOW;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_IS_MANUAL_MODE;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_LOW_FLOW;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_OH_DURATION_LIMIT;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_OH_VOLUME_LIMIT;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_REMAIN_DURATION;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_RF_LINKED;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_SHUTDOWN_FAILURE;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_SIGNAL;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_TOTAL_DURATION;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_WATERING_MODE;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_WATER_CUT;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_WSKIP_DATE_TIME;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_WSKIP_NEXT_RAIN;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_WSKIP_PREV_RAIN;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.GSON;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.WATERING_MODE;
import static org.openhab.binding.linktap.protocol.frames.DismissAlertReq.ALERT_DEVICE_FALL;
import static org.openhab.binding.linktap.protocol.frames.DismissAlertReq.ALERT_UNEXPECTED_HIGH_FLOW;
import static org.openhab.binding.linktap.protocol.frames.DismissAlertReq.ALERT_UNEXPECTED_LOW_FLOW;
import static org.openhab.binding.linktap.protocol.frames.DismissAlertReq.ALERT_VALVE_SHUTDOWN_FAIL;
import static org.openhab.binding.linktap.protocol.frames.DismissAlertReq.ALERT_WATER_CUTOFF;
import static org.openhab.binding.linktap.protocol.frames.SetDeviceConfigReq.CONFIG_DURATION_LIMIT;
import static org.openhab.binding.linktap.protocol.frames.SetDeviceConfigReq.CONFIG_VOLUME_LIMIT;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_DATETIME_SYNC;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_IMMEDIATE_WATER_STOP;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_NOTIFICATION_WATERING_SKIPPED;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_RAINFALL_DATA;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_UPDATE_WATER_TIMER_STATUS;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linktap.protocol.frames.AlertStateReq;
import org.openhab.binding.linktap.protocol.frames.DeviceCmdReq;
import org.openhab.binding.linktap.protocol.frames.DismissAlertReq;
import org.openhab.binding.linktap.protocol.frames.LockReq;
import org.openhab.binding.linktap.protocol.frames.SetDeviceConfigReq;
import org.openhab.binding.linktap.protocol.frames.StartWateringReq;
import org.openhab.binding.linktap.protocol.frames.TLGatewayFrame;
import org.openhab.binding.linktap.protocol.frames.WaterMeterStatus;
import org.openhab.binding.linktap.protocol.frames.WateringSkippedNotification;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
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
public class LinkTapHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LinkTapHandler.class);

    private static final String MARKER_INVALID_DEVICE_KEY = "---INVALID---";
    private String registeredDeviceId = "";

    private static final String DEFAULT_INST_WATERING_VOL_LIMIT = "0";
    private static final String DEFAULT_INST_WATERING_TIME_LIMIT = "15";

    private final Object pollLock = new Object();
    private final Storage<String> strStore;

    protected ExpiringCache<String> lastPollResultCache = new ExpiringCache<>(Duration.ofSeconds(5),
            LinkTapHandler::expireCacheContents);

    private final Object schedulerLock = new Object();
    private @Nullable ScheduledFuture<?> backgroundGwPollingScheduler;

    private volatile long lastStatusCommandRecvTs = 0L;

    public LinkTapHandler(Thing thing, Storage<String> strStore) {
        super(thing);
        this.strStore = strStore;
    }

    private void startStatusPolling() {
        synchronized (schedulerLock) {
            cancelStatusPolling();
            backgroundGwPollingScheduler = scheduler.scheduleWithFixedDelay(() -> {
                if (lastStatusCommandRecvTs + 135000 > System.currentTimeMillis()) {
                    return;
                }
                pollForUpdate();
            }, 1, 10, TimeUnit.SECONDS);
        }
    }

    private void cancelStatusPolling() {
        synchronized (schedulerLock) {
            final ScheduledFuture<?> ref = backgroundGwPollingScheduler;
            if (ref != null) {
                ref.cancel(true);
                backgroundGwPollingScheduler = null;
            }
        }
    }

    private static @Nullable String expireCacheContents() {
        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        scheduler.submit(() -> {
            if (command instanceof RefreshType rt) {
                switch (channelUID.getId()) {
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
                    case DEVICE_CHANNEL_FAILSAFE_VOLUME: {
                        SendRequest(new SetDeviceConfigReq(CONFIG_VOLUME_LIMIT, targetValue));
                    }
                        break;
                    case DEVICE_CHANNEL_TOTAL_DURATION: {
                        SendRequest(new SetDeviceConfigReq(CONFIG_DURATION_LIMIT, targetValue));
                    }
                        break;
                }
            } else if (command instanceof StringType stringCmd) {
                switch (channelUID.getId()) {
                    case DEVICE_CHANNEL_CHILD_LOCK: {
                        SendRequest(new LockReq(Integer.valueOf(command.toString())));
                    }
                        break;
                }
            } else if (command instanceof OnOffType) {
                // Alert dismiss events below
                switch (channelUID.getId()) {
                    case DEVICE_CHANNEL_IS_MANUAL_MODE:
                        // We can pull from channels I suspect as they feed stateful representations of data
                        // therefore the data we need below - we need to cache from writes and reads from the relevant
                        // polls / writes, for the items / data points.
                        if (command.equals(OnOffType.OFF)) {
                            SendRequest(new DeviceCmdReq(CMD_IMMEDIATE_WATER_STOP));
                        }
                    case DEVICE_CHANNEL_ACTIVE_WATERING:
                        if (command.equals(OnOffType.ON)) {
                            String volLimit = strStore.get(DEVICE_CHANNEL_OH_VOLUME_LIMIT);
                            if (volLimit == null) {
                                volLimit = DEFAULT_INST_WATERING_VOL_LIMIT;
                            }
                            String durLimit = strStore.get(DEVICE_CHANNEL_OH_DURATION_LIMIT);
                            if (durLimit == null) {
                                durLimit = DEFAULT_INST_WATERING_TIME_LIMIT;
                            }
                            SendRequest(new StartWateringReq(Integer.parseInt(durLimit), Integer.parseInt(volLimit)));
                        } else if (command.equals(OnOffType.OFF)) {
                            SendRequest(new DeviceCmdReq(CMD_IMMEDIATE_WATER_STOP));
                        }
                    case DEVICE_CHANNEL_FALL_STATUS: // 1
                        if (command.equals(OnOffType.OFF)) {
                            SendRequest(new DismissAlertReq(ALERT_DEVICE_FALL));
                        }
                        break;
                    case DEVICE_CHANNEL_SHUTDOWN_FAILURE: // 2
                        if (command.equals(OnOffType.OFF)) {
                            SendRequest(new DismissAlertReq(ALERT_VALVE_SHUTDOWN_FAIL));
                        }
                        break;
                    case DEVICE_CHANNEL_WATER_CUT: // 3
                        if (command.equals(OnOffType.OFF)) {
                            SendRequest(new DismissAlertReq(ALERT_WATER_CUTOFF));
                        }
                        break;
                    case DEVICE_CHANNEL_HIGH_FLOW: // 4
                        if (command.equals(OnOffType.OFF)) {
                            SendRequest(new DismissAlertReq(ALERT_UNEXPECTED_HIGH_FLOW));
                        }
                        break;
                    case DEVICE_CHANNEL_LOW_FLOW: // 5
                        if (command.equals(OnOffType.OFF)) {
                            SendRequest(new DismissAlertReq(ALERT_UNEXPECTED_LOW_FLOW));
                        }
                        break;
                }
            }
        });

    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        final LinkTapBridgeHandler bridge = (LinkTapBridgeHandler) getBridgeHandler();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge is not selected / set");
            return;
        }

        scheduler.execute(() -> {
            switch (bridge.getThing().getStatus()) {
                case UNKNOWN:
                case INITIALIZING:
                case UNINITIALIZED:
                    scheduler.schedule(this::initialize, 1, TimeUnit.SECONDS);
                    break;
                default:
                    initAfterBridge(bridge);
            }
        });
    }

    private void initAfterBridge(final LinkTapBridgeHandler bridge) {
        String deviceId = getValidatedIdString();
        if (deviceId.equals(MARKER_INVALID_DEVICE_KEY)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Device not found in bridges known devices");
            if (!registeredDeviceId.isBlank()) {
                LinkTapBridgeHandler.devIdLookup.deregisterItem(registeredDeviceId, this, () -> {
                });
            }
            registeredDeviceId = "";
            return;
        } else {
            registeredDeviceId = deviceId;
        }

        boolean knownToBridge = bridge.GetDiscoveredDevices().anyMatch(x -> deviceId.equals(x.deviceId));
        if (knownToBridge) {
            updateStatus(ThingStatus.ONLINE);
            LinkTapBridgeHandler.devIdLookup.registerItem(registeredDeviceId, this, () -> {
            });
            scheduleInitialPoll();
            scheduler.execute(() -> {
                SendRequest(new AlertStateReq(0, true));
            });
            startStatusPolling();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge does not recognise device id");
        }
    }

    @Override
    public void dispose() {
        cancelInitialPoll(true);
        LinkTapBridgeHandler.devIdLookup.deregisterItem(registeredDeviceId, this, () -> {
        });
        cancelStatusPolling();
    }

    public String SendRequest(TLGatewayFrame frame) {
        if (frame instanceof DeviceCmdReq) {
            final String deviceAddr = getValidatedIdString();
            if (deviceAddr.equals(MARKER_INVALID_DEVICE_KEY)) {
                logger.warn("Device is unknown - will not send");
                return "";
            }
            ((DeviceCmdReq) frame).deviceId = deviceAddr;
        }

        // Validate the payload is within the expected limits for the device its being sent to
        {
            final String validationError = frame.isValid();
            if (!validationError.isEmpty()) {
                logger.warn("{} -> Payload validation failed - {}", getThing().getLabel(), validationError);
                return "";
            }
        }
        Bridge parentBridge = getBridge();
        if (parentBridge == null) {
            logger.warn("Cannot send device does not have a valid bridge");
            return "";
        }
        LinkTapBridgeHandler parentBridgeHandler = (LinkTapBridgeHandler) parentBridge.getHandler();
        if (parentBridgeHandler == null) {
            logger.warn("Cannot send device does not have a valid bridge handler");
            return "";
        }
        return parentBridgeHandler.SendRequest(frame);
    }

    private @Nullable BridgeHandler getBridgeHandler() {
        Bridge bridgeRef = getBridge();
        if (bridgeRef == null) {
            return null;
        } else {
            return bridgeRef.getHandler();
        }
    }

    @NotNull
    public String getValidatedIdString() {
        final LinkTapDeviceConfiguration config = getConfigAs(LinkTapDeviceConfiguration.class);

        BridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler instanceof LinkTapBridgeHandler vesyncBridgeHandler) {
            final String devId = config.deviceId;

            // Try to use the device address id directly
            if (devId != null) {
                logger.trace("Searching for device address id : {}", devId);
                @Nullable
                final LinkTapDeviceMetadata metadata = vesyncBridgeHandler.getDeviceLookup().get(devId.toLowerCase());

                if (metadata != null) {
                    return metadata.deviceId;
                }
            }

            final String deviceName = config.deviceName;

            // Check if the device name can be matched to a single device
            if (deviceName != null) {
                final String[] matchedAddressIds = vesyncBridgeHandler.GetDiscoveredDevices()
                        .filter(x -> deviceName.equals(x.deviceName)).map(x -> x.deviceId).toArray(String[]::new);

                for (String val : matchedAddressIds) {
                    logger.trace("Found Address ID match on name with : {}", val);
                }

                if (matchedAddressIds.length != 1) {
                    return MARKER_INVALID_DEVICE_KEY;
                }

                return matchedAddressIds[0];
            }
        }

        return MARKER_INVALID_DEVICE_KEY;
    }

    public void processDeviceCommand(final int commandId, final String frame) {
        lastStatusCommandRecvTs = System.currentTimeMillis();
        logger.debug("{} processing device request with command {}", getThing().getLabel(), commandId);

        switch (commandId) {
            case CMD_UPDATE_WATER_TIMER_STATUS:
                // Store the latest value in the cache - to prevent unnecessary polls
                lastPollResultCache.putValue(frame);
                processCommand3(frame);
                break;
            case CMD_NOTIFICATION_WATERING_SKIPPED:
                processCommand9(frame);
                break;
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

        updateState(DEVICE_CHANNEL_WATERING_MODE, new StringType(WATERING_MODE.values()[devStatus.planMode].getDesc()));
        updateState(DEVICE_CHANNEL_IS_MANUAL_MODE, OnOffType.from(devStatus.isManualMode));
        updateState(DEVICE_CHANNEL_ACTIVE_WATERING, OnOffType.from(devStatus.isWatering));
        updateState(DEVICE_CHANNEL_RF_LINKED, OnOffType.from(devStatus.isRfLinked));
        updateState(DEVICE_CHANNEL_FLM_LINKED, OnOffType.from(devStatus.isFlmPlugin));
        updateState(DEVICE_CHANNEL_FALL_STATUS, OnOffType.from(devStatus.isFall));
        updateState(DEVICE_CHANNEL_SHUTDOWN_FAILURE, OnOffType.from(devStatus.isBroken));
        updateState(DEVICE_CHANNEL_HIGH_FLOW, OnOffType.from(devStatus.isLeak));
        updateState(DEVICE_CHANNEL_LOW_FLOW, OnOffType.from(devStatus.isClog));
        updateState(DEVICE_CHANNEL_FINAL_SEGMENT, OnOffType.from(devStatus.isFinal));
        updateState(DEVICE_CHANNEL_SIGNAL, new QuantityType<>(devStatus.signal, Units.PERCENT));
        updateState(DEVICE_CHANNEL_BATTERY, new QuantityType<>(devStatus.battery, Units.PERCENT));
        updateState(DEVICE_CHANNEL_WATER_CUT, OnOffType.from(devStatus.isCutoff));
        updateState(DEVICE_CHANNEL_CHILD_LOCK, new StringType(CHILD_LOCK_MODE.values()[devStatus.childLock].getDesc()));
        updateState(DEVICE_CHANNEL_TOTAL_DURATION, new QuantityType<>(devStatus.totalDuration, Units.SECOND));
        updateState(DEVICE_CHANNEL_REMAIN_DURATION, new QuantityType<>(devStatus.remainDuration, Units.SECOND));
        updateState(DEVICE_CHANNEL_FAILSAFE_DURATION, new QuantityType<>(devStatus.failsafeDuration, Units.SECOND));
        updateState(DEVICE_CHANNEL_FLOW_RATE, new QuantityType<>(
                "L".equals(volumeUnit) ? devStatus.speed : (devStatus.speed * 3.785), Units.LITRE_PER_MINUTE));
        updateState(DEVICE_CHANNEL_CURRENT_VOLUME, new QuantityType<>(
                "L".equals(volumeUnit) ? devStatus.volume : (devStatus.volume * 3.785), Units.LITRE));
        updateState(DEVICE_CHANNEL_FAILSAFE_VOLUME, new QuantityType<>(
                "L".equals(volumeUnit) ? devStatus.volumeLimit : (devStatus.volumeLimit * 3.785), Units.LITRE));
    }

    private void processCommand9(final String request) {
        final WateringSkippedNotification skippedNotice = GSON.fromJson(request, WateringSkippedNotification.class);
        if (skippedNotice == null) {
            return;
        }
        logger.trace("Received rainfall skipped notice - past hour {}, next hour {}", skippedNotice.getPastRainfall(),
                skippedNotice.getFutureRainfall());
        updateState(DEVICE_CHANNEL_WSKIP_PREV_RAIN,
                new QuantityType<>(skippedNotice.getPastRainfall(), Units.MILLIMETRE_PER_HOUR));
        updateState(DEVICE_CHANNEL_WSKIP_NEXT_RAIN,
                new QuantityType<>(skippedNotice.getFutureRainfall(), Units.MILLIMETRE_PER_HOUR));
        updateState(DEVICE_CHANNEL_WSKIP_DATE_TIME, new DateTimeType());
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);

        if (getThing().getStatusInfo().getStatus() == ThingStatus.ONLINE) {
            scheduler.execute(this::pollForUpdate);
        }
    }

    private void scheduleInitialPoll() {
        cancelInitialPoll(false);
        initialPollingTask = scheduler.schedule(() -> {
            // 15 second's is to ensure even slow systems have time to pull the gateway data
            // ready.
            sendChannelRefresh(DEVICE_CHANNEL_OH_DURATION_LIMIT);
            sendChannelRefresh(DEVICE_CHANNEL_OH_VOLUME_LIMIT);

            pollForUpdate();
        }, 15, TimeUnit.SECONDS);
    }

    private void sendChannelRefresh(final String channelName) {
        final Channel ch = getThing().getChannel(DEVICE_CHANNEL_OH_VOLUME_LIMIT);
        if (ch != null) {
            handleCommand(ch.getUID(), RefreshType.REFRESH);
        }
    }

    private void cancelInitialPoll(final boolean interruptAllowed) {
        final ScheduledFuture<?> pollJob = initialPollingTask;
        if (pollJob != null && !pollJob.isCancelled()) {
            pollJob.cancel(interruptAllowed);
            initialPollingTask = null;
        }
    }

    @Nullable
    // This is used to coalesce poll's for CMD 3 - WATER METER STATUS
    // otherwise bulk new channel links force many poll's, and an unsolicited update
    // may recently have already provided the data needed.
    ScheduledFuture<?> initialPollingTask = null;

    public void pollForUpdate() {
        String response = "";
        synchronized (pollLock) {
            response = lastPollResultCache.getValue();
            if (response == null) {
                response = SendRequest(new DeviceCmdReq(CMD_UPDATE_WATER_TIMER_STATUS));
                // Todo: Check response status
                lastPollResultCache.putValue(response);
            }
        }
        processCommand3(response);
    }
}
