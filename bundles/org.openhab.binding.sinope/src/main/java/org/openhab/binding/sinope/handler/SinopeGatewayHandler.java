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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
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
import org.openhab.core.thing.Thing;
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

    enum ConnectionResult {
        CONNECTED,
        BUSY,
        AUTH_FAILED,
        INACTIVE
    }

    private record ClientConnection(long generation, long scanGen, Socket socket) {
    }

    private static final int TIMEOUT = 20000;
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final int FIRST_POLL_INTERVAL = 1; // In seconds
    private static final int DEFAULT_REFRESH_INTERVAL = 60; // In seconds
    private static final int PAIRING_RETRY_DELAY_SECONDS = 2;
    private static final int PAIRING_ERROR_RETRY_DELAY_SECONDS = 10;
    private static final int EXPECTED_API_KEY_LENGTH = 8;

    private final Logger logger = LoggerFactory.getLogger(SinopeGatewayHandler.class);
    private final Object configLock = new Object();
    private final Object schedulerLock = new Object();
    private final Object socketOwnershipLock = new Object();
    private final Object connectLock = new Object();
    private final Object ioLock = new Object();
    private final AtomicLong lifecycleGeneration = new AtomicLong();
    private final AtomicLong searchGeneration = new AtomicLong();
    private final AtomicReference<@Nullable Socket> clientSocketRef = new AtomicReference<>();
    private final AtomicReference<@Nullable Socket> connectingSocket = new AtomicReference<>();
    private final AtomicReference<@Nullable Socket> searchSocket = new AtomicReference<>();
    private final AtomicReference<@Nullable Socket> pairingSocket = new AtomicReference<>();
    private final List<SinopeThermostatHandler> thermostatHandlers = new CopyOnWriteArrayList<>();

    private @Nullable ScheduledFuture<?> pollFuture;
    private @Nullable ScheduledFuture<?> connectFuture;
    private @Nullable ScheduledFuture<?> pairingFuture;
    private @Nullable Future<?> pairingTaskFuture;
    private @Nullable ExecutorService pairingExecutor;
    private @Nullable ScheduledFuture<?> pollSearch;
    private long refreshInterval = DEFAULT_REFRESH_INTERVAL; // In seconds
    private int seq = 1;
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

    private static boolean isValidPort(@Nullable Integer port) {
        return port != null && port >= MIN_PORT && port <= MAX_PORT;
    }

    private boolean isActiveGeneration(long generation) {
        return !disposed && lifecycleGeneration.get() == generation;
    }

    private boolean isCurrentSearch(long lifecycleGen, long scanGen) {
        return isActiveGeneration(lifecycleGen) && searchGeneration.get() == scanGen;
    }

    boolean isBusy() {
        return searching || pairing;
    }

    @Override
    public void thingUpdated(Thing thing) {
        synchronized (configLock) {
            super.thingUpdated(thing);
        }
    }

    @Override
    public void initialize() {
        synchronized (configLock) {
            logger.debug("Initializing Sinope Gateway");
            Bridge currentThing = getThing();
            long generation;
            synchronized (socketOwnershipLock) {
                generation = lifecycleGeneration.incrementAndGet();
                searchGeneration.incrementAndGet();
                disposed = false;
                pairing = false;
                searching = false;
                pairedApiKey = null;
            }

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
            if (port == null || !isValidPort(port)) {
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Gateway port must be between 1 and 65535");
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
                startPairing(generation, currentThing, hostname, port, gatewayIdBytes);
                return;
            }

            if (SinopeConfig.convert(apiKey) == null) {
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "API Key is invalid");
                return;
            }

            updateStatus(ThingStatus.UNKNOWN);
            scheduleConnect(generation);
        }
    }

    private void scheduleConnect(long generation) {
        synchronized (schedulerLock) {
            if (isActiveGeneration(generation)) {
                cancelFuture(connectFuture, false);
                connectFuture = scheduler.schedule(() -> connectAndStartPolling(generation), 0, TimeUnit.SECONDS);
            }
        }
    }

    private void connectAndStartPolling(long generation) {
        long scanGen;
        synchronized (socketOwnershipLock) {
            if (!isActiveGeneration(generation) || pairing || searching) {
                return;
            }
            scanGen = searchGeneration.get();
        }
        try {
            ConnectionResult result = connectToBridge(generation, scanGen);
            if (result == ConnectionResult.CONNECTED) {
                if (isActiveGeneration(generation) && !pairing && !searching && searchGeneration.get() == scanGen) {
                    schedulePoll(generation);
                    updateStatus(ThingStatus.ONLINE);
                }
            } else if (result == ConnectionResult.AUTH_FAILED && isActiveGeneration(generation) && !pairing
                    && !searching && searchGeneration.get() == scanGen) {
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR,
                        "Gateway rejected login. Check Gateway ID and API Key.");
            }
        } catch (IOException e) {
            if (isActiveGeneration(generation) && !pairing && !searching && searchGeneration.get() == scanGen) {
                logger.debug("Unable to connect to Sinopé Gateway: {}", e.getMessage());
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
                        "Can't connect to gateway. Please make sure that another instance is not connected.");
                schedulePoll(generation);
            }
        }
    }

    @Override
    public void dispose() {
        synchronized (configLock) {
            Socket oldPairing;
            Socket oldSearch;
            Socket oldConnecting;
            Socket oldClient;
            synchronized (socketOwnershipLock) {
                disposed = true;
                lifecycleGeneration.incrementAndGet();
                searchGeneration.incrementAndGet();
                pairing = false;
                searching = false;
                oldPairing = pairingSocket.getAndSet(null);
                oldSearch = searchSocket.getAndSet(null);
                oldConnecting = connectingSocket.getAndSet(null);
                oldClient = clientSocketRef.getAndSet(null);
            }

            synchronized (schedulerLock) {
                cancelFuture(connectFuture, true);
                connectFuture = null;
                cancelFuture(pollSearch, true);
                pollSearch = null;
                cancelFuture(pollFuture, true);
                pollFuture = null;
            }
            shutdownPairingExecutor(true);

            closeQuietly(oldPairing);
            closeQuietly(oldSearch);
            closeQuietly(oldConnecting);
            closeQuietly(oldClient);
            pairedApiKey = null;
            super.dispose();
        }
    }

    private static void cancelFuture(@Nullable Future<?> future, boolean mayInterrupt) {
        if (future != null && !future.isCancelled()) {
            future.cancel(mayInterrupt);
        }
    }

    private ExecutorService ensurePairingExecutor() {
        ExecutorService executor = pairingExecutor;
        if (executor == null || executor.isShutdown()) {
            String threadName = "OH-binding-" + getThing().getUID() + "-pairing";
            executor = Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, threadName);
                thread.setDaemon(true);
                return thread;
            });
            pairingExecutor = executor;
        }
        return executor;
    }

    private void shutdownPairingExecutor(boolean interruptRunning) {
        synchronized (schedulerLock) {
            cancelFuture(pairingFuture, interruptRunning);
            pairingFuture = null;
            if (interruptRunning) {
                cancelFuture(pairingTaskFuture, true);
            }
            pairingTaskFuture = null;
            ExecutorService executor = pairingExecutor;
            pairingExecutor = null;
            if (executor != null) {
                if (interruptRunning) {
                    executor.shutdownNow();
                } else {
                    executor.shutdown();
                }
            }
        }
    }

    private @Nullable Socket openRegisteredSocket(String hostname, int port, AtomicReference<@Nullable Socket> slot,
            BooleanSupplier stillValid) throws IOException {
        if (!isValidPort(port)) {
            throw new IOException("Invalid gateway port: " + port);
        }
        Socket socket = new Socket();
        Socket previous;
        synchronized (socketOwnershipLock) {
            if (!stillValid.getAsBoolean()) {
                closeQuietly(socket);
                return null;
            }
            previous = slot.getAndSet(socket);
        }
        closeQuietly(previous);
        try {
            socket.connect(new InetSocketAddress(hostname, port), TIMEOUT);
            socket.setSoTimeout(TIMEOUT);
            return socket;
        } catch (IOException e) {
            slot.compareAndSet(socket, null);
            closeQuietly(socket);
            throw e;
        }
    }

    private void startPairing(long generation, Bridge expectedThing, String hostname, int port, byte[] gatewayId) {
        stopPoll();
        Socket oldConnecting;
        Socket oldClient;
        synchronized (socketOwnershipLock) {
            if (!isActiveGeneration(generation)) {
                return;
            }
            pairing = true;
            oldConnecting = connectingSocket.getAndSet(null);
            oldClient = clientSocketRef.getAndSet(null);
        }
        closeQuietly(oldConnecting);
        closeQuietly(oldClient);
        schedulePairingAttempt(generation, expectedThing, hostname, port, gatewayId, 0);
    }

    private void schedulePairingAttempt(long generation, Bridge expectedThing, String hostname, int port,
            byte[] gatewayId, long delaySeconds) {
        synchronized (schedulerLock) {
            if (!pairing || !isActiveGeneration(generation)) {
                return;
            }
            cancelFuture(pairingFuture, false);
            ensurePairingExecutor();
            pairingFuture = scheduler.schedule(
                    () -> submitPairingAttempt(generation, expectedThing, hostname, port, gatewayId), delaySeconds,
                    TimeUnit.SECONDS);
        }
    }

    private void submitPairingAttempt(long generation, Bridge expectedThing, String hostname, int port,
            byte[] gatewayId) {
        synchronized (schedulerLock) {
            if (!pairing || !isActiveGeneration(generation)) {
                return;
            }
            ExecutorService executor = pairingExecutor;
            if (executor == null || executor.isShutdown()) {
                return;
            }
            Future<?> currentTask = pairingTaskFuture;
            if (currentTask != null && !currentTask.isDone()) {
                return;
            }
            try {
                pairingTaskFuture = executor
                        .submit(() -> attemptPairing(generation, expectedThing, hostname, port, gatewayId));
            } catch (RejectedExecutionException e) {
                logger.debug("Pairing task rejected because pairing executor was shut down");
            }
        }
    }

    private void attemptPairing(long generation, Bridge expectedThing, String hostname, int port, byte[] gatewayId) {
        if (!pairing || !isActiveGeneration(generation)) {
            return;
        }
        Socket socket = null;
        try {
            socket = openRegisteredSocket(hostname, port, pairingSocket,
                    () -> pairing && isActiveGeneration(generation));
            if (socket == null) {
                return;
            }

            // Gateway requires an initial ping before requesting an authentication key
            sendAndReceive(socket, new SinopePingRequest());

            logger.debug("Requesting authentication key from Sinopé Gateway - press WEB button");
            SinopeAuthenticationKeyAnswer answer = (SinopeAuthenticationKeyAnswer) sendAndReceive(socket,
                    new SinopeAuthenticationKeyRequest(gatewayId));

            byte[] apiKeyBytes = answer.getApiKey();
            if (answer.getStatus() != 0xFF && apiKeyBytes.length == EXPECTED_API_KEY_LENGTH
                    && !isAllZero(apiKeyBytes)) {
                String formattedApiKey = ByteUtil.toString(apiKeyBytes).trim();
                Socket socketToClose;
                synchronized (socketOwnershipLock) {
                    if (!pairing || !isActiveGeneration(generation)) {
                        return;
                    }
                    pairedApiKey = formattedApiKey;
                    pairing = false;
                    pairingSocket.compareAndSet(socket, null);
                    socketToClose = socket;
                    socket = null;
                }
                closeQuietly(socketToClose);

                updateBridgeApiKey(generation, expectedThing, hostname, port, gatewayId, formattedApiKey);
                logger.info("Successfully paired with Sinopé Gateway and obtained API key.");
                shutdownPairingExecutor(false);
                scheduleConnect(generation);
                return;
            }

            logger.debug("Sinopé Gateway pairing not completed yet (status=0x{}, keyLen={}), retrying",
                    String.format("%02X", answer.getStatus()), apiKeyBytes.length);
            if (pairing && isActiveGeneration(generation)) {
                updateStatus(ThingStatusDetail.CONFIGURATION_PENDING,
                        "Press the WEB button on the Sinopé Gateway to obtain the API key.");
                schedulePairingAttempt(generation, expectedThing, hostname, port, gatewayId,
                        PAIRING_RETRY_DELAY_SECONDS);
            }
        } catch (SocketTimeoutException e) {
            if (pairing && isActiveGeneration(generation)) {
                logger.debug("Timed out waiting for WEB button press on Sinopé Gateway; retrying");
                updateStatus(ThingStatusDetail.CONFIGURATION_PENDING,
                        "Press the WEB button on the Sinopé Gateway to obtain the API key.");
                schedulePairingAttempt(generation, expectedThing, hostname, port, gatewayId,
                        PAIRING_RETRY_DELAY_SECONDS);
            }
        } catch (IOException e) {
            if (pairing && isActiveGeneration(generation)) {
                logger.debug("Communication error during Sinopé Gateway pairing: {}", e.getMessage());
                updateStatus(ThingStatusDetail.CONFIGURATION_PENDING,
                        "Cannot reach Sinopé Gateway to pair. Press the WEB button once reachable.");
                schedulePairingAttempt(generation, expectedThing, hostname, port, gatewayId,
                        PAIRING_ERROR_RETRY_DELAY_SECONDS);
            }
        } finally {
            pairingSocket.compareAndSet(socket, null);
            closeQuietly(socket);
        }
    }

    private void updateBridgeApiKey(long generation, Bridge expectedThing, String hostname, int port, byte[] gatewayId,
            String apiKey) {
        synchronized (configLock) {
            Bridge currentThing = getThing();
            if (!isActiveGeneration(generation) || !expectedThing.getUID().equals(currentThing.getUID())
                    || !expectedThing.getConfiguration().equals(currentThing.getConfiguration())) {
                logger.debug("Skipping API key persistence because gateway lifecycle changed");
                return;
            }
            SinopeConfig currentConfig = getConfigAs(SinopeConfig.class);
            byte[] currentGatewayId = currentConfig.gatewayId != null ? SinopeConfig.convert(currentConfig.gatewayId)
                    : null;
            if (!Objects.equals(hostname, currentConfig.hostname) || !Objects.equals(port, currentConfig.port)
                    || currentGatewayId == null || !Arrays.equals(gatewayId, currentGatewayId)) {
                logger.debug("Skipping API key persistence because gateway configuration changed");
                return;
            }
            String existingKey = currentConfig.apiKey;
            if (existingKey != null && !existingKey.isBlank()) {
                return;
            }

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

    void schedulePoll() {
        schedulePoll(lifecycleGeneration.get());
    }

    private void schedulePoll(long generation) {
        synchronized (schedulerLock) {
            if (searching || pairing || !isActiveGeneration(generation)) {
                return;
            }
            cancelFuture(pollFuture, false);
            logger.debug("Scheduling poll for {} s out, then every {} s", FIRST_POLL_INTERVAL, refreshInterval);
            pollFuture = scheduler.scheduleWithFixedDelay(() -> poll(generation), FIRST_POLL_INTERVAL, refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    void stopPoll() {
        synchronized (schedulerLock) {
            cancelFuture(pollFuture, true);
            pollFuture = null;
        }
    }

    private void poll(long generation) {
        long scanGen;
        synchronized (socketOwnershipLock) {
            if (searching || pairing || !isActiveGeneration(generation)) {
                return;
            }
            scanGen = searchGeneration.get();
        }
        if (!thermostatHandlers.isEmpty() || getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Polling for state");
            ConnectionResult result;
            try {
                result = connectToBridge(generation, scanGen);
            } catch (IOException e) {
                handleCommunicationError(generation, scanGen, null);
                if (isActiveGeneration(generation) && !searching && !pairing && searchGeneration.get() == scanGen) {
                    logger.debug("Polling issue", e);
                }
                return;
            }
            if (result == ConnectionResult.CONNECTED) {
                logger.debug("Connected to bridge");
                for (SinopeThermostatHandler sinopeThermostatHandler : thermostatHandlers) {
                    if (!isActiveGeneration(generation) || searching || pairing || searchGeneration.get() != scanGen) {
                        return;
                    }
                    try {
                        sinopeThermostatHandler.update();
                    } catch (IOException e) {
                        if (isActiveGeneration(generation) && !searching && !pairing
                                && searchGeneration.get() == scanGen) {
                            logger.debug("Polling issue", e);
                        }
                        return;
                    }
                }
            } else if (result == ConnectionResult.AUTH_FAILED && isActiveGeneration(generation) && !searching
                    && !pairing && searchGeneration.get() == scanGen) {
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR,
                        "Gateway rejected login. Check Gateway ID and API Key.");
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

    boolean connectToBridge() throws UnknownHostException, IOException {
        return connectToBridgeForCommand() == ConnectionResult.CONNECTED;
    }

    ConnectionResult connectToBridgeForCommand() throws UnknownHostException, IOException {
        long generation;
        long scanGen;
        synchronized (socketOwnershipLock) {
            generation = lifecycleGeneration.get();
            scanGen = searchGeneration.get();
            if (!isActiveGeneration(generation)) {
                return ConnectionResult.INACTIVE;
            }
            if (searching || pairing) {
                return ConnectionResult.BUSY;
            }
        }
        try {
            return connectToBridge(generation, scanGen);
        } catch (IOException e) {
            handleCommunicationError(generation, scanGen, null);
            throw e;
        }
    }

    private ConnectionResult connectToBridge(long generation, long scanGen) throws UnknownHostException, IOException {
        synchronized (connectLock) {
            if (!isActiveGeneration(generation)) {
                return ConnectionResult.INACTIVE;
            }
            if (searching || pairing || searchGeneration.get() != scanGen) {
                return ConnectionResult.BUSY;
            }

            Socket existing = clientSocketRef.get();
            if (existing != null && existing.isConnected() && !existing.isClosed()) {
                return ConnectionResult.CONNECTED;
            }

            SinopeConfig config = getConfigAs(SinopeConfig.class);
            String hostname = config.hostname;
            Integer port = config.port;
            byte[] gatewayId = config.gatewayId != null ? SinopeConfig.convert(config.gatewayId) : null;
            String effectiveApiKey = getEffectiveApiKey(config);
            byte[] apiKey = effectiveApiKey != null ? SinopeConfig.convert(effectiveApiKey) : null;
            if (hostname == null || hostname.isBlank() || port == null || !isValidPort(port) || gatewayId == null
                    || apiKey == null) {
                return ConnectionResult.AUTH_FAILED;
            }

            // Connect and login while holding connectLock (serializing concurrent reconnects) but never
            // holding socketOwnershipLock/schedulerLock so dispose() and startSearch() can cancel promptly.
            Socket newSocket;
            try {
                newSocket = openRegisteredSocket(hostname, port, connectingSocket, () -> !searching && !pairing
                        && isActiveGeneration(generation) && searchGeneration.get() == scanGen);
            } catch (IOException e) {
                if (!isActiveGeneration(generation)) {
                    return ConnectionResult.INACTIVE;
                }
                if (searching || pairing || searchGeneration.get() != scanGen) {
                    return ConnectionResult.BUSY;
                }
                throw e;
            }
            if (newSocket == null) {
                return !isActiveGeneration(generation) ? ConnectionResult.INACTIVE : ConnectionResult.BUSY;
            }
            try {
                SinopeApiLoginRequest loginRequest = new SinopeApiLoginRequest(gatewayId, apiKey);
                SinopeApiLoginAnswer loginAnswer = (SinopeApiLoginAnswer) sendAndReceive(newSocket, loginRequest);
                if (loginAnswer.getStatus() != 0) {
                    if (!isActiveGeneration(generation)) {
                        return ConnectionResult.INACTIVE;
                    }
                    if (searching || pairing || searchGeneration.get() != scanGen) {
                        return ConnectionResult.BUSY;
                    }
                    return ConnectionResult.AUTH_FAILED;
                }

                Socket replaced;
                synchronized (socketOwnershipLock) {
                    if (!isActiveGeneration(generation)) {
                        return ConnectionResult.INACTIVE;
                    }
                    if (searching || pairing || searchGeneration.get() != scanGen) {
                        return ConnectionResult.BUSY;
                    }
                    connectingSocket.compareAndSet(newSocket, null);
                    replaced = clientSocketRef.getAndSet(newSocket);
                    newSocket = null;
                }
                closeQuietly(replaced);
                if (isActiveGeneration(generation) && !searching && !pairing && searchGeneration.get() == scanGen) {
                    updateStatus(ThingStatus.ONLINE);
                }
                return ConnectionResult.CONNECTED;
            } catch (IOException e) {
                if (!isActiveGeneration(generation)) {
                    return ConnectionResult.INACTIVE;
                }
                if (searching || pairing || searchGeneration.get() != scanGen) {
                    return ConnectionResult.BUSY;
                }
                throw e;
            } finally {
                if (newSocket != null) {
                    connectingSocket.compareAndSet(newSocket, null);
                    closeQuietly(newSocket);
                }
            }
        }
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

    public byte[] newSeq() {
        synchronized (ioLock) {
            return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(seq++).array();
        }
    }

    private ClientConnection acquireClientConnection() throws UnknownHostException, IOException {
        long generation;
        long scanGen;
        synchronized (socketOwnershipLock) {
            generation = lifecycleGeneration.get();
            scanGen = searchGeneration.get();
            if (!isActiveGeneration(generation)) {
                throw new IOException("Sinopé Gateway is not active");
            }
            if (searching || pairing) {
                throw new IOException("Sinopé Gateway is currently busy (discovery or pairing in progress)");
            }
        }
        ConnectionResult result;
        try {
            result = connectToBridge(generation, scanGen);
        } catch (IOException e) {
            handleCommunicationError(generation, scanGen, null);
            throw e;
        }
        if (result == ConnectionResult.CONNECTED) {
            synchronized (socketOwnershipLock) {
                if (!isActiveGeneration(generation) || searching || pairing || searchGeneration.get() != scanGen) {
                    throw new IOException("Sinopé Gateway is not available for requests");
                }
                Socket localSocket = clientSocketRef.get();
                if (localSocket != null && localSocket.isConnected() && !localSocket.isClosed()) {
                    return new ClientConnection(generation, scanGen, localSocket);
                }
            }
        }
        throw new IOException("Could not create a socket to the gateway. Check host/ip/gateway Id");
    }

    SinopeAnswer execute(SinopeRequest command) throws UnknownHostException, IOException {
        ClientConnection conn = acquireClientConnection();
        try {
            synchronized (ioLock) {
                if (!isActiveGeneration(conn.generation()) || searching || pairing
                        || searchGeneration.get() != conn.scanGen()) {
                    throw new IOException("Sinopé Gateway is not available for requests");
                }
                return sendAndReceive(conn.socket(), command);
            }
        } catch (IOException e) {
            handleCommunicationError(conn.generation(), conn.scanGen(), conn.socket());
            throw e;
        }
    }

    SinopeAnswer execute(SinopeDataRequest command) throws UnknownHostException, IOException {
        ClientConnection conn = acquireClientConnection();
        try {
            synchronized (ioLock) {
                if (!isActiveGeneration(conn.generation()) || searching || pairing
                        || searchGeneration.get() != conn.scanGen()) {
                    throw new IOException("Sinopé Gateway is not available for requests");
                }
                OutputStream outToServer = conn.socket().getOutputStream();
                InputStream inputStream = conn.socket().getInputStream();
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
        } catch (IOException e) {
            handleCommunicationError(conn.generation(), conn.scanGen(), conn.socket());
            throw e;
        }
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
        if (!isValidPort(config.port)) {
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
        SinopeConfig config = getConfigAs(SinopeConfig.class);
        String effectiveApiKey = getEffectiveApiKey(config);
        if (config.hostname == null || config.hostname.isBlank() || !isValidPort(config.port)
                || config.gatewayId == null || effectiveApiKey == null || SinopeConfig.convert(config.gatewayId) == null
                || SinopeConfig.convert(effectiveApiKey) == null) {
            logger.debug("Cannot start device discovery: gateway configuration is incomplete");
            return;
        }

        long lifecycleGen;
        long scanGen;
        Socket oldSearch;
        Socket oldConnecting;
        Socket oldClient;
        synchronized (socketOwnershipLock) {
            lifecycleGen = lifecycleGeneration.get();
            if (!isActiveGeneration(lifecycleGen) || pairing) {
                logger.debug("Cannot start device discovery while gateway is pairing or disposed");
                return;
            }
            scanGen = searchGeneration.incrementAndGet();
            this.searching = true;
            oldSearch = searchSocket.getAndSet(null);
            oldConnecting = connectingSocket.getAndSet(null);
            oldClient = clientSocketRef.getAndSet(null);
        }

        stopPoll();
        closeQuietly(oldSearch);
        closeQuietly(oldConnecting);
        closeQuietly(oldClient);

        synchronized (schedulerLock) {
            if (!isCurrentSearch(lifecycleGen, scanGen)) {
                return;
            }
            cancelFuture(pollSearch, true);
            pollSearch = scheduler.schedule(() -> search(lifecycleGen, scanGen, deviceDiscoveredCallback),
                    FIRST_POLL_INTERVAL, TimeUnit.SECONDS);
        }
    }

    private void search(long lifecycleGen, long scanGen, final Consumer<byte[]> deviceDiscoveredCallback) {
        if (!isCurrentSearch(lifecycleGen, scanGen)) {
            return;
        }
        SinopeConfig config = getConfigAs(SinopeConfig.class);
        String hostname = config.hostname;
        Integer port = config.port;
        byte[] gatewayId = config.gatewayId != null ? SinopeConfig.convert(config.gatewayId) : null;
        String effectiveApiKey = getEffectiveApiKey(config);
        byte[] apiKey = effectiveApiKey != null ? SinopeConfig.convert(effectiveApiKey) : null;

        if (hostname == null || hostname.isBlank() || port == null || !isValidPort(port) || gatewayId == null
                || apiKey == null) {
            finishSearch(lifecycleGen, scanGen);
            return;
        }

        Socket socket = null;
        try {
            socket = openRegisteredSocket(hostname, port, searchSocket, () -> isCurrentSearch(lifecycleGen, scanGen));
            if (socket == null) {
                return;
            }

            sendAndReceive(socket, new SinopePingRequest());

            SinopeApiLoginAnswer loginAnswer = (SinopeApiLoginAnswer) sendAndReceive(socket,
                    new SinopeApiLoginRequest(gatewayId, apiKey));
            if (loginAnswer.getStatus() != 0) {
                logger.debug("Gateway login failed during device discovery (status=0x{})",
                        String.format("%02X", loginAnswer.getStatus()));
                return;
            }

            logger.debug("Successful login for device discovery - press both buttons on device");
            // Disable read timeout while listening for unsolicited reports so partial frames are never split;
            // stopSearch() and dispose() close searchSocket to unblock readFully().
            socket.setSoTimeout(0);

            while (isCurrentSearch(lifecycleGen, scanGen) && socket.isConnected() && !socket.isClosed()) {
                SinopeDeviceReportAnswer answ = new SinopeDeviceReportAnswer(socket.getInputStream());
                if (!isCurrentSearch(lifecycleGen, scanGen)) {
                    break;
                }
                logger.debug("Got report answer: {}", answ);
                byte[] deviceId = answ.getDeviceId();
                if (deviceId.length > 0) {
                    logger.debug("Discovered Sinopé device id: {}", ByteUtil.toString(deviceId).trim());
                    deviceDiscoveredCallback.accept(deviceId);
                }
            }
        } catch (UnknownHostException e) {
            logger.warn("Unexpected error when searching for new devices", e);
        } catch (IOException e) {
            logger.debug("Network connection closed during device discovery: {}", e.getMessage());
        } finally {
            searchSocket.compareAndSet(socket, null);
            closeQuietly(socket);
            finishSearch(lifecycleGen, scanGen);
        }
    }

    private void finishSearch(long lifecycleGen, long scanGen) {
        boolean shouldResumePoll = false;
        synchronized (socketOwnershipLock) {
            if (searchGeneration.get() == scanGen && isActiveGeneration(lifecycleGen)) {
                searching = false;
                shouldResumePoll = true;
            }
        }
        if (shouldResumePoll) {
            schedulePoll(lifecycleGen);
        }
    }

    public void stopSearch() {
        Socket oldSearch;
        long generation;
        boolean shouldResumePoll;
        synchronized (socketOwnershipLock) {
            searchGeneration.incrementAndGet();
            this.searching = false;
            oldSearch = this.searchSocket.getAndSet(null);
            generation = lifecycleGeneration.get();
            shouldResumePoll = isActiveGeneration(generation);
        }
        synchronized (schedulerLock) {
            cancelFuture(pollSearch, true);
            this.pollSearch = null;
        }
        closeQuietly(oldSearch);

        if (shouldResumePoll) {
            schedulePoll(generation);
        }
    }

    public Socket getClientSocket() throws UnknownHostException, IOException {
        return acquireClientConnection().socket();
    }

    private void handleCommunicationError(long generation, long scanGen, @Nullable Socket failedSocket) {
        Socket socketToClose = null;
        boolean shouldReportError = false;
        synchronized (socketOwnershipLock) {
            if (!isActiveGeneration(generation) || searching || pairing || searchGeneration.get() != scanGen) {
                return;
            }
            if (failedSocket != null) {
                if (clientSocketRef.compareAndSet(failedSocket, null)) {
                    socketToClose = failedSocket;
                    shouldReportError = true;
                }
            } else if (clientSocketRef.get() == null) {
                shouldReportError = true;
            }
        }
        closeQuietly(socketToClose);
        if (shouldReportError && isActiveGeneration(generation) && !searching && !pairing
                && searchGeneration.get() == scanGen) {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    public void setCommunicationError(boolean hasError) {
        long generation;
        long scanGen;
        Socket currentSocket = null;
        synchronized (socketOwnershipLock) {
            generation = lifecycleGeneration.get();
            scanGen = searchGeneration.get();
            if (!isActiveGeneration(generation) || searching || pairing) {
                return;
            }
            if (hasError) {
                currentSocket = clientSocketRef.getAndSet(null);
            }
        }
        if (hasError) {
            closeQuietly(currentSocket);
            if (isActiveGeneration(generation) && !searching && !pairing && searchGeneration.get() == scanGen) {
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } else {
            updateStatus(ThingStatus.ONLINE);
            schedulePoll(generation);
        }
    }
}
