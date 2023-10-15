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
package org.openhab.binding.elroconnects.internal.handler;

import static org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.ElroDeviceType;
import org.openhab.binding.elroconnects.internal.ElroConnectsDynamicStateDescriptionProvider;
import org.openhab.binding.elroconnects.internal.ElroConnectsMessage;
import org.openhab.binding.elroconnects.internal.devices.ElroConnectsDevice;
import org.openhab.binding.elroconnects.internal.devices.ElroConnectsDeviceCxsmAlarm;
import org.openhab.binding.elroconnects.internal.devices.ElroConnectsDeviceEntrySensor;
import org.openhab.binding.elroconnects.internal.devices.ElroConnectsDeviceGenericAlarm;
import org.openhab.binding.elroconnects.internal.devices.ElroConnectsDeviceMotionSensor;
import org.openhab.binding.elroconnects.internal.devices.ElroConnectsDevicePowerSocket;
import org.openhab.binding.elroconnects.internal.devices.ElroConnectsDeviceTemperatureSensor;
import org.openhab.binding.elroconnects.internal.discovery.ElroConnectsDiscoveryService;
import org.openhab.binding.elroconnects.internal.util.ElroConnectsUtil;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link ElroConnectsBridgeHandler} is the bridge handler responsible to for handling all communication with the
 * ELRO Connects K1 Hub. All individual device communication passes through the hub.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class ElroConnectsBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(ElroConnectsBridgeHandler.class);

    private static final int PORT = 1025; // UDP port for UDP socket communication with K1 hub
    private static final int RESPONSE_TIMEOUT_MS = 5000; // max time to wait for receiving all responses on a request

    // Default scene names are not received from K1 hub, so kept here
    private static final Map<Integer, String> DEFAULT_SCENES = Map.ofEntries(Map.entry(0, "Home"), Map.entry(1, "Away"),
            Map.entry(2, "Sleep"));
    private static final int MAX_DEFAULT_SCENE = 2;

    // Command filter when syncing devices and scenes, other values would filter what gets received
    private static final String SYNC_COMMAND = "0002";

    // Regex for valid connectorId
    private static final Pattern CONNECTOR_ID_PATTERN = Pattern.compile("^ST_([0-9a-f]){12}$");

    // Message string for acknowledging receipt of data
    private static final String ACK_STRING = "{\"answer\": \"APP_answer_OK\"}";
    private static final byte[] ACK = ACK_STRING.getBytes(StandardCharsets.UTF_8);

    // Connector expects to receive messages with an increasing id for each message
    // Max msgId is 65536, therefore use short and convert to unsigned Integer when using it
    private short msgId;

    private NetworkAddressService networkAddressService;

    // Used when restarting connection, delay restart for 1s to avoid high network traffic
    private volatile boolean restart;
    static final int RESTART_DELAY_MS = 1000;

    private volatile String connectorId = "";
    // Used for getting IP address and keep connection alive messages
    private static final String QUERY_BASE_STRING = "IOT_KEY?";
    private volatile String queryString = QUERY_BASE_STRING + connectorId;
    // Regex to retrieve ctrlKey from response on IP address message
    private static final Pattern CTRL_KEY_PATTERN = Pattern.compile("KEY:([0-9a-f]*)");

    private int refreshInterval = 60;
    private volatile @Nullable InetAddress addr;
    private volatile String ctrlKey = "";

    private boolean legacyFirmware = false;

    private volatile @Nullable DatagramSocket socket;
    private volatile @Nullable DatagramPacket ackPacket;

    private volatile @Nullable ScheduledFuture<?> syncFuture;
    private volatile @Nullable CompletableFuture<Boolean> awaitResponse;

    private ElroConnectsDynamicStateDescriptionProvider stateDescriptionProvider;

    private final Map<Integer, String> scenes = new ConcurrentHashMap<>();
    private final Map<Integer, ElroConnectsDevice> devices = new ConcurrentHashMap<>();
    private final Map<Integer, ElroConnectsDeviceHandler> deviceHandlers = new ConcurrentHashMap<>();

    private int currentScene;

    // We only keep 2 gson adapters used to serialize and deserialize all messages sent and received
    private final Gson gsonOut = new Gson();
    private Gson gsonIn = new Gson();

    private @Nullable ElroConnectsDiscoveryService discoveryService = null;

    public ElroConnectsBridgeHandler(Bridge bridge, NetworkAddressService networkAddressService,
            ElroConnectsDynamicStateDescriptionProvider stateDescriptionProvider) {
        super(bridge);
        this.networkAddressService = networkAddressService;
        this.stateDescriptionProvider = stateDescriptionProvider;

        resetScenes();
    }

    @Override
    public void initialize() {
        ElroConnectsBridgeConfiguration config = getConfigAs(ElroConnectsBridgeConfiguration.class);
        connectorId = config.connectorId;
        refreshInterval = config.refreshInterval;
        legacyFirmware = config.legacyFirmware;

        if (connectorId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.no-connector-id");
            return;
        } else if (!CONNECTOR_ID_PATTERN.matcher(connectorId).matches()) {
            String msg = String.format("@text/offline.invalid-connector-id [ \"%s\" ]", connectorId);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
            return;
        }

        queryString = QUERY_BASE_STRING + connectorId;

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.submit(this::startCommunication);
    }

    @Override
    public void dispose() {
        stopCommunication();
    }

    private synchronized void startCommunication() {
        ElroConnectsBridgeConfiguration config = getConfigAs(ElroConnectsBridgeConfiguration.class);
        InetAddress addr = null;

        // First try with configured IP address if there is one
        String ipAddress = config.ipAddress;
        if (!ipAddress.isEmpty()) {
            try {
                this.addr = InetAddress.getByName(ipAddress);
                addr = getAddr(false);
            } catch (IOException e) {
                logger.warn("Unknown host for {}, trying to discover address", ipAddress);
            }
        }

        // Then try broadcast to detect IP address if configured IP address did not work
        if (addr == null) {
            try {
                addr = getAddr(true);
            } catch (IOException e) {
                String msg = String.format("@text/offline.find-ip-fail [ \"%s\" ]", connectorId);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
                stopCommunication();
                return;
            }
        }

        if (addr == null) {
            String msg = String.format("@text/offline.find-ip-fail [ \"%s\" ]", connectorId);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
            stopCommunication();
            return;
        }

        // Found valid IP address, update configuration with detected IP address
        Configuration configuration = thing.getConfiguration();
        configuration.put(CONFIG_IP_ADDRESS, addr.getHostAddress());
        updateConfiguration(configuration);

        String ctrlKey = this.ctrlKey;
        if (ctrlKey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-data-error");
            stopCommunication();
            return;
        }

        DatagramSocket socket;
        try {
            socket = createSocket(false);
            this.socket = socket;
        } catch (IOException e) {
            String msg = String.format("@text/offline.communication-error [ \"%s\" ]", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
            stopCommunication();
            return;
        }

        ackPacket = new DatagramPacket(ACK, ACK.length, addr, PORT);

        logger.debug("Connected to connector {} at {}:{}", connectorId, addr, PORT);

        try {
            // Start ELRO Connects listener. This listener will act on all messages coming from ELRO K1 Connector.
            (new NamedThreadFactory(THREAD_NAME_PREFIX + thing.getUID().getAsString()).newThread(this::runElroEvents))
                    .start();

            keepAlive();

            // First get status, then name. The status response contains the device types needed to instantiate correct
            // classes.
            getDeviceStatuses();
            getDeviceNames();

            syncScenes();
            getCurrentScene();

            updateStatus(ThingStatus.ONLINE);

            // Enable discovery of devices
            ElroConnectsDiscoveryService service = discoveryService;
            if (service != null) {
                service.startBackgroundDiscovery();
            }
        } catch (IOException e) {
            String msg = String.format("@text/offline.communication-error [ \"%s\" ]", e.getMessage());
            restartCommunication(msg);
            return;
        }

        scheduleSyncStatus();
    }

    /**
     * Get the IP address and ctrl key of the connector by broadcasting message with connectorId. This should be used
     * when initializing the connection. the ctrlKey and addr fields are set.
     *
     * @param broadcast, if true find address by broadcast, otherwise simply send to configured address to retrieve key
     *            only
     * @return IP address of connector
     * @throws IOException
     */
    private @Nullable InetAddress getAddr(boolean broadcast) throws IOException {
        try (DatagramSocket socket = createSocket(true)) {
            String response = sendAndReceive(socket, queryString, broadcast);
            Matcher keyMatcher = CTRL_KEY_PATTERN.matcher(response);
            ctrlKey = keyMatcher.find() ? keyMatcher.group(1) : "";
            logger.debug("Key: {}", ctrlKey);

            return addr;
        }
    }

    /**
     * Send keep alive message.
     *
     * @throws IOException
     */
    private void keepAlive() throws IOException {
        DatagramSocket socket = this.socket;
        if (socket != null) {
            logger.trace("Keep alive");
            // Sending query string, so the connection with the K1 hub stays alive
            awaitResponse(true);
            send(socket, queryString, false);
        } else {
            restartCommunication("@text/offline.no-socket");
        }
    }

    /**
     * Cleanup socket when the communication with ELRO Connects connector is closed.
     *
     */
    private synchronized void stopCommunication() {
        ScheduledFuture<?> sync = syncFuture;
        if (sync != null) {
            sync.cancel(true);
        }
        syncFuture = null;

        stopAwaitResponse();

        DatagramSocket socket = this.socket;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        this.socket = null;

        logger.debug("Communication stopped");
    }

    /**
     * Close and restart communication with ELRO Connects system, to be called after error in communication.
     *
     * @param offlineMessage message for thing status
     */
    private synchronized void restartCommunication(String offlineMessage) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, offlineMessage);

        stopCommunication();

        if (!restart) {
            logger.debug("Restart communication");

            restart = true;
            scheduler.schedule(this::startFromRestart, RESTART_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }

    private synchronized void startFromRestart() {
        restart = false;
        if (ThingStatus.OFFLINE.equals(thing.getStatus())) {
            startCommunication();
        }
    }

    private DatagramSocket createSocket(boolean timeout) throws SocketException {
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
        if (timeout) {
            socket.setSoTimeout(1000);
        }
        return socket;
    }

    /**
     * Read messages received through UDP socket.
     *
     * @param socket
     */
    private void runElroEvents() {
        DatagramSocket socket = this.socket;

        if (socket != null) {
            logger.debug("Listening for messages");

            try {
                byte[] buffer = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                while (!Thread.interrupted()) {
                    String response = receive(socket, packet);
                    processMessage(socket, response);
                }
            } catch (IOException e) {
                String msg = String.format("@text/offline.communication-error [ \"%s\" ]", e.getMessage());
                restartCommunication(msg);
            }
        } else {
            restartCommunication("@text/offline.no-socket");
        }
    }

    /**
     * Schedule regular queries to sync devices and scenes.
     */
    private void scheduleSyncStatus() {
        syncFuture = scheduler.scheduleWithFixedDelay(() -> {
            try {
                keepAlive();
                syncDevices();
                syncScenes();
                getCurrentScene();
            } catch (IOException e) {
                String msg = String.format("@text/offline.communication-error [ \"%s\" ]", e.getMessage());
                restartCommunication(msg);
            }
        }, refreshInterval, refreshInterval, TimeUnit.SECONDS);
    }

    /**
     * Process response received from K1 Hub and send acknowledgement through open socket.
     *
     * @param socket
     * @param response
     * @throws IOException
     */
    private void processMessage(DatagramSocket socket, String response) throws IOException {
        if (!response.startsWith("{")) {
            // Not a Json to interpret, just ignore
            stopAwaitResponse();
            return;
        }
        ElroConnectsMessage message;
        String json = "";
        try {
            json = response.split("\\R")[0];
            message = gsonIn.fromJson(json, ElroConnectsMessage.class);
            sendAck(socket);
        } catch (JsonSyntaxException ignore) {
            logger.debug("Cannot decode, not a valid json: {}", json);
            return;
        }

        if (message == null) {
            return;
        }

        switch (message.getCmdId()) {
            case ELRO_IGNORE_YES_NO:
                break;
            case ELRO_REC_DEVICE_NAME:
                processDeviceNameMessage(message);
                break;
            case ELRO_REC_DEVICE_STATUS:
                processDeviceStatusMessage(message);
                break;
            case ELRO_REC_ALARM:
                processAlarmTriggerMessage(message);
                break;
            case ELRO_REC_SCENE_NAME:
                processSceneNameMessage(message);
                break;
            case ELRO_REC_SCENE_TYPE:
                processSceneTypeMessage(message);
                break;
            case ELRO_REC_SCENE:
                processSceneMessage(message);
                break;
            default:
                logger.debug("CmdId not implemented: {}", message.getCmdId());
        }
    }

    private void processDeviceStatusMessage(ElroConnectsMessage message) {
        int deviceId = message.getDeviceId();
        String deviceStatus = message.getDeviceStatus();
        if ("OVER".equals(deviceStatus)) {
            // last message in series received
            stopAwaitResponse();
            return;
        }

        ElroConnectsDevice device = devices.get(deviceId);
        device = (device == null) ? addDevice(message) : device;
        if (device == null) {
            // device type not recognized, could not be added
            return;
        }
        device.setDeviceStatus(deviceStatus);

        device.updateState();
    }

    private void processDeviceNameMessage(ElroConnectsMessage message) {
        String answerContent = message.getAnswerContent();
        if ("NAME_OVER".equals(answerContent)) {
            // last message in series received
            stopAwaitResponse();
            return;
        }
        if (answerContent.length() <= 4) {
            logger.debug("Could not decode answer {}", answerContent);
            return;
        }

        int deviceId = Integer.parseInt(answerContent.substring(0, 4), 16);
        String deviceName = ElroConnectsUtil.decode(answerContent.substring(4));
        ElroConnectsDevice device = devices.get(deviceId);
        if (device != null) {
            device.setDeviceName(deviceName);
            logger.debug("Device ID {} name: {}", deviceId, deviceName);
        }
    }

    private void processSceneNameMessage(ElroConnectsMessage message) {
        int sceneId = message.getSceneGroup();
        String answerContent = message.getAnswerContent();
        String sceneName;
        if (sceneId > MAX_DEFAULT_SCENE) {
            if (answerContent.length() < 44) {
                logger.debug("Could not decode answer {}", answerContent);
                return;
            }
            sceneName = ElroConnectsUtil.decode(answerContent.substring(6, 38));
            scenes.put(sceneId, sceneName);
            logger.debug("Scene ID {} name: {}", sceneId, sceneName);
        }
    }

    private void processSceneTypeMessage(ElroConnectsMessage message) {
        String sceneContent = message.getSceneContent();
        if ("OVER".equals(sceneContent)) {
            // last message in series received
            stopAwaitResponse();

            updateSceneOptions();
        }
    }

    private void processSceneMessage(ElroConnectsMessage message) {
        int sceneId = message.getSceneGroup();

        currentScene = sceneId;

        updateState(SCENE, new StringType(String.valueOf(currentScene)));
    }

    private void processAlarmTriggerMessage(ElroConnectsMessage message) {
        String answerContent = message.getAnswerContent();
        if (answerContent.length() < 10) {
            logger.debug("Could not decode answer {}", answerContent);
            return;
        }

        int deviceId = Integer.parseInt(answerContent.substring(6, 10), 16);

        ElroConnectsDeviceHandler handler = deviceHandlers.get(deviceId);
        if (handler != null) {
            handler.triggerAlarm();
        }
        // Also trigger an alarm on the bridge, so the alarm also comes through when no thing for the device is
        // configured
        triggerChannel(ALARM, Integer.toString(deviceId));
        logger.debug("Device ID {} alarm", deviceId);

        if (answerContent.length() < 22) {
            logger.debug("Could not get device status from alarm message for device {}", deviceId);
            return;
        }
        String deviceStatus = answerContent.substring(14, 22);
        ElroConnectsDevice device = devices.get(deviceId);
        if (device != null) {
            device.setDeviceStatus(deviceStatus);
            device.updateState();
        }
    }

    private @Nullable ElroConnectsDevice addDevice(ElroConnectsMessage message) {
        int deviceId = message.getDeviceId();
        String deviceType = message.getDeviceName();
        ElroDeviceType type = TYPE_MAP.getOrDefault(deviceType, ElroDeviceType.DEFAULT);

        ElroConnectsDevice device;
        switch (type) {
            case CO_ALARM:
            case SM_ALARM:
            case WT_ALARM:
            case THERMAL_ALARM:
                device = new ElroConnectsDeviceGenericAlarm(deviceId, this);
                break;
            case CXSM_ALARM:
                device = new ElroConnectsDeviceCxsmAlarm(deviceId, this);
                break;
            case POWERSOCKET:
                device = new ElroConnectsDevicePowerSocket(deviceId, this);
                break;
            case ENTRY_SENSOR:
                device = new ElroConnectsDeviceEntrySensor(deviceId, this);
                break;
            case MOTION_SENSOR:
                device = new ElroConnectsDeviceMotionSensor(deviceId, this);
                break;
            case TH_SENSOR:
                device = new ElroConnectsDeviceTemperatureSensor(deviceId, this);
                break;
            default:
                logger.debug("Device type {} not supported", deviceType);
                return null;
        }
        device.setDeviceType(deviceType);
        devices.put(deviceId, device);
        return device;
    }

    /**
     * Just before sending message, this method should be called to make sure we wait for all responses that are still
     * expected to be received. The last response will be indicated by a token in the last response message.
     *
     * @param waitResponse true if we want to wait for response for next message to be sent before allowing subsequent
     *            message
     */
    private void awaitResponse(boolean waitResponse) {
        CompletableFuture<Boolean> waiting = awaitResponse;
        if (waiting != null) {
            try {
                logger.trace("Waiting for previous response before sending");
                waiting.get(RESPONSE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ignore) {
                logger.trace("Wait for previous response timed out");
            }
        }
        awaitResponse = waitResponse ? new CompletableFuture<>() : null;
    }

    /**
     * This method is called when all responses on a request have been received.
     */
    private void stopAwaitResponse() {
        CompletableFuture<Boolean> future = awaitResponse;
        if (future != null) {
            future.complete(true);
        }
        awaitResponse = null;
    }

    private void sendAck(DatagramSocket socket) throws IOException {
        logger.debug("Send Ack: {}", ACK_STRING);
        socket.send(ackPacket);
    }

    private String sendAndReceive(DatagramSocket socket, String query, boolean broadcast)
            throws UnknownHostException, IOException {
        send(socket, query, broadcast);
        byte[] buffer = new byte[4096];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        return receive(socket, packet);
    }

    private void send(DatagramSocket socket, String query, boolean broadcast) throws IOException {
        final InetAddress address = broadcast
                ? InetAddress.getByName(networkAddressService.getConfiguredBroadcastAddress())
                : addr;
        if (address == null) {
            if (broadcast) {
                restartCommunication("@text/offline.no-broadcast-address");
            } else {
                restartCommunication("@text/offline.no-hub-address");
            }
            return;
        }
        logger.debug("Send: {}", query);
        final byte[] queryBuffer = query.getBytes(StandardCharsets.UTF_8);
        DatagramPacket queryPacket = new DatagramPacket(queryBuffer, queryBuffer.length, address, PORT);
        socket.send(queryPacket);
    }

    private String receive(DatagramSocket socket, DatagramPacket packet) throws IOException {
        socket.receive(packet);
        String response = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
        logger.debug("Received: {}", response);
        addr = packet.getAddress();
        return response;
    }

    /**
     * Basic method to send an {@link ElroConnectsMessage} to the K1 hub.
     *
     * @param elroMessage
     * @param waitResponse true if no new messages should be allowed to be sent before receiving the full response
     * @throws IOException
     */
    private synchronized void sendElroMessage(ElroConnectsMessage elroMessage, boolean waitResponse)
            throws IOException {
        DatagramSocket socket = this.socket;
        if (socket != null) {
            String message = gsonOut.toJson(elroMessage);
            awaitResponse(waitResponse);
            send(socket, message, false);
        } else {
            throw new IOException("No socket");
        }
    }

    /**
     * Send device control command. The device command string various by device type. The calling method is responsible
     * for creating the appropriate command string.
     *
     * @param deviceId
     * @param deviceCommand ELRO Connects device command string
     * @throws IOException
     */
    public void deviceControl(int deviceId, String deviceCommand) throws IOException {
        String connectorId = this.connectorId;
        String ctrlKey = this.ctrlKey;
        logger.debug("Device control {}, status {}", deviceId, deviceCommand);
        ElroConnectsMessage elroMessage = new ElroConnectsMessage(msgIdIncrement(), connectorId, ctrlKey,
                ELRO_DEVICE_CONTROL, legacyFirmware).withDeviceId(deviceId).withDeviceStatus(deviceCommand);
        sendElroMessage(elroMessage, false);
    }

    public void renameDevice(int deviceId, String deviceName) throws IOException {
        String connectorId = this.connectorId;
        String ctrlKey = this.ctrlKey;
        String encodedName = ElroConnectsUtil.encode(deviceName, 15);
        encodedName = encodedName + ElroConnectsUtil.crc16(encodedName);
        logger.debug("Rename device {} to {}", deviceId, deviceName);
        ElroConnectsMessage elroMessage = new ElroConnectsMessage(msgIdIncrement(), connectorId, ctrlKey,
                ELRO_DEVICE_RENAME, legacyFirmware).withDeviceId(deviceId).withDeviceName(encodedName);
        sendElroMessage(elroMessage, false);
    }

    private void joinDevice() throws IOException {
        String connectorId = this.connectorId;
        String ctrlKey = this.ctrlKey;
        logger.debug("Put hub in join device mode");
        ElroConnectsMessage elroMessage = new ElroConnectsMessage(msgIdIncrement(), connectorId, ctrlKey,
                ELRO_DEVICE_JOIN);
        sendElroMessage(elroMessage, false);
    }

    private void cancelJoinDevice() throws IOException {
        String connectorId = this.connectorId;
        String ctrlKey = this.ctrlKey;
        logger.debug("Cancel hub in join device mode");
        ElroConnectsMessage elroMessage = new ElroConnectsMessage(msgIdIncrement(), connectorId, ctrlKey,
                ELRO_DEVICE_CANCEL_JOIN);
        sendElroMessage(elroMessage, false);
    }

    private void removeDevice(int deviceId) throws IOException {
        if (devices.remove(deviceId) == null) {
            logger.debug("Device {} not known, cannot remove", deviceId);
            return;
        }
        ThingHandler handler = getDeviceHandler(deviceId);
        if (handler != null) {
            handler.dispose();
        }
        String connectorId = this.connectorId;
        String ctrlKey = this.ctrlKey;
        logger.debug("Remove device {} from hub", deviceId);
        ElroConnectsMessage elroMessage = new ElroConnectsMessage(msgIdIncrement(), connectorId, ctrlKey,
                ELRO_DEVICE_REMOVE, legacyFirmware).withDeviceId(deviceId);
        sendElroMessage(elroMessage, false);
    }

    private void replaceDevice(int deviceId) throws IOException {
        if (getDevice(deviceId) == null) {
            logger.debug("Device {} not known, cannot replace", deviceId);
            return;
        }
        String connectorId = this.connectorId;
        String ctrlKey = this.ctrlKey;
        logger.debug("Replace device {} in hub", deviceId);
        ElroConnectsMessage elroMessage = new ElroConnectsMessage(msgIdIncrement(), connectorId, ctrlKey,
                ELRO_DEVICE_REPLACE, legacyFirmware).withDeviceId(deviceId);
        sendElroMessage(elroMessage, false);
    }

    /**
     * Send request to receive all device names.
     *
     * @throws IOException
     */
    private void getDeviceNames() throws IOException {
        String connectorId = this.connectorId;
        String ctrlKey = this.ctrlKey;
        logger.debug("Get device names");
        ElroConnectsMessage elroMessage = new ElroConnectsMessage(msgIdIncrement(), connectorId, ctrlKey,
                ELRO_GET_DEVICE_NAME).withDeviceId(0);
        sendElroMessage(elroMessage, true);
    }

    /**
     * Send request to receive all device statuses.
     *
     * @throws IOException
     */
    private void getDeviceStatuses() throws IOException {
        String connectorId = this.connectorId;
        String ctrlKey = this.ctrlKey;
        logger.debug("Get all equipment status");
        ElroConnectsMessage elroMessage = new ElroConnectsMessage(msgIdIncrement(), connectorId, ctrlKey,
                ELRO_GET_DEVICE_STATUSES);
        sendElroMessage(elroMessage, true);
    }

    /**
     * Send request to sync all devices statuses.
     *
     * @throws IOException
     */
    private void syncDevices() throws IOException {
        String connectorId = this.connectorId;
        String ctrlKey = this.ctrlKey;
        logger.debug("Sync device status");
        ElroConnectsMessage elroMessage = new ElroConnectsMessage(msgIdIncrement(), connectorId, ctrlKey,
                ELRO_SYNC_DEVICES).withDeviceStatus(SYNC_COMMAND);
        sendElroMessage(elroMessage, true);
    }

    /**
     * Send request to get the currently selected scene.
     *
     * @throws IOException
     */
    private void getCurrentScene() throws IOException {
        String connectorId = this.connectorId;
        String ctrlKey = this.ctrlKey;
        logger.debug("Get current scene");
        ElroConnectsMessage elroMessage = new ElroConnectsMessage(msgIdIncrement(), connectorId, ctrlKey,
                ELRO_GET_SCENE);
        sendElroMessage(elroMessage, true);
    }

    /**
     * Send message to set the current scene.
     *
     * @throws IOException
     */
    private void selectScene(int scene) throws IOException {
        String connectorId = this.connectorId;
        String ctrlKey = this.ctrlKey;
        logger.debug("Select scene {}", scene);
        ElroConnectsMessage elroMessage = new ElroConnectsMessage(msgIdIncrement(), connectorId, ctrlKey,
                ELRO_SELECT_SCENE, legacyFirmware).withSceneType(scene);
        sendElroMessage(elroMessage, false);
    }

    /**
     * Send request to sync all scenes.
     *
     * @throws IOException
     */
    private void syncScenes() throws IOException {
        String connectorId = this.connectorId;
        String ctrlKey = this.ctrlKey;
        logger.debug("Sync scenes");
        ElroConnectsMessage elroMessage = new ElroConnectsMessage(msgIdIncrement(), connectorId, ctrlKey,
                ELRO_SYNC_SCENES, legacyFirmware).withSceneGroup(0).withSceneContent(SYNC_COMMAND)
                .withAnswerContent(SYNC_COMMAND);
        sendElroMessage(elroMessage, true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Channel {}, command {}, type {}", channelUID, command, command.getClass());
        try {
            if (SCENE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(SCENE, new StringType(String.valueOf(currentScene)));
                } else if (command instanceof StringType stringCommand) {
                    try {
                        selectScene(Integer.valueOf(stringCommand.toString()));
                    } catch (NumberFormatException nfe) {
                        logger.debug("Cannot interpret scene command {}", command);
                    }
                }
            }
        } catch (IOException e) {
            String msg = String.format("@text/offline.communication-error [ \"%s\" ]", e.getMessage());
            restartCommunication(msg);
        }
    }

    /**
     * We do not get scene delete messages, therefore call this method before requesting list of scenes to clear list of
     * scenes.
     */
    private void resetScenes() {
        scenes.clear();
        scenes.putAll(DEFAULT_SCENES);

        updateSceneOptions();
    }

    /**
     * Update state option list for scene selection channel.
     */
    private void updateSceneOptions() {
        // update the command scene command options
        List<StateOption> stateOptionList = new ArrayList<>();
        scenes.forEach((id, scene) -> {
            StateOption option = new StateOption(Integer.toString(id), scene);
            stateOptionList.add(option);
        });
        logger.trace("Scenes: {}", stateOptionList);

        Channel channel = thing.getChannel(SCENE);
        if (channel != null) {
            ChannelUID channelUID = channel.getUID();
            StateDescription stateDescription = StateDescriptionFragmentBuilder.create().withReadOnly(false)
                    .withOptions(stateOptionList).build().toStateDescription();
            stateDescriptionProvider.setDescription(channelUID, stateDescription);
        }
    }

    /**
     * Messages need to be sent with consecutive id's. Increment the msgId field and rotate at max unsigned short.
     *
     * @return new message id
     */
    private int msgIdIncrement() {
        return Short.toUnsignedInt(msgId++);
    }

    /**
     * Set the {@link ElroConnectsDeviceHandler} for the device with deviceId, should be called from the thing handler
     * when initializing the thing.
     *
     * @param deviceId
     * @param handler
     */
    public void setDeviceHandler(int deviceId, ElroConnectsDeviceHandler handler) {
        deviceHandlers.put(deviceId, handler);
    }

    /**
     * Unset the {@link ElroConnectsDeviceHandler} for the device with deviceId, should be called from the thing handler
     * when disposing the thing.
     *
     * @param deviceId
     * @param handler
     */
    public void unsetDeviceHandler(int deviceId, ElroConnectsDeviceHandler handler) {
        deviceHandlers.remove(deviceId, handler);
    }

    public @Nullable ElroConnectsDeviceHandler getDeviceHandler(int deviceId) {
        return deviceHandlers.get(deviceId);
    }

    public String getConnectorId() {
        return connectorId;
    }

    public @Nullable ElroConnectsDevice getDevice(int deviceId) {
        return devices.get(deviceId);
    }

    /**
     * Get full list of devices connected to the K1 hub. This can be used by the {@link ElroConnectsDiscoveryService} to
     * scan for devices connected to the K1 hub.
     *
     * @return devices
     */
    public Map<Integer, ElroConnectsDevice> getDevices() {
        return devices;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(ElroConnectsDiscoveryService.class);
    }

    public Map<Integer, String> listDevicesFromConsole() {
        return devices.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getDeviceName()));
    }

    public void refreshFromConsole() {
        try {
            keepAlive();
            getDeviceStatuses();
            getDeviceNames();
        } catch (IOException e) {
            String msg = String.format("@text/offline.communication-error [ \"%s\" ]", e.getMessage());
            restartCommunication(msg);
        }
    }

    public void joinDeviceFromConsole() {
        try {
            joinDevice();
        } catch (IOException e) {
            String msg = String.format("@text/offline.communication-error [ \"%s\" ]", e.getMessage());
            restartCommunication(msg);
        }
    }

    public void cancelJoinDeviceFromConsole() {
        try {
            cancelJoinDevice();
        } catch (IOException e) {
            String msg = String.format("@text/offline.communication-error [ \"%s\" ]", e.getMessage());
            restartCommunication(msg);
        }
    }

    public boolean renameDeviceFromConsole(int deviceId, String deviceName) {
        if (getDevice(deviceId) == null) {
            return false;
        }
        try {
            renameDevice(deviceId, deviceName);
        } catch (IOException e) {
            String msg = String.format("@text/offline.communication-error [ \"%s\" ]", e.getMessage());
            restartCommunication(msg);
        }
        return true;
    }

    public boolean removeDeviceFromConsole(int deviceId) {
        if (getDevice(deviceId) == null) {
            return false;
        }
        try {
            removeDevice(deviceId);
        } catch (IOException e) {
            String msg = String.format("@text/offline.communication-error [ \"%s\" ]", e.getMessage());
            restartCommunication(msg);
        }
        return true;
    }

    public boolean replaceDeviceFromConsole(int deviceId) {
        if (getDevice(deviceId) == null) {
            return false;
        }
        try {
            replaceDevice(deviceId);
        } catch (IOException e) {
            String msg = String.format("@text/offline.communication-error [ \"%s\" ]", e.getMessage());
            restartCommunication(msg);
        }
        return true;
    }

    public void setDiscoveryService(ElroConnectsDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }
}
