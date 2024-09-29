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
package org.openhab.binding.paradoxalarm.internal.handlers;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.paradoxalarm.internal.communication.CommunicationState;
import org.openhab.binding.paradoxalarm.internal.communication.GenericCommunicator;
import org.openhab.binding.paradoxalarm.internal.communication.ICommunicatorBuilder;
import org.openhab.binding.paradoxalarm.internal.communication.IDataUpdateListener;
import org.openhab.binding.paradoxalarm.internal.communication.IP150Command;
import org.openhab.binding.paradoxalarm.internal.communication.IParadoxCommunicator;
import org.openhab.binding.paradoxalarm.internal.communication.IParadoxInitialLoginCommunicator;
import org.openhab.binding.paradoxalarm.internal.communication.ISocketTimeOutListener;
import org.openhab.binding.paradoxalarm.internal.communication.ParadoxBuilderFactory;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxRuntimeException;
import org.openhab.binding.paradoxalarm.internal.model.PanelType;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxInformationConstants;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxPanel;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxIP150BridgeHandler} This is the handler that takes care of communication to/from Paradox alarm
 * system.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault({})
public class ParadoxIP150BridgeHandler extends BaseBridgeHandler
        implements IDataUpdateListener, ISocketTimeOutListener {

    private static final String RESET_COMMAND = "RESET";

    private static final int ONLINE_WAIT_TRESHOLD_MILLIS = 10000;

    private static final int INITIAL_SCHEDULE_DELAY_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(ParadoxIP150BridgeHandler.class);

    private IParadoxCommunicator communicator;
    private ParadoxPanel panel = new ParadoxPanel();

    private ParadoxIP150BridgeConfiguration config;
    private @Nullable ScheduledFuture<?> refreshCacheUpdateSchedule;

    private long timeStamp = 0;

    private ScheduledFuture<?> resetScheduleFuture;

    public ParadoxIP150BridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Start initialize()...");
        updateStatus(ThingStatus.UNKNOWN);
        logger.debug("Starting creation of communicator.");
        config = getConfigAs(ParadoxIP150BridgeConfiguration.class);

        scheduler.execute(this::initializeCommunicator);
        logger.debug("Finished initialize().");
    }

    private synchronized void initializeCommunicator() {
        try {
            String ipAddress = config.getIpAddress();
            int tcpPort = config.getPort();
            String ip150Password = config.getIp150Password();
            String pcPassword = config.getPcPassword();
            boolean useEncryption = config.isEncrypt();

            // Early exit. If panel type is configured and known to the binding skip auto-detection. Saves one full
            // initial login process to detect the panel type.
            PanelType configuredPanelType = PanelType.from(config.getPanelType());
            if (configuredPanelType != PanelType.UNKNOWN) {
                logger.debug("Configuration file has pannelType={}. Skipping Phase1 (Autodiscovery)",
                        configuredPanelType);
                scheduler.schedule(() -> createDiscoveredCommunicatorJob(configuredPanelType), 3, TimeUnit.SECONDS);
                return;
            }

            logger.debug("Phase1 - Auto discover communicator");
            IParadoxInitialLoginCommunicator initialCommunicator = new GenericCommunicator(ipAddress, tcpPort,
                    ip150Password, pcPassword, scheduler, useEncryption);
            initialCommunicator.startLoginSequence();

            timeStamp = System.currentTimeMillis();
            scheduler.schedule(() -> doPostOnlineTask(initialCommunicator), 500, TimeUnit.MILLISECONDS);
        } catch (UnknownHostException e) {
            logger.warn("Error while starting socket communication. {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unknown host. Probably misconfiguration or DNS issue.");
            throw new ParadoxRuntimeException(e);
        } catch (IOException e) {
            logger.warn("Error while starting socket communication. {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error while starting socket communication.");
            throw new ParadoxRuntimeException(e);
        }
    }

    private synchronized void doPostOnlineTask(IParadoxInitialLoginCommunicator initialCommunicator) {
        if (!initialCommunicator.isOnline()) {
            if (System.currentTimeMillis() - timeStamp <= ONLINE_WAIT_TRESHOLD_MILLIS) {
                scheduler.schedule(() -> doPostOnlineTask(initialCommunicator), 500, TimeUnit.MILLISECONDS);
                logger.debug("Communicator not yet online. Rescheduling...");
            } else {
                logger.warn(
                        "Initial communicator not coming up online for {} seconds. Probably there is something wrong with communication.",
                        ONLINE_WAIT_TRESHOLD_MILLIS);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error while starting socket communication.");
            }
            return;
        }

        byte[] panelInfoBytes = initialCommunicator.getPanelInfoBytes();

        PanelType panelType = ParadoxInformationConstants.parsePanelType(panelInfoBytes);
        logger.info("Found {} panel type.", panelType);
        CommunicationState.logout(initialCommunicator);

        // Wait 3 seconds before we create the discovered communicator so we ensure that the socket is closed
        // successfully from both ends
        scheduler.schedule(() -> createDiscoveredCommunicatorJob(panelType), 3, TimeUnit.SECONDS);
    }

    protected void createDiscoveredCommunicatorJob(PanelType panelType) {
        // If not detected properly, use the value from config
        if (panelType == PanelType.UNKNOWN) {
            panelType = PanelType.from(config.getPanelType());
        }

        logger.debug("Phase2 - Creating communicator for panel {}", panelType);
        ICommunicatorBuilder builder = new ParadoxBuilderFactory().createBuilder(panelType);
        communicator = builder.withIp150Password(config.getIp150Password()).withPcPassword(config.getPcPassword())
                .withIpAddress(config.getIpAddress()).withTcpPort(config.getPort())
                .withMaxPartitions(config.getMaxPartitions()).withMaxZones(config.getMaxZones())
                .withScheduler(scheduler).withEncryption(config.isEncrypt()).build();

        panel.setCommunicator(communicator);

        Collection<IDataUpdateListener> listeners = Arrays.asList(panel, this);
        communicator.setListeners(listeners);
        communicator.setStoListener(this);
        logger.debug("Listeners set to: {}", listeners);

        communicator.startLoginSequence();

        timeStamp = System.currentTimeMillis();
        doPostOnlineFinalCommunicatorJob();
    }

    private void doPostOnlineFinalCommunicatorJob() {
        if (!communicator.isOnline()) {
            if (System.currentTimeMillis() - timeStamp <= ONLINE_WAIT_TRESHOLD_MILLIS) {
                scheduler.schedule(this::doPostOnlineFinalCommunicatorJob, 1, TimeUnit.SECONDS);
                logger.debug("Communicator not yet online. Rescheduling...");
                return;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error while starting socket communication.");
                ParadoxRuntimeException exception = new ParadoxRuntimeException(
                        "Communicator didn't go online in defined treshold time. " + ONLINE_WAIT_TRESHOLD_MILLIS
                                + "sec. You can still try to reset the bridge with command RESET.");
                logger.debug("Problem with communication", exception);
                throw exception;
            }
        }

        logger.debug("Communicator created successfully.");
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        cancelSchedule(refreshCacheUpdateSchedule);
        CommunicationState.logout(communicator);
        panel.dispose();
        super.dispose();
    }

    private void scheduleRefresh() {
        logger.debug("Scheduling cache update. Refresh interval: {}s. Starts after: {}s.", config.getRefresh(),
                INITIAL_SCHEDULE_DELAY_SECONDS);
        refreshCacheUpdateSchedule = scheduler.scheduleWithFixedDelay(communicator::refreshMemoryMap,
                INITIAL_SCHEDULE_DELAY_SECONDS, config.getRefresh(), TimeUnit.SECONDS);
    }

    private void cancelSchedule(@Nullable ScheduledFuture<?> schedule) {
        if (schedule != null && !schedule.isCancelled()) {
            boolean cancelingResult = schedule.cancel(true);
            String cancelingSuccessful = cancelingResult ? "successful" : "failed";
            logger.debug("Canceling schedule of {} is {}", schedule, cancelingSuccessful);
        }
    }

    @Override
    public void update() {
        updateStatusChannel();

        Bridge bridge = getThing();
        if (bridge.getStatus() == ThingStatus.OFFLINE) {
            if (communicator.isOnline()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("Bridge {} triggered update but is OFFLINE", bridge.getUID());
                return;
            }
        }

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

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {}", command);
        if (ThingStatus.OFFLINE == getThing().getStatus() && command instanceof RefreshType) {
            logger.debug("Bridge {} is OFFLINE. Cannot handle refresh commands.", getThing().getUID());
            return;
        }

        if (ParadoxAlarmBindingConstants.IP150_COMMUNICATION_COMMAND_CHANNEL_UID.equals(channelUID.getId())) {
            logger.debug("Command is instance of {}", command.getClass());
            if (command instanceof StringType) {
                String commandAsString = command.toFullString();
                if (commandAsString.equals(RESET_COMMAND)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                            "Bringing bridge offline due to reinitialization of communicator.");
                    resetCommunicator();
                } else {
                    communicator.executeCommand(commandAsString);
                }
            }
        }

        if (communicator != null && communicator.isOnline()) {
            logger.debug("Communicator is online");
            communicator.refreshMemoryMap();
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.debug("Communicator is null or not online");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Device is offline");
        }
    }

    private void resetCommunicator() {
        updateStatus(ThingStatus.OFFLINE);

        synchronized (communicator) {
            if (communicator != null) {
                CommunicationState.logout(communicator);
            }

            initializeCommunicator();
        }

        resetScheduleFuture = null;
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        updateStatusChannel();

        super.updateStatus(status, statusDetail, description);

        if (status.equals(ThingStatus.ONLINE)) {
            if (refreshCacheUpdateSchedule == null || refreshCacheUpdateSchedule.isDone()) {
                scheduleRefresh();
            }
        } else {
            cancelSchedule(refreshCacheUpdateSchedule);
        }
    }

    private void updateStatusChannel() {
        StringType communicatorStatus = communicator != null && communicator.isOnline()
                ? ParadoxAlarmBindingConstants.STATE_ONLINE
                : ParadoxAlarmBindingConstants.STATE_OFFLINE;
        updateState(ParadoxAlarmBindingConstants.IP150_COMMUNICATION_STATE_CHANNEL_UID, communicatorStatus);
    }

    public IParadoxCommunicator getCommunicator() {
        return communicator;
    }

    public ParadoxPanel getPanel() {
        return panel;
    }

    @Override
    public void onSocketTimeOutOccurred(IOException exception) {
        logger.warn("TIMEOUT! {} received message for socket timeout. ", this, exception);
        if (resetScheduleFuture == null) {
            communicator.executeCommand(IP150Command.LOGOUT.name());

            logger.warn("Reset task is null. Will schedule new task to reset communicator in {} seconds.",
                    config.getReconnectWaitTime());
            resetScheduleFuture = scheduler.schedule(this::resetCommunicator, config.getReconnectWaitTime(),
                    TimeUnit.SECONDS);
        }
    }

    @Override
    public String toString() {
        return "ParadoxIP150BridgeHandler [" + getThing().getUID().getAsString() + "]";
    }
}
