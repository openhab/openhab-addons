/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.somfymylink.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link SomfyMyLinkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Johnson - Initial contribution
 */
public class SomfyMyLinkHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyMyLinkHandler.class);

    private SomfyMyLinkConfiguration config;

    private static final int MYLINK_PORT = 44100;
    public static final int MYLINK_DEFAULT_TIMEOUT = 10000;

    private ServiceRegistration<DiscoveryService> discoveryServiceRegistration;

    // Gson & parser
    private final Gson gson = new Gson();

    public SomfyMyLinkHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        this.config = getThing().getConfiguration().as(SomfyMyLinkConfiguration.class);

        if (validConfiguration(this.config)) {
            SomfyMyLinkDeviceDiscoveryService discovery = new SomfyMyLinkDeviceDiscoveryService(this);

            this.discoveryServiceRegistration = this.bundleContext.registerService(DiscoveryService.class, discovery,
                    null);

            discovery.activate(null);

            this.scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    connect();
                }
            }, 0, TimeUnit.SECONDS);
        }
    }

    private boolean validConfiguration(SomfyMyLinkConfiguration config) {

        // if (this.config == null) {
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "mylink configuration missing");
        // return false;
        // }

        if (StringUtils.isEmpty(this.config.getIpAddress())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "mylink address not specified");

            return false;
        }

        return true;
    }

    private synchronized void connect() {
        // if (this.session.isConnected()) {
        // return;
        // }

        logger.debug("Connecting to mylink at {}", config.getIpAddress());

        /*
         * try {
         * if (!login(config)) {
         * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "invalid username/password");
         *
         * return;
         * }
         *
         * // Disable prompts
         * sendCommand(new LutronCommand(LutronOperation.EXECUTE, LutronCommandType.MONITORING, -1, MONITOR_PROMPT,
         * MONITOR_DISABLE));
         *
         * // Check the time device database was last updated. On the initial connect, this will trigger
         * // a scan for paired devices.
         * sendCommand(
         * new LutronCommand(LutronOperation.QUERY, LutronCommandType.SYSTEM, -1, SYSTEM_DBEXPORTDATETIME));
         * } catch (LutronSafemodeException e) {
         * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "main repeater is in safe mode");
         * disconnect();
         * scheduleConnectRetry(reconnectInterval); // Possibly a temporary problem. Try again later.
         *
         * return;
         * } catch (IOException e) {
         * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
         * disconnect();
         * scheduleConnectRetry(reconnectInterval); // Possibly a temporary problem. Try again later.
         *
         * return;
         * } catch (InterruptedException e) {
         * Thread.currentThread().interrupt();
         *
         * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "login interrupted");
         * disconnect();
         *
         * return;
         * }
         *
         *
         * this.messageSender = this.scheduler.schedule(new Runnable() {
         *
         * @Override
         * public void run() {
         * sendCommands();
         * }
         * }, 0, TimeUnit.SECONDS);
         *
         */

        logger.debug("Connected to mylink at {}", config.getIpAddress());

        updateStatus(ThingStatus.ONLINE);

        // keepAlive = scheduler.scheduleWithFixedDelay(this::sendKeepAlive, heartbeatInterval, heartbeatInterval,
        // TimeUnit.MINUTES);
    }

    private synchronized void disconnect() {
        logger.debug("Disconnecting from mylink");

        /*
         * if (connectRetryJob != null) {
         * connectRetryJob.cancel(true);
         * }
         *
         * if (this.keepAlive != null) {
         * this.keepAlive.cancel(true);
         * }
         *
         * if (this.keepAliveReconnect != null) {
         * // This method can be called from the keepAliveReconnect thread. Make sure
         * // we don't interrupt ourselves, as that may prevent the reconnection attempt.
         * this.keepAliveReconnect.cancel(false);
         * }
         *
         * if (this.messageSender != null) {
         * this.messageSender.cancel(true);
         * }
         *
         *
         * try {
         * this.session.close();
         * } catch (IOException e) {
         * logger.error("Error disconnecting", e);
         * }
         *
         */
    }

    public SomfyMyLinkShade[] getShadeList() throws SomfyMyLinkException {
        String targetId = "*.*";
        String command = "mylink.status.info";

        SomfyMyLinkShadesResponse response = (SomfyMyLinkShadesResponse) sendCommandWithResponse(command, targetId,
                SomfyMyLinkShadesResponse.class);

        if (response != null) {
            return response.getResult();
        }

        return null;
    }

    public SomfyMyLinkScene[] getSceneList() throws SomfyMyLinkException {
        String targetId = "*.*";
        String command = "mylink.scene.list";

        SomfyMyLinkScenesResponse response = (SomfyMyLinkScenesResponse) sendCommandWithResponse(command, targetId,
                SomfyMyLinkScenesResponse.class);

        if (response != null) {
            return response.getResult();
        }

        return null;
    }

    public void commandShadeUp(String targetId) throws SomfyMyLinkException {

        try {
            sendCommand("mylink.move.up", targetId);
        } catch (Exception e) {
            logger.error("Error commanding shade up: " + e.getMessage());
            throw new SomfyMyLinkException("Error commanding shade up", e);
        }
    }

    public void commandShadeDown(String targetId) throws SomfyMyLinkException {

        try {
            sendCommand("mylink.move.down", targetId);
        } catch (Exception e) {
            logger.error("Error commanding shade down: " + e.getMessage());
            throw new SomfyMyLinkException("Error commanding shade down", e);
        }
    }

    public void commandShadeStop(String targetId) throws SomfyMyLinkException {

        try {
            sendCommand("mylink.move.stop", targetId);
        } catch (Exception e) {
            logger.error("Error commanding shade stop: " + e.getMessage());
            throw new SomfyMyLinkException("Error commanding shade stop", e);
        }
    }

    private void sendCommand(String command, String targetId) throws SomfyMyLinkException {
        String myLinkCommand = buildCommand(command, targetId);
        try {

            Socket socket = getConnection();
            OutputStream out = socket.getOutputStream();

            try {
                byte[] sendBuffer = myLinkCommand.getBytes(StandardCharsets.US_ASCII);
                // send the command
                out.write(sendBuffer, 0, sendBuffer.length);
            } finally {
                // cleanup
                try {
                    out.close();
                    socket.close();
                } catch (SocketException e) {
                } catch (IOException e) {
                }
            }
        } catch (SocketTimeoutException e) {
            logger.error("Timeout getting Somfy MyLink shade list " + e.getMessage());
            throw new SomfyMyLinkException("Connection issue discovering Somfy devices", e);

        } catch (Exception e) {
            logger.error("Error getting Somfy MyLink shade list " + e.getMessage());
            throw new SomfyMyLinkException("Error discovering Somfy devices", e);
        }
    }

    private SomfyMyLinkResponseBase sendCommandWithResponse(String command, String targetId, Type responseType)
            throws SomfyMyLinkException {

        String myLinkCommand = buildCommand(command, targetId);

        try {
            Socket socket = getConnection();
            OutputStream out = socket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            try {
                byte[] sendBuffer = myLinkCommand.getBytes(StandardCharsets.US_ASCII);

                // send the command
                out.write(sendBuffer, 0, sendBuffer.length);

                // now read the reply
                char[] readBuff = new char[1024];
                int readCount;
                String message = "";

                // while we are getting data ...
                while (((readCount = in.read(readBuff)) != -1)) {
                    logger.debug("Got response. Len: " + readCount);
                    message += new String(readBuff, 0, readCount);

                    try {

                        SomfyMyLinkResponseBase data = gson.fromJson(message, responseType);

                        // check if there was an error
                        if (data.getError() != null) {
                            logger.error("Error communicating with mylink: " + data.getError());
                            throw new SomfyMyLinkException("Error communicating with mylink:" + data.getError());
                        }

                        return data;

                    } catch (JsonSyntaxException e) {
                        // it wasn't a full message?
                        logger.debug("Partial message recieved.");
                    }
                }
            } finally {
                // cleanup
                try {
                    out.close();
                    in.close();
                    socket.close();
                } catch (SocketException e) {
                } catch (IOException e) {
                }
            }

            return null;

        } catch (SocketTimeoutException e) {
            logger.error("Timeout getting Somfy MyLink shade list " + e.getMessage());
            throw new SomfyMyLinkException("Connection issue discovering Somfy devices", e);

        } catch (Exception e) {
            logger.error("Error getting Somfy MyLink shade list " + e.getMessage());
            throw new SomfyMyLinkException("Error discovering Somfy devices", e);
        }
    }

    private Socket getConnection() throws UnknownHostException, IOException {
        return getConnection(MYLINK_DEFAULT_TIMEOUT);
    }

    private Socket getConnection(int timeout) throws UnknownHostException, IOException {
        String myLinkAddress = config.getIpAddress();
        Socket socket = new Socket(myLinkAddress, MYLINK_PORT);
        socket.setSoTimeout(timeout);
        return socket;
    }

    private String buildCommand(String command, String targetId) {
        int randomNum = ThreadLocalRandom.current().nextInt(1, 1000);

        // fix '-' back to '.'
        targetId = targetId.replace('-', '.');

        String myLinkCommand = "{\"id\": " + randomNum + ", \"method\": \"" + command
                + "\",\"params\": {\"targetID\": \"" + targetId + "\",\"auth\": \"" + config.getSystemId() + "\"}}";

        return myLinkCommand;
    }

    @Override
    public void thingUpdated(Thing thing) {
        SomfyMyLinkConfiguration newConfig = thing.getConfiguration().as(SomfyMyLinkConfiguration.class);

        boolean validConfig = validConfiguration(newConfig);
        boolean needsReconnect = false; // validConfig && !this.config.sameConnectionParameters(newConfig);

        if (!validConfig || needsReconnect) {
            dispose();
        }

        this.thing = thing;
        this.config = newConfig;

        if (needsReconnect) {
            initialize();
        }
    }

    @Override
    public void dispose() {
        disconnect();

        if (this.discoveryServiceRegistration != null) {
            this.discoveryServiceRegistration.unregister();
            this.discoveryServiceRegistration = null;
        }
    }

    public SomfyMyLinkConfiguration getMyLinkConfig() {
        return config;
    }
}
