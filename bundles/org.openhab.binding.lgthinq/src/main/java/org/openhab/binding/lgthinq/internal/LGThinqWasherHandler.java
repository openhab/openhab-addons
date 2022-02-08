/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.internal;

import static org.openhab.binding.lgthinq.internal.LGThinqBindingConstants.*;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqDeviceV1MonitorExpiredException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqDeviceV1OfflineException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.LGThinqApiClientService;
import org.openhab.binding.lgthinq.lgservices.LGThinqApiV1ClientServiceImpl;
import org.openhab.binding.lgthinq.lgservices.LGThinqApiV2ClientServiceImpl;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.LGDevice;
import org.openhab.binding.lgthinq.lgservices.model.washer.WMCapability;
import org.openhab.binding.lgthinq.lgservices.model.washer.WMSnapshot;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGThinqWasherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinqWasherHandler extends LGThinqDeviceThing {

    private final LGThinqDeviceDynStateDescriptionProvider stateDescriptionProvider;
    @Nullable
    private WMCapability wmCapability;
    private final String lgPlatfomType;
    private final Logger logger = LoggerFactory.getLogger(LGThinqWasherHandler.class);
    @NonNullByDefault
    private final LGThinqApiClientService lgThinqApiClientService;
    private ThingStatus lastThingStatus = ThingStatus.UNKNOWN;
    // Bridges status that this thing must top scanning for state change
    private static final Set<ThingStatusDetail> BRIDGE_STATUS_DETAIL_ERROR = Set.of(ThingStatusDetail.BRIDGE_OFFLINE,
            ThingStatusDetail.BRIDGE_UNINITIALIZED, ThingStatusDetail.COMMUNICATION_ERROR,
            ThingStatusDetail.CONFIGURATION_ERROR);
    private @Nullable ScheduledFuture<?> thingStatePollingJob;
    private @Nullable Future<?> commandExecutorQueueJob;
    // *** Long running isolated threadpools.
    private final ScheduledExecutorService pollingScheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    private boolean monitorV1Began = false;
    private String monitorWorkId = "";
    private final LinkedBlockingQueue<AsyncCommandParams> commandBlockQueue = new LinkedBlockingQueue<>(20);
    @NonNullByDefault
    private String bridgeId = "";

    public LGThinqWasherHandler(Thing thing, LGThinqDeviceDynStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        lgPlatfomType = "" + thing.getProperties().get(PLATFORM_TYPE);
        lgThinqApiClientService = lgPlatfomType.equals(PLATFORM_TYPE_V1) ? LGThinqApiV1ClientServiceImpl.getInstance()
                : LGThinqApiV2ClientServiceImpl.getInstance();
    }

    static class AsyncCommandParams {
        final String channelUID;
        final Command command;

        public AsyncCommandParams(String channelUUID, Command command) {
            this.channelUID = channelUUID;
            this.command = command;
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return super.getServices();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Thinq thing.");
        Bridge bridge = getBridge();
        initializeThing((bridge == null) ? null : bridge.getStatus());
    }

    @Override
    protected void startCommandExecutorQueueJob() {
        if (commandExecutorQueueJob == null || commandExecutorQueueJob.isDone()) {
            commandExecutorQueueJob = getExecutorService().submit(queuedCommandExecutor);
        }
    }

    private ExecutorService getExecutorService() {
        return executorService;
    }

    private void stopCommandExecutorQueueJob() {
        if (commandExecutorQueueJob != null) {
            commandExecutorQueueJob.cancel(true);
        }
    }

    protected void startThingStatePolling() {
        if (thingStatePollingJob == null || thingStatePollingJob.isDone()) {
            thingStatePollingJob = getLocalScheduler().scheduleWithFixedDelay(this::updateThingStateFromLG, 10,
                    DEFAULT_STATE_POLLING_UPDATE_DELAY, TimeUnit.SECONDS);
        }
    }

    private void updateThingStateFromLG() {
        try {
            WMSnapshot shot = getSnapshotDeviceAdapter(getDeviceId());
            if (shot == null) {
                // no data to update. Maybe, the monitor stopped, then it gonna be restarted next try.
                return;
            }
            if (!shot.isOnline()) {
                if (getThing().getStatus() != ThingStatus.OFFLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE);
                    updateState(CHANNEL_POWER_ID,
                            OnOffType.from(shot.getPowerStatus() == DevicePowerState.DV_POWER_OFF));
                }
                return;
            }

            updateState(CHANNEL_POWER_ID, OnOffType.from(shot.getPowerStatus() == DevicePowerState.DV_POWER_ON));

            updateStatus(ThingStatus.ONLINE);
        } catch (LGThinqException e) {
            logger.error("Error updating thing {}/{} from LG API. Thing goes OFFLINE until next retry.",
                    getDeviceAlias(), getDeviceId(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private ScheduledExecutorService getLocalScheduler() {
        return pollingScheduler;
    }

    private String getBridgeId() {
        if (bridgeId.isBlank() && getBridge() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            logger.error("Configuration error um Thinq Thing - No Bridge defined for the thing.");
            return "UNKNOWN";
        } else if (bridgeId.isBlank() && getBridge() != null) {
            bridgeId = getBridge().getUID().getId();
        }
        return bridgeId;
    }

    private void forceStopDeviceV1Monitor(String deviceId) {
        try {
            monitorV1Began = false;
            lgThinqApiClientService.stopMonitor(getBridgeId(), deviceId, monitorWorkId);
        } catch (Exception e) {
            logger.error("Error stopping LG Device monitor", e);
        }
    }

    @NonNull
    private String emptyIfNull(@Nullable String value) {
        return value == null ? "" : "" + value;
    }

    @Override
    public void updateChannelDynStateDescription() throws LGThinqApiException {
        // not dynamic state channel in this device
    }

    @Override
    public WMCapability getCapabilities() throws LGThinqApiException {
        if (wmCapability == null) {
            wmCapability = (WMCapability) lgThinqApiClientService.getCapability(getDeviceId(), getDeviceUriJsonConfig(),
                    false);
        }
        return Objects.requireNonNull(wmCapability, "Unexpected error. Return ac-capability shouldn't ever be null");
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Nullable
    private WMSnapshot getSnapshotDeviceAdapter(String deviceId) throws LGThinqApiException {
        // analise de platform version
        if (PLATFORM_TYPE_V2.equals(lgPlatfomType)) {
            return (WMSnapshot) lgThinqApiClientService.getDeviceData(getBridgeId(), getDeviceId());
        } else {
            try {
                if (!monitorV1Began) {
                    monitorWorkId = lgThinqApiClientService.startMonitor(getBridgeId(), getDeviceId());
                    monitorV1Began = true;
                }
            } catch (LGThinqDeviceV1OfflineException e) {
                forceStopDeviceV1Monitor(deviceId);
                WMSnapshot shot = new WMSnapshot();
                shot.setOnline(false);
                return shot;
            } catch (Exception e) {
                forceStopDeviceV1Monitor(deviceId);
                throw new LGThinqApiException("Error starting device monitor in LG API for the device:" + deviceId, e);
            }
            int retries = 10;
            WMSnapshot shot;
            while (retries > 0) {
                // try to get monitoring data result 3 times.
                try {
                    shot = (WMSnapshot) lgThinqApiClientService.getMonitorData(getBridgeId(), deviceId, monitorWorkId);
                    if (shot != null) {
                        return shot;
                    }
                    Thread.sleep(500);
                    retries--;
                } catch (LGThinqDeviceV1MonitorExpiredException e) {
                    forceStopDeviceV1Monitor(deviceId);
                    logger.info("Monitor for device {} was expired. Forcing stop and start to next cycle.", deviceId);
                    return null;
                } catch (Exception e) {
                    // If it can't get monitor handler, then stop monitor and restart the process again in new
                    // interaction
                    // Force restart monitoring because of the errors returned (just in case)
                    forceStopDeviceV1Monitor(deviceId);
                    throw new LGThinqApiException("Error getting monitor data for the device:" + deviceId, e);
                }
            }
            forceStopDeviceV1Monitor(deviceId);
            throw new LGThinqApiException("Exhausted trying to get monitor data for the device:" + deviceId);
        }
    }

    protected void stopThingStatePolling() {
        if (thingStatePollingJob != null && !thingStatePollingJob.isDone()) {
            logger.debug("Stopping LG thinq polling for device/alias: {}/{}", getDeviceId(), getDeviceAlias());
            thingStatePollingJob.cancel(true);
        }
    }

    private void handleStatusChanged(ThingStatus newStatus, ThingStatusDetail statusDetail) {
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
    public void onDeviceAdded(LGDevice device) {
        // TODO - handle it. Think if it's needed
    }

    @Override
    public String getDeviceId() {
        return getThing().getUID().getId();
    }

    @Override
    public String getDeviceAlias() {
        return emptyIfNull(getThing().getProperties().get(DEVICE_ALIAS));
    }

    @Override
    public String getDeviceModelName() {
        return emptyIfNull(getThing().getProperties().get(MODEL_NAME));
    }

    @Override
    public String getDeviceUriJsonConfig() {
        return emptyIfNull(getThing().getProperties().get(MODEL_URL_INFO));
    }

    @Override
    public boolean onDeviceStateChanged() {
        // TODO - HANDLE IT, Think if it's needed
        return false;
    }

    @Override
    public void onDeviceRemoved() {
        // TODO - HANDLE IT, Think if it's needed
    }

    @Override
    public void onDeviceGone() {
        // TODO - HANDLE IT, Think if it's needed
    }

    @Override
    public void dispose() {
        if (thingStatePollingJob != null) {
            thingStatePollingJob.cancel(true);
            stopThingStatePolling();
            stopCommandExecutorQueueJob();
            thingStatePollingJob = null;
        }
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
                logger.error(
                        "Device's command queue reached the size limit. Probably the device is busy ou stuck. Ignoring command.");
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Device Command Queue is Busy");
            }

        }
    }

    private final Runnable queuedCommandExecutor = new Runnable() {
        @Override
        public void run() {
            while (true) {
                AsyncCommandParams params;
                try {
                    params = commandBlockQueue.take();
                } catch (InterruptedException e) {
                    logger.debug("Interrupting async command queue executor.");
                    return;
                }
                Command command = params.command;

                try {
                    switch (params.channelUID) {
                        case CHANNEL_POWER_ID: {
                            if (command instanceof OnOffType) {
                                lgThinqApiClientService.turnDevicePower(getBridgeId(), getDeviceId(),
                                        command == OnOffType.ON ? DevicePowerState.DV_POWER_ON
                                                : DevicePowerState.DV_POWER_OFF);
                            } else {
                                logger.warn("Received command different of OnOffType in Power Channel. Ignoring");
                            }
                            break;
                        }
                        default: {
                            logger.error("Command {} to the channel {} not supported. Ignored.", command,
                                    params.channelUID);
                        }
                    }
                } catch (LGThinqException e) {
                    logger.error("Error executing Command {} to the channel {}. Thing goes offline until retry",
                            command, params.channelUID, e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            }
        }
    };
}
