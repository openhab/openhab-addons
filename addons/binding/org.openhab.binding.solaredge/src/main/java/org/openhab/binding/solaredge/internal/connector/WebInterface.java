/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import org.openhab.binding.solaredge.internal.command.Login;
import org.openhab.binding.solaredge.internal.command.SolarEdgeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The connector is responsible for communication with the solaredge webportal
 *
 * @author afriese - Initial contribution
 */
public class WebInterface {

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
            logger.error("Could not start HTTP Client");
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
                assert true;
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
     * authenticates with the Nibe Uplink WEB interface
     *
     * @throws UnsupportedEncodingException
     */
    public synchronized void authenticate() {
        setAuthenticated(false);

        if (preCheck()) {

            StatusUpdateListener statusUpdater = new StatusUpdateListener() {

                @Override
                public void update(CommunicationStatus status) {

                    if (status.getHttpCode().equals(Code.OK)) {
                        handler.setStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "logged in");
                        setAuthenticated(true);
                    } else if (status.getHttpCode().equals(Code.FOUND)) {
                        handler.setStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                                "invalid token");
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

            new Login(handler, statusUpdater).performAction(asyncclient);
        }
    }

    /**
     * performs some pre cheks on configuration before attempting to login
     *
     * @return error message or SUCCESS
     */
    private boolean preCheck() {
        String preCheckStatusMessage = "";
        if (this.config.getToken() == null) {
            preCheckStatusMessage = "please configure token first";
        } else if (this.config.getSolarId() == null) {
            preCheckStatusMessage = "please configure solarId first";
        } else {
            return true;
        }

        this.handler.setStatusInfo(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR,
                preCheckStatusMessage);
        return false;

    }

    private synchronized boolean isAuthenticated() {
        return authenticated;
    }

    private synchronized void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
