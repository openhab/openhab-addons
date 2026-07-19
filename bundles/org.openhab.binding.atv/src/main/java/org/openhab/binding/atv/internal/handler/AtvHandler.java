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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.AtvBindingConstants;
import org.openhab.binding.atv.internal.AtvStateDescriptionProvider;
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
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
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

    /**
     * Maps each channel id to the feature that backs it. Channels absent from this map (e.g.
     * remote-key, media-type, playback-state) are always kept because they are either general
     * controls or derived state that no single feature describes.
     */
    private static final Map<String, FeatureName> CHANNEL_FEATURES = Map.ofEntries(
            Map.entry(CHANNEL_POWER, FeatureName.PowerState), Map.entry(CHANNEL_MEDIA_CONTROL, FeatureName.PlayPause),
            Map.entry(CHANNEL_TITLE, FeatureName.Title), Map.entry(CHANNEL_ARTIST, FeatureName.Artist),
            Map.entry(CHANNEL_ALBUM, FeatureName.Album), Map.entry(CHANNEL_GENRE, FeatureName.Genre),
            Map.entry(CHANNEL_POSITION, FeatureName.Position), Map.entry(CHANNEL_DURATION, FeatureName.TotalTime),
            Map.entry(CHANNEL_PROGRESS, FeatureName.Position), Map.entry(CHANNEL_SHUFFLE, FeatureName.Shuffle),
            Map.entry(CHANNEL_REPEAT, FeatureName.Repeat), Map.entry(CHANNEL_SERIES_NAME, FeatureName.SeriesName),
            Map.entry(CHANNEL_SEASON_NUMBER, FeatureName.SeasonNumber),
            Map.entry(CHANNEL_EPISODE_NUMBER, FeatureName.EpisodeNumber),
            Map.entry(CHANNEL_CONTENT_ID, FeatureName.ContentIdentifier),
            Map.entry(CHANNEL_ITUNES_ID, FeatureName.iTunesStoreIdentifier),
            Map.entry(CHANNEL_ARTWORK, FeatureName.Artwork), Map.entry(CHANNEL_APP, FeatureName.LaunchApp),
            Map.entry(CHANNEL_APP_NAME, FeatureName.App), Map.entry(CHANNEL_ACCOUNT, FeatureName.SwitchAccount),
            Map.entry(CHANNEL_VOLUME, FeatureName.Volume), Map.entry(CHANNEL_OUTPUT_DEVICES, FeatureName.OutputDevices),
            Map.entry(CHANNEL_OUTPUT_DEVICE_VOLUME, FeatureName.SetVolume),
            Map.entry(CHANNEL_KEYBOARD_INPUT, FeatureName.TextSet),
            Map.entry(CHANNEL_KEYBOARD_FOCUS, FeatureName.TextFocusState),
            Map.entry(CHANNEL_TOUCH_GESTURE, FeatureName.Click), Map.entry(CHANNEL_PLAY_URL, FeatureName.PlayUrl),
            Map.entry(CHANNEL_STREAM_URL, FeatureName.StreamFile));

    private final Logger logger = LoggerFactory.getLogger(AtvHandler.class);
    private final @Nullable FileHostService fileHostService;

    private AtvConfiguration config = new AtvConfiguration();
    private @Nullable AtvRuntime runtime;
    private @Nullable AppleTV appleTV;
    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable ScheduledFuture<?> pollJob;
    private @Nullable AtvStateDescriptionProvider stateDescriptionProvider;
    private @Nullable String lastArtworkId;

    // pairing state kept alive between showing the PIN and the user entering it
    private @Nullable PairingHandler pendingPairing;
    private @Nullable Protocol pendingProtocol;

    public AtvHandler(Thing thing, @Nullable FileHostService fileHostService) {
        super(thing);
        this.fileHostService = fileHostService;
    }

    @Override
    public void initialize() {
        config = getConfigAs(AtvConfiguration.class);
        if (config.macAddress.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.no-identifier");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        stateDescriptionProvider = lookupStateDescriptionProvider();
        runtime = new AtvRuntime(scheduler, Clock.systemUTC(), fileHostService);
        scheduler.execute(this::connect);
    }

    @Override
    public void dispose() {
        cancel(reconnectJob);
        reconnectJob = null;
        cancel(pollJob);
        pollJob = null;
        PairingHandler pairing = pendingPairing;
        if (pairing != null) {
            pairing.close();
            pendingPairing = null;
            pendingProtocol = null;
        }
        AppleTV atv = appleTV;
        if (atv != null) {
            atv.close();
            appleTV = null;
        }
        clearDynamicOptions();
        lastArtworkId = null;
        // the runtime uses openHAB's shared scheduler, so nothing to shut down here
        runtime = null;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        PairingHandler pairing = pendingPairing;
        Protocol protocol = pendingProtocol;
        if (pairing != null && protocol != null) {
            Object pin = configurationParameters.get(pinKey(protocol));
            if (pin instanceof String pinValue && !pinValue.isBlank()) {
                completePairing(pairing, pinValue);
                return;
            }
        }
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        AppleTV atv = appleTV;
        if (atv == null) {
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

    /**
     * Streams an audio stream to the device via RAOP at the given volume, blocking until playback
     * finishes.
     *
     * @param stream the audio stream to play
     * @param volumePercent the playback volume in percent (0-100)
     */
    public void streamAudio(InputStream stream, double volumePercent) {
        AppleTV atv = appleTV;
        if (atv == null) {
            logger.warn("Cannot stream audio to {}: not connected", config.macAddress);
            return;
        }
        try {
            await(atv.stream().streamFile(stream, null, Map.<String, Object> of(Stream.OPTION_VOLUME, volumePercent)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            logger.warn("Failed to stream audio to {}", config.macAddress, e);
        }
    }

    /**
     * Stops any audio the sink is currently playing.
     */
    public void stopAudio() {
        AppleTV atv = appleTV;
        if (atv != null) {
            try {
                atv.remoteControl().stop();
            } catch (RuntimeException e) {
                logger.debug("Failed to stop audio", e);
            }
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
        try {
            AtvConfig device = scanForDevice();
            if (device == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.not-found");
                scheduleReconnect();
                return;
            }
            updateDeviceProperties(device);
            applyStoredCredentials(device);

            Protocol needsPairing = protocolNeedingPairing(device);
            if (needsPairing != null) {
                beginPairing(device, needsPairing);
                return;
            }

            AppleTV atv = await(Atv.connect(device, new ConnectOptions(runtime, null, null)));
            appleTV = atv;
            registerListeners(atv);
            pruneUnsupportedChannels(atv);
            updateStatus(ThingStatus.ONLINE);
            clearPins();
            refreshAll();
            refreshDynamicOptions(atv);
            startPolling();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Connection to {} failed", config.macAddress, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduleReconnect();
        } catch (ExecutionException | TimeoutException e) {
            logger.debug("Connection to {} failed", config.macAddress, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduleReconnect();
        }
    }

    private @Nullable AtvConfig scanForDevice() throws InterruptedException, ExecutionException, TimeoutException {
        ScanOptions options = ScanOptions.defaults().withTimeout(SCAN_TIMEOUT).withRuntime(runtime);
        options = config.host.isBlank() ? options.withIdentifiers(config.macAddress)
                : options.withHosts(List.of(config.host));
        List<AtvConfig> results = await(Atv.scan(options));
        String mac = config.macAddress.toLowerCase(Locale.ROOT);
        return results.stream()
                .filter(c -> c.allIdentifiers().stream().anyMatch(id -> id.toLowerCase(Locale.ROOT).equals(mac)))
                .findFirst().orElse(results.isEmpty() ? null : results.get(0));
    }

    private void applyStoredCredentials(AtvConfig device) {
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
            Configuration updated = editConfiguration();
            if (protocol != null) {
                // keep the entered PIN visible so it is clear it was accepted
                updated.put(pinKey(protocol), pin);
                if (!credentials.isBlank()) {
                    updated.put(credentialKey(protocol), credentials);
                }
            }
            updateConfiguration(updated);
            // re-read config and reconnect (which pairs the next protocol if needed)
            config = getConfigAs(AtvConfiguration.class);
            scheduler.execute(this::connect);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pairingFailed(pairing, protocol, e);
        } catch (ExecutionException | TimeoutException e) {
            pairingFailed(pairing, protocol, e);
        }
    }

    /**
     * Handles a failed pairing step: clears only that protocol's PIN field and re-arms pairing so the
     * device shows a fresh PIN for another attempt.
     */
    private void pairingFailed(PairingHandler pairing, @Nullable Protocol protocol, Exception cause) {
        logger.debug("Pairing {} failed", protocol, cause);
        pairing.close();
        pendingPairing = null;
        pendingProtocol = null;
        if (protocol != null) {
            Configuration updated = editConfiguration();
            updated.put(pinKey(protocol), "");
            updateConfiguration(updated);
            config = getConfigAs(AtvConfiguration.class);
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/offline.pairing-failed [\"" + protocol + "\"]");
        scheduler.execute(this::connect);
    }

    private void registerListeners(AppleTV atv) {
        atv.addListener(new DeviceListener() {
            @Override
            public void connectionLost(@Nullable Exception exception) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        exception != null ? exception.getMessage() : null);
                scheduleReconnect();
            }

            @Override
            public void connectionClosed() {
                updateStatus(ThingStatus.OFFLINE);
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

        atv.power().addListener((oldState, newState) -> updateState(CHANNEL_POWER, powerToState(newState)));
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
        atv.keyboard().addListener(
                (oldState, newState) -> updateState(CHANNEL_KEYBOARD_FOCUS, new StringType(newState.name())));
    }

    private void refreshAll() {
        AppleTV atv = appleTV;
        if (atv == null) {
            return;
        }
        atv.metadata().playing().thenAccept(this::updatePlaying);
        updateState(CHANNEL_POWER, powerToState(atv.power().powerState()));
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
        p.shuffle().ifPresent(s -> updateState(CHANNEL_SHUFFLE, new StringType(s.name())));
        p.repeat().ifPresent(r -> updateState(CHANNEL_REPEAT, new StringType(r.name())));

        Optional<Integer> position = p.position();
        Optional<Integer> total = p.totalTime();
        updateState(CHANNEL_POSITION,
                position.isPresent() ? new QuantityType<>(position.get(), Units.SECOND) : UnDefType.UNDEF);
        updateState(CHANNEL_DURATION,
                total.isPresent() ? new QuantityType<>(total.get(), Units.SECOND) : UnDefType.UNDEF);
        if (position.isPresent() && total.isPresent() && total.get() > 0) {
            updateState(CHANNEL_PROGRESS,
                    new PercentType((int) Math.min(100, Math.round(100.0 * position.get() / total.get()))));
        }

        switch (p.deviceState()) {
            case Playing -> updateState(CHANNEL_MEDIA_CONTROL, PlayPauseType.PLAY);
            case Paused, Stopped, Idle -> updateState(CHANNEL_MEDIA_CONTROL, PlayPauseType.PAUSE);
            default -> {
            }
        }

        AppleTV atv = appleTV;
        if (atv != null && atv.features().inState(FeatureState.Available, FeatureName.App)) {
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

    private void startPolling() {
        cancel(pollJob);
        if (config.refreshInterval > 0) {
            pollJob = scheduler.scheduleWithFixedDelay(this::refreshAll, config.refreshInterval, config.refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    private void updateArtwork() {
        AppleTV atv = appleTV;
        if (atv == null || !isLinked(CHANNEL_ARTWORK)) {
            return;
        }
        // Skip the (potentially large) fetch when the artwork has not changed since the last update.
        try {
            String artworkId = atv.metadata().artworkId();
            if (artworkId.equals(lastArtworkId)) {
                return;
            }
            lastArtworkId = artworkId;
        } catch (RuntimeException e) {
            // artworkId is unsupported by this protocol; fall through and always fetch
            logger.trace("Artwork identifier unavailable, fetching unconditionally", e);
        }
        atv.metadata().artwork().thenAccept(artwork -> {
            ArtworkInfo info = artwork;
            if (info.bytes().length > 0) {
                updateState(CHANNEL_ARTWORK, new org.openhab.core.library.types.RawType(info.bytes(), info.mimetype()));
            }
        }).exceptionally(e -> {
            // reset so the next now-playing update retries the fetch
            lastArtworkId = null;
            return null;
        });
    }

    /**
     * Removes channels whose backing feature the connected device reports as unsupported. Channels
     * whose support is unknown or merely unavailable are kept, since they may become available once
     * something is playing or the device state changes.
     */
    private void pruneUnsupportedChannels(AppleTV atv) {
        List<Channel> keep = new ArrayList<>();
        List<String> removed = new ArrayList<>();
        for (Channel channel : thing.getChannels()) {
            FeatureName feature = CHANNEL_FEATURES.get(channel.getUID().getIdWithoutGroup());
            if (feature != null && atv.features().inState(FeatureState.Unsupported, feature)) {
                removed.add(channel.getUID().getId());
            } else {
                keep.add(channel);
            }
        }
        if (!removed.isEmpty()) {
            logger.debug("Removing unsupported channels {}", removed);
            updateThing(editThing().withChannels(keep).build());
        }
    }

    private void refreshDynamicOptions(AppleTV atv) {
        AtvStateDescriptionProvider provider = stateDescriptionProvider;
        if (provider == null) {
            return;
        }
        if (isLinked(CHANNEL_APP) && atv.features().inState(
                List.of(FeatureState.Available, FeatureState.Unknown, FeatureState.Unavailable), FeatureName.AppList)) {
            atv.apps().appList()
                    .thenAccept(apps -> provider.setStateOptions(new ChannelUID(thing.getUID(), CHANNEL_APP),
                            apps.stream().map(this::appOption).toList()))
                    .exceptionally(e -> {
                        logger.debug("Fetching app list failed", e);
                        return null;
                    });
        }
        if (isLinked(CHANNEL_ACCOUNT) && atv.features().inState(
                List.of(FeatureState.Available, FeatureState.Unknown, FeatureState.Unavailable),
                FeatureName.AccountList)) {
            atv.userAccounts().accountList()
                    .thenAccept(accounts -> provider.setStateOptions(new ChannelUID(thing.getUID(), CHANNEL_ACCOUNT),
                            accounts.stream().map(this::accountOption).toList()))
                    .exceptionally(e -> {
                        logger.debug("Fetching account list failed", e);
                        return null;
                    });
        }
    }

    private StateOption appOption(App app) {
        return new StateOption(app.identifier(), labelOrId(app.name(), app.identifier()));
    }

    private StateOption accountOption(UserAccount account) {
        return new StateOption(account.identifier(), labelOrId(account.name(), account.identifier()));
    }

    private String labelOrId(@Nullable String label, String identifier) {
        return label == null || label.isBlank() ? identifier : label;
    }

    private void clearDynamicOptions() {
        AtvStateDescriptionProvider provider = stateDescriptionProvider;
        if (provider != null) {
            provider.removeStateOptions(new ChannelUID(thing.getUID(), CHANNEL_APP));
            provider.removeStateOptions(new ChannelUID(thing.getUID(), CHANNEL_ACCOUNT));
        }
    }

    private @Nullable AtvStateDescriptionProvider lookupStateDescriptionProvider() {
        try {
            BundleContext context = FrameworkUtil.getBundle(AtvHandler.class).getBundleContext();
            ServiceReference<AtvStateDescriptionProvider> reference = context
                    .getServiceReference(AtvStateDescriptionProvider.class);
            return reference != null ? context.getService(reference) : null;
        } catch (RuntimeException e) {
            logger.debug("State description provider unavailable; dynamic options disabled", e);
            return null;
        }
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

    private String pinKey(Protocol protocol) {
        return switch (protocol) {
            case AirPlay -> CONFIG_AIRPLAY_PIN;
            case Companion -> CONFIG_COMPANION_PIN;
            case RAOP -> CONFIG_RAOP_PIN;
            default -> CONFIG_AIRPLAY_PIN;
        };
    }

    /** Clears the pairing PIN fields once the device is fully paired and online; they are spent by then. */
    private void clearPins() {
        if (config.airplayPin.isBlank() && config.companionPin.isBlank() && config.raopPin.isBlank()) {
            return;
        }
        Configuration updated = editConfiguration();
        updated.put(CONFIG_AIRPLAY_PIN, "");
        updated.put(CONFIG_COMPANION_PIN, "");
        updated.put(CONFIG_RAOP_PIN, "");
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

    private void scheduleReconnect() {
        cancel(reconnectJob);
        reconnectJob = scheduler.schedule(this::connect, RECONNECT_SECONDS, TimeUnit.SECONDS);
    }

    private void cancel(@Nullable ScheduledFuture<?> job) {
        if (job != null) {
            job.cancel(true);
        }
    }

    private <T> T await(CompletableFuture<T> future) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(OP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
