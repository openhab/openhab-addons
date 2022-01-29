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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqDeviceV1MonitorExpiredException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqDeviceV1OfflineException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqException;
import org.openhab.binding.lgthinq.internal.handler.LGThinqBridgeHandler;
import org.openhab.binding.lgthinq.lgapi.LGThinqApiClientService;
import org.openhab.binding.lgthinq.lgapi.LGThinqApiV1ClientServiceImpl;
import org.openhab.binding.lgthinq.lgapi.LGThinqApiV2ClientServiceImpl;
import org.openhab.binding.lgthinq.lgapi.model.*;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

import static org.openhab.binding.lgthinq.internal.LGThinqBindingConstants.*;
import static org.openhab.core.library.types.OnOffType.ON;

/**
 * The {@link LGThinqAirConditionerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinqAirConditionerHandler extends BaseThingHandler implements LGThinqDeviceThing {

    private final LGThinqDeviceDynStateDescriptionProvider stateDescriptionProvider;
    private final ChannelUID opModeChannelUID;
    private final ChannelUID opModeFanSpeedUID;
    @Nullable
    private ACCapability acCapability;
    private final String lgPlatfomType;
    private final Logger logger = LoggerFactory.getLogger(LGThinqAirConditionerHandler.class);
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

    public LGThinqAirConditionerHandler(Thing thing,
            LGThinqDeviceDynStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        lgPlatfomType = "" + thing.getProperties().get(PLATFORM_TYPE);
        lgThinqApiClientService = lgPlatfomType.equals(PLATFORM_TYPE_V1) ? LGThinqApiV1ClientServiceImpl.getInstance()
                : LGThinqApiV2ClientServiceImpl.getInstance();
        opModeChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_MOD_OP_ID);
        opModeFanSpeedUID = new ChannelUID(getThing().getUID(), CHANNEL_FAN_SPEED_ID);
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
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {}", bridgeStatusInfo);
        initializeThing(bridgeStatusInfo.getStatus());
    }

    private void initializeThing(@Nullable ThingStatus bridgeStatus) {
        logger.debug("initializeThing LQ Thinq {}. Bridge status {}", getThing().getUID(), bridgeStatus);
        String deviceId = getThing().getUID().getId();
        if (!SUPPORTED_LG_PLATFORMS.contains(lgPlatfomType)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "LG Platform [" + lgPlatfomType + "] not supported for this thing");
            return;
        }
        Bridge bridge = getBridge();
        if (!deviceId.isBlank()) {
            try {
                updateChannelDynStateDescription();
            } catch (LGThinqApiException e) {
                logger.error(
                        "Error updating channels dynamic options descriptions based on capabilities of the device. Fallback to default values.");
            }
            if (bridge != null) {
                LGThinqBridgeHandler handler = (LGThinqBridgeHandler) bridge.getHandler();
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
        // finally, start command queue, regardless os the thing state, as we can still try to send commands without
        // property ONLINE (the successful result from command request can put the thing in ONLINE status).
        startCommandExecutorQueueJob();
    }

    private void startCommandExecutorQueueJob() {
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
            ACSnapShot shot = getSnapshotDeviceAdapter(getDeviceId());
            if (shot == null) {
                // no data to update. Maybe, the monitor stopped, then it gonna be restarted next try.
                return;
            }
            if (!shot.isOnline()) {
                if (getThing().getStatus() != ThingStatus.OFFLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE);
                    updateState(CHANNEL_POWER_ID,
                            OnOffType.from(shot.getAcPowerStatus() == DevicePowerState.DV_POWER_OFF));
                }
                return;
            }
            if (shot.getOperationMode() != null) {
                updateState(CHANNEL_MOD_OP_ID, new DecimalType(shot.getOperationMode()));
            }
            if (shot.getAcPowerStatus() != null) {
                updateState(CHANNEL_POWER_ID, OnOffType.from(shot.getAcPowerStatus() == DevicePowerState.DV_POWER_ON));
                // TODO - validate if is needed to change the status of the thing from OFFLINE to ONLINE (as
                // soon as LG WebOs do)
            }
            if (shot.getAcFanSpeed() != null) {
                updateState(CHANNEL_FAN_SPEED_ID, new DecimalType(shot.getAirWindStrength()));
            }
            if (shot.getCurrentTemperature() != null) {
                updateState(CHANNEL_CURRENT_TEMP_ID, new DecimalType(shot.getCurrentTemperature()));
            }
            if (shot.getTargetTemperature() != null) {
                updateState(CHANNEL_TARGET_TEMP_ID, new DecimalType(shot.getTargetTemperature()));
            }
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
        ACCapability acCap = getAcCapabilities();
        if (isLinked(opModeChannelUID)) {
            List<StateOption> options = new ArrayList<>();
            acCap.getSupportedOpMode().forEach((v) -> options
                    .add(new StateOption(emptyIfNull(acCap.getOpMod().get(v)), emptyIfNull(CAP_OP_MODE.get(v)))));
            stateDescriptionProvider.setStateOptions(opModeChannelUID, options);
        }
        if (isLinked(opModeFanSpeedUID)) {
            List<StateOption> options = new ArrayList<>();
            acCap.getSupportedFanSpeed().forEach((v) -> options
                    .add(new StateOption(emptyIfNull(acCap.getFanSpeed().get(v)), emptyIfNull(CAP_FAN_SPEED.get(v)))));
            stateDescriptionProvider.setStateOptions(opModeFanSpeedUID, options);
        }
    }

    @Override
    public ACCapability getAcCapabilities() throws LGThinqApiException {
        if (acCapability == null) {
            acCapability = lgThinqApiClientService.getDeviceCapability(getDeviceId(), getDeviceUriJsonConfig(), false);
        }
        return Objects.requireNonNull(acCapability, "Unexpected error. Return ac-capability shouldn't ever be null");
    }

    @Nullable
    private ACSnapShot getSnapshotDeviceAdapter(String deviceId) throws LGThinqApiException {
        // analise de platform version
        if (PLATFORM_TYPE_V2.equals(lgPlatfomType)) {
            return lgThinqApiClientService.getAcDeviceData(getBridgeId(), getDeviceId());
        } else {
            try {
                if (!monitorV1Began) {
                    monitorWorkId = lgThinqApiClientService.startMonitor(getBridgeId(), getDeviceId());
                    monitorV1Began = true;
                }
            } catch (LGThinqDeviceV1OfflineException e) {
                forceStopDeviceV1Monitor(deviceId);
                ACSnapShot shot = new ACSnapShotV1();
                shot.setOnline(false);
                return shot;
            } catch (Exception e) {
                forceStopDeviceV1Monitor(deviceId);
                throw new LGThinqApiException("Error starting device monitor in LG API for the device:" + deviceId, e);
            }
            int retries = 10;
            ACSnapShot shot;
            while (retries > 0) {
                // try to get monitoring data result 3 times.
                try {
                    shot = lgThinqApiClientService.getMonitorData(getBridgeId(), deviceId, monitorWorkId);
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
                        case CHANNEL_MOD_OP_ID: {
                            if (params.command instanceof DecimalType) {
                                lgThinqApiClientService.changeOperationMode(getBridgeId(), getDeviceId(),
                                        ((DecimalType) command).intValue());
                            } else {
                                logger.warn("Received command different of Numeric in Mod Operation. Ignoring");
                            }
                            break;
                        }
                        case CHANNEL_FAN_SPEED_ID: {
                            if (command instanceof DecimalType) {
                                lgThinqApiClientService.changeFanSpeed(getBridgeId(), getDeviceId(),
                                        ((DecimalType) command).intValue());
                            } else {
                                logger.warn("Received command different of Numeric in FanSpeed Channel. Ignoring");
                            }
                            break;
                        }
                        case CHANNEL_POWER_ID: {
                            if (command instanceof OnOffType) {
                                lgThinqApiClientService.turnDevicePower(getBridgeId(), getDeviceId(),
                                        command == ON ? DevicePowerState.DV_POWER_ON : DevicePowerState.DV_POWER_OFF);
                            } else {
                                logger.warn("Received command different of OnOffType in Power Channel. Ignoring");
                            }
                            break;
                        }
                        case CHANNEL_TARGET_TEMP_ID: {
                            double targetTemp;
                            if (command instanceof DecimalType) {
                                targetTemp = ((DecimalType) command).doubleValue();
                            } else if (command instanceof QuantityType) {
                                targetTemp = ((QuantityType<?>) command).doubleValue();
                            } else {
                                logger.warn("Received command different of Numeric in TargetTemp Channel. Ignoring");
                                break;
                            }
                            lgThinqApiClientService.changeTargetTemperature(getBridgeId(), getDeviceId(),
                                    ACTargetTmp.statusOf(targetTemp));
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
