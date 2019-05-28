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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.somfymylink.internal.SomfyMyLinkBindingConstants;
import org.openhab.binding.somfymylink.internal.config.SomfyMyLinkConfiguration;
import org.openhab.binding.somfymylink.internal.discovery.SomfyMyLinkDeviceDiscoveryService;
import org.openhab.binding.somfymylink.internal.model.SomfyMyLinkErrorResponse;
import org.openhab.binding.somfymylink.internal.model.SomfyMyLinkPingResponse;
import org.openhab.binding.somfymylink.internal.model.SomfyMyLinkResponseBase;
import org.openhab.binding.somfymylink.internal.model.SomfyMyLinkScene;
import org.openhab.binding.somfymylink.internal.model.SomfyMyLinkScenesResponse;
import org.openhab.binding.somfymylink.internal.model.SomfyMyLinkShade;
import org.openhab.binding.somfymylink.internal.model.SomfyMyLinkShadesResponse;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.somfymylink.internal.SomfyMyLinkBindingConstants.*;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    private static final int HEARTBEAT_MINUTES = 2;
    private static final int MYLINK_PORT = 44100;
    private static final int MYLINK_DEFAULT_TIMEOUT = 5000;
    private static final Object CONNECTION_LOCK = new Object();
    private static final int CONNECTION_DELAY = 1000;

    private static final String MYLINK_COMMAND_TEMPLATE = "{\"id\": %1$s, \"method\": \"%2$s\",\"params\": {\"%3$s\": %4$s,\"auth\": \"%5$s\"}}";

    @Nullable
    private ScheduledFuture<?> heartbeat;

    @Nullable
    private ServiceRegistration<DiscoveryService> discoveryServiceRegistration;

    private SomfyMyLinkDeviceDiscoveryService discovery;

    @Nullable
    private SomfyMyLinkStateDescriptionOptionsProvider stateDescriptionProvider;

    // Gson & parser
    private final Gson gson = new Gson();

    public SomfyMyLinkBridgeHandler(Bridge bridge, @Nullable SomfyMyLinkStateDescriptionOptionsProvider stateDescriptionProvider) {
        super(bridge);

        this.discovery = new SomfyMyLinkDeviceDiscoveryService(this);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command received on mylink {}", command);

        try {
            if (CHANNEL_SCENES.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    // TODO: handle data refresh
                    return;
                }

                if (CHANNEL_SCENES.equals(channelUID.getId()) && command instanceof StringType) {
                    Integer sceneId = Integer.decode(command.toString());
                    commandScene(sceneId);
                }
            }
        } catch (SomfyMyLinkException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing mylink");
        config = getThing().getConfiguration().as(SomfyMyLinkConfiguration.class);

        if (validConfiguration(config)) {
            this.discoveryServiceRegistration = this.bundleContext.registerService(DiscoveryService.class, this.discovery, null);
            this.discovery.activate(null);

            // kick off the bridge connection process
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

    private void connect() {
        try {
            if(config == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "mylink config not specified");
                return;
            }

            if (StringUtils.isEmpty(config.ipAddress) || StringUtils.isEmpty(config.systemId)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "mylink config not specified");
                return;
            }

            // start the keepalive process
            ensureKeepAlive();
            
            logger.debug("Connecting to mylink at {}", config.ipAddress);
            
            // send a ping
            sendPing();

            logger.debug("Connected to mylink at {}", config.ipAddress);

            updateStatus(ThingStatus.ONLINE);

        } catch (SomfyMyLinkException e) {
            logger.debug("Problem connecting to mylink, bridge OFFLINE");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void ensureKeepAlive()
    {
        if(heartbeat == null) {
            logger.debug("Starting keepalive job in {} min, every {} min", HEARTBEAT_MINUTES, HEARTBEAT_MINUTES);
            //heartbeat = scheduler.scheduleWithFixedDelay(this::sendKeepAlive, HEARTBEAT_MINUTES, HEARTBEAT_MINUTES, TimeUnit.MINUTES);

            heartbeat = this.scheduler.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    sendKeepAlive();
                }
            }, 1, 1, TimeUnit.MINUTES);

        }
    }

    private void disconnect() {
        logger.debug("Disconnecting from mylink");

        if (heartbeat != null) {
            logger.debug("Cancelling keepalive job");
            heartbeat.cancel(true);
        } else {
            logger.debug("Keepalive was not active");
        }
    }

    private void sendKeepAlive() {
        try {
            logger.debug("Keep alive triggered");

            if(getThing().getStatus() != ThingStatus.ONLINE) {
                // try connecting
                logger.debug("Bridge offline, trying to connect");
                connect();
            } else {
                // send a ping
                sendPing();
                logger.debug("Keep alive succeeded");
            }
        } catch (SomfyMyLinkException e) {
            logger.debug("Problem pinging mylink during keepalive");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public SomfyMyLinkShade[] getShadeList() throws SomfyMyLinkException {
        String command = buildShadeCommand("mylink.status.info", "*.*");

        SomfyMyLinkShadesResponse response = (SomfyMyLinkShadesResponse) sendCommandWithResponse(command, SomfyMyLinkShadesResponse.class);

        if (response != null) {
            return response.getResult();
        }

        return new SomfyMyLinkShade[0];
    }

    public void sendPing() throws SomfyMyLinkException {
        String command = buildShadeCommand("mylink.status.ping", "*.*");

        SomfyMyLinkPingResponse response = (SomfyMyLinkPingResponse) sendCommandWithResponse(command, SomfyMyLinkPingResponse.class);

        if (response != null) {
            return;
        }

        return;
    }

    public SomfyMyLinkScene[] getSceneList() throws SomfyMyLinkException {
        String command = buildShadeCommand("mylink.scene.list", "*.*");

        SomfyMyLinkScenesResponse response = (SomfyMyLinkScenesResponse) sendCommandWithResponse(command, SomfyMyLinkScenesResponse.class);

        if (response != null) {
            List<StateOption> options = new ArrayList<>();
            for (SomfyMyLinkScene scene : response.result) {
                options.add(new StateOption(scene.getTargetID(), scene.getName()));
            }

            logger.debug("Setting {} options on bridge", options.size());

            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), SomfyMyLinkBindingConstants.CHANNEL_SCENES), options);

            return response.getResult();
        }

        return new SomfyMyLinkScene[0];
    }

    public void commandShadeUp(String targetId) throws SomfyMyLinkException {
        try {
            String command = buildShadeCommand("mylink.move.up", targetId);
            sendCommand(command);
        } catch (SomfyMyLinkException e) {
            logger.info("Error commanding shade up: " + e.getMessage());
            throw new SomfyMyLinkException("Error commanding shade up", e);
        }
    }

    public void commandShadeDown(String targetId) throws SomfyMyLinkException {
        try {
            String command = buildShadeCommand("mylink.move.down", targetId);
            sendCommand(command);
        } catch (SomfyMyLinkException e) {
            logger.info("Error commanding shade down: " + e.getMessage());
            throw new SomfyMyLinkException("Error commanding shade down", e);
        }
    }

    public void commandShadeStop(String targetId) throws SomfyMyLinkException {
        try {
            String command = buildShadeCommand("mylink.move.stop", targetId);
            sendCommand(command);
        } catch (SomfyMyLinkException e) {
            logger.info("Error commanding shade stop: " + e.getMessage());
            throw new SomfyMyLinkException("Error commanding shade stop", e);
        }
    }

    public void commandScene(Integer sceneId) throws SomfyMyLinkException {
        try {
            String command = buildSceneCommand("mylink.scene.run", sceneId);
            sendCommand(command);
        } catch (SomfyMyLinkException e) {
            logger.info("Error commanding shade stop: " + e.getMessage());
            throw new SomfyMyLinkException("Error commanding shade stop", e);
        }
    }

    private void sendCommand(String command) throws SomfyMyLinkException {
        //String myLinkCommand = buildCommand(command, targetId);

        synchronized(CONNECTION_LOCK) {
            try {
                logger.debug("Sending: {}", command);
                Socket socket = getConnection();
                OutputStream out = socket.getOutputStream();

                try {
                    byte[] sendBuffer = command.getBytes(StandardCharsets.US_ASCII);
                    // send the command
                    out.write(sendBuffer, 0, sendBuffer.length);
                } finally {
                    logger.debug("Cleaning up after command");
                    // cleanup
                    try {
                        out.close();
                        socket.close();
                    } catch (SocketException e) {
                        logger.debug("Error during socket tidy up. {}", e.getMessage());
                    } catch (IOException e) {
                        logger.debug("Error during socket tidy up. {}", e.getMessage());
                    }
                }

                // give time for mylink to process
                Thread.sleep(CONNECTION_DELAY);

            } catch (SocketTimeoutException e) {
                logger.warn("Timeout sending command to mylink: {} Message: {}", command, e.getMessage());
                throw new SomfyMyLinkException("Timeout sending command to mylink", e);
            } catch (SocketException e) {
                logger.warn("Problem sending command to mylink: {} Message: {}", command, e.getMessage());
                throw new SomfyMyLinkException("Problem sending command to mylink", e);
            } catch (IOException e) {
                logger.warn("Problem sending command to mylink: {} Message: {}", command, e.getMessage());
                throw new SomfyMyLinkException("Problem sending command to mylink", e);
            } catch (InterruptedException e) {
                logger.debug("Interrupted while waiting after sending command to mylink: {} Message: {}", command, e.getMessage());
            }
        }
    }
    
    @Nullable
    private SomfyMyLinkResponseBase sendCommandWithResponse(String command, Type responseType)
            throws SomfyMyLinkException {

        synchronized(CONNECTION_LOCK) {
            try {
                logger.debug("Sending: {}", command);
                Socket socket = getConnection();
                OutputStream out = socket.getOutputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                try {
                    byte[] sendBuffer = command.getBytes(StandardCharsets.US_ASCII);

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
                            
                            logger.debug("Got message: " + message);
                            
                            JsonParser parser = new JsonParser();
                            JsonObject o = parser.parse(message).getAsJsonObject();

                            if(o.has("error")) {

                                SomfyMyLinkErrorResponse errorResponse = gson.fromJson(message, SomfyMyLinkErrorResponse.class);

                                logger.info("Error communicating with mylink: {}", errorResponse.error.message);
                                throw new SomfyMyLinkException("Error communicating with mylink: " + errorResponse.error.message);
                            }
                            
                            SomfyMyLinkResponseBase data = gson.fromJson(message, responseType);

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
                        logger.debug("Error during socket tidy up. {}", e.getMessage());
                    } catch (IOException e) {
                        logger.debug("Error during socket tidy up. {}", e.getMessage());
                    }
                }

                // only if we didn't already get a response give time for mylink to process
                Thread.sleep(CONNECTION_DELAY);

                return null;
            } catch (SocketTimeoutException e) {
                logger.info("Timeout sending command to mylink: " + command + "Message: " + e.getMessage());
                throw new SomfyMyLinkException("Timeout sending command to mylink", e);
            } catch (SocketException e) {
                logger.info("Problem sending command to mylink: " + command + "Message: " + e.getMessage());
                throw new SomfyMyLinkException("Problem sending command to mylink", e);
            } catch (IOException e) {
                logger.info("Problem sending command to mylink: " + command + "Message: " + e.getMessage());
                throw new SomfyMyLinkException("Problem sending command to mylink", e);
            } catch (InterruptedException e) {
                logger.debug("Interrupted while waiting after sending command to mylink: " + command + "Message: " + e.getMessage());
                return null;
            }
        }
    }

    private Socket getConnection() throws UnknownHostException, IOException {
        if(config == null) throw new SomfyMyLinkException("Config not setup correctly");

        logger.debug("Getting connection to mylink on:" + config.ipAddress + " Post: " + MYLINK_PORT);
        String myLinkAddress = config.ipAddress;
        Socket socket = new Socket(myLinkAddress, MYLINK_PORT);
        socket.setSoTimeout(MYLINK_DEFAULT_TIMEOUT);
        return socket;
    }

    private String buildShadeCommand(String method, String targetId) {
        if(config == null && StringUtils.isEmpty(config.systemId)) throw new SomfyMyLinkException("Config not setup correctly");

        int randomNum = ThreadLocalRandom.current().nextInt(1, 1000);

        // quote and fix '-' back to '.'
        String tId = String.format("\"%1$s\"", targetId).replace('-', '.');

        String myLinkCommand = String.format(MYLINK_COMMAND_TEMPLATE, randomNum, method, "targetID", tId, config.systemId); 

        return myLinkCommand;
    }

    private String buildSceneCommand(String method, Integer sceneId) {
        if(config == null && StringUtils.isEmpty(config.systemId)) throw new SomfyMyLinkException("Config not setup correctly");

        int randomNum = ThreadLocalRandom.current().nextInt(1, 1000);

        String myLinkCommand = String.format(MYLINK_COMMAND_TEMPLATE, randomNum, method, "sceneId", sceneId, config.systemId); 

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
        logger.debug("Dispose called on {}", SomfyMyLinkBridgeHandler.class);
        disconnect();

        if (discoveryServiceRegistration != null) {
            this.discovery.deactivate();
            this.discoveryServiceRegistration.unregister();
            discoveryServiceRegistration = null;
        }

        logger.debug("Dispose finishing on {}", SomfyMyLinkBridgeHandler.class);
    }
}
