/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.handler;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.config.LeapBridgeConfig;
import org.openhab.binding.lutron.internal.discovery.LeapDeviceDiscoveryService;
import org.openhab.binding.lutron.internal.protocol.FanSpeedType;
import org.openhab.binding.lutron.internal.protocol.GroupCommand;
import org.openhab.binding.lutron.internal.protocol.LutronCommandNew;
import org.openhab.binding.lutron.internal.protocol.OutputCommand;
import org.openhab.binding.lutron.internal.protocol.leap.LeapCommand;
import org.openhab.binding.lutron.internal.protocol.leap.LeapMessageParser;
import org.openhab.binding.lutron.internal.protocol.leap.LeapMessageParserCallbacks;
import org.openhab.binding.lutron.internal.protocol.leap.Request;
import org.openhab.binding.lutron.internal.protocol.leap.dto.Area;
import org.openhab.binding.lutron.internal.protocol.leap.dto.ButtonGroup;
import org.openhab.binding.lutron.internal.protocol.leap.dto.Device;
import org.openhab.binding.lutron.internal.protocol.leap.dto.OccupancyGroup;
import org.openhab.binding.lutron.internal.protocol.leap.dto.ZoneStatus;
import org.openhab.binding.lutron.internal.protocol.lip.LutronCommandType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge handler responsible for communicating with Lutron hubs that support the LEAP protocol, such as Caseta and
 * RA2 Select.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class LeapBridgeHandler extends LutronBridgeHandler implements LeapMessageParserCallbacks {
    private static final int DEFAULT_RECONNECT_MINUTES = 5;
    private static final int DEFAULT_HEARTBEAT_MINUTES = 5;
    private static final long KEEPALIVE_TIMEOUT_SECONDS = 30;

    private static final String STATUS_INITIALIZING = "Initializing";

    private final Logger logger = LoggerFactory.getLogger(LeapBridgeHandler.class);

    private @NonNullByDefault({}) LeapBridgeConfig config;
    private int reconnectInterval;
    private int heartbeatInterval;
    private int sendDelay;

    private @NonNullByDefault({}) SSLSocketFactory sslsocketfactory;
    private @Nullable SSLSocket sslsocket;
    private @Nullable BufferedWriter writer;
    private @Nullable BufferedReader reader;

    private @NonNullByDefault({}) LeapMessageParser leapMessageParser;

    private final BlockingQueue<LeapCommand> sendQueue = new LinkedBlockingQueue<>();

    private @Nullable Future<?> asyncInitializeTask;

    private @Nullable Thread senderThread;
    private @Nullable Thread readerThread;

    private @Nullable ScheduledFuture<?> keepAliveJob;
    private @Nullable ScheduledFuture<?> keepAliveReconnectJob;
    private @Nullable ScheduledFuture<?> connectRetryJob;
    private final Object keepAliveReconnectLock = new Object();

    private final Map<Integer, Integer> zoneToDevice = new HashMap<>();
    private final Map<Integer, Integer> deviceToZone = new HashMap<>();
    private final Object zoneMapsLock = new Object();

    private @Nullable Map<Integer, List<Integer>> deviceButtonMap;
    private final Object deviceButtonMapLock = new Object();

    private volatile boolean deviceDataLoaded = false;
    private volatile boolean buttonDataLoaded = false;

    private final Map<Integer, LutronHandler> childHandlerMap = new ConcurrentHashMap<>();
    private final Map<Integer, OGroupHandler> groupHandlerMap = new ConcurrentHashMap<>();

    private @Nullable LeapDeviceDiscoveryService discoveryService;

    public void setDiscoveryService(LeapDeviceDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public LeapBridgeHandler(Bridge bridge) {
        super(bridge);
        leapMessageParser = new LeapMessageParser(this);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(LeapDeviceDiscoveryService.class);
    }

    @Override
    public void initialize() {
        SSLContext sslContext;

        childHandlerMap.clear();
        groupHandlerMap.clear();

        config = getConfigAs(LeapBridgeConfig.class);
        String keystorePassword = (config.keystorePassword == null) ? "" : config.keystorePassword;

        String ipAddress = config.ipAddress;
        if (ipAddress == null || ipAddress.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "bridge address not specified");
            return;
        }

        reconnectInterval = (config.reconnect > 0) ? config.reconnect : DEFAULT_RECONNECT_MINUTES;
        heartbeatInterval = (config.heartbeat > 0) ? config.heartbeat : DEFAULT_HEARTBEAT_MINUTES;
        sendDelay = (config.delay < 0) ? 0 : config.delay;

        if (config.keystore == null || keystorePassword == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Keystore/keystore password not configured");
            return;
        } else {
            try (FileInputStream keystoreInputStream = new FileInputStream(config.keystore)) {
                logger.trace("Initializing keystore");
                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

                keystore.load(keystoreInputStream, keystorePassword.toCharArray());

                logger.trace("Initializing SSL Context");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keystore, keystorePassword.toCharArray());

                TrustManager[] trustManagers;
                if (config.certValidate) {
                    // Use default trust manager which will attempt to validate server certificate from hub
                    TrustManagerFactory tmf = TrustManagerFactory
                            .getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init(keystore);
                    trustManagers = tmf.getTrustManagers();
                } else {
                    // Use no-op trust manager which will not verify certificates
                    trustManagers = defineNoOpTrustManager();
                }

                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), trustManagers, null);

                sslsocketfactory = sslContext.getSocketFactory();
            } catch (FileNotFoundException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Keystore file not found");
                return;
            } catch (CertificateException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Certificate exception");
                return;
            } catch (UnrecoverableKeyException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Key unrecoverable with supplied password");
                return;
            } catch (KeyManagementException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Key management exception");
                logger.debug("Key management exception", e);
                return;
            } catch (KeyStoreException | NoSuchAlgorithmException | IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Error initializing keystore");
                logger.debug("Error initializing keystore", e);
                return;
            }
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Connecting");
        asyncInitializeTask = scheduler.submit(this::connect); // start the async connect task
    }

    /**
     * Return a no-op SSL trust manager which will not verify server or client certificates.
     */
    private TrustManager[] defineNoOpTrustManager() {
        return new TrustManager[] { new X509TrustManager() {
            @Override
            public void checkClientTrusted(final X509Certificate @Nullable [] chain, final @Nullable String authType) {
                logger.debug("Assuming client certificate is valid");
                if (chain != null && logger.isTraceEnabled()) {
                    for (int cert = 0; cert < chain.length; cert++) {
                        logger.trace("Subject DN: {}", chain[cert].getSubjectDN());
                        logger.trace("Issuer DN: {}", chain[cert].getIssuerDN());
                        logger.trace("Serial number {}:", chain[cert].getSerialNumber());
                    }
                }
            }

            @Override
            public void checkServerTrusted(final X509Certificate @Nullable [] chain, final @Nullable String authType) {
                logger.debug("Assuming server certificate is valid");
                if (chain != null && logger.isTraceEnabled()) {
                    for (int cert = 0; cert < chain.length; cert++) {
                        logger.trace("Subject DN: {}", chain[cert].getSubjectDN());
                        logger.trace("Issuer DN: {}", chain[cert].getIssuerDN());
                        logger.trace("Serial number: {}", chain[cert].getSerialNumber());
                    }
                }
            }

            @Override
            public X509Certificate @Nullable [] getAcceptedIssuers() {
                return null;
            }
        } };
    }

    private synchronized void connect() {
        deviceDataLoaded = false;
        buttonDataLoaded = false;

        try {
            logger.debug("Opening SSL connection to {}:{}", config.ipAddress, config.port);
            SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(config.ipAddress, config.port);
            sslsocket.startHandshake();
            writer = new BufferedWriter(new OutputStreamWriter(sslsocket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
            this.sslsocket = sslsocket;
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unknown host");
            return;
        } catch (IllegalArgumentException e) {
            // port out of valid range
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid port number");
            return;
        } catch (InterruptedIOException e) {
            logger.debug("Interrupted while establishing connection");
            Thread.currentThread().interrupt();
            return;
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error opening SSL connection. Check log.");
            logger.info("Error opening SSL connection: {}", e.getMessage());
            disconnect(false);
            scheduleConnectRetry(reconnectInterval); // Possibly a temporary problem. Try again later.
            return;
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, STATUS_INITIALIZING);

        Thread readerThread = new Thread(this::readerThreadJob, "Lutron reader");
        readerThread.setDaemon(true);
        readerThread.start();
        this.readerThread = readerThread;

        Thread senderThread = new Thread(this::senderThreadJob, "Lutron sender");
        senderThread.setDaemon(true);
        senderThread.start();
        this.senderThread = senderThread;

        sendCommand(new LeapCommand(Request.getButtonGroups()));
        queryDiscoveryData();
        sendCommand(new LeapCommand(Request.subscribeOccupancyGroupStatus()));

        logger.debug("Starting keepalive job with interval {}", heartbeatInterval);
        keepAliveJob = scheduler.scheduleWithFixedDelay(this::sendKeepAlive, heartbeatInterval, heartbeatInterval,
                TimeUnit.MINUTES);
    }

    /**
     * Called by connect() and discovery service to request fresh discovery data
     */
    public void queryDiscoveryData() {
        sendCommand(new LeapCommand(Request.getDevices()));
        sendCommand(new LeapCommand(Request.getAreas()));
        sendCommand(new LeapCommand(Request.getOccupancyGroups()));
    }

    private void scheduleConnectRetry(long waitMinutes) {
        logger.debug("Scheduling connection retry in {} minutes", waitMinutes);
        connectRetryJob = scheduler.schedule(this::connect, waitMinutes, TimeUnit.MINUTES);
    }

    /**
     * Disconnect from bridge, cancel retry and keepalive jobs, stop reader and writer threads, and clean up.
     *
     * @param interruptAll Set if reconnect task should be interrupted if running. Should be false when calling from
     *            connect or reconnect, and true when calling from dispose.
     */
    private synchronized void disconnect(boolean interruptAll) {
        logger.debug("Disconnecting");

        ScheduledFuture<?> connectRetryJob = this.connectRetryJob;
        if (connectRetryJob != null) {
            connectRetryJob.cancel(true);
        }
        ScheduledFuture<?> keepAliveJob = this.keepAliveJob;
        if (keepAliveJob != null) {
            keepAliveJob.cancel(true);
        }

        reconnectTaskCancel(interruptAll); // May be called from keepAliveReconnectJob thread

        Thread senderThread = this.senderThread;
        if (senderThread != null && senderThread.isAlive()) {
            senderThread.interrupt();
        }

        Thread readerThread = this.readerThread;
        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
        }
        SSLSocket sslsocket = this.sslsocket;
        if (sslsocket != null) {
            try {
                sslsocket.close();
            } catch (IOException e) {
                logger.debug("Error closing SSL socket: {}", e.getMessage());
            }
            this.sslsocket = null;
        }
        BufferedReader reader = this.reader;
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                logger.debug("Error closing reader: {}", e.getMessage());
            }
        }
        BufferedWriter writer = this.writer;
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                logger.debug("Error closing writer: {}", e.getMessage());
            }
        }

        deviceDataLoaded = false;
        buttonDataLoaded = false;
    }

    private synchronized void reconnect() {
        logger.debug("Attempting to reconnect to the bridge");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "reconnecting");
        disconnect(false);
        connect();
    }

    /**
     * Method executed by the message sender thread (senderThread)
     */
    private void senderThreadJob() {
        logger.debug("Command sender thread started");
        try {
            while (!Thread.currentThread().isInterrupted() && writer != null) {
                LeapCommand command = sendQueue.take();
                logger.trace("Sending command {}", command);

                try {
                    BufferedWriter writer = this.writer;
                    if (writer != null) {
                        writer.write(command.toString() + "\n");
                        writer.flush();
                    }
                } catch (InterruptedIOException e) {
                    logger.debug("Interrupted while sending");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Interrupted");
                    break; // exit loop and terminate thread
                } catch (IOException e) {
                    logger.warn("Communication error, will try to reconnect. Error: {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    sendQueue.add(command); // Requeue command
                    reconnect();
                    break; // reconnect() will start a new thread; terminate this one
                }
                if (sendDelay > 0) {
                    Thread.sleep(sendDelay); // introduce delay to throttle send rate
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            logger.debug("Command sender thread exiting");
        }
    }

    /**
     * Method executed by the message reader thread (readerThread)
     */
    private void readerThreadJob() {
        logger.debug("Message reader thread started");
        String msg = null;
        try {
            BufferedReader reader = this.reader;
            while (!Thread.interrupted() && reader != null && (msg = reader.readLine()) != null) {
                leapMessageParser.handleMessage(msg);
            }
            if (msg == null) {
                logger.debug("End of input stream detected");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection lost");
            }
        } catch (InterruptedIOException e) {
            logger.debug("Interrupted while reading");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Interrupted");
        } catch (IOException e) {
            logger.debug("I/O error while reading from stream: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Runtime exception in reader thread", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } finally {
            logger.debug("Message reader thread exiting");
        }
    }

    /**
     * Called if NoContent response received for a buttongroup read request. Creates empty deviceButtonMap.
     */
    @Override
    public void handleEmptyButtonGroupDefinition() {
        logger.debug("No content in button group definition. Creating empty deviceButtonMap.");
        Map<Integer, List<Integer>> deviceButtonMap = new HashMap<>();
        synchronized (deviceButtonMapLock) {
            this.deviceButtonMap = deviceButtonMap;
            buttonDataLoaded = true;
        }
        checkInitialized();
    }

    /**
     * Set state to online if offline/initializing and all required initialization info is loaded.
     * Currently this means device (zone) and button group data.
     */
    private void checkInitialized() {
        ThingStatusInfo statusInfo = getThing().getStatusInfo();
        if (statusInfo.getStatus() == ThingStatus.OFFLINE && STATUS_INITIALIZING.equals(statusInfo.getDescription())) {
            if (deviceDataLoaded && buttonDataLoaded) {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    /**
     * Notify child thing handler of a zonelevel update from a received zone status message.
     */
    @Override
    public void handleZoneUpdate(ZoneStatus zoneStatus) {
        logger.trace("Zone: {} level: {}", zoneStatus.getZone(), zoneStatus.level);
        Integer integrationId = zoneToDevice(zoneStatus.getZone());

        if (integrationId == null) {
            logger.debug("Unable to map zone {} to device", zoneStatus.getZone());
            return;
        }
        logger.trace("Zone {} mapped to device id {}", zoneStatus.getZone(), integrationId);

        // dispatch update to proper thing handler
        LutronHandler handler = findThingHandler(integrationId);
        if (handler != null) {
            if (zoneStatus.fanSpeed != null) {
                // handle fan controller
                FanSpeedType fanSpeed = zoneStatus.fanSpeed;
                try {
                    handler.handleUpdate(LutronCommandType.OUTPUT, OutputCommand.ACTION_ZONELEVEL.toString(),
                            Integer.valueOf(fanSpeed.speed()).toString());
                } catch (NumberFormatException e) {
                    logger.warn("Number format exception parsing update");
                } catch (RuntimeException e) {
                    logger.warn("Runtime exception while processing update");
                }
            } else {
                // handle switch/dimmer/shade
                try {
                    handler.handleUpdate(LutronCommandType.OUTPUT, OutputCommand.ACTION_ZONELEVEL.toString(),
                            Integer.valueOf(zoneStatus.level).toString());
                } catch (NumberFormatException e) {
                    logger.warn("Number format exception parsing update");
                } catch (RuntimeException e) {
                    logger.warn("Runtime exception while processing update");
                }
            }
        } else {
            logger.debug("No thing configured for integration ID {}", integrationId);
        }
    }

    /**
     * Notify child group handler of a received occupancy group update.
     *
     * @param occupancyStatus
     * @param groupNumber
     */
    @Override
    public void handleGroupUpdate(int groupNumber, String occupancyStatus) {
        logger.trace("Group {} state update: {}", groupNumber, occupancyStatus);

        // dispatch update to proper handler
        OGroupHandler handler = findGroupHandler(groupNumber);
        if (handler != null) {
            try {
                switch (occupancyStatus) {
                    case "Occupied":
                        handler.handleUpdate(LutronCommandType.GROUP, GroupCommand.ACTION_GROUPSTATE.toString(),
                                GroupCommand.STATE_GRP_OCCUPIED.toString());
                        break;
                    case "Unoccupied":
                        handler.handleUpdate(LutronCommandType.GROUP, GroupCommand.ACTION_GROUPSTATE.toString(),
                                GroupCommand.STATE_GRP_UNOCCUPIED.toString());
                        break;
                    case "Unknown":
                        handler.handleUpdate(LutronCommandType.GROUP, GroupCommand.ACTION_GROUPSTATE.toString(),
                                GroupCommand.STATE_GRP_UNKNOWN.toString());
                        break;
                    default:
                        logger.debug("Unexpected occupancy status: {}", occupancyStatus);
                        return;
                }
            } catch (NumberFormatException e) {
                logger.warn("Number format exception parsing update");
            } catch (RuntimeException e) {
                logger.warn("Runtime exception while processing update");
            }
        } else {
            logger.debug("No group thing configured for group ID {}", groupNumber);
        }
    }

    @Override
    public void handleMultipleButtonGroupDefinition(List<ButtonGroup> buttonGroupList) {
        Map<Integer, List<Integer>> deviceButtonMap = new HashMap<>();

        for (ButtonGroup buttonGroup : buttonGroupList) {
            int parentDevice = buttonGroup.getParentDevice();
            logger.trace("Found ButtonGroup: {} parent device: {}", buttonGroup.getButtonGroup(), parentDevice);
            List<Integer> buttonList = buttonGroup.getButtonList();
            deviceButtonMap.put(parentDevice, buttonList);
        }
        synchronized (deviceButtonMapLock) {
            this.deviceButtonMap = deviceButtonMap;
            buttonDataLoaded = true;
        }
        checkInitialized();
    }

    @Override
    public void handleMultipleDeviceDefintion(List<Device> deviceList) {
        synchronized (zoneMapsLock) {
            zoneToDevice.clear();
            deviceToZone.clear();
            for (Device device : deviceList) {
                Integer zoneid = device.getZone();
                Integer deviceid = device.getDevice();
                logger.trace("Found device: {} id: {} zone: {}", device.name, deviceid, zoneid);
                if (zoneid > 0 && deviceid > 0) {
                    zoneToDevice.put(zoneid, deviceid);
                    deviceToZone.put(deviceid, zoneid);
                }
                if (deviceid == 1) { // ID 1 is the bridge
                    setBridgeProperties(device);
                }
            }
        }
        deviceDataLoaded = true;
        checkInitialized();

        LeapDeviceDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.processDeviceDefinitions(deviceList);
        }
    }

    @Override
    public void handleMultipleAreaDefinition(List<Area> areaList) {
        LeapDeviceDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.setAreas(areaList);
        }
    }

    @Override
    public void handleMultipleOccupancyGroupDefinition(List<OccupancyGroup> oGroupList) {
        LeapDeviceDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.setOccupancyGroups(oGroupList);
        }
    }

    @Override
    public void validMessageReceived(String communiqueType) {
        reconnectTaskCancel(true); // Got a good message, so cancel reconnect task.
    }

    /**
     * Set informational bridge properties from the Device entry for the hub/repeater
     */
    private void setBridgeProperties(Device device) {
        if (device.getDevice() == 1 && device.repeaterProperties != null) {
            Map<String, String> properties = editProperties();
            if (device.name != null) {
                properties.put(PROPERTY_PRODTYP, device.name);
            }
            if (device.modelNumber != null) {
                properties.put(Thing.PROPERTY_MODEL_ID, device.modelNumber);
            }
            if (device.serialNumber != null) {
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, device.serialNumber);
            }
            if (device.firmwareImage != null && device.firmwareImage.firmware != null
                    && device.firmwareImage.firmware.displayName != null) {
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, device.firmwareImage.firmware.displayName);
            }
            updateProperties(properties);
        }
    }

    /**
     * Queue a LeapCommand for transmission by the sender thread.
     */
    public void sendCommand(@Nullable LeapCommand command) {
        if (command != null) {
            sendQueue.add(command);
        }
    }

    /**
     * Convert a LutronCommand into a LeapCommand and queue it for transmission by the sender thread.
     */
    @Override
    public void sendCommand(LutronCommandNew command) {
        logger.trace("Received request to send Lutron command: {}", command);
        sendCommand(command.leapCommand(this, deviceToZone(command.getIntegrationId())));
    }

    /**
     * Returns LEAP button number for given integrationID and component. Returns 0 if button number cannot be
     * determined.
     */
    public int getButton(int integrationID, int component) {
        synchronized (deviceButtonMapLock) {
            if (deviceButtonMap != null) {
                List<Integer> buttonList = deviceButtonMap.get(integrationID);
                if (buttonList != null && component <= buttonList.size()) {
                    return buttonList.get(component - 1);
                } else {
                    logger.debug("Could not find button component {} for id {}", component, integrationID);
                    return 0;
                }
            } else {
                logger.debug("Device to button map not populated");
                return 0;
            }
        }
    }

    private @Nullable LutronHandler findThingHandler(@Nullable Integer integrationId) {
        if (integrationId != null) {
            return childHandlerMap.get(integrationId);
        } else {
            return null;
        }
    }

    private @Nullable OGroupHandler findGroupHandler(int integrationId) {
        return groupHandlerMap.get(integrationId);
    }

    private @Nullable Integer zoneToDevice(int zone) {
        synchronized (zoneMapsLock) {
            return zoneToDevice.get(zone);
        }
    }

    private @Nullable Integer deviceToZone(@Nullable Integer device) {
        if (device == null) {
            return null;
        }
        synchronized (zoneMapsLock) {
            return deviceToZone.get(device);
        }
    }

    /**
     * Executed by keepAliveJob. Sends a LEAP ping request and schedules a reconnect task.
     */
    private void sendKeepAlive() {
        logger.trace("Sending keepalive query");
        sendCommand(new LeapCommand(Request.ping()));
        // Reconnect if no response is received within KEEPALIVE_TIMEOUT_SECONDS.
        reconnectTaskSchedule();
    }

    /**
     * Schedules the reconnect task keepAliveReconnectJob to execute in KEEPALIVE_TIMEOUT_SECONDS. This should be
     * cancelled by calling reconnectTaskCancel() if a valid response is received from the bridge.
     */
    private void reconnectTaskSchedule() {
        synchronized (keepAliveReconnectLock) {
            keepAliveReconnectJob = scheduler.schedule(this::keepaliveTimeoutExpired, KEEPALIVE_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Cancels the reconnect task keepAliveReconnectJob.
     */
    private void reconnectTaskCancel(boolean interrupt) {
        synchronized (keepAliveReconnectLock) {
            ScheduledFuture<?> keepAliveReconnectJob = this.keepAliveReconnectJob;
            if (keepAliveReconnectJob != null) {
                logger.trace("Canceling scheduled reconnect job.");
                keepAliveReconnectJob.cancel(interrupt);
                this.keepAliveReconnectJob = null;
            }
        }
    }

    /**
     * Executed by keepAliveReconnectJob if it is not cancelled by the LEAP message parser calling
     * validMessageReceived() which in turn calls reconnectTaskCancel().
     */
    private void keepaliveTimeoutExpired() {
        logger.debug("Keepalive response timeout expired. Initiating reconnect.");
        reconnect();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_COMMAND)) {
            if (command instanceof StringType) {
                sendCommand(new LeapCommand(command.toString()));
            }
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof OGroupHandler) {
            // We need a different map for group things because the numbering is separate
            OGroupHandler handler = (OGroupHandler) childHandler;
            int groupId = handler.getIntegrationId();
            groupHandlerMap.put(groupId, handler);
            logger.trace("Registered group handler for ID {}", groupId);
        } else {
            LutronHandler handler = (LutronHandler) childHandler;
            int intId = handler.getIntegrationId();
            childHandlerMap.put(intId, handler);
            logger.trace("Registered child handler for ID {}", intId);
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof OGroupHandler) {
            OGroupHandler handler = (OGroupHandler) childHandler;
            int groupId = handler.getIntegrationId();
            groupHandlerMap.remove(groupId);
            logger.trace("Unregistered group handler for ID {}", groupId);
        } else {
            LutronHandler handler = (LutronHandler) childHandler;
            int intId = handler.getIntegrationId();
            childHandlerMap.remove(intId);
            logger.trace("Unregistered child handler for ID {}", intId);
        }
    }

    @Override
    public void dispose() {
        Future<?> asyncInitializeTask = this.asyncInitializeTask;
        if (asyncInitializeTask != null && !asyncInitializeTask.isDone()) {
            asyncInitializeTask.cancel(true); // Interrupt async init task if it isn't done yet
        }
        disconnect(true);
    }
}
