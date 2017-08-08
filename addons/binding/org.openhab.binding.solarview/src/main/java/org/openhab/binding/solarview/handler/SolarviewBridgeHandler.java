/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solarview.handler;

import static org.openhab.binding.solarview.SolarviewBindingConstants.THING_TYPE_BRIDGE;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.solarview.internal.Energy;
import org.openhab.binding.solarview.internal.config.SolarviewBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolarviewBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Guenther Schreiner - Initial contribution
 */
public class SolarviewBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SolarviewBridgeHandler.class);
    /** Set of things provided by {@link SolarviewBridgeHandler}. */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    /** Configuration options for {@link SolarviewBridgeHandler}. */
    private SolarviewBridgeConfiguration configuration = null;
    /** Communication handle towards Solarview server. */
    private Socket clientSocket = null;
    /** Solarview query string. */
    private String SOLARVIEW_REQUEST_EOL = ".";

    public SolarviewBridgeHandler(Bridge bridge) {
        super(bridge);
        logger.debug("Creating a SolarviewBridgeHandler for thing '{}'.", getThing().getUID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        logger.info("Initializing Solarview bridge handler for '{}'.", getThing().getUID());
        updateStatus(ThingStatus.OFFLINE);

        configuration = getConfigAs(SolarviewBridgeConfiguration.class);

        if (configuration != null) {
            logger.trace("No configuration found, using default values.");
            configuration = new SolarviewBridgeConfiguration();
        }
        if (!connect()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unable to connect Solarview Bridge.");
            return;
        }
        updateStatus(ThingStatus.ONLINE);
        super.initialize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void dispose() {
        logger.trace("Shutting down Solarview bridge '{}'.", getThing().getUID());

        if (clientSocket != null) {
            disconnect();
        }

        super.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand({},{}) called.", channelUID.getAsString(), command);
        logger.debug("Bridge commands not supported.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        logger.trace("childHandlerInitialized({},{}) called.", childHandler, childThing);
        super.childHandlerInitialized(childHandler, childThing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        logger.trace("childHandlerDisposed({},{}) called.", childHandler, childThing);
        super.childHandlerDisposed(childHandler, childThing);
    }

    /**
     * Initializes a TCP connection handle towards <b>Solarview</b> server
     * based on the parameters provided by {@link SolarviewBridgeConfiguration}.
     *
     * @return Success of the initialization.
     */
    private synchronized boolean connect() {
        logger.trace("connect() called.");

        do {
            if (clientSocket != null) {
                logger.trace("handle already exists, therefore initiating a reconnect (disconnect/connect).");
                disconnect();
            }

            try {
                logger.trace("connect() tries to open connection endpoint towards ({},TCP/{}).", configuration.hostName,
                        configuration.tcpPort);
                clientSocket = new Socket(configuration.hostName, configuration.tcpPort);
            } catch (IOException e) {
                logger.info("Exception occurred on socket creation towards ({},TCP/{}): {}.", configuration.hostName,
                        configuration.tcpPort, e.getMessage());
                clientSocket = null;
                break;
            }

            try {
                logger.trace("connect() tries to set timeout to {} msecs.", configuration.timeoutMsecs);
                clientSocket.setSoTimeout(configuration.timeoutMsecs);
            } catch (SocketException e) {
                logger.info("Exception occurred during setting socket timeout: {}.", e.getMessage());
                try {
                    clientSocket.close();
                } catch (IOException ioe) {
                    logger.info("Exception occurred during closing socket: {}.", ioe.getMessage());
                }
                clientSocket = null;
            }
        } while (false);

        /**
         * Adapt bridge Status according to connection handle
         */
        if (clientSocket != null) {
            if (thing.getStatus() != ThingStatus.ONLINE) {
                logger.trace("connect() changes bridge status to ONLINE.");
                updateStatus(ThingStatus.ONLINE);
            }
            logger.trace("connect() finished successfully.");
            return true;
        } else {
            if (thing.getStatus() != ThingStatus.OFFLINE) {
                logger.trace("connect() changes bridge status to OFFLINE.");
                updateStatus(ThingStatus.OFFLINE);
            }
            logger.trace("connect() finished unsuccessfully.");
            return false;
        }
    }

    /**
     * Free the connection handle.
     */
    private synchronized void disconnect() {
        logger.trace("disconnect() called.");

        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.info("Exception occurred during closing socket: {}.", e.getMessage());
            }
            clientSocket = null;
        }
        logger.trace("disconnect() finished.");
    }

    /**
     * Asks the <b>Solarview</b> server for an information update of the
     * set of information identified by queryString and
     * returns the set of results in a common information structure
     * of type {@link Energy}.
     *
     * @param queryString
     *            String to be sent as query.
     * @return Energy
     *         - Set of energy information values.
     */
    public synchronized Energy updateEnergyDataFromServer(String queryString) {
        logger.trace("updateEnergyDataFromServer({}) called.", queryString);

        /** Energy information as JSON string response */
        String energyInformationString;
        /** Energy information as common structure */
        Energy thisEnergy = null;

        do {
            if (clientSocket == null) {
                logger.trace(
                        "updateEnergyDataFromServer(): communication handle does not exist, therefore initiate a connect.");
                connect();
            }
            if (clientSocket == null) {
                logger.trace(
                        "updateEnergyDataFromServer(): communication handle does still not exist, therefore abort this try.");
                break;
            }
            try {
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                logger.trace("updateEnergyDataFromServer(): sending request to server.");
                outToServer.writeBytes(queryString + SOLARVIEW_REQUEST_EOL);
                logger.trace("updateEnergyDataFromServer(): trying to receive response from server.");
                energyInformationString = inFromServer.readLine();
                logger.trace("updateEnergyDataFromServer(): got response {}.", energyInformationString);
                thisEnergy = new Energy(energyInformationString);

            } catch (IOException e) {
                logger.info("Exception occurred during reading on socket: {}.", e.getMessage());
            }
        } while (false);
        logger.trace("updateEnergyDataFromServer(): tear down the handle for later reinitialization.");
        disconnect();
        logger.debug("updateEnergyDataFromServer() returns {} energy information.",
                (thisEnergy == null) ? "no" : "retrieved");
        return thisEnergy;
    }

}
/**
 * end-of-SolarviewBridgeHandler.java
 */
