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
package org.openhab.binding.nuki.internal.handler;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nuki.internal.constants.NukiBindingConstants;
import org.openhab.binding.nuki.internal.dataexchange.BridgeListResponse;
import org.openhab.binding.nuki.internal.dataexchange.BridgeLockStateResponse;
import org.openhab.binding.nuki.internal.dataexchange.NukiBaseResponse;
import org.openhab.binding.nuki.internal.dataexchange.NukiHttpClient;
import org.openhab.binding.nuki.internal.dto.BridgeApiDeviceStateDto;
import org.openhab.binding.nuki.internal.dto.BridgeApiListDeviceDto;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractNukiDeviceHandler} is a base class for implementing ThingHandlers for Nuki devices
 *
 * @author Jan Vybíral - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractNukiDeviceHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final int JOB_INTERVAL = 60;

    @Nullable
    private NukiHttpClient nukiHttpClient;
    @Nullable
    protected ScheduledFuture<?> reInitJob;
    protected final String nukiId;

    public AbstractNukiDeviceHandler(Thing thing) {
        super(thing);
        String id = thing.getProperties().get(NukiBindingConstants.PROPERTY_NUKI_ID);
        if (id == null) {
            Object idFromOldConfig = getConfig().get(NukiBindingConstants.PROPERTY_NUKI_ID);
            if (idFromOldConfig != null) {
                logger.warn(
                        "SmartLock '{}' was created by old version of binding. It is recommended to delete it and discover again",
                        thing.getUID());
                int nukiId = Integer.parseInt(idFromOldConfig.toString(), 16);
                this.nukiId = Integer.toString(nukiId);
            } else {
                throw new IllegalStateException(String.format(
                        "%s is missing from properties of %s. Delete thing and add it again using discovery",
                        NukiBindingConstants.PROPERTY_NUKI_ID, thing));
            }
        } else {
            nukiId = id;
        }
    }

    @Override
    public void initialize() {
        scheduler.execute(this::initializeHandler);
    }

    @Override
    public void dispose() {
        stopReInitJob();
    }

    private void initializeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            initializeHandler(null, null);
        } else {
            initializeHandler(bridge.getHandler(), bridge.getStatus());
        }
    }

    protected NukiHttpClient getNukiHttpClient() {
        return Objects.requireNonNull(this.nukiHttpClient, "HTTP client is null");
    }

    private void initializeHandler(@Nullable ThingHandler bridgeHandler, @Nullable ThingStatus bridgeStatus) {
        if (bridgeHandler != null && bridgeStatus != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                nukiHttpClient = ((NukiBridgeHandler) bridgeHandler).getNukiHttpClient();
                BridgeListResponse bridgeListResponse = getNukiHttpClient().getList();
                if (handleResponse(bridgeListResponse, null, null)) {
                    BridgeApiListDeviceDto device = bridgeListResponse.getDevice(this.nukiId);
                    if (device == null) {
                        logger.warn("Configured Smart Lock [{}] not present in bridge device list", this.nukiId);
                    } else {
                        updateStatus(ThingStatus.ONLINE);
                        refreshData(device);
                        stopReInitJob();
                    }
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                stopReInitJob();
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            stopReInitJob();
        }
    }

    protected void refreshData(BridgeApiListDeviceDto device) {
        updateProperty(NukiBindingConstants.PROPERTY_NAME, device.getName());
        if (device.getFirmwareVersion() != null) {
            updateProperty(NukiBindingConstants.PROPERTY_FIRMWARE_VERSION, device.getFirmwareVersion());
        }

        if (device.getLastKnownState() != null) {
            refreshState(device.getLastKnownState());
        }
    }

    /**
     * Method to refresh state of this thing. Implementors should read values from state and update corresponding
     * channels.
     * 
     * @param state Current state of this thing as obtained from Bridge API
     */
    public abstract void refreshState(BridgeApiDeviceStateDto state);

    protected <T> void updateState(String channelId, T state, Function<T, State> transform) {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            updateState(channel.getUID(), state, transform);
        }
    }

    protected <T> void updateState(ChannelUID channel, T state, Function<T, State> transform) {
        updateState(channel, state == null ? UnDefType.NULL : transform.apply(state));
    }

    protected State toDateTime(String dateTimeString) {
        try {
            ZonedDateTime date = OffsetDateTime.parse(dateTimeString).atZoneSameInstant(ZoneId.systemDefault());
            return new DateTimeType(date);
        } catch (DateTimeParseException e) {
            logger.debug("Failed to parse date from '{}'", dateTimeString);
            return UnDefType.UNDEF;
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        scheduler.execute(() -> {
            Bridge bridge = getBridge();
            if (bridge == null) {
                initializeHandler(null, bridgeStatusInfo.getStatus());
            } else {
                initializeHandler(bridge.getHandler(), bridgeStatusInfo.getStatus());
            }
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand({}, {})", channelUID, command);

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Thing is not ONLINE; command[{}] for channelUID[{}] is ignored", command, channelUID);
        } else if (command instanceof RefreshType) {
            scheduler.execute(() -> {
                BridgeLockStateResponse bridgeLockStateResponse = getNukiHttpClient().getBridgeLockState(nukiId,
                        getDeviceType());
                if (handleResponse(bridgeLockStateResponse, channelUID.getAsString(), command.toString())) {
                    if (!doHandleRefreshCommand(channelUID, command, bridgeLockStateResponse)) {
                        logger.debug("Command[{}] for channelUID[{}] not implemented!", command, channelUID);
                    }
                }
            });
        } else {
            scheduler.execute(() -> {
                if (!doHandleCommand(channelUID, command)) {
                    logger.debug("Unexpected command[{}] for channelUID[{}]!", command, channelUID);
                }
            });
        }
    }

    /**
     * Get type of this device
     * 
     * @return Device type
     */
    protected abstract int getDeviceType();

    /**
     * Method to handle channel command - will not receive REFRESH command
     * 
     * @param channelUID Channel which received command
     * @param command Command received
     * @return true if command was handled
     */
    protected abstract boolean doHandleCommand(ChannelUID channelUID, Command command);

    /**
     * Method for handlign {@link RefreshType} command
     * 
     * @param channelUID Channel which received command
     * @param command Command received, will always be {@link RefreshType}
     * @param response Response from /lockState endpoint of Bridge API
     * @return true if command was handled
     */
    protected boolean doHandleRefreshCommand(ChannelUID channelUID, Command command, BridgeLockStateResponse response) {
        refreshState(response.getBridgeApiLockStateDto());
        return true;
    }

    protected boolean handleResponse(NukiBaseResponse nukiBaseResponse, @Nullable String channelUID,
            @Nullable String command) {
        if (nukiBaseResponse.getStatus() == 200 && nukiBaseResponse.isSuccess()) {
            logger.debug("Command[{}] succeeded for channelUID[{}] on nukiId[{}]!", command, channelUID, nukiId);
            return true;
        } else if (nukiBaseResponse.getStatus() != 200) {
            logger.debug("Request to Bridge failed! status[{}] - message[{}]", nukiBaseResponse.getStatus(),
                    nukiBaseResponse.getMessage());
        } else if (!nukiBaseResponse.isSuccess()) {
            logger.debug(
                    "Request from Bridge to Smart Lock failed! status[{}] - message[{}] - isSuccess[{}]. Check if Nuki Smart Lock is powered on!",
                    nukiBaseResponse.getStatus(), nukiBaseResponse.getMessage(), nukiBaseResponse.isSuccess());
        }
        logger.debug("Could not handle command[{}] for channelUID[{}] on nukiId[{}]!", command, channelUID, nukiId);

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, nukiBaseResponse.getMessage());

        updateState(NukiBindingConstants.CHANNEL_SMARTLOCK_LOCK, OnOffType.OFF, Function.identity());
        updateState(NukiBindingConstants.CHANNEL_SMARTLOCK_STATE, NukiBindingConstants.LOCK_STATES_UNDEFINED,
                DecimalType::new);
        updateState(NukiBindingConstants.CHANNEL_SMARTLOCK_DOOR_STATE, NukiBindingConstants.DOORSENSOR_STATES_UNKNOWN,
                DecimalType::new);

        withBridgeAsync(bridge -> {
            bridge.checkBridgeOnline();
            startReInitJob();
        });
        return false;
    }

    private void withBridgeAsync(Consumer<NukiBridgeHandler> handler) {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler instanceof NukiBridgeHandler) {
                scheduler.execute(() -> handler.accept((NukiBridgeHandler) bridgeHandler));
            }
        }
    }

    private void startReInitJob() {
        logger.trace("Starting reInitJob with interval of  {}secs for Smart Lock[{}].", JOB_INTERVAL, nukiId);
        if (reInitJob != null) {
            logger.trace("Already started reInitJob for Smart Lock[{}].", nukiId);
            return;
        }
        reInitJob = scheduler.scheduleWithFixedDelay(this::initializeHandler, 1, JOB_INTERVAL, TimeUnit.SECONDS);
    }

    private void stopReInitJob() {
        logger.trace("Stopping reInitJob for Smart Lock[{}].", nukiId);
        if (reInitJob != null) {
            reInitJob.cancel(true);
            logger.trace("Stopped reInitJob for Smart Lock[{}].", nukiId);
        }
        reInitJob = null;
    }
}
