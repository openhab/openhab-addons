/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.nuki.internal.NukiBindingConstants;
import org.openhab.binding.nuki.internal.converter.LockActionConverter;
import org.openhab.binding.nuki.internal.dataexchange.BridgeLockActionResponse;
import org.openhab.binding.nuki.internal.dataexchange.BridgeLockStateResponse;
import org.openhab.binding.nuki.internal.dataexchange.NukiBaseResponse;
import org.openhab.binding.nuki.internal.dataexchange.NukiHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NukiSmartLockHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Katter - Initial contribution
 */
public class NukiSmartLockHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(NukiSmartLockHandler.class);
    private static final int JOB_INTERVAL = 60;

    private NukiHttpClient nukiHttpClient;
    private ScheduledFuture<?> reInitJob;
    private String nukiId;
    private boolean unlatch;

    public NukiSmartLockHandler(Thing thing) {
        super(thing);
        logger.debug("Instantiating NukiSmartLockHandler({})", thing);
    }

    @Override
    public void initialize() {
        logger.debug("initialize() for Smart Lock[{}].", getThing().getUID());
        Configuration config = getConfig();
        nukiId = (String) config.get(NukiBindingConstants.CONFIG_NUKI_ID);
        unlatch = (Boolean) config.get(NukiBindingConstants.CONFIG_UNLATCH);
        if (nukiId == null) {
            logger.debug("NukiSmartLockHandler[{}] is not initializable, nukiId setting is unset in the configuration!",
                    getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "nukiId setting is unset");
        } else {
            scheduler.execute(this::initializeHandler);
        }
    }

    @Override
    public void dispose() {
        logger.debug("dispose() for Smart Lock[{}].", getThing().getUID());
        stopReInitJob();
    }

    private void initializeHandler() {
        logger.debug("initializeHandler() for Smart Lock[{}]", nukiId);
        Bridge bridge = getBridge();
        if (bridge == null) {
            initializeHandler(null, null);
        } else {
            initializeHandler(bridge.getHandler(), bridge.getStatus());
        }
    }

    private void initializeHandler(ThingHandler bridgeHandler, ThingStatus bridgeStatus) {
        if (bridgeHandler != null && bridgeStatus != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                nukiHttpClient = ((NukiBridgeHandler) bridgeHandler).getNukiHttpClient();
                BridgeLockStateResponse bridgeLockStateResponse = nukiHttpClient.getBridgeLockState(nukiId);
                if (handleResponse(bridgeLockStateResponse, null, null)) {
                    updateStatus(ThingStatus.ONLINE);
                    for (Channel channel : thing.getChannels()) {
                        handleCommand(channel.getUID(), RefreshType.REFRESH);
                    }
                    stopReInitJob();
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

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged({}) for Smart Lock[{}].", bridgeStatusInfo, nukiId);
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
        logger.debug("handleCommand({}, {})", channelUID, command);

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Thing is not ONLINE; command[{}] for channelUID[{}] is ignored", command, channelUID);
            return;
        }

        if (command instanceof RefreshType) {
            handleCommandRefreshType(channelUID, command);
            return;
        }

        boolean validCmd = true;
        switch (channelUID.getId()) {
            case NukiBindingConstants.CHANNEL_SMARTLOCK_LOCK:
                if (command instanceof OnOffType) {
                    int lockAction;
                    if (unlatch) {
                        lockAction = (command == OnOffType.OFF ? NukiBindingConstants.LOCK_ACTIONS_UNLATCH
                                : NukiBindingConstants.LOCK_ACTIONS_LOCK);
                    } else {
                        lockAction = (command == OnOffType.OFF ? NukiBindingConstants.LOCK_ACTIONS_UNLOCK
                                : NukiBindingConstants.LOCK_ACTIONS_LOCK);
                    }
                    Channel channelLockState = thing.getChannel(NukiBindingConstants.CHANNEL_SMARTLOCK_STATE);
                    if (channelLockState != null) {
                        updateState(channelLockState.getUID(),
                                new DecimalType(LockActionConverter.getLockStateFor(lockAction)));
                    }
                    BridgeLockActionResponse bridgeLockActionResponse = nukiHttpClient.getBridgeLockAction(nukiId,
                            lockAction);
                    handleResponse(bridgeLockActionResponse, channelUID.getAsString(), command.toString());
                } else {
                    validCmd = false;
                }
                break;
            case NukiBindingConstants.CHANNEL_SMARTLOCK_STATE:
                if (command instanceof DecimalType) {
                    int lockAction;
                    lockAction = ((DecimalType) command).intValue();
                    lockAction = LockActionConverter.getLockActionFor(lockAction);
                    updateState(channelUID, new DecimalType(LockActionConverter.getLockStateFor(lockAction)));
                    BridgeLockActionResponse bridgeLockActionResponse = nukiHttpClient.getBridgeLockAction(nukiId,
                            lockAction);
                    handleResponse(bridgeLockActionResponse, channelUID.getAsString(), command.toString());
                } else {
                    validCmd = false;
                }
                break;
            default:
                validCmd = false;
                break;
        }
        if (!validCmd) {
            logger.debug("Unexpected command[{}] for channelUID[{}]!", command, channelUID);
        }
    }

    private void handleCommandRefreshType(ChannelUID channelUID, Command command) {
        logger.debug("handleCommandRefreshType({}, {})", channelUID, command);
        BridgeLockStateResponse bridgeLockStateResponse;
        switch (channelUID.getId()) {
            case NukiBindingConstants.CHANNEL_SMARTLOCK_LOCK:
                bridgeLockStateResponse = nukiHttpClient.getBridgeLockState(nukiId);
                if (handleResponse(bridgeLockStateResponse, channelUID.getAsString(), command.toString())) {
                    int lockState = bridgeLockStateResponse.getState();
                    State state;
                    if (lockState == NukiBindingConstants.LOCK_STATES_LOCKED) {
                        state = OnOffType.ON;
                    } else if (lockState == NukiBindingConstants.LOCK_STATES_UNLOCKED) {
                        state = OnOffType.OFF;
                    } else {
                        logger.warn(
                                "Smart Lock returned lockState[{}]. Intentionally setting possibly wrong value 'OFF' for channel 'smartlockLock'!",
                                lockState);
                        state = OnOffType.OFF;
                    }
                    updateState(channelUID, state);
                }
                break;
            case NukiBindingConstants.CHANNEL_SMARTLOCK_STATE:
                bridgeLockStateResponse = nukiHttpClient.getBridgeLockState(nukiId);
                if (handleResponse(bridgeLockStateResponse, channelUID.getAsString(), command.toString())) {
                    updateState(channelUID, new DecimalType(bridgeLockStateResponse.getState()));
                }
                break;
            case NukiBindingConstants.CHANNEL_SMARTLOCK_LOW_BATTERY:
                bridgeLockStateResponse = nukiHttpClient.getBridgeLockState(nukiId);
                if (handleResponse(bridgeLockStateResponse, channelUID.getAsString(), command.toString())) {
                    updateState(channelUID, bridgeLockStateResponse.isBatteryCritical() ? OnOffType.ON : OnOffType.OFF);
                }
                break;
            default:
                logger.debug("Command[{}] for channelUID[{}] not implemented!", command, channelUID);
                return;
        }
    }

    private boolean handleResponse(NukiBaseResponse nukiBaseResponse, String channelUID, String command) {
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
        Channel channelLock = thing.getChannel(NukiBindingConstants.CHANNEL_SMARTLOCK_LOCK);
        if (channelLock != null) {
            updateState(channelLock.getUID(), OnOffType.OFF);
        }
        Channel channelLockState = thing.getChannel(NukiBindingConstants.CHANNEL_SMARTLOCK_STATE);
        if (channelLockState != null) {
            updateState(channelLockState.getUID(), new DecimalType(NukiBindingConstants.LOCK_STATES_UNDEFINED));
        }
        startReInitJob();
        return false;
    }

    private void startReInitJob() {
        logger.trace("Starting reInitJob with interval of  {}secs for Smart Lock[{}].", JOB_INTERVAL, nukiId);
        if (reInitJob != null) {
            logger.trace("Already started reInitJob for Smart Lock[{}].", nukiId);
            return;
        }
        reInitJob = scheduler.scheduleWithFixedDelay(this::initializeHandler, JOB_INTERVAL, JOB_INTERVAL,
                TimeUnit.SECONDS);
    }

    private void stopReInitJob() {
        logger.trace("Stopping reInitJob for Smart Lock[{}].", nukiId);
        if (reInitJob != null && !reInitJob.isCancelled()) {
            logger.trace("Stopped reInitJob for Smart Lock[{}].", nukiId);
            reInitJob.cancel(true);
        }
        reInitJob = null;
    }

    public void handleApiServletUpdate(ChannelUID channelUID, State newState) {
        logger.trace("handleApiServletUpdate({}, {})", channelUID, newState);
        updateState(channelUID, newState);
    }
}
