/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.sinope.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sinope.SinopeBindingConstants;
import org.openhab.binding.sinope.internal.SinopeConfigStatusMessage;
import org.openhab.binding.sinope.internal.config.SinopeConfig;
import org.openhab.binding.sinope.internal.core.SinopeApiLoginAnswer;
import org.openhab.binding.sinope.internal.core.SinopeApiLoginRequest;
import org.openhab.binding.sinope.internal.core.SinopeAuthenticationKeyAnswer;
import org.openhab.binding.sinope.internal.core.SinopeAuthenticationKeyRequest;
import org.openhab.binding.sinope.internal.core.SinopeDeviceReportAnswer;
import org.openhab.binding.sinope.internal.core.SinopePingRequest;
import org.openhab.binding.sinope.internal.core.base.SinopeAnswer;
import org.openhab.binding.sinope.internal.core.base.SinopeDataAnswer;
import org.openhab.binding.sinope.internal.core.base.SinopeDataRequest;
import org.openhab.binding.sinope.internal.core.base.SinopeRequest;
import org.openhab.binding.sinope.internal.discovery.SinopeThingsDiscoveryService;
import org.openhab.binding.sinope.internal.util.ByteUtil;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ConfigStatusBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SinopeGatewayHandler} is responsible for handling commands for the Sinopé Gateway.
 *
 * @author Pascal Larin - Initial contribution
 */
@NonNullByDefault
public class SinopeGatewayHandler extends ConfigStatusBridgeHandler {

    private static final int TIMEOUT = 20000;
    private static final int SEARCH_READ_TIMEOUT = 2000;
    private static final int FIRST_POLL_INTERVAL = 1; // In seconds
    private static final int DEFAULT_REFRESH_INTERVAL = 60; // In seconds
    private static final int PAIRING_RETRY_DELAY_SECONDS = 2;
    private static final int PAIRING_ERROR_RETRY_DELAY_SECONDS = 10;
    private static final int EXPECTED_API_KEY_LENGTH = 8;

    private final Logger logger = LoggerFactory.getLogger(SinopeGatewayHandler.class);
    private @Nullable ScheduledFuture<?> pollFuture;
    private @Nullable ScheduledFuture<?> connectFuture;
    private @Nullable ScheduledFuture<?> pairingFuture;
    private @Nullable ScheduledFuture<?> pollSearch;
    private long refreshInterval = DEFAULT_REFRESH_INTERVAL; // In seconds
    private final List<SinopeThermostatHandler> thermostatHandlers = new CopyOnWriteArrayList<>();
    private int seq = 1;
    private @Nullable Socket clientSocket;
    private final AtomicReference<@Nullable Socket> searchSocket = new AtomicReference<>();
    private final AtomicReference<@Nullable Socket> pairingSocket = new AtomicReference<>();
    private volatile boolean searching;
    private volatile boolean pairing;
    private volatile boolean disposed;
    private volatile @Nullable String pairedApiKey;

    public SinopeGatewayHandler(final Bridge bridge) {
        super(bridge);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(SinopeThingsDiscoveryService.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Sinope Gateway");
        disposed = false;
        pairedApiKey = null;

        SinopeConfig config = getConfigAs(SinopeConfig.class);
        Integer refresh = config.refresh;
        refreshInterval = (refresh != null && refresh > 0) ? refresh : DEFAULT_REFRESH_INTERVAL;

        String hostname = config.hostname;
        Integer port = config.port;
        String gatewayId = config.gatewayId;
        String apiKey = config.apiKey;

        if (hostname == null || hostname.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Gateway hostname must be set");
            return;
        }
        if (port == null || port <= 0) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Gateway port must be set");
            return;
        }
        byte[] gatewayIdBytes = gatewayId != null ? SinopeConfig.convert(gatewayId) : null;
        if (gatewayIdBytes == null) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Gateway ID must be set");
            return;
        }

        if (apiKey == null || apiKey.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_PENDING,
                    "Press the WEB button on the Sinopé Gateway to obtain the API key.");
            startPairing(hostname, port, gatewayIdBytes);
            return;
        }

        if (SinopeConfig.convert(apiKey) == null) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "API Key is invalid");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        connectFuture = scheduler.schedule(this::connectAndStartPolling, 0, TimeUnit.SECONDS);
    }

    private void connectAndStartPolling() {
        if (disposed || pairing || searching) {
            return;
        }
        try {
            if (connectToBridge()) {
                if (!disposed) {
                    schedulePoll();
                    updateStatus(ThingStatus.ONLINE);
                }
            } else if (!disposed) {
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR,
                        "Gateway rejected login. Check Gateway ID and API Key.");
            }
        } catch (IOException e) {
            if (!disposed) {
                logger.debug("Unable to connect to Sinopé Gateway: {}", e.getMessage());
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
                        "Can't connect to gateway. Please make sure that another instance is not connected.");
                schedulePoll();
            }
        }
    }

    @Override
    public void dispose() {
        disposed = true;
        ScheduledFuture<?> localConnectFuture = connectFuture;
        if (localConnectFuture != null) {
            localConnectFuture.cancel(true);
            connectFuture = null;
        }
        stopPairing();
        stopSearch();
        stopPoll();
        closeClientSocket();
        pairedApiKey = null;
        super.dispose();
    }

    private void startPairing(String hostname, int port, byte[] gatewayId) {
        stopPoll();
        closeClientSocket();
        pairing = true;
        schedulePairingAttempt(hostname, port, gatewayId, 0);
    }

    private synchronized void schedulePairingAttempt(String hostname, int port, byte[] gatewayId, long delaySeconds) {
        if (!pairing || disposed) {
            return;
        }
        ScheduledFuture<?> localFuture = pairingFuture;
        if (localFuture != null && !localFuture.isDone()) {
            localFuture.cancel(false);
        }
        pairingFuture = scheduler.schedule(() -> attemptPairing(hostname, port, gatewayId), delaySeconds,
                TimeUnit.SECONDS);
    }

    private void attemptPairing(String hostname, int port, byte[] gatewayId) {
        if (!pairing || disposed) {
            return;
        }
        Socket socket = null;
        try {
            socket = new Socket(hostname, port);
            socket.setSoTimeout(TIMEOUT);
            pairingSocket.set(socket);

            // Gateway requires an initial ping before requesting an authentication key
            sendAndReceive(socket, new SinopePingRequest());

            logger.debug("Requesting authentication key from Sinopé Gateway - press WEB button");
            SinopeAuthenticationKeyAnswer answer = (SinopeAuthenticationKeyAnswer) sendAndReceive(socket,
                    new SinopeAuthenticationKeyRequest(gatewayId));

            if (!pairing || disposed) {
                return;
            }

            byte[] apiKeyBytes = answer.getApiKey();
            if (answer.getStatus() != 0xFF && apiKeyBytes.length == EXPECTED_API_KEY_LENGTH
                    && !isAllZero(apiKeyBytes)) {
                String formattedApiKey = ByteUtil.toString(apiKeyBytes).trim();
                pairedApiKey = formattedApiKey;
                pairing = false;
                pairingSocket.compareAndSet(socket, null);
                closeQuietly(socket);
                socket = null;

                updateBridgeApiKey(formattedApiKey);
                logger.info("Successfully paired with Sinopé Gateway and obtained API key.");
                connectAndStartPolling();
                return;
            }

            logger.debug("Sinopé Gateway pairing not completed yet (status=0x{}, keyLen={}), retrying",
                    String.format("%02X", answer.getStatus()), apiKeyBytes.length);
            updateStatus(ThingStatusDetail.CONFIGURATION_PENDING,
                    "Press the WEB button on the Sinopé Gateway to obtain the API key.");
            schedulePairingAttempt(hostname, port, gatewayId, PAIRING_RETRY_DELAY_SECONDS);
        } catch (SocketTimeoutException e) {
            if (pairing && !disposed) {
                logger.debug("Timed out waiting for WEB button press on Sinopé Gateway; retrying");
                updateStatus(ThingStatusDetail.CONFIGURATION_PENDING,
                        "Press the WEB button on the Sinopé Gateway to obtain the API key.");
                schedulePairingAttempt(hostname, port, gatewayId, PAIRING_RETRY_DELAY_SECONDS);
            }
        } catch (IOException e) {
            if (pairing && !disposed) {
                logger.debug("Communication error during Sinopé Gateway pairing: {}", e.getMessage());
                updateStatus(ThingStatusDetail.CONFIGURATION_PENDING,
                        "Cannot reach Sinopé Gateway to pair. Press the WEB button once reachable.");
                schedulePairingAttempt(hostname, port, gatewayId, PAIRING_ERROR_RETRY_DELAY_SECONDS);
            }
        } finally {
            pairingSocket.compareAndSet(socket, null);
            closeQuietly(socket);
        }
    }

    private void updateBridgeApiKey(String apiKey) {
        Configuration config = editConfiguration();
        config.put(SinopeBindingConstants.CONFIG_PROPERTY_API_KEY, apiKey);
        try {
            updateConfiguration(config);
            logger.debug("Updated configuration parameter '{}'", SinopeBindingConstants.CONFIG_PROPERTY_API_KEY);
        } catch (IllegalStateException e) {
            logger.trace("Configuration update failed.", e);
            logger.warn("Unable to persist Sinopé Gateway API key into read-only Thing configuration.");
        }
    }

    private void stopPairing() {
        pairing = false;
        synchronized (this) {
            ScheduledFuture<?> localFuture = pairingFuture;
            if (localFuture != null) {
                localFuture.cancel(true);
                pairingFuture = null;
            }
        }
        Socket localSocket = pairingSocket.getAndSet(null);
        closeQuietly(localSocket);
    }

    private static boolean isAllZero(byte[] bytes) {
        for (byte b : bytes) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    private static SinopeAnswer sendAndReceive(Socket socket, SinopeRequest command) throws IOException {
        OutputStream outToServer = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        outToServer.write(command.getPayload());
        outToServer.flush();
        return command.getReplyAnswer(inputStream);
    }

    synchronized void schedulePoll() {
        if (searching || pairing || disposed) {
            return;
        }
        ScheduledFuture<?> localPollFuture = pollFuture;
        if (localPollFuture != null) {
            localPollFuture.cancel(false);
        }
        logger.debug("Scheduling poll for {} s out, then every {} s", FIRST_POLL_INTERVAL, refreshInterval);
        pollFuture = scheduler.scheduleWithFixedDelay(() -> poll(), FIRST_POLL_INTERVAL, refreshInterval,
                TimeUnit.SECONDS);
    }

    synchronized void stopPoll() {
        ScheduledFuture<?> localPollFuture = pollFuture;
        if (localPollFuture != null && !localPollFuture.isCancelled()) {
            localPollFuture.cancel(true);
            pollFuture = null;
        }
    }

    private synchronized void poll() {
        if (searching || pairing || disposed) {
            return;
        }
        if (!thermostatHandlers.isEmpty() || getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Polling for state");
            try {
                if (connectToBridge()) {
                    logger.debug("Connected to bridge");
                    for (SinopeThermostatHandler sinopeThermostatHandler : thermostatHandlers) {
                        sinopeThermostatHandler.update();
                    }
                }
            } catch (IOException e) {
                setCommunicationError(true);
                logger.debug("Polling issue", e);
            }
        } else {
            logger.debug("nothing to poll");
        }
    }

    private @Nullable String getEffectiveApiKey(SinopeConfig config) {
        String configKey = config.apiKey;
        if (configKey != null && !configKey.isBlank()) {
            return configKey;
        }
        return pairedApiKey;
    }

    synchronized boolean connectToBridge() throws UnknownHostException, IOException {
        if (searching || pairing || disposed) {
            return false;
        }
        SinopeConfig config = getConfigAs(SinopeConfig.class);
        Socket localSocket = this.clientSocket;
        if (localSocket == null || !localSocket.isConnected() || localSocket.isClosed()) {
            String hostname = config.hostname;
            Integer port = config.port;
            byte[] gatewayId = config.gatewayId != null ? SinopeConfig.convert(config.gatewayId) : null;
            String effectiveApiKey = getEffectiveApiKey(config);
            byte[] apiKey = effectiveApiKey != null ? SinopeConfig.convert(effectiveApiKey) : null;
            if (hostname == null || port == null || gatewayId == null || apiKey == null) {
                return false;
            }
            Socket newSocket = new Socket(hostname, port);
            try {
                newSocket.setSoTimeout(TIMEOUT);
                SinopeApiLoginRequest loginRequest = new SinopeApiLoginRequest(gatewayId, apiKey);
                SinopeApiLoginAnswer loginAnswer = (SinopeApiLoginAnswer) sendAndReceive(newSocket, loginRequest);
                if (loginAnswer.getStatus() == 0) {
                    this.clientSocket = newSocket;
                    updateStatus(ThingStatus.ONLINE);
                    return true;
                }
                closeQuietly(newSocket);
                return false;
            } catch (IOException e) {
                closeQuietly(newSocket);
                throw e;
            }
        }
        return true;
    }

    private synchronized void closeClientSocket() {
        Socket localSocket = this.clientSocket;
        this.clientSocket = null;
        closeQuietly(localSocket);
    }

    private void closeQuietly(@Nullable Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.debug("Error closing gateway socket: {}", e.getMessage());
            }
        }
    }

    public synchronized byte[] newSeq() {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(seq++).array();
    }

    synchronized SinopeAnswer execute(SinopeRequest command) throws UnknownHostException, IOException {
        if (searching) {
            throw new IOException("Sinopé Gateway is currently in device discovery mode");
        }
        Socket socket = this.getClientSocket();
        return sendAndReceive(socket, command);
    }

    synchronized SinopeAnswer execute(SinopeDataRequest command) throws UnknownHostException, IOException {
        if (searching) {
            throw new IOException("Sinopé Gateway is currently in device discovery mode");
        }
        Socket socket = this.getClientSocket();

        OutputStream outToServer = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        if (logger.isDebugEnabled()) {
            int leftBytes = inputStream.available();
            if (leftBytes > 0) {
                logger.debug("Hum... some leftovers: {} bytes", leftBytes);
            }
        }
        outToServer.write(command.getPayload());
        outToServer.flush();

        SinopeDataAnswer answ = command.getReplyAnswer(inputStream);

        while (answ.getMore() == 0x01) {
            answ = command.getReplyAnswer(inputStream);
        }
        return answ;
    }

    public boolean registerThermostatHandler(SinopeThermostatHandler thermostatHandler) {
        if (!thermostatHandlers.contains(thermostatHandler)) {
            return thermostatHandlers.add(thermostatHandler);
        }
        return false;
    }

    public boolean unregisterThermostatHandler(SinopeThermostatHandler thermostatHandler) {
        return thermostatHandlers.remove(thermostatHandler);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> configStatusMessages = new LinkedList<>();

        SinopeConfig config = getConfigAs(SinopeConfig.class);
        if (config.hostname == null || config.hostname.isBlank()) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(SinopeBindingConstants.CONFIG_PROPERTY_HOST)
                    .withMessageKeySuffix(SinopeConfigStatusMessage.HOST_MISSING.getMessageKey())
                    .withArguments(SinopeBindingConstants.CONFIG_PROPERTY_HOST).build());
        }
        if (config.port == null || config.port <= 0) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(SinopeBindingConstants.CONFIG_PROPERTY_PORT)
                    .withMessageKeySuffix(SinopeConfigStatusMessage.PORT_MISSING.getMessageKey())
                    .withArguments(SinopeBindingConstants.CONFIG_PROPERTY_PORT).build());
        }

        if (config.gatewayId == null || SinopeConfig.convert(config.gatewayId) == null) {
            configStatusMessages
                    .add(ConfigStatusMessage.Builder.error(SinopeBindingConstants.CONFIG_PROPERTY_GATEWAY_ID)
                            .withMessageKeySuffix(SinopeConfigStatusMessage.GATEWAY_ID_INVALID.getMessageKey())
                            .withArguments(SinopeBindingConstants.CONFIG_PROPERTY_GATEWAY_ID).build());
        }
        String apiKey = config.apiKey;
        if (apiKey != null && !apiKey.isBlank() && SinopeConfig.convert(apiKey) == null) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(SinopeBindingConstants.CONFIG_PROPERTY_API_KEY)
                    .withMessageKeySuffix(SinopeConfigStatusMessage.API_KEY_INVALID.getMessageKey())
                    .withArguments(SinopeBindingConstants.CONFIG_PROPERTY_API_KEY).build());
        }

        return configStatusMessages;
    }

    public void startSearch(final Consumer<byte[]> deviceDiscoveredCallback) throws UnknownHostException, IOException {
        if (disposed || pairing) {
            logger.debug("Cannot start device discovery while gateway is pairing or disposed");
            return;
        }
        SinopeConfig config = getConfigAs(SinopeConfig.class);
        String effectiveApiKey = getEffectiveApiKey(config);
        if (config.hostname == null || config.port == null || config.gatewayId == null || effectiveApiKey == null
                || SinopeConfig.convert(config.gatewayId) == null || SinopeConfig.convert(effectiveApiKey) == null) {
            logger.debug("Cannot start device discovery: gateway configuration is incomplete");
            return;
        }

        stopPoll();
        synchronized (this) {
            this.searching = true;
            closeClientSocket();
            ScheduledFuture<?> existingSearch = pollSearch;
            if (existingSearch != null && !existingSearch.isDone()) {
                existingSearch.cancel(true);
            }
            pollSearch = scheduler.schedule(() -> search(deviceDiscoveredCallback), FIRST_POLL_INTERVAL,
                    TimeUnit.SECONDS);
        }
    }

    private void search(final Consumer<byte[]> deviceDiscoveredCallback) {
        SinopeConfig config = getConfigAs(SinopeConfig.class);
        String hostname = config.hostname;
        Integer port = config.port;
        byte[] gatewayId = config.gatewayId != null ? SinopeConfig.convert(config.gatewayId) : null;
        String effectiveApiKey = getEffectiveApiKey(config);
        byte[] apiKey = effectiveApiKey != null ? SinopeConfig.convert(effectiveApiKey) : null;

        if (hostname == null || port == null || gatewayId == null || apiKey == null) {
            searching = false;
            schedulePoll();
            return;
        }

        Socket socket = null;
        try {
            socket = new Socket(hostname, port);
            socket.setSoTimeout(TIMEOUT);
            searchSocket.set(socket);

            sendAndReceive(socket, new SinopePingRequest());

            SinopeApiLoginAnswer loginAnswer = (SinopeApiLoginAnswer) sendAndReceive(socket,
                    new SinopeApiLoginRequest(gatewayId, apiKey));
            if (loginAnswer.getStatus() != 0) {
                logger.debug("Gateway login failed during device discovery (status=0x{})",
                        String.format("%02X", loginAnswer.getStatus()));
                return;
            }

            logger.debug("Successful login for device discovery - press both buttons on device");
            socket.setSoTimeout(SEARCH_READ_TIMEOUT);

            while (searching && !disposed && socket.isConnected() && !socket.isClosed()) {
                try {
                    SinopeDeviceReportAnswer answ = new SinopeDeviceReportAnswer(socket.getInputStream());
                    logger.debug("Got report answer: {}", answ);
                    byte[] deviceId = answ.getDeviceId();
                    if (deviceId.length > 0) {
                        logger.debug("Discovered Sinopé device id: {}", ByteUtil.toString(deviceId).trim());
                        deviceDiscoveredCallback.accept(deviceId);
                    }
                } catch (SocketTimeoutException e) {
                    // Expected while waiting for the user to press buttons on a device
                }
            }
        } catch (UnknownHostException e) {
            logger.warn("Unexpected error when searching for new devices", e);
        } catch (IOException e) {
            logger.debug("Network connection closed during device discovery: {}", e.getMessage());
        } finally {
            searchSocket.compareAndSet(socket, null);
            closeQuietly(socket);
            searching = false;
            if (!disposed) {
                schedulePoll();
            }
        }
    }

    public void stopSearch() {
        this.searching = false;
        synchronized (this) {
            ScheduledFuture<?> localPollSearch = this.pollSearch;
            if (localPollSearch != null && !localPollSearch.isCancelled()) {
                localPollSearch.cancel(true);
                this.pollSearch = null;
            }
        }
        Socket localSearchSocket = this.searchSocket.getAndSet(null);
        closeQuietly(localSearchSocket);

        if (!disposed) {
            schedulePoll();
        }
    }

    public synchronized Socket getClientSocket() throws UnknownHostException, IOException {
        if (connectToBridge()) {
            Socket localSocket = clientSocket;
            if (localSocket != null) {
                return localSocket;
            }
        }
        throw new IOException("Could not create a socket to the gateway. Check host/ip/gateway Id");
    }

    public void setCommunicationError(boolean hasError) {
        if (disposed) {
            return;
        }
        if (hasError) {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR);
            closeClientSocket();
        } else {
            updateStatus(ThingStatus.ONLINE);
            schedulePoll();
        }
    }
}
