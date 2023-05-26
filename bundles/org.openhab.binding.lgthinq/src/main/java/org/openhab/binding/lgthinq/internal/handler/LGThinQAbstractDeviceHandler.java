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
package org.openhab.binding.lgthinq.internal.handler;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.LGThinQDeviceDynStateDescriptionProvider;
import org.openhab.binding.lgthinq.internal.errors.*;
import org.openhab.binding.lgthinq.internal.type.ThinqChannelGroupTypeProvider;
import org.openhab.binding.lgthinq.internal.type.ThinqChannelTypeProvider;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientService;
import org.openhab.binding.lgthinq.lgservices.model.*;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.*;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGThinQAbstractDeviceHandler} is a main interface contract for all LG Thinq things
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class LGThinQAbstractDeviceHandler<C extends CapabilityDefinition, S extends SnapshotDefinition>
        extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(LGThinQAbstractDeviceHandler.class);
    protected final String lgPlatformType;
    private final Class<S> snapshotClass;
    @Nullable
    protected C thinQCapability;
    private @Nullable Future<?> commandExecutorQueueJob;
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private @Nullable ScheduledFuture<?> thingStatePollingJob;
    private final ScheduledExecutorService pollingScheduler = Executors.newScheduledThreadPool(1);
    private Integer fetchMonitorRetries = 0;
    private boolean monitorV1Began = false;
    private String monitorWorkId = "";
    protected final LinkedBlockingQueue<AsyncCommandParams> commandBlockQueue = new LinkedBlockingQueue<>(30);
    private String bridgeId = "";
    private ThingStatus lastThingStatus = ThingStatus.UNKNOWN;
    // Bridges status that this thing must top scanning for state change
    private static final Set<ThingStatusDetail> BRIDGE_STATUS_DETAIL_ERROR = Set.of(ThingStatusDetail.BRIDGE_OFFLINE,
            ThingStatusDetail.BRIDGE_UNINITIALIZED, ThingStatusDetail.COMMUNICATION_ERROR,
            ThingStatusDetail.CONFIGURATION_ERROR);

    protected final LGThinQDeviceDynStateDescriptionProvider stateDescriptionProvider;
    @Nullable
    protected ThinqChannelTypeProvider thinqChannelProvider;
    @Nullable
    protected ThinqChannelGroupTypeProvider thinqChannelGroupProvider;

    public LGThinQAbstractDeviceHandler(Thing thing,
            LGThinQDeviceDynStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        normalizeConfigurationsAndProperties();
        lgPlatformType = "" + thing.getProperties().get(PLATFORM_TYPE);
        this.snapshotClass = (Class<S>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[1];
    }

    private void normalizeConfigurationsAndProperties() {
        List.of(PLATFORM_TYPE, MODEL_URL_INFO, DEVICE_ID).forEach(p -> {
            if (!thing.getProperties().containsKey(p)) {
                thing.setProperty(p, (String) thing.getConfiguration().get(p));
            }
        });
    }

    protected static class AsyncCommandParams {
        final String channelUID;
        final Command command;

        public AsyncCommandParams(String channelUUID, Command command) {
            this.channelUID = channelUUID;
            this.command = command;
        }
    }

    /**
     * Return empty string if null argument is passed
     *
     * @param value value to test
     * @return empty string if null argument is passed
     */
    protected final String emptyIfNull(@Nullable String value) {
        return Objects.requireNonNullElse(value, "");
    }

    /**
     * Return the key informed if there is no correpondent value in map for that key.
     *
     * @param map map with key/value
     * @param key key to search for a value into map
     * @return return value related to that key in the map, or the own key if there is no correspondent.
     */
    protected final String keyIfValueNotFound(Map<String, String> map, @NonNull String key) {
        return Objects.requireNonNullElse(map.get(key), key);
    }

    protected void startCommandExecutorQueueJob() {
        if (commandExecutorQueueJob == null || commandExecutorQueueJob.isDone()) {
            commandExecutorQueueJob = getExecutorService().submit(getQueuedCommandExecutor());
        }
    }

    protected void stopCommandExecutorQueueJob() {
        if (commandExecutorQueueJob != null) {
            commandExecutorQueueJob.cancel(true);
        }
    }

    protected void handleStatusChanged(ThingStatus newStatus, ThingStatusDetail statusDetail) {
        if (lastThingStatus != ThingStatus.ONLINE && newStatus == ThingStatus.ONLINE) {
            // start the thing polling
            startThingStatePolling();
        } else if (lastThingStatus == ThingStatus.ONLINE && newStatus == ThingStatus.OFFLINE
                && BRIDGE_STATUS_DETAIL_ERROR.contains(statusDetail)) {
            // comunication error is not a specific Bridge error, then we must analise it to give
            // this thinq the change to recovery from communication errors
            if (statusDetail != ThingStatusDetail.COMMUNICATION_ERROR
                    || (getBridge() != null && getBridge().getStatus() != ThingStatus.ONLINE)) {
                // in case of status offline, I only stop the polling if is not an COMMUNICATION_ERROR or if
                // the bridge is out
                stopThingStatePolling();
            }

        }
        lastThingStatus = newStatus;
    }

    @Override
    protected void updateStatus(ThingStatus newStatus, ThingStatusDetail statusDetail, @Nullable String description) {
        handleStatusChanged(newStatus, statusDetail);
        super.updateStatus(newStatus, statusDetail, description);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateThingStateFromLG();
        } else {
            AsyncCommandParams params = new AsyncCommandParams(channelUID.getId(), command);
            try {
                // Ensure commands are send in a pipe per device.
                commandBlockQueue.add(params);
            } catch (IllegalStateException ex) {
                getLogger().error(
                        "Device's command queue reached the size limit. Probably the device is busy ou stuck. Ignoring command.");
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Device Command Queue is Busy");
            }

        }
    }

    protected ExecutorService getExecutorService() {
        return executorService;
    }

    public abstract void onDeviceAdded(@NonNullByDefault LGDevice device);

    public String getDeviceId() {
        return Objects.requireNonNullElse(getThing().getProperties().get(DEVICE_ID), "undef");
    }

    public abstract String getDeviceAlias();

    public abstract String getDeviceUriJsonConfig();

    public abstract void onDeviceRemoved();

    public abstract void onDeviceDisconnected();

    public abstract void updateChannelDynStateDescription() throws LGThinqApiException;

    public abstract LGThinQApiClientService<C, S> getLgThinQAPIClientService();

    protected void createDynSwitchChannel(String channelName, ChannelUID chanelUuid) {
        if (getCallback() == null) {
            logger.error("Unexpected behaviour. Callback not ready! Can't create dynamic channels");
        } else {
            // dynamic create channel
            ChannelBuilder builder = getCallback().createChannelBuilder(chanelUuid,
                    new ChannelTypeUID(BINDING_ID, channelName));
            Channel channel = builder.withKind(ChannelKind.STATE).withAcceptedItemType("Switch").build();
            updateThing(editThing().withChannel(channel).build());
        }
    }

    public C getCapabilities() throws LGThinqApiException {
        if (thinQCapability == null) {
            thinQCapability = getLgThinQAPIClientService().getCapability(getDeviceId(), getDeviceUriJsonConfig(),
                    false);
        }
        return Objects.requireNonNull(thinQCapability, "Unexpected error. Return of capability shouldn't ever be null");
    }

    protected abstract Logger getLogger();

    protected void initializeThing(@Nullable ThingStatus bridgeStatus) {
        getLogger().debug("initializeThing LQ Thinq {}. Bridge status {}", getThing().getUID(), bridgeStatus);
        String thingId = getThing().getUID().getId();
        Bridge bridge = getBridge();

        if (!thingId.isBlank()) {
            try {
                updateChannelDynStateDescription();
            } catch (LGThinqApiException e) {
                getLogger().error(
                        "Error updating channels dynamic options descriptions based on capabilities of the device. Fallback to default values.",
                        e);
            }
            if (bridge != null) {
                bridgeId = bridge.getUID().getId();
                LGThinQBridgeHandler handler = (LGThinQBridgeHandler) bridge.getHandler();
                // registry this thing to the bridge
                if (handler == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
                } else {
                    handler.registryListenerThing(this);
                    if (bridgeStatus == ThingStatus.ONLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                    }
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-device-id");
        }
        // finally, start command queue, regardless of the thing state, as we can still try to send commands without
        // property ONLINE (the successful result from command request can put the thing in ONLINE status).
        startCommandExecutorQueueJob();
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            try {
                getLgThinQAPIClientService().initializeDevice(bridgeId, getDeviceId());
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.error(
                            "Error initializing device {} from bridge {}. Is the device support pre-condition setup ?",
                            thingId, bridgeId, e);
                } else {
                    logger.error(
                            "Error initializing device {} from bridge {}. Is the device support pre-condition setup ?",
                            getDeviceId(), bridgeId);
                }
            }
            // force start state pooling if the device is ONLINE
            startThingStatePolling();
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        getLogger().debug("bridgeStatusChanged {}", bridgeStatusInfo);
        super.bridgeStatusChanged(bridgeStatusInfo);
        // restart scheduler
        initializeThing(bridgeStatusInfo.getStatus());
    }

    private class UpdateThingStateFromLG implements Runnable {
        @Override
        public void run() {
            updateThingStateFromLG();
        }
    }

    protected void updateThingStateFromLG() {
        try {
            @Nullable
            S shot = getSnapshotDeviceAdapter(getDeviceId(), getCapabilities());
            if (shot == null) {
                // no data to update. Maybe, the monitor stopped, then it'a going to be restarted next try.
                return;
            }
            fetchMonitorRetries = 0;
            if (!shot.isOnline()) {
                if (getThing().getStatus() != ThingStatus.OFFLINE) {
                    // only update channels if the device has just gone OFFLINE.
                    updateDeviceChannels(shot);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.device-disconnected");
                    onDeviceDisconnected();
                }
            } else {
                // do not update channels if the device is offline
                updateDeviceChannels(shot);
                if (getThing().getStatus() != ThingStatus.ONLINE)
                    updateStatus(ThingStatus.ONLINE);
            }

        } catch (LGThinqApiExhaustionException e) {
            fetchMonitorRetries++;
            getLogger().warn("LG API returns null monitoring data for the thing {}/{}. No data available yet ?",
                    getDeviceAlias(), getDeviceId());
            if (fetchMonitorRetries > MAX_GET_MONITOR_RETRIES) {
                getLogger().error(
                        "The thing {}/{} reach maximum retries for monitor data. Thing goes OFFLINE until next retry.",
                        getDeviceAlias(), getDeviceId(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        } catch (LGThinqException e) {
            getLogger().error("Error updating thing {}/{} from LG API. Thing goes OFFLINE until next retry.",
                    getDeviceAlias(), getDeviceId(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (Throwable e) {
            getLogger().error(
                    "System error in pooling thread (UpdateDevice) for device {}/{}. Filtering to do not stop the thread",
                    getDeviceAlias(), getDeviceId(), e);
        }
    }

    protected abstract void updateDeviceChannels(S snapshot);

    protected String translateFeatureToItemType(FeatureDataType dataType) {
        switch (dataType) {
            case UNDEF:
            case ENUM:
                return "String";
            case RANGE:
                return "Dimmer";
            case BOOLEAN:
                return "Switch";
            default:
                throw new IllegalStateException(
                        String.format("Feature DataType %s not supported for this ThingHandler", dataType));
        }
    }

    protected void stopThingStatePolling() {
        if (thingStatePollingJob != null && !thingStatePollingJob.isDone()) {
            getLogger().debug("Stopping LG thinq polling for device/alias: {}/{}", getDeviceId(), getDeviceAlias());
            thingStatePollingJob.cancel(true);
        }
    }

    protected void startThingStatePolling() {
        if (thingStatePollingJob == null || thingStatePollingJob.isDone()) {
            thingStatePollingJob = pollingScheduler.scheduleWithFixedDelay(new UpdateThingStateFromLG(), 10,
                    DEFAULT_STATE_POLLING_UPDATE_DELAY, TimeUnit.SECONDS);
        }
    }

    private void stopDeviceV1Monitor(String deviceId) {
        try {
            monitorV1Began = false;
            getLgThinQAPIClientService().stopMonitor(getBridgeId(), deviceId, monitorWorkId);
        } catch (LGThinqDeviceV1OfflineException e) {
            getLogger().debug("Monitor stopped. Device is unavailable/disconnected", e);
        } catch (Exception e) {
            getLogger().error("Error stopping LG Device monitor", e);
        }
    }

    protected String getBridgeId() {
        if (bridgeId.isBlank() && getBridge() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            getLogger().error("Configuration error um Thinq Thing - No Bridge defined for the thing.");
            return "UNKNOWN";
        } else if (bridgeId.isBlank() && getBridge() != null) {
            bridgeId = getBridge().getUID().getId();
        }
        return bridgeId;
    }

    abstract protected DeviceTypes getDeviceType();

    @Nullable
    protected S getSnapshotDeviceAdapter(String deviceId, CapabilityDefinition capDef)
            throws LGThinqApiException, LGThinqApiExhaustionException {
        // analise de platform version
        if (PLATFORM_TYPE_V2.equals(lgPlatformType)) {
            return getLgThinQAPIClientService().getDeviceData(getBridgeId(), getDeviceId(), getCapabilities());
        } else {
            try {
                if (!monitorV1Began) {
                    monitorWorkId = getLgThinQAPIClientService().startMonitor(getBridgeId(), getDeviceId());
                    monitorV1Began = true;
                }
            } catch (LGThinqDeviceV1OfflineException e) {
                stopDeviceV1Monitor(deviceId);
                try {
                    S shot = snapshotClass.getDeclaredConstructor().newInstance();
                    shot.setOnline(false);
                    return shot;
                } catch (Exception ex) {
                    getLogger().error("Unexpected error. Can't find default constructor for the Snapshot subclass", ex);
                    return null;
                }

            } catch (Exception e) {
                stopDeviceV1Monitor(deviceId);
                throw new LGThinqApiException("Error starting device monitor in LG API for the device:" + deviceId, e);
            }
            int retries = 10;
            @Nullable
            S shot;
            try {
                while (retries > 0) {
                    // try to get monitoring data result 3 times.

                    shot = getLgThinQAPIClientService().getMonitorData(getBridgeId(), deviceId, monitorWorkId,
                            getDeviceType(), getCapabilities());
                    if (shot != null) {
                        return shot;
                    }
                    Thread.sleep(500);
                    retries--;

                }
            } catch (LGThinqDeviceV1MonitorExpiredException | LGThinqUnmarshallException e) {
                getLogger().debug("Monitor for device {} is invalid. Forcing stop and start to next cycle.", deviceId);
                return null;
            } catch (Exception e) {
                // If it can't get monitor handler, then stop monitor and restart the process again in new
                // interaction
                // Force restart monitoring because of the errors returned (just in case)
                throw new LGThinqApiException("Error getting monitor data for the device:" + deviceId, e);
            } finally {
                stopDeviceV1Monitor(deviceId);
            }
            throw new LGThinqApiExhaustionException("Exhausted trying to get monitor data for the device:" + deviceId);
        }
    }

    protected abstract void processCommand(AsyncCommandParams params) throws LGThinqApiException;

    protected Runnable getQueuedCommandExecutor() {
        return queuedCommandExecutor;
    }

    private final Runnable queuedCommandExecutor = () -> {
        while (true) {
            AsyncCommandParams params;
            try {
                params = commandBlockQueue.take();
            } catch (InterruptedException e) {
                getLogger().debug("Interrupting async command queue executor.");
                return;
            }

            try {
                processCommand(params);
            } catch (LGThinqException e) {
                getLogger().error("Error executing Command {} to the channel {}. Thing goes offline until retry",
                        params.command, params.channelUID, e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    };

    @Override
    public void dispose() {
        logger.debug("Disposing Thinq Thing {}", getDeviceId());
        if (thingStatePollingJob != null) {
            thingStatePollingJob.cancel(true);
            stopThingStatePolling();
            stopCommandExecutorQueueJob();
            thingStatePollingJob = null;
        }
    }

    protected Channel createDynChannel(String channelName, ChannelUID chanelUuid, String itemType) {
        if (getCallback() == null) {
            throw new IllegalStateException("Unexpected behaviour. Callback not ready! Can't create dynamic channels");
        } else {
            // dynamic create channel
            ChannelBuilder builder = getCallback().createChannelBuilder(chanelUuid,
                    new ChannelTypeUID(BINDING_ID, channelName));
            Channel channel = builder.withKind(ChannelKind.STATE).withAcceptedItemType(itemType).build();
            updateThing(editThing().withChannel(channel).build());
            return channel;
        }
    }
}
