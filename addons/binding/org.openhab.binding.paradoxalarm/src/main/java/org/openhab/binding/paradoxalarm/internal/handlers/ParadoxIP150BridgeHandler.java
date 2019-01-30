/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.handlers;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.paradoxalarm.internal.communication.GenericCommunicator;
import org.openhab.binding.paradoxalarm.internal.communication.IParadoxCommunicator;
import org.openhab.binding.paradoxalarm.internal.communication.IParadoxGenericCommunicator;
import org.openhab.binding.paradoxalarm.internal.communication.ParadoxCommunicatorFactory;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxBindingException;
import org.openhab.binding.paradoxalarm.internal.model.PanelType;
import org.openhab.binding.paradoxalarm.internal.model.RawStructuredDataCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxIP150BridgeHandler} This is the handler that takes care of communication to/from Paradox alarm
 * system.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ParadoxIP150BridgeHandler extends BaseBridgeHandler {

    private static final String RESET = "RESET";

    private static final int INITIAL_SCHEDULE_DELAY = 15;
    private static final int FAILED_READ_ATTEMPTS_TRESHOLD = 10;

    private final static Logger logger = LoggerFactory.getLogger(ParadoxIP150BridgeHandler.class);

    private static IParadoxCommunicator communicator;
    private static ParadoxIP150BridgeConfiguration config;
    private @Nullable ScheduledFuture<?> refreshCacheUpdateSchedule;
    private int failedReadAttempts;

    public ParadoxIP150BridgeHandler(Bridge bridge) throws Exception {
        super(bridge);
        logger.info("Starting creation of communicator handler");
        config = getConfigAs(ParadoxIP150BridgeConfiguration.class);
        communicator = initializeCommunicator();
        updateDataCache(true);
        logger.info("Communicator handler created successfully");
    }

    private IParadoxCommunicator initializeCommunicator() throws Exception {
        synchronized (ParadoxIP150BridgeHandler.class) {
            String ipAddress = config.getIpAddress();
            int tcpPort = config.getPort();
            String ip150Password = config.getIp150Password();
            String pcPassword = config.getPcPassword();

            logger.info("Phase1 - Identify communicator");
            IParadoxGenericCommunicator initialCommunicator = new GenericCommunicator(ipAddress, tcpPort, ip150Password,
                    pcPassword);
            byte[] panelInfoBytes = initialCommunicator.getPanelInfoBytes();

            PanelType panelType = PanelType.parsePanelType(panelInfoBytes);
            logger.info("Found {} panel type.", panelType);
            initialCommunicator.close();

            // If not detected properly, use the value from config
            String panelTypeStr;
            if (panelType != PanelType.UNKNOWN) {
                panelTypeStr = panelType.name();
            } else {
                panelTypeStr = config.getPanelType();
            }

            logger.info("Phase2 - Creating communicator for panel {}", panelType);
            ParadoxCommunicatorFactory factory = new ParadoxCommunicatorFactory(ipAddress, tcpPort, ip150Password,
                    pcPassword);
            return factory.createCommunicator(panelTypeStr);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        updateStatus(ThingStatus.UNKNOWN);
        try {
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error initializing panel handler. Exception: " + e);
        }
        logger.debug("Finished initializing!");
    }

    @Override
    public void dispose() {
        HandlersUtil.cancelSchedule(refreshCacheUpdateSchedule);
        communicator.close();
        super.dispose();
    }

    private void scheduleRefresh() {
        logger.debug("Scheduling cache update. Refresh interval: " + config.getRefresh() + "s.");
        refreshCacheUpdateSchedule = scheduler.scheduleWithFixedDelay(() -> {
            updateDataCache();
        }, INITIAL_SCHEDULE_DELAY, config.getRefresh(), TimeUnit.SECONDS);
    }

    private void updateDataCache() {
        updateDataCache(false);
    }

    private void updateDataCache(boolean withEpromValues) {
        try {
            logger.debug("Refreshing memory map");
            RawStructuredDataCache cache = RawStructuredDataCache.getInstance();

            boolean isOnline = communicator.isOnline();
            cache.setIsOnline(isOnline);

            if (isOnline) {
                communicator.refreshMemoryMap();

                cache.setPanelInfoBytes(communicator.getPanelInfoBytes());
                cache.setPartitionStateFlags(communicator.readPartitionFlags());
                cache.setZoneStateFlags(communicator.readZoneStateFlags());

                if (withEpromValues) {
                    cache.setPartitionLabels(communicator.readPartitionLabels());
                    cache.setZoneLabels(communicator.readZoneLabels());
                }
            }

            announceUpdateToHandlers();

            if (failedReadAttempts > 0) {
                logger.info("Successfully refreshed memory map after {} failed attempts.", failedReadAttempts);
                failedReadAttempts = 0;
            }

        } catch (IOException e) {
            handleSocketReadError(e);
        } catch (InterruptedException | ParadoxBindingException e) {
            logger.error("Exception while refreshing memory map in communicator. {}", e);
        }
    }

    private void handleSocketReadError(IOException e) {
        failedReadAttempts++;
        logger.error("Communicator cannot refresh cached memory map. Attempt: {}, IOException msg: {}",
                failedReadAttempts, e.getMessage());
        logger.debug("Stack trace: {}", e);

        if (failedReadAttempts == FAILED_READ_ATTEMPTS_TRESHOLD) {
            logger.info("Will attempt to reinitialize communicator.");
            @SuppressWarnings("null")
            ChannelUID uid = getThing().getChannel(ParadoxAlarmBindingConstants.IP150_COMMUNICATION_COMMAND_CHANNEL_UID)
                    .getUID();
            handleCommand(uid, new StringType(RESET));
        }
    }

    private void announceUpdateToHandlers() {
        Bridge bridge = getThing();
        List<Thing> things = bridge.getThings();
        for (Thing thing : things) {
            ThingHandler handler = thing.getHandler();
            Channel bridgeChannel = bridge
                    .getChannel(ParadoxAlarmBindingConstants.IP150_COMMUNICATION_COMMAND_CHANNEL_UID);
            if (handler != null && bridgeChannel != null) {
                handler.handleCommand(bridgeChannel.getUID(), RefreshType.REFRESH);
            }
        }
    }

    public IParadoxCommunicator reinitializeCommunicator() throws Exception {
        synchronized (ParadoxIP150BridgeHandler.class) {
            if (communicator != null) {
                communicator.close();
            }
            communicator = initializeCommunicator();

            return communicator;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {}", command.toFullString());
        if (ParadoxAlarmBindingConstants.IP150_COMMUNICATION_COMMAND_CHANNEL_UID.equals(channelUID.getId())) {
            logger.debug("Command is instance of {}", command.getClass());
            if (command instanceof StringType) {
                String commandAsString = command.toFullString();
                if (commandAsString.equals(RESET)) {
                    try {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                "Bringing bridge offline due to reinitialization of communicator.");
                        reinitializeCommunicator();
                    } catch (Exception e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Error reinitializing communicator. Exception: " + e);
                    }
                } else {
                    communicator.executeCommand(commandAsString);
                }
            }
        }
        updateDataCache(true);

        if (communicator == null || !communicator.isOnline()) {
            logger.debug("Communicator is null or not online");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Device is offline");
        } else {
            logger.debug("Communicator is online");
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @SuppressWarnings("null")
    @Override
    protected void updateStatus(@NonNull ThingStatus status, ThingStatusDetail statusDetail,
            @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
        if (status.equals(ThingStatus.ONLINE)) {
            if (refreshCacheUpdateSchedule == null || refreshCacheUpdateSchedule.isDone()) {
                scheduleRefresh();
            }
        } else {
            HandlersUtil.cancelSchedule(refreshCacheUpdateSchedule);
        }
    }

}
