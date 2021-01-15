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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.nuki.internal.NukiBindingConstants;
import org.openhab.binding.nuki.internal.converter.OpenerActionConverter;
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
 * @author Alexander Koch - Add Nuki Opener Support
 */
@NonNullByDefault
public class NukiOpenerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(NukiOpenerHandler.class);
    private static final int JOB_INTERVAL = 60;

    private @Nullable NukiHttpClient nukiHttpClient;
    private @Nullable ScheduledFuture<?> reInitJob;
    private @Nullable String nukiId;

    public NukiOpenerHandler(Thing thing) {
        super(thing);
        logger.debug("Instantiating NukiOpenerHandler({})", thing);
    }

    @Override
    public void initialize() {
        logger.debug("initialize() for Opener[{}].", getThing().getUID());
        Configuration config = getConfig();
        nukiId = (String) config.get(NukiBindingConstants.CONFIG_NUKI_ID);
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
        logger.debug("initializeHandler() for Opener[{}]", nukiId);
        Bridge bridge = getBridge();
        if (bridge == null) {
            initializeHandler(null, null);
        } else {
            initializeHandler(bridge.getHandler(), bridge.getStatus());
        }
    }

    private void initializeHandler(@Nullable ThingHandler bridgeHandler, @Nullable ThingStatus bridgeStatus) {
        if (bridgeHandler != null && bridgeStatus != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                nukiHttpClient = ((NukiBridgeHandler) bridgeHandler).getNukiHttpClient();
                BridgeLockStateResponse bridgeLockStateResponse = nukiHttpClient.getBridgeOpenerState(nukiId);
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
        logger.debug("bridgeStatusChanged({}) for Opener[{}].", bridgeStatusInfo, nukiId);
        scheduler.execute(this::initializeHandler);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand({}, {})", channelUID, command);

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Thing is not ONLINE; command[{}] for channelUID[{}] is ignored", command, channelUID);
            return;
        }

        if (command instanceof RefreshType) {
            scheduler.execute(() -> handleCommandRefreshType(channelUID, command));
            return;
        }

        boolean validCmd = true;
        switch (channelUID.getId()) {
            case NukiBindingConstants.CHANNEL_OPENER_LOCK:
                if (command instanceof OnOffType) {
                    int lockAction;

                    lockAction = (command == OnOffType.OFF ? NukiBindingConstants.OPENER_ACTIONS_OPEN
                            : NukiBindingConstants.OPENER_ACTIONS_DEACTIVATE_RTO);

                    Channel channelLockState = thing.getChannel(NukiBindingConstants.CHANNEL_OPENER_STATE);
                    if (channelLockState != null) {
                        updateState(channelLockState.getUID(),
                                new DecimalType(OpenerActionConverter.getLockStateFor(lockAction)));
                    }
                    BridgeLockActionResponse bridgeLockActionResponse = nukiHttpClient.getBridgeOpenerAction(nukiId,
                            lockAction);
                    handleResponse(bridgeLockActionResponse, channelUID.getAsString(), command.toString());
                } else {
                    validCmd = false;
                }
                break;
            case NukiBindingConstants.CHANNEL_OPENER_STATE:
                if (command instanceof DecimalType) {
                    int lockAction;
                    lockAction = ((DecimalType) command).intValue();
                    lockAction = OpenerActionConverter.getLockActionFor(lockAction);
                    updateState(channelUID, new DecimalType(OpenerActionConverter.getLockStateFor(lockAction)));
                    BridgeLockActionResponse bridgeLockActionResponse = nukiHttpClient.getBridgeOpenerAction(nukiId,
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
            case NukiBindingConstants.CHANNEL_OPENER_LOCK:
                bridgeLockStateResponse = nukiHttpClient.getBridgeOpenerState(nukiId);
                if (handleResponse(bridgeLockStateResponse, channelUID.getAsString(), command.toString())) {
                    int lockState = bridgeLockStateResponse.getState();
                    State state;
                    if (lockState == NukiBindingConstants.OPENER_STATES_ONLINE) {
                        state = OnOffType.ON;
                    } else if (lockState == NukiBindingConstants.OPENER_STATES_OPEN
                            || lockState == NukiBindingConstants.OPENER_STATES_RTO_ACTIVE) {
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
            case NukiBindingConstants.CHANNEL_OPENER_STATE:
                bridgeLockStateResponse = nukiHttpClient.getBridgeOpenerState(nukiId);
                if (handleResponse(bridgeLockStateResponse, channelUID.getAsString(), command.toString())) {
                    updateState(channelUID, new DecimalType(bridgeLockStateResponse.getState()));
                }
                break;
            case NukiBindingConstants.CHANNEL_OPENER_LOW_BATTERY:
                bridgeLockStateResponse = nukiHttpClient.getBridgeOpenerState(nukiId);
                if (handleResponse(bridgeLockStateResponse, channelUID.getAsString(), command.toString())) {
                    updateState(channelUID, bridgeLockStateResponse.isBatteryCritical() ? OnOffType.ON : OnOffType.OFF);
                }
                break;
            default:
                logger.debug("Command[{}] for channelUID[{}] not implemented!", command, channelUID);
                return;
        }
    }

    private boolean handleResponse(NukiBaseResponse nukiBaseResponse, @Nullable String channelUID,
            @Nullable String command) {
        if (nukiBaseResponse.getStatus() != 200) {
            logger.debug("Request to Bridge failed! status[{}] - message[{}]", nukiBaseResponse.getStatus(),
                    nukiBaseResponse.getMessage());
        } else if (nukiBaseResponse.isSuccess()) {
            logger.debug("Command[{}] succeeded for channelUID[{}] on nukiId[{}]!", command, channelUID, nukiId);
            return true;
        } else {
            logger.debug(
                    "Request from Bridge to Opener failed! status[{}] - message[{}] - isSuccess[{}]. Check if Nuki Opener is powered on!",
                    nukiBaseResponse.getStatus(), nukiBaseResponse.getMessage(), nukiBaseResponse.isSuccess());
        }
        logger.debug("Could not handle command[{}] for channelUID[{}] on nukiId[{}]!", command, channelUID, nukiId);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, nukiBaseResponse.getMessage());
        Channel channelLock = thing.getChannel(NukiBindingConstants.CHANNEL_OPENER_LOCK);
        if (channelLock != null) {
            updateState(channelLock.getUID(), OnOffType.OFF);
        }
        Channel channelLockState = thing.getChannel(NukiBindingConstants.CHANNEL_OPENER_STATE);
        if (channelLockState != null) {
            updateState(channelLockState.getUID(), new DecimalType(NukiBindingConstants.OPENER_STATES_UNDEFINED));
        }
        startReInitJob();
        return false;
    }

    private void startReInitJob() {
        logger.trace("Starting reInitJob with interval of  {}secs for Opener[{}].", JOB_INTERVAL, nukiId);
        if (reInitJob != null) {
            logger.trace("Already started reInitJob for Opener[{}].", nukiId);
            return;
        }
        reInitJob = scheduler.scheduleWithFixedDelay(this::initializeHandler, JOB_INTERVAL, JOB_INTERVAL,
                TimeUnit.SECONDS);
    }

    private void stopReInitJob() {
        logger.trace("Stopping reInitJob for Opener[{}].", nukiId);
        ScheduledFuture<?> reInitJob = this.reInitJob;
        if (reInitJob != null) {
            logger.trace("Stopped reInitJob for Opener[{}].", nukiId);
            reInitJob.cancel(true);
        }
        this.reInitJob = null;
    }

    public void handleApiServletUpdate(ChannelUID channelUID, State newState) {
        logger.trace("handleApiServletUpdate({}, {})", channelUID, newState);
        updateState(channelUID, newState);
    }
}
