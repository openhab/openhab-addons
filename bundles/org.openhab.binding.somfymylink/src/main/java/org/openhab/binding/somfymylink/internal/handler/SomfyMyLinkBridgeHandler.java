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
package org.openhab.binding.somfymylink.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

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
import java.util.concurrent.ScheduledFuture;
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
import org.openhab.binding.somfymylink.internal.config.SomfyMyLinkConfiguration;
import org.openhab.binding.somfymylink.internal.discovery.SomfyMyLinkDeviceDiscoveryService;
import org.openhab.binding.somfymylink.internal.model.SomfyMyLinkPingResponse;
import org.openhab.binding.somfymylink.internal.model.SomfyMyLinkResponseBase;
import org.openhab.binding.somfymylink.internal.model.SomfyMyLinkScene;
import org.openhab.binding.somfymylink.internal.model.SomfyMyLinkScenesResponse;
import org.openhab.binding.somfymylink.internal.model.SomfyMyLinkShade;
import org.openhab.binding.somfymylink.internal.model.SomfyMyLinkShadesResponse;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link SomfyMyLinkBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Johnson - Initial contribution
 */
@NonNullByDefault
public class SomfyMyLinkBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyMyLinkBridgeHandler.class);

    @Nullable
    private SomfyMyLinkConfiguration config;

    private static final int RECONNECT_MINUTES = 2;
    private static final int HEARTBEAT_MINUTES = 1;
    private static final int MYLINK_PORT = 44100;
    public static final int MYLINK_DEFAULT_TIMEOUT = 10000;

    @Nullable
    private ScheduledFuture<?> connectRetryJob;
    
    @Nullable
    private ScheduledFuture<?> keepAlive;

    @Nullable
    private ScheduledFuture<?> keepAliveReconnect;

    @Nullable
    private ServiceRegistration<DiscoveryService> discoveryServiceRegistration;

    // Gson & parser
    private final Gson gson = new Gson();

    public SomfyMyLinkBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getThing().getConfiguration().as(SomfyMyLinkConfiguration.class);

        if (validConfiguration(config)) {
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

    private boolean validConfiguration(@Nullable SomfyMyLinkConfiguration config) {
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "mylink configuration missing");
            return false;
        }

        if (StringUtils.isEmpty(config.ipAddress)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "mylink address not specified");
            return false;
        }

        return true;
    }

    private void scheduleConnectRetry(long waitMinutes) {
        logger.debug("Scheduling connection retry in {} minutes", waitMinutes);
        connectRetryJob = scheduler.schedule(this::connect, waitMinutes, TimeUnit.MINUTES);
    }

    private synchronized void connect() {
        try {

            if(config == null) {
                throw new SomfyMyLinkException("Config not setup correctly");
            }

            if (StringUtils.isEmpty(config.ipAddress) || StringUtils.isEmpty(config.systemId)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "mylink config not specified");
                return;
            }
            
            logger.debug("Connecting to mylink at {}", config.ipAddress);
            
            // send a ping
            sendPing();

            logger.debug("Connected to mylink at {}", config.ipAddress);

            updateStatus(ThingStatus.ONLINE);

            keepAlive = scheduler.scheduleWithFixedDelay(this::sendKeepAlive, HEARTBEAT_MINUTES, HEARTBEAT_MINUTES,
                    TimeUnit.MINUTES);

        } catch (SomfyMyLinkException e) {
            logger.debug("problem connecting to mylink");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            disconnect();
            scheduleConnectRetry(RECONNECT_MINUTES); // Possibly a temporary problem. Try again later.
        }
    }

    private synchronized void disconnect() {
        logger.debug("Disconnecting from mylink");

        if (connectRetryJob != null) {
            connectRetryJob.cancel(true);
        }

        if (keepAlive != null) {
            keepAlive.cancel(true);
        }

        if (keepAliveReconnect != null) {
            keepAliveReconnect.cancel(false);
        }
    }

    private void sendKeepAlive() {
        // Reconnect if no response is received within 30 seconds.
        keepAliveReconnect = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                reconnect();
            }
        }, 30, TimeUnit.SECONDS);

        try {

            logger.debug("Keep alive triggered");
            
            // send a ping
            sendPing();

            // System is alive, cancel reconnect task.
            if (keepAliveReconnect != null) {
                keepAliveReconnect.cancel(true);
            }
            
            logger.debug("Keep alive succeeded");

        } catch (SomfyMyLinkException e) {
            logger.debug("Problem pinging mylink during keepalive");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private synchronized void reconnect() {
        logger.debug("Keepalive timeout, attempting to reconnect to the bridge");

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE);
        disconnect();
        connect();
    }

    public SomfyMyLinkShade[] getShadeList() throws SomfyMyLinkException {
        String targetId = "*.*";
        String command = "mylink.status.info";

        SomfyMyLinkShadesResponse response = (SomfyMyLinkShadesResponse) sendCommandWithResponse(command, targetId,
                SomfyMyLinkShadesResponse.class);

        if (response != null) {
            return response.getResult();
        }

        return new SomfyMyLinkShade[0];
    }

    public String[] sendPing() throws SomfyMyLinkException {
        String targetId = "*.*";
        String command = "mylink.status.ping";

        SomfyMyLinkPingResponse response = (SomfyMyLinkPingResponse) sendCommandWithResponse(command, targetId,
                SomfyMyLinkPingResponse.class);

        if (response != null) {
            return response.getResult();
        }

        return new String[0];
    }

    public SomfyMyLinkScene[] getSceneList() throws SomfyMyLinkException {
        String targetId = "*.*";
        String command = "mylink.scene.list";

        SomfyMyLinkScenesResponse response = (SomfyMyLinkScenesResponse) sendCommandWithResponse(command, targetId,
                SomfyMyLinkScenesResponse.class);

        if (response != null) {
            return response.getResult();
        }

        return new SomfyMyLinkScene[0];
    }

    public void commandShadeUp(String targetId) throws SomfyMyLinkException {
        try {
            sendCommand("mylink.move.up", targetId);
        } catch (SomfyMyLinkException e) {
            logger.error("Error commanding shade up: " + e.getMessage());
            throw new SomfyMyLinkException("Error commanding shade up", e);
        }
    }

    public void commandShadeDown(String targetId) throws SomfyMyLinkException {
        try {
            sendCommand("mylink.move.down", targetId);
        } catch (SomfyMyLinkException e) {
            logger.error("Error commanding shade down: " + e.getMessage());
            throw new SomfyMyLinkException("Error commanding shade down", e);
        }
    }

    public void commandShadeStop(String targetId) throws SomfyMyLinkException {
        try {
            sendCommand("mylink.move.stop", targetId);
        } catch (SomfyMyLinkException e) {
            logger.error("Error commanding shade stop: " + e.getMessage());
            throw new SomfyMyLinkException("Error commanding shade stop", e);
        }
    }

    private void sendCommand(String command, String targetId) throws SomfyMyLinkException {
        String myLinkCommand = buildCommand(command, targetId);
        try {
            logger.debug("Sending: " + command + " Target: " + targetId);
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
            logger.error("Timeout sending command to mylink: " + command + "Message: " + e.getMessage());
            throw new SomfyMyLinkException("Timeout sending command to mylink", e);
        } catch (SocketException e) {
            logger.error("Problem sending command to mylink: " + command + "Message: " + e.getMessage());
            throw new SomfyMyLinkException("Problem sending command to mylink", e);
        } catch (IOException e) {
            logger.error("Problem sending command to mylink: " + command + "Message: " + e.getMessage());
            throw new SomfyMyLinkException("Problem sending command to mylink", e);
        }
    }
    
    @Nullable
    private SomfyMyLinkResponseBase sendCommandWithResponse(String command, String targetId, Type responseType)
            throws SomfyMyLinkException {
        String myLinkCommand = buildCommand(command, targetId);

        try {
            logger.debug("Sending: " + command + " Target: " + targetId);
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
                        
                        logger.debug("got message: " + message);
                        
                        SomfyMyLinkResponseBase data = gson.fromJson(message, responseType);

                        // check if there was an error
                        if (data.getError() != null) {
                            logger.error("Error communicating with mylink: " + data.getError());
                            throw new SomfyMyLinkException("Error communicating with mylink:" + data.getError());
                        }
                        return data;
                    } catch (JsonSyntaxException e) {
                        // it wasn't a full message?
                        logger.debug("Trouble parsing message received. Message:" + e.getMessage());
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
            logger.error("Timeout sending command to mylink: " + command + "Message: " + e.getMessage());
            throw new SomfyMyLinkException("Timeout sending command to mylink", e);
        } catch (SocketException e) {
            logger.error("Problem sending command to mylink: " + command + "Message: " + e.getMessage());
            throw new SomfyMyLinkException("Problem sending command to mylink", e);
        } catch (IOException e) {
            logger.error("Problem sending command to mylink: " + command + "Message: " + e.getMessage());
            throw new SomfyMyLinkException("Problem sending command to mylink", e);
        }
    }

    private Socket getConnection() throws UnknownHostException, IOException {
        return getConnection(MYLINK_DEFAULT_TIMEOUT);
    }

    private Socket getConnection(int timeout) throws UnknownHostException, IOException {
        if(config == null) throw new SomfyMyLinkException("Config not setup correctly");

        logger.debug("Getting connection to mylink on:" + config.ipAddress + " Post: " + MYLINK_PORT);
        String myLinkAddress = config.ipAddress;
        Socket socket = new Socket(myLinkAddress, MYLINK_PORT);
        socket.setSoTimeout(timeout);
        return socket;
    }

    private String buildCommand(String command, String targetId) {
        if(config == null && StringUtils.isEmpty(config.systemId)) throw new SomfyMyLinkException("Config not setup correctly");

        int randomNum = ThreadLocalRandom.current().nextInt(1, 1000);

        // fix '-' back to '.'
        String tId = targetId.replace('-', '.');

        String myLinkCommand = "{\"id\": " + randomNum + ", \"method\": \"" + command
                + "\",\"params\": {\"targetID\": \"" + tId + "\",\"auth\": \"" + config.systemId + "\"}}";

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
        config = newConfig;

        if (needsReconnect) {
            initialize();
        }
    }

    @Override
    public void dispose() {
        disconnect();

        if (discoveryServiceRegistration != null) {
            discoveryServiceRegistration.unregister();
            discoveryServiceRegistration = null;
        }
    }
}
