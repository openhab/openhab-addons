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
package org.openhab.binding.atv.internal.client.protocols.companion;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.auth.HapPairVerify;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.exceptions.AtvException;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;
import org.openhab.binding.atv.internal.client.exceptions.OperationTimeoutError;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;
import org.openhab.binding.atv.internal.client.support.Opack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol logic related to Companion.
 *
 * <p>
 * Messages are OPACK dictionaries carried in {@code E_OPACK} (or other OPACK) frames with the
 * envelope keys {@code _t} (message type: 1=event, 2=request, 3=response), {@code _i}
 * (identifier), {@code _c} (content) and {@code _x} (XID used for request/response
 * correlation). Authentication frames ({@code PS_*}/{@code PV_*}) carry no XID and are
 * correlated by frame type instead, with the quirk that {@code *_Start} is only used for the
 * <em>first</em> message of an exchange and {@code *_Next} for everything after — including
 * the response to the first message.
 *
 * <p>
 * {@link #start()} connects and, when the service has credentials, runs HAP pair-verify
 * and enables transport encryption with keys derived using an empty salt and the infos
 * {@code ClientEncrypt-main} (output) / {@code ServerEncrypt-main} (input).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class CompanionProtocol implements CompanionConnection.FrameListener {

    /** Companion message type. */
    public enum MessageType {
        Event(1),
        Request(2),
        Response(3);

        private final int value;

        MessageType(int value) {
            this.value = value;
        }

        /** Numeric value used on the wire. */
        public int value() {
            return value;
        }
    }

    /** Listener interface for Companion events. */
    public interface Listener {

        /**
         * An event was received.
         *
         * @param eventName event identifier ({@code _i})
         * @param data event content ({@code _c})
         */
        void eventReceived(String eventName, Map<String, Object> data);
    }

    /** Default exchange timeout. */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    static final String SRP_SALT = "";
    static final String SRP_OUTPUT_INFO = "ClientEncrypt-main";
    static final String SRP_INPUT_INFO = "ServerEncrypt-main";

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanionProtocol.class);

    private final CompanionConnection connection;
    private final BaseService service;
    // Keyed by Long XID for OPACK frames or FrameType for auth frames.
    private final ConcurrentMap<Object, CompletableFuture<Map<String, Object>>> queues = new ConcurrentHashMap<>();

    private volatile @Nullable Listener listener;
    private long xid = ThreadLocalRandom.current().nextInt(0, 1 << 16);
    private boolean started;

    /**
     * Creates a new protocol instance.
     *
     * @param connection connection to the device (this protocol registers itself as its frame listener)
     * @param service service configuration carrying the credentials
     */
    public CompanionProtocol(CompanionConnection connection, BaseService service) {
        this.connection = connection;
        this.service = service;
        connection.setListener(this);
    }

    /**
     * Sets the listener receiving unsolicited events.
     *
     * @param listener event listener
     */
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * Connects to the device and sets up encryption when credentials are present.
     *
     * <p>
     * Blocking; call from a dedicated (virtual) thread.
     *
     * @throws ProtocolError if already started
     * @throws AuthenticationError if pair-verify fails
     */
    public synchronized void start() {
        if (started) {
            throw new ProtocolError("Already started");
        }
        started = true;
        connection.connect();

        LOGGER.debug("Companion credentials: {}", service.credentials().orElse(null));

        String credentialsString = service.credentials().orElse(null);
        if (credentialsString != null) {
            try {
                setupEncryption(HapCredentials.parse(credentialsString));
            } catch (AuthenticationError e) {
                throw e;
            } catch (Exception e) {
                throw new AuthenticationError("failed to set up encryption", e);
            }
        }
    }

    /** Disconnects from the device. */
    public void stop() {
        queues.clear();
        connection.close();
    }

    private void setupEncryption(HapCredentials credentials) {
        CompanionPairVerifyProcedure pairVerifier = new CompanionPairVerifyProcedure(this,
                new HapPairVerify(credentials));
        pairVerifier.verifyCredentials();
        var keys = pairVerifier.encryptionKeys(SRP_SALT, SRP_OUTPUT_INFO, SRP_INPUT_INFO);
        connection.enableEncryption(keys.outputKey(), keys.inputKey());
    }

    /**
     * Exchanges an auth frame ({@code PS_*} or {@code PV_*}) with the default timeout.
     *
     * <p>
     * The response to a {@code *_Start} frame arrives as {@code *_Next}.
     *
     * @param frameType auth frame type to send
     * @param data OPACK dictionary payload
     * @return future completing with the response dictionary
     */
    public CompletableFuture<Map<String, Object>> exchangeAuth(FrameType frameType, Map<String, Object> data) {
        Object identifier;
        if (frameType == FrameType.PS_Start) {
            identifier = FrameType.PS_Next;
        } else if (frameType == FrameType.PV_Start) {
            identifier = FrameType.PV_Next;
        } else {
            identifier = frameType;
        }
        return exchangeGenericOpack(frameType, data, identifier, DEFAULT_TIMEOUT);
    }

    /**
     * Sends data as OPACK and decodes the response, correlated by XID, with the default
     * timeout.
     *
     * @param frameType frame type to send
     * @param data OPACK dictionary payload; an {@code _x} entry is added
     * @return future completing with the response dictionary
     */
    public CompletableFuture<Map<String, Object>> exchangeOpack(FrameType frameType, Map<String, Object> data) {
        return exchangeOpack(frameType, data, DEFAULT_TIMEOUT);
    }

    /**
     * Sends data as OPACK and decodes the response, correlated by XID.
     *
     * @param frameType frame type to send
     * @param data OPACK dictionary payload; an {@code _x} entry is added
     * @param timeout maximum time to wait for the response
     * @return future completing with the response dictionary
     */
    public CompletableFuture<Map<String, Object>> exchangeOpack(FrameType frameType, Map<String, Object> data,
            Duration timeout) {
        long identifier;
        synchronized (this) {
            identifier = xid;
            xid++;
        }
        data.put("_x", identifier);
        return exchangeGenericOpack(frameType, data, identifier, timeout);
    }

    private CompletableFuture<Map<String, Object>> exchangeGenericOpack(FrameType frameType, Map<String, Object> data,
            Object identifier, Duration timeout) {
        LOGGER.debug("Exchange OPACK: {}", data);

        CompletableFuture<Map<String, Object>> shared = new CompletableFuture<>();
        queues.put(identifier, shared);
        try {
            sendOpack(frameType, data);
        } catch (RuntimeException e) {
            queues.remove(identifier);
            shared.completeExceptionally(e);
            return shared;
        }

        return shared.orTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS).handle((response, error) -> {
            if (error != null) {
                queues.remove(identifier);
                if (error instanceof TimeoutException) {
                    throw new OperationTimeoutError("no response to " + frameType + " within " + timeout, error);
                }
                if (error instanceof AtvException atvError) {
                    throw atvError;
                }
                throw new ProtocolError("exchange failed", error);
            }
            if (response.containsKey("_em")) {
                throw new ProtocolError("Command failed: " + response.get("_em"));
            }
            return response;
        });
    }

    /**
     * Sends an OPACK dictionary without waiting for a response.
     * An {@code _x} entry is added when not present.
     *
     * @param frameType frame type to send
     * @param data OPACK dictionary payload
     */
    public void sendOpack(FrameType frameType, Map<String, Object> data) {
        synchronized (this) {
            if (!data.containsKey("_x")) {
                data.put("_x", xid);
                xid++;
            }
        }
        LOGGER.debug("Send OPACK: {}", data);
        connection.send(frameType, Opack.pack(data));
    }

    @Override
    public void frameReceived(FrameType frameType, byte[] data) {
        LOGGER.debug("Received frame {} ({} bytes)", frameType, data.length);

        if (!frameType.isOpackFrame() && !frameType.isAuthFrame()) {
            LOGGER.debug("Received unsupported frame type: {}", frameType);
            return;
        }
        try {
            Object opackData = Opack.unpack(data).value();
            if (!(opackData instanceof Map)) {
                LOGGER.debug("Unsupported OPACK base type: {}", opackData == null ? null : opackData.getClass());
                return;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) opackData;
            if (frameType.isAuthFrame()) {
                handleAuth(frameType, message);
            } else {
                handleOpack(frameType, message);
            }
        } catch (Exception e) {
            LOGGER.warn("failed to process frame", e);
        }
    }

    private void handleAuth(FrameType frameType, Map<String, Object> opackData) {
        LOGGER.debug("Process incoming auth frame ({}): {}", frameType, opackData);
        CompletableFuture<Map<String, Object>> shared = queues.remove(frameType);
        if (shared != null) {
            shared.complete(opackData);
        } else {
            LOGGER.warn("No receiver for auth frame {}", frameType);
        }
    }

    private void handleOpack(FrameType frameType, Map<String, Object> opackData) {
        LOGGER.debug("Process incoming OPACK frame ({}): {}", frameType, opackData);

        Long messageType = toLong(opackData.get("_t"));
        if (messageType != null && messageType == MessageType.Event.value()) {
            LOGGER.debug("Received event: {}", opackData);
            Listener currentListener = listener;
            if (currentListener != null) {
                String eventName = (String) opackData.get("_i");
                @SuppressWarnings("unchecked")
                Map<String, Object> content = (Map<String, Object>) opackData.get("_c");
                if (eventName != null && content != null) {
                    currentListener.eventReceived(eventName, content);
                } else {
                    LOGGER.debug("Ignoring malformed event (missing _i or _c): {}", opackData);
                }
            }
        } else if (messageType != null && messageType == MessageType.Response.value()) {
            Long responseXid = toLong(opackData.get("_x"));
            CompletableFuture<Map<String, Object>> shared = responseXid == null ? null : queues.remove(responseXid);
            if (shared != null) {
                shared.complete(opackData);
            } else {
                LOGGER.debug("No receiver for XID {}", responseXid);
            }
        } else {
            LOGGER.warn("Got OPACK frame with unsupported type: {}", messageType);
        }
    }

    /**
     * Extracts a long from an OPACK-decoded number ({@link Number} or
     * {@link Opack.SizedLong}).
     *
     * @param value decoded value
     * @return the long value, or {@code null} if not a number
     */
    static @Nullable Long toLong(@Nullable Object value) {
        if (value instanceof Opack.SizedLong sized) {
            return sized.value();
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
