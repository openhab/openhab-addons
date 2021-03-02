/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.flicbutton.internal.handler;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.flicbutton.internal.discovery.FlicButtonDiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.flic.fliclib.javaclient.FlicClient;

/**
 * The {@link FlicDaemonBridgeHandler} handles a running instance of the fliclib-linux-hci server (flicd).
 *
 * @author Patrick Fink - Initial contribution
 */
public class FlicDaemonBridgeHandler extends BaseBridgeHandler {
    private static final Logger logger = LoggerFactory.getLogger(FlicDaemonBridgeHandler.class);
    private static final long REINITIALIZE_DELAY_SECONDS = 10;
    // Config parameters
    private FlicDaemonBridgeConfiguration cfg;
    // Services
    private FlicButtonDiscoveryService buttonDiscoveryService;
    private Future<?> flicClientFuture;
    // For disposal
    private Collection<Future<?>> startedTasks = new ArrayList<>(2);
    private FlicClient flicClient;

    public FlicDaemonBridgeHandler(Bridge bridge, FlicButtonDiscoveryService buttonDiscoveryService) {
        super(bridge);
        this.buttonDiscoveryService = buttonDiscoveryService;
    }

    public FlicClient getFlicClient() {
        return flicClient;
    }

    @Override
    public void initialize() {
        try {
            initConfigParameters();
            startFlicdClientAsync();
            activateButtonDiscoveryService();
            initThingStatus();
        } catch (UnknownHostException ignored) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname wrong or unknown!");
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configuration (hostname, port) is invalid and cannot be parsed.");
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error connecting to flicd!\n" + e);
        }
    }

    private void initConfigParameters() throws UnknownHostException {
        cfg = getConfigAs(FlicDaemonBridgeConfiguration.class);
        cfg.initAndValidate();
    }

    private void activateButtonDiscoveryService() {
        buttonDiscoveryService.activate(flicClient);
    }

    private void startFlicdClientAsync() throws IOException {
        flicClient = new FlicClient(cfg.getHost().getHostAddress(), cfg.getPort());
        Runnable flicClientService = () -> {
            try {
                flicClient.handleEvents();
                logger.warn("Listening to flicd unexpectedly ended");
            } catch (IOException e) {
                logger.debug("Error occured while listening to flicd", e);
            } finally {
                onClientFailure();
            }
        };

        flicClientFuture = scheduler.submit(flicClientService);
        startedTasks.add(flicClientFuture);
    }

    private void onClientFailure() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "flicd client terminated, probably flicd is not reachable.");
        dispose();
        scheduleReinitialize();
    }

    private void initThingStatus() {
        if (!flicClientFuture.isDone()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "flicd client could not be started, probably flicd is not reachable.");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Future<?> startedTask : startedTasks) {
            startedTask.cancel(true);
        }
        startedTasks = new ArrayList<>(2);
        buttonDiscoveryService.deactivate();
    }

    private void scheduleReinitialize() {
        startedTasks.add(scheduler.schedule(this::initialize, REINITIALIZE_DELAY_SECONDS, TimeUnit.SECONDS));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands to the fliclib-linux-hci are supported.
        // So there is nothing to handle in the bridge handler
    }
}
