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
package org.openhab.binding.atv.internal.handler;

import static org.openhab.binding.atv.internal.AtvBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.atv.internal.AtvBindingConstants;
import org.openhab.binding.atv.internal.AtvDynamicCommandDescriptionProvider;
import org.openhab.binding.atv.internal.AtvDynamicStateDescriptionProvider;
import org.openhab.binding.atv.internal.client.AppleTV;
import org.openhab.binding.atv.internal.client.Atv;
import org.openhab.binding.atv.internal.client.DeviceListener;
import org.openhab.binding.atv.internal.client.PairingHandler;
import org.openhab.binding.atv.internal.client.capability.PushListener;
import org.openhab.binding.atv.internal.client.capability.PushUpdater;
import org.openhab.binding.atv.internal.client.capability.Stream;
import org.openhab.binding.atv.internal.client.conf.AtvConfig;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.core.AtvRuntime;
import org.openhab.binding.atv.internal.client.core.FileHostService;
import org.openhab.binding.atv.internal.client.dto.App;
import org.openhab.binding.atv.internal.client.dto.ConnectOptions;
import org.openhab.binding.atv.internal.client.dto.FeatureName;
import org.openhab.binding.atv.internal.client.dto.FeatureState;
import org.openhab.binding.atv.internal.client.dto.InputAction;
import org.openhab.binding.atv.internal.client.dto.PairOptions;
import org.openhab.binding.atv.internal.client.dto.Playing;
import org.openhab.binding.atv.internal.client.dto.PowerState;
import org.openhab.binding.atv.internal.client.dto.Protocol;
import org.openhab.binding.atv.internal.client.dto.RepeatState;
import org.openhab.binding.atv.internal.client.dto.ScanOptions;
import org.openhab.binding.atv.internal.client.dto.ShuffleState;
import org.openhab.binding.atv.internal.client.dto.UserAccount;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;
import org.openhab.binding.atv.internal.client.exceptions.BlockedStateError;
import org.openhab.binding.atv.internal.client.exceptions.CredentialsRejectedError;
import org.openhab.binding.atv.internal.client.exceptions.InvalidCredentialsError;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;
import org.openhab.binding.atv.internal.config.AtvConfiguration;
import org.openhab.core.OpenHAB;
import org.openhab.core.audio.AudioManager;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mdns.MDNSClient;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the connection to a single Apple TV or AirPlay device, mapping the client library onto
 * openHAB channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class AtvHandler extends BaseThingHandler {

    private static final Duration SCAN_TIMEOUT = Duration.ofSeconds(8);
    private static final long OP_TIMEOUT_SECONDS = 30;
    // Delay for an attempt that should happen right away, e.g. to upgrade a partial connection.
    private static final long RECONNECT_NOW_SECONDS = 3;
    // How long an asleep check waits for the device to push its power state when it did not answer the
    // initial query.
    private static final long POWER_REPORT_WAIT_SECONDS = 2;
    // Cadence for extrapolating the playback position channel while media is playing.
    private static final long POSITION_INTERVAL_SECONDS = 5;
    // Window over which repeated refresh requests are merged into one.
    private static final long REFRESH_DEBOUNCE_SECONDS = 2;
    // How long to wait for an HTTP server to start answering before giving up on a stream-url request.
    private static final long HTTP_RESPONSE_TIMEOUT_SECONDS = 10;
    private static final Pattern IPV4_LITERAL = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}");

    private final Logger logger = LoggerFactory.getLogger(AtvHandler.class);
    private final @Nullable FileHostService fileHostService;
    private final AtvDynamicStateDescriptionProvider stateDescriptionProvider;
    private final AtvDynamicCommandDescriptionProvider commandDescriptionProvider;
    private final @Nullable MDNSClient mdnsClient;
    private final HttpClient httpClient;
    // Brings an attempt forward when the device announces itself over mDNS, so a remote power-on is noticed
    // sooner than the next scheduled asleep check.
    private final ServiceListener announcementListener = new AnnouncementListener();

    private volatile AtvConfiguration config = new AtvConfiguration();
    private volatile @Nullable AtvRuntime runtime;
    // Written by connect() and by the protocol threads that report a disconnect. The atomic reference lets
    // handleDisconnect clear only the connection it was told about, not a newer one.
    private final AtomicReference<@Nullable AppleTV> appleTV = new AtomicReference<>();
    private volatile @Nullable ScheduledFuture<?> reconnectJob;
    // Dedicated single thread for the blocking connect, scan and probe work, so a device that becomes
    // unreachable cannot tie up openHAB's shared thing-handler pool and stall other bindings. It also runs
    // connects one at a time. shutdownNow on dispose interrupts a connect that is blocked.
    private volatile @Nullable ScheduledExecutorService connectExecutor;
    private volatile @Nullable ScheduledFuture<?> positionJob;
    private volatile @Nullable ScheduledFuture<?> healthJob;
    private volatile @Nullable ScheduledFuture<?> refreshJob;
    // In-flight audio-sink playback, so a sink stop request can end it.
    private final AtomicReference<@Nullable CompletableFuture<Void>> playback = new AtomicReference<>();
    // Guards the position ticker, which now-playing updates start and stop from several threads.
    private final Object positionLock = new Object();
    private volatile @Nullable String lastArtworkId;
    // Forces the first artwork evaluation after connect, so restored state is cleared even when the id is null.
    private volatile boolean artworkEvaluated;
    // Last position reported by the device and when it arrived, so the ticker can extrapolate instead of
    // asking the device for metadata every second.
    private volatile int lastPositionSeconds;
    private volatile long lastPositionMillis;
    private volatile int lastTotalSeconds;

    // Replaced on every initialize, so nothing carries over from a previous session.
    private volatile ConnectionSupervisor supervisor;
    private volatile boolean disposed;
    // Bumped on every initialize and dispose. A connect or pairing task remembers the value it started
    // with and drops its results if it changed, so a task left over from before a reinitialize cannot
    // touch the new session's state.
    private final AtomicInteger generation = new AtomicInteger();
    // Held only while publishing or clearing the connection, so dispose cannot slip in between a connect
    // checking its generation and storing its connection.
    private final Object connectionLock = new Object();
    // Makes cancelling the pending reconnect and scheduling its replacement one step, so two threads asking
    // for a reconnect at once cannot leave an extra job behind.
    private final Object scheduleLock = new Object();
    // True while connect() runs. mDNS announcements are ignored then, since an attempt is already underway.
    private volatile boolean connecting;
    // Set once the device rejects a stored pairing. Reconnecting cannot help until the user re-pairs, which
    // reinitializes the handler.
    private volatile boolean credentialsRejected;
    // Number of audio streams in progress, so the health check does not reconnect mid-playback.
    private final AtomicInteger activeStreams = new AtomicInteger();
    // Whether the active power probe has ever answered on the current connection. A probe that never works
    // (e.g. some tvOS builds) must not trigger a reconnect loop, only one that stops working.
    private volatile boolean probeSucceeded;

    // pairing state kept alive between showing the PIN and the user entering it
    private volatile @Nullable PairingHandler pendingPairing;
    private volatile @Nullable Protocol pendingProtocol;

    public AtvHandler(Thing thing, @Nullable FileHostService fileHostService,
            AtvDynamicStateDescriptionProvider stateDescriptionProvider,
            AtvDynamicCommandDescriptionProvider commandDescriptionProvider, @Nullable MDNSClient mdnsClient,
            HttpClient httpClient) {
        super(thing);
        this.fileHostService = fileHostService;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.mdnsClient = mdnsClient;
        this.httpClient = httpClient;
        this.supervisor = newSupervisor();
    }

    @Override
    public void initialize() {
        migrateLegacyDeviceId();
        config = getConfigAs(AtvConfiguration.class);
        if (config.deviceId.isBlank()) {
            updateStatus(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "@text/offline.no-identifier");
            return;
        }
        // openHAB reuses the same handler instance across a configuration change (dispose then initialize),
        // so every flag dispose() set has to be cleared here or the handler stays dead.
        generation.incrementAndGet();
        disposed = false;
        connecting = false;
        credentialsRejected = false;
        pendingPairing = null;
        pendingProtocol = null;
        supervisor = newSupervisor();
        activeStreams.set(0);
        probeSucceeded = false;
        artworkEvaluated = false;
        updateStatus(ThingStatus.UNKNOWN);
        // The client's heartbeats and timers run on a binding-private pool, not openHAB's shared
        // thing-handler pool, so nothing the client schedules can starve other bindings.
        runtime = new AtvRuntime(ThreadPoolManager.getScheduledPool(BINDING_ID), Clock.systemUTC(), fileHostService);
        connectExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "atv-connect-" + getThing().getUID().getId());
            thread.setDaemon(true);
            return thread;
        });
        MDNSClient mdns = mdnsClient;
        if (mdns != null) {
            mdns.addServiceListener(MDNS_AIRPLAY, announcementListener);
        }
        submitConnect();
    }

    @Override
    public void dispose() {
        synchronized (connectionLock) {
            disposed = true;
            generation.incrementAndGet();
        }
        MDNSClient mdns = mdnsClient;
        if (mdns != null) {
            mdns.removeServiceListener(MDNS_AIRPLAY, announcementListener);
        }
        cancel(reconnectJob);
        reconnectJob = null;
        cancel(refreshJob);
        refreshJob = null;
        stopPositionTicker();
        cancel(healthJob);
        healthJob = null;
        PairingHandler pairing;
        AppleTV atv;
        synchronized (connectionLock) {
            pairing = pendingPairing;
            pendingPairing = null;
            pendingProtocol = null;
            atv = appleTV.getAndSet(null);
        }
        if (pairing != null) {
            pairing.close();
        }
        if (atv != null) {
            atv.close();
        }
        ScheduledExecutorService executor = connectExecutor;
        connectExecutor = null;
        if (executor != null) {
            // shutdownNow interrupts a connect that is blocked on an unreachable device so it exits promptly.
            // Not awaited: a connect stuck in a socket call ignores the interrupt, and the generation check
            // already stops it from touching the next session once it returns.
            executor.shutdownNow();
        }
        clearDynamicOptions();
        lastArtworkId = null;
        // the runtime uses a shared binding-managed pool, so nothing to shut down here
        runtime = null;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        PairingHandler pairing = pendingPairing;
        if (pairing != null && pendingProtocol != null) {
            Object pin = configurationParameters.get(CONFIG_PIN);
            if (pin instanceof String pinValue && !pinValue.isBlank()) {
                // Store everything else the user submitted alongside the PIN. Delegating to super here
                // would dispose the handler and throw away the pairing in progress, so the parameters are
                // applied directly instead.
                storeConfiguration(updated -> configurationParameters.forEach((key, value) -> {
                    if (!CONFIG_PIN.equals(key)) {
                        updated.put(key, value);
                    }
                }));
                // Finishing pairing waits on the network, so it runs on the dedicated executor rather than
                // the shared thread that delivers this configuration update.
                ScheduledExecutorService executor = connectExecutor;
                if (executor != null) {
                    int gen = generation.get();
                    try {
                        executor.execute(() -> completePairing(gen, pairing, pinValue));
                    } catch (RejectedExecutionException e) {
                        // Disposed meanwhile, which also closed the pairing.
                    }
                }
                return;
            }
        }
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        AppleTV atv = appleTV.get();
        if (atv == null) {
            // A power ON while offline is a wake request. Reconnect and send it once connected.
            if (CHANNEL_POWER.equals(channelUID.getIdWithoutGroup()) && command == OnOffType.ON) {
                requestWake();
            }
            return;
        }
        if (command instanceof RefreshType) {
            refreshAll();
            return;
        }
        String id = channelUID.getIdWithoutGroup();
        try {
            dispatchCommand(atv, id, command);
        } catch (RuntimeException e) {
            // Bad input (e.g. an unknown shuffle mode) or a command the device cannot take right now (mid
            // reconnect, or unsupported on a speaker). Not worth an error in the log.
            logger.debug("Command {} for channel {} failed: {}", command, id, e.getMessage());
        }
    }

    private void dispatchCommand(AppleTV atv, String id, Command command) {
        String value = command.toString();
        switch (id) {
            case CHANNEL_POWER -> {
                if (command instanceof OnOffType onOff) {
                    if (onOff == OnOffType.ON) {
                        atv.power().turnOn();
                    } else {
                        atv.power().turnOff();
                    }
                }
            }
            case CHANNEL_REMOTE_KEY -> sendRemoteKey(atv, value);
            case CHANNEL_MEDIA_CONTROL -> handleMediaControl(atv, command);
            case CHANNEL_VOLUME -> {
                if (command instanceof PercentType percent) {
                    atv.audio().setVolume(percent.doubleValue());
                } else if (command instanceof OnOffType onOff) {
                    atv.audio().setVolume(onOff == OnOffType.ON ? 100 : 0);
                }
            }
            case CHANNEL_APP -> atv.apps().launchApp(value);
            case CHANNEL_ACCOUNT -> atv.userAccounts().switchAccount(value);
            case CHANNEL_KEYBOARD_INPUT -> atv.keyboard().textSet(value);
            case CHANNEL_SHUFFLE -> atv.remoteControl().setShuffle(ShuffleState.valueOf(value));
            case CHANNEL_REPEAT -> atv.remoteControl().setRepeat(RepeatState.valueOf(value));
            case CHANNEL_POSITION -> {
                if (command instanceof QuantityType<?> quantity) {
                    atv.remoteControl().setPosition(Duration.ofSeconds(quantity.longValue()));
                }
            }
            case CHANNEL_PLAY_URL -> resolveMediaSource(value).ifPresent(source -> atv.stream().playUrl(source));
            case CHANNEL_STREAM_URL -> resolveMediaSource(value).ifPresent(source -> streamUrl(atv, source));
            case CHANNEL_TOUCH_GESTURE -> handleTouchGesture(atv, value);
            case CHANNEL_OUTPUT_DEVICES -> atv.audio().setOutputDevices(List.of(value.split(",")));
            case CHANNEL_OUTPUT_DEVICE_VOLUME -> handleOutputDeviceVolume(atv, value);
            default -> logger.debug("Unhandled command {} for channel {}", command, id);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        AppleTV atv = appleTV.get();
        if (atv == null) {
            return;
        }
        // Refresh the app/account option lists so they appear when the item is linked after connect.
        String id = channelUID.getIdWithoutGroup();
        if (CHANNEL_APP.equals(id) || CHANNEL_ACCOUNT.equals(id)) {
            refreshDynamicOptions(atv);
        }
        // Populate the newly linked item with current state instead of waiting for the next update. Items
        // are linked in bursts at startup, so coalesce them into a single refresh of all channels.
        scheduleRefresh();
    }

    /**
     * Streams an audio stream to the device via RAOP at the given volume, blocking until the device has
     * finished playing it.
     *
     * @param stream the audio stream to play
     * @param volumePercent the playback volume in percent (0-100)
     */
    public void streamAudio(InputStream stream, double volumePercent) {
        AppleTV atv = appleTV.get();
        if (atv == null) {
            logger.debug("Cannot stream audio to {}: not connected", config.deviceId);
            return;
        }
        activeStreams.incrementAndGet();
        try {
            CompletableFuture<Void> future = atv.stream().streamFile(stream, null,
                    Map.<String, Object> of(Stream.OPTION_VOLUME, volumePercent));
            playback.set(future);
            // No timeout: RAOP paces the audio in real time, so this call lasts as long as the clip, and a
            // sink stream can be an endless radio stream. A timeout would cut playback short.
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            logger.debug("Failed to stream audio to {}", config.deviceId, e);
        } catch (BlockedStateError e) {
            logger.debug("Cannot stream audio to {}: connection closed", config.deviceId);
        } finally {
            playback.set(null);
            activeStreams.decrementAndGet();
        }
    }

    /**
     * Accepts an http(s) URL as is, or a bare file name from openHAB's sounds folder, which it turns into
     * a path. Anything else is rejected so these channels cannot be used to read arbitrary files.
     */
    Optional<String> resolveMediaSource(String value) {
        String source = value.trim();
        String lower = source.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return Optional.of(source);
        }
        Path soundsFolder = Path.of(OpenHAB.getConfigFolder(), AudioManager.SOUND_DIR).toAbsolutePath().normalize();
        if (!source.isEmpty() && !source.contains("/") && !source.contains("\\") && !".".equals(source)
                && !"..".equals(source)) {
            Path file = soundsFolder.resolve(source).normalize();
            if (soundsFolder.equals(file.getParent()) && Files.isRegularFile(file)) {
                return Optional.of(file.toString());
            }
        }
        logger.debug("Ignoring media source '{}': expected an http(s) URL or a file name in {}", source, soundsFolder);
        return Optional.empty();
    }

    /** Streams an http(s) URL or a local file to the device over RAOP without blocking the caller. */
    private void streamUrl(AppleTV atv, String source) {
        if (!source.regionMatches(true, 0, "http", 0, 4)) {
            trackStream(atv.stream().streamFile(source));
            return;
        }
        // Opening the HTTP stream waits for the server, so keep it off the thread delivering the command.
        scheduler.execute(() -> {
            InputStreamResponseListener listener = new InputStreamResponseListener();
            Request request = httpClient.newRequest(source);
            request.send(listener);
            try {
                Response response = listener.get(HTTP_RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (response.getStatus() != HttpStatus.OK_200) {
                    logger.debug("Cannot stream {}: HTTP {}", source, response.getStatus());
                    response.abort(new IOException("unexpected status " + response.getStatus()));
                    return;
                }
                InputStream in = listener.getInputStream();
                trackStream(atv.stream().streamFile(in, null, Map.of())).whenComplete((result, error) -> {
                    try {
                        in.close();
                    } catch (IOException e) {
                        logger.trace("Closing stream for {} failed", source, e);
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException | TimeoutException | RuntimeException e) {
                logger.debug("Cannot stream {}: {}", source, e.getMessage());
                request.abort(e);
            }
        });
    }

    /** Counts a stream as active until it finishes, so the health check leaves the connection alone meanwhile. */
    private CompletableFuture<Void> trackStream(CompletableFuture<Void> future) {
        activeStreams.incrementAndGet();
        return future.whenComplete((result, error) -> {
            activeStreams.decrementAndGet();
            if (error != null) {
                logger.debug("Streaming to {} failed: {}", config.deviceId, error.getMessage());
            }
        });
    }

    /**
     * Stops audio the sink is currently playing. Does nothing when the sink is idle, so a stop request
     * never interrupts what the user is watching on the device.
     */
    public void stopAudio() {
        AppleTV atv = appleTV.get();
        if (atv == null || playback.get() == null) {
            return;
        }
        try {
            // RAOP holds the remote-control takeover while streaming, so this reaches the RAOP session.
            atv.remoteControl().stop();
        } catch (RuntimeException e) {
            logger.debug("Failed to stop audio", e);
        }
    }

    /**
     * Returns the configured default audio-sink volume in percent (0-100).
     *
     * @return the default audio-sink volume
     */
    public int getNotificationVolume() {
        return Math.max(0, Math.min(100, config.notificationVolume));
    }

    /** Runs only on connectExecutor, one attempt at a time. */
    private void connect(int gen) {
        // Nothing to do while waiting for a PIN. Completing or failing the pairing reconnects.
        if (isStale(gen) || pendingPairing != null) {
            return;
        }
        // A job left over from two reconnect requests arriving together. Only a partial connection is worth
        // replacing.
        if (appleTV.get() != null && !isDegradedConnection()) {
            return;
        }
        connecting = true;
        // A retry queued before this attempt started is now redundant. Anything that asks for another
        // attempt while this one runs schedules a fresh job.
        synchronized (scheduleLock) {
            cancel(reconnectJob);
        }
        // Discard any prior connection first so a reconnect does not leak its protocols and heartbeats.
        dropConnection();
        try {
            List<AtvConfig> found = scanForDevice();
            if (isStale(gen)) {
                return;
            }
            AtvConfig device = ownDevice(found);
            if (device == null) {
                retry(gen, supervisor.onFailed(Reachability.NO_ANSWER), notFoundMessage(found));
                return;
            }
            updateDeviceProperties(device);
            applyStoredCredentials(device);

            Protocol needsPairing = protocolNeedingPairing(device);
            if (needsPairing != null) {
                beginPairing(gen, device, needsPairing);
                return;
            }

            ConnectionSupervisor.Plan plan = supervisor.onFound(device.deepSleep());
            if (plan == ConnectionSupervisor.Plan.CHECK) {
                plan = checkAsleep(device);
                if (isStale(gen)) {
                    return;
                }
            }
            if (plan == ConnectionSupervisor.Plan.WAIT) {
                retry(gen, supervisor.waitRetry(), "@text/offline.no-response");
                return;
            }

            AppleTV atv = await(Atv.connect(device, new ConnectOptions(runtime, null, null)), AppleTV::close);
            synchronized (connectionLock) {
                if (isStale(gen)) {
                    // Disposed or reinitialized while this connect was in flight. Close the late connection
                    // rather than leaking it and its heartbeats.
                    atv.close();
                    return;
                }
                appleTV.set(atv);
            }
            supervisor.onConnected(powerOf(atv));
            probeSucceeded = false;
            registerListeners(atv);
            if (isStale(gen)) {
                return;
            }
            Protocol rejected = rejectedProtocol(atv);
            if (rejected != null) {
                // Connected on the other protocols, but one pairing was removed on the device.
                logger.debug("{} credentials for {} were rejected; clear them to pair again",
                        credentialProtocol(rejected), config.deviceId);
                updateStatus(ThingStatus.ONLINE,
                        "@text/online.credentials-rejected [\"" + credentialProtocol(rejected) + "\"]");
            } else if (isDegradedConnection()) {
                // Only part of the protocols came up, so metadata, push updates and remote control are
                // missing. A wake upgrades the connection.
                updateStatus(ThingStatus.ONLINE, "@text/online.partial");
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
            clearPins();
            refreshAll();
            refreshDynamicOptions(atv);
            startHealthCheck(gen);
            if (supervisor.consumeWake()) {
                wake(atv);
            }
        } catch (InterruptedException e) {
            // Only dispose interrupts this thread, so there is nothing left to report or retry.
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            if (isStale(gen)) {
                return;
            }
            if (e.getCause() instanceof CredentialsRejectedError rejected) {
                // Retrying cannot fix a removed pairing, so stop here until the user re-pairs.
                credentialsRejected(rejected.protocol());
                return;
            }
            logger.debug("Connection to {} failed", config.deviceId, e);
            retry(gen, supervisor.onFailed(Reachability.of(e)), failureMessage(e));
        } catch (BlockedStateError e) {
            if (isStale(gen)) {
                return;
            }
            // The connection dropped while it was still being set up (common right after a wake, while the
            // device settles). Treat it as a transient disconnect and retry.
            logger.debug("Connection to {} closed during setup; reconnecting", config.deviceId);
            dropConnection();
            retry(gen, supervisor.onFailed(Reachability.HOST_UP), "@text/offline.connect-failed");
        } catch (RuntimeException e) {
            // Without this the executor would swallow the exception and no retry would ever be scheduled.
            if (isStale(gen)) {
                return;
            }
            logger.debug("Connection to {} failed unexpectedly", config.deviceId, e);
            dropConnection();
            retry(gen, supervisor.onFailed(Reachability.of(e)), failureMessage(e));
        } finally {
            if (!isStale(gen)) {
                connecting = false;
            }
        }
    }

    /**
     * Reports a pairing the device no longer accepts. The stored credentials are left alone: clearing them
     * would start a new pairing, which pops a PIN up on the TV without the user asking for it.
     */
    private void credentialsRejected(Protocol protocol) {
        credentialsRejected = true;
        Protocol stored = credentialProtocol(protocol);
        logger.debug("{} credentials for {} were rejected; clear them to pair again", stored, config.deviceId);
        updateStatus(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                "@text/offline.credentials-rejected [\"" + stored + "\"]");
    }

    private @Nullable Protocol rejectedProtocol(AppleTV atv) {
        return atv.connectFailures().entrySet().stream()
                .filter(e -> e.getValue() instanceof AuthenticationError
                        || e.getValue() instanceof InvalidCredentialsError)
                .map(Map.Entry::getKey).findFirst().orElse(null);
    }

    /** MRP is tunneled over AirPlay on current devices, so its credentials are the AirPlay ones. */
    private Protocol credentialProtocol(Protocol protocol) {
        return protocol == Protocol.MRP ? Protocol.AirPlay : protocol;
    }

    private boolean isStale(int gen) {
        return disposed || gen != generation.get();
    }

    private List<AtvConfig> scanForDevice() throws InterruptedException, ExecutionException, TimeoutException {
        // Knock only while a wake is pending: it exists to pull a device out of sleep, so knocking on every
        // routine reconnect would keep waking a device the user deliberately turned off.
        ScanOptions options = ScanOptions.defaults().withTimeout(SCAN_TIMEOUT).withRuntime(runtime)
                .withKnock(supervisor.isWaking());
        MDNSClient mdns = mdnsClient;
        if (config.host.isBlank()) {
            if (mdns == null) {
                return List.of();
            }
            options = options.withIdentifiers(config.deviceId).withBrowser(mdns::list);
        } else {
            options = options.withHosts(List.of(config.host));
        }
        return await(Atv.scan(options));
    }

    @Nullable
    AtvConfig ownDevice(List<AtvConfig> found) {
        String id = config.deviceId.toLowerCase(Locale.ROOT);
        // Only ever accept this Thing's own device. Whatever else answers at the configured address (e.g.
        // after a DHCP change) must not receive this Thing's credentials or pairing requests.
        return found.stream()
                .filter(c -> c.allIdentifiers().stream().anyMatch(other -> other.toLowerCase(Locale.ROOT).equals(id)))
                .findFirst().orElse(null);
    }

    /**
     * Names the device found at the configured host when it is not this Thing's device. A hand-entered
     * hardware MAC is the usual cause, since devices identify themselves by their AirPlay ID instead.
     */
    String notFoundMessage(List<AtvConfig> found) {
        if (config.host.isBlank() || found.isEmpty()) {
            return "@text/offline.not-found";
        }
        AtvConfig other = found.get(0);
        String id = other.getService(Protocol.AirPlay).flatMap(BaseService::identifier).or(other::identifier)
                .orElse("?");
        return "@text/offline.wrong-device [\"" + id + "\", \"" + config.host + "\", \"" + config.deviceId + "\"]";
    }

    private void applyStoredCredentials(AtvConfig device) {
        if (!config.password.isBlank()) {
            // Password-protected AirPlay speakers: RAOP reads it when setting up the audio session.
            device.getService(Protocol.AirPlay).ifPresent(service -> service.setPassword(config.password));
            device.getService(Protocol.RAOP).ifPresent(service -> service.setPassword(config.password));
        }
        if (!config.airplayCredentials.isBlank()) {
            device.setCredentials(Protocol.AirPlay, config.airplayCredentials);
        }
        if (!config.companionCredentials.isBlank()) {
            device.setCredentials(Protocol.Companion, config.companionCredentials);
        }
        if (!config.raopCredentials.isBlank()) {
            device.setCredentials(Protocol.RAOP, config.raopCredentials);
        } else if (!config.airplayCredentials.isBlank()) {
            // AirPlay 2 devices (e.g. Apple TV) authorize RAOP audio with the AirPlay credentials
            device.setCredentials(Protocol.RAOP, config.airplayCredentials);
        }
    }

    /** Returns the first relevant protocol that requires pairing but has no stored credentials. */
    private @Nullable Protocol protocolNeedingPairing(AtvConfig device) {
        List<Protocol> relevant = THING_TYPE_SPEAKER.equals(thing.getThingTypeUID())
                ? List.of(Protocol.AirPlay, Protocol.RAOP)
                : List.of(Protocol.AirPlay, Protocol.Companion);
        for (Protocol protocol : relevant) {
            Optional<BaseService> service = device.getService(protocol);
            if (service.isPresent()
                    && service.get()
                            .pairing() == org.openhab.binding.atv.internal.client.dto.PairingRequirement.Mandatory
                    && credentialFor(protocol).isBlank()) {
                return protocol;
            }
        }
        return null;
    }

    private void beginPairing(int gen, AtvConfig device, Protocol protocol)
            throws InterruptedException, ExecutionException, TimeoutException {
        PairingHandler pairing = await(Atv.pair(device, protocol, new PairOptions(runtime, null, pairingOptions())),
                PairingHandler::close);
        try {
            await(pairing.begin());
        } catch (Exception e) {
            pairing.close();
            throw e;
        }
        PairingHandler previous;
        synchronized (connectionLock) {
            // Checked and stored together so dispose either sees this pairing and closes it, or this sees
            // the dispose and closes it here.
            if (isStale(gen)) {
                previous = pairing;
            } else {
                previous = pendingPairing;
                pendingPairing = pairing;
                pendingProtocol = protocol;
            }
        }
        if (previous != null) {
            previous.close();
        }
        if (!isStale(gen)) {
            updateStatus(ThingStatusDetail.OFFLINE.AUTHORIZATION_PENDING,
                    "@text/offline.pairing [\"" + protocol + "\"]");
        }
    }

    private void completePairing(int gen, PairingHandler pairing, String pin) {
        Protocol protocol = pendingProtocol;
        try {
            pairing.pin(pin);
            await(pairing.finish());
            String credentials = Objects.requireNonNullElse(pairing.service().credentials().orElse(""), "");
            pairing.close();
            if (isStale(gen)) {
                return;
            }
            pendingPairing = null;
            pendingProtocol = null;
            storeConfiguration(updated -> {
                // Clear the shared PIN field so the next pairing step starts blank.
                updated.put(CONFIG_PIN, "");
                if (protocol != null && !credentials.isBlank()) {
                    updated.put(credentialKey(protocol), credentials);
                }
            });
            // reconnect, which pairs the next protocol if needed
            submitConnect();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pairing.close();
        } catch (ExecutionException | TimeoutException | RuntimeException e) {
            if (isStale(gen)) {
                pairing.close();
                return;
            }
            pairingFailed(pairing, protocol, e);
        }
    }

    /**
     * Handles a failed pairing step: clears the PIN field and re-arms pairing so the device shows a fresh
     * PIN for another attempt.
     */
    private void pairingFailed(PairingHandler pairing, @Nullable Protocol protocol, Exception cause) {
        logger.debug("Pairing {} failed", protocol, cause);
        pairing.close();
        pendingPairing = null;
        pendingProtocol = null;
        storeConfiguration(updated -> updated.put(CONFIG_PIN, ""));
        updateStatus(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                "@text/offline.pairing-failed [\"" + protocol + "\"]");
        submitConnect();
    }

    private void registerListeners(AppleTV atv) {
        atv.addListener(new DeviceListener() {
            @Override
            public void connectionLost(@Nullable Exception exception) {
                handleDisconnect(atv, exception);
            }

            @Override
            public void connectionClosed() {
                // A graceful close (e.g. the device tearing down its side on sleep) is still a disconnect,
                // so recover from it like any other.
                handleDisconnect(atv, null);
            }
        });

        PushUpdater push = atv.pushUpdater();
        push.addListener(new PushListener() {
            @Override
            @NonNullByDefault({})
            public void playstatusUpdate(PushUpdater updater, Playing playstatus) {
                updatePlaying(playstatus);
            }

            @Override
            @NonNullByDefault({})
            public void playstatusError(PushUpdater updater, Exception exception) {
                logger.debug("Push update error", exception);
            }
        });
        push.start();

        atv.power().addListener((oldState, newState) -> {
            supervisor.onPower(newState);
            updateState(CHANNEL_POWER, powerToState(newState));
            // Woke on a partial (Companion-only) connection. Reconnect to restore the other protocols.
            if (newState == PowerState.On && oldState != PowerState.On && isDegradedConnection()) {
                logger.debug("Device {} woke with a partial connection; reconnecting to restore all protocols",
                        config.deviceId);
                scheduleConnect(generation.get(), RECONNECT_NOW_SECONDS);
            }
        });
        atv.audio().addListener(new org.openhab.binding.atv.internal.client.capability.AudioListener() {
            @Override
            public void volumeUpdate(double oldLevel, double newLevel) {
                updateState(CHANNEL_VOLUME, new PercentType((int) Math.round(newLevel)));
            }

            @Override
            @NonNullByDefault({})
            public void outputDevicesUpdate(List<org.openhab.binding.atv.internal.client.dto.OutputDevice> oldDevices,
                    List<org.openhab.binding.atv.internal.client.dto.OutputDevice> newDevices) {
                updateState(CHANNEL_OUTPUT_DEVICES, new StringType(
                        newDevices.stream().map(d -> d.identifier()).reduce((a, b) -> a + "," + b).orElse("")));
            }
        });
        atv.keyboard().addListener((oldState, newState) -> {
            updateState(CHANNEL_KEYBOARD_FOCUS, new StringType(newState.name()));
        });
    }

    /** Coalesces a burst of refresh requests (e.g. items linking at startup) into a single refresh. */
    private void scheduleRefresh() {
        cancel(refreshJob);
        refreshJob = scheduler.schedule(this::refreshAll, REFRESH_DEBOUNCE_SECONDS, TimeUnit.SECONDS);
    }

    private void refreshAll() {
        AppleTV atv = appleTV.get();
        if (atv == null) {
            return;
        }
        atv.metadata().playing().thenAccept(this::updatePlaying);
        // Speakers have no power management, and powerState() throws NotSupportedError there.
        if (!atv.features().inState(FeatureState.Unsupported, FeatureName.PowerState)) {
            PowerState power = atv.power().powerState();
            supervisor.onPower(power);
            updateState(CHANNEL_POWER, powerToState(power));
        }
        if (atv.features().inState(FeatureState.Available, FeatureName.Volume)) {
            updateState(CHANNEL_VOLUME, new PercentType((int) Math.round(atv.audio().volume())));
        }
    }

    private void updatePlaying(Playing p) {
        updateState(CHANNEL_TITLE, stringOrUndef(p.title()));
        updateState(CHANNEL_ARTIST, stringOrUndef(p.artist()));
        updateState(CHANNEL_ALBUM, stringOrUndef(p.album()));
        updateState(CHANNEL_GENRE, stringOrUndef(p.genre()));
        updateState(CHANNEL_MEDIA_TYPE, new StringType(p.mediaType().name()));
        updateState(CHANNEL_PLAYBACK_STATE, new StringType(p.deviceState().name()));
        updateState(CHANNEL_SERIES_NAME, stringOrUndef(p.seriesName()));
        updateState(CHANNEL_SEASON_NUMBER, decimalOrUndef(p.seasonNumber()));
        updateState(CHANNEL_EPISODE_NUMBER, decimalOrUndef(p.episodeNumber()));
        updateState(CHANNEL_CONTENT_ID, stringOrUndef(p.contentIdentifier()));
        Optional<Long> itunesId = p.iTunesStoreIdentifier();
        updateState(CHANNEL_ITUNES_ID, itunesId.isPresent() ? new DecimalType(itunesId.get()) : UnDefType.UNDEF);
        Optional<ShuffleState> shuffle = p.shuffle();
        updateState(CHANNEL_SHUFFLE, shuffle.isPresent() ? new StringType(shuffle.get().name()) : UnDefType.UNDEF);
        Optional<RepeatState> repeat = p.repeat();
        updateState(CHANNEL_REPEAT, repeat.isPresent() ? new StringType(repeat.get().name()) : UnDefType.UNDEF);

        Optional<Integer> total = p.totalTime();
        updateState(CHANNEL_DURATION,
                total.isPresent() ? new QuantityType<>(total.get(), Units.SECOND) : UnDefType.UNDEF);
        updatePositionChannels(p);

        switch (p.deviceState()) {
            case Playing -> {
                updateState(CHANNEL_MEDIA_CONTROL, PlayPauseType.PLAY);
                startPositionTicker();
            }
            case Paused, Stopped, Idle -> {
                updateState(CHANNEL_MEDIA_CONTROL, PlayPauseType.PAUSE);
                stopPositionTicker();
            }
            default -> stopPositionTicker();
        }

        AppleTV atv = appleTV.get();
        // Not-unsupported rather than available, so an idle Apple TV clears the channel. Speakers are skipped.
        if (atv != null && !atv.features().inState(FeatureState.Unsupported, FeatureName.App)) {
            atv.metadata().app().ifPresentOrElse(app -> {
                updateState(CHANNEL_APP, new StringType(app.identifier()));
                updateState(CHANNEL_APP_NAME, new StringType(app.name()));
            }, () -> {
                updateState(CHANNEL_APP, UnDefType.UNDEF);
                updateState(CHANNEL_APP_NAME, UnDefType.UNDEF);
            });
        }

        updateArtwork();
    }

    private void updatePositionChannels(Playing p) {
        Optional<Integer> position = p.position();
        Optional<Integer> total = p.totalTime();
        // Remember the reported position so the ticker can extrapolate from it between updates.
        lastPositionSeconds = position.orElse(-1);
        lastTotalSeconds = total.orElse(0);
        lastPositionMillis = System.currentTimeMillis();
        publishPosition(lastPositionSeconds, lastTotalSeconds);
    }

    private void publishPosition(int positionSeconds, int totalSeconds) {
        if (positionSeconds < 0) {
            updateState(CHANNEL_POSITION, UnDefType.UNDEF);
            updateState(CHANNEL_PROGRESS, UnDefType.UNDEF);
            return;
        }
        updateState(CHANNEL_POSITION, new QuantityType<>(positionSeconds, Units.SECOND));
        if (totalSeconds > 0) {
            updateState(CHANNEL_PROGRESS,
                    new PercentType((int) Math.min(100, Math.round(100.0 * positionSeconds / totalSeconds))));
        } else {
            updateState(CHANNEL_PROGRESS, UnDefType.UNDEF);
        }
    }

    private void sendRemoteKey(AppleTV atv, String key) {
        var rc = atv.remoteControl();
        switch (key.toLowerCase(Locale.ROOT)) {
            case "up" -> rc.up();
            case "down" -> rc.down();
            case "left" -> rc.left();
            case "right" -> rc.right();
            case "select" -> rc.select();
            case "menu" -> rc.menu();
            case "home" -> rc.home();
            case "homehold" -> rc.home(InputAction.Hold);
            case "topmenu" -> rc.topMenu();
            case "play" -> rc.play();
            case "playpause" -> rc.playPause();
            case "pause" -> rc.pause();
            case "stop" -> rc.stop();
            case "next" -> rc.next();
            case "previous" -> rc.previous();
            case "skipforward" -> rc.skipForward();
            case "skipbackward" -> rc.skipBackward();
            case "channelup" -> rc.channelUp();
            case "channeldown" -> rc.channelDown();
            case "screensaver" -> rc.screensaver();
            case "guide" -> rc.guide();
            case "controlcenter" -> rc.controlCenter();
            default -> logger.debug("Unknown remote key {}", key);
        }
    }

    private void handleMediaControl(AppleTV atv, Command command) {
        var rc = atv.remoteControl();
        if (command instanceof PlayPauseType playPause) {
            if (playPause == PlayPauseType.PLAY) {
                rc.play();
            } else {
                rc.pause();
            }
        } else if (command instanceof NextPreviousType nextPrevious) {
            if (nextPrevious == NextPreviousType.NEXT) {
                rc.next();
            } else {
                rc.previous();
            }
        } else if (command instanceof RewindFastforwardType rewindFf) {
            if (rewindFf == RewindFastforwardType.FASTFORWARD) {
                rc.skipForward();
            } else {
                rc.skipBackward();
            }
        }
    }

    private void handleTouchGesture(AppleTV atv, String value) {
        String[] parts = value.split(":", 2);
        String kind = parts[0].trim();
        String[] args = parts.length > 1 ? parts[1].split(",") : new String[0];
        try {
            switch (kind) {
                case "click" -> atv.touch().click(InputAction.SingleTap);
                case "swipe" -> atv.touch().swipe(Integer.parseInt(args[0].trim()), Integer.parseInt(args[1].trim()),
                        Integer.parseInt(args[2].trim()), Integer.parseInt(args[3].trim()),
                        Integer.parseInt(args[4].trim()));
                case "action" -> atv.touch().action(Integer.parseInt(args[0].trim()), Integer.parseInt(args[1].trim()),
                        org.openhab.binding.atv.internal.client.dto.TouchAction.valueOf(args[2].trim()));
                default -> logger.debug("Unknown touch gesture {}", value);
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            logger.debug("Invalid touch gesture {}", value, e);
        }
    }

    private void handleOutputDeviceVolume(AppleTV atv, String value) {
        String[] parts = value.split("=", 2);
        if (parts.length != 2) {
            logger.debug("Invalid output device volume {}", value);
            return;
        }
        try {
            double level = Double.parseDouble(parts[1].trim());
            atv.audio().outputDevices().stream().filter(d -> d.identifier().equals(parts[0].trim())).findFirst()
                    .ifPresent(device -> atv.audio().setVolume(level, device));
        } catch (NumberFormatException e) {
            logger.debug("Invalid output device volume {}", value, e);
        }
    }

    private void startHealthCheck(int gen) {
        cancel(healthJob);
        if (config.healthCheckInterval > 0) {
            ScheduledFuture<?> job = scheduler.scheduleWithFixedDelay(this::runHealthCheck, config.healthCheckInterval,
                    config.healthCheckInterval, TimeUnit.SECONDS);
            healthJob = job;
            // dispose() may have cancelled the previous job just before this one was stored.
            if (isStale(gen)) {
                cancel(job);
            }
        }
    }

    private void runHealthCheck() {
        AppleTV atv = appleTV.get();
        if (atv == null || activeStreams.get() > 0) {
            return;
        }
        PowerState cached;
        CompletableFuture<PowerState> probe;
        try {
            cached = atv.power().powerState();
            probe = atv.power().refreshPowerState();
        } catch (NotSupportedError e) {
            disableHealthCheck();
            return;
        } catch (BlockedStateError e) {
            // The connection is closing, so a reconnect is already on its way.
            return;
        }
        probe.whenComplete((fetched, error) -> {
            if (error != null || fetched == null) {
                // The relay reports an unsupported probe as a failed future rather than throwing, so the
                // "no active power query" case has to be recognised here as well.
                if (unwrap(error) instanceof NotSupportedError) {
                    disableHealthCheck();
                    return;
                }
                // Only treat a failure as a stall if the probe worked before. Some tvOS builds never answer
                // it, and reconnecting on every such failure would loop.
                if (probeSucceeded) {
                    // Handled as a lost connection, so a device that reported Off before going quiet is
                    // treated as asleep rather than failed.
                    logger.debug("Power probe stopped responding on {}; dropping the connection", config.deviceId);
                    handleDisconnect(atv, null);
                    atv.close();
                }
                return;
            }
            probeSucceeded = true;
            if (fetched != cached) {
                // The poll itself updates the tracked state, so the device's answer is already in effect.
                logger.debug("Power poll on {} read {} while {} was tracked", config.deviceId, fetched, cached);
            }
        });
    }

    /** Stops the health check on a device that offers no active power query (e.g. a speaker). */
    private void disableHealthCheck() {
        logger.debug("Active power probe unsupported on {}; disabling health check", config.deviceId);
        cancel(healthJob);
        healthJob = null;
    }

    private @Nullable Throwable unwrap(@Nullable Throwable error) {
        return error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
    }

    /**
     * Starts the position ticker if it is not already running. Synchronized because now-playing updates
     * arrive concurrently from the push listener, the metadata refresh and channel linking, and two of them
     * passing the check together would leave an orphaned job ticking for the life of the runtime.
     */
    private void startPositionTicker() {
        synchronized (positionLock) {
            ScheduledFuture<?> job = positionJob;
            if (disposed || (job != null && !job.isCancelled())) {
                return;
            }
            positionJob = scheduler.scheduleWithFixedDelay(this::tickPosition, POSITION_INTERVAL_SECONDS,
                    POSITION_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void stopPositionTicker() {
        synchronized (positionLock) {
            cancel(positionJob);
            positionJob = null;
        }
    }

    /** Extrapolates the playback position from the last update rather than polling the device each second. */
    private void tickPosition() {
        if (appleTV.get() == null || disposed) {
            stopPositionTicker();
            return;
        }
        int reported = lastPositionSeconds;
        if (reported < 0) {
            return;
        }
        long elapsed = (System.currentTimeMillis() - lastPositionMillis) / 1000L;
        int total = lastTotalSeconds;
        int position = (int) (reported + elapsed);
        publishPosition(total > 0 ? Math.min(position, total) : position, total);
    }

    private void updateArtwork() {
        AppleTV atv = appleTV.get();
        if (atv == null || !isLinked(CHANNEL_ARTWORK)) {
            return;
        }
        // Skip the (potentially large) fetch when the artwork has not changed since the last update.
        try {
            @Nullable
            String artworkId = atv.metadata().artworkId();
            if (artworkEvaluated && Objects.equals(artworkId, lastArtworkId)) {
                return;
            }
            artworkEvaluated = true;
            lastArtworkId = artworkId;
            if (artworkId == null) {
                // No artwork, so clear any stale image.
                updateState(CHANNEL_ARTWORK, UnDefType.UNDEF);
                return;
            }
        } catch (RuntimeException e) {
            // This protocol has no artwork identifier, so always fetch.
            logger.trace("Artwork identifier unavailable, fetching unconditionally", e);
        }
        atv.metadata().artwork().thenAccept(artwork -> {
            if (artwork != null && artwork.bytes().length > 0) {
                updateState(CHANNEL_ARTWORK,
                        new org.openhab.core.library.types.RawType(artwork.bytes(), artwork.mimetype()));
            } else {
                updateState(CHANNEL_ARTWORK, UnDefType.UNDEF);
            }
        }).exceptionally(e -> {
            // reset so the next now-playing update retries the fetch
            lastArtworkId = null;
            return null;
        });
    }

    private void refreshDynamicOptions(AppleTV atv) {
        if (isLinked(CHANNEL_APP) && atv.features().inState(
                List.of(FeatureState.Available, FeatureState.Unknown, FeatureState.Unavailable), FeatureName.AppList)) {
            ChannelUID appChannel = new ChannelUID(thing.getUID(), CHANNEL_APP);
            atv.apps().appList()
                    .thenAccept(apps -> setOptions(appChannel,
                            apps.stream().map(app -> Map.entry(app.identifier(), appLabel(app))).toList()))
                    .exceptionally(e -> {
                        logger.debug("Fetching app list failed", e);
                        return null;
                    });
        }
        if (isLinked(CHANNEL_ACCOUNT) && atv.features().inState(
                List.of(FeatureState.Available, FeatureState.Unknown, FeatureState.Unavailable),
                FeatureName.AccountList)) {
            ChannelUID accountChannel = new ChannelUID(thing.getUID(), CHANNEL_ACCOUNT);
            atv.userAccounts().accountList()
                    .thenAccept(accounts -> setOptions(accountChannel, accounts.stream()
                            .map(account -> Map.entry(account.identifier(), accountLabel(account))).toList()))
                    .exceptionally(e -> {
                        logger.debug("Fetching account list failed", e);
                        return null;
                    });
        }
    }

    /**
     * Publishes the same id/label pairs as both command and state options: the command options let a UI
     * offer them as sendable commands, the state options give the current value a friendly display label.
     */
    private void setOptions(ChannelUID channel, List<Map.Entry<String, String>> options) {
        commandDescriptionProvider.setCommandOptions(channel,
                options.stream().map(o -> new CommandOption(o.getKey(), o.getValue())).toList());
        stateDescriptionProvider.setStateOptions(channel,
                options.stream().map(o -> new StateOption(o.getKey(), o.getValue())).toList());
    }

    private String appLabel(App app) {
        return labelOrId(app.name(), app.identifier());
    }

    private String accountLabel(UserAccount account) {
        return labelOrId(account.name(), account.identifier());
    }

    private String labelOrId(@Nullable String label, String identifier) {
        return label == null || label.isBlank() ? identifier : label;
    }

    private void clearDynamicOptions() {
        ChannelUID appChannel = new ChannelUID(thing.getUID(), CHANNEL_APP);
        ChannelUID accountChannel = new ChannelUID(thing.getUID(), CHANNEL_ACCOUNT);
        stateDescriptionProvider.removeStateOptions(appChannel);
        stateDescriptionProvider.removeStateOptions(accountChannel);
        commandDescriptionProvider.removeCommandOptions(appChannel);
        commandDescriptionProvider.removeCommandOptions(accountChannel);
    }

    private void updateDeviceProperties(AtvConfig device) {
        Map<String, String> properties = editProperties();
        var info = device.deviceInfo();
        properties.put(AtvBindingConstants.PROPERTY_MODEL, String.valueOf(info.model()));
        info.version().ifPresent(v -> properties.put(AtvBindingConstants.PROPERTY_OS_VERSION, v));
        info.buildNumber().ifPresent(b -> properties.put(AtvBindingConstants.PROPERTY_BUILD_NUMBER, b));
        updateProperties(properties);
    }

    private Map<String, Object> pairingOptions() {
        return config.name.isBlank() ? Map.of() : Map.of("name", config.name);
    }

    private String credentialFor(Protocol protocol) {
        return switch (protocol) {
            case AirPlay -> config.airplayCredentials;
            case Companion -> config.companionCredentials;
            case RAOP -> config.raopCredentials;
            default -> "";
        };
    }

    private String credentialKey(Protocol protocol) {
        return switch (protocol) {
            case AirPlay -> CONFIG_AIRPLAY_CREDENTIALS;
            case Companion -> CONFIG_COMPANION_CREDENTIALS;
            case RAOP -> CONFIG_RAOP_CREDENTIALS;
            default -> CONFIG_AIRPLAY_CREDENTIALS;
        };
    }

    /** Clears the pairing PIN once the device is fully paired and online. It is spent by then. */
    private void clearPins() {
        if (config.pin.isBlank()) {
            return;
        }
        storeConfiguration(updated -> updated.put(CONFIG_PIN, ""));
    }

    /** Moves the device ID of a Thing created by a test build to the parameter's current name. */
    void migrateLegacyDeviceId() {
        Configuration current = getConfig();
        if (current.get(CONFIG_LEGACY_DEVICE_ID) instanceof String legacy && !legacy.isBlank()
                && !(current.get(CONFIG_DEVICE_ID) instanceof String id && !id.isBlank())) {
            storeConfiguration(updated -> {
                updated.put(CONFIG_DEVICE_ID, legacy);
                updated.remove(CONFIG_LEGACY_DEVICE_ID);
            });
        }
    }

    /** Applies an edit to the Thing configuration and re-reads the cached copy. */
    private void storeConfiguration(Consumer<Configuration> edit) {
        Configuration updated = editConfiguration();
        edit.accept(updated);
        updateConfiguration(updated);
        config = getConfigAs(AtvConfiguration.class);
    }

    private State powerToState(PowerState state) {
        return switch (state) {
            case On -> OnOffType.ON;
            case Off -> OnOffType.OFF;
            default -> UnDefType.UNDEF;
        };
    }

    private State stringOrUndef(Optional<String> value) {
        return value.isPresent() ? new StringType(value.get()) : UnDefType.UNDEF;
    }

    private State decimalOrUndef(Optional<Integer> value) {
        return value.isPresent() ? new DecimalType(value.get()) : UnDefType.UNDEF;
    }

    private void handleDisconnect(AppleTV connection, @Nullable Exception exception) {
        int gen = generation.get();
        if (!appleTV.compareAndSet(connection, null)) {
            // A newer connection replaced this one, or it was closed on purpose. Nothing to recover.
            return;
        }
        stopPositionTicker();
        retry(gen, supervisor.onConnectionLost(),
                exception != null ? failureMessage(exception) : "@text/offline.connection-lost");
    }

    /** Closes and forgets the current connection, if any. */
    private void dropConnection() {
        AppleTV existing = appleTV.getAndSet(null);
        if (existing != null) {
            existing.close();
        }
    }

    /**
     * Shows the supervisor's verdict and schedules the next attempt.
     *
     * @param offlineMessage status description to show if the verdict is OFFLINE
     */
    private void retry(int gen, ConnectionSupervisor.Retry retry, String offlineMessage) {
        if (isStale(gen)) {
            return;
        }
        if (retry.status() == ConnectionSupervisor.Status.ASLEEP) {
            // Asleep is not a failure: the device is there and can be woken, so the Thing stays ONLINE.
            updateStatus(ThingStatus.ONLINE, "@text/online.asleep");
        } else {
            updateStatus(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, offlineMessage);
        }
        scheduleConnect(gen, retry.delaySeconds());
    }

    /** The deepest cause's message, which names the actual problem rather than a wrapper's class name. */
    private String failureMessage(Throwable failure) {
        Throwable root = failure;
        for (int depth = 0; depth < 8 && root.getCause() != null; depth++) {
            root = Objects.requireNonNull(root.getCause());
        }
        String message = root.getMessage();
        return message == null || message.isBlank() ? "@text/offline.connect-failed" : message;
    }

    /**
     * Whether the current connection is missing the protocols that carry metadata, push updates and remote
     * control - typically Companion-only after a wake.
     */
    private boolean isDegradedConnection() {
        AppleTV atv = appleTV.get();
        if (atv == null || THING_TYPE_SPEAKER.equals(thing.getThingTypeUID())) {
            // A speaker only ever connects AirPlay and RAOP, so there is nothing to upgrade.
            return false;
        }
        // AirPlay and RAOP register without opening a connection, so their presence proves nothing about
        // the device. MRP - native or tunneled over AirPlay - is what actually carries the state.
        return !atv.connectedProtocols().contains(Protocol.MRP);
    }

    private ConnectionSupervisor newSupervisor() {
        return new ConnectionSupervisor(getThing().getUID().getId(), Clock.systemUTC());
    }

    private void requestWake() {
        supervisor.requestWake();
        scheduleConnect(generation.get(), RECONNECT_NOW_SECONDS);
    }

    /** Replaces any pending reconnect with one after the given delay, unless the session has ended. */
    private void scheduleConnect(int gen, long delaySeconds) {
        synchronized (scheduleLock) {
            ScheduledExecutorService executor = connectExecutor;
            if (executor == null || isStale(gen)) {
                return;
            }
            cancel(reconnectJob);
            try {
                reconnectJob = executor.schedule(() -> connect(gen), delaySeconds, TimeUnit.SECONDS);
            } catch (RejectedExecutionException e) {
                // Disposed meanwhile, which shut the executor down.
            }
        }
    }

    private void submitConnect() {
        scheduleConnect(generation.get(), 0);
    }

    /**
     * Handles an mDNS announcement for this device by bringing the next attempt forward. A sleeping device
     * keeps announcing itself, so this does not mean it woke. It only makes a remote power-on show up sooner
     * than the next scheduled check. It never knocks, so it cannot wake a device the user turned off.
     */
    private void onMdnsAnnouncement(@Nullable ServiceInfo info) {
        if (info == null || disposed || credentialsRejected || pendingPairing != null || !matchesDevice(info)) {
            return;
        }
        followAddressChange(info);
        if (appleTV.get() != null || connecting) {
            return;
        }
        ScheduledFuture<?> pending = reconnectJob;
        if (pending != null && !pending.isDone() && pending.getDelay(TimeUnit.SECONDS) <= RECONNECT_NOW_SECONDS) {
            return;
        }
        if (supervisor.onAnnouncement()) {
            logger.debug("mDNS announcement from {}, trying now", config.deviceId);
            scheduleConnect(generation.get(), RECONNECT_NOW_SECONDS);
        }
    }

    /**
     * Updates the configured host when this device announces itself from a new IPv4 address (e.g. after a
     * DHCP change). The scan only accepts a device with this Thing's identifier, so without this a moved
     * device would stay unreachable.
     */
    private void followAddressChange(ServiceInfo info) {
        String id = info.getPropertyString("deviceid");
        String host = config.host;
        Inet4Address[] addresses = info.getInet4Addresses();
        // A host name is left alone. It is meant to keep working when the address changes.
        if (!IPV4_LITERAL.matcher(host).matches() || id == null || !id.equalsIgnoreCase(config.deviceId)
                || addresses.length == 0) {
            return;
        }
        for (Inet4Address address : addresses) {
            if (host.equals(address.getHostAddress())) {
                return;
            }
        }
        String newHost = addresses[0].getHostAddress();
        logger.debug("Device {} moved from {} to {}, updating host", config.deviceId, host, newHost);
        storeConfiguration(updated -> updated.put(CONFIG_HOST, newHost));
    }

    private boolean matchesDevice(ServiceInfo info) {
        String id = info.getPropertyString("deviceid");
        if (id != null && id.equalsIgnoreCase(config.deviceId)) {
            return true;
        }
        String host = config.host;
        if (!host.isBlank()) {
            for (Inet4Address address : info.getInet4Addresses()) {
                if (host.equals(address.getHostAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    @NonNullByDefault({})
    private final class AnnouncementListener implements ServiceListener {
        @Override
        public void serviceAdded(ServiceEvent event) {
            onMdnsAnnouncement(event.getInfo());
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            onMdnsAnnouncement(event.getInfo());
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
        }
    }

    private void wake(AppleTV atv) {
        try {
            atv.power().turnOn();
        } catch (RuntimeException e) {
            logger.debug("Failed to wake {}", config.deviceId, e);
        }
    }

    /**
     * Asks a device believed asleep whether it still is, over a Companion-only connection that is closed
     * again right away.
     */
    private ConnectionSupervisor.Plan checkAsleep(AtvConfig device) throws InterruptedException {
        try {
            AppleTV check = await(Atv.connect(device, new ConnectOptions(runtime, null, Protocol.Companion)),
                    AppleTV::close);
            try {
                PowerState power = awaitPower(check);
                logger.trace("Asleep check of {} read power {}", config.deviceId, power);
                return supervisor.onChecked(Reachability.ANSWERED, power);
            } finally {
                check.close();
            }
        } catch (ExecutionException | TimeoutException e) {
            Reachability reach = Reachability.of(e);
            logger.trace("Asleep check of {} failed ({}): {}", config.deviceId, reach, e.getMessage());
            return supervisor.onChecked(reach, PowerState.Unknown);
        }
    }

    /**
     * Returns the device's power state, waiting briefly for it to push one if it is not known yet. Some tvOS
     * builds do not answer the initial power query and only report through the status subscription.
     */
    private PowerState awaitPower(AppleTV atv) throws InterruptedException {
        PowerState power = powerOf(atv);
        if (power != PowerState.Unknown) {
            return power;
        }
        CompletableFuture<PowerState> reported = new CompletableFuture<>();
        try {
            atv.power().addListener((oldState, newState) -> {
                if (newState != PowerState.Unknown) {
                    reported.complete(newState);
                }
            });
        } catch (RuntimeException e) {
            return PowerState.Unknown;
        }
        // Covers a report that arrived before the listener was added.
        power = powerOf(atv);
        if (power != PowerState.Unknown) {
            return power;
        }
        try {
            return reported.get(POWER_REPORT_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            return PowerState.Unknown;
        }
    }

    private PowerState powerOf(AppleTV atv) {
        try {
            return atv.power().powerState();
        } catch (RuntimeException e) {
            // A speaker has no power state, and a closing connection blocks the call.
            return PowerState.Unknown;
        }
    }

    /**
     * Cancels a job without interrupting it. Interrupting would abort a connect that is already running and
     * push the retry onto the slow cadence, which is the opposite of what every caller wants.
     */
    private void cancel(@Nullable ScheduledFuture<?> job) {
        if (job != null) {
            job.cancel(false);
        }
    }

    private <T> T await(CompletableFuture<T> future) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(OP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Waits for an operation that opens a resource. If the wait is abandoned, the operation keeps running,
     * so whatever it opens later is closed on arrival instead of leaking.
     */
    private <T> T await(CompletableFuture<T> future, Consumer<? super T> closer)
            throws InterruptedException, ExecutionException, TimeoutException {
        try {
            return await(future);
        } catch (InterruptedException | TimeoutException e) {
            future.thenAccept(closer);
            throw e;
        }
    }
}
