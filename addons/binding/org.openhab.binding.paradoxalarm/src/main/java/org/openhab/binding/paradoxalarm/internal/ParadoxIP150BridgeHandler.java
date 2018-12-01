/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.paradoxalarm.internal.communication.IParadoxCommunicator;
import org.openhab.binding.paradoxalarm.internal.communication.ParadoxCommunicatorFactory;
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

    private final static Logger logger = LoggerFactory.getLogger(ParadoxIP150BridgeHandler.class);

    private static IParadoxCommunicator communicator;
    private @NonNullByDefault({}) static ParadoxIP150BridgeConfiguration config;
    private @Nullable ScheduledFuture<?> refreshCacheUpdateSchedule;

    public ParadoxIP150BridgeHandler(Bridge bridge) throws Exception {
        super(bridge);
        logger.info("Starting creation of communicator handler");
        config = getConfigAs(ParadoxIP150BridgeConfiguration.class);
        getCommunicator();
        updateDataCache(true);
        logger.info("Communicator handler created successfully");
    }

    public static IParadoxCommunicator getCommunicator() throws Exception {
        synchronized (ParadoxIP150BridgeHandler.class) {
            if (communicator == null) {
                communicator = initializeCommunicator();
            }
            return communicator;
        }
    }

    protected static IParadoxCommunicator initializeCommunicator() throws Exception {
        synchronized (ParadoxIP150BridgeHandler.class) {
            String ipAddress = config.getIpAddress();
            int tcpPort = config.getPort();
            String ip150Password = config.getIp150Password();
            String pcPassword = config.getPcPassword();
            String panelTypeStr = config.getPanelType();
            // IParadoxGenericCommunicator initialCommunicator = new GenericCommunicator(ipAddress, tcpPort,
            // ip150Password,
            // pcPassword);
            // byte[] panelInfoBytes = initialCommunicator.getPanelInfoBytes();
            // PanelType panelType = PanelType.parsePanelType(panelInfoBytes);
            // logger.info("Found {} panel type.", panelType);
            // initialCommunicator.close();
            // Thread.sleep(500);

            ParadoxCommunicatorFactory factory = new ParadoxCommunicatorFactory(ipAddress, tcpPort, ip150Password,
                    pcPassword);
            IParadoxCommunicator communicator = factory.createCommunicator(panelTypeStr);
            return communicator;
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        updateStatus(ThingStatus.UNKNOWN);
        try {
            scheduleRefresh();

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
        try {
            communicator.close();
            // This is very ugly but Paradox supports only one connection at a time and if not closed properly if
            // handler gots destroyed/recreated before the full socket closure. The new handler cannot establish proper
            // communication.
            Thread.sleep(10000);
        } catch (IOException e) {
            logger.error("Unable to close communicator socket and logout from paradox system");
        } catch (InterruptedException e) {
            logger.error("Unable to pause thread for 10 sec");
        }

        super.dispose();
    }

    private void scheduleRefresh() {
        logger.debug("Scheduling cache update. Refresh interval: " + config.getRefresh() + "s.");
        refreshCacheUpdateSchedule = scheduler.scheduleWithFixedDelay(() -> {
            updateDataCache();
        }, 0, config.getRefresh(), TimeUnit.SECONDS);
    }

    private void updateDataCache() {
        updateDataCache(false);
    }

    private void updateDataCache(boolean withEpromValues) {
        try {
            logger.debug("Refreshing memory map");
            communicator.refreshMemoryMap();

            RawStructuredDataCache cache = RawStructuredDataCache.getInstance();

            cache.setPanelInfoBytes(communicator.getPanelInfoBytes());
            cache.setPartitionStateFlags(communicator.readPartitionFlags());
            cache.setZoneStateFlags(communicator.readZoneStateFlags());

            if (withEpromValues) {
                cache.setPartitionLabels(communicator.readPartitionLabels());
                cache.setZoneLabels(communicator.readZoneLabels());
            }
        } catch (Exception e) {
            logger.error("Communicator cannot refresh cached memory map. Exception: ", e);
        }
    }

    public static IParadoxCommunicator reinitializeCommunicator() throws Exception {
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
        if (ParadoxAlarmBindingConstants.IP150_COMMAND_CHANNEL_UID.equals(channelUID.getId())) {
            logger.debug("Command is instance of {}", command.getClass());
            if (command instanceof StringType) {
                String commandAsString = command.toFullString();
                switch (commandAsString) {
                    case "LOGIN":
                        logger.debug("Identified command LOGIN");
                        break;
                    case "LOGOUT":
                        logger.debug("Identified command LOGOUT");
                        break;
                    case "RESET":
                        logger.debug("Identified command RESET");
                        break;
                    default:
                        logger.debug("Unknown command");
                }
            }
        }
    }

}
