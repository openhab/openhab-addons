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
package org.openhab.binding.cbus.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cbus.internal.CBusNetworkConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daveoxley.cbus.CGateException;
import com.daveoxley.cbus.CGateSession;
import com.daveoxley.cbus.Network;
import com.daveoxley.cbus.Project;

/**
 * The {@link CBusNetworkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */
@NonNullByDefault
public class CBusNetworkHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(CBusNetworkHandler.class);
    private @Nullable CBusNetworkConfiguration configuration;
    private @Nullable Network network;
    private @Nullable Project projectObject;
    private @Nullable ScheduledFuture<?> initNetwork;
    private @Nullable ScheduledFuture<?> networkSync;

    public CBusNetworkHandler(Bridge thing) {
        super(thing);
    }

    // This is abstract in base class so have to implement it.
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        logger.debug("initialize ");
        configuration = getConfigAs(CBusNetworkConfiguration.class);
        logger.debug("Using configuration {}", configuration);
        CBusCGateHandler bridgeHandler = getCBusCGateHandler();
        if (bridgeHandler == null || !bridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("bridge not online");
            updateStatus(ThingStatus.OFFLINE);
            return;
        }
        logger.debug("Bridge online so init properly");
        scheduler.execute(this::cgateOnline);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> networkSync = this.networkSync;
        if (networkSync != null) {
            networkSync.cancel(false);
        }
        ScheduledFuture<?> initNetwork = this.initNetwork;
        if (initNetwork != null) {
            initNetwork.cancel(false);
        }
        super.dispose();
    }

    public void cgateStateChanged(boolean isOnline) {
        logger.debug("CgateStateChanged {}", isOnline);
        if (!isOnline) {
            network = null;
            projectObject = null;
            updateStatus();
        } else {
            cgateOnline();
        }
    }

    private void cgateOnline() {
        CBusNetworkConfiguration configuration = this.configuration;
        if (configuration == null) {
            logger.debug("cgateOnline - NetworkHandler not initialised");
            return;
        }
        ThingStatus lastStatus = getThing().getStatus();
        logger.debug("cgateOnline {}", lastStatus);

        Integer networkID = configuration.id;
        String project = configuration.project;
        logger.debug("cgateOnline netid {} project {}", networkID, project);
        Project projectObject = getProjectObject();
        Network network = getNetwork();
        logger.debug("network {}", network);
        CBusCGateHandler cbusCGateHandler = getCBusCGateHandler();
        if (cbusCGateHandler == null) {
            logger.debug("NoCGateHandler");
            return;
        }
        try {
            if (projectObject == null) {
                CGateSession session = cbusCGateHandler.getCGateSession();
                if (session != null) {
                    try {
                        projectObject = (Project) session.getCGateObject("//" + project);
                        this.projectObject = projectObject;
                    } catch (CGateException ignore) {
                        // We dont need to do anything other than stop this propagating
                    }
                }
                if (projectObject == null) {
                    logger.debug("Cant get projectobject");
                    return;
                }
            }
            if (network == null) {
                CGateSession session = cbusCGateHandler.getCGateSession();
                if (session != null) {
                    try {
                        network = (Network) session.getCGateObject("//" + project + "/" + networkID);
                        this.network = network;
                    } catch (CGateException ignore) {
                        // We dont need to do anything other than stop this propagating
                    }
                }
                if (network == null) {
                    logger.debug("cgateOnline: Cant get network");
                    return;
                }
            }
            String state = network.getState();
            logger.debug("Network state is {}", state);
            if ("new".equals(state)) {
                projectObject.start();
                logger.debug("Need to wait for it to be synced");
            } else if ("sync".equals(state)) {
                logger.debug("Network is syncing so wait for it to be ok");
            }
            if (!"ok".equals(state)) {
                ScheduledFuture<?> initNetwork = this.initNetwork;
                if (initNetwork == null || initNetwork.isCancelled()) {
                    this.initNetwork = scheduler.scheduleWithFixedDelay(this::checkNetworkOnline, 30, 30,
                            TimeUnit.SECONDS);
                    logger.debug("Schedule a check every minute");
                } else {
                    logger.debug("initNetwork alreadys started");
                }
                updateStatus();
                return;
            }
        } catch (CGateException e) {
            logger.warn("Cannot load C-Bus network {}", networkID, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
        }
        updateStatus();
    }

    private void checkNetworkOnline() {
        Network network = getNetwork();
        try {
            if (network != null && network.isOnline()) {
                logger.debug("Network is online");
                ScheduledFuture<?> initNetwork = this.initNetwork;
                if (initNetwork != null) {
                    initNetwork.cancel(false);
                    this.initNetwork = null;
                }
            } else {
                ThingStatus lastStatus = getThing().getStatus();
                logger.debug("Network still not online {}", lastStatus);
            }
        } catch (CGateException e) {
            logger.warn("Cannot check if network is online {} ", network.getNetworkID());
        }
        updateStatus();
    }

    private void updateStatus() {
        CBusNetworkConfiguration configuration = this.configuration;
        if (configuration == null) {
            logger.debug("updateStatus - NetworkHandler not initialised");
            return;
        }
        ThingStatus lastStatus = getThing().getStatus();
        Network network = getNetwork();
        CBusCGateHandler cbusCGateHandler = getCBusCGateHandler();
        try {
            if (cbusCGateHandler == null || !cbusCGateHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "CGate connection offline");
            } else if (network == null) {
                logger.debug("No network - set configuration error");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No Network object available");
            } else if (network.isOnline()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Network is not reporting online");
            }
        } catch (CGateException e) {
            logger.warn("Problem checking network state for network {}", network.getNetworkID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        if (!getThing().getStatus().equals(lastStatus)) {
            ScheduledFuture<?> networkSync = this.networkSync;
            if (lastStatus == ThingStatus.OFFLINE) {
                if (networkSync == null || networkSync.isCancelled()) {
                    this.networkSync = scheduler.scheduleWithFixedDelay(this::doNetworkSync, 10,
                            configuration.syncInterval, TimeUnit.SECONDS);
                }
            } else {
                if (networkSync != null) {
                    networkSync.cancel(false);
                }
            }
            for (Thing thing : getThing().getThings()) {
                ThingHandler handler = thing.getHandler();
                if (handler instanceof CBusGroupHandler) {
                    ((CBusGroupHandler) handler).updateStatus();
                }
            }
        }
    }

    private void doNetworkSync() {
        Network network = getNetwork();
        try {
            if (getThing().getStatus().equals(ThingStatus.ONLINE) && network != null) {
                logger.info("Starting network sync on network {}", network.getNetworkID());
                network.startSync();
            }
        } catch (CGateException e) {
            logger.warn("Cannot start network sync on network {} - {}", network.getNetworkID(), e.getMessage());
        }
    }

    private @Nullable CBusCGateHandler getCBusCGateHandler() {
        logger.debug("getCBusCGateHandler");
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Required bridge not defined for device.");
            return null;
        }
        ThingHandler handler = bridge.getHandler();
        if (handler instanceof CBusCGateHandler) {
            return (CBusCGateHandler) handler;
        } else {
            logger.debug("No available bridge handler found for bridge: {}.", bridge.getUID());
            return null;
        }
    }

    public @Nullable Network getNetwork() {
        return network;
    }

    public int getNetworkId() {
        CBusNetworkConfiguration configuration = this.configuration;
        if (configuration == null) {
            logger.debug("getNetworkId - NetworkHandler not initialised");
            return -1;
        }
        return configuration.id;
    }

    private @Nullable Project getProjectObject() {
        return projectObject;
    }
}
