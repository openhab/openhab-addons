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

import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_OH_DURATION_LIMIT;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.DEVICE_CHANNEL_OH_VOLUME_LIMIT;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.EMPTY_STRING;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linktap.configuration.LinkTapDeviceConfiguration;
import org.openhab.binding.linktap.protocol.frames.DeviceCmdReq;
import org.openhab.binding.linktap.protocol.frames.TLGatewayFrame;
import org.openhab.binding.linktap.protocol.http.DeviceIdException;
import org.openhab.binding.linktap.protocol.http.InvalidParameterException;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.RefreshType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PollingDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public abstract class PollingDeviceHandler extends BaseThingHandler implements IBridgeData {

    protected boolean initPending = true;

    protected static final String MARKER_INVALID_DEVICE_KEY = "---INVALID---";

    private final Logger logger = LoggerFactory.getLogger(PollingDeviceHandler.class);
    private final Object pollLock = new Object();
    private final Object schedulerLock = new Object();
    private final Object readBackPollLock = new Object();
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final Bundle bundle;

    protected volatile LinkTapDeviceConfiguration config = new LinkTapDeviceConfiguration();
    private volatile long lastStatusCommandRecvTs = 0L;

    protected String registeredDeviceId = EMPTY_STRING;
    protected ExpiringCache<String> lastPollResultCache = new ExpiringCache<>(Duration.ofSeconds(5),
            PollingDeviceHandler::expireCacheContents);
    private @Nullable ScheduledFuture<?> backgroundGwPollingScheduler;
    private @Nullable ScheduledFuture<?> readBackPollSf = null;

    protected void requestReadbackPoll() {
        synchronized (readBackPollLock) {
            cancelReadbackPoll();
            scheduler.schedule(() -> {
                pollForUpdate(true);
            }, 750, TimeUnit.MILLISECONDS);
        }
    }

    protected void cancelReadbackPoll() {
        synchronized (readBackPollLock) {
            ScheduledFuture<?> readBackPollSfRef = readBackPollSf;
            if (readBackPollSfRef != null) {
                readBackPollSfRef.cancel(false);
                readBackPollSf = null;
            }
        }
    }

    public PollingDeviceHandler(final Thing thing, TranslationProvider translationProvider,
            LocaleProvider localeProvider) {
        super(thing);
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(getClass());
    }

    public String getLocalizedText(String key, @Nullable Object @Nullable... arguments) {
        String result = translationProvider.getText(bundle, key, key, localeProvider.getLocale(), arguments);
        return Objects.nonNull(result) ? result : key;
    }

    private void startStatusPolling() {
        synchronized (schedulerLock) {
            cancelStatusPolling();
            backgroundGwPollingScheduler = scheduler.scheduleWithFixedDelay(() -> {
                if (lastStatusCommandRecvTs + 135000 > System.currentTimeMillis()) {
                    return;
                }
                pollForUpdate(false);
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
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(LinkTapDeviceConfiguration.class);
        if (!(getBridgeHandler() instanceof LinkTapBridgeHandler bridgeHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    getLocalizedText("polling-device.error.bridge-unset"));
            return;
        } else if (ThingStatus.OFFLINE.equals(bridgeHandler.getThing().getStatus())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        scheduler.execute(() -> {
            if (ThingStatus.ONLINE.equals(bridgeHandler.getThing().getStatus())) {
                initAfterBridge(bridgeHandler);
            }
        });
    }

    protected void initAfterBridge(final LinkTapBridgeHandler bridge) {
        initPending = true;
        String deviceId = getValidatedIdString();
        if (MARKER_INVALID_DEVICE_KEY.equals(deviceId)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    getLocalizedText("polling-device.error.device-unknown-in-bridge"));
            if (!registeredDeviceId.isBlank()) {
                deregisterDevice();
            }
            registeredDeviceId = EMPTY_STRING;
            return;
        } else {
            registeredDeviceId = deviceId;
        }

        // This can be called, and then the bridge data gets updated
        boolean knownToBridge = bridge.getDiscoveredDevices().anyMatch(x -> deviceId.equals(x.deviceId));
        if (knownToBridge) {
            if (!ThingStatus.ONLINE.equals(getThing().getStatus())) {
                updateStatus(ThingStatus.ONLINE);
            }
            registerDevice();
            scheduleInitialPoll();
            scheduler.execute(this::runStartInit);
            startStatusPolling();
            initPending = false;
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    getLocalizedText("polling-device.error.unknown-device-id"));
        }
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
        scheduler.execute(() -> {
            if (initPending && ThingStatus.ONLINE.equals(status)) {
                final BridgeHandler handler = getBridgeHandler();
                if (handler instanceof LinkTapBridgeHandler linkTapBridge) {
                    initAfterBridge(linkTapBridge);
                }
            }
        });
    }

    protected abstract void runStartInit();

    protected abstract void registerDevice();

    @Override
    public void dispose() {
        cancelInitialPoll(true);
        deregisterDevice();
        cancelStatusPolling();
    }

    protected abstract void deregisterDevice();

    @Nullable
    BridgeHandler getBridgeHandler() {
        Bridge bridgeRef = getBridge();
        if (bridgeRef == null) {
            return null;
        } else {
            return bridgeRef.getHandler();
        }
    }

    public String sendRequest(TLGatewayFrame frame) throws InvalidParameterException {
        if (frame instanceof DeviceCmdReq devCmdReq) {
            final String deviceAddr = getValidatedIdString();
            if (deviceAddr.equals(MARKER_INVALID_DEVICE_KEY)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        getLocalizedText("polling-device.error.unknown-device"));
                return EMPTY_STRING;
            }
            devCmdReq.deviceId = deviceAddr;
        }

        final Bridge parentBridge = getBridge();
        if (parentBridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    getLocalizedText("polling-device.error.bridge-unset"));
            return EMPTY_STRING;
        }
        final LinkTapBridgeHandler parentBridgeHandler = (LinkTapBridgeHandler) parentBridge.getHandler();
        if (parentBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    getLocalizedText("polling-device.error.bridge-unset"));
            return EMPTY_STRING;
        }
        try {
            return parentBridgeHandler.sendRequest(frame);
        } catch (final DeviceIdException die) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, getLocalizedText(die.getI18Key()));
        }
        return EMPTY_STRING;
    }

    @NotNull
    public String getValidatedIdString() {
        BridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler instanceof LinkTapBridgeHandler vesyncBridgeHandler) {
            final String devId = config.id;

            // Try to use the device address id directly
            if (!devId.isEmpty()) {
                logger.trace("Searching for device address id : {}", devId);
                @Nullable
                final LinkTapDeviceMetadata metadata = vesyncBridgeHandler.getDeviceLookup().get(devId);

                if (metadata != null) {
                    logger.trace("Using matched Address ID (dev_id) {}", metadata.deviceId);
                    return metadata.deviceId;
                }
            }

            final String deviceName = config.name;

            // Check if the device name can be matched to a single device
            if (!deviceName.isEmpty()) {
                final String[] matchedAddressIds = vesyncBridgeHandler.getDiscoveredDevices()
                        .filter(x -> deviceName.equals(x.deviceName)).map(x -> x.deviceId).toArray(String[]::new);

                for (String val : matchedAddressIds) {
                    logger.trace("Found Address ID match on name with : {}", val);
                }

                if (matchedAddressIds.length != 1) {
                    return MARKER_INVALID_DEVICE_KEY;
                }

                logger.trace("Using matched Address ID (dev_name) {}", matchedAddressIds[0]);
                return matchedAddressIds[0];
            }
        }

        return MARKER_INVALID_DEVICE_KEY;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);

        if (getThing().getStatusInfo().getStatus() == ThingStatus.ONLINE) {
            scheduler.execute(() -> {
                pollForUpdate(false);
            });
        }
    }

    private void scheduleInitialPoll() {
        cancelInitialPoll(false);
        initialPollingTask = scheduler.schedule(() -> {
            // 15 second's is to ensure even slow systems have time to pull the gateway data
            // ready.
            sendChannelRefresh(DEVICE_CHANNEL_OH_DURATION_LIMIT);
            sendChannelRefresh(DEVICE_CHANNEL_OH_VOLUME_LIMIT);

            pollForUpdate(false);
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

    public void pollForUpdate(boolean skipCache) {
        String response = EMPTY_STRING;
        synchronized (pollLock) {
            response = lastPollResultCache.getValue();
            if (response == null || skipCache) {
                response = getPollResponseData();
                lastPollResultCache.putValue(response);
            }
        }
        processPollResponseData(response);
    }

    protected abstract String getPollResponseData();

    protected abstract void processPollResponseData(final String data);

    protected void receivedDataPush() {
        lastStatusCommandRecvTs = System.currentTimeMillis();
    }
}
