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
package org.openhab.binding.easee.internal.connector;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.openhab.binding.easee.internal.AtomicReferenceTrait;
import org.openhab.binding.easee.internal.UtilsTrait;
import org.openhab.binding.easee.internal.command.EaseeCommand;
import org.openhab.binding.easee.internal.command.account.Login;
import org.openhab.binding.easee.internal.command.account.RefreshToken;
import org.openhab.binding.easee.internal.handler.EaseeHandler;
import org.openhab.binding.easee.internal.model.GenericErrorResponse;
import org.openhab.binding.easee.internal.model.account.AuthenticationDataResponse;
import org.openhab.binding.easee.internal.model.account.AuthenticationResultData;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The connector is responsible for communication with the Easee Cloud API
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class WebInterface implements AtomicReferenceTrait, UtilsTrait {

    private final Logger logger = LoggerFactory.getLogger(WebInterface.class);

    /**
     * handler for updating thing status
     */
    private final EaseeHandler handler;

    /**
     * holds authentication status
     */
    private boolean authenticated = false;

    /**
     * access token returned by login, needed to authenticate all requests send to API.
     */
    private String accessToken;

    /**
     * refresh token returned by login, needed for refreshing the access token.
     */
    private String refreshToken;

    /**
     * expiry of the access token.
     */
    private Date tokenExpiry;

    /**
     * last refresh of the access token.
     */
    private Date tokenRefreshDate;

    /**
     * HTTP client for asynchronous calls
     */
    private final HttpClient httpClient;

    /**
     * the scheduler which periodically sends web requests to the Easee Cloud API. Should be initiated with the thing's
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
     * this class is responsible for executing periodic web requests. This ensures that only one request is executed
     * at the same time and there will be a guaranteed minimum delay between subsequent requests.
     *
     * @author afriese - initial contribution
     */
    private class WebRequestExecutor implements Runnable {

        /**
         * queue which holds the commands to execute
         */
        private final Queue<EaseeCommand> commandQueue;

        /**
         * constructor
         */
        WebRequestExecutor() {
            this.commandQueue = new BlockingArrayQueue<>(WEB_REQUEST_QUEUE_MAX_SIZE);
        }

        private final StatusUpdateListener authenticationListener = new StatusUpdateListener() {
            @Override
            public void update(CommunicationStatus status, @Nullable AuthenticationResultData data) {
                GenericErrorResponse response = data != null ? data.getErrorResponse() : null;
                String msg = response != null ? response.getTitle() : "";
                if (msg.isBlank()) {
                    msg = status.getMessage();
                }

                switch (status.getHttpCode()) {
                    case BAD_REQUEST:
                        handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                        setAuthenticated(false);
                        break;
                    case SERVICE_UNAVAILABLE:
                        handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, msg);
                        setAuthenticated(false);
                        break;
                    case OK:
                        AuthenticationDataResponse resp = data != null ? data.getSuccessResponse() : null;
                        if (resp != null && resp.isValidLogin()) {
                            accessToken = resp.accessToken;
                            refreshToken = resp.refreshToken;
                            tokenRefreshDate = new Date();
                            tokenExpiry = new Date(
                                    System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(resp.expiresIn));
                            handler.setStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE,
                                    "Access Token Refreshed/Validated");
                            setAuthenticated(true);
                            break;
                        }
                    default:
                        handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
                        setAuthenticated(false);
                }
            }
        };

        /**
         * puts a command into the queue
         *
         * @param command
         */
        void enqueue(EaseeCommand command) {
            try {
                commandQueue.add(command);
            } catch (IllegalStateException ex) {
                if (commandQueue.size() >= WEB_REQUEST_QUEUE_MAX_SIZE) {
                    logger.debug(
                            "Could not add command to command queue because queue is already full. Maybe Easee Cloud is down?");
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
            } else {
                refreshAccessToken();

                if (isAuthenticated() && !commandQueue.isEmpty()) {
                    executeCommand();
                }
            }
        }

        /**
         * authenticates with the Easee Cloud interface.
         */
        private synchronized void authenticate() {
            setAuthenticated(false);
            EaseeCommand loginCommand = new Login(handler);
            loginCommand.setListener(authenticationListener);
            loginCommand.performAction(httpClient);
        }

        /**
         * periodically refreshed the access token.
         */
        private synchronized void refreshAccessToken() {
            Date now = new Date();
            long expiryBuffer = TimeUnit.MINUTES.toMillis(WEB_REQUEST_TOKEN_EXPIRY_BUFFER_MINUTES);
            long maxAge = TimeUnit.MINUTES.toMillis(WEB_REQUEST_TOKEN_MAX_AGE_MINUTES);

            if (tokenExpiry.getTime() - now.getTime() - expiryBuffer < 0
                    || tokenRefreshDate.getTime() + maxAge < now.getTime()) {
                logger.debug("access token needs to be refreshed, last refresh: {}, expiry: {}",
                        formatDate(tokenRefreshDate), formatDate(tokenRefreshDate));

                EaseeCommand refreshCommand = new RefreshToken(handler, accessToken, refreshToken);
                refreshCommand.setListener(authenticationListener);
                refreshCommand.performAction(httpClient);
            }
        }

        /**
         * executes the next command in the queue. requires authenticated session.
         */
        private void executeCommand() {
            EaseeCommand command = commandQueue.poll();
            if (command != null) {

                StatusUpdateListener statusUpdater = new StatusUpdateListener() {
                    @Override
                    public void update(CommunicationStatus status, @Nullable AuthenticationResultData data) {
                        GenericErrorResponse response = data != null ? data.getErrorResponse() : null;
                        String msg = response != null ? response.getTitle() : "";
                        if (msg.isBlank()) {
                            msg = status.getMessage();
                        }

                        switch (status.getHttpCode()) {
                            case SERVICE_UNAVAILABLE:
                                handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, msg);
                                setAuthenticated(false);
                                break;
                            case OK:
                                // no action needed as the thing is already online.
                                break;
                            default:
                                handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
                                setAuthenticated(false);

                        }
                    }
                };
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
    public WebInterface(ScheduledExecutorService scheduler, EaseeHandler handler, HttpClient httpClient) {
        this.handler = handler;
        this.scheduler = scheduler;
        this.httpClient = httpClient;
        this.tokenExpiry = INVALID_DATE;
        this.tokenRefreshDate = INVALID_DATE;
        this.accessToken = "";
        this.refreshToken = "";
        this.requestExecutor = new WebRequestExecutor();
        this.requestExecutorJobReference = new AtomicReference<>(null);
    }

    public void start() {
        setAuthenticated(false);
        updateJobReference(requestExecutorJobReference, scheduler.scheduleWithFixedDelay(requestExecutor,
                WEB_REQUEST_INITIAL_DELAY, WEB_REQUEST_INTERVAL, TimeUnit.MILLISECONDS));
    }

    /**
     * queues any command for execution
     *
     * @param command
     */
    public void enqueueCommand(EaseeCommand command) {
        requestExecutor.enqueue(command);
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

    /**
     * update the authentication status, also resets token data.
     *
     * @param authenticated
     */
    private void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        if (!authenticated) {
            this.tokenExpiry = INVALID_DATE;
            this.accessToken = "";
            this.refreshToken = "";
        }
    }

    /**
     * returns the current access token.
     *
     * @return
     */
    public String getAccessToken() {
        return accessToken;
    }

}
