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
package org.openhab.binding.atv.internal.client.protocols.mrp;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.auth.EncryptionKeys;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.core.AtvRuntime;
import org.openhab.binding.atv.internal.client.core.DeviceLoop;
import org.openhab.binding.atv.internal.client.core.Heartbeater;
import org.openhab.binding.atv.internal.client.core.MessageDispatcher;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;
import org.openhab.binding.atv.internal.client.exceptions.ConnectionLostError;
import org.openhab.binding.atv.internal.client.exceptions.InvalidStateError;
import org.openhab.binding.atv.internal.client.exceptions.OperationTimeoutError;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ProtocolMessageOuterClass.ProtocolMessage;
import org.openhab.binding.atv.internal.client.settings.InfoSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol logic related to MRP.
 *
 * <p>
 * Wraps an {@link AbstractMrpConnection} and automatically sends the initial messages in
 * the right order: {@code DEVICE_INFORMATION} always first, then the
 * {@code CRYPTO_PAIRING} verify flow (enabling encryption), then
 * {@code SET_CONNECTION_STATE}, {@code CLIENT_UPDATES_CONFIG} and
 * {@code GET_KEYBOARD_SESSION}.
 *
 * <p>
 * Request/response correlation: normal messages get a random UUID {@code identifier};
 * crypto messages carry no identifier and are correlated with the synthetic key
 * {@code "type_&lt;n&gt;"} instead (only one such message can be outstanding at a time).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MrpProtocol extends MessageDispatcher<ProtocolMessage.Type, ProtocolMessage>
        implements AbstractMrpConnection.Listener {

    /**
     * Time between periodic heartbeats.
     */
    public static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(30);

    /** Immediate re-attempts after a failed heartbeat. */
    public static final int HEARTBEAT_RETRIES = 1;

    /** HKDF salt for the MRP channel keys. */
    public static final String SRP_SALT = "MediaRemote-Salt";

    /** HKDF info for the key encrypting our outgoing messages. */
    public static final String SRP_OUTPUT_INFO = "MediaRemote-Write-Encryption-Key";

    /** HKDF info for the key decrypting incoming messages. */
    public static final String SRP_INPUT_INFO = "MediaRemote-Read-Encryption-Key";

    private static final Logger LOGGER = LoggerFactory.getLogger(MrpProtocol.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    /** Protocol internal state. */
    public enum ProtocolState {
        /** Not connected. */
        NOT_CONNECTED,
        /** Connecting. */
        CONNECTING,
        /** Connected, initial message exchange in progress. */
        CONNECTED,
        /** Ready for use. */
        READY,
        /** Stopped. */
        STOPPED
    }

    private final AbstractMrpConnection connection;
    private final BaseService service;
    private final InfoSettings info;
    private final DeviceLoop loop;
    private final AtvRuntime runtime;
    private final ConcurrentMap<String, Outstanding> outstanding = new ConcurrentHashMap<>();

    private volatile byte[] pairingId;
    private volatile @Nullable ProtocolMessage deviceInfo;
    private volatile ProtocolState state = ProtocolState.NOT_CONNECTED;
    private volatile @Nullable Heartbeater heartbeater;

    private record Outstanding(CompletableFuture<ProtocolMessage> future, ScheduledFuture<?> timeout) {
    }

    /**
     * Creates a new protocol with a random pairing id.
     *
     * @param connection connection used for transport
     * @param service the MRP service (credentials are read from it)
     * @param info identity settings used for {@code DEVICE_INFORMATION}
     * @param loop device loop used for message dispatch
     * @param runtime runtime providing the scheduler used for timeouts and heartbeats
     */
    public MrpProtocol(AbstractMrpConnection connection, BaseService service, InfoSettings info, DeviceLoop loop,
            AtvRuntime runtime) {
        this(connection, service, info, loop, runtime, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a new protocol with an explicit pairing id (used by the pairing handler so
     * the id matches the one baked into new credentials).
     *
     * @param connection connection used for transport
     * @param service the MRP service (credentials are read from it)
     * @param info identity settings used for {@code DEVICE_INFORMATION}
     * @param loop device loop used for message dispatch
     * @param runtime runtime providing the scheduler used for timeouts and heartbeats
     * @param pairingId our pairing identifier bytes
     */
    public MrpProtocol(AbstractMrpConnection connection, BaseService service, InfoSettings info, DeviceLoop loop,
            AtvRuntime runtime, byte[] pairingId) {
        super(loop);
        this.connection = connection;
        this.service = service;
        this.info = info;
        this.loop = loop;
        this.runtime = runtime;
        this.pairingId = pairingId.clone();
        connection.setListener(this);
    }

    /**
     * Returns the connection this protocol runs on.
     */
    public AbstractMrpConnection connection() {
        return connection;
    }

    /**
     * Returns the device loop this protocol dispatches on.
     */
    public DeviceLoop loop() {
        return loop;
    }

    /**
     * Returns the runtime used for timers.
     */
    public AtvRuntime runtime() {
        return runtime;
    }

    /**
     * Returns our pairing identifier bytes.
     */
    public byte[] pairingId() {
        return pairingId.clone();
    }

    /**
     * Returns the device information message received during {@link #start()}.
     */
    public Optional<ProtocolMessage> deviceInfo() {
        return Optional.ofNullable(deviceInfo);
    }

    /** Test hook to set the device info directly. */
    void deviceInfo(ProtocolMessage deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    /**
     * Returns the current protocol state.
     */
    public ProtocolState state() {
        return state;
    }

    /**
     * Connects to the device and performs the full initial message exchange.
     *
     * @return future completing when the protocol is ready
     */
    public CompletableFuture<Void> start() {
        return start(false);
    }

    /**
     * Connects to the device and listens to incoming messages.
     *
     * @param skipInitialMessages if {@code true}, stop after {@code DEVICE_INFORMATION}
     *            (used by pairing)
     * @return future completing when the protocol is ready (or connected, when initial
     *         messages are skipped)
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public CompletableFuture<Void> start(boolean skipInitialMessages) {
        if (state != ProtocolState.NOT_CONNECTED) {
            return CompletableFuture.failedFuture(new InvalidStateError(state.name()));
        }
        state = ProtocolState.CONNECTING;

        CompletableFuture<Void> result = new CompletableFuture<>();
        Thread.ofVirtual().name("mrp-start").start(() -> {
            try {
                connection.connect().join();
                state = ProtocolState.CONNECTED;

                // In case credentials have been given externally (i.e. not by pairing
                // with a device), then use that client id
                Optional<String> credentials = service.credentials();
                if (credentials.isPresent()) {
                    pairingId = HapCredentials.parse(credentials.get()).clientId();
                }

                // The first message must always be DEVICE_INFORMATION, otherwise the
                // device will not respond with anything
                ProtocolMessage receivedInfo = sendAndReceive(
                        MrpMessages.deviceInformation(info, new String(pairingId, StandardCharsets.UTF_8), false))
                        .join();
                this.deviceInfo = receivedInfo;

                // Distribute the device information to all listeners (as the
                // send-and-receive correlation stops that propagation)
                dispatch(ProtocolMessage.Type.DEVICE_INFO_MESSAGE, receivedInfo);

                if (!skipInitialMessages) {
                    try {
                        enableEncryption();
                    } catch (AuthenticationError e) {
                        throw e;
                    } catch (RuntimeException e) {
                        throw new AuthenticationError("authentication failed", e);
                    }

                    // This should be the first message sent after encryption has been enabled
                    send(MrpMessages.setConnectionState()).join();

                    // Subscribe to updates at this stage
                    sendAndReceive(MrpMessages.clientUpdatesConfig()).join();
                    sendAndReceive(MrpMessages.getKeyboardSession()).join();

                    state = ProtocolState.READY;
                }
                MrpFutures.completeVoid(result);
            } catch (Throwable t) {
                stop();
                result.completeExceptionally(unwrap(t));
            }
        });
        return result;
    }

    private static Throwable unwrap(Throwable t) {
        if (t instanceof java.util.concurrent.CompletionException completion) {
            Throwable cause = completion.getCause();
            if (cause != null) {
                return cause;
            }
        }
        return t;
    }

    /**
     * Disconnects from the device.
     */
    public void stop() {
        if (!outstanding.isEmpty()) {
            LOGGER.debug("There were {} outstanding requests", outstanding.size());
        }
        @Nullable
        Heartbeater beater = heartbeater;
        if (beater != null) {
            beater.stop();
            heartbeater = null;
        }
        for (String identifier : outstanding.keySet()) {
            Outstanding pending = outstanding.remove(identifier);
            if (pending != null) {
                pending.timeout().cancel(false);
                pending.future().completeExceptionally(new ConnectionLostError("protocol stopped"));
            }
        }
        connection.close();
        state = ProtocolState.STOPPED;
    }

    /**
     * Enables sending periodic heartbeat messages with the default interval and retry count.
     */
    public void enableHeartbeat() {
        enableHeartbeat(HEARTBEAT_INTERVAL, HEARTBEAT_RETRIES);
    }

    /**
     * Enables sending periodic heartbeat messages.
     *
     * @param interval time between heartbeats
     * @param retries immediate re-attempts after a failed heartbeat
     */
    public void enableHeartbeat(Duration interval, int retries) {
        Heartbeater beater = new Heartbeater(connection.toString(),
                () -> sendAndReceive(MrpMessages.create(ProtocolMessage.Type.GENERIC_MESSAGE).build()),
                connection::close, runtime.scheduler(), interval, retries);
        heartbeater = beater;
        beater.start();
    }

    private void enableEncryption() {
        // Encryption can be enabled whenever credentials are available but only
        // after DEVICE_INFORMATION has been sent
        Optional<String> credentialsString = service.credentials();
        if (credentialsString.isEmpty()) {
            return;
        }

        // Verify credentials and generate keys
        HapCredentials credentials = HapCredentials.parse(credentialsString.get());
        MrpPairVerifyProcedure pairVerifier = new MrpPairVerifyProcedure(this, credentials);
        pairVerifier.verifyCredentials().join();
        EncryptionKeys keys = pairVerifier.encryptionKeys(SRP_SALT, SRP_OUTPUT_INFO, SRP_INPUT_INFO);
        connection.enableEncryption(keys.outputKey(), keys.inputKey());
    }

    /**
     * Sends a message and expects no response.
     *
     * @param message the message to send
     * @return future completing when the message has been handed to the connection
     */
    public CompletableFuture<Void> send(ProtocolMessage message) {
        if (state != ProtocolState.CONNECTED && state != ProtocolState.READY) {
            return CompletableFuture.failedFuture(new InvalidStateError(state.name()));
        }
        try {
            connection.send(message);
            return CompletableFuture.completedFuture(null);
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Sends a message with a generated identifier and waits for the response.
     *
     * @param message the message to send
     * @return future completed with the response
     */
    public CompletableFuture<ProtocolMessage> sendAndReceive(ProtocolMessage message) {
        return sendAndReceive(message, true, DEFAULT_TIMEOUT);
    }

    /**
     * Sends a message and waits for the response.
     *
     * @param message the message to send
     * @param generateIdentifier if {@code true} a random UUID identifier is stamped on the
     *            message; if {@code false} the synthetic key {@code "type_&lt;n&gt;"} is
     *            used for correlation (crypto pairing messages carry no identifier)
     * @return future completed with the response
     */
    public CompletableFuture<ProtocolMessage> sendAndReceive(ProtocolMessage message, boolean generateIdentifier) {
        return sendAndReceive(message, generateIdentifier, DEFAULT_TIMEOUT);
    }

    /**
     * Sends a message and waits for the response.
     *
     * @param message the message to send
     * @param generateIdentifier if {@code true} a random UUID identifier is stamped on the
     *            message; if {@code false} the synthetic key {@code "type_&lt;n&gt;"} is
     *            used for correlation
     * @param timeout maximum time to wait for the response
     * @return future completed with the response, or exceptionally with
     *         {@link OperationTimeoutError} when the response does not arrive in time
     */
    public CompletableFuture<ProtocolMessage> sendAndReceive(ProtocolMessage message, boolean generateIdentifier,
            Duration timeout) {
        if (state != ProtocolState.CONNECTED && state != ProtocolState.READY) {
            return CompletableFuture.failedFuture(new InvalidStateError(state.name()));
        }

        // Some messages will respond with the same identifier as used in the
        // corresponding request. Others will not and one example is the crypto
        // message (for pairing). They will never include an identifier, but it
        // is in turn only possible to have one of those message outstanding at
        // one time (i.e. it's not possible to mix up the responses). In those
        // cases, a "fake" identifier is used that includes the message type.
        String identifier;
        ProtocolMessage toSend = message;
        if (generateIdentifier) {
            identifier = UUID.randomUUID().toString().toUpperCase();
            toSend = message.toBuilder().setIdentifier(identifier).build();
        } else {
            identifier = "type_" + message.getType().getNumber();
        }

        CompletableFuture<ProtocolMessage> future = new CompletableFuture<>();
        ScheduledFuture<?> timeoutTask = runtime.scheduler().schedule(() -> {
            Outstanding pending = outstanding.remove(identifier);
            if (pending != null) {
                pending.future()
                        .completeExceptionally(new OperationTimeoutError("no response to " + message.getType()));
            }
        }, timeout.toMillis(), TimeUnit.MILLISECONDS);
        outstanding.put(identifier, new Outstanding(future, timeoutTask));

        try {
            connection.send(toSend);
        } catch (RuntimeException e) {
            Outstanding pending = outstanding.remove(identifier);
            if (pending != null) {
                pending.timeout().cancel(false);
            }
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public void messageReceived(ProtocolMessage message) {
        // If the message identifier is outstanding, then someone is waiting for
        // the response so complete the future here
        String identifier = message.getIdentifier().isEmpty() ? "type_" + message.getType().getNumber()
                : message.getIdentifier();
        Outstanding pending = outstanding.remove(identifier);
        if (pending != null) {
            pending.timeout().cancel(false);
            pending.future().complete(message);
        } else {
            dispatch(message.getType(), message);
        }
    }

    @Override
    public void connectionLost(@Nullable Exception exception) {
        stop();
    }
}
