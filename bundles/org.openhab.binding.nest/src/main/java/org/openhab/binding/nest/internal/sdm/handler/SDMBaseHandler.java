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
package org.openhab.binding.nest.internal.sdm.handler;

import static org.openhab.core.thing.ThingStatus.*;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.sdm.SDMBindingConstants;
import org.openhab.binding.nest.internal.sdm.api.SDMAPI;
import org.openhab.binding.nest.internal.sdm.config.SDMDeviceConfiguration;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMCommandRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMCommandResponse;
import org.openhab.binding.nest.internal.sdm.dto.SDMDevice;
import org.openhab.binding.nest.internal.sdm.dto.SDMEvent;
import org.openhab.binding.nest.internal.sdm.dto.SDMEvent.SDMResourceUpdate;
import org.openhab.binding.nest.internal.sdm.dto.SDMIdentifiable;
import org.openhab.binding.nest.internal.sdm.dto.SDMParentRelation;
import org.openhab.binding.nest.internal.sdm.dto.SDMResourceName.SDMResourceNameType;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMCameraImageTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMCameraLiveStreamTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMConnectivityStatus;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMConnectivityTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMDeviceInfoTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMDeviceSettingsTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMResolution;
import org.openhab.binding.nest.internal.sdm.exception.FailedSendingSDMDataException;
import org.openhab.binding.nest.internal.sdm.exception.InvalidSDMAccessTokenException;
import org.openhab.binding.nest.internal.sdm.listener.SDMEventListener;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SDMBaseHandler} provides the common functionality of all SDM device thing handlers.
 *
 * @author Brian Higginbotham - Initial contribution
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public abstract class SDMBaseHandler extends BaseThingHandler implements SDMIdentifiable, SDMEventListener {

    private final Logger logger = LoggerFactory.getLogger(SDMBaseHandler.class);

    protected @NonNullByDefault({}) SDMDeviceConfiguration config;
    protected SDMDevice device = new SDMDevice();
    protected String deviceId = "";
    protected @Nullable ZonedDateTime lastRefreshDateTime;
    protected @Nullable ScheduledFuture<?> refreshJob;
    protected final TimeZoneProvider timeZoneProvider;

    public SDMBaseHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        updateBridgeStatus();
    }

    /**
     * Updates the thing state based on that of the bridge.
     */
    protected void updateBridgeStatus() {
        Bridge bridge = getBridge();
        ThingStatus bridgeStatus = bridge != null ? bridge.getStatus() : null;
        if (bridge == null) {
            disableRefresh();
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else if (bridgeStatus == ONLINE && thing.getStatus() != ONLINE) {
            enableRefresh();
        } else if (bridgeStatus == OFFLINE) {
            disableRefresh();
            updateStatus(OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (bridgeStatus == UNKNOWN) {
            disableRefresh();
            updateStatus(UNKNOWN);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            delayedRefresh();
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for {}", thing.getUID());
        config = getConfigAs(SDMDeviceConfiguration.class);
        deviceId = config.deviceId;
        updateStatus(ThingStatus.UNKNOWN);
        updateBridgeStatus();
    }

    @Override
    public void dispose() {
        disableRefresh();
    }

    @Override
    public String getId() {
        return deviceId;
    }

    protected @Nullable SDMAccountHandler getAccountHandler() {
        Bridge bridge = getBridge();
        return bridge != null ? (SDMAccountHandler) bridge.getHandler() : null;
    }

    protected @Nullable SDMAPI getAPI() {
        SDMAccountHandler accountHandler = getAccountHandler();
        return accountHandler != null ? accountHandler.getAPI() : null;
    }

    protected @Nullable SDMDevice getDeviceInfo() throws FailedSendingSDMDataException, InvalidSDMAccessTokenException {
        SDMAPI api = getAPI();
        return api == null ? null : api.getDevice(deviceId);
    }

    protected <T extends SDMCommandResponse> @Nullable T executeDeviceCommand(SDMCommandRequest<T> request)
            throws FailedSendingSDMDataException, InvalidSDMAccessTokenException {
        SDMAPI api = getAPI();
        return api == null ? null : api.executeDeviceCommand(deviceId, request);
    }

    protected @Nullable SDMTraits getTraitsForUpdate(SDMEvent event) {
        SDMResourceUpdate resourceUpdate = event.resourceUpdate;
        if (resourceUpdate == null) {
            return null;
        }

        SDMTraits traits = resourceUpdate.traits;
        if (traits == null) {
            return null;
        }

        ZonedDateTime localRefreshDateTime = lastRefreshDateTime;
        if (localRefreshDateTime == null || event.timestamp.isBefore(localRefreshDateTime)) {
            return null;
        }

        return traits;
    }

    @Override
    public void onEvent(SDMEvent event) {
        SDMTraits traits = getTraitsForUpdate(event);
        if (traits != null) {
            logger.debug("Updating traits using resource update traits in event");
            device.traits.updateTraits(traits);
        }
    }

    protected void refreshDevice() {
        try {
            SDMDevice localDevice = getDeviceInfo();
            if (localDevice == null) {
                logger.debug("Cannot refresh device (empty response or handler has no bridge)");
                return;
            }

            this.device = localDevice;
            this.lastRefreshDateTime = ZonedDateTime.now();

            Map<String, String> properties = editProperties();
            properties.putAll(getDeviceProperties(localDevice));
            updateProperties(properties);

            updateStateWithTraits(localDevice.traits);
        } catch (InvalidSDMAccessTokenException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (FailedSendingSDMDataException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    protected void updateStateWithTraits(SDMTraits traits) {
        SDMConnectivityTrait connectivity = traits.connectivity;
        if (connectivity == null && device.traits.connectivity != null) {
            logger.debug("Skipping partial update for device with connectivity trait");
            return;
        }

        ThingStatus thingStatus = connectivity == null || connectivity.status == null
                || connectivity.status == SDMConnectivityStatus.ONLINE ? ThingStatus.ONLINE : ThingStatus.OFFLINE;

        if (thing.getStatus() != thingStatus) {
            updateStatus(thingStatus);
        }
    }

    protected void enableRefresh() {
        scheduleRefreshJob();
        SDMAccountHandler handler = getAccountHandler();
        if (handler != null) {
            handler.addThingDataListener(getId(), this);
        }
    }

    protected void disableRefresh() {
        cancelRefreshJob();
        SDMAccountHandler handler = getAccountHandler();
        if (handler != null) {
            handler.removeThingDataListener(getId(), this);
        }
    }

    protected void cancelRefreshJob() {
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null && !localRefreshJob.isCancelled()) {
            localRefreshJob.cancel(true);
        }
    }

    protected void scheduleRefreshJob() {
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob == null || localRefreshJob.isCancelled()) {
            refreshJob = scheduler.scheduleWithFixedDelay(this::refreshDevice, 0, config.refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    protected void delayedRefresh() {
        cancelRefreshJob();
        refreshJob = scheduler.scheduleWithFixedDelay(this::refreshDevice, 3, config.refreshInterval, TimeUnit.SECONDS);
    }

    public static Map<String, String> getDeviceProperties(SDMDevice device) {
        Map<String, String> properties = new HashMap<>();

        SDMTraits traits = device.traits;

        SDMDeviceInfoTrait deviceInfo = traits.deviceInfo;
        if (deviceInfo != null && !deviceInfo.customName.isBlank()) {
            properties.put(SDMBindingConstants.PROPERTY_CUSTOM_NAME, deviceInfo.customName);
        }

        List<SDMParentRelation> parentRelations = device.parentRelations;
        for (SDMParentRelation parentRelation : parentRelations) {
            if (parentRelation.parent.type == SDMResourceNameType.ROOM && !parentRelation.displayName.isBlank()) {
                properties.put(SDMBindingConstants.PROPERTY_ROOM, parentRelation.displayName);
                break;
            }
        }

        SDMDeviceSettingsTrait deviceSettings = traits.deviceSettings;
        if (deviceSettings != null) {
            properties.put(SDMBindingConstants.PROPERTY_TEMPERATURE_SCALE, deviceSettings.temperatureScale.name());
        }

        SDMCameraImageTrait cameraImage = traits.cameraImage;
        if (cameraImage != null) {
            SDMResolution resolution = cameraImage.maxImageResolution;
            properties.put(SDMBindingConstants.PROPERTY_MAX_IMAGE_RESOLUTION,
                    String.format("%sx%s", resolution.width, resolution.height));
        }

        SDMCameraLiveStreamTrait cameraLiveStream = traits.cameraLiveStream;
        if (cameraLiveStream != null) {
            List<String> audioCodecs = cameraLiveStream.audioCodecs;
            if (audioCodecs != null) {
                properties.put(SDMBindingConstants.PROPERTY_AUDIO_CODECS,
                        audioCodecs.stream().collect(Collectors.joining(", ")));
            }

            SDMResolution maxVideoResolution = cameraLiveStream.maxVideoResolution;
            if (maxVideoResolution != null) {
                SDMResolution resolution = maxVideoResolution;
                properties.put(SDMBindingConstants.PROPERTY_MAX_VIDEO_RESOLUTION,
                        String.format("%sx%s", resolution.width, resolution.height));
            }

            List<String> supportedProtocols = cameraLiveStream.supportedProtocols;
            if (supportedProtocols != null) {
                properties.put(SDMBindingConstants.PROPERTY_SUPPORTED_PROTOCOLS,
                        supportedProtocols.stream().collect(Collectors.joining(", ")));
            }

            List<String> videoCodecs = cameraLiveStream.videoCodecs;
            if (videoCodecs != null) {
                properties.put(SDMBindingConstants.PROPERTY_VIDEO_CODECS,
                        videoCodecs.stream().collect(Collectors.joining(", ")));
            }
        }

        return properties;
    }
}
