/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.connector;

import java.io.UnsupportedEncodingException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.solaredge.config.SolarEdgeConfiguration;
import org.openhab.binding.solaredge.handler.SolarEdgeHandler;
import org.openhab.binding.solaredge.internal.command.PrivateApiTokenCheck;
import org.openhab.binding.solaredge.internal.command.PublicApiKeyCheck;
import org.openhab.binding.solaredge.internal.command.SolarEdgeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The connector is responsible for communication with the solaredge webportal
 *
 * @author Alexander Friese - initial contribution
 */
public class WebInterface {

    private static final long PUBLIC_API_DAY_LIMIT = 300;
    private static final long MINUTES_PER_DAY = 1440;
    private static final long LOGIN_WAIT_TIME = 1000;

    private final Logger logger = LoggerFactory.getLogger(WebInterface.class);

    /**
     * Configuration of the bridge from
     * {@link org.openhab.BoxHandler.fritzaha.handler.FritzAhaBridgeHandler}
     */
    private final SolarEdgeConfiguration config;

    /**
     * Bridge thing handler for updating thing status
     */
    private final SolarEdgeHandler handler;

    /**
     * holds authentication status
     */
    private boolean authenticated = false;

    /**
     * HTTP client for asynchronous calls
     */
    private HttpClient asyncclient;
    /**
     * Maximum number of simultaneous asynchronous connections
     */
    private int asyncmaxconns = 20;

    /**
     * Constructor to set up interface
     *
     * @param config Bridge configuration
     */
    public WebInterface(SolarEdgeConfiguration config, SolarEdgeHandler handler) {
        this.config = config;
        this.handler = handler;
        asyncclient = new HttpClient(new SslContextFactory(true));
        asyncclient.setMaxConnectionsPerDestination(asyncmaxconns);
        try {
            asyncclient.start();
        } catch (Exception e) {
            logger.warn("Could not start HTTP Client");
        }
        authenticate();
    }

    /**
     * executes any command provided by parameter
     *
     * @param command
     */
    public void executeCommand(SolarEdgeCommand command) {
        if (!isAuthenticated()) {
            authenticate();
            try {
                Thread.sleep(LOGIN_WAIT_TIME);
            } catch (InterruptedException e) {
            }
        }

        if (isAuthenticated()) {
            StatusUpdateListener statusUpdater = new StatusUpdateListener() {

                @Override
                public void update(CommunicationStatus status) {
                    if (status.getHttpCode().equals(Code.SERVICE_UNAVAILABLE)) {
                        handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                status.getMessage());
                        setAuthenticated(false);
                    } else if (!status.getHttpCode().equals(Code.OK)) {
                        handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                status.getMessage());
                        setAuthenticated(false);
                    }

                }
            };

            command.setListener(statusUpdater);
            command.performAction(asyncclient);
        }

    }

    /**
     * authenticates with the Solaredge WEB interface
     *
     * @throws UnsupportedEncodingException
     */
    public synchronized void authenticate() {
        setAuthenticated(false);

        if (preCheck()) {

            SolarEdgeCommand tokenCheckCommand;

            StatusUpdateListener tokenCheckListener = new StatusUpdateListener() {

                @Override
                public void update(CommunicationStatus status) {

                    if (status.getHttpCode().equals(Code.OK)) {
                        handler.setStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "logged in");
                        setAuthenticated(true);
                    } else if (status.getHttpCode().equals(Code.FOUND)) {
                        handler.setStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                                "invalid token");
                        setAuthenticated(false);
                    } else if (status.getHttpCode().equals(Code.FORBIDDEN)) {
                        handler.setStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                                "invalid api key or solarId is not valid for this api key");
                        setAuthenticated(false);
                    } else if (status.getHttpCode().equals(Code.SERVICE_UNAVAILABLE)) {
                        handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                status.getMessage());
                        setAuthenticated(false);
                    } else {
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
            tokenCheckCommand.performAction(asyncclient);
        }
    }

    /**
     * performs some pre cheks on configuration before attempting to login
     *
     * @return error message or SUCCESS
     */
    private boolean preCheck() {
        String preCheckStatusMessage = "";
        if (this.config.getTokenOrApiKey() == null) {
            preCheckStatusMessage = "please configure token/api_key first";
        } else if (this.config.getSolarId() == null || this.config.getSolarId().isEmpty()) {
            preCheckStatusMessage = "please configure solarId first";
        } else if (this.config.isUsePrivateApi() == false && calcRequestsPerDay() > PUBLIC_API_DAY_LIMIT) {
            preCheckStatusMessage = "daily request limit (" + PUBLIC_API_DAY_LIMIT + ") exceeded: "
                    + calcRequestsPerDay();
        } else if (this.config.isUsePrivateApi() && !this.config.isMeterInstalled()) {
            preCheckStatusMessage = "a meter must be present in order to use the private API";
        } else {
            return true;
        }

        this.handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, preCheckStatusMessage);
        return false;

    }

    private long calcRequestsPerDay() {
        return MINUTES_PER_DAY / this.config.getLiveDataPollingInterval()
                + 4 * MINUTES_PER_DAY / this.config.getAggregateDataPollingInterval();
    }

    /**
     * returns authentication status.
     *
     * @return
     */
    private synchronized boolean isAuthenticated() {
        return authenticated;
    }

    private synchronized void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

}
