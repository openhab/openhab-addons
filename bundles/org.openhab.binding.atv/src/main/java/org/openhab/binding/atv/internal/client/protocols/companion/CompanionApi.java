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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.core.Core;
import org.openhab.binding.atv.internal.client.dto.InputAction;
import org.openhab.binding.atv.internal.client.dto.TouchAction;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;
import org.openhab.binding.atv.internal.client.protocols.companion.CompanionProtocol.MessageType;
import org.openhab.binding.atv.internal.client.settings.InfoSettings;
import org.openhab.binding.atv.internal.client.support.NsKeyedArchiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * High level implementation of the Companion API.
 *
 * <p>
 * Public operations return {@link CompletableFuture}s; multi-step sequences (connect, text
 * input, clicks, swipes) run blocking on dedicated virtual threads, while single exchanges are
 * composed asynchronously so listener callbacks (delivered on the device loop) may safely
 * trigger further commands.
 *
 * <p>
 * Connection flow: protocol start (pair-verify), then {@code _systemInfo},
 * {@code _touchStart}, {@code _sessionStart} (SID composed as {@code remote << 32 | local}
 * against service {@code com.apple.tvremoteservices}), {@code TVRCSessionStart} (required
 * before {@code FetchAttentionState} on newer tvOS), {@code _tiStart} and finally an
 * {@code _interest} subscription for {@code _iMC}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class CompanionApi implements CompanionProtocol.Listener {

    /** Touchpad width used with {@code _touchStart}. */
    public static final double TOUCHPAD_WIDTH = 1000.0;
    /** Touchpad height used with {@code _touchStart}. */
    public static final double TOUCHPAD_HEIGHT = 1000.0;
    /** Delay between touch move events in milliseconds. */
    public static final int TOUCHPAD_DELAY_MS = 16;

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanionApi.class);

    private final Core core;
    private final ExecutorService blockingExecutor = Executors
            .newThreadPerTaskExecutor(Thread.ofVirtual().name("companion-api-", 0).factory());
    private final ConcurrentMap<String, CopyOnWriteArrayList<Consumer<Map<String, Object>>>> eventListeners = new ConcurrentHashMap<>();
    private final List<String> subscribedEvents = new CopyOnWriteArrayList<>();
    private final Object connectionLock = new Object();

    private @Nullable CompanionConnection connection;
    private @Nullable CompanionProtocol protocol;
    private @Nullable CompletableFuture<@Nullable Void> connectFuture;
    private volatile long sid;
    private volatile long baseTimestamp = System.nanoTime();

    /**
     * Creates a new API instance.
     *
     * @param core protocol context
     */
    public CompanionApi(Core core) {
        this.core = core;
    }

    /**
     * The current session identifier (0 before {@code _sessionStart}).
     */
    public long sid() {
        return sid;
    }

    /** Executor for blocking multi-step sequences; used by the interface implementations. */
    Executor blockingExecutor() {
        return blockingExecutor;
    }

    /**
     * Registers a listener for a named Companion event. Listeners are invoked on the device loop.
     *
     * @param event event identifier (e.g. {@code "_iMC"})
     * @param listener callback receiving the event content
     */
    public void listenTo(String event, Consumer<Map<String, Object>> listener) {
        eventListeners.computeIfAbsent(event, key -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Dispatches an event to all its listeners via the device loop.
     *
     * @param event event identifier
     * @param data event content
     * @return future completed once every listener has been invoked
     */
    public CompletableFuture<Void> dispatch(String event, Map<String, Object> data) {
        List<CompletableFuture<Void>> invocations = new ArrayList<>();
        for (Consumer<Map<String, Object>> listener : eventListeners.getOrDefault(event,
                new CopyOnWriteArrayList<>())) {
            invocations.add(core.loop().submitVoid(() -> {
                try {
                    listener.accept(data);
                } catch (RuntimeException e) {
                    LOGGER.warn("Error dispatching event {}", event, e);
                }
            }));
        }
        return CompletableFuture.allOf(invocations.toArray(CompletableFuture[]::new));
    }

    @Override
    public void eventReceived(String eventName, Map<String, Object> data) {
        LOGGER.debug("Got event {} from device: {}", eventName, data);
        dispatch(eventName, data);
    }

    /**
     * Connects to the remote device, running the full connect sequence. Subsequent
     * calls return the same (possibly already completed) future.
     *
     * @return future completing when the connection is fully established
     */
    @SuppressWarnings({ "PMD.CompareObjectsWithEquals", "PMD.AvoidCatchingGenericException" })
    public CompletableFuture<@Nullable Void> connect() {
        synchronized (connectionLock) {
            CompletableFuture<@Nullable Void> existing = connectFuture;
            if (existing != null) {
                return existing;
            }
            CompletableFuture<@Nullable Void> future = new CompletableFuture<>();
            connectFuture = future;
            blockingExecutor.execute(() -> {
                try {
                    doConnect();
                    future.complete(null);
                } catch (Throwable t) {
                    synchronized (connectionLock) {
                        if (connectFuture == future) {
                            connectFuture = null;
                        }
                        CompanionProtocol currentProtocol = protocol;
                        if (currentProtocol != null) {
                            currentProtocol.stop();
                            protocol = null;
                            connection = null;
                        }
                    }
                    future.completeExceptionally(t);
                }
            });
            return future;
        }
    }

    private void doConnect() {
        LOGGER.debug("Connect to Companion from API");
        CompanionConnection newConnection = new CompanionConnection(core.address(), core.service().port(),
                new CompanionConnection.ConnectionListener() {
                    @Override
                    public void connectionLost(Exception exception) {
                        core.deviceListener().fire(listener -> listener.connectionLost(exception));
                    }

                    @Override
                    public void connectionClosed() {
                        core.deviceListener().fire(listener -> listener.connectionClosed());
                    }
                });
        CompanionProtocol newProtocol = new CompanionProtocol(newConnection, core.service());
        newProtocol.setListener(this);
        synchronized (connectionLock) {
            connection = newConnection;
            protocol = newProtocol;
        }
        newProtocol.start();

        systemInfo();
        touchStart();
        sessionStart();
        tvRcSessionStart();
        textInputStart();

        subscribeEventBlocking("_iMC");
    }

    /**
     * Disconnects from the device: unsubscribes all events, stops the session, touch and
     * text-input subsystems (errors ignored) and closes the connection.
     *
     * @return future completing when disconnected
     */
    public CompletableFuture<Void> disconnect() {
        return CompletableFuture.runAsync(() -> {
            CompanionProtocol currentProtocol;
            synchronized (connectionLock) {
                currentProtocol = protocol;
            }
            if (currentProtocol == null) {
                return;
            }
            try {
                for (String event : List.copyOf(subscribedEvents)) {
                    unsubscribeEventBlocking(event);
                }
                // Sometimes these fail for unknown reasons; swallow errors.
                sessionStop();
                touchStop();
                textInputStop();
            } catch (Exception ex) {
                LOGGER.debug("Ignoring error during disconnect: {}", ex.toString());
            } finally {
                synchronized (connectionLock) {
                    currentProtocol.stop();
                    protocol = null;
                    connection = null;
                    connectFuture = null;
                }
            }
        }, blockingExecutor);
    }

    // Command plumbing

    /**
     * Sends a request command to the device and returns the response.
     *
     * @param identifier command identifier ({@code _i})
     * @param content command content ({@code _c})
     * @return future completing with the full response dictionary
     */
    public CompletableFuture<Map<String, Object>> sendCommand(String identifier, Map<String, Object> content) {
        return connect().thenCompose(unused -> exchange(identifier, content, MessageType.Request));
    }

    private CompletableFuture<Map<String, Object>> exchange(String identifier, Map<String, Object> content,
            MessageType messageType) {
        CompanionProtocol currentProtocol = protocolOrThrow();
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("_i", identifier);
        envelope.put("_t", (long) messageType.value());
        envelope.put("_c", content);
        return currentProtocol.exchangeOpack(FrameType.E_OPACK, envelope).handle((response, error) -> {
            if (error == null) {
                return response;
            }
            Throwable cause = error;
            if (cause instanceof CompletionException completionException) {
                Throwable inner = completionException.getCause();
                if (inner != null) {
                    cause = inner;
                }
            }
            if (cause instanceof ProtocolError protocolError) {
                throw protocolError;
            }
            throw new ProtocolError("Command " + identifier + " failed", cause);
        });
    }

    /** Blocking variant of {@link #sendCommand} for the internal sequential flows. */
    private Map<String, Object> exchangeBlocking(String identifier, Map<String, Object> content) {
        return join(exchange(identifier, content, MessageType.Request));
    }

    /**
     * Sends an event message (no response expected).
     *
     * @param identifier event identifier ({@code _i})
     * @param content event content ({@code _c})
     * @return future completing when the event has been sent
     */
    public CompletableFuture<Void> sendEvent(String identifier, Map<String, Object> content) {
        return connect().thenRun(() -> sendEventDirect(identifier, content));
    }

    private void sendEventDirect(String identifier, Map<String, Object> content) {
        CompanionProtocol currentProtocol = protocolOrThrow();
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("_i", identifier);
        envelope.put("_t", (long) MessageType.Event.value());
        envelope.put("_c", content);
        try {
            currentProtocol.sendOpack(FrameType.E_OPACK, envelope);
        } catch (ProtocolError e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ProtocolError("Send event failed", e);
        }
    }

    private CompanionProtocol protocolOrThrow() {
        synchronized (connectionLock) {
            CompanionProtocol currentProtocol = protocol;
            if (currentProtocol == null) {
                throw new IllegalStateException("not connected to companion");
            }
            return currentProtocol;
        }
    }

    // Connect sequence steps

    private void systemInfo() {
        LOGGER.debug("Sending system information");
        HapCredentials credentials = HapCredentials.parse(core.service().credentials().orElse(null));
        InfoSettings info = core.settings().info();

        // Several of these values are effectively arbitrary.
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("_bf", 0L);
        content.put("_cf", 512L);
        content.put("_clFl", 128L);
        // A null "_i" stops the device from pushing TVSystemStatus (power state) events;
        // fall back to a stable identifier.
        String identifier = info.rpId() != null && !info.rpId().isEmpty() ? info.rpId()
                : info.deviceId().replace(":", "").toLowerCase();
        content.put("_i", identifier);
        content.put("_idsID", credentials.clientId());
        // Not really device id here, but better than anything...
        content.put("_pubID", info.deviceId());
        content.put("_sf", 256L); // Status flags?
        content.put("_sv", "170.18"); // Software Version (I guess?)
        content.put("model", info.model());
        content.put("name", info.name());

        exchangeBlocking("_systemInfo", content);
    }

    private void sessionStart() {
        long localSid = ThreadLocalRandom.current().nextLong(0, 1L << 32);
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("_srvT", "com.apple.tvremoteservices");
        content.put("_sid", localSid);
        Map<String, Object> response = exchangeBlocking("_sessionStart", content);

        Map<String, Object> responseContent = content(response);
        if (responseContent == null) {
            throw new ProtocolError("missing content");
        }
        Long remoteSid = CompanionProtocol.toLong(responseContent.get("_sid"));
        if (remoteSid == null) {
            throw new ProtocolError("missing _sid in session start response");
        }
        sid = (remoteSid << 32) | localSid;
        LOGGER.debug("Started session with SID 0x{}", Long.toHexString(sid));
    }

    /**
     * Opens a TV Remote Client session: tvOS does not answer {@code FetchAttentionState}
     * until such a session is registered with the tvremoted process.
     */
    private void tvRcSessionStart() {
        try {
            Map<String, Object> response = exchangeBlocking("TVRCSessionStart",
                    new LinkedHashMap<>(Map.of("ProtocolVersionKey", "1.2")));
            LOGGER.debug("Started TV RC session: {}", content(response));
        } catch (Exception ex) {
            LOGGER.debug("TVRCSessionStart not supported: {}", ex.toString());
        }
    }

    private void sessionStop() {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("_srvT", "com.apple.tvremoteservices");
        content.put("_sid", sid);
        exchangeBlocking("_sessionStop", content);
        LOGGER.debug("Stopped session with SID 0x{}", Long.toHexString(sid));
    }

    // Event subscriptions

    /**
     * Subscribes to updates of an event via {@code _interest}.
     *
     * @param event event identifier
     * @return future completing when subscribed
     */
    public CompletableFuture<Void> subscribeEvent(String event) {
        if (subscribedEvents.contains(event)) {
            return CompletableFuture.completedFuture(null);
        }
        return sendEvent("_interest", new LinkedHashMap<>(Map.of("_regEvents", List.of(event))))
                .thenRun(() -> subscribedEvents.add(event));
    }

    /**
     * Unsubscribes from updates of an event.
     *
     * @param event event identifier
     * @return future completing when unsubscribed
     */
    public CompletableFuture<Void> unsubscribeEvent(String event) {
        if (!subscribedEvents.contains(event)) {
            return CompletableFuture.completedFuture(null);
        }
        return sendEvent("_interest", new LinkedHashMap<>(Map.of("_deregEvents", List.of(event))))
                .thenRun(() -> subscribedEvents.remove(event));
    }

    private void subscribeEventBlocking(String event) {
        if (!subscribedEvents.contains(event)) {
            sendEventDirect("_interest", new LinkedHashMap<>(Map.of("_regEvents", List.of(event))));
            subscribedEvents.add(event);
        }
    }

    private void unsubscribeEventBlocking(String event) {
        if (subscribedEvents.contains(event)) {
            sendEventDirect("_interest", new LinkedHashMap<>(Map.of("_deregEvents", List.of(event))));
            subscribedEvents.remove(event);
        }
    }

    // Apps and accounts

    /**
     * Launches an app on the remote device.
     *
     * @param bundleIdentifierOrUrl app bundle identifier, or URL/URL scheme
     * @return future completing when the app has been launched
     */
    public CompletableFuture<Void> launchApp(String bundleIdentifierOrUrl) {
        String launchCommandKey = isUrlOrScheme(bundleIdentifierOrUrl) ? "_urlS" : "_bundleID";
        Map<String, Object> content = new LinkedHashMap<>();
        content.put(launchCommandKey, bundleIdentifierOrUrl);
        return sendCommand("_launchApp", content).thenRun(() -> {
        });
    }

    /**
     * Returns the raw response listing launchable apps.
     *
     * @return future completing with the response dictionary
     */
    public CompletableFuture<Map<String, Object>> appList() {
        return sendCommand("FetchLaunchableApplicationsEvent", new LinkedHashMap<>());
    }

    /**
     * Switches user account on the remote device.
     *
     * @param accountId account identifier
     * @return future completing when the account has been switched
     */
    public CompletableFuture<Void> switchAccount(String accountId) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("SwitchAccountID", accountId);
        return sendCommand("SwitchUserAccountEvent", content).thenRun(() -> {
        });
    }

    /**
     * Returns the raw response listing user accounts.
     *
     * @return future completing with the response dictionary
     */
    public CompletableFuture<Map<String, Object>> accountList() {
        return sendCommand("FetchUserAccountsEvent", new LinkedHashMap<>());
    }

    // HID and touch

    /**
     * Sends a HID button command.
     *
     * @param down {@code true} for button down, {@code false} for button up
     * @param command button to press
     * @return future completing when the command has been acknowledged
     */
    public CompletableFuture<Void> hidCommand(boolean down, HidCommand command) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("_hBtS", down ? 1L : 2L);
        content.put("_hidC", (long) command.value());
        return sendCommand("_hidC", content).thenRun(() -> {
        });
    }

    /**
     * Sends a touch event.
     *
     * @param x x coordinate, clamped to [0,1000]
     * @param y y coordinate, clamped to [0,1000]
     * @param mode touch action
     * @return future completing when the event has been sent
     */
    public CompletableFuture<Void> hidEvent(int x, int y, TouchAction mode) {
        long clampedX = Math.min(Math.max(x, 0), (int) TOUCHPAD_WIDTH);
        long clampedY = Math.min(Math.max(y, 0), (int) TOUCHPAD_HEIGHT);
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("_ns", System.nanoTime() - baseTimestamp);
        content.put("_tFg", 1L);
        content.put("_cx", clampedX);
        content.put("_tPh", (long) mode.value());
        content.put("_cy", clampedY);
        return sendEvent("_hidT", content);
    }

    /**
     * Generates a swipe gesture from start to end coordinates (in range [0,1000]) over the
     * given duration.
     *
     * @param startX start x coordinate
     * @param startY start y coordinate
     * @param endX end x coordinate
     * @param endY end y coordinate
     * @param durationMs time in milliseconds to reach the end coordinates
     * @return future completing when the gesture has been sent
     */
    public CompletableFuture<Void> swipe(int startX, int startY, int endX, int endY, int durationMs) {
        return CompletableFuture.runAsync(() -> {
            long endTime = System.nanoTime() + durationMs * 1_000_000L;
            double x = startX;
            double y = startY;
            join(hidEvent((int) x, (int) y, TouchAction.Press));
            long current = System.nanoTime();
            while (current < endTime) {
                x = x + (endX - x) * TOUCHPAD_DELAY_MS * 1_000_000.0 / (endTime - current);
                y = y + (endY - y) * TOUCHPAD_DELAY_MS * 1_000_000.0 / (endTime - current);
                x = Math.min(Math.max(x, 0), TOUCHPAD_WIDTH);
                y = Math.min(Math.max(y, 0), TOUCHPAD_HEIGHT);
                join(hidEvent((int) x, (int) y, TouchAction.Hold));
                sleep(TOUCHPAD_DELAY_MS);
                current = System.nanoTime();
            }
            join(hidEvent(endX, endY, TouchAction.Release));
        }, blockingExecutor);
    }

    /**
     * Generates a touch event to x,y coordinates in range [0,1000].
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param mode touch mode (press, hold or release)
     * @return future completing when the event has been sent
     */
    public CompletableFuture<Void> action(int x, int y, TouchAction mode) {
        return hidEvent(x, y, mode);
    }

    /**
     * Sends a touch click.
     *
     * @param action single tap, double tap or hold
     * @return future completing when the click has been sent
     */
    public CompletableFuture<Void> click(InputAction action) {
        return CompletableFuture.runAsync(() -> {
            if (action == InputAction.SingleTap || action == InputAction.DoubleTap) {
                int count = action == InputAction.SingleTap ? 1 : 2;
                for (int i = 0; i < count; i++) {
                    join(hidCommand(true, HidCommand.Select));
                    sleep(20);
                    join(hidCommand(false, HidCommand.Select));
                    join(hidEvent((int) TOUCHPAD_WIDTH, (int) TOUCHPAD_HEIGHT, TouchAction.Click));
                }
            } else { // Hold
                join(hidCommand(true, HidCommand.Select));
                sleep(1000);
                join(hidCommand(false, HidCommand.Select));
                join(hidEvent((int) TOUCHPAD_WIDTH, (int) TOUCHPAD_HEIGHT, TouchAction.Click));
            }
        }, blockingExecutor);
    }

    private void touchStart() {
        baseTimestamp = System.nanoTime();
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("_height", TOUCHPAD_HEIGHT);
        content.put("_tFl", 0L);
        content.put("_width", TOUCHPAD_WIDTH);
        exchangeBlocking("_touchStart", content);
    }

    private void touchStop() {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("_i", 1L);
        exchangeBlocking("_touchStop", content);
    }

    // Media control

    /**
     * Sends a media control command.
     *
     * @param command command to send
     * @param args extra command arguments, or {@code null}
     * @return future completing with the response dictionary
     */
    public CompletableFuture<Map<String, Object>> mediaControlCommand(MediaControlCommand command,
            @Nullable Map<String, Object> args) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("_mcc", (long) command.value());
        if (args != null) {
            content.putAll(args);
        }
        return sendCommand("_mcc", content);
    }

    // Text input (RTI)

    private Map<String, Object> textInputStart() {
        Map<String, Object> response = exchangeBlocking("_tiStart", new LinkedHashMap<>());
        Map<String, Object> responseContent = content(response);
        join(dispatch("_tiStart", responseContent == null ? Map.of() : responseContent));
        return response;
    }

    private void textInputStop() {
        exchangeBlocking("_tiStop", new LinkedHashMap<>());
    }

    /**
     * Sends a text input command: the text input session is restarted to obtain up-to-date
     * state, then clear/insert operations are sent as RTI events.
     *
     * @param text text to append (empty string appends nothing)
     * @param clearPreviousInput whether existing text is cleared first
     * @return future completing with the (predicted) current text, or {@code null} when no
     *         keyboard/text input session is active
     */
    public CompletableFuture<@Nullable String> textInputCommand(String text, boolean clearPreviousInput) {
        return CompletableFuture.supplyAsync(() -> {
            join(connect());
            // Restart the text input session so that we have up-to-date data.
            textInputStop();
            Map<String, Object> response = textInputStart();
            Map<String, Object> responseContent = content(response);
            Object tiData = responseContent == null ? null : responseContent.get("_tiD");
            if (!(tiData instanceof byte[] archive)) {
                return null;
            }

            List<@Nullable Object> properties = NsKeyedArchiver.readArchiveProperties(archive, List.of("sessionUUID"),
                    List.of("documentState", "docSt", "contextBeforeInput"));
            byte[] sessionUuid = (byte[]) Objects.requireNonNull(properties.get(0), "missing sessionUUID");
            Object rawCurrentText = properties.get(1);
            String currentText = rawCurrentText == null ? "" : (String) rawCurrentText;

            if (clearPreviousInput) {
                Map<String, Object> content = new LinkedHashMap<>();
                content.put("_tiV", 1L);
                content.put("_tiD", RtiTextPayloads.clearTextPayload(sessionUuid));
                sendEventDirect("_tiC", content);
                currentText = "";
            }

            if (!text.isEmpty()) {
                Map<String, Object> content = new LinkedHashMap<>();
                content.put("_tiV", 1L);
                content.put("_tiD", RtiTextPayloads.inputTextPayload(sessionUuid, text));
                sendEventDirect("_tiC", content);
                currentText += text;
            }

            return currentText;
        }, blockingExecutor);
    }

    // Power state

    /**
     * Fetches the attention state (system status) from the device.
     *
     * @return future completing with the current system status
     */
    public CompletableFuture<SystemStatus> fetchAttentionState() {
        return sendCommand("FetchAttentionState", new LinkedHashMap<>()).thenApply(response -> {
            Map<String, Object> responseContent = content(response);
            if (responseContent == null) {
                throw new ProtocolError("missing content");
            }
            Long state = CompanionProtocol.toLong(responseContent.get("state"));
            if (state == null) {
                throw new ProtocolError("missing state");
            }
            return SystemStatus.fromValue(state.intValue());
        });
    }

    // Helpers

    @SuppressWarnings("unchecked")
    static @Nullable Map<String, Object> content(Map<String, Object> response) {
        Object content = response.get("_c");
        return content instanceof Map ? (Map<String, Object>) content : null;
    }

    static boolean isUrlOrScheme(String value) {
        try {
            return new URI(value).getScheme() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    static <T> T join(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw e;
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProtocolError("interrupted", e);
        }
    }
}
