/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.connector;

import java.io.UnsupportedEncodingException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.nibeuplink.config.NibeUplinkConfiguration;
import org.openhab.binding.nibeuplink.handler.NibeUplinkHandler;
import org.openhab.binding.nibeuplink.internal.command.Login;
import org.openhab.binding.nibeuplink.internal.command.NibeUplinkCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles requests to the NibeUplink web interface. It manages authentication and wraps commands.
 *
 * @author afriese
 *
 */
public class UplinkWebInterface {

    private static final long LOGIN_WAIT_TIME = 1000;

    private final Logger logger = LoggerFactory.getLogger(UplinkWebInterface.class);

    /**
     * Configuration of the bridge from
     * {@link org.openhab.BoxHandler.fritzaha.handler.FritzAhaBridgeHandler}
     */
    private final NibeUplinkConfiguration config;

    /**
     * Bridge thing handler for updating thing status
     */
    private final NibeUplinkHandler uplinkHandler;

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
    public UplinkWebInterface(NibeUplinkConfiguration config, NibeUplinkHandler handler) {
        this.config = config;
        this.uplinkHandler = handler;
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
    public void executeCommand(NibeUplinkCommand command) {
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
                        uplinkHandler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                status.getMessage());
                        setAuthenticated(false);
                    } else if (!status.getHttpCode().equals(Code.OK)) {
                        uplinkHandler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
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
                    if (status.getHttpCode().equals(Code.FOUND)) {
                        uplinkHandler.setStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "logged in");
                        setAuthenticated(true);
                    } else if (status.getHttpCode().equals(Code.OK)) {
                        uplinkHandler.setStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                                "invalid username or password");
                        setAuthenticated(false);
                    } else if (status.getHttpCode().equals(Code.SERVICE_UNAVAILABLE)) {
                        uplinkHandler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                status.getMessage());
                        setAuthenticated(false);
                    } else {
                        uplinkHandler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                status.getMessage());
                        setAuthenticated(false);
                    }

                }
            };

            new Login(uplinkHandler, statusUpdater).performAction(asyncclient);
        }
    }

    /**
     * performs some pre cheks on configuration before attempting to login
     *
     * @return error message or SUCCESS
     */
    private boolean preCheck() {
        String preCheckStatusMessage = "";
        if (this.config.getPassword() == null || this.config.getPassword().isEmpty()) {
            preCheckStatusMessage = "please configure password first";
        } else if (this.config.getUser() == null || this.config.getUser().isEmpty()) {
            preCheckStatusMessage = "please configure user first";
        } else if (this.config.getNibeId() == null || this.config.getNibeId().isEmpty()) {
            preCheckStatusMessage = "please configure nibeId first";
        } else {
            return true;
        }

        this.uplinkHandler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
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
