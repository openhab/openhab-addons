/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.nuki.NukiBindingConstants;
import org.openhab.binding.nuki.internal.converter.LockActionConverter;
import org.openhab.binding.nuki.internal.dataexchange.BridgeLockActionResponse;
import org.openhab.binding.nuki.internal.dataexchange.BridgeLockStateResponse;
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

    private final static Logger logger = LoggerFactory.getLogger(NukiSmartLockHandler.class);
    private final static int RETRYJOB_INTERVAL = 60;

    private NukiHttpClient nukiHttpClient;
    private ScheduledFuture<?> retryJob;
    private String nukiId;

    public NukiSmartLockHandler(Thing thing) {
        super(thing);
        logger.trace("Instantiating NukiSmartLockHandler({})", thing);
    }

    @Override
    public void initialize() {
        logger.debug("NukiSmartLockHandler:initialize()");
        scheduler.execute(() -> initializeHandler());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("NukiSmartLockHandler:handleCommand({}, {})", channelUID, command);
        String nukiId = (String) getConfig().get(NukiBindingConstants.CONFIG_NUKI_ID);
        if (command instanceof RefreshType) {
            scheduler.execute(() -> handleCommandRefreshType(channelUID, command, nukiId));
        } else if (command instanceof OnOffType) {
            scheduler.execute(() -> handleCommandOnOffType(channelUID, command, nukiId));
        } else if (command instanceof DecimalType) {
            scheduler.execute(() -> handleCommandDecimalType(channelUID, command, nukiId));
        } else {
            logger.warn("NukiSmartLockHandler:handleCommand({}, {}) not implemented!", channelUID, command);
        }
    }

    @Override
    public void dispose() {
        logger.debug("NukiSmartLockHandler:dispose()");
        cancelRetryJob();
    }

    private void initializeHandler() {
        logger.debug("NukiSmartLockHandler:initializeHandler()");
        if (!getNukiHttpClient()) {
            logger.warn("NukiSmartLockHandler:initializeHandler():Could not get NukiHttpClient!");
            return;
        }
        BridgeLockStateResponse bridgeLockStateResponse = nukiHttpClient.getBridgeLockState(nukiId);
        if (bridgeLockStateResponse.getStatus() == HttpStatus.OK_200
                && bridgeLockStateResponse.getBridgeLockState().isSuccess()) {
            updateStatus(ThingStatus.ONLINE);
            if (retryJob != null) {
                for (Channel channel : thing.getChannels()) {
                    handleCommand(channel.getUID(), RefreshType.REFRESH);
                }
            }
            cancelRetryJob();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    bridgeLockStateResponse.getMessage());
            scheduleRetryJob();
        }
    }

    private boolean getNukiHttpClient() {
        logger.debug("NukiSmartLockHandler:getNukiHttpClient()");
        nukiId = (String) getConfig().get(NukiBindingConstants.CONFIG_NUKI_ID);
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            logger.warn("Setting Smart Lock[{}] offline because Bridge is offline!", nukiId);
            scheduleRetryJob();
            return false;
        }
        NukiBridgeHandler nbh = (NukiBridgeHandler) bridge.getHandler();
        if (nbh == null) {
            logger.warn("Setting Smart Lock[{}] offline because NukiBridgeHandler is null!", nukiId);
            scheduleRetryJob();
            return false;
        }
        nukiHttpClient = nbh.getNukiHttpClient();
        return true;
    }

    private void handleCommandRefreshType(ChannelUID channelUID, Command command, String nukiId) {
        logger.debug("NukiSmartLockHandler:handleCommandRefreshType({}, {}, {})", channelUID, command, nukiId);
        BridgeLockStateResponse bridgeLockStateResponse = nukiHttpClient.getBridgeLockState(nukiId);
        if (bridgeLockStateResponse.getStatus() != 200 || !bridgeLockStateResponse.getBridgeLockState().isSuccess()) {
            logger.debug("Could not handle command[{}] for channelUID[{}] and nukiId[{}]! Message[{}]", command,
                    channelUID, nukiId, bridgeLockStateResponse.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    bridgeLockStateResponse.getMessage());
        } else {
            int lockState = bridgeLockStateResponse.getBridgeLockState().getState();
            State state;
            switch (channelUID.getId()) {
                case NukiBindingConstants.CHANNEL_SMARTLOCK_UNLOCK:
                    if (lockState == NukiBindingConstants.LOCK_STATES_LOCKED) {
                        state = OnOffType.ON;
                    } else if (lockState == NukiBindingConstants.LOCK_STATES_UNLOCKED) {
                        state = OnOffType.OFF;
                    } else {
                        logger.warn(
                                "Smart Lock returned lockState[{}]. Intentionally setting wrong value 'OFF' for channel 'smartlockUnlock'!",
                                lockState);
                        state = OnOffType.OFF;
                    }
                    break;
                case NukiBindingConstants.CHANNEL_SMARTLOCK_LOCK_ACTION:
                    state = new DecimalType(lockState);
                    break;
                case NukiBindingConstants.CHANNEL_SMARTLOCK_LOW_BATTERY:
                    state = bridgeLockStateResponse.getBridgeLockState().isBatteryCritical() == true ? OnOffType.ON
                            : OnOffType.OFF;
                    break;
                default:
                    logger.warn("Command[{}] for channelUID[{}] not implemented!", command, channelUID);
                    return;
            }
            updateState(channelUID, state);
        }
    }

    private void handleCommandOnOffType(ChannelUID channelUID, Command command, String nukiId) {
        logger.debug("NukiSmartLockHandler:handleCommandOnOffType({}, {}, {})", channelUID, command, nukiId);
        if (!channelUID.getId().equals(NukiBindingConstants.CHANNEL_SMARTLOCK_UNLOCK)) {
            logger.warn("Command[{}] for channelUID[{}] not implemented!", command, channelUID);
            return;
        }
        int lockAction;
        boolean unlatch = (Boolean) getConfig().get(NukiBindingConstants.CONFIG_UNLATCH);
        if (unlatch) {
            lockAction = (command == OnOffType.OFF ? NukiBindingConstants.LOCK_ACTIONS_UNLATCH
                    : NukiBindingConstants.LOCK_ACTIONS_LOCK);
        } else {
            lockAction = (command == OnOffType.OFF ? NukiBindingConstants.LOCK_ACTIONS_UNLOCK
                    : NukiBindingConstants.LOCK_ACTIONS_LOCK);
        }
        Channel channelLockAction = thing.getChannel(NukiBindingConstants.CHANNEL_SMARTLOCK_LOCK_ACTION);
        if (channelLockAction != null) {
            updateState(channelLockAction.getUID(), new DecimalType(LockActionConverter.getLockStateFor(lockAction)));
        }
        BridgeLockActionResponse bridgeLockActionResponse = nukiHttpClient.getBridgeLockAction(nukiId, lockAction);
        if (bridgeLockActionResponse.getStatus() != 200
                || !bridgeLockActionResponse.getBridgeLockAction().isSuccess()) {
            logger.error("Could not handle command[{}] for channelUID[{}] and nukiId[{}]! Message[{}]", command,
                    channelUID, nukiId, bridgeLockActionResponse.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    bridgeLockActionResponse.getMessage());
            if (channelLockAction != null) {
                updateState(channelLockAction.getUID(), new DecimalType(NukiBindingConstants.LOCK_STATES_UNDEFINED));
            }
            scheduleRetryJob();
        } else {
            logger.debug("Command[{}] succeeded for channelUID[{}]", command, channelUID);
        }
    }

    private void handleCommandDecimalType(ChannelUID channelUID, Command command, String nukiId) {
        logger.debug("NukiSmartLockHandler:handleCommandDecimalType({}, {}, {})", channelUID, command, nukiId);
        if (!channelUID.getId().equals(NukiBindingConstants.CHANNEL_SMARTLOCK_LOCK_ACTION)) {
            logger.warn("Command[{}] for channelUID[{}] not implemented!", command, channelUID);
            return;
        }
        int lockAction;
        lockAction = (command instanceof DecimalType) ? ((DecimalType) command).intValue() : 0;
        lockAction = LockActionConverter.getLockActionFor(lockAction);
        updateState(channelUID, new DecimalType(LockActionConverter.getLockStateFor(lockAction)));
        BridgeLockActionResponse bridgeLockActionResponse = nukiHttpClient.getBridgeLockAction(nukiId, lockAction);
        if (bridgeLockActionResponse.getStatus() != 200
                || !bridgeLockActionResponse.getBridgeLockAction().isSuccess()) {
            logger.error("Could not handle command[{}] for channelUID[{}] and nukiId[{}]! Message[{}]", command,
                    channelUID, nukiId, bridgeLockActionResponse.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    bridgeLockActionResponse.getMessage());
            updateState(channelUID, new DecimalType(NukiBindingConstants.LOCK_STATES_UNDEFINED));
            scheduleRetryJob();
        } else {
            logger.debug("Command[{}] succeeded for channelUID[{}]", command, channelUID);
        }
    }

    private void scheduleRetryJob() {
        logger.trace("NukiSmartLockHandler:scheduleRetryJob():Scheduling retryJob in {}secs for Smart Lock[{}].",
                RETRYJOB_INTERVAL, nukiId);
        if (retryJob != null && !retryJob.isDone()) {
            logger.trace("NukiSmartLockHandler:scheduleRetryJob():Already scheduled for Smart Lock[{}].", nukiId);
            return;
        }
        retryJob = null;
        retryJob = scheduler.schedule(() -> {
            initialize();
        }, RETRYJOB_INTERVAL, TimeUnit.SECONDS);
    }

    private void cancelRetryJob() {
        logger.trace("NukiSmartLockHandler:cancelRetryJob():Canceling retryJob for Smart Lock[{}].", nukiId);
        if (retryJob != null) {
            retryJob.cancel(true);
            retryJob = null;
        }
    }

    public void handleApiServletUpdate(ChannelUID channelUID, State newState) {
        logger.trace("NukiSmartLockHandler:handleApiServletUpdate({}, {})", channelUID, newState);
        updateState(channelUID, newState);
    }

}
