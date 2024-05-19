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
package org.openhab.binding.myuplink.internal.connector;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.openhab.binding.myuplink.internal.AtomicReferenceTrait;
import org.openhab.binding.myuplink.internal.Utils;
import org.openhab.binding.myuplink.internal.command.MyUplinkCommand;
import org.openhab.binding.myuplink.internal.command.account.Login;
import org.openhab.binding.myuplink.internal.handler.MyUplinkBridgeHandler;
import org.openhab.binding.myuplink.internal.handler.StatusHandler;
import org.openhab.binding.myuplink.internal.model.ValidationException;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The connector is responsible for communication with the NIBE myUplink Cloud API
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class WebInterface implements AtomicReferenceTrait {

    private final Logger logger = LoggerFactory.getLogger(WebInterface.class);

    /**
     * bridge handler
     */
    private final MyUplinkBridgeHandler handler;

    /**
     * handler for updating bridge status
     */
    private final StatusHandler bridgeStatusHandler;

    /**
     * holds authentication status
     */
    private boolean authenticated = false;

    /**
     * access token returned by login, needed to authenticate all requests send to API.
     */
    private String accessToken;

    /**
     * expiry of the access token.
     */
    private Instant tokenExpiry;

    /**
     * last refresh of the access token.
     */
    private Instant tokenRefreshDate;

    /**
     * HTTP client for asynchronous calls
     */
    private final HttpClient httpClient;

    /**
     * the scheduler which periodically sends web requests to the NIBE myUplink Cloud API. Should be initiated with the
     * thing's
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
        private final Queue<MyUplinkCommand> commandQueue;

        /**
         * constructor
         */
        WebRequestExecutor() {
            this.commandQueue = new BlockingArrayQueue<>(WEB_REQUEST_QUEUE_MAX_SIZE);
        }

        private void processAuthenticationResult(CommunicationStatus status, JsonObject jsonObject) {
            String msg = Utils.getAsString(jsonObject, JSON_KEY_ERROR);
            if (msg == null || msg.isBlank()) {
                msg = status.getMessage();
            }

            switch (status.getHttpCode()) {
                case BAD_REQUEST:
                    bridgeStatusHandler.updateStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            msg);
                    setAuthenticated(false);
                    break;
                case OK:
                    String accessToken = Utils.getAsString(jsonObject, JSON_KEY_AUTH_ACCESS_TOKEN);
                    int expiresInSeconds = Utils.getAsInt(jsonObject, JSON_KEY_AUTH_EXPIRES_IN);
                    if (accessToken != null && expiresInSeconds != 0) {
                        WebInterface.this.accessToken = accessToken;
                        tokenRefreshDate = Instant.now();
                        tokenExpiry = tokenRefreshDate.plusSeconds(expiresInSeconds);

                        logger.debug("access token refreshed: {}, expiry: {}", tokenRefreshDate.toString(),
                                tokenExpiry.toString());

                        bridgeStatusHandler.updateStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE,
                                STATUS_TOKEN_VALIDATED);
                        setAuthenticated(true);
                        handler.startDiscovery();
                        break;
                    }
                default:
                    bridgeStatusHandler.updateStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            msg);
                    setAuthenticated(false);
            }
        }

        /**
         * puts a command into the queue
         *
         * @param command
         */
        void enqueue(MyUplinkCommand command) {
            try {
                commandQueue.add(command);
            } catch (IllegalStateException ex) {
                if (commandQueue.size() >= WEB_REQUEST_QUEUE_MAX_SIZE) {
                    logger.debug(
                            "Could not add command to command queue because queue is already full. Maybe NIBE myUplink is down?");
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
            logger.debug("run queued commands, queue size is {}", commandQueue.size());
            if (!isAuthenticated()) {
                authenticate();
            } else {
                refreshAccessToken();

                if (isAuthenticated() && !commandQueue.isEmpty()) {
                    try {
                        executeCommand();
                    } catch (Exception ex) {
                        logger.warn("command execution ended with exception:", ex);
                    }
                }
            }
        }

        /**
         * authenticates with the NIBE myUplink Cloud interface.
         */
        private synchronized void authenticate() {
            setAuthenticated(false);
            MyUplinkCommand loginCommand = new Login(handler, this::processAuthenticationResult);
            try {
                loginCommand.performAction(httpClient, "");
            } catch (ValidationException e) {
                // this cannot happen
            }
        }

        /**
         * periodically refreshed the access token.
         */
        private synchronized void refreshAccessToken() {
            Instant now = Instant.now();

            if (now.plus(WEB_REQUEST_TOKEN_EXPIRY_BUFFER_MINUTES, ChronoUnit.MINUTES).isAfter(tokenExpiry)
                    || now.isAfter(tokenRefreshDate.plus(WEB_REQUEST_TOKEN_MAX_AGE_MINUTES, ChronoUnit.MINUTES))) {
                logger.debug("access token needs to be refreshed, last refresh: {}, expiry: {}",
                        tokenRefreshDate.toString(), tokenExpiry.toString());

                MyUplinkCommand refreshCommand = new Login(handler, this::processAuthenticationResult);
                try {
                    refreshCommand.performAction(httpClient, "");
                } catch (ValidationException e) {
                    // this cannot happen
                }
            }
        }

        /**
         * executes the next command in the queue. requires authenticated session.
         *
         * @throws ValidationException
         */
        private void executeCommand() throws ValidationException {
            MyUplinkCommand command = commandQueue.poll();
            if (command != null) {
                command.performAction(httpClient, accessToken);
            }
        }
    }

    /**
     * Constructor to set up interface
     */
    public WebInterface(ScheduledExecutorService scheduler, MyUplinkBridgeHandler handler, HttpClient httpClient,
            StatusHandler bridgeStatusHandler) {
        this.handler = handler;
        this.bridgeStatusHandler = bridgeStatusHandler;
        this.scheduler = scheduler;
        this.httpClient = httpClient;
        this.tokenExpiry = OUTDATED_DATE;
        this.tokenRefreshDate = OUTDATED_DATE;
        this.accessToken = "";
        this.requestExecutor = new WebRequestExecutor();
        this.requestExecutorJobReference = new AtomicReference<>(null);
    }

    public void start() {
        setAuthenticated(false);
        updateJobReference(requestExecutorJobReference, scheduler.scheduleWithFixedDelay(requestExecutor,
                WEB_REQUEST_INITIAL_DELAY, WEB_REQUEST_INTERVAL, TimeUnit.SECONDS));
    }

    /**
     * queues any command for execution
     *
     * @param command
     */
    public void enqueueCommand(MyUplinkCommand command) {
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
            this.tokenExpiry = OUTDATED_DATE;
            this.accessToken = "";
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
