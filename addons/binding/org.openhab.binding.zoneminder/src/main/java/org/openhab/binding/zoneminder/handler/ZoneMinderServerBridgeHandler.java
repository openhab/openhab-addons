/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.api.MonitorDaemonStatus;
import org.openhab.binding.zoneminder.internal.api.MonitorData;
import org.openhab.binding.zoneminder.internal.api.ServerVersion;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderMessage.ZoneMinderRequestType;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderOutgoingRequest;
import org.openhab.binding.zoneminder.internal.command.http.ZoneMinderHttpMonitorRequest;
import org.openhab.binding.zoneminder.internal.command.http.ZoneMinderHttpRequest;
import org.openhab.binding.zoneminder.internal.command.http.ZoneMinderHttpServerRequest;
import org.openhab.binding.zoneminder.internal.config.ZoneMinderBridgeServerConfig;
import org.openhab.binding.zoneminder.internal.connection.ZoneMinderHttpProxy;
import org.openhab.binding.zoneminder.internal.connection.ZoneMinderTelnetConnection;
import org.openhab.binding.zoneminder.internal.data.ZoneMinderData;
import org.openhab.binding.zoneminder.internal.data.ZoneMinderMonitorData;
import org.openhab.binding.zoneminder.internal.data.ZoneMinderServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * TODOS
 *  Set Id of ZOneMinder Monitor to something (Hardcoded right now
 *  Check server version / API Version
 *  Check If Server is Online
 *  Don't set binding offline when server is Offline
 *  Call this one to indicate problems: updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not control device at IP address x.x.x.x");
 *  This API call indicates the state of the ZM Server: /zm/api/states.json
 *  This API call indicates the same as above:/zm/api/host/daemonCheck.json
 *  This API call indicate server load: /zm/api/host/getLoad.json
 *  This API call indicate disk usage /zm/api/host/getDiskPercent.json
 *  http://<SERVERNAME>/zm/cgi-bin/nph-zms?monitor=9&user=USER&pass=PASS
 *  http://<SERVERNAME>/zm/api/configs.json
 */

/**
 * Handler for a ZoneMinder Server.
 *
 * @author Martin S. Eskildsen
 *
 */
public class ZoneMinderServerBridgeHandler extends ZoneMinderBaseBridgeHandler {

    public static final int TELNET_TIMEOUT = 5000;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets
            .newHashSet(ZoneMinderConstants.THING_TYPE_BRIDGE_ZONEMINDER_SERVER);

    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Telnet Connection to ZoneMinder Server
     */
    private ZoneMinderTelnetConnection telnetMonitor = null;

    /**
     *
     */
    private String httpProtocol;

    /**
     *
     */
    private String hostName;

    /**
     *
     */
    private Integer telnetPort = ZoneMinderConstants.DEFAULT_TELNET_PORT;

    /**
     *
     */
    private Integer httpPort = ZoneMinderConstants.DEFAULT_HTTP_PORT;

    /**
     *
     */
    private String zoneMinderServerPath = ""; // ZoneMinderHttpProxy.PATH_ZONEMINDER_BASE;

    /**
     *
     */
    private String userName;

    /**
     *
     */
    private String password;

    /**
     * ZoneMinder HTTP connection
     */
    private ZoneMinderHttpProxy zoneMinderServerProxy = null;

    /**
     * ZoneMinder Telnet Connection
     */
    private Socket tcpSocket = null;
    private PrintWriter tcpOutput = null;
    private BufferedReader tcpInput = null;

    private ServerVersion zmServerVersion;

    /**
     * Constructor
     *
     * @param bridge
     *            Bridge object representing a ZoneMinder Server
     */
    public ZoneMinderServerBridgeHandler(Bridge bridge) {
        super(bridge, ZoneMinderThingType.ZoneMinderServerBridge);
    }

    /**
     * Initializes the bridge.
     */
    @Override
    public void initialize() {
        logger.debug("About to initialize bridge " + ZoneMinderConstants.BRIDGE_ZONEMINDER_SERVER);
        super.initialize();
        try {
            updateStatus(ThingStatus.INITIALIZING);

            ZoneMinderBridgeServerConfig config = getBridgeConfig();

            this.httpProtocol = config.getProtocol();
            this.hostName = config.getHostName();
            this.telnetPort = config.getTelnetPort();
            this.httpPort = config.getHttpPort();
            this.zoneMinderServerPath = config.getServerBasePath();
            this.userName = config.getUserName();
            this.password = config.getPassword();

            logger.debug("ZoneMinder Server Bridge Handler Initialized");
            logger.debug("   Protocol:           {}", httpProtocol);
            logger.debug("   HostName:           {}", hostName);
            logger.debug("   Port (HTTP)         {}", httpPort);
            logger.debug("   Port (Telnet)       {}", telnetPort);
            logger.debug("   Server Path         {}", zoneMinderServerPath);
            logger.debug("   User:               {}", userName);
            // logger.debug(" Password: {}", password);

            startMonitor(config.getRefreshInterval());
        } catch (Exception ex) {
            logger.error("'ZoneMinderServerBridgeHandler' failed to initialize. Exception='{}'", ex.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
        }
    }

    /**
     * Disposes the bridge.
     */
    @Override
    public void dispose() {
        logger.debug("Stop polling of ZoneMinder Server API");

        stopMonitor();
    }

    @Override
    public String getZoneMinderId() {
        return getThing().getUID().getAsString();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // can be overridden by subclasses
        logger.debug("Channel '" + channelUID.getAsString() + "' was linked to '" + this.thing.getThingTypeUID() + "'");
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        // can be overridden by subclasses
        logger.debug(
                "Channel '" + channelUID.getAsString() + "' was unlinked from '" + this.thing.getThingTypeUID() + "'");
    }

    protected ZoneMinderBridgeServerConfig getBridgeConfig() {
        return this.getConfigAs(ZoneMinderBridgeServerConfig.class);
    }

    /**
     * Just logging - nothing to do.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("update " + channelUID.getAsString() + " with " + command.toString());
    }

    @Override
    protected void refreshThings() {

        List<Thing> things = getThing().getThings();

        for (Thing thing : things) {
            try {
                if (thing.getThingTypeUID().equals(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR)) {
                    Thing thingMonitor = thing;
                    ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

                    ZoneMinderHttpRequest req = new ZoneMinderHttpMonitorRequest(ZoneMinderRequestType.MONITOR_THING,
                            thingHandler.getZoneMinderId());
                    sendZoneMinderHttpRequest(req);
                    logger.debug("Updated ZoneMinderApiData for Thing: {}  {}", thing.getThingTypeUID(),
                            thing.getUID());
                }
            } catch (Exception ex) {
                logger.error("Method 'refreshThings' for Bridge {} failed for thing='{}' - Exception='{}'",
                        this.getZoneMinderId(), thing.getUID(), ex.getMessage());
            }
        }

    }

    @Override
    public Boolean isAlive() {
        return isConnected();
    }

    @Override
    public void updateChannel(ChannelUID channel) {
        super.updateChannel(channel);
    }

    @Override
    void openConnection() {

        if (isConnected() == false) {
            logger.debug("Connecting ZoneMinder Server Bridge to Telnet.");

            try {
                closeConnection();

                logger.debug("openConnection(): Connecting to ZoneMinder Server (Telnet)");

                tcpSocket = new Socket();
                SocketAddress TPIsocketAddress = new InetSocketAddress(hostName, telnetPort);
                tcpSocket.connect(TPIsocketAddress, TELNET_TIMEOUT);
                tcpInput = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
                tcpOutput = new PrintWriter(tcpSocket.getOutputStream(), true);

                Thread tcpListener = new Thread(new TCPListener());
                tcpListener.start();

                zoneMinderServerProxy = new ZoneMinderHttpProxy(getBridgeConfig().getProtocol(),
                        getBridgeConfig().getHostName(), getBridgeConfig().getServerBasePath(),
                        getBridgeConfig().getUserName(), getBridgeConfig().getPassword());

                setConnected(zoneMinderServerProxy.connect());

                // If we are connected, ask for the server version
                if (isConnected()) {
                    // TODO:: FIX THIS CALL : sendZoneMinderHttpRequest
                    // sendZoneMinderHttpRequest(ZoneMinderRequestType.GET_SERVERVERSION);
                }

            } catch (UnknownHostException unknownException) {
                logger.error("openConnection(): Unknown Host Exception: ", unknownException);
                setConnected(false);
            } catch (SocketException socketException) {
                logger.error("openConnection(): Socket Exception: ", socketException);
                setConnected(false);
            } catch (IOException ioException) {
                logger.error("openConnection(): IO Exception: ", ioException);
                setConnected(false);
            } catch (Exception exception) {
                logger.error("openConnection(): Exception: ", exception);
                setConnected(false);
            }

        }

    }

    @Override
    synchronized void closeConnection() {
        try {
            if (zoneMinderServerProxy != null) {
                zoneMinderServerProxy.close();
                zoneMinderServerProxy = null;
            }
            logger.debug("closeConnection(): Closed HTTP Connection!");

            if (tcpSocket != null) {
                tcpSocket.close();
                tcpSocket = null;
            }
            if (tcpInput != null) {
                tcpInput.close();
                tcpInput = null;
            }
            if (tcpOutput != null) {
                tcpOutput.close();
                tcpOutput = null;
            }
            logger.debug("closeConnection(): Closed TCP Connection!");
            setConnected(false);

        } catch (IOException ioException) {
            logger.error("closeConnection(): Unable to close connection - " + ioException.getMessage());
        } catch (Exception exception) {
            logger.error("closeConnection(): Error closing connection - " + exception.getMessage());
        }

    }

    /**
     *
     **/
    protected void writeTCP(String writeString) {
        try {
            tcpOutput.write(writeString);
            tcpOutput.flush();
            logger.debug("writeTCP(): Message Sent: {}", writeString);
        } catch (Exception exception) {
            logger.error("writeTCP(): Unable to write to socket: {} ", exception);
            setConnected(false);
        }
    }

    /**
     *
     **/

    protected String readTCP() {
        String message = "";

        try {

            message = tcpInput.readLine();
            logger.debug("readTCP(): Message Received: {}", message);
        } catch (SocketTimeoutException stException) {
            // Just ignore this

        } catch (IOException ioException) {
            logger.error("readTCP(): IO Exception: ", ioException);
            setConnected(false);
        } catch (Exception exception) {
            logger.error("readTCP(): Exception: ", exception);
            setConnected(false);
        }

        return message;
    }

    /**
     * TCPMessageListener: Receives Socket messages from the ZoneMinder API.
     */
    private class TCPListener implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(TCPListener.class);

        /**
         * Run method. Runs the MessageListener thread
         */
        @Override
        public void run() {
            String messageLine;

            try {

                // Allow other things to get started before running
                Thread.sleep(1000);
                while (isConnected()) {
                    if ((messageLine = readTCP()) != null) {
                        handleIncomingTelnetMessage(messageLine);
                    } else {
                        setConnected(false);
                    }
                }
            } catch (Exception e) {
                logger.error("TCPListener(): Unable to read message: ", e);
                closeConnection();
            }
        }
    }

    @Override
    public boolean sendZoneMinderHttpRequest(ZoneMinderHttpRequest request) {
        ZoneMinderData data = null;
        ThingTypeUID thingTypeUID = request.getThingTypeUID();
        String zoneMinderId = request.getId();
        switch (request.getRequestType()) {

            case MONITOR_THING:
                ZoneMinderHttpMonitorRequest monitorRequest = (ZoneMinderHttpMonitorRequest) request;
                MonitorData monitorData = zoneMinderServerProxy.getMonitor(zoneMinderId);
                MonitorDaemonStatus captureDaemonStatus = zoneMinderServerProxy
                        .getMonitorCaptureDaemonStatus(zoneMinderId);
                MonitorDaemonStatus analysisDaemonStatus = zoneMinderServerProxy
                        .getMonitorAnalysisDaemonStatus(zoneMinderId);
                MonitorDaemonStatus frameDaemonStatus = zoneMinderServerProxy.getMonitorFrameDaemonStatus(zoneMinderId);
                data = new ZoneMinderMonitorData(monitorData, captureDaemonStatus, analysisDaemonStatus,
                        frameDaemonStatus);
                break;
            case SERVER_THING:
                ZoneMinderHttpServerRequest serverRequest = (ZoneMinderHttpServerRequest) request;
                ServerVersion serverVersionData = zoneMinderServerProxy.getServerVersion();

                data = new ZoneMinderServerData(serverVersionData);
                /*
                 * case IS_ALIVE:
                 * checkIsAlive();
                 * try {
                 * ChannelUID channel = this.thing.getChannel(ZoneMinderConstants.CHANNEL_SERVER_ONLINE).getUID();
                 * updateChannel(channel);
                 * } catch (Exception e) {
                 * // Just ignore it :-)
                 * }
                 * return true;
                 */
            default:
                logger.warn("Unhandled HTTP request occurred (request='{}'", request.getRequestType());
        }

        notifyZoneMinderApiDataUpdated(thingTypeUID, zoneMinderId, data);
        return false;
    }

    @Override
    protected boolean onHandleZoneMinderTelnetRequest(ZoneMinderRequestType requestType,
            ZoneMinderOutgoingRequest request) {
        boolean result = false;

        switch (requestType) {
            case MONITOR_TRIGGER:
                logger.debug("[TCP] Writing command '{}' to ZoneMinder Server", request.toCommandString());
                writeTCP(request.toCommandString());
                result = true;
                break;

            default:
                result = false;
        }
        return result;
    }

    @Override
    protected void checkIsAlive() {

        // Ask all things if they are alive
        for (Thing thing : getThing().getThings()) {
            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();
            if (thingHandler instanceof ZoneMinderThingMonitorHandler) {
                MonitorDaemonStatus status = zoneMinderServerProxy
                        .getMonitorCaptureDaemonStatus(thingHandler.getZoneMinderId());
                thingHandler.checkIsAlive(status);
            }

        }
    }

    public ArrayList<MonitorData> getMonitors() {
        return zoneMinderServerProxy.getMonitors();
    }

}
