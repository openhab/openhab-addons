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
package org.openhab.binding.solaredge.internal.connector;

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.*;

import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.openhab.binding.solaredge.internal.AtomicReferenceTrait;
import org.openhab.binding.solaredge.internal.command.PrivateApiTokenCheck;
import org.openhab.binding.solaredge.internal.command.PublicApiKeyCheck;
import org.openhab.binding.solaredge.internal.command.SolarEdgeCommand;
import org.openhab.binding.solaredge.internal.config.SolarEdgeConfiguration;
import org.openhab.binding.solaredge.internal.handler.SolarEdgeHandler;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The connector is responsible for communication with the solaredge webportal
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class WebInterface implements AtomicReferenceTrait {

    private static final int API_KEY_THRESHOLD = 40;
    private static final int TOKEN_THRESHOLD = 80;

    private final Logger logger = LoggerFactory.getLogger(WebInterface.class);

    /**
     * Configuration
     */
    private SolarEdgeConfiguration config;

    /**
     * handler for updating thing status
     */
    private final SolarEdgeHandler handler;

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
    private final AtomicReference<@Nullable Future<?>> requestExecutorJobReference;

    /**
     * this class is responsible for executing periodic web requests. This ensures that only one request is executed at
     * the same time and there will be a guaranteed minimum delay between subsequent requests.
     *
     * @author afriese - initial contribution
     */
    private class WebRequestExecutor implements Runnable {

        /**
         * queue which holds the commands to execute
         */
        private final Queue<SolarEdgeCommand> commandQueue;

        /**
         * constructor
         */
        WebRequestExecutor() {
            this.commandQueue = new BlockingArrayQueue<>(WEB_REQUEST_QUEUE_MAX_SIZE);
        }

        /**
         * puts a command into the queue
         *
         * @param command
         */
        void enqueue(SolarEdgeCommand command) {
            try {
                commandQueue.add(command);
            } catch (IllegalStateException ex) {
                if (commandQueue.size() >= WEB_REQUEST_QUEUE_MAX_SIZE) {
                    logger.debug(
                            "Could not add command to command queue because queue is already full. Maybe SolarEdge is down?");
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
                                handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                        status.getMessage());
                                setAuthenticated(false);
                                break;
                            case OK:
                                // no action needed as the thing is already online.
                                break;
                            default:
                                handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        status.getMessage());
                                setAuthenticated(false);

                        }
                    }
                };

                SolarEdgeCommand command = commandQueue.poll();
                command.setListener(statusUpdater);
                command.performAction(httpClient);
            }
        }
    }

    /**
     * Constructor to set up interface
     *
     * @param config Bridge configuration
     */
    public WebInterface(ScheduledExecutorService scheduler, SolarEdgeHandler handler, HttpClient httpClient) {
        this.config = handler.getConfiguration();
        this.handler = handler;
        this.scheduler = scheduler;
        this.httpClient = httpClient;
        this.requestExecutor = new WebRequestExecutor();
        this.requestExecutorJobReference = new AtomicReference<>(null);
    }

    public void start() {
        this.config = handler.getConfiguration();
        setAuthenticated(false);
        updateJobReference(requestExecutorJobReference, scheduler.scheduleWithFixedDelay(requestExecutor,
                WEB_REQUEST_INITIAL_DELAY, WEB_REQUEST_INTERVAL, TimeUnit.MILLISECONDS));
    }

    /**
     * queues any command for execution
     *
     * @param command
     */
    public void enqueueCommand(SolarEdgeCommand command) {
        requestExecutor.enqueue(command);
    }

    /**
     * authenticates with the Solaredge WEB interface
     */
    private synchronized void authenticate() {
        setAuthenticated(false);

        if (preCheck()) {
            SolarEdgeCommand tokenCheckCommand;

            StatusUpdateListener tokenCheckListener = new StatusUpdateListener() {

                @Override
                public void update(CommunicationStatus status) {
                    String errorMessageCodeFound;
                    String errorMessgaeCodeForbidden;
                    if (config.isUsePrivateApi()) {
                        errorMessageCodeFound = "login error with private API: invalid token";
                        errorMessgaeCodeForbidden = "login error with private API: invalid solarId";
                    } else {
                        errorMessageCodeFound = "login error with public API: unknown error";
                        errorMessgaeCodeForbidden = "login error with public API: invalid api key or solarId is not valid for this api key";
                    }

                    switch (status.getHttpCode()) {
                        case OK:
                            handler.setStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "logged in");
                            setAuthenticated(true);
                            break;
                        case FOUND:
                            handler.setStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                                    errorMessageCodeFound);
                            setAuthenticated(false);
                            break;
                        case FORBIDDEN:
                            handler.setStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                                    errorMessgaeCodeForbidden);
                            setAuthenticated(false);
                            break;
                        case SERVICE_UNAVAILABLE:
                            handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                    status.getMessage());
                            setAuthenticated(false);
                            break;
                        default:
                            handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    status.getMessage());
                            setAuthenticated(false);
                    }
                }
            };

            if (config.isUsePrivateApi()) {
                tokenCheckCommand = new PrivateApiTokenCheck(handler, tokenCheckListener);
            } else {
                tokenCheckCommand = new PublicApiKeyCheck(handler, tokenCheckListener);
            }
            tokenCheckCommand.performAction(httpClient);
        }
    }

    /**
     * performs some pre cheks on configuration before attempting to login
     *
     * @return true on success, false otherwise
     */
    private boolean preCheck() {
        String preCheckStatusMessage = "";
        String localTokenOrApiKey = config.getTokenOrApiKey();
        String localSolarId = config.getSolarId();

        if (localTokenOrApiKey == null || localTokenOrApiKey.isEmpty()) {
            preCheckStatusMessage = "please configure token/api_key first";
        } else if (localSolarId == null || localSolarId.isEmpty()) {
            preCheckStatusMessage = "please configure solarId first";
        } else if (config.isUsePrivateApi() && localTokenOrApiKey.length() < TOKEN_THRESHOLD) {
            preCheckStatusMessage = "you will have to use a 'token' and not an 'api key' when using private API";
        } else if (!config.isUsePrivateApi() && localTokenOrApiKey.length() > API_KEY_THRESHOLD) {
            preCheckStatusMessage = "you will have to use an 'api key' and not a 'token' when using public API";
        } else if (!config.isUsePrivateApi() && calcRequestsPerDay() > WEB_REQUEST_PUBLIC_API_DAY_LIMIT) {
            preCheckStatusMessage = "daily request limit (" + WEB_REQUEST_PUBLIC_API_DAY_LIMIT + ") exceeded: "
                    + calcRequestsPerDay();
        } else if (config.isUsePrivateApi() && !config.isMeterInstalled()) {
            preCheckStatusMessage = "a meter must be present in order to use the private API";
        } else {
            return true;
        }

        this.handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, preCheckStatusMessage);
        return false;
    }

    /**
     * calculates requests per day. just an internal helper
     *
     * @return
     */
    private long calcRequestsPerDay() {
        return MINUTES_PER_DAY / this.config.getLiveDataPollingInterval()
                + 4 * MINUTES_PER_DAY / this.config.getAggregateDataPollingInterval();
    }

    /**
     * will be called by the ThingHandler to abort periodic jobs.
     */
    public void dispose() {
        logger.debug("Webinterface disposed.");
        cancelJobReference(requestExecutorJobReference);
        setAuthenticated(false);
    }

    /**
     * returns authentication status.
     *
     * @return
     */
    private boolean isAuthenticated() {
        return authenticated;
    }

    private void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
