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
package org.openhab.binding.nibeuplink.internal.connector;

import static org.openhab.binding.nibeuplink.internal.NibeUplinkBindingConstants.*;

import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.openhab.binding.nibeuplink.internal.AtomicReferenceTrait;
import org.openhab.binding.nibeuplink.internal.command.Login;
import org.openhab.binding.nibeuplink.internal.command.NibeUplinkCommand;
import org.openhab.binding.nibeuplink.internal.config.NibeUplinkConfiguration;
import org.openhab.binding.nibeuplink.internal.handler.NibeUplinkHandler;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles requests to the NibeUplink web interface. It manages authentication and wraps commands.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class UplinkWebInterface implements AtomicReferenceTrait {

    private static final int NIBE_ID_THRESHOLD = 14;

    private final Logger logger = LoggerFactory.getLogger(UplinkWebInterface.class);

    /**
     * Configuration
     */
    private NibeUplinkConfiguration config;

    /**
     * handler for updating thing status
     */
    private final NibeUplinkHandler uplinkHandler;

    /**
     * holds authentication status
     */
    private boolean authenticated = false;

    /**
     * HTTP client for asynchronous calls
     */
    private final HttpClient httpClient;

    /**
     * the scheduler which periodically sends web requests to the solaredge API. Should be initiated with the thing's
     * existing scheduler instance.
     */
    private final ScheduledExecutorService scheduler;

    /**
     * request executor
     */
    private final WebRequestExecutor requestExecutor;

    /**
     * periodic request executor job
     */
    private AtomicReference<@Nullable Future<?>> requestExecutorJobReference = new AtomicReference<>(null);

    /**
     * this class is responsible for executing periodic web requests. This ensures that only one request is executed at
     * the same time and there will be a guaranteed minimum delay between subsequent requests.
     *
     * @author afriese - initial contribution
     */
    @NonNullByDefault
    private class WebRequestExecutor implements Runnable {

        /**
         * queue which holds the commands to execute
         */
        private final Queue<@Nullable NibeUplinkCommand> commandQueue;

        /**
         * constructor
         */
        WebRequestExecutor() {
            this.commandQueue = new BlockingArrayQueue<>(WEB_REQUEST_QUEUE_MAX_SIZE);
        }

        /**
         * puts a command into the queue
         *
         * @param command the command which will be queued
         */
        void enqueue(NibeUplinkCommand command) {
            try {
                commandQueue.add(command);
            } catch (IllegalStateException ex) {
                if (commandQueue.size() >= WEB_REQUEST_QUEUE_MAX_SIZE) {
                    logger.debug(
                            "Could not add command to command queue because queue is already full. Maybe NIBE Uplink is down?");
                } else {
                    logger.warn("Could not add command to queue - IllegalStateException");
                }
            }
        }

        /**
         * executes the web request
         */
        @Override
        public void run() {
            if (!isAuthenticated()) {
                authenticate();
            }

            else if (isAuthenticated() && !commandQueue.isEmpty()) {
                StatusUpdateListener statusUpdater = new StatusUpdateListener() {
                    @Override
                    public void update(CommunicationStatus status) {
                        switch (status.getHttpCode()) {
                            case SERVICE_UNAVAILABLE:
                                uplinkHandler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                        status.getMessage());
                                setAuthenticated(false);
                                break;
                            case FOUND:
                                uplinkHandler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                        "most likely your NibeId is wrong. please check your NibeId.");
                                setAuthenticated(false);
                                break;
                            case OK:
                                // no action needed as the thing is already online.
                                break;
                            default:
                                uplinkHandler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        status.getMessage());
                                setAuthenticated(false);
                        }
                    }
                };

                NibeUplinkCommand command = commandQueue.poll();
                if (command != null) {
                    command.setListener(statusUpdater);
                    command.performAction(httpClient);
                }
            }
        }
    }

    /**
     * Constructor to set up interface
     *
     * @param config the Bridge configuration
     */
    public UplinkWebInterface(ScheduledExecutorService scheduler, NibeUplinkHandler handler, HttpClient httpClient) {
        this.config = handler.getConfiguration();
        this.uplinkHandler = handler;
        this.scheduler = scheduler;
        this.requestExecutor = new WebRequestExecutor();
        this.httpClient = httpClient;
    }

    /**
     * starts the periodic request executor job which handles all web requests
     */
    public void start() {
        this.config = uplinkHandler.getConfiguration();
        setAuthenticated(false);
        updateJobReference(requestExecutorJobReference, scheduler.scheduleWithFixedDelay(requestExecutor,
                WEB_REQUEST_INITIAL_DELAY, WEB_REQUEST_INTERVAL, TimeUnit.MILLISECONDS));
    }

    /**
     * queues any command for execution
     *
     * @param command the command which will be put into the queue
     */
    public void enqueueCommand(NibeUplinkCommand command) {
        requestExecutor.enqueue(command);
    }

    /**
     * authenticates with the Nibe Uplink WEB interface
     */
    private synchronized void authenticate() {
        setAuthenticated(false);

        if (preCheck()) {
            StatusUpdateListener statusUpdater = new StatusUpdateListener() {

                @Override
                public void update(CommunicationStatus status) {
                    switch (status.getHttpCode()) {
                        case FOUND:
                            uplinkHandler.setStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "logged in");
                            setAuthenticated(true);
                            break;
                        case OK:
                            uplinkHandler.setStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                                    "invalid username or password");
                            setAuthenticated(false);
                            break;
                        case SERVICE_UNAVAILABLE:
                            uplinkHandler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                    status.getMessage());
                            setAuthenticated(false);
                            break;
                        default:
                            uplinkHandler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    status.getMessage());
                            setAuthenticated(false);
                    }
                }
            };

            new Login(uplinkHandler, statusUpdater).performAction(httpClient);
        }
    }

    /**
     * performs some pre cheks on configuration before attempting to login
     *
     * @return true on success, false otherwise
     */
    private boolean preCheck() {
        String preCheckStatusMessage = "";
        String localPassword = config.getPassword();
        String localUser = config.getUser();
        String localNibeId = config.getNibeId();

        if (localPassword == null || localPassword.isEmpty()) {
            preCheckStatusMessage = "please configure password first";
        } else if (localUser == null || localUser.isEmpty()) {
            preCheckStatusMessage = "please configure user first";
        } else if (localNibeId == null || localNibeId.isEmpty()) {
            preCheckStatusMessage = "please configure nibeId first";
        } else if (localNibeId.length() > NIBE_ID_THRESHOLD) {
            preCheckStatusMessage = "your NibeId is too long. Please refer to the documentation on how to set this value.";
        } else {
            return true;
        }

        this.uplinkHandler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                preCheckStatusMessage);
        return false;
    }

    /**
     * will be called by the ThingHandler to abort periodic jobs.
     */
    public void dispose() {
        logger.debug("Webinterface disposed.");
        cancelJobReference(requestExecutorJobReference);
        setAuthenticated(false);
    }

    private boolean isAuthenticated() {
        return authenticated;
    }

    private void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
