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
package org.openhab.binding.cbus.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.cbus.CBusBindingConstants;
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
    private @Nullable CBusCGateHandler bridgeHandler = null;
    private @Nullable Network network = null;
    private @Nullable Project projectObject = null;
    private @Nullable ScheduledFuture<?> initNetwork = null;
    private @Nullable ScheduledFuture<?> networkSync = null;

    public CBusNetworkHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        logger.debug("initialize --");
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
        } else
            cgateOnline();
    }

    private void cgateOnline() {
        ThingStatus lastStatus = getThing().getStatus();
        logger.debug("cgateOnline {}", lastStatus);
        String networkID = getConfig().get(CBusBindingConstants.CONFIG_NETWORK_ID).toString();
        String project = getConfig().get(CBusBindingConstants.CONFIG_NETWORK_PROJECT).toString();
        logger.debug("cgateOnline netid {} project {}", networkID, project);
        Project projectObject = getProjectObject();
        Network network = getNetwork();
        logger.debug("network {}", network);
        CBusCGateHandler cbusCGateHandler = getCBusCGateHandler();
        logger.debug("cgateHandler {}", cbusCGateHandler);
        if (cbusCGateHandler == null) {
            logger.debug("NoCGateHandler");
            return;
        }
        try {
            logger.debug("projectobject {}", projectObject);
            if (projectObject == null) {
                CGateSession session = cbusCGateHandler.getCGateSession();
                if (session != null) {
                    try {
                        projectObject = (Project) session.getCGateObject("//" + project);
                        this.projectObject = projectObject;
                    } catch (CGateException e2) {
                        // We dont need to do anything other than stop this propagating
                    }
                }
                if (projectObject == null) {
                    logger.debug("Cant get projectobject");
                    return;
                }
            }
            logger.debug("projectobject {}", projectObject);
            if (network == null) {
                CGateSession session = cbusCGateHandler.getCGateSession();
                if (session != null) {
                    try {
                        network = (Network) session.getCGateObject("//" + project + "/" + networkID);
                        this.network = network;
                    } catch (CGateException e2) {
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
                    this.initNetwork = scheduler.scheduleWithFixedDelay(() -> {
                        checkNetworkOnline();
                    }, 30, 30, TimeUnit.SECONDS);
                    logger.debug("Schedule a check every minute");
                } else
                    logger.debug("initNetwork alreadys started");
                updateStatus();
                return;
            }
            logger.debug("State should be ok !!!!!!");
        } catch (CGateException e) {
            logger.warn("Cannot load C-Bus network {}", networkID, e);
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.COMMUNICATION_ERROR);
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
                }
                updateStatus();
            } else {
                ThingStatus lastStatus = getThing().getStatus();
                logger.debug("Network still not online {}", lastStatus);
                updateStatus();
            }
        } catch (CGateException e) {
            logger.warn("Cannot check if network is online {} ", network.getNetworkID());
            updateStatus();
        }
    }

    private void updateStatus() {
        logger.debug("updateStatus");
        ThingStatus lastStatus = getThing().getStatus();
        Network network = getNetwork();
        CBusCGateHandler cbusCGateHandler = getCBusCGateHandler();
        try {
            if (cbusCGateHandler == null || !cbusCGateHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "CGate connection offline");
            } else if (network == null) {
                logger.debug("No network - set configuration error");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            } else if (network.isOnline()) {
                logger.debug("Network is online");
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("Network is offline");
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
                if (networkSync == null || networkSync.isCancelled())
                    this.networkSync = scheduler.scheduleWithFixedDelay(() -> {
                        doNetworkSync();
                    }, 10, Integer.parseInt(getConfig().get(CBusBindingConstants.CONFIG_NETWORK_SYNC).toString()),
                            TimeUnit.SECONDS);
                logger.debug("Schedule networkSync to start in {} seconds",
                        Integer.parseInt(getConfig().get(CBusBindingConstants.CONFIG_NETWORK_SYNC).toString()));
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

    public @Nullable synchronized CBusCGateHandler getCBusCGateHandler() {
        if (this.bridgeHandler == null) {
            logger.debug("getCBusCGateHandler --");
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.warn("Required bridge not defined for device.");
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            logger.debug("handler is {} ", handler);
            if (handler instanceof CBusCGateHandler) {
                this.bridgeHandler = (CBusCGateHandler) handler;
            } else {
                logger.debug("No available bridge handler found for bridge: {} .", bridge.getUID());
                this.bridgeHandler = null;
            }
        }
        return bridgeHandler;
    }

    public @Nullable Network getNetwork() {
        return network;
    }

    private @Nullable Project getProjectObject() {
        return projectObject;
    }
}
