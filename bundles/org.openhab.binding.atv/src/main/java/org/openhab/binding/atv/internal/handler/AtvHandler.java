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

import java.io.InputStream;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.openhab.binding.atv.internal.client.dto.ArtworkInfo;
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
import org.openhab.binding.atv.internal.client.exceptions.BlockedStateError;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;
import org.openhab.binding.atv.internal.config.AtvConfiguration;
import org.openhab.core.config.core.Configuration;
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
    private static final long RECONNECT_SECONDS = 30;
    // Fast retry after a graceful drop or to upgrade a partial connection once the device wakes.
    private static final long RECONNECT_NOW_SECONDS = 3;
    // Cadence for re-probing an asleep but still-discoverable device to detect a wake (e.g. the user
    // turning it on with the remote). Lower means a faster power-on response for automations, at the cost
    // of more frequent lightweight Companion power probes; the probe never holds a connection, so it does
    // not flap.
    private static final long ASLEEP_RETRY_SECONDS = 20;
    // An asleep device stays ONLINE while it remains reachable. If it cannot be reached for this long it
    // is treated as gone (powered off) and reported OFFLINE. Must exceed any asleep unreachable cycle.
    private static final long GONE_AFTER_MILLIS = Duration.ofMinutes(10).toMillis();
    // While a user wake is pending, retry fast to catch a duty-cycle device's brief reachable window
    // instead of waiting out the slow asleep cadence.
    private static final long WAKE_RETRY_SECONDS = 5;
    // Give up a pending wake after this long so an off device is not hammered forever. Must exceed a
    // sleeping device's unreachable cycle so at least one reachable window is caught.
    private static final long WAKE_TIMEOUT_MILLIS = Duration.ofMinutes(5).toMillis();
    // Cadence for extrapolating the playback position channel while media is playing.
    private static final long POSITION_INTERVAL_SECONDS = 1;
    // Window over which repeated refresh requests are merged into one.
    private static final long REFRESH_DEBOUNCE_SECONDS = 2;
    // Cadence for the stale-connection watchdog check.
    private static final long WATCHDOG_INTERVAL_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(AtvHandler.class);
    private final @Nullable FileHostService fileHostService;
    private final AtvDynamicStateDescriptionProvider stateDescriptionProvider;
    private final AtvDynamicCommandDescriptionProvider commandDescriptionProvider;

    private volatile AtvConfiguration config = new AtvConfiguration();
    private volatile @Nullable AtvRuntime runtime;
    // Written from the scheduler thread (connect) and the protocol loop thread (disconnect); the atomic
    // reference makes the compare-and-clear in handleDisconnect safe against a concurrent reconnect.
    private final AtomicReference<@Nullable AppleTV> appleTV = new AtomicReference<>();
    private volatile @Nullable ScheduledFuture<?> reconnectJob;
    private volatile @Nullable ScheduledFuture<?> positionJob;
    private volatile @Nullable ScheduledFuture<?> staleJob;
    private volatile @Nullable ScheduledFuture<?> healthJob;
    private volatile @Nullable ScheduledFuture<?> refreshJob;
    // In-flight audio-sink playback, so a sink stop request can end it.
    private final AtomicReference<@Nullable CompletableFuture<Void>> playback = new AtomicReference<>();
    // Guards the position ticker; a separate monitor from connect()'s, which is held for the whole
    // (slow) connect and must not block the protocol threads that report now-playing updates.
    private final Object positionLock = new Object();
    private volatile @Nullable String lastArtworkId;
    // Forces the first artwork evaluation after connect, so restored state is cleared even when the id is null.
    private volatile boolean artworkEvaluated;
    // Last position reported by the device and when it arrived, so the ticker can extrapolate instead of
    // asking the device for metadata every second.
    private volatile int lastPositionSeconds;
    private volatile long lastPositionMillis;
    private volatile int lastTotalSeconds;

    // Tells a graceful sleep apart from a real failure, and detects wake from a partial connection.
    private volatile PowerState lastPowerState = PowerState.Unknown;
    private volatile long lastReachableMillis;
    private volatile boolean disposed;
    // Set when power ON is commanded while offline; honored once a connection is (re)established.
    private volatile boolean pendingWake;
    private volatile long pendingWakeDeadlineMillis;
    // Keeps the repeating "still asleep" probe result to a single log line per sleep.
    private volatile boolean asleepReported;
    // Set after a device drops the connection while asleep; the next connect only checks power and
    // disconnects again if still asleep, so the Thing does not bounce ONLINE every retry.
    private volatile boolean wakeProbe;
    // Time of the last update received from the device; drives the stale-connection watchdog.
    private volatile long lastEventMillis;
    // True while the audio sink is streaming, so the watchdog does not reconnect mid-playback.
    private volatile boolean streaming;
    // Whether the active power probe has ever answered on the current connection; a probe that never
    // works (e.g. some tvOS builds) must not trigger a reconnect loop, only a regression from working.
    private volatile boolean probeSucceeded;

    // pairing state kept alive between showing the PIN and the user entering it
    private @Nullable PairingHandler pendingPairing;
    private @Nullable Protocol pendingProtocol;

    public AtvHandler(Thing thing, @Nullable FileHostService fileHostService,
            AtvDynamicStateDescriptionProvider stateDescriptionProvider,
            AtvDynamicCommandDescriptionProvider commandDescriptionProvider) {
        super(thing);
        this.fileHostService = fileHostService;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.commandDescriptionProvider = commandDescriptionProvider;
    }

    @Override
    public void initialize() {
        config = getConfigAs(AtvConfiguration.class);
        if (config.macAddress.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.no-identifier");
            return;
        }
        // openHAB reuses the same handler instance across a configuration change (dispose then initialize),
        // so every flag dispose() set has to be cleared here or the handler stays dead.
        disposed = false;
        lastPowerState = PowerState.Unknown;
        pendingWake = false;
        wakeProbe = false;
        asleepReported = false;
        streaming = false;
        probeSucceeded = false;
        artworkEvaluated = false;
        updateStatus(ThingStatus.UNKNOWN);
        runtime = new AtvRuntime(scheduler, Clock.systemUTC(), fileHostService);
        scheduler.execute(this::connect);
    }

    @Override
    public void dispose() {
        disposed = true;
        cancel(reconnectJob);
        reconnectJob = null;
        cancel(refreshJob);
        refreshJob = null;
        stopPositionTicker();
        cancel(staleJob);
        staleJob = null;
        cancel(healthJob);
        healthJob = null;
        PairingHandler pairing = pendingPairing;
        if (pairing != null) {
            pairing.close();
            pendingPairing = null;
            pendingProtocol = null;
        }
        AppleTV atv = appleTV.getAndSet(null);
        if (atv != null) {
            atv.close();
        }
        clearDynamicOptions();
        lastArtworkId = null;
        // the runtime uses openHAB's shared scheduler, so nothing to shut down here
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
                completePairing(pairing, pinValue);
                return;
            }
        }
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        AppleTV atv = appleTV.get();
        if (atv == null) {
            // A power ON while offline is a wake request; reconnect and send it once connected.
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
            case CHANNEL_PLAY_URL -> atv.stream().playUrl(value);
            case CHANNEL_STREAM_URL -> atv.stream().streamFile(value);
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
            logger.debug("Cannot stream audio to {}: not connected", config.macAddress);
            return;
        }
        streaming = true;
        try {
            CompletableFuture<Void> future = atv.stream().streamFile(stream, null,
                    Map.<String, Object> of(Stream.OPTION_VOLUME, volumePercent));
            playback.set(future);
            // No timeout: RAOP paces the audio in real time, so this call lasts as long as the clip. A
            // timeout here would cut playback short and let the caller close the stream mid-send.
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            logger.debug("Failed to stream audio to {}", config.macAddress, e);
        } catch (BlockedStateError e) {
            logger.debug("Cannot stream audio to {}: connection closed", config.macAddress);
        } finally {
            playback.set(null);
            streaming = false;
            markEvent();
        }
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

    private synchronized void connect() {
        if (disposed) {
            return;
        }
        // Discard any prior connection first so a reconnect does not leak its protocols and heartbeats.
        AppleTV existing = appleTV.getAndSet(null);
        if (existing != null) {
            existing.close();
        }
        try {
            AtvConfig device = scanForDevice();
            if (device == null) {
                setDisconnectedStatus("@text/offline.not-found");
                scheduleConnect(RECONNECT_SECONDS);
                return;
            }
            markReachable();
            updateDeviceProperties(device);
            applyStoredCredentials(device);

            Protocol needsPairing = protocolNeedingPairing(device);
            if (needsPairing != null) {
                beginPairing(device, needsPairing);
                return;
            }

            if (wakeProbe && !pendingWake && probeStillAsleep(device)) {
                // Still asleep (a lightweight Companion-only probe read power Off). Keep the Thing ONLINE
                // with the sleeping note and probe again later for wake rather than fully reconnecting.
                setDisconnectedStatus(null);
                scheduleConnect(ASLEEP_RETRY_SECONDS);
                return;
            }
            wakeProbe = false;
            asleepReported = false;

            AppleTV atv = await(Atv.connect(device, new ConnectOptions(runtime, null, null)));
            appleTV.set(atv);
            if (disposed) {
                // Disposed while this async connect was in flight; close the late connection rather than
                // leaking it and its heartbeats past the handler's lifetime.
                appleTV.compareAndSet(atv, null);
                atv.close();
                return;
            }
            markEvent();
            probeSucceeded = false;
            registerListeners(atv);
            if (isDegradedConnection()) {
                // Only part of the protocols came up, so metadata, push updates and remote control are
                // missing. Report it rather than looking fully online; a wake upgrades the connection.
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/online.partial");
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
            clearPins();
            refreshAll();
            refreshDynamicOptions(atv);
            startWatchdog();
            startHealthCheck();
            if (pendingWake) {
                pendingWake = false;
                wake(atv);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Connection to {} failed", config.macAddress, e);
            setDisconnectedStatus(e.getMessage());
            scheduleConnect(RECONNECT_SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            logger.debug("Connection to {} failed", config.macAddress, e);
            setDisconnectedStatus(e.getMessage());
            scheduleConnect(RECONNECT_SECONDS);
        } catch (BlockedStateError e) {
            // The connection dropped while it was still being set up (common right after a wake, while the
            // device settles). Treat it as a transient disconnect and retry.
            logger.debug("Connection to {} closed during setup; reconnecting", config.macAddress);
            appleTV.set(null);
            setDisconnectedStatus(null);
            scheduleConnect(RECONNECT_NOW_SECONDS);
        }
    }

    private @Nullable AtvConfig scanForDevice() throws InterruptedException, ExecutionException, TimeoutException {
        // Knock only while a wake is pending: it exists to pull a device out of sleep, so knocking on every
        // routine reconnect would keep waking a device the user deliberately turned off.
        ScanOptions options = ScanOptions.defaults().withTimeout(SCAN_TIMEOUT).withRuntime(runtime)
                .withKnock(isWakePending());
        options = config.host.isBlank() ? options.withIdentifiers(config.macAddress)
                : options.withHosts(List.of(config.host));
        List<AtvConfig> results = await(Atv.scan(options));
        String mac = config.macAddress.toLowerCase(Locale.ROOT);
        return results.stream()
                .filter(c -> c.allIdentifiers().stream().anyMatch(id -> id.toLowerCase(Locale.ROOT).equals(mac)))
                .findFirst().orElse(results.isEmpty() ? null : results.get(0));
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

    private void beginPairing(AtvConfig device, Protocol protocol)
            throws InterruptedException, ExecutionException, TimeoutException {
        PairingHandler pairing = await(Atv.pair(device, protocol, new PairOptions(runtime, null, pairingOptions())));
        await(pairing.begin());
        pendingPairing = pairing;
        pendingProtocol = protocol;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                "@text/offline.pairing [\"" + protocol + "\"]");
    }

    private void completePairing(PairingHandler pairing, String pin) {
        Protocol protocol = pendingProtocol;
        try {
            pairing.pin(pin);
            await(pairing.finish());
            String credentials = Objects.requireNonNullElse(pairing.service().credentials().orElse(""), "");
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
            scheduler.execute(this::connect);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pairingFailed(pairing, protocol, e);
        } catch (ExecutionException | TimeoutException e) {
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
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/offline.pairing-failed [\"" + protocol + "\"]");
        scheduler.execute(this::connect);
    }

    private void registerListeners(AppleTV atv) {
        atv.addListener(new DeviceListener() {
            @Override
            public void connectionLost(@Nullable Exception exception) {
                handleDisconnect(atv, exception);
            }

            @Override
            public void connectionClosed() {
                // A graceful close (e.g. the device tearing down its side on sleep) is still a disconnect;
                // recover like any other rather than leaving the Thing stranded OFFLINE with no reason.
                handleDisconnect(atv, null);
            }
        });

        PushUpdater push = atv.pushUpdater();
        push.addListener(new PushListener() {
            @Override
            @NonNullByDefault({})
            public void playstatusUpdate(PushUpdater updater, Playing playstatus) {
                markEvent();
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
            markEvent();
            lastPowerState = newState;
            updateState(CHANNEL_POWER, powerToState(newState));
            // Woke on a partial (Companion-only) connection; reconnect to restore the richer protocols.
            if (newState == PowerState.On && oldState != PowerState.On && isDegradedConnection()) {
                logger.debug("Device {} woke with a partial connection; reconnecting to restore all protocols",
                        config.macAddress);
                scheduleConnect(RECONNECT_NOW_SECONDS);
            }
        });
        atv.audio().addListener(new org.openhab.binding.atv.internal.client.capability.AudioListener() {
            @Override
            public void volumeUpdate(double oldLevel, double newLevel) {
                markEvent();
                updateState(CHANNEL_VOLUME, new PercentType((int) Math.round(newLevel)));
            }

            @Override
            @NonNullByDefault({})
            public void outputDevicesUpdate(List<org.openhab.binding.atv.internal.client.dto.OutputDevice> oldDevices,
                    List<org.openhab.binding.atv.internal.client.dto.OutputDevice> newDevices) {
                markEvent();
                updateState(CHANNEL_OUTPUT_DEVICES, new StringType(
                        newDevices.stream().map(d -> d.identifier()).reduce((a, b) -> a + "," + b).orElse("")));
            }
        });
        atv.keyboard().addListener((oldState, newState) -> {
            markEvent();
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
        // Speakers do not support power management; calling powerState() there throws NotSupportedError.
        if (!atv.features().inState(FeatureState.Unsupported, FeatureName.PowerState)) {
            lastPowerState = atv.power().powerState();
            updateState(CHANNEL_POWER, powerToState(lastPowerState));
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
        // Not-unsupported rather than available, so an idle Apple TV clears the channel; speakers are skipped.
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

    private void startWatchdog() {
        cancel(staleJob);
        if (config.staleTimeout > 0) {
            staleJob = scheduler.scheduleWithFixedDelay(this::checkStaleness, WATCHDOG_INTERVAL_SECONDS,
                    WATCHDOG_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void startHealthCheck() {
        cancel(healthJob);
        if (config.healthCheckInterval > 0) {
            healthJob = scheduler.scheduleWithFixedDelay(this::runHealthCheck, config.healthCheckInterval,
                    config.healthCheckInterval, TimeUnit.SECONDS);
        }
    }

    private void runHealthCheck() {
        AppleTV atv = appleTV.get();
        if (atv == null || streaming) {
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
            // Connection is closing; a reconnect is already in progress.
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
                // Only treat a failure as a stall if the probe worked before - some tvOS builds never
                // answer it, and reconnecting on every such failure would loop.
                if (probeSucceeded) {
                    logger.debug("Power probe stopped responding on {}; reconnecting", config.macAddress);
                    scheduleConnect(RECONNECT_NOW_SECONDS);
                }
                return;
            }
            probeSucceeded = true;
            markEvent();
            if (fetched != cached) {
                logger.debug("Power probe reports {} but tracked {} on {}; reconnecting to clear stalled push stream",
                        fetched, cached, config.macAddress);
                scheduleConnect(RECONNECT_NOW_SECONDS);
            }
        });
    }

    /** Stops the health check on a device that offers no active power query (e.g. a speaker). */
    private void disableHealthCheck() {
        logger.debug("Active power probe unsupported on {}; disabling health check", config.macAddress);
        cancel(healthJob);
        healthJob = null;
    }

    private @Nullable Throwable unwrap(@Nullable Throwable error) {
        return error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
    }

    private void markEvent() {
        lastEventMillis = System.currentTimeMillis();
    }

    private void checkStaleness() {
        if (config.staleTimeout <= 0 || appleTV.get() == null || streaming) {
            return;
        }
        if (System.currentTimeMillis() - lastEventMillis >= config.staleTimeout * 60_000L) {
            logger.debug("No updates from {} for {} min; rebuilding connection to clear a possible stalled push stream",
                    config.macAddress, config.staleTimeout);
            markEvent();
            scheduleConnect(RECONNECT_NOW_SECONDS);
        }
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
                // No artwork; clear any stale image.
                updateState(CHANNEL_ARTWORK, UnDefType.UNDEF);
                return;
            }
        } catch (RuntimeException e) {
            // artworkId is unsupported by this protocol; fall through and always fetch
            logger.trace("Artwork identifier unavailable, fetching unconditionally", e);
        }
        atv.metadata().artwork().thenAccept(artwork -> {
            @Nullable
            ArtworkInfo info = artwork;
            if (info != null && info.bytes().length > 0) {
                updateState(CHANNEL_ARTWORK, new org.openhab.core.library.types.RawType(info.bytes(), info.mimetype()));
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

    /** Clears the pairing PIN once the device is fully paired and online; it is spent by then. */
    private void clearPins() {
        if (config.pin.isBlank()) {
            return;
        }
        storeConfiguration(updated -> updated.put(CONFIG_PIN, ""));
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
        return value.isPresent() ? new org.openhab.core.library.types.DecimalType(value.get()) : UnDefType.UNDEF;
    }

    private void handleDisconnect(AppleTV connection, @Nullable Exception exception) {
        if (!appleTV.compareAndSet(connection, null)) {
            // A newer connection replaced this one, or we closed it intentionally; nothing to recover.
            return;
        }
        stopPositionTicker();
        markReachable();
        setDisconnectedStatus(exception != null ? exception.getMessage() : null);
        if (lastPowerState == PowerState.Off) {
            // Asleep: the device will not hold a connection, so probe slowly for wake instead of flapping
            // (fast while a wake is pending). A GUI power ON wakes it immediately via requestWake().
            wakeProbe = true;
            scheduleConnect(ASLEEP_RETRY_SECONDS);
        } else {
            // Awake a moment ago; a genuine glitch, so retry promptly.
            scheduleConnect(RECONNECT_NOW_SECONDS);
        }
    }

    private void setDisconnectedStatus(@Nullable String communicationError) {
        if (lastPowerState == PowerState.Off) {
            if (System.currentTimeMillis() - lastReachableMillis < GONE_AFTER_MILLIS) {
                // Asleep but still reachable on the network: report ONLINE, since OFFLINE should mean
                // unreachable, but flag the duty cycle so it is clear that only the power channel acts.
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.DUTY_CYCLE, "@text/online.asleep");
            } else {
                // Asleep and unreachable for a long time: the device is likely powered off.
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.not-found");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, communicationError);
        }
    }

    private void markReachable() {
        lastReachableMillis = System.currentTimeMillis();
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

    private void requestWake() {
        logger.debug("Power ON requested while offline; reconnecting to wake {}", config.macAddress);
        pendingWake = true;
        pendingWakeDeadlineMillis = System.currentTimeMillis() + WAKE_TIMEOUT_MILLIS;
        scheduleConnect(RECONNECT_NOW_SECONDS);
    }

    /**
     * Whether a user wake request is still active. A duty-cycle device is only reachable for brief windows,
     * so a wake keeps retrying until it lands one. Auto-clears (and returns false) once the deadline passes
     * so an off device is not retried forever.
     */
    private boolean isWakePending() {
        if (!pendingWake) {
            return false;
        }
        if (System.currentTimeMillis() >= pendingWakeDeadlineMillis) {
            logger.debug("Wake request for {} timed out; device did not respond", config.macAddress);
            pendingWake = false;
            return false;
        }
        return true;
    }

    /**
     * Schedules the next connect attempt: fast while a wake is pending (to catch a brief reachable window),
     * otherwise at the given normal cadence. The single entry point for every retry, so the cadence does
     * not depend on which code path asked for it.
     */
    private void scheduleConnect(long normalSeconds) {
        cancel(reconnectJob);
        long delay = isWakePending() ? WAKE_RETRY_SECONDS : normalSeconds;
        reconnectJob = scheduler.schedule(this::connect, delay, TimeUnit.SECONDS);
    }

    private void wake(AppleTV atv) {
        try {
            atv.power().turnOn();
        } catch (RuntimeException e) {
            logger.debug("Failed to wake {}", config.macAddress, e);
        }
    }

    /**
     * Lightweight wake probe: opens a Companion-only connection (which reports power without the AirPlay
     * tunnel or RAOP), reads the power state, and closes it. Returns {@code true} when the device is still
     * asleep or could not be reached, so the caller keeps the Thing offline instead of a full reconnect.
     */
    private boolean probeStillAsleep(AtvConfig device) {
        try {
            AppleTV probe = await(Atv.connect(device, new ConnectOptions(runtime, null, Protocol.Companion)));
            try {
                return isAsleep(probe);
            } finally {
                probe.close();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        } catch (ExecutionException | TimeoutException | RuntimeException e) {
            // Logged once per sleep rather than on every 60s probe, which would otherwise repeat for as
            // long as the device stays asleep.
            if (!asleepReported) {
                asleepReported = true;
                logger.debug("Wake probe of {} did not respond; still asleep", config.macAddress);
            } else {
                logger.trace("Wake probe of {} did not respond; still asleep", config.macAddress);
            }
            return true;
        }
    }

    private boolean isAsleep(AppleTV atv) {
        try {
            return atv.power().powerState() == PowerState.Off;
        } catch (RuntimeException e) {
            // Power unsupported or not yet known; do not treat as asleep.
            return false;
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
}
